package wildrune.ouyaframework.game.menus;

import wildrune.ouyaframework.graphics.SpriteBatch;
import wildrune.ouyaframework.graphics.basic.Color;
import wildrune.ouyaframework.graphics.basic.SpriteFont;
import wildrune.ouyaframework.input.InputSystem;

public class MenuTextButton extends ScreenElement
{
	protected String text;
	protected SpriteFont font;
	protected Color	selectColor;

	public MenuTextButton(String id, String text, SpriteFont font, Color selectColor) 
	{
		super(id);
		this.selectColor = selectColor;
		this.text = text;
		this.font = font;
	}

	@Override
	public void Update(float dt) 
	{
	}

	@Override
	public void Draw(SpriteBatch batch, boolean selected, float dt) 
	{
		if(selected)
			batch.DrawText(font, text, position, selectColor);
		else
			batch.DrawText(font, text, position, Color.WHITE_SMOKE);
	}

	@Override
	public boolean HandleInput(InputSystem input) 
	{
		// if we pressed the action button
		
		/*if(this.eventHandler != null)
		{
			eventHandler.HandleEvent(this);
			
			return true;
		}*/
		
		return false;
	}
}
