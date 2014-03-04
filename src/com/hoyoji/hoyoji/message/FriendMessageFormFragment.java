package com.hoyoji.hoyoji.message;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.view.HyjDateTimeField;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.friend.AddFriendListFragment;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.User;

public class FriendMessageFormFragment extends HyjUserFormFragment {

	private HyjModelEditor<Message> mMessageEditor = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjTextField mEditTextToUser = null;
	private HyjTextField mEditTextTitle = null;
	private HyjRemarkField mEditTextDetail = null;

	@Override
	public Integer useContentView() {
		return R.layout.message_formfragment_friendmessage;
	}

	@Override
	public Integer useOptionsMenuView() {
		return null;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		Message friendAddMessage;

		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if (modelId != -1) {
			friendAddMessage = new Select().from(Message.class)
					.where("_id=?", modelId).executeSingle();
		} else {
			friendAddMessage = new Message();
		}
		mMessageEditor = friendAddMessage.newModelEditor();

		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(
				R.id.friendAddRequestMessageFormFragment_editText_date);
		mDateTimeFieldDate.setText(friendAddMessage.getDate());
		mDateTimeFieldDate.setEnabled(false);

		mEditTextToUser = (HyjTextField) getView().findViewById(
				R.id.friendAddRequestMessageFormFragment_editText_toUser);
		mEditTextToUser.setEnabled(false);
		mEditTextTitle = (HyjTextField) getView().findViewById(
				R.id.friendAddRequestMessageFormFragment_editText_title);
		mEditTextTitle.setText(friendAddMessage.getMessageTitle());
		mEditTextTitle.setEnabled(false);

		mEditTextDetail = (HyjRemarkField) getView().findViewById(
				R.id.friendAddRequestMessageFormFragment_editText_detail);
		mEditTextDetail.setText(friendAddMessage.getMessageDetail());
		Button actionButton = (Button) getView().findViewById(
				R.id.button_save);
		if (friendAddMessage.getFromUserId().equals(
				HyjApplication.getInstance().getCurrentUser().getId())) {
			// mDateTimeFieldDate.setLabel(R.string.friendAddRequestMessageFormFragment_textView_date_send);
			// mEditTextToUser.setLabel(R.string.friendAddRequestMessageFormFragment_textView_toUser);
			mEditTextToUser.setText(friendAddMessage.getToUserDisplayName());
		} else {
			mDateTimeFieldDate
					.setLabel(R.string.friendAddRequestMessageFormFragment_textView_date_receive);
			mEditTextToUser
					.setLabel(R.string.friendAddRequestMessageFormFragment_textView_fromUser);
			mEditTextToUser.setText(friendAddMessage.getFromUserDisplayName());
			mEditTextDetail.setEnabled(false);
			
			actionButton
					.setText(R.string.friendAddRequestMessageFormFragment_button_accept);
		}
		if(friendAddMessage.getType().equalsIgnoreCase("System.Friend.AddResponse") || friendAddMessage.getType().equalsIgnoreCase("System.Friend.Delete")){
			actionButton.setVisibility(View.GONE);
			mEditTextDetail.setEnabled(false);
		}
		

	}

	private void fillData() {
		Message modelCopy = (Message) mMessageEditor.getModelCopy();
		modelCopy.setMessageDetail(mEditTextDetail.getText().toString().trim());
	}

	private void showValidatioErrors() {
		HyjUtil.displayToast(R.string.app_validation_error);

		mEditTextDetail.setError(mMessageEditor
				.getValidationError("messageDetail"));
	}

