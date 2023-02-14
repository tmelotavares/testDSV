package com.dsv.datafactory.file.extraction;

import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.domain.ExtractLines;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcr;
import com.dsv.datafactory.file.extraction.processor.modules.ConfigModule;
import com.dsv.datafactory.model.Document;
import com.dsv.datafactory.model.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
@Disabled
public class SaveDocumentToDisk {
    String src = "C:\\_dev_stuff\\data\\pngs\\";
    String dst = "C:\\_dev_stuff\\test_res\\";
    GoogleOcr ocr = new GoogleOcr();



    @Test
    void get_test() {
        Config config = new Config();
        config.lineServiceUrl = "http://localhost:8005/jenks/clustering";
        config.goodnessOfFit = "0.999";
        ExtractLines jenks = new ExtractLines(config);

        File dir = new File(src);
        String[] paths = dir.list();
        ArrayList toProcess = new ArrayList();
        ArrayList<String> mids = new ArrayList<>();

        for (File req : dir.listFiles()) {

            String mid = req.getName().substring(0, 64);
            if (!mids.contains(mid)) {
                mids.add(mid);
            }
        }
        for (String mid : mids) {
            Document toSave = null;
            toProcess.clear();
            for (String path : paths) {
                if (path.contains(mid)) {
                    toProcess.add(src + path);
                }
            }
            if (notDone(mid)) {
                try {
                    toSave = ocr.generateDocument(toProcess, mid);
                    jenks.generateInputFromDocument(toSave);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    saveDocument(toSave, mid);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    Boolean notDone(String masterId){
        File resp =  new File(dst+masterId+".json");
        return !resp.exists();
    }

    String serialize(Document doc) {
        String sDoc = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            sDoc = mapper.writeValueAsString(doc);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return sDoc;
    }


    void saveDocument(Document document, String fname) throws IOException {
        String serialized = serialize(document);
        if (serialized != null) {
            String fileName = dst + fname+".json";
            FileOutputStream outputStream = new FileOutputStream(fileName);
            byte[] strToBytes = serialized.getBytes();
            outputStream.write(strToBytes);

            outputStream.close();
        }
    }

}
