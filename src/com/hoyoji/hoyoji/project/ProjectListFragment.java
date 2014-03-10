package com.hoyoji.hoyoji.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hoyoji.android.hyjframework.fragment.HyjUserFragment;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.project.SubProjectListFragment.OnSelectSubProjectsListener;

public class ProjectListFragment extends HyjUserFragment implements OnSelectSubProjectsListener{
	
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	public Integer useContentView() {
		return R.layout.project_listfragment_project;
	}
	
	@Override
	public void onInitViewData() {
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) getView().findViewById(R.id.projectListFragment_pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(100);
		SubProjectListFragment firstFragment = SubProjectListFragment.newInstance(null, null);
		firstFragment.setOnSelectSubProjectsListener(this);
		mSectionsPagerAdapter.addPage(firstFragment);
		mViewPager.setCurrentItem(0);
	}

	@Override
	public Integer useOptionsMenuView() {
		return R.menu.project_listfragment_project;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.projectListFragment_action_project_addnew){
			openActivityWithFragment(ProjectFormFragment.class, R.string.projectFormFragment_title_addnew, null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

	    private List<SubProjectListFragment> pages;
	    
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			pages = new ArrayList<SubProjectListFragment>();
		}

		public void addPage(SubProjectListFragment fragment){
			pages.add(fragment);
			this.notifyDataSetChanged();
		}

		public void removePageAt(int position){
			pages.remove(position);
		}
		
		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			return pages.get(position);
		}

		@Override
		public int getCount() {
			return pages.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return pages.get(position).getTitle();
		}
		
		//-----------------------------------------------------------------------------
		  // Used by ViewPager.  "Object" represents the page; tell the ViewPager where the
		  // page should be displayed, from left-to-right.  If the page no longer exists,
		  // return POSITION_NONE.
		  @Override
		  public int getItemPosition (Object object)
		  {
		    int index = pages.indexOf (object);
		    if (index == -1)
		      return POSITION_NONE;
		    else
		      return index;
		  }

//		  //-----------------------------------------------------------------------------
//		  // Used by ViewPager.  Called when ViewPager needs a page to display; it is our job
//		  // to add the page to the container, which is normally the ViewPager itself.  Since
//		  // all our pages are persistent, we simply retrieve it from our "views" ArrayList.
//		  @Override
//		  public Object instantiateItem (ViewGroup container, int position)
//		  {
//		    View v = pages.get(position).getView();
//		    container.addView (v);
//		    return v;
//		  }

		  //-----------------------------------------------------------------------------
		  // Used by ViewPager.  Called when ViewPager no longer needs a page to display; it
		  // is our job to remove the page from the container, which is normally the
		  // ViewPager itself.  Since all our pages are persistent, we do nothing to the
		  // contents of our "views" ArrayList.
//		  @Override
//		  public void destroyItem (ViewGroup container, int position, Object object)
//		  {
//			  super.destroyItem(container, position, object);
//			  pages.remove(position);
//			  this.notifyDataSetChanged();
//		  }


//		  //-----------------------------------------------------------------------------
//		  // Used by ViewPager.
//		  @Override
//		  public boolean isViewFromObject (View view, Object object)
//		  {
//		    return view == object;
//		  }

	}
	@Override
	public void onSelectSubProjectsListener(final String parentProjectId, final String title) {
		for(int i = mSectionsPagerAdapter.getCount()-1; i > mViewPager.getCurrentItem()+1; i--){
//			if(i == mViewPager.getCurrentItem() && 
//				((SubProjectListFragment)mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).getActivity() != null){
//				break;
//			}
			mSectionsPagerAdapter.removePageAt(i);
		}
		mSectionsPagerAdapter.notifyDataSetChanged();
		if(mSectionsPagerAdapter.getCount()-1 > mViewPager.getCurrentItem()){
			mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1, true);
			((SubProjectListFragment)mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem())).requery(parentProjectId, title);
		} else {
			SubProjectListFragment nextFragment = SubProjectListFragment.newInstance(parentProjectId, title);
			nextFragment.setOnSelectSubProjectsListener(this);
			mSectionsPagerAdapter.addPage(nextFragment);
			mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1, true);
//			nextFragment.requery(parentProjectId, title);
		}
	}

}
