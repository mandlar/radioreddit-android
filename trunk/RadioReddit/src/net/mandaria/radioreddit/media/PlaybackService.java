/*
 *	radio reddit for android: mobile app to listen to radioreddit.com
 *  Copyright (C) 2011 Bryan Denny
 *  
 *  This file is part of "radio reddit for android"
 *
 *  "radio reddit for android" is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  "radio reddit for android" is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with "radio reddit for android".  If not, see <http://www.gnu.org/licenses/>.
 */

// This file was derived from NPR's PlaybackService which is licensed under Apache License, Version 2.0
// You can find the original file here: https://code.google.com/p/npr-android-app/source/browse/trunk/Npr/src/org/npr/android/news/PlaybackService.java

package net.mandaria.radioreddit.media;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

import com.flurry.android.FlurryAgent;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.activities.RadioReddit;
import net.mandaria.radioreddit.activities.Settings;
import net.mandaria.radioreddit.data.DatabaseService;
import net.mandaria.radioreddit.tasks.GetCurrentEpisodeInformationTask;
import net.mandaria.radioreddit.tasks.GetCurrentSongInformationTask;
import net.mandaria.radioreddit.tasks.GetVoteScoreTask;

public class PlaybackService extends Service implements OnPreparedListener, OnBufferingUpdateListener, OnCompletionListener, OnErrorListener, OnInfoListener
{

	private static final String LOG_TAG = PlaybackService.class.getName();

	private static final String SERVICE_PREFIX = "net.mandaria.radioreddit.";
	public static final String SERVICE_CHANGE_NAME = SERVICE_PREFIX + "CHANGE";
	public static final String SERVICE_CLOSE_NAME = SERVICE_PREFIX + "CLOSE";
	public static final String SERVICE_UPDATE_NAME = SERVICE_PREFIX + "UPDATE";

	public static final String EXTRA_TITLE = "title";
	public static final String EXTRA_BUFFERED = "buffered";
	public static final String EXTRA_DOWNLOADED = "downloaded";
	public static final String EXTRA_DURATION = "duration";
	public static final String EXTRA_POSITION = "position";

	private MediaPlayer mediaPlayer;
	private boolean isPrepared = false;
	private boolean isPreparing = false;
	private boolean isAborting = false;
	private boolean isBuffering = false;
	private boolean lostDataConnection = false;
	private boolean mDoHasWiFi = false;

	private long mLastCurrentSongInformationUpdateMillis = 0;
	private String lastSongTitle = "";
	private String lastSongArtist = "";

	private int mLastCurrentPosition = 0;
	private long mFirstDuplicateCurrentPositionMillis = 0;

	private StreamProxy proxy;
	private NotificationManager notificationManager;
	private static final int NOTIFICATION_ID = 1;
	private int bindCount = 0;

	private TelephonyManager telephonyManager;
	private PhoneStateListener listener;
	private boolean isPausedInCall = false;
	private Intent lastChangeBroadcast;
	private Intent lastUpdateBroadcast;
	private int lastBufferPercent = 0;

	private Handler mHandler = new Handler();

	private HeadsetBroadcastReceiver headsetReceiver;

	private PowerManager.WakeLock mWakeLock;
	private WifiManager.WifiLock mWifiLock;

	// Amount of time to rewind playback when resuming after call
	private final static int RESUME_REWIND_TIME = 3000;

	@Override
	public void onStart(Intent intent, int startId)
	{
	   super.onStart(intent, startId);
	   FlurryAgent.onStartSession(this, getString(R.string.flurrykey));
	}
	
	@Override
	public void onCreate()
	{
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Log.w(LOG_TAG, "Playback service created");

		telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		// Create a PhoneStateListener to watch for offhook and idle events
		listener = new RadioRedditPhoneStateListener();

		// Register the listener with the telephony manager.
		telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

		// Register the listener for connectivity
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(connectivityListener, intentFilter);

		// Register headset receiver
		headsetReceiver = new HeadsetBroadcastReceiver();
		registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		bindCount++;
		Log.d(LOG_TAG, "Bound PlaybackService, count " + bindCount);
		return new ListenBinder();
	}

