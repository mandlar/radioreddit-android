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
import net.mandaria.radioreddit.activities.Login;
import net.mandaria.radioreddit.apis.RadioRedditAPI;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.flurry.android.FlurryAgent;

public class VoteRedditTask extends AsyncTask<Void, String, String>
{
	private static String TAG = "RadioReddit";
	private Context _context;
	private RadioRedditApplication _application;
	private Exception ex;
	private boolean _liked;
	private String _iden;
	private String _captcha;
	//private ProgressDialog _progressDialog;

	public VoteRedditTask(RadioRedditApplication application, Context context, boolean liked, String iden, String captcha)
	{
		_context = context;
		_application = application;
		_liked = liked;
		_iden = iden;
		_captcha = captcha;
		//_progressDialog = ProgressDialog.show(_context, "Voting on currently playing...", "Please wait...", true);
		if(liked)
		{
			if(_application.CurrentSong != null)
				_application.CurrentSong.Likes = "true";
			if(_application.CurrentEpisode != null)
				_application.CurrentEpisode.Likes = "true";
		}
		else
		{
			if(_application.CurrentSong != null)
				_application.CurrentSong.Likes = "false";
			if(_application.CurrentEpisode != null)
				_application.CurrentEpisode.Likes = "false";
		}
	}

	@Override
	protected String doInBackground(Void... unused)
	{
		String errorMessage = "";
		try
		{
			errorMessage = RadioRedditAPI.VoteOnCurrentlyPlaying(_context, _application, _liked, _iden, _captcha);
		}
		catch(Exception e)
		{
			ex = e;
			Log.e(TAG, "FAIL: voting on currently playing: " + e);
		}

		return errorMessage;
	}

	@Override
	protected void onProgressUpdate(String... item)
	{

	}

	@Override
	protected void onPostExecute(String result)
	{
		//_progressDialog.dismiss();
		if(result != null && result.equals(""))
		{
			//Toast.makeText(_context, "Successfully voted!", Toast.LENGTH_LONG).show();
		}
		else
		{
			if(result != null)
			{
				// check for CAPTCHA: <captcha>
				// download the captcha, show a new dialog requiring the user to enter it
				// then resubmit
				if(result.contains("CAPTCHA:"))
				{
					String captcha = result.substring(8);
					
					new GetCaptchaTask(_application, _context, "", null, null, captcha, _liked).execute();
				}
				else
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(_context);
				    builder.setMessage(_context.getString(R.string.error) + ": " + result)
				    	.setTitle(_context.getString(R.string.voting_error))
				    	.setIcon(android.R.drawable.ic_dialog_alert)
				    	.setCancelable(true)
				        .setPositiveButton(_context.getString(R.string.ok), null);
				    
				    if(result.equals(_context.getString(R.string.error_YouMustBeLoggedInToVote)))
				    {
				    	builder.setTitle(_context.getString(R.string.login));
				    	builder.setMessage(result);
				    	builder.setNegativeButton(_context.getString(R.string.login), new OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								FlurryAgent.onEvent("radio reddit - Vote Error Dialog - Login");
								_context.startActivity(new Intent(_context, Login.class));
							}
						});
				    }
				    
				    final AlertDialog alert = builder.create();
				    alert.show();
				}
				
			    if(_application.CurrentSong != null)
					_application.CurrentSong.Likes = "null";
				if(_application.CurrentEpisode != null)
					_application.CurrentEpisode.Likes = "null";
			    
				Log.e(TAG, "FAIL: Post execute: " + result);
			}
		}

		if(ex != null)
			Log.e(TAG, "FAIL: EXCEPTION: Post execute: " + ex);

	}
}
