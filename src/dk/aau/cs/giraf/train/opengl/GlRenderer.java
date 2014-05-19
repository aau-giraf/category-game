package dk.aau.cs.giraf.train.opengl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;
import android.util.TimingLogger;
import dk.aau.cs.giraf.train.opengl.game.GameData;

/** 
 * The renderer which draws on {@link GLSurfaceView}.
 * 
 * @author Jesper Riemer Andersen
 * @see Renderer
 */
public class GlRenderer implements Renderer {
    
    /* Constants */
    protected static final float NEAR_CLIPPING_PLANE_DEPTH = -(GameData.FOREGROUND + 0.1f);
    protected static final float FAR_CLIPPING_PLANE_DEPTH  = -(GameData.BACKGROUND - 0.1f);
    protected static final float FIELD_OF_VIEW_ANGLE = 45.0f;
    
    /** The width of the GLSurfaceView */
    private static int surfaceWidth;
    /** The height of the GLSurfaceView */
    private static int surfaceHeight;
    
    /** The class that does all the drawing */
    private GameDrawer gameDrawer;
    
    /** The game data. Contains current velocity, distance traveled, velocity handlers, etc. */
    private GameData gameData;
    
    /** Apllication context, used to get resources */
    private Context context;
    
    public GlRenderer(Context context, GameData gameData) {
        this.context = context;
        this.gameData = gameData;
    }
    
    /** 
     * Here we do our drawing.
     * This is run by a thread that is automatically generated by the OpenGL library.
     * @see Renderer#onDrawFrame(GL10)
     */
    @Override
	public void onDrawFrame(GL10 gl) {
        /* Clears the screen to the color we previously decided on @onSurfaceCreated,
         * and clear the depth buffer and reset the scene */
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        
        this.gameDrawer.drawGame();
        
        measureFps();
    }
    
    /** A timestamp for the last time fps was written to the log  */
    private long timestamp = 0;
    /** The current number of frames counted this second */
    private int frames = 1;
    
    /** Measures FPS and prints it in the log */
    private void measureFps() {
        if(System.currentTimeMillis() >= timestamp + 1000) {
            timestamp = System.currentTimeMillis();
            Log.d(GlRenderer.class.getSimpleName(), "FPS: " + Integer.toString(frames)); // write to log
            frames = 1; // also count the frame from the current iteration
        }
        else {
            frames++;
        }
    }
    
    /** 
     * If the surface changes, reset the view.
     * @see Renderer#onSurfaceChanged(GL10, int, int)
     */
    @Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
        //((GameActivity) this.context).showProgressDialog();
        if(height == 0) { 						//Prevent A Divide By Zero By
            height = 1; 						//Making Height Equal One
        }
        
        Log.d(GlRenderer.class.getSimpleName(), "Screen size: " + Integer.toString(width) + "x" + Integer.toString(height)); // write to log
        
        GlRenderer.surfaceWidth = width;
        GlRenderer.surfaceHeight = height;
        
        gl.glViewport(0, 0, width, height);   //Reset The Current Viewport
        gl.glMatrixMode(GL10.GL_PROJECTION);  //Select The Projection Matrix
        gl.glLoadIdentity();                  //Reset The Projection Matrix
        
        //Create the game perspective
        GLU.gluPerspective(gl, GlRenderer.FIELD_OF_VIEW_ANGLE, (float)width / (float)height, GlRenderer.NEAR_CLIPPING_PLANE_DEPTH, GlRenderer.FAR_CLIPPING_PLANE_DEPTH);
        
        gl.glMatrixMode(GL10.GL_MODELVIEW);   //Select The Modelview Matrix
        gl.glLoadIdentity();                  //Reset  The Modelview Matrix
        
        TimingLogger timingLogger = new TimingLogger(GlRenderer.class.getSimpleName(), "onSurfaceChanged");
        this.gameDrawer.initiaslise(context); //(Re)initialise
        this.gameDrawer.loadGame();           //Load all texture
        timingLogger.addSplit("loaded all textures");
        timingLogger.dumpToLog();
        
        //((GameActivity) this.context).dismissProgressDialog();
    }
    
    /** 
     * Get the visible height of the GLSurfaceView at the given depth.
     * The height is calculated by the field of view and the depth.
     * 
     * @param depth to calculate the visible height
     */
    public static float getActualHeight(float depth) {
        double otherAngles = (180.0 - GlRenderer.FIELD_OF_VIEW_ANGLE) / 2.0;
        double hypotenuse = Math.abs(depth) / Math.sin(Math.toRadians(otherAngles));
        return (float) Math.sqrt(Math.pow(hypotenuse, 2.0) - Math.pow(Math.abs(depth), 2.0)) * 2;
    }
    
    /** 
     * Get the visible width of the GLSurfaceView.
     * The width is calculated by the height and the aspect ratio.
     * 
     * @param actualHeight is the calculated height from {@link #getActualHeight(float depth)}
     * @see #getActualHeight(float depth)
     */
    public static float getActualWidth(float actualHeight) {
        float aspectRatio = (float) GlRenderer.surfaceWidth / GlRenderer.surfaceHeight;
        return actualHeight * aspectRatio;
    }
    
    /** 
     * The Surface is created/initialised.
     * @see Renderer#onSurfaceCreated(GL10, EGLConfig)
     */
    @Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        this.gameDrawer = new GameDrawer(this.context, gl, this.gameData); //Create the game drawer instance
        
        gl.glShadeModel(GL10.GL_SMOOTH);                    //Enable Smooth Shading
        
        gl.glClearColor(1f, 1f, 1f, 1f);                    //Set Background color
        
        /* Set up the depth buffer */
        gl.glClearDepthf(1.0f);          //Depth Buffer Setup
        gl.glEnable(GL10.GL_DEPTH_TEST); //Enables Depth Testing
        gl.glDepthFunc(GL10.GL_LEQUAL);  //The Type Of Depth Testing To Do
        
        /* Set up blending */
        gl.glEnable(GL10.GL_BLEND);                                     //Enable blending
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA); //Set The Blending Function
        
        /* Set up face culling */
        gl.glEnable(GL10.GL_CULL_FACE); //Enable face culling
        gl.glCullFace(GL10.GL_BACK);    // specify which faces to not draw
        
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); // Really Nice Perspective Calculations
    }
    
    /** Free memory when an instance state is saved. */
    public void onSaveInstanceState() {
        this.gameDrawer.freeMemory();
        this.gameData.freeMemory();
    }
}
