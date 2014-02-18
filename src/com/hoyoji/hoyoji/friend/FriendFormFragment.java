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
import com.hoyoji.android.hyjframework.view.HyjDateTimeField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.FriendCategory;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.friend.FriendCategoryListFragment;


public class FriendFormFragment extends HyjUserFormFragment {
	private final static int GET_FRIEND_CATEGORY_ID = 1;
	
	private HyjModelEditor mFriendEditor = null;
	private HyjSelectorField mSelectorFieldFriendCategory = null;
	private HyjTextField mTextFieldNickName = null;
	private HyjDateTimeField mDateTimeDate = null;
	
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
		
				
		mTextFieldNickName = (HyjTextField) getView().findViewById(R.id.friendFormFragment_textField_nickName);
		mTextFieldNickName.setText(friend.getNickName());
		
		FriendCategory friendCategory = friend.getFriendCategory();
		mSelectorFieldFriendCategory = (HyjSelectorField) getView().findViewById(R.id.friendFormFragment_selectorField_friend_category);
		
		if(friendCategory != null){
			mSelectorFieldFriendCategory.setModelId(friendCategory.getId());
			mSelectorFieldFriendCategory.setText(friendCategory.getName());
		}
		mSelectorFieldFriendCategory.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				FriendFormFragment.this
				.openActivityWithFragmentForResult(FriendCategoryListFragment.class, R.string.friendCategoryListFragment_title_select_friend_category, null, GET_FRIEND_CATEGORY_ID);
			}
		});
		
	}
	
	private void fillData(){
		Friend modelCopy = (Friend) mFriendEditor.getModelCopy();
		modelCopy.setNickName(mTextFieldNickName.getText().toString().trim());
		modelCopy.setFriendCategoryId(mSelectorFieldFriendCategory.getModelId());

		HyjUtil.displayToast(this.mDateTimeDate.getText().toString());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		mSelectorFieldFriendCategory.setError(mFriendEditor.getValidationError("friendCategory"));
		mTextFieldNickName.setError(mFriendEditor.getValidationError("nickName"));
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
             case GET_FRIEND_CATEGORY_ID:
            	 if(resultCode == Activity.RESULT_OK){
            		long _id = data.getLongExtra("MODEL_ID", -1);
            		FriendCategory friendCategory = FriendCategory.load(FriendCategory.class, _id);
            		mSelectorFieldFriendCategory.setText(friendCategory.getName());
            		mSelectorFieldFriendCategory.setModelId(friendCategory.getId());
            	 }
             case 2:

          }
    }
}