	@Override
	public void onSave(View v) {
		super.onSave(v);
		if(mMessageEditor.getModel().getType().equalsIgnoreCase("System.Friend.AddResponse") || mMessageEditor.getModel().getType().equalsIgnoreCase("System.Friend.Delete")){
			return;
		}
		if (mMessageEditor.getModel().getFromUserId()
				.equals(HyjApplication.getInstance().getCurrentUser().getId())) {
			fillData();
			mMessageEditor.validate();

			if (mMessageEditor.hasValidationErrors()) {
				showValidatioErrors();
			} else {
				Friend newFriend = new Select().from(Friend.class).where("friendUserId=?",
						mMessageEditor.getModelCopy().getToUserId()).executeSingle();
				if (newFriend != null) {
					((HyjActivity) FriendMessageFormFragment.this
							.getActivity()).dismissProgressDialog();
					HyjUtil.displayToast(R.string.friendListFragment_addFriend_error_exists);
				} else {
					HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
						@Override
						public void finishCallback(Object object) {
							JSONArray jsonArray = (JSONArray) object;
							if (jsonArray.length() < 2
									|| jsonArray.optJSONArray(1).length() < 1) {
								((HyjActivity) FriendMessageFormFragment.this
										.getActivity()).dismissProgressDialog();
								HyjUtil.displayToast(R.string.friendAddRequestMessageFormFragment_toast_cannot_get_user);
								return;
							}
							JSONObject jsonUser = null;
							try {
								jsonUser = jsonArray.optJSONArray(1)
										.getJSONObject(0);
							} catch (JSONException e) {
							}

							if (jsonArray.optJSONArray(0).length() > 0) {
								// 好友已经在服务器上存在，如果该好友不在本地（可能是未同步，或是同步出错？），我们将其加进来
								JSONObject jsonFriend = jsonArray.optJSONArray(
										0).optJSONObject(0);
								loadFriendPicturesAndSaveFriend(jsonUser,
										jsonFriend);
//							} else if (jsonUser.optString("id").equals(
//									HyjApplication.getInstance()
//											.getCurrentUser().getId())) {
//								// 添加自己为好友
//								addSelfAsFriend(jsonUser);
//								((HyjActivity) FriendAddMessageFormFragment.this
//										.getActivity()).dismissProgressDialog();

							} else if (jsonUser.optString(
									"newFriendAuthentication").equals("non")) {

								addFriendWithoutAuthorization(jsonUser);

							} else {
								sendAddFriendRequestMessage(jsonUser);
							}
						}

						@Override
						public void errorCallback(Object object) {
							displayError(object);
						}
					};

					try {
						JSONObject data = new JSONObject();
						data.put("__dataType", "Friend");
						data.put("friendUserId", mMessageEditor.getModelCopy()
								.getToUserId());
						data.put("ownerUserId", HyjApplication.getInstance()
								.getCurrentUser().getId());
						JSONObject dataUser = new JSONObject();
						dataUser.put("__dataType", "User");
						dataUser.put("id", mMessageEditor.getModelCopy()
								.getToUserId());
						HyjHttpPostAsyncTask.newInstance(serverCallbacks, "["
								+ data.toString() + "," + dataUser.toString()
								+ "]", "findDataFilter");
						((HyjActivity) this.getActivity())
								.displayProgressDialog(
										R.string.addFriendListFragment_title_add,
										R.string.friendListFragment_addFriend_progress_adding);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			Friend newFriend = new Select().from(Friend.class).where("friendUserId=?",
					mMessageEditor.getModelCopy().getFromUserId()).executeSingle();
			if (newFriend != null) {
				((HyjActivity) FriendMessageFormFragment.this.getActivity())
						.dismissProgressDialog();
				HyjUtil.displayToast(R.string.friendListFragment_addFriend_error_exists);
			} else {
				HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
					@Override
					public void finishCallback(Object object) {
						JSONArray jsonArray = (JSONArray) object;
						if (jsonArray.length() < 2
								|| jsonArray.optJSONArray(1).length() < 1) {
							((HyjActivity) FriendMessageFormFragment.this
									.getActivity()).dismissProgressDialog();
							HyjUtil.displayToast(R.string.friendAddRequestMessageFormFragment_toast_cannot_get_user);
							return;
						}
						JSONObject jsonUser = null;
						try {
							jsonUser = jsonArray.optJSONArray(1).getJSONObject(
									0);
						} catch (JSONException e) {
						}

						if (jsonArray.optJSONArray(0).length() > 0) {
							// 好友已经在服务器上存在，如果该好友不在本地（可能是未同步，或是同步出错？），我们将其加进来
							JSONObject jsonFriend = jsonArray.optJSONArray(0)
									.optJSONObject(0);
							loadFriendPicturesAndSaveFriend(jsonUser,
									jsonFriend);
						} else {
							sendAddFriendResponseMessage(jsonUser);
						}
					}

					@Override
					public void errorCallback(Object object) {
						displayError(object);
					}
				};

				try {
					JSONObject data = new JSONObject();
					data.put("__dataType", "Friend");
					data.put("friendUserId", mMessageEditor.getModelCopy()
							.getFromUserId());
					data.put("ownerUserId", HyjApplication.getInstance()
							.getCurrentUser().getId());
					JSONObject dataUser = new JSONObject();
					dataUser.put("__dataType", "User");
					dataUser.put("id", mMessageEditor.getModelCopy()
							.getFromUserId());
					HyjHttpPostAsyncTask.newInstance(serverCallbacks, "["
							+ data.toString() + "," + dataUser.toString()
							+ "]", "findDataFilter");
					((HyjActivity) this.getActivity())
							.displayProgressDialog(
									R.string.addFriendListFragment_title_add,
									R.string.friendListFragment_addFriend_progress_adding);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void addFriendWithoutAuthorization(final JSONObject jsonUser) {
		// send message to server to request add new friend
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				loadNewlyAddedFriend(jsonUser);
			}

			@Override
			public void errorCallback(Object object) {
				displayError(object);
			}
		};

		JSONObject data = new JSONObject();
		try {
			data.put("__dataType", "Message");
			data.put("id", UUID.randomUUID().toString());
			data.put("toUserId", jsonUser.optString("id"));
			data.put("fromUserId", HyjApplication.getInstance()
					.getCurrentUser().getId());
			data.put("type", "System.Friend.AddResponse");
			data.put("messageState", "new");
			data.put("messageTitle", "好友请求");
			data.put("date", HyjUtil.formatDateToIOS(new Date()));
			data.put("detail", "用户"
					+ HyjApplication.getInstance().getCurrentUser()
							.getDisplayName() + "成功添加您为好友");
			data.put("messageBoxId", jsonUser.optString("messageBoxId"));
			data.put("ownerUserId", jsonUser.optString("id"));
			JSONObject msgData = new JSONObject();
			try {
				msgData.put("fromUserDisplayName", HyjApplication.getInstance()
						.getCurrentUser().getDisplayName());
				msgData.put(
						"toUserDisplayName",
						HyjUtil.ifJSONNull(jsonUser, "nickName",
								jsonUser.getString("userName")));
			} catch (JSONException e) {
			}
			data.put("messageData", msgData);
			HyjHttpPostAsyncTask.newInstance(serverCallbacks,
					"[" + data.toString() + "]", "postData");

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

				((HyjActivity) FriendMessageFormFragment.this.getActivity())
						.dismissProgressDialog();
				HyjUtil.displayToast(R.string.friendListFragment_addFriend_progress_add_success);
//				HyjUtil.displayToast(R.string.friendAddRequestMessageFormFragment_toast_accept_success);
				FriendMessageFormFragment.this.getActivity().finish();
			}

			@Override
			public void errorCallback(Object object) {
				displayError(object);
			}
		};

		HyjHttpPostAsyncTask.newInstance(serverCallbacks,
				jsonUser.optString("id"), "fetchRecordPictures");
	}

	private void loadNewlyAddedFriend(final JSONObject jsonUser) {
		// load new friend from server
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				try {
					JSONObject jsonFriend;
					jsonFriend = ((JSONArray) object).getJSONArray(0)
							.getJSONObject(0);
					loadFriendPicturesAndSaveFriend(jsonUser, jsonFriend);
				} catch (JSONException e) {
					e.printStackTrace();
					((HyjActivity) FriendMessageFormFragment.this
							.getActivity()).dismissProgressDialog();
				}
			}

			@Override
			public void errorCallback(Object object) {
				displayError(object);
			}
		};

		JSONObject data = new JSONObject();
		try {
			data.put("__dataType", "Friend");
			data.put("ownerUserId", HyjApplication.getInstance()
					.getCurrentUser().getId());
			data.put("friendUserId", jsonUser.optString("id"));
			HyjHttpPostAsyncTask.newInstance(serverCallbacks,
					"[" + data.toString() + "]", "getData");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void sendAddFriendRequestMessage(JSONObject jsonUser) {
		final Message msg = new Message();
		msg.setDate(HyjUtil.formatDateToIOS(new Date()));
		msg.setMessageState("new");
		msg.setType("System.Friend.AddRequest");
		msg.setOwnerUserId(jsonUser.optString("id"));
		msg.setFromUserId(HyjApplication.getInstance().getCurrentUser().getId());
		msg.setToUserId(jsonUser.optString("id"));
		msg.setMessageTitle("好友请求");
//		msg.setMessageDetail("用户"
//				+ HyjApplication.getInstance().getCurrentUser()
//						.getDisplayName() + "请求将您添加为好友");
		msg.setMessageDetail(mMessageEditor.getModelCopy().getMessageDetail());
		msg.setMessageBoxId(jsonUser.optString("messageBoxId"));
		JSONObject msgData = new JSONObject();
		try {
			msgData.put("fromUserDisplayName", HyjApplication.getInstance()
					.getCurrentUser().getDisplayName());
			msgData.put(
					"toUserDisplayName",
					HyjUtil.ifJSONNull(jsonUser, "nickName",
							jsonUser.getString("userName")));
		} catch (JSONException e) {
		}
		msg.setMessageData(msgData.toString());

		// send message to server to request add new friend
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
//				msg.save();
				((HyjActivity) FriendMessageFormFragment.this.getActivity())
						.dismissProgressDialog();
				HyjUtil.displayToast(R.string.friendAddRequestMessageFormFragment_toast_resend_success);
			}

			@Override
			public void errorCallback(Object object) {
				displayError(object);
			}
		};

		HyjHttpPostAsyncTask.newInstance(serverCallbacks, "["
				+ msg.toJSON().toString() + "]", "postData");
		((HyjActivity) this.getActivity()).displayProgressDialog(
				R.string.addFriendListFragment_title_add,
				R.string.friendListFragment_addFriend_progress_adding);

	}

