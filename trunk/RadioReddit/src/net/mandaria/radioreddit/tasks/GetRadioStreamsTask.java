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

import java.util.Locale;

import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.apis.RadioRedditAPI;
import net.mandaria.radioreddit.data.DatabaseService;
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
			
			// Update cache of streams
			DatabaseService service = new DatabaseService(); 
			service.UpdateCachedStreams(_context, result.RadioStreams);

			if(_application.CurrentStream == null)
				_application.CurrentStream = result.RadioStreams.get(0); // TODO: this needs to be set to a specific stream, e.g. main
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
