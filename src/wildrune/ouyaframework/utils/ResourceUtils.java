package wildrune.ouyaframework.utils;

import java.io.InputStream;

import wildrune.ouyaframework.math.RuneMath;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;

/**
 * Handles texture specific stuff
 * @author Wildrune
 *
 */
public class ResourceUtils 
{
	/**
	 * Loads a bitmap from a stream
	 * @param stream the inputstream where to load from
	 * @param powerOfTwo specify if we want to convert non power of two to power of two
	 * @return returns the loaded bitmap
	 */
	public static Bitmap LoadBitmapFromStream(InputStream stream, boolean powerOfTwo)
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
	
	/***
	 * Loads a font from a typeface
	 * @param typeFace
	 * @param file
	 * @return
	 */
	public static Typeface LoadTypefaceFromAssets(AssetManager assets, String file)
	{
		return Typeface.createFromAsset(assets, file);
	}
}