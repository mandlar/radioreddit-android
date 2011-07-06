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

	@Override
	public void onCreate()
	{
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(getApplicationContext()));

		super.onCreate();
	}
}