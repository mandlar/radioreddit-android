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

import java.util.ArrayList;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.objects.RadioStream;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomRadioStreamsAdapter extends ArrayAdapter<RadioStream>
{
	private static String TAG = "RadioReddit";
	private int layout;
	Context context;
	ArrayList<RadioStream> streams;

	public CustomRadioStreamsAdapter(Context context, int layout, ArrayList<RadioStream> streams)
	{
		super(context, layout, streams);
		this.layout = layout;
		this.context = context;
		this.streams = streams;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if(convertView == null)
		{
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(layout, null);
		}

		RadioStream stream = streams.get(position);

		// Name of stream
		TextView lbl_stream_name = (TextView) convertView.findViewById(R.id.lbl_stream_name);
		lbl_stream_name.setText(stream.Name);

		// URL of stream
		TextView lbl_stream_url = (TextView) convertView.findViewById(R.id.lbl_stream_url);
		lbl_stream_url.setText(stream.Relay);

		return convertView;
	}

}
