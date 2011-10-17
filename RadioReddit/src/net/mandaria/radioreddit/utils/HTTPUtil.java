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

package net.mandaria.radioreddit.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import net.mandaria.radioreddit.activities.Settings;
import net.mandaria.radioreddit.objects.RedditAccount;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.net.Uri;

public class HTTPUtil
{
	
	public static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android; radio reddit for Android; Mandaria Software)";

	public static String slurp(InputStream in) throws IOException
	{
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for(int n; (n = in.read(b)) != -1;)
		{
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}

	// gets http output from URL
	public static String get(Context c, String url) throws ClientProtocolException, IOException
	{
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setRequestProperty("User-Agent", HTTPUtil.USER_AGENT);

		RedditAccount account = Settings.getRedditAccount(c);
		if(account != null)
		{
			conn.setRequestProperty("Cookie", "reddit_session=" + account.Cookie);
		}

		String output = HTTPUtil.slurp(conn.getInputStream());
		conn.getInputStream().close();

		return output;
	}

	// posts data to http, gets output from URL
	public static String post(Context c, String url, List<NameValuePair> params) throws ClientProtocolException, IOException
	{
		StringBuilder post = new StringBuilder(); // Using the built in method to post a List<NameValuePair> fails on Android 2.1 / 2.2.... manually post the data
		for(NameValuePair p : params)
		{
			post.append(Uri.encode(p.getName()) + "=" + Uri.encode(p.getValue()) + "&");
		}
		post.deleteCharAt(post.length() - 1); // Remove trailing &

		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setDoOutput(true);
		conn.setRequestProperty("User-Agent", HTTPUtil.USER_AGENT);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", Integer.toString(post.length()));

		RedditAccount account = Settings.getRedditAccount(c);
		if(account != null)
		{
			conn.setRequestProperty("Cookie", "reddit_session=" + account.Cookie);
		}

		OutputStream os = conn.getOutputStream();
		os.write(post.toString().getBytes());

		String output = HTTPUtil.slurp(conn.getInputStream());
		conn.getInputStream().close();

		return output;
	}
}
