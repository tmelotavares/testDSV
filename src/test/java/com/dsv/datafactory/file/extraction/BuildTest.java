package com.dsv.datafactory.file.extraction;


import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.domain.ExtractContent;

import com.dsv.datafactory.model.MetaData;
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

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;

///// path names must used forward "/" when building in linux env

@Disabled // Disabled due to missing resource files
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BuildTest {
    // Tests to run when building jar file- much is taken from UnpackExtractHOCR but with specific examples
    private ExtractContent extractContent;
    private File standard =new File("src/test/resources/build_data/standard/pdf_files");
    private ArrayList<String> newFormat =  new ArrayList<>(Arrays.asList("sample_outputalice_5_pg0","sample_outputalice_5_pg1","sample_outputalice_5_pg2","sample_outputalice_5_pg3","sample_outputalice_5_pg4"));

    @BeforeAll
    void setup() {
        Module modules = Modules.combine(getTestConfigModule());
        Injector injector = Guice.createInjector(modules);
        extractContent = injector.getInstance(ExtractContent.class);
    }

    private Module getTestConfigModule() {
        return new AbstractModule() {
            @Override protected void configure() {
                Config config = new Config();
                bind(Config.class).toInstance(config);
            }
        };
    }


    @Test
    void basicExtractionTest() throws IOException {
        ///Basic test for build, ensure that sample pdfs go through and produce hocr result
        File[] files = standard.listFiles();
        for (File file:files){
            MetaData imageExtractionMetadata = new MetaData();
            imageExtractionMetadata.fileName = file.getName();
            imageExtractionMetadata.sortedImagePaths =newFormat;
            MetaData extraction = extractContent.execute(imageExtractionMetadata);
            assertNotNull(extraction);}
        }
    }

