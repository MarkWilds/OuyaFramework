package wildrune.ouyaframework.math;

import wildrune.ouyaframework.util.ObjectPool;

/***
 * Vector 3D for game math
 * @author Wildrune
 *
 */
public class Vec4 
{
	private float[] elements;
	public static ObjectPool<Vec4> mParentPool;
	public float x, y, z, w;
	
	/**
	 * Constructor
	 */
	public Vec4()
	{
		elements = new float[4];
		this.x = 0.0f;
		this.y = 0.0f;
		this.z = 0.0f;
		this.w = 0.0f;
	}
	
	public void Recycle()
	{
		if(mParentPool != null)
			mParentPool.Recycle(this);
	}
	
	public void Zero()
	{
		x = y = z = w = 0f;
	}
	
	/**
	 * Converts this vector to a array
	 */
	public float[] ToArray()
	{
		elements[0] = x;
		elements[1] = y;
		elements[2] = z;
		elements[3] = w;
		return elements;
	}
	
	/***
	 * Overloaded constructors
	 * @return this vector
	 */
	public Vec4(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Vec4(Vec4 other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
		this.w = other.w;
	}
	
	/***
	 * Add by another vector
	 * @return this vector
	 */
	public Vec4 Add(float x, float y, float z, float w){
		this.x += x;
		this.y += y;
		this.z += z;
		this.w += w;
		return this;
	}
	
	public Vec4 Add(Vec4 rhs) {
		this.x += rhs.x;
		this.y += rhs.y;
		this.z += rhs.z;
		this.w += rhs.w;
		return this;
	}
	
	/***
	 * Substract another vector
	 * @return this vector
	 */
	public Vec4 Substract(float x, float y, float z, float w){
		this.x -= x;
		this.y -= y;
		this.z -= z;
		this.w -= w;
		return this;
	}
	
	public Vec4 Substract(Vec4 rhs){
		this.x -= rhs.x;
		this.y -= rhs.y;
		this.z -= rhs.z;
		this.w -= rhs.w;
		return this;
	}
	
	/***
	 * Scale vector
	 * @return this vector
	 */
	public Vec4 Scale(float scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		this.w *= scalar;
		return this;
	}
	
	public Vec4 Scale(Vec4 rhs) {
		this.x *= rhs.x;
		this.y *= rhs.y;
		this.z *= rhs.z;
		this.w *= rhs.w;
		return this;
	}
	
	/***
	 * Negate this vector
	 * @return this vector
	 */
	public Vec4 Negate() {
		this.x = -this.x;
		this.y = -this.y;
		this.z = -this.z;
		this.w = -this.w;
		return this;
	}
	
	/***
	 * Get the length squared of this vector
	 * @return length squared
	 */
	public float LengthSq() {
		return (this.x * this.x) + (this.y * this.y) + (this.z * this.z) + (this.w * this.w);
	}
	
	/***
	 * Get the length of this vector
	 * @return length
	 */
	public float Length() {
		return (float) Math.sqrt((this.x * this.x) + (this.y * this.y) + (this.z * this.z) + (this.w * this.w));
	}
	
	/***
	 * Normalize his vector
	 * @return this vector
	 */
	public Vec4 Normalize() {
		float length = this.Length();
		if( RuneMath.IsCloseEnough(length, 0.0f))
		{
			this.x /= length;
			this.y /= length;
			this.z /= length;
			this.w /= length;
		}
		return this;
	}
	
	public static float Length(float x, float y, float z, float w)
	{
		return (float)Math.sqrt(x * x + y * y + z * z + w * w);
	}
	
	/***
	 * Computes the dot product between this vector and the rhs vector
	 * @return dot product
	 */
	public float Dot(Vec4 rhs) {
		return (this.x * rhs.x) + (this.y * rhs.y) + (this.z * rhs.z) + (this.w * rhs.w);
	}
	
	/***
	 * Get the distance between vectors
	 * @return distance between vectors
	 */
	public float Distance(float x, float y, float z, float w) {
		float distX = this.x - x;
		float distY = this.y - y;
		float distZ = this.z - z;
		float distW = this.w - w;
		
		return (float) Math.sqrt(distX * distX + distY * distY + distZ * distZ + distW * distW);
	}
	
	public float Distance(Vec4 other) {
		return Distance(other.x, other.y, other.z, other.w);
	}
}
