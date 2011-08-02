package net.mandaria.radioreddit.activities;

import net.mandaria.radioreddit.R;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import com.flurry.android.FlurryAgent;

public class Login extends Activity
{
	
	private int sdkVersion = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
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
		setContentView(R.layout.login);
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
    
}
