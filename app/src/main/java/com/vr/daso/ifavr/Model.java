package com.vr.daso.ifavr;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Daniel on 03.11.2015.
 */
public class Model {
    private int nvertices = 0;
    private int npositions = 0;
    private int ntexels = 0;
    private int nnormals = 0;
    private int nfaces = 0;
    private int npoints = 0;
    private int ncolors = 0;

    private int nverticestotal = 0;

//    private float positions[][];    // XYZ
//    private float texels[][];          // UV
//    private float normals[][];        // XYZ
//    private int faces[][];              // PTN PTN PTN

    private ArrayList<float[]> positions = new ArrayList<float[]>();
    private ArrayList<float[]> texels= new ArrayList<float[]>();
    private ArrayList<float[]> normals= new ArrayList<float[]>();
    private ArrayList<int[]> faces= new ArrayList<int[]>();
    private ArrayList<float[]> points= new ArrayList<float[]>();
    private ArrayList<float[]> colors = new ArrayList<float[]>();

    public enum MODE {UNDECIDED, MESH, ATOM};
    private MODE mode;

    boolean finalized = false;

    final String TAG = "ModelObject";

    Model() {
     mode = Model.MODE.UNDECIDED;
    }

    float[] positions() {
        return collectFromFaces(0, DATATYPE.VERTICES);
    }

    void addPositions(float _x, float _y, float _z) {
        if (!finalized) {
            npositions++;
            float[] posar = {_x, _y, _z};
            positions.add(posar);
        }
        else {
            Log.e(TAG, "addPositions could not be called. Model is already finalized!");
        }
    }

    int positionsSize() {
        return npositions;
    }

    Object[] texels() {
        return texels.toArray();
    }

    void addTexels(float _uv1, float _uv2) {
        if (!finalized) {
            ntexels++;
            float[] texar = {_uv1, _uv2};
            texels.add(texar);
        }
        else {
            Log.e(TAG, "addTexels could not be called. Model is already finalized!");
        }
    }

    float[] normals() {
        return collectFromFaces(2, DATATYPE.NORMALS);
    }

    void addNormals(float _x, float _y, float _z) {
        if (!finalized) {
            nnormals++;
            float[] nomar = {_x, _y, _z};
            normals.add(nomar);
        }
        else {
            Log.e(TAG, "addNormals could not be called. Model is already finalized!");
        }
    }

    Object[] faces() {
        return faces.toArray();
    }

    void addFaces(int[] _vals) {
        if (!finalized) {
            nfaces++;
            faces.add(_vals);
        }
        else {
            Log.e(TAG, "addFaces could not be called. Model is already finalized!");
        }
    }

    void addPoints(float _x, float _y, float _z) {
        if (!finalized) {
            npoints++;
            float[] ptsar = {_x, _y, _z};
            points.add(ptsar);
        }
        else {
            Log.e(TAG, "addPoints could not be called. Model is already finalized!");
        }
    }

    float[] colors() {
        return collectFromFaces(0, DATATYPE.COLORS);
    }

    void addColors(float _red, float _green, float _blue, float _alpha) {
        if (!finalized) {
            ncolors++;
            float[] colar = {_red, _green, _blue, _alpha};
            colors.add(colar);
        }
        else {
            Log.e(TAG, "addColors could not be called. Model is already finalized!");
        }
    }

    enum DATATYPE {VERTICES, NORMALS, COLORS}
    float[] collectFromFaces(int _offset, DATATYPE _datatype) {
        int offs = 0;
        float[] part;
        float[] normy;
        int rep;
        switch (_datatype) {
            case COLORS: rep = 4; normy = new float[faces.size()*12]; break;
            default: rep = 3; normy = new float[faces.size()*9]; break;
        }

        for (int i = 0; i < faces.size(); i++) {  // for all faces
            for (int fi = _offset; fi < 9; fi += 3) { // a single face has three vertices that are defined within
                switch (_datatype) {
                    case VERTICES: part = positions.get((faces.get(i))[fi] - 1); break; // we extract a single vertice
                    case NORMALS: part = normals.get((faces.get(i))[fi] - 1); break;
                    case COLORS: part = colors.get(0); break; // TODO: Adjust for multiple colors
                    default: return normy;
                }
                for (int j = 0; j < rep; j++) { //then we write it to our array
                    normy[offs + j] = part[j];
                }
                nverticestotal++;
                offs += rep;
            }
        }
        return normy;
    }

    int verticesTotal() {
        return nverticestotal;
    }

    Object[] points() {
        return points.toArray();
    }

    void setMode(MODE _mode) {
        mode = _mode;
    }

    MODE getMode() {
        return mode;
    }

    void finalizeModel() {
        if (!finalized) {
            finalized = true;
            nvertices = npositions * 3;
        }
        else {
            Log.e(TAG, "finalize() already called before");
        }
    }
}
