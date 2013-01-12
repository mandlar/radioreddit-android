package net.mandaria.radioreddit.data;

import java.util.ArrayList;
import java.util.List;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.objects.RadioSong;
import net.mandaria.radioreddit.objects.RadioStream;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class TopChartExpandableListAdapter extends BaseExpandableListAdapter
{
	private static String TAG = "RadioReddit";
	Context context;
	List<RadioSong> songs;
	
	public TopChartExpandableListAdapter(Context context, List<RadioSong> songs)
	{
		super();
		this.context = context;
		this.songs = songs;
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
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		if(convertView == null)
		{
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.topchart_child, null);
		}

//		RadioStream stream = streams.get(position);
//
//		// Name of stream
//		TextView lbl_stream_name = (TextView) convertView.findViewById(R.id.lbl_stream_name);
//		lbl_stream_name.setText(stream.Name);
//
//		// URL of stream
//		TextView lbl_stream_url = (TextView) convertView.findViewById(R.id.lbl_stream_url);
//		lbl_stream_url.setText(stream.Relay);

		return convertView;
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
		if(convertView == null)
		{
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.topchart_parent, null);
		}
		
		RadioSong song = songs.get(groupPosition);
		
		TextView lbl_SongName = (TextView) convertView.findViewById(R.id.lbl_SongName);
		lbl_SongName.setText(song.Title);
		
		TextView lbl_SongArtist = (TextView) convertView.findViewById(R.id.lbl_SongArtist);
		lbl_SongArtist.setText(song.Artist + " (" + song.Redditor + ")");

//		RadioStream stream = streams.get(position);
//
//		// Name of stream
//		TextView lbl_stream_name = (TextView) convertView.findViewById(R.id.lbl_stream_name);
//		lbl_stream_name.setText(stream.Name);
//
//		// URL of stream
//		TextView lbl_stream_url = (TextView) convertView.findViewById(R.id.lbl_stream_url);
//		lbl_stream_url.setText(stream.Relay);

		return convertView;
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

}
