package com.vr.daso.ifavr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
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

//    ArrayList<glDrawable> load(InputStream _file, int[] _shader, int _mOffset, float _initial_x, float _initial_y, float _intital_z, String _tag) {
//        Log.i(TAG, "load");
//        ArrayList<Model> models = new ArrayList<Model>();
//        ArrayList<Material> materiallibrary = new ArrayList<>();
//        ArrayList<glDrawable> result = new ArrayList<>();
//        Model model = new Model();
//        boolean decided = false;
//        String name = "Default name";
//        String tag = _tag;
//        boolean object_name_defined_once = false;
//        String material = "None";
//        int last_model_vertices_offset = 0;
//        int vertices_count = 0;
//
//        try {
//            BufferedReader in = new BufferedReader(new InputStreamReader(_file));
//            if (in.ready()) {
//                String line;
//                try {
//                    while (true) {
//                        line = in.readLine(); //IOException leads to abort here
//                        ArrayList<String> linetokens = tokenize(line);
//
//                        if (model.getMode() == Model.MODE.UNDECIDED || model.getMode() == Model.MODE.MESH) {
//                            if (linetokens.get(0).contentEquals("v")) {
//                                float x = Float.parseFloat(linetokens.get(1));
//                                float y = Float.parseFloat(linetokens.get(2));
//                                float z = Float.parseFloat(linetokens.get(3));
//                                model.addPositions(x, y, z);
//                                model.addColors(0.8359375f, 0.17578125f, 0.125f, 0.5f);
//                                vertices_count++;
//                            }
//                            if (linetokens.get(0).contentEquals("vt")) {
//                                float uv1 = Float.parseFloat(linetokens.get(1));
//                                float uv2 = Float.parseFloat(linetokens.get(2));
//                                model.addTexels(uv1, uv2);
//                            }
//                            if (linetokens.get(0).contentEquals("vn")) {
//                                float x = Float.parseFloat(linetokens.get(1));
//                                float y = Float.parseFloat(linetokens.get(2));
//                                float z = Float.parseFloat(linetokens.get(3));
//                                model.addNormals(x, y, z);
//                            }
//                            if (linetokens.get(0).contentEquals("f")) {
//                                int[] facevals = new int[9];
//
//                                for (int i = 0; i < 3; i++) {
//                                    String[] strings = linetokens.get(i + 1).split("/");
//                                    facevals[i * 3] = Integer.parseInt(strings[0]) /*- last_model_vertices_offset*/;
//                                    try {
//                                        facevals[i * 3 + 1] = Integer.parseInt(strings[1]) /*- last_model_vertices_offset*/;
//                                    } catch (NumberFormatException e) {
////                                        Log.i(TAG, e.getMessage());
//                                        facevals[i * 3 + 1] = 0;
//                                    }
//                                    facevals[i * 3 + 2] = Integer.parseInt(strings[2]) /*- last_model_vertices_offset*/;
//                                }
//                                model.addFaces(facevals);
//                            }
//                            if (linetokens.get(0).contentEquals("mtllib")) {
//                                InputStream is = new ByteArrayInputStream(  ("/main/res/raw/"+linetokens.get(1)).getBytes() );
//                                materiallibrary.addAll(
//                                        parseMTL( is )
//                                );
//                            }
//                            if (!decided) {
//                                model.setMode(Model.MODE.MESH);
//                                decided = true;
//                            }
//                        }
//                        if (model.getMode() == Model.MODE.UNDECIDED || model.getMode() == Model.MODE.ATOM) {
//                            if (linetokens.get(0).matches("\\d+\\.\\d+")) { //any number of format X+.Y+
//                                float x = Float.parseFloat(linetokens.get(1));
//                                float y = Float.parseFloat(linetokens.get(2));
//                                float z = Float.parseFloat(linetokens.get(3));
//                                model.addPoints(x, y, z);
//                            }
//                            if (!decided) {
//                                model.setMode(Model.MODE.ATOM);
//                                decided = true;
//                            }
//                        }
//                        if (linetokens.get(0).contentEquals("o")) {
//                            if (object_name_defined_once) {
//                                result.add(makeDrawable(model, _shader, _mOffset, _initial_x, _initial_y, _intital_z, name, tag));
//                                model.clearFaces(); //delete Faces from current model
//                                model.clearVerticesTotal();
//                                //model = new Model(); // start a new model
//                                //model.setMode(Model.MODE.MESH);
////                                last_model_vertices_offset = vertices_count;
//                            }
//                            name = linetokens.get(1);
//                            object_name_defined_once = true;
//                        }
//                        if (linetokens.get(0).contentEquals("usemtl")) {
//                            material = linetokens.get(1);
//                        }
//                    }
//                } finally {
//                    // continue normally
//                }
//
//            } else {
//                Log.e(TAG, "Could not ready file " + _file + "! BufferedReader is not ready!");
//            }
//        } catch (FileNotFoundException e) {
//            Log.e(TAG, e + ": Could not ready file " + _file + "! File not found!");
//        } catch (IOException e) {
//            Log.e(TAG, e + ": Could not process file " + _file);
//        } finally {
//            result.add(makeDrawable(model, _shader, _mOffset, _initial_x, _initial_y, _intital_z, name, tag));
//            return result;
//        }
//    }

    ArrayList<glDrawable> load(InputStream _file, int[] _shader, int _mOffset, float _initial_x, float _initial_y, float _initial_z, String _tag) {
        ArrayList<glDrawable> result = new ArrayList<>();

        ArrayList<Model> models = loadModels(_file );
        Iterator<Model> it = models.iterator();
        while (it.hasNext()) {
            result.add(new glDrawable( it.next(), _shader, _mOffset, _initial_x, _initial_y, _initial_z, _tag) {
                    public void AnimationStep() {}
                       }
            );
        }

        return result;
    }

    private ArrayList<String> tokenize(String _input) {
        ArrayList<String> result = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(_input);
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result;
    }

    glDrawable makeDrawable(Model _base, int[] _shader, int _mOffset, float _initial_x, float _initial_y, float _initial_z, String _name, String _tag) {
        return new glDrawable(_base, _shader, _mOffset, _initial_x, _initial_y, _initial_z, _tag) {
            public void AnimationStep() {
            }
        };
    }

    sbsDrawable loadSBSImage(InputStream _file, InputStream _canvas, int[] _shader, int _mOffset, float _initial_x, float _initial_y, float _initial_z, String _tag) { // TODO: WIP
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        Bitmap image = BitmapFactory.decodeStream(_file, null, options);

        Bitmap leftImage = Bitmap.createBitmap(image, 0, 0, image.getWidth()/2 , image.getHeight());
        Bitmap rightImage = Bitmap.createBitmap(image, image.getWidth() / 2, 0, image.getWidth() / 2, image.getHeight());

        leftImage = Bitmap.createScaledBitmap(leftImage, 1024, 1024, false);
        rightImage = Bitmap.createScaledBitmap(rightImage, 1024, 1024, false);

        // create a canvas on which we will stick the images
        ArrayList<Model> models = loadModels(_canvas);
        sbsDrawable result = new sbsDrawable(models.get(0),  _shader, _mOffset, _initial_x, _initial_y, _initial_z, _tag, leftImage, rightImage );

        return result;
    }

    private ArrayList<Model> loadModels(InputStream _file) {
        Log.i(TAG, "load");
        ArrayList<Model> models = new ArrayList<Model>();
        ArrayList<Material> materiallibrary = new ArrayList<>();
        Model model = new Model();
        boolean decided = false;
        String name = "Default";
        boolean object_name_defined_once = false;
        String material = "None";
        int last_model_vertices_offset = 0;
        int vertices_count = 0;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(_file));
            if (in.ready()) {
                String line;
                try {
                    while (true) {
                        line = in.readLine(); //IOException leads to abort here
                        ArrayList<String> linetokens = tokenize(line);

                        if (model.getMode() == Model.MODE.UNDECIDED || model.getMode() == Model.MODE.MESH) {
                            if (linetokens.get(0).contentEquals("v")) {
                                float x = Float.parseFloat(linetokens.get(1));
                                float y = Float.parseFloat(linetokens.get(2));
                                float z = Float.parseFloat(linetokens.get(3));
                                model.addPositions(x, y, z);
                                model.addColors(0.8359375f, 0.17578125f, 0.125f, 0.5f);
                                vertices_count++;
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

                                for (int i = 0; i < 3; i++) {
                                    String[] strings = linetokens.get(i + 1).split("/");
                                    facevals[i * 3] = Integer.parseInt(strings[0]) /*- last_model_vertices_offset*/;
                                    try {
                                        facevals[i * 3 + 1] = Integer.parseInt(strings[1]) /*- last_model_vertices_offset*/;
                                    } catch (NumberFormatException e) {
//                                        Log.i(TAG, e.getMessage());
                                        facevals[i * 3 + 1] = 0;
                                    }
                                    facevals[i * 3 + 2] = Integer.parseInt(strings[2]) /*- last_model_vertices_offset*/;
                                }
                                model.addFaces(facevals);
                            }
                            if (linetokens.get(0).contentEquals("mtllib")) {
                                InputStream is = new ByteArrayInputStream(  ("/main/res/raw/"+linetokens.get(1)).getBytes() );
                                materiallibrary.addAll(
                                        parseMTL( is )
                                );
                            }
                            if (!decided) {
                                model.setMode(Model.MODE.MESH);
                                decided = true;
                            }
                        }
                        if (model.getMode() == Model.MODE.UNDECIDED || model.getMode() == Model.MODE.ATOM) {
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
                        if (linetokens.get(0).contentEquals("o")) {
                            if (object_name_defined_once) {
                                models.add(model);
                                model = model.clone();  // start a new model with the old one as a base
                                model.clearFaces(); //delete Faces from current model
                                model.clearVerticesTotal();
                                //model.setMode(Model.MODE.MESH);
//                                last_model_vertices_offset = vertices_count;
                            }
                            model.setName(linetokens.get(1) );
                            object_name_defined_once = true;
                        }
                        if (linetokens.get(0).contentEquals("usemtl")) {
                            material = linetokens.get(1);
                        }
                    }
                } finally {
                    // continue normally
                }

            } else {
                Log.e(TAG, "Could not ready file " + _file + "! BufferedReader is not ready!");
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, e + ": Could not ready file " + _file + "! File not found!");
        } catch (IOException e) {
            Log.e(TAG, e + ": Could not process file " + _file);
        } finally {
            models.add( model );
            return models;
        }
    }

    private ArrayList<Material> parseMTL(InputStream _file) {
        ArrayList<Material> result = new ArrayList<>();
        Material material = new Material();
        boolean material_defined_once = false;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(_file));
            if (in.ready()) {
                String line;
                try {
                    while (true) {
                        line = in.readLine(); //IOException leads to abort here
                        ArrayList<String> linetokens = tokenize(line);

                        if (linetokens.get(0).contentEquals("newmtl")) {
                            if (material_defined_once) {
                                result.add(material);
                                material = new Material(); // start a new model
                            }
                            material.name = linetokens.get(1);
                            material_defined_once = true;
                        }
                        if (linetokens.get(0).contentEquals("Ns")) {
                            material.Ns = Float.parseFloat(linetokens.get(1));
                        }
                        if (linetokens.get(0).contentEquals("Ka")) {
                            material.Ka[0] = Float.parseFloat(linetokens.get(1));
                            material.Ka[1] = Float.parseFloat(linetokens.get(2));
                            material.Ka[2] = Float.parseFloat(linetokens.get(3));
                        }
                        if (linetokens.get(0).contentEquals("Kd")) {
                            material.Kd[0] = Float.parseFloat(linetokens.get(1));
                            material.Kd[1] = Float.parseFloat(linetokens.get(2));
                            material.Kd[2] = Float.parseFloat(linetokens.get(3));
                        }
                        if (linetokens.get(0).contentEquals("Ks")) {
                            material.Ks[0] = Float.parseFloat(linetokens.get(1));
                            material.Ks[1] = Float.parseFloat(linetokens.get(2));
                            material.Ks[2] = Float.parseFloat(linetokens.get(3));
                        }
                        if (linetokens.get(0).contentEquals("Ke")) {
                            material.Ke[0] = Float.parseFloat(linetokens.get(1));
                            material.Ke[1] = Float.parseFloat(linetokens.get(2));
                            material.Ke[2] = Float.parseFloat(linetokens.get(3));
                        }
                        if (linetokens.get(0).contentEquals("Ni")) {
                            material.Ni = Float.parseFloat(linetokens.get(1));
                        }
                        if (linetokens.get(0).contentEquals("d")) {
                            material.d = Float.parseFloat(linetokens.get(1));
                        }
                        if (linetokens.get(0).contentEquals("map_Kd")) {
                            material.map_Kd = linetokens.get(1);
                        }
                        if (linetokens.get(0).contentEquals("map_Bump")) {
                            material.map_Bump = linetokens.get(1);
                        }
                        if (linetokens.get(0).contentEquals("map_Ks")) {
                            material.map_Ks = linetokens.get(1);
                        }
                    }
                }
                finally {
                    // continue normally
                }
            }
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, e + ": Could not ready file " + _file + "! File not found!");
        } catch (IOException e) {
            Log.e(TAG, e + ": Could not process file " + _file);
        } finally {
            return result;
        }
    }
}