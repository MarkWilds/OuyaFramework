package wildrune.ouyaframework.input;

/**
 * Represents the state of a gamepad
 */
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
	
	/**
	 * Swap two states around
	 */
	public void CopyStateTo(GamepadState dest)
	{
		dest.buttonsPressed = buttonsPressed;
		
		// left axis
		dest.l2_axis = l2_axis;
		dest.ls_x_axis = ls_x_axis;
		dest.ls_y_axis = ls_y_axis;
		
		// right axis
		dest.r2_axis = r2_axis;
		dest.rs_x_axis = rs_x_axis;
		dest.rs_y_axis = rs_y_axis;
	}
}
