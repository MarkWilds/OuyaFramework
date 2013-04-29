package wildrune.ouyaframework.game.screens;

/**
 * Represents a game state in the game
 * @author Wildrune
 *
 */
public abstract class GameScreen 
{
	/**
	 * Screen states
	 */
	enum ScreenState
	{
		TRANSITION_ON,
		TRANSITION_OFF,
		ACTIVE,
		HIDDEN
	}
	
	/**
     * Normally when one screen is brought up over the top of another,
     * the first screen will transition off to make room for the new
     *  one. This property indicates whether the screen is only a small
     * popup, in which case screens underneath it do not need to bother
     * transitioning off.
	 */
	public boolean isPopup;
	
	/**
	 * Indicates how long it takes to transition on when it is activated
	 */
	public float transitionOnTime;
	
	/**
	 * Indicates how long it takes to transition off when it is deactivated
	 */
	public float transitionOffTime;
	
	/**
	 * Current position of the screen transition
	 * From 0 (fully active, no transition) to one (transitioned fully off to nothing)
	 */
	public float transitionPosition;
	
	/**
	 * Indicates if this screen is exiting
	 */
	public boolean isExiting;
	
	/**
	 * Indicates if an other screen has focus
	 */
	public boolean otherScreenHasFocus;
	
	/**
	 * Indicates the controlling player
	 */
	public int playerControlling;
	
	/**
	 * Current screen state
	 */
	public ScreenState screenState;
	
	/**
	 * The screenmanager this screen belongs too
	 */
	public ScreenManager screenManager;
	
	/**
	 * Abstract methods
	 */
	public abstract void Create();
	public abstract void Dispose();

	public abstract void Draw(float dt);
	public abstract boolean HandleInput();
	
	/**
	 * Update this screen
	 * @param dt
	 * @param otherScreenHasFocus
	 * @param coveredByOtherScreen
	 */
	public void Update(float dt, boolean otherScreenHasFocus, boolean coveredByOtherScreen)
	{
		this.otherScreenHasFocus = otherScreenHasFocus;
		
		if(isExiting)
		{
			// if the screen is going away to die
			screenState = ScreenState.TRANSITION_OFF;
			
			if(!UpdateTransition(dt, transitionOffTime, 1))
			{
				// remove
				screenManager.RemoveScreen(this);
			}
		}
		else if(coveredByOtherScreen)
		{
			// if the s	creen is covered by another it should transition off
			if(UpdateTransition(dt, transitionOffTime, 1))
			{
				// still busy transitioning
				screenState = ScreenState.TRANSITION_OFF;
			}
			else
			{
				// transition finished
				screenState = ScreenState.HIDDEN;
			}
		}
		else
		{
			// otherwise the screen should tranisition on and become active
			if(UpdateTransition(dt, transitionOnTime, 1))
			{
				// still busy transitioning
				screenState = ScreenState.TRANSITION_ON;
			}
			else
			{
				// transition finished
				screenState = ScreenState.ACTIVE;
			}
		}
	}

	/**
	 * Helper for updating the screen transition position
	 */
	public boolean UpdateTransition(float dt, float time, int direction)
	{
		float tranDelta;	
		
		if(dt == 0.0f)
			tranDelta = 1.0f;
		else
			tranDelta = (dt / time);
		
		// update transition position
		transitionPosition += tranDelta * direction;
			
		//did we reach the end	 of the transition
		if( (direction < 0 && transitionPosition <= 1.0f) 	||
				(direction > 0 && transitionPosition >= 1.0f) )
		{
			// clamp position
			if(transitionPosition <= 0.0f)
				transitionPosition = 0.0f;
			else if ( transitionPosition >= 1.0f)
				transitionPosition = 1.0f;
			
			return false;
		}
			
		return true;
	}
	
	/**
	 * Current alpha of the screen transition, ranging from
	 * 1 (fully active, no transition) to 0 (transitioned fully off to nothing)
	 * 1.0f - transitionPosion
	 */
	public float GetTransitionAlpha()
	{
		return 1.0f - transitionPosition;
	}
	
	/**
	 * Tells if this screen is an active screen
	 */
	public boolean IsActive()
	{
		return !otherScreenHasFocus && 
				(screenState == ScreenState.TRANSITION_ON || screenState == ScreenState.ACTIVE);
	}
	
	/**
	 * Tells the screen to transition off
	 */
	public void ExitScreen()
	{
		if(transitionOffTime == 0.0f)
		{
			//if this screen has a 0 transition off time remove it immediately.
			screenManager.RemoveScreen(this);
		}
		else
		{
			isExiting = true;
		}
		
	}
}
