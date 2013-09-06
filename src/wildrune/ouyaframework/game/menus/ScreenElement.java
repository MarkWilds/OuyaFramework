package wildrune.ouyaframework.game.menus;

import wildrune.ouyaframework.graphics.SpriteBatch;
import wildrune.ouyaframework.input.InputSystem;
import wildrune.ouyaframework.math.Vec2;

public abstract class ScreenElement 
{
	public boolean 	isEnabled;
	public String 	identifier;
	public Vec2 	position;
	protected MenuEventHandler eventHandler; 
	
	public ScreenElement(String id)
	{
		this.eventHandler = null;
		this.identifier = id;
		this.position = new Vec2();
		this.isEnabled = true;
	}
	
	public abstract void Update(float dt);
	public abstract void Draw(SpriteBatch batch, boolean selected, float dt);
	public abstract boolean HandleInput(InputSystem input);
	
	public void SetEventHandler(MenuEventHandler handler)
	{
		eventHandler = handler;
	}
}
