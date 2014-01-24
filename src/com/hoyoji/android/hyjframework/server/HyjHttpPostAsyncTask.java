package com.hoyoji.android.hyjframework.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTask;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.User;


public class HyjHttpPostAsyncTask extends HyjAsyncTask {

	public HyjHttpPostAsyncTask(HyjAsyncTaskCallbacks callbacks) {
		super(callbacks);
	}

	public static HyjHttpPostAsyncTask newInstance(HyjAsyncTaskCallbacks callbacks, String... params){
		HyjHttpPostAsyncTask newTask = new HyjHttpPostAsyncTask(callbacks);
		newTask.execute(params);
		return newTask;
	}	
	
	@Override
	protected Object doInBackground(String... params) {
		ConnectivityManager connMgr = (ConnectivityManager)HyjApplication.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	    String target = "post";
	    if(params.length == 2){
	    	target = params[1];
	    }
	    if (networkInfo != null && networkInfo.isConnected()) {
	        return HyjServer.doHttpPost(this, HyjApplication.getServerUrl()+target+".php", params[0], true);
	    } else {
	    	try {
				return new JSONObject("{'__summary' : {'msg' : '"+HyjApplication.getInstance().getString(R.string.server_connection_disconnected)+"'}}");
			} catch (JSONException e) {
				e.printStackTrace();
			}
	    }
		return null;
	}
	

    public void doPublishProgress(Integer progress){
    	this.publishProgress(progress);
    }
    
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(Object result) {
    	if(mServerCallback != null){
    		if(result != null){
    			if(result instanceof JSONObject){
    				JSONObject jsonResult = (JSONObject)result;
        			if(jsonResult.isNull("__summary")){
        				mServerCallback.finishCallback(result);
        			} else {
        				mServerCallback.errorCallback(result);
        			}		
    			} else {
    				mServerCallback.errorCallback(result);
    			}
            } else {
            	try {
					mServerCallback.errorCallback(new JSONObject("{'__summary' : {'msg' : '"+HyjApplication.getInstance().getString(R.string.server_dataparse_error)+"'}}"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
    }
}