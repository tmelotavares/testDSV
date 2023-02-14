package com.dsv.datafactory.file.extraction.processor.models;

import java.util.ArrayList;

public class GoogleVisionResponse {
    private ArrayList<EntityAnnotation> textAnnotations = new ArrayList<>();
    private TextAnnotation fullTextAnnotation;

    public void setFullTextAnnotation(TextAnnotation fullTextAnnotation) {
        this.fullTextAnnotation = fullTextAnnotation;
    }

    public void setTextAnnotations(ArrayList<EntityAnnotation> textAnnotations) {
        this.textAnnotations = textAnnotations;
    }

    public void addTextAnnotation(EntityAnnotation entity){
        this.textAnnotations.add(entity);
    }

    public void addMultipleTextAnnotations(ArrayList<EntityAnnotation> textAnnotations) {
        for (EntityAnnotation textAnnotation : textAnnotations) {
            this.textAnnotations.add(textAnnotation);
        }
    }

    public TextAnnotation getFullTextAnnotation() {
        return fullTextAnnotation;
    }

    public ArrayList<EntityAnnotation> getTextAnnotations() {
        return textAnnotations;
    }


}
