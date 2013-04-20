package wildrune.ouyaframework.graphics;

import wildrune.ouyaframework.graphics.basic.Color;
import wildrune.ouyaframework.graphics.basic.Texture2D;
import wildrune.ouyaframework.math.RuneMath;
import wildrune.ouyaframework.math.Vec2;
import wildrune.ouyaframework.math.Vec4;
import android.graphics.Rect;

/**
 * Holding current frame sprite info!
 * @author Wildrune
 *
 */
public class SpriteInfo
{
	public static final float[] attributes = new float[9];
	
	public Rect source;
	public Rect destination;
	public Texture2D texture;
	public Color color;
	public Vec4 originRotationDepth;
	
	public SpriteInfo()
	{
		source = new Rect(0, 0, 1, 1);
		destination = new Rect(0, 0, 1, 1);
		texture = null;
		color = new Color(1, 1, 1, 1);
		originRotationDepth = new Vec4(0, 0, 0, 0);
	}
	
	/**
	 * Overloaded constructor
	 */
	public SpriteInfo(Texture2D tex, Rect source, Rect dest, Color color, Vec4 ord)
	{
		this.texture = tex;
		this.source = source;
		this.destination = dest;
		this.color = color;
		this.originRotationDepth = ord;
	}
	
	/**
	 * Encodes the values into a vector4
	 */
	public static Vec4 ToOriginRotDepth(Vec2 origin, float rot, float depth)
	{
		return ToOriginRotDepth(origin.x, origin.y, rot, depth);
	}
	
	/**
	 * Encodes the values into a vector4
	 */
	public static Vec4 ToOriginRotDepth(float oX, float oY, float rot, float depth)
	{
		Vec4 encodedVec = RuneMath.GetVec4();
		
		// set values
		encodedVec.x = oX;
		encodedVec.y = oY;
		encodedVec.z = rot;
		encodedVec.w = depth;
		
		return encodedVec;
	}
}