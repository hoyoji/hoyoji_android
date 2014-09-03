package com.hoyoji.hoyoji.setting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.activeandroid.DatabaseHelper;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTask;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjDialogFragment;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.fragment.HyjUserFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.userdatabase.HyjUserDbHelper;
import com.hoyoji.android.hyjframework.userdatabase.HyjUserDbContract.UserDatabaseEntry;
import com.hoyoji.android.hyjframework.view.HyjImageField;
import com.hoyoji.android.hyjframework.view.HyjImagePreview;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.android.hyjframework.view.HyjImageField.PictureItem;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.AppConstants;
import com.hoyoji.hoyoji.LoginActivity;
import com.hoyoji.hoyoji.PictureUploadService;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.QQLogin;
import com.hoyoji.hoyoji.models.WBLogin;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.models.UserData;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQAuth;
import com.tencent.sample.Util;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;


public class SystemSettingFormFragment extends HyjUserFragment {
	private HyjTextField mTextFieldUserName = null;
	private HyjTextField mTextFieldNickName = null;
	private HyjTextField mTextFieldEmail = null;
	private Button mButtonCheckEmail = null;
	private HyjTextField mTextFieldPhone = null;
	private Button mButtonPhone = null;
	private HyjTextField mTextFieldQQ = null;
	private HyjTextField mTextFieldWB = null;
	private Button mButtonQQ = null;
	private Button mButtonWB = null;
	private Button mButtonChangePassword = null;
	private Button mButtonUploadPicture = null;
	private CheckBox mCheckBoxAddFriendValidation = null;
	private Button mButtonMoneyExpenseColorPicker = null;
	private Button mButtonMoneyIncomeColorPicker = null;
	private HyjImageView takePictureButton = null;
	private ChangeObserver mChangeObserver;

	private Resources r = null;
	
