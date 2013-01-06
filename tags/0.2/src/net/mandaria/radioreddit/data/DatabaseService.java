package net.mandaria.radioreddit.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import net.mandaria.radioreddit.data.DatabaseHelper;
import net.mandaria.radioreddit.objects.RadioStream;

public class DatabaseService
{
	
	private static String TAG = "RadioReddit";

	public DatabaseService()
	{
		
	}
	
	// TODO:  This should probably be inside of the databasehelper as a factory?
	private DatabaseHelper GetDatabaseHelper(Context context)
	{
		DatabaseHelper myDbHelper = new DatabaseHelper(context);
		 
    	boolean databaseCreated = myDbHelper.createDataBase();
    	//if(databaseCreated == true)
    	//{
    		// if needed later, we can execute database code here on creation
    	//}

        try 
        {
        	myDbHelper.openDataBase();
        }
        catch(SQLException sqle)
        {
        	throw sqle;
        }
        
        return myDbHelper;
	}
	
	public void testDatabsase(Context context) throws Exception
	{
		DatabaseHelper helper = new DatabaseHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		if(db == null)
			throw new Exception("Null DB");
	}
	
	public ArrayList<RadioStream> GetCachedStreams(Context context)
	{
		DatabaseHelper myDbHelper = GetDatabaseHelper(context);
		
		SQLiteDatabase db = myDbHelper.myDataBase;
		Cursor cursor = db.rawQuery("SELECT * FROM StreamsCache ORDER BY Name ASC", null);
		
		ArrayList<RadioStream> cachedStreams = new ArrayList<RadioStream>();
		
		while(cursor.moveToNext())
		{
			String name = cursor.getString(cursor.getColumnIndex("Name"));
			String type = cursor.getString(cursor.getColumnIndex("Type"));
			String description = cursor.getString(cursor.getColumnIndex("Description"));
			String status = cursor.getString(cursor.getColumnIndex("Status"));
			String relay = cursor.getString(cursor.getColumnIndex("Relay"));
			boolean online = (cursor.getInt(cursor.getColumnIndex("Online")) == 1);
	    	
	    	RadioStream stream = new RadioStream();
	    	stream.Name = name;
	    	stream.Type = type;
	    	stream.Description = description;
	    	stream.Status = status;
	    	stream.Relay = relay;
	    	stream.Online = online;
			
	    	cachedStreams.add(stream);
		}
		
		cursor.close();
		db.close();
		
		return cachedStreams;
	}	
	
	public void UpdateCachedStreams(Context context, ArrayList<RadioStream>streams)
	{
		DatabaseHelper myDbHelper = GetDatabaseHelper(context);
		
		// Delete all cached streams		
		SQLiteDatabase db = myDbHelper.myDataBase;
		db.execSQL("DELETE FROM StreamsCache");
		
		// loop through and insert each stream 
		for(RadioStream stream : streams)
		{
			ContentValues values = new ContentValues();
			values.put("Name", stream.Name);
			values.put("Type", stream.Type);
			values.put("Description", stream.Description);
			values.put("Status", stream.Status);
			values.put("Relay", stream.Relay);
			values.put("Online", stream.Online);
			db.insert("StreamsCache", null, values);
		}
		
		db.close();
	}
}
