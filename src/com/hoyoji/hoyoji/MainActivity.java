package com.hoyoji.hoyoji;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
import com.activeandroid.util.Log;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTask;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjUserActivity;
import com.hoyoji.android.hyjframework.server.HyjServer;
import com.hoyoji.hoyoji.friend.FriendListFragment;
import com.hoyoji.hoyoji.home.HomeListFragment;
import com.hoyoji.hoyoji.message.MessageDownloadService;
import com.hoyoji.hoyoji.message.MessageListFragment;
import com.hoyoji.hoyoji.models.ClientSyncRecord;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.MoneyApportion;
import com.hoyoji.hoyoji.models.MoneyBorrow;
import com.hoyoji.hoyoji.models.MoneyBorrowContainer;
import com.hoyoji.hoyoji.models.MoneyDepositIncomeApportion;
import com.hoyoji.hoyoji.models.MoneyDepositReturnApportion;
import com.hoyoji.hoyoji.models.MoneyExpense;
import com.hoyoji.hoyoji.models.MoneyExpenseApportion;
import com.hoyoji.hoyoji.models.MoneyExpenseContainer;
import com.hoyoji.hoyoji.models.MoneyIncome;
import com.hoyoji.hoyoji.models.MoneyIncomeApportion;
import com.hoyoji.hoyoji.models.MoneyIncomeContainer;
import com.hoyoji.hoyoji.models.MoneyLend;
import com.hoyoji.hoyoji.models.MoneyLendContainer;
import com.hoyoji.hoyoji.models.MoneyPayback;
import com.hoyoji.hoyoji.models.MoneyPaybackContainer;
import com.hoyoji.hoyoji.models.MoneyReturn;
import com.hoyoji.hoyoji.models.MoneyReturnContainer;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.money.MoneySearchListFragment;
import com.hoyoji.hoyoji.money.currency.CurrencyExchangeViewPagerListFragment;
import com.hoyoji.hoyoji.money.currency.CurrencyListFragment;
import com.hoyoji.hoyoji.money.currency.ExchangeListFragment;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.money.moneycategory.ExpenseIncomeCategoryViewPagerListFragment;
import com.hoyoji.hoyoji.money.moneycategory.MoneyExpenseCategoryListFragment;
import com.hoyoji.hoyoji.money.moneycategory.MoneyIncomeCategoryListFragment;
import com.hoyoji.hoyoji.money.report.MoneyReportFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.setting.SystemSettingFormFragment;
import com.hoyoji.hoyoji_android.R;
import com.jauker.widget.BadgeView;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;

public class MainActivity extends HyjUserActivity {
//	private String[] mDrawerListerTitles = null;
//	private DrawerLayout mDrawerLayout;
//	private ListView mDrawerList;
//	private ActionBarDrawerToggle mDrawerToggle;
	MenuItem mSyncButton = null;
	
	SectionsPagerAdapter mSectionsPagerAdapter;
	ChangeObserver mChangeObserver;
	MessageChangeObserver mMessageChangeObserver;
	ViewPager mViewPager;

	private LinearLayout mTabAccount;
	private LinearLayout mTabProject;
	private LinearLayout mTabHome;
	private LinearLayout mTabFriend;
	private LinearLayout mTabMessage;

	private TextView mAccount;
	private TextView mProject;
	private TextView mHome;
	private TextView mFriend;
	private TextView mMessage;

	private BadgeView mBadgeViewforAccount;
	private BadgeView mBadgeViewforProject;
	private BadgeView mBadgeViewforHome;
	private BadgeView mBadgeViewforFriend;
	private BadgeView mBadgeViewforMessage;

	private View mTabLine;

	private int currentIndex;
	private int screenWidth;
	private int intTabLineWidth;
	private double dblTabLineWidth;
	
