package com.hoyoji.hoyoji;

import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
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
import com.hoyoji.hoyoji.models.Currency;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.FriendCategory;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.MessageBox;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyApportion;
import com.hoyoji.hoyoji.models.MoneyBorrow;
import com.hoyoji.hoyoji.models.MoneyBorrowApportion;
import com.hoyoji.hoyoji.models.MoneyBorrowContainer;
import com.hoyoji.hoyoji.models.MoneyDepositIncomeApportion;
import com.hoyoji.hoyoji.models.MoneyDepositIncomeContainer;
import com.hoyoji.hoyoji.models.MoneyDepositReturnApportion;
import com.hoyoji.hoyoji.models.MoneyDepositReturnContainer;
import com.hoyoji.hoyoji.models.MoneyExpense;
import com.hoyoji.hoyoji.models.MoneyExpenseApportion;
import com.hoyoji.hoyoji.models.MoneyExpenseCategory;
import com.hoyoji.hoyoji.models.MoneyExpenseContainer;
import com.hoyoji.hoyoji.models.MoneyIncome;
import com.hoyoji.hoyoji.models.MoneyIncomeApportion;
import com.hoyoji.hoyoji.models.MoneyIncomeCategory;
import com.hoyoji.hoyoji.models.MoneyIncomeContainer;
import com.hoyoji.hoyoji.models.MoneyLend;
import com.hoyoji.hoyoji.models.MoneyLendApportion;
import com.hoyoji.hoyoji.models.MoneyLendContainer;
import com.hoyoji.hoyoji.models.MoneyPayback;
import com.hoyoji.hoyoji.models.MoneyPaybackApportion;
import com.hoyoji.hoyoji.models.MoneyPaybackContainer;
import com.hoyoji.hoyoji.models.MoneyReturn;
import com.hoyoji.hoyoji.models.MoneyReturnApportion;
import com.hoyoji.hoyoji.models.MoneyReturnContainer;
import com.hoyoji.hoyoji.models.MoneyTransfer;
import com.hoyoji.hoyoji.models.ParentProject;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectRemark;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.QQLogin;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.MoneySearchListFragment;
import com.hoyoji.hoyoji.money.currency.CurrencyListFragment;
import com.hoyoji.hoyoji.money.currency.ExchangeListFragment;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.money.moneycategory.MoneyExpenseCategoryListFragment;
import com.hoyoji.hoyoji.money.moneycategory.MoneyIncomeCategoryListFragment;
import com.hoyoji.hoyoji.money.report.MoneyReportFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.setting.SystemSettingFormFragment;
import com.hoyoji.hoyoji_android.R;

public class MainActivity extends HyjUserActivity {
	private Menu mOptionsMenu;
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
				this.getContentResolver()
						.registerContentObserver(
								ContentProvider.createUri(
										ClientSyncRecord.class, null), true,
								mChangeObserver);
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

//		MenuItem refreshItem = menu.findItem(R.id.mainActivity_action_sync);
//		if (refreshItem != null) {
//			final View view = MenuItemCompat.getActionView(refreshItem);
//			if(HyjApplication.getInstance().getIsSyncing()){
////				setRefreshActionButtonState(true, updateUploadCount(view, null));
//			} else {
//				updateUploadCount(view, null);
//			}
//			view.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					if(!HyjUtil.hasNetworkConnection()){
//						HyjUtil.displayToast(R.string.server_connection_disconnected);
//						return;
//					}
//					if (!HyjApplication.getInstance().getIsSyncing()) {
//						uploadData(true);
//					}
//				}
//			});
//		}

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
		 case 0 :
				 openActivityWithFragment(MoneySearchListFragment.class,
				 R.string.moneySearchListFragment_title, null);
				 break;
		 case 1 :
				 openActivityWithFragment(MoneyReportFragment.class,
				 R.string.moneyReportFragment_title, null);
				 break;
		case 2:
			openActivityWithFragment(MessageListFragment.class,
					R.string.friendListFragment_title_manage_message, null);
			break;
		
