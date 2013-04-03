package wildrune.ouyaframework.graphics.basic;

import static android.opengl.GLES20.*;
import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * Representing a 2D texture
 * NOT THREAD safe
 * @author Wildrune
 *
 */
public class Texture2D 
{	
	private final static String LOG_TAG = "Texture2D";
	
	/**
	 * Data members
	 */
	public int[] textureId;
	
	/**
	 * Constructor
	 */
	public Texture2D()
	{
		textureId = new int[1];
		textureId[0] = 0;
	}
	
	public boolean Create(Bitmap bitmap, boolean mipmap)
	{
		if(bitmap == null)
			return false;
		
		// generate texture id
		glGenTextures(1, textureId, 0);
		
		// if we got a valid id
		if(textureId[0] != 0)
		{					
			// bind
			glBindTexture(GL_TEXTURE_2D, textureId[0]);
			
			// load texture to GPU
			GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
			
			SetSamplerState();
			
			// generate mipmaps
			if(mipmap)
				glGenerateMipmap(GL_TEXTURE_2D);
			
			// unbind
			glBindTexture(GL_TEXTURE_2D, 0);
			
			int error = glGetError();
			if(error != GL_NO_ERROR)
			{
				String strError = GLUtils.getEGLErrorString(error);
				Log.e(LOG_TAG, "Error: " + strError);
				return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Specify the sampler state for this texture
	 */
	public void SetSamplerState()
	{
		// set filtering
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		
		// set wrapping
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	}
	
	/**
	 * Bind the texture to a specified texture unit
	 * @param unit the texture unit to specify this texture to
	 */
	public void Bind(int unit)
	{
		if(this.textureId[0] == 0 || unit < 0)
			return;
		
		// set the unit we put this texture in
		glActiveTexture(GL_TEXTURE0 + unit );
		
		// bind the texture itself
		glBindTexture(GL_TEXTURE_2D, this.textureId[0]);
	}
	
	/**
	 * Unbind this texture
	 */
	public void Unbind()
	{
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	/**
	 * Dispose of this texture
	 */
	public void Dispose()
	{
		if(this.textureId[0] == 0)
			return;
		
		glDeleteTextures(1, this.textureId, 0);
	}
}
