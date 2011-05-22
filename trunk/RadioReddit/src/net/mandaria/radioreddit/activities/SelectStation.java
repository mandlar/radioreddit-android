package net.mandaria.radioreddit.activities;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.R.array;
import net.mandaria.radioreddit.R.id;
import net.mandaria.radioreddit.R.layout;
import net.mandaria.radioreddit.data.CustomRadioStreamsAdapter;
import net.mandaria.radioreddit.objects.RadioStream;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class SelectStation extends Activity 
{
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.selectstation);
		
		RadioRedditApplication application = (RadioRedditApplication)getApplication();
		
		ListView list_Stations = (ListView)findViewById(R.id.list_Stations);
		CustomRadioStreamsAdapter adapter = new CustomRadioStreamsAdapter(this, R.layout.radio_stream_item, application.RadioStreams);
		//ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.stations, android.R.layout.simple_list_item_1); 
		list_Stations.setAdapter(adapter);
        
		list_Stations.setOnItemClickListener(new OnItemClickListener(){
        	@Override
        	public void onItemClick(AdapterView parent, View view, int position, long id)
        	{
        		Intent result = new Intent();
        		
        		RadioRedditApplication application = (RadioRedditApplication)getApplication();
        		
        		RadioStream newStream =  application.RadioStreams.get(position);
        		
        		boolean changedStream = false;
        		if(!application.CurrentStream.Name.equals(newStream.Name))
        			changedStream = true;
        		
        		result.putExtra("changed_stream", changedStream);
        		
        		application.CurrentStream = newStream;

//        		TextView lbl_stream_name = (TextView)view.findViewById(R.id.lbl_stream_name);
//        	    result.putExtra("stream_name", lbl_stream_name.getText());
//        		
//            	TextView lbl_stream_url = (TextView)view.findViewById(R.id.lbl_stream_url);
//        	    result.putExtra("stream_url", lbl_stream_url.getText());

        	    setResult(Activity.RESULT_OK, result);
        	    finish();
        	}
        });
    }

}
