package com.hoyoji.hoyoji;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.Cache;
import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTask;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjClientSyncRecord;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjUserActivity;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.userdatabase.HyjUserDbHelper;
import com.hoyoji.android.hyjframework.userdatabase.HyjUserDbContract.UserDatabaseEntry;
import com.hoyoji.hoyoji.friend.FriendListFragment;
import com.hoyoji.hoyoji.home.HomeListFragment;
import com.hoyoji.hoyoji.message.MessageDownloadService;
import com.hoyoji.hoyoji.message.MessageListFragment;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.money.MoneyBorrowFormFragment;
import com.hoyoji.hoyoji.money.MoneyBorrowListFragment;
import com.hoyoji.hoyoji.money.MoneyExpenseFormFragment;
import com.hoyoji.hoyoji.money.MoneyExpenseListFragment;
import com.hoyoji.hoyoji.money.MoneyIncomeFormFragment;
import com.hoyoji.hoyoji.money.MoneyIncomeListFragment;
import com.hoyoji.hoyoji.money.MoneyLendFormFragment;
import com.hoyoji.hoyoji.money.MoneyLendListFragment;
import com.hoyoji.hoyoji.money.MoneyPaybackFormFragment;
import com.hoyoji.hoyoji.money.MoneyPaybackListFragment;
import com.hoyoji.hoyoji.money.MoneyReturnFormFragment;
import com.hoyoji.hoyoji.money.MoneyReturnListFragment;
import com.hoyoji.hoyoji.money.MoneyTransferFormFragment;
import com.hoyoji.hoyoji.money.MoneyTransferListFragment;
import com.hoyoji.hoyoji.money.currency.CurrencyListFragment;
import com.hoyoji.hoyoji.money.currency.ExchangeListFragment;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;

public class MainActivity extends HyjUserActivity {
	private Menu mOptionsMenu;
	private boolean mIsUploading = false;
	
	@Override
	protected void onDestroy() {
		if (mChangeObserver != null) {
			this.getContentResolver()
					.unregisterContentObserver(mChangeObserver);
		}
		super.onDestroy();
	}

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
	ChangeObserver mChangeObserver;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected Integer getContentView() {
		return R.layout.activity_main;
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (HyjApplication.getInstance().isLoggedIn()) {
			Intent startIntent = new Intent(this, MessageDownloadService.class);
			startService(startIntent);
			if (mChangeObserver == null) {
				mChangeObserver = new ChangeObserver();
				this.getContentResolver().registerContentObserver(
						ContentProvider.createUri(HyjClientSyncRecord.class,
								null), true, mChangeObserver);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		// Set the adapter for the list view
		mDrawerListerTitles = getResources().getStringArray(
				R.array.mainActivity_drawer_list_titles);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.main_drawer_list_item, mDrawerListerTitles));
		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.mainActivity_drawer_open,
				R.string.mainActivity_drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				supportInvalidateOptionsMenu(); // creates call to
												// onPrepareOptionsMenu()
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				supportInvalidateOptionsMenu(); // creates call to
												// onPrepareOptionsMenu()
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
		mViewPager.setOffscreenPageLimit(4);
		mViewPager.setCurrentItem(2, false);

	}

	/* Called whenever we call invalidateOptionsMenu() */
	// @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		// boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);

