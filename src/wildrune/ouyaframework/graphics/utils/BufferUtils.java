package wildrune.ouyaframework.graphics.utils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class BufferUtils 
{
	static
	{
		System.loadLibrary("bufferutils");
	}
	
	public static FloatBuffer newFloatBuffer (int numFloats) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numFloats * 4);
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asFloatBuffer();
	}

	public static ShortBuffer newShortBuffer (int numShorts) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numShorts * 2);
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asShortBuffer();
	}
	
	public static IntBuffer newIntBuffer(int numInts)
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect(numInts * 4);
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asIntBuffer();
	}

	/**
	 * Native copy methods
	 */
	public static native void CopyJNI(float[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes);
	public static native void CopyJNI(short[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes);
	
	/** 
	 * Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements.
	 */
	public static void CopyFloats (float[] src, int srcOffset, FloatBuffer dst, int numElements) 
	{
		CopyJNI(src, srcOffset << 2, dst, dst.position() << 2, numElements << 2);
	}
	
	/** 
	 * Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements.
	 */
	public static void CopyShorts (short[] src, int srcOffset, ShortBuffer dst, int numElements) 
	{
		CopyJNI(src, srcOffset << 1, dst, dst.position() << 1, numElements << 1);
	}
}
