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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.apis.RadioRedditAPI;
import net.mandaria.radioreddit.objects.RadioEpisode;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class SendExceptionEmailTask extends AsyncTask<Void, Void, Void>
{
	private static String TAG = "RadioReddit";
	private Context _context;
	private String _stacktrace;
	private String _debug; 
	private String _application;

	private Exception ex;

	public SendExceptionEmailTask(Context context, String stacktrace, String debug, String application)
	{
		_context = context;
		_stacktrace = stacktrace;
		_debug = debug;
		_application = application;

	}

	// Posts debug info to website, which then sends a debug email to developer
	@Override
	protected Void doInBackground(Void... unused)
	{
		String url = "http://www.bryandenny.com/software/BugReport.aspx";
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("stacktrace", _stacktrace));
		nvps.add(new BasicNameValuePair("debug", _debug));
		nvps.add(new BasicNameValuePair("application", _application));
		try
		{
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			httpClient.execute(httpPost);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
