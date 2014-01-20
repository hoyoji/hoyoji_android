package com.hoyoji.hoyoji.models;

import java.util.List;
import java.util.UUID;

import android.provider.BaseColumns;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.hoyoji.R;

@Table(name = "Project", id = BaseColumns._ID)
public class Project extends HyjModel {

	@Column(name = "id", index = true, unique = true)
	private String mId;

	@Column(name = "name")
	private String mName;

	@Column(name = "ownerUserId")
	private String mOwnerUserId;

	@Column(name = "currencyId")
	private String mCurrencyId;

	@Column(name = "autoApportion")
	private String mAutoApportion;

	@Column(name = "defaultIncomeCategoryId")
	private String mDefaultIncomeCategoryId;

	@Column(name = "defaultExpenseCategoryId")
	private String mDefaultExpenseCategoryId;

	@Column(name = "depositeIncomeCategoryId")
	private String mDepositeIncomeCategoryId;

	@Column(name = "depositeExpenseCategoryId")
	private String mDepositeExpenseCategoryId;

	@Column(name = "serverRecordHash")
	private String mServerRecordHash;

	@Column(name = "lastServerUpdateTime")
	private String mLastServerUpdateTime;

	@Column(name = "lastClientUpdateTime")
	private String mLastClientUpdateTime;
	
	public Project(){
		super();
		mId = UUID.randomUUID().toString();
	}
	
	@Override
	public void validate(HyjModelEditor modelEditor) {
		if(this.getName().length() == 0){
			modelEditor.setValidationError("name", R.string.projectFormFragment_editText_hint_projectName);
		} else {
			modelEditor.removeValidationError("name");
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
	
	public List<ParentProject> getParentProjects() {
		return getMany(ParentProject.class, "subProjectId");
	}
	
	public List<ParentProject> getSubProjects() {
		return getMany(ParentProject.class, "parentProjectId");
	}
	
	public String getOwnerUserId() {
		return mOwnerUserId;
	}

	public void setOwnerUserId(String mOwnerUserId) {
		this.mOwnerUserId = mOwnerUserId;
	}

	public String getCurrencyId() {
		return mCurrencyId;
	}

	public void setCurrencyId(String mCurrencyId) {
		this.mCurrencyId = mCurrencyId;
	}

	public String getAutoApportion() {
		return mAutoApportion;
	}

	public void setAutoApportion(String mAutoApportion) {
		this.mAutoApportion = mAutoApportion;
	}

	public String getDefaultIncomeCategoryId() {
		return mDefaultIncomeCategoryId;
	}

	public void setDefaultIncomeCategoryId(String mDefaultIncomeCategoryId) {
		this.mDefaultIncomeCategoryId = mDefaultIncomeCategoryId;
	}

	public String getDefaultExpenseCategoryId() {
		return mDefaultExpenseCategoryId;
	}

	public void setDefaultExpenseCategoryId(String mDefaultExpenseCategoryId) {
		this.mDefaultExpenseCategoryId = mDefaultExpenseCategoryId;
	}

	public String getDepositeIncomeCategoryId() {
		return mDepositeIncomeCategoryId;
	}

	public void setDepositeIncomeCategoryId(String mDepositeIncomeCategoryId) {
		this.mDepositeIncomeCategoryId = mDepositeIncomeCategoryId;
	}

	public String getDepositeExpenseCategoryId() {
		return mDepositeExpenseCategoryId;
	}

	public void setDepositeExpenseCategoryId(String mDepositeExpenseCategoryId) {
		this.mDepositeExpenseCategoryId = mDepositeExpenseCategoryId;
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
