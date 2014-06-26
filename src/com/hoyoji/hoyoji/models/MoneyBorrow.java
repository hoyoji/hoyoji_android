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
import com.hoyoji.hoyoji_android.R;

@Table(name = "MoneyBorrow", id = BaseColumns._ID)
public class MoneyBorrow extends HyjModel{

	@Column(name = "id", index = true, unique = true)
	private String mUUID;
	
	@Column(name = "pictureId")
	private String mPictureId;
	
	@Column(name = "date")
	private String mDate;

	@Column(name = "amount")
	private Double mAmount;

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
	
	@Column(name = "exchangeRate")
	private Double mExchangeRate;
	
	// 记录amount对应的币种，应该和 moneyAccount 的币种一致
	@Column(name = "currencyId")
	private String mCurrencyId;
	
	// 记录项目对应的币种，应该和 projectId 的币种一致
	@Column(name = "projectCurrencyId")
	private String mProjectCurrencyId;
	
	@Column(name = "returnDate")
	private String mReturnDate;
	
	@Column(name = "returnedAmount")
	private Double mReturnedAmount;

	// 如果有，说明该借入是从该借出导入生成的
	@Column(name = "moneyLendId")
	private String mMoneyLendId;

	// 该借入是由 收入分摊（MoneyIncomeApportion） 生成的
	@Column(name = "moneyIncomeApportionId")
	private String mMoneyIncomeApportionId;

	// 该借入是由 支出分摊（MoneyExpenseApportion） 生成的。
	// 帮别人（被分摊人）生成的, 所以借入的 moneyAccountId 为 null，ownerUserId 会是别人，friendUserId 会是自己
	@Column(name = "moneyExpenseApportionId")
	private String mMoneyExpenseApportionId;

	// 该借入是由 预收会费分摊（MoneyDepositIncome） 生成的
	@Column(name = "moneyDepositIncomeApportionId")
	private String mMoneyDepositIncomeApportionId;
	
	// Borrow :
	// Deposit :
	@Column(name = "borrowType")
	private String mBorrowType;
	
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
	
	public MoneyBorrow(){
		super();
		UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
		mUUID = UUID.randomUUID().toString();
		this.setMoneyAccount(userData.getActiveMoneyAccount());
		this.setProject(userData.getActiveProject());
		mExchangeRate = 1.00;
		mReturnedAmount = 0.00; 
		mBorrowType = "Borrow";
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
		return (Picture) getModel(Picture.class, mPictureId);
	}

	public void setPicture(Picture picture){
		this.setPictureId(picture.getId());
	}
	
	public List<Picture> getPictures(){
		return getMany(Picture.class, "recordId", "displayOrder");
	}

	public List<MoneyBorrowApportion> getApportions(){
		return getMany(MoneyBorrowApportion.class, "moneyBorrowId");
	}
	
