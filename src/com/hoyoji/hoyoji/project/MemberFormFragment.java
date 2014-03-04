package com.hoyoji.hoyoji.project;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.view.HyjBooleanView;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.friend.AddFriendListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.User;


public class MemberFormFragment extends HyjUserFormFragment {
	private final static int GET_Friend_ID = 2;
	
	private HyjModelEditor<ProjectShareAuthorization> mProjectShareAuthorizationEditor = null;
	private List<ProjectShareAuthorization> mProjectShareAuthorizations;
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
		final ProjectShareAuthorization projectShareAuthorization;
		Project project;
		
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		Long projectId = intent.getLongExtra("PROJECT_ID", -1);
		if(modelId != -1){
			projectShareAuthorization =  new Select().from(ProjectShareAuthorization.class).where("_id=?", modelId).executeSingle();
			project = projectShareAuthorization.getProject();
		} else {
			projectShareAuthorization = new ProjectShareAuthorization();
			project = Project.load(Project.class, projectId);
			projectShareAuthorization.setProjectId(project.getId());
			projectShareAuthorization.setState("Accept");
		}

		mProjectShareAuthorizationEditor = projectShareAuthorization.newModelEditor();
//		final ProjectShareAuthorization projectShareAuthorization = mProjectShareAuthorizationEditor.getModelCopy();
		
		mProjectShareAuthorizations = new Select().from(ProjectShareAuthorization.class).where("projectId = ? AND state <> ? AND id <> ?", project.getId(), "Delete", projectShareAuthorization.getId()).execute();
		mProjectShareAuthorizations.add(mProjectShareAuthorizationEditor.getModelCopy());
		
		mTextFieldProjectName = (HyjTextField) getView().findViewById(R.id.memberFormFragment_textField_projectName);
		mTextFieldProjectName.setText(project.getDisplayName());
		mTextFieldProjectName.setEnabled(false);
		
		mNumericFieldSharePercentage = (HyjNumericField) getView().findViewById(R.id.memberFormFragment_textField_sharePercentage);
		mNumericFieldSharePercentage.setNumber(projectShareAuthorization.getSharePercentage());
		mNumericFieldSharePercentage.setEnabled(!projectShareAuthorization.getSharePercentageType().equalsIgnoreCase("Average"));
		if(modelId == -1){
			setAveragePercentage(mProjectShareAuthorizationEditor.getModelCopy());
		}
		
