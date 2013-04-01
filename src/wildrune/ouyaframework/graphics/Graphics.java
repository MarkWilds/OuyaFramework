package wildrune.ouyaframework.graphics;

import android.graphics.Rect;

/***
 * Class managing the graphics
 * @author Wildrune
 *
 */
public class Graphics 
{
	public Rect viewportNormal;
	public Rect viewportSafeArea;
	
	/***
	 * Default constructor
	 */
	public Graphics(int width, int height)
	{
		// set normal viewport
		viewportNormal = new Rect(0, 0, width, height);
		
		// calculate safe area
		int leftSafe = (int)(width * 0.05f);
		int topSafe = (int)(height * 0.05f);
		int rightSafe = (int)(width - width * 0.05f);
		int bottomSafe = (int)(height - height * 0.05f);
		
		// set safe area viewport
		viewportSafeArea = new Rect(leftSafe, topSafe, rightSafe, bottomSafe);
	}
}
