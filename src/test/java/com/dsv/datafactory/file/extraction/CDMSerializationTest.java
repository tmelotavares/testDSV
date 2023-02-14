package com.dsv.datafactory.file.extraction;

import com.dsv.datafactory.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class CDMSerializationTest {
    String testPagePath = "src/test/resources/CDMFiles/page_test.json";
    String testWordPath1 = "src/test/resources/CDMFiles/word_test1.json";
    String testWordPath2 = "src/test/resources/CDMFiles/word_test2.json";

    @Test
    void testPageDeserialization() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Boolean condition = true;
        Page page = new Page();
        try{
            page = objectMapper.readValue(new File(testPagePath), Page.class);
        }
        catch(Exception e){
            condition = false;
        }
        Assertions.assertTrue(condition);
        Page manualPage = loadTestPage(testPagePath);
        Assertions.assertEquals(page.getWidth(), manualPage.getWidth());
        Assertions.assertEquals(page.getHeight(), manualPage.getHeight());
        Assertions.assertEquals(page.getRotation(), manualPage.getRotation());
        Assertions.assertEquals(page.getPageKey(), manualPage.getPageKey());
        Assertions.assertEquals(page.getPageNumber(), manualPage.getPageNumber());
    }


    @Test
    void testWords() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Boolean condition = true;
        Word word1 = new Word();
        Word word2 = new Word();
        try{
            word1 = objectMapper.readValue(new File(testWordPath1), Word.class);
            word2 = objectMapper.readValue(new File(testWordPath2), Word.class);
        }
        catch(Exception e){
            condition = false;
        }
        Assertions.assertTrue(condition);

        String json1 = new String(Files.readAllBytes(new File(testWordPath1).toPath()), "UTF-8");
        String json2 = new String(Files.readAllBytes(new File(testWordPath2).toPath()), "UTF-8");
        Word manualWord1 = getSingleWord(objectMapper.readTree(json1));
        Word manualWord2 = getSingleWord(objectMapper.readTree(json2));

        Assertions.assertEquals(word1.getRotation(), manualWord1.getRotation());
        Assertions.assertEquals(word1.getWord(), manualWord1.getWord());
        Assertions.assertEquals(word1.getConfidence(), manualWord1.getConfidence());
        Assertions.assertEquals(word1.getxMean(), manualWord1.getxMean());
        Assertions.assertEquals(word1.getyMean(), manualWord1.getyMean());

        Assertions.assertEquals(word2.getRotation(), manualWord2.getRotation());
        Assertions.assertEquals(word2.getWord(), manualWord2.getWord());
        Assertions.assertEquals(word2.getConfidence(), manualWord2.getConfidence());
        Assertions.assertEquals(word2.getxMean(), manualWord2.getxMean());
        Assertions.assertEquals(word2.getyMean(), manualWord2.getyMean());
    }

    Page loadTestPage(String path) throws IOException {
        File testObj = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = new String(Files.readAllBytes(testObj.toPath()), "UTF-8");
        JsonNode jsonNode = objectMapper.readTree(json);
        Page page = new Page();
        page.setHeight(jsonNode.get("height").asInt());
        page.setWidth(jsonNode.get("width").asInt());
        page.setPageKey(jsonNode.get("pageKey").asText());
        page.setPageNumber(jsonNode.get("pageNumber").asInt());
        page.setRotation(jsonNode.get("rotation").asInt());
        page.setLines(getLines(jsonNode.get("lines")));
        page.setLanguage(getLanguage(jsonNode.get("language")));

        return page;
    }

    List<Language> getLanguage(JsonNode pageNode){
        List<Language> languages = new ArrayList<>();
        for(JsonNode obj: pageNode){
            Language lang = new Language();
            lang.setLanguageCode(obj.get("languageCode").asText());
            lang.setConfidence((float) obj.get("confidence").asDouble());
            languages.add(lang);
        }

        return languages;
    }

    List<Line> getLines(JsonNode pageNode){
        List<Line> lines = new ArrayList<>();
        for(JsonNode obj: pageNode){
            Line line = new Line();
            line.setLineNumber(obj.get("lineNumber").asInt());
            line.setWords(getWordsFromJson(obj.get("words")));
            lines.add(line);
        }

        return lines;
    }

    List<Word> getWordsFromJson(JsonNode lineWords){
        List<Word> words = new ArrayList<>();
        if(lineWords.isArray()){
            for(JsonNode obj: lineWords){
                Word word = getSingleWord(obj);
            }
        }else{
            words.add(getSingleWord(lineWords));
        }
        return words;
    }

    Word getSingleWord(JsonNode word){
        Word newWord = new Word();
        newWord.setWord(word.get("word").asText());
        newWord.setConfidence(word.get("confidence").asInt());
        newWord.setRotation(word.get("rotation").asInt());
        newWord.setxMean(word.get("xMean").asInt());
        newWord.setyMean(word.get("yMean").asInt());
        newWord.setBoundingBox(getBoundingBox(word.get("boundingBox")));
        newWord.setTopLeftCorner(getVertice(word.get("topLeftCorner")));
        newWord.setLowLeftCorner(getVertice(word.get("lowLeftCorner")));
        newWord.setTopRightCorner(getVertice(word.get("topRightCorner")));
        newWord.setLowRightCorner(getVertice(word.get("lowRightCorner")));

        return newWord;
    }

    BoundingBox getBoundingBox(JsonNode bbox){
        return new BoundingBox(bbox.get("x1").asInt(), bbox.get("x2").asInt(), bbox.get("y1").asInt(), bbox.get("y2").asInt());
    }

    Vertices getVertice(JsonNode vertice){
        return new Vertices(vertice.get("x"). asInt(), vertice.get("y"). asInt());
    }
}
