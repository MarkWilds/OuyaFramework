package wildrune.ouyaframework.graphics.states;

import static android.opengl.GLES20.*;

public class BlendState implements GraphicState
{
	// static common states
	public final static BlendState Opaque;
	public final static BlendState AlphaBlend;
	public final static BlendState Additive;
	public final static BlendState NonPremultiplied;
	
	// initialize static common sampler states
	static
	{
		Opaque = new BlendState(GL_ONE, GL_ZERO);
		AlphaBlend = new BlendState(GL_ONE, GL_SRC_ALPHA);
		Additive = new BlendState(GL_SRC_ALPHA, GL_ONE);
		NonPremultiplied = new BlendState(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}
	
	// data members
	public int sourceBlend;
	public int destinationBlend;
	
	public BlendState(int source, int dest)
	{
		sourceBlend = source;
		destinationBlend = dest;
	}
	
	@Override
	public void SetState() 
	{
		glBlendFunc(sourceBlend, destinationBlend);
	}

}
