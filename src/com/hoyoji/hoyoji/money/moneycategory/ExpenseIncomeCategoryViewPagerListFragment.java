package com.hoyoji.hoyoji.money.moneycategory;

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
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.friend.FriendListFragment;
import com.hoyoji.hoyoji.project.MemberListFragment;
import com.hoyoji.hoyoji.project.SubProjectListFragment.OnSelectSubProjectsListener;

public class ExpenseIncomeCategoryViewPagerListFragment extends HyjUserFragment implements OnPageChangeListener {
	
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	public ViewPager mViewPager;

	
	@Override
	public Integer useContentView() {
		return R.layout.expense_income_categroy_listfragment_viewpager;
	}
	
	@Override
	public void onInitViewData() {
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) getView().findViewById(R.id.ecpenseIncomeViewPagerListFragment_pager);
		//.setBackgroundColor(Color.LTGRAY);
//		mViewPager.setPageTransformer(true, new DepthPageTransformer());
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(1);
		mViewPager.setOnPageChangeListener(this);
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
				return new MoneyExpenseCategoryListFragment();
			case 1:
				return new MoneyIncomeCategoryListFragment();
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
				return "支出分类管理";
			case 1:
				return "收入分类管理";
			}
			return null;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
			((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(mSectionsPagerAdapter.getPageTitle(position));
	}
}
