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

package net.mandaria.radioreddit.tasks;

import java.util.List;
import java.util.Locale;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.apis.RadioRedditAPI;
import net.mandaria.radioreddit.data.SongListExpandableListAdapter;
import net.mandaria.radioreddit.data.SongListExpandableListAdapter.SongListChildViewHolder;
import net.mandaria.radioreddit.objects.RadioSong;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class GetSongVoteScoreTask extends AsyncTask<Void, RadioSong, RadioSong>
{
	private static String TAG = "RadioReddit";
	private Context _context;
	private RadioRedditApplication _application;
	private Exception ex;
	private RadioSong _song;
	private SongListChildViewHolder _holder;
	private List<RadioSong> _songs;
	private int _groupPosition;

	public GetSongVoteScoreTask(RadioRedditApplication application, Context context, List<RadioSong> songs, int groupPosition, RadioSong song, SongListChildViewHolder holder)
	{
		_context = context;
		_application = application;
		_song = song;
		_holder = holder;
		_songs = songs;
		_groupPosition = groupPosition;
	}

	@Override
	protected RadioSong doInBackground(Void... unused)
	{
		RadioSong song = null;
		try
		{
			song = RadioRedditAPI.GetSongVoteScore(_context, _application, _song);
		}
		catch(Exception e)
		{
			ex = e;
			Log.e(TAG, "FAIL: get current song information: " + e);
		}

		return song;
	}

	@Override
	protected void onProgressUpdate(RadioSong... item)
	{

	}

	@Override
	protected void onPostExecute(RadioSong result)
	{
		if(result != null && result.ErrorMessage.equals(""))
		{
			if(_songs != null) // used for TopCharts/RecentlyPlayed/etc.
			{
				_songs.set(_groupPosition, result);
			
				SongListExpandableListAdapter.setUpOrDownVote(result.Likes, _holder);
			}
			else // used for PlaybackService
				_application.CurrentSong = result;
		}
		else
		{
			if(result != null)
			{
				Toast.makeText(_context, result.ErrorMessage, Toast.LENGTH_LONG).show();
				Log.e(TAG, "FAIL: Post execute: " + result.ErrorMessage);
			}
		}

		if(ex != null)
			Log.e(TAG, "FAIL: EXCEPTION: Post execute: " + ex);

	}
	
	
}
