package net.mandaria.radioreddit.tasks;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.activities.Login;
import net.mandaria.radioreddit.activities.RadioReddit;
import net.mandaria.radioreddit.activities.Settings;
import net.mandaria.radioreddit.apis.RadioRedditAPI;
import net.mandaria.radioreddit.apis.RedditAPI;
import net.mandaria.radioreddit.objects.RadioEpisode;
import net.mandaria.radioreddit.objects.RedditAccount;
import net.mandaria.radioreddit.utils.HTTPUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class GetCaptchaTask extends AsyncTask<Void, BitmapDrawable, BitmapDrawable>
{
	private static String TAG = "RadioReddit";
	private Context _context;
	private RadioRedditApplication _application;
	private Exception ex;
	private String _captcha;
	private boolean _liked;

	public GetCaptchaTask(RadioRedditApplication application, Context context, String captcha, boolean liked)
	{
		_context = context;
		_application = application;
		_captcha = captcha;
		_liked = liked;
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
						new VoteRedditTask(_application, _context, _liked, _captcha, txt_Captcha.getText().toString()).execute();
						
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

