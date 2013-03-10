package wildrune.ouyaframework.system;

/***
 * Handles timing for the game
 * @author Wildrune
 *
 */
public class Clock 
{
	long prevTime;
	float deltaTime;
	
	/***
	 * Constructor
	 */
	public Clock()
	{
		prevTime = 0;
		deltaTime = 0.0f;
		
	}
	
	/***
	 * Start the timer
	 */
	public void Start()
	{
		prevTime = System.nanoTime();
	}
	
	/***
	 * Advance the clock
	 */
	public void Tick()
	{
		long curTime = System.nanoTime();
		deltaTime = (curTime - prevTime) / 1000000000.0f;
		prevTime = curTime;
	}
	
	/***
	 * Get the deltatime for this frame
	 * @return deltatime in seconds
	 */
	public float GetDeltaTime()
	{
		return deltaTime;
	}
}
