package net.mandaria.radioreddit;
import java.util.ArrayList;

import net.mandaria.radioreddit.errors.CustomExceptionHandler;
import net.mandaria.radioreddit.objects.RadioStream;
import android.app.Application;

public class RadioRedditApplication extends Application 
{
	public String current_station = "main"; // TODO: don't hard code "main" here, pull it programatically
	public ArrayList<RadioStream> RadioStreams;
	
	@Override
	public void onCreate() 
	{
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(getApplicationContext()));
		
		super.onCreate();	 
	}
}