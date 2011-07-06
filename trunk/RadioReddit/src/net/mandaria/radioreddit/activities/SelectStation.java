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

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.data.CustomRadioStreamsAdapter;
import net.mandaria.radioreddit.objects.RadioStream;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SelectStation extends Activity
{
	private int sdkVersion = 0;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		try
		{
			sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		}
		catch(NumberFormatException e)
		{

		}

		// Disable title on phones, enable action bar on tablets
		if(sdkVersion < 11)
			requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.selectstation);

		RadioRedditApplication application = (RadioRedditApplication) getApplication();

		if(sdkVersion >= 11)
		{
			if(application.CurrentStream != null)
				getActionBar().setTitle("Current Station: " + application.CurrentStream.Name);
		}

		ListView list_Stations = (ListView) findViewById(R.id.list_Stations);
		CustomRadioStreamsAdapter adapter = new CustomRadioStreamsAdapter(this, R.layout.radio_stream_item, application.RadioStreams);

		list_Stations.setAdapter(adapter); // TODO: there is a null pointer exception here if we run out of memory and return to this activity on low memory

		list_Stations.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView parent, View view, int position, long id)
			{
				Intent result = new Intent();

				RadioRedditApplication application = (RadioRedditApplication) getApplication();

				RadioStream newStream = application.RadioStreams.get(position);

				boolean changedStream = false;
				if(!application.CurrentStream.Name.equals(newStream.Name))
					changedStream = true;

				result.putExtra("changed_stream", changedStream);

				application.CurrentStream = newStream;

				setResult(Activity.RESULT_OK, result);
				finish();
			}
		});
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
