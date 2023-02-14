package com.dsv.datafactory.file.extraction.processor.domain.ocr;

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
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleOcr {
    
    private final ECSLogger logger = ECSLoggerProvider.getLogger(GoogleOcr.class.getName());
    Feature feature = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build() ;


    public Document generateDocument(ArrayList<String>listOfPathImgs, String documentKey) throws IOException {
        Document document = new Document();
        try{
        logger.info("Starting GoogleOcr "+ documentKey);
        logger.info("Feature type set to " + feature.toString());

        document.setPages(extractPages(listOfPathImgs,documentKey));
        document.setKey(documentKey);
        }
        catch (Exception e) {
            // Log error (since IOException cannot be thrown by a Cloud Function)
            logger.error("Error in Document generation" + e.getMessage(), e);}
        return document;
    }

    public List<com.dsv.datafactory.model.Page> extractPages(ArrayList<String> listOfPathImgs, String documentKey) throws IOException {
        List<com.dsv.datafactory.model.Page> pages = new ArrayList<>();
        for (int i = 0; i < listOfPathImgs.size(); i++){
            pages.add(extractPage(listOfPathImgs.get(i),i,documentKey+i));}
        return pages;
    }

    public com.dsv.datafactory.model.Page extractPage(String pathToImg, Integer pageNumber, String pageKey) throws IOException {
        try {
            com.dsv.datafactory.model.Page page = new com.dsv.datafactory.model.Page();
            page.setPageNumber(pageNumber);
            page.setPageKey(pageKey);
            AnnotateImageResponse rawResponse = generateResponseFromImage(pathToImg);
            GoogleVisionResponse response = new OcrParser(rawResponse).parse();
            assert(response!=null);
            GooglePage googlePage = retrieveGooglePageInformation(response);
            page.setHeight(googlePage.getHeight());
            page.setWidth(googlePage.getWidth());
            logger.info("Set page height and width " + page.getHeight() + " " + page.getWidth());
            page.setLanguage(this.retrieveLanguagesFromPage(googlePage));
            List<EntityAnnotation> annotations = retrieveAnnotationsList(response);
            List<Line> lines = generateLineArray(annotations);
            page.setRotation(getPageRotation(lines.get(0).getWords()));
            page.setLines(lines);

            if(page.getRotation() != 0) correctPageCoordinates(page);

            return page;
        }
        catch (AssertionError e){
            logger.error("Page either contains no text or there was error in GoogleOCR"+e.getMessage());
        }
        catch (Exception e){
            logger.error("Error extracting OCR "+e.getMessage());
        }
        return null;
    }

    public AnnotateImageResponse generateResponseFromImage(String pathToImg) throws IOException {
        logger.info("Sending request to google vision");
        Image processedImage = processImg(pathToImg);
        List<AnnotateImageRequest> requests = generatePngRequest(processedImage,this.feature);
        if (retrieveAnnotatedImageResponse(requests) == null || !retrieveAnnotatedImageResponse(requests).hasFullTextAnnotation()) {
            logger.error(String.format("Image %s contains no text", pathToImg));
            return null;
        }
        if (retrieveAnnotatedImageResponse(requests).hasError()) {
            // Log error
            logger.error(
                    "Error in vision API call: " + retrieveAnnotatedImageResponse(requests).getError().getMessage());
            return null;
        }
        return retrieveAnnotatedImageResponse(requests);
    }

    public List<Line> generateLineArray(List<EntityAnnotation> annotations){
        logger.info("Generating lines");
        List<Line> lines = new ArrayList<>();
        Line line = new Line();
        for (int j = 0; j < annotations.size(); j++) {

            if (j == 0) {
                System.out.println(annotations.get(j));
            } else {
                 line.addWord(generateWord(annotations.get(j)));
            }
        }
        lines.add(line);
        return lines;
    }

    /**
     Checking the page rotation based on the distribution of words rotation
     **/
    public int getPageRotation(List<Word> words){
        int[] rotations = {0, 0, 0, 0};
        for(Word word:words){
            ++rotations[word.getRotation()/90];
        }
        int maxRotation = 0;
        for(int i = 1; i < 4; ++i){
            if(rotations[i]>rotations[maxRotation]) maxRotation = i;
        }
        return maxRotation*90;
    }

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

        for(Word word : page.getLines().get(0).getWords()){
            correctWordCoordinates(word, page.getRotation(), page.getHeight(), page.getWidth());
        }

    }

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
        int[] result = {minX, minY, maxX, maxY};
        return result;
    }

    /**
     Normalizing the bounding box coordinates.
     (making sure that the bounding box is a rectangle)
     **/
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

    /**
     Check the words rotation based on the order of boundingBox vertices in the response.
     More info about the algorithm in google vision api documentation.
     **/
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

//               float confidence = textPlusCoordinate.getScore();
//                logger.info("Generating words from entity annotations");
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
        googlePage.getTextProperty().getDetectedLanguages().forEach(l -> languages.add(new Language(l.getConfidence(), l.getLanguageCode())));
        return languages;
    }

    public ArrayList<EntityAnnotation> retrieveAnnotationsList(GoogleVisionResponse imageResponse){
            return imageResponse.getTextAnnotations();}

    public GooglePage retrieveGooglePageInformation(GoogleVisionResponse response){
        return response.getFullTextAnnotation().getPages().get(0);
    }

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

    public List<AnnotateImageResponse> retrieveAnnotatedImageResponseAll(List<AnnotateImageRequest> requests) throws IOException {
        try{
            ImageAnnotatorClient client = ImageAnnotatorClient.create();
            List<AnnotateImageResponse> response = client.batchAnnotateImages(requests).getResponsesList();
            client.close();
            return response;
        }

        catch (Exception e){
            logger.warn(e.getMessage());
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
