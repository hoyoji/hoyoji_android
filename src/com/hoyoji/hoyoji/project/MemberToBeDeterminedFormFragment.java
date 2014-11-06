package com.hoyoji.hoyoji.project;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.friend.AddFriendListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.User;


public class MemberToBeDeterminedFormFragment extends HyjUserFormFragment {
	private final static int GET_FRIEND_ID = 2;
	
	private HyjModelEditor<ProjectShareAuthorization> mProjectShareAuthorizationEditor = null;
	private List<ProjectShareAuthorization> mProjectShareAuthorizations;
	private HyjTextField mTextFieldProjectName = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.project_formfragment_member_tbd;
	}
	
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		final ProjectShareAuthorization projectShareAuthorization;
		Project project;
		
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			projectShareAuthorization =  new Select().from(ProjectShareAuthorization.class).where("_id=?", modelId).executeSingle();
			project = projectShareAuthorization.getProject();
		} else {
			projectShareAuthorization = new ProjectShareAuthorization();
			Long project_id = intent.getLongExtra("PROJECT_ID", -1);
			if(project_id != -1){
				project = Project.load(Project.class, project_id);
			} else {
				String projectId = intent.getStringExtra("PROJECTID");
				project = Project.getModel(Project.class, projectId);
			}
			projectShareAuthorization.setProjectId(project.getId());
			projectShareAuthorization.setState("NotInvite");
		}

		mProjectShareAuthorizationEditor = projectShareAuthorization.newModelEditor();
//		final ProjectShareAuthorization projectShareAuthorization = mProjectShareAuthorizationEditor.getModelCopy();
		
		mProjectShareAuthorizations = new Select().from(ProjectShareAuthorization.class).where("projectId = ? AND state <> ? AND id <> ?", project.getId(), "Delete", projectShareAuthorization.getId()).execute();
		mProjectShareAuthorizations.add(mProjectShareAuthorizationEditor.getModelCopy());
		
		mTextFieldProjectName = (HyjTextField) getView().findViewById(R.id.memberTBDFormFragment_textField_projectName);
		mTextFieldProjectName.setText(project.getDisplayName());
		mTextFieldProjectName.setEnabled(false);
		
		if(modelId != -1){
			boolean canNotEdit = false;
			if(!mProjectShareAuthorizationEditor.getModel().getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
				canNotEdit = true;
			}
			if(canNotEdit){
				getView().findViewById(R.id.button_save).setVisibility(View.GONE);
				if(this.mOptionsMenu != null){
					hideSaveAction();
				}
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if(mProjectShareAuthorizationEditor != null && mProjectShareAuthorizationEditor.getModel().get_mId() != null){
				boolean canNotEdit = false;
				if(!mProjectShareAuthorizationEditor.getModel().getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
					canNotEdit = true;
				} else {
					// 如果是待定成员也不能修改
					 if(mProjectShareAuthorizationEditor.getModel().getLocalFriendId() != null){
						 Friend f = Friend.getModel(Friend.class, mProjectShareAuthorizationEditor.getModel().getLocalFriendId());
						 if(f != null && f.getToBeDetermined()){
							 canNotEdit = true;
						 }
					 }
				}
				if(canNotEdit){
					hideSaveAction();
				}
		}
	}
	
	
	private void fillData() {
		ProjectShareAuthorization modelCopy = mProjectShareAuthorizationEditor.getModelCopy();
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		if(mProjectShareAuthorizationEditor.getModel().get_mId() != null && !mProjectShareAuthorizationEditor.getModel().getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
			return;
		}
		
		fillData();

		mProjectShareAuthorizationEditor.validate();
		
		if(mProjectShareAuthorizationEditor.hasValidationErrors()){
			showValidatioErrors();
		} else if(mProjectShareAuthorizationEditor.getModelCopy().get_mId() == null){ 
			sendNewProjectShareAuthorizationToServer();
		} else {
			sendEditProjectShareAuthorizationToServer();
			
		}
	}	

		private void sendInviteMessage() {
			
			HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
				@Override
				public void finishCallback(Object object) {
					loadProjectProjectShareAuthorizations(object);
					HyjUtil.displayToast(R.string.memberTBDFormFragment_toast_share_request_sent_success);
				}

				@Override
				public void errorCallback(Object object) {
					displayError(object);
				}
			};

			mProjectShareAuthorizationEditor.getModelCopy().setState("Wait");
			JSONObject msg = getInviteMessage();
			String data = "[" + msg.toString() + "]";
			
			HyjHttpPostAsyncTask.newInstance(serverCallbacks, data, "projectShareInvite");
			((HyjActivity) MemberToBeDeterminedFormFragment.this.getActivity())
			.displayProgressDialog(
					R.string.memberTBDFormFragment_title_edit,
					R.string.memberTBDFormFragment_progress_inviting);
		}

	 
	private void sendNewProjectShareAuthorizationToServer() {
		
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				loadProjectProjectShareAuthorizations(object);
				HyjUtil.displayToast(R.string.memberTBDFormFragment_toast_share_add_success);
			}

			@Override
			public void errorCallback(Object object) {
				displayError(object);
			}
		};

