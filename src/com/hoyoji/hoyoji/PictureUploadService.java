package com.hoyoji.hoyoji;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.activeandroid.query.Select;
import com.baidu.frontia.Frontia;
import com.baidu.frontia.FrontiaFile;
import com.baidu.frontia.api.FrontiaStorage;
import com.baidu.frontia.api.FrontiaStorageListener.FileListListener;
import com.baidu.frontia.api.FrontiaStorageListener.FileProgressListener;
import com.baidu.frontia.api.FrontiaStorageListener.FileTransferListener;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.hoyoji.models.Picture;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class PictureUploadService extends Service {
	public static final String TAG = "PictureUploadService";
	private static String mPictureUploading = null;
	private static FrontiaStorage mCloudStorage;

	@Override
	public void onCreate() {
		super.onCreate();

		mCloudStorage = Frontia.getStorage();
		
		UpdateReceiver  receiver = new UpdateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(receiver, filter); //注册,开始接听广播
		
	}
	
	// 接收来自Service的广播消息
	private static class UpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				try {
					// 由于刚刚切换网络,不能马上发送数据,等几秒后再发,比较可靠
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				uploadPictures();
			}
		}

	}
	     
	static void uploadPictures() {
			if (mPictureUploading == null) {
//				mPictureUploadThread = new Thread(new Runnable() {
//					public void run() {
						mPictureUploading = "uploading";
						final ConnectivityManager connectivityManager = (ConnectivityManager)HyjApplication.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
				        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
						if(wifiNetworkInfo != null && wifiNetworkInfo.isConnected()){
							if(HyjApplication.getInstance().getCurrentUser() == null){
								mPictureUploading = null;
								return;
							}
							try{
												final Picture picToUpload = new Select().from(Picture.class).where("toBeUploaded = ? AND ownerUserId = ?", 1, HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
												if(picToUpload != null){
													// uploading big picture ...
					//									uploadSingleBigPicture(picToUpload);
														File f = HyjUtil.createImageFile(picToUpload.getId(), picToUpload.getPictureType());
														if(f.exists()){
															// send to cloud storage ...
															final FrontiaFile mFile = new FrontiaFile();
															mFile.setNativePath(f.getAbsolutePath());
															mFile.setRemotePath("/" + f.getName());
													    	mCloudStorage.uploadFile(mFile,
													                new FileProgressListener() {
													                    @Override
													                    public void onProgress(String source, long bytes, long total) {
													                    }
													                },
													                new FileTransferListener() {
													                    @Override
													                    public void onSuccess(String source, String newTargetName) {
					//								                    	mFile.setRemotePath(newTargetName);
					
													        				HyjModelEditor<Picture> picEditor = picToUpload.newModelEditor();
													        				picEditor.getModelCopy().setToBeUploaded(false);
													        				picEditor.save();
																			mPictureUploading = null;
													        				uploadPictures();
													                    }
					
													                    @Override
													                    public void onFailure(String source, int errCode, String errMsg) {
													                    	if(errCode == -1){
														        				HyjModelEditor<Picture> picEditor = picToUpload.newModelEditor();
														        				picEditor.getModelCopy().setToBeUploaded(false);
														        				picEditor.save();
													                    	}
													                    	Log.i(TAG, errMsg);
//													                    	mCloudStorage.downloadFile(mFile, new FileProgressListener(){
//
//																				@Override
//																				public void onProgress(
//																						String arg0,
//																						long arg1,
//																						long arg2) {
//																					// TODO Auto-generated method stub
//																					
//																				}}, new FileTransferListener(){
//
//																					@Override
//																					public void onFailure(
//																							String arg0,
//																							int arg1,
//																							String arg2) {
//																						Log.i(arg0, arg2);
//																						
//																					}
//
//																					@Override
//																					public void onSuccess(
//																							String arg0,
//																							String arg1) {
//																						Log.i(arg0, arg1);
//																						
//																					}});
//													                    	

																			mPictureUploading = null;
													        				uploadPictures();
													                    }
													                }
													        );
														}
												} else {
													mPictureUploading = null;
												}
							} catch(Exception e){
								mPictureUploading = null;
							}
						}
//					}
//				});
//				mPictureUploadThread.start();
			}
	}

        
	protected static void uploadSingleBigPicture(final Picture picToUpload) throws IOException {

			File f = HyjUtil.createImageFile(picToUpload.getId(), picToUpload.getPictureType());
			if(f.exists()){
				// send to cloud storage ...
				final FrontiaFile mFile = new FrontiaFile();
				mFile.setNativePath(f.getAbsolutePath());
				mFile.setRemotePath(f.getName());
		    	mCloudStorage.uploadFile(mFile,
		                new FileProgressListener() {
		                    @Override
		                    public void onProgress(String source, long bytes, long total) {
//		                    	mInfoView.setText(source + " upload......:"
//		                                + bytes * 100 / total + "%");
		                    }
		                },
		                new FileTransferListener() {
		                    @Override
		                    public void onSuccess(String source, String newTargetName) {
		                    	mFile.setRemotePath(newTargetName);
//		                        mInfoView.setText(source + " uploaded as "
//		                                + newTargetName + " in the cloud.\n������:������������������������������������������������������������������������������~");

		        				HyjModelEditor<Picture> picEditor = picToUpload.newModelEditor();
		        				picEditor.getModelCopy().setToBeUploaded(false);
		        				picEditor.save();
		                    }

		                    @Override
		                    public void onFailure(String source, int errCode, String errMsg) {
		                    	if(errCode == -1){
			        				HyjModelEditor<Picture> picEditor = picToUpload.newModelEditor();
			        				picEditor.getModelCopy().setToBeUploaded(false);
			        				picEditor.save();
		                    	}
		                    	Log.i(TAG, errMsg);
//		                    	mInfoView.setText(source + " errCode:"
//		                                + errCode + ", errMsg:" + errMsg);
		                    }
		                }
		        );
				
			}
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		uploadPictures();
		return super.onStartCommand(intent, flags, startId);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy() executed");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}