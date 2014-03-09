package com.hoyoji.hoyoji.models;

import java.util.List;
import java.util.UUID;

import android.provider.BaseColumns;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.hoyoji.R;

@Table(name = "Picture", id = BaseColumns._ID)
public class Picture extends HyjModel {

	@Column(name = "id", index = true, unique = true)
	private String mUUID;

	@Column(name = "title")
	private String mTitle;

	@Column(name = "ownerUserId")
	private String mOwnerUserId;

	@Column(name = "path")
	private String mPath;

	@Column(name = "pictureType")
	private String mPictureType;

	@Column(name = "recordId")
	private String mRecordId;

	@Column(name = "recordType")
	private String mRecordType;

	@Column(name = "toBeUploaded")
	private Boolean mToBeUploaded;

	@Column(name = "toBeDownloaded")
	private Boolean mToBeDownloaded;

	@Column(name = "_creatorId")
	private String m_creatorId;

	@Column(name = "serverRecordHash")
	private String mServerRecordHash;

	@Column(name = "lastServerUpdateTime")
	private String mLastServerUpdateTime;

	@Column(name = "lastClientUpdateTime")
	private Long mLastClientUpdateTime;
	
	public Picture(){
		super();
		mUUID = UUID.randomUUID().toString();
		this.setToBeUpaded(true);
		this.setToBeDownloaded(false);
	}
	
	@Override
	public void validate(HyjModelEditor modelEditor) {
		if(this.getPath().length() == 0){
			modelEditor.setValidationError("path", R.string.projectFormFragment_editText_hint_projectName);
		} else {
			modelEditor.removeValidationError("path");
		}	
	}

	public String getId() {
		return mUUID;
	}

	public void setId(String mUUID) {
		this.mUUID = mUUID;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}
	
	public String getOwnerUserId() {
		return mOwnerUserId;
	}

	public void setOwnerUserId(String mOwnerUserId) {
		this.mOwnerUserId = mOwnerUserId;
	}

	public String getPath() {
		return mPath;
	}

	public void setPath(String path) {
		this.mPath = path;
	}

	public String getPictureType() {
		return mPictureType;
	}

	public void setPictureType(String pictureType) {
		this.mPictureType = pictureType;
	}

	public String getRecordId() {
		return mRecordId;
	}

	public void setRecordId(String recordId) {
		this.mRecordId = recordId;
	}

	public String getRecordType() {
		return mRecordType;
	}

	public void setRecordType(String recordType) {
		this.mRecordType = recordType;
	}

	public Boolean getToBeUploaded() {
		return mToBeUploaded;
	}

	public void setToBeUpaded(Boolean toBeUploaded) {
		this.mToBeUploaded = toBeUploaded;
	}

	public Boolean getToBeDownloaded() {
		return mToBeDownloaded;
	}

	public void setToBeDownloaded(boolean toBeDownloaded) {
		this.mToBeDownloaded = toBeDownloaded;
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
	
}
