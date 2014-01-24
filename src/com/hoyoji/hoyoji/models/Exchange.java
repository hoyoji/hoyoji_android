package com.hoyoji.hoyoji.models;

import java.util.List;
import java.util.UUID;

import android.provider.BaseColumns;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.hoyoji.R;

@Table(name = "Exchange", id = BaseColumns._ID)
public class Exchange extends HyjModel {

	@Column(name = "id", index = true, unique = true)
	private String mId;

	@Column(name = "rate")
	private String mRate;

	@Column(name = "ownerUserId")
	private String mOwnerUserId;

	@Column(name = "localCurrencyId")
	private String mLocalCurrencyId;

	@Column(name = "foreignCurrencyId")
	private String mForeignCurrencyId;
	
	@Column(name = "autoUpdate")
	private String mAutoUpdate;

	@Column(name = "serverRecordHash")
	private String mServerRecordHash;

	@Column(name = "lastServerUpdateTime")
	private String mLastServerUpdateTime;

	@Column(name = "lastClientUpdateTime")
	private String mLastClientUpdateTime;
	
	public Exchange(){
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

	public String getRate() {
		return mRate;
	}

	public void setRate(String mRate) {
		this.mRate = mRate;
	}

	public String getLocalCurrencyId() {
		return mLocalCurrencyId;
	}

	public void setLocalCurrencyId(String mLocalCurrencyId) {
		this.mLocalCurrencyId = mLocalCurrencyId;
	}

	public String getForeignCurrencyId() {
		return mForeignCurrencyId;
	}

	public void setForeignCurrencyId(String mForeignCurrencyId) {
		this.mForeignCurrencyId = mForeignCurrencyId;
	}

	public String getAutoUpdate() {
		return mAutoUpdate;
	}

	public void setAutoUpdate(String mAutoUpdate) {
		this.mAutoUpdate = mAutoUpdate;
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
