package com.dsv.datafactory.file.extraction.processor.models;

import java.util.ArrayList;

public class GoogleParagraph {
    private TextProperty property;
    private BoundingPoly boundingBox;
    private ArrayList<GoogleWord> words;
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

    public void setWords(ArrayList<GoogleWord> words) {
        this.words = words;
    }

    public double getConfidence() {
        return confidence;
    }

    public TextProperty getProperty() {
        return property;
    }

    public BoundingPoly getBoundingBox() {
        return boundingBox;
    }

    public ArrayList<GoogleWord> getWords() {
        return words;
    }
}
