package com.dsv.datafactory.file.extraction;
import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcr;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcrP;
import com.dsv.datafactory.file.extraction.processor.models.GoogleVisionResponse;
import com.dsv.datafactory.model.*;
import com.dsv.datafactory.model.Word;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dsv.datafactory.file.extraction.processor.models.*;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RotationLogicTest {
    //GoogleOcr refac = new GoogleOcr();
    GoogleOcrP refac ;
    GoogleVisionResponse ocr270; //= loadGoogleVisionResponse("src/test/resources/SerializedGoogleVisionResponses/0be2936c091083d30d5ec089e1c26ecd4becfa0a8b511c83febc8c57bc3d2cdc1.json");
    GoogleVisionResponse ocr90; //= loadGoogleVisionResponse("src/test/resources/SerializedGoogleVisionResponses/4ff6d7edf0d7f06f53cd480a21ccaabaab1ad426fb471898741d8bde05b5f9f84.json");
    GoogleVisionResponse ocr180; //= loadGoogleVisionResponse("src/test/resources/SerializedGoogleVisionResponses/3b391e3b3fd948c2eed56b20a68ef39178736ca9f5d0c8c5ff025a3a348f22093.json");
    GoogleVisionResponse ocr0;//= loadGoogleVisionResponse("src/test/resources/SerializedGoogleVisionResponses/00b7ec45d2877c2aedf6d8c07b21a0aa50d62d1a2dcde7531acb68441889f03a0.json");


    public RotationLogicTest() throws IOException {
    }

    @BeforeEach
    void setup(){
        Config config = new Config();
        config.runGVInPararell = "false";
        refac = new GoogleOcrP(config);
        ocr270 = loadGoogleVisionResponse("src/test/resources/SerializedGoogleVisionResponses/0be2936c091083d30d5ec089e1c26ecd4becfa0a8b511c83febc8c57bc3d2cdc1.json");
        ocr90 = loadGoogleVisionResponse("src/test/resources/SerializedGoogleVisionResponses/4ff6d7edf0d7f06f53cd480a21ccaabaab1ad426fb471898741d8bde05b5f9f84.json");
        ocr180 = loadGoogleVisionResponse("src/test/resources/SerializedGoogleVisionResponses/3b391e3b3fd948c2eed56b20a68ef39178736ca9f5d0c8c5ff025a3a348f22093.json");
        ocr0 = loadGoogleVisionResponse("src/test/resources/SerializedGoogleVisionResponses/00b7ec45d2877c2aedf6d8c07b21a0aa50d62d1a2dcde7531acb68441889f03a0.json");

    }

    @Test
    void getMinMaxCoordinatesTest0(){
        EntityAnnotation entity1 = ocr0.getTextAnnotations().get(5);
        EntityAnnotation entity2 = ocr0.getTextAnnotations().get(7);
        BoundingPoly bp1 = getBoundingPoly(entity1);
        BoundingPoly bp2 = getBoundingPoly(entity2);
        int[] minMaxCoordinates1 = refac.getMinMaxCoordinatesFromVertices(bp1);
        int[] minMaxCoordinates2 = refac.getMinMaxCoordinatesFromVertices(bp2);

        Assertions.assertEquals(minMaxCoordinates1[0], 912);
        Assertions.assertEquals(minMaxCoordinates1[1], 69);
        Assertions.assertEquals(minMaxCoordinates1[2], 1023);
        Assertions.assertEquals(minMaxCoordinates1[3], 107);

        Assertions.assertEquals(minMaxCoordinates2[0], 1083);
        Assertions.assertEquals(minMaxCoordinates2[1], 69);
        Assertions.assertEquals(minMaxCoordinates2[2], 1088);
        Assertions.assertEquals(minMaxCoordinates2[3], 107);
    }

    @Test
    void getMinMaxCoordinatesTest270(){
        EntityAnnotation entity3 = ocr270.getTextAnnotations().get(3);
        EntityAnnotation entity4 = ocr270.getTextAnnotations().get(10);
        BoundingPoly bp3 = getBoundingPoly(entity3);
        BoundingPoly bp4 = getBoundingPoly(entity4);

        int[] minMaxCoordinates3 = refac.getMinMaxCoordinatesFromVertices(bp3);
        int[] minMaxCoordinates4 = refac.getMinMaxCoordinatesFromVertices(bp4);

        Assertions.assertEquals(minMaxCoordinates3[0], 98);
        Assertions.assertEquals(minMaxCoordinates3[1], 46);
        Assertions.assertEquals(minMaxCoordinates3[2], 117);
        Assertions.assertEquals(minMaxCoordinates3[3], 58);

        Assertions.assertEquals(minMaxCoordinates4[0], 153);
        Assertions.assertEquals(minMaxCoordinates4[1], 1846);
        Assertions.assertEquals(minMaxCoordinates4[2], 171);
        Assertions.assertEquals(minMaxCoordinates4[3], 1861);
    }

    @Test
    void getMinMaxCoordinatesTest180(){
        EntityAnnotation entity5 = ocr180.getTextAnnotations().get(1);
        EntityAnnotation entity6 = ocr180.getTextAnnotations().get(7);
        BoundingPoly bp5 = getBoundingPoly(entity5);
        BoundingPoly bp6 = getBoundingPoly(entity6);

        int[] minMaxCoordinates5 = refac.getMinMaxCoordinatesFromVertices(bp5);
        int[] minMaxCoordinates6 = refac.getMinMaxCoordinatesFromVertices(bp6);

        Assertions.assertEquals(minMaxCoordinates5[0], 1526);
        Assertions.assertEquals(minMaxCoordinates5[1], 2268);
        Assertions.assertEquals(minMaxCoordinates5[2], 1630);
        Assertions.assertEquals(minMaxCoordinates5[3], 2288);

        Assertions.assertEquals(minMaxCoordinates6[0], 683);
        Assertions.assertEquals(minMaxCoordinates6[1], 2256);
        Assertions.assertEquals(minMaxCoordinates6[2], 726);
        Assertions.assertEquals(minMaxCoordinates6[3], 2284);
    }

    @Test
    void getMinMaxCoordinatesTest90(){
        EntityAnnotation entity7 = ocr90.getTextAnnotations().get(5);
        EntityAnnotation entity8 = ocr90.getTextAnnotations().get(2);
        BoundingPoly bp7 = getBoundingPoly(entity7);
        BoundingPoly bp8 = getBoundingPoly(entity8);

        int[] minMaxCoordinates7 = refac.getMinMaxCoordinatesFromVertices(bp7);
        int[] minMaxCoordinates8 = refac.getMinMaxCoordinatesFromVertices(bp8);

        Assertions.assertEquals(minMaxCoordinates7[0], 1432);
        Assertions.assertEquals(minMaxCoordinates7[1], 130);
        Assertions.assertEquals(minMaxCoordinates7[2], 1453);
        Assertions.assertEquals(minMaxCoordinates7[3], 205);

        Assertions.assertEquals(minMaxCoordinates8[0], 1488);
        Assertions.assertEquals(minMaxCoordinates8[1], 171);
        Assertions.assertEquals(minMaxCoordinates8[2], 1514);
        Assertions.assertEquals(minMaxCoordinates8[3], 193);
    }

    @Test
    void createWordVerticeTest0(){
        EntityAnnotation entity1 = ocr0.getTextAnnotations().get(8);
        EntityAnnotation entity2 = ocr0.getTextAnnotations().get(15);
        BoundingPoly bp1 = getBoundingPoly(entity1);
        BoundingPoly bp2 = getBoundingPoly(entity2);
        int[] minMaxCoordinates1 = refac.getMinMaxCoordinatesFromVertices(bp1);
        int[] minMaxCoordinates2 = refac.getMinMaxCoordinatesFromVertices(bp2);

        List<Vertices> rawVertice1 = getVertices(getVertices(bp1));
        List<Vertices> rawVertice2 = getVertices(getVertices(bp2));
        List<Vertices> correctedVertice1 = getCorrectedVertices(getVertices(bp1), minMaxCoordinates1);
        List<Vertices> correctedVertice2 = getCorrectedVertices(getVertices(bp2), minMaxCoordinates2);

        int[] rawVerticeArray1 = verticeToArray(rawVertice1);
        int[] rawVerticeArray2 = verticeToArray(rawVertice2);
        int[] correctedVerticeArray1 = verticeToArray(correctedVertice1);
        int[] correctedVerticeArray2 = verticeToArray(correctedVertice2);

        int[] rawVerticeReference1 = {1095, 69, 1139, 69, 1139, 107, 1095, 107};
        int[] rawVerticeReference2 = {572, 124, 651, 124, 651, 155, 572, 155};
        int[] correctedVerticeReference1 = {1095, 69, 1139, 69, 1139, 107, 1095, 107};
        int[] correctedVerticeReference2 = {572, 124, 651, 124, 651, 155, 572, 155};

        Assertions.assertArrayEquals(rawVerticeArray1, rawVerticeReference1);
        Assertions.assertArrayEquals(rawVerticeArray2, rawVerticeReference2);
        Assertions.assertArrayEquals(correctedVerticeArray1, correctedVerticeReference1);
        Assertions.assertArrayEquals(correctedVerticeArray2, correctedVerticeReference2);

    }

    @Test
    void createWordVerticeTest270(){
        EntityAnnotation entity1 = ocr270.getTextAnnotations().get(3);
        EntityAnnotation entity2 = ocr270.getTextAnnotations().get(10);
        BoundingPoly bp1 = getBoundingPoly(entity1);
        BoundingPoly bp2 = getBoundingPoly(entity2);
        int[] minMaxCoordinates1 = refac.getMinMaxCoordinatesFromVertices(bp1);
        int[] minMaxCoordinates2 = refac.getMinMaxCoordinatesFromVertices(bp2);

        List<Vertices> rawVertice1 = getVertices(getVertices(bp1));
        List<Vertices> rawVertice2 = getVertices(getVertices(bp2));
        List<Vertices> correctedVertice1 = getCorrectedVertices(getVertices(bp1), minMaxCoordinates1);
        List<Vertices> correctedVertice2 = getCorrectedVertices(getVertices(bp2), minMaxCoordinates2);

        int[] rawVerticeArray1 = verticeToArray(rawVertice1);
        int[] rawVerticeArray2 = verticeToArray(rawVertice2);
        int[] correctedVerticeArray1 = verticeToArray(correctedVertice1);
        int[] correctedVerticeArray2 = verticeToArray(correctedVertice2);

        int[] rawVerticeReference1 = {98, 58, 98, 46, 117, 46, 117, 58};
        int[] rawVerticeReference2 = {153, 1861, 153, 1846, 171, 1846, 171, 1861};
        int[] correctedVerticeReference1 = {98, 58, 98, 46, 117, 46, 117, 58};
        int[] correctedVerticeReference2 = {153, 1861, 153, 1846, 171, 1846, 171, 1861};

        Assertions.assertArrayEquals(rawVerticeArray1, rawVerticeReference1);
        Assertions.assertArrayEquals(rawVerticeArray2, rawVerticeReference2);
        Assertions.assertArrayEquals(correctedVerticeArray1, correctedVerticeReference1);
        Assertions.assertArrayEquals(correctedVerticeArray2, correctedVerticeReference2);

    }

    @Test
    void createWordVerticeTest180(){
        EntityAnnotation entity1 = ocr180.getTextAnnotations().get(1);
        EntityAnnotation entity2 = ocr180.getTextAnnotations().get(7);
        BoundingPoly bp1 = getBoundingPoly(entity1);
        BoundingPoly bp2 = getBoundingPoly(entity2);
        int[] minMaxCoordinates1 = refac.getMinMaxCoordinatesFromVertices(bp1);
        int[] minMaxCoordinates2 = refac.getMinMaxCoordinatesFromVertices(bp2);

        List<Vertices> rawVertice1 = getVertices(getVertices(bp1));
        List<Vertices> rawVertice2 = getVertices(getVertices(bp2));
        List<Vertices> correctedVertice1 = getCorrectedVertices(getVertices(bp1), minMaxCoordinates1);
        List<Vertices> correctedVertice2 = getCorrectedVertices(getVertices(bp2), minMaxCoordinates2);

        int[] rawVerticeArray1 = verticeToArray(rawVertice1);
        int[] rawVerticeArray2 = verticeToArray(rawVertice2);
        int[] correctedVerticeArray1 = verticeToArray(correctedVertice1);
        int[] correctedVerticeArray2 = verticeToArray(correctedVertice2);

        int[] rawVerticeReference1 = {1630, 2288, 1526, 2287, 1526, 2268, 1630, 2269};
        int[] rawVerticeReference2 = {726, 2284, 683, 2284, 683, 2256, 726, 2256};
        int[] correctedVerticeReference1 = {1630, 2288, 1526, 2288, 1526, 2268, 1630, 2268};
        int[] correctedVerticeReference2 = {726, 2284, 683, 2284, 683, 2256, 726, 2256};

        Assertions.assertArrayEquals(rawVerticeArray1, rawVerticeReference1);
        Assertions.assertArrayEquals(rawVerticeArray2, rawVerticeReference2);
        Assertions.assertArrayEquals(correctedVerticeArray1, correctedVerticeReference1);
        Assertions.assertArrayEquals(correctedVerticeArray2, correctedVerticeReference2);
    }
    @Test
    void createWordVerticeTest90(){
        EntityAnnotation entity1 = ocr90.getTextAnnotations().get(4);
        EntityAnnotation entity2 = ocr90.getTextAnnotations().get(9);
        BoundingPoly bp1 = getBoundingPoly(entity1);
        BoundingPoly bp2 = getBoundingPoly(entity2);
        int[] minMaxCoordinates1 = refac.getMinMaxCoordinatesFromVertices(bp1);
        int[] minMaxCoordinates2 = refac.getMinMaxCoordinatesFromVertices(bp2);

        List<Vertices> rawVertice1 = getVertices(getVertices(bp1));
        List<Vertices> rawVertice2 = getVertices(getVertices(bp2));
        List<Vertices> correctedVertice1 = getCorrectedVertices(getVertices(bp1), minMaxCoordinates1);
        List<Vertices> correctedVertice2 = getCorrectedVertices(getVertices(bp2), minMaxCoordinates2);

        int[] rawVerticeArray1 = verticeToArray(rawVertice1);
        int[] rawVerticeArray2 = verticeToArray(rawVertice2);
        int[] correctedVerticeArray1 = verticeToArray(correctedVertice1);
        int[] correctedVerticeArray2 = verticeToArray(correctedVertice2);

        int[] rawVerticeReference1 = {1513, 952, 1513, 1002, 1484, 1002, 1484, 952};
        int[] rawVerticeReference2 = {1451, 483, 1451, 529, 1430, 529, 1430, 483};
        int[] correctedVerticeReference1 = {1513, 952, 1513, 1002, 1484, 1002, 1484, 952};
        int[] correctedVerticeReference2 = {1451, 483, 1451, 529, 1430, 529, 1430, 483};

        Assertions.assertArrayEquals(rawVerticeArray1, rawVerticeReference1);
        Assertions.assertArrayEquals(rawVerticeArray2, rawVerticeReference2);
        Assertions.assertArrayEquals(correctedVerticeArray1, correctedVerticeReference1);
        Assertions.assertArrayEquals(correctedVerticeArray2, correctedVerticeReference2);
    }

    @Test
    void getWordRotationTest0(){
        EntityAnnotation entity1 = ocr0.getTextAnnotations().get(2);
        EntityAnnotation entity2 = ocr0.getTextAnnotations().get(21);
        EntityAnnotation entity3 = ocr0.getTextAnnotations().get(13);
        EntityAnnotation entity4 = ocr0.getTextAnnotations().get(9);
        Word word1 = refac.generateWord(entity1);
        Word word2 = refac.generateWord(entity2);
        Word word3 = refac.generateWord(entity3);
        Word word4 = refac.generateWord(entity4);

        Assertions.assertEquals(word1.getRotation(), 0);
        Assertions.assertEquals(word2.getRotation(), 0);
        Assertions.assertEquals(word3.getRotation(), 0);
        Assertions.assertEquals(word4.getRotation(), 0);
    }

    @Test
    void getWordRotationTest90(){
        EntityAnnotation entity1 = ocr90.getTextAnnotations().get(3);
        EntityAnnotation entity2 = ocr90.getTextAnnotations().get(11);
        EntityAnnotation entity3 = ocr90.getTextAnnotations().get(35);
        EntityAnnotation entity4 = ocr90.getTextAnnotations().get(24);
        Word word1 = refac.generateWord(entity1);
        Word word2 = refac.generateWord(entity2);
        Word word3 = refac.generateWord(entity3);
        Word word4 = refac.generateWord(entity4);

        Assertions.assertEquals(word1.getRotation(), 90);
        Assertions.assertEquals(word2.getRotation(), 90);
        Assertions.assertEquals(word3.getRotation(), 90);
        Assertions.assertEquals(word4.getRotation(), 90);
    }

    @Test
    void getWordRotationTest180(){
        EntityAnnotation entity1 = ocr180.getTextAnnotations().get(4);
        EntityAnnotation entity2 = ocr180.getTextAnnotations().get(9);
        EntityAnnotation entity3 = ocr180.getTextAnnotations().get(27);
        EntityAnnotation entity4 = ocr180.getTextAnnotations().get(40);
        Word word1 = refac.generateWord(entity1);
        Word word2 = refac.generateWord(entity2);
        Word word3 = refac.generateWord(entity3);
        Word word4 = refac.generateWord(entity4);

        Assertions.assertEquals(word1.getRotation(), 180);
        Assertions.assertEquals(word2.getRotation(), 180);
        Assertions.assertEquals(word3.getRotation(), 270);
        Assertions.assertEquals(word4.getRotation(), 270);
    }

    @Test
    void getWordRotationTest270(){
        EntityAnnotation entity1 = ocr270.getTextAnnotations().get(4);
        EntityAnnotation entity2 = ocr270.getTextAnnotations().get(9);
        EntityAnnotation entity3 = ocr270.getTextAnnotations().get(27);
        EntityAnnotation entity4 = ocr270.getTextAnnotations().get(40);
        Word word1 = refac.generateWord(entity1);
        Word word2 = refac.generateWord(entity2);
        Word word3 = refac.generateWord(entity3);
        Word word4 = refac.generateWord(entity4);

        Assertions.assertEquals(word1.getRotation(), 270);
        Assertions.assertEquals(word2.getRotation(), 270);
        Assertions.assertEquals(word3.getRotation(), 270);
        Assertions.assertEquals(word4.getRotation(), 270);
    }

    @Test
    void getPageRotationTest0() throws IOException {
        com.dsv.datafactory.model.Page page = generatePage(ocr0);
        Assertions.assertEquals(page.getRotation(), 0);
    }

    @Test
    void getPageRotationTest90() throws IOException {
        com.dsv.datafactory.model.Page page = generatePage(ocr90);
        Assertions.assertEquals(page.getRotation(), 90);
    }

    @Test
    void getPageRotationTest180() throws IOException {
        com.dsv.datafactory.model.Page page = generatePage(ocr180);
        Assertions.assertEquals(page.getRotation(), 180);
    }

    @Test
    void getPageRotationTest270() throws IOException {
        com.dsv.datafactory.model.Page page = generatePage(ocr270);
        Assertions.assertEquals(page.getRotation(), 270);
    }

    @Test
    void correctPageCoordinatesTest0() throws IOException {
        com.dsv.datafactory.model.Page page = generatePage(ocr0);
        int width = ocr0.getFullTextAnnotation().getPages().get(0).getWidth();
        int height = ocr0.getFullTextAnnotation().getPages().get(0).getHeight();
        Assertions.assertEquals(page.getWidth(), width);
        Assertions.assertEquals(page.getHeight(), height);
    }

    @Test
    void correctPageCoordinatesTest90() throws IOException {
        com.dsv.datafactory.model.Page page = generatePage(ocr90);
        int width = ocr90.getFullTextAnnotation().getPages().get(0).getWidth();
        int height = ocr90.getFullTextAnnotation().getPages().get(0).getHeight();
        Assertions.assertEquals(page.getWidth(), height);
        Assertions.assertEquals(page.getHeight(), width);
    }
    @Test
    void correctPageCoordinatesTest180() throws IOException {
        com.dsv.datafactory.model.Page page = generatePage(ocr180);
        int width = ocr180.getFullTextAnnotation().getPages().get(0).getWidth();
        int height = ocr180.getFullTextAnnotation().getPages().get(0).getHeight();
        Assertions.assertEquals(page.getWidth(), width);
        Assertions.assertEquals(page.getHeight(), height);
    }

    @Test
    void correctPageCoordinatesTest270() throws IOException {
        com.dsv.datafactory.model.Page page = generatePage(ocr270);
        int width = ocr270.getFullTextAnnotation().getPages().get(0).getWidth();
        int height = ocr270.getFullTextAnnotation().getPages().get(0).getHeight();
        Assertions.assertEquals(page.getWidth(), height);
        Assertions.assertEquals(page.getHeight(), width);
    }

    @Test
    void correctWordCoordinatesTest0() throws IOException {
        com.dsv.datafactory.model.Page page = generatePage(ocr0);
        List<Word> words= page.getLines().get(0).getWords();
        Word word1 = words.get(2);
        Word word2 = words.get(21);
        Word word3 = words.get(13);
        Word word4 = words.get(9);

        int[] referenceCoordinates1 = {647, 69, 757, 107};
        int[] referenceCoordinates2 = {567, 165, 623, 197};
        int[] referenceCoordinates3 = {561, 124, 568, 155};
        int[] referenceCoordinates4 = {1192, 69, 1197, 107};

        Assertions.assertArrayEquals(boundingBoxToArray(word1.getBoundingBox()), referenceCoordinates1);
        Assertions.assertArrayEquals(boundingBoxToArray(word2.getBoundingBox()), referenceCoordinates2);
        Assertions.assertArrayEquals(boundingBoxToArray(word3.getBoundingBox()), referenceCoordinates3);
        Assertions.assertArrayEquals(boundingBoxToArray(word4.getBoundingBox()), referenceCoordinates4);
    }

    @Test
    void correctWordCoordinatesTest90() throws IOException {
        com.dsv.datafactory.model.Page page = generatePage(ocr90);
        List<Word> words= page.getLines().get(0).getWords();
        Word word1 = words.get(4);
        Word word2 = words.get(9);
        Word word3 = words.get(27);
        Word word4 = words.get(40);

        int[] referenceCoordinates1 = {130, 279, 205, 300};
        int[] referenceCoordinates2 = {541, 281, 573, 302};
        int[] referenceCoordinates3 = {1444, 323, 1452, 354};
        int[] referenceCoordinates4 = {522, 341, 577, 364};

        Assertions.assertArrayEquals(boundingBoxToArray(word1.getBoundingBox()), referenceCoordinates1);
        Assertions.assertArrayEquals(boundingBoxToArray(word2.getBoundingBox()), referenceCoordinates2);
        Assertions.assertArrayEquals(boundingBoxToArray(word3.getBoundingBox()), referenceCoordinates3);
        Assertions.assertArrayEquals(boundingBoxToArray(word4.getBoundingBox()), referenceCoordinates4);
    }

    @Test
    void correctWordCoordinatesTest180() throws IOException {
        com.dsv.datafactory.model.Page page = generatePage(ocr180);
        List<Word> words= page.getLines().get(0).getWords();
        Word word1 = words.get(4);
        Word word2 = words.get(9);
        Word word3 = words.get(27);
        Word word4 = words.get(40);

        int[] referenceCoordinates1 = {1045, 91, 1051, 119};
        int[] referenceCoordinates2 = {1224, 93, 1229, 121};
        int[] referenceCoordinates3 = {1462, 287, 1496, 343};
        int[] referenceCoordinates4 = {1520, 330, 1545, 354};

        Assertions.assertArrayEquals(boundingBoxToArray(word1.getBoundingBox()), referenceCoordinates1);
        Assertions.assertArrayEquals(boundingBoxToArray(word2.getBoundingBox()), referenceCoordinates2);
        Assertions.assertArrayEquals(boundingBoxToArray(word3.getBoundingBox()), referenceCoordinates3);
        Assertions.assertArrayEquals(boundingBoxToArray(word4.getBoundingBox()), referenceCoordinates4);
    }

    @Test
    void correctWordCoordinatesTest270() throws IOException {
        com.dsv.datafactory.model.Page page = generatePage(ocr270);
        List<Word> words= page.getLines().get(0).getWords();
        Word word1 = words.get(4);
        Word word2 = words.get(9);
        Word word3 = words.get(27);
        Word word4 = words.get(40);

        int[] referenceCoordinates1 = {354, 146, 360, 172};
        int[] referenceCoordinates2 = {665, 153, 680, 171};
        int[] referenceCoordinates3 = {1279, 147, 1361, 169};
        int[] referenceCoordinates4 = {433, 199, 522, 221};

        Assertions.assertArrayEquals(boundingBoxToArray(word1.getBoundingBox()), referenceCoordinates1);
        Assertions.assertArrayEquals(boundingBoxToArray(word2.getBoundingBox()), referenceCoordinates2);
        Assertions.assertArrayEquals(boundingBoxToArray(word3.getBoundingBox()), referenceCoordinates3);
        Assertions.assertArrayEquals(boundingBoxToArray(word4.getBoundingBox()), referenceCoordinates4);
    }

    int[] boundingBoxToArray(BoundingBox bbox){
        int[] res = {bbox.getX1(), bbox.getY1(), bbox.getX2(), bbox.getY2()};
        return res;
    }

    int[] verticeToArray(List<Vertices> vertices){
        ArrayList<Integer> res = new ArrayList<>();

        for(Vertices vertice:vertices){
            res.add(vertice.getX());
            res.add(vertice.getY());
        }
        return res.stream().mapToInt(i -> i).toArray();
    }

    BoundingPoly getBoundingPoly(EntityAnnotation word){
        return word.getBoundingPoly();
    }

    List<Vertices> getVertices(BoundingPoly bp){
        List<Vertices> res = new ArrayList<>();
        Vertices topLeft = bp.getVertices().get(0);
        Vertices topRight = bp.getVertices().get(1);
        Vertices lowRight = bp.getVertices().get(2);
        Vertices lowLeft = bp.getVertices().get(3);
        res.add(topLeft);
        res.add(topRight);
        res.add(lowRight);
        res.add(lowLeft);

        return res;
    }

    List<Vertices> getVertices(List<Vertices> VerticesList){
        List<Vertices> res = new ArrayList<>();
        for(Vertices Vertices: VerticesList){
            res.add(new Vertices(Vertices.getX(), Vertices.getY()));
        }
        return res;
    }

    List<Vertices> getCorrectedVertices(List<Vertices> VerticesList, int[] minMaxCoordinates){
        List<Vertices> res = new ArrayList<>();

        for(int i = 0; i<4; ++i ){
            res.add(refac.createWordVertice(VerticesList.get(i),minMaxCoordinates));
        }
        return res;
    }


    public com.dsv.datafactory.model.Page generatePage(GoogleVisionResponse response) throws IOException {
        com.dsv.datafactory.model.Page page = new com.dsv.datafactory.model.Page();
        page.setPageNumber(0);
        page.setPageKey("");
        GooglePage googlePage = response.getFullTextAnnotation().getPages().get(0);
        page.setHeight(googlePage.getHeight());
        page.setWidth(googlePage.getWidth());
        page.setLanguage(refac.retrieveLanguagesFromPage(googlePage));
        List<EntityAnnotation> annotations = response.getTextAnnotations();
        List<Line> lines = refac.generateLineArray(annotations);
        page.setRotation(refac.getPageRotation(lines.get(0).getWords()));
        page.setLines(lines);

        if(page.getRotation() != 0) refac.correctPageCoordinates(page);
        return page;

    }

    GoogleVisionResponse loadGoogleVisionResponse(String path){
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
