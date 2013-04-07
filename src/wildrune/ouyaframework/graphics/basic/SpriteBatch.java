package wildrune.ouyaframework.graphics.basic;

import wildrune.ouyaframework.math.RuneMath;
import wildrune.ouyaframework.math.Vec2;
import wildrune.ouyaframework.math.Vec4;
import android.graphics.Rect;

/**
 * Class managing batching of sprites
 * Based on the XNA spritebatcher
 * @author Wildrune
 *
 */
public class SpriteBatch 
{
	// Constants
	public final static int maxBatchSize = 2048;
	public final static int minBatchSize = 128;
	public final static int initialQueueSize = 64;
	public final static int verticesPerSprite = 4;
	public final static int indicesPerSprite = 6;
	
	// members
	private SpriteInfo[] spriteInfoQueue;
	private int spriteQueueCount;
	private int spriteQueueArraySize;
	
	private VertexBuffer vertexBuffer;
	private IndexBuffer indexBuffer;
	
	/**
	 * Constructor
	 */
	public void SpriteBatch()
	{
		
	}
	
	
	/**
	 * Holding current frame sprite info!
	 * @author Wildrune
	 *
	 */
	private class SpriteInfo
	{
		public Rect source;
		public Rect destination;
		public Texture2D texture;
		public Color color;
		public Vec4 originRotationDepth;
		
		public SpriteInfo()
		{
			source = new Rect();
			destination = new Rect();
			texture = null;
			color = new Color();
			originRotationDepth = new Vec4();
		}
		
		/**
		 * Overloaded constructor
		 */
		public SpriteInfo(Texture2D tex, Rect source, Rect dest, Color color, Vec4 ord)
		{
			this.texture = tex;
			this.source = source;
			this.destination = dest;
			this.color = color;
			this.originRotationDepth = ord;
		}
	}

	/**
	 * Encodes the values into a vector4
	 */
	public static Vec4 ToOriginRotDepth(Vec2 origin, float rot, float depth)
	{
		return ToOriginRotDepth(origin.x, origin.y, rot, depth);
	}
	
	/**
	 * Encodes the values into a vector4
	 */
	public static Vec4 ToOriginRotDepth(float oX, float oY, float rot, float depth)
	{
		Vec4 encodedVec = RuneMath.GetVec4();
		
		// set values
		encodedVec.x = oX;
		encodedVec.y = oY;
		encodedVec.z = rot;
		encodedVec.w = depth;
		
		return encodedVec;
	}
}
