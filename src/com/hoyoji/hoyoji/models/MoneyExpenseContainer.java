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

@Table(name = "MoneyExpenseContainer", id = BaseColumns._ID)
public class MoneyExpenseContainer extends HyjModel{

	@Column(name = "id", index = true, unique = true)
	private String mUUID;
	
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

	@Column(name = "moneyExpenseCategoryMain")
	private String mMoneyExpenseCategoryMain;
	
	@Column(name = "exchangeRate")
	private Double mExchangeRate;

	@Column(name = "moneyIncomeId")
	private String mMoneyIncomeId;
	
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

	@Column(name = "_creatorId")
	private String m_creatorId;

	@Column(name = "serverRecordHash")
	private String mServerRecordHash;

	@Column(name = "lastServerUpdateTime")
	private String mLastServerUpdateTime;

	@Column(name = "lastClientUpdateTime")
	private Long mLastClientUpdateTime;
	
	public MoneyExpenseContainer(){
		super();
		UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
		mUUID = UUID.randomUUID().toString();
		mExpenseType = "MoneyExpense";
		mMoneyAccountId = userData.getActiveMoneyAccountId();
		mProjectId = userData.getActiveProjectId();
		mExchangeRate = 1.00;
		if(this.getProject() != null){
			mMoneyExpenseCategory = this.getProject().getDefaultExpenseCategory();
			mMoneyExpenseCategoryMain = this.getProject().getDefaultExpenseCategoryMain();
		}
	}

	public String getId() {
		return mUUID;
	}

	public void setId(String mUUID) {
		this.mUUID = mUUID;
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
		return getModel(Picture.class, mPictureId);
	}

	public void setPicture(Picture picture){
		this.setPictureId(picture.getId());
	}
	
	public List<Picture> getPictures(){
		return getMany(Picture.class, "recordId");
	}

	public List<MoneyExpenseApportion> getApportions(){
		return getMany(MoneyExpenseApportion.class, "moneyExpenseContainerId");
	}
	
	public String getDate() {
		return mDate;
	}

