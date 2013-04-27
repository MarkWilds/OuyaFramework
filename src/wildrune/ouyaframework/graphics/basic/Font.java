package wildrune.ouyaframework.graphics.basic;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import wildrune.ouyaframework.graphics.SpriteBatch;
import wildrune.ouyaframework.graphics.SpriteBatch.SpriteEffect;
import wildrune.ouyaframework.graphics.states.SamplerState;
import wildrune.ouyaframework.math.RuneMath;
import wildrune.ouyaframework.math.Vec2;
import wildrune.ouyaframework.utils.interfaces.IDisposable;

public class Font implements IDisposable
{
	public static final String LOG_TAG = "Font";
	
	// structures
	private class Glyph
	{
		public Rectangle region;
		public float charWidth;
		
		public Glyph()
		{
			charWidth = 0.0f;
			region = new Rectangle();
		}
	}
	
	// Static vars
	private static final int MIN_CHAR = 32;
	private static final int MAX_CHAR = 126;
	private static final int UNKNOWN_CHAR = 32;
	private static final int CHAR_COUNT = MAX_CHAR - MIN_CHAR + 1; // +1 for the unknown char
	
	private static final int CHARS_IN_ROW = 16;
	private static final int MIN_FONT_SIZE = 14;
	private static final int MAX_FONT_SIZE = 128;
	
	// intern vars
	private Glyph[] glyphs;
	private Rectangle textureRegion;
	private Texture2D texture;
	private int fontPaddingX, fontPaddingY;
	
	public Texture2D GetTexture()
	{
		return texture;
	}
	
	/**
	 * Initialize glyphs
	 */
	public Font()
	{		
		texture = new Texture2D();
		
		// initialize glyphs
		glyphs = new Glyph[CHAR_COUNT];
		for(int g = 0; g < CHAR_COUNT; g++)
		{
			glyphs[g] = new Glyph();
		}
	}
	
	/**
	 * Disposes of this font's resources
	 */
	public void Dispose()
	{
		texture.Dispose();
	}
	
	/**
	 * Returns the ideal power of two size for a given size in pixels
	 * @param pixels the pixes size
	 * @return the power of two size
	 */
	private int GetIdealTextureSize(int pixels)
	{
		int prevPOT = RuneMath.PrevPower2( pixels );
		int nextPOT = RuneMath.NextPower2( pixels - prevPOT );
		return prevPOT + nextPOT;
	}
	
	/**
	 * Gives back the length in pixels
	 * @param text the text to measure
	 * @return length of the text in pixels
	 */
	public int MeasureText(String text)
	{
		int textLength = text.length();
		int measuredLength = 0;
		Glyph glyph;
		for(int i = 0; i < textLength; i++)
		{
			// get region
			char c = text.charAt(i);
			
			// get the unknown char glyph if this is not a valid character
			if(c < MIN_CHAR || c > MAX_CHAR)
				glyph = glyphs[CHAR_COUNT - 1];
			else
				glyph = glyphs[c - MIN_CHAR];
			
			measuredLength += glyph.charWidth;
		}
		
		return measuredLength;
	}
	
