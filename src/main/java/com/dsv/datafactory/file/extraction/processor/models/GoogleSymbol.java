package com.dsv.datafactory.file.extraction.processor.models;

public class GoogleSymbol {
    private TextProperty property;
    private BoundingPoly boundingBox;
    private String text;
    private double confidence;

    public void setProperty(TextProperty property) {
        this.property = property;
    }

    public void setBoundingBox(BoundingPoly boundingBox) {
        this.boundingBox = boundingBox;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getConfidence() {
        return confidence;
    }

    public TextProperty getProperty() {
        return property;
    }

    public String getText() {
        return text;
    }

    public BoundingPoly getBoundingBox() {
        return boundingBox;
    }
}

