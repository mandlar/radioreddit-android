package net.mandaria.radioreddit;

import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RadioReddit extends Activity 
{
	MediaPlayer mediaPlayer = new MediaPlayer();;
	Button btn_play;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        

        
        
        btn_play = (Button)findViewById(R.id.btn_play);
        btn_play.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{		
				if (mediaPlayer.isPlaying()) 
				{
					mediaPlayer.stop();
					btn_play.setText("Play");
				} 
				else 
				{
					mediaPlayer.reset();
					try 
					{
						mediaPlayer.setDataSource("http://texas.radioreddit.com:8000/");
					} 
					catch (IllegalArgumentException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					catch (IllegalStateException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				       
					try 
					{
						mediaPlayer.prepare();
					} 
					catch (IllegalStateException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					catch (IOException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mediaPlayer.start();
					btn_play.setText("Stop");
				}
			}
		});
    }
}