	public void setDate(String mDate) {
		this.mDate = mDate;
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
	
	public Double getLocalAmount(){
		Double rate = 1.0;
		String userCurrencyId = HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId();
//		if(this.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
//			if(userCurrency.getId().equals(this.getMoneyAccount().getCurrencyId())){
//				rate = 1.0;
//			}else{
//				Exchange exchange = Exchange.getExchange(userCurrency.getId(), this.getMoneyAccount().getCurrencyId());
//			    if(exchange != null){
//			    	rate = exchange.getRate();
//			    }
//			}
//			return this.getAmount0()/rate;
//		}else{
			if(!userCurrencyId.equals(this.getProject().getCurrencyId())){
				Double exchange = Exchange.getExchangeRate(userCurrencyId, this.getProject().getCurrencyId());
				if(exchange != null){
				   	rate = exchange;
			    }
			}
			return this.getAmount0()*this.getExchangeRate()/rate;
		}
//	}

	public void setAmount(Double mAmount) {
		if(mAmount != null){
			mAmount = HyjUtil.toFixed2(mAmount);
		}
		this.mAmount = mAmount;
	}
	
	public String getExpenseType() {
		return mExpenseType;
	}

	public void setExpenseType(String mExpenseType) {
		this.mExpenseType = mExpenseType;
	}
	
	public String getFriendUserId() {
		return mFriendUserId;
	}

	public void setFriendUserId(String mFriendUserId) {
		this.mFriendUserId = mFriendUserId;
	}

	public String getLocalFriendId() {
		return mLocalFriendId;
	}

	public void setLocalFriendId(String mLocalFriendId) {
		this.mLocalFriendId = mLocalFriendId;
	}
	
	public Friend getFriend(){
		if(mFriendUserId != null){
			return new Select().from(Friend.class).where("friendUserId=?",mFriendUserId).executeSingle();
		}else if(mLocalFriendId != null){
			return getModel(Friend.class, mLocalFriendId);
		}
		return null;
	}
	
	public void setFriend(Friend mFriend) {
		if(mFriend == null){
			this.mFriendUserId = null;
			this.mLocalFriendId = null;
		}else if(mFriend.getFriendUserId() != null){
			this.mFriendUserId = mFriend.getFriendUserId();
			this.mLocalFriendId = null;
		}
		else {
			this.mFriendUserId= null;
			this.mLocalFriendId = mFriend.getId();
		}
	}

	public String getFriendAccountId() {
		return mFriendAccountId;
	}

	public void setFriendAccountId(String mFriendAccountId) {
		this.mFriendAccountId = mFriendAccountId;
	}

	public MoneyAccount getMoneyAccount(){
		if(mMoneyAccountId == null){
			return null;
		}
		return getModel(MoneyAccount.class, mMoneyAccountId);
	}
	
	public void setMoneyAccount(MoneyAccount mMoneyAccount) {
		this.mMoneyAccountId = mMoneyAccount.getId();
	}
	
	public String getMoneyAccountId() {
		return mMoneyAccountId;
	}

	public void setMoneyAccountId(String mMoneyAccountId) {
		this.mMoneyAccountId = mMoneyAccountId;
	}

	public Project getProject(){
		if(mProjectId == null){
			return null;
		}
		return getModel(Project.class, mProjectId);
	}
	
	public void setProject(Project mProject) {
		this.mProjectId = mProject.getId();
	}
	
	public String getProjectId() {
		return mProjectId;
	}

	public void setProjectId(String mProjectId) {
		this.mProjectId = mProjectId;
	}

	public String getMoneyExpenseCategory() {
		return mMoneyExpenseCategory;
	}

	public void setMoneyExpenseCategory(String mMoneyExpenseCategory) {
		this.mMoneyExpenseCategory = mMoneyExpenseCategory;
	}

	public String getMoneyExpenseCategoryMain() {
		return mMoneyExpenseCategoryMain;
	}

	public void setMoneyExpenseCategoryMain(String mMoneyExpenseCategoryMain) {
		this.mMoneyExpenseCategoryMain = mMoneyExpenseCategoryMain;
	}	
	
	public Double getExchangeRate() {
		return mExchangeRate;
	}

	public void setExchangeRate(Double mExchangeRate) {
		if(mExchangeRate != null) {
			mExchangeRate = HyjUtil.toFixed2(mExchangeRate);
		}
		this.mExchangeRate = mExchangeRate;
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

	public User getOwnerUser() {
		return getModel(User.class, mOwnerUserId);
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
	public void validate(HyjModelEditor<?> modelEditor) {
		if(this.getDate() == null){
			modelEditor.setValidationError("date",R.string.moneyExpenseFormFragment_editText_hint_date);
		}else{
			modelEditor.removeValidationError("date");
		}
		
		if(this.getAmount() == null){
			modelEditor.setValidationError("amount",R.string.moneyExpenseFormFragment_editText_hint_amount);
		}else if(this.getAmount() < 0){
			modelEditor.setValidationError("amount",R.string.moneyExpenseFormFragment_editText_validationError_negative_amount);
		}else if(this.getAmount() > 99999999){
			modelEditor.setValidationError("amount",R.string.moneyExpenseFormFragment_editText_validationError_beyondMAX_amount);
		}
		else{
			modelEditor.removeValidationError("amount");
		}
		
		if(this.getMoneyExpenseCategory() == null || this.getMoneyExpenseCategory().length() == 0){
			modelEditor.setValidationError("moneyExpenseCategory", R.string.moneyExpenseFormFragment_editText_hint_moneyExpenseCategory);
		}else{
			modelEditor.removeValidationError("moneyExpenseCategory");
		}
		
		if(this.getExchangeRate() == null){
			modelEditor.setValidationError("exchangeRate",R.string.moneyExpenseFormFragment_editText_hint_exchangeRate);
		}else if(this.getExchangeRate() == 0){
			modelEditor.setValidationError("exchangeRate",R.string.moneyExpenseFormFragment_editText_validationError_zero_exchangeRate);
		}else if(this.getExchangeRate() < 0){
			modelEditor.setValidationError("exchangeRate",R.string.moneyExpenseFormFragment_editText_validationError_negative_exchangeRate);
		}else if(this.getExchangeRate() > 99999999){
			modelEditor.setValidationError("exchangeRate",R.string.moneyExpenseFormFragment_editText_validationError_beyondMAX_exchangeRate);
		}
		else{
			modelEditor.removeValidationError("exchangeRate");
		}
		
		if(this.getMoneyAccountId() == null){
			modelEditor.setValidationError("moneyAccount",R.string.moneyExpenseFormFragment_editText_hint_moneyAccount);
		}else{
			modelEditor.removeValidationError("moneyAccount");
		}
		
		if(this.getProjectId() == null){
			modelEditor.setValidationError("project",R.string.moneyExpenseFormFragment_editText_hint_project);
		}else{
			modelEditor.removeValidationError("project");
		}
	}
	
	@Override
	public void save(){
		if(this.getOwnerUserId() == null){
			this.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
		}
		super.save();
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
	
	public boolean hasEditPermission(){
		if(!this.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
			return false;
		}
		
		ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("projectId = ? AND friendUserId=?", this.getProjectId(), HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
		if(psa == null){
			return false;
		}
		return psa.getProjectShareMoneyExpenseEdit();
	}
	
	public boolean hasAddNewPermission(String projectId){
		ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("projectId = ? AND friendUserId=?", projectId, HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
		if(psa == null){
			return false;
		}
		return psa.getProjectShareMoneyExpenseAddNew();
	}

	public boolean hasDeletePermission(){
		if(!this.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
			return false;
		}
		
		ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("projectId = ? AND friendUserId=?", this.getProjectId(), HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
		if(psa == null){
			return false;
		}
		return psa.getProjectShareMoneyExpenseDelete();
	}

	public String getMoneyIncomeId(){
		return mMoneyIncomeId;
	}
	
	public void setMoneyIncomeId(String moneyIncomeId) {
		this.mMoneyIncomeId = moneyIncomeId;
	}

}