//		try {
			String data = "[";
			
			JSONObject jsonPSA = mProjectShareAuthorizationEditor.getModelCopy().toJSON();
			data += jsonPSA.toString();
			
			//如果项目也是新建的，一同保存到服务器
			if(mProjectShareAuthorizationEditor.getModelCopy().getProject().isClientNew()){
				data += "," + mProjectShareAuthorizationEditor.getModelCopy().getProject().toJSON().toString();
			}
			
			JSONObject msg = getInviteMessage();
			data += "," + msg.toString() + "]";
			
			HyjHttpPostAsyncTask.newInstance(serverCallbacks, data, "projectShareAdd");
			((HyjActivity) MemberToBeDeterminedFormFragment.this.getActivity())
			.displayProgressDialog(
					R.string.memberTBDFormFragment_title_addnew,
					R.string.memberTBDFormFragment_progress_adding);
//		} catch (JSONException e1) {
//		}
	}
	
private void sendEditProjectShareAuthorizationToServer() {
		
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				loadProjectProjectShareAuthorizations(object);
				HyjUtil.displayToast(R.string.memberTBDFormFragment_toast_share_edit_success);
			}

			@Override
			public void errorCallback(Object object) {
				displayError(object);
			}
		};

//		try {
			String data = "[";
			
			JSONObject jsonPSA = mProjectShareAuthorizationEditor.getModelCopy().toJSON();
			data += jsonPSA.toString();
			
			//如果项目也是新建的，一同保存到服务器
			if(mProjectShareAuthorizationEditor.getModelCopy().getProject().isClientNew()){
				data += "," + mProjectShareAuthorizationEditor.getModelCopy().getProject().toJSON().toString();
			}
			
			JSONObject msg = new JSONObject();
			try {
				msg.put("__dataType", "Message");
				msg.put("toUserId", mProjectShareAuthorizationEditor.getModelCopy().getFriendUserId());
				msg.put("fromUserId", HyjApplication.getInstance()
						.getCurrentUser().getId());
				msg.put("type", "Project.Share.Edit");
				msg.put("messageState", "new");
				msg.put("messageTitle", "修改项目共享");
				msg.put("date", HyjUtil.formatDateToIOS(new Date()));
				msg.put("detail", "用户"
						+ HyjApplication.getInstance().getCurrentUser()
								.getDisplayName() + "修改了项目共享权限: " + mProjectShareAuthorizationEditor.getModelCopy().getProject().getName());
		//		msg.put("messageBoxId", mProjectShareAuthorizationEditor.getModelCopy().getFriendUser().getMessageBoxId());
				msg.put("ownerUserId", mProjectShareAuthorizationEditor.getModelCopy().getFriendUserId());
				
				JSONObject msgData = new JSONObject();
				msgData.put("fromUserDisplayName", HyjApplication.getInstance().getCurrentUser().getDisplayName());
				msgData.put("toUserDisplayName", mProjectShareAuthorizationEditor.getModelCopy().getFriend().getFriendUserName());
				msgData.put("shareAllSubProjects", mProjectShareAuthorizationEditor.getModelCopy().getShareAllSubProjects());
				msgData.put("projectShareAuthorizationId", mProjectShareAuthorizationEditor.getModelCopy().getId());
		//		msgData.put("fromMessageBoxId", HyjApplication.getInstance().getCurrentUser().getMessageBoxId());
				msgData.put("projectName", mProjectShareAuthorizationEditor.getModelCopy().getProject().getName());
				msgData.put("projectIds", new JSONArray("[" + mProjectShareAuthorizationEditor.getModelCopy().getProjectId()  + "]"));
				msgData.put("projectCurrencyIds", new JSONArray("[" + mProjectShareAuthorizationEditor.getModelCopy().getProject().getCurrencyId()  + "]"));
				msg.put("messageData", msgData.toString());
				
				if(mProjectShareAuthorizationEditor.getModelCopy().getState().equals("Accept")){
					//只有接受了的项目，我们才发送修改消息
					msg.put("id", UUID.randomUUID().toString());
				}
			} catch (JSONException e1) {
			}
				
				
			data += "," + msg.toString() + "]";
			
			HyjHttpPostAsyncTask.newInstance(serverCallbacks, data, "projectShareEdit");
			((HyjActivity) MemberToBeDeterminedFormFragment.this.getActivity())
			.displayProgressDialog(
					R.string.memberTBDFormFragment_title_edit,
					R.string.memberTBDFormFragment_progress_saving);
	}

	private JSONObject getInviteMessage() {
		JSONObject msg = new JSONObject();
		try {
			msg.put("__dataType", "Message");
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
	//		msg.put("messageBoxId", mProjectShareAuthorizationEditor.getModelCopy().getFriendUser().getMessageBoxId());
			msg.put("ownerUserId", mProjectShareAuthorizationEditor.getModelCopy().getFriendUserId());
			
			JSONObject msgData = new JSONObject();
			msgData.put("fromUserDisplayName", HyjApplication.getInstance().getCurrentUser().getDisplayName());
			msgData.put("toUserDisplayName", mProjectShareAuthorizationEditor.getModelCopy().getFriend().getFriendUserName());
			msgData.put("shareAllSubProjects", mProjectShareAuthorizationEditor.getModelCopy().getShareAllSubProjects());
			msgData.put("projectShareAuthorizationId", mProjectShareAuthorizationEditor.getModelCopy().getId());
	//		msgData.put("fromMessageBoxId", HyjApplication.getInstance().getCurrentUser().getMessageBoxId());
			msgData.put("projectName", mProjectShareAuthorizationEditor.getModelCopy().getProject().getName());
			msgData.put("projectIds", new JSONArray("[" + mProjectShareAuthorizationEditor.getModelCopy().getProjectId()  + "]"));
			msgData.put("projectCurrencyIds", new JSONArray("[" + mProjectShareAuthorizationEditor.getModelCopy().getProject().getCurrencyId()  + "]"));
			msg.put("messageData", msgData.toString());
			
			if(!mProjectShareAuthorizationEditor.getModelCopy().getState().equals("NotInvite")){
					// 该消息不会发给用户，只在服务器上做处理，所以没有id。在服务器上，没有id的消息是不会被保存的。
					msg.put("id", UUID.randomUUID().toString());
			}
		} catch (JSONException e) {
		}
		return msg;
		
	}

	protected void loadProjectProjectShareAuthorizations(Object object) {
//		// load new ProjectData from server
//		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
//			@Override
//			public void finishCallback(Object object) {
				try {

					JSONArray jsonObjects = (JSONArray) object;
					ActiveAndroid.beginTransaction();
//
//					for (int i = 0; i < jsonArray.length(); i++) {
//						JSONArray jsonObjects = jsonArray.getJSONArray(i);
						for (int j = 0; j < jsonObjects.length(); j++) {
							if (jsonObjects.optJSONObject(j)
									.optString("__dataType")
									.equals("ProjectShareAuthorization")) {
								String id = jsonObjects.optJSONObject(j).optString("id");
								ProjectShareAuthorization newProjectShareAuthorization = HyjModel.getModel(ProjectShareAuthorization.class, id);
								if(newProjectShareAuthorization == null){
									newProjectShareAuthorization = new ProjectShareAuthorization();
								}
								newProjectShareAuthorization.loadFromJSON(
										jsonObjects.optJSONObject(j), true);
								newProjectShareAuthorization.save();
							}
						}
//					}

					ActiveAndroid.setTransactionSuccessful();
					if(getActivity().getCallingActivity() != null){
						Intent data = new Intent();
						data.putExtra("MODELID", mProjectShareAuthorizationEditor.getModelCopy().getId());
						getActivity().setResult(Activity.RESULT_OK, data);
					}
					getActivity().finish();
//				} catch (JSONException e) {
//					e.printStackTrace();
				} finally {
					ActiveAndroid.endTransaction();
				}

				((HyjActivity) MemberToBeDeterminedFormFragment.this.getActivity())
				.dismissProgressDialog();
//			}
//
//			@Override
//			public void errorCallback(Object object) {
//				displayError(object);
//			}
//		};
//
//		JSONArray data = new JSONArray();
//		try {
//				JSONObject newObj = new JSONObject();
//				newObj = new JSONObject();
//				newObj.put("__dataType", "ProjectShareAuthorization");
//				newObj.put("main.projectId", projectId);
////				newObj.put("main.state", "Accept");
//				data.put(newObj);
//			HyjHttpPostAsyncTask.newInstance(serverCallbacks, data.toString(), "getData");
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
	}

	
