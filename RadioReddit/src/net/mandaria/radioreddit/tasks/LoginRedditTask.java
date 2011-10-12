package net.mandaria.radioreddit.tasks;

import java.util.Locale;

import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.apis.RadioRedditAPI;
import net.mandaria.radioreddit.apis.RedditAPI;
import net.mandaria.radioreddit.objects.RadioEpisode;
import net.mandaria.radioreddit.objects.RedditAccount;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class LoginRedditTask extends AsyncTask<Void, RedditAccount, RedditAccount>
{
	private static String TAG = "RadioReddit";
	private Context _context;
	private RadioRedditApplication _application;
	private Exception ex;
	private String _username;
	private String _password;

	public LoginRedditTask(RadioRedditApplication application, Context context, String username, String password)
	{
		_context = context;
		_application = application;
		_username = username;
		_password = password;
	}

	@Override
	protected RedditAccount doInBackground(Void... unused)
	{
		RedditAccount account = null;
		try
		{
			account = RedditAPI.login(_context, _username, _password);
		}
		catch(Exception e)
		{
			ex = e;
			Log.e(TAG, "FAIL: logging into reddit: " + e);
		}

		return account;
	}

	@Override
	protected void onProgressUpdate(RedditAccount... item)
	{

	}

	@Override
	protected void onPostExecute(RedditAccount result)
	{

		if(result != null && result.ErrorMessage.equals(""))
		{
			Toast.makeText(_context, "SUCCESS: yum, cookies: " + result.Cookie, Toast.LENGTH_LONG).show();
			
			// TODO: save user name, cookie, and maybe modhash to database here
			// also want to have it in a global object
			
//			if(_application.CurrentStream.Name.equals(_startingStream)) // make sure we're on the same stream)
//				_application.CurrentEpisode = result;
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
