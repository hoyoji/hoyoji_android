package com.hoyoji.hoyoji;

import java.util.Locale;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.activity.HyjUserActivity;
import com.hoyoji.android.hyjframework.fragment.HyjUserFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;
import com.hoyoji.hoyoji.home.HomeListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;

public class MainActivity extends HyjUserActivity {
    private String[] mDrawerListerTitles = null;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;  
    
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	@Override
	protected Integer getContentView() {
		// TODO Auto-generated method stub
		return R.layout.activity_main;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        
        // set a custom shadow that overlays the main content when the drawer opens  
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);  
        
        // Set the adapter for the list view
        mDrawerListerTitles = getResources().getStringArray(R.array.mainActivity_drawer_list_titles);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.main_drawer_list_item, mDrawerListerTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.mainActivity_drawer_open, R.string.mainActivity_drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setCurrentItem(1, false);

	}
	
	/* Called whenever we call invalidateOptionsMenu() */
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        // If the nav drawer is open, hide action items related to the content view
//        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//      //  menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
//        return super.onPrepareOptionsMenu(menu);
//    }
    
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView parent, View view, int position, long id) {
	        selectItem(position);
	    }
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
	    // Highlight the selected item, update the title, and close the drawer
	    mDrawerList.setItemChecked(position, true);
	    switch(position){
	    	case 6 :
	    		HyjApplication.getInstance().switchUser();
	    	break;
	    }
	    
	    mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			Fragment fragment = null;
			switch(position){
			case 0:
				fragment = new ProjectListFragment();
				break;
			case 1:
				fragment = new HomeListFragment();
				break;
			case 2:
				fragment = new FriendListFragment();
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.mainActivity_section_title_project).toUpperCase(l);
			case 1:
				return getString(R.string.mainActivity_section_title_home).toUpperCase(l);
			case 2:
				return getString(R.string.mainActivity_section_title_friend).toUpperCase(l);
			}
			return null;
		}
	}
}
