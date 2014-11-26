package com.hoyoji.hoyoji.project;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.hoyoji.android.hyjframework.fragment.HyjUserFragment;
import com.hoyoji.android.hyjframework.view.HyjTabStrip;
import com.hoyoji.android.hyjframework.view.HyjViewPager;
import com.hoyoji.android.hyjframework.view.HyjTabStrip.OnTabSelectedListener;
import com.hoyoji.android.hyjframework.view.HyjViewPager.OnOverScrollListener;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.project.MemberListFragment;

public class ProjectViewPagerListFragment extends HyjUserFragment {
	
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	public ViewPager mViewPager;

	protected boolean isClosingActivity = false;

	private HyjTabStrip mTabStrip;

	
	@Override
	public Integer useContentView() {
		return R.layout.project_listfragment_viewpager;
	}
	
	@Override
	public void onInitViewData() {
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) getView().findViewById(R.id.projectViewPagerListFragment_pager);
		//.setBackgroundColor(Color.LTGRAY);
//		mViewPager.setPageTransformer(true, new DepthPageTransformer());
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);
		((HyjViewPager)mViewPager).setOnOverScrollListener(new OnOverScrollListener(){
			@Override
			public void onOverScroll(float mOverscroll) {
//				Log.i("mOverscroll", "" + mOverscroll);
				if(mOverscroll / getResources().getDisplayMetrics().density < -150){
					if(!isClosingActivity ){
						isClosingActivity = true;
						getActivity().finish();
					}
				}
			}
		});
		
		mTabStrip = (HyjTabStrip) getView().findViewById(R.id.projectViewPagerListFragment_tabstrip);
		mTabStrip.initTabLine(mSectionsPagerAdapter.getCount());
		for(int i = 0; i < mSectionsPagerAdapter.getCount(); i ++){
			CharSequence title = mSectionsPagerAdapter.getPageTitle(i);
			mTabStrip.addTab(title.toString());
		}
		
		mViewPager.setOnPageChangeListener(new OnPageChangeListener()
		{
			@Override
			public void onPageSelected(int position) {
				((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("账本"+mSectionsPagerAdapter.getPageTitle(position));
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
		mViewPager.setCurrentItem(1, false);
	}
	
	
//	@Override
//	public boolean handleBackPressed() {
//		boolean backPressedHandled = false; //super.handleBackPressed();
////		if(mViewPager.getCurrentItem() > 0){
////			mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
////			backPressedHandled = true;
////		}
//		return backPressedHandled;
//	}
	
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
				return new ProjectEventListFragment();
			case 1 :
				return new ProjectMoneySearchListFragment();
			case 2:
				return new MemberListFragment();
			case 3:
				return new ProjectFormFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch(position){
			case 0 :
				return "活动";
			case 1 :
				return "流水";
			case 2:
				return "成员";
			case 3:
				return "资料";
			}
			return null;
		}
	}

}
