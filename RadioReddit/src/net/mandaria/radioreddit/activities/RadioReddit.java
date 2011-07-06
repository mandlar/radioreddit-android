package net.mandaria.radioreddit.activities;

import java.util.Locale;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.media.PlaybackService;
import net.mandaria.radioreddit.media.StreamProxy;
import net.mandaria.radioreddit.tasks.GetRadioStreamsTask;

import android.app.Activity;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class RadioReddit extends Activity
{
	TextView lbl_station;
	TextView lbl_SongVote;
	TextView lbl_SongTitle;
	TextView lbl_SongArtist;
	TextView lbl_SongPlaylist;
	ImageView btn_SongInfo;
	TextView lbl_Buffering;
	TextView lbl_Connecting;
	LinearLayout div_header;
	LinearLayout div_station;

	ProgressBar progress_LoadingSong;
	ImageView img_Logo;

	Button btn_play;
	Button btn_downvote;
	Button btn_upvote;
	StreamProxy proxy;

	private String LOG_TAG = "RadioReddit";
	private int sdkVersion = 0;

	private Handler mHandler = new Handler();
	private long mLastStreamsInformationUpdateMillis = 0;

	private PlaybackService player;
	private ServiceConnection conn;
	private BroadcastReceiver changeReceiver = new PlaybackChangeReceiver();
	private BroadcastReceiver updateReceiver = new PlaybackUpdateReceiver();
	private BroadcastReceiver closeReceiver = new PlaybackCloseReceiver();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		RadioRedditApplication application = (RadioRedditApplication) getApplication();

		new GetRadioStreamsTask(application, RadioReddit.this, Locale.getDefault()).execute();

		try
		{
			sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		}
		catch(NumberFormatException e)
		{

		}

		// Disable title on phones, enable action bar on tablets
		if(sdkVersion < 11)
			requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		div_header = (LinearLayout) findViewById(R.id.div_header);
		if(sdkVersion >= 11)
		{
			div_header.setVisibility(View.GONE);
		}

		div_station = (LinearLayout) findViewById(R.id.div_station);
		div_station.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ChooseStation();
			}
		});

		lbl_SongVote = (TextView) findViewById(R.id.lbl_SongVote);
		lbl_SongTitle = (TextView) findViewById(R.id.lbl_SongTitle);
		lbl_SongArtist = (TextView) findViewById(R.id.lbl_SongArtist);
		lbl_SongPlaylist = (TextView) findViewById(R.id.lbl_SongPlaylist);
		btn_SongInfo = (ImageView) findViewById(R.id.btn_SongInfo);
		btn_SongInfo.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				ViewEpisodeInfo();
			}
		});

		lbl_Buffering = (TextView) findViewById(R.id.lbl_Buffering);
		lbl_Connecting = (TextView) findViewById(R.id.lbl_Connecting);
		img_Logo = (ImageView) findViewById(R.id.img_Logo);
		progress_LoadingSong = (ProgressBar) findViewById(R.id.progress_LoadingSong);

		btn_upvote = (Button) findViewById(R.id.btn_upvote);
		btn_downvote = (Button) findViewById(R.id.btn_downvote);
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
						if(player.isPlaying())
						{
							player.stop();

							hideSongInformation();

							showPlayButton();

							lbl_Buffering.setVisibility(View.GONE);
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

							lbl_Buffering.setVisibility(View.GONE);
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
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		RadioRedditApplication application = (RadioRedditApplication) getApplication();

		MenuItem chooseStation = (MenuItem) menu.findItem(R.id.chooseStation);
		MenuItem viewEpisodeInfo = (MenuItem) menu.findItem(R.id.viewEpisodeInfo);

		// Connecting to radio reddit
		if(application.RadioStreams == null)
		{
			chooseStation.setEnabled(false);
		}
		else
		{
			chooseStation.setEnabled(true);
		}

		if(application.CurrentEpisode != null)
			viewEpisodeInfo.setVisible(true);
		else
			viewEpisodeInfo.setVisible(false);

		if(sdkVersion >= 11)
		{
			if(application.CurrentStream != null)
				getActionBar().setTitle(getResources().getString(R.string.currentStation) + ": " + application.CurrentStream.Name);
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		// FlurryAgent.onEvent("radio reddit - Menu Button");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.chooseStation:
				ChooseStation();
				return true;
			case R.id.viewEpisodeInfo:
				ViewEpisodeInfo();
				return true;
			case R.id.email_feedback:
				// FlurryAgent.onEvent("radio reddit - Menu Button - Email Feedback");
				SendEmail();
				return true;
			case R.id.exit:
				ExitApp();
				return true;
		}
		return false;
	}

	private void ViewEpisodeInfo()
	{
		Intent i = new Intent(RadioReddit.this, ViewEpisodeInformation.class);
		startActivity(i);
	}

	private void ChooseStation()
	{
		RadioRedditApplication application = (RadioRedditApplication) getApplication();

		if(player != null && !player.isPreparing()) // check if a current listen is in progress to prevent state exception
		{
			if(application.RadioStreams != null && application.RadioStreams.size() > 0)
			{
				Intent i = new Intent(RadioReddit.this, SelectStation.class);
				startActivityForResult(i, 1);
			}
			else
			{
				// Try to get streams again
				new GetRadioStreamsTask(application, RadioReddit.this, Locale.getDefault()).execute();
			}
		}
		else
		{
			Toast.makeText(RadioReddit.this, getString(R.string.pleaseWaitToChangeStation), Toast.LENGTH_LONG).show();
		}
	}

	private void ExitApp()
	{
		// kill the service, then exit to home launcher
		if(player != null)
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
		sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.email_address) });
		// Subject
		String appName = getString(R.string.app_name);
		String version = "";
		try
		{
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		}
		catch(NameNotFoundException e)
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

		if(player == null)
		{
			attachToPlaybackService();
		}

		RadioRedditApplication application = (RadioRedditApplication) getApplication();
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

		// Explicitly start the service. Don't use BIND_AUTO_CREATE, since it causes an implicit service stop when the last binder is removed.
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
		// Toast.makeText(this, "detached from window", Toast.LENGTH_LONG).show();
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
			// Toast.makeText(RadioReddit.this, "PlaybackChange - onReceive", Toast.LENGTH_LONG).show();

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
			RadioRedditApplication application = (RadioRedditApplication) getApplication();
			// Toast.makeText(RadioReddit.this, "PlaybackUpdate - onReceive", Toast.LENGTH_LONG).show();
			int buffered = intent.getIntExtra(PlaybackService.EXTRA_BUFFERED, 0);
			int duration = intent.getIntExtra(PlaybackService.EXTRA_DURATION, 1);
			int position = intent.getIntExtra(PlaybackService.EXTRA_POSITION, 0);
			int downloaded = intent.getIntExtra(PlaybackService.EXTRA_DOWNLOADED, 1);

			// Log.e("RADIO REDDIT BUFFERED", String.valueOf(buffered));
			// Log.e("RADIO REDDIT DURATION", String.valueOf(duration));
			// Log.e("RADIO REDDIT POSITION", String.valueOf(position));
			// Log.e("RADIO REDDIT DOWNLOADED", String.valueOf(downloaded));

			if(player != null && player.isBuffering())
			{
				hideSongInformation();
				lbl_Buffering.setVisibility(View.VISIBLE);
				progress_LoadingSong.setVisibility(View.VISIBLE);

			}
			else
			{
				lbl_Buffering.setVisibility(View.GONE);
				if(application.CurrentSong != null || application.CurrentEpisode != null)
					progress_LoadingSong.setVisibility(View.GONE);
			}
		}
	}

	private class PlaybackCloseReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{

		}
	}

	protected void listen(String url)
	{
		if(player != null)
		{
			try
			{
				player.listen(url, true);
			}
			catch(Exception ex)
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

			if(changedStream || (player != null && !player.isPlaying())) // check if already playing
			{
				RadioRedditApplication application = (RadioRedditApplication) getApplication();
				application.CurrentSong = null; // clear the current song
				application.CurrentEpisode = null; // clear the current episode

				playStream();
			}
		}
	}

	private void playStream()
	{
		RadioRedditApplication application = (RadioRedditApplication) getApplication();

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
			Toast.makeText(this, getString(R.string.error_RadioRedditServerIsDownNotification), Toast.LENGTH_LONG).show();

			// Try to get streams again
			new GetRadioStreamsTask(application, RadioReddit.this, Locale.getDefault()).execute();
		}

	}

	private Runnable mUpdateTimeTask = new Runnable()
	{
		@Override
		public void run()
		{
			RadioRedditApplication application = (RadioRedditApplication) getApplication();

			// Update stream information every 30 seconds
			if((SystemClock.elapsedRealtime() - mLastStreamsInformationUpdateMillis) > 30000)
			{
				new GetRadioStreamsTask(application, RadioReddit.this, Locale.getDefault()).execute();
				mLastStreamsInformationUpdateMillis = SystemClock.elapsedRealtime();
			}

			// Connecting to radio reddit
			if(application.RadioStreams == null)
			{
				progress_LoadingSong.setVisibility(View.VISIBLE);
				lbl_Connecting.setVisibility(View.VISIBLE);
				div_station.setVisibility(View.GONE);
				btn_play.setVisibility(View.GONE);
				btn_upvote.setVisibility(View.GONE);
				btn_downvote.setVisibility(View.GONE);
				img_Logo.setImageResource(R.drawable.logo_darkeyes);
			}
			else
			{
				if(player != null && !player.isPlaying() && !player.isPreparing())
					progress_LoadingSong.setVisibility(View.GONE);
				lbl_Connecting.setVisibility(View.GONE);
				div_station.setVisibility(View.VISIBLE);
				btn_play.setVisibility(View.VISIBLE);
				btn_upvote.setVisibility(View.VISIBLE);
				btn_downvote.setVisibility(View.VISIBLE);
				img_Logo.setImageResource(R.drawable.logo);
			}

			if(sdkVersion >= 11)
				invalidateOptionsMenu(); // force update of menu to enable "Choose Station" when connected (mainly for Android 3.0)

			if(player != null && player.isPlaying())
			{
				// Update current song information
				if(!player.isBuffering())
				{
					if(application.CurrentSong != null && !player.isBuffering())
					{
						showSongInformation();

						progress_LoadingSong.setVisibility(View.GONE);
					}
					else if(application.CurrentEpisode != null)
					{
						showEpisodeInformation();

						progress_LoadingSong.setVisibility(View.GONE);
					}
				}
			}
			else
			{
				hideSongInformation();
				if(player != null && !player.isPreparing())
					showPlayButton();
				else if(player != null && player.isPreparing() && !player.isAborting())
					progress_LoadingSong.setVisibility(View.VISIBLE); // show please wait spinner when "re-connecting" to stream from network change
			}

			mHandler.postDelayed(this, 1000); // update every 1 second
		}

	};

	private void showEpisodeInformation()
	{
		RadioRedditApplication application = (RadioRedditApplication) getApplication();
		lbl_SongVote.setVisibility(View.VISIBLE);
		lbl_SongTitle.setVisibility(View.VISIBLE);
		lbl_SongArtist.setVisibility(View.VISIBLE);
		lbl_SongPlaylist.setVisibility(View.GONE);
		lbl_SongVote.setText(application.CurrentEpisode.Score);
		lbl_SongTitle.setText(application.CurrentEpisode.EpisodeTitle);
		lbl_SongArtist.setText(application.CurrentEpisode.ShowTitle);
		btn_SongInfo.setVisibility(View.VISIBLE);
		// lbl_SongVote.setText(getString(R.string.vote_to_submit_song));
		// lbl_SongTitle.setText(getString(R.string.dummy_song_title));
		// lbl_SongArtist.setText(getString(R.string.dummy_song_artist));
		// lbl_SongPlaylist.setText(getString(R.string.dummy_song_playlist));

	}

	private void showSongInformation()
	{
		RadioRedditApplication application = (RadioRedditApplication) getApplication();
		lbl_SongVote.setVisibility(View.VISIBLE);
		lbl_SongTitle.setVisibility(View.VISIBLE);
		lbl_SongArtist.setVisibility(View.VISIBLE);
		lbl_SongPlaylist.setVisibility(View.VISIBLE);
		lbl_SongVote.setText(application.CurrentSong.Score);
		lbl_SongTitle.setText(application.CurrentSong.Title);
		lbl_SongArtist.setText(application.CurrentSong.Artist + " (" + application.CurrentSong.Redditor + ")");
		lbl_SongPlaylist.setText("playlist: " + application.CurrentSong.Playlist); // TODO: pull playlist to strings.xml
		btn_SongInfo.setVisibility(View.GONE);
		// lbl_SongVote.setText(getString(R.string.vote_to_submit_song));
		// lbl_SongTitle.setText(getString(R.string.dummy_song_title));
		// lbl_SongArtist.setText(getString(R.string.dummy_song_artist));
		// lbl_SongPlaylist.setText(getString(R.string.dummy_song_playlist));

	}

	private void hideSongInformation()
	{
		// remove song information
		lbl_SongVote.setVisibility(View.GONE);
		lbl_SongTitle.setVisibility(View.GONE);
		lbl_SongArtist.setVisibility(View.GONE);
		lbl_SongPlaylist.setVisibility(View.GONE);
		lbl_SongVote.setText("");
		lbl_SongTitle.setText("");
		lbl_SongArtist.setText("");
		lbl_SongPlaylist.setText("");
		btn_SongInfo.setVisibility(View.GONE);
	}

}