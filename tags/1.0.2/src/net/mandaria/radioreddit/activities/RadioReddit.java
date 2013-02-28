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

package net.mandaria.radioreddit.activities;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.FlurryAgent;

import net.mandaria.radioreddit.tasks.GetAdRefreshRateTask;
import net.mandaria.radioreddit.tasks.GetInHouseAdsPercentageTask;
import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.data.DatabaseService;
import net.mandaria.radioreddit.media.PlaybackService;
import net.mandaria.radioreddit.media.StreamProxy;
import net.mandaria.radioreddit.objects.RadioEpisode;
import net.mandaria.radioreddit.objects.RadioSong;
import net.mandaria.radioreddit.objects.RadioStreams;
import net.mandaria.radioreddit.objects.RedditAccount;
import net.mandaria.radioreddit.tasks.GetRadioStreamsTask;
import net.mandaria.radioreddit.tasks.VoteOnSongTask;
import net.mandaria.radioreddit.tasks.VoteRedditTask;
import net.mandaria.radioreddit.tasks.VoteOnEpisodeTask;
import net.mandaria.radioreddit.utils.ActivityUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class RadioReddit extends SherlockActivity
{
	TextView lbl_SongVote;
	TextView lbl_SongTitle;
	TextView lbl_SongArtist;
	TextView lbl_SongPlaylist;
	ImageView btn_SongInfo;
	TextView lbl_Buffering;
	TextView lbl_Connecting;
	
	WebView adView;
	LinearLayout div_ads;

	ProgressBar progress_LoadingSong;
	ImageView img_Logo;

	Button btn_play;
	Button btn_downvote;
	Button btn_upvote;
	StreamProxy proxy;

	private String LOG_TAG = "RadioReddit";

	private Handler mHandler = new Handler();
	private long mLastStreamsInformationUpdateMillis = 0;
	private long mLastAdServedMillis = 0; // last ad update
	private boolean isStreamCacheLoaded = false;

	private PlaybackService player;
	private ServiceConnection conn;
	private BroadcastReceiver changeReceiver = new PlaybackChangeReceiver();
	private BroadcastReceiver updateReceiver = new PlaybackUpdateReceiver();
	private BroadcastReceiver closeReceiver = new PlaybackCloseReceiver();
	
	@Override
	public void onStart()
	{
	   super.onStart();
	   FlurryAgent.onStartSession(this, getString(R.string.flurrykey));
	   
	   UpdateAdValues();
	}
	
	@Override
	public void onStop()
	{
	   super.onStop();
	   FlurryAgent.onEndSession(this);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		RadioRedditApplication application = (RadioRedditApplication) getApplication();

		new GetRadioStreamsTask(application, RadioReddit.this, Locale.getDefault()).execute();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

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
				FlurryAgent.onEvent("radio reddit - View Episode Info");
				ViewEpisodeInfo();
			}
		});

		lbl_Buffering = (TextView) findViewById(R.id.lbl_Buffering);
		lbl_Connecting = (TextView) findViewById(R.id.lbl_Connecting);
		img_Logo = (ImageView) findViewById(R.id.img_Logo);
		progress_LoadingSong = (ProgressBar) findViewById(R.id.progress_LoadingSong);
		adView = (WebView)findViewById(R.id.ad);
		div_ads = (LinearLayout)findViewById(R.id.div_ads);

		btn_upvote = (Button) findViewById(R.id.btn_upvote);
		btn_upvote.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				RadioRedditApplication application = (RadioRedditApplication) getApplication();
				// TODO: when do we allow them to vote? only when song info is available?
				if(application.CurrentSong != null || application.CurrentEpisode != null)
				{
					// TODO: consolidate ifs better
					if(application.CurrentStream.Type.equals("music"))
					{
						if(application.CurrentSong != null &&application.CurrentSong.Name.equals(""))
						{
							// TODO: show dialog warning
							final AlertDialog.Builder builder = new AlertDialog.Builder(RadioReddit.this);
						    builder.setMessage(getString(R.string.warning_SubmitSong) + "\n\n" + getString(R.string.warning_SubmitNote))
						    	.setTitle(getString(R.string.submit_title))
						    	.setIcon(android.R.drawable.ic_dialog_alert)
						    	.setCancelable(true)
						        .setPositiveButton(getString(R.string.submit), new DialogInterface.OnClickListener()
								{
									
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										RadioRedditApplication application = (RadioRedditApplication)getApplication();
										
										if(application.playBackType.equals("stream"))
											new VoteRedditTask(application, RadioReddit.this, true, "", "").execute();
										else if(application.playBackType.equals("song"))
										{
											new VoteOnSongTask(application, RadioReddit.this, application.CurrentSong, true, "", "").execute();
											application.CurrentSong.Likes = "true";
										}
										else if(application.playBackType.equals("episode"))
										{
											new VoteOnEpisodeTask(application, RadioReddit.this, application.CurrentEpisode, true, "", "").execute();
											application.CurrentEpisode.Likes = "true";
										}
										
										setUpOrDownVote("true");
										
									}
								})
						        .setNegativeButton(getString(R.string.no), null);
						    
						    final AlertDialog alert = builder.create();
						    alert.show();
						}
						else
						{
							if(application.playBackType.equals("stream"))
								new VoteRedditTask(application, RadioReddit.this, true, "", "").execute();
							else if(application.playBackType.equals("song"))
							{
								new VoteOnSongTask(application, RadioReddit.this, application.CurrentSong, true, "", "").execute();
								application.CurrentSong.Likes = "true";
							}
							else if(application.playBackType.equals("episode"))
							{
								new VoteOnEpisodeTask(application, RadioReddit.this, application.CurrentEpisode, true, "", "").execute();
								application.CurrentEpisode.Likes = "true";
							}
								
							setUpOrDownVote("true");
						}
					}
					else if(application.CurrentStream.Type.equals("talk"))
					{
						if(application.CurrentEpisode != null && application.CurrentEpisode.Name.equals(""))
						{
							final AlertDialog.Builder builder = new AlertDialog.Builder(RadioReddit.this);
						    builder.setMessage(getString(R.string.warning_SubmitEpisode) + "\n\n" + getString(R.string.warning_SubmitNote))
						    	.setTitle(getString(R.string.submit_title))
						    	.setIcon(android.R.drawable.ic_dialog_alert)
						    	.setCancelable(true)
						        .setPositiveButton(getString(R.string.submit), new DialogInterface.OnClickListener()
								{
									
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										RadioRedditApplication application = (RadioRedditApplication)getApplication();
										
										if(application.playBackType.equals("stream"))
											new VoteRedditTask(application, RadioReddit.this, true, "", "").execute();
										else if(application.playBackType.equals("song"))
										{
											new VoteOnSongTask(application, RadioReddit.this, application.CurrentSong, true, "", "").execute(); // TODO: replace with VoteOnEpisodeTask
											application.CurrentSong.Likes = "true";
										}
										else if(application.playBackType.equals("episode"))
										{
											new VoteOnEpisodeTask(application, RadioReddit.this, application.CurrentEpisode, true, "", "").execute();
											application.CurrentEpisode.Likes = "true";
										}
										
										setUpOrDownVote("true");
										
									}
								})
						        .setNegativeButton(getString(R.string.no), null);
						    
						    final AlertDialog alert = builder.create();
						    alert.show();
						}	
						else
						{
							if(application.playBackType.equals("stream"))
								new VoteRedditTask(application, RadioReddit.this, true, "", "").execute();
							else if(application.playBackType.equals("song"))
							{
								new VoteOnSongTask(application, RadioReddit.this, application.CurrentSong, true, "", "").execute(); // TODO: replace with VoteOnEpisodeTask
								application.CurrentSong.Likes = "true";
							}
							else if(application.playBackType.equals("episode"))
							{
								new VoteOnEpisodeTask(application, RadioReddit.this, application.CurrentEpisode, true, "", "").execute();
								application.CurrentEpisode.Likes = "true";
							}
							
							setUpOrDownVote("true");
						}
					}
				}
			}
		});
		
		btn_downvote = (Button) findViewById(R.id.btn_downvote);
		btn_downvote.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				RadioRedditApplication application = (RadioRedditApplication) getApplication();
				// TODO: when do we allow them to vote? only when song info is available?
				if(application.CurrentSong != null || application.CurrentEpisode != null)
				{
					// TODO: consolidate ifs better
					// TODO: consolidate with btn_upvote function
					if(application.CurrentStream.Type.equals("music"))
					{
						if(application.CurrentSong.Name.equals(""))
						{
							// TODO: show dialog warning
							final AlertDialog.Builder builder = new AlertDialog.Builder(RadioReddit.this);
						    builder.setMessage(getString(R.string.warning_SubmitSong) + "\n\n" + getString(R.string.warning_SubmitNote))
						    	.setTitle(getString(R.string.submit_title))
						    	.setIcon(android.R.drawable.ic_dialog_alert)
						    	.setCancelable(true)
						        .setPositiveButton(getString(R.string.submit), new DialogInterface.OnClickListener()
								{
									
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										RadioRedditApplication application = (RadioRedditApplication)getApplication();
										
										if(application.playBackType.equals("stream"))
											new VoteRedditTask(application, RadioReddit.this, false, "", "").execute();
										else if(application.playBackType.equals("song"))
										{
											new VoteOnSongTask(application, RadioReddit.this, application.CurrentSong, false, "", "").execute();
											application.CurrentSong.Likes = "false";
										}
										else if(application.playBackType.equals("episode"))
										{
											new VoteOnEpisodeTask(application, RadioReddit.this, application.CurrentEpisode, false, "", "").execute();
											application.CurrentEpisode.Likes = "false";
										}
										
										setUpOrDownVote("false");
										
									}
								})
						        .setNegativeButton(getString(R.string.no), null);
						    
						    final AlertDialog alert = builder.create();
						    alert.show();
						}
						else
						{
							if(application.playBackType.equals("stream"))
								new VoteRedditTask(application, RadioReddit.this, false, "", "").execute();
							else if(application.playBackType.equals("song"))
							{
								new VoteOnSongTask(application, RadioReddit.this, application.CurrentSong, false, "", "").execute();
								application.CurrentSong.Likes = "false";
							}
							else if(application.playBackType.equals("episode"))
							{
								new VoteOnEpisodeTask(application, RadioReddit.this, application.CurrentEpisode, false, "", "").execute();
								application.CurrentEpisode.Likes = "false";
							}
							
							setUpOrDownVote("false");
						}
					}
					else if(application.CurrentStream.Type.equals("talk"))
					{
						if(application.CurrentEpisode.Name.equals(""))
						{
							final AlertDialog.Builder builder = new AlertDialog.Builder(RadioReddit.this);
						    builder.setMessage(getString(R.string.warning_SubmitEpisode) + "\n\n" + getString(R.string.warning_SubmitNote))
						    	.setTitle(getString(R.string.submit_title))
						    	.setIcon(android.R.drawable.ic_dialog_alert)
						    	.setCancelable(true)
						        .setPositiveButton(getString(R.string.submit), new DialogInterface.OnClickListener()
								{
									
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										RadioRedditApplication application = (RadioRedditApplication)getApplication();
										
										if(application.playBackType.equals("stream"))
											new VoteRedditTask(application, RadioReddit.this, false, "", "").execute();
										else if(application.playBackType.equals("song"))
										{
											new VoteOnSongTask(application, RadioReddit.this, application.CurrentSong, false, "", "").execute(); // TODO: replace with VoteOnEpisodeTask
											application.CurrentSong.Likes = "false";
										}
										else if(application.playBackType.equals("episode"))
										{
											new VoteOnEpisodeTask(application, RadioReddit.this, application.CurrentEpisode, false, "", "").execute();
											application.CurrentEpisode.Likes = "false";
										}
										
										setUpOrDownVote("false");
										
									}
								})
						        .setNegativeButton(getString(R.string.no), null);
						    
						    final AlertDialog alert = builder.create();
						    alert.show();
						}	
						else
						{
							if(application.playBackType.equals("stream"))
								new VoteRedditTask(application, RadioReddit.this, false, "", "").execute();
							else if(application.playBackType.equals("song"))
							{
								new VoteOnSongTask(application, RadioReddit.this, application.CurrentSong, false, "", "").execute(); // TODO: replace with VoteOnEpisodeTask
								application.CurrentSong.Likes = "false";
							}
							else if(application.playBackType.equals("episode"))
							{
								new VoteOnEpisodeTask(application, RadioReddit.this, application.CurrentEpisode, false, "", "").execute();
								application.CurrentEpisode.Likes = "false";
							}
							
							setUpOrDownVote("false");
						}
					}
				}
			}
		});
		
		btn_play = (Button) findViewById(R.id.btn_play);
		btn_play.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO: Might need to re-write this function due to PlayerService changes
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
							FlurryAgent.onEvent("radio reddit - Stop Button");
							player.stop();

							hideSongInformation();

							showPlayButton();

							lbl_Buffering.setVisibility(View.GONE);
							progress_LoadingSong.setVisibility(View.GONE);
						}
						else
						{
							FlurryAgent.onEvent("radio reddit - Play Button");
							playStream();
						}
					}
					else
					{
						// But we can tell the media player we actually don't want to start playing, we changed our mind
						if(!player.isAborting())
						{
							FlurryAgent.onEvent("radio reddit - Stop Button - Abort");
							// Mediaplayer is preparing, we want to not stream
							player.abort();

							hideSongInformation();

							showPlayButton();

							lbl_Buffering.setVisibility(View.GONE);
							progress_LoadingSong.setVisibility(View.GONE);
						}
						else
						{
							FlurryAgent.onEvent("radio reddit - Play Button - Stop Abort");
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
		MenuItem login = (MenuItem) menu.findItem(R.id.login);
		MenuItem logout = (MenuItem) menu.findItem(R.id.logout);
		
		RedditAccount account = Settings.getRedditAccount(RadioReddit.this);
		
		if(account != null)
		{
			login.setVisible(false);
			logout.setVisible(true);
			
			logout.setTitle(getString(R.string.logout) + " (" + account.Username + ")");
		}
		else
		{
			login.setVisible(true);
			logout.setVisible(false);
		}

		// Connecting to radio reddit
		if(application.RadioStreams == null || application.RadioStreams.size() == 0)
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

		// TODO: maybe check if there is enough room to write the full text "Current station: main"
		//getResources().getString(R.string.currentStation) + ": " +
		if(application.CurrentStream != null)
			getSupportActionBar().setTitle( application.CurrentStream.Name);

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		FlurryAgent.onEvent("radio reddit - Menu Button");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.login) 
		{
			FlurryAgent.onEvent("radio reddit - Menu Button - Login");
			startActivity(new Intent(this, Login.class));
			return true;
		}
		else if (item.getItemId() == R.id.logout) 
		{
			FlurryAgent.onEvent("radio reddit - Menu Button - Logout");
			Logout();
			return true;
		} 
		else if (item.getItemId() == R.id.chooseStation) 
		{
			FlurryAgent.onEvent("radio reddit - Menu Button - Choose Station");
			ChooseStation();
			return true;
		} 
		else if (item.getItemId() == R.id.viewEpisodeInfo) 
		{
			FlurryAgent.onEvent("radio reddit - Menu Button - View Episode Info");
			ViewEpisodeInfo();
			return true;
		} 
		else if (item.getItemId() == R.id.settings) 
		{
			FlurryAgent.onEvent("radio reddit - Menu Button - Settings");
			startActivity(new Intent(this, Settings.class));
			return true;
		} 
		else if (item.getItemId() == R.id.email_feedback) {
			FlurryAgent.onEvent("radio reddit - Menu Button - Email Feedback");
			SendEmail();
			return true;
		} 
		else if (item.getItemId() == R.id.exit) 
		{
			FlurryAgent.onEvent("radio reddit - Menu Button - Exit App");
			ExitApp();
			return true;
		}
		else if (item.getItemId() == R.id.charts) 
		{
			FlurryAgent.onEvent("radio reddit - Menu Button - Charts");
			if(RadioRedditApplication.isProVersion(this))
			{
				startActivityForResult(new Intent(this, TopCharts.class), 2);
			}
			else
			{
				showGoProDialog();
			}
			
			return true;
		}
		else if (item.getItemId() == R.id.recentlyplayed) 
		{
			FlurryAgent.onEvent("radio reddit - Menu Button - Recently Played");
			if(RadioRedditApplication.isProVersion(this))
			{
				startActivityForResult(new Intent(this, RecentlyPlayed.class), 2);
			}
			else
			{
				showGoProDialog();
			}
			
			return true;
		}
		return false;
	}
	
	private void showGoProDialog()
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(RadioReddit.this);
	    builder.setMessage(getString(R.string.gopro_body))
	    	.setTitle(getString(R.string.gopro_title))
	    	.setIcon(android.R.drawable.ic_dialog_alert)
	    	.setCancelable(true)
	        .setPositiveButton(getString(R.string.gopro_yes), new DialogInterface.OnClickListener()
			{
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					FlurryAgent.onEvent("radio reddit - Go Pro");
					// launch intent to google play store for pro version
					boolean usePlayStoreLink = true;
					String url = "";
					if(usePlayStoreLink)
						url = "https://play.google.com/store/apps/details?id=net.mandaria.radioredditpro";
					else
					// For Amazon Appstore
						url ="http://www.amazon.com/gp/mas/dl/android?p=net.mandaria.radioredditpro";
					
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				}
			})
	        .setNegativeButton(getString(R.string.gopro_no), null);
	    
	    final AlertDialog alert = builder.create();
	    alert.show();
	}
	
	private void Logout()
	{		
		final AlertDialog.Builder builder = new AlertDialog.Builder(RadioReddit.this);
	    builder.setMessage(getString(R.string.logout_body))
	    	.setTitle(getString(R.string.logout))
	    	.setIcon(android.R.drawable.ic_dialog_alert)
	    	.setCancelable(true)
	        .setPositiveButton(getString(R.string.logout), new DialogInterface.OnClickListener()
			{
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					FlurryAgent.onEvent("radio reddit - Logout - Yes");
					
					RedditAccount emptyAccount = new RedditAccount();
					emptyAccount.Username = emptyAccount.Cookie = emptyAccount.Modhash = "";
					
					Settings.setRedditAccount(RadioReddit.this, emptyAccount);
					
					Toast.makeText(RadioReddit.this,  getString(R.string.youHaveBeenLoggedOut), Toast.LENGTH_LONG).show();
				}
			})
	        .setNegativeButton(getString(R.string.no), null);
	    
	    final AlertDialog alert = builder.create();
	    alert.show();
	}

	private void ViewEpisodeInfo()
	{
		Intent i = new Intent(RadioReddit.this, ViewEpisodeInformation.class);
		startActivity(i);
	}

	private void ChooseStation()
	{
		RadioRedditApplication application = (RadioRedditApplication) getApplication();

//		if(player != null && !player.isPreparing()) // check if a current listen is in progress to prevent state exception
//		{
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
//		}
//		else
//		{
//			Toast.makeText(RadioReddit.this, getString(R.string.pleaseWaitToChangeStation), Toast.LENGTH_LONG).show();
//		}
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
		
		ActivityUtil.SetKeepScreenOn(this);

		if(player == null)
		{
			attachToPlaybackService();
		}

		startUpdateTimer();
		displayAd();

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

			if(player != null && (player.isPlaying() || player.isPreparing()))
			{
				showStopButton();
				
				RadioRedditApplication application = (RadioRedditApplication) getApplication();
				
				if(application.CurrentSong == null && application.CurrentEpisode == null)
				{
					hideSongInformation();
	
					// show progress bar while waiting to load song information
					progress_LoadingSong.setVisibility(View.VISIBLE);
				}
			}
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
				FlurryAgent.onEvent("radio reddit - Is Buffering");
				hideSongInformation();
				lbl_Buffering.setVisibility(View.VISIBLE);
				progress_LoadingSong.setVisibility(View.VISIBLE);

			}
			else
			{
				FlurryAgent.onEvent("radio reddit - Is Not Buffering");
				lbl_Buffering.setVisibility(View.GONE);
				if(application.CurrentSong != null || application.CurrentEpisode != null)
				{
					progress_LoadingSong.setVisibility(View.GONE);
				}
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
				Toast.makeText(this, getString(R.string.error_OnListen) + ": " + ex.toString(), Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.w(LOG_TAG, "Request Code: " + requestCode);
		if(resultCode == Activity.RESULT_OK)
		{
			if(requestCode == 1) // stream
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
			else if(requestCode == 2) // individual song/episode
			{
				String song_url = data.getStringExtra("song_url");
				Log.w(LOG_TAG, "Song URL: " + song_url);
				
				playStream(song_url);
			}
		}
	}

	private void playStream()
	{
		playStream("");
	}
	
	private void playStream(String url)
	{
		RadioRedditApplication application = (RadioRedditApplication) getApplication();

		if(application.CurrentStream != null || !url.equals(""))
		{
			if(url.equals(""))
			{
				application.playBackType = "stream";
				
				Map params = new HashMap();
				params.put("station", application.CurrentStream.Name);
				FlurryAgent.onEvent("radio reddit - Play Stream", params);
				listen(application.CurrentStream.Relay);
			}
			else
			{
				//application.playBackType = "song";
				RadioSong song = application.CurrentSong;
				RadioEpisode episode = application.CurrentEpisode;
				
				// stop / cleanup
				if(player != null)
				{
					player.stop();
				}
				
				application.CurrentSong = song; // cleanup will remove current song from application, must re-add it or lose it
				application.CurrentEpisode = episode;
				
				Map params = new HashMap();
				params.put("song", url);
				FlurryAgent.onEvent("radio reddit - Play Song", params);
				listen(url);
			}
			
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
			
			// Attempt to load songs from cache
			if(application.RadioStreams == null && isStreamCacheLoaded == false)
			{
				DatabaseService service = new DatabaseService();
				application.RadioStreams = service.GetCachedStreams(RadioReddit.this);
				
				if(application.CurrentStream == null && application.RadioStreams != null && application.RadioStreams.size() > 0)
				{
					application.CurrentStream = RadioStreams.getMainStream(application.RadioStreams);
				}	
				
				isStreamCacheLoaded = true;
			}

			// Update stream information every 30 seconds
			if((SystemClock.elapsedRealtime() - mLastStreamsInformationUpdateMillis) > 30000)
			{
				new GetRadioStreamsTask(application, RadioReddit.this, Locale.getDefault()).execute();
				mLastStreamsInformationUpdateMillis = SystemClock.elapsedRealtime();
			}

			// Connecting to radio reddit
			if(application.RadioStreams == null || application.RadioStreams.size() == 0)
			{		
				progress_LoadingSong.setVisibility(View.VISIBLE);
				lbl_Connecting.setVisibility(View.VISIBLE);
				btn_play.setVisibility(View.GONE);
				btn_upvote.setVisibility(View.GONE);
				btn_downvote.setVisibility(View.GONE);
				img_Logo.setImageResource(R.drawable.logo_darkeyes);
			}
			else if(application.isRadioRedditDown == true)
			{
				img_Logo.setImageResource(R.drawable.logo_darkeyes);
				lbl_SongTitle.setVisibility(View.VISIBLE);
				lbl_SongTitle.setText(getString(R.string.error_RadioRedditIsDown) + "\n\n" + application.radioRedditIsDownErrorMessage);
			}
			else
			{
				if(player == null || (player != null && !player.isPlaying() && !player.isPreparing()))//if(player != null && !player.isPlaying() && !player.isPreparing())
				{
					progress_LoadingSong.setVisibility(View.GONE);
				}
				lbl_Connecting.setVisibility(View.GONE);
				btn_play.setVisibility(View.VISIBLE);
				btn_upvote.setVisibility(View.VISIBLE);
				btn_downvote.setVisibility(View.VISIBLE);
				img_Logo.setImageResource(R.drawable.logo);
			}

			invalidateOptionsMenu(); // force update of menu to enable "Choose Station" when connected

			if(player != null && player.isPlaying())
			{
				// Update current song information
				if(!player.isBuffering())
				{
					if(application.CurrentSong != null)
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
				if(application.isRadioRedditDown == false)
					hideSongInformation();
				if(player == null) // if(player != null && !player.isPreparing())
				{
					showPlayButton();
				}
				else if(player != null && player.isPreparing() && !player.isAborting())
					progress_LoadingSong.setVisibility(View.VISIBLE); // show please wait spinner when "re-connecting" to stream from network change
			}
			

			// Refresh the ads
			if((SystemClock.elapsedRealtime() - mLastAdServedMillis) > Settings.getAdRefreshRate(RadioReddit.this))
			{
				displayAd();
				mLastAdServedMillis = SystemClock.elapsedRealtime();
			}

			mHandler.postDelayed(this, 1000); // update every 1 second
		}

	};
	
	// when getting vote info, sets if user previously voted
	private void setUpOrDownVote(String vote)
	{
		if(vote != null && !vote.equals("null"))
		{
			if(vote.equals("true"))
			{
				lbl_SongVote.setTextColor(getResources().getColor(R.color.UpVote));
				btn_upvote.setBackgroundResource(R.drawable.didupvote_button);
				btn_downvote.setBackgroundResource(R.drawable.willdownvote_button);
			}
			else
			{
				lbl_SongVote.setTextColor(getResources().getColor(R.color.DownVote));
				btn_upvote.setBackgroundResource(R.drawable.willupvote_button);
				btn_downvote.setBackgroundResource(R.drawable.diddownvote_button);
			}
		}
		else
		{
			lbl_SongVote.setTextColor(getResources().getColor(R.color.NoVote));
			btn_upvote.setBackgroundResource(R.drawable.willupvote_button);
			btn_downvote.setBackgroundResource(R.drawable.willdownvote_button);
		}
	}

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
		
		setUpOrDownVote(application.CurrentEpisode.Likes);
		
		btn_upvote.setEnabled(true);
		btn_downvote.setEnabled(true);
		
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
		
		String score = application.CurrentSong.Score;
		if(application.CurrentSong.CumulativeScore != null)
			score += " (" + application.CurrentSong.CumulativeScore + ")";
		
		lbl_SongVote.setText(score);
		lbl_SongTitle.setText(application.CurrentSong.Title);
		lbl_SongArtist.setText(application.CurrentSong.Artist + " (" + application.CurrentSong.Redditor + ")");
		lbl_SongPlaylist.setText(getString(R.string.playlist) + ": " + application.CurrentSong.Playlist);
		btn_SongInfo.setVisibility(View.GONE);
		
		setUpOrDownVote(application.CurrentSong.Likes);
		
		btn_upvote.setEnabled(true);
		btn_downvote.setEnabled(true);
		
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
		btn_upvote.setBackgroundResource(R.drawable.willupvote_button);
		btn_downvote.setBackgroundResource(R.drawable.willdownvote_button);
		btn_upvote.setEnabled(false);
		btn_downvote.setEnabled(false);
	}
	
	private void UpdateAdValues() 
	{
		GetAdRefreshRateTask task_adrefreshrate = (GetAdRefreshRateTask) new GetAdRefreshRateTask(getApplication(), this, Locale.getDefault()).execute();
	
		GetInHouseAdsPercentageTask task_inhouseadspercentage = (GetInHouseAdsPercentageTask) new GetInHouseAdsPercentageTask(getApplication(), this, Locale.getDefault()).execute();
	}
	
	// Used to show ads or a banner message (e.g. radioreddit.com is down)
	private void displayAd()
	{
		Random r = new Random();
		int randomInt = r.nextInt(100);
		
		// Display in house ad
		if(randomInt < Settings.getInHouseAdsPercentage(this))
		{
			div_ads.setVisibility(View.VISIBLE);
			
			// Setup handler for 404's
			adView.setWebViewClient(new WebViewClient() 
			{
			    @Override
			    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) 
			    {
			    	//displayOfflineAd(); 
			    	// For now, just don't display any ads
			    	div_ads.setVisibility(View.GONE);
					
			        super.onReceivedError(view, errorCode, description, failingUrl);
			    }
			    
			    @Override
			    public boolean shouldOverrideUrlLoading(WebView view, String url) 
			    {
			    	try
			    	{ 
				    	Intent i = new Intent(Intent.ACTION_VIEW);
				    	i.setData(Uri.parse(url));
				    	startActivity(i);
			    	}
			    	catch(Exception ex)
			    	{
			    		// TODO: this should be made into it's own string
			    		Toast.makeText(RadioReddit.this, "Web Browser Error: Application is not installed on your phone", Toast.LENGTH_LONG).show();
			    	}
			    	return true;
			    	
			    }

			 });

			int applicationID = 2;
			if(RadioRedditApplication.isProVersion(RadioReddit.this))
				applicationID = 3;
			
			// Request an ad
			adView.loadUrl("http://www.bryandenny.com/software/android/default.aspx?id=" + applicationID);
			//Toast.makeText(getApplicationContext(), "Show inhouse ad " + randomInt, Toast.LENGTH_LONG).show();
		}
		else // display no ads
		{
			div_ads.setVisibility(View.GONE);
		}
	}

}