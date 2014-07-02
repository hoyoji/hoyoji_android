package com.hoyoji.hoyoji.money.report;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.fragment.HyjUserFragment;
import com.hoyoji.hoyoji_android.R;

public class MoneyReportFragment extends HyjUserFragment {
	
	private SectionsPagerAdapter mSectionsPagerAdapter;
	public ViewPager mViewPager;
	
	@Override
	public Integer useContentView() {
		return R.layout.money_fragment_report;
	}
	
	@Override
	public void onInitViewData() {
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) getView().findViewById(R.id.moneyReportFragment_pager);
		//.setBackgroundColor(Color.LTGRAY);
//		mViewPager.setPageTransformer(true, new DepthPageTransformer());
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(2);
	}
	
	
	@Override
	public boolean handleBackPressed() {
		boolean backPressedHandled = false; //super.handleBackPressed();
		if(mViewPager.getCurrentItem() > 0){
			mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
			backPressedHandled = true;
		}
		return backPressedHandled;
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
				return new MoneyTransactionPersonalSummaryFragment();
			case 1:
				return new MoneyTransactionProjectSummaryFragment();
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
				return HyjApplication.getInstance().getString(R.string.moneyTransactionSummaryFragment_title_personal);
			case 1:
				return HyjApplication.getInstance().getString(R.string.moneyTransactionSummaryFragment_title_project);
			}
			return null;
		}
	}
}
