package com.hoyoji.hoyoji.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import android.provider.BaseColumns;

import com.activeandroid.Cache;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.hoyoji_android.R;

@Table(name = "Project", id = BaseColumns._ID)
public class Project extends HyjModel {

	@Column(name = "id", index = true, unique = true)
	private String mUUID;

	@Column(name = "name")
	private String mName;

	@Column(name = "ownerUserId")
	private String mOwnerUserId;

	@Column(name = "currencyId")
	private String mCurrencyId;

	@Column(name = "autoApportion")
	private Boolean mAutoApportion;

	@Column(name = "defaultIncomeCategory")
	private String mDefaultIncomeCategory;

	@Column(name = "defaultIncomeCategoryMain")
	private String mDefaultIncomeCategoryMain;

	@Column(name = "defaultExpenseCategory")
	private String mDefaultExpenseCategory;

	@Column(name = "defaultExpenseCategoryMain")
	private String mDefaultExpenseCategoryMain;

	@Column(name = "incomeTotal")
	private Double mIncomeTotal = 0.0;
	
	@Column(name = "expenseTotal")
	private Double mExpenseTotal = 0.0;
	
	@Column(name = "depositTotal")
	private Double mDepositTotal = 0.0;
	
//	@Column(name = "depositeIncomeCategory")
//	private String mDepositIncomeCategory;
//
//	@Column(name = "depositeExpenseCategory")
//	private String mDepositExpenseCategory;

	@Column(name = "_creatorId")
	private String m_creatorId;

	@Column(name = "serverRecordHash")
	private String mServerRecordHash;

	@Column(name = "lastServerUpdateTime")
	private String mLastServerUpdateTime;

	@Column(name = "lastClientUpdateTime")
	private Long mLastClientUpdateTime;
	
	public Project() {
		super();
		mUUID = UUID.randomUUID().toString();
		setAutoApportion(false);
		mCurrencyId = HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId();
	}

