package net.mandaria.radioreddit.activities;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.R.array;
import net.mandaria.radioreddit.R.id;
import net.mandaria.radioreddit.R.layout;
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
		
		ListView list_Stations = (ListView)findViewById(R.id.list_Stations);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.stations, android.R.layout.simple_list_item_1); 
		list_Stations.setAdapter(adapter);
        
		list_Stations.setOnItemClickListener(new OnItemClickListener(){
        	@Override
        	public void onItemClick(AdapterView parent, View view, int position, long id)
        	{
        		Intent result = new Intent();

        	    //Bundle b = new Bundle();
        	    //b.putString("station", parent.getSelectedItem().toString());
        		TextView lbl_text1 = (TextView)view.findViewById(android.R.id.text1);

        	    result.putExtra("station", lbl_text1.getText());

        	    setResult(Activity.RESULT_OK, result);
        	    finish();
        	}
        });
    }

}
