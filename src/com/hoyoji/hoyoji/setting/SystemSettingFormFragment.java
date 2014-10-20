package com.hoyoji.hoyoji.setting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
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
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFragment;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.PictureUploadService;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.User;


public class SystemSettingFormFragment extends HyjUserFragment {
	private TextView mTextFieldUserName = null;
	private TextView mTextFieldNickName = null;
	private Button mButtonChangePassword = null;
	private Button mButtonUploadPicture = null;
	private CheckBox mCheckBoxAddFriendValidation = null;
	private Button mButtonMoneyExpenseColorPicker = null;
	private Button mButtonMoneyIncomeColorPicker = null;
	private HyjImageView takePictureButton = null;
//	private ChangeObserver mChangeObserver;
	
	private RelativeLayout mRelativeLayoutBindID = null;
	private Button mButtonSwitchUser = null;

	private Resources r = null;
	
	private int mExpenseColor = 0;
	private int mIncomeColor = 0;
	
	private FrameLayout mLinearLayoutChangeNickName = null;
	
	
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
		
		mTextFieldUserName = (TextView) getView().findViewById(R.id.systemSettingFormFragment_textField_userName);
		mTextFieldUserName.setText(user.getUserName());
		mTextFieldUserName.setEnabled(false);
		
		mTextFieldNickName = (TextView) getView().findViewById(R.id.systemSettingFormFragment_textField_nickName);
		
		if(user.getNickName() == null || user.getNickName().equals("")) {
			mTextFieldNickName.setText("无昵称");
		} else {
			mTextFieldNickName.setText(user.getNickName());
		}
		
		
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
		
//		mChangeObserver = new ChangeObserver();
//		this.getActivity().getContentResolver().registerContentObserver(ContentProvider.createUri(UserData.class, null), true,
//				mChangeObserver);
		
		mRelativeLayoutBindID = (RelativeLayout) getView().findViewById(R.id.bindID_relativeLayout);	
		mRelativeLayoutBindID.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				SystemSettingFormFragment.this.openActivityWithFragment(BindIDFragment.class, R.string.bindIDFragment_title, null);
			}
		});
		
		mButtonSwitchUser = (Button) getView().findViewById(R.id.systemSettingFormFragment_button_switchUser);	
		mButtonSwitchUser.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				HyjApplication.getInstance().switchUser();
			}
		});
		
		mLinearLayoutChangeNickName = (FrameLayout) getView().findViewById(R.id.change_nickname);	
		mLinearLayoutChangeNickName.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				SystemSettingFormFragment.this.openActivityWithFragment(ChangeNickNameFormFragment.class, R.string.changeNickNameFormFragment_title, null);
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mTextFieldNickName.setText(HyjApplication.getInstance().getCurrentUser().getNickName());
		takePictureButton.setImage(HyjApplication.getInstance().getCurrentUser().getPicture());
    }
	
	@Override
	public void onStop() {
//		User curUser = HyjApplication.getInstance().getCurrentUser();
//		String newNickName = mTextFieldNickName.getText().trim();
//		String curNickName = curUser.getNickName();
//		if(curNickName == null){
//			curNickName = "";
//		}
//		if(!newNickName.equals(curNickName)){
//			curUser.setNickName(newNickName);
//			curUser.save();
//		}
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
	
}
