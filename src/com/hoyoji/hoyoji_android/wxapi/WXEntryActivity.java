package com.hoyoji.hoyoji_android.wxapi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.activeandroid.DatabaseHelper;
import com.activeandroid.util.Log;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTask;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjHttpGetExchangeRateAsyncTask;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.userdatabase.HyjUserDbHelper;
import com.hoyoji.android.hyjframework.userdatabase.HyjUserDbContract.UserDatabaseEntry;
import com.hoyoji.hoyoji.AppConstants;
import com.hoyoji.hoyoji.LoginActivity;
import com.hoyoji.hoyoji.models.Currency;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.FriendCategory;
import com.hoyoji.hoyoji.models.MessageBox;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyExpenseCategory;
import com.hoyoji.hoyoji.models.MoneyIncomeCategory;
import com.hoyoji.hoyoji.models.ParentProject;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectRemark;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.QQLogin;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.models.WBLogin;
import com.hoyoji.hoyoji.models.WXLogin;
import com.hoyoji.hoyoji_android.R;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.tencent.mm.sdk.openapi.ShowMessageFromWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.sample.Util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
	
	private String mUserName;

	// IWXAPI �ǵ���app��΢��ͨ�ŵ�openapi�ӿ�
	private IWXAPI api;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mUserName="";
		// setContentView(R.layout.entry);

		// ͨ��WXAPIFactory��������ȡIWXAPI��ʵ��
		api = WXAPIFactory.createWXAPI(this, AppConstants.WX_APP_ID, false);

		// regBtn = (Button) findViewById(R.id.reg_btn);
		// regBtn.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // ����appע�ᵽ΢��
		api.registerApp(AppConstants.WX_APP_ID);
		// }
		// });
		//
		// gotoBtn = (Button) findViewById(R.id.goto_send_btn);
		// gotoBtn.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// startActivity(new Intent(WXEntryActivity.this,
		// SendToWXActivity.class));
		// finish();
		// }
		// });
		//
		// launchBtn = (Button) findViewById(R.id.launch_wx_btn);
		// launchBtn.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Toast.makeText(WXEntryActivity.this, "launch result = " +
		// api.openWXApp(), Toast.LENGTH_LONG).show();
		// }
		// });
		//
		// checkBtn = (Button) findViewById(R.id.check_timeline_supported_btn);
		// checkBtn.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// int wxSdkVersion = api.getWXAppSupportAPI();
		// if (wxSdkVersion >= TIMELINE_SUPPORTED_VERSION) {
		// Toast.makeText(WXEntryActivity.this, "wxSdkVersion = " +
		// Integer.toHexString(wxSdkVersion) + "\ntimeline supported",
		// Toast.LENGTH_LONG).show();
		// } else {
		// Toast.makeText(WXEntryActivity.this, "wxSdkVersion = " +
		// Integer.toHexString(wxSdkVersion) + "\ntimeline not supported",
		// Toast.LENGTH_LONG).show();
		// }
		// }
		// });

		api.handleIntent(getIntent(), this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		setIntent(intent);
		api.handleIntent(intent, this);
	}

	// ΢�ŷ������󵽵���Ӧ��ʱ����ص�÷���
	@Override
	public void onReq(BaseReq req) {
		// switch (req.getType()) {
		// case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
		// goToGetMsg();
		// break;
		// case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
		// goToShowMsg((ShowMessageFromWX.Req) req);
		// break;
		// default:
		// break;
		// }
	}

	// ����Ӧ�÷��͵�΢�ŵ�����������Ӧ����ص�÷���
	@Override
	public void onResp(BaseResp resp) {
		int result = 0;
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			result = R.string.errcode_success;
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = R.string.errcode_cancel;
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = R.string.errcode_deny;
			break;
		default:
			result = R.string.errcode_unknown;
			break;
		}
		if (result != R.string.errcode_success) {
			Toast.makeText(this, result, Toast.LENGTH_LONG).show();
			return;
		}
		SendAuth.Resp sendAuthResp = (SendAuth.Resp) resp;
