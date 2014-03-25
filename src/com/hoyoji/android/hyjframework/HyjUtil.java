package com.hoyoji.android.hyjframework;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.RegisterActivity;
import com.hoyoji.hoyoji.models.ClientSyncRecord;
import com.hoyoji.hoyoji.models.Exchange;

public class HyjUtil {
	public static void displayToast(int msg){
		Toast.makeText(HyjApplication.getInstance(), msg, Toast.LENGTH_LONG).show();
	}
	
	public static void displayToast(String msg){
		Toast.makeText(HyjApplication.getInstance(), msg, Toast.LENGTH_LONG).show();
	}
	
	public static void flattenJSONArray(JSONArray array, List<JSONObject> list){
		for (int i = 0; i < array.length(); i++) {
            try {
            	Object o = array.get(i); 
            	if(o instanceof JSONArray){
            		flattenJSONArray((JSONArray) o, list);
            	} else {
            		list.add((JSONObject) o);
            	}
			} catch (JSONException e) {
				e.printStackTrace();
			}
        }
	}
	
	 public static String getSHA1(String s) {
	        try {
	            MessageDigest digest = MessageDigest.getInstance("SHA-1");
	            digest.update(s.getBytes());
	            byte messageDigest[] = digest.digest();
	            return toHexString(messageDigest);
	        } catch (NoSuchAlgorithmException e) {
	            e.printStackTrace();
	        }
	        return "";
	    }
	  
