package com.hoyoji.android.hyjframework.fragment;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.baidu.frontia.Frontia;
import com.baidu.frontia.FrontiaFile;
import com.baidu.frontia.api.FrontiaStorage;
import com.baidu.frontia.api.FrontiaStorageListener.FileProgressListener;
import com.baidu.frontia.api.FrontiaStorageListener.FileTransferListener;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjBlankUserActivity;
import com.hoyoji.android.hyjframework.view.HyjImagePreview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

public class HyjImagePreviewFragment extends HyjUserFragment {
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		final HyjImagePreview img = new HyjImagePreview(this.getActivity());
		Intent intent = this.getActivity().getIntent();
		String pictureName = intent.getStringExtra("pictureName");
		String pictureType = intent.getStringExtra("pictureType");
		
		final File f;
		try {
			f = HyjUtil.createImageFile(pictureName, pictureType);
			if(f.exists()){
				Bitmap bmp = HyjUtil.decodeSampledBitmapFromFile(f.getAbsolutePath(), null, null);
				img.setImageBitmap(bmp);
		        img.setMaxZoom(4f);
			} else {
				FrontiaStorage mCloudStorage = Frontia.getStorage();
				FrontiaFile mFile = new FrontiaFile();
				mFile.setNativePath(f.getAbsolutePath());
				mFile.setRemotePath("/" + f.getName());
				mCloudStorage.downloadFile(mFile, 
						new FileProgressListener(){
					@Override
					public void onProgress(
							String arg0,
							long arg1,
							long arg2) {
						// TODO Auto-generated method stub
						
					}}, new FileTransferListener(){

						@Override
						public void onFailure(
								String arg0,
								int arg1,
								String arg2) {
							Log.i(arg0, arg2);
							
						}

					@Override
					public void onSuccess(String arg0, String arg1) {
						Log.i(arg0, arg1);

						Bitmap bmp = HyjUtil.decodeSampledBitmapFromFile(f.getAbsolutePath(), null, null);
						img.setImageBitmap(bmp);
				        img.setMaxZoom(4f);
					}});
													                    	

			}
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
