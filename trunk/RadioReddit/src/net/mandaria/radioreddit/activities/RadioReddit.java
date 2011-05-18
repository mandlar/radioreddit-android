package net.mandaria.radioreddit.activities;

import java.io.IOException;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.R.drawable;
import net.mandaria.radioreddit.R.id;
import net.mandaria.radioreddit.R.layout;
import net.mandaria.radioreddit.media.PlaybackService;
import net.mandaria.radioreddit.media.StreamProxy;
import net.mandaria.radioreddit.media.PlaybackService.ListenBinder;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class RadioReddit extends Activity {
	
	TextView lbl_station;
	
	MediaPlayer mediaPlayer = new MediaPlayer();;
	Button btn_play;
	StreamProxy proxy;

	private PlaybackService player;
	private ServiceConnection conn;
	private BroadcastReceiver changeReceiver = new PlaybackChangeReceiver();
	private BroadcastReceiver updateReceiver = new PlaybackUpdateReceiver();
	private BroadcastReceiver closeReceiver = new PlaybackCloseReceiver();
	private boolean playButtonisPause = false;

	@Override
	public void onAttachedToWindow() 
	{
		super.onAttachedToWindow();

	}

	private void init() 
	{
		attachToPlaybackService();
	}

	public void attachToPlaybackService() 
	{
		Intent serviceIntent = new Intent(getApplicationContext(), PlaybackService.class);
		conn = new ServiceConnection() 
		{
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) 
			{
				player = ((PlaybackService.ListenBinder) service).getService();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) 
			{
				// Log.w(LOG_TAG, "DISCONNECT");
				player = null;
			}
		};

		// Explicitly start the service. Don't use BIND_AUTO_CREATE, since it
		// causes an implicit service stop when the last binder is removed.
		getApplicationContext().startService(serviceIntent);
		getApplicationContext().bindService(serviceIntent, conn, 0);

		registerReceiver(changeReceiver, new IntentFilter(PlaybackService.SERVICE_CHANGE_NAME));
		registerReceiver(updateReceiver, new IntentFilter(PlaybackService.SERVICE_UPDATE_NAME));
		registerReceiver(closeReceiver, new IntentFilter(PlaybackService.SERVICE_CLOSE_NAME));
	}

	@Override
	public void onDetachedFromWindow() 
	{
		super.onDetachedFromWindow();
		// Log.d(LOG_TAG, "detached from window");
		unregisterReceiver(changeReceiver);
		unregisterReceiver(updateReceiver);
		unregisterReceiver(closeReceiver);
		getApplicationContext().unbindService(conn);
	}

	private class PlaybackChangeReceiver extends BroadcastReceiver 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String title = intent.getStringExtra(PlaybackService.EXTRA_TITLE);
			// infoText.setText(title);
		}
	}

	private class PlaybackUpdateReceiver extends BroadcastReceiver 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			int duration = intent.getIntExtra(PlaybackService.EXTRA_DURATION, 1);
			int position = intent.getIntExtra(PlaybackService.EXTRA_POSITION, 0);
			int downloaded = intent.getIntExtra(PlaybackService.EXTRA_DOWNLOADED, 1);
			// if (!playButtonisPause && player != null && player.isPlaying()) {
			// playButton.setImageResource(android.R.drawable.ic_media_pause);
			// playButtonisPause = true;
			// }
			// playButton.setEnabled(true);
			// progressBar.setEnabled(true);
			// progressBar.setMax(duration);
			// progressBar.setProgress(position);
			// progressBar.setSecondaryProgress(downloaded);
		}
	}

	private class PlaybackCloseReceiver extends BroadcastReceiver 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			// playButton.setEnabled(false);
			// playButton.setImageResource(android.R.drawable.ic_media_play);
			// progressBar.setEnabled(false);
			// progressBar.setProgress(0);
			// progressBar.setSecondaryProgress(0);
			// infoText.setText(null);
		}
	}

	private void togglePlay() 
	{
		if (player.isPlaying()) 
		{
			player.pause();
			// playButton.setImageResource(android.R.drawable.ic_media_play);
			playButtonisPause = false;
		} 
		else 
		{
			player.play();
			// playButton.setImageResource(android.R.drawable.ic_media_pause);
			playButtonisPause = true;
		}
	}

	protected void listen(String url) 
	{
		if (player != null) 
		{
			try 
			{
				player.listen(url, true);
			}
			catch (Exception ex)
			{
				Toast.makeText(this, "Error on listen: " + ex.toString(), Toast.LENGTH_LONG).show();
			}
//			catch (IllegalArgumentException e) 
//			{
//				// Log.e(LOG_TAG, "", e);
//			} 
//			catch (IllegalStateException e) 
//			{
//				// Log.e(LOG_TAG, "", e);
//			} 
//			catch (IOException e) 
//			{
//				// Log.e(LOG_TAG, "", e);
//			}
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		init();
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		lbl_station = (TextView) findViewById(R.id.lbl_station);
		lbl_station.setOnClickListener(new OnClickListener() 
		{	
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				Intent i = new Intent(RadioReddit.this, SelectStation.class);
				startActivityForResult(i, 1);
			}
		});

		btn_play = (Button) findViewById(R.id.btn_play);
		btn_play.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				if (player.isPlaying()) 
				{
					player.stop();
					// TODO: pull the drawable out to onResume()
					Resources res = getResources();
					Drawable play = res.getDrawable(R.drawable.playbutton);
					btn_play.setBackgroundDrawable(play);
				} 
				else 
				{
					String url = getString(R.string.main_stream_url);

					listen(url);

					// TODO: pull the drawable out to onResume()
					Resources res = getResources();
					Drawable stop = res.getDrawable(R.drawable.stopbutton);
					btn_play.setBackgroundDrawable(stop);
				}
			}
		});
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode == Activity.RESULT_OK)
		{
			// TODO: needs to check iff current station is already selected
			String station = data.getStringExtra("station");
			if(station.equals("main stream"))
			{
				listen(getString(R.string.main_stream_url));
			}
			else if (station.equals("electronic"))
			{
				listen(getString(R.string.electronic_stream_url));
			}
			else if (station.equals("rock"))
			{
				listen(getString(R.string.rock_stream_url));
			}
			else if (station.equals("hip hop and rap"))
			{
				listen(getString(R.string.hip_hop_and_rap_stream_url));
			}
			
			lbl_station.setText(station);
		}
	}
	
}