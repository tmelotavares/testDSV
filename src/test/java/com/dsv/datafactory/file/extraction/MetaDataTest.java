package com.dsv.datafactory.file.extraction;

import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.domain.ExtractContent;
import com.dsv.datafactory.file.extraction.processor.domain.ExtractLines;
import com.dsv.datafactory.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.*;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.junit.jupiter.api.*;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Disabled //For now disabled for build do to external file reference
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MetaDataTest {
    private ExtractContent extractContent;
    private ExtractLines extractLines;
    private String testCases = "C:\\Users\\Kaden.Behan\\OneDrive - DSV\\Desktop\\test_cases_file_extractor\\";
    private String imagePath = "src/test/resources/images/";
    private ArrayList<String> test_blank2 = new ArrayList<>(Arrays.asList(imagePath+"test_blank0.png"));
    private ArrayList<String> produced_more_words_jenks = new ArrayList<>(Arrays.asList(imagePath+"39_COM_INV_rotated_20.jpg",imagePath+"39_COM_INV_rotated_21.jpg"));

    private ArrayList<String> known_issue_grouping = new ArrayList<>(Arrays.asList(imagePath+"2267-1 & 2 CIPL_10.jpg"));
    private ArrayList<String> test_blank = new ArrayList<>(Arrays.asList(imagePath+"0f62a47c044152f3ccdcefb7fa33435b76a41254ebac4a9b61e1ec007f68fe3e0.png"));
    private ArrayList<String> newFormat =  new ArrayList<>(Arrays.asList(imagePath+"sample_outputalice_30_pg_300.jpg",imagePath+"sample_outputalice_30_pg_301.jpg",imagePath+"sample_outputalice_30_pg_302.jpg"));

    @BeforeAll
    void setup() {
        Module modules = Modules.combine(getTestConfigModule());
        Injector injector = Guice.createInjector(modules);
        extractContent = injector.getInstance(ExtractContent.class);
        extractLines = injector.getInstance(ExtractLines.class);
    }

    private Module getTestConfigModule() {
        return new AbstractModule() {
            @Override protected void configure() {
                Config config = new Config();
                bind(Config.class).toInstance(config);
                config.lineServiceUrl = "http://localhost:8005/jenks/clustering";
                config.goodnessOfFit = ".999";
                config.runGVInPararell= "false";
                ExtractLines extractLines = new ExtractLines(config);
                bind(ExtractLines.class).toInstance(extractLines);
            }
        };
    }

    void extractFromPath(String pathToImages,ArrayList<String> paths,String fileName,String key) throws JsonProcessingException {

        File images = new File(pathToImages);
        File[] files = images.listFiles();
        Integer numDocs = files.length;
        MetaData uploadedFile = new MetaData();
        uploadedFile.key = key;
        uploadedFile.fileName = fileName;
        uploadedFile.sortedImagePaths =paths;
        uploadedFile.shipmentId = "test_shipmentId";
        MetaData doc = extractContent.execute(uploadedFile);

    }

    @Test
    void testMetaDataUpdated() throws IOException {
        MetaData uploadedFile = new MetaData();
        uploadedFile.key = "sample_key5";
        uploadedFile.fileName = "sample_output";
        uploadedFile.shipmentId = "test_shipmentId";
        uploadedFile.sortedImagePaths = newFormat;
        uploadedFile.extractedOCRDocumentPath = uploadedFile.key + ".json";
        MetaData metaData = extractContent.execute(uploadedFile);
        assert(metaData.extractedOCRDocumentPath != null);
        ObjectMapper mapper = new ObjectMapper();
        Document doc = mapper.readValue(new File(uploadedFile.extractedOCRDocumentPath),Document.class);
        for (int i =0; i < doc.getPages().size()-1;i++){
            assert(doc.getPages().get(i).getPageKey().equals(uploadedFile.key+i));
            assert(doc.getPages().get(i).getPageNumber() == i);
        }

    }
    @Test
    void testLineExtraction() throws IOException {
        MetaData uploadedFile = new MetaData();
        uploadedFile.key = "sample_key5";
        uploadedFile.fileName = "sample_output";
        uploadedFile.shipmentId = "test_shipmentId";

        uploadedFile.sortedImagePaths =newFormat;
        MetaData metaData = extractContent.execute(uploadedFile);
        Document document = loadFromDisk(metaData.extractedOCRDocumentPath);

        List<Page> pages = new ArrayList<>();
        for (Page page:document.getPages()){
            JsonObject requestValue = new JsonObject();
            List<Integer> ymeans = page.getLines().get(0).getWords().stream().map(Word::getyMean).collect(Collectors.toList());
            requestValue.addProperty("min_goodness_of_fit",88);
            requestValue.addProperty( "start_num_of_class",5);
            JsonArray values = JsonParser.parseString(ymeans.toString()).getAsJsonArray();
            requestValue.add("values",values);
            System.out.println(requestValue);
        }

        String jsonString = new String(Files.readAllBytes(Paths.get("src/test/resources/lines/outputJenks.json")));
        byte [] data = jsonString.getBytes(StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        JenksResponse jenks;

        jenks = mapper.readValue(data, JenksResponse.class);

        Page firstPage = document.getPages().get(0);
        Integer numWords = firstPage.getLines().get(0).getWords().size();
        List<Line> newLines = new ArrayList<>();
        for (Cluster clust : jenks.getValues()){
            int lineNumber = clust.getLineNumber();
            List<Integer> meansForLine = clust.getYMeans();
            Line newLine = new Line();
            List<Word> newWords = firstPage.getLines().get(0).getWords().stream().filter(x->meansForLine.stream().anyMatch(i -> i.equals(x.getyMean()))).collect(Collectors.toList());
            newLine.setLineNumber(lineNumber);
            newLine.setWords(newWords);
            newLines.add(newLine);
        }

        firstPage.setLines(newLines);

        //Check results ///
        assertEquals(numWords, (int) firstPage.getLines().stream().map(Line::getWords).mapToLong(List::size).sum());
        assertEquals(9,firstPage.getLines().size());

        //Assert Document object itself has been updated //
        assertEquals(numWords, (int) document.getPages().get(0).getLines().stream().map(Line::getWords).mapToLong(List::size).sum());
        assertEquals(9,document.getPages().get(0).getLines().size());
    }

    public Document loadFromDisk(String documentPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(documentPath),Document.class);
    }

    @Test
    void testextractLines() throws IOException {
        ///Basic test for build, ensure that sample pdfs go through and produce hocr result
        MetaData uploadedFile = new MetaData();
        uploadedFile.key = "sample_key5";
        uploadedFile.shipmentId = "test_shipmentId";
        uploadedFile.fileName = "sample_output";
        uploadedFile.sortedImagePaths = newFormat;
        MetaData metaData = extractContent.execute(uploadedFile);
        Document document = loadFromDisk(metaData.extractedOCRDocumentPath);
        Page firstPage = document.getPages().get(0);
        Integer numWords = firstPage.getLines().get(0).getWords().size();
        //TODO update with new input from jenks
        String jsonString = new String(Files.readAllBytes(Paths.get("src/test/resources/lines/outputJenks.json")));
        extractLines.parseResults(jsonString,firstPage);
        assertEquals(numWords, (int) document.getPages().get(0).getLines().stream().map(Line::getWords).mapToLong(List::size).sum());
        assertEquals(9,document.getPages().get(0).getLines().size());
    }

    @Test
    void testKnownUngroupedLines() throws IOException {
        MetaData uploadedFile = new MetaData();
        uploadedFile.key = "sample_key5";
        uploadedFile.fileName = "sample_output";
        uploadedFile.shipmentId = "test_shipmentId";
        uploadedFile.sortedImagePaths = known_issue_grouping;
        MetaData metaData = extractContent.execute(uploadedFile);
        Document document = loadFromDisk(metaData.extractedOCRDocumentPath);

    }

    @Test
    void testKnownOvergroupedLines() throws IOException {
        MetaData uploadedFile = new MetaData();
        uploadedFile.key = "39_COM_INV";
        uploadedFile.fileName = "39_COM_INV_rotated_2.pdf";
        uploadedFile.shipmentId = "test_shipmentId";
        uploadedFile.sortedImagePaths = produced_more_words_jenks;
        MetaData metaData = extractContent.execute(uploadedFile);
        Document document = loadFromDisk(metaData.extractedOCRDocumentPath);

    }

    Page generateTestPage(int numLines,int numWords){
        Page page = new Page();
        ArrayList<Line> lines = new ArrayList<>();
        for (int i = 0; i< numLines;i++){
            Line line = new Line();
            ArrayList<Word> words = this.generateTestWords(numWords);
            line.setWords(words);
            line.setLineNumber(i);
            lines.add(line);
        }
        page.setLines(lines);
        return page;
    }


    ArrayList<Word> generateTestWords(int numWords){
        ArrayList<Word> output = new ArrayList<>();
        for (int i = 0; i< numWords;i++){
            Word word = new Word();
            word.setWord("test");
            output.add(word);
        }
        return output;
    }

    ArrayList<Word> generateTestWordsByYMeans(ArrayList<Integer> yMeans){
        ArrayList<Word> output = new ArrayList<>();
        for (int i: yMeans){
            Word word = new Word();
            word.setWord("test");
            word.setyMean(i);
            output.add(word);

        }
        return output;
    }

    @Test
    void testYMeansMatching(){
        ///Test case 1 all words match //
        List<Integer> meansForLine = new ArrayList<>(Arrays.asList(100,130,150,200));
        ArrayList<Word> allMatch = generateTestWordsByYMeans((ArrayList<Integer>) meansForLine);
        List<Integer> finalMeansForLine = meansForLine;
        ArrayList<Word> matches = (ArrayList<Word>) allMatch.stream().filter(x-> finalMeansForLine.stream().anyMatch(i -> i.equals(x.getyMean()))).collect(Collectors.toList());
        assertEquals(matches.size(),allMatch.size());

        ///Test case 2 partial match//

        List<Integer> meansForLine2 = new ArrayList<>(Arrays.asList(100,130,150,200));
        ArrayList<Word> partialMatch = generateTestWordsByYMeans(new ArrayList<>(Arrays.asList(100,130,400,500,800)));
        matches = (ArrayList<Word>) partialMatch.stream().filter(x-> meansForLine2.stream().anyMatch(i -> i.equals(x.getyMean()))).collect(Collectors.toList());
        assertEquals(matches.size(),2);

        ///Test case 3 no words match //

        List<Integer> meansForLine3 = new ArrayList<>(Arrays.asList(100,130,150,200));
        ArrayList<Word> noMatch = generateTestWordsByYMeans(new ArrayList<>(Arrays.asList(700,630,400,500,800)));
        matches = (ArrayList<Word>) noMatch.stream().filter(x-> meansForLine3.stream().anyMatch(i -> i.equals(x.getyMean()))).collect(Collectors.toList());
        assertEquals(matches.size(),0);

        ///Test case 4 partial match //

        List<Integer> meansForLine4 = new ArrayList<>(Arrays.asList(100,500,200,350,700));
        ArrayList<Word> partialMatch2 = generateTestWordsByYMeans(new ArrayList<>(Arrays.asList(700,200,400,500,100)));
        matches = (ArrayList<Word>) partialMatch2.stream().filter(x-> meansForLine4.stream().anyMatch(i -> i.equals(x.getyMean()))).collect(Collectors.toList());
        assertEquals(matches.size(),4);

        ///Test case 5 all match //

        List<Integer> meansForLine5 = new ArrayList<>(Arrays.asList(100,130,150,200,300,400));
        ArrayList<Word> allMatch2 = generateTestWordsByYMeans(new ArrayList<>(Arrays.asList(100,400)));
        matches = (ArrayList<Word>) allMatch2.stream().filter(x-> meansForLine5.stream().anyMatch(i -> i.equals(x.getyMean()))).collect(Collectors.toList());
        assertEquals(matches.size(),2);
        assertEquals(matches.size(),allMatch2.size());

    }

    @Test
    void testWordCountMatching() throws IOException {
        ///Test case 1 num words should be both 100//
        Page origPage = generateTestPage(1,100);
        Page newPage = generateTestPage(10,10);

        int origWords = origPage.getLines().get(0).getWords().size();
        int newWords = (int) newPage.getLines().stream().map(Line::getWords).mapToLong(List::size).sum();

        assertEquals(origWords,100);
        assertEquals(newWords,100);
        assertEquals(origWords,newWords);

        // Test case 2 num words should be both 10//

        Page origPage1 = generateTestPage(1,10);
        Page newPage1 = generateTestPage(10,1);

        int origWords1 = origPage1.getLines().get(0).getWords().size();
        int newWords1 = (int) newPage1.getLines().stream().map(Line::getWords).mapToLong(List::size).sum();

        assertEquals(origWords1,10);
        assertEquals(newWords1,10);
        assertEquals(origWords1,newWords1);

        // Test case 3 words should mismatch//

        Page origPage2 = generateTestPage(1,235);
        Page newPage2 = generateTestPage(10,10);

        int origWords2 = origPage2.getLines().get(0).getWords().size();
        int newWords2 = (int) newPage2.getLines().stream().map(Line::getWords).mapToLong(List::size).sum();

        assertEquals(origWords2,235);
        assertEquals(newWords2,100);
        assertNotEquals(origWords2,newWords2);

        // Test case 4 words should mismatch //

        Page origPage3 = generateTestPage(1,125);
        Page newPage3 = generateTestPage(10,1);

        int origWords3 = origPage3.getLines().get(0).getWords().size();
        int newWords3 = (int) newPage3.getLines().stream().map(Line::getWords).mapToLong(List::size).sum();

        assertEquals(origWords3,125);
        assertEquals(newWords3,10);
        assertNotEquals(origWords3,newWords3);
    }


    @Test
    void basicExtractionTest() throws IOException {
        ///Basic test for build, ensure that sample pdfs go through and produce hocr result
        MetaData uploadedFile = new MetaData();
        uploadedFile.key = "sample_key5";
        uploadedFile.fileName = "sample_output";
        uploadedFile.sortedImagePaths =test_blank;
        uploadedFile.shipmentId = "test_shipmentId";
        MetaData metaData = extractContent.execute(uploadedFile);
        Document document = loadFromDisk(metaData.extractedOCRDocumentPath);
        assert(document.getPages().size()==3);}

    @Test
    void testLargeFileNumPages() throws IOException {
        String path = testCases + "large_files/test/";
        File dir = new File(path);
        for(File d : dir.listFiles()){
            //Need to run setup to clear previous hocr values
            String imagePath = d.getName();
            String key = imagePath;
//            String files = Arrays.asList(d.)
            /// in this case not sorted by page - here just testing performance
            ArrayList<String> mockMetaDataPaths = new ArrayList<>();
            for (File file: d.listFiles()){
                mockMetaDataPaths.add(file.getAbsolutePath());
            }
            long startTime = System.nanoTime();
            extractFromPath(d.getAbsolutePath(),mockMetaDataPaths,key,d.getName());
            long endTime = System.nanoTime();
            long duration = (endTime - startTime)/ 1_000_000_000;
            System.out.println(duration);
        }

    }


    @Disabled
    @Test
    void generateOutput() throws IOException {
        ArrayList<String>  pathReg1 = new ArrayList<>(Collections.singletonList("src\\test\\resources\\build_data\\standard\\regression\\comparison_pngs\\0547240.jpg"));
        ArrayList<String>  pathReg2 = new ArrayList<>(Arrays.asList("src\\test\\resources\\build_data\\standard\\regression\\comparison_pngs\\A05BER07D99118B15_01_10.jpg","src\\test\\resources\\build_data\\standard\\regression\\comparison_pngs\\A05BER07D99118B15_01_11.jpg"));
        MetaData uploadedFile1 = new MetaData();
        uploadedFile1.fileName = "A05BER07D99118B15_01_1";
        uploadedFile1.sortedImagePaths =pathReg2;
        uploadedFile1.shipmentId = "test_shipmentId";

        MetaData metaData = extractContent.execute(uploadedFile1);
        Document document = loadFromDisk(metaData.extractedOCRDocumentPath);
        assert(document!=null);
        File fileCSV = new File("src/test/resources/build_data/standard/regression/curr_psm1_html/"+"A05BER07D99118B15_01_1.html");
        OutputStreamWriter writer =
                new OutputStreamWriter(new FileOutputStream(fileCSV), StandardCharsets.UTF_8);

        writer.close();
    }

}
