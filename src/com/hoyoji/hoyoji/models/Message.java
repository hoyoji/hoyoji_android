package com.hoyoji.hoyoji.models;

import java.util.List;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.provider.BaseColumns;
import android.widget.TextView;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.hoyoji.R;

@Table(name = "Message", id = BaseColumns._ID)
public class Message extends HyjModel {

	@Column(name = "id", index = true, unique = true)
	private String mId;

	@Column(name = "date")
	private String mDate;
	
	@Column(name = "messageState")
	private String mMessageState;

	@Column(name = "ownerUserId")
	private String mOwnerUserId;

	@Column(name = "fromUserId")
	private String mFromUserId;

	@Column(name = "toUserId")
	private String mToUserId;

	@Column(name = "messageTitle")
	private String mMessageTitle;

	@Column(name = "messageData")
	private String mMessageData;
	
	@Column(name = "detail")
	private String mMessageDetail;

	@Column(name = "type")
	private String mMessageType;

	@Column(name = "messageBoxId")
	private String mMessageBoxId;
	
	public Message(){
		super();
		mId = UUID.randomUUID().toString();
	}
	
	@Override
	public void validate(HyjModelEditor modelEditor) {
	}

	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String mDate) {
		this.mDate = mDate;
	}

	public String getOwnerUserId() {
		return mOwnerUserId;
	}

	public void setOwnerUserId(String mOwnerUserId) {
		this.mOwnerUserId = mOwnerUserId;
	}

	public String getMessageState() {
		return mMessageState;
	}

	public void setMessageState(String mMessageState) {
		this.mMessageState = mMessageState;
	}

	public String getFromUserId() {
		return mFromUserId;
	}

	public void setFromUserId(String mFromUserId) {
		this.mFromUserId = mFromUserId;
	}

	public String getToUserId() {
		return mToUserId;
	}

	public void setToUserId(String mToUserId) {
		this.mToUserId = mToUserId;
	}

	private String getUserDisplayName(String friendUserId, String fromOrToUserDisplayName){
		if(friendUserId.equals(HyjApplication.getInstance().getCurrentUser().getId())){
			return HyjApplication.getInstance().getApplicationContext().getString(R.string.messageListItem_user_self);
			}
		Friend friend = new Select().from(Friend.class).where("friendUserId=?", friendUserId).executeSingle();
		if(friend != null){
			return friend.getDisplayName();
		} else {
			User user = HyjModel.getModel(User.class, friendUserId);
			if(user != null){
				return user.getDisplayName();
			} else {
				try {
					JSONObject dataObject = new JSONObject(this.getMessageData());
					return dataObject.getString(fromOrToUserDisplayName);
				} catch (Exception e) {
					return HyjApplication.getInstance().getApplicationContext().getString(R.string.messageListItem_fromUser_not_friend);
				}
			}
		}
	}
	
	public String getToUserDisplayName(){
		String friendUserId = this.getToUserId();
		return getUserDisplayName(friendUserId, "toUserDisplayName");
	}
	
	public String getFromUserDisplayName(){
		String friendUserId = this.getFromUserId();
		return getUserDisplayName(friendUserId, "fromUserDisplayName");
	}
	
	public String getMessageTitle() {
		return mMessageTitle;
	}

	public void setMessageTitle(String mMessageTitle) {
		this.mMessageTitle = mMessageTitle;
	}

	public String getMessageData() {
		return mMessageData;
	}

	public void setMessageData(String mMessageData) {
		this.mMessageData = mMessageData;
	}

	public String getMessageDetail() {
		return mMessageDetail;
	}

	public void setMessageDetail(String mDetail) {
		this.mMessageDetail = mDetail;
	}

	public String getType() {
		return mMessageType;
	}

	public void setType(String mType) {
		this.mMessageType = mType;
	}

	public String getMessageBoxId() {
		return mMessageBoxId;
	}

	public void setMessageBoxId(String mMessageBoxId) {
		this.mMessageBoxId = mMessageBoxId;
	}


	@Override
	public void save(){
		if(this.getOwnerUserId() == null){
			this.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
		}
		super.save();
	}
}