	@Override
	public boolean onUnbind(Intent arg0)
	{
		bindCount--;
		Log.d(LOG_TAG, "Unbinding PlaybackService, count " + bindCount);
		if(!isPlaying() && bindCount == 0)
		{
			Log.w(LOG_TAG, "Will stop self");
			stopSelf();
		}
		else
		{
			Log.d(LOG_TAG, "Will not stop self");
		}
		return false;
	}

	synchronized public boolean isBuffering()
	{
		return isBuffering;
	}

	synchronized public boolean isPlaying()
	{
		if(isPrepared)
		{
			return mediaPlayer.isPlaying();
		}
		return false;
	}

	synchronized public boolean isPreparing()
	{
		return isPreparing;
	}

	synchronized public boolean isAborting()
	{
		return isAborting;
	}

	synchronized public int getPosition()
	{
		if(isPrepared)
		{
			return mediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	synchronized public int getDuration()
	{
		if(isPrepared)
		{
			return mediaPlayer.getDuration();
		}
		return 0;
	}

	synchronized public int getCurrentPosition()
	{
		if(isPrepared)
		{
			return mediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	synchronized public void seekTo(int pos)
	{
		if(isPrepared)
		{
			mediaPlayer.seekTo(pos);
		}
	}

	synchronized public void play()
	{
		Log.w(LOG_TAG, "Playback service - play() start");
		if(!isPrepared || isPreparing)
		{
			Log.e(LOG_TAG, "play - not prepared");
			return;
		}

		mediaPlayer.start();

		// Get wifi state
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		mDoHasWiFi = (ni == null || ni.getType() == ConnectivityManager.TYPE_WIFI);

		// Activate wifi lock to prevent phone from losing wifi conection when screen is off
		WifiManager wm = ((WifiManager) getSystemService(WIFI_SERVICE));
		mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, LOG_TAG);
		mWifiLock.acquire();

		// Activate wake lock to prevent phone from losing wifi connection when screen is off. This will keep CPU running
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOG_TAG);
		mWakeLock.acquire();

		// Change broadcasts are sticky, so when a new receiver connects, it will have the data without polling.
		if(lastChangeBroadcast != null)
		{
			getApplicationContext().removeStickyBroadcast(lastChangeBroadcast);
		}
		lastChangeBroadcast = new Intent(SERVICE_CHANGE_NAME);
		// lastChangeBroadcast.putExtra(EXTRA_TITLE, current.title);
		getApplicationContext().sendStickyBroadcast(lastChangeBroadcast); // broadcasts that playing has started
		
		// Setup notification so service can start in foreground
		String songTitle = getString(R.string.app_name);
		String songArtist = "";
		
		Notification notification = getNotification(songTitle, songArtist);
		
		startForeground(NOTIFICATION_ID, notification);
		
		updateNotification();
		Log.w(LOG_TAG, "Playback service - play() end");
	}

	private void updateNotification()
	{
		// Log.w(LOG_TAG, "Playback service - updateNotification() start");
		RadioRedditApplication application = (RadioRedditApplication) getApplication();
		

		String songTitle = getString(R.string.app_name);
		String songArtist = "";
		if(application.CurrentSong != null)
		{
			if(application.CurrentSong.Title != null)
				songTitle = application.CurrentSong.Title;
			if(application.CurrentSong.Artist != null && application.CurrentSong.Redditor != null)
				songArtist = application.CurrentSong.Artist + " (" + application.CurrentSong.Redditor + ")";
		}
		else if(application.CurrentEpisode != null)
		{
			if(application.CurrentEpisode.EpisodeTitle != null)
				songTitle = application.CurrentEpisode.EpisodeTitle;
			if(application.CurrentEpisode.ShowTitle != null)
				songArtist = application.CurrentEpisode.ShowTitle;
		}
		
		//Log.w(LOG_TAG, "Song Title: " + songTitle + " Song Artist: " + songArtist);
		//Log.w(LOG_TAG, "Last Song Title: " + lastSongTitle + " Last Song Artist: " + lastSongArtist);

		// Only update the notification if there has been a change
		if(!lastSongArtist.equals(songArtist) || !lastSongTitle.equals(songTitle))
		{
			lastSongTitle = songTitle;
			lastSongArtist = songArtist;
			
			Notification notification = getNotification(songTitle, songArtist);
			
			notificationManager.notify(NOTIFICATION_ID, notification);
			
			DatabaseService service = new DatabaseService();
			
			if(application.playBackType.equals("stream") && application.CurrentStream != null)
			{
				if(application.CurrentStream.Type.equals("music") && application.CurrentSong != null)
				{
					service.AddRecentlyPlayedSong(getApplicationContext(), application.CurrentSong);
				}
				else if(application.CurrentStream.Type.equals("talk") && application.CurrentEpisode != null)
				{
					service.AddRecentlyPlayedEpisode(getApplicationContext(), application.CurrentEpisode);
				}
			}
		}
		// Log.w(LOG_TAG, "Playback service - updateNotification() stop");
	}
	
	
	// Get a notification object to display
	private Notification getNotification(String songTitle, String songArtist)
	{
		int icon = R.drawable.stat_notify_musicplayer;
		long when = 0; // Doesn't display a time
		Notification notification = new Notification(icon, songTitle, when);
		notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		Context c = getApplicationContext();

		Intent notificationIntent;
		notificationIntent = new Intent(this, RadioReddit.class);

		notificationIntent.setAction(Intent.ACTION_VIEW);
		notificationIntent.addCategory(Intent.CATEGORY_DEFAULT);
		// notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Useful test, use this and then have the service return to UI the current status of stream. This would cover the case of activity being destroyed by OS
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(c, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		notification.setLatestEventInfo(c, songTitle, songArtist, contentIntent);
		
		return notification;
	}

	synchronized public void pause()
	{
		Log.d(LOG_TAG, "pause");
		if(isPrepared)
		{
			mediaPlayer.pause();
		}
		notificationManager.cancel(NOTIFICATION_ID);
	}

	synchronized public void stop()
	{
		Log.d(LOG_TAG, "stop");
		if(isPrepared)
		{
			if(proxy != null)
			{
				proxy.stop();
				proxy = null;
			}
			mediaPlayer.stop();
			isPrepared = false;
		}
		
		if(mediaPlayer != null)
			mediaPlayer.release();
		
		mediaPlayer = null;
		cleanup();
	}

	// Used to stop the media player from playing once it is in the "prepared" state
	synchronized public void abort()
	{
		isAborting = true;
	}

	// Used to start the media player (after being aborted) while the MediaPlayer is still in "preparing" state
	synchronized public void stopAbort()
	{
		isAborting = false;
	}

	/**
	 * Start listening to the given URL.
	 */
	public void listen(String url, boolean stream) throws IllegalArgumentException, IllegalStateException, IOException
	{
		Log.w(LOG_TAG, "Playback service - listen() start");
		
		// First, clean up any existing audio.
		//if(isPlaying())
		if(mediaPlayer != null)
		{
			stop();
		}

		Log.d(LOG_TAG, "listening to " + url + " stream=" + stream);
		String playUrl = url;
		// From 2.2 on (SDK ver 8), the local mediaplayer can handle Shoutcast
		// streams natively. Let's detect that, and not proxy.
		Log.d(LOG_TAG, "SDK Version " + Build.VERSION.SDK);
		int sdkVersion = 0;
		try
		{
			sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		}
		catch(NumberFormatException e)
		{

		}

		if((stream && sdkVersion < 8) || Settings.getEnableCompatibilityMode(this))
		{
			Log.i(LOG_TAG, "Using proxy...");
			if(proxy == null)
			{
				proxy = new StreamProxy();
				proxy.init();
				proxy.start();
			}
			String proxyUrl = String.format("http://127.0.0.1:%d/%s", proxy.getPort(), url);
			playUrl = proxyUrl;
		}

		synchronized(this)
		{
			Log.d(LOG_TAG, "reset: " + playUrl);
			//mediaPlayer.reset();
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setOnBufferingUpdateListener(this);
			mediaPlayer.setOnCompletionListener(this);
			mediaPlayer.setOnErrorListener(this);
			mediaPlayer.setOnInfoListener(this);
			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.setDataSource(playUrl);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			Log.d(LOG_TAG, "Preparing: " + playUrl);
			mediaPlayer.prepareAsync();
			isPreparing = true;
			Log.d(LOG_TAG, "Waiting for prepare");
		}
		Log.w(LOG_TAG, "Playback service - listen() stop");
	}

	@Override
	public void onPrepared(MediaPlayer mp)
	{
		Log.d(LOG_TAG, "Prepared");
		synchronized(this)
		{
			if(mediaPlayer != null)
			{
				isPrepared = true;
				isPreparing = false;
			}
		}

		if(isAborting) // The user chose to stop the stream during Preparing state, so we are no longer going to play the stream
		{
			isAborting = false;
			return;
		}

		play();
		if(onPreparedListener != null)
		{
			onPreparedListener.onPrepared(mp);
		}

		startUpdateTimer();
	}

	private Runnable mUpdateTimeTask = new Runnable()
	{
		public void run()
		{
			updateProgress();
			boolean success = mHandler.postDelayed(this, 100); // 1/10 second
		}
	};

	private void startUpdateTimer()
	{
		Log.i(LOG_TAG, "Begin startUpdateTimer()");
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.postDelayed(mUpdateTimeTask, 100);
		Log.i(LOG_TAG, "End startUpdateTimer()");
	}

	private void stopUpdateTimer()
	{
		Log.i(LOG_TAG, "Begin stopUpdateTimer()");
		mHandler.removeCallbacks(mUpdateTimeTask);
		Log.i(LOG_TAG, "Stop stopUpdateTimer()");
	}

	@Override
	public void onDestroy()
	{
		Log.w(LOG_TAG, "Playback service - onDestroy() start");
		super.onDestroy();
		Log.w(LOG_TAG, "Service exiting");

		stopUpdateTimer();

		stop();
		synchronized(this)
		{
			if(mediaPlayer != null)
			{
				mediaPlayer.release();
				mediaPlayer = null;
			}
		}

		telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);

		try
		{
			unregisterReceiver(headsetReceiver);
		}
		catch(IllegalArgumentException ex)
		{
			// do nothing, there is no other way to tell if a receiver was registered before or not
		}

		try
		{
			unregisterReceiver(connectivityListener);
		}
		catch(IllegalArgumentException ex)
		{
			// do nothing, there is no other way to tell if a receiver was registered before or not
		}

		Log.w(LOG_TAG, "Playback service - onDestroy() stop");
	}

	public class ListenBinder extends Binder
	{
		public PlaybackService getService()
		{
			return PlaybackService.this;
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int progress)
	{
		// Log.w(LOG_TAG, "Buffering progress:" + String.valueOf(progress));
		lastBufferPercent = progress;
		updateProgress();
	}

	/**
	 * Sends an UPDATE broadcast with the latest info.
	 */
	private void updateProgress()
	{
		// Log.w(LOG_TAG, "Playback service - updateProgress() start");
		if(isPrepared && mediaPlayer != null && mediaPlayer.isPlaying())
		{
			// Check if buffering
			if(mediaPlayer.getCurrentPosition() == mLastCurrentPosition)
			{
				if(mFirstDuplicateCurrentPositionMillis == 0)
				{
					mFirstDuplicateCurrentPositionMillis = SystemClock.elapsedRealtime();
				}

				if((SystemClock.elapsedRealtime() - mFirstDuplicateCurrentPositionMillis) > 500)
				{
					isBuffering = true;
				}
				else
				{
					isBuffering = false;
				}
			}
			else
			{
				isBuffering = false;
				mFirstDuplicateCurrentPositionMillis = 0;
			}

			mLastCurrentPosition = mediaPlayer.getCurrentPosition();

			// Update song information
			RadioRedditApplication application = (RadioRedditApplication) getApplication();			
			
			// Update song information every 30 seconds
			if((SystemClock.elapsedRealtime() - mLastCurrentSongInformationUpdateMillis) > 30000)
			{
				if(application.playBackType.equals("stream"))
				{
					if(application.CurrentStream.Type.equals("music"))
						new GetCurrentSongInformationTask(application, this, Locale.getDefault()).execute();
					else if(application.CurrentStream.Type.equals("talk"))
						new GetCurrentEpisodeInformationTask(application, this, Locale.getDefault()).execute();
				}
				else // selected song
				{
					new GetVoteScoreTask(application, this, null, 0, application.CurrentSong, null).execute();
				}

				mLastCurrentSongInformationUpdateMillis = SystemClock.elapsedRealtime();
			}


			// Update notification
			updateNotification();

			// Update broadcasts are sticky, so when a new receiver connects, it will have the data without polling.
			if(lastUpdateBroadcast != null)
			{
				getApplicationContext().removeStickyBroadcast(lastUpdateBroadcast);
			}
			lastUpdateBroadcast = new Intent(SERVICE_UPDATE_NAME);
			lastUpdateBroadcast.putExtra(EXTRA_BUFFERED, lastBufferPercent);

			// int position = mediaPlayer.getCurrentPosition();
			// int duration = mediaPlayer.getDuration();
			// lastUpdateBroadcast.putExtra(EXTRA_DURATION, mediaPlayer.getDuration());
			// lastUpdateBroadcast.putExtra(EXTRA_DOWNLOADED,(int) ((lastBufferPercent / 100.0) * mediaPlayer.getDuration()));
			// lastUpdateBroadcast.putExtra(EXTRA_POSITION, mediaPlayer.getCurrentPosition());
			getApplicationContext().sendStickyBroadcast(lastUpdateBroadcast);
		}
		// Log.w(LOG_TAG, "Playback service - updateProgress() stop");
	}

	@Override
	public void onCompletion(MediaPlayer mp)
	{
		Log.w(LOG_TAG, "Playback service - onCompletion() start");

		synchronized(this)
		{
			if(!isPrepared)
			{
				// This file was not good and MediaPlayer quit
				Log.w(LOG_TAG, "MediaPlayer refused to play current item. Bailing on prepare.");
			}
		}

		cleanup();

		if(onCompletionListener != null)
		{
			onCompletionListener.onCompletion(mp);
		}

		if(lostDataConnection)// && bufferPercent < 99)
		{
			Log.v(LOG_TAG, "Track ran out of data, pausing");
			stop();
			// pause();
			// mp.release();
			// mp = null;
			ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo activeni = cm.getActiveNetworkInfo();
			if(activeni != null && activeni.isConnected())
			{
				Log.v(LOG_TAG, "Another data connection is available, attempting to resume");
				RadioRedditApplication application = (RadioRedditApplication) getApplication();
				try
				{
					listen(application.CurrentStream.Relay, true);// play();
					// TODO: needs to return a reconnecting state or similar to the UI so it knows we are connecting again
				}
				catch(Exception ex)
				{
					// failed none the less
				}
				// pause();
				lostDataConnection = false;
			}
		}

		if(bindCount == 0 && !isPlaying())
		{
			stopSelf();
		}

		Log.w(LOG_TAG, "Playback service - onCompletion() stop");
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra)
	{
		Log.w(LOG_TAG, "onError(" + what + ", " + extra + ")");
		synchronized(this)
		{
			if(!isPrepared)
			{
				// This file was not good and MediaPlayer quit
				Log.w(LOG_TAG, "MediaPlayer refused to play current item. Bailing on prepare.");
			}
		}

		Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_StreamConnectionError), Toast.LENGTH_LONG).show();

		return false; // false causes the onCompletion handler to be called. True means we handled the error
	}

	@Override
	public boolean onInfo(MediaPlayer arg0, int arg1, int arg2)
	{
		Log.w(LOG_TAG, "onInfo(" + arg1 + ", " + arg2 + ")");
		return false;
	}

	/**
	 * Remove all intents and notifications about the last media.
	 */
	private void cleanup()
	{
		Log.w(LOG_TAG, "Playback service - cleanup() start");
		
		// Stop notifications and service from running on foreground
		notificationManager.cancel(NOTIFICATION_ID);
		stopForeground(true);
		
		// Clear song information
		RadioRedditApplication application = (RadioRedditApplication) getApplication();
		application.CurrentEpisode = null;
		application.CurrentSong = null;
		lastSongTitle = "";
		lastSongArtist = "";
		
		// Stop broadcasts
		if(lastChangeBroadcast != null)
		{
			getApplicationContext().removeStickyBroadcast(lastChangeBroadcast);
		}
		if(lastUpdateBroadcast != null)
		{
			getApplicationContext().removeStickyBroadcast(lastUpdateBroadcast);
		}
		getApplicationContext().sendBroadcast(new Intent(SERVICE_CLOSE_NAME));

		// Stop locks on wifi/wake
		if(mWakeLock != null && mWakeLock.isHeld() == true)
			mWakeLock.release();

		if(mWifiLock != null && mWifiLock.isHeld() == true)
			mWifiLock.release();

		Log.w(LOG_TAG, "Playback service - cleanup() stop");
	}

	// -----------
	// Telephony listener
	public class RadioRedditPhoneStateListener extends PhoneStateListener
	{
		@Override
		public void onCallStateChanged(int state, String incomingNumber)
		{
			switch(state)
			{
				case TelephonyManager.CALL_STATE_OFFHOOK:
				case TelephonyManager.CALL_STATE_RINGING:
					// Phone going offhook or ringing, pause the player.
					if(isPlaying())
					{
						pause();
						isPausedInCall = true;
					}
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					// Phone idle. Rewind a couple of seconds and start playing.
					if(isPausedInCall)
					{
						seekTo(Math.max(0, getPosition() - RESUME_REWIND_TIME));
						play();
					}
					break;
			}
		}
	}

	// -----------
	// Connection receiver
	// Borrowed from Last.FM: https://github.com/c99koder/lastfm-android/blob/master/app/src/fm/last/android/player/RadioPlayerService.java
	BroadcastReceiver connectivityListener = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			NetworkInfo ni = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

			if(ni.getState() == NetworkInfo.State.DISCONNECTED || ni.getState() == NetworkInfo.State.SUSPENDED)
			{
				if(isPlaying())// mState != STATE_STOPPED && mState != STATE_ERROR && mState != STATE_PAUSED)
				{
					ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
					NetworkInfo activeni = cm.getActiveNetworkInfo();
					if(activeni != null && activeni.isConnected())
					{
						Log.v(LOG_TAG, "A network other than the active network has disconnected, ignoring");
						lostDataConnection = true; // This is necessary. If you go from 3g to wifi it won't think the active network is being disconnected (even though wifi is "taking over")
						return;
					}
					// Ignore disconnections that don't change our WiFi / cell state
					if((ni.getType() == ConnectivityManager.TYPE_WIFI) != mDoHasWiFi)
					{
						return;
					}

					// We just lost the WiFi connection so update our state
					if(ni.getType() == ConnectivityManager.TYPE_WIFI)
						mDoHasWiFi = false;

					Log.v(LOG_TAG, "Data connection lost! Type: " + ni.getTypeName() + " Subtype: " + ni.getSubtypeName() + "Extra Info: " + ni.getExtraInfo() + " Reason: " + ni.getReason());
					lostDataConnection = true;
				}
			}
			else if(ni.getState() == NetworkInfo.State.CONNECTED && isPlaying())// mState != STATE_STOPPED && mState != STATE_ERROR)
			{
				if(lostDataConnection || ni.isFailover() || ni.getType() == ConnectivityManager.TYPE_WIFI)
				{
					if(ni.getType() == ConnectivityManager.TYPE_WIFI)
					{
						if(!mDoHasWiFi)
							mDoHasWiFi = true;
						else
							return;
					}
					Log.v(LOG_TAG, "New data connection attached! Type: " + ni.getTypeName() + " Subtype: " + ni.getSubtypeName() + "Extra Info: " + ni.getExtraInfo() + " Reason: " + ni.getReason());
					if(lostDataConnection)
					{
						if(!isPlaying())// mState == STATE_PAUSED)
						{
							play();// pause();
							lostDataConnection = false;
						}
					}
				}
			}
		}
	};

	// -----------
	// Headset receiver
	public class HeadsetBroadcastReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{

			String action = intent.getAction();
			if((action.compareTo(Intent.ACTION_HEADSET_PLUG)) == 0)
			{
				int headSetState = intent.getIntExtra("state", 0);
				// int hasMicrophone = intent.getIntExtra("microphone", 0);
				if(headSetState == 0)
				{
					if(isPrepared && mediaPlayer.isPlaying())
					{
						stop();
					}
				}
			}

		}
	}

	// -----------
	// Some stuff added for inspection when testing

	private OnCompletionListener onCompletionListener;

	/**
	 * Allows a class to be notified when the currently playing track is completed. Mostly used for testing the service
	 * 
	 * @param listener
	 */
	public void setOnCompletionListener(OnCompletionListener listener)
	{
		onCompletionListener = listener;
	}

	private OnPreparedListener onPreparedListener;

	/**
	 * Allows a class to be notified when the currently selected track has been prepared to start playing. Mostly used for testing.
	 * 
	 * @param listener
	 */
	public void setOnPreparedListener(OnPreparedListener listener)
	{
		onPreparedListener = listener;
	}
}
