package com.vr.daso.ifavr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Daniel on 03.11.2015.
 */
public class glDrawable implements Animated {
    private final String TAG = "glDrawableObject";
    private String objectTag;

    private static final int COORDS_PER_VERTEX = 3;

    private float[] COORDS;
    private float[] COLORS;
    private float[] ACTIVE_COLORS;
    private float[] NORMALS;
    private float[] TEXELS;

    private int COORDS_COUNT = 0;

    private int program;
    private int positionParam;
    private int normalParam;
    private int colorParam;
    private int modelParam;
    private int modelViewParam;
    private int modelViewProjectionParam;
    private int lightPosParam;
    private float[] model = new float[16];
    private float[] lightPosInEyeSpace;

    private float[] modelViewProjection;
    private float[] modelView;

    private int texelParam;
    private int texelCoordParam;

    private FloatBuffer fbVertices;
    private FloatBuffer fbColors;
    private FloatBuffer fbNormals;
    private FloatBuffer fbTexels;

    private final String name;

    private boolean isInteractable = false; //this should be relegated to an interface
    private boolean isPresent = true; // denotes wehter this object exists in the scene
    private boolean isHidden = false; // denotes wether this object should be hidden
    private boolean hasTexture = false;

    public float[] pose;

    private final int TEXTURE_COORDINATE_DATA_SIZE = 2;
    private int textureDataHandle; // TODO: refactor

    private boolean animationEnabled = false;
    private boolean animationPaused = false;


    glDrawable(Model _model, int[] _shader, int _mOffset, float _initial_x, float _initial_y, float _intital_z, String _name, String _tag) {
//        switch (_model.getMode() ) {
//            case MESH:
        name = _name;
        objectTag = _tag;
        modelViewProjection = new float[16];
        modelView = new float[16];
        try {
            COORDS = _model.positions();
            COORDS_COUNT = _model.verticesTotal();
            COLORS = _model.colors();
            NORMALS =  _model.normals();
            TEXELS = _model.texels();
        }
        catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }
 //               break;

 //           case ATOM:
 //               break;

  //          case UNDECIDED:
   //             Log.e(TAG, "Model-type is undecided!");
 //       }
        prepareFloatBuffer();
        createProgram(_shader);
        createParameters();
        Matrix.setIdentityM(model, 0);
        translate(_mOffset, _initial_x, _initial_y, _intital_z);
    }

    private void prepareFloatBuffer() {
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(COORDS.length * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        fbVertices = bbVertices.asFloatBuffer();
        fbVertices.put(COORDS);
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

        if (TEXELS != null) {
            ByteBuffer bbTexels = ByteBuffer.allocateDirect(TEXELS.length * 4);
            bbNormals.order(ByteOrder.nativeOrder());
            fbTexels = bbTexels.asFloatBuffer();
            fbTexels.put(TEXELS);
            fbTexels.position(0);
        }
        else {
           Log.e(TAG, "Texels are null");
        }


//        ByteBuffer bbFoundColors = ByteBuffer.allocateDirect(_d.ACTIVE_COLORS.length * 4);
//        bbFoundColors.order(ByteOrder.nativeOrder());
//        cubeFoundColors = bbFoundColors.asFloatBuffer();
//        cubeFoundColors.put(_d.ACTIVE_COLORS);
//        cubeFoundColors.position(0);

        checkGLError(TAG + " " + name + ": Setting Byte Buffers");
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
        checkGLError(TAG + " " + name + ": Create Program");
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

        checkGLError(TAG + " " + name + ": Create Parameters");
    }

    private void translate(int _mOffset, float _x, float _y, float _z){
        Matrix.translateM(model, _mOffset, _x, _y, _z);
    }

    /**
     * Draws the Object.
     * @param _view
     * @param _perspective
     * @param _lightPosInEyeSpace
     */
    public void draw(float[] _view, float[] _perspective, float[] _lightPosInEyeSpace) {
        createParameters();
        if (hasTexture) {
            texelParam = GLES20.glGetUniformLocation(program, "u_Texture");
            texelCoordParam = GLES20.glGetAttribLocation(program, "a_TexCoordinate");
        }

        Matrix.multiplyMM(modelView, 0, _view, 0, model, 0);
        Matrix.multiplyMM(modelViewProjection, 0, _perspective, 0,
                modelView, 0);

        GLES20.glUseProgram(program);

        // Set ModelView, MVP, position, normals, and color.
        GLES20.glUniform3fv(lightPosParam, 1, _lightPosInEyeSpace, 0);
        GLES20.glUniformMatrix4fv(modelParam, 1, false, model, 0);
        GLES20.glUniformMatrix4fv(modelViewParam, 1, false, modelView, 0);
        GLES20.glUniformMatrix4fv(modelViewProjectionParam, 1, false,
                modelViewProjection, 0);
        GLES20.glVertexAttribPointer(positionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, fbVertices);
        GLES20.glVertexAttribPointer(normalParam, 3, GLES20.GL_FLOAT, false, 0,
                fbNormals);
        GLES20.glVertexAttribPointer(colorParam, 4, GLES20.GL_FLOAT, false, 0, fbColors);

        if ( hasTexture ) {
            GLES20.glVertexAttribPointer(texelParam, 2, GLES20.GL_FLOAT, false, 0, fbTexels);

            // Set the active texture unti to texture unit 0
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0); // TODO: for multiple textures, this needs to be refactored

            // Bind the texture to this unit
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle);

            // Tell the texture uniform smapler to use this texture in the shader by binding to texture unit 0
            GLES20.glUniform1i(texelParam, 0);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, COORDS_COUNT);

        checkGLError(TAG + " " + name + ": Draw");
    }

    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     *
     * @param label Label to report in case of error.
     */
    private void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    /**
     * Load a texture from an image file.
     * @param context Context of the application
     * @param resourceId the ID of the image-resource that should serve as a texture
     */
    public void loadTexture(final Context context, final int resourceId) {
        // create a unique identifier for the texture
        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);

        //decode the image file into an Android Bitmap object
        if (textureHandle[0] != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false; // no pre-scaling

            // Read the resource in
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            // Recycle the bitmap
            bitmap.recycle();

            textureDataHandle =  textureHandle[0];
            GLES20.glEnableVertexAttribArray(texelParam);
            hasTexture = true;
            return;
        }

        throw new RuntimeException("Error loading texture.");

     }

    public void startAnimation() {
        animationEnabled = true;
    }

    public void stopAnimation() {
        animationEnabled = false;
    }

    public void pauseAnimation() {
        animationPaused = true;
    }

    String getName() {
        return name;
    }

    String getTag() {
        return objectTag;
    }

    public void AnimationStep() {

    }

    public float[] getModel() {
        return model;
    }

    public void setModel(float[] _model) {
        model = _model;
    }

    //    abstract public void onLookedAt();

//    @Override


}
