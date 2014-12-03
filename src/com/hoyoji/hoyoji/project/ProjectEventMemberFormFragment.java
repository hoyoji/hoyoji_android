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
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;

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
import com.hoyoji.android.hyjframework.view.HyjDateTimeField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.friend.FriendListFragment;
import com.hoyoji.hoyoji.models.Event;
import com.hoyoji.hoyoji.models.EventMember;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.User;

public class ProjectEventMemberFormFragment extends HyjUserFormFragment {
	private final static int GET_FRIEND_ID = 1;
	private static final int TAG_MEMBER_IS_LOCAL_FRIEND = R.id.projectEventMemberFormFragment_selectorField_friend;
	
	private HyjModelEditor<EventMember> mEventMemberEditor = null;
	private List<EventMember> mEventMembers;
//	private HyjTextField mTextFieldName = null;
	private HyjTextField mEventName = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjDateTimeField mDateTimeFieldStartDate = null;
	private HyjDateTimeField mDateTimeFieldEndDate = null;
	private HyjSelectorField mSelectorFieldFriend = null;
	private RadioGroup stateRadioGroup = null;
	private RadioButton unSignUpRadioButton = null;
	private RadioButton signUpRadioButton = null;
	private RadioButton signInRadioButton = null;
	private ProjectShareAuthorization jsonPSA = null;

	@Override
	public Integer useContentView() {
		return R.layout.project_formfragment_event_member;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		EventMember eventMember;
		Event event;

		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if (modelId != -1) {
			eventMember = new Select().from(EventMember.class).where("_id=?", modelId).executeSingle();
			event = eventMember.getEvent();
		} else {
			eventMember = new EventMember();
			Long event_id = intent.getLongExtra("EVENT_ID", -1);
			if(event_id != -1){
				event = Event.load(Event.class, event_id);
			} else {
				String eventId = intent.getStringExtra("EVENTID");
				event = Event.getModel(Event.class, eventId);
			}
			eventMember.setEventId(event.getId());
			eventMember.setState("UnSignUp");
		}
		mEventMemberEditor = eventMember.newModelEditor();
		
		mEventMembers = new Select().from(EventMember.class).where("eventId = ? AND id <> ?", event.getId(), eventMember.getId()).execute();
		mEventMembers.add(mEventMemberEditor.getModelCopy());
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.projectEventMemberFormFragment_hyjDateTimeField_date);
		mDateTimeFieldDate.setEnabled(false);
		
		mEventName = (HyjTextField) getView().findViewById(R.id.projectEventMemberFormFragment_textField_eventName);
		mEventName.setText(event.getName());
		mEventName.setEnabled(false);
		
		mDateTimeFieldStartDate = (HyjDateTimeField) getView().findViewById(R.id.projectEventMemberFormFragment_hyjDateTimeField_startDate);
		
		mDateTimeFieldEndDate = (HyjDateTimeField) getView().findViewById(R.id.projectEventMemberFormFragment_hyjDateTimeField_endDate);
		
