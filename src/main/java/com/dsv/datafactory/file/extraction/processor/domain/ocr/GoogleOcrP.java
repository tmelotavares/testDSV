package com.dsv.datafactory.file.extraction.processor.domain.ocr;

import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.logging.ECSLoggerProvider;
import com.dsv.datafactory.file.extraction.processor.models.BoundingPoly;
import com.dsv.datafactory.file.extraction.processor.models.EntityAnnotation;
import com.dsv.datafactory.file.extraction.processor.models.GooglePage;
import com.dsv.datafactory.file.extraction.processor.models.GoogleVisionResponse;
import com.dsv.datafactory.model.*;
import com.dsv.datafactory.model.Word;
import com.dsv.logger.ECSLogger;

import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.Image;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GoogleOcrP {

    private final ECSLogger logger = ECSLoggerProvider.getLogger(GoogleOcr.class.getName());
    Feature feature = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build() ;
    private final Boolean runGVInPararell;

    @Inject
    public GoogleOcrP (Config config){
        this.runGVInPararell = config.runGVInPararell.equals("true");
    }
    public Document generateDocument(ArrayList<String>listOfPathImgs, String documentKey) throws IOException {
        Document document = new Document();
        try{
            logger.info("Starting GoogleOcr "+ documentKey);
            logger.info("Feature type set to " + feature.toString());

            document.setPages(extractAllPages(listOfPathImgs));
            // need to be able to maintain order here - if there are duplicate pages then using response.getIndex of in stream does not work
            AtomicInteger count = new AtomicInteger(0);
            document.getPages().forEach(x-> {
                count.getAndIncrement();
                x.setPageKey(documentKey+(count.get()-1));
                x.setPageNumber(count.get()-1);
            });
            document.setKey(documentKey);
            // remove null pages
            document.setPages(document.getPages().parallelStream().filter(x->x.getLanguage()!=null).collect(Collectors.toList()));
        }
        catch (Exception e) {
            // Log error (since IOException cannot be thrown by a Cloud Function)
            logger.error("Error in Document generation" + e.getMessage(), e);}
        return document;
    }

    public List<AnnotateImageResponse> extractFullDocumentResponse(List<AnnotateImageRequest>requests) throws IOException {
        List<AnnotateImageResponse> responses;
        int gvThreshold = 15;
        if (this.runGVInPararell){
            logger.info("Running GV calls in parallel");
            responses = requests.parallelStream().map(x -> {
                try {
                    return retrieveAnnotatedImageResponse(Collections.singletonList(x));
                } catch (Exception e) {
                    e.printStackTrace();
                }return null;
            }).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
        }
        else if (requests.size() >= gvThreshold) {
            logger.info("Chunking requests list to threshold size " + gvThreshold + "Original size of requests was "+ requests.size());
            responses= Lists.partition(requests, gvThreshold).parallelStream().map(x -> {
                try {
                    return retrieveAnnotatedImageResponseAll(x);
                } catch (Exception e) {
                    logger.error("Could not retrieve annotated image response for request");
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).flatMap(Collection::parallelStream).collect(Collectors.toList());
        }
        else {
            responses = retrieveAnnotatedImageResponseAll(requests);
        }
    return responses;}



    public AnnotateImageResponse retrieveAnnotatedImageResponse(List<AnnotateImageRequest> requests) throws IOException {
        try{
            ImageAnnotatorClient client = ImageAnnotatorClient.create();
            AnnotateImageResponse response = client.batchAnnotateImages(requests).getResponses(0);
            client.close();
            return response;
        }

        catch (Exception e){
            logger.warn(e.getMessage());
        }
        return null;
    }

    public ArrayList<Image> generateImages(ArrayList<String> pathImgs){
       return pathImgs.parallelStream().map(x-> {
            try {
                return this.processImg(x);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("Could not load image from path " + x);
            }return null;
        }).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Page> extractAllPages (ArrayList<String> pathImgs) throws IOException {
        ArrayList<Image> images = generateImages(pathImgs);
        List<AnnotateImageRequest>requests =  bulkGeneratePngRequest(images, this.feature);
        List<AnnotateImageResponse> responses =  extractFullDocumentResponse(requests);
        return generatePagesFromResponses(responses);
    }

    public ArrayList<Page> generatePagesFromResponses (List<AnnotateImageResponse> responses) {
        return responses.parallelStream().map(this::extractPage).collect(Collectors.toCollection(ArrayList::new));
    }

    public com.dsv.datafactory.model.Page extractPage(AnnotateImageResponse response){
        Page page = new Page();
        if (response.getFullTextAnnotation().getPagesCount() != 0) {
            GoogleVisionResponse response2 = new OcrParser(response).parse();
            GooglePage googlePage = retrieveGooglePageInformation(response2);
            page.setHeight(googlePage.getHeight());
            page.setWidth(googlePage.getWidth());
            logger.info("Set page height and width " + page.getHeight() + " " + page.getWidth());
            page.setLanguage(this.retrieveLanguagesFromPage(googlePage));
            List<EntityAnnotation> annotations = retrieveAnnotationsList(response2);
            List<Line> lines = generateLineArray(annotations);
            page.setRotation(getPageRotation(lines.get(0).getWords()));
            page.setLines(lines);
            if (page.getRotation() != 0) correctPageCoordinates(page);
        }
        return page;
    }

    public List<Line> generateLineArray(List<EntityAnnotation> annotations){
        logger.info("Generating lines");
        annotations.remove(0);
        List<Word> allWords = annotations.parallelStream().map(this::generateWord).collect(Collectors.toCollection(ArrayList::new));
        Line line = new Line();
        line.setWords(allWords);
        return new ArrayList<>(Collections.singletonList(line));
    }
    //FIXME: if the method needs comments, it is not clear enough
    /**
     Checking the page rotation based on the distribution of words rotation
     **/
    public int getPageRotation(List<Word> words){
        int[] rotations = {0, 0, 0, 0};
        words.parallelStream().forEach(x->++rotations[x.getRotation()/90]);
        int maxRotation = 0;
        for(int i = 1; i < 4; ++i){
            if(rotations[i]>rotations[maxRotation]) maxRotation = i;
        }
        return maxRotation*90;
    }
    //FIXME: if the method needs comments, it is not clear enough
    /**
     Checking the page rotation based on the distribution of words rotation
     **/
    public void correctPageCoordinates(com.dsv.datafactory.model.Page page){

        if(page.getRotation() == 90 | page.getRotation() == 270){
            int height = page.getHeight();
            int width = page.getWidth();
            page.setWidth(height);
            page.setHeight(width);
        }
        page.getLines().get(0).getWords().parallelStream().forEach(x-> correctWordCoordinates(x,page.getRotation(),page.getHeight(),page.getWidth()));
    }
    //FIXME: if the method needs comments, it is not clear enough
    /**
     Transpose word coordinates sot that it reflect 0deg orientation of page
     */
    public void correctWordCoordinates(Word word, int rotation, int height, int width){
        int minX = word.getBoundingBox().getX1();
        int maxX = word.getBoundingBox().getX2();
        int minY = word.getBoundingBox().getY1();
        int maxY = word.getBoundingBox().getY2();
        int x1, x2, y1, y2;
        if(rotation == 90){
            x1 = minY;
            x2 = maxY;
            y1 = height - maxX;
            y2 = height - minX;
        }
        else if(rotation == 180){
            x1 = width - maxX;
            x2 = width - minX;
            y1 = height - maxY;
            y2 = height - minY;
        }
        else if(rotation == 270){
            x1 = width - maxY;
            x2 = width - minY;
            y1 = minX;
            y2 = maxX;
        } else return;
        word.setTopLeftCorner(new Vertices(x1, y1));
        word.setTopRightCorner(new Vertices(x2, y1));
        word.setLowRightCorner(new Vertices(x2, y2));
        word.setLowLeftCorner(new Vertices(x1, y2));
        word.setxMean(caclulateMean(word.getTopLeftCorner().getX(),word.getLowRightCorner().getX()));
        word.setyMean(caclulateMean(word.getTopLeftCorner().getY(),word.getLowRightCorner().getY()));
        word.setBoundingBox(new BoundingBox(x1, x2, y1, y2));
        word.setRotation(word.getRotation() - rotation);
    }
    //FIXME: if the method needs comments, it is not clear enough
    /**
     Get Min and Max X/Y coordinates from the bounginPoly for a word.
     **/
    public int[] getMinMaxCoordinatesFromVertices(BoundingPoly vertices){
        int minX = vertices.getVertices().get(0).getX();
        int maxX = vertices.getVertices().get(0).getX();
        int minY = vertices.getVertices().get(0).getY();
        int maxY = vertices.getVertices().get(0).getY();

        for(int i = 1; i < 4; ++i){
            int x = vertices.getVertices().get(i).getX();
            int y = vertices.getVertices().get(i).getY();

            if(x>maxX) maxX=x;
            if(x<minX) minX=x;
            if(y>maxY) maxY=y;
            if(y<minY) minY=y;
        }

        if(minX<0) minX = 0;
        if(minY<0) minY = 0;
        return new int[] {minX, minY, maxX, maxY};
    }

    /**
     Normalizing the bounding box coordinates.
     (making sure that the bounding box is a rectangle)
     **/
    //FIXME: if the method needs comments, it is not clear enough
    public Vertices createWordVertice(Vertices vertex, int[] minMaxCoordinates){
        int minX = minMaxCoordinates[0];
        int maxX = minMaxCoordinates[2];
        int minY = minMaxCoordinates[1];
        int maxY = minMaxCoordinates[3];
        int meanX = (minX+maxX)/2;
        int meanY = (minY+maxY)/2;
        int resX = 0;
        int resY = 0;
        if(vertex.getX() >= meanX) resX = maxX;
        else resX = minX;
        if(vertex.getY() >= meanY) resY = maxY;
        else resY = minY;

        return new Vertices(resX, resY);
    }

    public Vertices createWordVertice(Vertices vertex, int[] minMaxCoordinates){
        int minX = minMaxCoordinates[0];
        int maxX = minMaxCoordinates[2];
        int minY = minMaxCoordinates[1];
        int maxY = minMaxCoordinates[3];
        int meanX = (minX + maxX) / 2;
        int meanY = (minY + maxY) / 2;
        int resX = vertex.getX() >= meanX ? maxX : minX;
        int resY = vertex.getY() >= meanY ? maxY : minY;

        return new Vertices(resX, resY);
    }

    /**
     Check the words rotation based on the order of boundingBox vertices in the response.
     More info about the algorithm in google vision api documentation.
     **/
    //FIXME: if the method needs comments, it is not clear enough
    public int getWordsRotation(com.dsv.datafactory.model.Word word, int[] minMaxCoordinates){
        int minX = minMaxCoordinates[0];
        int maxX = minMaxCoordinates[2];
        int minY = minMaxCoordinates[1];
        int maxY = minMaxCoordinates[3];

        if(word.getTopLeftCorner().getX() == minX & word.getTopLeftCorner().getY() == minY) return 0;
        else if(word.getTopLeftCorner().getX() == minX & word.getTopLeftCorner().getY() == maxY) return 270;
        else if(word.getTopLeftCorner().getX() == maxX & word.getTopLeftCorner().getY() == minY) return 90;
        else return 180;
    }

    public Word generateWord(EntityAnnotation textPlusCoordinate){

        String description = textPlusCoordinate.getDescription();
        BoundingPoly bounding = textPlusCoordinate.getBoundingPoly();

        Vertices topLeft = bounding.getVertices().get(0);
        Vertices topRight = bounding.getVertices().get(1);
        Vertices lowRight = bounding.getVertices().get(2);
        Vertices lowLeft = bounding.getVertices().get(3);

        int[] minMaxCoordinates = getMinMaxCoordinatesFromVertices(bounding);

        Word word = new Word();
        word.setWord(description);
        word.setTopLeftCorner(createWordVertice(topLeft, minMaxCoordinates));
        word.setTopRightCorner(createWordVertice(topRight, minMaxCoordinates));
        word.setLowRightCorner(createWordVertice(lowRight, minMaxCoordinates));
        word.setLowLeftCorner(createWordVertice(lowLeft, minMaxCoordinates));
        word.setxMean(caclulateMean(word.getTopLeftCorner().getX(),word.getLowRightCorner().getX()));
        word.setyMean(caclulateMean(word.getTopLeftCorner().getY(),word.getLowRightCorner().getY()));
        word.setBoundingBox(new BoundingBox(minMaxCoordinates[0], minMaxCoordinates[2], minMaxCoordinates[1], minMaxCoordinates[3]));
        word.setRotation(getWordsRotation(word, minMaxCoordinates));
        return word;
    }

    public int caclulateMean(int cord1,int cord2){
        return (cord1+cord2)/2;
    }

    public List<Language> retrieveLanguagesFromPage(GooglePage googlePage){
        List<Language> languages = new ArrayList<>();
        googlePage.getTextProperty().getDetectedLanguages().parallelStream().forEach(l -> languages.add(new Language(l.getConfidence(), l.getLanguageCode())));
        return languages;
    }

    public ArrayList<EntityAnnotation> retrieveAnnotationsList(GoogleVisionResponse imageResponse){
        return imageResponse.getTextAnnotations();}

    public GooglePage retrieveGooglePageInformation(GoogleVisionResponse response){
        return response.getFullTextAnnotation().getPages().get(0);
    }


    public List<AnnotateImageResponse> retrieveAnnotatedImageResponseAll(List<AnnotateImageRequest> requests) throws IOException {
        try{
            ImageAnnotatorClient client = ImageAnnotatorClient.create();
            List<AnnotateImageResponse> response = client.batchAnnotateImages(requests).getResponsesList();
            client.close();
            return response;
        }

        catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }



    public List<AnnotateImageRequest> generatePngRequest(Image image, Feature feature){
        List<AnnotateImageRequest> requests = new ArrayList<>();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
        requests.add(request);
        return requests;
    }

    public List<AnnotateImageRequest> bulkGeneratePngRequest(ArrayList<Image> images, Feature feature){
        return images.parallelStream().flatMap(x-> generatePngRequest(x,feature).stream()).collect(Collectors.toList());
    }

    public Image processImg(String pathToImage) throws IOException {
        logger.info("Processing images");
        File file = new File(pathToImage);
        Path path = Paths.get(file.getAbsolutePath());
        byte[] data = Files.readAllBytes(path);
        ByteString imgBytes = ByteString.copyFrom(data);
        return Image.newBuilder().setContent(imgBytes).build();
    }





}
