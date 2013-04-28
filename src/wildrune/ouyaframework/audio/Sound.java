package wildrune.ouyaframework.audio;

public class Sound 
{
	public int soundId;
	private Audio audio;

	public Sound(int id, Audio audio)
	{
		this.soundId = id;
		this.audio = audio;
	}
	
	public void Play(float volume)
	{
		audio.Play(this, volume);
	}
	
	public void Release()
	{
		audio.Release(this);
	}
}
