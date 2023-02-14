package com.dsv.datafactory.file.extraction.processor.domain;

import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.logging.ECSLoggerProvider;

import com.dsv.datafactory.model.*;
import com.dsv.logger.ECSLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.*;
import net.bytebuddy.asm.Advice;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


import javax.inject.Inject;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import java.util.List;

import java.util.logging.Level;
import java.util.stream.Collectors;

public class ExtractLines {

    String lineServiceUrl;
    String startNumberOfClasses;
    String goodnessOfFit;
    private final ECSLogger logger = ECSLoggerProvider.getLogger(ExtractLines.class.getName());

    @Inject
    public ExtractLines(Config config){
        this.lineServiceUrl = config.lineServiceUrl;
        this.startNumberOfClasses = config.startNumberOfClasses;
        this.goodnessOfFit = config.goodnessOfFit;
    }

    public void generateInputFromDocument(Document document) throws IOException {
        try{
        for (Page page:document.getPages()){
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
            requestValue.addProperty("type",  "customs");
            String results = submitRequest(requestValue.toString());
            parseResults(results,page);
        }
    }catch(Exception e){
            logger.error("Error extracting lines caused in LineExtractor "+ e);
        }}

    public void parseResults(String results,Page originalPage){
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
        int newWords = (int) newLines.stream().map(Line::getWords).mapToLong(List::size).sum();
        if (!newLines.isEmpty() && originalNumWords == newWords){
        originalPage.setLines(newLines);
    }
        else{
          	//TODO dumb logic, to be replaced later
           	originalPage.setLines(newLines);
            logger.error("Number of words before and after line clustering are not aligned, original words : "+ originalNumWords + " words after clustering: "+ newWords);
        }
        }

    String submitRequest(String jsonRequest) throws IOException {

        CloseableHttpClient httpClient = HttpClients.custom().build();
        HttpPost post = new HttpPost(lineServiceUrl);
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
            e.printStackTrace();
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
            logger.warn(e.getMessage());
        }
        return jenks;
    }

}
