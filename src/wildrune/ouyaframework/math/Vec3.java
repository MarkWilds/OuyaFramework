package wildrune.ouyaframework.math;

/***
 * Vector 3D for game math
 * @author Wildrune
 *
 */
public class Vec3 
{
	public float x, y, z;
	
	public Vec3()
	{
		this.x = 0.0f;
		this.y = 0.0f;
		this.z = 0.0f;
	}
	
	/***
	 * Overloaded constructors
	 * @return this vector
	 */
	public Vec3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3(Vec3 other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}
	
	/***
	 * Add by another vector
	 * @return this vector
	 */
	public Vec3 Add(float x, float y, float z){
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public Vec3 Add(Vec3 rhs) {
		this.x += rhs.x;
		this.y += rhs.y;
		this.z += rhs.z;
		return this;
	}
	
	/***
	 * Substract another vector
	 * @return this vector
	 */
	public Vec3 Substract(float x, float y, float z){
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}
	
	public Vec3 Substract(Vec3 rhs){
		this.x -= rhs.x;
		this.y -= rhs.y;
		this.z -= rhs.z;
		return this;
	}
	
	/***
	 * Scale vector
	 * @return this vector
	 */
	public Vec3 Scale(float scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		return this;
	}
	
	public Vec3 Scale(Vec3 rhs) {
		this.x *= rhs.x;
		this.y *= rhs.y;
		this.z *= rhs.z;
		return this;
	}
	
	/***
	 * Negate this vector
	 * @return this vector
	 */
	public Vec3 Negate() {
		this.x = -this.x;
		this.y = -this.y;
		this.z = -this.z;
		return this;
	}
	
	/***
	 * Get the length squared of this vector
	 * @return length squared
	 */
	public float LengthSq() {
		return (this.x * this.x) + (this.y * this.y) + (this.z * this.z);
	}
	
	/***
	 * Get the length of this vector
	 * @return length
	 */
	public float Length() {
		return (float) Math.sqrt((this.x * this.x) + (this.y * this.y) + (this.z * this.z));
	}
	
	/***
	 * Normalize his vector
	 * @return this vector
	 */
	public Vec3 Normalize() {
		float length = this.Length();
		if( RuneMath.IsCloseEnough(length, 0.0f))
		{
			this.x /= length;
			this.y /= length;
			this.z /= length;
		}
		return this;
	}
	
	/***
	 * Computes the dot product between this vector and the rhs vector
	 * @return dot product
	 */
	public float Dot(Vec3 rhs) {
		return (this.x * rhs.x) + (this.y * rhs.y) + (this.z * rhs.z);
	}
	
	/***
	 * Computes the cross product between this vector and the rhs vector
	 * @return the cross product vector
	 */
	public Vec3 Cross(Vec3 rhs) {
		Vec3 cross = new Vec3();
		cross.x = (y * rhs.z) - (z * rhs.y);
		cross.y = (z * rhs.x) - (x * rhs.z);
		cross.z = (x * rhs.y) - (y * rhs.y);
		return cross;		
	}
	
	/***
	 * Get the distance between vectors
	 * @return distance between vectors
	 */
	public float Distance(float x, float y, float z) {
		float distX = this.x - x;
		float distY = this.y - y;
		float distZ = this.z - z;
		
		return (float) Math.sqrt(distX * distX + distY * distY + distZ * distZ);
	}
	
	public float Distance(Vec3 other) {
		float distX = this.x - other.x;
		float distY = this.y - other.y;
		float distZ = this.z - other.z;
		
		return (float) Math.sqrt(distX * distX + distY * distY + distZ * distZ);
	}
}
