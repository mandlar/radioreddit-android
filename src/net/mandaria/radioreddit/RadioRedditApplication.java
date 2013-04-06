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

package net.mandaria.radioreddit;

import java.util.ArrayList;

import net.mandaria.radioreddit.errors.CustomExceptionHandler;
import net.mandaria.radioreddit.objects.RadioEpisode;
import net.mandaria.radioreddit.objects.RadioSong;
import net.mandaria.radioreddit.objects.RadioStream;
import android.app.Application;
import android.content.Context;

public class RadioRedditApplication extends Application
{
	public RadioStream CurrentStream;
	public RadioSong CurrentSong;
	public RadioEpisode CurrentEpisode;
	public ArrayList<RadioStream> RadioStreams;
	public boolean isRadioRedditDown = false;
	public String radioRedditIsDownErrorMessage = "";
	public String playBackType = "";
	
	// Use this variable to control if purchase links use Google PlayStore or Amazon Appstore 
	public final static boolean usePlayStoreLink = true;

	@Override
	public void onCreate()
	{
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(getApplicationContext()));

		super.onCreate();
	}
	
	public static boolean isProVersion(Context context)
	{
		return context.getString(R.string.app_name).contains("pro");
	}
	
	public static String getPaidVersionLink()
	{
		String url = "";
		if(usePlayStoreLink)
			url = "https://play.google.com/store/apps/details?id=net.mandaria.radioredditpro";
		else
		// For Amazon Appstore
			url = "http://www.amazon.com/gp/mas/dl/android?p=net.mandaria.radioredditpro";
		return url;
	}
	
	public static int getApplicationID(Context context)
	{
		int applicationID = 2; // free - play store
		if(RadioRedditApplication.getPaidVersionLink().contains("amazon"))
			applicationID = 4; // free - amazon
		if(RadioRedditApplication.isProVersion(context))
			applicationID = 3; // pro - either store
		
		return applicationID;
	}
}