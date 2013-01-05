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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
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
			String url = context.getString(R.string.reddit_login) + "/" + username;
			
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
	public static String Vote(Context context, RedditAccount account, int voteDirection, String fullname)
	{
		String errorMessage = ""; 
		
		try
		{
			try
			{
				account.Modhash = updateModHash(context);
				
				if(account.Modhash == null)
				{
					errorMessage = context.getString(R.string.error_ThereWasAProblemVotingPleaseTryAgain);
					return errorMessage;
				}
			}
			catch (Exception ex)
			{
				errorMessage = ex.getMessage();
				return errorMessage;
			}
			
			String url = context.getString(R.string.reddit_vote);
			
			// post values
			ArrayList<NameValuePair> post_values = new ArrayList<NameValuePair>();
			
			BasicNameValuePair id = new BasicNameValuePair("id", fullname);
			post_values.add(id);
			
			BasicNameValuePair dir = new BasicNameValuePair("dir", Integer.toString(voteDirection));
			post_values.add(dir);
			
			// not required
			//BasicNameValuePair r = new BasicNameValuePair("r", "radioreddit"); // TODO: shouldn't be hard coded, could be talkradioreddit
			//post_values.add(r);
			
			BasicNameValuePair uh = new BasicNameValuePair("uh", account.Modhash);
			post_values.add(uh);
			
			BasicNameValuePair api_type = new BasicNameValuePair("api_type", "json");
			post_values.add(api_type);
			
			String outputVote = HTTPUtil.post(context, url, post_values);
			
			JSONTokener reddit_vote_tokener = new JSONTokener(outputVote);
			JSONObject reddit_vote_json = new JSONObject(reddit_vote_tokener);
			
			if(reddit_vote_json.has("json"))
			{
				JSONObject json = reddit_vote_json.getJSONObject("json");
			
				if (json.has("errors") && json.getJSONArray("errors").length() > 0)
			    {
					String error = json.getJSONArray("errors").getJSONArray(0).getString(1);
					
					errorMessage = error;
			    }
			}
			// success!
		}
		catch(Exception ex)
		{
			// We fail to vote...
			CustomExceptionHandler ceh = new CustomExceptionHandler(context);
			ceh.sendEmail(ex);

			ex.printStackTrace();
			
			errorMessage = ex.toString();
		}
		
		return errorMessage;
	}
	
	// 1:subreddit 2:threadId 3:commentId
	// The following commented-out one is good, but tough to get right, e.g.,
	// http://www.reddit.com/eorhm vs. http://www.reddit.com/prefs, mobile, store, etc.
	// So, for now require the captured URLs to have /comments or /tb prefix.
