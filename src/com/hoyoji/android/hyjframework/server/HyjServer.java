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
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTask;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.User;

public class HyjServer {
	
	public static void sendMsg() {
		
	}
	
	public static void searchData() {
		
	}
	
	public static Object doHttpPost(Object asyncTask, String serverUrl, String postData, boolean returnJSONError){
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
				post.setHeader("Authorization", "BASIC " + Base64.encodeToString(auth.getBytes(), Base64.DEFAULT | Base64.NO_WRAP));
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
						if(asyncTask instanceof HyjAsyncTask){
							((HyjAsyncTask)asyncTask).doPublishProgress((int) ((count / (float) length) * 100));
						}
					}
					Thread.sleep(10);
				}
				s = new String(baos.toByteArray());
				Log.i("Server", s);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if(returnJSONError){
				s = "{'__summary' : {'msg' : '"+HyjApplication.getInstance().getString(R.string.server_connection_error)+":\\n"+e.getLocalizedMessage()+"'}}";
			}			
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (Exception squish) {
			}
		}

		try {
			if(s == null){
				return null;
			} else if(s.startsWith("{")){
				return new JSONObject(s);
			} else if(s.startsWith("[")){
				return new JSONArray(s);
			} else if(s.equals("")){
				return new JSONArray();
			} else {
				if(returnJSONError){
					return new JSONObject("{'__summary' : {'msg' : '"+HyjApplication.getInstance().getString(R.string.server_dataparse_error)+"'}}");
				} 
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
    }    
	
}
