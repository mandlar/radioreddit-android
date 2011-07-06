package net.mandaria.radioreddit.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.net.Uri;

public class HTTPUtil
{

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
		// conn.setRequestProperty("User-Agent", Utils.USER_AGENT);

		// Account a = Account.getActiveAccount(c); // Account is just a class to hold the reddit login cookie
		// if(a != null)
		// {
		// conn.setRequestProperty("Cookie", "reddit_session=" + a.redditSession); // I tried sending cookies in other ways but none worked on Android 2.1 or 2.2 except this
		// }

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
		// conn.setRequestProperty("User-Agent", Utils.USER_AGENT);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", Integer.toString(post.length()));

		// Account a = Account.getActiveAccount(c);
		// if(a != null)
		// {
		// conn.setRequestProperty("Cookie", "reddit_session=" + a.redditSession);
		// }

		OutputStream os = conn.getOutputStream();
		os.write(post.toString().getBytes());

		String output = HTTPUtil.slurp(conn.getInputStream());
		conn.getInputStream().close();

		return output;
	}
}
