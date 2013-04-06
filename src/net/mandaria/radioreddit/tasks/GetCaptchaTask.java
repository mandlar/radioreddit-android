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

package net.mandaria.radioreddit.tasks;

import java.io.InputStream;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.objects.RadioEpisode;
import net.mandaria.radioreddit.objects.RadioSong;
import net.mandaria.radioreddit.utils.HTTPUtil;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class GetCaptchaTask extends AsyncTask<Void, BitmapDrawable, BitmapDrawable>
{
	private static String TAG = "RadioReddit";
	private Context _context;
	private RadioRedditApplication _application;
	private Exception ex;
	private String _captcha;
	private boolean _liked;
	private String _type;
	private RadioSong _song;
	private RadioEpisode _episode;

	public GetCaptchaTask(RadioRedditApplication application, Context context, String type, RadioSong song, RadioEpisode episode, String captcha, boolean liked)
	{
		_context = context;
		_application = application;
		_captcha = captcha;
		_liked = liked;
		_type = type;
		_song = song;
		_episode = episode;
	}

	@Override
	protected BitmapDrawable doInBackground(Void... unused)
	{
		BitmapDrawable bmd = null;
		try
		{
			InputStream in = HTTPUtil.getInputStream(_context, _context.getString(R.string.reddit_captcha) + _captcha);
			
			//get image as bitmap
			Bitmap captchaOrg  = BitmapFactory.decodeStream(in);

			// create matrix for the manipulation
			Matrix matrix = new Matrix();
			// resize the bit map
			matrix.postScale(5f, 5f);

			// recreate the new Bitmap
			Bitmap resizedBitmap = Bitmap.createScaledBitmap (captchaOrg,
					captchaOrg.getWidth() * 5, captchaOrg.getHeight() * 5, true);

			bmd = new BitmapDrawable(resizedBitmap);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			ex = e;
			Log.e(TAG, "FAIL: getting captcha: " + e);
		}

		return bmd;
	}

	@Override
	protected void onProgressUpdate(BitmapDrawable... item)
	{

	}

	@Override
	protected void onPostExecute(BitmapDrawable result)
	{
		if(result != null)
		{
			LayoutInflater inflater = (LayoutInflater)_context.getSystemService(_context.LAYOUT_INFLATER_SERVICE);
		    final View layout = inflater.inflate(R.layout.captcha, null);
			
		    ImageView img_Captcha = (ImageView)layout.findViewById(R.id.img_Captcha);
		    img_Captcha.setBackgroundDrawable(result);
		    
		    
			final AlertDialog.Builder builder = new AlertDialog.Builder(_context);
		    builder.setView(layout)
		    	.setTitle(_context.getString(R.string.please_enter_the_CAPTCHA))
		    	.setIcon(android.R.drawable.ic_dialog_alert)
		    	.setCancelable(true)
		        .setPositiveButton(_context.getString(R.string.im_human), new OnClickListener()
				{
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						EditText txt_Captcha = (EditText)layout.findViewById(R.id.txt_Captcha);
						
						// attempt to resubmit
						if(_type.equals("song"))
						{
							new VoteOnSongTask(_application, _context, _song, _liked, _captcha, txt_Captcha.getText().toString()).execute();
						}
						else if(_type.equals("episode"))
						{
							new VoteOnEpisodeTask(_application, _context, _episode, _liked, _captcha, txt_Captcha.getText().toString()).execute();
						}
						else
						{
							new VoteRedditTask(_application, _context, _liked, _captcha, txt_Captcha.getText().toString()).execute();
						}
						
					}
				})
		    	.setNegativeButton(_context.getString(R.string.cancel), null);
		    
		    final AlertDialog alert = builder.create();
		    alert.show();

			//Toast.makeText(_context, "Retrieved Captcha!", Toast.LENGTH_LONG).show();
		}
		else
		{
			final AlertDialog.Builder builder = new AlertDialog.Builder(_context);
		    builder.setMessage(_context.getString(R.string.error) + ": " + _context.getString(R.string.error_FailedToRetrieveCAPTCHA))
		    	.setTitle(_context.getString(R.string.CAPTCHA_error))
		    	.setIcon(android.R.drawable.ic_dialog_alert)
		    	.setCancelable(true)
		        .setPositiveButton(_context.getString(R.string.ok), null);
		    
		    final AlertDialog alert = builder.create();
		    alert.show();
		    
			Log.e(TAG, "FAIL: Post execute: " + result);

		}

		if(ex != null)
			Log.e(TAG, "FAIL: EXCEPTION: Post execute: " + ex);

	}
}

