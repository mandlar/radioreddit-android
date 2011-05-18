package net.mandaria.radioreddit;
import net.mandaria.radioreddit.errors.CustomExceptionHandler;
import android.app.Application;

public class RadioRedditApplication extends Application 
{
	public String current_station = "main stream";
	
	@Override
	public void onCreate() 
	{
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(getApplicationContext()));
		
		super.onCreate();	 
	}
}