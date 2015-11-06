package com.vr.daso.ifavr;

import android.util.Log;
import android.opengl.GLES20;
import android.opengl.Matrix;

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

    private int program;
    private int positionParam;
    private int normalParam;
    private int colorParam;
    private int modelParam;
    private int modelViewParam;
    private int modelViewProjectionParam;
    private int lightPosParam;
    private float[] model;
    private float[] lightPosInEyeSpace;

    public FloatBuffer fbVertices;
    public FloatBuffer fbColors;
    public FloatBuffer fbNormals;

    public final String name = "Drawable";

    private boolean isInteractable = false; //this should be relegated to an interface
    private boolean isPresent = true; // denotes wehter this object exists in the scene
    private boolean isHidden = false; // denotes wether this object should be hidden

    public float[] pose;


    glDrawable(Model _model, int[] _shader, int _mOffset, float _initial_x, float _initial_y, float _intital_z) {
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
        createProgram(_shader);
//        checkGLError("Cube program");
        createParameters();
        Matrix.setIdentityM(model, 0);
        translate(_mOffset, _initial_x, _initial_y, _intital_z);
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

    void attachShader(int _shader) {
        GLES20.glAttachShader(program, _shader);
    }

    void link() {
        GLES20.glLinkProgram(program);
    }

    void use() {
        GLES20.glUseProgram(program);
    }

    private void createProgram(int[] _shader) {
        program = GLES20.glCreateProgram();
        for (int shader : _shader) {
            GLES20.glAttachShader(program, shader);
        }
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);
    }

    private void createParameters() {
        positionParam = GLES20.glGetAttribLocation(program, "a_Position");
        normalParam = GLES20.glGetAttribLocation(program, "a_Normal");
        colorParam = GLES20.glGetAttribLocation(program, "a_Color");

        modelParam = GLES20.glGetUniformLocation(program, "u_Model");
        modelViewParam = GLES20.glGetUniformLocation(program, "u_MVMatrix");
        modelViewProjectionParam = GLES20.glGetUniformLocation(program, "u_MVP");
        lightPosParam = GLES20.glGetUniformLocation(program, "u_LightPos");

        GLES20.glEnableVertexAttribArray(positionParam);
        GLES20.glEnableVertexAttribArray(normalParam);
        GLES20.glEnableVertexAttribArray(colorParam);
    }

    private void translate(int _mOffset, float _x, float _y, float _z){
        Matrix.translateM(model, _mOffset, _x, _y, _z);
    }

    public void draw() {
        GLES20.glUseProgram(program);

        // Set ModelView, MVP, position, normals, and color.
        GLES20.glUniform3fv(lightPosParam, 1, lightPosInEyeSpace, 0);
        GLES20.glUniformMatrix4fv(modelParam, 1, false, model, 0);
        GLES20.glUniformMatrix4fv(modelViewParam, 1, false, modelView, 0);
        GLES20.glUniformMatrix4fv(modelViewProjectionParam, 1, false,
                modelViewProjection, 0);
        GLES20.glVertexAttribPointer(positionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, vertices);
        GLES20.glVertexAttribPointer(normalParam, 3, GLES20.GL_FLOAT, false, 0,
                normals);
        GLES20.glVertexAttribPointer(colorParam, 4, GLES20.GL_FLOAT, false, 0, colors);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }
}
