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

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.apis.RedditAPI;
import net.mandaria.radioreddit.tasks.LoginRedditTask;
import net.mandaria.radioreddit.utils.ActivityUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.flurry.android.FlurryAgent;

public class Login extends SherlockActivity
{
	Button btn_login;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{	
		super.onCreate(savedInstanceState);
		
		ActivityUtil.SetKeepScreenOn(this);
		
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
				
				// check if username or password are empty or if user name has a space (not allowed on reddit)
				if(username.equals(""))
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
				    builder.setMessage(R.string.usernameIsRequired)
				    	.setTitle(R.string.loginError)
				    	.setIcon(android.R.drawable.ic_dialog_alert)
				    	.setCancelable(true)
				        .setPositiveButton(R.string.ok, null);
				    
				    final AlertDialog alert = builder.create();
				    alert.show();
				}
				else if (password.equals(""))
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
				    builder.setMessage(R.string.passwordIsRequired)
				    	.setTitle(R.string.loginError)
				    	.setIcon(android.R.drawable.ic_dialog_alert)
				    	.setCancelable(true)
				        .setPositiveButton(R.string.ok, null);
				    
				    final AlertDialog alert = builder.create();
				    alert.show();
				}
				else if (username.contains(" "))
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
				    builder.setMessage(R.string.usernameCannotHaveSpaces)
				    	.setTitle(R.string.loginError)
				    	.setIcon(android.R.drawable.ic_dialog_alert)
				    	.setCancelable(true)
				        .setPositiveButton(R.string.ok, null);
				    
				    final AlertDialog alert = builder.create();
				    alert.show();
				}
				else
				{
					new LoginRedditTask((RadioRedditApplication)Login.this.getApplication(), Login.this, username, password).execute();
				}
				
				
				
			}
		});
		
		TextView lbl_Register = (TextView)findViewById(R.id.lbl_Register);
		lbl_Register.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.reddit_register_url)));
				startActivity(myIntent);
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
