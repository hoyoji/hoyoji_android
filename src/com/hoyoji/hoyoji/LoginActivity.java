package com.hoyoji.hoyoji;

import java.security.NoSuchAlgorithmException;
import org.json.JSONException;
import org.json.JSONObject;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTask;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.server.HyjServer;
import com.hoyoji.android.hyjframework.userdatabase.HyjUserDbHelper;
import com.hoyoji.android.hyjframework.userdatabase.HyjUserDbContract.UserDatabaseEntry;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends HyjActivity {
	// Values for email and password at the time of the login attempt.
	private String mUserName;
	private String mPassword;

	// UI references.
	private EditText mUserNameView;
	private EditText mPasswordView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set up the login form.
		mUserName = "";
		mUserNameView = (EditText) findViewById(R.id.editText_username);
		mUserNameView.setText(mUserName);

		mPasswordView = (EditText) findViewById(R.id.editText_password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.imeAction_login || id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		findViewById(R.id.button_sign_in).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
		
		findViewById(R.id.button_register).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
						startActivity(intent);
					}
				});
	}

	@Override
	protected Integer getContentView() {
		// TODO Auto-generated method stub
		return R.layout.activity_login;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.loginActivity_action_forgot_password:
    		Intent intent = new Intent(this, SettingsActivity.class);
    		startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 * @throws NoSuchAlgorithmException 
	 */
	public void attemptLogin() {
		
		// Reset errors.
		mUserNameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUserName = mUserNameView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.loginActivity_error_field_required) + getString(R.string.loginActivity_editText_hint_password));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.loginActivity_error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}
		mPassword = HyjUtil.getSHA1(mPassword);

		// Check for a valid email address.
		if (TextUtils.isEmpty(mUserName)) {
			mUserNameView.setError(getString(R.string.loginActivity_error_field_required) + getString(R.string.loginActivity_editText_hint_username));
			focusView = mUserNameView;
			cancel = true;
		} 
		
//		else if (!mUserName.contains("@")) {
//			mUserNameView.setError(getString(R.string.error_invalid_email));
//			focusView = mUserNameView;
//			cancel = true;
//		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			doLogin();
		}
	}

	private void doLogin(){
			this.displayProgressDialog(R.string.loginActivity_action_sign_in, R.string.loginActivity_progress_signing_in);
			HyjAsyncTask.newInstance(new HyjAsyncTaskCallbacks(){
				@Override
				public void finishCallback(Object object) {
					if(object != null){
						String userId = (String)object;
						loginUser(userId);
					} else {
						loginFromServer(true);
					}	
				}

				@Override
				public Object doInBackground(String... string) {
					final HyjUserDbHelper mDbHelper = new HyjUserDbHelper(LoginActivity.this);
					final SQLiteDatabase rDb = mDbHelper.getReadableDatabase();
					
					// Define a projection that specifies which columns from the database
					// you will actually use after this query.
					String[] projection = {
					    UserDatabaseEntry.COLUMN_NAME_ID,
					    UserDatabaseEntry.COLUMN_NAME_USERNAME
					    };
					String[] args = { mUserName };
					Cursor cursor = rDb.query(
						UserDatabaseEntry.TABLE_NAME,  // The table to query
					    projection,                               // The columns to return
					    UserDatabaseEntry.COLUMN_NAME_USERNAME + "=?",            // The columns for the WHERE clause
					    args,                            // The values for the WHERE clause
					    null,                                     // don't group the rows
					    null,                                     // don't filter by row groups
					    null	                                  // The sort order
					    );
					String userId = null;
					if(cursor.getCount() > 0){
						cursor.moveToFirst();
						userId = cursor.getString(
						    cursor.getColumnIndexOrThrow(UserDatabaseEntry.COLUMN_NAME_ID)
						);
						cursor.close();
						rDb.close();
						mDbHelper.close();
						return userId;
					} else {
						cursor.close();
						rDb.close();
						mDbHelper.close();
						return null;
					}
				}
			});
			
	}
	
	private void loginUser(String userId){
		if(((HyjApplication)getApplication()).login(userId, mPassword)){
			this.dismissProgressDialog();
			finish();
		} else {
			loginFromServer(false);
		}
	}
	private void loginUser(String userId, JSONObject jsonUser) throws JSONException{
		if(((HyjApplication)getApplication()).login(userId, mPassword, jsonUser)){
			this.dismissProgressDialog();
			finish();
		} else {
			this.dismissProgressDialog();
			mPasswordView.setError(getString(R.string.loginActivity_error_incorrect_password));
			mPasswordView.requestFocus();
		}
	}
	private void loginFromServer(final boolean createUserDatabaseEntry){
		//从服务器上下载用户数据
		JSONObject postData = new JSONObject();
		try {
			postData.put("userName", mUserName);
			postData.put("password", mPassword);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks(){
			@Override
			public void finishCallback(Object object) {
				JSONObject jsonObject = (JSONObject)object;
				String userId;
				try {
					userId = jsonObject.getJSONObject("user").getString("id");
				
					if(createUserDatabaseEntry){
						final HyjUserDbHelper mDbHelper = new HyjUserDbHelper(LoginActivity.this);
						final SQLiteDatabase wDb = mDbHelper
								.getWritableDatabase();
						ContentValues values = new ContentValues();
						values.put(UserDatabaseEntry.COLUMN_NAME_ID,
								userId);
						values.put(
								UserDatabaseEntry.COLUMN_NAME_USERNAME,
								mUserName);
		
						wDb.insert(
								UserDatabaseEntry.TABLE_NAME, null,
								values);
						wDb.close();
						mDbHelper.close();
					}
					loginUser(userId, jsonObject);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			@Override
			public void errorCallback(Object object) {
				LoginActivity.this.dismissProgressDialog();
				try {
					LoginActivity.this.displayDialog(((JSONObject)object).getJSONObject("__summary").getString("msg"));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		HyjHttpPostAsyncTask.newInstance(serverCallbacks, postData.toString(), "login");
	}

}
