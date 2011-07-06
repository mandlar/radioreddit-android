package net.mandaria.radioreddit.tasks;

import java.util.Locale;

import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.apis.RadioRedditAPI;
import net.mandaria.radioreddit.objects.RadioStreams;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class GetRadioStreamsTask extends AsyncTask<Void, RadioStreams, RadioStreams>
{
	private static String TAG = "RadioReddit";
	private Context _context;
	private Locale _locale;
	private RadioRedditApplication _application;
	private Exception ex;

	public GetRadioStreamsTask(RadioRedditApplication application, Context context, Locale locale)
	{
		_context = context;
		_locale = locale;
		_application = application;
	}

	@Override
	protected RadioStreams doInBackground(Void... unused)
	{
		RadioStreams streams = null;
		try
		{
			streams = RadioRedditAPI.GetStreams(_context, _application);
		}
		catch(Exception e)
		{
			ex = e;
			Log.e(TAG, "FAIL: get current song information: " + e);
		}

		return streams;
	}

	@Override
	protected void onProgressUpdate(RadioStreams... item)
	{

	}

	@Override
	protected void onPostExecute(RadioStreams result)
	{
		if(result != null && result.ErrorMessage.equals(""))
		{
			_application.RadioStreams = result.RadioStreams;

			if(_application.CurrentStream == null)
				_application.CurrentStream = result.RadioStreams.get(0);
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
