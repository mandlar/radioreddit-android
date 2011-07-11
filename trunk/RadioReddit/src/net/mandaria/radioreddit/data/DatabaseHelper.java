package net.mandaria.radioreddit.data;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.mandaria.radioreddit.R;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
	 
    //The Android's default system path of your application database.
	private final Context myContext;
    private static String DB_PATH = "";//"/data/data/net.mandaria.radioredditfree/databases/";
    private static String DB_NAME = "radioreddit.db";
    private static int DB_VERSION = 1;
    public SQLiteDatabase myDataBase; 
   
 
    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DatabaseHelper(Context context) 
    {
    	super(context, DB_NAME, null, DB_VERSION);
        this.myContext = context;
        this.DB_PATH = context.getString(R.string.database);
    }	
 
    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public boolean createDataBase()
    {
    	boolean dbExist = checkDataBase();
 
    	if(dbExist)
    	{
    		// Do nothing - database already exist
    		return false;
    	}
    	else
    	{
    		// By calling this method an empty database will be created into the default system path
            // of your application so we are going to be able to overwrite that database with our database.
        	this.getReadableDatabase();
        	return true;
    	}
    }
 
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase()
    {
    	SQLiteDatabase checkDB = null;
 
    	try
    	{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    	}
    	catch(SQLiteException e)
    	{
    		//database does't exist yet.
    	}
 
    	if(checkDB != null)
    	{
    		checkDB.close();
    	}
 
    	return checkDB != null ? true : false;
    }
 
    public void openDataBase() throws SQLException
    {
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }
 
    @Override
	public synchronized void close() 
    {
    	if(myDataBase != null)
    		myDataBase.close();
 
    	super.close();
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		String createStreamsCacheTable = 
			"CREATE TABLE [StreamsCache] (" +
			"[_id] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," +
			"[Name] TEXT  NOT NULL," +
			"[Type] TEXT  NOT NULL," +
			"[Description] TEXT  NOT NULL," +
			"[Status] TEXT  NOT NULL," +
			"[Relay] TEXT  NOT NULL," +
			"[Online] BOOLEAN  NOT NULL" +
			")";
		
		try
		{
			db.execSQL(createStreamsCacheTable);
		}
		catch(Exception ex)
		{
			throw new Error("Error creating database", ex);
		}
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		// TODO: upgrade DB stuff here...
	}
}
