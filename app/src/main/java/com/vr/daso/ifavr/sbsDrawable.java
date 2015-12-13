package com.vr.daso.ifavr;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.google.vrtoolkit.cardboard.Eye;

/**
 * Created by Daniel on 10.12.2015.
 */
public class sbsDrawable extends glDrawable {
    Bitmap leftImage;
    Bitmap rightImage;
    int textureDataHandleLeft;
    int textureDataHandleRight;

    sbsDrawable(Model _model, int[] _shader, int _mOffset, float _initial_x, float _initial_y, float _intital_z, String _tag, Bitmap _leftImage, Bitmap _rightImage) {
        super(_model, _shader, _mOffset, _initial_x, _initial_y, _intital_z, _tag);
        leftImage = _leftImage;
        rightImage = _rightImage;
        hasTexture = true;

        // create a unique identifier for the texture
        final int[] textureHandle = new int[2];
        GLES20.glGenTextures(2, textureHandle, 0);

        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
        // Scale up if the texture if smaller.
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        // scale linearly when image smaller than texture
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, leftImage, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[1]);
        // Scale up if the texture if smaller.
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_MAG_FILTER,
//                GLES20.GL_LINEAR);

        // scale linearly when image smaller than texture
//       GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_MIN_FILTER,
//                GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, rightImage, 0);

        // Set filtering
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Load the bitmap into the bound texture

        textureDataHandleLeft =  textureHandle[0];
        textureDataHandleRight = textureHandle[1];


        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture 1");
        }
        if (textureHandle[1] == 0)
        {
            throw new RuntimeException("Error loading texture 2");
        }

        leftImage.recycle();
        rightImage.recycle();
    }

    /**
     * http://www.jayway.com/2010/12/30/opengl-es-tutorial-for-android-part-vi-textures/
     * @param _view
     * @param _perspective
     * @param _lightPosInEyeSpace
     * @param _eyetype
     */
    @Override
    public void draw(float[] _view, float[] _perspective, float[] _lightPosInEyeSpace, int _eyetype) {
        createParameters();
        checkGLError(TAG + " " + objectTag + ": Create Parameters");
        if (hasTexture) {
            texelCoordParam = GLES20.glGetAttribLocation(program, "a_TexCoordinate");
            checkGLError(TAG + " " + objectTag + ": Get Tex Coordinates");
//            texelParam = GLES20.glGetUniformLocation(program, "u_Texture");
            checkGLError(TAG + " " + objectTag + ": Get Texture Sampler Uniform Location");
//            GLES20.glEnableVertexAttribArray(texelParam);
            checkGLError(TAG + " " + objectTag + ": Enable texelParam");
        }

        Matrix.multiplyMM(modelView, 0, _view, 0, model, 0);
        checkGLError(TAG + " " + objectTag + ": Multiply to Model View");
        Matrix.multiplyMM(modelViewProjection, 0, _perspective, 0,
                modelView, 0);
        checkGLError(TAG + " " + objectTag + ": Multiply to Model View Projection");

        GLES20.glUseProgram(program);
        checkGLError(TAG + " " + objectTag + ": Create Program");

        // Set ModelView, MVP, position, normals, and color.
        GLES20.glUniform3fv(lightPosParam, 1, _lightPosInEyeSpace, 0);
        checkGLError(TAG + " " + objectTag + ": lightPosInEyeSpace");
        GLES20.glUniformMatrix4fv(modelParam, 1, false, model, 0);
        checkGLError(TAG + " " + objectTag + ": Model Parameters");
        GLES20.glUniformMatrix4fv(modelViewParam, 1, false, modelView, 0);
        checkGLError(TAG + " " + objectTag + ": Model View Parameter");
        GLES20.glUniformMatrix4fv(modelViewProjectionParam, 1, false,
                modelViewProjection, 0);
        checkGLError(TAG + " " + objectTag + ": Model View Projection Parameter");
        GLES20.glVertexAttribPointer(positionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, fbVertices);
        checkGLError(TAG + " " + objectTag + ": Vertex Parameters");
        GLES20.glVertexAttribPointer(normalParam, 3, GLES20.GL_FLOAT, false, 0,
                fbNormals);
        checkGLError(TAG + " " + objectTag + ": Normal Parameters");
//        GLES20.glVertexAttribPointer(colorParam, 4, GLES20.GL_FLOAT, false, 0, fbColors);
//        checkGLError(TAG + " " + objectTag + ": Color Parameters");

        if ( hasTexture ) {
            GLES20.glVertexAttribPointer(texelCoordParam, 2, GLES20.GL_FLOAT, false, 0, fbTexels);

            switch (_eyetype) {
                case (Eye.Type.LEFT):
                    // Set the active texture unit to texture unit 0
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0); // TODO: for multiple textures, this needs to be refactored
                    checkGLError(TAG + " " + objectTag + ": ActiveTexture");

                    // Bind the texture to this unit
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandleLeft);
                    checkGLError(TAG + " " + objectTag + ": bindTexture");

                    // Tell the texture uniform smapler to use this texture in the shader by binding to texture unit 0
//                    GLES20.glUniform1i(texelParam, 0);
                    checkGLError(TAG + " " + objectTag + ": Uniform");

                    break;

                case (Eye.Type.RIGHT):
                    // Set the active texture unit to texture unit 0
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE1); // TODO: for multiple textures, this needs to be refactored

                    // Bind the texture to this unit
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandleRight);

                    // Tell the texture uniform smapler to use this texture in the shader by binding to texture unit 1
