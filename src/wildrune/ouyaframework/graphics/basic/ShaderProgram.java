package wildrune.ouyaframework.graphics.basic;

import static android.opengl.GLES20.*;
import android.opengl.GLES20;
import android.util.Log;

/**
 * Takes care of handling the OpenGL ES 2.0 shaders
 * @author Wildrune
 *
 */
public class ShaderProgram 
{
	private static final String LOG_TAG = "ShaderProgram";
	
	/**
	 * Data members
	 */
	private int		mProgramHandle;
	private boolean mLinked;
	
	private Shader mVertex;
	private Shader mFragment;
	
	/**
	 * Getters
	 */
	public boolean IsLinked() { return mLinked; }
	public int GetHandle() { return mProgramHandle; }
	
	
	/**
	 * Default constructor
	 */
	public ShaderProgram()
	{
		this.mProgramHandle = 0;
		this.mLinked = false;
	}
	
	/**
	 * Create this shaderprogram
	 * @return true if succeeded, false if not
	 */
	public boolean Create()
	{
		// create program
		this.mProgramHandle = glCreateProgram();
		return (this.mProgramHandle > 0);
	}
	
	
	/**
	 * Dispose of the shader program
	 */
	public void Dispose()
	{
		if(this.mProgramHandle > 0)
		{
			glDeleteProgram(mProgramHandle);
			mProgramHandle = 0;
			mLinked = false;
		}
		
		// dispose of the shaders
		if(mVertex != null)
		{
			mVertex.Dispose();
			mVertex = null;
		}
		
		if(mFragment != null)
		{
			mFragment.Dispose();
			mFragment = null;
		}
	}
	
	/**
	 * Attach a shader to the program
	 * Needs a vertex and fragment shader to function correctly
	 * @param shader The shader to attach to this program
	 */
	private void AttachShader(Shader shader)
	{
		// attach the shader
		glAttachShader(mProgramHandle, shader.GetHandle());
	}
	
	/**
	 * Links the attached shader together, creating directly from the shaders inserted
	 * @return true on succes, false on failure
	 */
	public boolean LinkShaders(String vertex, String fragment)
	{
		Shader sVertex = new Shader( vertex, GLES20.GL_VERTEX_SHADER );
		Shader sFragment = new Shader( fragment, GLES20.GL_FRAGMENT_SHADER );

		// create shaders error check
		if( !sVertex.Create() || !sFragment.Create())
			return false;
		
		// compile shaders and check if we succeeded
		if(!sVertex.Compile() || !sFragment.Compile())
			return false;
		
		// link shaders and check if we succeeded
		if(!LinkShaders(sVertex, sFragment))
			return false;
		
		return true;
	}
	
	/**
	 * Links the attached shader together
	 * @return true on succes, false on failure
	 */
	public boolean LinkShaders(Shader vertex, Shader fragment)
	{
		// error check
		if( vertex == null || fragment == null)
			return false;
		
		// check if the shaders are compiled
		if(!vertex.Compile() || !fragment.Compile())
			return false;
		
		// local vars
		int[] linked = new int[1];
		linked[0] = 0;
		
		// set shader
		mVertex = vertex;
		mFragment = fragment;

		// attach shaders
		AttachShader(mVertex);
		AttachShader(mFragment);
		
		// link the program
		glLinkProgram(mProgramHandle);
		
		// check for errors
		glGetProgramiv(mProgramHandle, GL_LINK_STATUS, linked, 0);
		mLinked = (linked[0] > 0);
		if( !(mLinked && this.mProgramHandle > 0) )
		{
			if(this.mProgramHandle > 0)
				Log.e(LOG_TAG, "Linking failed:\n" + glGetProgramInfoLog(mProgramHandle));
			else
				Log.e(LOG_TAG, "You should first create the program before linking!");
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Sets this shader program as current
	 */
	public void Bind()
	{
		if(this.mProgramHandle > 0)
			glUseProgram(mProgramHandle);
	}
	
	/**
	 * Unbinds this shader
	 */
	public void Unbind()
	{
		if(this.mProgramHandle > 0)
			glUseProgram(0);
	}
	
	/**
	 * Get an attribute location if it is contained in this shader program
	 * @param attribName name of the attribute location
	 * @return id of the found attribute location
	 */
	public int GetAttribLocation(String attribName)
	{
		return glGetAttribLocation( this.mProgramHandle , attribName);
	}
	
	/**
	 * Get an uniform location if it is contained in this shader program
	 * @param uniformName name of the uniform location
	 * @return id of the found uniform location
	 */
	public int GetUniformLocation(String uniformName)
	{
		return glGetUniformLocation( this.mProgramHandle, uniformName);
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
	 * Set 2 floats
	 * @param location
	 * @param x, y floats
	 */
	public void SetUniform(int location, float x, float y)
	{
		glUniform2f(location, x, y);
	}
	
	/**
	 * Set 3 floats
	 * @param location
	 * @param x, y, z floats
	 */
	public void SetUniform(int location, float x, float y, float z)
	{
		glUniform3f(location, x, y, z);
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
