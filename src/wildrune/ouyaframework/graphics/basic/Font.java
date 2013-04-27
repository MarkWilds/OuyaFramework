package wildrune.ouyaframework.graphics.basic;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.Log;
import wildrune.ouyaframework.graphics.SpriteBatch;
import wildrune.ouyaframework.math.RuneMath;
import wildrune.ouyaframework.math.Vec2;

public class Font 
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
	
	private static final int MIN_FONT_SIZE = 14;
	private static final int MAX_FONT_SIZE = 180;
	
	// intern vars
	private Glyph[] glyphs;
	private Rectangle textureRegion;
	private Texture2D texture;
	private float fontHeight, fontAscent, fontDescent;
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
	 * Creates a font that can be used in openGL from a typeface
	 * @param typeface
	 * @return
	 */
	public boolean Create(Typeface typeface, boolean aa, int size, int fontPadX, int fontPadY, boolean stroke, int strokeSize)
	{
		// vars
		float maxCharWidth = 0;
		float cellWidth, cellHeight;
		int textureWidth;
		fontPaddingX = fontPadX;
		fontPaddingY = fontPadY;
		
		// error checks
		if(typeface == null)
			return false;

		// we create a paint object with our font info
		Paint paint = new Paint();
		paint.setAntiAlias(aa);
		paint.setTextSize(size);
		paint.setColor( 0xff000000 );
		paint.setTypeface(typeface);
		
		// get the font metrics
		Paint.FontMetrics fm = paint.getFontMetrics();
		fontHeight = (float)Math.ceil(  Math.abs(fm.bottom) + Math.abs(fm.top) );
		fontAscent = (float)Math.ceil( Math.abs( fm.ascent ) );
		fontDescent = (float)Math.ceil( Math.abs(fm.descent) );
		
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
		cellWidth = maxCharWidth + (2 * fontPadX);
		cellHeight = fontHeight + (2 * fontPadY);
		int maxSize = (int) (cellWidth > cellHeight? cellWidth : cellHeight);
		
		if(maxSize < MIN_FONT_SIZE || maxSize > MAX_FONT_SIZE)
			return false;
		
		// specify the texture size
		if( maxSize <= 24)
			textureWidth = 256;
		else if (maxSize <= 40)
			textureWidth = 512;
		else if( maxSize <= 80)
			textureWidth = 1024;
		else
			textureWidth = 2048;
		
		// get row count
		int colCount = (int) (textureWidth / cellWidth);
		int rowCount = (int) Math.ceil( CHAR_COUNT / colCount );
		int textureHeight = RuneMath.NextPower2( rowCount * (int)cellHeight);
		
		// create bitmap
		Bitmap bitmap;
		bitmap = Bitmap.createBitmap(textureWidth, textureHeight, Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(bitmap);
		bitmap.eraseColor(0x00000000);
		
		// draw stroked
		if(stroke)
		{
			paint.setStrokeWidth((float)strokeSize);
			paint.setStyle(Style.FILL_AND_STROKE);

			// draw all characters onto the bitmap
			float offsX = fontPadX;
			float offsY = ( cellHeight - 1) - fontDescent - fontPadY;
			
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
		float offsY = ( cellHeight - 1) - fontDescent - fontPadY;
		
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
		texture.Create(bitmap, false);
		bitmap.recycle();
		
		// create glyph regions
		offsX = offsY = 0;
		for(char c = MIN_CHAR; c <= MAX_CHAR; c++)
		{
			// set the texture region
			Rectangle region = GetGlyphRegion(c);
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
	
	/**
	 * Gets the region for the character
	 * @param c The character to search the region for
	 * @return The rectangle region on the texture for this char
	 */
	private Rectangle GetGlyphRegion(char c)
	{
		int index = c - MIN_CHAR;
		
		if(c >= MIN_CHAR && c <= MAX_CHAR)
		{
			return glyphs[index].region;
		}
		
		return null;
	}
	
	public int MeasureText(String text)
	{
		return 0;
	}
	
	/***
	 * Draws text at the given position
	 * @param batch
	 * @param text
	 * @param position
	 */
	public void DrawText(SpriteBatch batch, String text, Vec2 position, Color color, float scale, float rot, float spacing)
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
					0, 0, 0, rot);
			
			// set new position
			position.x += glyph.charWidth * scale + spacing; 
		}
	}
}
