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

package net.mandaria.radioreddit.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import net.mandaria.radioreddit.objects.RadioEpisode;
import net.mandaria.radioreddit.objects.RadioSong;
import net.mandaria.radioreddit.objects.RadioStream;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
		if(streams != null)
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
	
	public ArrayList<RadioSong> GetRecentlyPlayedSongs(Context context)
	{
		DatabaseHelper myDbHelper = GetDatabaseHelper(context);
		
		SQLiteDatabase db = myDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM RecentlyPlayed WHERE Type = 'Song' ORDER BY _id DESC", null);
		
		ArrayList<RadioSong> songs = new ArrayList<RadioSong>();
		
		while(cursor.moveToNext())
		{			
			String listenDate = cursor.getString(cursor.getColumnIndex("ListenDate"));
			int id = cursor.getInt(cursor.getColumnIndex("id"));
			String title = cursor.getString(cursor.getColumnIndex("Title"));
			String artist = cursor.getString(cursor.getColumnIndex("Artist"));
			String redditor = cursor.getString(cursor.getColumnIndex("Redditor"));
			String genre = cursor.getString(cursor.getColumnIndex("Genre"));
			String reddit_title = cursor.getString(cursor.getColumnIndex("Reddit_title"));
			String reddit_url = cursor.getString(cursor.getColumnIndex("Reddit_url"));
			String preview_url = cursor.getString(cursor.getColumnIndex("Preview_url"));
			String download_url = cursor.getString(cursor.getColumnIndex("Download_url"));
			String bandcamp_link = cursor.getString(cursor.getColumnIndex("Bandcamp_link"));
			String bandcamp_art = cursor.getString(cursor.getColumnIndex("Bandcamp_art"));
			String itunes_link = cursor.getString(cursor.getColumnIndex("Itunes_link"));
			String itunes_art = cursor.getString(cursor.getColumnIndex("Itunes_art"));
			String itunes_price = cursor.getString(cursor.getColumnIndex("Itunes_price"));
			String name = cursor.getString(cursor.getColumnIndex("Name"));
	    	
	    	RadioSong song = new RadioSong();
	    	song.ErrorMessage = "";
	    	// TODO: add listen date?
	    	song.ID = id;
	    	song.Title = title;
	    	song.Artist = artist;
	    	song.Redditor = redditor;
	    	song.Genre = genre;
	    	song.Reddit_title = reddit_title;
	    	song.Reddit_url = reddit_url;
	    	song.Preview_url = preview_url;
	    	song.Download_url = download_url;
	    	song.Bandcamp_link = bandcamp_link;
	    	song.Bandcamp_art = bandcamp_art;
	    	song.Itunes_link = itunes_link;
	    	song.Itunes_art = itunes_art;
	    	song.Itunes_price = itunes_price;
	    	song.Name = name;
			
	    	songs.add(song);
		}
		
		cursor.close();
		db.close();
		
		return songs;
	}	
	
	public ArrayList<RadioEpisode> GetRecentlyPlayedEpisodes(Context context)
	{
		DatabaseHelper myDbHelper = GetDatabaseHelper(context);
		
		SQLiteDatabase db = myDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM RecentlyPlayed WHERE Type = 'Episode' ORDER BY _id DESC", null);
		
		ArrayList<RadioEpisode> episodes = new ArrayList<RadioEpisode>();
		
		while(cursor.moveToNext())
		{						
			String listenDate = cursor.getString(cursor.getColumnIndex("ListenDate"));
			int id = cursor.getInt(cursor.getColumnIndex("id"));
			String reddit_title = cursor.getString(cursor.getColumnIndex("Reddit_title"));
			String reddit_url = cursor.getString(cursor.getColumnIndex("Reddit_url"));
			String preview_url = cursor.getString(cursor.getColumnIndex("Preview_url"));
			String download_url = cursor.getString(cursor.getColumnIndex("Download_url"));
			String name = cursor.getString(cursor.getColumnIndex("Name"));
			String episodeTitle = cursor.getString(cursor.getColumnIndex("EpisodeTitle"));
			String episodeDescription = cursor.getString(cursor.getColumnIndex("EpisodeDescription"));
			String episodeKeywords = cursor.getString(cursor.getColumnIndex("EpisodeKeywords"));
			String showTitle = cursor.getString(cursor.getColumnIndex("ShowTitle"));
			String showHosts = cursor.getString(cursor.getColumnIndex("ShowHosts"));
			String showRedditors = cursor.getString(cursor.getColumnIndex("ShowRedditors"));
			String showGenre = cursor.getString(cursor.getColumnIndex("ShowGenre"));
			String showFeed = cursor.getString(cursor.getColumnIndex("ShowFeed"));
	    	
	    	RadioEpisode episode = new RadioEpisode();
	    	episode.ErrorMessage = "";
	    	// TODO: add listen date?
	    	episode.ID = id;
	    	episode.Reddit_title = reddit_title;
	    	episode.Reddit_url = reddit_url;
	    	episode.Preview_url = preview_url;
	    	episode.Download_url = download_url;
	    	episode.Name = name;
	    	episode.EpisodeTitle = episodeTitle;
	    	episode.EpisodeDescription = episodeDescription;
	    	episode.EpisodeKeywords = episodeKeywords;
	    	episode.ShowTitle = showTitle;
	    	episode.ShowHosts = showHosts;
	    	episode.ShowRedditors = showRedditors;
	    	episode.ShowGenre = showGenre;
	    	episode.ShowFeed = showFeed;
			
	    	episodes.add(episode);
		}
		
		cursor.close();
		db.close();
		
		return episodes;
	}	
}
