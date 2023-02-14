package com.dsv.datafactory.file.extraction;

import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.domain.ExtractContent;
import com.dsv.datafactory.file.extraction.processor.domain.ExtractLines;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcr;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcrP;
import com.dsv.datafactory.model.Document;
import com.dsv.datafactory.model.Line;
import com.dsv.datafactory.model.MetaData;
import com.dsv.datafactory.model.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled
public class PerformanceTests {

    public GoogleOcr ocr = new GoogleOcr();
    public GoogleOcrP refac ;
    public Config config = new Config();
    ExtractLines extractLines ;
    private ExtractContent extractContent;

    private Path mid = Paths.get("src","test","resources","performance","mid/");
    private Path large = Paths.get("src","test","resources","performance","large/");
    private Path small = Paths.get("src","test","resources","performance","small/");

    private ArrayList<String> largeImgs =  new ArrayList<>(Arrays.asList(large.toAbsolutePath()+"/00c2393325a10e2e09e3fd0e2322834cb6aa6207233248827f5aa596a497c7840.png",large.toAbsolutePath()+"/00c2393325a10e2e09e3fd0e2322834cb6aa6207233248827f5aa596a497c7841.png",large.toAbsolutePath()+"/00c2393325a10e2e09e3fd0e2322834cb6aa6207233248827f5aa596a497c7842.png"));
    ObjectMapper mapper = new ObjectMapper();


    // Full flow no Jenks

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

    @Test
    public void testLargeFiles() throws IOException {
        ///Test bottle neck for files with over 6k words
        config.lineServiceUrl = "http://localhost:8005/jenks/clustering";
        config.startNumberOfClasses = "5";
        config.goodnessOfFit = ".999";
        extractLines = new ExtractLines(config);
        long startTime = System.currentTimeMillis();
        Document document = ocr.generateDocument(largeImgs,"large_file");
        long endTime = System.currentTimeMillis();
        System.out.println(endTime-startTime);
        long startJenksTime = System.currentTimeMillis();
        extractLines.generateInputFromDocument(document);
        long endJenksTime = System.currentTimeMillis();
        System.out.println(endJenksTime-startJenksTime);
    }

    @Test
    public void testLargeFilesAverage() throws IOException {
        ///Test bottle neck for files with over 6k words
        config.lineServiceUrl = "http://localhost:8005/jenks/clustering";
        config.startNumberOfClasses = "5";
        config.goodnessOfFit = ".999";
        extractLines = new ExtractLines(config);
        int averageTime = 0;
        Integer averageWord = 0;
        int averageJenks = 0;
        for (File file : large.toAbsolutePath().toFile().listFiles()) {
            ArrayList<String> sortedImageArray = new ArrayList<>();
            for (File img : file.listFiles()) {

                sortedImageArray.add(img.getAbsolutePath());
            }

            long startTime = System.currentTimeMillis();
            Document document = ocr.generateDocument(sortedImageArray, "large_file");
            long endTime = System.currentTimeMillis();
            int numWords = document.getPages().stream().map(i -> i.getLines().stream().map(w -> w.getWords().size()).reduce(0, Integer::sum)).reduce(0, Integer::sum);
            averageWord += numWords;
            System.out.println("Num Words " + numWords);
            averageTime += endTime - startTime;
            System.out.println( "Time taken to process " + file.getName()  + "  " + (endTime - startTime));
            long startJenksTime = System.currentTimeMillis();
            extractLines.generateInputFromDocument(document);
            long endJenksTime = System.currentTimeMillis();
            averageJenks += endJenksTime - startJenksTime;
            System.out.println(endJenksTime - startJenksTime);
        }
        System.out.println("Average jenks processing time " + averageJenks / large.toAbsolutePath().toFile().listFiles().length);
        System.out.println("Average gv processing time " + averageTime /  large.toAbsolutePath().toFile().listFiles().length);
        System.out.println("Average words " + averageWord/ large.toAbsolutePath().toFile().listFiles().length);
    }

    @Test
    public void testLargeFilesAverageRefact() throws IOException {
        ///Test bottle neck for files with over 6k words
        config.lineServiceUrl = "http://localhost:8005/jenks/clustering";
        config.startNumberOfClasses = "5";
        config.goodnessOfFit = ".999";
        extractLines = new ExtractLines(config);
        int averageTime = 0;
        Integer averageWord = 0;
        int averageJenks = 0;
        for (File file : large.toAbsolutePath().toFile().listFiles()) {
            ArrayList<String> sortedImageArray = new ArrayList<>();
            for (File img : file.listFiles()) {
                sortedImageArray.add(img.getAbsolutePath());
            }
            long startTime = System.currentTimeMillis();
            Document document = refac.generateDocument(sortedImageArray, "large_file");
            long endTime = System.currentTimeMillis();
            int numWords = document.getPages().stream().map(i -> i.getLines().stream().map(w -> w.getWords().size()).reduce(0, Integer::sum)).reduce(0, Integer::sum);
            averageWord += numWords;
            System.out.println("Num Words " + numWords);
            averageTime += endTime - startTime;
            System.out.println( "Time taken to process " + file.getName()  + "  " + (endTime - startTime));
            long startJenksTime = System.currentTimeMillis();
            extractLines.generateInputFromDocument(document);
            long endJenksTime = System.currentTimeMillis();
            averageJenks += endJenksTime - startJenksTime;
            System.out.println(endJenksTime - startJenksTime);
        }
        System.out.println("Average jenks processing time " + averageJenks / large.toAbsolutePath().toFile().listFiles().length);
        System.out.println("Average gv processing time " + averageTime /  large.toAbsolutePath().toFile().listFiles().length);
        System.out.println("Average words " + averageWord/ large.toAbsolutePath().toFile().listFiles().length);
    }

