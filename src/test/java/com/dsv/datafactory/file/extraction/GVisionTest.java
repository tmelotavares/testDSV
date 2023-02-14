package com.dsv.datafactory.file.extraction;

import com.dsv.datafactory.file.extraction.processor.models.*;
import com.dsv.datafactory.model.Document;
import com.dsv.datafactory.model.Language;
import com.dsv.datafactory.model.Line;
import com.dsv.datafactory.model.Vertices;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Page;
import com.google.common.hash.Hashing;
import com.google.protobuf.ByteString;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Disabled // Disabled due to depending on developer's local files
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GVisionTest
{
    private Path basePath = Paths.get("src", "test", "resources", "images");

    private static final Logger logger = Logger.getLogger(GVisionTest.class.getName());


    @Test
    void testCreateDocument() throws IOException
    {

        Document document = new Document();
        List<com.dsv.datafactory.model.Page> pages = new ArrayList<>();

        File dir = new File("C:\\Users\\Peter.S.Larsen\\OneDrive - DSV\\DNA-495-TEST\\invoice_5");
        File[] directoryListing = dir.listFiles();

        for (int i = 0; i < directoryListing.length; i++) {

            File file = directoryListing[i];

            if (file.getName().toLowerCase().endsWith(".pdf")) {
                document.setKey(file.getName());
            }

            if (file.getName().toLowerCase().endsWith(".png")) {
                System.out.println(file.getName());
                String imgName = file.getName();


                Path path = Paths.get(file.getAbsolutePath());
                byte[] data = Files.readAllBytes(path);
                ByteString imgBytes = ByteString.copyFrom(data);

                // Builds the image annotation request
                List<AnnotateImageRequest> requests = new ArrayList<>();
                Image img = Image.newBuilder().setContent(imgBytes).build();
                Feature feat = Feature.newBuilder().setType(Type.DOCUMENT_TEXT_DETECTION).build();
                AnnotateImageRequest request =
                        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
                requests.add(request);

                // Detect text in an image using the Cloud Vision API
                AnnotateImageResponse visionResponse;

                try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
                    com.dsv.datafactory.model.Page page = new com.dsv.datafactory.model.Page();
                    page.setPageKey(imgName);
                    page.setPageNumber(i);

                    List<Line> lines = new ArrayList<>();
                    Line line = new Line();


                    visionResponse = client.batchAnnotateImages(requests).getResponses(0);
                    List<EntityAnnotation> textPlusCoordinates = visionResponse.getTextAnnotationsList();

                    Page gVpage = visionResponse.getFullTextAnnotation().getPages(0);
                    List<Language> languages = new ArrayList<>();
                    gVpage.getProperty().getDetectedLanguagesList().forEach(l -> languages.add(new Language(l.getConfidence(), l.getLanguageCode())));

                    page.setHeight(gVpage.getHeight());
                    page.setWidth(gVpage.getWidth());
                    page.setLanguage(languages);

                    for (int j = 0; j < textPlusCoordinates.size(); j++) {

                        if (j == 0) {
                            continue;

                        } else {

                            EntityAnnotation textPlusCoordinate = textPlusCoordinates.get(j);
                            float confidence = textPlusCoordinate.getScore();
                            String description = textPlusCoordinate.getDescription();
                            BoundingPoly bounding = textPlusCoordinate.getBoundingPoly();

                            Vertex topLeft = bounding.getVertices(0);
//                            Vertex topRight = bounding.getVertices(1);
                            Vertex lowRight = bounding.getVertices(2);
//                            Vertex lowLeft = bounding.getVertices(3);

                            com.dsv.datafactory.model.Word word = new com.dsv.datafactory.model.Word();
                            word.setWord(description);
                            word.setTopLeftCorner(new Vertices(topLeft.getX(), topLeft.getY()));
                            word.setLowRightCorner(new Vertices(lowRight.getX(), lowRight.getY()));
                            line.addWord(word);
                        }

                    }

                    if (visionResponse == null || !visionResponse.hasFullTextAnnotation()) {
                        logger.info(String.format("Image %s contains no text", imgName));
                        return;
                    }

                    if (visionResponse.hasError()) {
                        // Log error
                        logger.log(
                                Level.SEVERE, "Error in vision API call: " + visionResponse.getError().getMessage());
                        return;
                    }


                    lines.add(line);
                    page.setLines(lines);
                    pages.add(page);


                } catch (IOException e) {
                    // Log error (since IOException cannot be thrown by a Cloud Function)
                    logger.log(Level.SEVERE, "Error detecting text: " + e.getMessage(), e);
                    return;
                }
            }
        }


        document.setPages(pages);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(Paths.get(basePath.toString(),document.getKey()+".json").toString()), document);
        String jsonString = mapper.writeValueAsString(document);
        System.out.println(jsonString);
    }



    @Test
    void testGetTextFromPng() throws IOException
    {

        String folder = "invoice_1";
        List<String> pages = new ArrayList<>();

        File dir = new File("C:\\Users\\Peter.S.Larsen\\OneDrive - DSV\\DNA-495-TEST\\"+ folder);
        File[] directoryListing = dir.listFiles();

        for (int i = 0; i < directoryListing.length; i++) {

            File file = directoryListing[i];

            if (file.getName().toLowerCase().endsWith(".png")) {

                Path path = Paths.get(file.getAbsolutePath());
                byte[] data = Files.readAllBytes(path);
                ByteString imgBytes = ByteString.copyFrom(data);

                // Builds the image annotation request
                List<AnnotateImageRequest> requests = new ArrayList<>();
                Image img = Image.newBuilder().setContent(imgBytes).build();
                Feature feat = Feature.newBuilder().setType(Type.DOCUMENT_TEXT_DETECTION).build();
                AnnotateImageRequest request =
                        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
                requests.add(request);

                // Detect text in an image using the Cloud Vision API
                AnnotateImageResponse visionResponse;

                try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {

                    visionResponse = client.batchAnnotateImages(requests).getResponses(0);
                    String txt = visionResponse.getFullTextAnnotation().getText();
                    pages.add(txt);

                } catch (IOException e) {
                    // Log error (since IOException cannot be thrown by a Cloud Function)
                    logger.log(Level.SEVERE, "Error detecting text: " + e.getMessage(), e);
                    return;
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(Paths.get(basePath.toString(),folder+".json").toString()), pages);
        String jsonString = mapper.writeValueAsString(pages);
        System.out.println(jsonString);
    }

    @Test
    void testBlankPages() throws IOException
    {

        Document document = new Document();
        List<com.dsv.datafactory.model.Page> pages = new ArrayList<>();

        File dir = new File("H:\\training_data_google\\DNA-619\\test_imgs\\");
        File[] directoryListing = dir.listFiles();

        for (int i = 0; i < directoryListing.length; i++) {

            File file = directoryListing[i];

            if (file.getName().toLowerCase().endsWith(".pdf")) {
                document.setKey(file.getName());
            }

            if (file.getName().toLowerCase().endsWith(".png")) {
                System.out.println(file.getName());
                String imgName = file.getName();


                Path path = Paths.get(file.getAbsolutePath());
                byte[] data = Files.readAllBytes(path);
                ByteString imgBytes = ByteString.copyFrom(data);

                // Builds the image annotation request
                List<AnnotateImageRequest> requests = new ArrayList<>();
                Image img = Image.newBuilder().setContent(imgBytes).build();
                Feature feat = Feature.newBuilder().setType(Type.DOCUMENT_TEXT_DETECTION).build();
                AnnotateImageRequest request =
                        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
                requests.add(request);

                // Detect text in an image using the Cloud Vision API
                AnnotateImageResponse visionResponse;

                try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
                    com.dsv.datafactory.model.Page page = new com.dsv.datafactory.model.Page();
                    page.setPageKey(imgName);
                    page.setPageNumber(i);

                    List<Line> lines = new ArrayList<>();
                    Line line = new Line();


                    visionResponse = client.batchAnnotateImages(requests).getResponses(0);
                    List<EntityAnnotation> textPlusCoordinates = visionResponse.getTextAnnotationsList();
                    try {
                        Page gVpage = visionResponse.getFullTextAnnotation().getPages(0);
                        List<Language> languages = new ArrayList<>();
                        gVpage.getProperty().getDetectedLanguagesList().forEach(l -> languages.add(new Language(l.getConfidence(), l.getLanguageCode())));

                        page.setHeight(gVpage.getHeight());
                        page.setWidth(gVpage.getWidth());
                        page.setLanguage(languages);

                        for (int j = 0; j < textPlusCoordinates.size(); j++) {

                            if (j == 0) {
                                continue;

                            } else {

                                EntityAnnotation textPlusCoordinate = textPlusCoordinates.get(j);
                                float confidence = textPlusCoordinate.getScore();
                                String description = textPlusCoordinate.getDescription();
                                BoundingPoly bounding = textPlusCoordinate.getBoundingPoly();

                                Vertex topLeft = bounding.getVertices(0);
                                //                            Vertex topRight = bounding.getVertices(1);
                                Vertex lowRight = bounding.getVertices(2);
                                //                            Vertex lowLeft = bounding.getVertices(3);

                                com.dsv.datafactory.model.Word word = new com.dsv.datafactory.model.Word();
                                word.setWord(description);
                                word.setTopLeftCorner(new Vertices(topLeft.getX(), topLeft.getY()));
                                word.setLowRightCorner(new Vertices(lowRight.getX(), lowRight.getY()));
                                line.addWord(word);
                            }

                        }

                        if (visionResponse == null || !visionResponse.hasFullTextAnnotation()) {
                            logger.info(String.format("Image %s contains no text", imgName));
                            return;
                        }

                        if (visionResponse.hasError()) {
                            // Log error
                            logger.log(
                                    Level.SEVERE, "Error in vision API call: " + visionResponse.getError().getMessage());
                            return;
                        }


                        lines.add(line);
                        page.setLines(lines);
                    }catch (IndexOutOfBoundsException e) {
                        // Log error (since IOException cannot be thrown by a Cloud Function)
                        logger.log(Level.WARNING, "Error detecting text, probable blank page: " + e.getMessage(), e);

                    }
                    pages.add(page);


                } catch (IOException e) {
                    // Log error (since IOException cannot be thrown by a Cloud Function)
                    logger.log(Level.SEVERE, "Error detecting text: " + e.getMessage(), e);
                    return;
                }
            }
        }


        document.setPages(pages);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(Paths.get(basePath.toString(),document.getKey()+".json").toString()), document);
        String jsonString = mapper.writeValueAsString(document);
        System.out.println(jsonString);
    }

    @Test
    void testGetTextFromPdf() throws IOException
    {

        String folder = "invoice_1";
        List<String> pages = new ArrayList<>();

        File dir = new File("C:\\Users\\Peter.S.Larsen\\OneDrive - DSV\\DNA-495-TEST\\"+ folder);
        File[] directoryListing = dir.listFiles();

        for (int i = 0; i < directoryListing.length; i++) {

            File file = directoryListing[i];

            if (file.getName().toLowerCase().endsWith(".pdf")) {

                Path path = Paths.get(file.getAbsolutePath());
                byte[] data = Files.readAllBytes(path);
                ByteString content = ByteString.copyFrom(data);

                InputConfig inputConfig =
                        InputConfig.newBuilder().setMimeType("application/pdf").setContent(content).build();

                try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
                    Feature feature = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();

                    // Build the request object for that one file. Note: for additional file you have to create
                    // additional `AnnotateFileRequest` objects and store them in a list to be used below.
                    // Since we are sending a file of type `application/pdf`, we can use the `pages` field to
                    // specify which pages to process. The service can process up to 5 pages per document file.
                    // https://cloud.google.com/vision/docs/reference/rpc/google.cloud.vision.v1#google.cloud.vision.v1.AnnotateFileRequest
                    AnnotateFileRequest fileRequest =
                            AnnotateFileRequest.newBuilder()
                                    .setInputConfig(inputConfig)
                                    .addFeatures(feature)
                                    .build();

                    // Add each `AnnotateFileRequest` object to the batch request.
                    BatchAnnotateFilesRequest request =
                            BatchAnnotateFilesRequest.newBuilder().addRequests(fileRequest).build();

                    // Make the synchronous batch request.
                    BatchAnnotateFilesResponse response = client.batchAnnotateFiles(request);

                    AnnotateFileResponse annotateFileResponse = response.getResponses(0);

                    for (AnnotateImageResponse annotateImageResponse : annotateFileResponse.getResponsesList()) {

                        String txt = annotateImageResponse.getFullTextAnnotation().getText();
                        pages.add(txt);
                    }


                }


            }
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(Paths.get(basePath.toString(),folder+"_pdf"+".json").toString()), pages);
        String jsonString = mapper.writeValueAsString(pages);
        System.out.println(jsonString);
    }


    @Test
    void testCompareResults() throws IOException, NoSuchAlgorithmException
    {
        ObjectMapper objectMapper = new ObjectMapper();

        byte[] bytesPNG = Files.readAllBytes(Paths.get(basePath.toString(), "invoice_1.json"));
        byte[] bytesPDF = Files.readAllBytes(Paths.get(basePath.toString(), "invoice_1_pdf.json"));

        String txtPNG = new String(bytesPNG);
        String txtPDF = new String(bytesPDF);

        List<String> pagesPNG = objectMapper.readValue(txtPNG, List.class);
        List<String> pagesPDF = objectMapper.readValue(txtPDF, List.class);

        int count = 0;

        for (int i = 0; i < pagesPNG.size(); i++) {


            String png = pagesPNG.get(i);
            String pdf = pagesPDF.get(i);

            String hashPNG = Hashing.sha256().hashBytes(png.getBytes()).toString();
            String hashPDF = Hashing.sha256().hashBytes(pdf.getBytes()).toString();

           Set<String> pagePng = new HashSet<>(Arrays.asList(png.split("\n")));
           Set<String> pagePdf = new HashSet<>(Arrays.asList(pdf.split("\n")));

           System.out.println("page " + i);

           System.out.println("Before ");
           System.out.println("page size png: " + pagePng.size());
           System.out.println("page size pdf: " + pagePdf.size());

           System.out.println();

//           pagePdf.retainAll(pagePng);
//           pagePng.retainAll(pagePdf);

//           pagePng.removeAll(pagePdf);
           pagePdf.removeAll(pagePng);

//            System.out.println("After");
//            System.out.println("page size png: " + pagePng.size());
//            System.out.println("page size pdf: " + pagePdf.size());
//
//
//            System.out.println("page png checksum256: " + hashPNG);
//            System.out.println("page pdf checksum256: " + hashPDF);
//
//
//            System.out.println();

//            System.out.println("elements in png page not found in pdf page");
//
//            for (String s : pagePng) {
//
//                System.out.println(s);
//
//            }
//
//            System.out.println();

            System.out.println("elements in pdf page not found in png page");

            for (String s : pagePdf) {

                System.out.println(s);

            }


            System.out.println();



        }





    }

}



