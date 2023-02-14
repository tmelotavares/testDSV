package com.dsv.datafactory.file.extraction.processor.domain.saving;

import com.dsv.datafactory.file.extraction.processor.util.ConfigurationLoader;
import com.dsv.datafactory.model.Document;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;


import java.io.*;
import java.nio.charset.StandardCharsets;


public class DocumentToDisk {
//    private String pathImages;

    final static Logger logger = Logger.getLogger(DocumentToDisk.class.getSimpleName());

    public void execute(String key, Document res,String shipmentId) throws IOException
    {
        String pathDocument =  ConfigurationLoader.getOrDefault("PATH_DOCUMENT", "/files/hocr/")+shipmentId+"/";
        checkIfExists(pathDocument);
        saveDocumentAsJson(key, res, pathDocument);
    }

    private void saveDocumentAsJson(String key, Document file,String pathToDocument) throws IOException
    {
            writeToJson(key,file,pathToDocument);
    }

    private void checkIfExists(String pathShipmentIdFolder){
        File file = new File(pathShipmentIdFolder);
        if (!file.exists()) {
            file.mkdir();
        }

    }
    private void writeToJson(String key, Document doc,String pathDocument) throws IOException {
        String documentExt = "json";
        String fullPath = pathDocument +key + "."+ documentExt;
        doc.setPathToDocumentFile(fullPath);
        logger.info("fullpath is: " + fullPath);

        File file = new File(fullPath);

        OutputStreamWriter writer =
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(doc);
        writer.write(jsonString);
        writer.close();
    }


}