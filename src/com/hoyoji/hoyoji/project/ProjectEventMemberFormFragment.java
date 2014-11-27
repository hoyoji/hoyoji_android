package com.hoyoji.hoyoji.project;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
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
		
		if("unSignIn".equals(eventMember.getState())) {
			signInRadioButton.setChecked(true);
		} else if("signUp".equals(eventMember.getState())){
			signUpRadioButton.setChecked(true);
		} else {
			unSignUpRadioButton.setChecked(true);
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
			modelCopy.setState("unSignUp");
		} else if(stateRadioGroup.getCheckedRadioButtonId() == R.id.projectEventMemberFormFragment_radioButton_signUp){
			modelCopy.setState("signUp");
		} else if(stateRadioGroup.getCheckedRadioButtonId() == R.id.projectEventMemberFormFragment_radioButton_signIn){
			modelCopy.setState("signIn");
		}
	}

	private void showValidatioErrors() {
		HyjUtil.displayToast(R.string.app_validation_error);

//		mTextFieldName.setError(mEventMemberEditor.getValidationError("name"));
//		mDateTimeFieldStartDate.setError(mEventMemberEditor.getValidationError("startDate"));
//		mDateTimeFieldEndDate.setError(mEventMemberEditor.getValidationError("endDate"));
		mSelectorFieldFriend.setError(mEventMemberEditor.getValidationError("friendUser"));
	}

	@Override
	public void onSave(View v) {
		super.onSave(v);

		fillData();

		mEventMemberEditor.validate();
		if (mEventMemberEditor.hasValidationErrors()) {
			showValidatioErrors();
		} else {
			doSave();
		}
		
	}

	protected void doSave() {
		mEventMemberEditor.save();
		HyjUtil.displayToast(R.string.app_save_success);
		getActivity().finish();
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
	         			} else {
	         				mSelectorFieldFriend.setText(friend.getDisplayName());
	         				mSelectorFieldFriend.setModelId(friend.getId());
	         				mSelectorFieldFriend.setTag(TAG_MEMBER_IS_LOCAL_FRIEND, true);
	         			}
					}
	       	 }
	       	 break;
		}
	}
}
