package wildrune.ouyaframework.game.menus;

import wildrune.ouyaframework.input.InputSystem;
import wildrune.ouyaframework.graphics.SpriteBatch;

public class ConsoleMenu extends ScreenElement
{
	private ScreenElement[] menuItems;
	private int maxMenuItems;
	private int menuItemsCount;
	private int selectedItem;
	
	private int heightSpacing;
	
	public ConsoleMenu(String id, int maxItems, int spacing)
	{
		super(id);
		
		// init
		menuItems = new ScreenElement[maxItems];
		maxMenuItems = maxItems;
		menuItemsCount = 0;
		selectedItem = 0;
		heightSpacing = spacing;
	}
	
	public void AddItem(ScreenElement element)
	{
		if(menuItemsCount >= maxMenuItems)
			return;
		
		menuItems[menuItemsCount++] = element;
	}
	
	private void SelectPrev()
	{
		selectedItem++;
		
		if(selectedItem < 0)
			selectedItem = menuItemsCount;
	}
	
	private void SelectNext()
	{
		selectedItem++;
		
		if(selectedItem > menuItemsCount)
			selectedItem = 0;
	}

	@Override
	public void Update(float dt) 
	{ 
		for(int i = 0; i < menuItemsCount; i++)
		{
			menuItems[i].Update(dt);
		}
	}

	@Override
	public void Draw(SpriteBatch batch, boolean selected, float dt)
	{		
		for(int i = 0; i < menuItemsCount; i++)
		{
			if(i == selectedItem)
				selected = true;
			else
				selected = false;
			
			// set menu position
			menuItems[i].position.x = position.x;
			menuItems[i].position.y = position.y + i * heightSpacing;
			
			// draw item
			menuItems[i].Draw(batch, selected, dt);
		}
	}

	@Override
	public boolean HandleInput(InputSystem input) 
	{
		// handle selection
		if(menuItems[this.selectedItem].HandleInput(input))
				return true;
		
		return false;
	}

}
