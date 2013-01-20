package net.mandaria.radioreddit.data;

import java.util.List;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.objects.RadioSong;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
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
		View row = convertView;

		// A ViewHolder keeps references to children views to avoid unneccessary calls
		// to findViewById() on each row.
		final TopChartChildViewHolder holder;
		// When convertView is not null, we can reuse it directly, there is no need
		// to reinflate it. We only inflate a new View when the convertView supplied
		// by ListView is null.

		if (row == null) 
		{
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = vi.inflate(R.layout.topchart_child, parent, false);
			
			// Creates a ViewHolder and store references to the two children views
			// we want to bind data to.
			holder = new TopChartChildViewHolder();
			
			holder.btn_upvote = (Button) row.findViewById(R.id.btn_upvote);
			holder.btn_downvote = (Button) row.findViewById(R.id.btn_downvote);
			holder.btn_play = (Button) row.findViewById(R.id.btn_play);
			
			row.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (TopChartChildViewHolder) row.getTag();
		}
		
		RadioSong song = songs.get(groupPosition);

		// Bind the data efficiently with the holder.		
		setUpOrDownVote(song.Likes, holder);
		
		holder.btn_upvote.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Vote task
				setUpOrDownVote("true", holder);
				
			}
		});
		
		holder.btn_downvote.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Vote task
				setUpOrDownVote("false", holder);
				
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
		TopChartParentViewHolder holder;
		// When convertView is not null, we can reuse it directly, there is no need
		// to reinflate it. We only inflate a new View when the convertView supplied
		// by ListView is null.

		if (row == null) 
		{
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = vi.inflate(R.layout.topchart_parent, parent, false);
			
			// Creates a ViewHolder and store references to the two children views
			// we want to bind data to.
			holder = new TopChartParentViewHolder();
			
			holder.lbl_SongName = (TextView) row.findViewById(R.id.lbl_SongName);
			holder.lbl_SongArtist = (TextView) row.findViewById(R.id.lbl_SongArtist);
			row.setTag(holder);
		} 
		else 
		{
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (TopChartParentViewHolder) row.getTag();
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
	
	private void setUpOrDownVote(String vote, TopChartChildViewHolder holder)
	{
		if(!vote.equals("null"))
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
	}
	
	static class TopChartParentViewHolder {
		TextView lbl_SongName;
		TextView lbl_SongArtist;
	}
	
	static class TopChartChildViewHolder {
		Button btn_upvote;
		Button btn_downvote;
		Button btn_play;
	}

}
