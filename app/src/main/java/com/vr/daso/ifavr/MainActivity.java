package com.vr.daso.ifavr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

// VRToolKit
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Vibrator;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.microedition.khronos.egl.EGLConfig;

public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private View mContentView;
    private View mControlsView;
    private boolean mVisible;

    private static final String TAG = "MainActivity";

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private static float CAMERA_Z = 0.01f /*-5.40f*/;
    private static float CAMERA_Y = /*-18.0f*/0.0f;
    private static float CAMERA_X = 0.0f;
    private static final float TIME_DELTA = 1.0f;

    private static float CAMERA_CENTER_X = 0.0f;
    private static float CAMERA_CENTER_Y = CAMERA_Y;
    private static float CAMERA_CENTER_Z = 0.0f;

    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.12f;

    private static final int COORDS_PER_VERTEX = 3;

    // We keep the light always position just above the user.
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] { 0.0f, 2.0f, 0.0f, 1.0f };

    private final float[] lightPosInEyeSpace = new float[4];

    private FloatBuffer floorVertices;
    private FloatBuffer floorColors;
    private FloatBuffer floorNormals;

    private FloatBuffer cubeVertices;
    private FloatBuffer cubeColors;
    private FloatBuffer cubeFoundColors;
    private FloatBuffer cubeNormals;

    private int cubeProgram;
    private int floorProgram;

    private int cubePositionParam;
    private int cubeNormalParam;
    private int cubeColorParam;
    private int cubeModelParam;
    private int cubeModelViewParam;
    private int cubeModelViewProjectionParam;
    private int cubeLightPosParam;

    private int floorPositionParam;
    private int floorNormalParam;
    private int floorColorParam;
    private int floorModelParam;
    private int floorModelViewParam;
    private int floorModelViewProjectionParam;
    private int floorLightPosParam;

    //revised parameter scheme
    // idea: instead of predefined objects, we need the ability to dynamically load everything into the
    // scene. In the end, what we want is a Dynamic List of Drawable Objects
    private ArrayList<glDrawable> drawableObjects = new ArrayList<glDrawable>();
    private Interpreter interpreter = new Interpreter();  // TODO: templatize for different file formats

    private float[] modelCube;
    private float[] camera;
    private float[] view;
    private float[] headView;
    private float[] modelViewProjection;
    private float[] modelView;
    private float[] modelFloor;

    private int score = 0;
    private float objectDistance = 5.0f;
    private float floorDepth = 20.0f;

    private Vibrator vibrator;
    private CardboardOverlayView overlayView;

    private int timestep = 0;

    ////////////// /*** Pump Station ***/ ////////////////////
    private Pumpstation pumpStation = new Pumpstation();    //
    private Category pumpInformation;                       //
                                                            //
    private float CONTAINER_RELATIVE_OFFSET = 3.0f;         //
    private float CONTAINER_POSITION_DISTANCE = 6.0f;       //
                                                            //
    // color constants                                      //
    final float RGB_MAX = 255;                              //
    final float WATER_R = 64 / RGB_MAX;                     //
    final float WATER_G = 182 / RGB_MAX;                    //
    final float WATER_B = 255 / RGB_MAX;                    //
    final float WATER_T = 0.6f;                             //
    final float EMPTY_R = 255 / RGB_MAX;                    //
    final float EMPTY_G = 255 / RGB_MAX;                    //
    final float EMPTY_B = 255 / RGB_MAX;                    //
    final float EMPTY_T = 0.6f;                             //

    /**
     * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
     *
     * @param type The type of shader we will be creating.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The shader object handler.
     */
    private int loadGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     *
     * @param label Label to report in case of error.
     */
    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    /**
     * Sets the view to our CardboardView and initializes the transformation matrices we will use
     * to render our scene.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.common_ui);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRestoreGLStateEnabled(false);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);

        modelCube = new float[16];
        camera = new float[16];
        view = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
        modelFloor = new float[16];
        headView = new float[16];
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        WebServiceTask webTask = (WebServiceTask)new WebServiceTask().execute(
               /* "http://tu-dresden.de/ifa/"*/"http://www.webserviceX.NET/",
                /*"http://opcfoundation.org/webservices/XMLDA/1.0/Read"*/"GetCountries",
                /*"http://141.30.154.211:8087/OPC/DA"*/"http://www.webservicex.net/country.asmx" );

        overlayView = (CardboardOverlayView) findViewById(R.id.overlay);
        overlayView.show3DToast("Loading, please wait...");
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }

    /**
     * Creates the buffers we use to store information about the 3D world.
     *
     * <p>OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
     * Hence we use ByteBuffers.
     *
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.

        // make a floor
        ByteBuffer bbFloorVertices = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COORDS.length * 4);
        bbFloorVertices.order(ByteOrder.nativeOrder());
        floorVertices = bbFloorVertices.asFloatBuffer();
        floorVertices.put(WorldLayoutData.FLOOR_COORDS);
        floorVertices.position(0);

        //glObj.floorVertices = prepareFloatBuffer(glObj)

        ByteBuffer bbFloorNormals = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_NORMALS.length * 4);
        bbFloorNormals.order(ByteOrder.nativeOrder());
        floorNormals = bbFloorNormals.asFloatBuffer();
        floorNormals.put(WorldLayoutData.FLOOR_NORMALS);
        floorNormals.position(0);

        ByteBuffer bbFloorColors = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COLORS.length * 4);
        bbFloorColors.order(ByteOrder.nativeOrder());
        floorColors = bbFloorColors.asFloatBuffer();
        floorColors.put(WorldLayoutData.FLOOR_COLORS);
        floorColors.position(0);

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
        int gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);
        int passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.passthrough_fragment);

        int texturedVertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.simple_texture_vertex);
        int texturedFragmentShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.simple_fragment_shader);

        // *** First implementation trying to implement a Barkley teapot ***
        // Step 1: Create a drawable object from the OBJ-File
        int[] teapotshaders = {vertexShader, passthroughShader};
        int[] plainshader = {vertexShader, passthroughShader};
        int[] textureshaders = {texturedVertexShader, texturedFragmentShader};
//      glDrawable glTeapot = interpreter.load("res/gldrawable/teapot.obj", potshaders, 0, 0, 0, -objectDistance);
        drawableObjects.addAll(interpreter.load(
                getResources().openRawResource(R.raw.josie_rizal),  // OBJ-Datei
                teapotshaders, // Shader
                0, 0, /*-19.0f*/ -1.0f, -0.5f*objectDistance,   // Initiale Position
                "Test Object") //Tag
        );
        addAppendixByTag("Test Object", new Animator() {
            public void AnimationStep(float[] _model) {
                Matrix.rotateM(_model,
                        0,
                        TIME_DELTA,
                        0.0f,
                        0.1f,
                        0.0f);
            }
        });

        Iterator<glDrawable> it = drawableObjects.iterator();
        while (it.hasNext()) {
            it.next().setColor( (float) Math.random(),(float) Math.random(),(float) Math.random(), 0.5f );
        }
        addSingleAppendixByIdentity("Test Object", "Josie", new Interactor() {
            @Override
            public void onLookedAt() {
                parent[0].setColor(0.0f, 0.2f, 1.0f, 1.0f);
                Log.d(TAG, "YO! Default Object is being looked at!");
            }

            @Override
            public void onLookDiscontinued() {
                parent[0].enableOnLookedAtAction(true);
                parent[0].setColor(0.2f, 1.0f, 0.0f, 1.0f);
                Log.d(TAG, "WHAT?! Default Object is not being looked at anymore!");
            }

            @Override
            public void onClicked() {
                parent[0].enableOnLookedAtAction(false);
                parent[0].setColor(1.0f, 0.2f, 0.0f, 1.0f);
                Log.d(TAG, "DAMN! Default Object is being clicked!");
            }
        });

        //Pump-Over Text
        drawableObjects.addAll(interpreter.load(
                getResources().openRawResource(R.raw.pump_over_text),  // OBJ-Datei
                teapotshaders, // Shader
                0, 1.5f*CONTAINER_RELATIVE_OFFSET, /*-19.0f*/ -CONTAINER_RELATIVE_OFFSET, CONTAINER_POSITION_DISTANCE*0.4f,   // Initiale Position
                "Pump_Over_Text") //Tag
        );
        //Change the Rotation of the Model
        Matrix.rotateM(
                drawableObjects.get(getGlObjectIndexByTag("Pump_Over_Text")).getModel(),
                0,
                10,
                1.0f,
                0.0f,
                0.0f);
        Matrix.rotateM(
                drawableObjects.get(getGlObjectIndexByTag("Pump_Over_Text")).getModel(),
                0,
                180+70,
                0.0f,
                1.0f,
                0.0f);
        addSingleAppendixByIdentity("Text_Pump_Over", "Pump_Over_Text", new Interactor() {
            @Override
            public void onLookedAt() {
                parent[0].setColor(0.0f, 0.2f, 1.0f, 1.0f);
                Log.d(TAG, "What'cha looking at, fella?");
            }

            @Override
            public void onLookDiscontinued() {
                parent[0].enableOnLookedAtAction(true);
                parent[0].setColor(0.2f, 1.0f, 0.0f, 1.0f);
            }

            @Override
            public void onClicked() {
                parent[0].enableOnLookedAtAction(false);
                parent[0].setColor(1.0f, 0.2f, 0.0f, 1.0f);
            }
        });

        drawableObjects.addAll(interpreter.load(
                getResources().openRawResource(R.raw.simple_container),  // OBJ-Datei
                teapotshaders, // Shader
                0, CONTAINER_RELATIVE_OFFSET, /*-19.0f*/ -CONTAINER_RELATIVE_OFFSET, CONTAINER_POSITION_DISTANCE,   // Initiale Position
                "Container1") //Tag
        );
        addAppendixByTag("Container1", new Animator() {
            public void AnimationStep(float[] _model) {
                Matrix.rotateM(_model,
                        0,
                        TIME_DELTA,
                        0.0f,
                        0.1f,
                        0.0f);
            }
        });
        for (int i = 0; i < 2; i++) {
            String concat = "Sphere.00" + Integer.toString(i);
            drawableObjects.get(getGlObjectIndexByTagAndName("Container1", concat))
                    .setColor(0.8f, 0.25f, 0.6f, 0.8f);
        }
