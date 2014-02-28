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

@Table(name = "ParentProject", id = BaseColumns._ID)
public class ParentProject extends HyjModel {

	@Column(name = "id", index = true, unique = true)
	private String mId;

	@Column(name = "parentProjectId", index = true)
	private String mParentProjectId;

	@Column(name = "subProjectId", index = true)
	private String mSubProjectId;
	
	@Column(name = "ownerUserId")
	private String mOwnerUserId;
	
	public ParentProject(){
		super();
		mId = UUID.randomUUID().toString();
	}
	
	@Override
	public void validate(HyjModelEditor modelEditor) {
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
	
	public String getOwnerUserId() {
		return mOwnerUserId;
	}

	public void setOwnerUserId(String mOwnerUserId) {
		this.mOwnerUserId = mOwnerUserId;
	}

	public Project getParentProject(){
		if(mParentProjectId == null){
			return null;
		}
		return (Project) getModel(Project.class, mParentProjectId);
	}

	public Project getSubProject(){
		return (Project) getModel(Project.class, mSubProjectId);
	}
	
	public String getParentProjectId() {
		return mParentProjectId;
	}

	public void setParentProjectId(String mParentProjectId) {
		this.mParentProjectId = mParentProjectId;
	}

	public String getSubProjectId() {
		return mSubProjectId;
	}

	public void setSubProjectId(String mSubProjectId) {
		this.mSubProjectId = mSubProjectId;
	}

	@Override
	public void save(){
		if(this.getOwnerUserId() == null){
			this.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
		}
		super.save();
	}
}
