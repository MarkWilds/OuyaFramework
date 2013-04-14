package wildrune.ouyaframework.graphics;

import static android.opengl.GLES20.*;

import java.util.Arrays;
import java.util.Comparator;

import wildrune.ouyaframework.graphics.basic.*;
import wildrune.ouyaframework.math.*;
import android.graphics.Rect;
import android.opengl.GLES20;
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
		public int compare(SpriteInfo lhs, SpriteInfo rhs){
			return lhs.texture.textureHandle - rhs.texture.textureHandle;
		}
	}
	
	/**
	 * BackToFront comparator
	 */
	private class BackToFrontComp implements Comparator<SpriteInfo>
	{
		@Override
		public int compare(SpriteInfo lhs, SpriteInfo rhs) {
			return (int) (lhs.originRotationDepth.z - rhs.originRotationDepth.z);
		}
		
	}
	
	/**
	 * FrontToBack comparator
	 */
	private class FrontToBackComp implements Comparator<SpriteInfo>
	{
		@Override
		public int compare(SpriteInfo lhs, SpriteInfo rhs) {
			return (int) (rhs.originRotationDepth.z - lhs.originRotationDepth.z);
		}
		
	}
	
	// static constants
	private final static String LOG_TAG = "Spritebatch";
	
	private final static int BYTES_PER_FLOAT = 4;
	private final static int POSITION_ELEMENT_COUNT = 3;
	private final static int COLOR_ELEMENT_COUNT = 4;
	private final static int UV_ELEMENT_COUNT = 2;
	private final static int VERTEX_ELEMENTS = POSITION_ELEMENT_COUNT * COLOR_ELEMENT_COUNT * UV_ELEMENT_COUNT;
	
	// batch data
	private final static int maxBatchSize = 2048;
	//private final static int minBatchSize = 128;
	private final static int initialQueueSize = 64;
	private final static int verticesPerSprite = 4;
	private final static int indicesPerSprite = 6;
	
	// temp spriteinfo variable
	private final static SpriteInfo tempSprite = new SpriteInfo();
	
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
	
	/**
	 * Constructors
	 */
	public SpriteBatch(ShaderProgram spriteProgram, Graphics graphics)
	{
		// create buffers
		vertexBuffer = new VertexBuffer(VERTEX_ELEMENTS * maxBatchSize * verticesPerSprite, false);
		vertexBuffer.Create();
		
		// generate indices
		indexBuffer = new IndexBuffer(maxBatchSize * indicesPerSprite, true);
		CreateIndexValues();
		
		// create the used shader program
		spriteBatchProgram = spriteProgram;
		matrixLocaton = spriteBatchProgram.GetUniformLocation("uMVP");
		aPosition = spriteBatchProgram.GetAttribLocation("a_position");
		aColor = spriteBatchProgram.GetAttribLocation("a_color");
		aTexCoord = spriteBatchProgram.GetAttribLocation("a_texcoord_one");
		
		// create the transform matrix
		transformMatrix = Mat4.CreateOrtho2D(graphics.viewportNormal.width(), graphics.viewportNormal.height());
		
		// create the sprite queue
		spriteInfoQueue = new SpriteInfo[initialQueueSize];
		spriteQueueArraySize = initialQueueSize;
		spriteQueueCount = 0;
		beginEndPair = false;
		
		spriteSortMode = SpriteSortMode.DEFERRED;
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
	private void DrawSprite(Texture2D texture, Rect destination, Rect sourceRect, Color color, Vec4 originDepthRotation)
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
			RenderBatch(texture, spriteQueueCount, 1);
		else
			// Queue this sprite for later sorting and batched rendering
			spriteQueueCount++;
	}
	
	/**
	 * Standard draw sprite overload
	 * @param texture
	 * @param position
	 */
	public void DrawSprite(Texture2D texture, Vec2 position)
	{
		// set destination
		tempSprite.destination.left = (int)position.x;
		tempSprite.destination.top = (int)position.y;
		tempSprite.destination.right = texture.width;
		tempSprite.destination.bottom = texture.height;
		
		// "draw" the sprite
		DrawSprite(texture, tempSprite.destination, tempSprite.source, 
				Color.WHITE, tempSprite.originRotationDepth);
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
		
		// set the vertex buffer and attribute pointers
		vertexBuffer.Bind();
		vertexBuffer.SetVertexAttribPointer(0, aPosition, 3, VERTEX_ELEMENTS * BYTES_PER_FLOAT);
		vertexBuffer.SetVertexAttribPointer(3 * BYTES_PER_FLOAT, aColor, 4, VERTEX_ELEMENTS * BYTES_PER_FLOAT);
		vertexBuffer.SetVertexAttribPointer(7 * BYTES_PER_FLOAT, aTexCoord, 2, VERTEX_ELEMENTS * BYTES_PER_FLOAT);
		
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
		SortSprites();
		
		// used vars
		Texture2D batchTexture = null;;
		int batchStart = 0;
		
		// iterate all sprites
		for(int pos = 0; pos < spriteQueueCount; pos++)
		{
			Texture2D spriteTexture = spriteInfoQueue[pos].texture;
			
			// compare textures
			if(spriteTexture.compareTo(batchTexture) != 0)
			{
				if(pos > batchStart)
				{
					RenderBatch(batchTexture, batchStart, pos - batchStart);
				}
				
				batchTexture = spriteTexture;
				batchStart += pos;
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
		
		// iterate all sprites
		while(count > 0)
		{
			int batchSize = count;
			// int remainingSpace
			
			// check if we have room for all the sprites we want to draw
			
			// generate sprite vertex data
			for(int i = 0; i < batchSize; i++)
			{
				RenderSprite(spriteInfoQueue[ spriteBatchStart + i]);
			}
			
			count -= batchSize;
			spriteBatchStart += batchSize;
		}
		
		// draw the sprites
		glDrawElements(GL_TRIANGLES, count * indicesPerSprite, GL_UNSIGNED_SHORT, 0);
	}
	
	/**
	 * Generate the vertex attributes from sprite data
	 * and put these in the vertexbuffer
	 * @param sprite
	 */
	private void RenderSprite(SpriteInfo sprite)
	{
		// setup vertex attributes
		
		// put into vertexbuffer
	}
}