//        addAppendixByTag("Container1", new Interactor() {
//            @Override
//            public void onLookedAt() {
//                parent[refIndex].setColor(0.0f, 0.2f, 1.0f, 1.0f);
//            }
//            @Override
//            public void onLookDiscontinued() {
//                parent[refIndex].setColor(0.2f, 1.0f, 0.0f, 1.0f);
//            }
//        });

        drawableObjects.addAll(interpreter.load(
                getResources().openRawResource(R.raw.simple_container),  // OBJ-Datei
                teapotshaders, // Shader
                0, 0, /*-19.0f*/ -CONTAINER_RELATIVE_OFFSET, CONTAINER_POSITION_DISTANCE,   // Initiale Position
                "Container2") //Tag
        );
        addAppendixByTag("Container2", new Animator() {
            public void AnimationStep(float[] _model) {
                Matrix.rotateM(_model,
                        0,
                        TIME_DELTA,
                        0.0f,
                        0.1f,
                        0.0f);
            }
        });
        for (int i = 0; i < 2; i++) {
            String concat = "Sphere.00" + Integer.toString(i);
            drawableObjects.get(getGlObjectIndexByTagAndName("Container2", concat))
                    .setColor(0.8f, 0.25f, 0.6f, 0.8f);
        }
//        addAppendixByTag("Container2", new Interactor() {
//            @Override
//            public void onLookedAt() {
//                parent[refIndex].setColor(0.0f, 0.2f, 1.0f, 1.0f);
//            }
//            @Override
//            public void onLookDiscontinued() {
//                parent[refIndex].setColor(0.2f, 1.0f, 0.0f, 1.0f);
//            }
//        });

        drawableObjects.addAll(interpreter.load(
                getResources().openRawResource(R.raw.simple_container),  // OBJ-Datei
                teapotshaders, // Shader
                0, -CONTAINER_RELATIVE_OFFSET, /*-19.0f*/ -CONTAINER_RELATIVE_OFFSET, CONTAINER_POSITION_DISTANCE,   // Initiale Position
                "Container3") //Tag
        );
        addAppendixByTag("Container3", new Animator() {
            public void AnimationStep(float[] _model) {
                Matrix.rotateM(_model,
                        0,
                        TIME_DELTA,
                        0.0f,
                        0.1f,
                        0.0f);
            }
        });
        for (int i = 0; i < 2; i++) {
            String concat = "Sphere.00" + Integer.toString(i);
            drawableObjects.get(getGlObjectIndexByTagAndName("Container3", concat))
                    .setColor(0.8f, 0.25f, 0.6f, 0.8f);
        }
