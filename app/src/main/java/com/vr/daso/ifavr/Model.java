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

//    private float positions[][];    // XYZ
//    private float texels[][];          // UV
//    private float normals[][];        // XYZ
//    private int faces[][];              // PTN PTN PTN

    private ArrayList<float[]> positions = new ArrayList<float[]>();
    private ArrayList<float[]> texels= new ArrayList<float[]>();
    private ArrayList<float[]> normals= new ArrayList<float[]>();
    private ArrayList<int[]> faces= new ArrayList<int[]>();
    private ArrayList<float[]> points= new ArrayList<float[]>();

    public enum MODE {UNDECIDED, MESH, ATOM};
    private MODE mode;

    boolean finalized = false;

    final String TAG = "ModelObject";

    Model() {
     mode = Model.MODE.UNDECIDED;
    }

    float[] positions() {
//        Float[] returnval = new Float[positions.size()];
        int offs = 0;
        float[] part = new float[3];
        float[] floaty = new float[positions.size()*3];
        for (int i = 0; i < positions.size()-1; i++) {
            part = positions.get(i);
            for (int j = 0; j < 3; j++) {
                floaty[offs + j] = part[j];
            }
            offs += 3;
        }
        return floaty;
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
        int offs = 0;
        float[] part = new float[3];
        float[] normy = new float[normals.size()*3];
        for (int i = 0; i < normals.size()-1; i++) {
            part = normals.get(i);
            for (int j = 0; j < 3; j++) {
                normy[offs + j] = part[j];
            }
            offs += 3;
        }
        return normy;
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
