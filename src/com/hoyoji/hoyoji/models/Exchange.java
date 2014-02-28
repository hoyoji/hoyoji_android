package com.hoyoji.hoyoji.models;

import java.util.List;
import java.util.UUID;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.hoyoji.R;

@Table(name = "Exchange", id = BaseColumns._ID)
public class Exchange extends HyjModel {

	@Column(name = "id", index = true, unique = true)
	private String mId;

	@Column(name = "rate")
	private Double mRate;

	@Column(name = "ownerUserId")
	private String mOwnerUserId;

	@Column(name = "localCurrencyId")
	private String mLocalCurrencyId;

	@Column(name = "foreignCurrencyId")
	private String mForeignCurrencyId;
	
	@Column(name = "autoUpdate")
	private Boolean mAutoUpdate;

	
	public Exchange(){
		super();
		mId = UUID.randomUUID().toString();
		setAutoUpdate(true);
	}
	
	public static Exchange getExchange(String fromCurrency, String toCurrency){
		return new Select().from(Exchange.class).where("localCurrencyId=? AND foreignCurrencyId=?", fromCurrency, toCurrency).executeSingle();
		
	}
	
	@Override
	public void validate(HyjModelEditor<?> modelEditor) {
		if(this.getLocalCurrencyId() == null){
			modelEditor.setValidationError("localCurrency",R.string.exchangeFormFragment_editText_hint_localCurrency);
		}else{
			modelEditor.removeValidationError("localCurrency");
		}
		
		if(this.getForeignCurrencyId() == null){
			modelEditor.setValidationError("foreignCurrency",R.string.exchangeFormFragment_editText_hint_foreignCurrency);
		}else{
			modelEditor.removeValidationError("foreignCurrency");
		}
		
		if(this.getRate() == null){
			modelEditor.setValidationError("rate",R.string.exchangeFormFragment_editText_hint_rate);
		}else{
			modelEditor.removeValidationError("rate");
		}
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

	public Double getRate() {
		return mRate;
	}

	public void setRate(Double mRate) {
		this.mRate = mRate;
	}

	public String getLocalCurrencyId() {
		return mLocalCurrencyId;
	}
	
	public Currency getLocalCurrency() {
		if(mLocalCurrencyId == null){
			return null;
		}
		return getModel(Currency.class, mLocalCurrencyId);
	}

	public void setLocalCurrencyId(String mLocalCurrencyId) {
		this.mLocalCurrencyId = mLocalCurrencyId;
	}

	public String getForeignCurrencyId() {
		return mForeignCurrencyId;
	}

	public Currency getForeignCurrency() {
		if(mForeignCurrencyId == null){
			return null;
		}
		return getModel(Currency.class, mForeignCurrencyId);
	}
	
	public void setForeignCurrencyId(String mForeignCurrencyId) {
		this.mForeignCurrencyId = mForeignCurrencyId;
	}

	public Boolean getAutoUpdate() {
		return mAutoUpdate;
	}

	public void setAutoUpdate(Boolean mAutoUpdate) {
		this.mAutoUpdate = mAutoUpdate;
	}

	
	@Override
	public void save(){
		if(this.getOwnerUserId() == null){
			this.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
		}
		super.save();
	}
}
