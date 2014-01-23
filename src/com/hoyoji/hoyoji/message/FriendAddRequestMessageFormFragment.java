package com.hoyoji.hoyoji.message;

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
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.Project;


public class FriendAddRequestMessageFormFragment extends HyjUserFormFragment {
	private final static int GET_PARENT_PROJECT_ID = 1;
	
	private HyjModelEditor mMessageEditor = null;
	private EditText mEditTextDetail = null;
	private EditText mEditTextToUser = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.message_formfragment_friendaddrequestmessage;
	}
	
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		Message friendAddRequestMessage;
		
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			friendAddRequestMessage =  new Select().from(Project.class).where("_id=?", modelId).executeSingle();
		} else {
			friendAddRequestMessage = new Message();
		}
		mMessageEditor = friendAddRequestMessage.newModelEditor();
		
		mEditTextDetail = (EditText) getView().findViewById(R.id.friendAddRequestMessageFormFragment_editText_detail);
		mEditTextDetail.setText(friendAddRequestMessage.getDetail());
		
		mEditTextToUser = (EditText) getView().findViewById(R.id.friendAddRequestMessageFormFragment_editText_toUser);
		mEditTextToUser.setText(friendAddRequestMessage.getToUserId());
		
		
	}
	
	private void fillData(){
		Project modelCopy = (Project) mMessageEditor.getModelCopy();
		modelCopy.setName(mEditTextDetail.getText().toString().trim());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		
		mEditTextDetail.setError(mMessageEditor.getValidationError("name"));
		
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		mMessageEditor.validate();
		
		if(mMessageEditor.hasValidationErrors()){
			showValidatioErrors();
		} else {
			mMessageEditor.save();
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
