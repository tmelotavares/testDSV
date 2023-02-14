package com.dsv.datafactory.file.extraction.processor.models;

public class EntityAnnotation {
    private String locale;
    private String description;
    private double confidence;
    private BoundingPoly boundingPoly;
    //FIXME: Lombok @NoArgsConstructor
    public EntityAnnotation(){}
    //FIXME: Lombok @AllArgsConstructor
    public EntityAnnotation(String locale, String description, double confidence, BoundingPoly boundingPoly){
        this.locale = locale;
        this.description = description;
        this.confidence = confidence;
        this.boundingPoly = boundingPoly;
    }
    //FIXME: Why use getters and setters when you have lombok installed? Must be replaced with @Data
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public void setBoundingPoly(BoundingPoly boundingPoly) {
        this.boundingPoly = boundingPoly;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getDescription() {
        return description;
    }

    public String getLocale() {
        return locale;
    }

    public BoundingPoly getBoundingPoly() {
        return boundingPoly;
    }
}
