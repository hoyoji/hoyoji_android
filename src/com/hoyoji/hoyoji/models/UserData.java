package com.hoyoji.hoyoji.models;

import java.util.UUID;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.hoyoji.R;

import android.provider.BaseColumns;

@Table(name = "UserData", id = BaseColumns._ID)
public class UserData extends HyjModel {
	
	@Column(name = "id", index = true, unique = true)
	private String mId;

	@Column(name = "userId", index = true, unique = true)
	private String mUserId;
	
	@Column(name = "password")
	private String mPassword;
	
	@Column(name = "email")
	private String mEmail;
	
	@Column(name = "emailVerified")
	private boolean mEmailVerified;

	@Column(name = "phone")
	private String mPhone;
	
	@Column(name = "phoneVerified")
	private boolean mPhoneVerified;
	
	@Column(name = "activeProjectId")
	private String mActiveProjectId;

	@Column(name = "activeCurrencyId")
	private String mActiveCurrencyId;

	@Column(name = "activeMoneyAccountId")
	private String mActiveMoneyAccountId;

	@Column(name = "defaultFriendCategory")
	private String mDefaultFriendCategory;
	
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
	
	
	public UserData(){
		super();
		mId = UUID.randomUUID().toString();
	}

	public User getUser(){
		return (User) getModel(User.class, mUserId);
	}
	
	public void setUser(User user){
		mUserId = user.getId();
	}

	@Override
	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	public String getUserId() {
		return mUserId;
	}

	public void setUserId(String mUserId) {
		this.mUserId = mUserId;
	}

	public String getPassword() {
		return mPassword;
	}

	public void setPassword(String mPassword) {
		this.mPassword = mPassword;
	}

	public String getEmail() {
		return mEmail;
	}

	public void setEmail(String mEmail) {
		this.mEmail = mEmail;
	}

	public boolean ismEmailVerified() {
		return mEmailVerified;
	}

	public void setEmailVerified(boolean mEmailVerified) {
		this.mEmailVerified = mEmailVerified;
	}

	public String getPhone() {
		return mPhone;
	}

	public void setPhone(String mPhone) {
		this.mPhone = mPhone;
	}

	public boolean ismPhoneVerified() {
		return mPhoneVerified;
	}

	public void setPhoneVerified(boolean mPhoneVerified) {
		this.mPhoneVerified = mPhoneVerified;
	}

	public String getActiveProjectId() {
		return mActiveProjectId;
	}

	public void setActiveProjectId(String mActiveProjectId) {
		this.mActiveProjectId = mActiveProjectId;
	}

	public String getActiveCurrencyId() {
		return mActiveCurrencyId;
	}

	public void setActiveCurrencyId(String mActiveCurrencyId) {
		this.mActiveCurrencyId = mActiveCurrencyId;
	}

	public String getActiveMoneyAccountId() {
		return mActiveMoneyAccountId;
	}

	public void setActiveMoneyAccountId(String mActiveMoneyAccountId) {
		this.mActiveMoneyAccountId = mActiveMoneyAccountId;
	}

	public String getDefaultFriendCategory() {
		return mDefaultFriendCategory;
	}

	public void setDefaultFriendCategory(String mDefaultFriendCategory) {
		this.mDefaultFriendCategory = mDefaultFriendCategory;
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
		if(this.getPassword().length() == 0){
			modelEditor.setValidationError("password", R.string.registerActivity_editText_hint_password1);
		} else if(this.getPassword().length() < 6){
			modelEditor.setValidationError("password", R.string.registerActivity_validation_password_too_short);
		} else {
			modelEditor.removeValidationError("password");
		}
		
	}

	
}
