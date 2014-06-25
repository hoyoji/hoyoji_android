package com.hoyoji.hoyoji.setting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.TypedValue;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.userdatabase.HyjUserDbHelper;
import com.hoyoji.android.hyjframework.userdatabase.HyjUserDbContract.UserDatabaseEntry;
import com.hoyoji.android.hyjframework.view.HyjImageField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.android.hyjframework.view.HyjImageField.PictureItem;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.AppConstants;
import com.hoyoji.hoyoji.LoginActivity;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.QQLogin;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.models.UserData;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQAuth;
import com.tencent.sample.Util;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;


public class SystemSettingFormFragment extends HyjUserFormFragment {
	private HyjModelEditor<User> mUserEditor = null;
	private HyjModelEditor<UserData> mUserDataEditor = null;
	private HyjTextField mTextFieldUserName = null;
	private HyjTextField mTextFieldNickName = null;
	private HyjTextField mTextFieldEmail = null;
	private Button mButtonCheckEmail = null;
	private HyjTextField mTextFieldPhone = null;
	private Button mButtonPhone = null;
	private HyjTextField mTextFieldQQ = null;
	private Button mButtonQQ = null;
	private Button mButtonChangePassword = null;
	private CheckBox mCheckBoxAddFriendValidation = null;
	private Button mButtonMoneyExpenseColorPicker = null;
	private Button mButtonMoneyIncomeColorPicker = null;
	private LinearLayout mLnearLayoutAbout = null;
	private ChangeObserver mChangeObserver;
	
    public static QQAuth mQQAuth;
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
		User user;
//		Intent intent = getActivity().getIntent();
//		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		user =  new Select().from(User.class).where("id=?", HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
		
		mUserEditor = user.newModelEditor();
		mUserDataEditor = user.getUserData().newModelEditor();
		
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
		
		mButtonQQ = (Button) getView().findViewById(R.id.systemSettingFormFragment_button_QQBinding);
		
		setQQField();
		
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
		
