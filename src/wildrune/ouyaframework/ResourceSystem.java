package wildrune.ouyaframework;

import java.io.InputStream;

import wildrune.ouyaframework.graphics.basic.Font;
import wildrune.ouyaframework.graphics.basic.Texture2D;
import wildrune.ouyaframework.graphics.states.SamplerState;
import wildrune.ouyaframework.math.RuneMath;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Log;

public class ResourceSystem 
{
	/**
	 * Statics
	 */
	private final static String LOG_TAG = "Assets";
	
	/**
	 * Data members
	 */
	private FileSystem fileIO;
	
	/**
	 * default constructor
	 */
	public ResourceSystem(FileSystem io)
	{
		this.fileIO = io;
	}
	
	/**
	 * Load a font from a .ttf
	 */
	public Font LoadFont(String filePath, boolean aa, int size, int padX, int padY, boolean stroke, int strokeSize)
	{
		// get the typeface for this font
		Typeface typeface = LoadTypefaceFromAssets( fileIO.GetAssets(), filePath);
		Font font = new Font();
		font.Create(typeface, aa, size, padX, padY, stroke, strokeSize);
		
		return font;
	}
	
	/**
	 * Load a texture
	 */
	public Texture2D LoadTexture(String filePath)
	{
		Texture2D tempTex = new Texture2D();
		try {
			
			final InputStream stream = fileIO.ReadFromAssets(filePath);
			final Bitmap tempBMP = LoadBitmapFromStream(stream, false);
			
			if( !tempTex.Create( tempBMP, false, SamplerState.LinearClamp ) )
			{
				// could not load texture
				Log.e(LOG_TAG, "COULD NOT LOAD TEXTURE!");
				tempBMP.recycle();
				return null;
			}
			
			tempTex.Bind(0);
			tempBMP.recycle();
			
			return tempTex;
		}
		catch(Exception e) {
			Log.e(LOG_TAG, "Texture loading Exception: " + e.getMessage());
		}
		
		return null;
	}
	
	/**
	 * Loads a bitmap from a stream
	 * @param stream the inputstream where to load from
	 * @param powerOfTwo specify if we want to convert non power of two to power of two
	 * @return returns the loaded bitmap
	 */
	private Bitmap LoadBitmapFromStream(InputStream stream, boolean powerOfTwo)
	{
		// create bitmap options
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		
		Bitmap sourceBitmap = BitmapFactory.decodeStream(stream, null, options);
		int bWidth = sourceBitmap.getWidth();
		int bHeight = sourceBitmap.getHeight();
		
		// check if we want to convert the texture to power of two
		if( powerOfTwo && ( !RuneMath.IsPower2(bWidth) || !RuneMath.IsPower2(bHeight) ) )
		{		
			// check which power of two to choose
			int potW = RuneMath.ClosestPowerOf2(bWidth);
			int potH = RuneMath.ClosestPowerOf2(bHeight);
			
			// create new bitmap
			Bitmap scaledBitmap = Bitmap.createScaledBitmap(sourceBitmap, potW, potH, false);
			sourceBitmap.recycle();
			return scaledBitmap;
		}
		
		// create the bitmap and return it
		return sourceBitmap;
	}
	
	/**
	 * Loads a font from a typeface
	 * @param assets the assetmanager
	 * @param file the name of the file to use
	 * @return the typeface to return
	 */
	private Typeface LoadTypefaceFromAssets(AssetManager assets, String file)
	{
		return Typeface.createFromAsset(assets, file);
	}
}