//		byte[] accessToken = HyjUtil
//				.getHtmlByteArray("https://api.weixin.qq.com/sns/oauth2/access_token?appid="
//						+ AppConstants.WX_APP_ID
//						+ "&secret="
//						+ AppConstants.WX_APP_SECRET
//						+ "&code="
//						+ sendAuthResp.token + "&grant_type=authorization_code");
		// try {
		// String str = new String(accessToken, "UTF-8").toString();
		// Toast.makeText(this, str, Toast.LENGTH_LONG).show();
		// } catch (UnsupportedEncodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		
		HyjAsyncTaskCallbacks callbacks = new HyjAsyncTaskCallbacks(){
			@Override
			public void errorCallback(Object object) {
				JSONObject jsonObj = (JSONObject) object;
				try {
					HyjUtil.displayToast(jsonObj.getJSONObject("__summary").getString("msg"));
				} catch (JSONException e) {
				}
				finish();
			}

			@Override
			public void finishCallback(Object object) {
				JSONObject jsonAccessToken = (JSONObject) object;
				
				doWBLogin(jsonAccessToken);
			}
		};
		HyjHttpWXLoginAsyncTask.newInstance(sendAuthResp.token, callbacks );
	}
	
	
	private void doWBLogin(final JSONObject values) {    

 		HyjAsyncTask.newInstance(new HyjAsyncTaskCallbacks() {
 			@Override
 			public void finishCallback(Object object) {
 				if (object != null) {
 					// 在本地找到该QQ用户
// 					String userId = (String) object;
 					
 					loginWXFromServer(false, values);
 					
// 					((HyjApplication) getApplication()).loginQQ(userId, values);
// 					relogin();
// 					LoginActivity.this.dismissProgressDialog();
 				} else {
 					// 在本地找不到该QQ用户，我们到服务器上去找
 					loginWXFromServer(true, values);
 				}
 			}

 			@Override
 			public Object doInBackground(String... string) {
 				File file = new File("/data/data/" + HyjApplication.getInstance().getPackageName() + "/databases/");
 			      if(file.isDirectory()){
 			           File [] fileArray = file.listFiles();
 			           if(null != fileArray && 0 != fileArray.length){
 			                for(int i = 0; i < fileArray.length; i++){

 			    				String dbName = fileArray[i].getName();
 			    				if(dbName.endsWith(".db") || dbName.endsWith("-journal")){
 			    					continue;
 			    				}
 			    				
 			    				Cursor cursor = null;
 			    				SQLiteDatabase rDb = null;
 			    				DatabaseHelper mDbHelper = null;
 			    				try{
 					    				Configuration config = new Configuration.Builder(HyjApplication.getInstance())
 					    											.setDatabaseName(dbName)
 					    											.create(); 
 					    				mDbHelper = new DatabaseHelper(config);
 					    				rDb = mDbHelper.getReadableDatabase();
 		
 					    				// Define a projection that specifies which columns from the
 					    				// database
 					    				// you will actually use after this query.
 					    				String[] projection = { "userId", "openid" };
 					    				String[] args = { values.optString("openid") };
 					    				cursor = rDb.query("WXLogin", 
 					    						projection, // The columns to return
 					    						"openid=?", 
 					    						args, // The values for the WHERE clause
 					    						null, // don't group the rows
 					    						null, // don't filter by row groups
 					    						null // The sort order
 					    				);
 					    				String userId = null;
 					    				if (cursor.getCount() > 0) {
 					    					cursor.moveToFirst();
 					    					userId = cursor.getString(cursor.getColumnIndexOrThrow("userId"));
 					    					cursor.close();
 					    					rDb.close();
 					    					mDbHelper.close();
 					    					return userId;
 					    				}
 			    				} catch (Exception e){
 			    					
 			    				} finally{
 			    					if(cursor != null){
 			    						cursor.close();
 			    					}
 			    					if(rDb != null){
 			    						rDb.close();
 			    					}
 			    					if(mDbHelper != null){
 			    						mDbHelper.close();
 			    					}
 			    				}
 			                }
 			           }
 			      }
 				return null;
 			}
 		});

 	}
	
	
	private void loginWXFromServer(final boolean createUserDatabaseEntry, final JSONObject loginInfo) {
		if(createUserDatabaseEntry == true){
			java.util.Currency currency = java.util.Currency.getInstance(Locale.getDefault());
			try {
				loginInfo.put("currencyId", currency.getCurrencyCode());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		
		// 从服务器上下载用户数据
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				JSONObject jsonObject = (JSONObject) object;
				String userId;
				try {
					userId = jsonObject.getJSONObject("user").getString("id");

					if (createUserDatabaseEntry == true) {
						final HyjUserDbHelper mDbHelper = new HyjUserDbHelper(
								WXEntryActivity.this);
						final SQLiteDatabase wDb = mDbHelper
								.getWritableDatabase();
						ContentValues values = new ContentValues();
						values.put(UserDatabaseEntry.COLUMN_NAME_ID, userId);
						values.put(UserDatabaseEntry.COLUMN_NAME_USERNAME, mUserName);

						wDb.insert(UserDatabaseEntry.TABLE_NAME, null, values);
						wDb.close();
						mDbHelper.close();
						loginWXUserFirstTime(userId, HyjUtil.ifNull(jsonObject.getJSONObject("userData").optString("password"), loginInfo.optString("access_token")), jsonObject);
					} else {
						if(((HyjApplication) getApplication()).loginWXFirstTime(userId, HyjUtil.ifNull(jsonObject.getJSONObject("userData").optString("password"), loginInfo.optString("access_token")), jsonObject)){
							relogin();
						}
						WXEntryActivity.this.dismissProgressDialog();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				finish();
			}

			@Override
			public void errorCallback(Object object) {
				try {
					JSONObject json = (JSONObject) object;
					WXEntryActivity.this.dismissProgressDialog();
					WXEntryActivity.this.displayDialog("登录失败",
						json.getJSONObject("__summary").getString("msg"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				finish();
			}
		};
		
		HyjHttpPostAsyncTask.newInstance(serverCallbacks, loginInfo.toString(), "loginWX");
	}
	
	// 该WX好友第一次在本机登录，我们需要下载好友数据到本地
	private void loginWXUserFirstTime(String userId, String password, JSONObject jsonUser)
			throws JSONException {
		if (((HyjApplication) getApplication()).loginWXFirstTime(userId, password, jsonUser)) {
			downloadUserData();
		} else {
			this.dismissProgressDialog();
		}
	}
	
	
	private void relogin(){
		Intent i = getPackageManager().getLaunchIntentForPackage(
				getApplicationContext().getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		finish();
	}
	
	
	private void downloadUserData() {
		User user = HyjApplication.getInstance().getCurrentUser();
		
		MessageBox msgBox = HyjModel.getModel(MessageBox.class,
				user.getMessageBoxId1());
		if (msgBox != null) {
			this.dismissProgressDialog();
			relogin();
			return;
		}

		// UserData userData = HyjApplication.getInstance().getCurrentUser()
		// .getUserData();

		// 下载一些用户必须的资料
		JSONArray belongsToes = new JSONArray();
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("__dataType", "QQLogin");
			belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "WBLogin");
			belongsToes.put(jsonObj);
			
			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "WXLogin");
			belongsToes.put(jsonObj);
			
			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "MessageBox");
//			jsonObj.put("ownerUserId", HyjApplication.getInstance()
//					.getCurrentUser().getId());
			belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "Project");
			jsonObj.put("pst.friendUserId", HyjApplication.getInstance()
					.getCurrentUser().getId());
			// jsonObj.put("id", userData.getActiveProjectId());
			belongsToes.put(jsonObj);

			// jsonObj = new JSONObject();
			// jsonObj.put("__dataType", "ProjectShareAuthorization");
			// jsonObj.put("state", "Accept");
			// jsonObj.put("friendUserId", user.getId());
			//
			// JSONObject notFilter = new JSONObject();
			// notFilter.put("ownerUserId", user.getId());
			// jsonObj.put("__NOT_FILTER__", notFilter);
			// belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "ProjectShareAuthorization");
			// jsonObj.put("ownerUserId", user.getId());
//			JSONObject notFilter = new JSONObject();
//			notFilter.put("ownerUserId", "");
//			jsonObj.put("__NOT_FILTER__", notFilter);
			belongsToes.put(jsonObj);
			
			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "ProjectRemark");
//			 jsonObj.put("ownerUserId", user.getId());
			belongsToes.put(jsonObj);


			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "ParentProject");
			jsonObj.put("parentProjectId", null);
			// jsonObj.put("subProjectId", userData.getActiveProjectId());
//			jsonObj.put("ownerUserId", user.getId());
			belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "FriendCategory");
