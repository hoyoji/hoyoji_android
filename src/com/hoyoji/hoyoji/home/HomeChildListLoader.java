package com.hoyoji.hoyoji.home;

import java.util.ArrayList;
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


public class HomeChildListLoader extends AsyncTaskLoader<List<Map<String, Object>>> {

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

	    private List<Map<String, Object>> mChildList;
	    private Integer mLoadLimit = null;
	    
	    public HomeChildListLoader(Context context, Bundle queryParams) {
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
	    	
			HashMap<String, Object> groupObject1 = new HashMap<String, Object>();
			groupObject1.put("title", "日常支出");
			HashMap<String, Object> groupObject2 = new HashMap<String, Object>();
			groupObject2.put("title", "地铁");
			HashMap<String, Object> groupObject3 = new HashMap<String, Object>();
			groupObject3.put("title", "公司午餐");
			
			List<Map<String, Object>> groupList = new ArrayList<Map<String, Object>>();
			groupList.add(groupObject1);
			groupList.add(groupObject2);
			groupList.add(groupObject3);
			
			
			return groupList;
		}

		  /**
	     * Called when there is new data to deliver to the client.  The
	     * super class will take care of delivering it; the implementation
	     * here just adds a little more logic.
	     */
	    @Override public void deliverResult(List<Map<String, Object>> objects) {
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
	        
	        mChildList = null;
	    }
}