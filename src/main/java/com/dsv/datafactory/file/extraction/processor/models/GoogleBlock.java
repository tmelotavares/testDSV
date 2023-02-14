package com.dsv.datafactory.file.extraction.processor.models;


import java.util.ArrayList;

public class GoogleBlock {
    private TextProperty property;
    private BoundingPoly boundingBox;
    private ArrayList<GoogleParagraph> paragraphs;
    private String blockType;
    private double confidence;

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public void setBlockType(String blockType) {
        this.blockType = blockType;
    }

    public void setBoundingBox(BoundingPoly boundingBox) {
        this.boundingBox = boundingBox;
    }

    public void setParagraphs(ArrayList<GoogleParagraph> paragraphs) {
        this.paragraphs = paragraphs;
    }

    public void setProperty(TextProperty property) {
        this.property = property;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getBlockType() {
        return blockType;
    }

    public BoundingPoly getBoundingBox() {
        return boundingBox;
    }

    public TextProperty getProperty() {
        return property;
    }

    public ArrayList<GoogleParagraph> getParagraphs() {
        return paragraphs;
    }
}
