package net.mandaria.radioreddit.activities;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.apis.RedditAPI;
import net.mandaria.radioreddit.tasks.LoginRedditTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.flurry.android.FlurryAgent;

public class Login extends Activity
{
	
	private int sdkVersion = 0;
	Button btn_login;
	
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
		
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_login.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				EditText txt_username = (EditText)findViewById(R.id.txt_username);
				EditText txt_password = (EditText)findViewById(R.id.txt_password);
				
				String username = txt_username.getText().toString();
				String password = txt_password.getText().toString();
				
				// check if username or password are empty
				if(username.equals(""))
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
				    builder.setMessage("Username is required")
				    	.setTitle("Login Error")
				    	.setIcon(android.R.drawable.ic_dialog_alert)
				    	.setCancelable(true)
				        .setPositiveButton("OK", null);
				    
				    final AlertDialog alert = builder.create();
				    alert.show();
				}
				else if (password.equals(""))
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
				    builder.setMessage("Password is required")
				    	.setTitle("Login Error")
				    	.setIcon(android.R.drawable.ic_dialog_alert)
				    	.setCancelable(true)
				        .setPositiveButton("OK", null);
				    
				    final AlertDialog alert = builder.create();
				    alert.show();
				}
				// TODO: check if username has space (not allowed)
				else
				{
					new LoginRedditTask((RadioRedditApplication)Login.this.getApplication(), Login.this, username, password).execute();
				}
				
				
				
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
    
}
