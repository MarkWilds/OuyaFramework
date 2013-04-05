package wildrune.ouyaframework.graphics.basic;

import static android.opengl.GLES20.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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
	private final static int BYTES_PER_FLOAT = 4;
	
	// this is used for retreiving handles without having to allocate new int's everytime
	private final static IntBuffer tempHandle = BufferUtils.newIntBuffer(1);
	
	// private members
	private final FloatBuffer vertexBuffer;
	
	// public members
	public int bufferHandle;
	
	/**
	 * Constructor
	 */
	public VertexBuffer(float[] vertexData)
	{
		// create the float buffer
		vertexBuffer = ByteBuffer
				.allocateDirect(vertexData.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(vertexData);
		vertexBuffer.flip();
		
		// init id
		bufferHandle = 0;
	}
	
	public boolean Create()
	{
		glGenBuffers(1, tempHandle);
		
		if(bufferHandle == 0)
			return false;
		
		glGenBuffers(1, tempHandle);
		bufferHandle = tempHandle.get(0);
		
		// HACK FOR TESTING
		if(vertexBuffer.limit() > 0)
			glBufferData(GL_ARRAY_BUFFER, vertexBuffer.limit(), vertexBuffer, GL_STATIC_DRAW);
		
		return true;
	}
	
	/**
	 * Bind this buffer
	 */
	public void Bind()
	{
		if(bufferHandle > 0)
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
		//vertexBuffer.position(dataOffsetFloats);
		
	    // set vertex attribute
	    //glVertexAttribPointer(attribLocation, componentCount, GL_FLOAT, false, strideBytes, vertexBuffer);
		glVertexAttribPointer(attribLocation, componentCount, GL_FLOAT, false, strideBytes, dataOffsetFloats);
	    glEnableVertexAttribArray(attribLocation);
	    
	    //vertexBuffer.position(0);
	}

	@Override
	public void Dispose() 
	{
		if(bufferHandle != 0)
		{
			tempHandle.clear();
			tempHandle.put(bufferHandle);
			tempHandle.flip();
			
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glDeleteBuffers(1, tempHandle);
			bufferHandle = 0;
		}
	}
}
