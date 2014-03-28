package com.hoyoji.hoyoji.setting;

import android.view.KeyEvent;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjFragment;
import com.hoyoji.hoyoji.R;


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
			
		}
		HyjUtil.displayToast(R.string.app_save_success);
		
	}

	 
}
