package com.hoyoji.android.hyjframework.view;

import java.io.IOException;

import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.hoyoji.models.Picture;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageButton;
import android.widget.AbsListView.LayoutParams;

public class HyjImageView extends ImageButton {
	private Picture mPicture;
	
	public HyjImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setScaleType(ScaleType.FIT_XY);
//		Resources r = context.getResources();
//		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, r.getDisplayMetrics());
//		this.setLayoutParams(new LayoutParams((int)px, (int)px));
	}
		
	public void setImage(Picture picture){
		mPicture = picture;
		if(picture == null){
			return;
		}
		try {
			this.setImageURI(Uri.fromFile(HyjUtil.createImageFile(picture.getId()+"_icon", picture.getPictureType())));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
