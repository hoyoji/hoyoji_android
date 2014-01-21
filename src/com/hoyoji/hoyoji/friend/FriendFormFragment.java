package com.hoyoji.hoyoji.friend;

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
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.Project;


public class FriendFormFragment extends HyjUserFormFragment {
	private final static int GET_PARENT_PROJECT_ID = 1;
	
	private HyjModelEditor mFriendEditor = null;
	private EditText mEditTextFriendCategory = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.friend_formfragment_friend;
	}
	
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		Friend friend;
		
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			friend =  new Select().from(Friend.class).where("_id=?", modelId).executeSingle();
		} else {
			friend = new Friend();
		}
		mFriendEditor = friend.newModelEditor();
		
		mEditTextFriendCategory = (EditText) getView().findViewById(R.id.friendFormFragment_editText_friend_category);
		mEditTextFriendCategory.setText(friend.getFriendCategory());
		
	}
	
	private void fillData(){
		Friend modelCopy = (Friend) mFriendEditor.getModelCopy();
		modelCopy.setFriendCategory(mEditTextFriendCategory.getText().toString().trim());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		
		mEditTextFriendCategory.setError(mFriendEditor.getValidationError("friendCategory"));
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		mFriendEditor.validate();
		
		if(mFriendEditor.hasValidationErrors()){
			showValidatioErrors();
		} else {
			mFriendEditor.save();
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
