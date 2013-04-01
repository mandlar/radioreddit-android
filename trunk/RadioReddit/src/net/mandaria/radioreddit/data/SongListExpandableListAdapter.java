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

import java.util.List;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.apis.RadioRedditAPI;
import net.mandaria.radioreddit.objects.RadioSong;
import net.mandaria.radioreddit.tasks.GetSongVoteScoreTask;
import net.mandaria.radioreddit.tasks.VoteOnSongTask;
import net.mandaria.radioreddit.utils.APIUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

public class SongListExpandableListAdapter extends BaseExpandableListAdapter
{
	private static String TAG = "RadioReddit";
	Context context;
	List<RadioSong> songs;
	RadioRedditApplication application;
	GetSongVoteScoreTask task;
	
	public SongListExpandableListAdapter(Context context, RadioRedditApplication application, List<RadioSong> songs)
	{
		super();
		this.context = context;
		this.songs = songs;
		this.application = application;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		if(task != null)
			task.cancel(true);
		
		View row = convertView;

		// A ViewHolder keeps references to children views to avoid unneccessary calls
		// to findViewById() on each row.
		final SongListChildViewHolder holder;
		// When convertView is not null, we can reuse it directly, there is no need
		// to reinflate it. We only inflate a new View when the convertView supplied
		// by ListView is null.

		if (row == null) 
		{
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = vi.inflate(R.layout.songlist_child, parent, false);
			
			// Creates a ViewHolder and store references to the two children views
			// we want to bind data to.
			holder = new SongListChildViewHolder();
			
			holder.btn_upvote = (Button) row.findViewById(R.id.btn_upvote);
			holder.btn_downvote = (Button) row.findViewById(R.id.btn_downvote);
			holder.btn_play = (Button) row.findViewById(R.id.btn_play);
			holder.btn_download = (Button) row.findViewById(R.id.btn_download);
			
			row.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (SongListChildViewHolder) row.getTag();
		}
		
		holder.btn_upvote.setBackgroundResource(R.drawable.willupvote_button);
		holder.btn_downvote.setBackgroundResource(R.drawable.willdownvote_button);
		holder.btn_upvote.setEnabled(false);
		holder.btn_downvote.setEnabled(false);
		
		final RadioSong song = songs.get(groupPosition);
		if(song.Likes == null)
		{
			task = new GetSongVoteScoreTask(application, context, songs, groupPosition, song, holder);
			task.execute();
		}
		else
		{
			setUpOrDownVote(song.Likes, holder);
		}
		
		if (!APIUtil.isDownloadManagerAvailable(context))
		{
			holder.btn_download.setVisibility(View.GONE);
		}
		else if(song != null)
		{
			if(song.Download_url != null)
			{
				holder.btn_download.setVisibility(View.VISIBLE);
			}
			else
			{
				holder.btn_download.setVisibility(View.GONE);
			}
		}
		
		// Bind the data efficiently with the holder.		
		//setUpOrDownVote(song.Likes, holder);
		
		
		holder.btn_upvote.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				new VoteOnSongTask(application, context, song, true, "", "").execute();
				setUpOrDownVote("true", holder);
				song.Likes = null; // Forces user to re-get song info next time to reflect new vote
				songs.set(groupPosition, song);
				
			}
		});
		
		holder.btn_downvote.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				new VoteOnSongTask(application, context, song, false, "", "").execute();
				setUpOrDownVote("false", holder);
				song.Likes = null; // Forces user to re-get song info next time to reflect new vote
				songs.set(groupPosition, song);
			}
		});
		
		holder.btn_play.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				song.Playlist = "Song Preview";
				application.playBackType = "song";
				application.CurrentSong = song;
				application.CurrentEpisode = null;
				
				Intent result = new Intent();
				result.putExtra("song_url", song.Preview_url);
				
				Activity activity = (Activity)context;
				
				activity.setResult(Activity.RESULT_OK, result);
				activity.finish();
			}
		});
		
		holder.btn_download.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				final AlertDialog.Builder builder = new AlertDialog.Builder(context);
			    builder.setMessage(context.getString(R.string.download_body_song))
			    	.setTitle(context.getString(R.string.download_title))
			    	.setIcon(android.R.drawable.ic_dialog_alert)
			    	.setCancelable(true)
			        .setPositiveButton(context.getString(R.string.download_yes), new DialogInterface.OnClickListener()
					{
						
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							FlurryAgent.onEvent("radio reddit - Song List - Download");
							
							RadioRedditAPI.Download(context, song, null);
						}
					})
			        .setNegativeButton(context.getString(R.string.download_no), null);
			    
			    final AlertDialog alert = builder.create();
			    alert.show();
			}
		});
		
		return row;

	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public int getGroupCount()
	{
		// TODO Auto-generated method stub
		return songs.size();
	}

	@Override
	public long getGroupId(int groupPosition)
	{
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		View row = convertView;

		// A ViewHolder keeps references to children views to avoid unneccessary calls
		// to findViewById() on each row.
		SongListParentViewHolder holder;
		// When convertView is not null, we can reuse it directly, there is no need
		// to reinflate it. We only inflate a new View when the convertView supplied
		// by ListView is null.

		if (row == null) 
		{
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = vi.inflate(R.layout.songlist_parent, parent, false);
			
			// Creates a ViewHolder and store references to the two children views
			// we want to bind data to.
			holder = new SongListParentViewHolder();
			
			holder.lbl_SongName = (TextView) row.findViewById(R.id.lbl_SongName);
			holder.lbl_SongArtist = (TextView) row.findViewById(R.id.lbl_SongArtist);
			row.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (SongListParentViewHolder) row.getTag();
		}
		
		RadioSong song = songs.get(groupPosition);

		// Bind the data efficiently with the holder.
		holder.lbl_SongName.setText(song.Title);
		holder.lbl_SongArtist.setText(song.Artist + " (" + song.Redditor + ")");

		return row;
	}

	@Override
	public boolean hasStableIds()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	static class SongListParentViewHolder {
		TextView lbl_SongName;
		TextView lbl_SongArtist;
	}
	
	public static class SongListChildViewHolder {
		public Button btn_upvote;
		public Button btn_downvote;
		Button btn_play;
		Button btn_download;
		
	}
	
	public static void setUpOrDownVote(String vote, SongListChildViewHolder holder)
	{
		if(vote != null && !vote.equals("null"))
		{
			if(vote.equals("true"))
			{
				holder.btn_upvote.setBackgroundResource(R.drawable.didupvote_button);
				holder.btn_downvote.setBackgroundResource(R.drawable.willdownvote_button);
			}
			else
			{
				holder.btn_upvote.setBackgroundResource(R.drawable.willupvote_button);
				holder.btn_downvote.setBackgroundResource(R.drawable.diddownvote_button);
			}
		}
		else
		{
			holder.btn_upvote.setBackgroundResource(R.drawable.willupvote_button);
			holder.btn_downvote.setBackgroundResource(R.drawable.willdownvote_button);
		}
		
		holder.btn_upvote.setEnabled(true);
		holder.btn_downvote.setEnabled(true);
	}

}
