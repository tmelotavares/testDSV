package com.dsv.datafactory.file.extraction.processor.models;

public class DetectedLanguage {
    private float confidence;
    private String languageCode;
    //FIXME: Lombok @NoArgsConstructor
    public DetectedLanguage(){}
    //FIXME: Lombok @AllArgsConstructor
    public DetectedLanguage(float confidence, String languageCode){
        this.confidence = confidence;
        this.languageCode = languageCode;
    }
    //FIXME: Why use getters and setters when you have lombok installed? Must be replaced with @Data
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public void setLanguageCode(String language_code) {
        this.languageCode = language_code;
    }

    public float getConfidence() {
        return confidence;
    }

    public String getLanguageCode() {
        return languageCode;
    }
}