	@Override
	protected Integer getContentView() {
		return R.layout.activity_main;
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		if (HyjApplication.getInstance().isLoggedIn()) {
	        
			Intent startPictureUploadService = new Intent(this, PictureUploadService.class);
			startPictureUploadService.putExtra("init", true);
			startService(startPictureUploadService);
			
			if (mChangeObserver == null) {
				mChangeObserver = new ChangeObserver();
				this.getContentResolver()
						.registerContentObserver(
								ContentProvider.createUri(
										ClientSyncRecord.class, null), true,
								mChangeObserver);
			}
			if (mMessageChangeObserver == null) {
				mMessageChangeObserver = new MessageChangeObserver();
				this.getContentResolver()
						.registerContentObserver(
								ContentProvider.createUri(
										Message.class, null), true,
								mMessageChangeObserver);
			}
			updateMessageCount();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        
//		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//		mDrawerList = (ListView) findViewById(R.id.left_drawer);
//
//		// set a custom shadow that overlays the main content when the drawer
//		// opens
//		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
//				GravityCompat.START);
//
//		// Set the adapter for the list view
//		mDrawerListerTitles = getResources().getStringArray(
//				R.array.mainActivity_drawer_list_titles);
//		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
//				R.layout.main_drawer_list_item, mDrawerListerTitles));
//		// Set the list's click listener
//		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
//
//		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
//				R.drawable.ic_drawer, R.string.mainActivity_drawer_open,
//				R.string.mainActivity_drawer_close) {
//
//			/** Called when a drawer has settled in a completely closed state. */
//			public void onDrawerClosed(View view) {
//				supportInvalidateOptionsMenu(); // creates call to
//												// onPrepareOptionsMenu()
//			}
//
//			/** Called when a drawer has settled in a completely open state. */
//			public void onDrawerOpened(View drawerView) {
//				supportInvalidateOptionsMenu(); // creates call to
//												// onPrepareOptionsMenu()
//			}
//		};
//
//		// Set the drawer toggle as the DrawerListener
//		mDrawerLayout.setDrawerListener(mDrawerToggle);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		initView();
		initTabLine();
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(5);

		mViewPager.setOnPageChangeListener(new OnPageChangeListener()
		{
			@Override
			public void onPageSelected(int position)
			{
				resetTextView();
				switch (position)
				{
					case 0:
						mAccount.setTextColor(getResources().getColor(R.color.hoyoji_red));
						
	//					mTabAccount.removeView(mBadgeViewforAccount);
	//					mBadgeViewforAccount.setBadgeCount(5);
	//					mTabAccount.addView(mBadgeViewforAccount);
						break;
					case 1:
						mProject.setTextColor(getResources().getColor(R.color.hoyoji_red));
						break;
					case 2:
						mHome.setTextColor(getResources().getColor(R.color.hoyoji_red));
						break;
					case 3:
						mFriend.setTextColor(getResources().getColor(R.color.hoyoji_red));
						break;
					case 4:
						mMessage.setTextColor(getResources().getColor(R.color.hoyoji_red));
						break;
				}

				currentIndex = position;
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
			{
				LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) mTabLine
						.getLayoutParams();
				if (currentIndex == 0 && position == 0)// 0->1
				{
					lp.leftMargin = (int) (positionOffset * dblTabLineWidth + currentIndex * intTabLineWidth);
					mTabLine.setLayoutParams(lp);
				} else if (currentIndex == 1 && position == 0) // 1->0
				{
					lp.leftMargin = (int) (-(1-positionOffset) * dblTabLineWidth + currentIndex * intTabLineWidth);
					mTabLine.setLayoutParams(lp);

				} 
				
				else if (currentIndex == 1 && position == 1) // 1->2
				{
					lp.leftMargin = (int) (positionOffset * dblTabLineWidth + currentIndex * intTabLineWidth);
					mTabLine.setLayoutParams(lp);
				} else if (currentIndex == 2 && position == 1) // 2->1
				{
					lp.leftMargin = (int) (-(1-positionOffset) * dblTabLineWidth + currentIndex * intTabLineWidth);
					mTabLine.setLayoutParams(lp);

				} 
				
				else if (currentIndex == 2 && position == 2) // 2->3
				{
					lp.leftMargin = (int) (positionOffset * dblTabLineWidth + currentIndex * intTabLineWidth);
					mTabLine.setLayoutParams(lp);
				} else if (currentIndex == 3 && position == 2) // 3->2
				{
					lp.leftMargin = (int) (-(1-positionOffset) * dblTabLineWidth + currentIndex * intTabLineWidth);
					mTabLine.setLayoutParams(lp);

				}
				
				else if (currentIndex == 3 && position == 3) // 3->4
				{
					lp.leftMargin = (int) (positionOffset * dblTabLineWidth + currentIndex * intTabLineWidth);
					mTabLine.setLayoutParams(lp);
				} else if (currentIndex == 4 && position == 3) // 4->3
				{
					lp.leftMargin = (int) (-(1-positionOffset) * dblTabLineWidth + currentIndex * intTabLineWidth);
					mTabLine.setLayoutParams(lp);

				}

			}

			@Override
			public void onPageScrollStateChanged(int state)
			{
			}
		});

		mViewPager.setCurrentItem(2, false);

	}

	private void initTabLine()
	{
		mTabLine = findViewById(R.id.id_tab_line);
		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindow().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		screenWidth = outMetrics.widthPixels;
		LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) mTabLine.getLayoutParams();
		lp.width = screenWidth / 5;
 		mTabLine.setLayoutParams(lp);
		intTabLineWidth = lp.width;
		dblTabLineWidth = screenWidth * 1.0 / 5;
	}

	protected void resetTextView()
	{
		mAccount.setTextColor(getResources().getColor(R.color.black));
		mProject.setTextColor(getResources().getColor(R.color.black));
		mHome.setTextColor(getResources().getColor(R.color.black));
		mFriend.setTextColor(getResources().getColor(R.color.black));
		mMessage.setTextColor(getResources().getColor(R.color.black));
	}

	private void initView()
	{

		ActionBar actionBar = ((ActionBarActivity)this).getSupportActionBar();
		if(HyjApplication.getIsDebuggable()){
			actionBar.setTitle("好友记(测试版)");
		}
		mTabAccount = (LinearLayout) findViewById(R.id.id_tab_account_ly);
		mTabProject = (LinearLayout) findViewById(R.id.id_tab_project_ly);
		mTabHome = (LinearLayout) findViewById(R.id.id_tab_home_ly);
		mTabFriend = (LinearLayout) findViewById(R.id.id_tab_friend_ly);
		mTabMessage = (LinearLayout) findViewById(R.id.id_tab_message_ly);

		mTabAccount.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(0);
			}
		});
		mTabProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(1);
			}
		});
		mTabHome.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(2);
			}
		});
		mTabFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(3);
			}
		});
		mTabMessage.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(4);
			}
		});
		
		mAccount = (TextView) findViewById(R.id.id_account);
		mProject = (TextView) findViewById(R.id.id_project);
		mHome = (TextView) findViewById(R.id.id_home);
		mFriend = (TextView) findViewById(R.id.id_friend);
		mMessage = (TextView) findViewById(R.id.id_message);

		mBadgeViewforAccount = new BadgeView(this);
		mBadgeViewforProject = new BadgeView(this);
		mBadgeViewforHome = new BadgeView(this);
		mBadgeViewforFriend = new BadgeView(this);
		mBadgeViewforMessage = new BadgeView(this);
		
		mBadgeViewforMessage.setHideOnNull(true);
		mBadgeViewforMessage.setBadgeCount(0);
		mBadgeViewforMessage.setMaxLines(1);
		mBadgeViewforMessage.setSingleLine();
//		mBadgeViewforMessage.setEllipsize(TruncateAt.END);
		mTabMessage.removeView(mBadgeViewforMessage);
		mTabMessage.addView(mBadgeViewforMessage);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	// @Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		// If the nav drawer is open, hide action items related to the content
//		// view
//		// boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
//
////		MenuItem refreshItem = menu.findItem(R.id.mainActivity_action_sync);
////		if (refreshItem != null) {
////			final View view = MenuItemCompat.getActionView(refreshItem);
////			if(HyjApplication.getInstance().getIsSyncing()){
//////				setRefreshActionButtonState(true, updateUploadCount(view, null));
////			} else {
////				updateUploadCount(view, null);
////			}
////			view.setOnClickListener(new OnClickListener() {
////				@Override
////				public void onClick(View v) {
////					if(!HyjUtil.hasNetworkConnection()){
////						HyjUtil.displayToast(R.string.server_connection_disconnected);
////						return;
////					}
////					if (!HyjApplication.getInstance().getIsSyncing()) {
////						uploadData(true);
////					}
////				}
////			});
////		}
//
//		return super.onPrepareOptionsMenu(menu);
//	}

