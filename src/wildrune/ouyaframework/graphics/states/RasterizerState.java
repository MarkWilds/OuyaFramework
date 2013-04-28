package wildrune.ouyaframework.graphics.states;

import static android.opengl.GLES20.*;

public class RasterizerState implements GraphicState
{
	// static common states
	public final static RasterizerState CullNone;
	public final static RasterizerState CullClockwise;
	public final static RasterizerState CullCounterClockwise;
	
	// initialize static common sampler states
	static
	{
		CullNone = new RasterizerState(GL_CW, GL_FRONT_AND_BACK);
		CullClockwise = new RasterizerState(GL_CW, GL_BACK);
		CullCounterClockwise = new RasterizerState(GL_CCW, GL_BACK);
	}
	
	// data members
	public int cullDirection;
	public int cullMode;
	
	/**
	 * Initialize this rasterizer state
	 * @param dir
	 * @param mode
	 */
	public RasterizerState(int dir, int mode)
	{
		cullDirection = dir;
		cullMode = mode;
	}
	
	@Override
	public void SetState()
	{
		glFrontFace(cullDirection);
		glCullFace(cullMode);
	}
}
