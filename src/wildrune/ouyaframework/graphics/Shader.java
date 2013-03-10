package wildrune.ouyaframework.graphics;

import android.opengl.GLES20;
import android.util.Log;

/***
 * OpenGL ES 2.0 shader
 * @author Wildrune
 * @version 0.1
 *
 */
public class Shader 
{
	/***
	 * Data members
	 */
	private String 		mShaderSource;
	private int 		mShaderType;
	private int			mShaderID;
	private boolean		mResolved;
	private boolean		mCompiled;
	
	private static final String TAG = "Shader";
	
	/***
	 * Getters
	 */
	public String GetSource() { return mShaderSource; }
	public int GetType() { return mShaderType; }
	public int GetID() { return mShaderID; }
	public boolean IsResolved() { return mResolved; }
	public boolean IsCompiled() { return mCompiled; }
	
	/***
	 * Shader types supported by OpenGL ES 2.0
	 */
	public enum TYPE
	{
		VERTEX,
		FRAGMENT;
	}
		
	/***
	 * Constructor
	 */
	public Shader(String source, Shader.TYPE type)
	{
		this.mShaderSource = source;
		this.mShaderType = TranslateType(type);
		this.mShaderID = -1;
		this.mCompiled = false;
		
		// Get a shader handle from openGL
		this.mShaderID = GLES20.glCreateShader( this.mShaderType );
		
		// return based on succession
		this.mResolved = (this.mShaderID > 0) ? true : false;
		
		// set the shader source
		if(this.mResolved)
			GLES20.glShaderSource(this.mShaderID, this.mShaderSource);
	}
	
	/***
	 * Compiles the shader
	 * @return true on succes, false on failure
	 */
	public boolean Compile()
	{
		// local vars
		int[] compiled = new int[1];
		compiled[0] = 0;
		
		// compile the shader
		GLES20.glCompileShader(mShaderID);
		
		// check for errors
		GLES20.glGetShaderiv(mShaderID, GLES20.GL_COMPILE_STATUS, compiled, 0);
		mCompiled = (compiled[0] > 0) ? true : false;
		if( !(mCompiled && mResolved) )
		{
			if(mResolved)
				Log.d(TAG, "Compilation:\n" + GLES20.glGetShaderInfoLog(mShaderID) );
			else
				Log.d(TAG, "You should first resolve the shader before compiling");
			
			return false;
		}
		
		return true;
	}
	
	/***
	 * Dispose the shader
	 */
	public void Dispose()
	{
		GLES20.glDeleteShader(mShaderID);
		mShaderID = -1;
		mResolved = false;
		mCompiled = false;
	}
	
	/***
	 * Helper method transfer shader enum to openGL enum
	 * @param type The shader type
	 * @return the openGL shader type
	 */
	private int TranslateType(TYPE type)
	{
		// check which type it is
		switch(type)
		{
		case VERTEX:
			return GLES20.GL_VERTEX_SHADER;
		case FRAGMENT:
			return GLES20.GL_FRAGMENT_SHADER;
		default:
			return -1;
		}
	}
}
