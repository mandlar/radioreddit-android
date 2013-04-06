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
import net.mandaria.radioreddit.utils.ActivityUtil;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.flurry.android.FlurryAgent;

public class About extends SherlockActivity
{
	Button btn_login;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{	
		super.onCreate(savedInstanceState);
		
		ActivityUtil.SetKeepScreenOn(this);
		
		setContentView(R.layout.about);
		getSupportActionBar().setTitle(getString(R.string.about));
		
		TextView lbl_AcknowledgementsList = (TextView)findViewById(R.id.lbl_AcknowledgementsList);
		lbl_AcknowledgementsList.setMovementMethod(LinkMovementMethod.getInstance()); // makes links clickable
		lbl_AcknowledgementsList.setText(Html.fromHtml(getString(R.string.acknowledgements_list)));
		
		String version = "1.0";
		try
		{
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		}
		catch(NameNotFoundException ex)
		{

		}
		
		TextView lbl_VersionNumber = (TextView)findViewById(R.id.lbl_VersionNumber);
		lbl_VersionNumber.setText(" v." + version);
		
		Button btn_GoPro = (Button)findViewById(R.id.btn_GoPro);
		btn_GoPro.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				FlurryAgent.onEvent("radio reddit - About - Go Pro");
				
				String url = RadioRedditApplication.getPaidVersionLink();
				
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			}
		});
		
		if(RadioRedditApplication.isProVersion(this))
		{
			btn_GoPro.setEnabled(false);
			btn_GoPro.setText(getString(R.string.thanks_for_purchasing));
		}
		
		Button btn_VisitRadioRedditCom = (Button)findViewById(R.id.btn_VisitRadioRedditCom);
		btn_VisitRadioRedditCom.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				FlurryAgent.onEvent("radio reddit - About - radioreddit.com");
				
				String url = "http://www.radioreddit.com";
				
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				
			}
		});
		
		Button btn_HelpTranslate = (Button)findViewById(R.id.btn_HelpTranslate);
		btn_HelpTranslate.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				FlurryAgent.onEvent("radio reddit - About - Help translate");
				
				String url = "http://crowdin.net/project/radioreddit";
				
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				
			}
		});
		
		Button btn_SourceCodeProject = (Button)findViewById(R.id.btn_SourceCodeProject);
		btn_SourceCodeProject.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				FlurryAgent.onEvent("radio reddit - About - Source code project");
				
				String url = "https://code.google.com/p/radioreddit-android/";
				
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				
			}
		});
		
		Button btn_VisitRRadioReddit = (Button)findViewById(R.id.btn_VisitRRadioReddit);
		btn_VisitRRadioReddit.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				FlurryAgent.onEvent("radio reddit - About - r/radioreddit");
				
				String url = "http://www.reddit.com/r/radioreddit";
				
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				
			}
		});
		
		Button btn_VisitRTalkRadioReddit = (Button)findViewById(R.id.btn_VisitRTalkRadioReddit);
		btn_VisitRTalkRadioReddit.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				FlurryAgent.onEvent("radio reddit - About - r/talkradioreddit");
				
				String url = "http://www.reddit.com/r/talkradioreddit";
				
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));				
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
