package wildrune.ouyaframework.graphics;

import static android.opengl.GLES20.*;

import java.util.Arrays;
import java.util.Comparator;

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
	
	/**
	 * Texture comparator
	 */
	private class TextureComp implements Comparator<SpriteInfo>
	{
		@Override
		public int compare(SpriteInfo lhs, SpriteInfo rhs)
		{
			if(lhs == null || lhs.texture == null)
				return 1;
			
			if(rhs == null || rhs.texture == null)
				return -1;
			
			return lhs.texture.textureHandle - rhs.texture.textureHandle;
		}
	}
	
	/**
	 * BackToFront comparator
	 */
	private class BackToFrontComp implements Comparator<SpriteInfo>
	{
		@Override
		public int compare(SpriteInfo lhs, SpriteInfo rhs) 
		{
			if(lhs == null)
				return -1;
			
			if(rhs == null)
				return 1;
			
			return (int) (lhs.originRotationDepth.w - rhs.originRotationDepth.w);
		}
		
	}
	
	/**
	 * FrontToBack comparator
	 */
	private class FrontToBackComp implements Comparator<SpriteInfo>
	{
		@Override
		public int compare(SpriteInfo lhs, SpriteInfo rhs) 
		{
			if(lhs == null)
				return 1;
			
			if(rhs == null)
				return -1;
			
			return (int) (rhs.originRotationDepth.w - lhs.originRotationDepth.w);
		}
		
	}
	
	// static constants
	private final static String LOG_TAG = "Spritebatch";
	
	private final static int POSITION_ELEMENT_COUNT = 3;
	private final static int COLOR_ELEMENT_COUNT = 4;
	private final static int UV_ELEMENT_COUNT = 2;
	private final static int VERTEX_ELEMENTS = POSITION_ELEMENT_COUNT + COLOR_ELEMENT_COUNT + UV_ELEMENT_COUNT;
	private final static int BYTES_PER_FLOAT = 4;
	
	// batch data
	private final static int maxBatchSize = 1024;
	private final static int initialQueueSize = 64;
	private final static int verticesPerSprite = 4;
	private final static int indicesPerSprite = 6;
	
	// vector2 for corner offsets
	private final Vec2[] cornerOffsets;
	
	// comparators
	private final TextureComp TexComp = new TextureComp();
	private final BackToFrontComp BackFrontComp = new BackToFrontComp();
	private final FrontToBackComp FrontBackComp = new FrontToBackComp();
	
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
	
	// shader program locations
	int matrixLocaton;
	int aPosition;
	int aColor;
	int aTexCoord;
	int uTexOne;
	
	/**
	 * Constructors
	 */
	public SpriteBatch(Graphics graphics, ShaderProgram shaderProgram)
	{
		// create the used shader program
		spriteBatchProgram = shaderProgram;
		
		matrixLocaton = spriteBatchProgram.GetUniformLocation("uMVP");
		aPosition = spriteBatchProgram.GetAttribLocation("a_position");
		aColor = spriteBatchProgram.GetAttribLocation("a_color");
		aTexCoord = spriteBatchProgram.GetAttribLocation("a_texcoord_one");
		uTexOne = spriteBatchProgram.GetUniformLocation("u_texture_one");
		
		// create buffers
		vertexBuffer = new VertexBuffer(VERTEX_ELEMENTS * maxBatchSize * verticesPerSprite, false);
		vertexBuffer.Create();
		
		// generate indices
		indexBuffer = new IndexBuffer(maxBatchSize * indicesPerSprite, true);
		CreateIndexValues();
		
		// create the transform matrix
		transformMatrix = Mat4.CreateOrtho2D(graphics.viewportNormal.width(), graphics.viewportNormal.height());
		
		// create the sprite queue
		spriteInfoQueue = new SpriteInfo[initialQueueSize];
		for(int i = 0; i < initialQueueSize; i++)
		{
			spriteInfoQueue[i] = new SpriteInfo();
		}
		
		spriteQueueArraySize = initialQueueSize;
		spriteQueueCount = 0;
		beginEndPair = false;
		spriteSortMode = SpriteSortMode.DEFERRED;
		
		// create corner offsets
		cornerOffsets = new Vec2[4];
		cornerOffsets[0] = new Vec2(0, 0);
		cornerOffsets[1] = new Vec2(0, 1);
		cornerOffsets[2] = new Vec2(1, 1);
		cornerOffsets[3] = new Vec2(1, 0);
	}

	/**
	 * Dispose of used resources
	 */
	public void Dispose()
	{
		vertexBuffer.Dispose();
		indexBuffer.Dispose();
	}
	
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
			indices[startOffset++] = (short) (i + 2);
			indices[startOffset++] = (short) (i + 1);
			
			indices[startOffset++] = i;
			indices[startOffset++] = (short) (i + 3);
			indices[startOffset++] = (short) (i + 2);
			
		}
		
		// create the indexbuffer
		indexBuffer.SetData(0, indices, 0, indices.length);
		indexBuffer.Create();
		indexBuffer.Apply();
	}
	
	/**
	 * Start the spritebatch drawing
	 */
	public void Begin(SpriteSortMode sortMode/* blendstate, samplerstate, depthstate, rasterizerstate*/)
	{
		// error check
		if(beginEndPair)
		{
			Log.e(LOG_TAG, "Cannot nest Begin calls on a single SpriteBatch");
			return;
		}
		
		// set state
		spriteSortMode = sortMode;
		
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
		if(!beginEndPair)
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
	private void DrawSprite(Texture2D texture, 
			float destLeft, float destTop, float destRight, float destBottom,
			float sourceLeft, float sourceTop, float sourceRight, float sourceBottom,
			float r, float g, float b, float a,
			float originX, float originY,
			float depth, float rotation )
	{
		// error check
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
		
		if(spriteInfo == null)
			Log.d(LOG_TAG, "SpriteInfo is null i: " + spriteQueueCount );
		
		// set destionation
		spriteInfo.destination.left = (int) destLeft;
		spriteInfo.destination.top = (int) destTop;
		spriteInfo.destination.right = (int) destRight;
		spriteInfo.destination.bottom = (int) destBottom;
		
		// set color
		spriteInfo.color.r = r;
		spriteInfo.color.g = g;
		spriteInfo.color.b = b;
		spriteInfo.color.a = a;
		
		// set origin, rotation and depth
		spriteInfo.originRotationDepth.x = originX;
		spriteInfo.originRotationDepth.y = originY;
		spriteInfo.originRotationDepth.z = rotation;
		spriteInfo.originRotationDepth.w = depth;
		
		// set texture
		spriteInfo.texture = texture;
		
		// set the spriteInfo source rect
		spriteInfo.source.left = (int) (sourceLeft / texture.width);
		spriteInfo.source.top = (int) (sourceTop / texture.height);
		spriteInfo.source.right = (int) (sourceRight / texture.width);
		spriteInfo.source.bottom = (int) (sourceBottom / texture.height);
		
		// check sort mode and react upon
		if(spriteSortMode == SpriteSortMode.IMMEDIATE)
			// draw the texture directly
			RenderBatch(texture, spriteQueueCount, 1);
		else // Queue this sprite for later sorting and batched rendering
			spriteQueueCount++;
	}
	
	/**
	 * Standard draw sprite overload
	 * @param texture
	 * @param position
	 */
	public void DrawSprite(Texture2D texture, Vec2 position)
	{	
		// "draw" the sprite
		DrawSprite(texture, position.x, position.y, texture.width, texture.height,
				0, 0, 1, 1,
				1, 1, 1, 1,
				0, 0, 0, 0);
	}
	
	/**
	 * Draw sprite with color
	 * @param texture
	 * @param position
	 * @param color
	 */
	public void DrawSprite(Texture2D texture, Vec2 position, Color color)
	{
		// "draw" the sprite
		DrawSprite(texture, position.x, position.y, texture.width, texture.height,
				0, 0, 1, 1,
				color.r, color.g, color.b, color.a,
				0, 0, 0, 0);
	}
	
	/**
	 * Draw sprite with color, rotation and depth
	 * @param texture
	 * @param position
	 * @param color
	 * @param rotation
	 * @param depth
	 */
	public void DrawSprite(Texture2D texture, Vec2 position, Color color, float rotation, float depth)
	{
		// "draw" the sprite
		DrawSprite(texture, position.x, position.y, texture.width, texture.height,
				0, 0, 1, 1,
				color.r, color.g, color.b, color.a,
				0, 0, rotation, depth);
	}
	
	/**
	 * Draws text
	 */
	public void DrawText(Font font, Vec2 position, String text){}	// INIT METHODS

	
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
		
		// initialize new spriteinfo if needed
		for(int i = this.spriteQueueCount; i < newSize; i++)
		{
			// init new sprite info
			newQueue[i] = new SpriteInfo();
		}
		
		this.spriteInfoQueue = newQueue;
		this.spriteQueueArraySize = newSize;
	}
	
	/**
	 * Sort the queue based on the sprite sort mode
	 */
	private void SortSprites() 
	{
		switch(spriteSortMode)
		{
			case TEXTURE:
				Arrays.sort(spriteInfoQueue, this.TexComp);
				break;
			case BACKTOFRONT:
				Arrays.sort(spriteInfoQueue, this.BackFrontComp);
				break;
			case FRONTTOBACK:
				Arrays.sort(spriteInfoQueue, this.FrontBackComp);
				break;
		}
	}
	
	// RENDER METHODS
	/**
	 * Set states, bind resources
	 */
	private void PrepareForRendering() 
	{
		// set the shader program
		spriteBatchProgram.Bind();
		spriteBatchProgram.SetUniform(matrixLocaton, transformMatrix.elements);
		spriteBatchProgram.SetUniform(uTexOne, 0);
		
		// set the vertex buffer and attribute pointers
		vertexBuffer.Bind();
		int bytesPerVertex = VERTEX_ELEMENTS * BYTES_PER_FLOAT;
		vertexBuffer.SetVertexAttribPointer(0 * BYTES_PER_FLOAT, aPosition, 3, bytesPerVertex);
		vertexBuffer.SetVertexAttribPointer(3 * BYTES_PER_FLOAT, aColor, 4, bytesPerVertex);
		vertexBuffer.SetVertexAttribPointer(7 * BYTES_PER_FLOAT, aTexCoord, 2, bytesPerVertex);
		
		// set indexbuffer
		indexBuffer.Bind();
	}
	
	/**
	 * Start flushing the sprites to the GPU
	 */
	private void FlushBatch() 
	{
		if(spriteQueueCount <= 0)
			return;
		
		// sort the sprites
		//SortSprites();
		
		// used vars
		Texture2D batchTexture = null;
		int batchStart = 0;
		
		// iterate all sprites
		for(int pos = 0; pos < spriteQueueCount; pos++)
		{
			Texture2D spriteTexture = spriteInfoQueue[pos].texture;
			
			if(spriteTexture == null)
				Log.d(LOG_TAG, "Texture is null" );
			
			// compare textures
			if(spriteTexture.compareTo(batchTexture) != 0)
			{
				if(pos > batchStart)
				{
					RenderBatch(batchTexture, batchStart, pos - batchStart);
				}
				
				batchTexture = spriteTexture;
				batchStart = pos;
			}
		}
		
		// render final batch
		RenderBatch(batchTexture, batchStart, spriteQueueCount - batchStart);
		
		// clear data
		spriteQueueCount = 0;
	}
	
	/**
	 * Render a batch
	 * @param tex
	 * @param spriteBatchStart
	 * @param count
	 */
	private void RenderBatch(Texture2D tex, int spriteBatchStart, int count)
	{
		// bind the texture
		tex.Bind(0);
		int spriteCount = count;
		
		// iterate all sprites
		while(count > 0)
		{
			int batchSize = count;
			
			// int remainingSpace
			// check if we have room for all the sprites we want to draw
			
			// generate sprite vertex data
			for(int i = 0; i < batchSize; i++)
			{
				RenderSprite(spriteInfoQueue[ spriteBatchStart + i], verticesPerSprite * VERTEX_ELEMENTS * i );
			}
			
			count -= batchSize;
			spriteBatchStart += batchSize;
		}
		
		// draw the sprites
		vertexBuffer.Apply();
		glDrawElements(GL_TRIANGLES, spriteCount * indicesPerSprite, GL_UNSIGNED_SHORT, 0);
	}
	
	/**
	 * Generate the vertex attributes from sprite data
	 * and put these in the vertexbuffer
	 * @param sprite
	 */
	private void RenderSprite(SpriteInfo sprite, int vertBuffOffset)
	{		
		// put all vertices attributes in the buffer
		for(int i = 0; i < verticesPerSprite; i++)
		{
			// create vertex
			SpriteInfo.attributes[0] = (cornerOffsets[i].x * sprite.texture.width) + sprite.destination.left;
			SpriteInfo.attributes[1] = (cornerOffsets[i].y * sprite.texture.height) + sprite.destination.top;
			SpriteInfo.attributes[2] = sprite.originRotationDepth.w;
			
			SpriteInfo.attributes[3] = sprite.color.r;
			SpriteInfo.attributes[4] = sprite.color.g;
			SpriteInfo.attributes[5] = sprite.color.b;
			SpriteInfo.attributes[6] = sprite.color.a;
			
			SpriteInfo.attributes[7] = cornerOffsets[i].x;
			SpriteInfo.attributes[8] = cornerOffsets[i].y;
		
			// put into vertexbuffer
			vertexBuffer.SetData(vertBuffOffset + VERTEX_ELEMENTS * i, SpriteInfo.attributes, 0, VERTEX_ELEMENTS);
		}
	}
}
