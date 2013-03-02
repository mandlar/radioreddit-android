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

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.apis.RadioRedditAPI;
import net.mandaria.radioreddit.data.SongListExpandableListAdapter;
import net.mandaria.radioreddit.objects.RadioSong;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GetTopChartTask extends AsyncTask<Void, List<RadioSong>, List<RadioSong>>
{
	private static String TAG = "RadioReddit";
	private Context _context;
	private RadioRedditApplication _application;
	private Exception ex;
	private String _type = "";
	private View _fragmentView;

	public GetTopChartTask(RadioRedditApplication application, Context context, View fragmentView, String type)
	{		
		_context = context;
		_fragmentView = fragmentView;
		_application = application;
		_type = type;
		
		LinearLayout div_PleaseWait_SongList = (LinearLayout)_fragmentView.findViewById(R.id.div_PleaseWait_SongList);
		if(div_PleaseWait_SongList != null)
		{
			div_PleaseWait_SongList.setVisibility(View.VISIBLE);
			
			LinearLayout div_Error_SongList = (LinearLayout)_fragmentView.findViewById(R.id.div_Error_SongList);
			div_Error_SongList.setVisibility(View.GONE);
			
			LinearLayout div_SongList = (LinearLayout)_fragmentView.findViewById(R.id.div_SongList);
			div_SongList.setVisibility(View.GONE);
		}
	}

	@Override
	protected List<RadioSong> doInBackground(Void... unused)
	{
		List<RadioSong> songs = null;
		try
		{
			songs = RadioRedditAPI.GetTopChartsByType(_context, _application, _type);
		}
		catch(Exception e)
		{
			ex = e;
			Log.e(TAG, "FAIL: getting top chart for " + _type + ": " + e);
		}

		return songs;
	}

	@Override
	protected void onProgressUpdate(List<RadioSong>... item)
	{

	}

	@Override
	protected void onPostExecute(List<RadioSong> result)
	{
		Activity activity = (Activity)_context;
		if(result != null)// && _errorMessage.equals("")) // TODO: do we need errormessage?
		{					
			//Toast.makeText(_context, "Received " + _type + " list data!", Toast.LENGTH_LONG).show();
			
			drawResultsToActivity(result, activity, _application, _fragmentView);
		}
		else
		{
			LinearLayout div_PleaseWait_SongList = (LinearLayout)_fragmentView.findViewById(R.id.div_PleaseWait_SongList);
			if(div_PleaseWait_SongList != null)
			{
				div_PleaseWait_SongList.setVisibility(View.GONE);
				
				LinearLayout div_Error_SongList = (LinearLayout)_fragmentView.findViewById(R.id.div_Error_SongList);
				div_Error_SongList.setVisibility(View.VISIBLE);
				
				LinearLayout div_SongList = (LinearLayout)_fragmentView.findViewById(R.id.div_SongList);
				div_SongList.setVisibility(View.GONE);
			}
		    
			Log.e(TAG, "FAIL: Post execute");//: " + _errorMessage);
		}

		if(ex != null)
			Log.e(TAG, "FAIL: EXCEPTION: Post execute: " + ex);

	}
	
	public static void drawResultsToActivity(List<RadioSong> result, Activity activity, RadioRedditApplication application, View fragmentView)
	{
		LinearLayout div_PleaseWait_SongList = (LinearLayout)fragmentView.findViewById(R.id.div_PleaseWait_SongList);
		if(div_PleaseWait_SongList != null)
		{
			div_PleaseWait_SongList.setVisibility(View.GONE);
			
			LinearLayout div_Error_SongList = (LinearLayout)fragmentView.findViewById(R.id.div_Error_SongList);
			div_Error_SongList.setVisibility(View.GONE);
			
			LinearLayout div_SongList = (LinearLayout)fragmentView.findViewById(R.id.div_SongList);
			div_SongList.setVisibility(View.VISIBLE);
		
			final ExpandableListView list_SongList = (ExpandableListView)fragmentView.findViewById(R.id.list_SongList);
		
			SongListExpandableListAdapter adapter = new SongListExpandableListAdapter(activity, application, result);
			list_SongList.setAdapter(adapter);
			
			list_SongList.setGroupIndicator(null);
	        
	        TextView lbl_NoSongList = (TextView)fragmentView.findViewById(R.id.lbl_NoSongList);
	       
	        lbl_NoSongList.setText(activity.getString(R.string.no_top_charts_found));
	  
	        if(list_SongList.getCount() == 0)
	        {
	        	lbl_NoSongList.setVisibility(View.VISIBLE);
	        }
	        else
	        {
	        	lbl_NoSongList.setVisibility(View.INVISIBLE);
	        }			
	        
	        list_SongList.setOnGroupExpandListener(new OnGroupExpandListener()
			{
				private int lastGroupExpand = -1;
				
				@Override
				public void onGroupExpand(int groupPosition)
				{
					if(lastGroupExpand != -1 && lastGroupExpand != groupPosition)
						list_SongList.collapseGroup(lastGroupExpand);
					
					lastGroupExpand = groupPosition;
						
					// TODO Auto-generated method stub
					
				}
			});
		}
	}
}
