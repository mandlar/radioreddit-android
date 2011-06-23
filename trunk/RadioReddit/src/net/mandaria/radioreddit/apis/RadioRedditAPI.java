package net.mandaria.radioreddit.apis;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.objects.RadioSong;
import net.mandaria.radioreddit.objects.RadioStream;
import net.mandaria.radioreddit.utils.HTTPUtil;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class RadioRedditAPI
{
	public static void GetStreams(Context context, RadioRedditApplication application)
	{
		try
		{
			String url = context.getString(R.string.radio_reddit_streams);
			String outputStreams = "";
			boolean errorGettingStreams = false;
			
			try
			{
				outputStreams = HTTPUtil.get(context, url);
			}
			catch(Exception ex)
			{
				errorGettingStreams = true;
				// TODO: will need to move to UI thread
				Toast.makeText(context, context.getString(R.string.error_RadioRedditServerIsDownNotification), Toast.LENGTH_LONG).show();
			}
			
			if(!errorGettingStreams)
			{
		        JSONTokener tokener = new JSONTokener(outputStreams);
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
		        	
		        	String outputStatus = "";
		        	boolean errorGettingStatus = false;
					
					try
					{
						outputStatus = HTTPUtil.get(context, status_url);
					}
					catch(Exception ex)
					{
						errorGettingStatus = true;
						// TODO: will need to move to UI thread
						Toast.makeText(context, context.getString(R.string.error_RadioRedditServerIsDownNotification), Toast.LENGTH_LONG).show();
					}
					
					if(!errorGettingStatus)
					{
		        	
			        	JSONTokener status_tokener = new JSONTokener(outputStatus);
				        JSONObject status_json = new JSONObject(status_tokener);
				        
				        radiostream.Online = Boolean.parseBoolean(status_json.getString("online").toLowerCase());				        
				        
				        if(radiostream.Online == true) // if offline, no other nodes are available
				        {
				        	radiostream.Relay = status_json.getString("relay");
				            
				            radiostreams.add(radiostream);
				        }
					}
		        }
		        
		        // JSON parsing reverses the list for some reason, fixing it...
		        if(radiostreams.size() > 0)
		        {
		        	Collections.reverse(radiostreams);
		       
			        application.RadioStreams = radiostreams;
			        
			        if(application.CurrentStream == null)
			        	application.CurrentStream = radiostreams.get(0);
		        }
		        else
		        {
		        	// TODO: will need to move to UI thread
		        	Toast.makeText(context, context.getString(R.string.error_NoStreams), Toast.LENGTH_LONG).show();
		        }
			}
		}
		catch(Exception ex)
		{
			// We fail to get the streams...
			// TODO: will need to move to UI thread
			Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
			ex.printStackTrace();
		}
 
	}
	
	public static RadioSong GetCurrentSongInformation(Context context, RadioRedditApplication application)
	{
		try
		{
			String status_url = context.getString(R.string.radio_reddit_base_url) + application.CurrentStream.Status + context.getString(R.string.radio_reddit_status);
			
			String outputStatus = "";
			boolean errorGettingStatus = false;
			
			try
			{
				outputStatus = HTTPUtil.get(context, status_url);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				errorGettingStatus = true;
				// TODO: must move to UI thread
				//Toast.makeText(context, context.getString(R.string.radioRedditServerIsDownNotification), Toast.LENGTH_LONG).show();
			}
			
			if(!errorGettingStatus)
			{
		        JSONTokener status_tokener = new JSONTokener(outputStatus);
		        JSONObject status_json = new JSONObject(status_tokener);
		        
		        RadioSong radiosong = new RadioSong();
		
		        radiosong.Playlist = status_json.getString("playlist");
		        
		        JSONObject songs = status_json.getJSONObject("songs");
		        JSONArray songs_array = songs.getJSONArray("song");
		        
		        // get the first song in the array
		        JSONObject song = songs_array.getJSONObject(0);
		        radiosong.ID = song.getInt("id");
		        radiosong.Title = song.getString("title");
		        radiosong.Artist = song.getString("artist");
		        radiosong.Redditor = song.getString("redditor");
		        radiosong.Genre = song.getString("genre");
		        radiosong.Reddit_title = song.getString("reddit_title");
		        radiosong.Reddit_url = song.getString("reddit_url");
		        if(song.has("preview_url"))
		        	radiosong.Preview_url = song.getString("preview_url");
		        if(song.has("download_url"))
		        	radiosong.Download_url = song.getString("download_url");
		        if(song.has("bandcamp_link"))
		        	radiosong.Bandcamp_link = song.getString("bandcamp_link");
		        if(song.has("bandcamp_art"))
		        	radiosong.Bandcamp_art = song.getString("bandcamp_art");
		        if(song.has("itunes_link"))
		        	radiosong.Itunes_link = song.getString("itunes_link");
		        if(song.has("itunes_art"))
		        	radiosong.Itunes_art = song.getString("itunes_art");
		        if(song.has("itunes_price"))
		        	radiosong.Itunes_price = song.getString("itunes_price");
		        
		        // get vote score
		        String reddit_info_url = context.getString(R.string.reddit_link_by) + URLEncoder.encode(radiosong.Reddit_url);
		        
		        String outputRedditInfo = "";
				boolean errorGettingRedditInfo = false;
				
				try
				{
					outputRedditInfo = HTTPUtil.get(context, reddit_info_url);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					errorGettingRedditInfo = true;
					// TODO: must move to UI thread
					//Toast.makeText(context, context.getString(R.string.radioRedditServerIsDownNotification), Toast.LENGTH_LONG).show();
				}
				
				if(!errorGettingRedditInfo)
				{
					//Log.e("radio_reddit_test", "Length: " + outputRedditInfo.length());
					//Log.e("radio_reddit_test", "Value: " + outputRedditInfo); // TODO: sometimes the value contains "error: 404", need to check for that
			        JSONTokener reddit_info_tokener = new JSONTokener(outputRedditInfo);
			        JSONObject reddit_info_json = new JSONObject(reddit_info_tokener);
			        
			        JSONObject data = reddit_info_json.getJSONObject("data");
			        
			        // default value of score
			        String score = context.getString(R.string.vote_to_submit_song);
		
		        	JSONArray children_array = data.getJSONArray("children");
		        	
		        	// Song hasn't been submitted yet
		        	if(children_array.length() > 0)
		        	{
		        		JSONObject children = children_array.getJSONObject(0);
		        		
			        	JSONObject children_data = children.getJSONObject("data");		        
			        	score = children_data.getString("score");
		        	}
					
			        radiosong.Score = score;
				}
				else
				{
					radiosong.Score = "?";
				}
		        
		        return radiosong;
			}
			return null;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			// We fail to get the streams...
			// TODO: must move to UI thread
			//Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
			return null;
		}
 
	}
}
