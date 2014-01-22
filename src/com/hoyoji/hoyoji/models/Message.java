package com.hoyoji.hoyoji.models;

import java.util.List;
import java.util.UUID;

import android.provider.BaseColumns;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.hoyoji.R;

@Table(name = "Project", id = BaseColumns._ID)
public class Message extends HyjModel {

	@Column(name = "id", index = true, unique = true)
	private String mId;

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

	@Column(name = "type")
	private String mType;

	@Column(name = "messageBoxId")
	private String mMessageBoxId;

	@Column(name = "serverRecordHash")
	private String mServerRecordHash;

	@Column(name = "lastServerUpdateTime")
	private String mLastServerUpdateTime;

	@Column(name = "lastClientUpdateTime")
	private String mLastClientUpdateTime;
	
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
	
	public String getOwnerUserId() {
		return mOwnerUserId;
	}

	public void setOwnerUserId(String mOwnerUserId) {
		this.mOwnerUserId = mOwnerUserId;
	}

	public String getmMessageState() {
		return mMessageState;
	}

	public void setmMessageState(String mMessageState) {
		this.mMessageState = mMessageState;
	}

	public String getmFromUserId() {
		return mFromUserId;
	}

	public void setmFromUserId(String mFromUserId) {
		this.mFromUserId = mFromUserId;
	}

	public String getmToUserId() {
		return mToUserId;
	}

	public void setmToUserId(String mToUserId) {
		this.mToUserId = mToUserId;
	}

	public String getmMessageTitle() {
		return mMessageTitle;
	}

	public void setmMessageTitle(String mMessageTitle) {
		this.mMessageTitle = mMessageTitle;
	}

	public String getmMessageData() {
		return mMessageData;
	}

	public void setmMessageData(String mMessageData) {
		this.mMessageData = mMessageData;
	}

	public String getmType() {
		return mType;
	}

	public void setmType(String mType) {
		this.mType = mType;
	}

	public String getmMessageBoxId() {
		return mMessageBoxId;
	}

	public void setmMessageBoxId(String mMessageBoxId) {
		this.mMessageBoxId = mMessageBoxId;
	}

	public String getServerRecordHash() {
		return mServerRecordHash;
	}

	public void setServerRecordHash(String mServerRecordHash) {
		this.mServerRecordHash = mServerRecordHash;
	}

	public String getLastServerUpdateTime() {
		return mLastServerUpdateTime;
	}

	public void setLastServerUpdateTime(String mLastServerUpdateTime) {
		this.mLastServerUpdateTime = mLastServerUpdateTime;
	}

	public String getLastClientUpdateTime() {
		return mLastClientUpdateTime;
	}

	public void setLastClientUpdateTime(String mLastClientUpdateTime) {
		this.mLastClientUpdateTime = mLastClientUpdateTime;
	}
	
}