	/**
	 * Creates a font that can be used in openGL from a typeface
	 * @param typeface
	 * @return
	 */
	public boolean Create(Typeface typeface, boolean aa, int size, int fontPadX, int fontPadY, boolean stroke, int strokeSize)
	{
		// vars
		float maxCharWidth = 0;
		float cellWidth, cellHeight;
		int textureWidth, textureHeight;
		fontPaddingX = fontPadX;
		fontPaddingY = fontPadY;
		
		// error checks
		if(typeface == null)
			return false;
		
		if(!stroke)
			strokeSize = 0;

		// we create a paint object with our font info
		Paint paint = new Paint();
		paint.setAntiAlias(aa);
		paint.setTextSize(size);
		paint.setColor( 0xff000000 );
		paint.setTypeface(typeface);
		
		// get the font metrics
		Paint.FontMetrics fm = paint.getFontMetrics();
		float fontHeight = (float)Math.ceil(  Math.abs(fm.bottom) + Math.abs(fm.top) );
		
		// get each chars width
		char[] character = new char[2];
		float[] width = new float[2];
		for(char c = MIN_CHAR; c <= MAX_CHAR; c++)
		{
			// get the character width
			character[0] = c;
			paint.getTextWidths(character, 0, 1, width);
			
			// put the char in the glyph
			int index = c - MIN_CHAR;
			glyphs[index].charWidth = width[0];
			
			// check for the max character width
			if(width[0] > maxCharWidth)
				maxCharWidth = width[0];
		}
		
		// get the unknown char width
		character[0] = (char) UNKNOWN_CHAR;
		paint.getTextWidths(character, 0, 1, width);
		glyphs[ CHAR_COUNT - 1].charWidth = width[0];
		
		if(width[0] > maxCharWidth)
			maxCharWidth = width[0];
		
		// decide the cell width and height
		cellWidth = maxCharWidth + (2 * fontPadX) + strokeSize;
		cellHeight = fontHeight + (2 * fontPadY) + strokeSize;
		int maxSize = (int) (cellWidth > cellHeight? cellWidth : cellHeight);
		
		if(maxSize < MIN_FONT_SIZE || maxSize > MAX_FONT_SIZE)
			return false;
		
		// calculate the perfect texture dimensions
		textureWidth = GetIdealTextureSize( (int) (CHARS_IN_ROW * cellWidth) );
		int rowCount = (int) (CHAR_COUNT / (float)CHARS_IN_ROW + 0.5f);
		textureHeight = GetIdealTextureSize( rowCount * (int)cellHeight );
		
		// create bitmap
		Bitmap bitmap;
		bitmap = Bitmap.createBitmap(textureWidth, textureHeight, Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(bitmap);
		bitmap.eraseColor(0x00000000);
		
		// draw stroked
		if(stroke)
		{
			paint.setStrokeWidth((float)strokeSize);
			paint.setStyle(Style.STROKE);

			// draw all characters onto the bitmap
			float offsX = fontPadX;
			float offsY = ( cellHeight - 1) - Math.abs(fm.bottom) - fontPadY;
			
			for(char c = MIN_CHAR; c <= MAX_CHAR; c++)
			{
				character[0] = c;
				canvas.drawText(character, 0, 1, offsX, offsY, paint);
				
				// new offsets
				offsX += cellWidth;
				if( (offsX + cellWidth - fontPadX) > textureWidth)
				{
					offsX = fontPadX;
					offsY += cellHeight;
				}
			}
			
			// draw unknown as last
			character[0] = UNKNOWN_CHAR;
			canvas.drawText(character, 0, 1, offsX, offsY, paint);
		}
		
		paint.setColor( 0xffffffff );
		paint.setStrokeWidth(0);
		paint.setStyle(Style.FILL);
		
		// draw all characters onto the bitmap
		float offsX = fontPadX;
		float offsY = ( cellHeight - 1) - Math.abs(fm.bottom) - fontPadY;
		
		for(char c = MIN_CHAR; c <= MAX_CHAR; c++)
		{
			character[0] = c;
			canvas.drawText(character, 0, 1, offsX, offsY, paint);
			
			// new offsets
			offsX += cellWidth;
			if( (offsX + cellWidth - fontPadX) > textureWidth)
			{
				offsX = fontPadX;
				offsY += cellHeight;
			}
		}
		
		// draw unknown as last
		character[0] = UNKNOWN_CHAR;
		canvas.drawText(character, 0, 1, offsX, offsY, paint);
		
		// create the texture
		texture.Create(bitmap, false, SamplerState.PointClamp);
		bitmap.recycle();
		
		// create glyph regions
		offsX = offsY = 0;
		for(char c = MIN_CHAR; c <= MAX_CHAR; c++)
		{
			// set the texture region
			Rectangle region = glyphs[c - MIN_CHAR].region;
			region.x = offsX;
			region.y = offsY;
			region.width = cellWidth;
			region.height = cellHeight;
			
			// create new offsets
			offsX += cellWidth;
			if(offsX + cellWidth > textureWidth)
			{
				offsX = 0;
				offsY += cellHeight;
			}
			
			// set unknown char region if this is the last character
			if( c == MAX_CHAR)
			{
				region = glyphs[CHAR_COUNT - 1].region;
				region.x = offsX;
				region.y = offsY;
				region.width = cellWidth;
				region.height = cellHeight;
			}
		}
		return true;
	}
	
	/***
	 * Draws text at the given position
	 * @param batch
	 * @param text
	 * @param position
	 */
	public void DrawText(SpriteBatch batch, String text, Vec2 position, Color color, float scale, float rot, float spacing, SpriteEffect effect)
	{		
		// take each character and print it out
		int textLength = text.length();
		Glyph glyph;
		for(int i = 0; i < textLength; i++)
		{
			// get region
			char c = text.charAt(i);
			
			// get the unknown char glyph if this is not a valid character
			if(c < MIN_CHAR || c > MAX_CHAR)
				glyph = glyphs[CHAR_COUNT - 1];
			else
				glyph = glyphs[c - MIN_CHAR];
			
			// create char position
			batch.DrawSprite(texture, position.x, position.y, glyph.region.width * scale, glyph.region.height * scale, 
					glyph.region.x, glyph.region.y, glyph.region.width, glyph.region.height, 
					color.r, color.g, color.b, color.a,
					0, 0, 0, rot,
					effect);
			
			// set new position
			position.x += (fontPaddingX + glyph.charWidth) * scale + spacing; 
		}
	}
}
