package net.mandaria.radioreddit.tasks;

import java.util.Locale;

import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.apis.RadioRedditAPI;
import net.mandaria.radioreddit.objects.RadioEpisode;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class GetCurrentEpisodeInformationTask extends AsyncTask<Void, RadioEpisode, RadioEpisode>
{
	private static String TAG = "RadioReddit";
	private Context _context;
	private Locale _locale;
	private RadioRedditApplication _application;
	private Exception ex;
	private String _startingStream;

	public GetCurrentEpisodeInformationTask(RadioRedditApplication application, Context context, Locale locale)
	{
		_context = context;
		_locale = locale;
		_application = application;
		_startingStream = application.CurrentStream.Name;
	}

	@Override
	protected RadioEpisode doInBackground(Void... unused)
	{
		RadioEpisode episode = null;
		try
		{
			episode = RadioRedditAPI.GetCurrentEpisodeInformation(_context, _application);
		}
		catch(Exception e)
		{
			ex = e;
			Log.e(TAG, "FAIL: get current episode information: " + e);
		}

		return episode;
	}

	@Override
	protected void onProgressUpdate(RadioEpisode... item)
	{

	}

	@Override
	protected void onPostExecute(RadioEpisode result)
	{

		if(result != null && result.ErrorMessage.equals(""))
		{
			if(_application.CurrentStream.Name.equals(_startingStream)) // make sure we're on the same stream)
				_application.CurrentEpisode = result;
		}
		else
		{
			if(result != null)
			{
				Toast.makeText(_context, result.ErrorMessage, Toast.LENGTH_LONG).show();
				Log.e(TAG, "FAIL: Post execute: " + result.ErrorMessage);
			}
		}

		if(ex != null)
			Log.e(TAG, "FAIL: EXCEPTION: Post execute: " + ex);

	}
}
