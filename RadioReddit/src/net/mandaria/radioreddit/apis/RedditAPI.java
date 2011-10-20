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


package net.mandaria.radioreddit.apis;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.json.JSONTokener;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.errors.CustomExceptionHandler;
import net.mandaria.radioreddit.objects.RedditAccount;
import net.mandaria.radioreddit.utils.HTTPUtil;
import android.content.Context;
import android.widget.Toast;

public class RedditAPI
{

	public static RedditAccount login(Context context, String username, String password)
	{
		RedditAccount account = new RedditAccount();
		
		try
		{
			String url = context.getString(R.string.reddit_login_ssl) + "/" + username;
			
			// post values
			ArrayList<NameValuePair> post_values = new ArrayList<NameValuePair>();
			
			BasicNameValuePair user = new BasicNameValuePair("user", username);
			post_values.add(user);
			
			BasicNameValuePair passwd = new BasicNameValuePair("passwd", password);
			post_values.add(passwd);
			
			BasicNameValuePair api_type = new BasicNameValuePair("api_type", "json");
			post_values.add(api_type);
			
			String outputLogin = HTTPUtil.post(context, url, post_values);
			
			JSONTokener reddit_login_tokener = new JSONTokener(outputLogin);
			JSONObject reddit_login_json = new JSONObject(reddit_login_tokener);
			
			JSONObject json = reddit_login_json.getJSONObject("json");
			
			if (json.getJSONArray("errors").length() > 0)
		    {
				String error = json.getJSONArray("errors").getJSONArray(0).getString(1);
				
				account.ErrorMessage = error;
		    }
			else
			{
				JSONObject data = json.getJSONObject("data");
				
				// success!
				String cookie = data.getString("cookie");
				String modhash = data.getString("modhash");
				
				account.Username = username;
				account.Cookie = cookie;
				account.Modhash = modhash;
				account.ErrorMessage = "";
			}
		}
		catch(Exception ex)
		{
			// We fail to get the streams...
			CustomExceptionHandler ceh = new CustomExceptionHandler(context);
			ceh.sendEmail(ex);

			ex.printStackTrace();
			
			account.ErrorMessage = ex.toString();
		}
		
		return account;
	}
	
	// voteDirection = -1 vote down, 0 remove vote, 1 vote up
	// fullname = A base-36 id of the form t[0-9]+_[a-z0-9]+ (e.g. t3_6nw57) that reddit associates with every Thing (post, comment, account)
	public static void Vote(Context context, RedditAccount account, int voteDirection, String fullname)
	{
		
	}
}