		mBooleanFieldSharePercentageType = (HyjBooleanView) getView().findViewById(R.id.memberFormFragment_textField_sharePercentageType);
		mBooleanFieldSharePercentageType.setBoolean(projectShareAuthorization.getSharePercentageType().equalsIgnoreCase("Average"));
		mBooleanFieldSharePercentageType.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(!mBooleanFieldSharePercentageType.getBoolean()){
					mBooleanFieldSharePercentageType.setBoolean(true);
					mNumericFieldSharePercentage.setEnabled(false);
					setAveragePercentage(mProjectShareAuthorizationEditor.getModelCopy());
				} else {
					mBooleanFieldSharePercentageType.setBoolean(false);
					mNumericFieldSharePercentage.setEnabled(true);
				}
			}
		});
		
		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.memberFormFragment_selectorField_friend);
		if(modelId != -1 && projectShareAuthorization.getFriendUserId() != null){
			mSelectorFieldFriend.setEnabled(false);
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
	
	private void setAveragePercentage(ProjectShareAuthorization projectShareAuthorization) {
		//将成员设成平均分摊
		double fixedPercentageTotal = 0.0;
//		double averageTotal = 0.0;
		int numOfAverage = 0;
		for(ProjectShareAuthorization psa : mProjectShareAuthorizations) {
			if(!psa.getSharePercentageType().equalsIgnoreCase("Average") && psa != projectShareAuthorization){
				fixedPercentageTotal += psa.getSharePercentage();
			} else {
				numOfAverage++;
//				averageTotal += psa.getSharePercentage();
			}
		}
		double averageAmount = HyjUtil.toFixed2((100.0 - Math.min(fixedPercentageTotal, 100.0)) / numOfAverage);
		double adjsutedAverageAmount = HyjUtil.toFixed2(averageAmount + (100.0 - fixedPercentageTotal - averageAmount * numOfAverage));
		for(ProjectShareAuthorization psa : mProjectShareAuthorizations) {
			if(psa.getSharePercentage().doubleValue() == adjsutedAverageAmount && psa != projectShareAuthorization){
				mNumericFieldSharePercentage.setNumber(averageAmount);
				return;
			} 
		}
		
		mNumericFieldSharePercentage.setNumber(adjsutedAverageAmount);
	}

	private void fillData() {
		ProjectShareAuthorization modelCopy = mProjectShareAuthorizationEditor.getModelCopy();
		modelCopy.setSharePercentage(mNumericFieldSharePercentage.getNumber());
		modelCopy.setSharePercentageType(mBooleanFieldSharePercentageType.getBoolean() ? "Average" : "Fixed");
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
		validateFixedPercentageTotal(mProjectShareAuthorizationEditor);
		
		if(mProjectShareAuthorizationEditor.hasValidationErrors()){
			showValidatioErrors();
		} else if(mProjectShareAuthorizationEditor.getModelCopy().get_mId() == null) {

			saveAverageTotal();				
			
			//去服务器上查找是否已经添加过共享给该好友
			HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
				@Override
				public void finishCallback(Object object) {
					JSONArray jsonArray = (JSONArray) object;
					if (jsonArray.optJSONArray(0).length() > 0) {
						((HyjActivity) MemberFormFragment.this.getActivity())
								.dismissProgressDialog();
						HyjUtil.displayToast(R.string.memberFormFragment_toast_friend_already_exists);
					} else {
						sendNewProjectShareAuthorizationToServer();
					}
				}

				@Override
				public void errorCallback(Object object) {
					displayError(object);
				}
			};

			try {
				JSONObject data = new JSONObject();
				data.put("__dataType", "ProjectShareAuthorization");
				data.put("projectId", mProjectShareAuthorizationEditor.getModelCopy().getProjectId());
				data.put("friendUserId", mProjectShareAuthorizationEditor.getModelCopy().getFriendUserId());
				data.put("state", "Accept");
				
				HyjHttpPostAsyncTask.newInstance(serverCallbacks,
						"[" + data.toString() + "]", "getData");
				((HyjActivity) this.getActivity()).displayProgressDialog(
						R.string.memberFormFragment_title_addnew,
						R.string.memberFormFragment_progress_adding);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		} else {
			HyjUtil.displayToast("cannot edit");
		}
	}	

	private void sendNewProjectShareAuthorizationToServer() {
		
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				try {
					ActiveAndroid.beginTransaction();
					mProjectShareAuthorizationEditor.getModelCopy().setState("Wait");
					mProjectShareAuthorizationEditor.save();
					ActiveAndroid.setTransactionSuccessful();
					HyjUtil.displayToast(R.string.memberFormFragment_toast_share_request_sent_success);
					getActivity().finish();
				}  finally {
				    ActiveAndroid.endTransaction();
				}
				
				((HyjActivity) MemberFormFragment.this.getActivity())
				.dismissProgressDialog();
			}

			@Override
			public void errorCallback(Object object) {
				displayError(object);
			}
		};

		try {
			JSONObject msg = new JSONObject();
			msg.put("__dataType", "Message");
			msg.put("id", UUID.randomUUID().toString());
			msg.put("toUserId", mProjectShareAuthorizationEditor.getModelCopy().getFriendUserId());
			msg.put("fromUserId", HyjApplication.getInstance()
					.getCurrentUser().getId());
			msg.put("type", "Project.Share.AddRequest");
			msg.put("messageState", "new");
			msg.put("messageTitle", "项目共享请求");
			msg.put("date", HyjUtil.formatDateToIOS(new Date()));
			msg.put("detail", "用户"
					+ HyjApplication.getInstance().getCurrentUser()
							.getDisplayName() + "给您共享项目: " + mProjectShareAuthorizationEditor.getModelCopy().getProject().getName());
			msg.put("messageBoxId", mProjectShareAuthorizationEditor.getModelCopy().getFriendUser().getMessageBoxId());
			msg.put("ownerUserId", mProjectShareAuthorizationEditor.getModelCopy().getFriendUserId());
			
			JSONObject msgData = new JSONObject();
			msgData.put("fromUserDisplayName", HyjApplication.getInstance().getCurrentUser().getDisplayName());
			msgData.put("toUserDisplayName", mProjectShareAuthorizationEditor.getModelCopy().getFriendUser().getDisplayName());
			msgData.put("shareAllSubProjects", mProjectShareAuthorizationEditor.getModelCopy().getShareAllSubProjects());
			msgData.put("projectShareAuthorizationId", mProjectShareAuthorizationEditor.getModelCopy().getId());
			msgData.put("fromMessageBoxId", HyjApplication.getInstance().getCurrentUser().getMessageBoxId());
			msgData.put("projectName", mProjectShareAuthorizationEditor.getModelCopy().getProject().getName());
			msgData.put("projectIds", new JSONArray("[" + mProjectShareAuthorizationEditor.getModelCopy().getProjectId()  + "]"));
			msgData.put("projectCurrencyIds", new JSONArray());
			msg.put("messageData", msgData.toString());
			

			String data = "[";
			JSONObject jsonPSA = mProjectShareAuthorizationEditor.getModelCopy().toJSON();
			jsonPSA.put("state", "Wait");
			data += jsonPSA.toString();
			data += "," + mProjectShareAuthorizationEditor.getModelCopy().getProject().toJSON().toString();
			for(ProjectShareAuthorization psa : mProjectShareAuthorizations) {
				if(psa != mProjectShareAuthorizationEditor.getModelCopy()){
					data += "," + psa.toJSON().toString();
				}
			}
			data += "," + msg.toString() + "]";
			HyjHttpPostAsyncTask.newInstance(serverCallbacks,
					data, "postData");
			
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
	}

	 private void saveAverageTotal(){
		 //重新计算所有均摊成员的占股比例并保存
			double fixedPercentageTotal = 0.0;
			int numOfAverage = 0;
			for(ProjectShareAuthorization psa : mProjectShareAuthorizations) {
				if(psa.getSharePercentageType().equalsIgnoreCase("Fixed")){
					fixedPercentageTotal += psa.getSharePercentage();
				} else {
					numOfAverage++;
				}
			}
			
			double averageTotal = 0.0;
			double averageAmount = 0.0;
			double averageTotalAmount = 100.0 - Math.min(fixedPercentageTotal, 100.0);
			if(numOfAverage > 0) {
				averageAmount = HyjUtil.toFixed2(averageTotalAmount / numOfAverage);
			}
			double diff = HyjUtil.toFixed2(100.0 - fixedPercentageTotal - averageAmount * numOfAverage);
			double adjustedAverageTotal = HyjUtil.toFixed2(averageAmount + diff);
			
			for(ProjectShareAuthorization psa : mProjectShareAuthorizations) {
				if(psa.getSharePercentageType().equalsIgnoreCase("Average")){
					if(!psa.getId().equals(mProjectShareAuthorizationEditor.getModelCopy().getId()) &&
							(psa.getSharePercentage().doubleValue() != averageAmount && 
							psa.getSharePercentage().doubleValue() != adjustedAverageTotal)){
//						HyjModelEditor<ProjectShareAuthorization> editor = psa.newModelEditor();
//						editor.getModelCopy().setSharePercentage(averageAmount);
						averageTotal += averageAmount;
//						editor.save();
					} else {
						averageTotal += psa.getSharePercentage();
					}
				}
			}
			if(HyjUtil.toFixed2(averageTotal) != HyjUtil.toFixed2(100.0 - fixedPercentageTotal)){
				mProjectShareAuthorizationEditor.getModelCopy().setSharePercentage(
						mProjectShareAuthorizationEditor.getModelCopy().getSharePercentage() + diff);
			}
	 }
	 
	 private double validateFixedPercentageTotal(HyjModelEditor<ProjectShareAuthorization> projectShareAuthorizationEditor) {
			if(projectShareAuthorizationEditor.getModelCopy().getSharePercentageType().equalsIgnoreCase("Average")){
				return 0.0;
			}
			if(projectShareAuthorizationEditor.getValidationError("sharePercentage") != null){
				return 0.0;
			}
			
			Double fixedPercentageTotal = 0.0;
			int numOfAverage = 0;
			for(ProjectShareAuthorization psa : mProjectShareAuthorizations) {
				if(psa.getSharePercentageType().equalsIgnoreCase("Fixed")){
					fixedPercentageTotal += psa.getSharePercentage();
				} else {
					numOfAverage ++;
				}
			}
			if(fixedPercentageTotal > 100.00 || (fixedPercentageTotal < 100.0 && numOfAverage == 0)){
				projectShareAuthorizationEditor.setValidationError("sharePercentage", this.getActivity().getString(R.string.memberFormFragment_editText_error_sharePercentage));
			} else {
				projectShareAuthorizationEditor.removeValidationError("sharePercentage");
			}
			return fixedPercentageTotal;
		}
	 
	 @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode){
              case GET_Friend_ID:
            	 if(resultCode == Activity.RESULT_OK){
            		 long _id = data.getLongExtra("MODEL_ID", -1);
 	         		 Friend friend = Friend.load(Friend.class, _id);
 	         		 if(friend.getFriendUserId() != null){
 	         			for(ProjectShareAuthorization psa : mProjectShareAuthorizations) {
	 	       				if(psa.getFriendUserId() != null && psa.getFriendUserId().equalsIgnoreCase(friend.getFriendUserId())){
	 	       					HyjUtil.displayToast(R.string.memberFormFragment_toast_friend_already_exists);
	 	       					return;
	 	       				}
 	         			}
 	 	         		 mSelectorFieldFriend.setText(friend.getDisplayName());
 	 	         		 mSelectorFieldFriend.setModelId(friend.getFriendUserId());
 	         		 } else {
 	         			 HyjUtil.displayToast(R.string.memberFormFragment_toast_cannot_select_local_friend);
 	         		 }
            	 }
            	 break;
          }
    }
	 

		private void displayError(Object object){
			((HyjActivity) this.getActivity())
			.dismissProgressDialog();
			JSONObject json = (JSONObject) object;
			HyjUtil.displayToast(json.optJSONObject("__summary").optString(
					"msg"));
		}
	 
}
