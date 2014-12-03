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
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjTextInputFormFragment;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeField;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.models.Event;
import com.hoyoji.hoyoji.models.Project;

public class ProjectEventFormFragment extends HyjUserFormFragment {
	private static final int GET_REMARK = 1;

	private HyjModelEditor<Event> mEventEditor = null;
	private HyjTextField mTextFieldName = null;
	private HyjTextField mProjectName = null;
	
	private HyjRemarkField mRemarkFieldDescription = null;
	
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjDateTimeField mDateTimeFieldStartDate = null;
	private HyjDateTimeField mDateTimeFieldEndDate = null;
	

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
			mDateTimeFieldDate.setText(event.getDate());
			mDateTimeFieldStartDate.setText(event.getStartDate());
			mDateTimeFieldEndDate.setText(event.getEndDate());
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
		
		if (modelId == -1){
			this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}else{
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    super.onCreateOptionsMenu(menu, inflater);
//	    if(mMoneyAccountEditor!= null && mMoneyAccountEditor.getModel().get_mId() != null && mMoneyAccountEditor.getModel().getAccountType().equalsIgnoreCase("Debt")){
//	    	setSaveActionEnable(false);
//	    }
	}
	
	
	private void fillData() {
		Event modelCopy = (Event) mEventEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setStartDate(mDateTimeFieldStartDate.getText());
		modelCopy.setEndDate(mDateTimeFieldEndDate.getText());
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
