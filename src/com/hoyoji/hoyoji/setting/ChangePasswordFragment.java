package com.hoyoji.hoyoji.setting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.KeyEvent;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.fragment.HyjFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.message.FriendMessageFormFragment;
import com.hoyoji.hoyoji.models.UserData;


public class ChangePasswordFragment extends HyjFragment {
	private EditText mEditTextOldPassword = null;
	private EditText mEditTextNewPassword1 = null;
	private EditText mEditTextNewPassword2 = null;
	String mOldPassword = "";
	String mNewPassword1 = "";
	String mNewPassword2 = "";
	boolean mHasError = false;
	
	@Override
	public Integer useContentView() {
		return R.layout.setting_fragment_changepassword;
	}
	 
	@Override
	public void onInitViewData(){
		mEditTextOldPassword = (EditText) getView().findViewById(R.id.changePasswordFragment_editText_oldPassword);
		mEditTextNewPassword1 = (EditText) getView().findViewById(R.id.changePasswordFragment_editText_newPassword1);
		mEditTextNewPassword2 = (EditText) getView().findViewById(R.id.changePasswordFragment_editText_newPassword2);
		mEditTextNewPassword2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.imeAction_changepassword || id == EditorInfo.IME_ACTION_DONE) {
							onSave();
							return true;
						}
						return false;
					}
				});
		
		getView().findViewById(R.id.changePasswordFragment_button_onSave).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				changePassword_submit(v);
				
			}
		});
		
		this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}

	private void fillData(){
		mOldPassword = mEditTextOldPassword.getText().toString().trim();
		mNewPassword1 = mEditTextNewPassword1.getText().toString();
		mNewPassword2 = mEditTextNewPassword2.getText().toString();
	}
	
	public boolean validateData(){
		boolean validatePass = true;
		fillData();
		
		if(!HyjUtil.getSHA1(mOldPassword).equals(HyjApplication.getInstance().getCurrentUser().getUserData().getPassword())){
			mEditTextOldPassword.setError(getString(R.string.changePasswordFragment_validation_wrong_oldPassword));
	   		validatePass = false;
		}else{
			mEditTextOldPassword.setError(null);
		}
		
		if(mNewPassword1.length() == 0){
	   		mEditTextNewPassword1.setError(getString(R.string.changePasswordFragment_editText_hint_newPassword1));
	   		validatePass = false;
		} else if(!mNewPassword1.matches("^.{6,18}$")){
			mEditTextNewPassword1.setError(getString(R.string.changePasswordFragment_validation_password_too_short));
	   		validatePass = false;
		} else if(checkPassWordComplexity(mNewPassword1)){
			mEditTextNewPassword1.setError(getString(R.string.changePasswordFragment_validation_password_too_simple));
			validatePass = false;
		}else {
			mEditTextNewPassword1.setError(null);
		}
		
		if(!mNewPassword1.equals(mNewPassword2)){
			mEditTextNewPassword2.setError(getString(R.string.changePasswordFragment_validation_two_password_not_same));
	   		validatePass = false;
		} else {
			mEditTextNewPassword2.setError(null);
		}
		return validatePass;
	}
	
	private boolean checkPassWordComplexity(String psw) {
		boolean repeat = true;
		boolean series = true;
		char first = psw.charAt(0);
		for (int i = 1; i < psw.length(); i++) {
			repeat = repeat && psw.charAt(i) == first;
			series = series && (int)psw.charAt(i) == (int)psw.charAt(i - 1) + 1;
		}
		if (repeat || series) {
			return true;
		}
		return false;
	}

	public void onSave(){
		changePassword_submit(null);
	}
	
	public void changePassword_submit(View v){
		if(!validateData()){
			HyjUtil.displayToast(R.string.app_validation_error);
		}else{			
			HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
				@Override
				public void finishCallback(Object object) {
//					$.$attrs.currentUser.xGet("userData").save({
//						"password" : Ti.Utils.sha1(newPassword)
//					}, {
//						patch : true,
//						wait : true
//					});
					
					HyjModelEditor<UserData> editor = HyjApplication.getInstance().getCurrentUser().getUserData().newModelEditor();
					editor.getModelCopy().setPassword(HyjUtil.getSHA1(mNewPassword1));
					editor.getModel().setSyncFromServer(true);
					editor.save();

					((HyjActivity) ChangePasswordFragment.this.getActivity()).dismissProgressDialog();
					HyjUtil.displayToast(R.string.app_save_success);
				}

				@Override
				public void errorCallback(Object object) {
					((HyjActivity) ChangePasswordFragment.this.getActivity()).dismissProgressDialog();
					
					JSONObject json = (JSONObject) object;
					HyjUtil.displayToast(json.optJSONObject("__summary").optString("msg"));
				}
			};

//			var data = {
//					userId : $.$attrs.currentUser.xGet("id"),
//					oldPassword : Ti.Utils.sha1(oldPassword),
//					newPassword : Ti.Utils.sha1(newPassword),
//					newPassword2 : Ti.Utils.sha1(newPassword2)
//				};
			try {
				JSONObject data = new JSONObject();
				data.put("userId", HyjApplication.getInstance()
						.getCurrentUser().getId());
				data.put("oldPassword", HyjUtil.getSHA1(mOldPassword));
				data.put("newPassword", HyjUtil.getSHA1(mNewPassword1));
				data.put("newPassword2", HyjUtil.getSHA1(mNewPassword2));
				
				HyjHttpPostAsyncTask.newInstance(serverCallbacks, data.toString() , "changePassword");
				
				((HyjActivity) this.getActivity())
						.displayProgressDialog(
								R.string.addFriendListFragment_title_add,
								R.string.friendListFragment_addFriend_progress_adding);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		
	}

	 
}