		mDateTimeFieldDate.setText(event.getDate());
		mDateTimeFieldStartDate.setText(event.getStartDate());
		mDateTimeFieldEndDate.setText(event.getEndDate());
		mDateTimeFieldDate.setEnabled(false);
		mDateTimeFieldStartDate.setEnabled(false);
		mDateTimeFieldEndDate.setEnabled(false);
		
		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.projectEventMemberFormFragment_selectorField_friend);

		if(modelId != -1){
			mSelectorFieldFriend.setEnabled(false);
			if(eventMember.getFriendUserId() != null){
				Friend friend = new Select().from(Friend.class).where("friendUserId=?", eventMember.getFriendUserId()).executeSingle();
				if(friend != null){
					mSelectorFieldFriend.setModelId(friend.getFriendUserId());
					mSelectorFieldFriend.setText(friend.getDisplayName());
				} else {
					User user = new Select().from(User.class).where("id=?", eventMember.getFriendUserId()).executeSingle();
					if(user != null){
						mSelectorFieldFriend.setModelId(user.getId());
						mSelectorFieldFriend.setText(user.getDisplayName());
					}
				}
  				mSelectorFieldFriend.setTag(TAG_MEMBER_IS_LOCAL_FRIEND, false);
			} else if(eventMember.getLocalFriendId() != null){
				Friend friend = HyjModel.getModel(Friend.class, eventMember.getLocalFriendId());
				if(friend != null){
					mSelectorFieldFriend.setModelId(friend.getId());
					mSelectorFieldFriend.setText(friend.getDisplayName());
				} else {
					mSelectorFieldFriend.setModelId(null);
					mSelectorFieldFriend.setText(eventMember.getFriendUserName());
				}
  				mSelectorFieldFriend.setTag(TAG_MEMBER_IS_LOCAL_FRIEND, true);
			} else {
				mSelectorFieldFriend.setModelId(null);
				mSelectorFieldFriend.setText(eventMember.getFriendUserName());
  				mSelectorFieldFriend.setTag(TAG_MEMBER_IS_LOCAL_FRIEND, true);
			}
		} else {
			String friendUserId = intent.getStringExtra("FRIEND_USERID");
			if(friendUserId != null){
				Friend friend = new Select().from(Friend.class).where("friendUserId=?", friendUserId).executeSingle();
				if(friend != null){
					mSelectorFieldFriend.setModelId(friend.getFriendUserId());
					mSelectorFieldFriend.setText(friend.getDisplayName());
				} else {
					User user = new Select().from(User.class).where("id=?", friendUserId).executeSingle();
					if(user != null){
						mSelectorFieldFriend.setModelId(user.getId());
						mSelectorFieldFriend.setText(user.getDisplayName());
					}
				}
  				mSelectorFieldFriend.setTag(TAG_MEMBER_IS_LOCAL_FRIEND, false);
			} else {
				String localFriendId = intent.getStringExtra("LOCAL_FRIENDID");
				if(localFriendId != null){
					Friend friend = HyjModel.getModel(Friend.class, localFriendId);
					if(friend != null){
						mSelectorFieldFriend.setModelId(friend.getId());
						mSelectorFieldFriend.setText(friend.getDisplayName());
					} else {
						mSelectorFieldFriend.setModelId(null);
						mSelectorFieldFriend.setText(null);
					}
	  				mSelectorFieldFriend.setTag(TAG_MEMBER_IS_LOCAL_FRIEND, true);
				}
			}
		}
		mSelectorFieldFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ProjectEventMemberFormFragment.this.openActivityWithFragmentForResult(FriendListFragment.class, R.string.projectEventMemberFormFragment_selectorField_hint_friend, null, GET_FRIEND_ID);
			}
		});	
		
		stateRadioGroup = (RadioGroup) getView().findViewById(R.id.projectEventMemberFormFragment_radioGroup_autoHide);
		unSignUpRadioButton = (RadioButton) getView().findViewById(R.id.projectEventMemberFormFragment_radioButton_unSignUp);
		signUpRadioButton = (RadioButton) getView().findViewById(R.id.projectEventMemberFormFragment_radioButton_signUp);
		signInRadioButton = (RadioButton) getView().findViewById(R.id.projectEventMemberFormFragment_radioButton_signIn);
		if("SignIn".equals(eventMember.getState())) {
			signInRadioButton.setChecked(true);
		} else if("SignUp".equals(eventMember.getState())){
			signUpRadioButton.setChecked(true);
		} else if("UnSignUp".equals(eventMember.getState())){
			unSignUpRadioButton.setChecked(true);
		}
		
		if(eventMember.getLocalFriendId() == null){
			stateRadioGroup.setEnabled(false);
			unSignUpRadioButton.setEnabled(false);
			signUpRadioButton.setEnabled(false);
			signInRadioButton.setEnabled(false);
		}
		
		if (modelId == -1){
			this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}else{
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    super.onCreateOptionsMenu(menu, inflater);
	}
	
	
	private void fillData() {
		EventMember modelCopy = (EventMember) mEventMemberEditor.getModelCopy();
//		modelCopy.setDate(mDateTimeFieldDate.getText());
//		modelCopy.setStartDate(mDateTimeFieldStartDate.getText());
//		modelCopy.setEndDate(mDateTimeFieldEndDate.getText());
//		modelCopy.setName(mTextFieldName.getText().toString().trim());
		if(mSelectorFieldFriend.getTag(TAG_MEMBER_IS_LOCAL_FRIEND) != null && (Boolean)mSelectorFieldFriend.getTag(TAG_MEMBER_IS_LOCAL_FRIEND) == false){
			modelCopy.setFriendUserId(mSelectorFieldFriend.getModelId());
			modelCopy.setLocalFriendId(null);
			if(modelCopy.getFriendUserId() != null){
				modelCopy.setFriendUserName(modelCopy.getFriend().getDisplayName());
			}
		} else if(mSelectorFieldFriend.getTag(TAG_MEMBER_IS_LOCAL_FRIEND) != null && (Boolean)mSelectorFieldFriend.getTag(TAG_MEMBER_IS_LOCAL_FRIEND) == true){
			modelCopy.setLocalFriendId(mSelectorFieldFriend.getModelId());
			modelCopy.setFriendUserId(null);
			if(modelCopy.getLocalFriendId() != null){
				modelCopy.setFriendUserName(modelCopy.getFriend().getDisplayName());
			}
		}
		
		if(stateRadioGroup.getCheckedRadioButtonId() == R.id.projectEventMemberFormFragment_radioButton_unSignUp){
			modelCopy.setState("UnSignUp");
		} else if(stateRadioGroup.getCheckedRadioButtonId() == R.id.projectEventMemberFormFragment_radioButton_signUp){
			modelCopy.setState("SignUp");
		} else if(stateRadioGroup.getCheckedRadioButtonId() == R.id.projectEventMemberFormFragment_radioButton_signIn){
			modelCopy.setState("SignIn");
		}
	}

	private void showValidatioErrors() {
		HyjUtil.displayToast(R.string.app_validation_error);
		mSelectorFieldFriend.setError(mEventMemberEditor.getValidationError("friendUser"));
	}

	@Override
	public void onSave(View v) {
		super.onSave(v);

		fillData();

		mEventMemberEditor.validate();
		if (mEventMemberEditor.hasValidationErrors()) {
			showValidatioErrors();
		} else if(mEventMemberEditor.getModelCopy().getFriendUserId() != null){
//			ProjectShareAuthorization importFiendPSA = null;
//			importFiendPSA = new Select()
//					.from(ProjectShareAuthorization.class)
//					.where("projectId=? and friendUserId=?",
//							mEventMemberEditor.getModelCopy().getEvent().getProjectId(), mEventMemberEditor.getModelCopy().getFriendUserId())
//					.executeSingle();
//	        if(importFiendPSA == null){
//	        	HyjUtil.displayToast(R.string.projectEventMemberFormFragment_toast_eventMember_add_projectAuthorization);
//	        	Bundle bundle = new Bundle();
//				bundle.putLong("PROJECT_ID", mEventMemberEditor.getModelCopy().getEvent().getProject().get_mId());
//				bundle.putString("FRIEND_USERID", mEventMemberEditor.getModelCopy().getFriendUserId());
//				openActivityWithFragment(MemberFormFragment.class, R.string.memberFormFragment_title_addnew, bundle);
//	        } else {
			if(mEventMemberEditor.getModelCopy().getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
				doSave();
			} else {
				sendNewEventMemberToServer();
				doSave();
				((HyjActivity) ProjectEventMemberFormFragment.this.getActivity()).dismissProgressDialog();
			}
//	        }
		} else {
			doSave();
		}
		
	}

	protected void doSave() {
		mEventMemberEditor.save();
		HyjUtil.displayToast(R.string.app_save_success);
		getActivity().finish();
	}
	
	private double setAveragePercentage(ProjectShareAuthorization projectShareAuthorization) {
		//将成员设成平均分摊
		double fixedPercentageTotal = 0.0;
//		double averageTotal = 0.0;
		int numOfAverage = 0;
		List<ProjectShareAuthorization> mProjectShareAuthorizations;
		mProjectShareAuthorizations = new Select().from(ProjectShareAuthorization.class).where("projectId = ? AND state <> ? AND id <> ?", projectShareAuthorization.getProjectId(), "Delete", projectShareAuthorization.getId()).execute();
		mProjectShareAuthorizations.add(projectShareAuthorization);
		
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
				return averageAmount;
			} 
		}
		
		return adjsutedAverageAmount;
	}
	
	private void sendNewEventMemberToServer() {
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				loadProjectProjectShareAuthorizations(object);
				HyjUtil.displayToast(R.string.projectEventMemberFormFragment_toast_eventMember_add_success);
			}

			@Override
			public void errorCallback(Object object) {
				JSONObject json = (JSONObject) object;
				HyjUtil.displayToast(json.optJSONObject("__summary").optString("msg"));
			}
		};
		String data = "[";
		
		jsonPSA = new Select().from(ProjectShareAuthorization.class).where("projectId=? and friendUserId=?",
				mEventMemberEditor.getModelCopy().getEvent().getProjectId(), mEventMemberEditor.getModelCopy().getFriendUserId()).executeSingle();
		
		if (jsonPSA != null && jsonPSA.getState().equals("Accept")) {
			
		} else if(jsonPSA != null && jsonPSA.getState().equals("Wait")) {
			if (jsonPSA.isClientNew()) {
				data += jsonPSA.toString();
			}
		} else {
			jsonPSA = new ProjectShareAuthorization();
			jsonPSA.setProjectShareMoneyExpenseAddNew(true);
			jsonPSA.setProjectShareMoneyExpenseDelete(true);
			jsonPSA.setProjectShareMoneyExpenseEdit(true);
			jsonPSA.setProjectShareMoneyExpenseOwnerDataOnly(false);
			jsonPSA.setShareAllSubProjects(false);
			jsonPSA.setSharePercentageType("Average");
			jsonPSA.setProjectId(mEventMemberEditor.getModelCopy().getEvent().getProjectId());
			jsonPSA.setState("Wait");
			jsonPSA.setFriendUserId(mEventMemberEditor.getModelCopy().getFriendUserId());
			jsonPSA.setFriendUserName(mEventMemberEditor.getModelCopy().getFriendDisplayName());
			jsonPSA.setSharePercentage(setAveragePercentage(jsonPSA));
			jsonPSA.save();
			if (jsonPSA.isClientNew()) {
				data += jsonPSA.toJSON();
			}
		}
		
		JSONObject jsonEM = mEventMemberEditor.getModelCopy().toJSON();
		data += "," + jsonEM.toString();
		
		//如果账本也是新建的，一同保存到服务器
		if(mEventMemberEditor.getModelCopy().getEvent().getProject().isClientNew()){
			data += "," + mEventMemberEditor.getModelCopy().getEvent().getProject().toJSON().toString();
		}
		//如果活动也是新建的，一同保存到服务器
		if(mEventMemberEditor.getModelCopy().getEvent().isClientNew()){
			data += "," + mEventMemberEditor.getModelCopy().getEvent().toJSON().toString();
		}
		JSONObject msg = getInviteMessage();
		data += "," + msg.toString() + "]";
		
		HyjHttpPostAsyncTask.newInstance(serverCallbacks, data, "eventMemberAdd");
		((HyjActivity) ProjectEventMemberFormFragment.this.getActivity()).displayProgressDialog(R.string.memberFormFragment_title_addnew,R.string.memberFormFragment_progress_adding);
	}
	
	private JSONObject getInviteMessage() {
		JSONObject msg = new JSONObject();
		try {
			msg.put("__dataType", "Message");
			msg.put("toUserId", mEventMemberEditor.getModelCopy().getFriendUserId());
			msg.put("fromUserId", HyjApplication.getInstance().getCurrentUser().getId());
			msg.put("type", "Event.Member.AddRequest");
			msg.put("messageState", "new");
			msg.put("messageTitle", "邀请活动请求");
			msg.put("date", HyjUtil.formatDateToIOS(new Date()));
			msg.put("detail", "用户"
					+ HyjApplication.getInstance().getCurrentUser()
							.getDisplayName() + "邀请您参加活动: " + mEventMemberEditor.getModelCopy().getEvent().getName());
			msg.put("ownerUserId", mEventMemberEditor.getModelCopy().getFriendUserId());
			
			JSONObject msgData = new JSONObject();
			msgData.put("fromUserDisplayName", HyjApplication.getInstance().getCurrentUser().getDisplayName());
			msgData.put("toUserDisplayName", mEventMemberEditor.getModelCopy().getFriend().getFriendUserName());
			msgData.put("eventMemberId", mEventMemberEditor.getModelCopy().getId());
			msgData.put("projectId", mEventMemberEditor.getModelCopy().getEvent().getProjectId());
			msgData.put("eventId", mEventMemberEditor.getModelCopy().getEventId());
			msgData.put("projectName", mEventMemberEditor.getModelCopy().getEvent().getProject().getName());
			msgData.put("eventName", mEventMemberEditor.getModelCopy().getEvent().getName());
			
			if(jsonPSA.getState() == "Wait"){
				msgData.put("shareAllSubProjects", false);
				msgData.put("projectShareAuthorizationId", jsonPSA.getId());
				msgData.put("projectIds", new JSONArray("[" + mEventMemberEditor.getModelCopy().getEvent().getProjectId()  + "]"));
				msgData.put("projectCurrencyIds", new JSONArray("[" + mEventMemberEditor.getModelCopy().getEvent().getProject().getCurrencyId()  + "]"));
			}
			
			msg.put("messageData", msgData.toString());
			
			if(mEventMemberEditor.getModelCopy().getState().equals("UnSignUp")){
					// 该消息不会发给用户，只在服务器上做处理，所以没有id。在服务器上，没有id的消息是不会被保存的。
					msg.put("id", UUID.randomUUID().toString());
			}
		} catch (JSONException e) {
		}
		return msg;
	}
	
	protected void loadProjectProjectShareAuthorizations(Object object) {
		try {
			JSONArray jsonObjects = (JSONArray) object;
			ActiveAndroid.beginTransaction();
				for (int j = 0; j < jsonObjects.length(); j++) {
					if (jsonObjects.optJSONObject(j).optString("__dataType").equals("ProjectShareAuthorization")) {
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

			ActiveAndroid.setTransactionSuccessful();
//			if(getActivity().getCallingActivity() != null){
//				Intent data = new Intent();
//				data.putExtra("MODELID", mProjectShareAuthorizationEditor.getModelCopy().getId());
//				getActivity().setResult(Activity.RESULT_OK, data);
//			}
			getActivity().finish();
		} finally {
			ActiveAndroid.endTransaction();
		}

		((HyjActivity) ProjectEventMemberFormFragment.this.getActivity()).dismissProgressDialog();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case GET_FRIEND_ID:
	       	 if(resultCode == Activity.RESULT_OK){
	       		 long _id = data.getLongExtra("MODEL_ID", -1);
	       		 if(_id == -1){
	 	   	       		mSelectorFieldFriend.setText(null);
	 	   	       		mSelectorFieldFriend.setModelId(null);
					} else {
		         		 Friend friend = Friend.load(Friend.class, _id);
	         			 for(EventMember em : mEventMembers) {
		       				if(friend.getFriendUserId() != null){
		       					if(em.getFriendUserId() != null && em.getFriendUserId().equalsIgnoreCase(friend.getFriendUserId())){
		 	       					HyjUtil.displayToast(R.string.memberFormFragment_toast_friend_already_exists);
		 	       					return;
		 	       				}
		 	 	         	} else {
		 	       				if(em.getLocalFriendId() != null && em.getLocalFriendId().equalsIgnoreCase(friend.getId())){
		 	       					HyjUtil.displayToast(R.string.memberFormFragment_toast_friend_already_exists);
		 	       					return;
		       					}
		 	       			}
	         			 }
	         			if(friend.getFriendUserId() != null){
	         				mSelectorFieldFriend.setText(friend.getDisplayName());
	         				mSelectorFieldFriend.setModelId(friend.getFriendUserId());
	         				mSelectorFieldFriend.setTag(TAG_MEMBER_IS_LOCAL_FRIEND, false);
	         				if(friend.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
	         					signUpRadioButton.setChecked(true);
	         				} else {
	         					unSignUpRadioButton.setChecked(true);
	         				}
	         				unSignUpRadioButton.setEnabled(false);
	         				signInRadioButton.setEnabled(false);
	         				signUpRadioButton.setEnabled(false);
	         			} else {
	         				mSelectorFieldFriend.setText(friend.getDisplayName());
	         				mSelectorFieldFriend.setModelId(friend.getId());
	         				mSelectorFieldFriend.setTag(TAG_MEMBER_IS_LOCAL_FRIEND, true);
	         				unSignUpRadioButton.setEnabled(true);
	         				signInRadioButton.setEnabled(true);
	         				signUpRadioButton.setEnabled(true);
	         				signUpRadioButton.setChecked(true);
	         			}
					}
	       	 }
	       	 break;
		}
	}
}
