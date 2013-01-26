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

public class RadioRedditApplication extends Application
{
	public RadioStream CurrentStream;
	public RadioSong CurrentSong;
	public RadioEpisode CurrentEpisode;
	public ArrayList<RadioStream> RadioStreams;
	public boolean isRadioRedditDown = false;
	public String radioRedditIsDownErrorMessage = "";
	public String playBackType = "";

	@Override
	public void onCreate()
	{
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(getApplicationContext()));

		super.onCreate();
	}
}