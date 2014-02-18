package com.hoyoji.hoyoji.models;

import java.util.List;
import java.util.UUID;

import android.provider.BaseColumns;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.hoyoji.R;

@Table(name = "MoneyTransfer", id = BaseColumns._ID)
public class MoneyTransfer extends HyjModel{
     
	@Column(name = "id", index = true, unique = true)
	private String mId;
	
	@Column(name = "pictureId")
	private String mPictureId;
	
	@Column(name = "date")
	private String mDate;

	@Column(name = "transferOutAmount")
	private Double mTransferOutAmount;
	
	@Column(name = "transferOutId")
	private String mTransferOutId;
	
	@Column(name = "transferInAmount")
	private Double mTransferInAmount;
	
	@Column(name = "transferInId")
	private String mTransferInId;
	
	@Column(name = "exchangeRate")
	private Double mExchangeRate;
	
	@Column(name = "projectId")
	private String mProjectId;
	
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
	
	public MoneyTransfer(){
		super();
		UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
		mId = UUID.randomUUID().toString();
		mTransferOutId = userData.getActiveMoneyAccountId();
		mTransferInId = userData.getActiveMoneyAccountId();
		mProjectId = userData.getActiveProjectId();
		mExchangeRate = 1.00;
	}
	
	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	public String getPictureId() {
		return mPictureId;
	}

	public void setPictureId(String mPictureId) {
		this.mPictureId = mPictureId;
	}

	public Picture getPicture(){
		if(mPictureId == null){
			return null;
		}
		return (Picture) getModel(Picture.class, mPictureId);
	}

	public void setPicture(Picture picture){
		this.setPictureId(picture.getId());
	}
	
	public List<Picture> getPictures(){
		return getMany(Picture.class, "recordId");
	}
	
	public String getDate() {
		return mDate;
	}

	public void setDate(String mDate) {
		this.mDate = mDate;
	}

	public Double getTransferOutAmount() {
		return mTransferOutAmount;
	}

	public void setTransferOutAmount(Double mTransferOutAmount) {
		this.mTransferOutAmount = mTransferOutAmount;
	}

	public String getTransferOutId() {
		return mTransferOutId;
	}

	public void setTransferOutId(String mTransferOutId) {
		this.mTransferOutId = mTransferOutId;
	}
	
	public MoneyAccount getTransferOut(){
		if(mTransferOutId == null){
			return null;
		}
		return (MoneyAccount) getModel(MoneyAccount.class, mTransferOutId);
	}
	
	public void setTransferOut(MoneyAccount mTransferOut) {
		this.mTransferOutId = mTransferOut.getId();
	}

	public Double getTransferInAmount() {
		return mTransferInAmount;
	}

	public void setTransferInAmount(Double mTransferInAmount) {
		this.mTransferInAmount = mTransferInAmount;
	}

	public String getTransferInId() {
		return mTransferInId;
	}

	public void setTransferInId(String mTransferInId) {
		this.mTransferInId = mTransferInId;
	}
	
	public MoneyAccount getTransferIn(){
		if(mTransferInId == null){
			return null;
		}
		return (MoneyAccount) getModel(MoneyAccount.class, mTransferInId);
	}
	
	public void setTransferIn(MoneyAccount mTransferIn) {
		this.mTransferInId = mTransferIn.getId();
	}

	public Double getExchangeRate() {
		return mExchangeRate;
	}

	public void setExchangeRate(Double mExchangeRate) {
		this.mExchangeRate = mExchangeRate;
	}

	public String getProjectId() {
		return mProjectId;
	}

	public void setProjectId(String mProjectId) {
		this.mProjectId = mProjectId;
	}

	public String getRemark() {
		return mRemark;
	}

	public void setRemark(String mRemark) {
		this.mRemark = mRemark;
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
//		if(this.getDate() == null){
//			modelEditor.setValidationError("date",R.string.moneyTransferFormFragment_editText_hint_date);
//		}else{
//			modelEditor.removeValidationError("date");
//		}
		
	}

}