	private void sendAddFriendResponseMessage(final JSONObject jsonUser) {
		final Message msg = new Message();
		msg.setDate(HyjUtil.formatDateToIOS(new Date()));
		msg.setMessageState("new");
		msg.setType("System.Friend.AddResponse");
		msg.setOwnerUserId(jsonUser.optString("id"));
		msg.setFromUserId(HyjApplication.getInstance().getCurrentUser().getId());
		msg.setToUserId(jsonUser.optString("id"));
		msg.setMessageTitle("接受添加好友");
		msg.setMessageDetail("用户"
				+ HyjApplication.getInstance().getCurrentUser()
						.getDisplayName() + "同意您的添加好友请求");
		msg.setMessageBoxId(jsonUser.optString("messageBoxId"));
		JSONObject msgData = new JSONObject();
		try {
			msgData.put("fromUserDisplayName", HyjApplication.getInstance()
					.getCurrentUser().getDisplayName());
			msgData.put(
					"toUserDisplayName",
					HyjUtil.ifJSONNull(jsonUser, "nickName",
							jsonUser.getString("userName")));
		} catch (JSONException e) {
		}
		msg.setMessageData(msgData.toString());

		// send message to server to request add new friend
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
//				((HyjActivity) FriendAddMessageFormFragment.this.getActivity())
//						.dismissProgressDialog();
				msg.save();
				loadNewlyAddedFriend(jsonUser);
//				HyjUtil.displayToast(R.string.friendAddRequestMessageFormFragment_toast_accept_success);
			}

			@Override
			public void errorCallback(Object object) {
				displayError(object);
			}
		};

		HyjHttpPostAsyncTask.newInstance(serverCallbacks, "["
				+ msg.toJSON().toString() + "]", "postData");

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

	private void displayError(Object object) {
		((HyjActivity) FriendMessageFormFragment.this.getActivity())
				.dismissProgressDialog();
		JSONObject json = (JSONObject) object;
		HyjUtil.displayToast(json.optJSONObject("__summary").optString("msg"));
	}
}
