package com.hoyoji.hoyoji.models;

import java.util.Iterator;
import java.util.List;
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
import com.hoyoji.hoyoji_android.R;

@Table(name = "Event", id = BaseColumns._ID)
public class Event extends HyjModel {

	@Column(name = "id", index = true, unique = true)
	private String mUUID;

	@Column(name = "date")
	private Long mDate;

	@Column(name = "name")
	private String mName;

	@Column(name = "projectId")
	private String mProjectId;
	
	@Column(name = "description")
	private String mDescription;
	
	@Column(name = "startDate")
	private Long mStartDate;
	
	@Column(name = "endDate")
	private Long mEndDate;
	
	@Column(name = "incomeTotal")
	private Double mIncomeTotal = 0.0;
	
	@Column(name = "expenseTotal")
	private Double mExpenseTotal = 0.0;

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
	
	
	public Event(){
		super();
		mUUID = UUID.randomUUID().toString();
	}

	
	@Override
	public void validate(HyjModelEditor<?> modelEditor) {
		if(this.getName() == null || "".equals(this.getName())){
			modelEditor.setValidationError("name", R.string.projectEventListFragment_editText_hint_name);
		} else {
			modelEditor.removeValidationError("name");
		}
		if(this.getDate() == null){
			modelEditor.setValidationError("date",R.string.projectEventListFragment_hyjDateTimeField_hint_date);
		}else{
			modelEditor.removeValidationError("date");
		}
		if(this.getProjectId() == null){
			modelEditor.setValidationError("project",R.string.projectEventListFragment_hyjSelectorField_hint_projectName);
		}else{
			modelEditor.removeValidationError("project");
		}
		if(this.getStartDate() == null){
			modelEditor.setValidationError("startDate",R.string.projectEventListFragment_hyjDateTimeField_hint_startDate);
		}else{
			modelEditor.removeValidationError("startDate");
		}
		if(this.getEndDate() == null){
			modelEditor.setValidationError("endDate",R.string.projectEventListFragment_hyjDateTimeField_hint_endDate);
		}else{
			modelEditor.removeValidationError("endDate");
		}
		
		if(this.getStartDate() < this.getEndDate()) {
			modelEditor.removeValidationError("endDate");
		} else {
			modelEditor.setValidationError("endDate",R.string.projectEventListFragment_hyjDateTimeField_error_endDate);
		}
	}

	public String getId() {
		return mUUID;
	}

	public void setId(String mUUID) {
		this.mUUID = mUUID;
	}
	
	public String getOwnerUserId() {
		return mOwnerUserId;
	}

	public void setOwnerUserId(String mOwnerUserId) {
		this.mOwnerUserId = mOwnerUserId;
	}

	public Long getDate() {
		return mDate;
	}

	public void setDate(Long mDate) {
		this.mDate = mDate;
	}
	
	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}
	
	public Project getProject(){
		if(mProjectId == null){
			return null;
		}
		return getModel(Project.class, mProjectId);
	}
	
	public void setProject(Project mProject) {
		if(mProject == null){
			this.mProjectId = null;
		} else {
			this.mProjectId = mProject.getId();
		}
	}

	public String getProjectId() {
		return mProjectId;
	}

	public void setProjectId(String mProjectId) {
		this.mProjectId = mProjectId;
	}
	
	public String getDescription() {
		return mDescription;
	}


	public void setDescription(String mDescription) {
		this.mDescription = mDescription;
	}


	public Long getStartDate() {
		return mStartDate;
	}


	public void setStartDate(Long mStartDate) {
		this.mStartDate = mStartDate;
	}


	public Long getEndDate() {
		return mEndDate;
	}


	public void setEndDate(Long mEndDate) {
		this.mEndDate = mEndDate;
	}

	public void setExpenseTotal(double expenseTotal){
		this.mExpenseTotal = expenseTotal;
	}
	
	public Double getExpenseTotal(){
		return this.mExpenseTotal;
	}

	public void setIncomeTotal(double incomeTotal){
		this.mIncomeTotal = incomeTotal;
	}

	public Double getIncomeTotal(){
		return this.mIncomeTotal;
	}
	
	public Double getBalance(){
        Double eventBalance = this.mIncomeTotal - this.mExpenseTotal;
		return HyjUtil.toFixed2(eventBalance);
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
		
		jsonObj.remove("incomeTotal");
		jsonObj.remove("expenseTotal");
		
		return jsonObj;
	}


	public List<EventMember> getEventMembers() {
		return new Select().from(EventMember.class)
				.where("eventId = ?", getId())
				.execute();
	}
}
