package wildrune.ouyaframework.input;

public class GamepadState 
{
	// gamepad state
	int buttonsPressed;
	
	// left axis
	float ls_x_axis;
	float ls_y_axis;
	float l2_axis;
	
	// right axis
	float rs_x_axis;
	float rs_y_axis;
	float r2_axis;
	
	/**
	 * Clears all state
	 */
	public void Clear()
	{
		buttonsPressed = 0;
		
		// left axis
		ls_x_axis = 0.0f;
		ls_y_axis = 0.0f;
		l2_axis = 0.0f;
		
		// right axis
		rs_x_axis = 0.0f;
		rs_y_axis = 0.0f;
		r2_axis = 0.0f;
	}
}