		case 3:
			openActivityWithFragment(ExchangeListFragment.class,
					R.string.exchangeListFragment_title_manage_exchange, null);
			break;
		case 4:
			openActivityWithFragment(CurrencyListFragment.class,
					R.string.currencyListFragment_title_manage_currency, null);
			break;
		case 5:
			openActivityWithFragment(MoneyExpenseCategoryListFragment.class,
					R.string.moneyCategoryFormDialogFragment_title_manage, null);
			break;
		case 6:
			openActivityWithFragment(MoneyIncomeCategoryListFragment.class,
					R.string.moneyCategoryFormDialogFragment_title_manage, null);
			break;
		case 7:
			openActivityWithFragment(SystemSettingFormFragment.class,
					R.string.systemSettingFormFragment_title, null);
			break;
		case 8:
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
		
		MenuItem refreshItem = menu.findItem(R.id.mainActivity_action_sync);
		if (refreshItem != null) {
			final View view = MenuItemCompat.getActionView(refreshItem);
			updateUploadCount(view, null);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(!HyjUtil.hasNetworkConnection()){
						HyjUtil.displayToast(R.string.server_connection_disconnected);
						return;
					}
					if (!HyjApplication.getInstance().getIsSyncing()) {
						uploadData(true);
					}
				}
			});
		}

		
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

