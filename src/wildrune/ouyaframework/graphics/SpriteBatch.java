package wildrune.ouyaframework.graphics;

import static android.opengl.GLES20.*;

import java.util.Arrays;
import java.util.Comparator;

import wildrune.ouyaframework.graphics.basic.*;
import wildrune.ouyaframework.math.*;
import android.util.Log;

/**
 * Class managing batching of sprites
 * Based on the XNA spritebatcher
 * @author Wildrune
 *
 */
public class SpriteBatch 
{
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
	private static final Vec2[] cornerOffsets;
	
	// Allows the sprite to be rendered flipped
	public enum SpriteEffect
	{
		NONE,
		FLIP_HORIZONTAL,
		FLIP_VERTICAL;
	}
	
	// sprites can be sorted in different ways
	public enum SpriteSortMode
	{
		DEFERRED,
		IMMEDIATE,
		TEXTURE,
		BACKTOFRONT,
		FRONTTOBACK
	}
	
	// spritebath shaders
	private final String vShader =
			"attribute vec3 a_position;" +
			"attribute vec4 a_color;" +
			"attribute vec2 a_texcoord_one;" +
		
			"uniform mat4 uMVP;" +
		
			"varying vec4 v_color;" +
			"varying vec2 v_texcoord_one;" +
		
			"void main()" +
			"{" +
				"gl_Position = uMVP * vec4(a_position, 1.0);" +  
				"v_color = a_color;" +
				"v_texcoord_one = a_texcoord_one;" +              
			"}";
	
	
	private final String fShader = 
			"precision mediump float;" +
			"uniform sampler2D u_texture_one;" +	
			"varying vec4 v_color;" +
			"varying vec2 v_texcoord_one;" +
			"void main()" +
			"{" +
				"vec4 newColor = v_color * texture2D(u_texture_one, v_texcoord_one );" +
				"gl_FragColor = newColor;" +
			"}";
	
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
	
	private float[] intermBuffer;
	private int		intermCount;
	
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
	
	// initialize static fields
	static
	{
		// create corner offsets
		cornerOffsets = new Vec2[4];		
		cornerOffsets[0] = new Vec2(0, 0);
		cornerOffsets[1] = new Vec2(1, 0);
		cornerOffsets[2] = new Vec2(0, 1);
		cornerOffsets[3] = new Vec2(1, 1);
	}
	
	/**
	 * Constructors
	 */
	public SpriteBatch(Graphics graphics)
	{
		// create the used shader program
		spriteBatchProgram = new ShaderProgram();
		
		// create program
		if(!spriteBatchProgram.Create())
			Log.d(LOG_TAG, "Could not create shaderProgram");
		
		if(!spriteBatchProgram.LinkShaders(vShader, fShader))
			Log.d(LOG_TAG, "Could not link shaderProgram");
		
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
		
		// optimizations
		intermBuffer = new float[verticesPerSprite * VERTEX_ELEMENTS * spriteQueueArraySize];
		intermCount = 0;
	}