	 public static boolean hasNetworkConnection(){
		 ConnectivityManager connMgr = (ConnectivityManager) HyjApplication
					.getInstance().getApplicationContext()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				return true;
			} else {
				return false;
			}
	 }
	 
	    public static String toHexString(byte[] keyData) {
	        if (keyData == null) {
	            return null;
	        }
	        int expectedStringLen = keyData.length * 2;
	        StringBuilder sb = new StringBuilder(expectedStringLen);
	        for (int i = 0; i < keyData.length; i++) {
	            String hexStr = Integer.toString(keyData[i] & 0x00FF,16);
	            if (hexStr.length() == 1) {
	                hexStr = "0" + hexStr;
	            }
	            sb.append(hexStr);
	        }
	        return sb.toString();
	    }
	    
		public static File createImageFile(String imageFileName) throws IOException {
		    // Create an image file name
		    File image = new File(
		    	HyjApplication.getInstance().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
		        imageFileName+".JPEG"
		    );

		    return image;
		}
		
		public static File createTempImageFile(String imageFileName) throws IOException {
		    // Create an image file name
			File outputDir = HyjApplication.getInstance().getCacheDir(); // context being the Activity pointer
			return File.createTempFile(imageFileName, ".JPEG", outputDir);
		}
		
		public static File createImageFile(String imageFileName, String type) throws IOException {
		    // Create an image file name
		    File image = new File(
		    	HyjApplication.getInstance().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
		        imageFileName+"."+type
		    );

		    return image;
		}
		

		public static void startRoateView(View v){
//			if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
				RotateAnimation anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				anim.setInterpolator(new LinearInterpolator());
				anim.setRepeatCount(Animation.INFINITE);
				anim.setDuration(1000);
				v.startAnimation(anim);
				
//				Animation rotation = AnimationUtils.loadAnimation(v.getContext(), R.anim.clockwise_rotate);
//			     rotation.setRepeatCount(Animation.INFINITE);
//			     v.startAnimation(rotation);
				
//			} else {
//				RotateDrawable d = (RotateDrawable)v.getDrawable();
//				ObjectAnimator anim = ObjectAnimator.ofInt(d, "Level", 10000);
//				anim.setRepeatCount(ObjectAnimator.INFINITE);
//				anim.setDuration(1000);
//				anim.start();
//			}
		}
		
		public static void stopRoateView(View view){
			view.setAnimation(null);
		}
		
		public static int calculateInSampleSize(
	            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {

	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;

	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }

	    return inSampleSize;
	}
		
		public static Bitmap decodeSampledBitmapFromResource(int resId,
				Integer reqWidth, Integer reqHeight) {
			Resources res = HyjApplication.getInstance().getResources();
		    // First decode with inJustDecodeBounds=true to check dimensions
		    final BitmapFactory.Options options = new BitmapFactory.Options();
		    if(reqWidth != null && reqHeight != null){
			    options.inJustDecodeBounds = true;
			    BitmapFactory.decodeResource(res, resId, options);
	
			    // Calculate inSampleSize
			    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	
			    // Decode bitmap with inSampleSize set
			    options.inJustDecodeBounds = false;
		    }
		    options.inPurgeable = true;
		    Bitmap bmp = BitmapFactory.decodeResource(res, resId, options);
		    if(bmp == null){
		    	return HyjUtil.getCommonBitmap(R.drawable.ic_action_refresh);
		    }
		    return bmp;
		}
		
		public static Bitmap decodeSampledBitmapFromFile(String photoPath, Integer targetW, Integer targetH){
		    // Get the dimensions of the bitmap
		    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		    if(targetW != null && targetH != null){
			    bmOptions.inJustDecodeBounds = true;
			    BitmapFactory.decodeFile(photoPath, bmOptions);
	
			    // Determine how much to scale down the image
			    bmOptions.inSampleSize = HyjUtil.calculateInSampleSize(bmOptions, targetW, targetH);
	
			    // Decode the image file into a Bitmap sized to fill the View
			    bmOptions.inJustDecodeBounds = false;
		    }
		    bmOptions.inPurgeable = true;
		    Bitmap bmp = BitmapFactory.decodeFile(photoPath, bmOptions);
		    if(bmp == null){
		    	return HyjUtil.getCommonBitmap(R.drawable.ic_action_picture);
		    }
		    return bmp;
		}
		
		static LinkedHashMap<String, Bitmap> commonBitmaps = new LinkedHashMap<String, Bitmap>();
		public static Bitmap getCommonBitmap(int resId) {
			Bitmap bitmap = commonBitmaps.get(String.valueOf(resId));
			if(bitmap == null){
				bitmap = decodeSampledBitmapFromResource(resId, null, null);
				commonBitmaps.put(String.valueOf(resId), bitmap);
			}
			return bitmap;
		}
		
		public static <T extends Object> T ifNull(T obj1, T obj2){
			if(obj1 == null){
				return obj2;
			} else {
				return obj1;
			}
		}
		
		public static <T extends Object> T ifJSONNull(JSONObject jsonObj, String field1, T obj){
			if(jsonObj.isNull(field1)){
				return obj;
			} else {
				try {
					return (T)jsonObj.opt(field1);
				} catch(Exception e){
					return null;
				}
			}
		}
		static SimpleDateFormat mIsoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		public static Date parseDateFromISO(String dateString){
			try {
				mIsoDateFormat.setTimeZone(TimeZone.getDefault());
				return mIsoDateFormat.parse(dateString.replaceAll("Z$", "+0000"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		}
		public static String formatDateToIOS(Date date){
			mIsoDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			return mIsoDateFormat.format(date).replaceAll("\\+0000$", "Z");
		}
		
		public static double toFixed2(Double number){
			return Math.round(number*100)/100.0;
		}
		
		public static double toFixed4(Double number){
			return Math.round(number*10000)/10000.0;
		}
		
		public static void updateClicentSyncRecord(String tableName, String recordId, String operation, boolean syncFromServer){
			
			if(!tableName.equalsIgnoreCase("ClientSyncRecord")){
				ClientSyncRecord clientSyncRecord = new Select().from(ClientSyncRecord.class).where("id=?", recordId).executeSingle();
				
				if(operation.equalsIgnoreCase("Delete")){
					if(syncFromServer){

						if(clientSyncRecord != null){
							clientSyncRecord.delete();
						}
						
					} else {
					
						if(clientSyncRecord == null){
							clientSyncRecord = new ClientSyncRecord();
							clientSyncRecord.setId(recordId);
							clientSyncRecord.setOperation(operation);
							clientSyncRecord.setTableName(tableName);
							clientSyncRecord.save();
						} else if(clientSyncRecord.getOperation().equalsIgnoreCase("Create")) {
							if(clientSyncRecord.getUploading()){
								// 新记录，正在上传时被删除。如果上传失败，我们会回来删除它
								clientSyncRecord.setOperation(operation);
								clientSyncRecord.save();
							} else {
								clientSyncRecord.delete();
							}
						} else if(clientSyncRecord.getOperation().equalsIgnoreCase("Update")) {
							if(clientSyncRecord.getUploading()){
								clientSyncRecord.setUploading(false);
							}
							clientSyncRecord.setOperation(operation);
							clientSyncRecord.save();
						}
					
					}
				} else 
				if(operation.equalsIgnoreCase("Update")){
					if(syncFromServer){
						
						if(clientSyncRecord != null){
							clientSyncRecord.delete();
						}
						
					} else {
					
						if(clientSyncRecord == null){
							clientSyncRecord = new ClientSyncRecord();
							clientSyncRecord.setId(recordId);
							clientSyncRecord.setOperation(operation);
							clientSyncRecord.setTableName(tableName);
							clientSyncRecord.save();
						} else if(clientSyncRecord.getOperation().equalsIgnoreCase("Create")) {
							if(clientSyncRecord.getUploading()){
								// 新记录，正在上传时被更新。如果上传失败，我们会回来将起改回到 "Create"
								clientSyncRecord.setOperation(operation);
								clientSyncRecord.save();
							}
						} else if(clientSyncRecord.getOperation().equalsIgnoreCase("Update")) {
							if(clientSyncRecord.getUploading()){
								clientSyncRecord.setUploading(false);
								clientSyncRecord.save();
							}
						}
						
					}
				} else
				if(operation.equalsIgnoreCase("Create")){
					if(syncFromServer){
						
						if(clientSyncRecord != null){
							clientSyncRecord.delete();
						}
						
					} else {
						if(clientSyncRecord == null){
							clientSyncRecord = new ClientSyncRecord();
							clientSyncRecord.setId(recordId);
							clientSyncRecord.setOperation(operation);
							clientSyncRecord.setTableName(tableName);
							clientSyncRecord.save();
						} else if(clientSyncRecord.getOperation().equalsIgnoreCase("Create")) {
							//clientSyncRecord.delete();
						} else if(clientSyncRecord.getOperation().equalsIgnoreCase("Update")) {
							clientSyncRecord.setOperation(operation);
							clientSyncRecord.save();
						} else if(clientSyncRecord.getOperation().equalsIgnoreCase("Delete")) {
							clientSyncRecord.setOperation(operation);
							clientSyncRecord.save();
						}
					
					}
				}
			}
		}

		public static void updateExchangeRate(final String fromCurrency, final String toCurrency, ImageView mImageViewRefreshRate, HyjNumericField mNumericExchangeRate) {
			final WeakReference<ImageView> refreshRateRefrence = new WeakReference<ImageView>(mImageViewRefreshRate);
			final WeakReference<HyjNumericField> exchangeRateRefrence = new WeakReference<HyjNumericField>(mNumericExchangeRate);
			
			HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
				@Override
				public void finishCallback(Object object) {
					System.gc();
					ImageView imageViewRefreshRate = refreshRateRefrence.get();
					HyjNumericField numericExchangeRate = exchangeRateRefrence.get();
					if(imageViewRefreshRate != null){
						HyjUtil.stopRoateView(imageViewRefreshRate);
						imageViewRefreshRate.setEnabled(true);
						numericExchangeRate.setEnabled(true);
						numericExchangeRate.setNumber((Double) object);
						
						Exchange exchange = new Select().from(Exchange.class).where("localCurrencyId=? AND foreignCurrencyId=?", fromCurrency, toCurrency).executeSingle();
					    if(exchange != null){
					    	exchange.setRate((Double) object);
					    	exchange.save();
					    }
					}
				}

				@Override
				public void errorCallback(Object object) {
					ImageView imageViewRefreshRate = refreshRateRefrence.get();
					HyjNumericField numericExchangeRate = exchangeRateRefrence.get();
					if(imageViewRefreshRate != null){
						HyjUtil.stopRoateView(imageViewRefreshRate);
						imageViewRefreshRate.setEnabled(true);
						numericExchangeRate.setEnabled(true);
					}
					if (object != null) {
						HyjUtil.displayToast(object.toString());
					} else {
						HyjUtil.displayToast(R.string.moneyExpenseFormFragment_toast_cannot_refresh_rate);
					}
				}
			};
			HyjHttpGetExchangeRateAsyncTask.newInstance(
					fromCurrency, toCurrency, serverCallbacks);
			
		}
		
		public static void detectMemoryLeak(Activity activity) {
//			if(activity == null){
//				return;
//			}
//			
//			final WeakReference<Activity> mActivity = new WeakReference<Activity>(activity);
//			Handler handler = new Handler(Looper
//					.getMainLooper());
//			handler.postDelayed(new Runnable() {
//				public void run() {
//					System.gc();
//					Activity activity = mActivity.get();
//					if(activity != null){
//						HyjUtil.displayToast("检测到内存泄漏啦... " + Integer.toHexString(activity.hashCode()));
//						detectMemoryLeak(activity);
//					} else {
//						//HyjUtil.displayToast("很好，无内存泄漏！");
//					}
//				}
//			}, 1000);
		}
}
