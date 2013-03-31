package wildrune.ouyaframework.math;

import android.opengl.Matrix;

/***
 * Matrix 4x4 for game math
 * Column major
 * @author Wildrune
 *
 */
public class Mat4 
{
	/**
	 * The elements of this 4x4 matrix
	 */
	public float[] elements;
	
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
	
	/**
	 * Gets the element at the specified row, col
	 * @return
	 */
	public float GetElement(int row, int col)
	{
		return elements[ (row - 1) * 4 + (col - 1)];
	}
	
	/**
	 * Multiply this matrix by a vector
	 */
	public void Multiply(Vec3 rhs, Vec3 dest)
	{
		// m11 * x + m21 * y + m31 * z + m41
		// m12 * x + m22 * y + m32 * z + m42
		// m13 * x + m23 * y + m33 * z + m43
		
		dest.x = elements[0] * rhs.x + elements[4] * rhs.y + elements[8] * rhs.z + elements[12];
		dest.y = elements[1] * rhs.x + elements[5] * rhs.y + elements[9] * rhs.z + elements[13];
		dest.z = elements[2] * rhs.x + elements[6] * rhs.y + elements[10] * rhs.z + elements[14];
	}
	
	/**
	 * Multiply this matrix by another matrix
	 * @param rhs the matrix to multiply this matrix with
	 */
	public void Multiply(Mat4 rhs)
	{
		Matrix.multiplyMM(elements, 0, elements, 0, rhs.elements, 0);
	}
	
	/**
	 * Multiply this matrix by another matrix (dest = this * rhs)
 	 * @param rhs the matrix to multiply this matrix with
	 * @param dest the matrix to save the results to
	 */
	public void Multiply(Mat4 rhs, Mat4 dest)
	{
		// dest = this * rhs
		Matrix.multiplyMM(dest.elements, 0, elements, 0, rhs.elements, 0);
	}
	
	/***
	 * Creates a translation matrix
	 */
	public void Translate(float x, float y, float z)
	{
		Matrix.translateM(elements, 0, x, y, z);
	}

	public void Translate(Vec3 rhs)
	{
		Translate(rhs.x, rhs.y, rhs.z);
	}
	
	/**
	 * Create a scale matrix
	 */
	public void Scale(float x, float y, float z)
	{
		Matrix.scaleM(elements, 0, x, y, z);
	}
	
	public void Scale(Vec3 scale)
	{
		Scale(scale.x, scale.y, scale.z);
	}
	
	/**
	 * Create a rotation matrix around the axis
	 */
	public void RotateAxis(float angle, float x, float y, float z)
	{
		Matrix.rotateM(elements, 0, angle, x, y, z);
	}
	
	public void RotateAxis(float angle, Vec3 axis)
	{
		RotateAxis(angle, axis.x, axis.y, axis.z);
	}
	
	/**
	 * Rotates this matrix with the specified euler angles
	 * @param yaw rotating around y axis
	 * @param pitch rotating around x axis
	 * @param roll rotating around z axis
	 */
	public void RotateEuler(float yaw, float pitch, float roll)
	{
		Matrix.setRotateEulerM(elements, 0, yaw, pitch, roll);
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
	public void Transpose(Mat4 transpose)
	{
		Matrix.transposeM(transpose.elements, 0, elements, 0);
	}
	
	/**
	 * Inverts this matrix and put its results in invet
	 */
	public void Invert(Mat4 invert)
	{
		Matrix.invertM(invert.elements, 0, elements, 0);
	}
	
	/**
	 * Creates a perspective matrix out of this matrix
	 * @param fov the field of view (45-90)
	 * @param aspect screen aspect ration (width/height)
	 * @param n near plane of the frustum
	 * @param f far plane of the frustum
	 */
	public void Perspective(float fov, float aspect, float n, float f)
	{
		Matrix.perspectiveM(elements, 0, fov, aspect, n, f);
	}
	
	/**
	 * Standard 2D orthographic projection
	 * @param width x dimension of the screen
	 * @param height y dimension of the screen
	 */
	public void Ortho2D(int width, int height)
	{
		Matrix.orthoM(elements, 0, 0, width, 0, height, 0, 1.0f);
	}
}
