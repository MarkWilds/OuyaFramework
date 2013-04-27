package wildrune.ouyaframework.graphics.states;

import static android.opengl.GLES20.*;

public class SamplerState implements GraphicState
{
	// static common states
	public final static SamplerState PointWrap;
	public final static SamplerState PointClamp;
	public final static SamplerState LinearWrap;
	public final static SamplerState LinearClamp;
	
	// initialize static common sampler states
	static
	{
		PointWrap = new SamplerState(GL_NEAREST, GL_NEAREST, GL_REPEAT, GL_REPEAT);
		PointClamp = new SamplerState(GL_NEAREST, GL_NEAREST, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE);
		LinearWrap = new SamplerState(GL_LINEAR, GL_LINEAR, GL_REPEAT, GL_REPEAT);
		LinearClamp = new SamplerState(GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE);
	}
	
	// data members
	public int minFilter;
	public int maxFilter;
	public int wrapS;
	public int wrapT;
	
	/**
	 * Initialize this sampler state
	 * @param min represents GL_TEXTURE_MIN_FILTER
	 * @param max repesents GL_TEXTURE_MAG_FILTER
	 * @param s represents GL_TEXTURE_WRAP_S
	 * @param t represents GL_TEXTURE_WRAP_T
	 */
	public SamplerState(int min, int max, int s, int t)
	{
		minFilter = min;
		maxFilter = max;
		wrapS = s;
		wrapT = t;
	}
	
	/**
	 * Sets this sampler state to be the current state
	 */
	@Override
	public void SetState()
	{
		// set filtering
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, maxFilter);
		
		// set wrapping
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT);
	}
}
