package com.dsv.datafactory.file.extraction.processor.models;

import com.dsv.datafactory.model.Vertices;

import java.util.ArrayList;

public class BoundingPoly {
    private ArrayList<Vertices> vertices;
    private ArrayList<NormalizedVertices> normalizedVertices;


    public void setVertices(ArrayList<Vertices> vertices) {
        this.vertices = vertices;
    }

    public void setNormalizedVertices(ArrayList<NormalizedVertices> normalizedVertices) {
        this.normalizedVertices = normalizedVertices;
    }

    public ArrayList<NormalizedVertices> getNormalizedVertices() {
        return normalizedVertices;
    }

    public ArrayList<Vertices> getVertices() {
        return vertices;
    }

    public void normalizeVertices(int width, int height){
        ArrayList<Vertices> normalized = new ArrayList<>();
        for(Vertices vertex : this.vertices){
            int x = vertex.getX()/width;
            int y = vertex.getY()/height;
            normalized.add(new Vertices(x, y));
        }
    }
}
