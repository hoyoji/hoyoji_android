package com.hoyoji.hoyoji.home;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjUtil;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;


public class HomeGroupListLoader extends AsyncTaskLoader<List<Map<String, Object>>> {

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

	    private List<Map<String, Object>> mGroupList;
	    private Integer mLoadLimit = null;
	    
	    public HomeGroupListLoader(Context context, Bundle queryParams) {
	    	super(context);
	    	if(queryParams != null){
	    		mLoadLimit = queryParams.getInt("limit");
	    	}
	    }
	    

	    public void changePostQuery(Bundle queryParams){
	    	if(queryParams != null){
	    		mLoadLimit = queryParams.getInt("limit");
	    	}
	    	this.onContentChanged();
	    }
	    
	    
	    /**
	     * This is where the bulk of our work is done.  This function is
	     * called in a background thread and should generate a new set of
	     * data to be published by the loader.
	     */
	    @Override 
	    public List<Map<String, Object>> loadInBackground() {
	    	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

			DateFormat df = SimpleDateFormat.getDateInstance();
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
			cal.clear(Calendar.MINUTE);
			cal.clear(Calendar.SECOND);
			cal.clear(Calendar.MILLISECOND);
			
			Calendar calToday = Calendar.getInstance();
			calToday.setTimeInMillis(cal.getTimeInMillis());
			
			// get start of this week in milliseconds
			cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
			//cal.add(Calendar.WEEK_OF_YEAR, -1);

			
			for(; cal.getTimeInMillis() <= calToday.getTimeInMillis(); calToday.add(Calendar.DAY_OF_YEAR, -1)){
				String ds = df.format(calToday.getTime());
				HashMap<String, Object> groupObject = new HashMap<String, Object>();
				groupObject.put("date", ds);
				list.add(groupObject);
			}
			return list;
		}

		  /**
	     * Called when there is new data to deliver to the client.  The
	     * super class will take care of delivering it; the implementation
	     * here just adds a little more logic.
	     */
	    @Override public void deliverResult(List<Map<String, Object>> objects) {
	        mGroupList = objects;

	        if (isStarted() && mGroupList != null) {
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
}