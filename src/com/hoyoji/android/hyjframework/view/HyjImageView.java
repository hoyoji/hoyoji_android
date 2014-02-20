package com.hoyoji.android.hyjframework.view;

import java.io.File;
import java.io.IOException;

import com.hoyoji.android.hyjframework.HyjBitmapWorkerAsyncTask;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Picture;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class HyjImageView extends ImageView {
	private Picture mPicture;
	
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

	public void setImage(Picture picture){
		mPicture = picture;
		if(picture == null){
			super.setImageBitmap(HyjUtil.getCommonBitmap(R.drawable.ic_action_picture));
		} else {
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
		if(id != null){
			setImage((Picture)Picture.getModel(Picture.class, id));
		} else {
			setImage((Picture)null);
		}
	}
}
