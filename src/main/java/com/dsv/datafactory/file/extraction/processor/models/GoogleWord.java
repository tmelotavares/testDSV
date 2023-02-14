package com.dsv.datafactory.file.extraction.processor.models;

import java.util.ArrayList;

public class GoogleWord {
    private TextProperty property;
    private BoundingPoly boundingBox;
    private ArrayList<GoogleSymbol> symbols;
    private double confidence;

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public void setBoundingBox(BoundingPoly boundingBox) {
        this.boundingBox = boundingBox;
    }

    public void setProperty(TextProperty property) {
        this.property = property;
    }

    public void setSymbols(ArrayList<GoogleSymbol> symbols) {
        this.symbols = symbols;
    }

    public double getConfidence() {
        return confidence;
    }

    public BoundingPoly getBoundingBox() {
        return boundingBox;
    }

    public TextProperty getProperty() {
        return property;
    }

    public ArrayList<GoogleSymbol> getSymbols() {
        return symbols;
    }
}
