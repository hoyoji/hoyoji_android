package com.hoyoji.hoyoji.home;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.MoneyBorrow;
import com.hoyoji.hoyoji.models.MoneyExpense;
import com.hoyoji.hoyoji.models.MoneyExpenseContainer;
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


public class HomeChildListLoader extends AsyncTaskLoader<List<HyjModel>> {

	/**
	 * Perform alphabetical comparison of application entry objects.
	 */
//	public static final Comparator<JSONObject> ALPHA_COMPARATOR = new Comparator<JSONObject>() {
//	    private final Collator sCollator = Collator.getInstance();
//	    @Override
//	    public int compare(JSONObject object1, JSONObject object2) {
//	        return sCollator.compare(object1.getString(mSortByField), object1.getString(mSortByField));
//	    }
//	};

		private DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	    private List<HyjModel> mChildList;
	    private Integer mLoadLimit = null;
	    private long mDateFrom = 0;
	    private long mDateTo = 0;
	    private ChangeObserver mChangeObserver;
	    private DateComparator mDateComparator = new DateComparator();
	    
	    public HomeChildListLoader(Context context, Bundle queryParams) {
	    	super(context);
			mDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    	if(queryParams != null){
	    		mLoadLimit = queryParams.getInt("limit");
	    		mDateFrom = queryParams.getLong("dateFrom");
	    		mDateTo = queryParams.getLong("dateTo");
	    	}
//	    	mChangeObserver = new ChangeObserver();
//	    	context.getContentResolver().registerContentObserver(
//	    			ContentProvider.createUri(MoneyExpenseContainer.class, null), true, mChangeObserver);
//	    	context.getContentResolver().registerContentObserver(
//	    			ContentProvider.createUri(MoneyIncome.class, null), true, mChangeObserver);

	    }
	    

	    public void changeQuery(Bundle queryParams){
	    	if(queryParams != null){
	    		mLoadLimit = queryParams.getInt("limit");
	    		mDateFrom = queryParams.getLong("dateFrom");
	    		mDateTo = queryParams.getLong("dateTo");
	    	}
	    	this.onContentChanged();
	    }
	    
	    
	    /**
	     * This is where the bulk of our work is done.  This function is
	     * called in a background thread and should generate a new set of
	     * data to be published by the loader.
	     */
	    @Override 
	    public List<HyjModel> loadInBackground() {
	    	
	    	String dateFrom = mDateFormat.format(new Date(mDateFrom));
	    	String dateTo = mDateFormat.format(new Date(mDateTo));
	    	ArrayList<HyjModel> list = new ArrayList<HyjModel>();
	    	
	    	List<HyjModel> moneyExpenses = new Select().from(MoneyExpenseContainer.class).where("date > ? AND date <= ?", dateFrom, dateTo).orderBy("date DESC").execute();
	    	list.addAll(moneyExpenses);
	    	
	    	List<HyjModel> moneyIncomes = new Select().from(MoneyIncome.class).where("date > ? AND date <= ?", dateFrom, dateTo).orderBy("date DESC").execute();
	    	list.addAll(moneyIncomes);
	    	
	    	List<HyjModel> moneyTransfers = new Select().from(MoneyTransfer.class).where("date > ? AND date <= ?", dateFrom, dateTo).orderBy("date DESC").execute();
	    	list.addAll(moneyTransfers);
	    	
	    	List<HyjModel> moneyBorrows = new Select().from(MoneyBorrow.class).where("date > ? AND date <= ?", dateFrom, dateTo).orderBy("date DESC").execute();
	    	list.addAll(moneyBorrows);
	    	
	    	List<HyjModel> moneyLends = new Select().from(MoneyLend.class).where("date > ? AND date <= ?", dateFrom, dateTo).orderBy("date DESC").execute();
	    	list.addAll(moneyLends);
	    	
	    	List<HyjModel> moneyReturns = new Select().from(MoneyReturn.class).where("date > ? AND date <= ?", dateFrom, dateTo).orderBy("date DESC").execute();
	    	list.addAll(moneyReturns);
	    	
	    	List<HyjModel> moneyPaybacks = new Select().from(MoneyPayback.class).where("date > ? AND date <= ?", dateFrom, dateTo).orderBy("date DESC").execute();
	    	list.addAll(moneyPaybacks);
	    	
	    	List<HyjModel> messages = new Select().from(Message.class).where("date > ? AND date <= ? AND (messageState=? OR messageState=?)", dateFrom, dateTo, "unread", "new").orderBy("date DESC").execute();
	    	list.addAll(messages);
	    	
	    	Collections.sort(list, mDateComparator);
	    	return list;
		}

