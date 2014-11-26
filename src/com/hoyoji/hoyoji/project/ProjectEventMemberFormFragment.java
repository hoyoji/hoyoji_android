package com.hoyoji.hoyoji.project;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
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
import com.hoyoji.hoyoji.models.User;

public class ProjectEventMemberFormFragment extends HyjUserFormFragment {
	private final static int GET_FRIEND_ID = 1;
	private static final int TAG_MEMBER_IS_LOCAL_FRIEND = R.id.memberFormFragment_selectorField_friend;
	
	private HyjModelEditor<EventMember> mEventMemberEditor = null;
	private HyjTextField mTextFieldName = null;
	private HyjTextField mProjectName = null;
	
	
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjDateTimeField mDateTimeFieldStartDate = null;
	private HyjDateTimeField mDateTimeFieldEndDate = null;
	
	private HyjSelectorField mSelectorFieldFriend = null;
	

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
			eventMember = new Select().from(Event.class).where("_id=?", modelId).executeSingle();
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
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.projectEventMemberFormFragment_hyjDateTimeField_date);
		mDateTimeFieldDate.setEnabled(false);
		

		mTextFieldName = (HyjTextField) getView().findViewById(R.id.projectEventMemberFormFragment_textField_name);
		mTextFieldName.setText(event.getName());
		
		mProjectName = (HyjTextField) getView().findViewById(R.id.projectEventMemberFormFragment_textField_projectName);
		mProjectName.setText(event.getProject().getDisplayName());
		mProjectName.setEnabled(false);
		
		mDateTimeFieldStartDate = (HyjDateTimeField) getView().findViewById(R.id.projectEventMemberFormFragment_hyjDateTimeField_startDate);
		
		mDateTimeFieldEndDate = (HyjDateTimeField) getView().findViewById(R.id.projectEventMemberFormFragment_hyjDateTimeField_endDate);
		
		if (modelId != -1) {
			mDateTimeFieldDate.setText(event.getDate());
			mDateTimeFieldStartDate.setText(event.getStartDate());
			mDateTimeFieldEndDate.setText(event.getEndDate());
		} else {
			mDateTimeFieldDate.setDate(new Date());
		}
		
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
				ProjectEventMemberFormFragment.this.openActivityWithFragmentForResult(FriendListFragment.class, R.string.memberFormFragment_editText_hint_friend, null, GET_FRIEND_ID);
			}
		});	
		
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
	}

	private void showValidatioErrors() {
		HyjUtil.displayToast(R.string.app_validation_error);

		mTextFieldName.setError(mEventMemberEditor.getValidationError("name"));
		mDateTimeFieldStartDate.setError(mEventMemberEditor.getValidationError("startDate"));
		mDateTimeFieldEndDate.setError(mEventMemberEditor.getValidationError("endDate"));
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
//		         		 Friend friend = Friend.load(Friend.class, _id);
//	         			 for(ProjectShareAuthorization psa : mProjectShareAuthorizations) {
//		       				if(friend.getFriendUserId() != null){
//		       					if(psa.getFriendUserId() != null && psa.getFriendUserId().equalsIgnoreCase(friend.getFriendUserId())){
//		 	       					HyjUtil.displayToast(R.string.memberFormFragment_toast_friend_already_exists);
//		 	       					return;
//		 	       				}
//		 	 	         	} else {
//		 	       				if(psa.getLocalFriendId() != null && psa.getLocalFriendId().equalsIgnoreCase(friend.getId())){
//		 	       					HyjUtil.displayToast(R.string.memberFormFragment_toast_friend_already_exists);
//		 	       					return;
//		       					}
//		 	       			}
//	         			 }
//	         			if(friend.getFriendUserId() != null){
//	         				mSelectorFieldFriend.setText(friend.getDisplayName());
//	         				mSelectorFieldFriend.setModelId(friend.getFriendUserId());
//	         				mSelectorFieldFriend.setTag(TAG_MEMBER_IS_LOCAL_FRIEND, false);
//	         			} else {
//	         				mSelectorFieldFriend.setText(friend.getDisplayName());
//	         				mSelectorFieldFriend.setModelId(friend.getId());
//	         				mSelectorFieldFriend.setTag(TAG_MEMBER_IS_LOCAL_FRIEND, true);
//	         			}
					}
	       	 }
	       	 break;
		}
	}
}
