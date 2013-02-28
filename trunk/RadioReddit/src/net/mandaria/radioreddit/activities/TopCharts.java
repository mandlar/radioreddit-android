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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.internal.view.menu.MenuItemWrapper;
import com.actionbarsherlock.view.MenuInflater;
import net.mandaria.radioreddit.R;
import net.mandaria.radioreddit.R.layout;
import net.mandaria.radioreddit.fragments.SongListFragment;
import net.mandaria.radioreddit.utils.ActivityUtil;

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

public class TopCharts extends SherlockFragmentActivity
{
	
	int _currentFragmentPage = 0;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topcharts);   
        
        getSupportActionBar().setTitle(getString(R.string.charts));
        
        FragmentPagerAdapter adapter = new TabsAdapter(getSupportFragmentManager());

        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        pager.setOffscreenPageLimit(3);
        pager.setAdapter(adapter);

        TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        indicator.setOnPageChangeListener(new OnPageChangeListener() {
			
        	@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
        		_currentFragmentPage = position;
            	invalidateOptionsMenu();
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				// TODO Auto-generated method stub
				
			}
			

		});
        
        ActionBar bar=getSupportActionBar();
    }
    
    class TabsAdapter extends FragmentPagerAdapter {
        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }
        

        @Override
        public Fragment getItem(int position) {
        	Fragment fragment = new Fragment();
        	switch(position)
        	{
        		case 0:
        			fragment = new SongListFragment("all");
        			break;
        		case 1:
        			fragment = new SongListFragment("month");
        			break;
        		case 2:
        			fragment = new SongListFragment("week");
        			break;
        		case 3:
        			fragment = new SongListFragment("day");
        			break;
        		default:
        			fragment = new SongListFragment("all");
        			break;
        	}
        	
            return fragment;
        }

        public CharSequence getPageTitle(int position) {
        	
        	String pageTitle = "";
        	switch(position)
        	{
        		case 0:
        			pageTitle = getString(R.string.all_time);
        			break;
        		case 1:
        			pageTitle = getString(R.string.month);
        			break;
        		case 2:
        			pageTitle = getString(R.string.week);
        			break;
        		case 3:
        			pageTitle = getString(R.string.day);
        			break;
        		default:
        			pageTitle = getString(R.string.all_time);
        			break;
        	}
        	return pageTitle;
        }

        @Override
        public int getCount() {
          return 4;
        }
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	ActivityUtil.SetKeepScreenOn(this);
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