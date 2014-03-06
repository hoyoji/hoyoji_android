package com.hoyoji.hoyoji;

import java.security.NoSuchAlgorithmException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTask;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.server.HyjServer;
import com.hoyoji.android.hyjframework.userdatabase.HyjUserDbHelper;
import com.hoyoji.android.hyjframework.userdatabase.HyjUserDbContract.UserDatabaseEntry;
import com.hoyoji.hoyoji.models.Currency;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.FriendCategory;
import com.hoyoji.hoyoji.models.MessageBox;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.ParentProject;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.models.UserData;

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
import android.view.WindowManager;
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
						if (id == R.id.imeAction_login
								|| id == EditorInfo.IME_ACTION_DONE
								|| id == EditorInfo.IME_NULL) {
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
						Intent intent = new Intent(LoginActivity.this,
								RegisterActivity.class);
						startActivity(intent);
					}
				});
	}

	@Override
	protected void onInitViewData() {
		// init view data here
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
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
	 * 
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
			mPasswordView
					.setError(getString(R.string.loginActivity_error_field_required)
							+ getString(R.string.loginActivity_editText_hint_password));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView
					.setError(getString(R.string.loginActivity_error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}
		mPassword = HyjUtil.getSHA1(mPassword);

		// Check for a valid email address.
		if (TextUtils.isEmpty(mUserName)) {
			mUserNameView
					.setError(getString(R.string.loginActivity_error_field_required)
							+ getString(R.string.loginActivity_editText_hint_username));
			focusView = mUserNameView;
			cancel = true;
		}

		// else if (!mUserName.contains("@")) {
		// mUserNameView.setError(getString(R.string.error_invalid_email));
		// focusView = mUserNameView;
		// cancel = true;
		// }

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

	private void doLogin() {
		this.displayProgressDialog(R.string.loginActivity_action_sign_in,
				R.string.loginActivity_progress_signing_in);
		HyjAsyncTask.newInstance(new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				if (object != null) {
					String userId = (String) object;
					loginUser(userId);
				} else {
					loginFromServer(true);
				}
			}

			@Override
			public Object doInBackground(String... string) {
				final HyjUserDbHelper mDbHelper = new HyjUserDbHelper(
						LoginActivity.this);
				final SQLiteDatabase rDb = mDbHelper.getReadableDatabase();

				// Define a projection that specifies which columns from the
				// database
				// you will actually use after this query.
				String[] projection = { UserDatabaseEntry.COLUMN_NAME_ID,
						UserDatabaseEntry.COLUMN_NAME_USERNAME };
				String[] args = { mUserName };
				Cursor cursor = rDb.query(UserDatabaseEntry.TABLE_NAME, // The
																		// table
																		// to
																		// query
						projection, // The columns to return
						UserDatabaseEntry.COLUMN_NAME_USERNAME + "=?", // The
																		// columns
																		// for
																		// the
																		// WHERE
																		// clause
						args, // The values for the WHERE clause
						null, // don't group the rows
						null, // don't filter by row groups
						null // The sort order
						);
				String userId = null;
				if (cursor.getCount() > 0) {
					cursor.moveToFirst();
					userId = cursor.getString(cursor
							.getColumnIndexOrThrow(UserDatabaseEntry.COLUMN_NAME_ID));
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

	private void loginUser(String userId) {
		if (((HyjApplication) getApplication()).login(userId, mPassword)) {
			downloadUserData();
		} else {
			loginFromServer(false);
		}
	}

	private void loginUser(String userId, JSONObject jsonUser)
			throws JSONException {
		if (((HyjApplication) getApplication()).login(userId, mPassword,
				jsonUser)) {
			downloadUserData();
		} else {
			mPasswordView
					.setError(getString(R.string.loginActivity_error_incorrect_password));
			mPasswordView.requestFocus();
			this.dismissProgressDialog();
		}
	}

	private void downloadUserData() {
		User user = HyjApplication.getInstance().getCurrentUser();

		MessageBox msgBox = HyjModel.getModel(MessageBox.class,
				user.getMessageBoxId1());
		if (msgBox != null) {
			this.dismissProgressDialog();
			 Intent i = getPackageManager()
			 .getLaunchIntentForPackage(getApplicationContext().getPackageName() );
			 i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
			 startActivity(i);
			finish();
			return;
		}

//		UserData userData = HyjApplication.getInstance().getCurrentUser()
//				.getUserData();

		// 下载一些用户必须的资料
		JSONArray belongsToes = new JSONArray();
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("__dataType", "MessageBox");
//			jsonObj1.put("id", user.getMessageBoxId());
			jsonObj.put("ownerUserId", HyjApplication.getInstance().getCurrentUser().getId());
			belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "Project");
			jsonObj.put("pst.friendUserId", HyjApplication.getInstance().getCurrentUser().getId());
//			jsonObj.put("id", userData.getActiveProjectId());
			belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "ProjectShareAuthorization");
			jsonObj.put("state", "Accept");
			jsonObj.put("friendUserId", user.getId());

			JSONObject notFilter = new JSONObject();
			notFilter.put("ownerUserId", user.getId());
			jsonObj.put("__NOT_FILTER__", notFilter);
			belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "ProjectShareAuthorization");
			jsonObj.put("ownerUserId", user.getId());
			belongsToes.put(jsonObj);
			
			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "ParentProject");
			jsonObj.put("parentProjectId", null);
//			jsonObj.put("subProjectId", userData.getActiveProjectId());
			jsonObj.put("ownerUserId", user.getId());
			belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "FriendCategory");
			jsonObj.put("ownerUserId", HyjApplication.getInstance().getCurrentUser().getId());
//			jsonObj.put("id", userData.getDefaultFriendCategoryId());
			belongsToes.put(jsonObj);
			
			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "Friend");
			jsonObj.put("ownerUserId", HyjApplication.getInstance().getCurrentUser().getId());
