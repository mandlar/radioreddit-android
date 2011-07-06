package net.mandaria.radioreddit.activities;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

public class ViewEpisodeInformation extends Activity
{
	private int sdkVersion = 0;

	TextView lbl_ShowTitle;
	TextView lbl_EpisodeTitle;
	TextView lbl_ShowHosts;
	TextView lbl_ShowRedditors;
	TextView lbl_EpisodeDescription;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
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
		setContentView(R.layout.viewepisodeinformation);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		lbl_ShowTitle = (TextView) findViewById(R.id.lbl_ShowTitle);
		lbl_EpisodeTitle = (TextView) findViewById(R.id.lbl_EpisodeTitle);
		lbl_ShowHosts = (TextView) findViewById(R.id.lbl_ShowHosts);
		lbl_ShowRedditors = (TextView) findViewById(R.id.lbl_ShowRedditors);
		lbl_EpisodeDescription = (TextView) findViewById(R.id.lbl_EpisodeDescription);

		RadioRedditApplication application = (RadioRedditApplication) getApplication();

		if(sdkVersion >= 11)
		{
			if(application.CurrentStream != null)
				getActionBar().setTitle("Current Station: " + application.CurrentStream.Name);  // TODO: pull into strings.xml
		}

		if(application.CurrentEpisode != null)
		{
			lbl_ShowTitle.setText(application.CurrentEpisode.ShowTitle);
			lbl_EpisodeTitle.setText(application.CurrentEpisode.EpisodeTitle);
			lbl_ShowHosts.setText(application.CurrentEpisode.ShowHosts);
			lbl_ShowRedditors.setText(application.CurrentEpisode.ShowRedditors);
			lbl_EpisodeDescription.setText(Html.fromHtml(application.CurrentEpisode.EpisodeDescription));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
				// app icon in Action Bar (Android 3.0) clicked; go home
				Intent intent = new Intent(this, RadioReddit.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
