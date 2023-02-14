package com.dsv.datafactory.file.extraction;

import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcr;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcrP;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.OcrParser;
import com.dsv.datafactory.file.extraction.processor.models.*;
import com.dsv.datafactory.file.extraction.processor.models.BoundingPoly;
import com.dsv.datafactory.file.extraction.processor.models.EntityAnnotation;
import com.dsv.datafactory.model.Vertices;
import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.TextAnnotation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParsingTest {
    String testDir = "src/test/resources/AnnotateImageResponseObjects_v2/";
    File[] paths = new File(testDir).listFiles();
    @BeforeAll
    void setup(){
        Config config = new Config();
        config.runGVInPararell = "false";
        GoogleOcrP refacOcr = new GoogleOcrP(config);
    }
    @Test
    void testPages1() throws IOException {
        testPagesBody(loadAnnotateImageResponse(paths[0].getAbsolutePath()));
    }

    @Test
    void testPages2() throws IOException {
        testPagesBody(loadAnnotateImageResponse(paths[1].getAbsolutePath()));
    }

    @Test
    void testPages3() throws IOException {
        testPagesBody(loadAnnotateImageResponse(paths[2].getAbsolutePath()));
    }

    @Test
    void testPages4() throws IOException {
        testPagesBody(loadAnnotateImageResponse(paths[3].getAbsolutePath()));
    }

    @Test
    void testBlocks1() throws IOException {
        testBlocksBody(loadAnnotateImageResponse(paths[0].getAbsolutePath()));
    }

    @Test
    void testBlocks2() throws IOException {
        testBlocksBody(loadAnnotateImageResponse(paths[1].getAbsolutePath()));
    }

    @Test
    void testBlocks3() throws IOException {
        testBlocksBody(loadAnnotateImageResponse(paths[2].getAbsolutePath()));
    }

    @Test
    void testBlocks4() throws IOException {
        testBlocksBody(loadAnnotateImageResponse(paths[3].getAbsolutePath()));
    }

    @Test
    void testParagraphs1() throws IOException {
        testParagraphsBody(loadAnnotateImageResponse(paths[0].getAbsolutePath()));
    }

    @Test
    void testParagraphs2() throws IOException {
        testParagraphsBody(loadAnnotateImageResponse(paths[1].getAbsolutePath()));
    }

    @Test
    void testParagraphs3() throws IOException {
        testParagraphsBody(loadAnnotateImageResponse(paths[2].getAbsolutePath()));
    }

    @Test
    void testParagraphs4() throws IOException {
        testParagraphsBody(loadAnnotateImageResponse(paths[3].getAbsolutePath()));
    }

    @Test
    void testWords1() throws IOException {
        testWordsBody(loadAnnotateImageResponse(paths[0].getAbsolutePath()));
    }

    @Test
    void testWords2() throws IOException {
        testWordsBody(loadAnnotateImageResponse(paths[1].getAbsolutePath()));
    }

    @Test
    void testWords3() throws IOException {
        testWordsBody(loadAnnotateImageResponse(paths[2].getAbsolutePath()));
    }

    @Test
    void testWords4() throws IOException {
        testWordsBody(loadAnnotateImageResponse(paths[3].getAbsolutePath()));
    }


    @Test
    void testSymbols1() throws IOException {
        testSymbolsBody(loadAnnotateImageResponse(paths[0].getAbsolutePath()));
    }

    @Test
    void testSymbols2() throws IOException {
        testSymbolsBody(loadAnnotateImageResponse(paths[1].getAbsolutePath()));
    }

    @Test
    void testSymbols3() throws IOException {
        testSymbolsBody(loadAnnotateImageResponse(paths[2].getAbsolutePath()));
    }

    @Test
    void testSymbols4() throws IOException {
        testSymbolsBody(loadAnnotateImageResponse(paths[3].getAbsolutePath()));
    }


    @Test
    void testTextAnnotations1() throws IOException {
        testTextAnnotationsBody(loadAnnotateImageResponse(paths[0].getAbsolutePath()));
    }

    @Test
    void testTextAnnotations2() throws IOException {
        testTextAnnotationsBody(loadAnnotateImageResponse(paths[1].getAbsolutePath()));
    }

    @Test
    void testTextAnnotations3() throws IOException {
        testTextAnnotationsBody(loadAnnotateImageResponse(paths[2].getAbsolutePath()));
    }

    @Test
    void testTextAnnotations4() throws IOException {
        testTextAnnotationsBody(loadAnnotateImageResponse(paths[3].getAbsolutePath()));
    }


    void testTextAnnotationsBody(AnnotateImageResponse raw){
        GoogleVisionResponse parsed = new OcrParser(raw).parse();
        checkAnnotationsCount(parsed, raw);
        for(int i = 0; i < parsed.getTextAnnotations().size(); ++i){
            EntityAnnotation parsedEntity = parsed.getTextAnnotations().get(i);
            com.google.cloud.vision.v1.EntityAnnotation rawEntity = raw.getTextAnnotations(i);

            checkAnnotationProperties(parsedEntity, rawEntity);
        }
    }

    void testPagesBody(AnnotateImageResponse raw){
        GoogleVisionResponse parsed = new OcrParser(raw).parse();
        ArrayList<GooglePage> parsedPages = parsed.getFullTextAnnotation().getPages();
        List<Page> rawPages = raw.getFullTextAnnotation().getPagesList();

        checkPageCount(parsed, raw);
        Assertions.assertEquals(parsed.getFullTextAnnotation().getText(), raw.getFullTextAnnotation().getText());
        for(int i = 0; i < rawPages.size(); ++i) {
            GooglePage parsedPage = parsedPages.get(i);
            Page rawPage = rawPages.get(i);
            checkPageProperties(parsedPage, rawPage);
            checkBlockCount(parsedPage, rawPage);
        }
    }

    void testBlocksBody(AnnotateImageResponse raw) {
        GoogleVisionResponse parsed = new OcrParser(raw).parse();
        ArrayList<GooglePage> parsedPages = parsed.getFullTextAnnotation().getPages();
        List<Page> rawPages = raw.getFullTextAnnotation().getPagesList();
        for(int i = 0; i < rawPages.size(); ++i) {
            GooglePage parsedPage = parsedPages.get(i);
            Page rawPage = rawPages.get(i);
            for(int j = 0; j < parsedPage.getBlocks().size(); ++j) {
                GoogleBlock parsedBlock = parsedPage.getBlocks().get(j);
                Block rawBlock = rawPage.getBlocks(j);

                checkBlockProperties(parsedBlock, rawBlock);
                checkParagraphCount(parsedBlock, rawBlock);
            }
        }
    }

    void testParagraphsBody(AnnotateImageResponse raw) {
        GoogleVisionResponse parsed = new OcrParser(raw).parse();
        ArrayList<GooglePage> parsedPages = parsed.getFullTextAnnotation().getPages();
        List<Page> rawPages = raw.getFullTextAnnotation().getPagesList();
        for(int i = 0; i < rawPages.size(); ++i) {
            GooglePage parsedPage = parsedPages.get(i);
            Page rawPage = rawPages.get(i);
            for(int j = 0; j < parsedPage.getBlocks().size(); ++j) {
                GoogleBlock parsedBlock = parsedPage.getBlocks().get(j);
                Block rawBlock = rawPage.getBlocks(j);
                for(int k = 0; k < parsedBlock.getParagraphs().size(); ++k) {
                    GoogleParagraph parsedParagraph = parsedBlock.getParagraphs().get(k);
                    Paragraph rawParagraph = rawBlock.getParagraphs(k);

                    checkParagraphProperties(parsedParagraph, rawParagraph);
                    checkWordCount(parsedParagraph, rawParagraph);
                }
            }
        }
    }

    void testWordsBody(AnnotateImageResponse raw) {
        GoogleVisionResponse parsed = new OcrParser(raw).parse();
        ArrayList<GooglePage> parsedPages = parsed.getFullTextAnnotation().getPages();
        List<Page> rawPages = raw.getFullTextAnnotation().getPagesList();
        for(int i = 0; i < rawPages.size(); ++i) {
            GooglePage parsedPage = parsedPages.get(i);
            Page rawPage = rawPages.get(i);
            for(int j = 0; j < parsedPage.getBlocks().size(); ++j) {
                GoogleBlock parsedBlock = parsedPage.getBlocks().get(j);
                Block rawBlock = rawPage.getBlocks(j);
                for(int k = 0; k < parsedBlock.getParagraphs().size(); ++k) {
                    GoogleParagraph parsedParagraph = parsedBlock.getParagraphs().get(k);
                    Paragraph rawParagraph = rawBlock.getParagraphs(k);
                    for(int l = 0; l < parsedParagraph.getWords().size(); ++l){
                        GoogleWord parsedWord = parsedParagraph.getWords().get(l);
                        Word rawWord = rawParagraph.getWords(l);

                        checkWordProperties(parsedWord, rawWord);
                        checkSymbolCount(parsedWord, rawWord);
                    }
                }
            }
        }
    }

    void testSymbolsBody(AnnotateImageResponse raw) {
        GoogleVisionResponse parsed = new OcrParser(raw).parse();
        ArrayList<GooglePage> parsedPages = parsed.getFullTextAnnotation().getPages();
        List<Page> rawPages = raw.getFullTextAnnotation().getPagesList();
        for(int i = 0; i < rawPages.size(); ++i) {
            GooglePage parsedPage = parsedPages.get(i);
            Page rawPage = rawPages.get(i);
            for(int j = 0; j < parsedPage.getBlocks().size(); ++j) {
                GoogleBlock parsedBlock = parsedPage.getBlocks().get(j);
                Block rawBlock = rawPage.getBlocks(j);
                for(int k = 0; k < parsedBlock.getParagraphs().size(); ++k) {
                    GoogleParagraph parsedParagraph = parsedBlock.getParagraphs().get(k);
                    Paragraph rawParagraph = rawBlock.getParagraphs(k);
                    for(int l = 0; l < parsedParagraph.getWords().size(); ++l) {
                        GoogleWord parsedWord = parsedParagraph.getWords().get(l);
                        Word rawWord = rawParagraph.getWords(l);
                        for(int m = 0; m < parsedWord.getSymbols().size(); ++m){
                            GoogleSymbol parsedSymbol = parsedWord.getSymbols().get(m);
                            Symbol rawSymbol = rawWord.getSymbols(m);

                            checkSymbolProperties(parsedSymbol, rawSymbol);
                        }
                    }
                }
            }
        }
    }


    void checkPageCount(GoogleVisionResponse parsed, AnnotateImageResponse raw){
        ArrayList<GooglePage> parsedPages = parsed.getFullTextAnnotation().getPages();
        List<Page> rawPages = raw.getFullTextAnnotation().getPagesList();

        Assertions.assertEquals(parsedPages.size(), rawPages.size());
    }

    void checkBlockCount(GooglePage parsed, Page raw){
        Assertions.assertEquals(parsed.getBlocks().size(), raw.getBlocksCount());
    }

    void checkParagraphCount(GoogleBlock parsed, Block raw){
        Assertions.assertEquals(parsed.getParagraphs().size(), raw.getParagraphsList().size());
    }

    void checkWordCount(GoogleParagraph parsed, Paragraph raw){
        Assertions.assertEquals(parsed.getWords().size(), raw.getWordsList().size());
    }

    void checkSymbolCount(GoogleWord parsed, Word raw){
        Assertions.assertEquals(parsed.getSymbols().size(), raw.getSymbolsList().size());
    }

    void checkAnnotationsCount(GoogleVisionResponse parsed, AnnotateImageResponse raw){
        Assertions.assertEquals(parsed.getTextAnnotations().size(), raw.getTextAnnotationsList().size());
    }

    void checkPageProperties(GooglePage parsed, Page raw){
        Assertions.assertEquals(parsed.getConfidence(), raw.getConfidence());
        Assertions.assertEquals(parsed.getHeight(), raw.getHeight());
        Assertions.assertEquals(parsed.getWidth(), raw.getWidth());
        checkTextProperty(parsed.getTextProperty(), raw.getProperty());
    }

    void checkBlockProperties(GoogleBlock parsed, Block raw){
        Assertions.assertEquals(parsed.getBlockType(), raw.getBlockType().name());
        checkBoundingPoly(parsed.getBoundingBox(), raw.getBoundingBox());
        checkTextProperty(parsed.getProperty(), raw.getProperty());
    }

    void checkParagraphProperties(GoogleParagraph parsed, Paragraph raw){
        Assertions.assertEquals(parsed.getConfidence(), raw.getConfidence());
        checkBoundingPoly(parsed.getBoundingBox(), raw.getBoundingBox());
        checkTextProperty(parsed.getProperty(), raw.getProperty());
    }

    void checkWordProperties(GoogleWord parsed, Word raw){
        Assertions.assertEquals(parsed.getConfidence(), raw.getConfidence());
        checkBoundingPoly(parsed.getBoundingBox(), raw.getBoundingBox());
        checkTextProperty(parsed.getProperty(), raw.getProperty());
    }

    void checkSymbolProperties(GoogleSymbol parsed, Symbol raw){
        Assertions.assertEquals(parsed.getConfidence(), raw.getConfidence());
        Assertions.assertEquals(parsed.getText(), raw.getText());
        checkBoundingPoly(parsed.getBoundingBox(), raw.getBoundingBox());
        checkTextProperty(parsed.getProperty(), raw.getProperty());

    }

    void checkAnnotationProperties(EntityAnnotation parsed, com.google.cloud.vision.v1.EntityAnnotation raw){
        Assertions.assertEquals(parsed.getConfidence(), raw.getConfidence());
        Assertions.assertEquals(parsed.getLocale(), raw.getLocale());
        Assertions.assertEquals(parsed.getDescription(), raw.getDescription());
        checkBoundingPoly(parsed.getBoundingPoly(), raw.getBoundingPoly());
    }

    void checkBoundingPoly(BoundingPoly parsed, com.google.cloud.vision.v1.BoundingPoly raw){
        for(int i = 0; i < parsed.getVertices().size(); ++i){
            Vertices parsedVertex = parsed.getVertices().get(i);
            Vertex rawVertex = raw.getVertices(i);

            Assertions.assertEquals(parsedVertex.getX(), rawVertex.getX());
            Assertions.assertEquals(parsedVertex.getY(), rawVertex.getY());
        }
        for(int i = 0; i < parsed.getNormalizedVertices().size(); ++i){
            NormalizedVertices parsedNVertex= parsed.getNormalizedVertices().get(i);
            NormalizedVertex rawNVertex = raw.getNormalizedVertices(i);

            Assertions.assertEquals(parsedNVertex.getX(), rawNVertex.getX());
            Assertions.assertEquals(parsedNVertex.getY(), rawNVertex.getY());
        }
    }

    void checkTextProperty(TextProperty parsed, TextAnnotation.TextProperty raw){
        checkDetectedLanguage(parsed.getDetectedLanguages(), raw.getDetectedLanguagesList());
        checkDetectedBreak(parsed.getDetectedBreak(), raw.getDetectedBreak());
    }

    void checkDetectedLanguage(ArrayList<DetectedLanguage> parsed, List<TextAnnotation.DetectedLanguage> raw){
        for(int i = 0; i < parsed.size(); ++i){
            Assertions.assertEquals(parsed.get(i).getConfidence(), raw.get(i).getConfidence());
            Assertions.assertEquals(parsed.get(i).getLanguageCode(), raw.get(i).getLanguageCode());
        }
    }

    void checkDetectedBreak(DetectedBreak parsed, TextAnnotation.DetectedBreak raw){
        Assertions.assertEquals(parsed.getType(), raw.getType().name());
        Assertions.assertEquals(parsed.getTypeValue(), raw.getType().getNumber());
        Assertions.assertEquals(parsed.getIsPrefix(), raw.getIsPrefix());
    }

    AnnotateImageResponse loadAnnotateImageResponse(String path){
        AnnotateImageResponse annotateImageResponse = null;

        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            annotateImageResponse = (AnnotateImageResponse) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return annotateImageResponse;
    }

}