	    static class DateComparator implements Comparator<HyjModel> {
			@Override
			public int compare(HyjModel lhs, HyjModel rhs) {
				String lhsStr = "";
				String rhsStr = "";
				if(lhs instanceof MoneyExpenseContainer){
					lhsStr = ((MoneyExpenseContainer) lhs).getDate();
				}
				if(rhs instanceof MoneyExpenseContainer){
					rhsStr = ((MoneyExpenseContainer) rhs).getDate();
				}
				
				if(lhs instanceof MoneyIncome){
					lhsStr = ((MoneyIncome) lhs).getDate();
				}
				if(rhs instanceof MoneyIncome){
					rhsStr = ((MoneyIncome) rhs).getDate();
				}
				
				if(lhs instanceof MoneyTransfer){
					lhsStr = ((MoneyTransfer) lhs).getDate();
				}
				if(rhs instanceof MoneyTransfer){
					rhsStr = ((MoneyTransfer) rhs).getDate();
				}
				
				if(lhs instanceof MoneyBorrow){
					lhsStr = ((MoneyBorrow) lhs).getDate();
				}
				if(rhs instanceof MoneyBorrow){
					rhsStr = ((MoneyBorrow) rhs).getDate();
				}
				
				if(lhs instanceof MoneyLend){
					lhsStr = ((MoneyLend) lhs).getDate();
				}
				if(rhs instanceof MoneyLend){
					rhsStr = ((MoneyLend) rhs).getDate();
				}
				
				if(lhs instanceof MoneyReturn){
					lhsStr = ((MoneyReturn) lhs).getDate();
				}
				if(rhs instanceof MoneyReturn){
					rhsStr = ((MoneyReturn) rhs).getDate();
				}
				
				if(lhs instanceof MoneyPayback){
					lhsStr = ((MoneyPayback) lhs).getDate();
				}
				if(rhs instanceof MoneyPayback){
					rhsStr = ((MoneyPayback) rhs).getDate();
				}
				
				if(lhs instanceof Message){
					lhsStr = ((Message) lhs).getDate();
				}
				if(rhs instanceof Message){
					rhsStr = ((Message) rhs).getDate();
				}
				return rhsStr.compareTo(lhsStr);
			}
	    } 
	    
		  @Override
		  protected void onAbandon (){
			  super.onAbandon();
//			  this.getContext().getContentResolver().unregisterContentObserver(mChangeObserver);
		  }


		/**
	     * Called when there is new data to deliver to the client.  The
	     * super class will take care of delivering it; the implementation
	     * here just adds a little more logic.
	     */
	    @Override public void deliverResult(List<HyjModel> objects) {
	        mChildList = objects;

	        if (isStarted() && mChildList != null) {
	            // If the Loader is currently started, we can immediately
	            // deliver its results.
	            super.deliverResult(objects);
	        }
	    }

	    /**
	     * Handles a request to start the Loader.
	     */
	    @Override 
	    protected void onStartLoading() {
	        if (mChildList != null) {
	            // If we currently have a result available, deliver it
	            // immediately.
	            deliverResult(mChildList);
	        }

	        if (takeContentChanged() || mChildList == null) {
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
	    public void onCanceled(List<HyjModel> objects) {
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
	        
	        mChildList = null;
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