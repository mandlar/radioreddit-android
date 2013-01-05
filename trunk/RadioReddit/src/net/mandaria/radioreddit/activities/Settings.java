package net.mandaria.radioreddit.activities;

import com.flurry.android.FlurryAgent;
import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.objects.RedditAccount;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.content.Context;

public class Settings extends PreferenceActivity
{

	private int sdkVersion = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.settings);
		
		try
		{
			sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		}
		catch(NumberFormatException e)
		{

		}
		
		// Disable option for Android 2.1 and below
		if(sdkVersion < 8)
		{
			CheckBoxPreference enable_compatibility_mode = (CheckBoxPreference)findPreference("enable_compatibility_mode");
			enable_compatibility_mode.setChecked(true);
			enable_compatibility_mode.setEnabled(false);
			enable_compatibility_mode.setSummary(getString(R.string.your_device_must_run_in_compatibility_mode));
		}
		
	}
	
	@Override
	public void onStart()
    {
       super.onStart();
       FlurryAgent.onStartSession(this, getString(R.string.flurrykey));
    }
    
    @Override
	public void onStop()
    {
       super.onStop();
       FlurryAgent.onEndSession(this);
    }
	
	public static boolean getEnableCompatibilityMode(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enable_compatibility_mode", false);
	}
	
	public static RedditAccount getRedditAccount(Context context)
	{
		RedditAccount account = new RedditAccount();
		account.Username = PreferenceManager.getDefaultSharedPreferences(context).getString("reddit_username", "");
		account.Cookie = PreferenceManager.getDefaultSharedPreferences(context).getString("reddit_cookie", "");;
		account.Modhash = PreferenceManager.getDefaultSharedPreferences(context).getString("reddit_modhash", "");;
		
		if(account.Username.equals(""))
			return null;
		else
			return account;
	}
	
	public static void setRedditAccount(Context context, RedditAccount account)
	{
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("reddit_username", account.Username).commit();
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("reddit_cookie", account.Cookie).commit();
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("reddit_modhash", account.Modhash).commit();
	}
	
}
