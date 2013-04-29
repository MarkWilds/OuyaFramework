package wildrune.ouyaframework.audio;

import wildrune.ouyaframework.OuyaGameActivity;
import wildrune.ouyaframework.utils.interfaces.IDisposable;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.SoundPool;

public class AudioSystem implements IDisposable
{
	private final static int MAX_SOUNDS = 20;
	
	private OuyaGameActivity game;
	private SoundPool soundPool;
	private MediaPlayer mediaPlayer;
	
	private boolean isPaused;
	
	public AudioSystem(OuyaGameActivity game)
	{
		this.game = game;
		isPaused = false;
	}
	
	/**
	 * Create this system
	 */
	public void Create()
	{
		soundPool = new SoundPool(MAX_SOUNDS, AudioManager.STREAM_MUSIC, 0);
		mediaPlayer = new MediaPlayer();
		
		// create callback for music prepare
		mediaPlayer.setOnPreparedListener(new AudioPreparedListener());
	}
	
	/**
	 * Load a sound
	 * @param fileName
	 * @return
	 */
	public Sound LoadSound(String fileName)
	{
		AssetFileDescriptor fd = game.gameFileIO.ReadFdFromAssets(fileName);
		int soundId = soundPool.load(fd, 1);
		
		return new Sound(soundId, this);
	}
	
	/**
	 * Stream music
	 * @param fileName
	 * @return
	 */
	public Music LoadMusic(String fileName)
	{
		AssetFileDescriptor fd = game.gameFileIO.ReadFdFromAssets(fileName);
		return new Music(fd);
	}
	
	@Override
	public void Dispose()
	{
		soundPool.release();
		soundPool = null;
		
		if(mediaPlayer.isPlaying())
			mediaPlayer.stop();
		
		mediaPlayer.release();
		mediaPlayer = null;
		
		game = null;
	}
	
	public void Release(Sound sound)
	{
		soundPool.unload(sound.soundId);
	}
	
	// is looping
	public boolean IsLooping()
	{
		return mediaPlayer.isLooping();
	}
	
	public boolean IsPaused()
	{
		return isPaused;
	}
	
	// is playing
	public boolean IsPlaying()
	{
		return mediaPlayer.isPlaying();
	}
	
	// play sound
	public void Play(Sound sound, float volume)
	{
		this.soundPool.play(sound.soundId, volume, volume, 1, 0, 1);
	}
	
	// play music
	public void Play(Music music)
	{
		if(isPaused)
		{
			isPaused = false;
			mediaPlayer.start();
			return;
		}
		
		if(mediaPlayer.isPlaying())
		{
			mediaPlayer.stop();
		}
		
		try 
		{
			// set data source
			AssetFileDescriptor musicFd = music.musicFd;
			mediaPlayer.setDataSource( musicFd.getFileDescriptor(), 
					musicFd.getStartOffset(),
					musicFd.getLength() );
			
			// prepare async
			mediaPlayer.prepareAsync();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	// pause music
	public void Pause()
	{
		if(mediaPlayer.isPlaying())
		{
			isPaused = true;
			mediaPlayer.pause();
		}
	}
	
	// stop music
	public void Stop()
	{
		mediaPlayer.stop();
	}
	
	// set looping
	public void SetLooping(boolean loop)
	{
		mediaPlayer.setLooping(loop);
	}
	
	// set music volume
	public void SetVolume(float vol)
	{
		mediaPlayer.setVolume(vol, vol);
	}
	
	/**
	 * Listens for when music is ready to play
	 */
	private class AudioPreparedListener implements OnPreparedListener
	{
		@Override
		public void onPrepared(MediaPlayer mp) 
		{	
			mp.start();
		}
	}
}
