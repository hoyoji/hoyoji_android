package com.hoyoji.hoyoji.message;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.server.HyjServer;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.friend.AddFriendListFragment;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.models.UserData;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Base64;
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
			        		if(!HyjUtil.hasNetworkConnection()){
								try {
									Thread.sleep(5000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								continue;
			        		}
		        		
			        		User currentUser = HyjApplication.getInstance().getCurrentUser();
							Log.d(TAG, "checking messages ...");  
			        		JSONObject postData = new JSONObject();
			        		try {
				        		postData.put("__dataType", "Message");
								postData.put("messageBoxId", currentUser.getMessageBoxId());
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
									List<Message> newMessages = new ArrayList<Message>();
									try {
					        			ActiveAndroid.beginTransaction();
										if (jsonArray.length() > 0) {
											for(int i=0; i < jsonArray.length(); i++){
												JSONObject jsonMessage = jsonArray.optJSONObject(i);
												Message newMessage = new Message();
												newMessage.loadFromJSON(jsonMessage);
												newMessage.save();
												if(newMessage.getType().equalsIgnoreCase("System.Friend.AddResponse") 
														|| newMessage.getType().equalsIgnoreCase("System.Friend.Delete")){
													newMessages.add(newMessage);
												}
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
									
									for(Message newMessage : newMessages){
										if(newMessage.getType().equalsIgnoreCase("System.Friend.AddResponse")) {
											String newUserId = "";
											if(newMessage.getToUserId().equals(currentUser.getId())){
												newUserId = newMessage.getFromUserId();
											} else if(newMessage.getFromUserId().equals(currentUser.getId())){
												newUserId = newMessage.getToUserId();
											} else {
												continue;
											}
											Friend newFriend = new Select().from(Friend.class).where("friendUserId=?", newUserId).executeSingle();
											if(newFriend == null){
												loadNewlyAddedFriend(newUserId);
											}
										}
										else if(newMessage.getType().equalsIgnoreCase("System.Friend.Delete")){
											Friend delFriend = new Select().from(Friend.class).where("friendUserId=?", newMessage.getFromUserId()).executeSingle();
											if(delFriend != null){
												delFriend.delete();
											}
										}
									}
									
								}
			        		} catch (Exception e) {
			        			e.printStackTrace();
			        		} 

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

	private void loadNewlyAddedFriend(String friendUserId) {
		// load new friend from server
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				try {
					JSONArray jsonArray = (JSONArray) object;
					JSONObject jsonFriend;
					jsonFriend = jsonArray.getJSONArray(0)
							.getJSONObject(0);
					JSONObject jsonUser = null;
					try {
						jsonUser = jsonArray.optJSONArray(1)
								.getJSONObject(0);
					} catch (JSONException e) {
					}
					loadFriendPicturesAndSaveFriend(jsonUser, jsonFriend);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void errorCallback(Object object) {
			}
		};

		try {
			JSONObject data = new JSONObject();
			data.put("__dataType", "Friend");
			data.put("friendUserId", friendUserId);
			data.put("ownerUserId", HyjApplication.getInstance()
					.getCurrentUser().getId());
			JSONObject dataUser = new JSONObject();
			dataUser.put("__dataType", "User");
			dataUser.put("id", friendUserId);
			HyjHttpPostAsyncTask.newInstance(serverCallbacks, "["
					+ data.toString() + "," + dataUser.toString()
					+ "]", "findDataFilter");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
    
	private void loadFriendPicturesAndSaveFriend(final JSONObject jsonUser,
			final JSONObject jsonFriend) {
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				Friend newFriend = HyjModel.getModel(Friend.class,
						jsonFriend.optString("id"));
				if (newFriend == null) {
					newFriend = new Friend();
				}
				newFriend.loadFromJSON(jsonFriend);

				User newUser = HyjModel.getModel(User.class,
						jsonUser.optString("id"));
				if (newUser == null) {
					newUser = new User();
				}
				newUser.loadFromJSON(jsonUser);

				saveUserPictures(object);
				newUser.save();
				newFriend.save();
			}

			@Override
			public void errorCallback(Object object) {
			}
		};

		HyjHttpPostAsyncTask.newInstance(serverCallbacks,
				jsonUser.optString("id"), "fetchRecordPictures");
	}
	

	private void saveUserPictures(Object object) {
		JSONArray pictureArray = (JSONArray) object;
		for (int i = 0; i < pictureArray.length(); i++) {
			try {
				JSONObject jsonPic = pictureArray.getJSONObject(i);
				String base64PictureIcon = jsonPic
						.optString("base64PictureIcon");
				if (base64PictureIcon != null) {
					byte[] decodedByte = Base64.decode(base64PictureIcon, 0);
					Bitmap icon = BitmapFactory.decodeByteArray(decodedByte, 0,
							decodedByte.length);
					FileOutputStream out = new FileOutputStream(
							HyjUtil.createImageFile(jsonPic.optString("id")
									+ "_icon"));
					icon.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.close();
					out = null;
					jsonPic.remove("base64PictureIcon");
				}
				Picture newPicture = new Picture();
				newPicture.loadFromJSON(jsonPic);

				newPicture.save();

			} catch (JSONException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
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