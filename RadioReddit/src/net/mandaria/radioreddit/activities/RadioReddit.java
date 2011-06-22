package net.mandaria.radioreddit.activities;

import java.io.IOException;
import java.util.Locale;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.R.drawable;
import net.mandaria.radioreddit.R.id;
import net.mandaria.radioreddit.R.layout;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.apis.RadioRedditAPI;
import net.mandaria.radioreddit.media.PlaybackService;
import net.mandaria.radioreddit.media.StreamProxy;
import net.mandaria.radioreddit.media.PlaybackService.ListenBinder;
import net.mandaria.radioreddit.objects.RadioStream;
import net.mandaria.radioreddit.tasks.GetCurrentSongInformationTask;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
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
	TextView lbl_Buffering;
	LinearLayout div_station;
	ProgressBar progress_LoadingSong;
	
	Button btn_play;
	StreamProxy proxy;
	
	private String LOG_TAG = "RadioReddit";
	
	private Handler mHandler = new Handler();

	private PlaybackService player;
	private ServiceConnection conn;
	private BroadcastReceiver changeReceiver = new PlaybackChangeReceiver();
	private BroadcastReceiver updateReceiver = new PlaybackUpdateReceiver();
	private BroadcastReceiver closeReceiver = new PlaybackCloseReceiver();
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		RadioRedditApplication application = (RadioRedditApplication)getApplication();
		//TODO: move this to Task, testing for now in onCreate:
		RadioRedditAPI.GetStreams(this, application);
		
		
		init();
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		div_station = (LinearLayout) findViewById(R.id.div_station);
		div_station.setOnClickListener(new OnClickListener() 
		{	
			@Override
			public void onClick(View v) 
			{
				RadioRedditApplication application = (RadioRedditApplication)getApplication();
				
				if(!player.isPreparing()) // check if a current listen is in progress to prevent state exception
				{
					if(application.RadioStreams != null && application.RadioStreams.size() > 0)
					{
						Intent i = new Intent(RadioReddit.this, SelectStation.class);
						startActivityForResult(i, 1);
					}
					else
					{
						// Try to get streams again
						RadioRedditAPI.GetStreams(RadioReddit.this, application);
					}
				}
				else
				{
					Toast.makeText(RadioReddit.this, getString(R.string.pleaseWaitToChangeStation), Toast.LENGTH_LONG).show();
				}
			}
		});
		
		lbl_SongVote = (TextView) findViewById(R.id.lbl_SongVote);
		lbl_SongTitle = (TextView) findViewById(R.id.lbl_SongTitle);
		lbl_SongArtist = (TextView) findViewById(R.id.lbl_SongArtist);
		lbl_SongPlaylist = (TextView) findViewById(R.id.lbl_SongPlaylist);
		lbl_Buffering = (TextView) findViewById(R.id.lbl_Buffering);
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
					if(!player.isPreparing()) // We can't do anything while media player is preparing....
					{
						if (player.isPlaying()) 
						{
							player.stop();
							
							hideSongInformation();
							
							showPlayButton();
							
							progress_LoadingSong.setVisibility(View.GONE);
						} 
						else 
						{			
							playStream();
						}
					}
					else
					{
						// But we can tell the media player we actually don't want to start playing, we changed our mind
						if(!player.isAborting())
						{
							// Mediaplayer is preparing, we want to not stream
							player.abort();
							
							hideSongInformation();
							
							showPlayButton();
							
							progress_LoadingSong.setVisibility(View.GONE);
						}
						else
						{
							// Mediaplayer is preparing, we want to stream (even though we previously aborted)
							player.stopAbort();
							
							hideSongInformation();
							
							showStopButton();
							
							progress_LoadingSong.setVisibility(View.VISIBLE);
						}
					}
				}
			}
		});
		
		startUpdateTimer();
	}	
	
	@Override
  	public boolean onCreateOptionsMenu(Menu menu)
  	{
  		super.onCreateOptionsMenu(menu);
  		MenuInflater inflater = getMenuInflater();
  		inflater.inflate(R.menu.menu, menu);
  		//FlurryAgent.onEvent("radio reddit - Menu Button");
  		return true;
  	}

  	@Override
  	public boolean onOptionsItemSelected(MenuItem item)
  	{
  		switch(item.getItemId())
  		{
  			case R.id.email_feedback:
  				//FlurryAgent.onEvent("radio reddit - Menu Button - Email Feedback");
  				SendEmail();
  				return true;
  			case R.id.exit:
  				ExitApp();
  				return true;
  		}
  		return false;
  	}
  	
