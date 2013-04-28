package wildrune.ouyaframework.graphics;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.glEnable;
import wildrune.ouyaframework.graphics.basic.Rectangle;
import wildrune.ouyaframework.graphics.states.BlendState;
import wildrune.ouyaframework.graphics.states.RasterizerState;
import wildrune.ouyaframework.graphics.states.SamplerState;

/***
 * Class managing the graphics
 * @author Wildrune
 *
 */
public class Graphics 
{
	/**
	 * Statics
	 */
	private final static float SAFE_AREA_RATIO = 0.05f;
	
	/**
	 * Viewports
	 */
	public Rectangle viewportNormal;
	public Rectangle viewportSafeArea;
	
	/**
	 * Current openGL states
	 */
	public SamplerState 	currentSamplerState;
	public BlendState 		currentBlendState;
	public RasterizerState 	currentRasterizerState;
	
	/***
	 * Default constructor
	 */
	public Graphics(int width, int height)
	{
		// set normal viewport
		viewportNormal = new Rectangle(0, 0, width, height);
		
		// calculate safe area
		int leftSafe = (int)(width * SAFE_AREA_RATIO);
		int topSafe = (int)(height * SAFE_AREA_RATIO);
		int rightSafe = (int)(width - width * SAFE_AREA_RATIO);
		int bottomSafe = (int)(height - height * SAFE_AREA_RATIO);
		
		// set safe area viewport
		viewportSafeArea = new Rectangle(leftSafe, topSafe, rightSafe, bottomSafe);
	}
	
	/**
	 * Sets and applies the state
	 * @param state
	 */
	public void SetBlendingState(BlendState state)
	{
		if(state != currentBlendState)
		{
			glEnable(GL_BLEND);
			currentBlendState = state;
			state.SetState();
		}
	}
	
	/**
	 * Sets and applies the state
	 * @param state
	 */
	public void SetRasterizerState(RasterizerState state)
	{
		if(state != currentRasterizerState)
		{
			currentRasterizerState = state;
			state.SetState();
		}
	}
}
