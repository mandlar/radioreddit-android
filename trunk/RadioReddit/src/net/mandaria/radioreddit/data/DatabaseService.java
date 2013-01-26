package net.mandaria.radioreddit.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import net.mandaria.radioreddit.data.DatabaseHelper;
import net.mandaria.radioreddit.objects.RadioEpisode;
import net.mandaria.radioreddit.objects.RadioSong;
import net.mandaria.radioreddit.objects.RadioStream;

public class DatabaseService
{
	
	private static String TAG = "RadioReddit";

	public DatabaseService()
	{
		
	}
	
	// TODO:  This should probably be inside of the databasehelper as a factory?
	private DatabaseHelper GetDatabaseHelper(Context context)
	{
		DatabaseHelper myDbHelper = new DatabaseHelper(context);
        
        return myDbHelper;
	}
	
	public void testDatabsase(Context context) throws Exception
	{
		DatabaseHelper helper = new DatabaseHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		if(db == null)
			throw new Exception("Null DB");
	}
	
	public ArrayList<RadioStream> GetCachedStreams(Context context)
	{
		DatabaseHelper myDbHelper = GetDatabaseHelper(context);
		
		SQLiteDatabase db = myDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM StreamsCache ORDER BY Name ASC", null);
		
		ArrayList<RadioStream> cachedStreams = new ArrayList<RadioStream>();
		
		while(cursor.moveToNext())
		{
			String name = cursor.getString(cursor.getColumnIndex("Name"));
			String type = cursor.getString(cursor.getColumnIndex("Type"));
			String description = cursor.getString(cursor.getColumnIndex("Description"));
			String status = cursor.getString(cursor.getColumnIndex("Status"));
			String relay = cursor.getString(cursor.getColumnIndex("Relay"));
			boolean online = (cursor.getInt(cursor.getColumnIndex("Online")) == 1);
	    	
	    	RadioStream stream = new RadioStream();
	    	stream.Name = name;
	    	stream.Type = type;
	    	stream.Description = description;
	    	stream.Status = status;
	    	stream.Relay = relay;
	    	stream.Online = online;
			
	    	cachedStreams.add(stream);
		}
		
		cursor.close();
		db.close();
		
		return cachedStreams;
	}	
	
	public void UpdateCachedStreams(Context context, ArrayList<RadioStream>streams)
	{
		DatabaseHelper myDbHelper = GetDatabaseHelper(context);
		
		// Delete all cached streams		
		SQLiteDatabase db = myDbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM StreamsCache");
		
		// loop through and insert each stream 
		for(RadioStream stream : streams)
		{
			ContentValues values = new ContentValues();
			values.put("Name", stream.Name);
			values.put("Type", stream.Type);
			values.put("Description", stream.Description);
			values.put("Status", stream.Status);
			values.put("Relay", stream.Relay);
			values.put("Online", stream.Online);
			db.insert("StreamsCache", null, values);
		}
		
		db.close();
	}
	
	public void AddRecentlyPlayedSong(Context context, RadioSong song)
	{
		DatabaseHelper myDbHelper = GetDatabaseHelper(context);
			
		SQLiteDatabase db = myDbHelper.getWritableDatabase();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentDateTime = sdf.format(new Date());
		
		ContentValues values = new ContentValues();
		values.put("ListenDate", currentDateTime);
		values.put("Type", "Song");
		values.put("id", song.ID); // radio reddit id
		values.put("Title", song.Title);
		values.put("Artist", song.Artist);
		values.put("Redditor", song.Redditor);
		values.put("Genre", song.Genre);
		values.put("Reddit_title", song.Reddit_title);
		values.put("Reddit_url", song.Reddit_url);
		values.put("Preview_url", song.Preview_url);
		values.put("Download_url", song.Download_url);
		values.put("Bandcamp_link", song.Bandcamp_link);
		values.put("Bandcamp_art", song.Bandcamp_art);
		values.put("Itunes_link", song.Itunes_link);
		values.put("Itunes_art", song.Itunes_art);
		values.put("Itunes_price", song.Itunes_price);
		values.put("Name", song.Name);
		values.put("EpisodeTitle", "");
		values.put("EpisodeDescription", "");
		values.put("EpisodeKeywords", "");
		values.put("ShowTitle", "");
		values.put("ShowHosts", "");
		values.put("ShowRedditors", "");
		values.put("ShowGenre", "");
		values.put("ShowFeed", "");
		db.insert("RecentlyPlayed", null, values);
		
		db.close();
	}
	
	public void AddRecentlyPlayedEpisode(Context context, RadioEpisode episode)
	{
		DatabaseHelper myDbHelper = GetDatabaseHelper(context);
			
		SQLiteDatabase db = myDbHelper.getWritableDatabase();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentDateTime = sdf.format(new Date());
		
		ContentValues values = new ContentValues();
		values.put("ListenDate", currentDateTime);
		values.put("Type", "Episode");
		values.put("id", episode.ID); // radio reddit id
		values.put("Title", "");
		values.put("Artist", "");
		values.put("Redditor", "");
		values.put("Genre", "");
		values.put("Reddit_title", episode.Reddit_title);
		values.put("Reddit_url", episode.Reddit_url);
		values.put("Preview_url", episode.Preview_url);
		values.put("Download_url", episode.Download_url);
		values.put("Bandcamp_link", "");
		values.put("Bandcamp_art", "");
		values.put("Itunes_link", "");
		values.put("Itunes_art", "");
		values.put("Itunes_price", "");
		values.put("Name", episode.Name);
		values.put("EpisodeTitle", episode.EpisodeTitle);
		values.put("EpisodeDescription", episode.EpisodeDescription);
		values.put("EpisodeKeywords", episode.EpisodeKeywords);
		values.put("ShowTitle", episode.ShowTitle);
		values.put("ShowHosts", episode.ShowHosts);
		values.put("ShowRedditors", episode.ShowRedditors);
		values.put("ShowGenre", episode.ShowGenre);
		values.put("ShowFeed", episode.ShowFeed);
		db.insert("RecentlyPlayed", null, values);
		
		db.close();
	}
}
