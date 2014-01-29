package com.hoyoji.hoyoji.models;

import java.util.UUID;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.hoyoji.R;

import android.provider.BaseColumns;

@Table(name = "Friend", id = BaseColumns._ID)
public class Friend extends HyjModel {
	
	@Column(name = "id", index = true, unique = true)
	private String mId;
	
	@Column(name = "nickName")
	private String mNickName;
	
	@Column(name = "friendUserId")
	private String mFriendUserId;
	
	@Column(name = "friendCategoryId")
	private String mFriendCategoryId;

	@Column(name = "serverRecordHash")
	private String mServerRecordHash;

	@Column(name = "lastServerUpdateTime")
	private String mLastServerUpdateTime;

	@Column(name = "lastClientUpdateTime")
	private String mLastClientUpdateTime;

	@Column(name = "lastSyncTime")
	private String mLastSyncTime;

	@Column(name = "ownerUserId")
	private String mOwnerUserId;

	@Column(name = "location")
	private String mLocation;
	
	@Column(name = "geoLon")
	private String mGeoLon;
	
	@Column(name = "geoLat")
	private String mGeoLat;
	
	@Column(name = "address")
	private String mAddress;
	
	
	public Friend(){
		super();
		mId = UUID.randomUUID().toString();
	}

	public User getFriendUser(){
		return (User) getModel(User.class, mFriendUserId);
	}
	
	public void setFriendUser(User user){
		mFriendUserId = user.getId();
	}

	@Override
	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}
	
	public String getNickName() {
		return mNickName;
	}

	public void setNickName(String nickName) {
		this.mNickName = nickName;
	}
	
	public String getFriendUserId() {
		return mFriendUserId;
	}

	public void setFriendUserId(String mFriendUserId) {
		this.mFriendUserId = mFriendUserId;
	}

	public FriendCategory getFriendCategory() {
		if(mFriendCategoryId == null){
			return null;
		}
		return (FriendCategory) getModel(FriendCategory.class, mFriendCategoryId);
	}

	public void setFriendCategory(FriendCategory mFriendCategory) {
		this.mFriendCategoryId = mFriendCategory.getId();
	}
	
	public String getFriendCategoryId() {
		return mFriendCategoryId;
	}

	public void setFriendCategoryId(String mFriendCategoryId) {
		this.mFriendCategoryId = mFriendCategoryId;
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

	public String getLastSyncTime() {
		return mLastSyncTime;
	}

	public void setLastSyncTime(String mLastSyncTime) {
		this.mLastSyncTime = mLastSyncTime;
	}

	public String getOwnerUserId() {
		return mOwnerUserId;
	}

	public void setOwnerUserId(String mOwnerUserId) {
		this.mOwnerUserId = mOwnerUserId;
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
		if(this.getFriendCategoryId() == null){
			modelEditor.setValidationError("friendCategory", R.string.friendFormFragment_editText_hint_friend_category);
		} else {
			modelEditor.removeValidationError("friendCategory");
		}
		if(this.getNickName().length() == 0){
			modelEditor.setValidationError("nickName", R.string.friendFormFragment_editText_hint_nickName);
		} else {
			modelEditor.removeValidationError("nickName");
		}		
	}

}