//	private class DrawerItemClickListener implements
//			ListView.OnItemClickListener {
//		@Override
//		public void onItemClick(AdapterView parent, View view, int position,
//				long id) {
//			selectItem(position);
//		}
//	}

//	private void selectItem(int position) {
//		// Highlight the selected item, update the title, and close the drawer
//		mDrawerList.setItemChecked(position, true);
//		switch (position) {
//		 case 0 :
//				 openActivityWithFragment(MoneySearchListFragment.class,
//				 R.string.moneySearchListFragment_title, null);
//				 break;
//		 case 1 :
//				 openActivityWithFragment(MoneyReportFragment.class,
//				 R.string.moneyReportFragment_title, null);
//				 break;
////		case 2:
////			openActivityWithFragment(MessageListFragment.class,
////					R.string.friendListFragment_title_manage_message, null);
////			break;
//		
//		case 2:
//			openActivityWithFragment(CurrencyExchangeViewPagerListFragment.class,
//					R.string.currency_exchang_eviewpager_listFragment_title, null);
//			break;
//		
////		case 3:
////			openActivityWithFragment(ExchangeListFragment.class,
////					R.string.exchangeListFragment_title_manage_exchange, null);
////			break;
////		case 4:
////			openActivityWithFragment(CurrencyListFragment.class,
////					R.string.currencyListFragment_title_manage_currency, null);
////			break;
////		case 4:
////			openActivityWithFragment(MoneyExpenseCategoryListFragment.class,
////					R.string.moneyCategoryFormDialogFragment_title_manage_expense, null);
////			break;
////		case 5:
////			openActivityWithFragment(MoneyIncomeCategoryListFragment.class,
////					R.string.moneyCategoryFormDialogFragment_title_manage_income, null);
////			break;
//		case 3:
//			openActivityWithFragment(ExpenseIncomeCategoryViewPagerListFragment.class,
//					R.string.expense_income_viewpager_listFragment_title, null);
//			break;
//		case 4:
//			openActivityWithFragment(SystemSettingFormFragment.class,
//					R.string.systemSettingFormFragment_title, null);
//			break;
////		case 5:
////			HyjApplication.getInstance().switchUser();
////			break;
//		}
//
//		mDrawerLayout.closeDrawer(mDrawerList);
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		
		mSyncButton = menu.findItem(R.id.mainActivity_action_sync);
		if (mSyncButton != null) {
			final View view = MenuItemCompat.getActionView(mSyncButton);
			updateUploadCount(view, null);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(!HyjUtil.hasNetworkConnection()){
						HyjUtil.displayToast(R.string.server_connection_disconnected);
						return;
					}

					if (!HyjApplication.getInstance().getIsSyncing()) {
						Intent startIntent = new Intent(HyjApplication.getInstance().getApplicationContext(), MessageDownloadService.class);
						startService(startIntent);

						uploadData(true);
					}
				}
			});
		}

		return true;
	}

//	@Override
//	protected void onPostCreate(Bundle savedInstanceState) {
//		super.onPostCreate(savedInstanceState);
//		// Sync the toggle state after onRestoreInstanceState has occurred.
//		mDrawerToggle.syncState();
//	}
//
//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		super.onConfigurationChanged(newConfig);
//		mDrawerToggle.onConfigurationChanged(newConfig);
//	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Pass the event to ActionBarDrawerToggle, if it returns
//		// true, then it has handled the app icon touch event
//		if (mDrawerToggle.onOptionsItemSelected(item)) {
//			return true;
//		}
//
////		 if(item.getItemId() == R.id.mainActivity_action_sync){
////			 	if(!HyjUtil.hasNetworkConnection()){
////					HyjUtil.displayToast(R.string.server_connection_disconnected);
////					return true;
////				}
////				if (!HyjApplication.getInstance().getIsSyncing()) {
////					uploadData(true);
////					return true;
////				}
////		 }
//		
//		return super.onOptionsItemSelected(item);
//	}

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
			switch (position) {
			case 0:
				fragment = new MoneyAccountListFragment();
				break;
			case 1:
				fragment = new ProjectListFragment();
				break;
			case 2:
				fragment = new HomeListFragment();
				break;
			case 3:
				fragment = new FriendListFragment();
				break;
			case 4:
				fragment = new MessageListFragment();
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(
						R.string.mainActivity_section_title_moneyaccount)
						.toUpperCase(l);
			case 1:
				return getString(R.string.mainActivity_section_title_project)
						.toUpperCase(l);
			case 2:
				return getString(R.string.mainActivity_section_title_home)
						.toUpperCase(l);
			case 3:
				return getString(R.string.mainActivity_section_title_friend)
						.toUpperCase(l);
			case 4:
				return getString(R.string.mainActivity_section_title_message)
						.toUpperCase(l);
			}
			return null;
		}
	}

	public View setRefreshActionButtonState(final boolean refreshing, View view) {
		if (view == null) {
			if (mSyncButton != null) {
				view = MenuItemCompat.getActionView(mSyncButton);
			}
		}
		if(view != null){
			if (refreshing) {
				HyjUtil.startRoateView(view
						.findViewById(R.id.actionbar_sync_Image));
			} else {
				HyjUtil.stopRoateView(view
						.findViewById(R.id.actionbar_sync_Image));
			}
		}
		return view;
	}

	public View updateUploadCount(View view, Integer count) {
		if (view == null) {
			if (mSyncButton != null) {
				view = MenuItemCompat.getActionView(mSyncButton);
			}
		}
		if (view != null) {
			final TextView tv = (TextView) view
					.findViewById(R.id.actionbar_sync_uploadCount);
			if (count == null) {
				HyjAsyncTask.newInstance(new HyjAsyncTaskCallbacks() {
					@Override
					public void finishCallback(Object object) {
						Integer count = (Integer) object;
						if (count > 0) {
							tv.setText(count.toString());
						} else {
							tv.setText("");
						}
					}

					@Override
					public Object doInBackground(String... string) {
						Integer count = 0;
						if (HyjApplication.getInstance().getCurrentUser() != null) {
							Cursor cursor = Cache.openDatabase().rawQuery(
									"SELECT COUNT(*) FROM ClientSyncRecord",
									null);
							if (cursor != null) {
								cursor.moveToFirst();
								count = cursor.getInt(0);
								cursor.close();
								cursor = null;
							}
						}
						return count;
					}
				});
//				count = 0;
//				if (HyjApplication.getInstance().getCurrentUser() != null) {
//					Cursor cursor = Cache.openDatabase().rawQuery(
//							"SELECT COUNT(*) FROM ClientSyncRecord",
//							null);
//					if (cursor != null) {
//						cursor.moveToFirst();
//						count = cursor.getInt(0);
//						cursor.close();
//						cursor = null;
//					}
//				}
			} else {
				if (count > 0) {
					tv.setText(count.toString());
				} else {
					tv.setText("");
				}
			}
//			
		}
		return view;
	}

	private class ChangeObserver extends ContentObserver {
		public ChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

//		@Override
//		public void onChange(boolean selfChange, Uri uri) {
//			super.onChange(selfChange, uri);
//		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			uploadData(false);
		}
	}
	
	private class MessageChangeObserver extends ContentObserver {
		public MessageChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

//		@Override
//		public void onChange(boolean selfChange, Uri uri) {
//			super.onChange(selfChange, uri);
//		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			updateMessageCount();
		}
	}

