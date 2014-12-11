package com.hoyoji.hoyoji.project;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjTextInputFormFragment;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeField;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.models.Event;
import com.hoyoji.hoyoji.models.EventMember;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;

public class ProjectEventFormFragment extends HyjUserFormFragment {
	private static final int GET_REMARK = 1;

	private HyjModelEditor<Event> mEventEditor = null;
	private HyjTextField mTextFieldName = null;
	private HyjTextField mProjectName = null;
	
	private HyjRemarkField mRemarkFieldDescription = null;
	
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjDateTimeField mDateTimeFieldStartDate = null;
	private HyjDateTimeField mDateTimeFieldEndDate = null;
	
	private ImageButton mButtonExpandMore;
	private LinearLayout mLinearLayoutExpandMore;
	
	@Override
	public Integer useContentView() {
		return R.layout.project_formfragment_event;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		Event event;
		Project project;

		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if (modelId != -1) {
			event = new Select().from(Event.class).where("_id=?", modelId).executeSingle();
			project = event.getProject();
		} else {
			event = new Event();
			Long project_id = intent.getLongExtra("PROJECT_ID", -1);
			if(project_id != -1){
				project = Project.load(Project.class, project_id);
			} else {
				String projectId = intent.getStringExtra("PROJECTID");
				project = Project.getModel(Project.class, projectId);
			}
			event.setProjectId(project.getId());
		}
		mEventEditor = event.newModelEditor();
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.projectEventListFragment_hyjDateTimeField_date);
		mDateTimeFieldDate.setEnabled(false);
		

		mTextFieldName = (HyjTextField) getView().findViewById(R.id.projectEventListFragment_textField_name);
		mTextFieldName.setText(event.getName());
		
		mProjectName = (HyjTextField) getView().findViewById(R.id.projectEventListFragment_textField_projectName);
		mProjectName.setText(project.getDisplayName());
		mProjectName.setEnabled(false);
		
		mDateTimeFieldStartDate = (HyjDateTimeField) getView().findViewById(R.id.projectEventListFragment_hyjDateTimeField_startDate);
		
		mDateTimeFieldEndDate = (HyjDateTimeField) getView().findViewById(R.id.projectEventListFragment_hyjDateTimeField_endDate);
		
		if (modelId != -1) {
			mDateTimeFieldDate.setTime(event.getDate());
			mDateTimeFieldStartDate.setTime(event.getStartDate());
			mDateTimeFieldEndDate.setTime(event.getEndDate());
		} else {
			mDateTimeFieldDate.setDate(new Date());
			mDateTimeFieldStartDate.setDate(new Date());
			mDateTimeFieldEndDate.setDate(new Date());
		}

		mRemarkFieldDescription = (HyjRemarkField) getView().findViewById(R.id.projectEventListFragment_HyjRemarkField_description);
		mRemarkFieldDescription.setText(event.getDescription());
		mRemarkFieldDescription.setEditable(false);
		mRemarkFieldDescription.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("TEXT", mRemarkFieldDescription.getText());
				bundle.putString("HINT", "请输入" + mRemarkFieldDescription.getLabelText());
				ProjectEventFormFragment.this.openActivityWithFragmentForResult(
								HyjTextInputFormFragment.class,
								R.string.projectEventListFragment_hyjRemarkField_hint_description,
								bundle, GET_REMARK);
			}
		});
		
		mLinearLayoutExpandMore = (LinearLayout)getView().findViewById(R.id.moneyExpenseContainerFormFragment_expandMore);
		mButtonExpandMore = (ImageButton)getView().findViewById(R.id.expand_more);
		mButtonExpandMore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mLinearLayoutExpandMore.getVisibility() == View.GONE){
					mButtonExpandMore.setImageResource(R.drawable.ic_action_collapse);
					mLinearLayoutExpandMore.setVisibility(View.VISIBLE);
				} else {
					mButtonExpandMore.setImageResource(R.drawable.ic_action_expand);
					mLinearLayoutExpandMore.setVisibility(View.GONE);
				}
			}
		});
		
		if(modelId != -1){
			if(!mEventEditor.getModel().getProject().getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
				mDateTimeFieldEndDate.setEnabled(false);
				mDateTimeFieldStartDate.setEnabled(false);
				mTextFieldName.setEnabled(false);
				getView().findViewById(R.id.button_save).setVisibility(View.GONE);
				if(this.mOptionsMenu != null){
					hideSaveAction();
				}
			}
		} else {
			mButtonExpandMore.setImageResource(R.drawable.ic_action_collapse);
			mLinearLayoutExpandMore.setVisibility(View.VISIBLE);
			this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    super.onCreateOptionsMenu(menu, inflater);
	    if(mEventEditor != null && !mEventEditor.getModel().getProject().getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
	    	hideSaveAction();
	    }
	}
	
	private void fillData() {
		Event modelCopy = (Event) mEventEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getTime());
		modelCopy.setStartDate(mDateTimeFieldStartDate.getTime());
		modelCopy.setEndDate(mDateTimeFieldEndDate.getTime());
		modelCopy.setName(mTextFieldName.getText().toString().trim());
		modelCopy.setDescription(mRemarkFieldDescription.getText().toString().trim());
	}

	private void showValidatioErrors() {
		HyjUtil.displayToast(R.string.app_validation_error);

		mTextFieldName.setError(mEventEditor.getValidationError("name"));
		mDateTimeFieldStartDate.setError(mEventEditor.getValidationError("startDate"));
		mDateTimeFieldEndDate.setError(mEventEditor.getValidationError("endDate"));
		mRemarkFieldDescription.setError(mEventEditor.getValidationError("description"));
	}

	@Override
	public void onSave(View v) {
		super.onSave(v);

		fillData();

		mEventEditor.validate();
		if (mEventEditor.hasValidationErrors()) {
			showValidatioErrors();
		} else {
			Friend toBeDeterminedFriend = new Select().from(Friend.class).where("toBeDetermined = 1 AND ownerUserId = ?", HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
			if(toBeDeterminedFriend != null){
				EventMember em = new EventMember();
				em.setEventId(mEventEditor.getModelCopy().getId());
				em.setState("SignUp");
				em.setFriendUserId(null);
				em.setLocalFriendId(toBeDeterminedFriend.getId());
				em.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
				em.setFriendUserName("待定成员");
				em.save();
			}
			doSave();
		}
//		if(mMoneyAccountEditor.getModelCopy().getAccountType().equalsIgnoreCase("Topup")){
//			if(mMoneyAccountEditor.getModelCopy().getFriendId() == null){
//				mMoneyAccountEditor.setValidationError("friend", R.string.moneyAccountFormFragment_editText_hint_friend);
//			} else {
//				mMoneyAccountEditor.removeValidationError("friend");
//			}
//		} else {
//			mMoneyAccountEditor.removeValidationError("friend");
//		}
		
	}

	protected void doSave() {
		mEventEditor.save();
		HyjUtil.displayToast(R.string.app_save_success);
		getActivity().finish();
	}
//	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case GET_REMARK:
			if (resultCode == Activity.RESULT_OK) {
				String text = data.getStringExtra("TEXT");
				mRemarkFieldDescription.setText(text);
			}
			break;

		}
	}
}
