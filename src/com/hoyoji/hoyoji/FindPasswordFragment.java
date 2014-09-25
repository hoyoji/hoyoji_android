package com.hoyoji.hoyoji;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.fragment.HyjUserFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.userdatabase.HyjUserDbHelper;
import com.hoyoji.android.hyjframework.userdatabase.HyjUserDbContract.UserDatabaseEntry;
import com.hoyoji.hoyoji_android.R;


public class FindPasswordFragment extends HyjUserFragment {
	private String mUserName;
	private String mFindPasswordEmail;
	private EditText mUserNameView;
	private EditText mFindPasswordEmailView;
//	private Spinner mFindPasswordSpinner;
	private Button mFindPasswordButton;
	
	@Override
	public Integer useContentView() {
		return R.layout.login_fragment_findpassword;
	}
	
	 public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserNameView = (EditText) getView().findViewById(R.id.editText_username);
        mFindPasswordEmailView = (EditText) getView().findViewById(R.id.editText_phoneoremail);
        mFindPasswordButton = (Button) getView().findViewById(R.id.button_findpassword);
        
//        mFindPasswordSpinner=  (Spinner)findViewById(R.id.spinner_findpasswordway);
//        List<String> findPasswordList = new ArrayList<String>();
//        findPasswordList.add("通过邮箱找回?");
//        findPasswordList.add("通过手机短信方式找回?");
//		ArrayAdapter<String> Qadapter1 = new ArrayAdapter<String>(FindPasswordActivity.this, android.R.layout.simple_spinner_item, findPasswordList);
//		mFindPasswordSpinner.setAdapter(Qadapter1);
        
		mFindPasswordButton.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					findPassword();
				}
			});
       }
	 
	 public void findPassword() {
		// Reset errors.
		mUserNameView.setError(null);
		mFindPasswordEmailView.setError(null);

		// Store values at the time of the login attempt.
		mUserName = mUserNameView.getText().toString();
		mFindPasswordEmail = mFindPasswordEmailView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid email address.
		Pattern pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
		Matcher matcher = pattern.matcher(mFindPasswordEmail);
//		System.out.println(matcher.matches());
		
		
		if (TextUtils.isEmpty(mFindPasswordEmail) || !matcher.matches()) {
			mFindPasswordEmailView
					.setError(getString(R.string.findpasswordFragment_validation_email_error));
			focusView = mFindPasswordEmailView;
			cancel = true;
		}
		
		// Check for a valid userName.
		if (TextUtils.isEmpty(mUserName) || mUserName.length() < 3) {
			mUserNameView
					.setError(getString(R.string.findpasswordFragment_validation_username_error_shortandempty));
			focusView = mUserNameView;
			cancel = true;
		}


		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			doFindPassword();
		}
		 
		 
		 
	 }
	 
	 public void doFindPassword() {
		 mUserName= mUserNameView.getText().toString();
		 mFindPasswordEmail = mFindPasswordEmailView.getText().toString();
		 
		 JSONObject findPasswordJsonObject = new JSONObject();
     	 try {
     		findPasswordJsonObject.put("userName", mUserName);
     		findPasswordJsonObject.put("email", mFindPasswordEmail);
		 } catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }
     	 
     	 
     	// 从服务器上下载用户数据
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				try {
					JSONObject json = (JSONObject) object;
					((HyjActivity) getActivity()).displayDialog(null,
							json.getJSONObject("__summary")
									.getString("msg"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void errorCallback(Object object) {
				try {
					JSONObject json = (JSONObject) object;
					((HyjActivity) getActivity()).displayDialog(null,
							json.getJSONObject("__summary")
									.getString("msg"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
     	 
     	 HyjHttpPostAsyncTask.newInstance(serverCallbacks, findPasswordJsonObject.toString(), "findPasswordSendEmail");
	 }
	 
	 
}
