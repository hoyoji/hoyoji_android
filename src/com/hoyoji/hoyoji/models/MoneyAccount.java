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
public class MoneyAccount extends HyjModel {

	@Column(name = "id", index = true, unique = true)
	private String mId;

	@Column(name = "name")
	private String mName;

	@Column(name = "ownerUserId")
	private String mOwnerUserId;

	@Column(name = "currencyId")
	private String mCurrencyId;

	@Column(name = "currencyBalance")
	private String mCurrencyBalance;

	@Column(name = "remark")
	private String mRemark;

	@Column(name = "sharingType")
	private String mSharingType;

	@Column(name = "accountType")
	private String mAccountType;

	@Column(name = "accountNumber")
	private String mAccountNumber;
	
	@Column(name = "bankAddress")
	private String mBankAddress;

	@Column(name = "friendId")
	private String mFriendId;

	@Column(name = "serverRecordHash")
	private String mServerRecordHash;

	@Column(name = "lastServerUpdateTime")
	private String mLastServerUpdateTime;

	@Column(name = "lastClientUpdateTime")
	private String mLastClientUpdateTime;
	
	public MoneyAccount(){
		super();
		mId = UUID.randomUUID().toString();
	}
	
	@Override
	public void validate(HyjModelEditor modelEditor) {
		if(this.getName().length() == 0){
			modelEditor.setValidationError("name", R.string.projectFormFragment_editText_hint_projectName);
		} else {
			modelEditor.removeValidationError("name");
		}	
	}

	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}
	
	public String getOwnerUserId() {
		return mOwnerUserId;
	}

	public void setOwnerUserId(String mOwnerUserId) {
		this.mOwnerUserId = mOwnerUserId;
	}

	public String getCurrencyId() {
		return mCurrencyId;
	}

	public void setCurrencyId(String mCurrencyId) {
		this.mCurrencyId = mCurrencyId;
	}

	public String getCurrencyBalance() {
		return mCurrencyBalance;
	}

	public void setCurrencyBalance(String mCurrencyBalance) {
		this.mCurrencyBalance = mCurrencyBalance;
	}

	public String getRemark() {
		return mRemark;
	}

	public void setmRemark(String mRemark) {
		this.mRemark = mRemark;
	}

	public String getSharingType() {
		return mSharingType;
	}

	public void setSharingType(String mSharingType) {
		this.mSharingType = mSharingType;
	}

	public String getAccountType() {
		return mAccountType;
	}

	public void setAccountType(String mAccountType) {
		this.mAccountType = mAccountType;
	}

	public String getAccountNumber() {
		return mAccountNumber;
	}

	public void setAccountNumber(String mAccountNumber) {
		this.mAccountNumber = mAccountNumber;
	}

	public String getBankAddress() {
		return mBankAddress;
	}

	public void setBankAddress(String mBankAddress) {
		this.mBankAddress = mBankAddress;
	}

	public String getFriendId() {
		return mFriendId;
	}

	public void setFriendId(String mFriendId) {
		this.mFriendId = mFriendId;
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
