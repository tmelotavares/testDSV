package com.dsv.datafactory.file.extraction;

import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcr;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.OcrParser;
import com.dsv.datafactory.file.extraction.processor.models.GoogleVisionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
@Disabled
public class SaveGoogleVisionResponseToDisk {
    GoogleOcr googleOcr = new GoogleOcr();
    String imageDir = "src/test/resources/images/SerializationTest/";
    String airDir = "src/test/resources/AnnotateImageResponseObjects/";
    String dstDir = "src/test/resources/SerializedGoogleVisionResponses/";

    @Test
    void ProcessAnnotateImageResponseAndSaveToDisk() {
        for (File image : new File(airDir).listFiles())
            try {
                String imagePath = image.getAbsolutePath();
                String dst = dstDir + image.getName().substring(0, image.getName().length() - ".object".length()) + ".json";
                AnnotateImageResponse response = loadAnnotateImageResponseFromDisk(imagePath);
                GoogleVisionResponse parsed = new OcrParser(response).parse();
                saveDocument(parsed, dst);
            }catch (Exception e) {
                e.printStackTrace();
            }
    }

        @Test
        void ProcessImagesAndSaveToDisk () {

            for (File image : new File(imageDir).listFiles())
                try {
                    String imagePath = image.getAbsolutePath();
                    String dst = dstDir + image.getName().substring(0, image.getName().length() - ".png".length()) + ".json";
                    AnnotateImageResponse response = googleOcr.generateResponseFromImage(imagePath);
                    GoogleVisionResponse parsed = new OcrParser(response).parse();
                    saveDocument(parsed, dst);
                } catch (Exception e) {
                    e.printStackTrace();
                }

        }
        String serialize (GoogleVisionResponse doc){
            String sDoc = null;
            ObjectMapper mapper = new ObjectMapper();
            try {
                sDoc = mapper.writeValueAsString(doc);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return sDoc;
        }

        void saveDocument (GoogleVisionResponse document, String dst) throws IOException {
            String serialized = serialize(document);
            if (serialized != null) {
                FileOutputStream outputStream = new FileOutputStream(dst);
                byte[] strToBytes = serialized.getBytes();
                outputStream.write(strToBytes);

                outputStream.close();
            }
        }

        AnnotateImageResponse loadAnnotateImageResponseFromDisk (String objPath){
            AnnotateImageResponse annotateImageResponse = null;
            try {
                FileInputStream fileInputStream = new FileInputStream(objPath);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                annotateImageResponse = (AnnotateImageResponse) objectInputStream.readObject();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return annotateImageResponse;
        }

        GoogleVisionResponse loadGoogleVisionResponseFromDisk(String path){
            GoogleVisionResponse response = null;
            try {
                FileInputStream fileInputStream = new FileInputStream(path);
                String sResponse = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8);
                ObjectMapper mapper = new ObjectMapper();
                response = mapper.readValue(sResponse, GoogleVisionResponse.class);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        }


