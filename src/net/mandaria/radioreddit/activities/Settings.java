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

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.flurry.android.FlurryAgent;
import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.objects.RedditAccount;
import net.mandaria.radioreddit.utils.ActivityUtil;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.view.View;
import android.content.Context;

public class Settings extends SherlockPreferenceActivity
{

	private int sdkVersion = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		ActivityUtil.SetKeepScreenOn(this);
		
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
		
		CheckBoxPreference keep_screen_on = (CheckBoxPreference)findPreference("keep_screen_on");
		keep_screen_on.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				Log.e("RadioReddit", "Toggle screen");
				ActivityUtil.SetKeepScreenOn(Settings.this, (Boolean) newValue);
				return true;
			}
		});
		
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
	
	public static boolean getKeepScreenOn(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("keep_screen_on", false);
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
	
	// Ad Refresh Rate, in miliseconds
	
	public static int getAdRefreshRate(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context).getInt("ad_refresh_rate", 60000);
	}
	  
	public static void setAdRefreshRate(Context context, int adRefreshRate)
	{
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("ad_refresh_rate", adRefreshRate).commit();
	}
	
	// In House Ads Percentage
	
	public static int getInHouseAdsPercentage(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context).getInt("in_house_ads_percentage", 0);
	}
	
	public static void setInHouseAdsPercentage(Context context, int inHouseAdsPercentage)
	{
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("in_house_ads_percentage", inHouseAdsPercentage).commit();
	}
	
}