//        addAppendixByTag("Container3", new Interactor() {
//            @Override
//            public void onLookedAt() {
//                parent[0].setColor(0.0f, 0.2f, 1.0f, 1.0f);
//            }
//            @Override
//            public void onLookDiscontinued() {
//                parent[0].setColor(0.2f, 1.0f, 0.0f, 1.0f);
//            }
//        });

//        drawableObjects.addAll(interpreter.load(
//                getResources().openRawResource(R.raw.plane),  // OBJ-Datei
//                textureshaders, // Shader
//                0, 0, /*-19.0f*/ 1.0f, objectDistance,   // Initiale Position
//                "Plane") //Tag
//        );
//        addAnimatorByTag("Plane", new Animator() {
//            public void AnimationStep(float[] _model) {
//                Matrix.rotateM(_model,
//                        0,
//                        TIME_DELTA,
//                        0.0f,
//                        0.1f,
//                        0.0f);
//            }
//        });
//        drawableObjects.get(getGlObjectIndexByTag("Plane")).loadTexture(this, R.raw.crate_texture);

//        drawableObjects.add(interpreter.loadSBSImage(
//                getResources().openRawResource(R.raw.crate_texture),
//                getResources().openRawResource(R.raw.plane),
//                textureshaders, // Shader
//                0, 0, /*-19.0f*/ -1.0f, - 2 * objectDistance - 3.0f,   // Initiale Position
//                "SBS Image") //Tag
//        );
//        addAppendixByTag("SBS Image", new Animator() {
//            public void AnimationStep(float[] _model) {
//                Matrix.rotateM(_model,
//                        0,
//                        TIME_DELTA,
//                        0.1f,
//                        0.1f,
//                        0.1f);
//                Matrix.translateM(_model, 0, 0.0001f, 0.0001f, 0.0001f);
//            }
//        });


