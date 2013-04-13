package wildrune.ouyaframework.graphics;

import wildrune.ouyaframework.graphics.basic.*;
import wildrune.ouyaframework.math.*;
import android.graphics.Rect;
import android.util.Log;

/**
 * Class managing batching of sprites
 * Based on the XNA spritebatcher
 * @author Wildrune
 *
 */
public class SpriteBatch 
{
	private final static String LOG_TAG = "Spritebatch";
	
	private final static int POSITION_ELEMENT_COUNT = 3;
	private final static int COLOR_ELEMENT_COUNT = 4;
	private final static int UV_ELEMENT_COUNT = 2;
	private final static int VERTEX_ELEMENTS = POSITION_ELEMENT_COUNT * COLOR_ELEMENT_COUNT * UV_ELEMENT_COUNT;
	
	public enum SpriteEffect
	{
		NONE,
		HORIZONTAL_FLIP,
		VERTICAL_FLIP
	}
	
	public enum SpriteSortMode
	{
		DEFERRED,
		IMMEDIATE,
		TEXTURE,
		BACKTOFRONT,
		FRONTTOBACK
	}
	
	// Constants
	private final static int maxBatchSize = 2048;
	private final static int minBatchSize = 128;
	private final static int initialQueueSize = 64;
	private final static int verticesPerSprite = 4;
	private final static int indicesPerSprite = 6;
	
	// members
	private SpriteInfo[] 	spriteInfoQueue;
	private int 			spriteQueueCount;
	private int 			spriteQueueArraySize;
	private SpriteSortMode  spriteSortMode;
	private boolean			beginEndPair;
	
	// blendstate
	// samplerstate
	// depthstencilstate
	// rasterizerstate
	
	private VertexBuffer 	vertexBuffer;
	private IndexBuffer 	indexBuffer;
	
	private ShaderProgram 	spriteBatchProgram;
	private Mat4			transformMatrix;
	
	/**
	 * Constructors
	 */
	public SpriteBatch(ShaderProgram spriteProgram)
	{
		// create buffers
		vertexBuffer = new VertexBuffer(VERTEX_ELEMENTS * maxBatchSize * verticesPerSprite, false);
		indexBuffer = new IndexBuffer(maxBatchSize * indicesPerSprite, true);
		
		// generate buffers
		CreateIndexValues();
		
		// create the used shader program
		spriteBatchProgram = spriteProgram;
		
		// create the sprite queue
		spriteInfoQueue = new SpriteInfo[initialQueueSize];
		spriteQueueArraySize = initialQueueSize;
		spriteQueueCount = 0;
		beginEndPair = false;
		
		spriteSortMode = SpriteSortMode.DEFERRED;
		
	}
	
	/**
	 * Start the spritebatch drawing
	 */
	public void Begin(/* spritemode, blendstate, samplerstate, depthstate, rasterizerstate*/Mat4 transformMatrix)
	{
		// error check
		if(beginEndPair)
		{
			Log.e(LOG_TAG, "Cannot nest Begin calls on a single SpriteBatch");
			return;
		}
			
		// set render states and batch states
		this.transformMatrix = transformMatrix;
		
		// set start batching
		if(spriteSortMode == SpriteSortMode.IMMEDIATE)
		{
			PrepareForRendering();
		}
		
		beginEndPair = true;
	}
	
	/**
	 * End spritebatch drawing
	 */
	public void End()
	{
		// error check
		if(beginEndPair)
		{
			Log.e(LOG_TAG, "Begin must be called before End");
			return;
		}
		
		if(spriteSortMode != SpriteSortMode.IMMEDIATE)
		{
			PrepareForRendering();
			FlushBatch();
		}
		
		beginEndPair = false;
	}
	
	/**
	 * Draws a sprite
	 */
	public void DrawSprite(Texture2D texture, Rect destination, Rect sourceRect, Color color, Vec4 originDepthRotation)
	{
		// error chec
		if(texture == null)
			return;
		
		if(!beginEndPair)
			return;
		
		// grow sprite gueue if needed
		if(spriteQueueCount >= spriteQueueArraySize)
		{
			GrowSpriteQueue();
		}
		
		// get spriteInfo reference
		SpriteInfo spriteInfo = spriteInfoQueue[spriteQueueCount];
		
		// store spriteInfo parameters
		spriteInfo.color = color;
		spriteInfo.destination = destination;
		spriteInfo.originRotationDepth = originDepthRotation;
		spriteInfo.texture = texture;
		
		if(sourceRect != null)
		{
			// set the spriteInfo source rect
			spriteInfo.source.left = sourceRect.left / texture.width;
			spriteInfo.source.top = sourceRect.top / texture.height;
			spriteInfo.source.right = sourceRect.right / texture.width;
			spriteInfo.source.bottom = sourceRect.bottom / texture.height;
		}
		else
		{
			// set the spriteInfo source rect to use the whole texture
			spriteInfo.source.left = 0;
			spriteInfo.source.top = 0;
			spriteInfo.source.right = 1;
			spriteInfo.source.bottom = 1;
		}
		
		// check sort mode and react upon
		if(spriteSortMode == SpriteSortMode.IMMEDIATE)
			// draw the texture directly
			RenderBatch(texture, spriteInfo, 1);
		else
			// Queue this sprite for later sorting and batched rendering
			spriteQueueCount++;
	}
	
	/**
	 * Draws text
	 */
	public void DrawText(){}
	
	// INIT METHODS
	/**
	 * We can precache the indices as they will never change.
	 */
	private void CreateIndexValues()
	{
		// create temp array
		short[] indices = new short[maxBatchSize * indicesPerSprite];
		
		// fill the array
		int startOffset = 0;
		for(short i = 0; i < maxBatchSize * verticesPerSprite; i += verticesPerSprite)
		{
			indices[startOffset++] = i;
			indices[startOffset++] = (short) (i + 1);
			indices[startOffset++] = (short) (i + 2);
			
			indices[startOffset++] = (short) i;
			indices[startOffset++] = (short) (i + 2);
			indices[startOffset++] = (short) (i + 3);
			
		}
		
		// create the indexbuffer
		indexBuffer.SetData(0, indices, 0, indices.length);
		indexBuffer.Create();
	}
	
	// MANAGEMENT
	/**
	 * Grows the spritequeue when needed
	 */
	private void GrowSpriteQueue() 
	{
		int newSize = this.spriteQueueArraySize * 2;
		SpriteInfo[] newQueue = new SpriteInfo[newSize];
		
		// copy old data over
		for(int i = 0; i < this.spriteQueueCount; i++)
		{
			newQueue[i] = this.spriteInfoQueue[i]; 
		}
		
		this.spriteInfoQueue = newQueue;
		this.spriteQueueArraySize = newSize;
	}
	
	/**
	 * Sort the queue based on the spritesortmode
	 */
	private void SortSprites() 
	{
		switch(spriteSortMode)
		{
			case TEXTURE:
				break;
			case BACKTOFRONT:
				break;
			case FRONTTOBACK:
				break;
		}
	}
	
	// RENDER METHODS
	/**
	 * Set states, bind resources
	 */
	private void PrepareForRendering() 
	{
		spriteBatchProgram.Bind();
		vertexBuffer.Bind();
		indexBuffer.Bind();
	}
	
	private void FlushBatch() 
	{
		// sort the sprites
		SortSprites();
	}
	
	private void RenderBatch(Texture2D tex, SpriteInfo spriteInfo, int count)
	{
		
	}
	
	private void RenderSprite()
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
