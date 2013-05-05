package wildrune.ouyaframework;

import static android.opengl.GLES20.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import tv.ouya.console.api.OuyaController;

import wildrune.ouyaframework.audio.AudioSystem;
import wildrune.ouyaframework.graphics.GraphicsSystem;
import wildrune.ouyaframework.graphics.utils.MultisampleConfigChooser;

import android.app.Activity;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Abstract class that handles the game
 * @author Wildrune
 *
 */
public abstract class OuyaGameActivity extends Activity implements GLSurfaceView.Renderer
{
	// data members
	private volatile boolean 	isGameStopping;
	private GLSurfaceView 		gameView;
	
	// start flags
	protected boolean isDebugMode;
	protected boolean isLowResMode;
	protected boolean isSampling;
	
	// ==================== SUBSYSTEMS =============================
	public GraphicsSystem 	Graphics;
	public FileSystem 		FileIO;
	public AudioSystem  	Audio;
	public ResourceSystem	Resources;

	// ===================== GAME CLOCK/TIMER =======================
	private ClockSystem			Clock;
	public ClockSystem.Timer 	gameTimer;
	private float				mAccumulatedFrameTime;

	// =====================  ABSTRACT METHODS ======================
	protected abstract void Create();
	protected abstract void Dispose();
	protected abstract void Update(float dt);
	protected abstract void Draw();
	
	// =====================  SYSTEM METHODS ========================
	/***
	 * Default constructor
	 */
	public OuyaGameActivity()
	{
		// default state
		isGameStopping = false;
		isLowResMode = false;
		isDebugMode = false;
		isSampling = false;
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
		
		// set options
		if(isSampling){
			gameView.setEGLConfigChooser(new MultisampleConfigChooser());
		}

		if(isDebugMode){
			gameView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);
		}

		if(isLowResMode){
			// if we want 720p or 1080p
			gameView.getHolder().setFixedSize(1280, 720);
			usedWidth = 1280;
			usedHeight = 720;
		}

		// initialize subsystems
		Graphics = new GraphicsSystem(usedWidth, usedHeight);
		FileIO = new FileSystem(this);
		Resources = new ResourceSystem(FileIO);
		Audio = new AudioSystem(this);
		Audio.Create();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		Clock = new ClockSystem();
		Clock.SetMaxFrameTime(500);
		gameTimer = Clock.Get();
		
		// OUYA initialization
		OuyaController.init(this);

		// when all configurations are set we start the rendering thread
		setContentView(gameView);
		gameView.setRenderer(this);
	}
	
	/***
	 * Handle android pause event
	 */
	@Override
	protected void onPause() 
	{
		// check if we are finishing or not
		if(isFinishing())
		{
			// release game
			Dispose();
			
			// release game subsystems
			Audio.Dispose();
		}

		gameView.onPause();
		super.onPause();
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
	 * Called w   hen OpenGL ES is instantiated.
	 */
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) 
	{		
		// enable states 
		glEnable(GL_BLEND);
		
		// sample coverage
		if(isSampling)
			glEnable(GL_SAMPLE_COVERAGE);
		
		// start our clock
		Clock.Start();
		
		// call subclass create method
		Create();
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
		Clock.Tick();
		
		// game timing
		mAccumulatedFrameTime += gameTimer.GetElapsedMiliseconds();
		
		// update game
		float targetFrameTime = Clock.GetTargetFrameTime();
		while(mAccumulatedFrameTime >= targetFrameTime)
		{
			// set fixed time
			Update( targetFrameTime / 1000.0f );
			mAccumulatedFrameTime -= targetFrameTime;
		}

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