//			jsonObj.put("id", userData.getDefaultFriendCategoryId());
			belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "Currency");
			jsonObj.put("ownerUserId", HyjApplication.getInstance().getCurrentUser().getId());
			belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "Exchange");
			jsonObj.put("ownerUserId", HyjApplication.getInstance().getCurrentUser().getId());
			belongsToes.put(jsonObj);
			
			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "MoneyAccount");
			jsonObj.put("ownerUserId", HyjApplication.getInstance().getCurrentUser().getId());
//			jsonObj.put("id", userData.getActiveMoneyAccountId());
			belongsToes.put(jsonObj);
			
			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "MoneyExpenseCategory");
			jsonObj.put("ownerUserId", HyjApplication.getInstance().getCurrentUser().getId());
//			jsonObj.put("id", userData.getActiveMoneyAccountId());
			belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "MoneyIncomeCategory");
			jsonObj.put("ownerUserId", HyjApplication.getInstance().getCurrentUser().getId());
//			jsonObj.put("id", userData.getActiveMoneyAccountId());
			belongsToes.put(jsonObj);
			
			// 从服务器上下载基础数据
			HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
				@Override
				public void finishCallback(Object object) {
					JSONArray jsonArray = (JSONArray) object;
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONArray array = jsonArray.optJSONArray(i);
						for (int j = 0; j < jsonArray.length(); j++) {
							JSONObject obj = array.optJSONObject(j);
							if (obj != null) {
								try {
									if (obj.optString("__dataType").equals(
											"MoneyAccount")) {
										MoneyAccount moneyAccount = new MoneyAccount();
										moneyAccount.loadFromJSON(obj, true);
										moneyAccount.save();
									} else if (obj
											.optString("__dataType")
											.equals("ProjectShareAuthorization")) {
										ProjectShareAuthorization newProjectShareAuthorization = new ProjectShareAuthorization();
										newProjectShareAuthorization
												.loadFromJSON(obj, true);
										newProjectShareAuthorization.save();
									} else if (obj.optString("__dataType")
											.equals("Currency")) {
										if (obj.isNull("symbol")) {
											java.util.Currency localeCurrency = java.util.Currency
													.getInstance(obj
															.optString("code"));
											obj.put("symbol",
													localeCurrency.getSymbol());
										}
										Currency newCurrency = new Currency();
										newCurrency.loadFromJSON(obj, true);
										newCurrency.save();
									} else if (obj.optString("__dataType")
											.equals("ParentProject")) {
										ParentProject parentProject = new ParentProject();
										parentProject.loadFromJSON(obj, true);
										parentProject.save();
									} else if (obj.optString("__dataType")
											.equals("FriendCategory")) {
										FriendCategory friendCategory = new FriendCategory();
										friendCategory.loadFromJSON(obj, true);
										friendCategory.save();
									} else if (obj.optString("__dataType")
											.equals("Project")) {
										Project project = new Project();
										project.loadFromJSON(obj, true);
										project.save();
									} else if (obj.optString("__dataType")
											.equals("MessageBox")) {
										MessageBox messageBox = new MessageBox();
										messageBox.loadFromJSON(obj, true);
										messageBox.save();
									} else if (obj.optString("__dataType")
											.equals("Friend")) {
										Friend friend = new Friend();
										friend.loadFromJSON(obj, true);
										friend.save();
									} else if (obj.optString("__dataType")
											.equals("Exchange")) {
										Exchange exchange = new Exchange();
										exchange.loadFromJSON(obj, true);
										exchange.save();
									} 
//									else if (obj.optString("__dataType")
//											.equals("MoneyExpenseCategory")) {
//										MoneyExpenseCategory moneyExpenseCategory = new MoneyExpenseCategory();
//										moneyExpenseCategory.loadFromJSON(obj, true);
//										moneyExpenseCategory.save();
//									} else if (obj.optString("__dataType")
//											.equals("MoneyIncomeCategory")) {
//										MoneyIncomeCategory moneyIncomeCategory = new MoneyIncomeCategory();
//										moneyIncomeCategory.loadFromJSON(obj, true);
//										moneyIncomeCategory.save();
//									}

								} catch (JSONException e) {
									//
								}
							}
						}
					}

					LoginActivity.this.dismissProgressDialog();

					 Intent i = getPackageManager()
					 .getLaunchIntentForPackage(getApplicationContext().getPackageName() );
					 i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
					 startActivity(i);
					finish();
				}

				@Override
				public void errorCallback(Object object) {
					LoginActivity.this.dismissProgressDialog();
					try {
						JSONObject json = (JSONObject) object;
						LoginActivity.this.displayDialog(
								json.getJSONObject("__summary")
										.getString("msg"), json.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};

			HyjHttpPostAsyncTask.newInstance(serverCallbacks,
					belongsToes.toString(), "getData");

		} catch (JSONException e) {
			//
		}
	}

	private void loginFromServer(final boolean createUserDatabaseEntry) {
		// 从服务器上下载用户数据
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				JSONObject jsonObject = (JSONObject) object;
				String userId;
				try {
					userId = jsonObject.getJSONObject("user").getString("id");

					if (createUserDatabaseEntry) {
						final HyjUserDbHelper mDbHelper = new HyjUserDbHelper(
								LoginActivity.this);
						final SQLiteDatabase wDb = mDbHelper
								.getWritableDatabase();
						ContentValues values = new ContentValues();
						values.put(UserDatabaseEntry.COLUMN_NAME_ID, userId);
						values.put(UserDatabaseEntry.COLUMN_NAME_USERNAME,
								mUserName);

						wDb.insert(UserDatabaseEntry.TABLE_NAME, null, values);
						wDb.close();
						mDbHelper.close();
					}
					loginUser(userId, jsonObject);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void errorCallback(Object object) {
				LoginActivity.this.dismissProgressDialog();
				try {
					JSONObject json = (JSONObject) object;
					LoginActivity.this.displayDialog(
							json.getJSONObject("__summary").getString("msg"),
							json.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		JSONObject postData = new JSONObject();
		try {
			postData.put("userName", mUserName);
			postData.put("password", mPassword);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		HyjHttpPostAsyncTask.newInstance(serverCallbacks, postData.toString(),
				"login");
	}

}
