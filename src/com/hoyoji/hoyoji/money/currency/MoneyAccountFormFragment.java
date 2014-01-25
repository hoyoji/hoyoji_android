package com.hoyoji.hoyoji.money.currency;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Project;


public class MoneyAccountFormFragment extends HyjUserFormFragment {
	private final static int GET_PARENT_PROJECT_ID = 1;
	
	private HyjModelEditor mProjectEditor = null;
	private EditText mEditTextProjectName = null;
	private EditText mEditTextParentProject = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.project_formfragment_project;
	}
	
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		Project project;
		
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			project =  new Select().from(Project.class).where("_id=?", modelId).executeSingle();
		} else {
			project = new Project();
		}
		mProjectEditor = project.newModelEditor();
		
		mEditTextProjectName = (EditText) getView().findViewById(R.id.projectFormFragment_editText_projectName);
		mEditTextProjectName.setText(project.getName());
		
		mEditTextParentProject = (EditText) getView().findViewById(R.id.projectFormFragment_editText_parentProject);
		mEditTextParentProject.setText("");
		mEditTextParentProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyAccountFormFragment.this
				.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.projectListFragment_title_select_parent_project, null, GET_PARENT_PROJECT_ID);
			}
		});
		
		
	}
	
	private void fillData(){
		Project modelCopy = (Project) mProjectEditor.getModelCopy();
		modelCopy.setName(mEditTextProjectName.getText().toString().trim());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		
		mEditTextProjectName.setError(mProjectEditor.getValidationError("name"));
		
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		mProjectEditor.validate();
		
		if(mProjectEditor.hasValidationErrors()){
			showValidatioErrors();
		} else {
			mProjectEditor.save();
			HyjUtil.displayToast(R.string.app_save_success);
			getActivity().finish();
		}
	}	
	
	 @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode){
             case GET_PARENT_PROJECT_ID:
            	 if(resultCode == Activity.RESULT_OK){
            		 HyjUtil.displayToast(String.valueOf(data.getLongExtra("MODEL_ID", -1)));
            	//	 ((Project)mProjectEditor.getModelCopy()).s
            	 }
             case 2:

          }
    }
}
