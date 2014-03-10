package com.hoyoji.android.hyjframework.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjBitmapWorkerAsyncTask;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.RegisterActivity;
import com.hoyoji.hoyoji.models.Picture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Base64;
import android.widget.ImageView;

public class HyjImageView extends ImageView {
	private String mPictureId = "";
	private int mBackgroundResource = -1;
	
	public HyjImageView(Context context, AttributeSet attrs) {
		super(context, attrs);

//		this.setScaleType(ScaleType.FIT_XY);
//		Resources r = context.getResources();
//		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, r.getDisplayMetrics());
//		this.setLayoutParams(new LayoutParams((int)px, (int)px));
	}
		
	public HyjImageView(Context context) {
		super(context);
	}
//	public void setBackgroundResource(int resId){
//		if(mBackgroundResource == -1 || resId != mBackgroundResource){
//			mBackgroundResource = resId;
//			this.setBackgroundResource(resId);
//		}
//	}
	public void setImage(Picture picture){
		if(picture == null){
			if(mPictureId != null){
				mPictureId = null;
			}
			if(this.getBackground() == null) {
				setImageBitmap(HyjUtil.getCommonBitmap(R.drawable.ic_action_picture));
			} else {
				this.setImageDrawable(null);
			}
		} else if(picture.getId().equals(mPictureId)){
			return;
		} else {
			mPictureId = picture.getId();
			File f;
				try {
					f = HyjUtil.createImageFile(picture.getId()+"_icon", picture.getPictureType());
					HyjBitmapWorkerAsyncTask.loadBitmap(f.getAbsolutePath(), this);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	}
	
	public void setImage(String id){
		if(id != null && mPictureId.equals(id)){
			return;
		}
		
		mPictureId = id;
		if(id != null){
			setImage((Picture)Picture.getModel(Picture.class, id));
		} else {
			setImage((Picture)null);
			
		}
	}
	
	public void loadRemoteImage(final String id){
		if(id == null || id.length() == 0){
			setImage((Picture)null);
			return;
		}
		
		HyjBitmapWorkerAsyncTask.loadRemoteBitmap(id, HyjApplication.getServerUrl()+"fetchUserImageIcon.php", this);
		
	}
}
