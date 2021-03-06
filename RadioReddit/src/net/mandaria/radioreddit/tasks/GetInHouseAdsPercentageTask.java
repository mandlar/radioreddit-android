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

import java.util.Locale;

import net.mandaria.radioreddit.RadioRedditApplication;
import net.mandaria.radioreddit.activities.Settings;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;


public class GetInHouseAdsPercentageTask extends AsyncTask<Void, Integer, Integer> 
{
	private static String TAG = "RadioReddit";
	private Context _context;
	private Locale _locale;
	private Application _application;
	private Exception ex;
	
	

    public GetInHouseAdsPercentageTask(Application application, Context context, Locale locale) 
    {
    	_context = context;
    	_locale = locale;
    	_application = application;
    }

	@Override
	protected Integer doInBackground(Void... unused) 
	{
		int inHouseAdsPercentage = -1;
		try 
		{
			String SOAP_ACTION = "http://www.bryandenny.com/software/android/GetInHouseAdsPercentageByApplicationID";
		    String METHOD_NAME = "GetInHouseAdsPercentageByApplicationID";
		    String NAMESPACE = "http://www.bryandenny.com/software/android/";
		    String URL = "http://www.bryandenny.com/software/android/service.asmx";
		    
		    int applicationID = RadioRedditApplication.getApplicationID(_context);

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			request.addProperty("applicationID", applicationID);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet=true;
            envelope.setOutputSoapObject(request);

            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

            androidHttpTransport.call(SOAP_ACTION, envelope);

            //SoapObject result = (SoapObject)envelope.getResponse();
            SoapPrimitive result = (SoapPrimitive)envelope.getResponse();

            //to get the data
            inHouseAdsPercentage = Integer.parseInt(result.toString());
            // 0 is the first object of data 
            
            //Log.e(TAG, "New in house ads percentage: " + inHouseAdsPercentage);
		}
		catch(Exception e)
		{
			ex = e;
			//Log.e(TAG, "FAIL: in house ads percentage: " + ex);
		}
		
		return inHouseAdsPercentage;
	}

	@Override
	protected void onProgressUpdate(Integer... item) 
	{

	}

	@Override
	protected void onPostExecute(Integer result) 
	{
		RadioRedditApplication appState = ((RadioRedditApplication)_application);
		if(result != -1)
		{
			//Log.e(TAG, "Post execute: " + result);
			Settings.setInHouseAdsPercentage(_context, result);
		}
		else
		{
			//Log.e(TAG, "FAIL: Post execute: " + result);
		}
		
		//if(ex != null)
			//Log.e(TAG, "FAIL: EXCEPTION: Post execute: " + ex);
		
	}
}

