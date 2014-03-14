package com.hoyoji.hoyoji.models;

import java.util.UUID;

import org.json.JSONObject;

import android.provider.BaseColumns;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.hoyoji.R;

@Table(name = "MoneyAccount", id = BaseColumns._ID)
public class MoneyAccount extends HyjModel {

	@Column(name = "id", index = true, unique = true)
	private String mUUID;

	@Column(name = "name")
	private String mName;

	@Column(name = "currencyId")
	private String mCurrencyId;

	@Column(name = "currentBalance")
	private Double mCurrentBalance;

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
	
	public MoneyAccount(){
		super();
		UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
		mUUID = UUID.randomUUID().toString();
		mCurrencyId = userData.getActiveCurrencyId();
		mAccountType = "Cash";
		mCurrentBalance = 0.00;
	}
	
	public String getDisplayName(){
		if(this.get_mId() == null && this.getName() == null){
			return null;
		}
		if(this.getAccountType().equalsIgnoreCase("Debt")){
			if(this.getFriendId() != null){
				Friend friend = HyjModel.getModel(Friend.class, this.getFriendId());
				return friend.getDisplayName();
			} else {
				if(this.getName().equalsIgnoreCase("__ANONYMOUS__")){
					return String.valueOf(R.string.app_moneyaccount_anonymous);
				}
				
				Friend friend = new Select().from(Friend.class).where("friendUserId=?", this.getName()).executeSingle();
				if(friend != null){
					return friend.getDisplayName();
				} else {
					User user = HyjModel.getModel(User.class, this.getName());
					if(user != null){
						return user.getDisplayName();
					} else {
						return this.getName();
					}
				}
			}
		}else{
			return this.getName();
		}
		
	}
	
	public static MoneyAccount getDebtAccount(String currencyId, Friend friend){
		if(friend == null){
			return new Select().from(MoneyAccount.class).where("accountType=? AND currencyId=? AND friendId IS NULL AND name=?", "Debt", currencyId, "__ANONYMOUS__").executeSingle();
		}
		
		String friendId;
		if(friend.getFriendUserId() == null){
			friendId = friend.getId();
			return new Select().from(MoneyAccount.class).where("accountType=? AND currencyId=? AND friendId=?", "Debt", currencyId, friendId).executeSingle();
		} else {	
			return new Select().from(MoneyAccount.class).where("accountType=? AND currencyId=? AND name=?", "Debt", currencyId, friend.getFriendUserId()).executeSingle();
		}
	}
	

	public static MoneyAccount getDebtAccount(String currencyId, String friendUserId) {
		return new Select().from(MoneyAccount.class).where("accountType=? AND currencyId=? AND name=?", "Debt", currencyId, friendUserId).executeSingle();
	}
	
	public static void createDebtAccount(Friend friend, String currencyId, Double amount){
		MoneyAccount createDebtAccount = new MoneyAccount();
		String debtAccountName = "__ANONYMOUS__";
		String friendId = null;
		if(friend != null){
			if(friend.getFriendUserId() == null){
				friendId = friend.getId();
				debtAccountName = null;
			}else{
				debtAccountName = friend.getFriendUserId();
			}
		}
		createDebtAccount.setName(debtAccountName);
		createDebtAccount.setCurrencyId(currencyId);
		createDebtAccount.setCurrentBalance(amount);
		createDebtAccount.setSharingType("Private");
		createDebtAccount.setAccountType("Debt");
		createDebtAccount.setFriendId(friendId);
		createDebtAccount.save();
	}
	
	public static void createDebtAccount(String friendUserId, String currencyId, Double amount){
		MoneyAccount createDebtAccount = new MoneyAccount();
		createDebtAccount.setName(friendUserId);
		createDebtAccount.setCurrencyId(currencyId);
		createDebtAccount.setCurrentBalance(amount);
		createDebtAccount.setSharingType("Private");
		createDebtAccount.setAccountType("Debt");
		createDebtAccount.save();
	}
	
	@Override
	public void validate(HyjModelEditor modelEditor) {
		if(this.getName().length() == 0){
			modelEditor.setValidationError("name", R.string.moneyAccountFormFragment_editText_hint_name);
		} else {
			modelEditor.removeValidationError("name");
		}
		if(this.getCurrencyId() == null){
			modelEditor.setValidationError("currency", R.string.moneyAccountFormFragment_editText_hint_currency);
		} else {
			modelEditor.removeValidationError("currency");
		}
	}

	public String getId() {
		return mUUID;
	}

	public void setId(String mUUID) {
		this.mUUID = mUUID;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}
	
	public String getCurrencySymbol(){
		Currency currency = getCurrency();
		if(currency == null){
			return this.getCurrencyId();
		}
		return currency.getSymbol();
	}
	
	public Currency getCurrency(){
		if(mCurrencyId == null){
			return null;
		}
		return (Currency) getModel(Currency.class, mCurrencyId);
	}
	
	public void setCurrency(Currency mCurrency) {
		this.mCurrencyId = mCurrency.getId();
	}
	
	public String getCurrencyId() {
		return mCurrencyId;
	}

	public void setCurrencyId(String mCurrencyId) {
		this.mCurrencyId = mCurrencyId;
	}

	public Double getCurrentBalance() {
		return mCurrentBalance;
	}

	public void setCurrentBalance(Double mCurrentBalance) {
		if(mCurrentBalance != null) {
			mCurrentBalance = HyjUtil.toFixed2(mCurrentBalance);
		}
		this.mCurrentBalance = mCurrentBalance;
	}

	public String getRemark() {
		return mRemark;
	}

	public void setRemark(String mRemark) {
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
		if(mFriendId != null && mFriendId.length() == 0){
			return null;
		}
		return mFriendId;
	}

	public void setFriendId(String mFriendId) {
		if(mFriendId != null && mFriendId.length() == 0){
			this.mFriendId = null;
		} else {
			this.mFriendId = mFriendId;
		}
	}

	public String getOwnerUserId() {
		return mOwnerUserId;
	}

	public void setOwnerUserId(String mOwnerUserId) {
		this.mOwnerUserId = mOwnerUserId;
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

	public JSONObject toJSON() {
		final JSONObject jsonObj = super.toJSON();
		jsonObj.remove("currentBalance");
		return jsonObj;
	}	

}
