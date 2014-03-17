package com.hoyoji.hoyoji.money;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.activeandroid.Cache;
import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.MoneyBorrow;
import com.hoyoji.hoyoji.models.MoneyExpense;
import com.hoyoji.hoyoji.models.MoneyIncome;
import com.hoyoji.hoyoji.models.MoneyLend;
import com.hoyoji.hoyoji.models.MoneyPayback;
import com.hoyoji.hoyoji.models.MoneyReturn;
import com.hoyoji.hoyoji.models.MoneyTransfer;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

public class MoneySearchGroupListLoader extends
		AsyncTaskLoader<List<Map<String, Object>>> {

	private DateFormat mDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private List<Map<String, Object>> mGroupList;
	private Integer mLoadLimit = 10;
	private boolean mHasMoreData = true;
	private ChangeObserver mChangeObserver;
	private String mProjectId;
	private String mMoneyAccountId;
	private String mFriendUserId;
	private String mLocalFriendId;
	private long mDateFrom = 0;
	private long mDateTo = 0;

	public MoneySearchGroupListLoader(Context context, Bundle queryParams) {
		super(context);
		mDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		copyQueryParams(queryParams);

		mChangeObserver = new ChangeObserver();
		context.getContentResolver().registerContentObserver(
				ContentProvider.createUri(MoneyExpense.class, null), true,
				mChangeObserver);
		context.getContentResolver().registerContentObserver(
				ContentProvider.createUri(MoneyIncome.class, null), true,
				mChangeObserver);
		context.getContentResolver().registerContentObserver(
				ContentProvider.createUri(MoneyTransfer.class, null), true,
				mChangeObserver);
		context.getContentResolver().registerContentObserver(
				ContentProvider.createUri(MoneyBorrow.class, null), true,
				mChangeObserver);
		context.getContentResolver().registerContentObserver(
				ContentProvider.createUri(MoneyLend.class, null), true,
				mChangeObserver);
		context.getContentResolver().registerContentObserver(
				ContentProvider.createUri(MoneyReturn.class, null), true,
				mChangeObserver);
		context.getContentResolver().registerContentObserver(
				ContentProvider.createUri(MoneyPayback.class, null), true,
				mChangeObserver);
	}

	private void copyQueryParams(Bundle queryParams) {
		if (queryParams != null) {
			mDateFrom = queryParams.getLong("dateFrom", 0);
			mDateTo = queryParams.getLong("dateTo", 0);
			mLoadLimit = queryParams.getInt("limit", 10);
			mProjectId = queryParams.getString("projectId");
			mMoneyAccountId = queryParams.getString("moneyAccountId");
			mFriendUserId = queryParams.getString("friendUserId");
			mLocalFriendId = queryParams.getString("localFriendId");
			mLoadLimit += queryParams.getInt("pageSize", 10);
		} else {
			mLoadLimit += 10;
		}
		
	}

	public void requery(Bundle queryParams){
		copyQueryParams(queryParams);
		this.onContentChanged();
	}
	
	public void fetchMore(Bundle queryParams) {
		copyQueryParams(queryParams);
		this.onContentChanged();
	}

	private String buildSearchQuery(String type){
		StringBuilder queryStringBuilder = new StringBuilder(" 1 = 1 ");
		if(mProjectId != null){
			queryStringBuilder.append(" AND projectId = '" + mProjectId + "' ");
		}
		if(mMoneyAccountId != null){
			queryStringBuilder.append(" AND moneyAccountId = '" + mMoneyAccountId + "' ");
		}
		if(mFriendUserId != null){
			queryStringBuilder.append(" AND (main.ownerUserId = '" + mFriendUserId + "' OR friendUserId = '" + mFriendUserId + "' OR EXISTS(SELECT apr.id FROM Money"+type+"Apportion apr WHERE apr.money"+type+"Id = main.id AND apr.friendUserId = '" + mFriendUserId + "'))");
		}
		if(mLocalFriendId != null){
			queryStringBuilder.append(" AND (localFriendId = '" + mLocalFriendId + "' OR EXISTS(SELECT apr.id FROM Money"+type+"Apportion apr WHERE apr.money"+type+"Id = main.id AND apr.localFriendId = '" + mLocalFriendId + "'))");
		}
		return queryStringBuilder.toString();
	}
	
	private String buildTransferSearchQuery(){
		StringBuilder queryStringBuilder = new StringBuilder(" 1 = 1 ");
		if(mProjectId != null){
			queryStringBuilder.append(" AND projectId = '" + mProjectId + "' ");
		}
		if(mMoneyAccountId != null){
			queryStringBuilder.append(" AND (transferInId = '" + mMoneyAccountId + "' OR transferOutId = '" + mMoneyAccountId + "') ");
		}
		if(mFriendUserId != null){
			queryStringBuilder.append(" AND (main.ownerUserId = '" + mFriendUserId + "' OR transferInFriendUserId = '" + mFriendUserId + "' OR transferOutFriendUserId = '" + mFriendUserId + "') ");
		}
		if(mLocalFriendId != null){
			queryStringBuilder.append(" AND (transferInLocalFriendId = '" + mLocalFriendId + "' OR transferOutLocalFriendId = '" + mLocalFriendId + "') ");
		}
		return queryStringBuilder.toString();
	}
	
	/**
	 * This is where the bulk of our work is done. This function is called in a
	 * background thread and should generate a new set of data to be published
	 * by the loader.
	 */
	@Override
	public List<Map<String, Object>> loadInBackground() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		String currentUserId = HyjApplication.getInstance().getCurrentUser().getUserData().getId();
		String localCurrencyId = HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId();

		DateFormat df = SimpleDateFormat.getDateInstance();
		Calendar calToday = Calendar.getInstance();
		if(mDateTo != 0){
			calToday.setTimeInMillis(mDateTo);
		}
		calToday.set(Calendar.HOUR_OF_DAY, 0);
		calToday.clear(Calendar.MINUTE);
		calToday.clear(Calendar.SECOND);
		calToday.clear(Calendar.MILLISECOND);

		Calendar dateFrom = Calendar.getInstance();
		dateFrom.setTimeInMillis(mDateFrom);
		dateFrom.set(Calendar.HOUR_OF_DAY, 0);
		dateFrom.clear(Calendar.MINUTE);
		dateFrom.clear(Calendar.SECOND);
		dateFrom.clear(Calendar.MILLISECOND);
		long dateFromInMillis = dateFrom.getTimeInMillis();
		
// get start of this week in milliseconds
//		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
		// cal.add(Calendar.WEEK_OF_YEAR, -1);
		
		int loadCount = 0;
		while (loadCount < mLoadLimit && calToday.getTimeInMillis() >= dateFromInMillis) {
			int count = 0;
			String[] args = new String[] {
					mDateFormat.format(calToday.getTime()),
					mDateFormat.format(new Date(calToday.getTimeInMillis() + 24 * 3600000)) };
			double expenseTotal = 0;
			double incomeTotal = 0;
			Cursor cursor = Cache
					.openDatabase()
					.rawQuery(
							"SELECT COUNT(*) AS count, SUM(CASE WHEN main.ownerUserId = '" + currentUserId + "' THEN main.amount / IFNULL(exma.rate, 1) ELSE main.amount * main.exchangeRate / IFNULL(ex.rate, 1) END) AS total FROM MoneyExpense main JOIN Project prj1 ON prj1.id = main.projectId LEFT JOIN MoneyAccount ma ON ma.id = main.moneyAccountId LEFT JOIN Exchange ex ON ex.foreignCurrencyId = prj1.currencyId AND ex.localCurrencyId = '" + localCurrencyId + "' LEFT JOIN Exchange exma ON exma.foreignCurrencyId = ma.currencyId AND exma.localCurrencyId = '" + localCurrencyId + "' " +
							"WHERE date > ? AND date <= ? AND " + buildSearchQuery("Expense"),
							args);
			if (cursor != null) {
				cursor.moveToFirst();
				count += cursor.getInt(0);
				expenseTotal += cursor.getDouble(1);
				cursor.close();
				cursor = null;
			}
			cursor = Cache
					.openDatabase()
					.rawQuery(
							"SELECT COUNT(*) AS count, SUM(CASE WHEN main.ownerUserId = '" + currentUserId + "' THEN main.amount / IFNULL(exma.rate, 1) ELSE main.amount * main.exchangeRate / IFNULL(ex.rate, 1) END) AS total FROM MoneyIncome main JOIN Project prj1 ON prj1.id = main.projectId LEFT JOIN MoneyAccount ma ON ma.id = main.moneyAccountId LEFT JOIN Exchange ex ON ex.foreignCurrencyId = prj1.currencyId AND ex.localCurrencyId = '" + localCurrencyId + "' LEFT JOIN Exchange exma ON exma.foreignCurrencyId = ma.currencyId AND exma.localCurrencyId = '" + localCurrencyId + "' " +
							"WHERE date > ? AND date <= ? AND " + buildSearchQuery("Income"),
							args);
			if (cursor != null) {
				cursor.moveToFirst();
				count += cursor.getInt(0);
				incomeTotal += cursor.getDouble(1);
				cursor.close();
				cursor = null;
			}
			cursor = Cache
					.openDatabase()
					.rawQuery(
							"SELECT COUNT(*) AS count, SUM(transferOutAmount) as total FROM MoneyTransfer main WHERE date > ? AND date <= ? AND " + buildTransferSearchQuery(),
							args);
			if (cursor != null) {
				cursor.moveToFirst();
				count += cursor.getInt(0);
//				transferTotal += cursor.getDouble(1);
				cursor.close();
				cursor = null;
			}
			cursor = Cache
					.openDatabase()
					.rawQuery(
							"SELECT COUNT(*) AS count, SUM(CASE WHEN main.ownerUserId = '" + currentUserId + "' THEN main.amount / IFNULL(exma.rate, 1) ELSE main.amount * main.exchangeRate / IFNULL(ex.rate, 1) END) AS total FROM MoneyBorrow main JOIN Project prj1 ON prj1.id = main.projectId LEFT JOIN MoneyAccount ma ON ma.id = main.moneyAccountId LEFT JOIN Exchange ex ON ex.foreignCurrencyId = prj1.currencyId AND ex.localCurrencyId = '" + localCurrencyId + "' LEFT JOIN Exchange exma ON exma.foreignCurrencyId = ma.currencyId AND exma.localCurrencyId = '" + localCurrencyId + "' " +
							"WHERE date > ? AND date <= ? AND " + buildSearchQuery("Borrow"),
							args);
			if (cursor != null) {
				cursor.moveToFirst();
				count += cursor.getInt(0);
//				incomeTotal += cursor.getDouble(1);
				cursor.close();
				cursor = null;
			}
			cursor = Cache
					.openDatabase()
					.rawQuery(
							"SELECT COUNT(*) AS count, SUM(CASE WHEN main.ownerUserId = '" + currentUserId + "' THEN main.amount / IFNULL(exma.rate, 1) ELSE main.amount * main.exchangeRate / IFNULL(ex.rate, 1) END) AS total FROM MoneyLend main JOIN Project prj1 ON prj1.id = main.projectId LEFT JOIN MoneyAccount ma ON ma.id = main.moneyAccountId LEFT JOIN Exchange ex ON ex.foreignCurrencyId = prj1.currencyId AND ex.localCurrencyId = '" + localCurrencyId + "' LEFT JOIN Exchange exma ON exma.foreignCurrencyId = ma.currencyId AND exma.localCurrencyId = '" + localCurrencyId + "' " +
							"WHERE date > ? AND date <= ? AND " + buildSearchQuery("Lend"),
							args);
			if (cursor != null) {
				cursor.moveToFirst();
				count += cursor.getInt(0);
//				incomeTotal += cursor.getDouble(1);
				cursor.close();
				cursor = null;
			}
			cursor = Cache
					.openDatabase()
					.rawQuery(
							"SELECT COUNT(*) AS count, SUM(CASE WHEN main.ownerUserId = '" + currentUserId + "' THEN main.amount / IFNULL(exma.rate, 1) ELSE main.amount * main.exchangeRate / IFNULL(ex.rate, 1) END) AS total FROM MoneyReturn main JOIN Project prj1 ON prj1.id = main.projectId LEFT JOIN MoneyAccount ma ON ma.id = main.moneyAccountId LEFT JOIN Exchange ex ON ex.foreignCurrencyId = prj1.currencyId AND ex.localCurrencyId = '" + localCurrencyId + "' LEFT JOIN Exchange exma ON exma.foreignCurrencyId = ma.currencyId AND exma.localCurrencyId = '" + localCurrencyId + "' " +
							"WHERE date > ? AND date <= ? AND " + buildSearchQuery("Return"),
							args);
			if (cursor != null) {
				cursor.moveToFirst();
				count += cursor.getInt(0);
//				incomeTotal += cursor.getDouble(1);
				cursor.close();
				cursor = null;
			}
			cursor = Cache
					.openDatabase()
					.rawQuery(
							"SELECT COUNT(*) AS count, SUM(CASE WHEN main.ownerUserId = '" + currentUserId + "' THEN main.amount / IFNULL(exma.rate, 1) ELSE main.amount * main.exchangeRate / IFNULL(ex.rate, 1) END) AS total FROM MoneyPayback main JOIN Project prj1 ON prj1.id = main.projectId LEFT JOIN MoneyAccount ma ON ma.id = main.moneyAccountId LEFT JOIN Exchange ex ON ex.foreignCurrencyId = prj1.currencyId AND ex.localCurrencyId = '" + localCurrencyId + "' LEFT JOIN Exchange exma ON exma.foreignCurrencyId = ma.currencyId AND exma.localCurrencyId = '" + localCurrencyId + "' " +
							"WHERE date > ? AND date <= ? AND " + buildSearchQuery("Payback"),
							args);
			if (cursor != null) {
				cursor.moveToFirst();
				count += cursor.getInt(0);
//				incomeTotal += cursor.getDouble(1);
				cursor.close();
				cursor = null;
			}
			if (count > 0) {
				String ds = df.format(calToday.getTime());
//				ds = ds.replaceAll("Z$", "+0000");
				HashMap<String, Object> groupObject = new HashMap<String, Object>();
				groupObject.put("date", ds);
				groupObject.put("dateInMilliSeconds", calToday.getTimeInMillis());
				groupObject.put("expenseTotal",
						HyjUtil.toFixed2(incomeTotal));
				groupObject.put("incomeTotal",
						HyjUtil.toFixed2(expenseTotal));
				list.add(groupObject);
				loadCount += count + 1;
			}

			// 我们要检查还有没有数据可以加载的，如果没有了，我们就break出。否则会进入无限循环。
			if(count == 0){
				long moreDataInMillis = getHasMoreDataDateInMillis(calToday.getTimeInMillis());
				if(moreDataInMillis == -1){
					break;
				} else {
					calToday.setTimeInMillis(moreDataInMillis);
				}
			} else {
				calToday.add(Calendar.DAY_OF_YEAR, -1);
			}
		}
		mHasMoreData = loadCount >= mLoadLimit;
		
//		Collections.sort(list,new Comparator<Map<String, Object>>(){
//			@Override
//			public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
//				return (int) ((Long)lhs.get("dateInMilliSeconds") - (Long)rhs.get("dateInMilliSeconds"));
//			}
//		});
		return list;
	}
	
	private long getHasMoreDataDateInMillis(long fromDateInMillis){
		String[] args = new String[] {
				mDateFormat.format(fromDateInMillis) };
		String dateString = null;
		Cursor cursor = Cache
				.openDatabase()
				.rawQuery(
						"SELECT MAX(date) FROM MoneyExpense main WHERE date <= ? AND " + buildSearchQuery("Expense"),
						args);
		if (cursor != null) {
			cursor.moveToFirst();
			dateString = cursor.getString(0);
			cursor.close();
			cursor = null;
		}
		cursor = Cache
				.openDatabase()
				.rawQuery(
						"SELECT MAX(date) FROM MoneyIncome main WHERE date <= ? AND " + buildSearchQuery("Income"),
						args);
		if (cursor != null) {
			cursor.moveToFirst();
			if(cursor.getString(0) != null){
				if(dateString == null
						|| dateString.compareTo(cursor.getString(0)) < 0){
					dateString = cursor.getString(0);
				}
			}
			cursor.close();
			cursor = null;
		}
		cursor = Cache
				.openDatabase()
				.rawQuery(
						"SELECT MAX(date) FROM MoneyBorrow main WHERE date <= ? AND " + buildSearchQuery("Borrow"),
						args);
		if (cursor != null) {
			cursor.moveToFirst();
			if(cursor.getString(0) != null){
				if(dateString == null
						|| dateString.compareTo(cursor.getString(0)) < 0){
					dateString = cursor.getString(0);
				}
			}
			cursor.close();
			cursor = null;
		}
		cursor = Cache
				.openDatabase()
				.rawQuery(
						"SELECT MAX(date) FROM MoneyLend main WHERE date <= ? AND " + buildSearchQuery("Lend"),
						args);
		if (cursor != null) {
			cursor.moveToFirst();
			if(cursor.getString(0) != null){
				if(dateString == null
						|| dateString.compareTo(cursor.getString(0)) < 0){
					dateString = cursor.getString(0);
				}
			}
			cursor.close();
			cursor = null;
		}
		cursor = Cache
				.openDatabase()
				.rawQuery(
						"SELECT MAX(date) FROM MoneyTransfer main WHERE date <= ? AND " + buildTransferSearchQuery(),
						args);
		if (cursor != null) {
			cursor.moveToFirst();
			if(cursor.getString(0) != null){
				if(dateString == null
						|| dateString.compareTo(cursor.getString(0)) < 0){
					dateString = cursor.getString(0);
				}
			}
			cursor.close();
			cursor = null;
		}
		cursor = Cache
				.openDatabase()
				.rawQuery(
						"SELECT MAX(date) FROM MoneyReturn main WHERE date <= ? AND " + buildSearchQuery("Return"),
						args);
		if (cursor != null) {
			cursor.moveToFirst();
			if(cursor.getString(0) != null){
				if(dateString == null
						|| dateString.compareTo(cursor.getString(0)) < 0){
					dateString = cursor.getString(0);
				}
			}
			cursor.close();
			cursor = null;
		}
		cursor = Cache
				.openDatabase()
				.rawQuery(
						"SELECT MAX(date) FROM MoneyPayback main WHERE date <= ? AND " + buildSearchQuery("Payback"),
						args);
		if (cursor != null) {
			cursor.moveToFirst();
			if(cursor.getString(0) != null){
				if(dateString == null
						|| dateString.compareTo(cursor.getString(0)) < 0){
					dateString = cursor.getString(0);
				}
			}
			cursor.close();
			cursor = null;
		}
		if(dateString != null){
			try {
				dateString = dateString.replaceAll("Z$", "+0000");
				Long dateInMillis = mDateFormat.parse(dateString).getTime();
				Calendar calToday = Calendar.getInstance();
				calToday.setTimeInMillis(dateInMillis);
				calToday.set(Calendar.HOUR_OF_DAY, 0);
				calToday.clear(Calendar.MINUTE);
				calToday.clear(Calendar.SECOND);
				calToday.clear(Calendar.MILLISECOND);
				return calToday.getTimeInMillis();
			} catch (ParseException e) {
				e.printStackTrace();
				return -1;
			}
		} else {
			return -1;
		}
	}
	
	public boolean hasMoreData() {
		return mHasMoreData;
	}

	/**
	 * Called when there is new data to deliver to the client. The super class
	 * will take care of delivering it; the implementation here just adds a
	 * little more logic.
	 */
	@Override
	public void deliverResult(List<Map<String, Object>> objects) {
		mGroupList = objects;

		if (isStarted() && mGroupList != null) {
			// If the Loader is currently started, we can immediately
			// deliver its results.
			super.deliverResult(objects);
		}
	}

	@Override
	protected void onAbandon() {
		super.onAbandon();
		this.getContext().getContentResolver()
				.unregisterContentObserver(mChangeObserver);
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		if (mGroupList != null) {
			// If we currently have a result available, deliver it
			// immediately.
			deliverResult(mGroupList);
		}

		if (takeContentChanged() || mGroupList == null) {
			// If the data has changed since the last time it was loaded
			// or is not currently available, start a load.
			forceLoad();
		}
	}

	/**
	 * Handles a request to stop the Loader.
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	/**
	 * Handles a request to cancel a load.
	 */
	@Override
	public void onCanceled(List<Map<String, Object>> objects) {
		super.onCanceled(objects);
	}

	/**
	 * Handles a request to completely reset the Loader.
	 */
	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		mGroupList = null;
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
			onContentChanged();
		}
	}
	
}