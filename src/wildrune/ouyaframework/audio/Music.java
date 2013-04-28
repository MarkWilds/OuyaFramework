package wildrune.ouyaframework.audio;

import android.content.res.AssetFileDescriptor;

public class Music 
{
	public AssetFileDescriptor musicFd;
	
	public Music(AssetFileDescriptor fd)
	{
		this.musicFd = fd;
	}
}