//                    GLES20.glUniform1i(texelParam, 1);

                    break;

                default:
                    break;
            }


        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, COORDS_COUNT);
//        GLES20.glDisable(GLES20.GL_TEXTURE_2D);
//        GLES20.glDisableVertexAttribArray(colorParam);
//        GLES20.glDisableVertexAttribArray(normalParam);
//        GLES20.glDisableVertexAttribArray(positionParam);
//        GLES20.glDisableVertexAttribArray(texelParam);

        checkGLError(TAG + " " + objectTag + ": Draw");
    }

    @Override
    protected void createParameters() {
        positionParam = GLES20.glGetAttribLocation(program, "a_Position");
        checkGLError(TAG + " " + name + ": Create Parameters / a_Position");
        normalParam = GLES20.glGetAttribLocation(program, "a_Normal");
        checkGLError(TAG + " " + name + ": Create Parameters / a_Normal");
        colorParam = GLES20.glGetAttribLocation(program, "a_Color");
        checkGLError(TAG + " " + name + ": Create Parameters /a_Color");

        modelParam = GLES20.glGetUniformLocation(program, "u_Model");
        checkGLError(TAG + " " + name + ": Create Parameters / u_Model");
        modelViewParam = GLES20.glGetUniformLocation(program, "u_MVMatrix");
        checkGLError(TAG + " " + name + ": Create Parameters /u_MVMatrix");
        modelViewProjectionParam = GLES20.glGetUniformLocation(program, "u_MVP");
        checkGLError(TAG + " " + name + ": Create Parameters / u_MVP");
        lightPosParam = GLES20.glGetUniformLocation(program, "u_LightPos");
        checkGLError(TAG + " " + name + ": Create Parameters / u_LightPos");

        GLES20.glEnableVertexAttribArray(positionParam);
        checkGLError(TAG + " " + name + ": Create Parameters / positionParam");
        GLES20.glEnableVertexAttribArray(normalParam);
        checkGLError(TAG + " " + name + ": Create Parameters / normalParam");
        GLES20.glEnableVertexAttribArray(colorParam);
        checkGLError(TAG + " " + name + ": Create Parameters / colorParam");

//        checkGLError(TAG + " " + name + ": Create Parameters");
    }


}
