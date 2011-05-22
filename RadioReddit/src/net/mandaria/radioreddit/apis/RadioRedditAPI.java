package net.mandaria.radioreddit.apis;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.objects.RadioStream;
import net.mandaria.radioreddit.utils.HTTPUtil;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

public class RadioRedditAPI
{
	public static void GetStreams(Context context, RadioRedditApplication application)
	{
		try
		{
			String url = context.getString(R.string.radio_reddit_streams);
	        JSONTokener tokener = new JSONTokener(HTTPUtil.get(context, url));
	        JSONObject json = new JSONObject(tokener);
	
	        JSONObject streams = json.getJSONObject("streams");
	        JSONArray streams_names = streams.names();
	        ArrayList<RadioStream> radiostreams = new ArrayList<RadioStream>();
	
	        // loop through each stream
	        for(int i = 0; i < streams.length(); i++) 
	        {
	        	String name = streams_names.getString(i);
	        	JSONObject stream = streams.getJSONObject(name);
	        	
	        	RadioStream radiostream = new RadioStream();
	        	radiostream.Name = name;
	        	radiostream.Description = stream.getString("description");
	        	radiostream.Status = stream.getString("status");
	        	
	        	// call status.json to get Relay
	        	// form url radioreddit.com + status + json
	        	String status_url = context.getString(R.string.radio_reddit_base_url) + radiostream.Status + context.getString(R.string.radio_reddit_status);
	        	JSONTokener status_tokener = new JSONTokener(HTTPUtil.get(context, status_url));
		        JSONObject status_json = new JSONObject(status_tokener);
	        	
	        	radiostream.Relay = status_json.getString("relay");
	            
	            radiostreams.add(radiostream);
	        }
	        
	        // JSON parsing reverses the list for some reason, fixing it...
	        Collections.reverse(radiostreams);
	       
	        application.RadioStreams = radiostreams;
	        
	        application.CurrentStream = radiostreams.get(0);
		}
		catch(Exception ex)
		{
			// We fail to get the streams...
			Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
		}
 
	}
}
