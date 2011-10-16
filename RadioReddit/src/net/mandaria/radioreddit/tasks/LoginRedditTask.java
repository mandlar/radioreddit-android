package net.mandaria.radioreddit.tasks;

import java.util.Locale;

import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.activities.Login;
import net.mandaria.radioreddit.activities.RadioReddit;
import net.mandaria.radioreddit.apis.RadioRedditAPI;
import net.mandaria.radioreddit.apis.RedditAPI;
import net.mandaria.radioreddit.objects.RadioEpisode;
import net.mandaria.radioreddit.objects.RedditAccount;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
	private ProgressDialog _progressDialog;

	public LoginRedditTask(RadioRedditApplication application, Context context, String username, String password)
	{
		_context = context;
		_application = application;
		_username = username;
		_password = password;
		_progressDialog = ProgressDialog.show(_context, "Logging in to reddit", "Please wait...", true);
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
		_progressDialog.dismiss();
		if(result != null && result.ErrorMessage.equals(""))
		{
			Toast.makeText(_context, "SUCCESS: yum, cookies: " + result.Cookie, Toast.LENGTH_LONG).show();
			
			// TODO: save user name, cookie, and maybe modhash to database here
			// also want to have it in a global object
			
			// Go back to main activity
			Intent intent = new Intent(_context, RadioReddit.class);
        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	_context.startActivity(intent);
		}
		else
		{
			if(result != null)
			{
				final AlertDialog.Builder builder = new AlertDialog.Builder(_context);
			    builder.setMessage("Error: " + result.ErrorMessage)
			    	.setTitle("Login Error")
			    	.setIcon(android.R.drawable.ic_dialog_alert)
			    	.setCancelable(true)
			        .setPositiveButton("OK", null);
			    
			    final AlertDialog alert = builder.create();
			    alert.show();
			    
				Log.e(TAG, "FAIL: Post execute: " + result.ErrorMessage);
			}
		}

		if(ex != null)
			Log.e(TAG, "FAIL: EXCEPTION: Post execute: " + ex);

	}
}
