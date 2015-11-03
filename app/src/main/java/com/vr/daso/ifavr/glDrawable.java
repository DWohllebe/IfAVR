package com.vr.daso.ifavr;

import android.util.Log;

/**
 * Created by Daniel on 03.11.2015.
 */
public class glDrawable {
    public final String TAG = "glDrawableObject";

    public float[] COORDS;
    public float[] COLORS;
    public float[] ACTIVE_COLORS;
    public float[] NORMALS;

    private int cubePositionParam;
    private int cubeNormalParam;
    private int cubeColorParam;
    private int cubeModelParam;
    private int cubeModelViewParam;
    private int cubeModelViewProjectionParam;
    private int cubeLightPosParam;

    public float[] pose;

    glDrawable(Model _model) {
        switch (_model.getMode() ) {
            case MESH:
                COORDS = (float[])_model.positions()[0];
                NORMALS = (float[])_model.normals()[0];
                break;

            case ATOM:
                break;

            case UNDECIDED:
                Log.e(TAG, "Model-type is undecided!");
        }
    }
}
