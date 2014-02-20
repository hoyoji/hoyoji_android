package com.hoyoji.android.hyjframework;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.RegisterActivity;

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
				// TODO Auto-generated catch block
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
		
		public static File createImageFile(String imageFileName, String type) throws IOException {
		    // Create an image file name
		    File image = new File(
		    	HyjApplication.getInstance().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
		        imageFileName+"."+type
		    );

		    return image;
		}
		

		public static void startRoateView(ImageView v){
//			if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
				RotateAnimation anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				anim.setInterpolator(new LinearInterpolator());
				anim.setRepeatCount(Animation.INFINITE);
				anim.setDuration(1000);
				v.startAnimation(anim);
//			} else {
//				RotateDrawable d = (RotateDrawable)v.getDrawable();
//				ObjectAnimator anim = ObjectAnimator.ofInt(d, "Level", 10000);
//				anim.setRepeatCount(ObjectAnimator.INFINITE);
//				anim.setDuration(1000);
//				anim.start();
//			}
		}
		
		public static void stopRoateView(ImageView v){
			v.setAnimation(null);
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
}
