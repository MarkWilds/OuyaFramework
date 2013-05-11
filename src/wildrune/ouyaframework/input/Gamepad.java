package wildrune.ouyaframework.input;

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
	private static SparseIntArray GAMEPAD_MAPPER;
	
	// general info
	public int deviceId;
	public int playerIndex;
	public boolean isConnected;
	
	// gamepad states
	private GamepadState gamepadState;
	private GamepadState lastState;
	private GamepadState currentState;
	
	static
	{
		GAMEPAD_MAPPER = new SparseIntArray(GamepadCodes.MAX_BUTTONS);
	
		// initialize the map
		GAMEPAD_MAPPER.append(OuyaGamepadCodes.BUTTON_O, GamepadCodes.BUTTON_O);
		GAMEPAD_MAPPER.append(OuyaGamepadCodes.BUTTON_U, GamepadCodes.BUTTON_U);
		GAMEPAD_MAPPER.append(OuyaGamepadCodes.BUTTON_Y, GamepadCodes.BUTTON_Y);
		GAMEPAD_MAPPER.append(OuyaGamepadCodes.BUTTON_A, GamepadCodes.BUTTON_A);
		
		GAMEPAD_MAPPER.append(OuyaGamepadCodes.BUTTON_L1, GamepadCodes.BUTTON_L1);
		GAMEPAD_MAPPER.append(OuyaGamepadCodes.BUTTON_L2, GamepadCodes.BUTTON_L2);
		GAMEPAD_MAPPER.append(OuyaGamepadCodes.BUTTON_R1, GamepadCodes.BUTTON_R1);
		GAMEPAD_MAPPER.append(OuyaGamepadCodes.BUTTON_R2, GamepadCodes.BUTTON_R2);
		GAMEPAD_MAPPER.append(OuyaGamepadCodes.BUTTON_MENU, GamepadCodes.BUTTON_MENU);
		
		GAMEPAD_MAPPER.append(OuyaGamepadCodes.BUTTON_DPAD_UP, GamepadCodes.BUTTON_DPAD_UP);
		GAMEPAD_MAPPER.append(OuyaGamepadCodes.BUTTON_DPAD_RIGHT, GamepadCodes.BUTTON_DPAD_RIGHT);
		GAMEPAD_MAPPER.append(OuyaGamepadCodes.BUTTON_DPAD_DOWN, GamepadCodes.BUTTON_DPAD_DOWN);
		GAMEPAD_MAPPER.append(OuyaGamepadCodes.BUTTON_DPAD_LEFT, GamepadCodes.BUTTON_DPAD_LEFT);
		GAMEPAD_MAPPER.append(OuyaGamepadCodes.BUTTON_R3, GamepadCodes.BUTTON_R3);
		GAMEPAD_MAPPER.append(OuyaGamepadCodes.BUTTON_L3, GamepadCodes.BUTTON_L3);
	}
	
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
			// and gamepadState into currentstate
			currentState.CopyStateTo(lastState);
			gamepadState.CopyStateTo(currentState);
		}
	}
	
	/**
	 * a key is down when lastState = false && currentState = false
	 * @param code
	 * @return
	 */
	public boolean GetButtonDown(int code)
	{
		int bitPos = 1 << (code - 1);
		
		int lastPressed = lastState.buttonsPressed & bitPos;
		int curPressed = currentState.buttonsPressed & bitPos;
		
		return ( lastPressed == 0 && curPressed > 0);
	}
	
	/**
	 * a key is pressed when lastState = true && currentState = true
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
	 * a key is up when lastState = true && currentState = false
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
	 * Returns the raw axis
	 * @param code the key code to return the axis for
	 * @return axis value between -1.0 and 1.0
	 */
	public float GetAxisRaw(int code)
	{
		switch(code)
		{
		case GamepadCodes.AXIS_LS_X:
			return currentState.ls_x_axis;
		case GamepadCodes.AXIS_LS_Y:
			return currentState.ls_y_axis;
		case GamepadCodes.AXIS_L2:
			return currentState.l2_axis;
		case GamepadCodes.AXIS_RS_X:
			return currentState.rs_x_axis;
		case GamepadCodes.AXIS_RS_Y:
			return currentState.rs_y_axis;
		case GamepadCodes.AXIS_R2:
			return currentState.r2_axis;
		default:
			return 0.0f;
		}
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
		int padCode = (GAMEPAD_MAPPER.get(keyCode) - 1);

		// error check
		if(padCode < 0 || padCode >= GamepadCodes.MAX_BUTTONS )
			return false;
		
		// set the button pressed state
		SetButtonPressed( 1 << padCode , true);
		
		return true;
	}

	/***
	 * Handle button up presses
	 */
	public boolean OnKeyUp(int keyCode, KeyEvent event) 
	{
		int padCode = (GAMEPAD_MAPPER.get(keyCode) - 1);
		
		// error check
		if(padCode < 0 || padCode >= GamepadCodes.MAX_BUTTONS )
			return false;
		
		// reset pressed and down
		SetButtonPressed( 1 << padCode , false);
		
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
			
			// simulate dpad with analog sticks
			SimulateLeftStickDpad();
			SimulateRightStickDpad();
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Simulate dpad button presses with the left stick
	 */
	private void SimulateLeftStickDpad()
	{
		int bitCode = -1;
		
		// X Axis
		if(gamepadState.ls_x_axis < -AXIS_BUTTON_THRESHOLD)
		{
			bitCode = 1 << (GamepadCodes.BUTTON_LS_LEFT - 1);
		}
		else if(gamepadState.ls_x_axis > AXIS_BUTTON_THRESHOLD)
		{
			bitCode = 1 << (GamepadCodes.BUTTON_LS_RIGHT - 1);
		}
		
		// Y Axis
		if(gamepadState.ls_y_axis < -AXIS_BUTTON_THRESHOLD)
		{
			bitCode = 1 << (GamepadCodes.BUTTON_LS_UP - 1);
		}
		else if(gamepadState.ls_y_axis > AXIS_BUTTON_THRESHOLD)
		{
			bitCode = 1 << (GamepadCodes.BUTTON_LS_DOWN - 1);
		}
		
		// set button state
		if(bitCode > -1)
		{
			SetButtonPressed(bitCode, true);
			bitCode = -1;
		}
		
		// reset X Axis if needed
		if(gamepadState.ls_x_axis >= -AXIS_BUTTON_THRESHOLD && gamepadState.ls_x_axis <= 0.0f)
		{
			bitCode = 1 << (GamepadCodes.BUTTON_LS_LEFT - 1);
		}
		else if(gamepadState.ls_x_axis <= AXIS_BUTTON_THRESHOLD && gamepadState.ls_x_axis >= 0.0f)
		{
			bitCode = 1 << (GamepadCodes.BUTTON_LS_RIGHT - 1);
		}
		
		// reset Y Axis if needed
		if(gamepadState.ls_y_axis >= -AXIS_BUTTON_THRESHOLD && gamepadState.ls_y_axis <= 0.0f )
		{
			bitCode = 1 << (GamepadCodes.BUTTON_LS_UP - 1);
		}
		else if(gamepadState.ls_y_axis <= AXIS_BUTTON_THRESHOLD && gamepadState.ls_y_axis >= 0.0f)
		{
			bitCode = 1 << (GamepadCodes.BUTTON_LS_DOWN - 1);
		}
		
		// set button state
		if(bitCode > -1)
		{
			SetButtonPressed(bitCode, false);
			bitCode = -1;
		}
	}
	
	/**
	 * Simulate dpad button presses with the right stick
	 */
	private void SimulateRightStickDpad()
	{
		int bitCode = -1;
		
		// X Axis
		if(gamepadState.rs_x_axis < -AXIS_BUTTON_THRESHOLD)
		{
			bitCode = 1 << (GamepadCodes.BUTTON_RS_LEFT - 1);
		}
		else if(gamepadState.rs_x_axis > AXIS_BUTTON_THRESHOLD)
		{
			bitCode = 1 << (GamepadCodes.BUTTON_RS_RIGHT - 1);
		}
		
		// Y Axis
		if(gamepadState.rs_y_axis < -AXIS_BUTTON_THRESHOLD)
		{
			bitCode = 1 << (GamepadCodes.BUTTON_RS_UP - 1);
		}
		else if(gamepadState.rs_y_axis > AXIS_BUTTON_THRESHOLD)
		{
			bitCode = 1 << (GamepadCodes.BUTTON_RS_DOWN - 1);
		}
		
		// set button state
		if(bitCode > -1)
		{
			SetButtonPressed(bitCode, true);
			bitCode = -1;
		}
		
		// reset X Axis if needed
		if(gamepadState.rs_x_axis >= -AXIS_BUTTON_THRESHOLD && gamepadState.rs_x_axis <= 0.0f)
		{
			bitCode = 1 << (GamepadCodes.BUTTON_RS_LEFT - 1);
		}
		else if(gamepadState.rs_x_axis <= AXIS_BUTTON_THRESHOLD && gamepadState.rs_x_axis >= 0.0f)
		{
			bitCode = 1 << (GamepadCodes.BUTTON_RS_RIGHT - 1);
		}
		
		// reset Y Axis if needed
		if(gamepadState.rs_y_axis >= -AXIS_BUTTON_THRESHOLD && gamepadState.rs_y_axis <= 0.0f )
		{
			bitCode = 1 << (GamepadCodes.BUTTON_RS_UP - 1);
		}
		else if(gamepadState.rs_y_axis <= AXIS_BUTTON_THRESHOLD && gamepadState.rs_y_axis >= 0.0f)
		{
			bitCode = 1 << (GamepadCodes.BUTTON_RS_DOWN - 1);
		}
		
		// set button state
		if(bitCode > -1)
		{
			SetButtonPressed(bitCode, false);
			bitCode = -1;
		}
	}
	
	/**
	 * Toggles a button pressed state on or off
	 * @param bitCode the code for the button to set state for
	 * @param on true if we want to toggle it on else false for toggle off
	 */
	private void SetButtonPressed(int bitCode, boolean on)
	{
		synchronized(gamepadState)
		{	
			int state = gamepadState.buttonsPressed;
			
			if(on)
				gamepadState.buttonsPressed = state | bitCode;
			else
				gamepadState.buttonsPressed = state & ~bitCode;
		}
	}
}
