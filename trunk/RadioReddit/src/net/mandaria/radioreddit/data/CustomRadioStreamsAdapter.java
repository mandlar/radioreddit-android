package net.mandaria.radioreddit.data;

import java.util.ArrayList;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.objects.RadioStream;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
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
         if (convertView == null) 
         {
        	 LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	 convertView = vi.inflate(layout, null);
         }
    	
    	RadioStream stream = streams.get(position);
    	
    	// Name of stream
    	TextView lbl_stream_name = (TextView)convertView.findViewById(R.id.lbl_stream_name);
    	lbl_stream_name.setText(stream.Name);
    	
    	// URL of stream
    	TextView lbl_stream_url = (TextView)convertView.findViewById(R.id.lbl_stream_url);
    	lbl_stream_url.setText(stream.Relay);

        return convertView;
    }

}


