package com.dsv.datafactory.file.extraction;
import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcr;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcrP;
import com.dsv.datafactory.file.extraction.processor.models.EntityAnnotation;
import com.dsv.datafactory.file.extraction.processor.models.GoogleVisionResponse;
import com.dsv.datafactory.model.*;
import com.dsv.datafactory.model.Word;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.FileInputStream;

import java.nio.charset.StandardCharsets;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NegativeCoordsTest {
    //GoogleOcr googleOcr = new GoogleOcr();
    GoogleOcrP refac;

    GoogleVisionResponse ocr = loadGoogleVisionResponseFromDisk("src/test/resources/images/negativeCoords/negativeCoordsExample.json");

    @BeforeAll
    void setup(){
        Config config = new Config();
        config.runGVInPararell = "false";
         refac = new GoogleOcrP(config);
    }
    @Test
    void negativeCoordinatesTest(){
        EntityAnnotation entity1 = ocr.getTextAnnotations().get(4);
        Word word1 = refac.generateWord(entity1);
        System.out.println(entity1.getBoundingPoly());
        Assertions.assertTrue(word1.getBoundingBox().getX1()>=0 & word1.getBoundingBox().getX2()>=0 & word1.getBoundingBox().getY1()>=0 & word1.getBoundingBox().getY2()>=0);
    }

    @Test
    void testMinMaxCoordinatesNegativeInput() {
        EntityAnnotation entity1 = ocr.getTextAnnotations().get(4);
        int[] minMax = refac.getMinMaxCoordinatesFromVertices(entity1.getBoundingPoly());
        for (int val : minMax) {
            Assertions.assertTrue(val >= 0);
        }
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


