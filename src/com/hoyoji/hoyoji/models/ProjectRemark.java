package com.hoyoji.hoyoji.models;

import java.util.UUID;

import android.provider.BaseColumns;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;

@Table(name = "ProjectRemark", id = BaseColumns._ID)
public class ProjectRemark extends HyjModel {

	@Column(name = "id", index = true, unique = true)
	private String mId;

	@Column(name = "remark")
	private String mRemark;

	@Column(name = "ownerUserId")
	private String mOwnerUserId;

	@Column(name = "projectId")
	private String mProjectId;

	
	public ProjectRemark(){
		super();
		mId = UUID.randomUUID().toString();
	}

	
	@Override
	public void validate(HyjModelEditor<?> modelEditor) {
		if(this.getRemark() == null || this.getRemark().length() == 0){
			modelEditor.setValidationError("remark", "请输入备注名称");
		}else{
			modelEditor.removeValidationError("remark");
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

	public String getRemark() {
		return mRemark;
	}

	public void setRemark(String name) {
		this.mRemark = name;
	}

	public String getProjectId() {
		return mProjectId;
	}
	
	public Project getProject() {
		if(mProjectId == null){
			return null;
		}
		return getModel(Project.class, mProjectId);
	}

	public void setProjectIdId(String projectId) {
		this.mProjectId = projectId;
	}

	
	@Override
	public void save(){
		if(this.getOwnerUserId() == null){
			this.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
		}
		super.save();
	}
}
