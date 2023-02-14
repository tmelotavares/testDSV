package com.dsv.datafactory.file.extraction.processor.models;

import java.util.ArrayList;

public class GooglePage {
    private int width;
    private int height;
    private ArrayList<GoogleBlock> blocks;
    private double confidence;
    private TextProperty textProperty;

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public void setBlocks(ArrayList<GoogleBlock> blocks) {
        this.blocks = blocks;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setTextProperty(TextProperty textProperty) {
        this.textProperty = textProperty;
    }

    public double getConfidence() {
        return confidence;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public TextProperty getTextProperty() {
        return textProperty;
    }

    public ArrayList<GoogleBlock> getBlocks() {
        return blocks;
    }
}
