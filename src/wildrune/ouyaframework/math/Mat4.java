package wildrune.ouyaframework.math;

import wildrune.ouyaframework.util.ObjectPool;
import android.opengl.Matrix;

/***
 * Matrix 4x4 for game math
 * Column major
 * NOT THREAD SAFE
 * @author Wildrune
 *
 */
public class Mat4 
{
	/**
	 * The elements of this 4x4 matrix
	 */
	public final float[] elements;
	public static ObjectPool<Mat4> mParentPool;
	
	/**
	 * Constructor
	 */
	public Mat4()
	{
		// create the elements array
		elements = new float[16];
		
		// set identity
		Matrix.setIdentityM(elements, 0);	
	}
	
	public void Identity()
	{
		// set identity
		Matrix.setIdentityM(elements, 0);	
	}
	
	public void Recycle()
	{
		if(mParentPool != null)
			mParentPool.Recycle(this);
	}
	
	/**
	 * Multiply this matrix by a vector
	 */
	public static Vec3 Multiply(Mat4 lhs, Vec3 rhs)
	{
		Vec3 dest = RuneMath.GetVec3();
		
		// m11 * x + m21 * y + m31 * z + m41
		// m12 * x + m22 * y + m32 * z + m42
		// m13 * x + m23 * y + m33 * z + m43
		dest.x = lhs.elements[0] * rhs.x + lhs.elements[4] * rhs.y + lhs.elements[8] * rhs.z + lhs.elements[12];
		dest.y = lhs.elements[1] * rhs.x + lhs.elements[5] * rhs.y + lhs.elements[9] * rhs.z + lhs.elements[13];
		dest.z = lhs.elements[2] * rhs.x + lhs.elements[6] * rhs.y + lhs.elements[10] * rhs.z + lhs.elements[14];
		
		return dest;
	}
	
	public Vec3 Multiply(Vec3 rhs)
	{
		return Multiply(this, rhs);
	}

	/**
	 * Multiply this matrix by another matrix (dest = lhs * rhs)
	 * @param dest the matrix to save the results to
	 * @param lhs the matrix to multiply rhs matrix with
 	 * @param rhs the matrix to multiply lhs matrix with
	 */
	public static void Multiply(Mat4 result, Mat4 lhs, Mat4 rhs)
	{
		Matrix.multiplyMM(result.elements, 0, lhs.elements, 0, rhs.elements, 0);
	}
	
	public void MultiplyAssign(Mat4 rhs)
	{
		Multiply(this, this, rhs);
		Mat4.Orthogonalize(this);
	}
	
	public Mat4 Multiply(Mat4 rhs)
	{
		Mat4 result = RuneMath.GetMat4();
		Multiply(result, this, rhs);
		return result;
	}

	/**
	 * Orthogonalizes the give matrix
	 */
	public static void Orthogonalize(Mat4 dest)
	{
		// renormalize
		float colOne = Vec3.Length(dest.elements[0], dest.elements[1], dest.elements[2]);
		float colTwo = Vec3.Length(dest.elements[4], dest.elements[5], dest.elements[6]);
		float colThree = Vec3.Length(dest.elements[8], dest.elements[9], dest.elements[10]);
		
		// normalize col one
		dest.elements[0] = dest.elements[0] / colOne;
		dest.elements[1] = dest.elements[1] / colOne;
		dest.elements[2] = dest.elements[2] / colOne;
		
		// normalize col two
		dest.elements[4] = dest.elements[4] / colTwo;
		dest.elements[5] = dest.elements[5] / colTwo;
		dest.elements[6] = dest.elements[6] / colTwo;
		
		// normalize col three
		dest.elements[8] = dest.elements[8] / colThree;
		dest.elements[9] = dest.elements[9] / colThree;
		dest.elements[10] = dest.elements[10] / colThree;
	}

	/***
	 * Creates a translation matrix from the given matrix
	 */
	public static Mat4 CreateTranslation(float x, float y, float z)
	{
		Mat4 result = RuneMath.GetMat4();
		
		// set translation part
		result.elements[12] = x;
		result.elements[13] = y;
		result.elements[14] = z;
		
		return result;
	}

	public static Mat4 CreateTranslation(Vec3 translation)
	{
		return CreateTranslation(translation.x, translation.y, translation.z);
	}
	
	/**
	 * Create a scale matrix
	 */
	public static Mat4 CreateScale(float x, float y, float z)
	{
		Mat4 result = RuneMath.GetMat4();
		
		// set scale part
		result.elements[0] = x;
		result.elements[5] = y;
		result.elements[10] = z;
		
		return result;
	}
	
	public static Mat4 CreateScale(Vec3 scale)
	{
		return CreateScale(scale.x, scale.y, scale.z);
	}
	
	public static Mat4 CreateScaleUniform(float s)
	{
		return CreateScale(s, s, s);
	}
	
