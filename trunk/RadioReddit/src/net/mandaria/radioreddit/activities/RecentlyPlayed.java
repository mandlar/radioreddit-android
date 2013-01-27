package net.mandaria.radioreddit.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.internal.view.menu.MenuItemWrapper;
import com.actionbarsherlock.view.MenuInflater;
import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.R.layout;
import net.mandaria.radioreddit.fragments.TopChartFragment;

import com.viewpagerindicator.TabPageIndicator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class RecentlyPlayed extends SherlockFragmentActivity
{
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recentlyplayed);   
        
        getSupportActionBar().setTitle(getString(R.string.recentlyplayed));
        
        TopChartFragment recentlyPlayed = new TopChartFragment("recentlyplayed");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.div_RecentlyPlayed, recentlyPlayed);

        ft.commit();
        
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    }
    
    
    @Override
	public boolean onPrepareOptionsMenu(com.actionbarsherlock.view.Menu menu)
	{		
		return true;
	}
    
    @Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) 
    {	
    	super.onCreateOptionsMenu(menu);		
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) 
	{
		return false;
	}
 
}