	@Override
	public void validate(HyjModelEditor modelEditor) {
		if (this.getName().length() == 0) {
			modelEditor.setValidationError("name",
					R.string.projectFormFragment_editText_hint_projectName);
		} else {
			modelEditor.removeValidationError("name");
		}
		if (this.getCurrencyId() == null) {
			modelEditor.setValidationError("currency",
					R.string.projectFormFragment_editText_hint_projectCurrency);
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

	public String getDisplayName() {
		ProjectRemark projectRemark = new Select().from(ProjectRemark.class).where("projectId=?", this.getId()).executeSingle();
		if(projectRemark != null){
			return projectRemark.getRemark();
		}
		return getName();
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

	public List<Project> getSubProjectsArray() {
		List<ParentProject> subProjectRecords = getMany(ParentProject.class, "parentProjectId");
		List<Project> subProjects = new ArrayList<Project>(); 
		for (Iterator<ParentProject> it = subProjectRecords.iterator(); it.hasNext();) {
			Project project = it.next().getSubProject();
			subProjects.add(project);
		}
		return subProjects;
    }
	
	public List<ProjectShareAuthorization> getProjectShareAuthorizations() {
		return new Select().from(ProjectShareAuthorization.class)
				.where("projectId =? AND state != 'Deleted'", getId())
				.execute();
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

	public Currency getCurrency() {
		if (mCurrencyId == null) {
			return null;
		}
		return getModel(Currency.class, mCurrencyId);
	}

	public String getCurrencySymbol() {
		if (mCurrencyId == null) {
			return null;
		}
		Currency currency = getModel(Currency.class, mCurrencyId);
		if (currency != null) {
			return currency.getSymbol();
		}
		return mCurrencyId;
	}

	public void setCurrency(Currency mCurrency) {
		this.mCurrencyId = mCurrency.getId();
	}

	public Boolean getAutoApportion() {
		return mAutoApportion;
	}

	public void setAutoApportion(Boolean mAutoApportion) {
		this.mAutoApportion = mAutoApportion;
	}

	public String getDefaultIncomeCategory() {
		return mDefaultIncomeCategory;
	}

	public void setDefaultIncomeCategory(String mDefaultIncomeCategory) {
		this.mDefaultIncomeCategory = mDefaultIncomeCategory;
	}

	public String getDefaultExpenseCategory() {
		return mDefaultExpenseCategory;
	}

	public void setDefaultExpenseCategory(String mDefaultExpenseCategory) {
		this.mDefaultExpenseCategory = mDefaultExpenseCategory;
	}

	public String getDefaultIncomeCategoryMain() {
		return mDefaultIncomeCategoryMain;
	}

	public void setDefaultIncomeCategoryMain(String mDefaultIncomeCategoryMain) {
		this.mDefaultIncomeCategoryMain = mDefaultIncomeCategoryMain;
	}

	public String getDefaultExpenseCategoryMain() {
		return mDefaultExpenseCategoryMain;
	}

	public void setDefaultExpenseCategoryMain(String mDefaultExpenseCategoryMain) {
		this.mDefaultExpenseCategoryMain = mDefaultExpenseCategoryMain;
	}

//	public String getDepositIncomeCategory() {
//		return mDepositIncomeCategory;
//	}
//
//	public void setDepositIncomeCategoryId(String mDepositIncomeCategory) {
//		this.mDepositIncomeCategory = mDepositIncomeCategory;
//	}
//
//	public String getDepositExpenseCategory() {
//		return mDepositExpenseCategory;
//	}
//
//	public void setDepositExpenseCategory(String mDepositExpenseCategory) {
//		this.mDepositExpenseCategory = mDepositExpenseCategory;
//	}

	public List<ProjectShareAuthorization> getShareAuthorizations() {
		return this.getMany(ProjectShareAuthorization.class, "projectId");
	}

	@Override
	public void save() {
		if (this.getOwnerUserId() == null) {
			this.setOwnerUserId(HyjApplication.getInstance().getCurrentUser()
					.getId());
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

	public Double getExpenseTotal(){
        Double projectExpenseTotal = this.mExpenseTotal;

		for(Iterator<Project> it = this.getSubProjectsArray().iterator(); it.hasNext();){
			Project subProject = it.next();
			Double rate = 1.0;
			if(!subProject.getCurrencyId().equalsIgnoreCase(this.getCurrencyId())){
				rate = Exchange.getExchangeRate(subProject.getCurrencyId(), this.getCurrencyId());
				if(rate == null){
					return null;
				}
			}
			projectExpenseTotal += subProject.getExpenseTotal() * rate;
		}
		return projectExpenseTotal;
	}
	
	public Double getIncomeTotal(){
        Double projectIncomeTotal = this.mIncomeTotal;
		
		for(Iterator<Project> it = this.getSubProjectsArray().iterator(); it.hasNext();){
			Project subProject = it.next();
			Double rate = 1.0;
			if(!subProject.getCurrencyId().equalsIgnoreCase(this.getCurrencyId())){
				rate = Exchange.getExchangeRate(subProject.getCurrencyId(), this.getCurrencyId());
				if(rate == null){
					return null;
				}
			}
			projectIncomeTotal += subProject.getIncomeTotal() * rate;
		}
		return projectIncomeTotal;
	}
	
	public Double getDepositTotal(){
        Double projectDepositTotal = this.mDepositTotal;
		
		for(Iterator<Project> it = this.getSubProjectsArray().iterator(); it.hasNext();){
			Project subProject = it.next();
			Double rate = 1.0;
			if(!subProject.getCurrencyId().equalsIgnoreCase(this.getCurrencyId())){
				rate = Exchange.getExchangeRate(subProject.getCurrencyId(), this.getCurrencyId());
				if(rate == null){
					return null;
				}
			}
			projectDepositTotal += subProject.getDepositTotal() * rate;
		}
		return projectDepositTotal;
	}
	
	public String getRemarkName() {
		ProjectRemark projectRemark = new Select().from(ProjectRemark.class).where("projectId=?", this.getId()).executeSingle();
		if(projectRemark != null){
			return projectRemark.getRemark();
		}
		return null;
	}	
	
}
