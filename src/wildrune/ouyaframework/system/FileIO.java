package wildrune.ouyaframework.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import android.content.Context;
import android.content.res.AssetManager;

/***
 * Handles android file IO
 * @author Mark van der Wal
 *
 */
public class FileIO 
{
	private Context mContext;
	
	/***
	 * default constructor
	 * @param context the android context to use
	 */
	public FileIO(Context context)
	{
		mContext = context;
	}
	
	/***
	 * Reads a text file from the assets fully
	 * @param file the file path to load from
	 * @return the fully loaded string
	 */
	public String ReadTextFromAssets(String file) throws RuntimeException
	{
		AssetManager assets = mContext.getAssets();
		StringBuilder strBody = new StringBuilder();
		BufferedReader fileReader = null;
		String nextLine;
		
		// try to load the file
		try {
			fileReader = new BufferedReader(new InputStreamReader( assets.open(file), "UTF-8" ) );
			
			// read all lines and append to the stringbuilder
			while( (nextLine = fileReader.readLine()) != null)
			{
				strBody.append(nextLine);
				strBody.append('\n');
			}
			
			fileReader.close();
		}
		catch(IOException e) {
			throw new RuntimeException("Could not load in a text file!" );
		}
		
		return strBody.toString();
	}
}