		mButtonMoneyExpenseColorPicker = (Button) getView().findViewById(R.id.systemSettingFormFragment_button_moneyExpenseColorPicker);
		mButtonMoneyExpenseColorPicker.setText(null);
		mExpenseColor = Color.parseColor("#FF0000");
		if(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor() != null){
			mExpenseColor = Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor());
		}
		mButtonMoneyExpenseColorPicker.setBackgroundColor(mExpenseColor);
		mButtonMoneyExpenseColorPicker.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ColorPickerDialog dialog = new ColorPickerDialog(getActivity(), mExpenseColor,   
                        HyjApplication.getInstance().getString(R.string.systemSettingFormFragment_button_moneyExpenseColorPicker),   
                        new ColorPickerDialog.OnColorChangedListener() {  
                      
                    @Override  
                    public void colorChanged(int color) {  
                    	mExpenseColor = color;
                    	mButtonMoneyExpenseColorPicker.setBackgroundColor(color);
                    }  
                });  
                dialog.show(getFragmentManager(), "ColorPickerDialog");  
			}
		});
		
		mButtonMoneyIncomeColorPicker = (Button) getView().findViewById(R.id.systemSettingFormFragment_button_moneyIncomeColorPicker);
		mButtonMoneyIncomeColorPicker.setText(null);
		mIncomeColor = Color.parseColor("#339900");
		if(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor() != null){
			mIncomeColor = Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor());
		}
		mButtonMoneyIncomeColorPicker.setBackgroundColor(mIncomeColor);
		mButtonMoneyIncomeColorPicker.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ColorPickerDialog dialog = new ColorPickerDialog(getActivity(), mIncomeColor,   
                        HyjApplication.getInstance().getString(R.string.systemSettingFormFragment_button_moneyIncomeColorPicker),   
                        new ColorPickerDialog.OnColorChangedListener() {  
                      
                    @Override  
                    public void colorChanged(int color) { 
                    	mIncomeColor = color;
                    	mButtonMoneyIncomeColorPicker.setBackgroundColor(color);
                    }  
                });  
                dialog.show(getFragmentManager(), "ColorPickerDialog");  
			}
		});
		
		
		mLnearLayoutAbout = (LinearLayout) getView().findViewById(R.id.systemSettingFormFragment_linearLayout_about);
		mLnearLayoutAbout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SystemSettingFormFragment.this.openActivityWithFragment(AboutFragment.class, R.string.aboutFragment_title, null);
			}
		});
		
		ImageView takePictureButton = (ImageView) getView().findViewById(R.id.systemSettingFormFragment_imageView_camera);	
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
							//mImageFieldPicture.takePictureFromCamera();
							return true;
						} else {
							//mImageFieldPicture.pickPictureFromGallery();
							return true;
						}
						// return false;
					}
				});
				popup.show();
			}
		});
		
		mChangeObserver = new ChangeObserver();
		this.getActivity().getContentResolver().registerContentObserver(ContentProvider.createUri(UserData.class, null), true,
				mChangeObserver);
	}
	
	

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
				HyjUtil.displayToast("QQ帐号绑定成功");
				setQQField();
			}

			@Override
			public void errorCallback(Object object) {
				try {
					JSONObject json = (JSONObject) object;
					((HyjActivity)getActivity()).dismissProgressDialog();
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

	private void fillData(){
		User modelCopy = (User) mUserEditor.getModelCopy();
		modelCopy.setNickName(mTextFieldNickName.getText());
		modelCopy.setNewFriendAuthentication(this.mCheckBoxAddFriendValidation.isChecked());
		

		UserData userDataCopy = (UserData) mUserDataEditor.getModelCopy();
		userDataCopy.setEmail(mTextFieldEmail.getText());
		userDataCopy.setExpenseColor(Integer.toHexString(mExpenseColor));
		userDataCopy.setIncomeColor(Integer.toHexString(mIncomeColor));
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		mUserEditor.validate();
		
		if(mUserEditor.hasValidationErrors()){
			showValidatioErrors();
		} else {
			savePictures();

			mUserDataEditor.save();
			mUserEditor.save();
			HyjUtil.displayToast(R.string.app_save_success);
			getActivity().finish();
		}
	}	
	 
	 private void savePictures() {
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
//				if (!mainPicSet && pi.getPicture() != null) {
//					mainPicSet = true;
//					mUserEditor.getModelCopy().setPicture(pi.getPicture());
//				}
//			}
		}
	 
//	 public void takePictureFromCamera() {
//			Picture newPicture = new Picture();
//			Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//			// Ensure that there's a camera activity to handle the intent
//			if (takePictureIntent.resolveActivity(getActivity().getContext()
//					.getPackageManager()) != null) {
//				// Create the File where the photo should go
//				File photoFile = null;
//				try {
//					photoFile = HyjUtil.createImageFile(newPicture.getId());
//					// Continue only if the File was successfully created
//					if (photoFile != null) {
//						takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//								Uri.fromFile(photoFile));
//						((HyjActivity) getActivity().getContext()).startActivityForResult(
//								takePictureIntent, HyjActivity.REQUEST_TAKE_PHOTO);
//
//						IntentFilter intentFilter = new IntentFilter(
//								"REQUEST_TAKE_PHOTO");
//						BroadcastReceiver receiver = new TakePhotoBroadcastReceiver(
//								photoFile, newPicture);
//						getContext().registerReceiver(receiver, intentFilter);
//					}
//				} catch (IOException ex) {
//					// Error occurred while creating the File
//					HyjUtil.displayToast(R.string.imageField_cannot_save_picture);
//				}
//			}
//		}
//
//		public void pickPictureFromGallery() {
//			Picture newPicture = new Picture();
//			Intent takePictureIntent = new Intent(Intent.ACTION_PICK,
//					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//			// Ensure that there's a camera activity to handle the intent
//			if (takePictureIntent.resolveActivity(this.getContext()
//					.getPackageManager()) != null) {
//				// Create the File where the photo should go
//				File photoFile = null;
//				try {
//					photoFile = HyjUtil.createImageFile(newPicture.getId());
//					// Continue only if the File was successfully created
//					if (photoFile != null) {
//						((HyjActivity) getContext()).startActivityForResult(
//								takePictureIntent, HyjActivity.REQUEST_TAKE_PHOTO);
//
//						IntentFilter intentFilter = new IntentFilter(
//								"REQUEST_TAKE_PHOTO");
//						BroadcastReceiver receiver = new TakePhotoBroadcastReceiver(
//								photoFile, newPicture);
//						getContext().registerReceiver(receiver, intentFilter);
//					}
//				} catch (IOException ex) {
//					// Error occurred while creating the File
//					HyjUtil.displayToast(R.string.imageField_cannot_save_picture);
//				}
//			}
//		}
//		private class TakePhotoBroadcastReceiver extends BroadcastReceiver {
//			File mPhotoFile;
//			Picture mPicture;
//
//			TakePhotoBroadcastReceiver(File photoFile, Picture picture) {
//				mPhotoFile = photoFile;
//				mPicture = picture;
//			}
//
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				try {
//					getContext().unregisterReceiver(this);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				if (intent.getAction().equals("REQUEST_TAKE_PHOTO")) {
//					int result = intent.getIntExtra("resultCode",
//							Activity.RESULT_CANCELED);
//					if (result == Activity.RESULT_OK) {
//						float pxW = TypedValue.applyDimension(
//								TypedValue.COMPLEX_UNIT_DIP, 50,
//								r.getDisplayMetrics());
//						float pxH = TypedValue.applyDimension(
//								TypedValue.COMPLEX_UNIT_DIP, 80,
//								r.getDisplayMetrics());
//						FileOutputStream out = null;
//						String picturePath;
//						
//						if(intent.getStringExtra("selectedImage") != null){
//							Uri selectedImage = Uri.parse(intent.getStringExtra("selectedImage"));
//							String[] filePathColumn = { MediaStore.Images.Media.DATA };
//							Cursor cursor = getContext().getContentResolver()
//									.query(selectedImage, filePathColumn, null,
//											null, null);
//							cursor.moveToFirst();
//
//							int columnIndex = cursor
//									.getColumnIndex(filePathColumn[0]);
//							picturePath = cursor.getString(columnIndex);
//							cursor.close();
//						} else {
//							picturePath = mPhotoFile.getAbsolutePath();
//						}
//
//						Bitmap scaled = HyjUtil.decodeSampledBitmapFromFile(
//								picturePath, (int) pxW, (int) pxH);
//
//						int px = (int) TypedValue.applyDimension(
//								TypedValue.COMPLEX_UNIT_DIP, 56,
//								r.getDisplayMetrics());
//						Bitmap thumbnail = ThumbnailUtils.extractThumbnail(
//								scaled, px, px);
//						
//						try {
//							out = new FileOutputStream(mPhotoFile);
//							scaled.compress(Bitmap.CompressFormat.JPEG, 60, out);
//							out.close();
//							out = null;
//							
//							out = new FileOutputStream(
//									HyjUtil.createImageFile(mPicture.getId()
//											+ "_icon"));
//							thumbnail.compress(Bitmap.CompressFormat.JPEG, 60, out);
//							out.close();
//							out = null;
//							thumbnail.recycle();
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//						if (out != null) {
//							try {
//								out.close();
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
//							out = null;
//						}
//
//						scaled.recycle();
//
//						mPicture.setPictureType("JPEG");
//					} else {
//						if (!mPhotoFile.exists()) {
//							//HyjUtil.displayToast(R.string.imageField_cannot_save_picture);
//						} else {
//							mPhotoFile.delete();
//						}
//					}
//				}
//			}
//		}
	 
	 @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode){
             case 1:
          }
    }
	 
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
			}
		}
}
