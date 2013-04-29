package wildrune.ouyaframework.game.screens;

import java.util.ArrayList;
import java.util.List;

import wildrune.ouyaframework.OuyaGameActivity;
import wildrune.ouyaframework.game.screens.GameScreen.ScreenState;
import wildrune.ouyaframework.graphics.SpriteBatch;
import wildrune.ouyaframework.graphics.SpriteBatch.SpriteEffect;
import wildrune.ouyaframework.graphics.basic.Color;
import wildrune.ouyaframework.graphics.basic.Rectangle;
import wildrune.ouyaframework.graphics.basic.Texture2D;

public class ScreenManager 
{
	/**
	 * Static members
	 */
	private final static int MAX_SCREENS = 8;
	
	/**
	 * data members
	 */
	private OuyaGameActivity game;
	private SpriteBatch spriteBatch;
	
	private List<GameScreen> screens;
	private List<GameScreen> screensToUpdate;
	
	private boolean initialized;
	
	// resources
	private Texture2D blank;
	
	/**
	 * Default constructor
	 * @param game the game this screenmanager belongs too
	 */
	public ScreenManager(OuyaGameActivity game)
	{
		this.game = game;
		this.initialized = false;
		this.spriteBatch = null;
		this.blank = null;
		
		screens = new ArrayList<GameScreen>(MAX_SCREENS);
		screensToUpdate = new ArrayList<GameScreen>(MAX_SCREENS);
	}
	
	/**
	 * Create resources for this screenmanager
	 */
	public void Create()
	{
		spriteBatch = new SpriteBatch(game.gameGraphics);
		
		// load blank texture
		
		initialized = true;
		
		// create screens if any
		List<GameScreen> localScreens = screens;
		int screenCount = localScreens.size();
		GameScreen curScreen = null;
		
		// iterate all screens and draw them if they are not hidden
		for(int i = 0; i < screenCount; i++)
		{
			curScreen = localScreens.get(i);			
			curScreen.Create();
		}
	}
	
	/**
	 * Dispose of the screenmanager screens
	 */
	public void Dispose()
	{
		spriteBatch.Dispose();
		blank.Dispose();
		
		// dispose of screens if any left
		List<GameScreen> localScreens = screens;
		int screenCount = localScreens.size();
		GameScreen curScreen = null;
		
		// iterate all screens and draw them if they are not hidden
		for(int i = 0; i < screenCount; i++)
		{
			curScreen = localScreens.get(i);			
			curScreen.Dispose();
		}
	}
	
	/**
	 * Update all screens in the manager
	 * @param dt the deltaTime
	 */
	public void Update(float dt)
	{
		boolean otherScreenHasFocus = false;
		boolean coveredByOtherScreen = false;
		GameScreen curScreen = null;
		
		// update input
		
		// clear update list
		screensToUpdate.clear();
		
		// adds screens to screens to update
		List<GameScreen> localScreens = screens;
		int screenCount = localScreens.size();
		
		// iterate all screens and put the in the to update list
		for(int i = 0; i < screenCount; i++)
		{
			screensToUpdate.add( localScreens.get(i) );
		}
		
		// update screens
		int screensSize = screenCount;
		while( screenCount > 0)
		{
			// pop the top screen
			curScreen = screensToUpdate.get(screensSize - 1);
			screensToUpdate.remove(screensSize - 1);
			
			// update the screen
			curScreen.Update(dt, otherScreenHasFocus, coveredByOtherScreen);
			
			// if this is the first active screen we came across.
			// give it a change to handle input
			if(curScreen.screenState == ScreenState.TRANSITION_ON ||
					curScreen.screenState == ScreenState.ACTIVE)
			{
			
				// if not other screen has focus handle the input
				if(!otherScreenHasFocus)
				{
					curScreen.HandleInput();
					otherScreenHasFocus = true;
				}
				
				// if this screen is a popup let the other screens know that need to be updated after
				if(!curScreen.isPopup)
				{
					coveredByOtherScreen = true;
				}
			}
		}
	}
	
	/**
	 * Draw all screens in the screenmanager
	 * @param dt the deltatime
	 */
	public void Draw(float dt)
	{
		List<GameScreen> localScreens = screens;
		int screenCount = localScreens.size();
		GameScreen curScreen = null;
		
		// iterate all screens and draw them if they are not hidden
		for(int i = 0; i < screenCount; i++)
		{
			curScreen = localScreens.get(i);
			if( curScreen.screenState == ScreenState.HIDDEN)
				continue;
			
			curScreen.Draw(dt);
		}
	}
	
	/**
	 * Adds the given screen to this manager
	 * @param screen
	 */
	public void AddScreen(GameScreen screen, int playerIndex)
	{
		screen.playerControlling = playerIndex;
		screen.screenManager = this;
		screen.isExiting = false;
		
		if(initialized)
		{
			screen.Create();
		}
		
		// add to collections
		screens.add(screen);
	}
	
	/**
	 * Remove a screen from this manager
	 * @param screen the screen to remove
	 */
	public void RemoveScreen(GameScreen screen)
	{
		// if we are initalized dispose of resources
		if(this.initialized)
		{
			screen.Dispose();
		}
		
		// remove from the collections
		screens.remove(screen);
		screensToUpdate.remove(screen);
	}
	
	/**
	 * Fades the backbuffer to black or to white
	 * @param alpha the alpha value of the fade
	 */
	public void FadeBackBufferToBlack(float alpha)
	{
		Rectangle viewport = game.gameGraphics.viewportNormal;
		Color color = Color.BLACK;
		
		// draw fullscreen quad
		spriteBatch.Begin();
		
		spriteBatch.DrawSprite(blank,
				0, 0, viewport.width, viewport.height, 
				0, 0, blank.width, blank.height, 
				color.r * alpha, color.g * alpha, color.b * alpha, color.a * alpha, 
				0.0f, 0.0f, 0.0f, 0.0f, SpriteEffect.NONE);
		
		spriteBatch.End();
	}
}