	public List<MoneyReturn> getMoneyReturns(){
		return getMany(MoneyReturn.class, "moneyBorrowId");
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
	
	public Double getAmount0() {
		if(mAmount == null){
			return 0.00;
		}
		return mAmount;
	}
	
	public Double getLocalAmount(){
		Double rate = 1.0;
		String userCurrencyId = HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId();
		if (!userCurrencyId.equals(this.getProject().getCurrencyId())) {
			Double exchange = Exchange.getExchangeRate(userCurrencyId, this
					.getProject().getCurrencyId());
			if (exchange != null) {
				rate = exchange;
			}
		}
		return this.getAmount0() * this.getExchangeRate() / rate;
	}

	public Double getProjectAmount(){
		return this.getAmount0() * this.getExchangeRate();
	}
	
	public void setAmount(Double mAmount) {
		if(mAmount != null){
			mAmount = HyjUtil.toFixed2(mAmount);
		}
		this.mAmount = mAmount;
	}
	
	public Friend getFriend(){
		if(mFriendUserId != null){
			return new Select().from(Friend.class).where("friendUserId=?",mFriendUserId).executeSingle();
		}else if(mLocalFriendId != null){
			return (Friend) getModel(Friend.class, mLocalFriendId);
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
		return (MoneyAccount) getModel(MoneyAccount.class, mMoneyAccountId);
	}
	
	public void setMoneyAccount(MoneyAccount mMoneyAccount) {
		if(mMoneyAccount == null){
			this.setMoneyAccountId(null, null);
		} else {
			this.setMoneyAccountId(mMoneyAccount.getId(), mMoneyAccount.getCurrencyId());
		}
	}
	
	public String getMoneyAccountId() {
		return mMoneyAccountId;
	}

	public void setMoneyAccountId(String mMoneyAccountId, String currencyId) {
		this.mMoneyAccountId = mMoneyAccountId;
		this.mCurrencyId = currencyId;
	}

	public Project getProject(){
		if(mProjectId == null){
			return null;
		}
		return (Project) getModel(Project.class, mProjectId);
	}
	
	public void setProject(Project mProject) {
		if(mProject == null){
			this.mProjectId = null;
			this.mProjectCurrencyId = null;
			return;
		}
		this.mProjectId = mProject.getId();
		this.mProjectCurrencyId = mProject.getCurrencyId();
	}
	
	public String getProjectId() {
		return mProjectId;
	}

	public void setProjectId(String mProjectId, String projectCurrencyId) {
		this.mProjectId = mProjectId;
		this.mProjectCurrencyId = projectCurrencyId;
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

	public String getReturnDate() {
		return mReturnDate;
	}

	public void setReturnDate(String mReturnDate) {
		this.mReturnDate = mReturnDate;
	}

	public Double getReturnedAmount() {
		return mReturnedAmount;
	}

	public void setReturnedAmount(Double mReturnedAmount) {
		if(mReturnedAmount != null) {
			mReturnedAmount = HyjUtil.toFixed2(mReturnedAmount);
		}
		this.mReturnedAmount = mReturnedAmount;
	}
	
	public String getRemark() {
		return mRemark;
	}
	
	public String getDisplayRemark() {
		String ownerUser = Friend.getFriendUserDisplayName(this.getOwnerUserId());
		if(ownerUser.length() > 0){
			ownerUser = "[" + ownerUser + "] ";
		} else {
			ownerUser = "";
		}
		
		if(mRemark != null && (mRemark.length() > 0 || ownerUser.length() > 0)){
			return ownerUser + mRemark;
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
		if(this.getDate() == null){
			modelEditor.setValidationError("date",R.string.moneyBorrowFormFragment_editText_hint_date);
		}else{
			modelEditor.removeValidationError("date");
		}
		
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
		
		if(this.getReturnDate() != null && HyjUtil.parseDateFromISO(this.getReturnDate()).before(HyjUtil.parseDateFromISO(this.getDate()))){
		    modelEditor.setValidationError("returnDate", R.string.moneyBorrowFormFragment_editText_validationError_returnDate_before_date);
		}else{
			modelEditor.removeValidationError("returnDate");
		}
		
		if(this.getMoneyAccountId() == null){
			modelEditor.setValidationError("moneyAccount",R.string.moneyBorrowFormFragment_editText_hint_moneyAccount);
		}else{
			modelEditor.removeValidationError("moneyAccount");
		}
		
		if(this.getProjectId() == null){
			modelEditor.setValidationError("project",R.string.moneyBorrowFormFragment_editText_hint_project);
		}else{
			modelEditor.removeValidationError("project");
		}
		
		if(this.getExchangeRate() == null){
			modelEditor.setValidationError("exchangeRate",R.string.moneyBorrowFormFragment_editText_hint_exchangeRate);
		}else if(this.getExchangeRate() == 0){
			modelEditor.setValidationError("exchangeRate",R.string.moneyBorrowFormFragment_editText_validationError_zero_exchangeRate);
		}else if(this.getExchangeRate() < 0){
			modelEditor.setValidationError("exchangeRate",R.string.moneyBorrowFormFragment_editText_validationError_negative_exchangeRate);
		}else if(this.getExchangeRate() > 99999999){
			modelEditor.setValidationError("exchangeRate",R.string.moneyBorrowFormFragment_editText_validationError_beyondMAX_exchangeRate);
		}
		else{
			modelEditor.removeValidationError("exchangeRate");
		}
		
	}

	@Override
	public void save(){
		if(this.getOwnerUserId() == null){
			this.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
		} else if(this.getOwnerUserId().equals("")){
			this.setOwnerUserId(null);
		}
		super.save();
	}

	public User getOwnerUser() {
		return getModel(User.class, mOwnerUserId);
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
		return psa.getProjectShareMoneyBorrowEdit();
	}
	
	public boolean hasAddNewPermission(String projectId){
		ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("projectId = ? AND friendUserId=?", projectId, HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
		if(psa == null){
			return false;
		}
		return psa.getProjectShareMoneyBorrowAddNew();
	}

	public boolean hasDeletePermission(){
		if(!this.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
			return false;
		}
		
		ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("projectId = ? AND friendUserId=?", this.getProjectId(), HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
		if(psa == null){
			return false;
		}
		return psa.getProjectShareMoneyBorrowDelete();
	}

	public void setMoneyLendId(String moneyLendId) {
		this.mMoneyLendId = moneyLendId;
	}

	public void setMoneyIncomeApportionId(String id) {
		this.mMoneyIncomeApportionId = id;
	}
	public String getMoneyIncomeApportionId() {
		return this.mMoneyIncomeApportionId;
	}
	public MoneyIncomeApportion getMoneyIncomeApportion() {
		if(this.mMoneyIncomeApportionId == null){
			return null;
		}
		return HyjModel.getModel(MoneyIncomeApportion.class, this.mMoneyIncomeApportionId);
	}
	public MoneyDepositIncomeApportion getMoneyDepositIncomeApportion() {
		if(this.mMoneyDepositIncomeApportionId == null){
			return null;
		}
		return HyjModel.getModel(MoneyDepositIncomeApportion.class, this.mMoneyDepositIncomeApportionId);
	}

	public void setMoneyExpenseApportionId(String id) {
		this.mMoneyExpenseApportionId = id;
	}
	
	public String getMoneyExpenseApportionId() {
		return this.mMoneyExpenseApportionId;
	}
	public MoneyExpenseApportion getMoneyExpenseApportion() {
		if(this.mMoneyExpenseApportionId == null){
			return null;
		}
		return HyjModel.getModel(MoneyExpenseApportion.class, this.mMoneyExpenseApportionId);
	}

	public String getBorrowType() {
		if(mBorrowType == null){
			return "Borrow";
		}
		return this.mBorrowType;
	}
	
	public void setBorrowType(String mBorrowType) {
		this.mBorrowType = mBorrowType;
	}
	
	public String getCurrencyId1() {
		return mCurrencyId;
	}

	public void setCurrencyId1(String mCurrencyId) {
		this.mCurrencyId = mCurrencyId;
	}

	public void setMoneyDepositIncomeApportionId(String id) {
		this.mMoneyDepositIncomeApportionId = id;
		
	}

	public String getMoneyDepositIncomeApportionId() {
		return mMoneyDepositIncomeApportionId;
	}
}