private void ExitApp()
{
	// kill the service, then exit to home launcher
	player.stopSelf();
	
	Intent intent = new Intent(Intent.ACTION_MAIN);
	intent.addCategory(Intent.CATEGORY_HOME);
	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	startActivity(intent);
}
  	
private void SendEmail()
{
	// Setup an intent to send email
	Intent sendIntent;
	sendIntent = new Intent(Intent.ACTION_SEND);
	sendIntent.setType("application/octet-stream");
	// Address
	sendIntent.putExtra(Intent.EXTRA_EMAIL,new String[] { getString(R.string.email_address) });
	// Subject
	String appName = getString(R.string.app_name);
	String version = "";
	try 
	{
		version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
	} catch (NameNotFoundException e) 
	{

	}
	
	sendIntent.putExtra(Intent.EXTRA_SUBJECT, appName + " " + version);
	// Body
	String body = "\n\n\n\n\n";
	body += getString(R.string.email_using_custom_rom) + "\n";
	body += "--------------------\n";
	body += getString(R.string.email_do_not_edit_message) + "\n\n";
	body += "BOARD: " + Build.BOARD + "\n";
	body += "BRAND: " + Build.BRAND + "\n";
	body += "CPU_ABI: " + Build.CPU_ABI + "\n";
	body += "DEVICE: " + Build.DEVICE + "\n";
	body += "DISPLAY: " + Build.DISPLAY + "\n";
	body += "FINGERPRINT: " + Build.FINGERPRINT + "\n";
	body += "HOST: " + Build.HOST + "\n";
	body += "ID: " + Build.ID + "\n";
	body += "MANUFACTURER: " + Build.MANUFACTURER + "\n";
	body += "MODEL: " + Build.MODEL + "\n";
	body += "PRODUCT: " + Build.PRODUCT + "\n";
	body += "TAGS: " + Build.TAGS + "\n";
	body += "TIME: " + Build.TIME + "\n";
	body += "TYPE: " + Build.TYPE + "\n";
	body += "USER: " + Build.USER + "\n";
	body += "VERSION.CODENAME: " + Build.VERSION.CODENAME + "\n";
	body += "VERSION.INCREMENTAL: " + Build.VERSION.INCREMENTAL + "\n";
	body += "VERSION.RELEASE: " + Build.VERSION.RELEASE + "\n";
	body += "VERSION.SDK: " + Build.VERSION.SDK + "\n";
	body += "VERSION.SDK_INT: " + Build.VERSION.SDK_INT + "\n";
	
	sendIntent.putExtra(Intent.EXTRA_TEXT, body);
	startActivity(Intent.createChooser(sendIntent, "Send Mail"));
}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		RadioRedditApplication application = (RadioRedditApplication)getApplication();
		lbl_station = (TextView) findViewById(R.id.lbl_station);
		
		if(application.CurrentStream != null)
			lbl_station.setText(application.CurrentStream.Name);
		
		startUpdateTimer();
		
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		stopUpdateTimer();
	}
	
	private void startUpdateTimer()
	{
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.postDelayed(mUpdateTimeTask, 0);
	}

	private void stopUpdateTimer()
	{
		mHandler.removeCallbacks(mUpdateTimeTask);
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
		//Toast.makeText(this, "detached from window", Toast.LENGTH_LONG).show();
		// Log.d(LOG_TAG, "detached from window");
		unregisterReceiver(changeReceiver);
		unregisterReceiver(updateReceiver);
		unregisterReceiver(closeReceiver);
		getApplicationContext().unbindService(conn);
	}
	
	private void showStopButton()
	{
		Resources res = getResources();

		Drawable stop = res.getDrawable(R.drawable.stop_button);
		btn_play.setBackgroundDrawable(stop);
	}

	private void showPlayButton()
	{
		Resources res = getResources();
		Drawable play = res.getDrawable(R.drawable.play_button);
		btn_play.setBackgroundDrawable(play);
	}
	
	private class PlaybackChangeReceiver extends BroadcastReceiver 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String title = intent.getStringExtra(PlaybackService.EXTRA_TITLE);
			// infoText.setText(title);
			//Toast.makeText(RadioReddit.this, "PlaybackChange - onReceive", Toast.LENGTH_LONG).show();
			
			if(player != null && player.isPlaying())
				showStopButton();
			else
				showPlayButton();
		}
	}

	private class PlaybackUpdateReceiver extends BroadcastReceiver 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			//Toast.makeText(RadioReddit.this, "PlaybackUpdate - onReceive", Toast.LENGTH_LONG).show();
			int buffered = intent.getIntExtra(PlaybackService.EXTRA_BUFFERED, 0);
			int duration = intent.getIntExtra(PlaybackService.EXTRA_DURATION, 1);
			int position = intent.getIntExtra(PlaybackService.EXTRA_POSITION, 0);
			int downloaded = intent.getIntExtra(PlaybackService.EXTRA_DOWNLOADED, 1);
			
