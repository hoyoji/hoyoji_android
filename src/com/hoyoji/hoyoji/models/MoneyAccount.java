package com.hoyoji.hoyoji.models;

import java.util.UUID;

import android.provider.BaseColumns;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.hoyoji.R;

@Table(name = "MoneyAccount", id = BaseColumns._ID)
public class MoneyAccount extends HyjModel {

	@Column(name = "id", index = true, unique = true)
	private String mId;

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

	
	public MoneyAccount(){
		super();
		UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
		mId = UUID.randomUUID().toString();
		mCurrencyId = userData.getActiveCurrencyId();
		mCurrentBalance = 0.00;
	}
	
	public static MoneyAccount getDebtAccount(String currencyId, Friend friend){
		String friendId;
		if(friend == null){
			friendId = "";
		}else{
			friendId = friend.getId();
		}
		return new Select().from(MoneyAccount.class).where("accountType=? AND currencyId=? AND friendId=?", "Debt", currencyId, friendId).executeSingle();
	}
	
	public static void createDebtAccount(Friend friend, String currencyId, Double amount){
		MoneyAccount createDebtAccount = new MoneyAccount();
		String debtAccountName = "匿名借贷账户";
		String friendId = "";
		if(friend != null){
			friendId = friend.getId();
			debtAccountName = friendId;
		}
		createDebtAccount.setName(debtAccountName);
		createDebtAccount.setCurrencyId(currencyId);
		createDebtAccount.setCurrentBalance(amount);
		createDebtAccount.setSharingType("Private");
		createDebtAccount.setAccountType("Debt");
		createDebtAccount.setFriendId(friendId);
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
		return mFriendId;
	}

	public void setFriendId(String mFriendId) {
		this.mFriendId = mFriendId;
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
}
