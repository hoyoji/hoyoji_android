package com.hoyoji.hoyoji;

import java.util.Currency;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.userdatabase.HyjUserDbHelper;
import com.hoyoji.android.hyjframework.userdatabase.HyjUserDbContract.UserDatabaseEntry;


public class RegisterActivity extends HyjActivity {
	EditText mEditTextUserName;
	EditText mEditTextPassword1;
	EditText mEditTextPassword2;
	String mUserName = "";
	String mPassword1 = "";
	String mPassword2 = "";
	boolean mHasError = false;
	
	@Override
	protected Integer getContentView() {
		return R.layout.activity_register;
	}
	
	@Override
	protected void onInitViewData(){
		mEditTextUserName = (EditText) findViewById(R.id.editText_username);
		mEditTextPassword1 = (EditText) findViewById(R.id.editText_password1);
		mEditTextPassword2 = (EditText) findViewById(R.id.editText_password2);
		mEditTextPassword2
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.imeAction_register || id == EditorInfo.IME_ACTION_DONE) {
							onSave();
							return true;
						}
						return false;
					}
				});	
//		mEditTextUserName.setOnFocusChangeListener(new OnFocusChangeListener() { 
//		        public void onFocusChange(View v, boolean hasFocus) {
//		            if(!hasFocus){
//		            	mUserName = mEditTextUserName.getText().toString().trim();
//	            		//validateData();
//		            }
//		        }
//		    });
//		mEditTextPassword1.setOnFocusChangeListener(new OnFocusChangeListener() { 
//	        public void onFocusChange(View v, boolean hasFocus) {
//	            if(!hasFocus){
//	            	mPassword1 = mEditTextPassword1.getText().toString();
//            		//validateData();
//	            }
//	        }
//	    });
//		mEditTextPassword2.setOnFocusChangeListener(new OnFocusChangeListener() { 
//	        public void onFocusChange(View v, boolean hasFocus) {
//	            if(!hasFocus){
//	            	mPassword2 = mEditTextPassword2.getText().toString();
//	            	//validateData();
//	            }
//	        }
//	    });		
	}
	
	private void fillData(){
    	mUserName = mEditTextUserName.getText().toString().trim();
    	mPassword1 = mEditTextPassword1.getText().toString();
    	mPassword2 = mEditTextPassword2.getText().toString();
	}
	
	public boolean validateData(){
		boolean valiatePass = true;
		fillData();
		
		if(mUserName.length() == 0){
	   		mEditTextUserName.setError(getString(R.string.registerActivity_editText_hint_username));
	   		valiatePass = false;
		} else if(mUserName.length() < 3){
	   		mEditTextUserName.setError(getString(R.string.registerActivity_validation_username_too_short));
	   		valiatePass = false;
		} else {
			mEditTextUserName.setError(null);
	   	}
		
		if(mPassword1.length() == 0){
	   		mEditTextPassword1.setError(getString(R.string.registerActivity_editText_hint_password1));
	   		valiatePass = false;
		} else if(mPassword1.length() < 6){
	   		mEditTextPassword1.setError(getString(R.string.registerActivity_validation_password_too_short));
	   		valiatePass = false;
		} else {
			mEditTextPassword1.setError(null);
		}
		
		if(!mPassword1.equals(mPassword2)){
	   		mEditTextPassword2.setError(getString(R.string.registerActivity_validation_two_password_not_same));
	   		valiatePass = false;
		} else {
			mEditTextPassword2.setError(null);
		}
		return valiatePass;
	}
	
	public void onSave(){
		onSave(null);
	}
	
	public void onSave(View v){
		if(!validateData()){
			HyjUtil.displayToast(R.string.app_validation_error);
		} else {
			final ProgressDialog progressDialog = this.displayProgressDialog(R.string.registerActivity_progress_register_title, R.string.registerActivity_progress_register_msg);  
			Currency currency = Currency.getInstance(Locale.getDefault());
			String currencyId = currency.getCurrencyCode();
			String currencySymbol = currency.getSymbol();
			JSONObject postData = new JSONObject();
			try {
				postData.put("userName", mUserName);
				postData.put("password", HyjUtil.getSHA1(mPassword1));
				postData.put("currencyId", currencyId);
				postData.put("currencySymbol", currencySymbol);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks(){
				@Override
				public void finishCallback(Object object) {
					progressDialog.dismiss();
					HyjUtil.displayToast(R.string.registerActivity_toast_register_success);
					RegisterActivity.this.finish();
				}
				@Override
				public void errorCallback(Object object) {
					progressDialog.dismiss();
					JSONObject json = (JSONObject)object;
					try {
						RegisterActivity.this.displayDialog(json.getJSONObject("__summary").getString("msg"), json.getJSONObject("userName").getString("msg"));
					} catch (Exception e) {
						e.printStackTrace();
						RegisterActivity.this.displayDialog(json.optJSONObject("__summary").optString("msg"), json.toString());
					}
				}
			};
			
			HyjHttpPostAsyncTask.newInstance(serverCallbacks, postData.toString(), "registerUser");
		}
	}
}
