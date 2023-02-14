package com.dsv.datafactory.file.extraction;

import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcr;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcrP;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@Disabled
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SaveAnnotateImageResponseToDisk {
    GoogleOcrP refac;
    String imageDir = Paths.get("src/test/resources/images/SerializationTest/").toAbsolutePath().toString();
    String dstDir = Paths.get("src/test/resources/AnnotateImageResponseObjects_v2/").toAbsolutePath().toString()+"/";

    @BeforeAll
    void setup(){
        Config config = new Config();
        config.runGVInPararell = "false";
        refac = new GoogleOcrP(config);
    }

    @Test
    void ProcessFilesAndSaveToDisk() throws IOException {
        ArrayList<String> pathImages = Arrays.stream(new File(imageDir).listFiles()).map(x -> x.getAbsolutePath()).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Image> images = refac.generateImages(pathImages);
        List<AnnotateImageRequest> requests = refac.bulkGeneratePngRequest(images, Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build() );
        List<AnnotateImageResponse>responses = refac.extractFullDocumentResponse(requests);
        for (int i =0;i< pathImages.size();i++){
            File image = new File(pathImages.get(i));
            String dst = dstDir + image.getName().substring(0, image.getName().length()-".png".length()) + ".object";
            FileOutputStream fileOutputStream = new FileOutputStream(dst);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(responses.get(i));
        }
    }

    void loadAnnotateImageResponseFromDisk(String objPath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(objPath);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            AnnotateImageResponse annotateImageResponse = (AnnotateImageResponse) objectInputStream.readObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}