		MenuItem refreshItem = menu.findItem(R.id.homeListFragment_action_sync);
		if (refreshItem != null) {
			final View view = MenuItemCompat.getActionView(refreshItem);
			updateUploadCount(view, null);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(!mIsUploading){
						setRefreshActionButtonState(true, view);
					}
				}
			});
		}

		return super.onPrepareOptionsMenu(menu);
	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		switch (position) {
		case 2:
			openActivityWithFragment(MessageListFragment.class,
					R.string.friendListFragment_title_manage_message, null);
			break;
		// case 2 :
		// openActivityWithFragment(MoneyAccountListFragment.class,
		// R.string.moneyAccountListFragment_title_manage_moneyAccount, null);
		// break;
		case 3:
			openActivityWithFragment(ExchangeListFragment.class,
					R.string.exchangeListFragment_title_manage_exchange, null);
			break;
		case 4:
			openActivityWithFragment(CurrencyListFragment.class,
					R.string.currencyListFragment_title_manage_currency, null);
			break;
		case 6:
			HyjApplication.getInstance().switchUser();
			break;
		}

		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.mOptionsMenu = menu;
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

		// if(item.getItemId() == R.id.homeListFragment_action_sync){
		// setRefreshActionButtonState(true);
		// }

		// Handle your other action bar items...
		if (item.getItemId() == R.id.mainActivity_action_money_addnew_expense) {
			openActivityWithFragment(MoneyExpenseFormFragment.class,
					R.string.moneyExpenseFormFragment_title_addnew, null);
			return true;
		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_income) {
			openActivityWithFragment(MoneyIncomeFormFragment.class,
					R.string.moneyIncomeFormFragment_title_addnew, null);
			return true;
		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_transfer) {
			openActivityWithFragment(MoneyTransferFormFragment.class,
					R.string.moneyTransferFormFragment_title_addnew, null);
			return true;
		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_borrow) {
			openActivityWithFragment(MoneyBorrowFormFragment.class,
					R.string.moneyBorrowFormFragment_title_addnew, null);
			return true;
		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_lend) {
			openActivityWithFragment(MoneyLendFormFragment.class,
					R.string.moneyLendFormFragment_title_addnew, null);
			return true;
		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_return) {
			openActivityWithFragment(MoneyReturnFormFragment.class,
					R.string.moneyReturnFormFragment_title_addnew, null);
			return true;
		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_payback) {
			openActivityWithFragment(MoneyPaybackFormFragment.class,
					R.string.moneyPaybackFormFragment_title_addnew, null);
			return true;
		}

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
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 4;
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
			}
			return null;
		}
	}

	public View setRefreshActionButtonState(final boolean refreshing, View view) {
		if (view == null) {
			if (mOptionsMenu != null) {
				final MenuItem refreshItem = mOptionsMenu
						.findItem(R.id.homeListFragment_action_sync);
				if (refreshItem != null) {
					view = MenuItemCompat.getActionView(refreshItem);
				}
			}
		}
		if (view != null) {
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
			if (mOptionsMenu != null) {
				final MenuItem refreshItem = mOptionsMenu
						.findItem(R.id.homeListFragment_action_sync);
				if (refreshItem != null) {
					view = MenuItemCompat.getActionView(refreshItem);
				}
			}
		}
		if (view != null) {
			final TextView tv = (TextView) view
					.findViewById(R.id.actionbar_sync_uploadCount);
			if (count == null) {
				HyjAsyncTask.newInstance(new HyjAsyncTaskCallbacks() {
					@Override
					public void finishCallback(Object object) {
						Integer count = (Integer)object;
						if (count > 0) {
							tv.setText(count.toString());
						} else {
							tv.setText("");
						}
					}
					@Override
					public Object doInBackground(String... string) {
						Integer count = 0;
						if(HyjApplication.getInstance().getCurrentUser() != null){
							Cursor cursor = Cache.openDatabase().rawQuery(
									"SELECT COUNT(*) FROM ClientSyncRecord", null);
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
			} else if (count > 0) {
				tv.setText(count.toString());
			} else {
				tv.setText("");
			}
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

		@Override
		public void onChange(boolean selfChange) {
			if(mIsUploading){
				return;
			}
			setRefreshActionButtonState(true, updateUploadCount(null, null));
			HyjAsyncTask.newInstance(new HyjAsyncTaskCallbacks() {
				@Override
				public void finishCallback(Object object) {
					Boolean result = (Boolean)object;
					mIsUploading = false;
					if(result){
						setRefreshActionButtonState(false, null);
						HyjUtil.displayToast("上传资料成功");
					}
				}
				@Override
				public void errorCallback(Object object) {
					Boolean result = (Boolean)object;
					mIsUploading = false;
					if(!result){
						setRefreshActionButtonState(false, null);
						HyjUtil.displayToast("上传资料失败，请重试");
					}
				}
				@Override
				public Object doInBackground(String... string) {

					
					return false;
				}
			});
		}
	}
}
