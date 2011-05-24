package net.mandaria.radioreddit.tasks;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import net.mandaria.radioreddit.RadioRedditApplication;
import android.app.Application;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;


public class GetCurrentSongInformationTask extends AsyncTask<Void, Integer, Integer> 
{
	private static String TAG = "RadioReddit";
	private Context _context;
	private Locale _locale;
	private Application _application;
	private Exception ex;
	
	

    public GetCurrentSongInformationTask(Application application, Context context, Locale locale) 
    {
    	_context = context;
    	_locale = locale;
    	_application = application;
    }

	@Override
	protected Integer doInBackground(Void... unused) 
	{
		int adRefreshRate = -1;
		try 
		{
			
            
            Log.e(TAG, "New ad refresh rate: " + adRefreshRate);
		}
		catch(Exception e)
		{
			ex = e;
			Log.e(TAG, "FAIL: New ad refresh rate: " + e);
		}
		
		return adRefreshRate;
	}

	@Override
	protected void onProgressUpdate(Integer... item) 
	{

	}

	@Override
	protected void onPostExecute(Integer result) 
	{
		RadioRedditApplication appState = ((RadioRedditApplication)_application);
		if(result != -1)
		{
			 Log.e(TAG, "Post execute: " + result);
			//Settings.setAdRefreshRate(_context, result);
		}
		else
		{
			Log.e(TAG, "FAIL: Post execute: " + result);
		}
		
		if(ex != null)
			Log.e(TAG, "FAIL: EXCEPTION: Post execute: " + ex);
		
	}
}
