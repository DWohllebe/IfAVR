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

    private int PositionParam;
    private int NormalParam;
    private int ColorParam;
    private int ModelParam;
    private int ModelViewParam;
    private int ModelViewProjectionParam;
    private int LightPosParam;

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
