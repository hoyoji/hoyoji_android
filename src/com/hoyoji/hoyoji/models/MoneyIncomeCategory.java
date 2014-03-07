package com.hoyoji.hoyoji.models;

import java.util.UUID;

import android.provider.BaseColumns;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;

@Table(name = "MoneyIncomeCategory", id = BaseColumns._ID)
public class MoneyIncomeCategory extends HyjModel {

	@Column(name = "id", index = true, unique = true)
	private String mId;

	@Column(name = "name")
	private String mName;

	@Column(name = "ownerUserId")
	private String mOwnerUserId;

	@Column(name = "parentIncomeCategoryId")
	private String mParentIncomeCategoryId;

	
	public MoneyIncomeCategory(){
		super();
		mId = UUID.randomUUID().toString();
	}

	
	@Override
	public void validate(HyjModelEditor<?> modelEditor) {
		if(this.getName() == null){
			modelEditor.setValidationError("name", "请输入分类名称");
		}else{
			modelEditor.removeValidationError("name");
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

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getParentIncomeCategoryId() {
		return mParentIncomeCategoryId;
	}
	
	public MoneyIncomeCategory getParentIncomeCategory() {
		if(mParentIncomeCategoryId == null){
			return null;
		}
		return getModel(MoneyIncomeCategory.class, mParentIncomeCategoryId);
	}

	public void setParentIncomeCategoryId(String parentIncomeCategoryId) {
		this.mParentIncomeCategoryId = parentIncomeCategoryId;
	}

	
	@Override
	public void save(){
		if(this.getOwnerUserId() == null){
			this.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
		}
		super.save();
	}
}
