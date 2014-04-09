package com.hoyoji.android.hyjframework.fragment;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjBlankUserActivity;
import com.hoyoji.android.hyjframework.view.HyjImagePreview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

public class HyjImagePreviewFragment extends HyjUserFragment {
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		HyjImagePreview img = new HyjImagePreview(this.getActivity());
		Intent intent = this.getActivity().getIntent();
		String pictureName = intent.getStringExtra("pictureName");
		String pictureType = intent.getStringExtra("pictureType");
		
		File f;
		try {
			f = HyjUtil.createImageFile(pictureName, pictureType);
			Bitmap bmp = HyjUtil.decodeSampledBitmapFromFile(f.getAbsolutePath(), null, null);
			img.setImageBitmap(bmp);
	        img.setMaxZoom(4f);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return img;
	}

	@Override
	public Integer useContentView() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
