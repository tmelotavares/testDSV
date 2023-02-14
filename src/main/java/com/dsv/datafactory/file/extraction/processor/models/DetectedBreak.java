package com.dsv.datafactory.file.extraction.processor.models;

public class DetectedBreak {
    private String type;
    private int typeValue;
    private boolean isPrefix;
    //FIXME: Lombok @NoArgsConstructor
    public DetectedBreak(){}
    //FIXME: Lombok @AllArgsConstructor
    public DetectedBreak(String type, int typeValue, boolean isPrefix){
        this.type = type;
        this.typeValue = typeValue;
        this.isPrefix = isPrefix;
    }
    //FIXME: Why use getters and setters when you have lombok installed? Must be replaced with @Data
    public String getType() {
        return type;
    }

    public int getTypeValue() {
        return typeValue;
    }

    public boolean getIsPrefix() {
        return isPrefix;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setIsPrefix(boolean prefix) {
        isPrefix = prefix;
    }

    public void setTypeValue(int typeValue) {
        this.typeValue = typeValue;
    }
}
