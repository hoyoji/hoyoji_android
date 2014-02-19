package com.hoyoji.android.hyjframework;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

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
		
		public static void startRoateView(View v){
			RotateAnimation anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			anim.setInterpolator(new LinearInterpolator());
			anim.setRepeatCount(Animation.INFINITE);
			anim.setDuration(1000);
			v.startAnimation(anim);
		}
		
		public static void stopRoateView(View v){
			v.setAnimation(null);
		}
		
}
