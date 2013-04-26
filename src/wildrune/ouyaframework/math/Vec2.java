package wildrune.ouyaframework.math;

import wildrune.ouyaframework.utils.ObjectPool;

/***
 * Vector 2D for game math
 * @author Wildrune
 *
 */
public class Vec2 
{
	private float[] elements;
	public static ObjectPool<Vec2> mParentPool;
	public float x, y;
	
	public Vec2() {}
	
	/***
	 * Overloaded constructors
	 * @return this vector
	 */
	public Vec2(float x, float y) {
		this.x = x;
		this.y = y;
		elements = new float[2];
	}
	
	public Vec2(Vec2 other) {
		this(other.x, other.y);
	}
	
	public void Recycle()
	{
		if(mParentPool != null)
			mParentPool.Recycle(this);
	}
	
	public void Zero()
	{
		x = y = 0f;
	}
	
	/**
	 * Converts this vector to a array
	 */
	public float[] ToArray()
	{
		elements[0] = x;
		elements[1] = y;
		return elements;
	}
	
	/***
	 * Add by another vector
	 * @return this vector
	 */
	public Vec2 Add(float x, float y){
		this.x += x;
		this.y += y;
		return this;
	}
	
	public Vec2 Add(Vec2 rhs) {
		this.x += rhs.x;
		this.y += rhs.y;
		return this;
	}
	
	/***
	 * Substract another vector
	 * @return this vector
	 */
	public Vec2 Substract(float x, float y){
		this.x -= x;
		this.y -= y;
		return this;
	}
	
	public Vec2 Substract(Vec2 rhs){
		this.x -= rhs.x;
		this.y -= rhs.y;
		return this;
	}
	
	/***
	 * Scale vector
	 * @return this vector
	 */
	public Vec2 Scale(float scalar) {
		this.x *= scalar;
		this.y *= scalar;
		return this;
	}
	
	public Vec2 Scale(Vec2 rhs) {
		this.x *= rhs.x;
		this.y *= rhs.y;
		return this;
	}
	
	/***
	 * Negate this vector
	 * @return this vector
	 */
	public Vec2 Negate() {
		this.x = -this.x;
		this.y = -this.y;
		return this;
	}
	
	/***
	 * Get the length squared of this vector
	 * @return length squared
	 */
	public float LengthSq() {
		return (this.x * this.x) + (this.y * this.y);
	}
	
	/***
	 * Get the length of this vector
	 * @return length
	 */
	public float Length() {
		return (float) Math.sqrt((this.x * this.x) + (this.y * this.y));
	}
	
	/***
	 * Normalize his vector
	 * @return this vector
	 */
	public Vec2 Normalize() {
		float length = this.Length();
		if( RuneMath.IsCloseEnough(length, 0.0f))
		{
			this.x /= length;
			this.y /= length;
		}
		return this;
	}
	
	/***
	 * Computes the dot product between this vector and the rhs vector
	 * @return dot product
	 */
	public float Dot(Vec2 rhs) {
		return (this.x * rhs.x) + (this.y * rhs.y);
	}
	
	/***
	 * Rotates this vector
	 * @return this vector
	 */
	public Vec2 Rotate(float angleDeg) {
		// get the cos & sin
		float cos = (float) Math.cos(angleDeg * RuneMath.TORAD);
		float sin = (float) Math.sin(angleDeg * RuneMath.TORAD);
		
		// rotate this point
		this.x = this.x * cos - this.y * sin;
		this.y = this.y * sin + this.x * cos;
		
		return this;
	}
	
	/***
	 * Gets the angle between 2 vectors
	 * @return the angle between vectors
	 */
	public float GetAngle(Vec2 rhs) {
		float dot = this.Dot(rhs);
		float mulLength = this.Length() * rhs.Length();
		
		return (float) Math.acos(dot / mulLength);
	}
	
	/***
	 * Gets the angle of this vector
	 * @return the angle
	 */
	public float GetAngle() {
		float angle = (float) (Math.atan2(this.y, this.x) * RuneMath.TODEG);
		
		if(angle < 0)
			angle += 360.0f;
		
		return angle;
	}
	
	/***
	 * Get the distance between vectors
	 * @return distance between vectors
	 */
	public float Distance(float x, float y) {
		float distX = this.x - x;
		float distY = this.y - y;
		
		return (float) Math.sqrt(distX * distX + distY * distY);
	}
	
	public float Distance(Vec2 other) {
		float distX = this.x - other.x;
		float distY = this.y - other.y;
		
		return (float) Math.sqrt(distX * distX + distY * distY);
	}
}