    @Test
    public void testLargeFilesIsolatedGvision() throws IOException {
        ///Test bottle neck for files with over 6k words
        int allFiles = 0;
        int averagePerDoc = 0;
        for (File file : large.toAbsolutePath().toFile().listFiles()) {
            int averageTime = 0;
            allFiles += file.listFiles().length;
            for (File img : file.listFiles()) {
                Image processedImage = ocr.processImg(img.getAbsolutePath());
                List<AnnotateImageRequest> requests = ocr.generatePngRequest(processedImage, Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build());
                long startTime = System.currentTimeMillis();
                ocr.retrieveAnnotatedImageResponse(requests); /// isolating only GV call
                long endTime = System.currentTimeMillis();
                averageTime += endTime - startTime;
                System.out.println("Time taken for "+img.getName() +" " + (endTime - startTime));
            }
            System.out.println( "Average time taken for : " + file.getName() + " total "+averageTime );
            System.out.println( "Average time taken for : " + file.getName() + " per img "+averageTime / file.listFiles().length);

        }
        System.out.println("Average time taken all files " + averagePerDoc/allFiles);
    }

    @Test
    public void testLargeFilesIsolatedGvisionBulk() throws IOException {
        ///Test bottle neck for files with over 6k words
        int allFiles = 0;
        int averagePerDoc = 0;
        for (File file : large.toAbsolutePath().toFile().listFiles()) {
            int averageTime = 0;
            ArrayList<Image> images = Arrays.stream(file.listFiles()).map(x-> {
                try {
                    return ocr.processImg(x.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
                List<AnnotateImageRequest> requests = ocr.bulkGeneratePngRequest(images, Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build());
                long startTime = System.currentTimeMillis();
                List<AnnotateImageResponse> responses = ocr.retrieveAnnotatedImageResponseAll(requests); /// isolating only GV call
                long endTime = System.currentTimeMillis();
                averageTime += endTime - startTime;
                averagePerDoc += averageTime;
            System.out.println( "Average time taken for : " + file.getName() + "  "+(endTime - startTime));;
        }
        System.out.println("Average time taken all files " + averagePerDoc/large.toAbsolutePath().toFile().listFiles().length);
        }
    @Test
    public void testLargeFilesIsolatedGvisionPara() throws IOException {
        ///Test bottle neck for files with over 6k words
        int allFiles = 0;
        int averagePerDoc = 0;
        for (File file : large.toAbsolutePath().toFile().listFiles()) {
            int averageTime = 0;
            ArrayList<Image> images = Arrays.stream(file.listFiles()).map(x-> {
                try {
                    return ocr.processImg(x.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
            List<AnnotateImageRequest> requests = ocr.bulkGeneratePngRequest(images, Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build());
            long startTime = System.currentTimeMillis();
            List<AnnotateImageResponse> responses = requests.parallelStream().map(x -> {
                try {
                    return ocr.retrieveAnnotatedImageResponse(Collections.singletonList(x));
                } catch (IOException e) {
                    e.printStackTrace();
                }return null;
            }).collect(Collectors.toCollection(ArrayList::new));
            //isolating only GV call
            long endTime = System.currentTimeMillis();
            averageTime += endTime - startTime;
            averagePerDoc += averageTime;
            System.out.println( "Average time taken for : " + file.getName() + "  "+(endTime - startTime));;
        }
        System.out.println("Average time taken all files " + averagePerDoc/large.toAbsolutePath().toFile().listFiles().length);
    }

    @Test
    public void testFileOverThresholdGV() throws IOException {
        ArrayList<String> pathImages = Arrays.stream(new File(large + "/many_pages/").listFiles()).map(x -> x.getAbsolutePath()).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Image> images = refac.generateImages(pathImages);
        List<AnnotateImageRequest>requests = refac.bulkGeneratePngRequest(images,Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build() );
        long startTime = System.currentTimeMillis();
        List<AnnotateImageResponse>responses = refac.extractFullDocumentResponse(requests);
        long endTime = System.currentTimeMillis();
        System.out.println(endTime-startTime);
        assert(responses.size() == 126);
    }

    @Test
    public void testFileOverThresholdGVPagesInOrder() throws IOException {
        ArrayList<String> pathImages = new ArrayList<>();
        pathImages.addAll(Arrays.stream(new File(large + "/large_1/").listFiles()).map(x -> x.getAbsolutePath()).collect(Collectors.toCollection(ArrayList::new)));
        Document document = refac.generateDocument(pathImages,"test_doc");
        assert(document.getPages().size() == 3);
        for (int i=0; i<document.getPages().size(); i++){
            // assert page keys are in order
            System.out.println(document.getPages().get(i).getPageKey());
            System.out.println(pathImages.get(i));
            assert(document.getPages().get(i).getPageKey().equals(pathImages.get(i)));
            assert(document.getPages().get(i).getPageNumber() == i);
        }
    }

    @Test
    public void testImageCreationMaintainsOrder() throws IOException {
        ArrayList<String> pathImages = new ArrayList<>();
        pathImages.addAll(Arrays.stream(new File(large + "/many_pages/").listFiles()).map(x -> x.getAbsolutePath()).collect(Collectors.toCollection(ArrayList::new)).subList(0,50));
        Document document = refac.generateDocument(pathImages,"test_doc");
        assert(document.getPages().size() == 50);
        for (int i=0; i<document.getPages().size(); i++){
            // assert page keys are in order
            System.out.println(document.getPages().get(i).getPageKey());
            System.out.println(pathImages.get(i));
            assert(document.getPages().get(i).getPageKey().equals(pathImages.get(i)));
            assert(document.getPages().get(i).getPageNumber() == i);
        }

    }

    @Test
    public void testEntireExtraction(){
        ///Test bottleneck for files with over 6k words
            ///Test bottle neck for files with over 6k words
            config.lineServiceUrl = "http://localhost:8005/jenks/clustering";
            config.startNumberOfClasses = "5";
            config.goodnessOfFit = ".999";
            extractLines = new ExtractLines(config);
            int averageTime = 0;
            for (File file : large.toAbsolutePath().toFile().listFiles()) {
                ArrayList<String> sortedImageArray = new ArrayList<>();
                for (File img : file.listFiles()) {
                    sortedImageArray.add(img.getAbsolutePath());
                }
                MetaData document = new MetaData();
                document.sortedImagePaths = sortedImageArray;
                document.key = file.getName();
                document.shipmentId = file.getName();
                long startTime = System.currentTimeMillis();
                MetaData result = extractContent.execute(document);
                long endTime = System.currentTimeMillis();
                assert(result.extractedOCRDocumentPath!=null);
                averageTime += endTime - startTime;
                System.out.println( "Average time taken for : " + file.getName() + "  "+(endTime - startTime));;
            }
            System.out.println("Average gv processing time " + averageTime /  large.toAbsolutePath().toFile().listFiles().length);

        }

    @Test
    public void testEntireExtractionProblemFilesBlanks() throws IOException {
        ///Test bottleneck for files with over 6k words
        ///Test bottle neck for files with over 6k words
        config.lineServiceUrl = "http://localhost:8005/jenks/clustering";
        config.startNumberOfClasses = "5";
        config.goodnessOfFit = ".999";
        extractLines = new ExtractLines(config);
        int averageTime = 0;
        ArrayList<String> pageKeysBlank = new ArrayList<>(Arrays.asList("983731","983733","983735","983737","983739","9837311","9837313"));
        // This file contains 8 blanks
        Path problemFiles = Paths.get("src","test","resources","prd_images_98373");
            ArrayList<String> sortedImageArray = new ArrayList<>();
            for (File img : problemFiles.toAbsolutePath().toFile().listFiles()) {
                sortedImageArray.add(img.getAbsolutePath());
            }
            MetaData document = new MetaData();
            document.sortedImagePaths = sortedImageArray;
            document.key = "98373";
            document.shipmentId = "98373";
            long startTime = System.currentTimeMillis();
            MetaData result = extractContent.execute(document);
            long endTime = System.currentTimeMillis();
            assert(result.extractedOCRDocumentPath!=null);
            averageTime += endTime - startTime;
            System.out.println( "Average time taken for : " +"98373"+ "  "+ averageTime);;
            Document doc = mapper.readValue(Paths.get("src","test","resources","hocr_results/98373/98373.json").toAbsolutePath().toFile(),Document.class);
            assert( doc.getPages().size() == 7);
            assert(!doc.getPages().stream().map(Page::getPageKey).collect(Collectors.toCollection(ArrayList::new)).containsAll(pageKeysBlank));
            assert(doc.getPages().stream().filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)).size()==doc.getPages().size());
            assert(doc.getPages().stream().filter(x->x.getLanguage()!=null).collect(Collectors.toCollection(ArrayList::new)).size()==doc.getPages().size());

    }
}
