package com.dsv.datafactory.file.extraction.processor.domain.readers;


import com.dsv.datafactory.file.extraction.processor.domain.ExtractLines;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcr;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcrP;
import com.dsv.datafactory.model.Document;


import javax.inject.Inject;

import java.util.ArrayList;


public class ReadImage {


    private GoogleOcrP googleOcrP;
    private ExtractLines lineExtractor;

    @Inject
    public ReadImage(GoogleOcrP ocr, ExtractLines extractLines)
    {
        this.googleOcrP = ocr;
        this.lineExtractor= extractLines;
    }

    public Document extract(ArrayList<String> listOfPathImgs, String key ) throws Exception {
        // made a new results each time to avoid accumulation
        Document document = googleOcrP.generateDocument(listOfPathImgs,key);
        lineExtractor.generateInputFromDocument(document);
        return document;

    }


}
