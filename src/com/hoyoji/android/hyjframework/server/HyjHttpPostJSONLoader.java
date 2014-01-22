package com.hoyoji.android.hyjframework.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.User;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Base64;
import android.util.Log;


public class HyjHttpPostJSONLoader extends AsyncTaskLoader<List<JSONObject>> {

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

	    private List<JSONObject> mJSONList;
//	    private String mSortByField;
	    private String mTarget = "post";
	    private String mPostData = "";
	    
	    public HyjHttpPostJSONLoader(Context context, String target, String postData) {
	    	super(context);
	    	if(target != null){
	    		mTarget = target;
	    	}
	    	if(postData != null){
		    	mPostData = postData;
	    	}
	    }
	    
//	    public HyjHttpPostJsonLoader(Context context, String target, String sortByField) {
//	    	super(context);
//	    	mSortByField = sortByField;
//	    	mTarget = target;
//	    }

	    /**
	     * This is where the bulk of our work is done.  This function is
	     * called in a background thread and should generate a new set of
	     * data to be published by the loader.
	     */
	    @Override 
	    public List<JSONObject> loadInBackground() {
	        ConnectivityManager connMgr = (ConnectivityManager)HyjApplication.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		    if (networkInfo != null && networkInfo.isConnected()) {
		        Object object = doHttpPost(HyjApplication.getServerUrl()+mTarget+".php", mPostData);

				List<JSONObject> list = new ArrayList<JSONObject>();
		        if(object == null){
		        	return list;
		        } else if(object instanceof JSONObject){
		        	list.add((JSONObject) object);
				} else {
					JSONArray array = ((JSONArray)object);
					for (int i = 0; i < array.length(); i++) {
			            try {
							list.add((JSONObject) array.get(i));
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        }
					return list;
				}
		    } else {
				//HyjUtil.displayToast(R.string.server_connection_disconnected);
		    }
			return new ArrayList<JSONObject>();
		}
		
		private Object doHttpPost(String serverUrl, String postData){
	    	User currentUser = HyjApplication.getInstance().getCurrentUser();
			Context appContext = HyjApplication.getInstance().getApplicationContext();

			InputStream is = null;
			String s = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(serverUrl);
				post.setEntity(new StringEntity(postData, HTTP.UTF_8));
				post.setHeader("Accept", "application/json");
				post.setHeader("Content-type", "application/json; charset=UTF-8");
				post.setHeader("Accept-Encoding", "gzip");
				post.setHeader("HyjApp-Version", appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0).versionName);
				if (currentUser != null) {
					String auth = URLEncoder.encode(currentUser.getUserName(), "UTF-8") + ":" + URLEncoder.encode(currentUser.getUserData().getPassword(), "UTF-8");
					//post.setHeader("Cookie", "authentication=" + Base64.encodeToString(auth.getBytes(), Base64.DEFAULT).replace("\r\n", "").replace("=", "%$09"));
					post.setHeader("Authorization", "BASIC " + Base64.encodeToString(auth.getBytes(), Base64.DEFAULT));
				}
				
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();
				long length = entity.getContentLength();
				is = entity.getContent();
				if (is != null) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buf = new byte[128];
					int ch = -1;
					int count = 0;

					while ((ch = is.read(buf)) != -1) {
						baos.write(buf, 0, ch);
						count += ch;
//						if (length > 0) {
//								publishProgress((int) ((count / (float) length) * 100));
//						}
						Thread.sleep(100);
					}
					s = new String(baos.toByteArray());
					Log.i("Server", s);
				}
			} catch (IOException e) {
				e.printStackTrace();
				//HyjUtil.displayToast(HyjApplication.getInstance().getString(R.string.server_connection_error)+":\\n"+e.getLocalizedMessage());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (is != null)
						is.close();
				} catch (Exception squish) {
				}
			}

			try {
				if(s.startsWith("{")){
					return new JSONObject(s);
				} else {
					return new JSONArray(s);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//HyjUtil.displayToast(R.string.server_dataparse_error);
				return null;
			}
	    }

		  /**
	     * Called when there is new data to deliver to the client.  The
	     * super class will take care of delivering it; the implementation
	     * here just adds a little more logic.
	     */
	    @Override public void deliverResult(List<JSONObject> objects) {
	        mJSONList = objects;

	        if (isStarted()) {
	            // If the Loader is currently started, we can immediately
	            // deliver its results.
	            super.deliverResult(objects);
	        }
	    }

	    /**
	     * Handles a request to start the Loader.
	     */
	    @Override protected void onStartLoading() {
	        if (mJSONList != null) {
	            // If we currently have a result available, deliver it
	            // immediately.
	            deliverResult(mJSONList);
	        }

	        if (takeContentChanged() || mJSONList == null) {
	            // If the data has changed since the last time it was loaded
	            // or is not currently available, start a load.
	            forceLoad();
	        }
	    }

	    /**
	     * Handles a request to stop the Loader.
	     */
	    @Override protected void onStopLoading() {
	        // Attempt to cancel the current load task if possible.
	        cancelLoad();
	    }

	    /**
	     * Handles a request to completely reset the Loader.
	     */
	    @Override protected void onReset() {
	        super.onReset();

	        // Ensure the loader is stopped
	        onStopLoading();
	        
	        mJSONList = null;
	    }
}