//        drawableObjects.add( interpreter.load(
//                getResources().openRawResource(R.raw.cube),  // OBJ-Datei
//                teapotshaders, // Shader
//                0, 0, -19, objectDistance) // Initiale Position
//        );

        checkGLError("Constructing drawable objects");

        int[] floorshaders = {vertexShader, gridShader};
        floorProgram = createProgram(floorshaders);

        checkGLError("Floor program");

        floorModelParam = GLES20.glGetUniformLocation(floorProgram, "u_Model");
        floorModelViewParam = GLES20.glGetUniformLocation(floorProgram, "u_MVMatrix");
        floorModelViewProjectionParam = GLES20.glGetUniformLocation(floorProgram, "u_MVP");
        floorLightPosParam = GLES20.glGetUniformLocation(floorProgram, "u_LightPos");

        floorPositionParam = GLES20.glGetAttribLocation(floorProgram, "a_Position");
        floorNormalParam = GLES20.glGetAttribLocation(floorProgram, "a_Normal");
        floorColorParam = GLES20.glGetAttribLocation(floorProgram, "a_Color");

        GLES20.glEnableVertexAttribArray(floorPositionParam);
        GLES20.glEnableVertexAttribArray(floorNormalParam);
        GLES20.glEnableVertexAttribArray(floorColorParam);

        checkGLError("Floor program params");

        Matrix.setIdentityM(modelFloor, 0);
        Matrix.translateM(modelFloor, 0, 0, -floorDepth, 0); // Floor appears below user.

        checkGLError("onSurfaceCreated");
    }

    private int createProgram(int[] _shader) {
        int _program = GLES20.glCreateProgram();
        for (int shader : _shader) {
            GLES20.glAttachShader(_program, shader);
        }
        GLES20.glLinkProgram(_program);
        GLES20.glUseProgram(_program);
        return _program;
    }

    /**
     * Converts a raw text file into a string.
     *
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The context of the text file, or null in case of error.
     */
    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        timestep++;
        makeShitUp();
        TankUpdateTask my_task = new TankUpdateTask();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            my_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pumpStation, drawableObjects);
        else
            my_task.execute(pumpStation, drawableObjects);
        // Build the Model part of the ModelView matrix.
