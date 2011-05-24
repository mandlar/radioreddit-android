package net.mandaria.radioreddit.apis;

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
	        	
	        	// TODO: get online status
	        	// if offline, do not add
	            
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
	
	public static RadioSong GetCurrentSongInformation(Context context, RadioRedditApplication application)
	{
		try
		{
			String status_url = context.getString(R.string.radio_reddit_base_url) + application.CurrentStream.Status + context.getString(R.string.radio_reddit_status);	        
	        JSONTokener status_tokener = new JSONTokener(HTTPUtil.get(context, status_url));
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
	        
	        // TODO: get vote score
	        
	        return radiosong;
		}
		catch(Exception ex)
		{
			// We fail to get the streams...
			Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
			return null;
		}
 
	}
}