	/**
	 * Dispose of used resources
	 */
	public void Dispose()
	{
		spriteBatchProgram.Dispose();
		vertexBuffer.Dispose();
		indexBuffer.Dispose();
		spriteInfoQueue = null;
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
			
			indices[startOffset++] = (short) (i + 1);
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
	 * Start the spritebatch drawing
	 */
	public void Begin()
	{
		this.Begin(this.spriteSortMode);
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
	 * Grows the intermediate buffe when needed
	 */
	private void GrowIntermBuffer()
	{
		int newSize = this.spriteQueueArraySize * 2;
		float[] newBuffer = new float[newSize * verticesPerSprite * VERTEX_ELEMENTS];
		
		// copy old data over
		for(int i = 0; i < this.intermCount; i++)
		{
			newBuffer[i] = this.intermBuffer[i];
		}
		
		this.intermBuffer = newBuffer;
	}
	
	/**
	 * Sort the queue based on the sprite sort mode
	 */
	private void SortSprites() 
	{
		switch(spriteSortMode)
		{
			case TEXTURE:
				Arrays.sort(spriteInfoQueue, 0, spriteQueueCount, this.TexComp);
				break;
			case BACKTOFRONT:
				Arrays.sort(spriteInfoQueue, 0, spriteQueueCount, this.BackFrontComp);
				break;
			case FRONTTOBACK:
				Arrays.sort(spriteInfoQueue, 0, spriteQueueCount, this.FrontBackComp);
				break;
		}
	}
	
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
		Texture2D spriteTexture = null;
		SpriteInfo[] localSpriteQueue = spriteInfoQueue;
		int batchStart = 0;
		
		// iterate all sprites
		for(int pos = 0; pos < spriteQueueCount; pos++)
		{
			spriteTexture = localSpriteQueue[pos].texture;
			
			// if the textures are not the same we will draw a batch
			if(spriteTexture.compareTo(batchTexture) != 0)
			{
				// only if the pos is higher than batch start we will draw
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
		
		// reset queue
		spriteQueueCount = 0;
	}
	
	/**
	 * Render a batch
	 * @param tex the texture to batch with
	 * @param spriteBatchStart where to start batching from
	 * @param count the amount of sprites to draw
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
		
		// upload data to the GPU
		vertexBuffer.SetData(0, intermBuffer, 0, intermCount);
		vertexBuffer.Apply();
		intermCount = 0;
		
		// draw the sprites
		glDrawElements(GL_TRIANGLES, spriteCount * indicesPerSprite, GL_UNSIGNED_SHORT, 0);
	}
	
	/**
	 * Generate the vertex attributes from sprite data
	 * and put these in the vertexbuffer
	 * @param sprite the sprite to add to the vertexbuffer
	 * @param vertBuffOffset where to start putting the data in the vertexbuffer
	 */
	private void RenderSprite(SpriteInfo sprite, int vertBuffOffset)
	{		
		// used variables
		int buffOffset = 0;
		float x, y, posX, posY;
		float cos = 1.0f;
		float sin = 0.0f;
		float rotation = sprite.originRotationDepth.z;
		int effect = sprite.spriteEffect.ordinal();
		
		// rotate sprite
		if(rotation != 0.0f)
		{
			cos = (float) Math.cos(rotation * RuneMath.TORAD);
			sin = (float) Math.sin(rotation * RuneMath.TORAD);
		}
		
		// put all vertices attributes in the buffer
		for(int i = 0; i < verticesPerSprite; i++)
		{
			buffOffset = vertBuffOffset + VERTEX_ELEMENTS * i;
			
			// scale and offset
			x = cornerOffsets[i].x * sprite.destination.width - sprite.originRotationDepth.x;
			y = cornerOffsets[i].y * sprite.destination.height - sprite.originRotationDepth.y;
			
			// rotate the created points
			posX = x * cos - y * sin;
			posY = x * sin + y * cos;
			
			// set the position and depth in a temp buffer
			intermBuffer[buffOffset] 	 = (int)(posX + sprite.destination.x);
			intermBuffer[buffOffset + 1] = (int)(posY + sprite.destination.y);
			intermBuffer[buffOffset + 2] = sprite.originRotationDepth.w;

			// colors
			intermBuffer[buffOffset + 3] = sprite.color.r;
			intermBuffer[buffOffset + 4] = sprite.color.g;
			intermBuffer[buffOffset + 5] = sprite.color.b;
			intermBuffer[buffOffset + 6] = sprite.color.a;
			
			// texture coordinates
			intermBuffer[buffOffset + 7] = cornerOffsets[i ^ effect].x * sprite.source.width + sprite.source.x;
			intermBuffer[buffOffset + 8] = cornerOffsets[i ^ effect].y * sprite.source.height + sprite.source.y;
			
			intermCount += VERTEX_ELEMENTS; 
		}
	}
	
	/**
	 * Draws a sprite
	 */
	public void DrawSprite(Texture2D texture, 
			float destLeft, float destTop, float destRight, float destBottom,
			float sourceLeft, float sourceTop, float sourceRight, float sourceBottom,
			float r, float g, float b, float a,
			float originX, float originY,
			float depth, float rotation,
			SpriteEffect effect)
	{
		if(this.spriteQueueCount >= SpriteBatch.maxBatchSize)
			return;
		
		// error check
		if(texture == null)
			return;
		
		if(!beginEndPair)
			return;
		
		// grow sprite gueue if needed
		if(spriteQueueCount >= spriteQueueArraySize)
		{
			GrowSpriteQueue();
			GrowIntermBuffer();
		}
		
		// get spriteInfo reference
		SpriteInfo spriteInfo = spriteInfoQueue[spriteQueueCount];
		
		// set destionation
		spriteInfo.destination.x = destLeft;
		spriteInfo.destination.y = destTop;
		spriteInfo.destination.width = destRight;
		spriteInfo.destination.height = destBottom;
		
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
		
		// set texture & effect
		spriteInfo.spriteEffect = effect;
		spriteInfo.texture = texture;
		
		// set the spriteInfo source rect
		spriteInfo.source.x = sourceLeft / texture.width;
		spriteInfo.source.y = sourceTop / texture.height;
		spriteInfo.source.width = sourceRight / texture.width;
		spriteInfo.source.height = sourceBottom / texture.height;
		
		// check sort mode and react upon
		if(spriteSortMode == SpriteSortMode.IMMEDIATE)
			// draw the texture directly
			RenderBatch(texture, spriteQueueCount, 1);
		else // Queue this sprite for later sorting and batched rendering
			spriteQueueCount++;
	}
	
	/**
	 * Standard draw sprite overload
	 */
	public void DrawSprite(Texture2D texture, Vec2 position, SpriteEffect effect)
	{	
		// "draw" the sprite
		DrawSprite(texture, position.x, position.y, texture.width, texture.height,
				0, 0, texture.width, texture.height,
				1, 1, 1, 1,
				0, 0, 0, 0,
				effect);
	}
	
	/**
	 * Draw sprite with color, origin
	 */
	public void DrawSprite(Texture2D texture, Vec2 position, Color color, SpriteEffect effect)
	{
		// "draw" the sprite
		DrawSprite(texture, position.x, position.y, texture.width, texture.height,
				0, 0, texture.width, texture.height,
				color.r, color.g, color.b, color.a,
				0, 0, 0, 0,
				effect);
	}
	
	/**
	 * Draw sprite with color, origin
	 */
	public void DrawSprite(Texture2D texture, Vec2 position, Color color, Vec2 origin, SpriteEffect effect)
	{
		// "draw" the sprite
		DrawSprite(texture, position.x, position.y, texture.width, texture.height,
				0, 0, texture.width, texture.height,
				color.r, color.g, color.b, color.a,
				origin.x, origin.y, 0, 0,
				effect);
	}
	
	/**
	 * Draw sprite with color, origin, rotation and depth
	 */
	public void DrawSprite(Texture2D texture, Vec2 position, Color color, Vec2 origin, float rotation, SpriteEffect effect)
	{
		// "draw" the sprite
		DrawSprite(texture, position.x, position.y, texture.width, texture.height,
				0, 0, texture.width, texture.height,
				color.r, color.g, color.b, color.a,
				origin.x, origin.y, 0 , rotation,
				effect);
	}
	
	/**
	 * Draw sprite with color, origin, rotation and depth
	 */
	public void DrawSprite(Texture2D texture, Vec2 position, Color color, Vec2 origin, float scale, float rotation, SpriteEffect effect)
	{
		// "draw" the sprite
		DrawSprite(texture, position.x, position.y, texture.width * scale, texture.height * scale,
				0, 0, texture.width, texture.height,
				color.r, color.g, color.b, color.a,
				origin.x * scale, origin.y * scale, 0 , rotation,
				effect);
	}
	
	/**
	 * Draw sprite with color, source rect, origin, rotation and depth
	 */
	public void DrawSprite(Texture2D texture, Vec2 position, Rectangle source, Color color, Vec2 origin, float scale, float rotation, SpriteEffect effect)
	{	
		// "draw" the sprite
		DrawSprite(texture, position.x, position.y, texture.width * scale, texture.height * scale,
				source.x, source.y, source.width, source.height,
				color.r, color.g, color.b, color.a,
				origin.x * scale, origin.y * scale, 0 , rotation,
				effect);
	}
	
	/**
	 * Draws text
	 */
	public void DrawText(Font font, String text, Vec2 position)
	{
		font.DrawText(this, text, position, Color.WHITE, 1.0f, 0.0f, 0.0f, SpriteEffect.NONE);
	}
	
	/**
	 * Draws text
	 */
	public void DrawText(Font font, String text, Vec2 position, Color color)
	{
		font.DrawText(this, text, position, color, 1.0f, 0.0f, 0.0f, SpriteEffect.NONE);
	}
	
	/**
	 * Draws text
	 */
	public void DrawText(Font font, String text, Vec2 position, Color color, float scale, float rot, float spacing)
	{
		font.DrawText(this, text, position, color, scale, rot, spacing, SpriteEffect.NONE);
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
	
	/**
	 * Holding sprite info!
	 * @author Wildrune
	 *
	 */
	public class SpriteInfo
	{
		// sprite info
		public Rectangle source;
		public Rectangle destination;
		public Texture2D texture;
		public Color color;
		public Vec4 originRotationDepth;
		public SpriteEffect spriteEffect;
		
		public SpriteInfo()
		{
			source = new Rectangle(0, 0, 1, 1);
			destination = new Rectangle(0, 0, 1, 1);
			texture = null;
			color = new Color(1, 1, 1, 1);
			originRotationDepth = new Vec4(0, 0, 0, 0);
			spriteEffect = SpriteEffect.NONE;
		}
	}
}
