package com.hoyoji.hoyoji.project;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjBooleanView;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.friend.FriendListFragment;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.User;


public class MemberFormFragment extends HyjUserFormFragment {
	private final static int GET_Friend_ID = 2;
	
	private HyjModelEditor<ProjectShareAuthorization> mProjectShareAuthorizationEditor = null;
	private HyjTextField mTextFieldProjectName = null;
	private HyjNumericField mNumericFieldSharePercentage = null;
	private HyjBooleanView mBooleanFieldSharePercentageType = null;
	private HyjSelectorField mSelectorFieldFriend = null;
	private CheckBox mCheckBoxShareAllSubProjects = null;
	
	private CheckBox mCheckBoxShareAuthExpenseSelf = null;
	private CheckBox mCheckBoxShareAuthExpenseAdd = null;
	private CheckBox mCheckBoxShareAuthExpenseEdit = null;
	private CheckBox mCheckBoxShareAuthExpenseDelete = null;

	private CheckBox mCheckBoxShareAuthIncomeSelf = null;
	private CheckBox mCheckBoxShareAuthIncomeAdd = null;
	private CheckBox mCheckBoxShareAuthIncomeEdit = null;
	private CheckBox mCheckBoxShareAuthIncomeDelete = null;

	private CheckBox mCheckBoxShareAuthBorrowSelf = null;
	private CheckBox mCheckBoxShareAuthBorrowAdd = null;
	private CheckBox mCheckBoxShareAuthBorrowEdit = null;
	private CheckBox mCheckBoxShareAuthBorrowDelete = null;

	private CheckBox mCheckBoxShareAuthLendSelf = null;
	private CheckBox mCheckBoxShareAuthLendAdd = null;
	private CheckBox mCheckBoxShareAuthLendEdit = null;
	private CheckBox mCheckBoxShareAuthLendDelete = null;
	
	private CheckBox mCheckBoxShareAuthReturnSelf = null;
	private CheckBox mCheckBoxShareAuthReturnAdd = null;
	private CheckBox mCheckBoxShareAuthReturnEdit = null;
	private CheckBox mCheckBoxShareAuthReturnDelete = null;

