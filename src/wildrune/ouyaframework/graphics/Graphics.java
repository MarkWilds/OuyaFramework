package wildrune.ouyaframework.graphics;

/***
 * Class managing the graphics
 * @author Wildrune
 *
 */
public class Graphics 
{
	private int mBackBufferWidth;
	private int mBackBufferHeight;
	
	/***
	 * Getters & setters
	 */
	public int GetWidth() {
		return mBackBufferWidth;
	}
	
	public int GetHeight() {
		return mBackBufferHeight;
	}
	
	public void SetWidth(int w) {
		mBackBufferWidth = w;
	}
	
	public void SetHeight(int h) {
		mBackBufferHeight = h;
	}
	
	/***
	 * Default constructor
	 */
	public Graphics(int width, int height)
	{
		// initialize
		mBackBufferWidth = width;
		mBackBufferHeight = height;
	}
}
