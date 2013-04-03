package wildrune.ouyaframework.graphics.basic;

import static android.opengl.GLES20.*;
import android.util.Log;

/***
 * Takes care of handling the OpenGL ES 2.0 shaders
 * @author Wildrune
 *
 */
public class ShaderProgram 
{
	private static final String LOG_TAG = "ShaderProgram";
	
	/***
	 * Data members
	 */
	private int		mProgramID;
	private boolean mLinked;
	private boolean	mResolved;
	
	private Shader mVertex;
	private Shader mFragment;
	
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
		this.mProgramID = glCreateProgram();
		
		// return based on succession
		this.mResolved = (this.mProgramID > 0) ? true : false;
	}
	
	/**
	 * Create a shaderprogram directly from shaders
	 */	
	public boolean Build(String vertex, String fragment)
	{
		mVertex = new Shader( vertex, Shader.TYPE.VERTEX );
		mFragment = new Shader( fragment, Shader.TYPE.FRAGMENT );
		
		// compile shaders and check if we succeeded
		if(!mVertex.Compile() || !mFragment.Compile())
			return false;
		
		// attach shaders
		AttachShader(mVertex);
		AttachShader(mFragment);
		
		// link shaders and check if we succeeded
		if(!LinkShaders())
			return false;
		
		return true;
	}
	
	/***
	 * Attach a shader to the program
	 * Needs a vertex and fragment shader to function correctly
	 * @param shader The shader to attach to this program
	 */
	public void AttachShader(Shader shader)
	{
		// attach the shader
		glAttachShader(mProgramID, shader.GetID());
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
		glLinkProgram(mProgramID);
		
		// check for errors
		glGetProgramiv(mProgramID, GL_LINK_STATUS, linked, 0);
		mLinked = (linked[0] > 0) ? true : false;
		if( !(mLinked && mResolved) )
		{
			if(mResolved)
				Log.d(LOG_TAG, "Linking failed:\n" + glGetProgramInfoLog(mProgramID));
			else
				Log.d(LOG_TAG, "You should first resolve the program before linking!");
			
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
			glUseProgram(mProgramID);
	}
	
	/***
	 * Dispose of the shader program
	 */
	public void Dispose()
	{
		if(mResolved)
			glDeleteProgram(mProgramID);
		
		// dispose of the shader
		mVertex.Dispose();
		mFragment.Dispose();
	}
	
	/**
	 * Get an attribute location if it is contained in this shader program
	 * @param attribName name of the attribute location
	 * @return id of the found attribute location
	 */
	public int GetAttribLocation(String attribName)
	{
		return glGetAttribLocation( this.mProgramID , attribName);
	}
	
	/**
	 * Get an uniform location if it is contained in this shader program
	 * @param uniformName name of the uniform location
	 * @return id of the found uniform location
	 */
	public int GetUniformLocation(String uniformName)
	{
		return glGetUniformLocation( this.mProgramID, uniformName);
	}
	
	/**
	 * Set uniform 4x4 matrix array
	 * @param location
	 * @param matrix4
	 */
	public void SetUniform(int location, float[] matrix4)
	{
		glUniformMatrix4fv(location, 1, false, matrix4, 0);
	}
	
	/**
	 * Set uniform float
	 * @param location
	 * @param data
	 */
	public void SetUniform(int location, float data)
	{
		glUniform1f(location, data);
	}
	
	/**
	 * Set uniform int
	 * @param location
	 * @param data
	 */
	public void SetUniform(int location, int data)
	{
		glUniform1i(location, data);
	}
}