//			jsonObj.put("ownerUserId", HyjApplication.getInstance()
//					.getCurrentUser().getId());
			// jsonObj.put("id", userData.getDefaultFriendCategoryId());
			belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "Friend");
//			jsonObj.put("ownerUserId", HyjApplication.getInstance()
//					.getCurrentUser().getId());
			// jsonObj.put("id", userData.getDefaultFriendCategoryId());
			belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "Currency");
//			jsonObj.put("ownerUserId", HyjApplication.getInstance()
//					.getCurrentUser().getId());
			belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "Exchange");
//			jsonObj.put("ownerUserId", HyjApplication.getInstance()
//					.getCurrentUser().getId());
			belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "MoneyAccount");
//			jsonObj.put("ownerUserId", HyjApplication.getInstance()
//					.getCurrentUser().getId());
			// jsonObj.put("id", userData.getActiveMoneyAccountId());
			belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "MoneyExpenseCategory");
//			jsonObj.put("ownerUserId", HyjApplication.getInstance()
//					.getCurrentUser().getId());
			belongsToes.put(jsonObj);

			jsonObj = new JSONObject();
			jsonObj.put("__dataType", "MoneyIncomeCategory");
//			jsonObj.put("ownerUserId", HyjApplication.getInstance()
//					.getCurrentUser().getId());
			belongsToes.put(jsonObj);

			// 从服务器上下载基础数据
			HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
				@Override
				public void finishCallback(Object object) {
					try {
						ActiveAndroid.beginTransaction();
						String figureUrl = null;
						JSONArray jsonArray = (JSONArray) object;
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONArray array = jsonArray.optJSONArray(i);
							for (int j = 0; j < jsonArray.length(); j++) {
								JSONObject obj = array.optJSONObject(j);
								if (obj != null) {
									if(HyjApplication.getIsDebuggable()){
										Log.i("Login Download Data : ",  obj.optString("__dataType"));
									}
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
											.equals("QQLogin")) {
										QQLogin qqLogin = new QQLogin();
										qqLogin.loadFromJSON(obj, true);
										if(!obj.isNull("figureUrl")){
											figureUrl = obj.getString("figureUrl");
										}
										qqLogin.save();
									} else if (obj.optString("__dataType")
											.equals("WXLogin")) {
										WXLogin wxLogin = new WXLogin();
										wxLogin.loadFromJSON(obj, true);
										if(!obj.isNull("headimgurl")){
											figureUrl = obj.getString("headimgurl");
										}
										wxLogin.save();
									}  else if (obj.optString("__dataType")
											.equals("WBLogin")) {
										WBLogin wbLogin = new WBLogin();
										wbLogin.loadFromJSON(obj, true);
										if(!obj.isNull("profile_image_url")){
											figureUrl = obj.getString("profile_image_url");
										}
										wbLogin.save();
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
									} else if (obj.optString("__dataType")
											.equals("MoneyExpenseCategory")) {
										MoneyExpenseCategory moneyExpenseCategory = new MoneyExpenseCategory();
										moneyExpenseCategory.loadFromJSON(obj,
												true);
										moneyExpenseCategory.save();
									} else if (obj.optString("__dataType")
											.equals("MoneyIncomeCategory")) {
										MoneyIncomeCategory moneyIncomeCategory = new MoneyIncomeCategory();
										moneyIncomeCategory.loadFromJSON(obj,
												true);
										moneyIncomeCategory.save();
									} else if (obj.optString("__dataType")
											.equals("ProjectRemark")) {
										ProjectRemark projectRemark = new ProjectRemark();
										projectRemark.loadFromJSON(obj,
												true);
										projectRemark.save();
									}
								}
							}
						}

						ActiveAndroid.setTransactionSuccessful();
						ActiveAndroid.endTransaction();


						if(figureUrl != null){
							final String figureUrl1 = figureUrl;
							HyjAsyncTask.newInstance(new HyjAsyncTaskCallbacks() {
								@Override
								public void finishCallback(Object object) {
									Bitmap thumbnail = null;
									if(object != null){
										thumbnail = (Bitmap) object;
									}
									
									FileOutputStream out;
									try {
										Picture figure = new Picture();
										File imgFile = HyjUtil.createImageFile(figure.getId() + "_icon");
										if(imgFile != null){
											out = new FileOutputStream(imgFile);
											thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, out);
											out.close();
											out = null;
											
											figure.setRecordId(HyjApplication.getInstance().getCurrentUser().getId());
											figure.setRecordType("User");
											figure.setDisplayOrder(0);
											figure.setPictureType("JPEG");
											
											HyjApplication.getInstance().getCurrentUser().setPicture(figure);
											HyjApplication.getInstance().getCurrentUser().save();
											figure.save();								
										}
									} catch (FileNotFoundException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
			
								@Override
								public Object doInBackground(String... string) {
									Bitmap thumbnail = null;
									thumbnail = Util.getbitmap(figureUrl1);
									return thumbnail;
								}
							});
						}
						
						
						
						WXEntryActivity.this.dismissProgressDialog();
						relogin();
					} catch (Exception e) {
						ActiveAndroid.endTransaction();
						WXEntryActivity.this.dismissProgressDialog();
					}
				}

				@Override
				public void errorCallback(Object object) {
					WXEntryActivity.this.dismissProgressDialog();
					try {
						JSONObject json = (JSONObject) object;
						WXEntryActivity.this.displayDialog(null,
								json.getJSONObject("__summary")
										.getString("msg"));
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

	// private void goToGetMsg() {
	// Intent intent = new Intent(this, GetFromWXActivity.class);
	// intent.putExtras(getIntent());
	// startActivity(intent);
	// finish();
	// }
	//
	// private void goToShowMsg(ShowMessageFromWX.Req showReq) {
	// WXMediaMessage wxMsg = showReq.message;
	// WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;
	//
	// StringBuffer msg = new StringBuffer(); // ��֯һ�����ʾ����Ϣ����
	// msg.append("description: ");
	// msg.append(wxMsg.description);
	// msg.append("\n");
	// msg.append("extInfo: ");
	// msg.append(obj.extInfo);
	// msg.append("\n");
	// msg.append("filePath: ");
	// msg.append(obj.filePath);
	//
	// Intent intent = new Intent(this, ShowFromWXActivity.class);
	// intent.putExtra(AppConstants.ShowMsgActivity.STitle, wxMsg.title);
	// intent.putExtra(AppConstants.ShowMsgActivity.SMessage, msg.toString());
	// intent.putExtra(AppConstants.ShowMsgActivity.BAThumbData,
	// wxMsg.thumbData);
	// startActivity(intent);
	// finish();
	// }

	protected void displayDialog(Object object, String string) {
		// TODO Auto-generated method stub
		
	}

	protected void dismissProgressDialog() {
		// TODO Auto-generated method stub
		
	}

	private static class HyjHttpWXLoginAsyncTask extends HyjAsyncTask {

		public HyjHttpWXLoginAsyncTask(HyjAsyncTaskCallbacks callbacks) {
			super(callbacks);
		}

		public static HyjHttpWXLoginAsyncTask newInstance(String code, HyjAsyncTaskCallbacks callbacks) {
			HyjHttpWXLoginAsyncTask newTask = new HyjHttpWXLoginAsyncTask(
					callbacks);
			newTask.execute(code);
			return newTask;
		}

		@Override
		protected Object doInBackground(String... params) {
			if (HyjUtil.hasNetworkConnection()) {
				String target = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
						+ AppConstants.WX_APP_ID
						+ "&secret="
						+ AppConstants.WX_APP_SECRET
						+ "&code="
						+ params[0]
						+ "&grant_type=authorization_code";
				return doHttpGet(target);
			} else {
				try {
					return new JSONObject("{'__summary' : {'msg' : '"
							+ HyjApplication.getInstance().getString(
									R.string.server_connection_disconnected)
							+ "'}}");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return null;
		}


		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(Object result) {
			if (mServerCallback != null) {
				if (result != null) {
					if (result instanceof JSONObject) {
						JSONObject jsonResult = (JSONObject) result;
						if (jsonResult.isNull("__summary")) {
							mServerCallback.finishCallback(result);
						} else {
							mServerCallback.errorCallback(result);
						}
					} else {
						mServerCallback.finishCallback(result);
					}
				} else {
					try {
						mServerCallback
								.errorCallback(new JSONObject(
										"{'__summary' : {'msg' : '"
												+ HyjApplication
														.getInstance()
														.getString(
																R.string.server_dataparse_error)
												+ "'}}"));
					} catch (JSONException e) {
					}
				}
			}
		}

		private Object doHttpGet(String serverUrl) {
			InputStream is = null;
			String s = null;
			try {
				DefaultHttpClient client = new DefaultHttpClient();

				client.addResponseInterceptor(new HttpResponseInterceptor() {
					@Override
					public void process(HttpResponse response,
							HttpContext context) throws HttpException,
							IOException {
						// Inflate any responses compressed with gzip
						final HttpEntity entity = response.getEntity();
						final Header encoding = entity.getContentEncoding();
						if (encoding != null) {
							for (HeaderElement element : encoding.getElements()) {
								if (element.getName().equalsIgnoreCase("gzip")) {
									response.setEntity(new InflatingEntity(
											response.getEntity()));
									break;
								}
							}
						}

					}
				});
				HttpGet get = new HttpGet(serverUrl);

				get.setHeader("Accept", "application/json");
				get.setHeader("Content-type", "application/json; charset=UTF-8");
				get.setHeader("Accept-Encoding", "gzip");

				HttpResponse response = client.execute(get);
				HttpEntity entity = response.getEntity();
				s = EntityUtils.toString(entity, HTTP.UTF_8);
			} catch (Exception e) {
				try {
					return new JSONObject("{'__summary' : {'msg' : '"
							+ HyjApplication.getInstance().getString(
									R.string.server_connection_error) + "'}}");
				} catch (JSONException e1) {
				}
			} finally {
				try {
					if (is != null)
						is.close();
				} catch (Exception squish) {
				}
			}

			try {
				if (s == null) {
					return null;
				} else if (s.startsWith("{")) {
					return new JSONObject(s);
				} else if (s.startsWith("[")) {
					return new JSONArray(s);
				} else if (s.equals("")) {
					return new JSONArray();
				} else {
					return new JSONObject("{'__summary' : {'msg' : '"
							+ HyjApplication.getInstance().getString(
									R.string.server_dataparse_error) + "'}}");

				}
			} catch (JSONException e) {
			}
			return null;
		}

		private static class InflatingEntity extends HttpEntityWrapper {
			public InflatingEntity(HttpEntity wrapped) {
				super(wrapped);
			}

			@Override
			public InputStream getContent() throws IOException {
				return new GZIPInputStream(wrappedEntity.getContent());
			}

			@Override
			public long getContentLength() {
				return -1;
			}
		}
	}
}