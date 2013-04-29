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
	 * Current alpha of the screen transition, ranging from
	 * 1 (fully active, no transition) to 0 (transitioned fully off to nothing)
	 * 1.0f - transitionPosion
	 */
	public float transitionAlpha;
	
	/**
	 * Current screen state
	 */
	public ScreenState screenState;
	
	/**
	 * Indicates if this screen is exiting
	 */
	public boolean isExiting;
	
	/**
	 * Indicates the controlling player
	 */
	public int playerControlling;
	
	/**
	 * The screenmanager this screen belongs too
	 */
	public ScreenManager screenManager;
	
	/**
	 * Constructor
	 */
	public GameScreen()
	{
		transitionAlpha = 1.0f;
	}
	
	public abstract void Create();
	public abstract void Dispose();
	
	public void Update(float dt, boolean otherScreenHasFocus, boolean coveredByOtherScreen)
	{
		
	}
	
	public void Draw(float dt)
	{
		
	}
	
	public boolean HandleInput()
	{
		return false;
	}
	
	public void UpdateTransition(float dt, float time, int direction)
	{
		
	}
	
	public boolean IsActive()
	{
		return false;
	}
}