//	@Override
//	protected void onNewIntent(Intent intent) {
//		// TODO Auto-generated method stub
//		super.onNewIntent(intent);
//		setIntent(intent);
//	}

	@Override
	protected void onResume() {
		super.onResume();
		XGPushClickedResult click = XGPushManager.onActivityStarted(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		XGPushManager.onActivityStoped(this);
	}
	
	@Override
	protected void onDestroy() {
		if (mChangeObserver != null) {
			this.getContentResolver()
					.unregisterContentObserver(mChangeObserver);
		}
		if (mMessageChangeObserver != null) {
			this.getContentResolver()
			.unregisterContentObserver(mMessageChangeObserver);
		}
		super.onDestroy();
	}
	
	public void downloadData() {
//		HyjAsyncTask.newInstance(new HyjAsyncTaskCallbacks() {
//			@Override

		new Thread(new Runnable() {
			public void finishCallback(final Object object) {
				MainActivity.this.runOnUiThread(new Runnable() {
					public void run() {
					HyjApplication.getInstance().setIsSyncing(false);
					if (object instanceof Boolean) {
						Boolean result = (Boolean) object;
						if (result == true) {
									((HyjActivity) MainActivity.this)
											.dismissProgressDialog();
									setRefreshActionButtonState(false, null);
									HyjUtil.displayToast("同步数据成功");
				                }
                		}
					}
				});
			}

//			@Override
			public void errorCallback(final Object object) {
				MainActivity.this.runOnUiThread(new Runnable() {
	                public void run() {
				HyjApplication.getInstance().setIsSyncing(false);
				if (object instanceof Boolean) {
					// Boolean result = (Boolean)object;
					// if(result == true){
					return;
					// }
				}

						setRefreshActionButtonState(false, null);
						HyjUtil.displayToast(object.toString());
						((HyjActivity) MainActivity.this).dismissProgressDialog();
	                }
	           });
			}

//			@Override
//			public Object doInBackground(String... string) {
	        public void run() {
				String postData =  HyjApplication.getInstance().getCurrentUser().getUserData().getLastSyncTime();
				if(postData == null){
					postData = "null";
				}
				Object result = HyjServer.doHttpPost(null,
						HyjApplication.getServerUrl() + "syncPull.php", postData, true);
				if (result == null) {
					errorCallback( HyjApplication.getInstance().getResources()
							.getString(R.string.server_dataparse_error));
				}
				if (result instanceof JSONObject) {
					JSONObject jsonResult = (JSONObject) result;
					if (jsonResult.isNull("__summary")) {
						try {

							saveData(jsonResult.getJSONArray("data"), 
									jsonResult.optString("lastSyncTime"));

							finishCallback( true);
						} catch (JSONException e) {
							errorCallback("下载数据失败，请重试");
						}
					} else {
						errorCallback(jsonResult.optJSONObject("__summary").optString(
								"msg"));
					}
					// } else if(result instanceof JSONArray) {
					// saveData((JSONArray)result);
				} else {
					errorCallback("下载数据失败，请重试");
				}
			}
		}).start();
	}

	private void saveDataRecursive(JSONArray result) throws JSONException {
		for (int i = 0; i < result.length(); i++) {
			Object o = result.get(i);
			if (o instanceof JSONArray) {
				saveDataRecursive((JSONArray) o);
			} else {
				JSONObject jsonObj = (JSONObject) o;
				String dataType = jsonObj.getString("__dataType");
				if(HyjApplication.getIsDebuggable()){
					Log.i("Downloaded Data " + dataType, jsonObj.toString());
				}
				HyjModel model = null;
				if (dataType.equals("ServerSyncDeletedRecords")) {
					model = HyjModel.createModel(jsonObj.getString("tableName"), jsonObj.getString("recordId"));
					if(model != null && model.get_mId() != null){
						if(jsonObj.getString("tableName").equals("MoneyExpenseApportion")){
							MoneyExpenseApportion apportion = (MoneyExpenseApportion)model;
							// 如果被删除的分摊是别人分摊给我的
							if(!apportion.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId()) && HyjApplication.getInstance().getCurrentUser().getId().equals(apportion.getFriendUserId())){
								MoneyExpenseContainer moneyExpenseContainer = apportion.getMoneyExpenseContainer();
								// 看该分摊对应的支出是不是没有权限了，如果是，就移除它以及其他相关的分摊
								if(moneyExpenseContainer != null 
										&& !moneyExpenseContainer.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())
										&& !HyjApplication.getInstance().getCurrentUser().getId().equals(moneyExpenseContainer.getFriendUserId())){
									ProjectShareAuthorization pst = ProjectShareAuthorization.getSelfProjectShareAuthorization(moneyExpenseContainer.getProjectId());
									if(pst != null && pst.getProjectShareMoneyExpenseOwnerDataOnly() == true){
										//删除支出的同时删除分摊
										Iterator<MoneyExpenseApportion> moneyExpenseApportions = moneyExpenseContainer.getApportions().iterator();
										while (moneyExpenseApportions.hasNext()) {
											MoneyExpenseApportion moneyExpenseApportion = moneyExpenseApportions.next();
											if(!moneyExpenseApportion.getId().equals(model.getId())){
												MoneyLend moneyLend = new Select().from(MoneyLend.class).where("moneyExpenseApportionId=?", moneyExpenseApportion.getId()).executeSingle();
												if(moneyLend != null){
													moneyLend.deleteFromServer();
												}
												MoneyExpense moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", moneyExpenseApportion.getId()).executeSingle();
												if(moneyExpense != null){
													moneyExpense.deleteFromServer();
												}
												MoneyBorrow moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyExpenseApportionId=?", moneyExpenseApportion.getId()).executeSingle();
												if(moneyBorrow != null){
													moneyBorrow.deleteFromServer();
												} 
												moneyExpenseApportion.deleteFromServer();
											}
										}
										
										moneyExpenseContainer.deleteFromServer();
									}
								}
							}
						} else if(jsonObj.getString("tableName").equals("MoneyIncomeApportion")){
							MoneyIncomeApportion apportion = (MoneyIncomeApportion)model;
							// 如果被删除的分摊是别人分摊给我的
							if(!apportion.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId()) && HyjApplication.getInstance().getCurrentUser().getId().equals(apportion.getFriendUserId())){
								MoneyIncomeContainer moneyIncomeContainer = apportion.getMoneyIncomeContainer();
								// 看该分摊对应的支出是不是没有权限了，如果是，就移除它以及其他相关的分摊
								if(moneyIncomeContainer != null 
										&& !moneyIncomeContainer.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())
										&& !HyjApplication.getInstance().getCurrentUser().getId().equals(moneyIncomeContainer.getFriendUserId())){
									ProjectShareAuthorization pst = ProjectShareAuthorization.getSelfProjectShareAuthorization(moneyIncomeContainer.getProjectId());
									if(pst != null && pst.getProjectShareMoneyExpenseOwnerDataOnly() == true){
										//删除支出的同时删除分摊
										Iterator<MoneyIncomeApportion> moneyIncomeApportions = moneyIncomeContainer.getApportions().iterator();
										while (moneyIncomeApportions.hasNext()) {
											MoneyIncomeApportion moneyIncomeApportion = moneyIncomeApportions.next();
											if(!moneyIncomeApportion.getId().equals(model.getId())){
												MoneyBorrow moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyIncomeApportionId=?", moneyIncomeApportion.getId()).executeSingle();
												if(moneyBorrow != null){
													moneyBorrow.deleteFromServer();
												} 
												MoneyIncome moneyIncome = new Select().from(MoneyIncome.class).where("moneyIncomeApportionId=?", moneyIncomeApportion.getId()).executeSingle();
												if(moneyIncome != null){
													moneyIncome.deleteFromServer();
												}
												
												MoneyLend moneyLend = new Select().from(MoneyLend.class).where("moneyIncomeApportionId=?", moneyIncomeApportion.getId()).executeSingle();
												if(moneyLend != null){
													moneyLend.deleteFromServer();
												} 
												moneyIncomeApportion.deleteFromServer();
											}
										}
										
										moneyIncomeContainer.deleteFromServer();
									}
								}
							}
						}
						model.deleteFromServer();
					}
				} else if (dataType.equals("MoveProject")) {
					model = HyjModel.createModel(jsonObj.getString("tableName"), jsonObj.getString("recordId"));
					if(model != null && model.get_mId() != null){
						model.deleteFromServer();
					}
				} else {
					model = HyjModel.createModel(dataType, jsonObj.getString("id"));
					if(model != null){
//						if(model.get_mId() != null){
//							if(jsonObj.getString("tableName").equals("MoneyExpenseApportion")){
//								
//							}
//						}
						model.loadFromJSON(jsonObj, true);
						model.save();
					}
				}
			}
		}
	}

	private void saveData(JSONArray result, String lastSyncTime) throws JSONException {
//		try {
//			for(int a = 0; a < result.length(); a++){
//				Log.i("Downloaded Data : ", result.get(a).toString());
//			}
			ActiveAndroid.beginTransaction();
			
			saveDataRecursive(result);

			HyjApplication
					.getInstance()
					.getCurrentUser()
					.getUserData()
					.setLastSyncTime(lastSyncTime);
			Cache.openDatabase().execSQL(
					"Update UserData SET lastSyncTime = '"
							+ HyjApplication.getInstance()
									.getCurrentUser()
									.getUserData()
									.getLastSyncTime()
							+ "' WHERE id = '"
							+ HyjApplication.getInstance()
									.getCurrentUser()
									.getUserDataId() + "'");

			ActiveAndroid.setTransactionSuccessful();
			ActiveAndroid.endTransaction();
//		} catch (JSONException e) {
//			ActiveAndroid.endTransaction();
//			((HyjActivity) MainActivity.this).dismissProgressDialog();
//			HyjUtil.displayToast("下载数据失败，请重试");
//		}
	}

	public void updateMessageCount() {
		HyjAsyncTask.newInstance(new HyjAsyncTaskCallbacks() {
			
			@Override
			public void finishCallback(final Object object) {
				int count = (Integer)object;
				if(count > 99){
					count = 99;
				}
				mBadgeViewforMessage.setBadgeCount(count);
			}
	
			@Override
			public void errorCallback(final Object object) {
			}
	
			@Override
			public Object doInBackground(String... string) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				int count = 0;
				if (HyjApplication.getInstance().getCurrentUser() != null) {
					Cursor cursor = Cache.openDatabase().rawQuery(
							"SELECT COUNT(*) FROM Message WHERE messageState = 'new' OR messageState = 'unread'",
							null);
					if (cursor != null) {
						cursor.moveToFirst();
						count = cursor.getInt(0);
						cursor.close();
						cursor = null;
					}
				}
				return count;
				
			}
		});
		
	}
	
	public void uploadData(final boolean downloadData) {
		if (HyjApplication.getInstance().getIsSyncing()) {
			return;
		}

		HyjApplication.getInstance().setIsSyncing(true);
		if(!HyjUtil.hasNetworkConnection()){
//			updateUploadCount(null, null);
			HyjApplication.getInstance().setIsSyncing(false);
			return;
		} else {
			setRefreshActionButtonState(true, updateUploadCount(null, null));
		}
		
		if (downloadData) {
			((HyjActivity) MainActivity.this).displayProgressDialog("同步数据",
					"正在同步数据，请稍后...");
		}
		new Thread(new Runnable() {
//		HyjAsyncTask.newInstance(new HyjAsyncTaskCallbacks() {
//			@Override
			public void finishCallback(final Object object) {
				MainActivity.this.runOnUiThread(new Runnable() {
	                public void run() {
						if (object instanceof Boolean) {
							Boolean result = (Boolean) object;
							if (result == true) {
								Cache.openDatabase()
										.execSQL(
												"DELETE FROM ClientSyncRecord WHERE uploading = 1");
								
								// HyjUtil.displayToast("上传数据成功");
		
								HyjApplication.getInstance().setIsSyncing(downloadData);
		
								if (downloadData) {
						            updateUploadCount(null, null);
									downloadData();
								} else {
				                	setRefreshActionButtonState(false,
									updateUploadCount(null, null));
								}
								
								// 上传大图
								Intent startPictureUploadService = new Intent(HyjApplication.getInstance().getApplicationContext(), PictureUploadService.class);
								startService(startPictureUploadService);
							}
						}
	                }
				});
			}

//			@Override
			public void errorCallback(final Object object) {
				MainActivity.this.runOnUiThread(new Runnable() {
	                public void run() {
						if (object instanceof Boolean) {
							// Boolean result = (Boolean)object;
							// if(result == true){
							return;
							// }
						}
						HyjApplication.getInstance().setIsSyncing(false);
						setRefreshActionButtonState(false, null);
						HyjUtil.displayToast(object.toString());
						if (downloadData) {
							((HyjActivity) MainActivity.this).dismissProgressDialog();
						}
	                }
				});
	                
			}

//			@Override
//			public Object doInBackground(String... string) {
	        public void run() {
				if (!downloadData) {
					try {
						// 等待其他一起修改的记录都提交了再上传
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				List<ClientSyncRecord> syncRecords = null;
				try {
					ActiveAndroid.beginTransaction();
//					Cache.openDatabase()
//							.execSQL(
//									"Update ClientSyncRecord SET uploading = 1 WHERE uploading = 0");
					syncRecords = new Select().from(ClientSyncRecord.class)
							.execute();
					for (ClientSyncRecord syncRec : syncRecords) {
						if(!syncRec.getUploading()){
							syncRec.setUploading(true);
							syncRec.save();
						}
					}
					ActiveAndroid.setTransactionSuccessful();
					ActiveAndroid.endTransaction();
				} catch (Exception e) {
					ActiveAndroid.endTransaction();
					rollbackUpload(syncRecords);
					errorCallback("上传数据失败");
				}

				if (syncRecords != null && syncRecords.size() == 0) {
					// 没有记录可上传
					finishCallback(true);
					return;
				}

				try {
					JSONArray postData = new JSONArray();
					for (ClientSyncRecord syncRec : syncRecords) {
						JSONObject jsonObj = new JSONObject();
						if (syncRec.getOperation().equalsIgnoreCase("Create")) {
							HyjModel model = HyjModel.createModel(syncRec.getTableName(),
									syncRec.getId());
							if(model.get_mId() != null){
								jsonObj.put("operation", "create");
								JSONObject recordData = model.toJSON();
								if(model instanceof MoneyApportion){
									recordData.put("date", ((MoneyApportion)model).getDate());
									recordData.put("projectId", ((MoneyApportion)model).getProject().getId());
									recordData.put("currencyId", ((MoneyApportion)model).getCurrencyId());
									recordData.put("exchangeRate", ((MoneyApportion)model).getExchangeRate());
									recordData.put("projectCurrencySymbol", ((MoneyApportion)model).getProject().getCurrencySymbol());
									recordData.put("projectCurrencyId", ((MoneyApportion)model).getProject().getCurrencyId());
									if(model instanceof MoneyDepositIncomeApportion){
										MoneyDepositIncomeApportion moneyDepositIncomeApportion = (MoneyDepositIncomeApportion)model;
										if(moneyDepositIncomeApportion != null){
											recordData.put("isImported", moneyDepositIncomeApportion.getMoneyDepositIncomeContainer().getIsImported());
										}
									} else if(model instanceof MoneyDepositReturnApportion){
										MoneyDepositReturnApportion moneyDepositReturnApportion = (MoneyDepositReturnApportion)model;
										if(moneyDepositReturnApportion != null){
											recordData.put("isImported", moneyDepositReturnApportion.getMoneyDepositReturnContainer().getIsImported());
										}
									} else if(model instanceof MoneyExpenseApportion){
										MoneyExpenseApportion moneyExpenseApportion = (MoneyExpenseApportion)model;
										if(moneyExpenseApportion != null){
											recordData.put("isImported", moneyExpenseApportion.getMoneyExpenseContainer().getIsImported());
										}
									} else if(model instanceof MoneyIncomeApportion){
										MoneyIncomeApportion moneyIncomeApportion = (MoneyIncomeApportion)model;
										if(moneyIncomeApportion != null){
											recordData.put("isImported", moneyIncomeApportion.getMoneyIncomeContainer().getIsImported());
										}
									}
								} else if(model instanceof MoneyExpenseContainer){
									recordData.put("projectCurrencySymbol", ((MoneyExpenseContainer)model).getProject().getCurrencySymbol());
									recordData.put("projectCurrencyId", ((MoneyExpenseContainer)model).getProject().getCurrencyId());
									recordData.put("currencyId", ((MoneyExpenseContainer)model).getMoneyAccount().getCurrencyId());
								} else if(model instanceof MoneyIncomeContainer){
									recordData.put("projectCurrencySymbol", ((MoneyIncomeContainer)model).getProject().getCurrencySymbol());
									recordData.put("projectCurrencyId", ((MoneyIncomeContainer)model).getProject().getCurrencyId());
									recordData.put("currencyId", ((MoneyIncomeContainer)model).getMoneyAccount().getCurrencyId());
								} else if(model instanceof MoneyBorrowContainer){
									recordData.put("currencyId", ((MoneyBorrowContainer)model).getMoneyAccount().getCurrencyId());
								} else if(model instanceof MoneyLendContainer){
									recordData.put("currencyId", ((MoneyLendContainer)model).getMoneyAccount().getCurrencyId());
								} else if(model instanceof MoneyPaybackContainer){
									recordData.put("currencyId", ((MoneyPaybackContainer)model).getMoneyAccount().getCurrencyId());
								} else if(model instanceof MoneyReturnContainer){
									recordData.put("currencyId", ((MoneyReturnContainer)model).getMoneyAccount().getCurrencyId());
								} 
//								else if(model instanceof MoneyExpense){
//									if(((MoneyExpense)model).getMoneyAccount() != null){
//										recordData.put("currencyId", ((MoneyExpense)model).getMoneyAccount().getCurrencyId());
//									} else {
//										recordData.put("currencyId", ((MoneyExpense)model).getMoneyExpenseApportion().getCurrencyId());
//									}
//								} else if(model instanceof MoneyIncome){
//									if(((MoneyIncome)model).getMoneyAccount() != null){
//										recordData.put("currencyId", ((MoneyIncome)model).getMoneyAccount().getCurrencyId());
//									}
//								} 
								else if(model instanceof MoneyBorrow){
//									if(((MoneyBorrow)model).getMoneyAccount() != null){
//										recordData.put("currencyId", ((MoneyBorrow)model).getMoneyAccount().getCurrencyId());
//									} else 
//									if(((MoneyBorrow)model).getMoneyExpenseApportion() != null){
//										recordData.put("moneyExpenseApportionFriendUserId", ((MoneyBorrow)model).getMoneyExpenseApportion().getFriendUserId());
//									} else if(((MoneyBorrow)model).getMoneyIncomeApportion() != null){
//										recordData.put("moneyIncomeApportionFriendUserId", ((MoneyBorrow)model).getMoneyIncomeApportion().getFriendUserId());
//									}
									recordData.put("projectCurrencySymbol", ((MoneyBorrow)model).getProject().getCurrencySymbol());
								} else if(model instanceof MoneyLend){
//									if(((MoneyLend)model).getMoneyAccount() != null){
//										recordData.put("currencyId", ((MoneyLend)model).getMoneyAccount().getCurrencyId());
//									} else 
//									if(((MoneyLend)model).getMoneyExpenseApportion() != null){
//										recordData.put("moneyExpenseApportionFriendUserId", ((MoneyLend)model).getMoneyExpenseApportion().getFriendUserId());
//									} else if(((MoneyLend)model).getMoneyIncomeApportion() != null){
//										recordData.put("moneyIncomeApportionFriendUserId", ((MoneyLend)model).getMoneyIncomeApportion().getFriendUserId());
//									}
									recordData.put("projectCurrencySymbol", ((MoneyLend)model).getProject().getCurrencySymbol());
								} else if(model instanceof MoneyPayback){
//									if(((MoneyPayback)model).getMoneyAccount() != null){
//										recordData.put("currencyId", ((MoneyPayback)model).getMoneyAccount().getCurrencyId());
//									}
									recordData.put("projectCurrencySymbol", ((MoneyPayback)model).getProject().getCurrencySymbol());
								} else if(model instanceof MoneyReturn){
//									if(((MoneyReturn)model).getMoneyAccount() != null){
//										recordData.put("currencyId", ((MoneyReturn)model).getMoneyAccount().getCurrencyId());
//									}
									recordData.put("projectCurrencySymbol", ((MoneyReturn)model).getProject().getCurrencySymbol());
								}
								jsonObj.put( "recordData", recordData);
								postData.put(jsonObj);
							}
						} else if (syncRec.getOperation().equalsIgnoreCase(
								"Update")) {
							HyjModel model = HyjModel.createModel(syncRec.getTableName(),
									syncRec.getId());
							if(model.get_mId() != null){
								jsonObj.put("operation", "update");
								JSONObject recordData = model.toJSON();
								if(model instanceof MoneyApportion){
									recordData.put("projectId", ((MoneyApportion)model).getProject().getId());
									recordData.put("moneyAccountId", ((MoneyApportion)model).getMoneyAccountId());
									recordData.put("currencyId", ((MoneyApportion)model).getCurrencyId());
									recordData.put("exchangeRate", ((MoneyApportion)model).getExchangeRate());
								} else if(model instanceof MoneyExpenseContainer){
									recordData.put("currencyId", ((MoneyExpenseContainer)model).getMoneyAccount().getCurrencyId());
								} else if(model instanceof MoneyIncomeContainer){
									recordData.put("currencyId", ((MoneyIncomeContainer)model).getMoneyAccount().getCurrencyId());
								} else if(model instanceof MoneyBorrowContainer){
									recordData.put("currencyId", ((MoneyBorrowContainer)model).getMoneyAccount().getCurrencyId());
								} else if(model instanceof MoneyLendContainer){
									recordData.put("currencyId", ((MoneyLendContainer)model).getMoneyAccount().getCurrencyId());
								} else if(model instanceof MoneyPaybackContainer){
									recordData.put("currencyId", ((MoneyPaybackContainer)model).getMoneyAccount().getCurrencyId());
								} else if(model instanceof MoneyReturnContainer){
									recordData.put("currencyId", ((MoneyReturnContainer)model).getMoneyAccount().getCurrencyId());
								} 
//								else if(model instanceof MoneyExpense){
//									if(((MoneyExpense)model).getMoneyAccount() != null){
//										recordData.put("currencyId", ((MoneyExpense)model).getMoneyAccount().getCurrencyId());
//									} else {
//										recordData.put("currencyId", ((MoneyExpense)model).getMoneyExpenseApportion().getCurrencyId());
//									}
//								} else if(model instanceof MoneyIncome){
//									if(((MoneyIncome)model).getMoneyAccount() != null){
//										recordData.put("currencyId", ((MoneyIncome)model).getMoneyAccount().getCurrencyId());
//									}
//								} else if(model instanceof MoneyBorrow){
//									if(((MoneyBorrow)model).getMoneyAccount() != null){
//										recordData.put("currencyId", ((MoneyBorrow)model).getMoneyAccount().getCurrencyId());
//									} else if(((MoneyBorrow)model).getMoneyExpenseApportion() != null){
//										recordData.put("currencyId", ((MoneyBorrow)model).getMoneyExpenseApportion().getCurrencyId());
//									} else if(((MoneyBorrow)model).getMoneyIncomeApportion() != null){
//										recordData.put("currencyId", ((MoneyBorrow)model).getMoneyIncomeApportion().getCurrencyId());
//									}
//								} else if(model instanceof MoneyLend){
//									if(((MoneyLend)model).getMoneyAccount() != null){
//										recordData.put("currencyId", ((MoneyLend)model).getMoneyAccount().getCurrencyId());
//									} else if(((MoneyLend)model).getMoneyExpenseApportion() != null){
//										recordData.put("currencyId", ((MoneyLend)model).getMoneyExpenseApportion().getCurrencyId());
//									} else if(((MoneyLend)model).getMoneyIncomeApportion() != null){
//										recordData.put("currencyId", ((MoneyLend)model).getMoneyIncomeApportion().getCurrencyId());
//									}
//								} else if(model instanceof MoneyPayback){
//									if(((MoneyPayback)model).getMoneyAccount() != null){
//										recordData.put("currencyId", ((MoneyPayback)model).getMoneyAccount().getCurrencyId());
//									}
//								} else if(model instanceof MoneyReturn){
//									if(((MoneyReturn)model).getMoneyAccount() != null){
//										recordData.put("currencyId", ((MoneyReturn)model).getMoneyAccount().getCurrencyId());
//									}
//								}
								jsonObj.put("recordData", recordData);
								postData.put(jsonObj);
							}
						} else if (syncRec.getOperation().equalsIgnoreCase(
								"Delete")) {
							jsonObj.put("operation", "delete");
							JSONObject recordData = new JSONObject();
							recordData.put("id", syncRec.getId());
							recordData.put("__dataType", syncRec.getTableName());
							jsonObj.put("recordData", recordData);
							postData.put(jsonObj);
						}
					}
					
					if(HyjApplication.getIsDebuggable()){
						for(int a = 0; a < postData.length(); a++){
							Log.i("Push Data : ", postData.get(a).toString());
						}
					}
					
					Object result = HyjServer.doHttpPost(null,
							HyjApplication.getServerUrl() + "syncPush2.php",
							postData.toString(), true);
					if (result == null) {
						rollbackUpload(syncRecords);
						errorCallback(HyjApplication.getInstance().getString(
								R.string.server_dataparse_error));
						return;
					}
					if (result instanceof JSONObject) {
						JSONObject jsonResult = (JSONObject) result;
						if (jsonResult.isNull("__summary")) {
							String lastUploadTime = jsonResult.optString("lastUploadTime");
							if(lastUploadTime.length() > 0){
								try {
									ActiveAndroid.beginTransaction();
									for (ClientSyncRecord syncRec : syncRecords) {
										if (!syncRec.getOperation().equalsIgnoreCase("Delete")) {
											HyjModel model = HyjModel.createModel(syncRec.getTableName(),
													syncRec.getId());
											model.setSyncFromServer(true);
											model.setLastServerUpdateTime(lastUploadTime);
											model.save();
										} 
									}
									ActiveAndroid.setTransactionSuccessful();
									ActiveAndroid.endTransaction();
									finishCallback(true);
								} catch (Exception e) {
									ActiveAndroid.endTransaction();
									rollbackUpload(syncRecords);
									errorCallback("上传数据失败");
								}
							} else {
								rollbackUpload(syncRecords);
								errorCallback("上传数据失败");
							}
						} else {
							rollbackUpload(syncRecords);
							errorCallback(jsonResult.optJSONObject("__summary")
									.optString("msg"));
						}
					} else {
						rollbackUpload(syncRecords);
						errorCallback("上传数据失败");
					}
				} catch (JSONException e) {
					rollbackUpload(syncRecords);
					errorCallback("上传数据失败");
				}
			}

			private void rollbackUpload(List<ClientSyncRecord> syncRecords) {
				if (syncRecords == null) {
					return;
				}
				try {
					ActiveAndroid.beginTransaction();
					for (ClientSyncRecord syncRec : syncRecords) {
						if (syncRec.getOperation().equalsIgnoreCase("Create")) {
							// 获取最新的更新记录，不能直接用 HyjModel.getModel，那样拿到的可能是被缓存的记录
							ClientSyncRecord rec = new Select().from(ClientSyncRecord.class).where("id=?", syncRec.getId()).executeSingle();
//									HyjModel.getModel(ClientSyncRecord.class, syncRec.getId());
							if (rec.getUploading() == true) {
								rec.setUploading(false);
								if (rec.getOperation().equalsIgnoreCase("Delete")) {
									//该记录在上传期间已被删除了，所以我们把这条更新记录也删除掉，不用上传了
									rec.delete();
								} else if (rec.getOperation().equalsIgnoreCase("Update")) {
									// 在上传的期间被改变了，但是该记录实际上没上传成功，所以我们还是放回新创建状态
									rec.setOperation("Create");
									rec.save();
								} else {
									// 保存 uploading 状态
									rec.save();
								}
							}
						}
					}
					ActiveAndroid.setTransactionSuccessful();
				} catch (Exception e) {
				}
				ActiveAndroid.endTransaction();
				
				// Cache.openDatabase().execSQL(
				// "Update ClientSyncRecord SET uploading = 0 WHERE uploading = 1");
			}

		}).start();
	}


//	private static class HandlerExtension extends Handler {
//		WeakReference<MainActivity> mActivity;
//
//		HandlerExtension(MainActivity activity) {
//			mActivity = new WeakReference<MainActivity>(activity);
//		}
//
//		@Override
//		public void handleMessage(android.os.Message msg) {
//			super.handleMessage(msg);
//			MainActivity theActivity = mActivity.get();
//			if (msg != null) {
//				Log.w(Constants.LogTag, msg.obj.toString());
//				TextView textView = (TextView) theActivity
//						.findViewById(R.id.deviceToken);
//				textView.setText(XGPushConfig.getToken(theActivity));
//			}
//			// XGPushManager.registerCustomNotification(theActivity,
//			// "BACKSTREET", "BOYS", System.currentTimeMillis() + 5000, 0);
//		}
//	}
}
