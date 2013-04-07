package wildrune.ouyaframework.graphics.basic;

import static android.opengl.GLES20.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import wildrune.ouyaframework.util.BufferUtils;
import wildrune.ouyaframework.util.interfaces.IDisposable;
import android.opengl.GLUtils;
import android.util.Log;

public class IndexBuffer implements IDisposable
{
	// constants
	private final static String LOG_TAG = "IndexBuffer";
	private final static int BYTES_PER_SHORT = 2;
	
	// this is used for retreiving handles without having to allocate new int's everytime
	private final static IntBuffer tempHandle = BufferUtils.newIntBuffer(1);
	
	// private members
	private final int bufferSize;
	private final ShortBuffer indexBuffer;
	private final boolean isStatic;
	private final int usage;
	private int bufferHandle;
	
	/**
	 * Constructor
	 */
	public IndexBuffer(int bufferSize, boolean isStatic)
	{
		// set members
		this.isStatic = isStatic;
		this.usage = this.isStatic ? GL_STATIC_DRAW : GL_DYNAMIC_DRAW;
		this.bufferSize = bufferSize;
		bufferHandle = 0;
		
		// create the float buffer
		indexBuffer = ByteBuffer
				.allocateDirect(bufferSize * BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer();
		indexBuffer.flip();
	}
	
	/**
	 * Sets the indices for this buffer
	 * @param indexData the data set for this buffer
	 */
	public void SetIndices(short[] indexData, int offset)
	{
		if(offset + indexData.length > bufferSize)
		{
			Log.d(LOG_TAG, "IndexBuffer out of space!");
			return;
		}
		
		// create the float buffer
		indexBuffer.put(indexData, offset , indexData.length);
		indexBuffer.position(0);
	}
	
	/**
	 * Creates the VBO and sets its data with the data from the shortBuffer
	 * @return true if created, false if not
	 */
	public boolean Create()
	{
		// create the buffer
		if(bufferHandle == 0)
		{
			glGenBuffers(1, tempHandle);
			bufferHandle = tempHandle.get(0);
			tempHandle.clear();
		}
		else // bound
		{
			Bind();
			
			// set its data
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.limit() * BYTES_PER_SHORT,
						this.indexBuffer, this.usage);
			
			// error check
			int error = glGetError();
			if(error != GL_NO_ERROR)
			{
				String msg = GLUtils.getEGLErrorString(error);
				Log.d(LOG_TAG, msg);
			}
			
			Unbind();
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Bind this buffer
	 */
	public void Bind()
	{
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferHandle);
	}
	
	/**
	 * Unbind this buffer
	 */
	public void Unbind()
	{
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	@Override
	public void Dispose() 
	{
		if(bufferHandle > 0)
		{
			tempHandle.clear();
			tempHandle.put(bufferHandle);
			tempHandle.flip();
			
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
			glDeleteBuffers(1, tempHandle);
			tempHandle.clear();
			bufferHandle = 0;
		}
	}
}
