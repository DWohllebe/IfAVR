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

        // create a unique identifier for the texture
        final int[] textureHandle = new int[2];
        GLES20.glGenTextures(2, textureHandle, 0);

        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
        // Scale up if the texture if smaller.
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        // scale linearly when image smaller than texture
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, leftImage, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[1]);
        // Scale up if the texture if smaller.
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        // scale linearly when image smaller than texture
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, rightImage, 0);

        // Set filtering
 //       GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
 //       GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Load the bitmap into the bound texture

        textureDataHandleLeft =  textureHandle[0];
        textureDataHandleRight = textureHandle[1];

        GLES20.glEnableVertexAttribArray(super.texelParam);

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture 1");
        }
        if (textureHandle[1] == 0)
        {
            throw new RuntimeException("Error loading texture 2");
        }

        super.hasTexture = true;
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
        if (hasTexture) {
//            GLES20.glEnable(GLES20.GL_TEXTURE_2D);
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
        checkGLError(TAG + " " + objectTag + ": Uniform Matrizes");
        GLES20.glVertexAttribPointer(positionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, fbVertices);
        checkGLError(TAG + " " + objectTag + ": Vertex Parameters");
        GLES20.glVertexAttribPointer(normalParam, 3, GLES20.GL_FLOAT, false, 0,
                fbNormals);
        checkGLError(TAG + " " + objectTag + ": Normal Parameters");
        GLES20.glVertexAttribPointer(colorParam, 4, GLES20.GL_FLOAT, false, 0, fbColors);
        checkGLError(TAG + " " + objectTag + ": Color Parameters");

        if ( hasTexture ) {
            GLES20.glVertexAttribPointer(texelParam, 2, GLES20.GL_FLOAT, false, 0, fbTexels);

            switch (_eyetype) {
                case (Eye.Type.LEFT):
                    // Set the active texture unit to texture unit 0
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0); // TODO: for multiple textures, this needs to be refactored
                    checkGLError(TAG + " " + objectTag + ": ActiveTexture");

                    // Bind the texture to this unit
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandleLeft);
                    checkGLError(TAG + " " + objectTag + ": bindTexture");

                    // Tell the texture uniform smapler to use this texture in the shader by binding to texture unit 0
                    GLES20.glUniform1i(texelParam, 0);
                    checkGLError(TAG + " " + objectTag + ": Uniform");

                    break;

                case (Eye.Type.RIGHT):
                    // Set the active texture unit to texture unit 0
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE1); // TODO: for multiple textures, this needs to be refactored

                    // Bind the texture to this unit
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandleRight);

                    // Tell the texture uniform smapler to use this texture in the shader by binding to texture unit 1
                    GLES20.glUniform1i(texelParam, 1);

                    break;

                default:
                    break;
            }


        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, COORDS_COUNT);
//        GLES20.glDisable(GLES20.GL_TEXTURE_2D);

        checkGLError(TAG + " " + objectTag + ": Draw");
    }
}
