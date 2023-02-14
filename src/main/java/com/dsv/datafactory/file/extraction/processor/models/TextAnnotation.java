package com.dsv.datafactory.file.extraction.processor.models;

import java.util.ArrayList;

public class TextAnnotation {
    private ArrayList<GooglePage> pages;
    private String text;
    //FIXME: Why use getters and setters when you have lombok installed? Must be replaced with @Data
    public void setPages(ArrayList<GooglePage> pages) {
        this.pages = pages;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public ArrayList<GooglePage> getPages() {
        return pages;
    }
}
