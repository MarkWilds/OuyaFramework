package wildrune.ouyaframework.graphics.basic;

import static android.opengl.GLES20.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.opengl.GLUtils;
import android.util.Log;

import wildrune.ouyaframework.util.BufferUtils;
import wildrune.ouyaframework.util.interfaces.IDisposable;

/**
 * Class acting as a wrapper for a GL VBO
 * @author Wildrune
 *
 */
public class VertexBuffer implements IDisposable
{
	// constants
	private final static String LOG_TAG = "VertexBuffer";
	private final static int BYTES_PER_FLOAT = 4;
	
	// this is used for retreiving handles without having to allocate new int's everytime
	private final static IntBuffer tempHandle = BufferUtils.newIntBuffer(1);
	
	// private members
	private final int bufferSize;
	private final FloatBuffer vertexBuffer;
	private final boolean isStatic;
	private final int usage;
	private int bufferHandle;
	
	/**
	 * Constructor
	 */
	public VertexBuffer(int bufferSize, boolean isStatic)
	{
		// set members
		this.isStatic = isStatic;
		this.usage = this.isStatic ? GL_STATIC_DRAW : GL_DYNAMIC_DRAW;
		this.bufferSize = bufferSize;
		bufferHandle = 0;
		
		// create the float buffer
		vertexBuffer = ByteBuffer
				.allocateDirect(bufferSize * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer();
		vertexBuffer.flip();
	}
	
	/**
	 * Sets the vertices for this buffer
	 * @param vertexData the data set for this buffer
	 */
	public void SetVertices(float[] vertexData, int offset)
	{
		if(offset + vertexData.length > bufferSize)
		{
			Log.d(LOG_TAG, "VertexBuffer out of space!");
			return;
		}
		
		// create the float buffer
		vertexBuffer.put(vertexData, offset , vertexData.length);
		vertexBuffer.position(0);
	}
	
	/**
	 * Creates the VBO and sets its data with the data from the floatbuffer
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
			glBufferData(GL_ARRAY_BUFFER, vertexBuffer.limit() * BYTES_PER_FLOAT,
						this.vertexBuffer, this.usage);
			
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
		glBindBuffer(GL_ARRAY_BUFFER, bufferHandle);
	}
	
	/**
	 * Unbind this buffer
	 */
	public void Unbind()
	{
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	/**
	 * Sets a vertex atribute for this vertex buffer
	 * @param attribLocation the location of the attribute in the shader
	 */
	public void SetVertexAttribPointer(int dataOffsetFloats, int attribLocation, int componentCount, int strideBytes)
	{
	    // set vertex attribute
		glVertexAttribPointer(attribLocation, componentCount, GL_FLOAT, false, strideBytes, dataOffsetFloats);
	    glEnableVertexAttribArray(attribLocation);
	}

	@Override
	public void Dispose() 
	{
		if(bufferHandle > 0)
		{
			tempHandle.clear();
			tempHandle.put(bufferHandle);
			tempHandle.flip();
			
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glDeleteBuffers(1, tempHandle);
			tempHandle.clear();
			bufferHandle = 0;
		}
	}
}