    public static QQAuth mQQAuth;
    /** 微博 Web 授权类，提供登陆等功能  */
    private WeiboAuth mWBAuth;
    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;
    /** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
    private SsoHandler mSsoHandler;
    
	private Tencent mTencent;
	private String mAppid;
	
	private int mExpenseColor = 0;
	private int mIncomeColor = 0;
	
	@Override
	public Integer useContentView() {
		return R.layout.setting_formfragment_systemsetting;
	}
	 
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		r = getResources();
		User user;
//		Intent intent = getActivity().getIntent();
//		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		user =  HyjApplication.getInstance().getCurrentUser();
		
		mTextFieldUserName = (HyjTextField) getView().findViewById(R.id.systemSettingFormFragment_textField_userName);
		mTextFieldUserName.setText(user.getUserName());
		mTextFieldUserName.setEnabled(false);
		mTextFieldUserName.setTextColor(Color.BLACK);
		
		mTextFieldNickName = (HyjTextField) getView().findViewById(R.id.systemSettingFormFragment_textField_nickName);
		mTextFieldNickName.setText(user.getNickName());

		mTextFieldEmail = (HyjTextField) getView().findViewById(R.id.systemSettingFormFragment_textField_email);
		mTextFieldEmail.setText(user.getUserData().getEmail());
		
		mButtonCheckEmail = (Button) getView().findViewById(R.id.systemSettingFormFragment_button_checkEmail);
		mButtonCheckEmail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				HyjUtil.displayToast("该功能尚未完善，请关注后续版本");
				
			}
		});
		getView().findViewById(R.id.systemSettingFormFragment_linearLayout_email).setVisibility(View.GONE);
		
		mTextFieldPhone = (HyjTextField) getView().findViewById(R.id.systemSettingFormFragment_textField_phone);
		mTextFieldPhone.setEnabled(false);
		
		mButtonPhone = (Button) getView().findViewById(R.id.systemSettingFormFragment_button_phoneBinding);
		
		setPhoneField();

		mTextFieldQQ = (HyjTextField) getView().findViewById(R.id.systemSettingFormFragment_textField_QQ);
		mTextFieldQQ.setEnabled(false);
		
		mTextFieldWB = (HyjTextField) getView().findViewById(R.id.systemSettingFormFragment_textField_WB);
		mTextFieldWB.setEnabled(false);
		
		mButtonQQ = (Button) getView().findViewById(R.id.systemSettingFormFragment_button_QQBinding);
		
		setQQField();
		
		mButtonWB = (Button) getView().findViewById(R.id.systemSettingFormFragment_button_WBBinding);
		
		setWBField();
		
		mWBAuth = new WeiboAuth(this.getActivity(), AppConstants.WEIBO_CONNECT_APP_KEY, AppConstants.WEIBO_CONNECT_REDIRECT_URL, AppConstants.WEIBO_CONNECT_SCOPE);
		
		mButtonChangePassword = (Button) getView().findViewById(R.id.systemSettingFormFragment_button_changePassword);
		mButtonChangePassword.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SystemSettingFormFragment.this.openActivityWithFragment(ChangePasswordFragment.class, R.string.changePasswordFragment_title, null);
			}
		});

		if(!HyjApplication.getInstance().getCurrentUser().getUserData().getHasPassword()){
			mButtonChangePassword.setText("设置密码");
		} 
		
		mCheckBoxAddFriendValidation = (CheckBox) getView().findViewById(R.id.systemSettingFormFragment_checkBox_validation_addFriend);
		mCheckBoxAddFriendValidation.setChecked(user.getNewFriendAuthentication());
		mCheckBoxAddFriendValidation.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				HyjApplication.getInstance().getCurrentUser().setNewFriendAuthentication(isChecked);
				HyjApplication.getInstance().getCurrentUser().save();
			}
			
		});
		
		mButtonMoneyExpenseColorPicker = (Button) getView().findViewById(R.id.systemSettingFormFragment_button_moneyExpenseColorPicker);
		mButtonMoneyExpenseColorPicker.setText(null);
		mExpenseColor = Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor());
		mButtonMoneyExpenseColorPicker.setBackgroundColor(mExpenseColor);
		mButtonMoneyExpenseColorPicker.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
						int tempColor = mExpenseColor;
                    	mExpenseColor = mIncomeColor;
                    	mButtonMoneyExpenseColorPicker.setBackgroundColor(mExpenseColor);
                    	
                    	mIncomeColor = tempColor;
                    	mButtonMoneyIncomeColorPicker.setBackgroundColor(mIncomeColor);
                    	
                    	
        				HyjApplication.getInstance().getCurrentUser().getUserData().setExpenseColor(Integer.toHexString(mExpenseColor));
        				HyjApplication.getInstance().getCurrentUser().getUserData().setIncomeColor(Integer.toHexString(mIncomeColor));
        				HyjApplication.getInstance().getCurrentUser().getUserData().save();
                    }  
			
		});
		
		mButtonMoneyIncomeColorPicker = (Button) getView().findViewById(R.id.systemSettingFormFragment_button_moneyIncomeColorPicker);
		mButtonMoneyIncomeColorPicker.setText(null);
		mIncomeColor = Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor());
		mButtonMoneyIncomeColorPicker.setBackgroundColor(mIncomeColor);
		mButtonMoneyIncomeColorPicker.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
						int tempColor = mExpenseColor;
		            	mExpenseColor = mIncomeColor;
		            	mButtonMoneyExpenseColorPicker.setBackgroundColor(mExpenseColor);
		            	
				    	mIncomeColor = tempColor;
                    	mButtonMoneyIncomeColorPicker.setBackgroundColor(mIncomeColor);

                    	HyjApplication.getInstance().getCurrentUser().getUserData().setExpenseColor(Integer.toHexString(mExpenseColor));
        				HyjApplication.getInstance().getCurrentUser().getUserData().setIncomeColor(Integer.toHexString(mIncomeColor));
        				HyjApplication.getInstance().getCurrentUser().getUserData().save();
            }
		});
		
		
//		mLnearLayoutAbout = (LinearLayout) getView().findViewById(R.id.systemSettingFormFragment_linearLayout_about);
//		mLnearLayoutAbout.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
////				SystemSettingFormFragment.this.openActivityWithFragment(AboutFragment.class, R.string.aboutFragment_title, null);
//			}
//		});
		TextView versionText = (TextView) getView().findViewById(R.id.systemSettingFormFragment_textView_version);
		Context appContext = HyjApplication.getInstance().getApplicationContext();
		try {
			versionText.setText(appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		takePictureButton = (HyjImageView) getView().findViewById(R.id.systemSettingFormFragment_imageView_camera);	
		takePictureButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				PopupMenu popup = new PopupMenu(getActivity(), v);
				MenuInflater inflater = popup.getMenuInflater();
				inflater.inflate(R.menu.picture_get_picture, popup.getMenu());
				popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						if (item.getItemId() == R.id.picture_take_picture) {
							takePictureFromCamera();
							return true;
						} else {
							pickPictureFromGallery();
							return true;
						}
						// return false;
					}
				});
				popup.show();
			}
		});
		takePictureButton.setImage(user.getPicture());

		mButtonUploadPicture = (Button) getView().findViewById(R.id.systemSettingFormFragment_button_uploadPicture);
		List<Picture> pics = new Select().from(Picture.class).where("toBeUploaded = ? AND ownerUserId = ? AND lastServerUpdateTime IS NOT NULL", 1, HyjApplication.getInstance().getCurrentUser().getId()).execute();
		if(pics.size() > 0){
			mButtonUploadPicture.setText("上传" + pics.size() + "张大图");
			mButtonUploadPicture.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					List<Picture> pics = new Select().from(Picture.class).where("toBeUploaded = ? AND ownerUserId = ? AND lastServerUpdateTime IS NOT NULL", 1, HyjApplication.getInstance().getCurrentUser().getId()).execute();
					if(pics.size() > 0){
						mButtonUploadPicture.setText("正在上传" + pics.size() + "张大图...");
						Intent startPictureUploadService = new Intent(HyjApplication.getInstance().getApplicationContext(), PictureUploadService.class);
						HyjApplication.getInstance().getApplicationContext().startService(startPictureUploadService);
					} else {
						HyjUtil.displayToast("无大图需要上传");
						mButtonUploadPicture.setText("无大图需要上传");
						v.setEnabled(false);
					}
				}
			});
		} else {
			mButtonUploadPicture.setEnabled(false);
		}
		
		mChangeObserver = new ChangeObserver();
		this.getActivity().getContentResolver().registerContentObserver(ContentProvider.createUri(UserData.class, null), true,
				mChangeObserver);
	}
	
	@Override
	public void onStop() {
		User curUser = HyjApplication.getInstance().getCurrentUser();
		String newNickName = mTextFieldNickName.getText().trim();
		String curNickName = curUser.getNickName();
		if(curNickName == null){
			curNickName = "";
		}
		if(!newNickName.equals(curNickName)){
			curUser.setNickName(newNickName);
			curUser.save();
		}
		super.onStop();
	}

//	@Override
//	public void onDestroy() {
//		User curUser = HyjApplication.getInstance().getCurrentUser();
//		String newNickName = mTextFieldNickName.getText().trim();
//		if(!newNickName.equals(curUser.getNickName())){
//			curUser.setNickName(newNickName);
//			curUser.save();
//		}
//		super.onDestroy();
//	}

	private void setPhoneField() {
		mTextFieldPhone.setText(HyjApplication.getInstance().getCurrentUser().getUserData().getPhone());
		if(mTextFieldPhone.getText() != null && mTextFieldPhone.getText().length() > 0){
			mButtonPhone.setText("解绑");
			mButtonPhone.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Bundle bundle = new Bundle();
					bundle.putString("clickType", "unBindPhone");
					SystemSettingFormFragment.this.openActivityWithFragment(BindPhoneFragment.class, R.string.bindPhoneFragment_unBindPhone_title, bundle);
					
				}
			});
		}else{
			mButtonPhone.setText("绑定");
			mButtonPhone.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					SystemSettingFormFragment.this.openActivityWithFragment(BindPhoneFragment.class, R.string.bindPhoneFragment_title, null);
					
				}
			});
		}
	}
	
	private void setQQField() {
		QQLogin qqLogin = new Select().from(QQLogin.class).where("userId=?", HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
		if(qqLogin != null){
			mTextFieldQQ.setText(qqLogin.getNickName());
			mButtonQQ.setText("解绑");
			mButtonQQ.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(!HyjApplication.getInstance().getCurrentUser().getUserData().getHasPassword()){
						HyjUtil.displayToast("您尚未设置登录密码，请先设置登录密码再解绑");
						return;
					}
					unBindQQ();
				}
			});
		}else{
			mButtonQQ.setText("绑定");
			mTextFieldQQ.setText(null);
			mButtonQQ.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					attemptQQLogin();
				}
			});
		}
	}
	
	public void attemptQQLogin() {
		if(mTencent == null){
			final Context ctxContext = getActivity().getApplicationContext();
			mAppid = AppConstants.TENTCENT_CONNECT_APP_ID;
			mQQAuth = QQAuth.createInstance(mAppid, ctxContext);
			mTencent = Tencent.createInstance(mAppid, getActivity());
		}
		
		if (mQQAuth.isSessionValid()) {
			mQQAuth.logout(getActivity());
		}
		
		IUiListener listener = new BaseUiListener() {
			@Override
			protected void doComplete(JSONObject values) {
				doBindQQ(values);
			}
		};
		mTencent.login(getActivity(), "all", listener);
	}
	
	private void doBindQQ(final JSONObject loginInfo) {    
		// 从服务器上下载用户数据
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				JSONObject jsonObject = (JSONObject) object;
				QQLogin qqLogin = new QQLogin();
				qqLogin.loadFromJSON(jsonObject, true);
				qqLogin.save();
				
				final User user = HyjApplication.getInstance().getCurrentUser();
				if(jsonObject.optString("nickName").length() > 0){
					// 设置用户的昵称拼音, 并同步回服务器
					if(!jsonObject.optString("nickName").equals(user.getNickName())){
						user.setNickName(jsonObject.optString("nickName"));
						mTextFieldNickName.setText(user.getNickName());
					}
				}
				final String figureUrl1 = jsonObject.optString("figureUrl");
				if(figureUrl1.length() > 0){
					HyjAsyncTask.newInstance(new HyjAsyncTaskCallbacks() {
						@Override
						public void finishCallback(Object object) {
							Bitmap thumbnail = null;
							if(object != null){
								thumbnail = (Bitmap) object;
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
										
										user.setPicture(figure);
										figure.save();								
										
										takePictureButton.setImage(figure);
									}
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							
							user.save();
							HyjUtil.displayToast("QQ帐号绑定成功");
							setQQField();
						}
	
						@Override
						public Object doInBackground(String... string) {
							Bitmap thumbnail = null;
							thumbnail = Util.getbitmap(figureUrl1);
							return thumbnail;
						}
					});
				} else {
					user.save();
					HyjUtil.displayToast("QQ帐号绑定成功");
					setQQField();
				}
				
			}

			@Override
			public void errorCallback(Object object) {
				try {
					JSONObject json = (JSONObject) object;
//					((HyjActivity)getActivity()).dismissProgressDialog();
					((HyjActivity)getActivity()).displayDialog("绑定QQ失败", json.getJSONObject("__summary").getString("msg"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		HyjHttpPostAsyncTask.newInstance(serverCallbacks, loginInfo.toString(), "bindQQ");
	}
	
	private class BaseUiListener implements IUiListener {

		@Override
		public void onComplete(Object response) {
//			Util.showResultDialog(LoginActivity.this, response.toString(), " ");
			doComplete((JSONObject)response);
		}

		protected void doComplete(JSONObject values) {

		}

		@Override
		public void onError(UiError e) {
			Util.toastMessage(getActivity(), "出错啦: " + e.errorDetail);
			Util.dismissDialog();
		}

		@Override
		public void onCancel() {
			//Util.toastMessage(LoginActivity.this, "onCancel: ");
			Util.dismissDialog();
		}
	}
	
	private void unBindQQ() {
		// 从服务器上下载用户数据
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
					QQLogin qqLogin = new Select().from(QQLogin.class).where("userId=?", HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
					if(qqLogin != null){
						qqLogin.deleteFromServer();
					}
					setQQField();
					((HyjActivity)getActivity()).dismissProgressDialog();
					HyjUtil.displayToast("解绑成功");
			}

			@Override
			public void errorCallback(Object object) {
				try {
					JSONObject json = (JSONObject) object;
					((HyjActivity)getActivity()).dismissProgressDialog();
					((HyjActivity)getActivity()).displayDialog("解绑QQ不成功",
						json.getJSONObject("__summary").getString("msg"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		QQLogin qqLogin = new Select().from(QQLogin.class).where("userId=?", HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
		if(qqLogin != null){
			((HyjActivity)getActivity()).displayProgressDialog(R.string.systemSettingFormFragment_toast_unBindQQ,
					R.string.systemSettingFormFragment_toast_unBindingQQ);
			HyjHttpPostAsyncTask.newInstance(serverCallbacks, qqLogin.toJSON().toString(), "unBindQQ");
		} else {
			HyjUtil.displayToast("找不到已绑定的QQ帐户");
		}

	}

//	private void fillData(){
//		User modelCopy = (User) mUserEditor.getModelCopy();
//		modelCopy.setNickName(mTextFieldNickName.getText());
//		modelCopy.setNewFriendAuthentication(this.mCheckBoxAddFriendValidation.isChecked());
//		
//
//		UserData userDataCopy = (UserData) mUserDataEditor.getModelCopy();
//		userDataCopy.setEmail(mTextFieldEmail.getText());
//		userDataCopy.setExpenseColor(Integer.toHexString(mExpenseColor));
//		userDataCopy.setIncomeColor(Integer.toHexString(mIncomeColor));
//	}
//	
//	private void showValidatioErrors(){
//		HyjUtil.displayToast(R.string.app_validation_error);
//		
//	}

//	 @Override
//	public void onSave(View v){
//		super.onSave(v);
//		
//		fillData();
//		
//		mUserEditor.validate();
//		
//		if(mUserEditor.hasValidationErrors()){
//			showValidatioErrors();
//		} else {
////			savePictures();
//
//			mUserDataEditor.save();
//			mUserEditor.save();
//			HyjUtil.displayToast(R.string.app_save_success);
//			getActivity().finish();
//		}
//	}	
	 
//	 private void savePictures() {
//			HyjImageField.ImageGridAdapter adapter = mImageFieldPicture.getAdapter();
//			int count = adapter.getCount();
//			boolean mainPicSet = false;
//			for (int i = 0; i < count; i++) {
//				PictureItem pi = adapter.getItem(i);
//				if (pi.getState() == PictureItem.NEW) {
//					Picture newPic = pi.getPicture();
//					newPic.setRecordId(mUserEditor.getModel().getId());
//					newPic.setRecordType("Picture");
//					newPic.save();
//				} else if (pi.getState() == PictureItem.DELETED) {
//					pi.getPicture().delete();
//				} else if (pi.getState() == PictureItem.CHANGED) {
//
//				}
//			if (!mainPicSet && pi.getPicture() != null && pi.getState() != PictureItem.DELETED) {
//					mainPicSet = true;
//					mUserEditor.getModelCopy().setPicture(pi.getPicture());
//				}
//			}
//		}
	
	private void setWBField() {
		WBLogin wbLogin = new Select().from(WBLogin.class).where("userId=?", HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
		if(wbLogin != null){
			mTextFieldWB.setText(wbLogin.getNickName());
			mButtonWB.setText("解绑");
			mButtonWB.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(!HyjApplication.getInstance().getCurrentUser().getUserData().getHasPassword()){
						HyjUtil.displayToast("您尚未设置登录密码，请先设置登录密码再解绑");
						return;
					}
					unBindWB();
				}
			});
		}else{
			mButtonWB.setText("绑定");
			mTextFieldWB.setText(null);
			mButtonWB.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					attemptWBLogin();
				}
			});
		}
	}
	
	public void attemptWBLogin() {
		mSsoHandler = new SsoHandler(this.getActivity(), mWBAuth);
        mSsoHandler.authorize(new AuthListener());
	}
	
	/**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
     *    该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    class AuthListener implements WeiboAuthListener {
        
        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                // 显示 Token
//                updateTokenView(false);
                
            	JSONObject wbJsonObject = new JSONObject();
            	
            	try {
					wbJsonObject.put("openid", values.get("uid"));
					wbJsonObject.put("access_token", values.get("access_token"));
					wbJsonObject.put("expires_in", values.get("expires_in"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
            	doBindWB(wbJsonObject);
            	
                // 保存 Token 到 SharedPreferences
                //HyjUtil.writeAccessToken(mAccessToken);
                Toast.makeText(SystemSettingFormFragment.this.getActivity(), 
                        R.string.weibosdk_demo_toast_auth_success, Toast.LENGTH_SHORT).show();
            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = getString(R.string.weibosdk_demo_toast_auth_failed);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(SystemSettingFormFragment.this.getActivity(), message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(SystemSettingFormFragment.this.getActivity(), 
                    R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(SystemSettingFormFragment.this.getActivity(), 
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
	
	private void doBindWB(final JSONObject loginInfo) {    
		// 从服务器上下载用户数据
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				JSONObject jsonObject = (JSONObject) object;
				WBLogin wbLogin = new WBLogin();
				wbLogin.loadFromJSON(jsonObject, true);
				wbLogin.save();
				
				final User user = HyjApplication.getInstance().getCurrentUser();
				if(jsonObject.optString("nickName").length() > 0){
					// 设置用户的昵称拼音, 并同步回服务器
					if(!jsonObject.optString("nickName").equals(user.getNickName())){
						user.setNickName(jsonObject.optString("nickName"));
						mTextFieldNickName.setText(user.getNickName());
					}
				}
				final String profile_image_url1 = jsonObject.optString("profile_image_url");
				if(profile_image_url1.length() > 0){
					HyjAsyncTask.newInstance(new HyjAsyncTaskCallbacks() {
						@Override
						public void finishCallback(Object object) {
							Bitmap thumbnail = null;
							if(object != null){
								thumbnail = (Bitmap) object;
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
										
										user.setPicture(figure);
										figure.save();								
										
										takePictureButton.setImage(figure);
									}
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							
							user.save();
							HyjUtil.displayToast("WB帐号绑定成功");
							setWBField();
						}
	
						@Override
						public Object doInBackground(String... string) {
							Bitmap thumbnail = null;
							thumbnail = Util.getbitmap(profile_image_url1);
							return thumbnail;
						}
					});
				} else {
					user.save();
					HyjUtil.displayToast("WB帐号绑定成功");
					setWBField();
				}
				
			}

			@Override
			public void errorCallback(Object object) {
				try {
					JSONObject json = (JSONObject) object;
//					((HyjActivity)getActivity()).dismissProgressDialog();
					((HyjActivity)getActivity()).displayDialog("绑定WB失败", json.getJSONObject("__summary").getString("msg"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		HyjHttpPostAsyncTask.newInstance(serverCallbacks, loginInfo.toString(), "bindWB");
	}
	
	private void unBindWB() {
		// 从服务器上下载用户数据
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
					WBLogin wbLogin = new Select().from(WBLogin.class).where("userId=?", HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
					if(wbLogin != null){
						wbLogin.deleteFromServer();
					}
					setWBField();
					((HyjActivity)getActivity()).dismissProgressDialog();
					HyjUtil.displayToast("解绑成功");
			}

			@Override
			public void errorCallback(Object object) {
				try {
					JSONObject json = (JSONObject) object;
					((HyjActivity)getActivity()).dismissProgressDialog();
					((HyjActivity)getActivity()).displayDialog("解绑WB不成功",
						json.getJSONObject("__summary").getString("msg"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		WBLogin wbLogin = new Select().from(WBLogin.class).where("userId=?", HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
		if(wbLogin != null){
			((HyjActivity)getActivity()).displayProgressDialog(R.string.systemSettingFormFragment_toast_unBindWB,
					R.string.systemSettingFormFragment_toast_unBindingWB);
			HyjHttpPostAsyncTask.newInstance(serverCallbacks, wbLogin.toJSON().toString(), "unBindWB");
		} else {
			HyjUtil.displayToast("找不到已绑定的WB帐户");
		}

	}
	 
	 public void takePictureFromCamera() {
			Picture newPicture = new Picture();
			Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// Ensure that there's a camera activity to handle the intent
			if (takePictureIntent.resolveActivity(getActivity()
					.getPackageManager()) != null) {
				// Create the File where the photo should go
				File photoFile = null;
				try {
					photoFile = HyjUtil.createImageFile(newPicture.getId());
					// Continue only if the File was successfully created
					if (photoFile != null) {
						takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
								Uri.fromFile(photoFile));
						((HyjActivity) getActivity()).startActivityForResult(
								takePictureIntent, HyjActivity.REQUEST_TAKE_PHOTO);

						IntentFilter intentFilter = new IntentFilter(
								"REQUEST_TAKE_PHOTO");
						BroadcastReceiver receiver = new TakePhotoBroadcastReceiver(
								photoFile, newPicture);
						getActivity().registerReceiver(receiver, intentFilter);
					} else {
						HyjUtil.displayToast(R.string.imageField_cannot_save_picture);
					}
				} catch (IOException ex) {
					// Error occurred while creating the File
					HyjUtil.displayToast(R.string.imageField_cannot_save_picture);
				}
			}
		}

		public void pickPictureFromGallery() {
			Picture newPicture = new Picture();
			Intent takePictureIntent = new Intent(Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			// Ensure that there's a camera activity to handle the intent
			if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
				// Create the File where the photo should go
				File photoFile = null;
				try {
					photoFile = HyjUtil.createImageFile(newPicture.getId());
					// Continue only if the File was successfully created
					if (photoFile != null) {
						((HyjActivity) getActivity()).startActivityForResult(
								takePictureIntent, HyjActivity.REQUEST_TAKE_PHOTO);

						IntentFilter intentFilter = new IntentFilter(
								"REQUEST_TAKE_PHOTO");
						BroadcastReceiver receiver = new TakePhotoBroadcastReceiver(
								photoFile, newPicture);
						getActivity().registerReceiver(receiver, intentFilter);
					} else {
						HyjUtil.displayToast(R.string.imageField_cannot_save_picture);
					}
				} catch (IOException ex) {
					// Error occurred while creating the File
					HyjUtil.displayToast(R.string.imageField_cannot_save_picture);
				}
			}
		}
		private class TakePhotoBroadcastReceiver extends BroadcastReceiver {
			File mPhotoFile;
			Picture mPicture;

			TakePhotoBroadcastReceiver(File photoFile, Picture picture) {
				mPhotoFile = photoFile;
				mPicture = picture;
			}

			@Override
			public void onReceive(Context context, Intent intent) {
				try {
					getActivity().unregisterReceiver(this);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (intent.getAction().equals("REQUEST_TAKE_PHOTO")) {
					int result = intent.getIntExtra("resultCode",
							Activity.RESULT_CANCELED);
					if (result == Activity.RESULT_OK) {
						float pxW = TypedValue.applyDimension(
								TypedValue.COMPLEX_UNIT_DIP, 400,
								r.getDisplayMetrics());
						float pxH = TypedValue.applyDimension(
								TypedValue.COMPLEX_UNIT_DIP, 600,
								r.getDisplayMetrics());
						FileOutputStream out = null;
						String picturePath;
						
						if(intent.getStringExtra("selectedImage") != null){
							Uri selectedImage = Uri.parse(intent.getStringExtra("selectedImage"));
							String[] filePathColumn = { MediaStore.Images.Media.DATA };
							Cursor cursor = getActivity().getContentResolver()
									.query(selectedImage, filePathColumn, null,
											null, null);
							cursor.moveToFirst();

							int columnIndex = cursor
									.getColumnIndex(filePathColumn[0]);
							picturePath = cursor.getString(columnIndex);
							cursor.close();
						} else {
							picturePath = mPhotoFile.getAbsolutePath();
						}

						Bitmap scaled = HyjUtil.decodeSampledBitmapFromFile(
								picturePath, (int) pxW, (int) pxH);

						int px = (int) TypedValue.applyDimension(
								TypedValue.COMPLEX_UNIT_DIP, 56,
								r.getDisplayMetrics());
						Bitmap thumbnail = ThumbnailUtils.extractThumbnail(
								scaled, px, px);
						
						try {
							out = new FileOutputStream(
									HyjUtil.createImageFile(mPicture.getId() + "_icon"));
							thumbnail.compress(Bitmap.CompressFormat.JPEG, 60, out);
							out.close();
							out = null;
							thumbnail.recycle();
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (out != null) {
							try {
								out.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
							out = null;
						}

						scaled.recycle();

						User curUser = HyjApplication.getInstance().getCurrentUser();
						mPicture.setPictureType("JPEG");
						mPicture.setRecordType("User");
						mPicture.setToBeUploaded(false);
						mPicture.setRecordId(curUser.getId());
						try{
							ActiveAndroid.beginTransaction();
							mPicture.save();
							Picture oldPic = curUser.getPicture();
							if(oldPic != null){
								oldPic.delete();
							}
							curUser.setPicture(mPicture);
							curUser.save();
							ActiveAndroid.setTransactionSuccessful();
							takePictureButton.setImage(mPicture);
						} catch (Exception e){
						}
						ActiveAndroid.endTransaction();
					} else {
						if (!mPhotoFile.exists()) {
							//HyjUtil.displayToast(R.string.imageField_cannot_save_picture);
						} else {
							mPhotoFile.delete();
						}
					}
				}
			}
		}
	 
//	 @Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//         switch(requestCode){
//             case 1:
//          }
//    }
	 
	 
	 
	 private class ChangeObserver extends ContentObserver {
			public ChangeObserver() {
				super(new Handler());
			}

			@Override
			public boolean deliverSelfNotifications() {
				return true;
			}

			@Override
			public void onChange(boolean selfChange) {
				setPhoneField();
				if(HyjApplication.getInstance().getCurrentUser().getUserData().getHasPassword()){
					mButtonChangePassword.setText("修改密码");
				} 
			}
		}
}
