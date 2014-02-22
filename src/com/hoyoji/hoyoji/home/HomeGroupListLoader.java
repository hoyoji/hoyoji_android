package com.hoyoji.hoyoji.home;

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
		AsyncTaskLoader<List<Map<String, Object>>> {

	private DateFormat mDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private List<Map<String, Object>> mGroupList;
	private Integer mLoadLimit = 10;
	private boolean mHasMoreData = true;
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
	public List<Map<String, Object>> loadInBackground() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		DateFormat df = SimpleDateFormat.getDateInstance();
		Calendar calToday = Calendar.getInstance();
		calToday.set(Calendar.HOUR_OF_DAY, 0);
		calToday.clear(Calendar.MINUTE);
		calToday.clear(Calendar.SECOND);
		calToday.clear(Calendar.MILLISECOND);

// get start of this week in milliseconds
//		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
		// cal.add(Calendar.WEEK_OF_YEAR, -1);

		int loadCount = 0;
		while (loadCount < mLoadLimit) {
			int count = 0;
			String[] args = new String[] {
					mDateFormat.format(calToday.getTime()),
					mDateFormat.format(new Date(calToday.getTimeInMillis() + 24 * 3600000)) };
			double expenseTotal = 0;
			double incomeTotal = 0;
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
				groupObject.put("dateInMilliSeconds", calToday.getTimeInMillis());
				groupObject.put("expenseTotal",
						"收入: ¥" + String.valueOf(incomeTotal));
				groupObject.put("incomeTotal",
						"支出: ¥" + String.valueOf(expenseTotal));
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
		
		Collections.sort(list,new Comparator<Map<String, Object>>(){
			@Override
			public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
				return (int) ((Long)rhs.get("dateInMilliSeconds") - (Long)lhs.get("dateInMilliSeconds"));
			}
		});
		return list;
	}
	
	private long getHasMoreDataDateInMillis(long fromDateInMillis){
		String[] args = new String[] {
				mDateFormat.format(fromDateInMillis) };
		Cursor cursor = Cache
				.openDatabase()
				.rawQuery(
						"SELECT MAX(date) FROM MoneyExpense WHERE date <= ?",
						args);
		if (cursor != null) {
			cursor.moveToFirst();
			String ds = cursor.getString(0);
			cursor.close();
			cursor = null;
			if(ds != null){
				try {
					Long dateInMillis = mDateFormat.parse(ds).getTime();
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
			}
		}
		return -1;
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