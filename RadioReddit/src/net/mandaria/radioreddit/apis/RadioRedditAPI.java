package net.mandaria.radioreddit.apis;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.objects.RadioStream;
import net.mandaria.radioreddit.utils.HTTPUtil;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

public class RadioRedditAPI
{
	public static void GetStreams(Context context)
	{
		try
		{
			String url = context.getString(R.string.radio_reddit_streams);
	        JSONTokener tokener = new JSONTokener(HTTPUtil.get(context, url));
	        JSONObject json = new JSONObject(tokener);
	        int length = json.length();
	
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
	        	radiostream.Server = stream.getString("server");
	        	radiostream.Status = stream.getString("status");
	            
	            radiostreams.add(radiostream);
	        }
	        
	        //TODO: do something with the radiostreams object
		}
		catch(Exception ex)
		{
			Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
		}
 
	}
}
