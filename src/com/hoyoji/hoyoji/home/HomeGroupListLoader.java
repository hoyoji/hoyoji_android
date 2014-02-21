package com.hoyoji.hoyoji.home;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import com.hoyoji.hoyoji.models.MoneyExpense;
import com.hoyoji.hoyoji.models.MoneyIncome;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

public class HomeGroupListLoader extends
		AsyncTaskLoader<SortedMap<String, Map<String, Object>>> {

	private DateFormat mDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private SortedMap<String, Map<String, Object>> mGroupList;
	private Integer mLoadLimit = 10;
	private ChangeObserver mChangeObserver;

	public HomeGroupListLoader(Context context, Bundle queryParams) {
		super(context);
		mDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		if (queryParams != null) {
			mLoadLimit = queryParams.getInt("limit", 10);
		}

		mChangeObserver = new ChangeObserver();
		context.getContentResolver().registerContentObserver(
				ContentProvider.createUri(MoneyExpense.class, null), true,
				mChangeObserver);
		context.getContentResolver().registerContentObserver(
				ContentProvider.createUri(MoneyIncome.class, null), true,
				mChangeObserver);
	}

	public void fetchMore(Bundle queryParams) {
		if (queryParams != null) {
			mLoadLimit += queryParams.getInt("pageSize", 10);
		} else {
			mLoadLimit += 10;
		}
		this.onContentChanged();
	}

	/**
	 * This is where the bulk of our work is done. This function is called in a
	 * background thread and should generate a new set of data to be published
	 * by the loader.
	 */
	@Override
	public SortedMap<String, Map<String, Object>> loadInBackground() {
		SortedMap<String, Map<String, Object>> list = new TreeMap<String, Map<String, Object>>(new Comparator<String>(){
			@Override
			public int compare(String lhs, String rhs) {
				return rhs.compareTo(lhs);
			}
		});

		DateFormat df = SimpleDateFormat.getDateInstance();
		Calendar calToday = Calendar.getInstance();
		calToday.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of
											// day !
		calToday.clear(Calendar.MINUTE);
		calToday.clear(Calendar.SECOND);
		calToday.clear(Calendar.MILLISECOND);

//		Calendar calToday = Calendar.getInstance();
//		calToday.setTimeInMillis(cal.getTimeInMillis());

		// get start of this week in milliseconds
//		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
		// cal.add(Calendar.WEEK_OF_YEAR, -1);

		int totalRowCount = 0;
		while (totalRowCount < mLoadLimit) {
			String[] args = new String[] {
					mDateFormat.format(calToday.getTime()),
					mDateFormat.format(new Date(calToday.getTimeInMillis() + 24
							* 60 * 60 * 1000)) };
			double expenseTotal = 0;
			double incomeTotal = 0;
			double count = 0;
			Cursor cursor = Cache
					.openDatabase()
					.rawQuery(
							"SELECT COUNT(*) AS count, SUM(amount) as total FROM MoneyExpense WHERE date > ? AND date <= ?",
							args);
			if (cursor != null) {
				cursor.moveToFirst();
				count += cursor.getInt(0);
				expenseTotal += cursor.getDouble(1);
			}
			cursor.close();
			cursor = null;
			cursor = Cache
					.openDatabase()
					.rawQuery(
							"SELECT COUNT(*) AS count, SUM(amount) as total FROM MoneyIncome WHERE date > ? AND date <= ?",
							args);
			if (cursor != null) {
				cursor.moveToFirst();
				count += cursor.getInt(0);
				incomeTotal += cursor.getDouble(1);
			}
			cursor.close();
			cursor = null;
			if (count > 0) {
				String ds = df.format(calToday.getTime());
				HashMap<String, Object> groupObject = new HashMap<String, Object>();
				groupObject.put("date", ds);
				groupObject.put("dateInMilliSeconds",
						calToday.getTimeInMillis());
				groupObject.put("expenseTotal",
						"收入: ¥" + String.valueOf(incomeTotal));
				groupObject.put("incomeTotal",
						"支出: ¥" + String.valueOf(expenseTotal));
				list.put(ds, groupObject);
				totalRowCount += count;
			}

			calToday.add(Calendar.DAY_OF_YEAR, -1);
			totalRowCount++;
		}
		
		return list;
	}

	/**
	 * Called when there is new data to deliver to the client. The super class
	 * will take care of delivering it; the implementation here just adds a
	 * little more logic.
	 */
	@Override
	public void deliverResult(SortedMap<String, Map<String, Object>> objects) {
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
	public void onCanceled(SortedMap<String, Map<String, Object>> objects) {
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