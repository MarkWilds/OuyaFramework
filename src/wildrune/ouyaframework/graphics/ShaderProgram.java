package wildrune.ouyaframework.graphics;

import android.opengl.GLES20;
import android.util.Log;

/***
 * Takes care of handling the OpenGL ES 2.0 shaders
 * @author Mark van der Wal
 *
 */
public class ShaderProgram 
{
	/***
	 * Data members
	 */
	private int		mProgramID;
	private boolean mLinked;
	private boolean	mResolved;
	
	private static final String TAG = "ShaderProgram";
	
	/***
	 * Getters
	 */
	public boolean IsLinked() { return mLinked; }
	public int GetID() { return mProgramID; }
	
	
	/***
	 * Default constructor
	 */
	public ShaderProgram()
	{
		this.mLinked = false;
		
		// create program
		this.mProgramID = GLES20.glCreateProgram();
		
		// return based on succession
		this.mResolved = (this.mProgramID > 0) ? true : false;
	}
	
	/***
	 * Attach a shader to the program
	 * Needs a vertex and fragment shader to function correctly
	 * @param shader The shader to attach to this program
	 */
	public void AttachShader(Shader shader)
	{
		// attach the shader
		GLES20.glAttachShader(mProgramID, shader.GetID());
	}
	
	/***
	 * Links the attached shader together
	 * @return true on succes, false on failure
	 */
	public boolean LinkShaders()
	{
		// local vars
		int[] linked = new int[1];
		linked[0] = 0;
		
		// link the program
		GLES20.glLinkProgram(mProgramID);
		
		// check for errors
		GLES20.glGetProgramiv(mProgramID, GLES20.GL_LINK_STATUS, linked, 0);
		mLinked = (linked[0] > 0) ? true : false;
		if( !(mLinked && mResolved) )
		{
			if(mResolved)
				Log.d(TAG, "Linking failed:\n" + GLES20.glGetProgramInfoLog(mProgramID));
			else
				Log.d(TAG, "You should first resolve the program before linking!");
			
			return false;
		}
		
		return true;
	}
	
	/***
	 * Sets this shader program as current
	 */
	public void Bind()
	{
		if(mResolved)
			GLES20.glUseProgram(mProgramID);
	}
	
	/***
	 * Dispose of the shader program
	 */
	public void Dispose()
	{
		if(mResolved)
			GLES20.glDeleteProgram(mProgramID);
	}
}
