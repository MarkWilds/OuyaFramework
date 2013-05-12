package wildrune.ouyaframework.game.menus;

import wildrune.ouyaframework.graphics.SpriteBatch;
import wildrune.ouyaframework.graphics.basic.Color;
import wildrune.ouyaframework.graphics.basic.SpriteFont;
import wildrune.ouyaframework.input.Gamepad;
import wildrune.ouyaframework.input.GamepadCodes;
import wildrune.ouyaframework.input.InputSystem;
import wildrune.ouyaframework.math.RuneMath;

public class MenuTextButton extends ScreenElement
{
	public String text;
	protected SpriteFont font;
	protected Color	selectColor;
	
	private float pulse;

	public MenuTextButton(String id, String text, SpriteFont font, Color selectColor) 
	{
		super(id);
		this.selectColor = selectColor;
		this.text = text;
		this.font = font;
		this.pulse = 0.0f;
	}

	@Override
	public void Update(float dt) 
	{
		pulse = (pulse + 180.0f * dt) % 360.0f;
	}

	@Override
	public void Draw(SpriteBatch batch, boolean selected, float dt) 
	{
		if(selected)
		{
			float scale = (float) (1.0f + Math.sin(pulse * RuneMath.TORAD) * 0.1f);
			batch.DrawText(font, text, position, selectColor, scale, 0.0f, 0.0f);
		}
		else
		{
			pulse = 0.0f;
			batch.DrawText(font, text, position, Color.WHITE_SMOKE);
		}
	}

	@Override
	public boolean HandleInput(InputSystem input) 
	{
		// if we pressed the action button
		Gamepad pad = input.GetGamepad(0);
		if(pad.GetButtonDown(GamepadCodes.BUTTON_O))
		{
			if(this.eventHandler != null)
			{
				eventHandler.HandleEvent(this);
				
				return true;
			}
		}
		
		return false;
	}
}