	/**
	 * Create a rotation matrix around the axis
	 */
	public static Mat4 CreateRotationAxis(float angle, float x, float y, float z)
	{
		// clear the matrix
		Mat4 result = RuneMath.GetMat4();
		
		// create rotation matrix
		float cos = (float) Math.cos( angle * RuneMath.TORAD );
		float sin = (float) Math.sin( angle * RuneMath.TORAD );

		result.elements[0] = x * x * (1.0f - cos) + cos;
		result.elements[1] = x * y * (1.0f - cos) + z * sin;
		result.elements[2] = x * z * (1.0f - cos) - y * sin;

		result.elements[4] = y * x * (1.0f - cos) - z * sin;
		result.elements[5] = y * y * (1.0f - cos) + cos;
		result.elements[6] = y * z * (1.0f - cos) + x * sin;

		result.elements[8] = z * x * (1.0f - cos) + y * sin;
		result.elements[9] = z * y * (1.0f - cos) - x * sin;
		result.elements[10] = z * z * (1.0f - cos) + cos;
		
		return result;
	}
	
	public static Mat4 CreateRotationAxis(float angle, Vec3 axis)
	{
		return CreateRotationAxis(angle, axis.x, axis.y, axis.z);
	}
	
	/**
	 * Creates a rotation matrix from euler angles
	 * @param yaw rotating around y axis
	 * @param pitch rotating around x axis
	 * @param roll rotating around z axis
	 */
	public static Mat4 CreateRotationEuler(float pitch, float yaw, float roll)
	{
		// clear the matrix
		Mat4 result = RuneMath.GetMat4();
		
		// perform yaw
		if(Math.abs(yaw) > 0f)
		{
			Mat4 yawM = Mat4.CreateRotationAxis( yaw, 0, 1f, 0);
			result = result.Multiply(yawM);
			RuneMath.Recycle(yawM);
		}
		
		// perform pitch
		if(Math.abs(pitch) > 0f)
		{
			Mat4 pitchM = Mat4.CreateRotationAxis(pitch, 1f, 0, 0);
			result = result.Multiply(pitchM);
			RuneMath.Recycle(pitchM);
		}
		
		// perform roll
		if(Math.abs(roll) > 0f)
		{
			Mat4 rollM = Mat4.CreateRotationAxis(roll, 0, 0, 1f);
			result = result.Multiply(rollM);
			RuneMath.Recycle(rollM);
		}
		
		return result;
	}
	
	/**
	 * Gets the euler angles from this matrix
	 */
	public void GetEuler(Vec3 euler)
	{
		// checking for domain errors
		float m32 = elements[9];
		float sp = -m32;
		if( sp <= -1.0f )
		{
			euler.x  = -1.570796f * RuneMath.TODEG; // -pi/2
		}
		else if ( sp >= 1.0f)
		{
			euler.x  = 1.570796f * RuneMath.TODEG; // pi/2
		}
		else
		{
			euler.x = (float)Math.asin(sp);
		}

		// chek for gimbal lock case
		if( RuneMath.IsCloseEnough( Math.abs(sp), 1.0f ) )
		{
			float m13 = elements[2];
			float m11 = elements[0];
			
			// in this case we are looking straight up or down
			euler.y = (float)Math.atan2(-m13, m11);
			euler.z = 0.0f;
		}
		else // no gimbal lock
		{
			float m12 = elements[1];
			float m22 = elements[5];
			float m31 = elements[8];
			float m33 = elements[10];
			
			// compute yaw
			euler.y = (float)Math.atan2(m31, m33) * RuneMath.TODEG;

			// compute roll
			euler.z = (float)Math.atan2(m12, m22) * RuneMath.TODEG;
		}
	}
	
	/**
	 * Transposes this matrix and put the results in transpose
	 */
	public Mat4 Transpose()
	{
		Mat4 result = RuneMath.GetMat4();
		Matrix.transposeM(result.elements, 0, elements, 0);
		return result;
	}
	
	/**
	 * Inverts this matrix and put its results in invert
	 */
	public Mat4 Invert()
	{
		Mat4 result = RuneMath.GetMat4();
		Matrix.invertM(result.elements, 0, elements, 0);
		return result;
	}
	
	/**
	 * Creates a perspective matrix out of this matrix
	 * @param fov the field of view (45-90)
	 * @param aspect screen aspect ration (width/height)
	 * @param n near plane of the frustum
	 * @param f far plane of the frustum
	 */
	public static Mat4 CreatePerspectiveR(float fov, float aspect, float n, float f)
	{
		Mat4 result = RuneMath.GetMat4();
		Matrix.perspectiveM(result.elements, 0, fov, aspect, n, f);
		return result;
	}
	
	/**
	 * Creates a perspective matrix out of this matrix
	 * @param fov the field of view (45-90)
	 * @param aspect screen aspect ration (width/height)
	 * @param n near plane of the frustum
	 * @param f far plane of the frustum
	 */
	public static Mat4 CreatePerspectiveL(float fov, float aspect, float n, float f)
	{	
		// create left handed perspective matrix
		Mat4 result = RuneMath.GetMat4();
		float tan = 1.0f / (float)Math.tan( fov / 2.0f  * RuneMath.TORAD );
		
		result.elements[0] = tan; // m11
		result.elements[5] = tan * aspect; // m22
		result.elements[10] = (f + n) 	/ (f - n); // m33
		result.elements[14] = (-2 * n * f) / (f - n); // m43
		result.elements[11] = 1.0f; // m34
		return result;
	}
	
	/**
	 * Standard 2D orthographic projection
	 * @param width x dimension of the screen
	 * @param height y dimension of the screen
	 */
	public static Mat4 CreateOrtho2D(int width, int height)
	{
		Mat4 result = RuneMath.GetMat4();
		Matrix.orthoM(result.elements, 0, 0, width, 0, height, -1.0f, 1.0f);
		return result;
	}
}
