package com.hoyoji.hoyoji.models;

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

@Table(name = "MoneyBorrowApportion", id = BaseColumns._ID)
public class MoneyBorrowApportion extends HyjModel implements MoneyApportion {

	@Column(name = "id", index = true, unique = true)
	private String mUUID;

	@Column(name = "amount")
	private Double mAmount;

	@Column(name = "moneyBorrowId")
	private String mMoneyBorrowId;

	@Column(name = "friendUserId")
	private String mFriendUserId;

	@Column(name = "apportionType")
	private String mApportionType;

	@Column(name = "remark")
	private String mRemark;

	@Column(name = "lastSyncTime")
	private String mLastSyncTime;

	@Column(name = "ownerUserId")
	private String mOwnerUserId;

	@Column(name = "_creatorId")
	private String m_creatorId;

	@Column(name = "serverRecordHash")
	private String mServerRecordHash;

	@Column(name = "lastServerUpdateTime")
	private String mLastServerUpdateTime;

	@Column(name = "lastClientUpdateTime")
	private Long mLastClientUpdateTime;
	
	public MoneyBorrowApportion(){
		super();
		mUUID = UUID.randomUUID().toString();
		mApportionType = "Share";
	}

	public String getId() {
		return mUUID;
	}

	public void setId(String mUUID) {
		this.mUUID = mUUID;
	}

	public Double getAmount() {
		return mAmount;
	}
	
	public Double getAmount0(){
		if(mAmount == null){
			return 0.00;
		}
		return mAmount;
	}

	public void setAmount(Double mAmount) {
		if(mAmount != null){
			mAmount = HyjUtil.toFixed2(mAmount);
		}
		this.mAmount = mAmount;
	}
	
	public String getMoneyBorrowId() {
		return mMoneyBorrowId;
	}

	public void setMoneyBorrowId(String mMoneyBorrowId) {
		this.mMoneyBorrowId = mMoneyBorrowId;
	}
	
	public MoneyBorrow getMoneyBorrow(){
		return getModel(MoneyBorrow.class, mMoneyBorrowId);
	}

	public User getFriendUser() {
		if(mFriendUserId != null){
			return HyjModel.getModel(User.class, mFriendUserId);
		}
		return null;
	}
	
	public String getFriendUserId() {
		return mFriendUserId;
	}

	public void setFriendUserId(String mFriendUserId) {
		this.mFriendUserId = mFriendUserId;
	}
	
	public Friend getFriend(){
		return getModel(Friend.class, mFriendUserId);
	}
	
	public String getApportionType() {
		return mApportionType;
	}
	
	public void setApportionType(String mApportionType) {
		this.mApportionType = mApportionType;
	}

	public String getRemark() {
		return mRemark;
	}
	
	public String getDisplayRemark() {
		if(mRemark != null){
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

	public User getOwnerUser() {
		return getModel(User.class, mOwnerUserId);
	}

	public void setOwnerUserId(String mOwnerUserId) {
		this.mOwnerUserId = mOwnerUserId;
	}
	
	@Override
	public void validate(HyjModelEditor<? extends HyjModel> modelEditor) {
		if(this.getAmount() == null){
			modelEditor.setValidationError("amount",R.string.moneyBorrowFormFragment_editText_hint_amount);
		}else if(this.getAmount() < 0){
			modelEditor.setValidationError("amount",R.string.moneyBorrowFormFragment_editText_validationError_negative_amount);
		}else if(this.getAmount() > 99999999){
			modelEditor.setValidationError("amount",R.string.moneyBorrowFormFragment_editText_validationError_beyondMAX_amount);
		}
		else{
			modelEditor.removeValidationError("amount");
		}
	}
	@Override
	public void save(){
		if(this.getOwnerUserId() == null){
			this.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
		}
		super.save();
	}

	public Project getProject() {
		return this.getMoneyBorrow().getProject();
	}

	public ProjectShareAuthorization getProjectShareAuthorization() {
		if(this.getMoneyBorrow() == null){
			return null;
		} else {
			return new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", 
					this.getMoneyBorrow().getProjectId(), this.getFriendUserId()).executeSingle();
		}
	}

	@Override
	public void setMoneyId(String moneyTransactionId) {
		this.setMoneyBorrowId(moneyTransactionId);
	}
	

	public void setCreatorId(String id){
		m_creatorId = id;
	}
	
	public String getCreatorId(){
		return m_creatorId;
	}
	
	public String getServerRecordHash(){
		return mServerRecordHash;
	}

	public void setServerRecordHash(String mServerRecordHash){
		this.mServerRecordHash = mServerRecordHash;
	}

	public String getLastServerUpdateTime(){
		return mLastServerUpdateTime;
	}

	public void setLastServerUpdateTime(String mLastServerUpdateTime){
		this.mLastServerUpdateTime = mLastServerUpdateTime;
	}

	public Long getLastClientUpdateTime(){
		return mLastClientUpdateTime;
	}

	public void setLastClientUpdateTime(Long mLastClientUpdateTime){
		this.mLastClientUpdateTime = mLastClientUpdateTime;
	}

	@Override
	public Object getMoneyAccountId() {
		return this.getMoneyBorrow().getMoneyAccountId();
	}

	@Override
	public String getCurrencyId() {
		return this.getMoneyBorrow().getMoneyAccount().getCurrencyId();
	}	
	
}