//        Matrix.rotateM(modelCube, 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);
/*        try {
            glDrawable animatedObject = drawableObjects.get(getGlObjectIndexByTag("Test Object") );
            float[] model = animatedObject.getModel();
            Matrix.rotateM(model,
                    0,
                    TIME_DELTA,
                    0.0f,
                    0.1f,
                    0.0f);
            animatedObject.setModel(model);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "Drawable Object not found");
        }
*/
        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, CAMERA_X, CAMERA_Y, CAMERA_Z, CAMERA_CENTER_X, CAMERA_CENTER_Y, CAMERA_CENTER_Z, 0.0f, 1.0f, 0.0f);

        headTransform.getHeadView(headView, 0);
/*
        for (int i=0; i < 5; i++) {
            drawableObjects.get( getGlObjectIndexByName( "Cylinder.00" + Integer.toString(i) ) ).setColor(
                    (float) Math.random(),
                    (float) Math.random(),
                    (float) Math.random(),
                    (float) Math.random()
            );
        }
*/
        // Do an animation step for every object
        glDrawable next;
        Iterator<glDrawable> it = drawableObjects.iterator();
        while (it.hasNext()) {
            next = it.next();
            next.prepareAnimation();
            if ( next.isLookedAt(headView, camera) ) {
                next.onLookedAt();
            }
            else if ( next.previouslyLookedAt() ) {
                next.onLookDiscontinued();
            }
        }

        checkGLError("onReadyToDraw");
    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        checkGLError("colorParam");

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        // Set the position of the light
        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);

        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