//	public static final String COMMENT_PATH_PATTERN_STRING
//		= "(?:/r/([^/]+)/comments|/comments|/tb)?/([^/]+)(?:/?$|/[^/]+/([a-zA-Z0-9]+)?)?";
	public static final String COMMENT_PATH_PATTERN_STRING
		= "(?:/r/([^/]+)/comments|/comments|/tb)/([^/]+)(?:/?$|/[^/]+/([a-zA-Z0-9]+)?)?";
	
	   // Group 1: Subreddit. Group 2: thread id (no t3_ prefix)
    private final static Pattern NEW_THREAD_PATTERN = Pattern.compile(COMMENT_PATH_PATTERN_STRING);
    // Group 1: whole error. Group 2: the time part
    private final static Pattern RATELIMIT_RETRY_PATTERN = Pattern.compile("(you are doing that too much. try again in (.+?)\\.)");
	// Group 1: Subreddit
    private final static Pattern SUBMIT_PATH_PATTERN = Pattern.compile("/(?:r/([^/]+)/)?submit/?");
    
    //(\[16, 17, "call", \["(.*?)"\]\])
    private final static Pattern CAPTCHA_PATTERN = Pattern.compile("\\[16, 17, \"call\", \\[\"(.*?)\"\\]\\]");
	
	// url = the url of the page being submitted 
	// title = the text that the user submits as the link's title
	// subreddit = the subreddit the post is being submitted to
	public static String SubmitLink(Context context, RedditAccount account, String title, String url, String subreddit, String iden, String captcha)
	{
		String errorMessage = ""; 
		
		try
		{
			try
			{
				account.Modhash = updateModHash(context);
				
				if(account.Modhash == null)
				{
					errorMessage = context.getString(R.string.error_ThereWasAProblemSubmittingPleaseTryAgain);
					return errorMessage;
				}
			}
			catch (Exception ex)
			{
				errorMessage = ex.getMessage();
				return errorMessage;
			}
			
			String posturl = context.getString(R.string.reddit_submit);
			
			// post values
			ArrayList<NameValuePair> post_values = new ArrayList<NameValuePair>();
			
			BasicNameValuePair postTitle = new BasicNameValuePair("title", title);
			post_values.add(postTitle);
			
			BasicNameValuePair postUrl = new BasicNameValuePair("url", url);
			post_values.add(postUrl);
			
			BasicNameValuePair sr = new BasicNameValuePair("sr", subreddit);
			post_values.add(sr);
			
			BasicNameValuePair kind = new BasicNameValuePair("kind", "link");
			post_values.add(kind);
			
			BasicNameValuePair uh = new BasicNameValuePair("uh", account.Modhash);
			post_values.add(uh);
			
			if(!iden.equals("") && !captcha.equals(""))
			{
				BasicNameValuePair postIden = new BasicNameValuePair("iden", iden);
				post_values.add(postIden);
				
				BasicNameValuePair postCaptcha = new BasicNameValuePair("captcha", captcha);
				post_values.add(postCaptcha);
			}
			// json doesn't work for this call
			//BasicNameValuePair api_type = new BasicNameValuePair("api_type", "json");
			//post_values.add(api_type);
			
			String outputSubmit = HTTPUtil.post(context, posturl, post_values);
			
			// returns shitty jquery not json
			if(outputSubmit.equals(""))
				return context.getString(R.string.error_NoContentReturnFromReplyPOST);
			
			if(outputSubmit.contains("WRONG_PASSWORD"))
				return context.getString(R.string.error_WrongPassword);
			
			if(outputSubmit.contains("USER_REQUIRED"))
				return context.getString(R.string.error_UserRequired);
			
			if(outputSubmit.contains("SUBREDDIT_NOEXIST"))
				return context.getString(R.string.error_ThatSubredditDoesNotExist);
			
			if(outputSubmit.contains("SUBREDDIT_NOTALLOWED"))
				return context.getString(R.string.error_YouAreNotAllowedToPostToThatSubreddit);
			
			String newId = "";
			String newSubreddit = "";
			Matcher idMatcher = NEW_THREAD_PATTERN.matcher(outputSubmit);
			if(idMatcher.find())
			{
				newSubreddit = idMatcher.group(1);
				newId = idMatcher.group(2);
			}
			else
			{
				if(outputSubmit.contains("ALREADY_SUB"))
					return context.getString(R.string.error_SorrySomeoneAlreadySubmitted);
				
				if(outputSubmit.contains("RATELIMIT"))
				{
					// Try to find the number of minutes using regex
					Matcher rateMatcher = RATELIMIT_RETRY_PATTERN.matcher(outputSubmit);
					if(rateMatcher.find())
						return rateMatcher.group(1);
					else
						return context.getString(R.string.error_YouAreTryingToSubmitTooFast);
				}
				
				if(outputSubmit.contains("BAD_CAPTCHA"))	
				{
					Matcher captchaMatcher = CAPTCHA_PATTERN.matcher(outputSubmit);
					if(captchaMatcher.find())
						return "CAPTCHA:" + captchaMatcher.group(1);
					else
						return context.getString(R.string.error_BadCAPTCHATryAgain);
				}
				
				if(outputSubmit.contains("verify your email"))
					return context.getString(R.string.error_EmailNotVerified);
				// TODO: somehow add link to: http://www.reddit.com/verify?reason=submit
				// could check for this error message on the other end and add a button that opens the link in the browser
				
				return context.getString(R.string.error_NoIdReturnedByReplyPOST);
			}
			// success!
		}
		catch(Exception ex)
		{
			// We fail to submit...
			CustomExceptionHandler ceh = new CustomExceptionHandler(context);
			ceh.sendEmail(ex);

			ex.printStackTrace();
			
			errorMessage = ex.toString();
		}
		
		return errorMessage;
	}
	
	// updateModHash
	public static String updateModHash(Context context)
	{
		// Calls me.json to get the current modhash for the user
		String output = "";
		boolean errorGettingModHash = false;
		
		try
		{
			try
			{
				output = HTTPUtil.get(context, context.getString(R.string.reddit_me));
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				errorGettingModHash = true;
				// For now, not used. It is acceptable to error out and not alert the user
				// radiosong.ErrorMessage = "Unable to connect to reddit";//context.getString(R.string.error_RadioRedditServerIsDownNotification);
			}
			
			if(!errorGettingModHash && output.length() > 0)
			{
				JSONTokener reddit_me_tokener = new JSONTokener(output);
				JSONObject reddit_me_json = new JSONObject(reddit_me_tokener);
	
				JSONObject data = reddit_me_json.getJSONObject("data");
				
				String modhash = data.getString("modhash");
				
				return modhash;
			}
			else
			{
				return null;
			}
		}
		catch(Exception ex)
		{
//			CustomExceptionHandler ceh = new CustomExceptionHandler(context);
//			ceh.sendEmail(ex);

			ex.printStackTrace();
			
			return null;
		}
	}
}
