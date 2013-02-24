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

package net.mandaria.radioreddit.activities;

import java.util.HashMap;
import java.util.Map;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.merge.MergeAdapter;
import com.flurry.android.FlurryAgent;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.data.CustomRadioStreamsAdapter;
import net.mandaria.radioreddit.objects.RadioStream;
import net.mandaria.radioreddit.objects.RadioStreams;
import net.mandaria.radioreddit.utils.ActivityUtil;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SelectStation extends SherlockActivity
{
	@Override
	public void onStart()
	{
	   super.onStart();
	   FlurryAgent.onStartSession(this, getString(R.string.flurrykey));
	}
	
	@Override
	public void onStop()
	{
	   super.onStop();
	   FlurryAgent.onEndSession(this);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		super.onCreate(savedInstanceState);
		
		ActivityUtil.SetKeepScreenOn(this);
		
		setContentView(R.layout.selectstation);

		RadioRedditApplication application = (RadioRedditApplication) getApplication();

		if(application.CurrentStream != null)
			getSupportActionBar().setTitle(getString(R.string.currentStation)+ ": " + application.CurrentStream.Name);

		ListView list_Stations = (ListView) findViewById(R.id.list_Stations);
		CustomRadioStreamsAdapter adapter_music = new CustomRadioStreamsAdapter(this, R.layout.radio_stream_item, RadioStreams.getMusicStreams(application.RadioStreams));
		CustomRadioStreamsAdapter adapter_talk = new CustomRadioStreamsAdapter(this, R.layout.radio_stream_item, RadioStreams.getTalkStreams(application.RadioStreams));
		
		MergeAdapter mergedAdapter = new MergeAdapter();
		mergedAdapter.addView(getHeaderView(getString(R.string.musicStations)), false);
		mergedAdapter.addAdapter(adapter_music);
		mergedAdapter.addView(getHeaderView(getString(R.string.talkStations)), false);
		mergedAdapter.addAdapter(adapter_talk);

		list_Stations.setAdapter(mergedAdapter); // TODO: there is a null pointer exception here if we run out of memory and return to this activity on low memory

		list_Stations.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView parent, View view, int position, long id)
			{
				Intent result = new Intent();
				
				TextView lbl_stream_name = (TextView)view.findViewById(R.id.lbl_stream_name);
				
				String streamName = lbl_stream_name.getText().toString();

				RadioRedditApplication application = (RadioRedditApplication) getApplication();

				RadioStream newStream = RadioStreams.getStreamByStreamName(application.RadioStreams, streamName);

				boolean changedStream = false;
				if(!application.CurrentStream.Name.equals(newStream.Name) || !application.playBackType.equals("stream"))
					changedStream = true;

				result.putExtra("changed_stream", changedStream);

				application.CurrentStream = newStream;
				
				Map params = new HashMap();
				params.put("station", application.CurrentStream.Name);
				params.put("changed stream", changedStream);
				FlurryAgent.onEvent("select station - station selected", params);

				setResult(Activity.RESULT_OK, result);
				finish();
			}
		});
	}
	
	private View getHeaderView(String headerCaption)
	{
		TextView textView = (TextView)getLayoutInflater().inflate(R.layout.tmp_lv_separator, null);
		textView.setText(headerCaption);
		
		return textView;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
				// app icon in Action Bar (Android 3.0) clicked; return result
				Intent result = new Intent();
				setResult(Activity.RESULT_CANCELED, result);
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

}
