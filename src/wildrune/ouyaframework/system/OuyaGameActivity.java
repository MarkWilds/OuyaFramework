package wildrune.ouyaframework.system;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import wildrune.ouyaframework.graphics.Graphics;
import tv.ouya.console.api.OuyaController;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.app.Activity;

/**
 * Abstract class that handles the game
 * @author Wildrune
 *
 */
public abstract class OuyaGameActivity extends Activity implements GLSurfaceView.Renderer
{
	// data members
	private volatile boolean isGameStopping;
	private GLSurfaceView gameView;
	
	// start flags
	protected boolean isDebugMode;
	protected boolean isLowResMode;
	protected boolean isFixedTimeStep;
	
	// ==================== SUBSYSTEMS ==========================
	protected Graphics 	gameGraphics;
	protected FileIO 	gameFileIO;
	private Clock		gameClock;
	
	// =====================  ABSTRACT METHODS ======================
	protected abstract void Create();
	protected abstract void Dispose();
	protected abstract void Update(float deltatime);
	protected abstract void Draw();
	
	// =====================  SYSTEM METHODS ======================
	/***
	 * Default constructor
	 */
	public OuyaGameActivity()
	{
		// default state
		isGameStopping = false;
		isLowResMode = false;
		isDebugMode = false;
	}
	
	/***
	 * Handle android app creation event
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		// initialize variables
		int usedWidth = 1920;
		int usedHeight = 1080;

		// initialize opengl view
		gameView = new GLSurfaceView( this );
		
		// set other options
		gameView.setPreserveEGLContextOnPause(true);
		gameView.setEGLContextClientVersion(2);
		
		// set the main view
		setContentView(gameView);

		// if we want better debug messages
		if(isDebugMode){
			gameView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);
		}

		// if we want 720p or 1080p
		if(isLowResMode){
			gameView.getHolder().setFixedSize(1280, 720);
			usedWidth = 1280;
			usedHeight = 720;
		}

		// initialize subsystems
		gameGraphics = new Graphics(usedWidth, usedHeight);
		gameFileIO = new FileIO(this);
		gameClock = new Clock();
		OuyaController.init(this);

		// when all configurations are set we start the rendering thread
		gameView.setRenderer(this);
	}
	
	/***
	 * Handle android pause event
	 */
	@Override
	protected void onPause() 
	{
		super.onPause();
		gameView.onPause();
		
		// check if we are finishing or not
		if(isFinishing())
			Dispose();
	}

	/***
	 * Handle android resume event
	 */
	@Override
	protected void onResume() 
	{
		super.onResume();
		gameView.onResume();
	}
	
	// =====================  GAME METHODS ======================
	/***
	 * Called when OpenGL ES is instantiated.
	 */
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) 
	{			
		// call subclass create method
		this.Create();
		
		// start our clock
		// IS THIS THE RIGHT PLACE??
		gameClock.Start();
	}

	/***
	 * Called when the surface dimensions changed.
	 */
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) 
	{
		// stub not needed on OUYA
	}
	
	/***
	 * Called as our main process method, not only for drawing,
	 * but also for updating!
	 */
	@Override
	public void onDrawFrame(GL10 gl) 
	{	
		// check if we are stopping
		if(isGameStopping && !isFinishing())
		{
			// close this android app
			this.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					// finish the app
					finish();
				}
			});
		}
		
		// update clock
		gameClock.Tick();
		
		// main game loop methods
		Update( gameClock.GetDeltaTime() );
		Draw();
	}
	
	// =====================  OUYA INPUT ======================
	/***
	 * Pass button down events to the OUYAController
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    return OuyaController.onKeyDown(keyCode, event);
	}

	/***
	 * Pass button up events to the OUYAController
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    return OuyaController.onKeyUp(keyCode, event);
	}

	/***
	 * Pass motion events to the OUYAController
	 */
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
	    return OuyaController.onGenericMotionEvent(event);
	}
	
	// =====================  GAME UTIL METHODS ======================
	/***
	 * Indicate that we want to close the game
	 */
	public void Exit(){
		isGameStopping = true;
	}
}