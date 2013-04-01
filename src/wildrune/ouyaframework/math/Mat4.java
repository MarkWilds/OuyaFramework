package wildrune.ouyaframework.math;

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
	
	/**
	 * Temporarly storage for multiplying matrices
	 */
	private static final float[] temp = new float[16];
	private static final float[] tempV = new float[3];
	
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
	
	/**
	 * Multiply this matrix by a vector
	 */
	public static void Multiply(Vec3 dest, Mat4 lhs, Vec3 rhs)
	{
		// m11 * x + m21 * y + m31 * z + m41
		// m12 * x + m22 * y + m32 * z + m42
		// m13 * x + m23 * y + m33 * z + m43
		tempV[0] = lhs.elements[0] * rhs.x + lhs.elements[4] * rhs.y + lhs.elements[8] * rhs.z + lhs.elements[12];
		tempV[1] = lhs.elements[1] * rhs.x + lhs.elements[5] * rhs.y + lhs.elements[9] * rhs.z + lhs.elements[13];
		tempV[2] = lhs.elements[2] * rhs.x + lhs.elements[6] * rhs.y + lhs.elements[10] * rhs.z + lhs.elements[14];
		
		// copy over the array
		dest.x = tempV[0];
		dest.y = tempV[1];
		dest.z = tempV[2];
	}

	/**
	 * Multiply this matrix by another matrix (dest = lhs * rhs)
	 * @param dest the matrix to save the results to
	 * @param lhs the matrix to multiply rhs matrix with
 	 * @param rhs the matrix to multiply lhs matrix with
	 */
	public static void Multiply(Mat4 dest, Mat4 lhs, Mat4 rhs)
	{
		Matrix.multiplyMM(temp, 0, lhs.elements, 0, rhs.elements, 0);
		
		// copy over the array
		System.arraycopy(temp, 0, dest.elements, 0, 16);
	}
	
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
	public static void CreateTranslation(Mat4 mat, float x, float y, float z)
	{
		// clear the matrix
		Matrix.setIdentityM(mat.elements,0);
		
		// set translation part
		mat.elements[12] = x;
		mat.elements[13] = y;
		mat.elements[14] = z;
	}

	public static void CreateTranslation(Mat4 mat, Vec3 translation)
	{
		CreateTranslation(mat, translation.x, translation.y, translation.z);
	}
	
	/**
	 * Create a scale matrix
	 */
	public static void CreateScale(Mat4 mat, float x, float y, float z)
	{
		// clear the matrix
		Matrix.setIdentityM(mat.elements,0);
		
		// set scale part
		mat.elements[0] = x;
		mat.elements[5] = y;
		mat.elements[10] = z;
	}
	
	public static void CreateScale(Mat4 mat, Vec3 scale)
	{
		CreateScale(mat, scale.x, scale.y, scale.z);
	}
	
	public static void CreateScaleUniform(Mat4 mat, float s)
	{
		CreateScale(mat, s, s, s);
	}
	
	/**
	 * Create a rotation matrix around the axis
	 */
	public static void CreateRotationAxis(Mat4 mat, float angle, float x, float y, float z)
	{
		// clear the matrix
		Matrix.setIdentityM(mat.elements, 0);
		
		// create rotation matrix
		float cos = (float) Math.cos( angle * RuneMath.TORAD );
		float sin = (float) Math.sin( angle * RuneMath.TORAD );

		mat.elements[0] = x * x * (1.0f - cos) + cos;
		mat.elements[1] = x * y * (1.0f - cos) + z * sin;
		mat.elements[2] = x * z * (1.0f - cos) - y * sin;

		mat.elements[4] = y * x * (1.0f - cos) - z * sin;
		mat.elements[5] = y * y * (1.0f - cos) + cos;
		mat.elements[6] = y * z * (1.0f - cos) + x * sin;

		mat.elements[8] = z * x * (1.0f - cos) + y * sin;
		mat.elements[9] = z * y * (1.0f - cos) - x * sin;
		mat.elements[10] = z * z * (1.0f - cos) + cos;
	}
	
	public static void CreateRotationAxis(Mat4 mat, float angle, Vec3 axis)
	{
		CreateRotationAxis(mat, angle, axis.x, axis.y, axis.z);
	}
	
	/**
	 * Creates a rotation matrix from euler angles
	 * @param yaw rotating around y axis
	 * @param pitch rotating around x axis
	 * @param roll rotating around z axis
	 */
	public static void CreateRotationEuler(Mat4 mat, float pitch, float yaw, float roll)
	{
		// clear the matrix
		Matrix.setIdentityM(mat.elements, 0);
		
		// perform yaw
		if(Math.abs(yaw) > 0f)
		{
			Mat4 yawM = RuneMath.GetMat4();
			Mat4.CreateRotationAxis(yawM, yaw, 0, 1f, 0);
			Mat4.Multiply(mat, mat, yawM);
			RuneMath.Recycle(yawM);
		}
		
		// perform pitch
		if(Math.abs(pitch) > 0f)
		{
			Mat4 pitchM = RuneMath.GetMat4();
			Mat4.CreateRotationAxis(pitchM, pitch, 1f, 0, 0);
			Mat4.Multiply(mat, mat, pitchM);
			RuneMath.Recycle(pitchM);
		}
		
		// perform roll
		if(Math.abs(roll) > 0f)
		{
			Mat4 rollM = RuneMath.GetMat4();
			Mat4.CreateRotationAxis(rollM, roll, 0, 0, 1f);
			Mat4.Multiply(mat, mat, rollM);
			RuneMath.Recycle(rollM);
		}
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
	public void Transpose(Mat4 dest)
	{
		Matrix.transposeM(temp, 0, elements, 0);
		
		// copy over the array
		System.arraycopy(temp, 0, dest.elements, 0, 16);
	}
	
	/**
	 * Inverts this matrix and put its results in invert
	 */
	public void Invert(Mat4 dest)
	{
		Matrix.invertM(temp, 0, elements, 0);
		
		// copy over the array
		System.arraycopy(temp, 0, dest.elements, 0, 16);
	}
	
	/**
	 * Creates a perspective matrix out of this matrix
	 * @param fov the field of view (45-90)
	 * @param aspect screen aspect ration (width/height)
	 * @param n near plane of the frustum
	 * @param f far plane of the frustum
	 */
	public static void CreatePerspectiveR(Mat4 mat, float fov, float aspect, float n, float f)
	{
		Matrix.perspectiveM(mat.elements, 0, fov, aspect, n, f);
	}
	
	/**
	 * Creates a perspective matrix out of this matrix
	 * @param fov the field of view (45-90)
	 * @param aspect screen aspect ration (width/height)
	 * @param n near plane of the frustum
	 * @param f far plane of the frustum
	 */
	public static void CreatePerspectiveL(Mat4 mat, float fov, float aspect, float n, float f)
	{	
		// create left handed perspective matrix
		float tan = 1.0f / (float)Math.tan( fov / 2.0f  * RuneMath.TORAD );
		
		mat.elements[0] = tan; // m11
		mat.elements[5] = tan * aspect; // m22
		mat.elements[10] = (f + n) 	/ (f - n); // m33
		mat.elements[14] = (-2 * n * f) / (f - n); // m43
		mat.elements[11] = 1.0f; // m34
	}
	
	/**
	 * Standard 2D orthographic projection
	 * @param width x dimension of the screen
	 * @param height y dimension of the screen
	 */
	public static void CreateOrtho2D(Mat4 mat, int width, int height)
	{
		Matrix.orthoM(mat.elements, 0, 0, width, 0, height, 0, 1.0f);
	}
}
