/*
 *	radio reddit for android: mobile app to listen to radioreddit.com
 *  Copyright (C) 2011 Bryan Denny
 *  
 *  This file is part of "radio reddit for android"
 *
 *  "radio reddit for android" is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  "radio reddit for android" is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with "radio reddit for android".  If not, see <http://www.gnu.org/licenses/>.
 */

package net.mandaria.radioreddit.tasks;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.activities.RadioReddit;
import net.mandaria.radioreddit.activities.Settings;
import net.mandaria.radioreddit.apis.RedditAPI;
import net.mandaria.radioreddit.objects.RedditAccount;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class LoginRedditTask extends AsyncTask<Void, RedditAccount, RedditAccount>
{
	private static String TAG = "RadioReddit";
	private static Context _context;
	private RadioRedditApplication _application;
	private Exception ex;
	private String _username;
	private String _password;
	private static ProgressDialog _progressDialog;

	public LoginRedditTask(RadioRedditApplication application, Context context, String username, String password)
	{
		_context = context;
		_application = application;
		_username = username;
		_password = password;
		showProgressDialog();
	}
	
	public void showProgressDialog() {
        FragmentManager fragmentManager = ((SherlockFragmentActivity)_context).getSupportFragmentManager();
        ProgressDialogFragment newFragment = new ProgressDialogFragment();
        newFragment.show(fragmentManager, "Dialog");
    }

	public static class ProgressDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            _progressDialog = ProgressDialog.show(_context, _context.getText(R.string.loggingInToReddit), _context.getText(R.string.pleaseWait), true);
            _progressDialog.setCanceledOnTouchOutside(false);
            _progressDialog.setCancelable(false);
            return _progressDialog;
        }
    }

	@Override
	protected RedditAccount doInBackground(Void... unused)
	{
		RedditAccount account = null;
		try
		{
			//android.os.SystemClock.sleep(7000); // for testing
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
		if(_progressDialog.isShowing())
			_progressDialog.dismiss();
		
		if(result != null && result.ErrorMessage.equals(""))
		{
			//Toast.makeText(_context, "SUCCESS: yum, cookies: " + result.Cookie, Toast.LENGTH_LONG).show();
			
			// Save to database (preferences)
			Settings.setRedditAccount(_context, result);
			
			Toast.makeText(_context, _context.getString(R.string.loggedInAs) + " " + result.Username, Toast.LENGTH_LONG).show();
			
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
			    builder.setMessage(_context.getString(R.string.error) + ": " + result.ErrorMessage)
			    	.setTitle(_context.getString(R.string.loginError))
			    	.setIcon(android.R.drawable.ic_dialog_alert)
			    	.setCancelable(true)
			        .setPositiveButton(_context.getString(R.string.ok), null);
			    
			    final AlertDialog alert = builder.create();
			    alert.show();
			    
				Log.e(TAG, "FAIL: Post execute: " + result.ErrorMessage);
			}
		}

		if(ex != null)
			Log.e(TAG, "FAIL: EXCEPTION: Post execute: " + ex);

	}
}
