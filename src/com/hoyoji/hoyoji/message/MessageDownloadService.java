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
import com.activeandroid.Cache;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.server.HyjServer;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.friend.AddFriendListFragment;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
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
	private Thread mMessageDownloadThread = null;

	// private MessageSericeBinder mBinder = new MessageSericeBinder();

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mMessageDownloadThread == null) {
			mMessageDownloadThread = new Thread(new Runnable() {
				@Override
				public void run() {
					// 开始执行后台任务
					if (HyjApplication.getInstance().getCurrentUser() != null) {
						try {
							if (!HyjUtil.hasNetworkConnection()) {
								return;
							}

							User currentUser = HyjApplication.getInstance()
									.getCurrentUser();
							Log.d(TAG, "checking messages ...");
							JSONObject postData = new JSONObject();
							postData.put("__dataType", "Message");
							postData.put("toUserId", currentUser.getId());
							JSONObject notFilter = new JSONObject();
							notFilter.put("messageState", "closed");
							postData.put("__NOT_FILTER__", notFilter);
							
							JSONObject jsonServerTime = null;
							String lastMessagesDownloadTime = currentUser
									.getUserData()
									.getLastMessagesDownloadTime();
							if (lastMessagesDownloadTime == null || lastMessagesDownloadTime.isEmpty()) {

								Object serverTime = HyjServer
										.doHttpPost(null,
												HyjApplication.getServerUrl()
														+ "getServerTime.php",
												"", true);
								jsonServerTime = (JSONObject) serverTime;
							} else {
								JSONObject timeFilter = new JSONObject();
								timeFilter.put("lastServerUpdateTime",
										currentUser.getUserData()
												.getLastMessagesDownloadTime());
								postData.put("__GREATER_FILTER__", timeFilter);
							}

							Object returnedObject = HyjServer.doHttpPost(null,
									HyjApplication.getServerUrl()
											+ "getData.php",
									"[" + postData.toString() + "]", true);
							if (returnedObject instanceof JSONArray) {
								final JSONArray jsonArray = ((JSONArray) returnedObject)
										.optJSONArray(0);
								List<Message> friendMessages = new ArrayList<Message>();
								List<Message> projectShareMessages = new ArrayList<Message>();
								try {
									ActiveAndroid.beginTransaction();
									if (jsonArray.length() > 0) {
										for (int i = 0; i < jsonArray.length(); i++) {
											JSONObject jsonMessage = jsonArray
													.optJSONObject(i);
											Message newMessage = new Message();
											newMessage.loadFromJSON(
													jsonMessage, true);
											newMessage.save();
											if (newMessage
													.getType()
													.equalsIgnoreCase(
															"System.Friend.AddResponse")
													|| newMessage
															.getType()
															.equalsIgnoreCase(
																	"System.Friend.Delete")) {
												friendMessages.add(newMessage);
											} else if (newMessage
													.getType()
													.equalsIgnoreCase(
															"Project.Share.Accept")
													|| newMessage
															.getType()
															.equalsIgnoreCase(
																	"Project.Share.Delete")) {
												projectShareMessages
														.add(newMessage);
											}
											if (lastMessagesDownloadTime == null || lastMessagesDownloadTime.isEmpty()
													|| lastMessagesDownloadTime
															.compareTo(jsonMessage
																	.optString("lastServerUpdateTime")) < 0) {
												lastMessagesDownloadTime = jsonMessage
														.optString("lastServerUpdateTime");
											}
										}
									}

									if ((lastMessagesDownloadTime == null || lastMessagesDownloadTime.isEmpty())
											&& jsonServerTime != null) {
										lastMessagesDownloadTime = jsonServerTime
												.optString("server_time");
									}

									if (!lastMessagesDownloadTime.equals(currentUser
											.getUserData()
											.getLastMessagesDownloadTime())) {
										// HyjModelEditor<UserData>
										// userDataEditor =
										// currentUser.getUserData().newModelEditor();
										// userDataEditor
										// .getModelCopy()
										// .setLastMessagesDownloadTime(
										// lastMessagesDownloadTime);
										// userDataEditor
										// .getModel().setSyncFromServer(true);
										// userDataEditor.save();
										
										currentUser.getUserData().setLastMessagesDownloadTime(lastMessagesDownloadTime);
										Cache.openDatabase()
												.execSQL(
														"Update UserData SET lastMessagesDownloadTime = '"
																+ lastMessagesDownloadTime
																+ "' WHERE id = '"
																+ currentUser.getUserDataId()
																+ "'");
									}
									ActiveAndroid.setTransactionSuccessful();
									if (jsonArray.length() > 0) {
										int newCount = 0;
										for(int i=0; i < jsonArray.length(); i++){
											if(jsonArray.optJSONObject(i).optString("messageState").equalsIgnoreCase("new")){
												newCount++;
											}
										}
										if(newCount > 0){
											Handler handler = new Handler(Looper
													.getMainLooper());
											handler.post(new Runnable() {
												public void run() {
													HyjUtil.displayToast(String
															.format(getApplicationContext()
																	.getString(
																			R.string.app_toast_new_messages),
																	jsonArray
																			.length()));
												}
											});
										}
									}
								} catch (Exception e) {
								} finally {
									ActiveAndroid.endTransaction();
								}
								processFriendMessages(friendMessages,
										currentUser);
								processProjectShareMessages(
										projectShareMessages, currentUser);

							}

						} catch (Exception e) {
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

	protected void processProjectShareMessages(List<Message> newMessages,
			User currentUser) {
		for (Message newMessage : newMessages) {
			if (newMessage.getType().equalsIgnoreCase("Project.Share.Accept")) {

				loadSharedProjectData(newMessage);

			} else if (newMessage.getType().equalsIgnoreCase(
					"Project.Share.Delete")) {

			}
		}

	}

	protected void loadSharedProjectData(Message message) {
		// load new ProjectData from server
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				try {

					JSONArray jsonArray = (JSONArray) object;
					ActiveAndroid.beginTransaction();

					for (int i = 0; i < jsonArray.length(); i++) {
						JSONArray jsonObjects = jsonArray.getJSONArray(i);
						for (int j = 0; j < jsonObjects.length(); j++) {
							if (jsonObjects.optJSONObject(j)
									.optString("__dataType").equals("Project")) {
								Project newProject = new Project();
								newProject.loadFromJSON(
										jsonObjects.optJSONObject(j), true);
								newProject.save();
							} else if (jsonObjects.optJSONObject(j)
									.optString("__dataType")
									.equals("ProjectShareAuthorization")) {
								ProjectShareAuthorization newProjectShareAuthorization = new ProjectShareAuthorization();
								newProjectShareAuthorization.loadFromJSON(
										jsonObjects.optJSONObject(j), true);
								newProjectShareAuthorization.save();
							}
						}
					}

					ActiveAndroid.setTransactionSuccessful();

				} catch (JSONException e) {
					e.printStackTrace();
				} finally {
					ActiveAndroid.endTransaction();
				}
			}

			@Override
			public void errorCallback(Object object) {
			}
		};

		JSONArray data = new JSONArray();
		try {
			JSONArray projectIds = new JSONObject(message.getMessageData())
					.optJSONArray("projectIds");
			for (int i = 0; i < projectIds.length(); i++) {
				JSONObject newObj = new JSONObject();
				newObj.put("__dataType", "Project");
				newObj.put("id", projectIds.get(i));
				data.put(newObj);
				JSONObject newObj1 = new JSONObject();
				newObj1.put("__dataType", "ProjectShareAuthorization");
				newObj1.put("projectId", projectIds.get(i));
				newObj1.put("state", "Accept");
				data.put(newObj1);
			}
			HyjHttpPostAsyncTask.newInstance(serverCallbacks, data.toString(),
					"getData");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected void processFriendMessages(List<Message> newMessages,
			User currentUser) {
		for (Message newMessage : newMessages) {
			if (newMessage.getType().equalsIgnoreCase(
					"System.Friend.AddResponse")) {
				String newUserId = "";
				if (newMessage.getToUserId().equals(currentUser.getId())) {
					newUserId = newMessage.getFromUserId();
				} else if (newMessage.getFromUserId().equals(
						currentUser.getId())) {
					newUserId = newMessage.getToUserId();
				} else {
					continue;
				}
				Friend newFriend = new Select().from(Friend.class)
						.where("friendUserId=?", newUserId).executeSingle();
				if (newFriend == null) {
					loadNewlyAddedFriend(newUserId);
				}
			} else if (newMessage.getType().equalsIgnoreCase(
					"System.Friend.Delete")) {
				Friend delFriend = new Select().from(Friend.class)
						.where("friendUserId=?", newMessage.getFromUserId())
						.executeSingle();
				if (delFriend != null) {
					delFriend.delete();
				}
			}
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy() executed");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// return mBinder;
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
					jsonFriend = jsonArray.getJSONArray(0).getJSONObject(0);
					JSONObject jsonUser = null;
					try {
						jsonUser = jsonArray.optJSONArray(1).getJSONObject(0);
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
			HyjHttpPostAsyncTask.newInstance(serverCallbacks,
					"[" + data.toString() + "," + dataUser.toString() + "]",
					"findDataFilter");
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
				newFriend.loadFromJSON(jsonFriend, true);

				User newUser = HyjModel.getModel(User.class,
						jsonUser.optString("id"));
				if (newUser == null) {
					newUser = new User();
				}
				newUser.loadFromJSON(jsonUser, true);

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
				if (base64PictureIcon.length() > 0) {
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
				newPicture.loadFromJSON(jsonPic, true);

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
	// class MessageSericeBinder extends Binder {
	//
	// public void startDownloadMessages() {
	// new Thread(new Runnable() {
	// @Override
	// public void run() {
	// // 执行具体的下载任务
	// }
	// }).start();
	// }
	//
	// }

}