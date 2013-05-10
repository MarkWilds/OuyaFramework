package wildrune.ouyaframework.input;

import android.util.Log;
import android.util.SparseIntArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Represents the gamepad for the OUYA
 * @author Wildrune
 *
 */
public class Gamepad 
{
	private final static float AXIS_BUTTON_THRESHOLD = 0.5f;
	private static final float STICK_DEADZONE = 0.25f;
	
	private static SparseIntArray GamepadMapper;
	
	// general info
	public int deviceId;
	public int playerIndex;
	public boolean isConnected;
	
	private GamepadState gamepadState;
	private GamepadState lastState;
	private GamepadState currentState;
	
	/**
	 * default constructor
	 * @param deviceId the device id of this gamepad
	 */
	public Gamepad(int deviceId)
	{
		this.deviceId = deviceId;
		this.isConnected = false;
		this.gamepadState = new GamepadState();
		this.lastState = new GamepadState();
		this.currentState = new GamepadState();

		Clear();
	}
	
	/**
	 * Clears all state
	 */
	public void Clear()
	{
		gamepadState.Clear();
		lastState.Clear();
		currentState.Clear();
	}
	
	/**
	 * Update the gamepad state
	 */
	public void UpdateFrame()
	{		
		synchronized(gamepadState)
		{
			// put currentState into lastState
			CopyState(currentState, lastState);
	
			// put gamepadState in currentState
			CopyState(gamepadState, currentState);
		}
	}
	
	/**
	 * Swap two states around
	 */
	private void CopyState(GamepadState source, GamepadState dest)
	{
		dest.buttonsPressed = source.buttonsPressed;
		
		// left axis
		dest.l2_axis = source.l2_axis;
		dest.ls_x_axis = source.ls_x_axis;
		dest.ls_y_axis = source.ls_y_axis;
		
		// right axis
		dest.r2_axis = source.r2_axis;
		dest.rs_x_axis = source.rs_x_axis;
		dest.rs_y_axis = source.rs_y_axis;
	}
	
	/**
	 * 
	 * @param code
	 * @return
	 */
	public boolean GetButtonDown(int code)
	{
		int bitPos = 1 << (code - 1);
		
		int lastPressed = lastState.buttonsPressed & bitPos;
		int curPressed = currentState.buttonsPressed & bitPos;
		
		//if(( lastPressed == 0 && curPressed > 0))
			//Log.d("GAMEPAD", "DOWN: " + lastState.buttonsPressed + " / " + currentState.buttonsPressed);
		
		return ( lastPressed == 0 && curPressed > 0);
	}
	
	/**
	 * 
	 * @param code
	 * @return
	 */
	public boolean GetButtonPressed(int code)
	{
		int bitPos = 1 << (code - 1);
		
		int lastPressed = lastState.buttonsPressed & bitPos;
		int curPressed = currentState.buttonsPressed & bitPos;
		
		return ( lastPressed > 0 && curPressed > 0);
	}
	
	/**
	 * 
	 * @param code
	 * @return
	 */
	public boolean GetButtonUp(int code)
	{
		int bitPos = 1 << (code - 1);
		
		int lastPressed = lastState.buttonsPressed & bitPos;
		int curPressed = currentState.buttonsPressed & bitPos;
		
		return ( lastPressed > 0 && curPressed == 0);
	}
	
	/**
	 * 
	 * @param code
	 * @return
	 */
	public float GetAxisRaw(int code)
	{
		return 0.0f;
	}
	
	/**
	 * 
	 * @param code
	 * @return
	 */
	public float GetAxis(int code)
	{
		return 0.0f;
	}	
	
	/***
	 * Handle button down presses
	 */
	public boolean OnKeyDown(int keyCode, KeyEvent event) 
	{
		int padCode = (GamepadMapper.get(keyCode) - 1);

		// error check
		if(padCode < 0 || padCode > 32 )
			return false;
		
		// set the button pressed state
		synchronized(gamepadState)
		{	
			int state = gamepadState.buttonsPressed;
			int bitCode = 1 << padCode;
			gamepadState.buttonsPressed = state | bitCode;
		}
		
		return true;
	}

