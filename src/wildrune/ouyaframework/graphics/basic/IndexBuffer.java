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
	private final ShortBuffer indexBuffer;
	private final boolean isStatic;
	private final int usage;
	private int bufferHandle;
	private int bufferSize;
	
	/**
	 * Constructor
	 */
	public IndexBuffer(int bufferSize, boolean isStatic)
	{
		// set members
		this.isStatic = isStatic;
		this.usage = this.isStatic ? GL_STATIC_DRAW : GL_DYNAMIC_DRAW;
		this.bufferHandle = 0;
		this.bufferSize = 0;
		
		// create the float buffer
		indexBuffer = ByteBuffer
				.allocateDirect(bufferSize * BYTES_PER_SHORT)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer();
	}
	
	/**
	 * Creates the VBO and sets its data with the data from the shortbuffer
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
			
			Bind();
			
			// if this a dynamic buffer we create a empty GPU buffer
			if(usage == GL_DYNAMIC_DRAW)
			{
				glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * BYTES_PER_SHORT,
						null, this.usage);
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Sets the indices array for this buffer
	 * @param indexData the data set for this buffer
	 */
	public void SetData(int bufferOffset, short[] indexData, int offset, int length)
	{
		int endOffset = bufferOffset + (length - offset);
		if( endOffset > indexBuffer.limit())
		{
			Log.d(LOG_TAG, "Size to write is to big for the indexBuffer");
			return;
		}
		
		// create the short buffer
		bufferSize += length;
		indexBuffer.position(bufferOffset);
		indexBuffer.put(indexData, offset, length);
	}
	
	/**
	 * Resolves this buffer
	 */
	public void Apply()
	{		
		// set its data
		indexBuffer.position(0);
		
		if(usage == GL_STATIC_DRAW)
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, bufferSize * BYTES_PER_SHORT,
					this.indexBuffer, this.usage);
		else
			glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, bufferSize * BYTES_PER_SHORT, indexBuffer);
		
		indexBuffer.clear();
		bufferSize = 0;
		
		// error check
		int error = glGetError();
		if(error != GL_NO_ERROR)
		{
			String msg = GLUtils.getEGLErrorString(error);
			Log.d(LOG_TAG, msg);
		}
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
		
		indexBuffer.clear();
	}
}
