package net.mandaria.radioreddit.tasks;

import java.util.List;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.apis.RadioRedditAPI;
import net.mandaria.radioreddit.data.DatabaseService;
import net.mandaria.radioreddit.data.TopChartExpandableListAdapter;
import net.mandaria.radioreddit.objects.RadioSong;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class GetRecentlyPlayedSongsTask extends AsyncTask<Void, List<RadioSong>, List<RadioSong>>
{
	private static String TAG = "RadioReddit";
	private Context _context;
	private RadioRedditApplication _application;
	private Exception ex;
	private String _type = "";
	private View _fragmentView;

	public GetRecentlyPlayedSongsTask(RadioRedditApplication application, Context context, View fragmentView, String type)
	{		
		_context = context;
		_fragmentView = fragmentView;
		_application = application;
		_type = type;
		
		LinearLayout div_PleaseWait_TopChart = (LinearLayout)_fragmentView.findViewById(R.id.div_PleaseWait_TopChart);
		if(div_PleaseWait_TopChart != null)
		{
			div_PleaseWait_TopChart.setVisibility(View.VISIBLE);
			
			LinearLayout div_Error_TopChart = (LinearLayout)_fragmentView.findViewById(R.id.div_Error_TopChart);
			div_Error_TopChart.setVisibility(View.GONE);
			
			LinearLayout div_TopChart = (LinearLayout)_fragmentView.findViewById(R.id.div_TopChart);
			div_TopChart.setVisibility(View.GONE);
		}
	}

	@Override
	protected List<RadioSong> doInBackground(Void... unused)
	{
		List<RadioSong> songs = null;
		try
		{
			DatabaseService service = new DatabaseService();
			songs = service.GetRecentlyPlayedSongs(_context);
		}
		catch(Exception e)
		{
			ex = e;
			Log.e(TAG, "FAIL: getting recently played songs for " + _type + ": " + e);
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
			LinearLayout div_PleaseWait_TopChart = (LinearLayout)_fragmentView.findViewById(R.id.div_PleaseWait_TopChart);
			if(div_PleaseWait_TopChart != null)
			{
				div_PleaseWait_TopChart.setVisibility(View.GONE);
				
				LinearLayout div_Error_TopChart = (LinearLayout)_fragmentView.findViewById(R.id.div_Error_TopChart);
				div_Error_TopChart.setVisibility(View.VISIBLE);
				
				LinearLayout div_TopChart = (LinearLayout)_fragmentView.findViewById(R.id.div_TopChart);
				div_TopChart.setVisibility(View.GONE);
			}
		    
			Log.e(TAG, "FAIL: Post execute");//: " + _errorMessage);
		}

		if(ex != null)
			Log.e(TAG, "FAIL: EXCEPTION: Post execute: " + ex);

	}
	
	public static void drawResultsToActivity(List<RadioSong> result, Activity activity, RadioRedditApplication application, View fragmentView)
	{
		LinearLayout div_PleaseWait_TopChart = (LinearLayout)fragmentView.findViewById(R.id.div_PleaseWait_TopChart);
		if(div_PleaseWait_TopChart != null)
		{
			div_PleaseWait_TopChart.setVisibility(View.GONE);
			
			LinearLayout div_Error_TopChart = (LinearLayout)fragmentView.findViewById(R.id.div_Error_TopChart);
			div_Error_TopChart.setVisibility(View.GONE);
			
			LinearLayout div_TopChart = (LinearLayout)fragmentView.findViewById(R.id.div_TopChart);
			div_TopChart.setVisibility(View.VISIBLE);
		
			final ExpandableListView list_TopChart = (ExpandableListView)fragmentView.findViewById(R.id.list_TopChart);
		
			TopChartExpandableListAdapter adapter = new TopChartExpandableListAdapter(activity, application, result);
			list_TopChart.setAdapter(adapter);
			
			list_TopChart.setGroupIndicator(null);
	        
	        TextView lbl_NoTopCharts = (TextView)fragmentView.findViewById(R.id.lbl_NoTopCharts);
	       
	        lbl_NoTopCharts.setText("No recently played songs found");
	  
	        if(list_TopChart.getCount() == 0)
	        {
	        	lbl_NoTopCharts.setVisibility(View.VISIBLE);
	        }
	        else
	        {
	        	lbl_NoTopCharts.setVisibility(View.INVISIBLE);
	        }			
	        
	        list_TopChart.setOnGroupExpandListener(new OnGroupExpandListener()
			{
				private int lastGroupExpand = -1;
				
				@Override
				public void onGroupExpand(int groupPosition)
				{
					if(lastGroupExpand != -1 && lastGroupExpand != groupPosition)
						list_TopChart.collapseGroup(lastGroupExpand);
					
					lastGroupExpand = groupPosition;
						
					// TODO Auto-generated method stub
					
				}
			});
		}
	}
}
