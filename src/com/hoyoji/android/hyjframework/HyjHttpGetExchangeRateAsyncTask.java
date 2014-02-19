package com.hoyoji.android.hyjframework;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTask;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.hoyoji.R;

public class HyjHttpGetExchangeRateAsyncTask extends HyjAsyncTask {

	public HyjHttpGetExchangeRateAsyncTask(HyjAsyncTaskCallbacks callbacks) {
		super(callbacks);
	}

	public static HyjHttpGetExchangeRateAsyncTask newInstance(
			String fromCurrency, String toCurrency,
			HyjAsyncTaskCallbacks callbacks) {
		HyjHttpGetExchangeRateAsyncTask newTask = new HyjHttpGetExchangeRateAsyncTask(
				callbacks);
		newTask.execute(fromCurrency, toCurrency);
		return newTask;
	}

	@Override
	protected Object doInBackground(String... params) {
		ConnectivityManager connMgr = (ConnectivityManager) HyjApplication
				.getInstance().getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		String target = "https://www.google.com/finance/converter?a=1&from=" + params[0] + "&to=" + params[1];
		if (networkInfo != null && networkInfo.isConnected()) {
			return doHttpGet(target);
		} else {
			return HyjApplication.getInstance().getString(R.string.server_connection_disconnected);
		}
	}

	public void doPublishProgress(Integer progress) {
		this.publishProgress(progress);
	}

	// onPostExecute displays the results of the AsyncTask.
	@Override
	protected void onPostExecute(Object result) {
		if (result instanceof Double) {
			mServerCallback.finishCallback(result);
		} else {
			mServerCallback.errorCallback(result);
		}
	}

	private Object doHttpGet(String serverUrl) {
		InputStream is = null;
		String s = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(serverUrl);

			get.setHeader("Accept", "application/json");
			get.setHeader("Content-type", "application/json; charset=UTF-8");
			get.setHeader("Accept-Encoding", "gzip");

			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
			if (is != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[128];
				int ch = -1;

				while ((ch = is.read(buf)) != -1) {
					baos.write(buf, 0, ch);
					Thread.sleep(10);
				}
				s = new String(baos.toByteArray());
				Log.i("Server", s);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return HyjApplication.getInstance().getString(
					R.string.server_connection_error)
					+ ":\\n" + e.getLocalizedMessage();
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (Exception squish) {
			}
		}

		if (s != null) {
			try {
				String[] tokens = s.split("<span class=bld>");
				tokens = tokens[1].split("</span>");
				s = tokens[0];
				Pattern p = Pattern.compile("([^\\s]+).+");
				Matcher m = p.matcher(s);
				if (m.find()) {
					//DecimalFormat df = new DecimalFormat("0.####");
					// double d = Double.parseDouble(m.group(1));
					return Double.valueOf(m.group(1));
				}
			} catch(Exception e) {
				return null;
			}
			
//			boolean errorMatch = s.matches(".+,error: \"(\\d)\".+");
//			if (errorMatch) {
//				return null;
//			} else {
//				Pattern p = Pattern.compile(".+,rhs: \"([^\\s]+).+");
//				Matcher m = p.matcher(s);
//
//				if (m.find()) {
//					DecimalFormat df = new DecimalFormat("0.####");
//					// double d = Double.parseDouble(m.group(1));
//					return Double.valueOf(df.format(m.group(1)));
//				}
//				return null;
//			}
		}
		return null;
	}
}