//		 if(item.getItemId() == R.id.mainActivity_action_sync){
//			 	if(!HyjUtil.hasNetworkConnection()){
//					HyjUtil.displayToast(R.string.server_connection_disconnected);
//					return true;
//				}
//				if (!HyjApplication.getInstance().getIsSyncing()) {
//					uploadData(true);
//					return true;
//				}
//		 }

		

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
						.findItem(R.id.mainActivity_action_sync);
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
						.findItem(R.id.mainActivity_action_sync);
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
		public void onChange(boolean selfChange, Uri uri) {
			// TODO Auto-generated method stub
			super.onChange(selfChange, uri);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			uploadData(false);
		}
	}


	@Override
	protected void onDestroy() {
		if (mChangeObserver != null) {
			this.getContentResolver()
					.unregisterContentObserver(mChangeObserver);
		}
		super.onDestroy();
	}
	
	public void downloadData() {
		HyjAsyncTask.newInstance(new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
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

			@Override
			public void errorCallback(Object object) {
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

			@Override
			public Object doInBackground(String... string) {
				String postData =  HyjApplication.getInstance().getCurrentUser().getUserData().getLastSyncTime();
				if(postData == null){
					postData = "null";
				}
				Object result = HyjServer.doHttpPost(null,
						HyjApplication.getServerUrl() + "syncPull.php", postData, true);
				if (result == null) {
					return HyjApplication.getInstance().getResources()
							.getString(R.string.server_dataparse_error);
				}
				if (result instanceof JSONObject) {
					JSONObject jsonResult = (JSONObject) result;
					if (jsonResult.isNull("__summary")) {
						try {

							saveData(jsonResult.getJSONArray("data"), 
									jsonResult.optString("lastSyncTime"));

							return true;
						} catch (JSONException e) {
							return "下载数据失败，请重试";
						}
					} else {
						return jsonResult.optJSONObject("__summary").optString(
								"msg");
					}
					// } else if(result instanceof JSONArray) {
					// saveData((JSONArray)result);
				} else {
					return "下载数据失败，请重试";
				}
			}
		});
	}

	private void saveDataRecursive(JSONArray result) throws JSONException {
		for (int i = 0; i < result.length(); i++) {
			Object o = result.get(i);
			if (o instanceof JSONArray) {
				saveDataRecursive((JSONArray) o);
			} else {
				JSONObject jsonObj = (JSONObject) o;
				String dataType = jsonObj.getString("__dataType");
				HyjModel model = null;
				if (dataType.equals("ServerSyncDeletedRecords")) {
					model = getModel(jsonObj.getString("tableName"), jsonObj.getString("recordId"));
					if(model != null && model.get_mId() != null){
						model.deleteFromServer();
					}
				} else {
					model = getModel(dataType, jsonObj.getString("id"));
					if(model != null){
						model.loadFromJSON(jsonObj, true);
						model.save();
					}
				}
			}
		}
	}

	private void saveData(JSONArray result, String lastSyncTime) {
		try {
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
		} catch (JSONException e) {
			ActiveAndroid.endTransaction();
			((HyjActivity) MainActivity.this).dismissProgressDialog();
			HyjUtil.displayToast("下载数据失败，请重试");
		}
	}

	public void uploadData(final boolean downloadData) {
		if(!HyjUtil.hasNetworkConnection()){
			updateUploadCount(null, null);
			return;
		} else {
			setRefreshActionButtonState(true, updateUploadCount(null, null));
		}
		if (HyjApplication.getInstance().getIsSyncing()) {
			return;
		}
		HyjApplication.getInstance().setIsSyncing(true);
		if (downloadData) {
			((HyjActivity) MainActivity.this).displayProgressDialog("同步数据",
					"正在同步数据，请稍后...");
		}
		HyjAsyncTask.newInstance(new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
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
					}
				}
			}

			@Override
			public void errorCallback(Object object) {
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

			@Override
			public Object doInBackground(String... string) {
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
					return "上传数据失败";
				}

				if (syncRecords.size() == 0) {
					return true;
				}

				try {

					JSONArray postData = new JSONArray();
					for (ClientSyncRecord syncRec : syncRecords) {
						JSONObject jsonObj = new JSONObject();
						if (syncRec.getOperation().equalsIgnoreCase("Create")) {
							HyjModel model = getModel(syncRec.getTableName(),
									syncRec.getId());
							if(model.get_mId() != null){
								jsonObj.put("operation", "create");
								JSONObject recordData = model.toJSON();
								if(model instanceof MoneyApportion){
									recordData.put("projectId", ((MoneyApportion)model).getProject().getId());
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
								jsonObj.put( "recordData", recordData);
								postData.put(jsonObj);
							}
						} else if (syncRec.getOperation().equalsIgnoreCase(
								"Update")) {
							HyjModel model = getModel(syncRec.getTableName(),
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
								jsonObj.put(
										"recordData", recordData);
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
					Log.i("Push Data : ", postData.toString());
					Object result = HyjServer.doHttpPost(null,
							HyjApplication.getServerUrl() + "syncPush2.php",
							postData.toString(), true);
					if (result == null) {
						rollbackUpload(syncRecords);
						return HyjApplication.getInstance().getString(
								R.string.server_dataparse_error);
					}
					if (result instanceof JSONObject) {
						JSONObject jsonResult = (JSONObject) result;
						if (jsonResult.isNull("__summary")) {
							String lastUploadTime = jsonResult.optString("lastUploadTime");
							if(lastUploadTime != null){
								try {
									ActiveAndroid.beginTransaction();
									for (ClientSyncRecord syncRec : syncRecords) {
										if (!syncRec.getOperation().equalsIgnoreCase("Delete")) {
											HyjModel model = getModel(syncRec.getTableName(),
													syncRec.getId());
											model.setSyncFromServer(true);
											model.setLastServerUpdateTime(lastUploadTime);
											model.save();
										} 
									}
									ActiveAndroid.setTransactionSuccessful();
									ActiveAndroid.endTransaction();
									return true;
								} catch (Exception e) {
									ActiveAndroid.endTransaction();
									rollbackUpload(syncRecords);
									return "上传数据失败";
								}
							} else {
								rollbackUpload(syncRecords);
								return "上传数据失败";
							}
						} else {
							rollbackUpload(syncRecords);
							return jsonResult.optJSONObject("__summary")
									.optString("msg");
						}
					} else {
						rollbackUpload(syncRecords);
						return "上传数据失败";
					}
				} catch (JSONException e) {
					rollbackUpload(syncRecords);
					return "上传数据失败";
				}
			}

			private void rollbackUpload(List<ClientSyncRecord> syncRecords) {
				if (syncRecords == null) {
					return;
				}
				for (ClientSyncRecord syncRec : syncRecords) {
					if (syncRec.getOperation().equalsIgnoreCase("Create")) {
						ClientSyncRecord rec = HyjModel.getModel(
								ClientSyncRecord.class, syncRec.getId());
						if (rec.getUploading() == true) {
							rec.setUploading(false);
							if (rec.getOperation().equalsIgnoreCase("Delete")) {
								rec.delete();
							} else if (rec.getOperation().equalsIgnoreCase(
									"Update")) {
								rec.setOperation("Create");
								rec.save();
							} else {
								rec.save();
							}
						}
					}
				}
				// Cache.openDatabase().execSQL(
				// "Update ClientSyncRecord SET uploading = 0 WHERE uploading = 1");
			}

		});
	}

	private HyjModel getModel(String tableName, String id) {
		HyjModel model = null;
		if (tableName.equalsIgnoreCase("Currency")) {
			model = HyjModel.getModel(Currency.class, id);
			if(model == null){
				model = new Currency();
			}
		} else if (tableName.equalsIgnoreCase("Exchange")) {
			model = HyjModel.getModel(Exchange.class, id);
			if(model == null){
				model = new Exchange();
			}
		} else if (tableName.equalsIgnoreCase("Friend")) {
			model = HyjModel.getModel(Friend.class, id);
			if(model == null){
				model = new Friend();
			}
		} else if (tableName.equalsIgnoreCase("FriendCategory")) {
			model = HyjModel.getModel(FriendCategory.class, id);
			if(model == null){
				model = new FriendCategory();
			}
		} else if (tableName.equalsIgnoreCase("Message")) {
			model = HyjModel.getModel(Message.class, id);
			if(model == null){
				model = new Message();
			}
		}  else if (tableName.equalsIgnoreCase("QQLogin")) {
			model = HyjModel.getModel(QQLogin.class, id);
			if(model == null){
				model = new QQLogin();
			}
		} else if (tableName.equalsIgnoreCase("MessageBox")) {
			model = HyjModel.getModel(MessageBox.class, id);
			if(model == null){
				model = new MessageBox();
			}
		} else if (tableName.equalsIgnoreCase("MoneyAccount")) {
			model = HyjModel.getModel(MoneyAccount.class, id);
			if(model == null){
				model = new MoneyAccount();
			}
		} else if (tableName.equalsIgnoreCase("MoneyBorrow")) {
			model = HyjModel.getModel(MoneyBorrow.class, id);
			if(model == null){
				model = new MoneyBorrow();
			}
		} else if (tableName.equalsIgnoreCase("MoneyBorrowApportion")) {
			model = HyjModel.getModel(MoneyBorrowApportion.class, id);
			if(model == null){
				model = new MoneyBorrowApportion();
			}
		} else if (tableName.equalsIgnoreCase("MoneyExpense")) {
			model = HyjModel.getModel(MoneyExpense.class, id);
			if(model == null){
				model = new MoneyExpense();
			}
		} else if (tableName.equalsIgnoreCase("MoneyExpenseContainer")) {
			model = HyjModel.getModel(MoneyExpenseContainer.class, id);
			if(model == null){
				model = new MoneyExpenseContainer();
			}
		} else if (tableName.equalsIgnoreCase("MoneyExpenseApportion")) {
			model = HyjModel.getModel(MoneyExpenseApportion.class, id);
			if(model == null){
				model = new MoneyExpenseApportion();
			}
		} else if (tableName.equalsIgnoreCase("MoneyIncome")) {
			model = HyjModel.getModel(MoneyIncome.class, id);
			if(model == null){
				model = new MoneyIncome();
			}
		}  else if (tableName.equalsIgnoreCase("MoneyIncomeContainer")) {
			model = HyjModel.getModel(MoneyIncomeContainer.class, id);
			if(model == null){
				model = new MoneyIncomeContainer();
			}
		}  else if (tableName.equalsIgnoreCase("MoneyDepositIncomeContainer")) {
			model = HyjModel.getModel(MoneyDepositIncomeContainer.class, id);
			if(model == null){
				model = new MoneyDepositIncomeContainer();
			}
		}   else if (tableName.equalsIgnoreCase("MoneyDepositReturnContainer")) {
			model = HyjModel.getModel(MoneyDepositReturnContainer.class, id);
			if(model == null){
				model = new MoneyDepositReturnContainer();
			}
		} else if (tableName.equalsIgnoreCase("MoneyIncomeApportion")) {
			model = HyjModel.getModel(MoneyIncomeApportion.class, id);
			if(model == null){
				model = new MoneyIncomeApportion();
			}
		} else if (tableName.equalsIgnoreCase("MoneyDepositIncomeApportion")) {
			model = HyjModel.getModel(MoneyDepositIncomeApportion.class, id);
			if(model == null){
				model = new MoneyDepositIncomeApportion();
			}
		} else if (tableName.equalsIgnoreCase("MoneyDepositReturnApportion")) {
			model = HyjModel.getModel(MoneyDepositReturnApportion.class, id);
			if(model == null){
				model = new MoneyDepositReturnApportion();
			}
		} else if (tableName.equalsIgnoreCase("MoneyLend")) {
			model = HyjModel.getModel(MoneyLend.class, id);
			if(model == null){
				model = new MoneyLend();
			}
		}  else if (tableName.equalsIgnoreCase("MoneyLendContainer")) {
			model = HyjModel.getModel(MoneyLendContainer.class, id);
			if(model == null){
				model = new MoneyLendContainer();
			}
		} else if (tableName.equalsIgnoreCase("MoneyLendApportion")) {
			model = HyjModel.getModel(MoneyLendApportion.class, id);
			if(model == null){
				model = new MoneyLendApportion();
			}
		} else if (tableName.equalsIgnoreCase("MoneyPayback")) {
			model = HyjModel.getModel(MoneyPayback.class, id);
			if(model == null){
				model = new MoneyPayback();
			}
		}  else if (tableName.equalsIgnoreCase("MoneyPaybackContainer")) {
			model = HyjModel.getModel(MoneyPaybackContainer.class, id);
			if(model == null){
				model = new MoneyPaybackContainer();
			}
		} else if (tableName.equalsIgnoreCase("MoneyPaybackApportion")) {
			model = HyjModel.getModel(MoneyPaybackApportion.class, id);
			if(model == null){
				model = new MoneyPaybackApportion();
			}
		} else if (tableName.equalsIgnoreCase("MoneyReturn")) {
			model = HyjModel.getModel(MoneyReturn.class, id);
			if(model == null){
				model = new MoneyReturn();
			}
		}  else if (tableName.equalsIgnoreCase("MoneyReturnContainer")) {
			model = HyjModel.getModel(MoneyReturnContainer.class, id);
			if(model == null){
				model = new MoneyReturnContainer();
			}
		} else if (tableName.equalsIgnoreCase("MoneyReturnApportion")) {
			model = HyjModel.getModel(MoneyReturnApportion.class, id);
			if(model == null){
				model = new MoneyReturnApportion();
			}
		} else if (tableName.equalsIgnoreCase("MoneyTransfer")) {
			model = HyjModel.getModel(MoneyTransfer.class, id);
			if(model == null){
				model = new MoneyTransfer();
			}
		} else if (tableName.equalsIgnoreCase("ParentProject")) {
			model = HyjModel.getModel(ParentProject.class, id);
			if(model == null){
				model = new ParentProject();
			}
		} else if (tableName.equalsIgnoreCase("Picture")) {
			model = HyjModel.getModel(Picture.class, id);
			if(model == null){
				model = new Picture();
			}
		} else if (tableName.equalsIgnoreCase("Project")) {
			model = HyjModel.getModel(Project.class, id);
			if(model == null){
				model = new Project();
			}
		} else if (tableName.equalsIgnoreCase("ProjectShareAuthorization")) {
			model = HyjModel.getModel(ProjectShareAuthorization.class, id);
			if(model == null){
				model = new ProjectShareAuthorization();
			}
		} else if (tableName.equalsIgnoreCase("User")) {
			model = HyjModel.getModel(User.class, id);
			if(model == null){
				model = new User();
			}
		} else if (tableName.equalsIgnoreCase("UserData")) {
			model = HyjModel.getModel(UserData.class, id);
			if(model == null){
				model = new UserData();
			}
		} else if (tableName.equalsIgnoreCase("ProjectRemark")) {
			model = HyjModel.getModel(ProjectRemark.class, id);
			if(model == null){
				model = new ProjectRemark();
			}
		} else if (tableName.equalsIgnoreCase("MoneyIncomeCategory")) {
			model = HyjModel.getModel(MoneyIncomeCategory.class, id);
			if(model == null){
				model = new MoneyIncomeCategory();
			}
		} else if (tableName.equalsIgnoreCase("MoneyExpenseCategory")) {
			model = HyjModel.getModel(MoneyExpenseCategory.class, id);
			if(model == null){
				model = new MoneyExpenseCategory();
			}
		}
		return model;
	}
}
