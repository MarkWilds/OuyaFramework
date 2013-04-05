package wildrune.ouyaframework.graphics.basic;

import android.opengl.GLES20;
import android.util.Log;

/**
 * OpenGL ES 2.0 shader
 * @author Wildrune
 * @version 0.2
 *
 */
public class Shader 
{
	private static final String LOG_TAG = "Shader";
	
	/***
	 * Data members
	 */
	private String 		mShaderSource;
	private int 		mShaderType;
	private int			mShaderHandle;
	private boolean		mCompiled;
	
	/**
	 * Getters
	 */
	public String GetSource() { return mShaderSource; }
	public int GetType() { return mShaderType; }
	public int GetHandle() { return mShaderHandle; }
	public boolean IsCompiled() { return mCompiled; }
		
	/**
	 * Constructor
	 */
	public Shader(String source, int type)
	{
		this.mShaderSource = source;
		this.mShaderType = type;
		this.mShaderHandle = 0;
		this.mCompiled = false;
	}
	
	/**
	 * Create this shader object
	 * @return true if succeeded, false if not
	 */
	public boolean Create()
	{
		// Get a shader handle from openGL
		this.mShaderHandle = GLES20.glCreateShader( this.mShaderType );
		
		// set the shader source
		if(this.mShaderHandle > 0)
		{
			GLES20.glShaderSource(this.mShaderHandle, this.mShaderSource);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Dispose the shader
	 */
	public void Dispose()
	{
		if(this.mShaderHandle > 0)
		{
			GLES20.glDeleteShader(mShaderHandle);
			mShaderHandle = 0;
			mCompiled = false;
		}
	}
	
	/**
	 * Compiles the shader
	 * @return true on succes, false on failure
	 */
	public boolean Compile()
	{
		// local vars
		int[] compiled = new int[1];
		compiled[0] = 0;
		
		// compile the shader
		GLES20.glCompileShader(mShaderHandle);
		
		// check for errors
		GLES20.glGetShaderiv(mShaderHandle, GLES20.GL_COMPILE_STATUS, compiled, 0);
		mCompiled = (compiled[0] > 0);
		if( !(mCompiled && this.mShaderHandle > 0) )
		{
			if(this.mShaderHandle > 0)
				Log.e(LOG_TAG, "Compilation:\n" + GLES20.glGetShaderInfoLog(mShaderHandle) );
			else
				Log.e(LOG_TAG, "The shader is not created yet!");
			
			return false;
		}
		
		return true;
	}
}