	private CheckBox mCheckBoxShareAuthPaybackSelf = null;
	private CheckBox mCheckBoxShareAuthPaybackAdd = null;
	private CheckBox mCheckBoxShareAuthPaybackEdit = null;
	private CheckBox mCheckBoxShareAuthPaybackDelete = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.project_formfragment_member;
	}
	
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		ProjectShareAuthorization projectShareAuthorization;
		
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		Long projectId = intent.getLongExtra("PROJECT_ID", -1);
		if(modelId != -1){
			projectShareAuthorization =  new Select().from(ProjectShareAuthorization.class).where("_id=?", modelId).executeSingle();
		} else {
			projectShareAuthorization = new ProjectShareAuthorization();
			Project project = Project.load(Project.class, projectId);
			projectShareAuthorization.setProjectId(project.getId());
		}
		
		mProjectShareAuthorizationEditor = projectShareAuthorization.newModelEditor();
		
		mTextFieldProjectName = (HyjTextField) getView().findViewById(R.id.memberFormFragment_textField_projectName);
		mTextFieldProjectName.setText(projectShareAuthorization.getProject().getDisplayName());
		mTextFieldProjectName.setEnabled(false);
		
		mNumericFieldSharePercentage = (HyjNumericField) getView().findViewById(R.id.memberFormFragment_textField_sharePercentage);
		mNumericFieldSharePercentage.setNumber(projectShareAuthorization.getSharePercentage());
		
		mBooleanFieldSharePercentageType = (HyjBooleanView) getView().findViewById(R.id.memberFormFragment_textField_sharePercentageType);
		mBooleanFieldSharePercentageType.setBoolean(projectShareAuthorization.getSharePercentageType().equals("Average"));
		mBooleanFieldSharePercentageType.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mBooleanFieldSharePercentageType.setBoolean(!mBooleanFieldSharePercentageType.getBoolean());
			}
		});
		
		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.memberFormFragment_selectorField_friend);
		mSelectorFieldFriend.setEnabled(modelId == -1);
		if(modelId != -1){
		Friend friend = new Select().from(Friend.class).where("friendUserId=?", projectShareAuthorization.getFriendUserId()).executeSingle();
			if(friend != null){
				mSelectorFieldFriend.setModelId(friend.getFriendUserId());
				mSelectorFieldFriend.setText(friend.getDisplayName());
			} else {
				User user = new Select().from(User.class).where("id=?", projectShareAuthorization.getFriendUserId()).executeSingle();
				if(user != null){
					mSelectorFieldFriend.setModelId(user.getId());
					mSelectorFieldFriend.setText(user.getDisplayName());
				}
			}
		}
		mSelectorFieldFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MemberFormFragment.this.openActivityWithFragmentForResult(FriendListFragment.class, R.string.memberFormFragment_editText_hint_friend, null, GET_Friend_ID);
			}
		});	
		
		mCheckBoxShareAllSubProjects = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAllSubProjects);
		mCheckBoxShareAllSubProjects.setChecked(projectShareAuthorization.getShareAllSubProjects());
	
		mCheckBoxShareAuthExpenseSelf = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_expense_self);
		mCheckBoxShareAuthExpenseSelf.setChecked(projectShareAuthorization.getProjectShareMoneyExpenseOwnerDataOnly());
		mCheckBoxShareAuthExpenseAdd = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_expense_add);
		mCheckBoxShareAuthExpenseAdd.setChecked(projectShareAuthorization.getProjectShareMoneyExpenseAddNew());
		mCheckBoxShareAuthExpenseEdit = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_expense_edit);
		mCheckBoxShareAuthExpenseEdit.setChecked(projectShareAuthorization.getProjectShareMoneyExpenseEdit());
		mCheckBoxShareAuthExpenseDelete = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_expense_delete);
		mCheckBoxShareAuthExpenseDelete.setChecked(projectShareAuthorization.getProjectShareMoneyExpenseDelete());
				
		mCheckBoxShareAuthIncomeSelf = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_income_self);
		mCheckBoxShareAuthIncomeSelf.setChecked(projectShareAuthorization.getProjectShareMoneyIncomeOwnerDataOnly());
		mCheckBoxShareAuthIncomeAdd = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_income_add);
		mCheckBoxShareAuthIncomeAdd.setChecked(projectShareAuthorization.getProjectShareMoneyIncomeAddNew());
		mCheckBoxShareAuthIncomeEdit = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_income_edit);
		mCheckBoxShareAuthIncomeEdit.setChecked(projectShareAuthorization.getProjectShareMoneyIncomeEdit());
		mCheckBoxShareAuthIncomeDelete = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_income_delete);
		mCheckBoxShareAuthIncomeDelete.setChecked(projectShareAuthorization.getProjectShareMoneyIncomeDelete());
		
		mCheckBoxShareAuthBorrowSelf = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_borrow_self);
		mCheckBoxShareAuthBorrowSelf.setChecked(projectShareAuthorization.getProjectShareMoneyBorrowOwnerDataOnly());
		mCheckBoxShareAuthBorrowAdd = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_borrow_add);
		mCheckBoxShareAuthBorrowAdd.setChecked(projectShareAuthorization.getProjectShareMoneyBorrowAddNew());
		mCheckBoxShareAuthBorrowEdit = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_borrow_edit);
		mCheckBoxShareAuthBorrowEdit.setChecked(projectShareAuthorization.getProjectShareMoneyBorrowEdit());
		mCheckBoxShareAuthBorrowDelete = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_borrow_delete);
		mCheckBoxShareAuthBorrowDelete.setChecked(projectShareAuthorization.getProjectShareMoneyBorrowDelete());
		
		mCheckBoxShareAuthLendSelf = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_lend_self);
		mCheckBoxShareAuthLendSelf.setChecked(projectShareAuthorization.getProjectShareMoneyLendOwnerDataOnly());
		mCheckBoxShareAuthLendAdd = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_lend_add);
		mCheckBoxShareAuthLendAdd.setChecked(projectShareAuthorization.getProjectShareMoneyLendAddNew());
		mCheckBoxShareAuthLendEdit = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_lend_edit);
		mCheckBoxShareAuthLendEdit.setChecked(projectShareAuthorization.getProjectShareMoneyLendEdit());
		mCheckBoxShareAuthLendDelete = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_lend_delete);
		mCheckBoxShareAuthLendDelete.setChecked(projectShareAuthorization.getProjectShareMoneyLendDelete());
		
		mCheckBoxShareAuthReturnSelf = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_return_self);
		mCheckBoxShareAuthReturnSelf.setChecked(projectShareAuthorization.getProjectShareMoneyReturnOwnerDataOnly());
		mCheckBoxShareAuthReturnAdd = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_return_add);
		mCheckBoxShareAuthReturnAdd.setChecked(projectShareAuthorization.getProjectShareMoneyReturnAddNew());
		mCheckBoxShareAuthReturnEdit = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_return_edit);
		mCheckBoxShareAuthReturnEdit.setChecked(projectShareAuthorization.getProjectShareMoneyReturnEdit());
		mCheckBoxShareAuthReturnDelete = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_return_delete);
		mCheckBoxShareAuthReturnDelete.setChecked(projectShareAuthorization.getProjectShareMoneyReturnDelete());

		mCheckBoxShareAuthPaybackSelf = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_payback_self);
		mCheckBoxShareAuthPaybackSelf.setChecked(projectShareAuthorization.getProjectShareMoneyPaybackOwnerDataOnly());
		mCheckBoxShareAuthPaybackAdd = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_payback_add);
		mCheckBoxShareAuthPaybackAdd.setChecked(projectShareAuthorization.getProjectShareMoneyPaybackAddNew());
		mCheckBoxShareAuthPaybackEdit = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_payback_edit);
		mCheckBoxShareAuthPaybackEdit.setChecked(projectShareAuthorization.getProjectShareMoneyPaybackEdit());
		mCheckBoxShareAuthPaybackDelete = (CheckBox)getView().findViewById(R.id.memberFormFragment_checkBox_shareAuthorization_payback_delete);
		mCheckBoxShareAuthPaybackDelete.setChecked(projectShareAuthorization.getProjectShareMoneyPaybackDelete());
	}
	
	private void fillData() {
		ProjectShareAuthorization modelCopy = mProjectShareAuthorizationEditor.getModelCopy();
		modelCopy.setSharePercentage(mNumericFieldSharePercentage.getNumber());
		modelCopy.setSharePercentageType(this.mBooleanFieldSharePercentageType.getBoolean() ? "Average" : "Fix");
		modelCopy.setShareAllSubProjects(mCheckBoxShareAllSubProjects.isChecked());
		
		modelCopy.setFriendUserId(mSelectorFieldFriend.getModelId());
		
		modelCopy.setProjectShareMoneyExpenseOwnerDataOnly(mCheckBoxShareAuthExpenseSelf.isChecked());
		modelCopy.setProjectShareMoneyExpenseAddNew(mCheckBoxShareAuthExpenseAdd.isChecked());
		modelCopy.setProjectShareMoneyExpenseEdit(mCheckBoxShareAuthExpenseEdit.isChecked());
		modelCopy.setProjectShareMoneyExpenseDelete(mCheckBoxShareAuthExpenseDelete.isChecked());	
		
		modelCopy.setProjectShareMoneyIncomeOwnerDataOnly(mCheckBoxShareAuthIncomeSelf.isChecked());
		modelCopy.setProjectShareMoneyIncomeAddNew(mCheckBoxShareAuthIncomeAdd.isChecked());
		modelCopy.setProjectShareMoneyIncomeEdit(mCheckBoxShareAuthIncomeEdit.isChecked());
		modelCopy.setProjectShareMoneyIncomeDelete(mCheckBoxShareAuthIncomeDelete.isChecked());
		
		modelCopy.setProjectShareMoneyBorrowOwnerDataOnly(mCheckBoxShareAuthBorrowSelf.isChecked());
		modelCopy.setProjectShareMoneyBorrowAddNew(mCheckBoxShareAuthBorrowAdd.isChecked());
		modelCopy.setProjectShareMoneyBorrowEdit(mCheckBoxShareAuthBorrowEdit.isChecked());
		modelCopy.setProjectShareMoneyBorrowDelete(mCheckBoxShareAuthBorrowDelete.isChecked());
		
		modelCopy.setProjectShareMoneyLendOwnerDataOnly(mCheckBoxShareAuthLendSelf.isChecked());
		modelCopy.setProjectShareMoneyLendAddNew(mCheckBoxShareAuthLendAdd.isChecked());
		modelCopy.setProjectShareMoneyLendEdit(mCheckBoxShareAuthLendEdit.isChecked());
		modelCopy.setProjectShareMoneyLendDelete(mCheckBoxShareAuthLendDelete.isChecked());
		
		modelCopy.setProjectShareMoneyReturnOwnerDataOnly(mCheckBoxShareAuthReturnSelf.isChecked());
		modelCopy.setProjectShareMoneyReturnAddNew(mCheckBoxShareAuthReturnAdd.isChecked());
		modelCopy.setProjectShareMoneyReturnEdit(mCheckBoxShareAuthReturnEdit.isChecked());
		modelCopy.setProjectShareMoneyReturnDelete(mCheckBoxShareAuthReturnDelete.isChecked());
		
		modelCopy.setProjectShareMoneyPaybackOwnerDataOnly(mCheckBoxShareAuthPaybackSelf.isChecked());
		modelCopy.setProjectShareMoneyPaybackAddNew(mCheckBoxShareAuthPaybackAdd.isChecked());
		modelCopy.setProjectShareMoneyPaybackEdit(mCheckBoxShareAuthPaybackEdit.isChecked());
		modelCopy.setProjectShareMoneyPaybackDelete(mCheckBoxShareAuthPaybackDelete.isChecked());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		
		mNumericFieldSharePercentage.setError(mProjectShareAuthorizationEditor.getValidationError("sharePercentage"));
		mSelectorFieldFriend.setError(mProjectShareAuthorizationEditor.getValidationError("currency"));
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();

		mProjectShareAuthorizationEditor.validate();
		
		if(mProjectShareAuthorizationEditor.hasValidationErrors()){
			showValidatioErrors();
		} else {
			try {
				ActiveAndroid.beginTransaction();
				mProjectShareAuthorizationEditor.save();
				HyjUtil.displayToast(R.string.app_save_success);
				ActiveAndroid.setTransactionSuccessful();
				getActivity().finish();
			} finally {
			    ActiveAndroid.endTransaction();
			}
		}
	}	
	
	 @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode){
              case GET_Friend_ID:
            	 if(resultCode == Activity.RESULT_OK){
            		 HyjUtil.displayToast(String.valueOf(data.getLongExtra("MODEL_ID", -1)));
            		 long _id = data.getLongExtra("MODEL_ID", -1);
 	         		 Friend friend = Friend.load(Friend.class, _id);
 	         		 if(friend.getFriendUser() != null){
 	 	         		 mSelectorFieldFriend.setText(friend.getDisplayName());
 	 	         		 mSelectorFieldFriend.setModelId(friend.getFriendUserId());
 	         		 } else {
 	         			 HyjUtil.displayToast(R.string.memberFormFragment_toast_cannot_select_local_friend);
 	         		 }
            	 }
            	 break;
          }
    }
	 
}
