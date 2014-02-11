package com.hoyoji.hoyoji.models;

import java.util.List;
import java.util.UUID;

import android.provider.BaseColumns;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.hoyoji.R;

@Table(name = "MoneyExpense", id = BaseColumns._ID)
public class MoneyExpense extends HyjModel{

	@Column(name = "id", index = true, unique = true)
	private String mId;
	
	@Column(name = "pictureId")
	private String mPictureId;
	
	@Column(name = "date")
	private String mDate;

	@Column(name = "amount")
	private Double mAmount;
	
	@Column(name = "expenseType")
	private String mExpenseType;

	@Column(name = "friendUserId")
	private String mFriendUserId;

	@Column(name = "localFriendId")
	private String mLocalFriendId;

	@Column(name = "friendAccountId")
	private String mFriendAccountId;

	@Column(name = "moneyAccountId")
	private String mMoneyAccountId;
	
	@Column(name = "projectId")
	private String mProjectId;
	
	@Column(name = "moneyExpenseCategory")
	private String mMoneyExpenseCategory;
	
	@Column(name = "exchangeRate")
	private Double mExchangeRate;

	@Column(name = "remark")
	private String mRemark;
	
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
	
	@Override
	public void validate(HyjModelEditor hyjModelEditor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	public String getmPictureId() {
		return mPictureId;
	}

	public void setmPictureId(String mPictureId) {
		this.mPictureId = mPictureId;
	}

	public String getmDate() {
		return mDate;
	}

	public void setmDate(String mDate) {
		this.mDate = mDate;
	}

	public Double getmAmount() {
		return mAmount;
	}

	public void setmAmount(Double mAmount) {
		this.mAmount = mAmount;
	}
	
	public String getmExpenseType() {
		return mExpenseType;
	}

	public void setmExpenseType(String mExpenseType) {
		this.mExpenseType = mExpenseType;
	}

	public String getmFriendUserId() {
		return mFriendUserId;
	}

	public void setmFriendUserId(String mFriendUserId) {
		this.mFriendUserId = mFriendUserId;
	}

	public String getmLocalFriendId() {
		return mLocalFriendId;
	}

	public void setmLocalFriendId(String mLocalFriendId) {
		this.mLocalFriendId = mLocalFriendId;
	}

	public String getmFriendAccountId() {
		return mFriendAccountId;
	}

	public void setmFriendAccountId(String mFriendAccountId) {
		this.mFriendAccountId = mFriendAccountId;
	}

	public String getmMoneyAccountId() {
		return mMoneyAccountId;
	}

	public void setmMoneyAccountId(String mMoneyAccountId) {
		this.mMoneyAccountId = mMoneyAccountId;
	}

	public String getmProjectId() {
		return mProjectId;
	}

	public void setmProjectId(String mProjectId) {
		this.mProjectId = mProjectId;
	}

	public String getmMoneyExpenseCategory() {
		return mMoneyExpenseCategory;
	}

	public void setmMoneyExpenseCategory(String mMoneyExpenseCategory) {
		this.mMoneyExpenseCategory = mMoneyExpenseCategory;
	}

	public Double getmExchangeRate() {
		return mExchangeRate;
	}

	public void setmExchangeRate(Double mExchangeRate) {
		this.mExchangeRate = mExchangeRate;
	}

	public String getmRemark() {
		return mRemark;
	}

	public void setmRemark(String mRemark) {
		this.mRemark = mRemark;
	}

	public String getmServerRecordHash() {
		return mServerRecordHash;
	}

	public void setmServerRecordHash(String mServerRecordHash) {
		this.mServerRecordHash = mServerRecordHash;
	}

	public String getmLastServerUpdateTime() {
		return mLastServerUpdateTime;
	}

	public void setmLastServerUpdateTime(String mLastServerUpdateTime) {
		this.mLastServerUpdateTime = mLastServerUpdateTime;
	}

	public String getmLastClientUpdateTime() {
		return mLastClientUpdateTime;
	}

	public void setmLastClientUpdateTime(String mLastClientUpdateTime) {
		this.mLastClientUpdateTime = mLastClientUpdateTime;
	}

	public String getmLastSyncTime() {
		return mLastSyncTime;
	}

	public void setmLastSyncTime(String mLastSyncTime) {
		this.mLastSyncTime = mLastSyncTime;
	}

	public String getmOwnerUserId() {
		return mOwnerUserId;
	}

	public void setmOwnerUserId(String mOwnerUserId) {
		this.mOwnerUserId = mOwnerUserId;
	}

	public String getmLocation() {
		return mLocation;
	}

	public void setmLocation(String mLocation) {
		this.mLocation = mLocation;
	}

	public String getmGeoLon() {
		return mGeoLon;
	}

	public void setmGeoLon(String mGeoLon) {
		this.mGeoLon = mGeoLon;
	}

	public String getmGeoLat() {
		return mGeoLat;
	}

	public void setmGeoLat(String mGeoLat) {
		this.mGeoLat = mGeoLat;
	}

	public String getmAddress() {
		return mAddress;
	}

	public void setmAddress(String mAddress) {
		this.mAddress = mAddress;
	}

}
