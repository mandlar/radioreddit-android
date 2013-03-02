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

package net.mandaria.radioreddit.fragments;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.tasks.GetRecentlyPlayedEpisodesTask;
import net.mandaria.radioreddit.tasks.GetRecentlyPlayedSongsTask;
import net.mandaria.radioreddit.tasks.GetTopChartTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;


public class SongListFragment extends SherlockFragment {
	
	private String _type = "";

	public SongListFragment()
	{
	
	}
	
	public SongListFragment(String type)
	{
		_type = type;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.songlist, container, false);
	}

	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setUserVisibleHint(true);
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		if(_type.equals("all") || _type.equals("month") || _type.equals("week") || _type.equals("day"))
		{
			// TODO: cache results to memory?
			//	if(application.TopCharts[_type] == null)
			new GetTopChartTask((RadioRedditApplication)getActivity().getApplication(), getActivity(), getView(), _type).execute();
			//  else
			//		GetTopChartTask.drawResultsToActivity(
		}
		else if(_type.equals("recentlyplayed_songs"))
		{
			new GetRecentlyPlayedSongsTask((RadioRedditApplication)getActivity().getApplication(), getActivity(), getView(), _type).execute();
		}
		else if(_type.equals("recentlyplayed_episodes"))
		{
			new GetRecentlyPlayedEpisodesTask((RadioRedditApplication)getActivity().getApplication(), getActivity(), getView(), _type).execute();
		}
		
		Button btn_TryAgain_SongList = (Button)getView().findViewById(R.id.btn_TryAgain_SongList);
		btn_TryAgain_SongList.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(_type.equals("all") || _type.equals("month") || _type.equals("week") || _type.equals("day"))
				{
					new GetTopChartTask((RadioRedditApplication)getActivity().getApplication(), getActivity(), getView(), _type).execute();
				}
				else if(_type.equals("recentlyplayed_songs"))
				{
					new GetRecentlyPlayedSongsTask((RadioRedditApplication)getActivity().getApplication(), getActivity(), getView(), _type).execute();
				}
				else if(_type.equals("recentlyplayed_episodes"))
				{
					new GetRecentlyPlayedEpisodesTask((RadioRedditApplication)getActivity().getApplication(), getActivity(), getView(), _type).execute();
				}
			}
		});
	}
}