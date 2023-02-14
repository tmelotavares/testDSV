package com.dsv.datafactory.file.extraction;

import com.dsv.datafactory.model.*;
import com.dsv.datafactory.model.Word;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Image;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.ByteString;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Disabled // disabled due to referencing unusable test_folder directory
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LineExtractionTest {
    private Path basePath = Paths.get("src", "test", "resources", "images");

    private static final Logger logger = Logger.getLogger(GVisionTest.class.getName());
    String lineServiceUrl = "http://localhost:8005/jenks/clustering";
    String startNumberOfClasses = "10";
    String goodnessOfFit = "0.999";
    String test_folder = "H:\\training_data_google\\pipelineTest\\";

    @Test
    void testLineExtraction() throws IOException {

        Document document = new Document();
        List<com.dsv.datafactory.model.Page> pages = new ArrayList<>();

        File dir = new File(test_folder);
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
                Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
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

                    com.google.cloud.vision.v1.Page gVpage = visionResponse.getFullTextAnnotation().getPages(0);
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
                            word.setyMean((int) (topLeft.getY() + lowRight.getY()) / 2);
                            word.setxMean((int) (topLeft.getX() + lowRight.getX()) / 2);
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
       generateInputFromDocument(document);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(Paths.get(basePath.toString(), document.getKey() + ".json").toString()), document);
        String jsonString = mapper.writeValueAsString(document);
        System.out.println(jsonString);
    }

    public void generateInputFromDocument(Document document) throws IOException, JsonProcessingException {
        try{
            for (com.dsv.datafactory.model.Page page:document.getPages()){
                JsonObject requestValue = new JsonObject();
                List<BoundingBox> wordBoxes = page.getLines().get(0).getWords().stream().map(Word::getBoundingBox).collect(Collectors.toList());

                List<String> strBoxes = new ArrayList<String>();
                for(BoundingBox box : wordBoxes){
                    strBoxes.add(box.serialize());
                }

                JsonArray values = JsonParser.parseString(strBoxes.toString()).getAsJsonArray();
                startNumberOfClasses = String.valueOf(Math.round(2* Math.log(values.size())));
                requestValue.add("values",values);
                requestValue.addProperty( "start_num_of_class",startNumberOfClasses);
                requestValue.addProperty("min_goodness_of_fit",goodnessOfFit);
                requestValue.addProperty("type", "customs");
                String results = submitRequest(requestValue.toString());
                parseResults(results,page);
                //System.out.println(results);
            }
        }catch(Exception e){
            logger.log(Level.SEVERE,e.getMessage());
        }}

    public void parseResults(String results, com.dsv.datafactory.model.Page originalPage){
        List<Line> newLines = new ArrayList<>();
        int originalNumWords =  originalPage.getLines().get(0).getWords().size();
        JenksResponse jenks = deserializeResponse(results);
        for (Cluster clust : jenks.getValues()){
            int lineNumber = clust.getLineNumber();
            List<Integer> meansForLine = clust.getYMeans();
            Line newLine = new Line();
            List<Word> newWords = originalPage.getLines().get(0).getWords().stream().filter(x->meansForLine.stream().anyMatch(i -> i.equals(x.getyMean()))).collect(Collectors.toList());
            newLine.setLineNumber(lineNumber);
            newLine.setWords(newWords);
            newLines.add(newLine);
        }
        if (!newLines.isEmpty() && originalNumWords == newLines.stream().map(Line::getWords).mapToLong(List::size).sum()){
            originalPage.setLines(newLines);
        }}

    String submitRequest(String jsonRequest) throws IOException {

        CloseableHttpClient httpClient = HttpClients.custom().build();
        //HttpPost post = new HttpPost(lineServiceUrl);
        HttpPost post = new HttpPost("http://localhost:8892/jenks/clustering");
        StringBody json = new StringBody(jsonRequest, ContentType.APPLICATION_JSON);

        HttpEntity httpEntity = MultipartEntityBuilder
                .create()
                .addPart("json", json)
                .build();

        post.setEntity(httpEntity);

        CloseableHttpResponse response;
        String responseBody = null;
        try {
            response = httpClient.execute(post);
//            StatusLine status = response.getStatusLine();
            responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

        }
        catch (Exception e){
            logger.log(Level.SEVERE,e.getMessage());
        }
        return responseBody;
    }

    JenksResponse deserializeResponse(String jsonResponse) {
        byte [] data = jsonResponse.getBytes(StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        JenksResponse jenks = null;
        try{
            jenks = mapper.readValue(data, JenksResponse.class);
        }
        catch (Exception e){
            logger.log(Level.SEVERE,e.getMessage());
        }
        return jenks;
    }

}



