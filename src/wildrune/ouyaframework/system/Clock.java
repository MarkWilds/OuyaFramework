package wildrune.ouyaframework.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/***
 * Handles timing for the game
 * @author Wildrune
 *
 */
public class Clock 
{
	// constants
	public static final int MAX_TIMERS = 4;
	public static final int MAX_FRAME_TIME = 500;
	public static final int TARGET_FRAME_TIME = 60;
	
	// clock members
	private long mPrevTime;
	private float mTotalTime;
	private float mMaxFrameTime;
	private float mTargetFrameTime;
	
	// timer management
	private List<Clock.Timer> mTimers;
	private Stack<Clock.Timer> mTimerStack;
	
	/***
	 * Constructor
	 */
	public Clock()
	{
		mPrevTime = 0;
		mTotalTime = 0.0f;
		mMaxFrameTime = MAX_FRAME_TIME;
		mTargetFrameTime = TARGET_FRAME_TIME;
		mTimers = new ArrayList<Clock.Timer>(MAX_TIMERS);
		mTimerStack = new Stack<Clock.Timer>();
		mTimerStack.ensureCapacity(MAX_TIMERS);
	}
	
	/***
	 * Sets the target frametime
	 * @param targetMs target frame time in miliseconds
	 */
	public void SetTargetFrameTime(float targetMs)
	{
		mTargetFrameTime = targetMs;
	}
	
	/***
	 * Get the target frame time
	 * @return target frame time in miliseconds
	 */
	public float GetTargetFrameTime()
	{
		return 1000.0f / mTargetFrameTime;
	}
	
	/***
	 * Sets the max frame time in miliseconds
	 * @param ms
	 */
	public void SetMaxFrameTime(float ms)
	{
		mMaxFrameTime = ms;
	}
	
	/***
	 * Start the timer
	 */
	public void Start()
	{
		mPrevTime = System.nanoTime();
	}
	
	/***
	 * Gets a new timer
	 * @return
	 */
	public Clock.Timer Get() throws RuntimeException
	{
		// check if we can still create a timer
		if(mTimers.size() >= MAX_TIMERS)
			throw new RuntimeException("Can't create a new timer, clock is at it's max!");
		
		// first check if we have any timers left
		if(!mTimerStack.isEmpty())
		{
			Clock.Timer timer = mTimerStack.pop();
			mTimers.add(timer);
			return timer;
		}
		
		// if we do not have any timers on the stack create a new one
		Clock.Timer timer = new Clock.Timer(this);
		mTimers.add(timer);
		
		return timer;
	}
	
	/***
	 * Recycles a clock
	 * @param timer
	 */
	public void Recycle(Clock.Timer timer)
	{
		if(mTimers.contains(timer))
			mTimers.remove(timer);
		
		mTimerStack.add(timer);
	}
	
	/***
	 * Clears the timers
	 */
	public void Clear()
	{
		mTimers.clear();
		mTimerStack.clear();
	}
	
	/***
	 * Advance the clock
	 */
	public void Tick()
	{
		boolean slow = false;
		long curTime = System.nanoTime();
		float frameTime = (curTime - mPrevTime) / 1000000.0f;
		mPrevTime = curTime;
		
		// calc total time since start
		mTotalTime += frameTime;
		
		// check for spiral of death
		if(frameTime > mMaxFrameTime)
			frameTime = mMaxFrameTime;
		
		// check if we are running slowly
		if(frameTime > mTargetFrameTime)
			slow = true;
		
		// update all timers
		if(!mTimers.isEmpty())
		{
			List<Clock.Timer> localTimers = mTimers;
			int timerLength = mTimers.size();
			for(int i = 0; i < timerLength; i++)
			{
				localTimers.get(i).mElapsedGameTime = frameTime;
				localTimers.get(i).mTotalGameTime = mTotalTime;
				localTimers.get(i).mIsRunningSlowly = slow;
			}
		}
	}
	
	/***
	 * Individual timers
	 * @author Wildrune
	 *
	 */
	public class Timer
	{
		private Clock		mClock;
		private float 		mElapsedGameTime;
		private float		mTotalGameTime;
		private float		mTimeScale;
		private boolean 	mIsRunningSlowly;
		
		/***
		 * Overloaded constructor
		 * @param clock the parent clock
		 */
		public Timer(Clock clock)
		{
			mElapsedGameTime = 0.0f;
			mTimeScale = 1.0f;
			mTotalGameTime = 0.0f;
			mIsRunningSlowly = false;
			mClock = clock;
		}
		
		/***
		 * Gets the parent clock this timer belongs too
		 * @return The parent clock
		 */
		public Clock GetClock()
		{
			return mClock;
		}
		
		/***
		 * Reuse this timer
		 */
		public void Recycle()
		{
			// recylce
			mClock.Recycle(this);
			
			// reset values
			mElapsedGameTime = 0.0f;
			mTotalGameTime = 0.0f;
			mTimeScale = 1.0f;
			mIsRunningSlowly = false;
		}
		
		/***
		 * Sets the timescale of this timer
		 * @param timescale
		 */
		public void SetTimeScale(float timescale){
			mTimeScale = timescale;
		}
	
		/***
		 * Gets the deltatime for this timer in miliseconds
		 * @return
		 */
		public float GetElapsedMiliseconds() {
			return mElapsedGameTime * mTimeScale;
		}
		
		/***
		 * Gets the deltatime for this timer in seconds
		 * @return
		 */
		public float GetElapsedSeconds() {
			return (mElapsedGameTime / 1000.0f) * mTimeScale;
		}
		
		/**
		 * Gets the total game time since start
		 * @return
		 */
		public float GetTotalGameTimeSeconds() {
			return (mTotalGameTime / 1000.0f);
		}
		
		/***
		 * Gets if the clock this timer belongs to is running slowly
		 * @return
		 */
		public boolean IsRunningSlowly(){
			return mIsRunningSlowly;
		}
		
		/***
		 * Resets the elapsed game time
		 */
		public void ResetElapsedGameTime()
		{
			mElapsedGameTime = 0.0f;
		}
	}
}
