package com.vr.daso.ifavr;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Daniel on 03.11.2015.
 */
public class glDrawable {
    public final String TAG = "glDrawableObject";

    public float[] COORDS;
    public float[] COLORS;
    public float[] ACTIVE_COLORS;
    public float[] NORMALS;

    public int program;
    public int positionParam;
    public int normalParam;
    public int colorParam;
    public int modelParam;
    public int modelViewParam;
    public int modelViewProjectionParam;
    public int lightPosParam;
    public float[] model;
    public float[] lightPosInEyeSpace;

    public FloatBuffer fbVertices;
    public FloatBuffer fbColors;
    public FloatBuffer fbNormals;

    public final String name = "Drawable";

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
        prepareFloatBuffer();
    }

    private void prepareFloatBuffer() {
        FloatBuffer result;

        ByteBuffer bbVertices = ByteBuffer.allocateDirect(COORDS.length * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        fbVertices = bbVertices.asFloatBuffer();
        fbVertices.put((COORDS));
        fbVertices.position(0);

        ByteBuffer bbColors = ByteBuffer.allocateDirect(COLORS.length * 4);
        bbColors.order(ByteOrder.nativeOrder());
        fbColors = bbColors.asFloatBuffer();
        fbColors.put(COLORS);
        fbColors.position(0);

        ByteBuffer bbNormals = ByteBuffer.allocateDirect(NORMALS.length * 4);
        bbNormals.order(ByteOrder.nativeOrder());
        fbNormals = bbNormals.asFloatBuffer();
        fbNormals.put(NORMALS);
        fbNormals.position(0);


//        ByteBuffer bbFoundColors = ByteBuffer.allocateDirect(_d.ACTIVE_COLORS.length * 4);
//        bbFoundColors.order(ByteOrder.nativeOrder());
//        cubeFoundColors = bbFoundColors.asFloatBuffer();
//        cubeFoundColors.put(_d.ACTIVE_COLORS);
//        cubeFoundColors.position(0);

    }
}
