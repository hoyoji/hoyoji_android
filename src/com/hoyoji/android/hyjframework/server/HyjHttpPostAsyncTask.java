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
	protected JSONObject doInBackground(String... params) {
		ConnectivityManager connMgr = (ConnectivityManager)HyjApplication.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	    String target = "post";
	    if(params.length == 2){
	    	target = params[1];
	    }
	    if (networkInfo != null && networkInfo.isConnected()) {
	        return doHttpPost(HyjApplication.getServerUrl()+target+".php", params[0]);
	    } else {
			String sErr = "{'__summary' : {'msg' : '"+HyjApplication.getInstance().getString(R.string.server_connection_disconnected)+"'}}";
	    	try {
				return new JSONObject(sErr);
			} catch (JSONException e) {
				e.printStackTrace();
			}
	    }
		return null;
	}
	
	private JSONObject doHttpPost(String serverUrl, String postData){
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
					if (length > 0) {
						publishProgress((int) ((count / (float) length) * 100));
					}
					Thread.sleep(100);
				}
				s = new String(baos.toByteArray());
				Log.i("Server", s);
			}
		} catch (IOException e) {
			s = "{'__summary' : {'msg' : '"+HyjApplication.getInstance().getString(R.string.server_connection_error)+":\\n"+e.getLocalizedMessage()+"'}}";
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
			return new JSONObject(s);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }
    
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(Object result) {
    	JSONObject jsonResult = (JSONObject)result;
    	if(mServerCallback != null){
    		if(jsonResult != null){
    			if(jsonResult.isNull("__summary")){
    				mServerCallback.finishCallback(jsonResult);
    			} else {
    				mServerCallback.errorCallback(jsonResult);
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