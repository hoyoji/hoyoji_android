package com.hoyoji.hoyoji.setting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
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
import com.hoyoji.android.hyjframework.fragment.HyjUserFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.models.UserData;


public class BindEmailFragment extends HyjUserFragment {
	private EditText mEditTextEmail = null;
	private EditText mEditTextVerificationCode = null;
	String mEmail = "";
	String mVerificationCode = "";
	boolean mHasError = false;
	private Button mButtonSubmie = null;
	private Button mButtonVerificationCode = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.setting_fragment_bindemail;
	}
	 
	@Override
	public void onInitViewData(){
		
		if(HyjApplication.getInstance().getCurrentUser().getUserData().ismEmailVerified() == null
		&& (HyjApplication.getInstance().getCurrentUser().getUserData().getEmail() == null 
		|| HyjApplication.getInstance().getCurrentUser().getUserData().getEmail().length() == 0)){
			getView().findViewById(R.id.bindEmailFragment_linearLayout_verificationCode).setVisibility(View.GONE);
			((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("绑定邮箱");
		} else if(HyjApplication.getInstance().getCurrentUser().getUserData().ismEmailVerified() == null
		&& HyjApplication.getInstance().getCurrentUser().getUserData().getEmail() != null 
		&& HyjApplication.getInstance().getCurrentUser().getUserData().getEmail().length() != 0){
			((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("验证邮箱");
		}
		
		mEditTextEmail = (EditText) getView().findViewById(R.id.bindEmailFragment_editText_email);
		mEditTextVerificationCode = (EditText) getView().findViewById(R.id.bindEmailFragment_editText_verificationCode);
		mButtonSubmie = (Button) getView().findViewById(R.id.bindEmailFragment_button_submit);
		mButtonVerificationCode = (Button) getView().findViewById(R.id.bindEmailFragment_button_verify);
		
		mButtonSubmie.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEmail = mEditTextEmail.getText().toString();
				if(!validateEmailData()){
					
				} else {
					getView().findViewById(R.id.bindEmailFragment_linearLayout_verificationCode).setVisibility(View.VISIBLE);
				}
			}
		});
		
		mButtonVerificationCode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mVerificationCode = mEditTextVerificationCode.getText().toString();
				if(!validateVerificationCodeData()){
					
				} else {
					
				}
			}
		});
//		mEditTextNewPassword2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//					@Override
//					public boolean onEditorAction(TextView textView, int id,
//							KeyEvent keyEvent) {
//						if (id == R.id.imeAction_changepassword || id == EditorInfo.IME_ACTION_DONE) {
//							onSave();
//							return true;
//						}
//						return false;
//					}
//				});
//		
//		getView().findViewById(R.id.changePasswordFragment_button_onSave).setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				fillData();
//				if(!validateData()){
//					HyjUtil.displayToast(R.string.app_validation_error);
//				} else {	
//					changePassword_submit(v);
//				}
//			}
//		});
//		
//		this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}

//	private void fillData(){
//		if(HyjApplication.getInstance().getCurrentUser().getUserData().getHasPassword()){
//			mOldPassword = mEditTextOldPassword.getText().toString();
//		}
//		mNewPassword1 = mEditTextNewPassword1.getText().toString();
//		mNewPassword2 = mEditTextNewPassword2.getText().toString();
//	}
//	
	public boolean validateEmailData(){
		boolean validatePass = true;
		
		// Check for a valid email address.
		Pattern pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
		Matcher matcher = pattern.matcher(mEmail);
//				System.out.println(matcher.matches());
		
		
		if (TextUtils.isEmpty(mEmail) || !matcher.matches()) {
			mEditTextEmail.setError(getString(R.string.bindEmailFragment_validation_wrong_email));
			validatePass = false;
		} else {
			mEditTextEmail.setError(null);
		}
		return validatePass;
	}
	
	public boolean validateVerificationCodeData(){
		boolean validatePass = true;
		
		if(mVerificationCode.length() != 6){
			mEditTextVerificationCode.setError(getString(R.string.bindEmailFragment_validation_wrong_verificationcode));
	   		validatePass = false;
		} else {
			mEditTextVerificationCode.setError(null);
		}
		return validatePass;
	}
//	
//	private boolean checkPassWordComplexity(String psw) {
//		boolean repeat = true;
//		boolean series = true;
//		char first = psw.charAt(0);
//		for (int i = 1; i < psw.length(); i++) {
//			repeat = repeat && psw.charAt(i) == first;
//			series = series && (int)psw.charAt(i) == (int)psw.charAt(i - 1) + 1;
//		}
//		if (repeat || series) {
//			return true;
//		}
//		return false;
//	}
//
//	private void onSave(){
//		fillData();
//		if(!validateData()){
//			HyjUtil.displayToast(R.string.app_validation_error);
//		} else {	
//			changePassword_submit(null);
//		}
//	}
//	
//	private void changePassword_submit(View v){
//			HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
//				@Override
//				public void finishCallback(Object object) {
//					
//					HyjModelEditor<UserData> editor = HyjApplication.getInstance().getCurrentUser().getUserData().newModelEditor();
//					editor.getModelCopy().setPassword(HyjUtil.getSHA1(mNewPassword1));
//					if(!editor.getModel().getHasPassword()){
//						editor.getModelCopy().setHasPassword(true);
//					}
//					editor.getModelCopy().setSyncFromServer(true);
//					editor.save();
//
//					((HyjActivity) BindEmailFragment.this.getActivity()).dismissProgressDialog();
//					HyjUtil.displayToast(R.string.app_save_success);
//					getActivity().finish();
//				}
//
//				@Override
//				public void errorCallback(Object object) {
//					((HyjActivity) BindEmailFragment.this.getActivity()).dismissProgressDialog();
//					
//					JSONObject json = (JSONObject) object;
//					HyjUtil.displayToast(json.optJSONObject("__summary").optString("msg"));
//				}
//			};
//
//			try {
//				JSONObject data = new JSONObject();
//				data.put("userId", HyjApplication.getInstance().getCurrentUser().getId());
//				if(HyjApplication.getInstance().getCurrentUser().getUserData().getHasPassword()){
//					data.put("oldPassword", HyjUtil.getSHA1(mOldPassword));
//				}
//				data.put("newPassword", HyjUtil.getSHA1(mNewPassword1));
//				data.put("newPassword2", HyjUtil.getSHA1(mNewPassword2));
//				
//				HyjHttpPostAsyncTask.newInstance(serverCallbacks, data.toString() , "changePassword");
//				
//				((HyjActivity) this.getActivity())
//						.displayProgressDialog(
//								R.string.changePasswordFragment_title,
//								R.string.changePasswordFragment_toast_changing);
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//	}

	 
}
