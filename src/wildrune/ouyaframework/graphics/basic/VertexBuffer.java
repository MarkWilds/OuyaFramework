package wildrune.ouyaframework.graphics.basic;

import static android.opengl.GLES20.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Class acting as a wrapper for a GL VBO
 * @author Wildrune
 *
 */
public class VertexBuffer 
{
	// constants
	private final static int BYTES_PER_FLOAT = 4;
	
	// private members
	private final FloatBuffer vertexBuffer;
	
	// public members
	public int[] bufferId;
	
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
		
		// init id
		bufferId = new int[1];
		bufferId[0] = 0;
	}
	
	public boolean Create()
	{
		//glGenBuffers(1, bufferId);
		
		if(bufferId[0] == 0)
			return false;
		
		return true;
	}
	
	/**
	 * Sets a vertex atribute for this vertex buffer
	 * @param attribLocation the location of the attribute in the shader
	 */
	public void SetVertexAttribPointer(int dataOffsetFloats, int attribLocation, int componentCount, int strideBytes)
	{
		vertexBuffer.position(dataOffsetFloats);
		
	    // set vertex attribute
	    glVertexAttribPointer(attribLocation, componentCount, GL_FLOAT, false, strideBytes, vertexBuffer);
	    glEnableVertexAttribArray(attribLocation);
	    
	    vertexBuffer.position(0);
	}
}
