package com.hoyoji.android.hyjframework;

import java.util.UUID;

import android.provider.BaseColumns;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;

@Table(name = "ClientSyncTable", id = BaseColumns._ID)
public class HyjClientSyncRecord extends HyjModel {

	@Column(name = "id", index = true, unique = true)
	private String mId;

//	@Column(name = "recordId", index = true, unique = true)
//	private String mRecordId;

	@Column(name = "tableName", index = true)
	private String mTableName;
	
	@Column(name = "operation", index = true)
	private String mOperation;

	@Column(name = "transactionId")
	private String mTransactionId;
	
//	@Column(name = "ownerUserId")
//	private String mOwnerUserId;
//	
	public HyjClientSyncRecord(){
		super();
//		mId = UUID.randomUUID().toString();
	}
	

	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	
//	public List<ParentProject> getParentProjects() {
//		return getMany(ParentProject.class, "");
//	}
//	
//	public String getOwnerUserId() {
//		return mOwnerUserId;
//	}
//
//	public void setOwnerUserId(String mOwnerUserId) {
//		this.mOwnerUserId = mOwnerUserId;
//	}

//	public String getRecordId(){
//		return mRecordId;
//	}
//
//	public void setRecordId(String recordId) {
//		this.mRecordId = recordId;
//	}

	public String getOperation() {
		return mOperation;
	}

	public void setOperation(String opertion) {
		this.mOperation = opertion;
	}
	
	public String getTableName() {
		return mTableName;
	}

	public void setTableName(String tableName) {
		this.mTableName = tableName;
	}
	
	public String getTransactionId() {
		return mTransactionId;
	}

	public void setTransactionId(String transactionId) {
		this.mTransactionId = transactionId;
	}

	@Override
	public void save(){
//		if(this.getOwnerUserId() == null){
//			this.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
//		}
		super.save();
	}


	@Override
	public void validate(HyjModelEditor<? extends HyjModel> hyjModelEditor) {
		// TODO Auto-generated method stub
		
	}
}
