package wildrune.ouyaframework.game.screens;

/**
 * Represents a game state in the game
 * @author Wildrune
 *
 */
public abstract class GameScreen 
{
	@SuppressWarnings("unused")
	private final static String LOG_TAG = "GameScreen";
	
	// Screen states
	public enum ScreenState
	{
		TRANSITION_ON,
		TRANSITION_OFF,
		ACTIVE,
		HIDDEN
	}
	
	// Holds transition data for this gamescreen instance
	public class TransitionData
	{
		// time for a transition to take
		public float onTime = 0.0f;
		public float offTime = 0.0f;
		
		// Current position of the screen transition
		// From 0 (fully active, no transition) to one (transitioned fully off to nothing)
		public float position = 1.0f;
		
		// constructor
		public TransitionData(float off, float on)
		{
			onTime = on;
			offTime = off;
		}
		
		// Current alpha of the screen transition, ranging from
		// (fully active, no transition) to 0 (transitioned fully off to nothing)
		public float GetTransitionAlpha()
		{
			return 1.0f - position;
		}
		
		// Helper for updating the screen transition position
		public boolean Transit(float dt, float time, int direction)
		{
			float tranDelta;
			
			if(time == 0.0f)
				tranDelta = 1.0f;
			else
				tranDelta = dt / time;
			
			// update transition position
			position += tranDelta * direction;
				
			//did we reach the end of the transition
			if( (direction < 0 && position <= 0.0f) 	||
					(direction > 0 && position >= 1.0f) )
			{
				// clamp position
				if(position < 0.0f)
					position = 0.0f;
				else if ( position > 1.0f)
					position = 1.0f;
				
				return false;
			}
				
			return true;
		}
	}
	
	// parent screenmanager
	public ScreenManager screenManager = null;
	
	// Current screen state
	public ScreenState screenState = ScreenState.TRANSITION_ON;
	
	// Transition data for this screen
	public TransitionData transition;
	
	// Indicates the controlling player
	public int controllingPlayer = 0;
	
    // Tells the screenmanager that underneath screens do not need to transition off
	public boolean isPopup = false;
	
	// Indicates if this screen is exiting
	public boolean isExiting = false;
	
	// Indicates if an other screen has focus
	public boolean otherScreenHasFocus = false;
	
	/**
	 * Default constructor
	 */
	public GameScreen()
	{
		transition = new TransitionData(0.0f, 0.0f);
	}
	
	/**
	 * Abstract methods
	 */
	public abstract void Create();
	public abstract void Dispose();

	public abstract void Update(float dt);
	public abstract void Draw(float dt);
	public abstract boolean HandleInput();
	
	/**
	 * Update this screen
	 * @param dt
	 * @param otherScreenHasFocus
	 * @param coveredByOtherScreen
	 */
	public void UpdateScreen(float dt, boolean otherScreenHasFocus, boolean coveredByOtherScreen)
	{
		this.otherScreenHasFocus = otherScreenHasFocus;
		
		if(isExiting)
		{
			// if the screen is going away to die
			screenState = ScreenState.TRANSITION_OFF;
			
			if(!transition.Transit(dt, transition.offTime, 1))
			{
				// remove
				screenManager.RemoveScreen(this);
			}
		}
		else if(coveredByOtherScreen)
		{
			// if the screen is covered by another it should transition off
			if(transition.Transit(dt, transition.offTime, 1))
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
			// otherwise the screen should transition on and become active
			if(transition.Transit(dt, transition.onTime, -1))
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
		
		// update the subclass update method
		Update(dt);
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
	 * Tell if this screen is currently transisioning
	 */
	public boolean IsTransisioning()
	{
		return screenState == ScreenState.TRANSITION_ON || screenState == ScreenState.TRANSITION_OFF;
	}
	
	/**
	 * Tells the screen to transition off
	 */
	public void ExitScreen()
	{
		if(transition.offTime == 0.0f)
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