//			Log.e("RADIO REDDIT BUFFERED", String.valueOf(buffered));
//			Log.e("RADIO REDDIT DURATION", String.valueOf(duration));
//			Log.e("RADIO REDDIT POSITION", String.valueOf(position));
//			Log.e("RADIO REDDIT DOWNLOADED", String.valueOf(downloaded));
			
			if(buffered > 0 && buffered < 100)
			{
				hideSongInformation(); // TODO: Need some sort of flag to keep showSongInfo from working until buffering is done. Otherwise we get "flashes"
				lbl_Buffering.setVisibility(View.VISIBLE);
				lbl_Buffering.setText("Buffering..." + buffered + "%");
				//Toast.makeText(RadioReddit.this, "Buffering..." + buffered + "%", Toast.LENGTH_SHORT).show();
			}
			else
			{
				lbl_Buffering.setVisibility(View.GONE);
			}
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
			//Toast.makeText(RadioReddit.this, "PlaybackClose - onReceive", Toast.LENGTH_LONG).show();
			// playButton.setEnabled(false);
			// playButton.setImageResource(android.R.drawable.ic_media_play);
			// progressBar.setEnabled(false);
			// progressBar.setProgress(0);
			// progressBar.setSecondaryProgress(0);
			// infoText.setText(null);
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
				ex.printStackTrace();
				Toast.makeText(this, "Error on listen: " + ex.toString(), Toast.LENGTH_LONG).show();
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode == Activity.RESULT_OK)
		{
			boolean changedStream = data.getBooleanExtra("changed_stream", false);
			
			//String stream_name = data.getStringExtra("stream_name");
			//String stream_url = data.getStringExtra("stream_url");
			
			if(changedStream || !player.isPlaying()) // check if already playing
			{
				RadioRedditApplication application = (RadioRedditApplication)getApplication();
				application.CurrentSong = null; // clear the current song
				
				playStream();
			}
		}
	}
	
	private void playStream()
	{
		RadioRedditApplication application = (RadioRedditApplication)getApplication();
		
		if(application.CurrentStream != null)
		{
			listen(application.CurrentStream.Relay);
			
			lbl_station.setText(application.CurrentStream.Name);
			
			hideSongInformation();
			
			// show progress bar while waiting to load song information
			progress_LoadingSong.setVisibility(View.VISIBLE);
			
			showStopButton();
		}
		else
		{
			Toast.makeText(this, getString(R.string.radioRedditServerIsDownNotification), Toast.LENGTH_LONG).show();
			// Try to get streams again
			RadioRedditAPI.GetStreams(this, application);
		}
		
	}
	
	private Runnable mUpdateTimeTask = new Runnable()
	{

		@Override
		public void run()
		{
			if(player != null && player.isPlaying())
			{
				RadioRedditApplication application = (RadioRedditApplication)getApplication();
				
				// Update current song information
				if(application.CurrentSong != null)
				{
					showSongInformation();
					
					progress_LoadingSong.setVisibility(View.GONE);
				}
			}
			else
			{
				hideSongInformation();
				if(player != null && !player.isPreparing())
					showPlayButton();
			}
			
			mHandler.postDelayed(this, 1000); // update every 1 second
		}
		
	};
	
	private void showSongInformation()
	{
		RadioRedditApplication application = (RadioRedditApplication)getApplication();
		lbl_SongVote.setText(application.CurrentSong.Score);
		lbl_SongTitle.setText(application.CurrentSong.Title);
		lbl_SongArtist.setText(application.CurrentSong.Artist + " (" + application.CurrentSong.Redditor + ")");
		lbl_SongPlaylist.setText("playlist: " + application.CurrentSong.Playlist);
		//lbl_SongVote.setText(getString(R.string.vote_to_submit_song));
		//lbl_SongTitle.setText(getString(R.string.dummy_song_title));
		//lbl_SongArtist.setText(getString(R.string.dummy_song_artist));
		//lbl_SongPlaylist.setText(getString(R.string.dummy_song_playlist));

	}
	
	private void hideSongInformation()
	{
		// remove song information
		lbl_SongVote.setText("");
		lbl_SongTitle.setText("");
		lbl_SongArtist.setText("");
		lbl_SongPlaylist.setText("");
	}
	
}