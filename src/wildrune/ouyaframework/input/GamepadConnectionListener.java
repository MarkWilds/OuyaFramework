package wildrune.ouyaframework.input;

public interface GamepadConnectionListener 
{
	public void OnGamepadConnected(int playerIndex);
	public void OnGamepadDisconnected(int playerIndex);
}
