package com.dsv.datafactory.file.extraction.processor.domain.ocr;

import com.dsv.datafactory.file.extraction.processor.models.*;
import com.dsv.datafactory.file.extraction.processor.models.BoundingPoly;
import com.dsv.datafactory.file.extraction.processor.models.EntityAnnotation;
import com.dsv.datafactory.file.extraction.processor.models.TextAnnotation;
import com.dsv.datafactory.model.Vertices;
import com.google.cloud.vision.v1.*;

import java.util.ArrayList;
import java.util.List;

public class OcrParser {
    AnnotateImageResponse raw;

    public OcrParser(AnnotateImageResponse response){
        this.raw = response;
    }

    public GoogleVisionResponse parse(){
        GoogleVisionResponse parsed = new GoogleVisionResponse();
        parsed.setTextAnnotations(parseTextAnnotations(raw.getTextAnnotationsList()));
        parsed.setFullTextAnnotation(parseFullTextAnnotation(raw.getFullTextAnnotation()));

        return parsed;
    }

    public ArrayList<EntityAnnotation> parseTextAnnotations(List<com.google.cloud.vision.v1.EntityAnnotation> rawTextAnnotations){
        ArrayList<EntityAnnotation> parsedEntities = new ArrayList<>();

        for(com.google.cloud.vision.v1.EntityAnnotation rawEntity: rawTextAnnotations){
            parsedEntities.add(parseEntityAnnotation(rawEntity));
        }
        return parsedEntities;
    }

    public EntityAnnotation parseEntityAnnotation(com.google.cloud.vision.v1.EntityAnnotation rawEntity){
        BoundingPoly parsedPoly = parseBoundingPoly(rawEntity.getBoundingPoly());
        return new EntityAnnotation(rawEntity.getLocale(), rawEntity.getDescription(), rawEntity.getConfidence(), parsedPoly);
    }

    public BoundingPoly parseBoundingPoly(com.google.cloud.vision.v1.BoundingPoly rawBoundingPoly){
        BoundingPoly parsedPoly = new BoundingPoly();
        ArrayList<Vertices> vertices = new ArrayList<>();
        ArrayList<NormalizedVertices> normVertices = new ArrayList<>();

        for(Vertex rawVertex : rawBoundingPoly.getVerticesList() ){
            vertices.add(new Vertices(rawVertex.getX(), rawVertex.getY()));
        }
        for(NormalizedVertex rawVertex : rawBoundingPoly.getNormalizedVerticesList() ){
            normVertices.add(new NormalizedVertices(rawVertex.getX(), rawVertex.getY()));
        }
        parsedPoly.setVertices(vertices);
        parsedPoly.setNormalizedVertices(normVertices);

        return parsedPoly;
    }

    public TextAnnotation parseFullTextAnnotation(com.google.cloud.vision.v1.TextAnnotation fullTextAnnotation){
        TextAnnotation parsedTextAnnotation = new TextAnnotation();
        parsedTextAnnotation.setText(fullTextAnnotation.getText());
        parsedTextAnnotation.setPages(parsePages(fullTextAnnotation.getPagesList()));

        return parsedTextAnnotation;
    }

    public ArrayList<GooglePage> parsePages(List<Page> rawPages){
        ArrayList<GooglePage> parsedPages = new ArrayList<>();

        for(Page rawPage : rawPages){
            GooglePage parsed = new GooglePage();
            parsed.setConfidence(rawPage.getConfidence());
            parsed.setHeight(rawPage.getHeight());
            parsed.setWidth(rawPage.getWidth());
            parsed.setBlocks(parseBlocks(rawPage.getBlocksList()));
            parsed.setTextProperty(parseTextProperty(rawPage.getProperty()));

            parsedPages.add(parsed);
        }

        return parsedPages;
    }

    public TextProperty parseTextProperty(com.google.cloud.vision.v1.TextAnnotation.TextProperty rawTextProperty){
        TextProperty property = new TextProperty();
        ArrayList<DetectedLanguage> detectedLanguages= new ArrayList<>();
        com.google.cloud.vision.v1.TextAnnotation.DetectedBreak rawBreak = rawTextProperty.getDetectedBreak();
        DetectedBreak detectedBreak = new DetectedBreak(rawBreak.getType().name(), rawBreak.getTypeValue(), rawBreak.getIsPrefix());
        property.setDetectedBreak(detectedBreak);
        for(com.google.cloud.vision.v1.TextAnnotation.DetectedLanguage rawDL: rawTextProperty.getDetectedLanguagesList() ){
            detectedLanguages.add(new DetectedLanguage(rawDL.getConfidence(), rawDL.getLanguageCode()));
        }
        property.setDetectedLanguages(detectedLanguages);

        return property;
    }


    public ArrayList<GoogleBlock> parseBlocks(List<Block> rawBlocks){
        ArrayList<GoogleBlock> blocks = new ArrayList<>();
        for(Block rawBlock : rawBlocks){
            GoogleBlock block = new GoogleBlock();
            block.setBlockType(rawBlock.getBlockType().name());
            block.setConfidence(rawBlock.getConfidence());
            block.setBoundingBox(parseBoundingPoly(rawBlock.getBoundingBox()));
            block.setProperty(parseTextProperty(rawBlock.getProperty()));
            block.setParagraphs(parseParagraphs(rawBlock.getParagraphsList()));

            blocks.add(block);
        }

        return blocks;
    }

    public ArrayList<GoogleParagraph> parseParagraphs(List<Paragraph> rawParagraphs){
        ArrayList<GoogleParagraph> paragraphs = new ArrayList<>();
        for(Paragraph rawParagraph : rawParagraphs){
            GoogleParagraph paragraph = new GoogleParagraph();
            paragraph.setBoundingBox(parseBoundingPoly(rawParagraph.getBoundingBox()));
            paragraph.setConfidence(rawParagraph.getConfidence());
            paragraph.setProperty(parseTextProperty(rawParagraph.getProperty()));
            paragraph.setWords(parseWords(rawParagraph.getWordsList()));

            paragraphs.add(paragraph);
        }

        return paragraphs;
    }

    public ArrayList<GoogleWord> parseWords(List<Word> rawWords){
        ArrayList<GoogleWord> words = new ArrayList<>();
        for(Word rawWord : rawWords){
            GoogleWord word = new GoogleWord();
            word.setConfidence(rawWord.getConfidence());
            word.setProperty(parseTextProperty(rawWord.getProperty()));
            word.setBoundingBox(parseBoundingPoly(rawWord.getBoundingBox()));
            word.setSymbols(parseSymbols(rawWord.getSymbolsList()));

            words.add(word);
        }
        return words;
    }

    public ArrayList<GoogleSymbol> parseSymbols(List<Symbol> rawSymbols){
        ArrayList<GoogleSymbol> symbols = new ArrayList<>();
        for(Symbol rawSymbol : rawSymbols){
            GoogleSymbol symbol = new GoogleSymbol();
            symbol.setBoundingBox(parseBoundingPoly(rawSymbol.getBoundingBox()));
            symbol.setText(rawSymbol.getText());
            symbol.setConfidence(rawSymbol.getConfidence());
            symbol.setProperty(parseTextProperty(rawSymbol.getProperty()));

            symbols.add(symbol);
        }
        return symbols;
    }

}
