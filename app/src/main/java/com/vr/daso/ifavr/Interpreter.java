package com.vr.daso.ifavr;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by Daniel on 03.11.2015.
 */


public class Interpreter {
    final String TAG = "InterpreterObject";

    class objInformation {
        public String nameOBJ;
        public String nameJAV;
    }

    glDrawable load(InputStream _file, int[] _shader, int _mOffset, float _initial_x, float _initial_y, float _intital_z) {
        Log.i(TAG, "load");
        ArrayList<Model> result = new ArrayList<Model>();
        Model model = new Model();
        boolean decided = false;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(_file));
            if (in.ready()) {
                String line;
                try {
                    while (true) {
                        line = in.readLine(); //IOException leads to abort here
                        ArrayList<String> linetokens = tokenize(line);

                        if (model.getMode() == Model.MODE.UNDECIDED || model.getMode() == Model.MODE.MESH ) {
                            if (linetokens.get(0).contentEquals("v")) {
                                float x = Float.parseFloat(linetokens.get(1));
                                float y = Float.parseFloat(linetokens.get(2));
                                float z = Float.parseFloat(linetokens.get(3));
                                model.addPositions(x, y, z);
                            }
                            if (linetokens.get(0).contentEquals("vt")) {
                                float uv1 = Float.parseFloat(linetokens.get(1));
                                float uv2 = Float.parseFloat(linetokens.get(2));
                                model.addTexels(uv1, uv2);
                            }
                            if (linetokens.get(0).contentEquals("vn")) {
                                float x = Float.parseFloat(linetokens.get(1));
                                float y = Float.parseFloat(linetokens.get(2));
                                float z = Float.parseFloat(linetokens.get(3));
                                model.addNormals(x, y, z);
                            }
                            if (linetokens.get(0).contentEquals("f")) {
                                int[] facevals = new int[9];

                                for (int i = 0; i < 3; i++){
                                    String[] strings = linetokens.get(i+1).split("/");
                                    facevals[i*3] = Integer.parseInt(strings[0]);
                                    try {
                                        facevals[i * 3 + 1] = Integer.parseInt(strings[1]);
                                    }
                                    catch(NumberFormatException e) {
//                                        Log.i(TAG, e.getMessage());
                                        facevals[i * 3 + 1] = 0;
                                    }
                                    facevals[i*3+2] = Integer.parseInt(strings[2]);
                                }
                                model.addFaces(facevals);
                            }
                            if (!decided) {
                                model.setMode(Model.MODE.MESH);
                                decided = true;
                            }
                        }
                        if (model.getMode() == Model.MODE.UNDECIDED || model.getMode() == Model.MODE.ATOM ) {
                            if (linetokens.get(0).matches("\\d+\\.\\d+")) { //any number of format X+.Y+
                                float x = Float.parseFloat(linetokens.get(1));
                                float y = Float.parseFloat(linetokens.get(2));
                                float z = Float.parseFloat(linetokens.get(3));
                                model.addPoints(x, y, z);
                            }
                            if (!decided) {
                                model.setMode(Model.MODE.ATOM);
                                decided = true;
                            }
                        }
                    }
                }
                finally {
                    // continue normally
                }

            } else {
                Log.e(TAG, "Could not ready file " + _file + "! BufferedReader is not ready!");
            }
        }
        catch(FileNotFoundException e) {
            Log.e(TAG, e +": Could not ready file " + _file + "! File not found!");
        }
        catch(IOException e) {
            Log.e(TAG, e + ": Could not process file " + _file);
        }
        finally {
            return makeDrawable(model, _shader, _mOffset, _initial_x, _initial_y, _intital_z);
        }
    }

    private ArrayList<String> tokenize(String _input) {
        ArrayList<String> result = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(_input);
        while(st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result;
    }

    glDrawable makeDrawable(Model _base, int[] _shader, int _mOffset, float _initial_x, float _initial_y, float _intital_z) {
        return new glDrawable(_base, _shader, _mOffset, _initial_x, _initial_y, _intital_z);
    }
}