//	 private void saveAverageTotal(){
//		 //重新计算所有均摊成员的占股比例并保存
//			double fixedPercentageTotal = 0.0;
//			int numOfAverage = 0;
//			for(ProjectShareAuthorization psa : mProjectShareAuthorizations) {
//				if(psa.getSharePercentageType().equalsIgnoreCase("Fixed")){
//					fixedPercentageTotal += psa.getSharePercentage();
//				} else {
//					numOfAverage++;
//				}
//			}
//			
//			double averageTotal = 0.0;
//			double averageAmount = 0.0;
//			double averageTotalAmount = 100.0 - Math.min(fixedPercentageTotal, 100.0);
//			if(numOfAverage > 0) {
//				averageAmount = HyjUtil.toFixed2(averageTotalAmount / numOfAverage);
//			}
//			double diff = HyjUtil.toFixed2(100.0 - fixedPercentageTotal - averageAmount * numOfAverage);
//			double adjustedAverageTotal = HyjUtil.toFixed2(averageAmount + diff);
//			
//			for(ProjectShareAuthorization psa : mProjectShareAuthorizations) {
//				if(psa.getSharePercentageType().equalsIgnoreCase("Average")){
//					if(!psa.getId().equals(mProjectShareAuthorizationEditor.getModelCopy().getId()) &&
//							(psa.getSharePercentage().doubleValue() != averageAmount && 
//							psa.getSharePercentage().doubleValue() != adjustedAverageTotal)){
////						HyjModelEditor<ProjectShareAuthorization> editor = psa.newModelEditor();
////						editor.getModelCopy().setSharePercentage(averageAmount);
//						averageTotal += averageAmount;
////						editor.save();
//					} else {
//						averageTotal += psa.getSharePercentage();
//					}
//				}
//			}
//			if(HyjUtil.toFixed2(averageTotal) != HyjUtil.toFixed2(100.0 - fixedPercentageTotal)){
//				mProjectShareAuthorizationEditor.getModelCopy().setSharePercentage(
//						mProjectShareAuthorizationEditor.getModelCopy().getSharePercentage() + diff);
//			}
//	 }
	 
		 @Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
	         switch(requestCode){
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
