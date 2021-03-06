package wildrune.ouyaframework.math;

import wildrune.ouyaframework.utils.ObjectPool;

/***
 * Math utility class
 * @author Wildrune
 *
 */
public class RuneMath 
{
	/***
	 * Math values
	 */
	public static final float TWOPI = (float) (Math.PI * 2.0f);
	public static final float PIOVERTWO = (float) (Math.PI / 2.0f);
	public static final float TORAD = (float) (Math.PI / 180.0f);
	public static final float TODEG = (float) (180.0f / Math.PI);
	public static final float EPSILONS = 1e-6f;
	public static final float EPSILONH = 1e-3f;
	
	// ############# MATH POOL ######################
	private static final ObjectPool<Vec2> mVector2Pool = new ObjectPool<Vec2>(Vec2.class);
	private static final ObjectPool<Vec3> mVector3Pool = new ObjectPool<Vec3>(Vec3.class);
	private static final ObjectPool<Vec4> mVector4Pool = new ObjectPool<Vec4>(Vec4.class);
	private static final ObjectPool<Mat4> mMatrix4Pool = new ObjectPool<Mat4>(Mat4.class);
	
	static
	{
		Vec2.mParentPool = mVector2Pool;
		Vec3.mParentPool = mVector3Pool;
		Vec4.mParentPool = mVector4Pool;
		Mat4.mParentPool = mMatrix4Pool;
	}
	
	// POOL GETTERS
	public static Vec2 GetVec2()
	{
		return mVector2Pool.Get();
	}
	
	public static Vec3 GetVec3()
	{
		return mVector3Pool.Get();
	}
	
	public static Vec4 GetVec4()
	{
		return mVector4Pool.Get();
	}
	
	public static Mat4 GetMat4()
	{
		return mMatrix4Pool.Get();
	}
	
	// POOL RECYCLERS
	public static void Recycle(Vec2 vec)
	{
		vec.Zero();
		mVector2Pool.Recycle(vec);
	}
	
	public static void Recycle(Vec3 vec)
	{
		vec.Zero();
		mVector3Pool.Recycle(vec);
	}
	
	public static void Recycle(Vec4 vec)
	{
		vec.Zero();
		mVector4Pool.Recycle(vec);
	}
	
	public static void Recycle(Mat4 mat)
	{
		mat.Identity();
		mMatrix4Pool.Recycle(mat);
	}
	
	// ############# POOL END #######################
	
	/***
	 * let's use see if the value is close enough to some other value
	 * @param value the value we perform the check on
	 * @param near the value that is used 
	 * @return true if the value is near enough
	 */
	public static boolean IsCloseEnough(float value, float near)
	{
		return Math.abs( (value - near) / ( (near == 0.0f) ? 1.0f : near) ) < EPSILONS;
	}
	
	/***
	 * Finding the roots of a quadratic equation in it's standard form
	 * ABC formula and discriminant used
	 * @param t1 the first result
	 * @param t2 the second result
	 * @return
	 */
	public static boolean SolveRoots(float a, float b, float c, float t1, float t2)
	{
        // calculate the discriminant
        float d = b * b - 4 * a * c;
        float denom = 1.0f / (2.0f * a);

        // if there are no real solutions return false
        if( d < 0.0f ) return false;

        if( IsCloseEnough(d, 0.0f) )
        {
            t1 = t2 = -b * denom;
        }
        else
        {
            // get the values for t
            t1 = (float) ((-b - Math.sqrt( d )) * denom);
            t2 = (float) ((-b + Math.sqrt( d )) * denom);
        }

        return true;
    }
	
	/***
	 * Check if a number is the power of two
	 * @param x the number we want to check
	 * @return true if it is a power of two number
	 */
	public static boolean IsPower2(int x)
	{
		return ( ( x > 0 ) && ( ( x & ( x - 1 ) ) == 0 ) );
	}
	
	/***
	 * Get the next power of two number
	 * @param x the number we want to find the next power of 2 of
	 * @return the next power of two number
	 */
	public static int NextPower2(int x)
	{
		int i = x & (~x + 1);

		while (i < x)
			i <<= 1;

		return i;
	}
	
	/***
	 * Get the previous power of two number
	 * @param x the number we want to find the previous power of 2 of
	 * @return the previous power of two number
	 */
	public static int PrevPower2(int x)
	{
		int m = x;
		int i = 0;
		
	 	for(; m > 1; i++)
	 		m = m >>> 1;

		// Round to nearest power
		if( (x & 1 << i-1) > 0) i++;
		return 1 << i;
	}
	
	/**
	 * Gets the closes power of 2 number
	 * @param x the number we want to find the closest power of to from
	 * @return the closest power of 2
	 */
	public static int ClosestPowerOf2(int x)
	{
		// get power of two
		int npot = RuneMath.NextPower2(x);
		int ppot = RuneMath.PrevPower2(x);
		
		// check which power of two to choose
		return RuneMath.IsClosestTo(npot, ppot, x);
	}
	
	/**
	 * Returns the value that is the closest to the given value
	 * @param a first value to compare
	 * @param b second value to compare
	 * @param to value to check with
	 * @return the closes value of a or b
	 */
	public static int IsClosestTo(int a, int b, int to)
	{
		int diffA = Math.abs(to - a);
		int diffB = Math.abs(to - b);
		
		return ( diffA < diffB ) ? a : b;
	}
}
