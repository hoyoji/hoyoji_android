package com.hoyoji.hoyoji.message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.activeandroid.ActiveAndroid;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.server.HyjServer;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.friend.AddFriendListFragment;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.models.UserData;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class MessageDownloadService extends Service {  
    public static final String TAG = "MessageDownloadService";  
    private Thread mMessageDownloadThread  = null;
//    private MessageSericeBinder mBinder = new MessageSericeBinder();  
  
    @Override  
    public void onCreate() {  
        super.onCreate();  
        Log.d(TAG, "onCreate() executed");  
    }  
  
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {
    	if(mMessageDownloadThread == null){
	    	mMessageDownloadThread = new Thread(new Runnable() {  
		        @Override  
		        public void run() {  
		            // 开始执行后台任务  
		        	while(HyjApplication.getInstance().getCurrentUser() != null){
			        		User currentUser = HyjApplication.getInstance().getCurrentUser();
							Log.d(TAG, "checking messages ...");  
			        		JSONObject postData = new JSONObject();
			        		try {
				        		postData.put("__dataType", "Message");
								postData.put("ownerUserId", currentUser.getId());
//								postData.put("__orderBy", "lastServerUpdateTime ASC");
								String lastMessagesDownloadTime = currentUser.getUserData().getLastMessagesDownloadTime();
								if(lastMessagesDownloadTime != null){
									JSONObject timeFilter = new JSONObject();
									timeFilter.put("lastServerUpdateTime", lastMessagesDownloadTime);
									postData.put("__GREATER_FILTER__", timeFilter);
								}
								
								Object returnedObject = HyjServer.doHttpPost(null, HyjApplication.getServerUrl()+"getData.php", "[" + postData.toString() + "]", true);
								if(returnedObject instanceof JSONArray){
									final JSONArray jsonArray = ((JSONArray) returnedObject).optJSONArray(0); 
									try {
					        			ActiveAndroid.beginTransaction();
										if (jsonArray.length() > 0) {
											for(int i=0; i < jsonArray.length(); i++){
												JSONObject jsonMessage = jsonArray.optJSONObject(i);
												Message newMessage = new Message();
												newMessage.loadFromJSON(jsonMessage);
												newMessage.save();
		
												if(lastMessagesDownloadTime == null || lastMessagesDownloadTime.compareTo(jsonMessage.optString("lastServerUpdateTime")) < 0){
													lastMessagesDownloadTime = jsonMessage.optString("lastServerUpdateTime");
												}
											}
										} 
										if(lastMessagesDownloadTime != currentUser.getUserData().getLastMessagesDownloadTime()){
											HyjModelEditor<UserData> userDataEditor = currentUser.getUserData().newModelEditor();
											userDataEditor.getModelCopy().setLastMessagesDownloadTime(lastMessagesDownloadTime);
											userDataEditor.save();
										}
										ActiveAndroid.setTransactionSuccessful();
										if(jsonArray.length() > 0){
										 Handler handler = new Handler(Looper.getMainLooper());  
									        handler.post(new Runnable(){  
									            public void run(){  
													HyjUtil.displayToast(String.format(getApplicationContext().getString(R.string.app_toast_new_messages), jsonArray.length())); 
									            }  
									        });  
										}
					        		} catch (Exception e) {
					        		} finally {
					        		    ActiveAndroid.endTransaction();
					        		}
								}
			        		} catch (Exception e) {} 

							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
		        	}
		        	mMessageDownloadThread = null;
		        }
	    	});
	    	mMessageDownloadThread.start();  
    	}
    	return super.onStartCommand(intent, flags, startId);  
    }  
  
    @Override  
    public void onDestroy() {  
        super.onDestroy();  
        Log.d(TAG, "onDestroy() executed");  
    }  
  
    @Override  
    public IBinder onBind(Intent intent) {  
//        return mBinder;
    	return null;
    }  

//    class MessageSericeBinder extends Binder {  
//  
//        public void startDownloadMessages() {  
//        	new Thread(new Runnable() {  
//                @Override  
//                public void run() {  
//                    // 执行具体的下载任务  
//                }  
//            }).start();  
//     }  
//  
//    }
  
}  