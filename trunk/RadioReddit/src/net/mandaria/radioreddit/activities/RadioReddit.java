package net.mandaria.radioreddit.activities;

import java.io.IOException;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.R.drawable;
import net.mandaria.radioreddit.R.id;
import net.mandaria.radioreddit.R.layout;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.apis.RadioRedditAPI;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class RadioReddit extends Activity {
	
	TextView lbl_station;
	TextView lbl_SongVote;
	TextView lbl_SongTitle;
	TextView lbl_SongArtist;
	TextView lbl_SongPlaylist;
	ProgressBar progress_LoadingSong;
	
	MediaPlayer mediaPlayer = new MediaPlayer();;
	Button btn_play;
	StreamProxy proxy;

	private PlaybackService player;
	private ServiceConnection conn;
	private BroadcastReceiver changeReceiver = new PlaybackChangeReceiver();
	private BroadcastReceiver updateReceiver = new PlaybackUpdateReceiver();
	private BroadcastReceiver closeReceiver = new PlaybackCloseReceiver();
	private boolean playButtonisPause = false;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		//TODO: move this to AsyncTask, testing for now in onCreate:
		RadioRedditAPI.GetStreams(this);
		
		init();
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		RadioRedditApplication application = (RadioRedditApplication)getApplication();
		
		lbl_station = (TextView) findViewById(R.id.lbl_station);
		lbl_station.setText(application.current_station);
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
		
		lbl_SongVote = (TextView) findViewById(R.id.lbl_SongVote);
		lbl_SongTitle = (TextView) findViewById(R.id.lbl_SongTitle);
		lbl_SongArtist = (TextView) findViewById(R.id.lbl_SongArtist);
		lbl_SongPlaylist = (TextView) findViewById(R.id.lbl_SongPlaylist);
		progress_LoadingSong = (ProgressBar) findViewById(R.id.progress_LoadingSong);

		btn_play = (Button) findViewById(R.id.btn_play);
		btn_play.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				if(player == null)
				{
					attachToPlaybackService();
				}
				else
				{
					if (player.isPlaying()) 
					{
						player.stop();
						
						hideSongInformation();
					} 
					else 
					{			
						RadioRedditApplication application = (RadioRedditApplication)getApplication();
						playStation(application.current_station);
						
						// show progress bar while waiting to load song information
						progress_LoadingSong.setVisibility(View.VISIBLE);
						
					}
				}
			}
		});
	}	

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
		Toast.makeText(this, "detached from window", Toast.LENGTH_LONG).show();
		// Log.d(LOG_TAG, "detached from window");
		unregisterReceiver(changeReceiver);
		unregisterReceiver(updateReceiver);
		unregisterReceiver(closeReceiver);
		getApplicationContext().unbindService(conn);
	}
	
	private void showSongInformation()
	{
		Resources res = getResources();

		Drawable stop = res.getDrawable(R.drawable.stopbutton);
		btn_play.setBackgroundDrawable(stop);

		lbl_SongVote.setText(getString(R.string.vote_to_submit_song));
		lbl_SongTitle.setText(getString(R.string.dummy_song_title));
		lbl_SongArtist.setText(getString(R.string.dummy_song_artist));
		lbl_SongPlaylist.setText(getString(R.string.dummy_song_playlist));

	}
	
	private void hideSongInformation()
	{
		Resources res = getResources();
		Drawable play = res.getDrawable(R.drawable.playbutton);
		btn_play.setBackgroundDrawable(play);
		
		// remove song information
		lbl_SongVote.setText("");
		lbl_SongTitle.setText("");
		lbl_SongArtist.setText("");
		lbl_SongPlaylist.setText("");
	}

	private class PlaybackChangeReceiver extends BroadcastReceiver 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String title = intent.getStringExtra(PlaybackService.EXTRA_TITLE);
			// infoText.setText(title);
			Toast.makeText(RadioReddit.this, "PlaybackChange - onReceive", Toast.LENGTH_LONG).show();
			// "get" song information -- TODO: eventually needs to be called every 30 seconds
			progress_LoadingSong.setVisibility(View.GONE);
			
			if(player != null && player.isPlaying())
			{
				showSongInformation();
			}
			else
			{
				hideSongInformation();
			}
		}
	}

	private class PlaybackUpdateReceiver extends BroadcastReceiver 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			Toast.makeText(RadioReddit.this, "PlaybackUpdate - onReceive", Toast.LENGTH_LONG).show();
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
			Toast.makeText(RadioReddit.this, "PlaybackClose - onReceive", Toast.LENGTH_LONG).show();
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
		// TODO: check if a current listen is in progress to prevent state exception
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode == Activity.RESULT_OK)
		{
			String station = data.getStringExtra("station");
			RadioRedditApplication application = (RadioRedditApplication)getApplication();
			
			if(!station.equals(application.current_station)) // check if already playing
			{
				application.current_station = station;
				playStation(station);
			}
		}
	}
	
	private void playStation(String station)
	{
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
		
		
		lbl_station.setText(station); // TODO: move to on resume
	}
	
}