//        Matrix.multiplyMM(modelView, 0, view, 0, modelCube, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
//        drawCube();

        // Set modelView for the floor, so we draw floor in the correct location
        Matrix.multiplyMM(modelView, 0, view, 0, modelFloor, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0,
                modelView, 0);
        drawFloor();

        Iterator<glDrawable> it = drawableObjects.iterator();
        while (it.hasNext()) {
            it.next().draw(view, perspective, lightPosInEyeSpace, eye.getType() );
        }
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    /**
     * Draw the cube.
     *
     * <p>We've set all of our transformation matrices. Now we simply pass them into the shader.
     */
    public void drawCube() {
        GLES20.glUseProgram(cubeProgram);

        GLES20.glUniform3fv(cubeLightPosParam, 1, lightPosInEyeSpace, 0);

        // Set the Model in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(cubeModelParam, 1, false, modelCube, 0);

        // Set the ModelView in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(cubeModelViewParam, 1, false, modelView, 0);

        // Set the position of the cube
        GLES20.glVertexAttribPointer(cubePositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, cubeVertices);

        // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(cubeModelViewProjectionParam, 1, false, modelViewProjection, 0);

        // Set the normal positions of the cube, again for shading
        GLES20.glVertexAttribPointer(cubeNormalParam, 3, GLES20.GL_FLOAT, false, 0, cubeNormals);
        GLES20.glVertexAttribPointer(cubeColorParam, 4, GLES20.GL_FLOAT, false, 0,
                isLookingAtObject() ? cubeFoundColors : cubeColors);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        checkGLError("Drawing cube");
    }

    /**
     * Draw the floor.
     *
     * <p>This feeds in data for the floor into the shader. Note that this doesn't feed in data about
     * position of the light, so if we rewrite our code to draw the floor first, the lighting might
     * look strange.
     */
    public void drawFloor() {
        GLES20.glUseProgram(floorProgram);

        // Set ModelView, MVP, position, normals, and color.
        GLES20.glUniform3fv(floorLightPosParam, 1, lightPosInEyeSpace, 0);
        GLES20.glUniformMatrix4fv(floorModelParam, 1, false, modelFloor, 0);
        GLES20.glUniformMatrix4fv(floorModelViewParam, 1, false, modelView, 0);
        GLES20.glUniformMatrix4fv(floorModelViewProjectionParam, 1, false,
                modelViewProjection, 0);
        GLES20.glVertexAttribPointer(floorPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, floorVertices);
        GLES20.glVertexAttribPointer(floorNormalParam, 3, GLES20.GL_FLOAT, false, 0,
                floorNormals);
        GLES20.glVertexAttribPointer(floorColorParam, 4, GLES20.GL_FLOAT, false, 0, floorColors);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        checkGLError("drawing floor");
    }

    /**
     * Called when the Cardboard trigger is pulled.
     */
    @Override
    public void onCardboardTrigger() {
        Log.i(TAG, "onCardboardTrigger");
        // Has an object been looked at?
        glDrawable next;
        Iterator<glDrawable> it = drawableObjects.iterator();
        while (it.hasNext()) {
            next = it.next();
            if ( next.isLookedAt(headView, camera) ) {
                overlayView.show3DToast("*click*");
                next.onClicked();
            }
        }

//        if (isLookingAtObject()) {
//            score++;
//            overlayView.show3DToast("Found it! Look around for another one.\nScore = " + score);
//            hideObject();
//        } else {
//            overlayView.show3DToast("Look around to find the object!");
//        }

//        moveCameraInViewDirection(0.5f);

        // Always give user feedback.
        vibrator.vibrate(50);
    }

    /**
     * Find a new random position for the object.
     *
     * <p>We'll rotate it around the Y-axis so it's out of sight, and then up or down by a little bit.
     */
    private void hideObject() {
        float[] rotationMatrix = new float[16];
        float[] posVec = new float[4];

        // First rotate in XZ plane, between 90 and 270 deg away, and scale so that we vary
        // the object's distance from the user.
        float angleXZ = (float) Math.random() * 180 + 90;
        Matrix.setRotateM(rotationMatrix, 0, angleXZ, 0f, 1f, 0f);
        float oldObjectDistance = objectDistance;
        objectDistance = (float) Math.random() * 15 + 5;
        float objectScalingFactor = objectDistance / oldObjectDistance;
        Matrix.scaleM(rotationMatrix, 0, objectScalingFactor, objectScalingFactor,
                objectScalingFactor);
        Matrix.multiplyMV(posVec, 0, rotationMatrix, 0, modelCube, 12);

        // Now get the up or down angle, between -20 and 20 degrees.
        float angleY = (float) Math.random() * 80 - 40; // Angle in Y plane, between -40 and 40.
        angleY = (float) Math.toRadians(angleY);
        float newY = (float) Math.tan(angleY) * objectDistance;

        Matrix.setIdentityM(modelCube, 0);
        Matrix.translateM(modelCube, 0, posVec[0], newY, posVec[2]);
    }

    /**
     * Check if user is looking at object by calculating where the object is in eye-space.
     *
     * @return true if the user is looking at the object.
     */
    private boolean isLookingAtObject() {
        float[] initVec = { 0, 0, 0, 1.0f };
        float[] objPositionVec = new float[4];

        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, headView, 0, modelCube, 0);
        Matrix.multiplyMV(objPositionVec, 0, modelView, 0, initVec, 0);

        float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
        float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

        return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
    }


    private void moveCameraInViewDirection(float _distance) {
//        float yaw = (float) Math.atan2( headView[4], headView[1] );
//        float pitch = (float) Math.atan2( -headView[8], Math.sqrt(Math.pow(headView[9], 2) + Math.pow( headView[10], 2) ) );
     //   float roll = (float) Math.atan2( headView[9], headView[10]);

        float[] initVec = { 0, 0, 0, 1.0f };
        float[] objPositionVec = new float[4];

        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, headView, 0, modelCube, 0);
        Matrix.multiplyMV(objPositionVec, 0, modelView, 0, initVec, 0);

        float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
        float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

        float dX = (float) (_distance * Math.sin(pitch) * Math.cos(yaw) );
        float dY = (float) (_distance * Math.sin(pitch) * Math.sin(yaw) );
        float dZ = (float) (_distance * Math.cos(pitch) );

        CAMERA_X += dX;
        CAMERA_CENTER_X += dX;
        CAMERA_Y += dY;
        CAMERA_CENTER_Y += dY;
        CAMERA_Z -= dZ;
        CAMERA_CENTER_Z -= dZ;
    }

    /**
     * Created by Daniel on 24.11.2015.
     * Based on the tutorial: http://seesharpgears.blogspot.de/2010/10/ksoap-android-web-service-tutorial-with.html
     */
    private class WebServiceTask extends AsyncTask<String, Category, Category> {
        Category C;

        protected Category doInBackground(String... strings) {
            while (true) {
                int count = strings.length;

                final String NAMESPACE = strings[0];
                final String METHOD_NAME = strings[1];
                final String SOAP_ACTION = NAMESPACE + METHOD_NAME;
                final String URL = strings[2];

                SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);

        /*
         * Create Category with Id to be passed as an argument
         *
         * */
                C = new Category();
                C.CategoryId = 1;

        /*
         * Set the category to be the argument of the web service method
         *
         * */
//                PropertyInfo pi = new PropertyInfo();
//                pi.setName("C");
//                pi.setValue(C);
//                pi.setType(C.getClass());
//                Request.addProperty(pi);

        /*
         * Set the web service envelope
         *
         * */
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(Request);

                envelope.addMapping(NAMESPACE, "Category", new Category().getClass());
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        /*
         * Call the web service and retrieve result
         *
         * */
                try {
                    androidHttpTransport.call(SOAP_ACTION, envelope);
                    SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
//                    C.CategoryId = Integer.parseInt(response.toString());
                    C.Name = response.getName();
                    C.Description = response.toString();//(String) response.getProperty(2).toString();
                    publishProgress(C);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (isCancelled()) {
                    return C;
                }
            }
        }


        protected void onProgressUpdate(Category... progress) {
            pumpInformation = progress[0];
        }

        protected void onPostExecute(Category result) {
            pumpInformation = result;
        }
    }

    private int getGlObjectIndexByName(String _name) {
        int loop = 0;
        glDrawable candidate;
        Iterator<glDrawable> it = drawableObjects.iterator();
        while (it.hasNext()) {
            if ( it.next().getName().matches(_name) ) {
                return loop;
            }
            loop++;
        }
        return (-1);
    }

    private int getGlObjectIndexByTag(String _name) {
        int loop = 0;
        Iterator<glDrawable> it = drawableObjects.iterator();
        while (it.hasNext()) {
            if ( it.next().getTag().matches(_name) ) {
                return loop;
            }
            loop++;
        }
        return (-1);
    }

    private int getGlObjectIndexByTagAndName(String _parent, String _name) {
        glDrawable next;
        int loop = 0;

        Iterator<glDrawable> it = drawableObjects.iterator();
        while (it.hasNext()) {
            next = it.next();
            if ( next.getTag().matches(_parent) && next.getName().matches(_name) ) {
                return loop;
            }
            loop++;
        }
        return (-1);
    }

    /**
     * Assigns an animator to all glDrawables that share the same tag (aka the same parent).
     * @param _tag
     */
    private void addAppendixByTag(String _tag, Animator _animator) {
        glDrawable candidate;
        Iterator<glDrawable> it = drawableObjects.iterator();
        while (it.hasNext()) {
            candidate = it.next();
            if ( candidate.getTag().matches(_tag) ) {
                candidate.setAnimation(_animator);
            }
        }
    }

    /**
     * Assigns an interactor to all glDrawables that share the same tag (aka the same parent).
     * @param _tag
     */
    private void addAppendixByTag(String _tag, Interactor _interactor) {
        glDrawable candidate;
        Interactor interactor;

        Iterator<glDrawable> it = drawableObjects.iterator();
        while (it.hasNext()) {
            candidate = it.next();
            interactor = new Interactor(_interactor);
            if ( candidate.getTag().matches(_tag) ) {
                glDrawable[] ref = new glDrawable[1];
                getReference(ref, candidate);
                interactor.setParent(ref);
                candidate.setInteraction(interactor);
            }
        }
    }

    private void addSingleAppendixByIdentity(String _tag, String _name, Interactor _interactor) {
        glDrawable candidate;

        Iterator<glDrawable> it = drawableObjects.iterator();
        while (it.hasNext()) {
            candidate = it.next();
            if ( candidate.getName().matches(_name) && candidate.getTag().matches(_tag) ) {
                glDrawable[] ref = new glDrawable[1];
                getReference(ref, candidate);
                _interactor.setParent(ref);
                candidate.setInteraction(_interactor);
                return;
            }
        }
    }

    static int getReference(glDrawable[] _reference, glDrawable _drawable) {
        int ref_index = 0;
//        glDrawable[] reference = new glDrawable[1];
//        reference[0] = _drawable;
        _reference[ref_index] = _drawable;
        ref_index++;
        return ref_index-1;
    }


    // TODO: Currently the TankUpdateTask works for only one station
    private class TankUpdateTask extends AsyncTask<Object, Integer, Integer> {
        protected Integer doInBackground(Object... params) { // param0: pumpStation, param1: drawables
            // section constants
            final int SECTIONS = 5;
            final float MAX_VAL = 100.0f;
            final float DELTA_SECTION = MAX_VAL / (float)SECTIONS;

            String concat = "";

            float fuellstand_1 = ((Pumpstation)params[0]).Fuellstand1_Ist;
            float fuellstand_2 = ((Pumpstation)params[0]).Fuellstand2_Ist;
            float fuellstand_3 = ((Pumpstation)params[0]).Fuellstand3_Ist;

            for (int i = 0; i < SECTIONS; i++) {
                // passe String an vorhandene Zylinder an
                concat = prepareConcatString(i);

                //Fuellstand Container 1
                try {
                    if ((i+1) * DELTA_SECTION >= fuellstand_1) {  // Section ist befüllt
                        publishProgress(
                                getGlObjectIndexInTask((ArrayList<glDrawable>) params[1], "Container1", concat),
                                1);


                    } else {
                        publishProgress(
                                getGlObjectIndexInTask((ArrayList<glDrawable>) params[1], "Container1", concat),
                                0);
                    }
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(TAG, e.toString() + " at Container1 with i=" + Integer.toString(i) + " with concat=" + concat);
                }

                //Fuellstand Container 2
                try {
                    if (i * DELTA_SECTION >= fuellstand_2) {  // Section ist befüllt
                        publishProgress(
                                getGlObjectIndexInTask((ArrayList<glDrawable>) params[1], "Container2", concat),
                                1);

                    } else {
                        publishProgress(
                                getGlObjectIndexInTask((ArrayList<glDrawable>) params[1], "Container2", concat),
                                0);
                    }
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(TAG, e.toString() + " at Container2 with i=" + Integer.toString(i) + " with concat=" + concat);
                }

                //Fuellstand Container 3
                try {
                    if (i * DELTA_SECTION >= fuellstand_3) {  // Section ist befüllt
                        publishProgress(
                                getGlObjectIndexInTask((ArrayList<glDrawable>) params[1], "Container3", concat),
                                1);

                    } else {
                        publishProgress(
                                getGlObjectIndexInTask((ArrayList<glDrawable>) params[1], "Container3", concat),
                                0);
                    }
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(TAG, e.toString() + " at Container3 with i=" + Integer.toString(i) + " with concat=" + concat);
                }

            }
            return 0;
        }

        protected void onProgressUpdate(Integer... progress) {
            switch (progress[1]) {
                case 0: // section is empty
                    drawableObjects.get(progress[0]).setColor(EMPTY_R, EMPTY_G, EMPTY_B, EMPTY_T);
                    break;
                case 1: // section is filled
                    drawableObjects.get(progress[0]).setColor(WATER_R, WATER_G, WATER_B, WATER_T);
            }
        }

        protected void onPostExecute(Integer result) {
            Log.d(TAG, "Pumpstation asessed");
        }

    String prepareConcatString(int i) throws IndexOutOfBoundsException {
        String concat = "";
        if (i < 10) {
            concat = "Cylinder.00" + Integer.toString(i);
        } else
        if (i < 100) {
            concat = "Cylinder.0" + Integer.toString(i);
        } else
        if (i < 1000) {
            concat = "Cylinder." + Integer.toString(i);
        } else
        {
            throw new IndexOutOfBoundsException();
        }
        return concat;
    }

        private int getGlObjectIndexInTask(ArrayList<glDrawable> _list, String _parent, String _name) {
            glDrawable next;
            int loop = 0;

            Iterator<glDrawable> it = _list.iterator();
            while (it.hasNext()) {
                next = it.next();
                if ( next.getTag().matches(_parent) && next.getName().matches(_name) ) {
                    return loop;
                }
                loop++;
            }
            return (-1);
        }
    }

    void makeShitUp() {
        pumpStation.Fuellstand1_Ist = (float) Math.random() * 100;
        pumpStation.Fuellstand2_Ist = 100;
        pumpStation.Fuellstand3_Ist = 35;
    }

}

