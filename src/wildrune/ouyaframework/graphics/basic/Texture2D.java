package wildrune.ouyaframework.graphics.basic;

import static android.opengl.GLES20.*;

import wildrune.ouyaframework.utils.interfaces.IDisposable;
import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * Representing a 2D texture
 * NOT THREAD safe
 * @author Wildrune
 *
 */
public class Texture2D implements IDisposable, Comparable<Texture2D>
{	
	private final static String LOG_TAG = "Texture2D";
	
	// this is used for retreiving handles without having to allocate new int's everytime
	private final static int[] temp = new int[1];
	
	/**
	 * Data members
	 */
	public int textureHandle;
	public int width;
	public int height;
	
	/**
	 * Constructor
	 */
	public Texture2D()
	{
		textureHandle = 0;
	}
	
	/**
	 * Compare this texture against another texture
	 */
	@Override
	public int compareTo(Texture2D tex)
	{
		if(tex == null)
			return 1;
		
		return this.textureHandle - tex.textureHandle;
	}
	
	/**
	 * Create this texture from a bitmap
	 * @param bitmap
	 * @param mipmap
	 * @return
	 */
	public boolean Create(Bitmap bitmap, boolean mipmap)
	{
		if(bitmap == null)
		{
			Log.d(LOG_TAG, "Bitmap is null");
			return false;
		}
		
		// set dimensions
		width = bitmap.getWidth();
		height = bitmap.getHeight();
		
		// generate texture id
		glGenTextures(1, temp, 0);
		
		// if we got a valid id
		if(temp[0] > 0)
		{
			// set id
			textureHandle = temp[0];
			temp[0] = 0;
			
			// bind
			glBindTexture(GL_TEXTURE_2D, textureHandle);
			
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
				String msg = GLUtils.getEGLErrorString(error);
				Log.d(LOG_TAG, msg);
			}
			
			return true;
		}
		
		Log.d(LOG_TAG, "No valid texture handle could be retreived");
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
		
		//int wrap = GL_REPEAT;
		int wrap = GL_CLAMP_TO_EDGE;
		
		// set wrapping
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrap);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrap);
	}
	
	/**
	 * Bind the texture to a specified texture unit
	 * @param unit the texture unit to specify this texture to
	 */
	public void Bind(int unit)
	{
		if(textureHandle == 0 || unit < 0)
			return;
		
		// set the unit we put this texture in
		glActiveTexture(GL_TEXTURE0 + unit );
		
		// bind the texture itself
		glBindTexture(GL_TEXTURE_2D, textureHandle);
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
	@Override
	public void Dispose()
	{
		if(textureHandle == 0)
			return;
		
		temp[0] = textureHandle;
		glDeleteTextures(1, temp, 0);
		textureHandle = temp[0] = 0;
	}
}