	/***
	 * Handle button up presses
	 */
	public boolean OnKeyUp(int keyCode, KeyEvent event) 
	{
		int padCode = (GamepadMapper.get(keyCode) - 1);
		
		// error check
		if(padCode < 0 || padCode > 32 )
			return false;
		
		// reset pressed and down
		synchronized(gamepadState)
		{	
			int state = gamepadState.buttonsPressed;
			gamepadState.buttonsPressed = state & ~(1 << padCode);
		}
		
		return true;
	}

	/***
	 * Handle motion events
	 */
	public boolean OnGenericMotionEvent(MotionEvent event) 
	{
		// if it is a joystick
		if( (event.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) != 0)
		{
			// left axis
			gamepadState.ls_x_axis = event.getAxisValue(OuyaGamepadCodes.AXIS_LS_X);
			gamepadState.ls_y_axis = event.getAxisValue(OuyaGamepadCodes.AXIS_LS_Y);
			gamepadState.l2_axis = event.getAxisValue(OuyaGamepadCodes.AXIS_L2);
			
			// right axis
			gamepadState.rs_x_axis = event.getAxisValue(OuyaGamepadCodes.AXIS_RS_X);
			gamepadState.rs_y_axis = event.getAxisValue(OuyaGamepadCodes.AXIS_RS_Y);
			gamepadState.r2_axis = event.getAxisValue(OuyaGamepadCodes.AXIS_R2);
			
			// check if we have any axis button events
			ResolveAxisButtons();
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * We support axis button emulation when a axis is within a threshold
	 */
	private void ResolveAxisButtons()
	{
		// AXIS_BUTTON_THRESHOLD
		
		// check left stick buttons
		
		// check right stick buttons
	}
	
	static
	{
		GamepadMapper = new SparseIntArray(32);
	
		// initialize the map
		GamepadMapper.append(OuyaGamepadCodes.BUTTON_O, GamepadCodes.BUTTON_O);
		GamepadMapper.append(OuyaGamepadCodes.BUTTON_U, GamepadCodes.BUTTON_U);
		GamepadMapper.append(OuyaGamepadCodes.BUTTON_Y, GamepadCodes.BUTTON_Y);
		GamepadMapper.append(OuyaGamepadCodes.BUTTON_A, GamepadCodes.BUTTON_A);
		
		GamepadMapper.append(OuyaGamepadCodes.BUTTON_L1, GamepadCodes.BUTTON_L1);
		GamepadMapper.append(OuyaGamepadCodes.BUTTON_L2, GamepadCodes.BUTTON_L2);
		GamepadMapper.append(OuyaGamepadCodes.BUTTON_R1, GamepadCodes.BUTTON_R1);
		GamepadMapper.append(OuyaGamepadCodes.BUTTON_R2, GamepadCodes.BUTTON_R2);
		GamepadMapper.append(OuyaGamepadCodes.BUTTON_MENU, GamepadCodes.BUTTON_MENU);
		
		GamepadMapper.append(OuyaGamepadCodes.BUTTON_DPAD_UP, GamepadCodes.BUTTON_DPAD_UP);
		GamepadMapper.append(OuyaGamepadCodes.BUTTON_DPAD_RIGHT, GamepadCodes.BUTTON_DPAD_RIGHT);
		GamepadMapper.append(OuyaGamepadCodes.BUTTON_DPAD_DOWN, GamepadCodes.BUTTON_DPAD_DOWN);
		GamepadMapper.append(OuyaGamepadCodes.BUTTON_DPAD_LEFT, GamepadCodes.BUTTON_DPAD_LEFT);
		GamepadMapper.append(OuyaGamepadCodes.BUTTON_R3, GamepadCodes.BUTTON_R3);
		GamepadMapper.append(OuyaGamepadCodes.BUTTON_L3, GamepadCodes.BUTTON_L3);
	}
}
