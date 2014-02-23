package com.hoyoji.hoyoji.models;

import java.util.UUID;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.hoyoji.R;

import android.provider.BaseColumns;

@Table(name = "User", id = BaseColumns._ID)
public class User extends HyjModel {
	@Column(name = "id", index = true, unique = true)
	private String mId;
	
	@Column(name = "userDataId", index = true, unique = true)
	private String mUserDataId;
	
	@Column(name = "userName", index = true, unique = true)
	private String mUserName;
	
	@Column(name = "nickName")
	private String mNickName;

	@Column(name = "isMerchant")
	private boolean mIsMerchant;
	
	@Column(name = "messageBoxId")
	private String mMessageBoxId;
	
	@Column(name = "newFriendAuthentication")
	private String mNewFriendAuthentication;
	
	@Column(name = "serverRecordHash")
	private String mServerRecordHash;
	
	@Column(name = "lastServerUpdateTime")
	private String mLastServerUpdateTime;

	@Column(name = "lastClientUpdateTime")
	private String mLastClientUpdateTime;
	
	@Column(name = "pictureId")
	private String mPictureId;

	@Column(name = "location")
	private String mLocation;
	
	@Column(name = "geoLon")
	private String mGeoLon;
	
	@Column(name = "geoLat")
	private String mGeoLat;
	
	@Column(name = "address")
	private String mAddress;
	
	public User(){
		super();
		mId = UUID.randomUUID().toString();
	}

	public UserData getUserData(){
		if(mUserDataId == null){
			return null;
		}
		return (UserData) getModel(UserData.class, mUserDataId);
	}

	public void setUserData(UserData userData){
		mUserDataId = userData.getId();
	}
	
	public boolean getIsMerchant(){
		return mIsMerchant;
	}

	@Override
	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	public String getUserDataId() {
		return mUserDataId;
	}

	public void setUserDataId(String mUserDataId) {
		this.mUserDataId = mUserDataId;
	}

	public String getUserName() {
		return mUserName;
	}

	public void setUserName(String mUserName) {
		this.mUserName = mUserName.trim();
	}

	public String getNickName() {
		return mNickName;
	}

	public void setNickName(String mNickName) {
		this.mNickName = mNickName;
	}

	public String getDisplayName() {
		if(this.getNickName() != null){
			return this.getNickName();
		}
		return this.getUserName();
	}
	
	public boolean ismIsMerchant() {
		return mIsMerchant;
	}

	public void setIsMerchant(boolean mIsMerchant) {
		this.mIsMerchant = mIsMerchant;
	}

	public String getMessageBoxId() {
		return mMessageBoxId;
	}

	public void setMessageBoxId(String mMessageBoxId) {
		this.mMessageBoxId = mMessageBoxId;
	}

	public String getNewFriendAuthentication() {
		return mNewFriendAuthentication;
	}

	public void setNewFriendAuthentication(String mNewFriendAuthentication) {
		this.mNewFriendAuthentication = mNewFriendAuthentication;
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

	public String getPictureId() {
		return mPictureId;
	}

	public void setPictureId(String mPictureId) {
		this.mPictureId = mPictureId;
	}

	public String getLocation() {
		return mLocation;
	}

	public void setLocation(String mLocation) {
		this.mLocation = mLocation;
	}

	public String getGeoLon() {
		return mGeoLon;
	}

	public void setGeoLon(String mGeoLon) {
		this.mGeoLon = mGeoLon;
	}

	public String getGeoLat() {
		return mGeoLat;
	}

	public void setGeoLat(String mGeoLat) {
		this.mGeoLat = mGeoLat;
	}

	public String getAddress() {
		return mAddress;
	}

	public void setAddress(String mAddress) {
		this.mAddress = mAddress;
	}

	@Override
	public void validate(HyjModelEditor modelEditor) {
		if(this.getUserName().length() == 0){
			modelEditor.setValidationError("userName", R.string.registerActivity_editText_hint_username);
		} else if(this.getUserName().length() < 3){
			modelEditor.setValidationError("userName", R.string.registerActivity_validation_username_too_short);
		} else {
			modelEditor.removeValidationError("userName");
		}		
	}	
	
	
}
