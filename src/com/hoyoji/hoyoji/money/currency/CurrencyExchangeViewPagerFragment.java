package com.hoyoji.hoyoji.money.currency;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.ViewPager.PageTransformer;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.hoyoji.android.hyjframework.fragment.HyjFragment;
import com.hoyoji.android.hyjframework.fragment.HyjUserFragment;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.view.HyjTabStrip;
import com.hoyoji.android.hyjframework.view.HyjViewPager;
import com.hoyoji.android.hyjframework.view.HyjTabStrip.OnTabSelectedListener;
import com.hoyoji.android.hyjframework.view.HyjViewPager.OnOverScrollListener;
import com.hoyoji.aaevent_android.R;
import com.hoyoji.hoyoji.friend.FriendListFragment;
import com.hoyoji.hoyoji.project.ProjectMemberListFragment;
import com.hoyoji.hoyoji.project.SubProjectListFragment.OnSelectSubProjectsListener;

public class CurrencyExchangeViewPagerFragment extends HyjUserFragment {
	
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	public ViewPager mViewPager;
	protected boolean isClosingActivity = false;

	protected DisplayMetrics mDisplayMetrics;

	
	@Override
	public Integer useContentView() {
		return R.layout.viewpager_tabstrip;
	}
	
	@Override
	public void onInitViewData() {
		mDisplayMetrics = getResources().getDisplayMetrics();
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) getView().findViewById(R.id.viewpager);
		//.setBackgroundColor(Color.LTGRAY);
//		mViewPager.setPageTransformer(true, new DepthPageTransformer());
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);

		((HyjViewPager)mViewPager).setOnOverScrollListener(new OnOverScrollListener(){
			@Override
			public void onOverScroll(float mOverscroll) {
//				Log.i("mOverscroll", "" + mOverscroll);
				if(mOverscroll / mDisplayMetrics.density < -150){
					if(!isClosingActivity  ){
						isClosingActivity = true;
						((HyjViewPager)mViewPager).setStopBounceBack(true);
						getActivity().finish();
					}
				}
			}
		});
		
		final HyjTabStrip mTabStrip = (HyjTabStrip) getView().findViewById(R.id.tabstrip);
		mTabStrip.initTabLine(mSectionsPagerAdapter.getCount());
		for(int i = 0; i < mSectionsPagerAdapter.getCount(); i ++){
			CharSequence title = mSectionsPagerAdapter.getPageTitle(i);
			mTabStrip.addTab(title.toString());
		}
		
		mViewPager.setOnPageChangeListener(new OnPageChangeListener()
		{
			@Override
			public void onPageSelected(int position) {
				((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(mSectionsPagerAdapter.getPageTitle(position));
				mTabStrip.setTabSelected(position);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				mTabStrip.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		mTabStrip.setOnTabSelectedListener(new OnTabSelectedListener(){
			@Override
			public void onTabSelected(int tag) {
				mViewPager.setCurrentItem(tag);
			}
		});
	}
	
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public static class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch(position){
			case 0 :
				return new CurrencyListFragment();
			case 1:
				return new ExchangeListFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch(position){
			case 0 :
				return "币种";
			case 1:
				return "汇率";
			}
			return null;
		}
	}
}
