package wildrune.ouyaframework.input;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class InputSystem
{
	// general input variables
	//private static final String OUYA_GAMEPAD_ID = "OUYA Game Controller";
	public static final int MAX_GAMEPADS = 4;
	
	private static final String EXTRA_GAMEPAD_DEVICE_ID = "DEVICE_ID";
	private static final String EXTRA_GAMEPAD_PLAYER_NUM = "PLAYER_NUM";
	private static final int INVALID_INPUT_DEVICE_ID = -1;
	
	// gamepad disconnect and connect
	private static final String OUYA_CONTROLLER_ADDED_ACTION = "tv.ouya.controller.added";
	private static final String OUYA_CONTROLLER_REMOVED_ACTION = "tv.ouya.controller.removed";
	  
	// the android context
	private Context context;
	private GamepadConnectionListener connectionListener;
	
	// the four gamepads for each player one
	private Gamepad[] gamepads;
	
	// system variable
	private GamepadConnectionHandler connectionHandler;
	
	/**
	 * Default constructor
	 * @param contenxt
	 */
	public InputSystem(Context context)
	{
		this.context = context;
		this.connectionListener = null;
		
		// create gamepads
		gamepads = new Gamepad[MAX_GAMEPADS];
		for(int i = 0; i < MAX_GAMEPADS; i++)
		{
			gamepads[i] = new Gamepad(INVALID_INPUT_DEVICE_ID);
			gamepads[i].playerIndex = i;
		}
		
		// register game connection listener
		IntentFilter connectionFilter = new IntentFilter();
		connectionFilter.addAction(OUYA_CONTROLLER_ADDED_ACTION);
		connectionFilter.addAction(OUYA_CONTROLLER_REMOVED_ACTION);
		connectionHandler = new GamepadConnectionHandler();
		context.registerReceiver(connectionHandler, connectionFilter);
	}
	
	/**
	 * Unregister the controller connection or disconnection event listener
	 */
	public void Deinit()
	{
		// unregister receiver
		context.unregisterReceiver(connectionHandler);
	}
	
	/**
	 * Resets the gamepads state for a new frame
	 */
	public void UpdateFrame()
	{
		for(int i = 0; i < MAX_GAMEPADS; i++)
		{
			gamepads[i].UpdateFrame();
		}
	}
	
	/**
	 * Sets a connection listener for the gamepads
	 * @param listener
	 */
	public void SetConnectionListener(GamepadConnectionListener listener)
	{
		connectionListener = listener;
	}
	
	/**
	 * Returns a gamepad belonging to the given player indexs
	 * @param playerindex should be between 0 - (MAX_CONTROLLERS - 1)
	 * @return the gamepad belonging to the specified playerindex
	 */
	public Gamepad GetGamepad(int playerindex)
	{
		if(playerindex < 0 && playerindex >= MAX_GAMEPADS)
			return null;
		
		return gamepads[playerindex];
	}
	
	/**
	 * Gets a gamepad belonging to the deviceId
	 * @param deviceId the device the gamepad belongs too
	 * @return the gamepad the deviceId belongs to
	 */
	private Gamepad GetGamepadByDeviceId(int deviceId)
	{
		for(int i = 0; i < MAX_GAMEPADS; i++)
		{
			Gamepad pad = gamepads[i];
			
			if(pad != null && pad.deviceId == deviceId)
				return pad;
		}
		
		return null;
	}
	
	/**
	 * Creates or gets a gamepad belonging to the deviceId
	 * @param deviceId
	 * @return
	 */
	private Gamepad GetOrCreateGamepadByDeviceId(int deviceId)
	{
		Gamepad pad = GetGamepadByDeviceId(deviceId);
		
		if(pad != null)
			return pad;
		
		// init a gamepad for a deviceId based on it's playerindex
		Uri gamepadUri = Uri.parse("content://tv.ouya.controllerdata");
		ContentResolver contentResolver = context.getContentResolver();
		
		// query the ouya system for this gamepad's playerindex
		Cursor cursor = contentResolver.query(gamepadUri, new String[] { "player_num" }, "input_device_id = ?", 
				new String[] { String.valueOf(deviceId) }, null);
		
		try
		{
			// cursor needs to contain data
			if(cursor != null && cursor.moveToNext())
			{
				// get player number
				int playerNr = cursor.getInt(0);
				
				// error check
				if(playerNr < 0 || playerNr >= MAX_GAMEPADS)
					return null;
				
				// init the pad and return it
				pad = gamepads[playerNr];
				pad.deviceId = deviceId;
				pad.isConnected = true;
				
				return pad;
			}
		}
		finally
		{
			// release resources
			if(cursor != null)
				cursor.close();
		}
		
		return null;
	}
	
	/***
	 * Pass button down events to the inputsystem
	 */
	@SuppressWarnings("unused")
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		// get the gamepad this input is for if any
		Gamepad pad = GetOrCreateGamepadByDeviceId(event.getDeviceId());
		
		// check if it deviceId hasn't been set properly
		if(pad.deviceId == INVALID_INPUT_DEVICE_ID)
			pad.deviceId = event.getDeviceId();
		
		if(pad != null)
			return pad.OnKeyDown(keyCode, event);
		
		return false;
	}

	/***
	 * Pass button up events to the inputsystem
	 */
	@SuppressWarnings("unused")
	public boolean onKeyUp(int keyCode, KeyEvent event) 
	{
		// get the gamepad this input is for if any
		Gamepad pad = GetOrCreateGamepadByDeviceId(event.getDeviceId());
		
		// check if it deviceId hasn't been set properly
		if(pad.deviceId == INVALID_INPUT_DEVICE_ID)
			pad.deviceId = event.getDeviceId();
		
		if(pad != null)
			return pad.OnKeyUp(keyCode, event);
		
		return false;
	}

	/***
	 * Pass motion events to the inputsystem
	 */
	@SuppressWarnings("unused")
	public boolean onGenericMotionEvent(MotionEvent event) 
	{
		// get the gamepad this input is for if any
		Gamepad pad = GetOrCreateGamepadByDeviceId(event.getDeviceId());
		
		// check if it deviceId hasn't been set properly
		if(pad.deviceId == INVALID_INPUT_DEVICE_ID)
			pad.deviceId = event.getDeviceId();
		
		if(pad != null)
			return pad.OnGenericMotionEvent(event);
		
		return false;
	}
	
	/**
	 * Handles the connection and disconnection of gamepads
	 */
	private class GamepadConnectionHandler extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			// get the action
			String action = intent.getAction();
			
			// if a controller connected
			if (action.equals(OUYA_CONTROLLER_ADDED_ACTION)) 
			{
			    int deviceId = intent.getIntExtra(EXTRA_GAMEPAD_DEVICE_ID, INVALID_INPUT_DEVICE_ID);
			    int playerNum = intent.getIntExtra(EXTRA_GAMEPAD_PLAYER_NUM, INVALID_INPUT_DEVICE_ID);
			
			    // error check
			    if ((playerNum < 0) || (playerNum >= MAX_GAMEPADS)) 
			    {
			      return;
			    }
			    
			    // init controller
			    Gamepad pad = gamepads[playerNum];
			    if(pad == null)
			    	pad = new Gamepad(deviceId);
			    else
			    	pad.deviceId = deviceId;
			    
			    pad.isConnected = true;
			    
			    // if we have a listener notify
			    if(connectionListener != null)
			    {
			    	connectionListener.OnGamepadConnected(playerNum);
			    }
			}
			// if a controller disconnected
			else if (action.equals(OUYA_CONTROLLER_REMOVED_ACTION)) 
			{
				int deviceId = intent.getIntExtra(EXTRA_GAMEPAD_DEVICE_ID, INVALID_INPUT_DEVICE_ID);
				
				// deinit controller
				Gamepad pad = GetGamepadByDeviceId(deviceId);
				
				if(pad != null)
				{
					pad.deviceId = INVALID_INPUT_DEVICE_ID;
					pad.isConnected = false;
					pad.Clear();
				
				    // if we have a listener notify
				    if(connectionListener != null)
				    {
				    	connectionListener.OnGamepadDisconnected(pad.playerIndex);
				    }
				}
			}
		} // end onReceive
	} // end GamepadConnectionHandler
}
