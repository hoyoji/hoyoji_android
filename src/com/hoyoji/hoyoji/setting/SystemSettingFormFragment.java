package com.hoyoji.hoyoji.setting;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.view.HyjImageField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.android.hyjframework.view.HyjImageField.PictureItem;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.FriendCategory;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.friend.FriendCategoryListFragment;


public class SystemSettingFormFragment extends HyjUserFormFragment {
	private HyjModelEditor<User> mUserEditor = null;
	private HyjModelEditor<UserData> mUserDataEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private HyjTextField mTextFieldUserName = null;
	private HyjTextField mTextFieldNickName = null;
	private HyjTextField mTextFieldEmail = null;
	private Button mButtonCheckEmail = null;
	private HyjTextField mTextFieldPhone = null;
	private Button mButtonPhone = null;
	private Button mButtonChangePassword = null;
	private CheckBox mCheckBoxAddFriendValidation = null;
	private Button mButtonMoneyExpenseColorPicker = null;
	private Button mButtonMoneyIncomeColorPicker = null;
	private LinearLayout mLnearLayoutAbout = null;
	private ChangeObserver mChangeObserver;
	
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
		
		mImageFieldPicture = (HyjImageField) getView().findViewById(R.id.systemSettingFormFragment_imageField_picture);		
		mImageFieldPicture.setImages(user.getPictures());
		
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
							mImageFieldPicture.takePictureFromCamera();
							return true;
						} else {
							mImageFieldPicture.pickPictureFromGallery();
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
			HyjImageField.ImageGridAdapter adapter = mImageFieldPicture.getAdapter();
			int count = adapter.getCount();
			boolean mainPicSet = false;
			for (int i = 0; i < count; i++) {
				PictureItem pi = adapter.getItem(i);
				if (pi.getState() == PictureItem.NEW) {
					Picture newPic = pi.getPicture();
					newPic.setRecordId(mUserEditor.getModel().getId());
					newPic.setRecordType("Picture");
					newPic.save();
				} else if (pi.getState() == PictureItem.DELETED) {
					pi.getPicture().delete();
				} else if (pi.getState() == PictureItem.CHANGED) {

				}
				if (!mainPicSet && pi.getPicture() != null) {
					mainPicSet = true;
					mUserEditor.getModelCopy().setPicture(pi.getPicture());
				}
			}
		}
	 
	
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
