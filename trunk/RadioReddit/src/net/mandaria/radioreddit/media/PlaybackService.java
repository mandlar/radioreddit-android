// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package net.mandaria.radioreddit.media;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.R.drawable;
import net.mandaria.radioreddit.R.string;
import net.mandaria.radioreddit.activities.RadioReddit;

public class PlaybackService extends Service implements OnPreparedListener,
    OnBufferingUpdateListener, OnCompletionListener, OnErrorListener,
    OnInfoListener 
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
  private Thread updateProgressThread;
  
  private HeadsetBroadcastReceiver headsetReceiver;
  
  private PowerManager.WakeLock mWakeLock;
  private WifiManager.WifiLock mWifiLock;

  // Amount of time to rewind playback when resuming after call 
  private final static int RESUME_REWIND_TIME = 3000;

  @Override
  public void onCreate() 
  {
    mediaPlayer = new MediaPlayer();
    mediaPlayer.setOnBufferingUpdateListener(this);
    mediaPlayer.setOnCompletionListener(this);
    mediaPlayer.setOnErrorListener(this);
    mediaPlayer.setOnInfoListener(this);
    mediaPlayer.setOnPreparedListener(this);
    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    Log.w(LOG_TAG, "Playback service created");

    telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
    // Create a PhoneStateListener to watch for offhook and idle events
    listener = new PhoneStateListener() // TODO: pull listener into its own class
    {
      @Override
      public void onCallStateChanged(int state, String incomingNumber) 
      {
        switch (state) 
        {
        	case TelephonyManager.CALL_STATE_OFFHOOK:
        	case TelephonyManager.CALL_STATE_RINGING:
        		// Phone going offhook or ringing, pause the player.
        		if (isPlaying()) 
        		{
        			pause();
        			isPausedInCall = true;
        		}
        		break;
        	case TelephonyManager.CALL_STATE_IDLE:
        		// Phone idle. Rewind a couple of seconds and start playing.
        		if (isPausedInCall) 
        		{
        			seekTo(Math.max(0, getPosition() - RESUME_REWIND_TIME));
        			play();
        		}
        		break;
        }
      }
    };

    // Register the listener with the telephony manager.
    telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
    
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
    if (!isPlaying() && bindCount == 0) 
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

  synchronized public boolean isPlaying() 
  {
    if (isPrepared) 
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
    if (isPrepared) {
      return mediaPlayer.getCurrentPosition();
    }
    return 0;
  }

  synchronized public int getDuration() 
  {
    if (isPrepared) 
    {
      return mediaPlayer.getDuration();
    }
    return 0;
  }

  synchronized public int getCurrentPosition() 
  {
    if (isPrepared) 
    {
      return mediaPlayer.getCurrentPosition();
    }
    return 0;
  }

  synchronized public void seekTo(int pos) 
  {
    if (isPrepared) 
    {
      mediaPlayer.seekTo(pos);
    }
  }

  synchronized public void play() 
  {
    if (!isPrepared) 
    {
      Log.e(LOG_TAG, "play - not prepared");
      return;
    }

    mediaPlayer.start();
    
    // Activate wifi lock to prevent phone from losing wifi conection when screen is off
    WifiManager wm = ((WifiManager)getSystemService( WIFI_SERVICE ));
    mWifiLock = wm.createWifiLock( WifiManager.WIFI_MODE_FULL , LOG_TAG );
    mWifiLock.acquire();
    
    // Activate wake lock to prevent phone from losing wifi connection when screen is off. This will keep CPU running 
    PowerManager pm = (PowerManager)getSystemService (Context.POWER_SERVICE); 
    mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOG_TAG); 
    mWakeLock.acquire(); 

    int icon = R.drawable.stat_notify_musicplayer;
    CharSequence contentText = "Radio Station Name Here?"; // TODO: show station / song name?
    long when = System.currentTimeMillis();
    Notification notification = new Notification(icon, contentText, when);
    notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
    Context c = getApplicationContext();
    CharSequence title = getString(R.string.app_name);
    Intent notificationIntent;
    notificationIntent = new Intent(this, RadioReddit.class);
      
    notificationIntent.setAction(Intent.ACTION_VIEW);
    notificationIntent.addCategory(Intent.CATEGORY_DEFAULT);
    //notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // TODO: as a test, use this and then have the service return to UI the current status of stream. This would cover the case of activity being destroyed by OS
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent contentIntent = PendingIntent.getActivity(c, 0,  notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    notification.setLatestEventInfo(c, title, contentText, contentIntent);
    notificationManager.notify(NOTIFICATION_ID, notification);

    // Change broadcasts are sticky, so when a new receiver connects, it will
    // have the data without polling.
    if (lastChangeBroadcast != null) 
    {
      getApplicationContext().removeStickyBroadcast(lastChangeBroadcast);
    }
    lastChangeBroadcast = new Intent(SERVICE_CHANGE_NAME);
    //lastChangeBroadcast.putExtra(EXTRA_TITLE, current.title);
    getApplicationContext().sendStickyBroadcast(lastChangeBroadcast); // broadcasts that playing has started
  }

  synchronized public void pause() 
  {
    Log.d(LOG_TAG, "pause");
    if (isPrepared) 
    {
      mediaPlayer.pause();
    }
    notificationManager.cancel(NOTIFICATION_ID);
  }

  synchronized public void stop() 
  {
    Log.d(LOG_TAG, "stop");
    if (isPrepared) 
    {
      if (proxy != null) 
      {
        proxy.stop();
        proxy = null;
      }
      mediaPlayer.stop();
      isPrepared = false;
    }
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
  public void listen(String url, boolean stream)
      throws IllegalArgumentException, IllegalStateException, IOException 
  {
    // First, clean up any existing audio.
    if (isPlaying()) 
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
    catch (NumberFormatException e) 
    {
    	
    }

    if (stream && sdkVersion < 8) 
    {
      if (proxy == null) 
      {
        proxy = new StreamProxy();
        proxy.init();
        proxy.start();
      }
      String proxyUrl = String.format("http://127.0.0.1:%d/%s", proxy.getPort(), url);
      playUrl = proxyUrl;
    }

    synchronized (this) 
    {
      Log.d(LOG_TAG, "reset: " + playUrl);
      mediaPlayer.reset();
      mediaPlayer.setDataSource(playUrl);
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
      Log.d(LOG_TAG, "Preparing: " + playUrl);
      mediaPlayer.prepareAsync();
      isPreparing = true;
      Log.d(LOG_TAG, "Waiting for prepare");
    }
  }

  @Override
  public void onPrepared(MediaPlayer mp) 
  {
    Log.d(LOG_TAG, "Prepared");
    synchronized (this) 
    {
      if (mediaPlayer != null) 
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
    if (onPreparedListener != null) 
    {
      onPreparedListener.onPrepared(mp);
    }

    updateProgressThread = new Thread(new Runnable() 
    {
      public void run() 
      {
        // Initially, don't send any updates, since it takes a while for the
        // media player to settle down. 
        try 
        {
          Thread.sleep(2000);
        } 
        catch (InterruptedException e) 
        {
          return;
        }
        while (true) 
        {
          updateProgress();
          try 
          {
            Thread.sleep(500);
          } 
          catch (InterruptedException e) 
          {
            break;
          }
        }
      }
    });
    updateProgressThread.start();
  }

  @Override
  public void onDestroy() 
  {
    super.onDestroy();
    Log.w(LOG_TAG, "Service exiting");

    if (updateProgressThread != null) 
    {
      updateProgressThread.interrupt();
      try 
      {
        updateProgressThread.join(3000);
      } 
      catch (InterruptedException e) 
      {
        Log.e(LOG_TAG, "", e);
      }
    }

    stop();
    synchronized (this) 
    {
      if (mediaPlayer != null) 
      {
        mediaPlayer.release();
        mediaPlayer = null;
      }
    }

    telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);
    
    unregisterReceiver(headsetReceiver);
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
	  //Log.e("radio reddit buffer", String.valueOf(progress));
	lastBufferPercent = progress;
	updateProgress();
    if (isPrepared) 
    {
      
      
      // This does weird things with live stream, such as repeating the last 10 second interval, over and over and over
      if(!mediaPlayer.isPlaying() && progress > 50)
      {
    	  if(isPrepared)
    	  {
    		  //Toast.makeText(this, "Starting...", Toast.LENGTH_LONG).show();
    		  //mediaPlayer.start();
    	  }
    	  
    	  return;
      }
    }
  }

  /**
   * Sends an UPDATE broadcast with the latest info.
   */
  private void updateProgress() 
  {
    if (isPrepared && mediaPlayer != null && mediaPlayer.isPlaying()) {
      // Update broadcasts are sticky, so when a new receiver connects, it will
      // have the data without polling.
      if (lastUpdateBroadcast != null) {
        getApplicationContext().removeStickyBroadcast(lastUpdateBroadcast);
      }
      lastUpdateBroadcast = new Intent(SERVICE_UPDATE_NAME);
      lastUpdateBroadcast.putExtra(EXTRA_BUFFERED, lastBufferPercent);
      
      
      //int position = mediaPlayer.getCurrentPosition();
      //int duration = mediaPlayer.getDuration();
      //lastUpdateBroadcast.putExtra(EXTRA_DURATION, mediaPlayer.getDuration());
//      lastUpdateBroadcast.putExtra(EXTRA_DOWNLOADED,
//          (int) ((lastBufferPercent / 100.0) * mediaPlayer.getDuration()));
      //lastUpdateBroadcast.putExtra(EXTRA_POSITION,
      //    mediaPlayer.getCurrentPosition());
      getApplicationContext().sendStickyBroadcast(lastUpdateBroadcast);
    }
  }
  
  @Override
  public void onCompletion(MediaPlayer mp) 
  {
    Log.w(LOG_TAG, "onComplete()");

    synchronized (this) 
    {
      if (!isPrepared) 
      {
        // This file was not good and MediaPlayer quit
        Log.w(LOG_TAG, "MediaPlayer refused to play current item. Bailing on prepare.");
      }
    }

    cleanup();
    
    if (onCompletionListener != null) 
    {
      onCompletionListener.onCompletion(mp);
    }

    if (bindCount == 0 && !isPlaying()) 
    {
      stopSelf();
    }
  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) 
  {
    Log.w(LOG_TAG, "onError(" + what + ", " + extra + ")");
    synchronized (this) 
    {
      if (!isPrepared) 
      {
        // This file was not good and MediaPlayer quit
        Log.w(LOG_TAG, "MediaPlayer refused to play current item. Bailing on prepare.");
      }
    }
    return false;
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
    notificationManager.cancel(NOTIFICATION_ID);
    if (lastChangeBroadcast != null) 
    {
      getApplicationContext().removeStickyBroadcast(lastChangeBroadcast);
    }
    if (lastUpdateBroadcast != null) 
    {
      getApplicationContext().removeStickyBroadcast(lastUpdateBroadcast);
    }
    getApplicationContext().sendBroadcast(new Intent(SERVICE_CLOSE_NAME));
    
    if(mWakeLock != null && mWakeLock.isHeld() == true)
    	 mWakeLock.release(); 
    
    if(mWifiLock != null && mWifiLock.isHeld() == true)
    	mWifiLock.release();
  }
  
  // -----------
  // Headset receiver
  public class HeadsetBroadcastReceiver extends BroadcastReceiver
  {

      @Override
      public void onReceive(Context context, Intent intent) 
      {

          String action = intent.getAction();
          if( (action.compareTo(Intent.ACTION_HEADSET_PLUG))  == 0)   
          {
              int headSetState = intent.getIntExtra("state", 0);      
              //int hasMicrophone = intent.getIntExtra("microphone", 0);
              if( headSetState == 0)
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
   * Allows a class to be notified when the currently playing track is
   * completed. Mostly used for testing the service
   * 
   * @param listener
   */
  public void setOnCompletionListener(OnCompletionListener listener) 
  {
    onCompletionListener = listener;
  }

  private OnPreparedListener onPreparedListener;

  /**
   * Allows a class to be notified when the currently selected track has been
   * prepared to start playing. Mostly used for testing.
   * 
   * @param listener
   */
  public void setOnPreparedListener(OnPreparedListener listener) 
  {
    onPreparedListener = listener;
  }
}
