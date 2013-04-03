package wildrune.ouyaframework.util;

import java.io.InputStream;

import wildrune.ouyaframework.math.RuneMath;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Handles texture specific stuff
 * @author Wildrune
 *
 */
public class TextureUtil 
{
	/**
	 * Loads a bitmap from a stream
	 * @param stream the inputstream where to load from
	 * @param powerOfTwo specify if we want to convert non power of two to power of two
	 * @return returns the loaded bitmap
	 */
	public static Bitmap LoadBitmapFromAssets(InputStream stream, boolean powerOfTwo)
	{
		// create bitmap options
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		
		Bitmap sourceBitmap = BitmapFactory.decodeStream(stream, null, options);
		int bWidth = sourceBitmap.getWidth();
		int bHeight = sourceBitmap.getHeight();
		
		// check if we want to convert the texture to power of two
		if( powerOfTwo && 
				!RuneMath.IsPower2(bWidth) || !RuneMath.IsPower2(bHeight) )
		{		
			// check which power of two to choose
			int potW = RuneMath.ClosestPower2(bWidth);
			int potH = RuneMath.ClosestPower2(bHeight);
			
			// create new bitmap
			Bitmap scaledBitmap = Bitmap.createScaledBitmap(sourceBitmap, potW, potH, true);
			sourceBitmap.recycle();
			return scaledBitmap;
		}
		
		// create the bitmap and return it
		return sourceBitmap;
	}
}
