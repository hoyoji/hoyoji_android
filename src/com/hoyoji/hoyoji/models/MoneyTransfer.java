package com.hoyoji.hoyoji.models;

import java.util.List;
import java.util.UUID;

import android.provider.BaseColumns;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
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
	
	@Column(name = "transferOutFriendUserId")
	private String mTransferOutFriendUserId;
	
	@Column(name = "transferOutLocalFriendId")
	private String mTransferOutLocalFriendId;
	
	@Column(name = "transferOutId")
	private String mTransferOutId;
	
	@Column(name = "transferInAmount")
	private Double mTransferInAmount;
	
	@Column(name = "transferInFriendUserId")
	private String mTransferInFriendUserId;
	
	@Column(name = "transferInLocalFriendId")
	private String mTransferInLocalFriendId;
	
	@Column(name = "transferInId")
	private String mTransferInId;
	
	@Column(name = "exchangeRate")
	private Double mExchangeRate;
	
	@Column(name = "projectId")
	private String mProjectId;
	
	@Column(name = "remark")
	private String mRemark;

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
	
	public Double getTransferOutAmount0() {
		if(mTransferOutAmount == null){
			return 0.00;
		}
		return mTransferOutAmount;
	}

	public void setTransferOutAmount(Double mTransferOutAmount) {
		if(mTransferOutAmount != null) {
			mTransferOutAmount = HyjUtil.toFixed2(mTransferOutAmount);
		}
		this.mTransferOutAmount = mTransferOutAmount;
	}

	public String getTransferOutFriendUserId() {
		return mTransferOutFriendUserId;
	}

	public void setTransferOutFriendUserId(String mTransferOutFriendUserId) {
		this.mTransferOutFriendUserId = mTransferOutFriendUserId;
	}
	
	public String getTransferOutLocalFriendId() {
		return mTransferOutLocalFriendId;
	}

	public void setTransferOutLocalFriendId(String mTransferOutLocalFriendId) {
		this.mTransferOutLocalFriendId = mTransferOutLocalFriendId;
	}
	
	public Friend getTransferOutFriend(){
		if(mTransferOutFriendUserId != null){
			return new Select().from(Friend.class).where("friendUserId=?",mTransferOutFriendUserId).executeSingle();
		}else if(mTransferOutLocalFriendId != null){
			return getModel(Friend.class,mTransferOutLocalFriendId);
		}
		return null;
	}
	
	public void setTransferOutFriend(Friend mTransferOutFriendUser){
		if(mTransferOutFriendUser.getFriendUserId() != null){
			this.mTransferOutFriendUserId = mTransferOutFriendUser.getFriendUserId();
		}else{
			this.mTransferOutLocalFriendId = mTransferOutFriendUser.getId();
		}
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
	
	public Double getTransferInAmount0() {
		if(mTransferInAmount == null){
			return 0.00;
		}
		return mTransferInAmount;
	}

	public void setTransferInAmount(Double mTransferInAmount) {
		if(mTransferInAmount != null){
			mTransferInAmount = HyjUtil.toFixed2(mTransferInAmount);
		}
		this.mTransferInAmount = mTransferInAmount;
	}
	
	public String getTransferInFriendUserId() {
		return mTransferInFriendUserId;
	}

	public void setTransferInFriendUserId(String mTransferInFriendUserId) {
		this.mTransferInFriendUserId = mTransferInFriendUserId;
	}
	
	public String getTransferInLocalFriendId() {
		return mTransferInLocalFriendId;
	}

	public void setTransferInLocalFriendId(String mTransferInLocalFriendId) {
		this.mTransferInLocalFriendId = mTransferInLocalFriendId;
	}
	
	public Friend getTransferInFriend(){
		if(mTransferInFriendUserId != null){
			return new Select().from(Friend.class).where("friendUserId=?",mTransferInFriendUserId).executeSingle();
		}else if(mTransferInLocalFriendId != null){
			return getModel(Friend.class,mTransferInLocalFriendId);
		}
		return null;
	}
	
	public void setTransferInFriend(Friend mTransferInFriendUser){
		if(mTransferInFriendUser.getFriendUserId() != null){
			this.mTransferInFriendUserId = mTransferInFriendUser.getFriendUserId();
		}else{
			this.mTransferInFriendUserId = mTransferInFriendUser.getId();
		}
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
		if(mExchangeRate != null){
			mExchangeRate = HyjUtil.toFixed2(mExchangeRate);
		}
		this.mExchangeRate = mExchangeRate;
	}

	public String getProjectId() {
		return mProjectId;
	}

	public void setProjectId(String mProjectId) {
		this.mProjectId = mProjectId;
	}
	
	public Project getProject(){
		return getModel(Project.class, mProjectId);
	}

	public String getRemark() {
		return mRemark;
	}
	
	public String getDisplayRemark() {
		if(mRemark != null && mRemark.length() > 0){
			return mRemark;
		} else {
			return HyjApplication.getInstance().getString(R.string.app_no_remark);
		}
	}

	public void setRemark(String mRemark) {
		this.mRemark = mRemark;
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

	@Override
	public void save(){
		if(this.getOwnerUserId() == null){
			this.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
		}
		super.save();
	}

	public User getOwnerUser() {
		return getModel(User.class, mOwnerUserId);
	}
}
