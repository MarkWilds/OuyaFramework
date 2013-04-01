package wildrune.ouyaframework.math;

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
	
	// ############# POOL ######################
	
	// ############# POOL END ##################
	
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
}
