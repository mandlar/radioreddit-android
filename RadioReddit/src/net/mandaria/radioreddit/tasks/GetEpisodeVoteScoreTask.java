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

import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.apis.RadioRedditAPI;
import net.mandaria.radioreddit.data.EpisodeListExpandableListAdapter;
import net.mandaria.radioreddit.data.EpisodeListExpandableListAdapter.SongListChildViewHolder;
import net.mandaria.radioreddit.objects.RadioEpisode;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class GetEpisodeVoteScoreTask extends AsyncTask<Void, RadioEpisode, RadioEpisode>
{
	private static String TAG = "RadioReddit";
	private Context _context;
	private RadioRedditApplication _application;
	private Exception ex;
	private RadioEpisode _episode;
	private SongListChildViewHolder _holder;
	private List<RadioEpisode> _episodes;
	private int _groupPosition;

	public GetEpisodeVoteScoreTask(RadioRedditApplication application, Context context, List<RadioEpisode> episodes, int groupPosition, RadioEpisode episode, SongListChildViewHolder holder)
	{
		_context = context;
		_application = application;
		_episode = episode;
		_holder = holder;
		_episodes = episodes;
		_groupPosition = groupPosition;
	}

	@Override
	protected RadioEpisode doInBackground(Void... unused)
	{
		RadioEpisode episode = null;
		try
		{
			episode = RadioRedditAPI.GetEpisodeVoteScore(_context, _application, _episode);
		}
		catch(Exception e)
		{
			ex = e;
			Log.e(TAG, "FAIL: get current song information: " + e);
		}

		return episode;
	}

	@Override
	protected void onProgressUpdate(RadioEpisode... item)
	{

	}

	@Override
	protected void onPostExecute(RadioEpisode result)
	{
		if(result != null && result.ErrorMessage.equals(""))
		{
			if(_episodes != null) // used for TopCharts/RecentlyPlayed/etc.
			{
				_episodes.set(_groupPosition, result);
			
				EpisodeListExpandableListAdapter.setUpOrDownVote(result.Likes, _holder);
			}
			else // used for PlaybackService
				_application.CurrentEpisode = result;
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
