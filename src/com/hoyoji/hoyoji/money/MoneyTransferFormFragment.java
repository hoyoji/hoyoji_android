package com.hoyoji.hoyoji.money;

import android.R.color;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeField;
import com.hoyoji.android.hyjframework.view.HyjImageField;
import com.hoyoji.android.hyjframework.view.HyjImageField.PictureItem;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyTransfer;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;


public class MoneyTransferFormFragment extends HyjUserFormFragment {
	private final static int GET_TRANSFEROUT_FRIEND_ID = 1;
	private final static int GET_TRANSFEROUT_ID = 2;
	private final static int GET_TRANSFERIN_FRIEND_ID = 3;
	private final static int GET_TRANSFERIN_ID = 4;
	private final static int GET_PROJECT_ID = 5;
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;
	
	private HyjModelEditor<MoneyTransfer> mMoneyTransferEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericTransferOutAmount = null;
	private HyjSelectorField mSelectorFieldTransferOutFriend = null;
	private ImageView mImageViewClearTransferOutFriend = null;
	private View mViewSeparatorTransferOut = null;
	private HyjSelectorField mSelectorFieldTransferOut = null;
	private HyjSelectorField mSelectorFieldTransferInFriend = null;
	private ImageView mImageViewClearTransferInFriend = null;
	private View mViewSeparatorTransferIn = null;
	private HyjSelectorField mSelectorFieldTransferIn = null;
	private HyjNumericField mNumericTransferInAmount = null;
	private View mViewSeparatorTransferInAmount = null;
	private HyjSelectorField mSelectorFieldProject = null;
	private HyjNumericField mNumericExchangeRate = null;
	private HyjRemarkField mRemarkFieldRemark = null;
	private ImageView mImageViewRefreshRate = null;
	private View mViewSeparatorExchange = null;
	private LinearLayout mLinearLayoutExchangeRate = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.money_formfragment_moneytransfer;
	}
	 
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		MoneyTransfer moneyTransfer;
		
		Intent intent = getActivity().getIntent();
		long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			moneyTransfer =  new Select().from(MoneyTransfer.class).where("_id=?", modelId).executeSingle();
		} else {
			moneyTransfer = new MoneyTransfer();
			
		}
		mMoneyTransferEditor = moneyTransfer.newModelEditor();
		
		setupDeleteButton(mMoneyTransferEditor);
		
		mImageFieldPicture = (HyjImageField) getView().findViewById(R.id.moneyTransferFormFragment_imageField_picture);		
		mImageFieldPicture.setImages(moneyTransfer.getPictures());
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyTransferFormFragment_textField_date);	
		if(modelId != -1){
			mDateTimeFieldDate.setText(moneyTransfer.getDate());
		}
		
		mNumericTransferOutAmount = (HyjNumericField) getView().findViewById(R.id.moneyTransferFormFragment_textField_transferOutAmount);		
		mNumericTransferOutAmount.setNumber(moneyTransfer.getTransferOutAmount());
		mNumericTransferOutAmount.getEditText().addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if(s!= null && s.length()>0 && mNumericExchangeRate.getNumber() != null){
					mNumericTransferInAmount.setNumber(Double.valueOf(s.toString()) * mNumericExchangeRate.getNumber());
				}else{
					mNumericTransferInAmount.setNumber(null);
				}
			}
			
		});
		
		Friend transferOutFriend = moneyTransfer.getTransferOutFriend();
		mSelectorFieldTransferOutFriend = (HyjSelectorField) getView().findViewById(R.id.moneyTransferFormFragment_selectorField_transferOutFriend);
		if(transferOutFriend != null){
			mSelectorFieldTransferOutFriend.setModelId(transferOutFriend.getId());
			mSelectorFieldTransferOutFriend.setText(transferOutFriend.getDisplayName());
		}
		mSelectorFieldTransferOutFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyTransferFormFragment.this
				.openActivityWithFragmentForResult(FriendListFragment.class, R.string.friendListFragment_title_select_friend_transferOut, null, GET_TRANSFEROUT_FRIEND_ID);
			}
		}); 
		
		mImageViewClearTransferOutFriend = (ImageView) getView().findViewById(
				R.id.moneyTransferFormFragment_imageView_clear_transferOutFriend);
		mImageViewClearTransferOutFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSelectorFieldTransferOutFriend.setModelId(null);
				mSelectorFieldTransferOutFriend.setText("");
				MoneyAccount activeAccount = HyjModel.getModel(MoneyAccount.class, HyjApplication.getInstance().getCurrentUser().getUserData().getActiveMoneyAccountId());
				mSelectorFieldTransferOut.setText(activeAccount.getName());
				mSelectorFieldTransferOut.setModelId(activeAccount.getId());
				mViewSeparatorTransferOut.setVisibility(View.VISIBLE);
         		mSelectorFieldTransferOut.setVisibility(View.VISIBLE);
         		
			}
		});
		
		mViewSeparatorTransferOut = (View) getView().findViewById(R.id.moneyTransferFormFragment_separatorField_transferOut);
		
		
		MoneyAccount transferOut = moneyTransfer.getTransferOut();
		mSelectorFieldTransferOut = (HyjSelectorField) getView().findViewById(R.id.moneyTransferFormFragment_selectorField_transferOut);
		if(transferOut != null){
			mSelectorFieldTransferOut.setModelId(transferOut.getId());
			mSelectorFieldTransferOut.setText(transferOut.getName() + "(" + transferOut.getCurrencyId() + ")");
		}
		mSelectorFieldTransferOut.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("excludeType", "Debt");
				
				MoneyTransferFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, bundle, GET_TRANSFEROUT_ID);
			}
		});	
		
		Friend transferInFriend = moneyTransfer.getTransferInFriend();
		mSelectorFieldTransferInFriend = (HyjSelectorField) getView().findViewById(R.id.moneyTransferFormFragment_selectorField_transferInFriend);
		if(transferInFriend != null){
			mSelectorFieldTransferInFriend.setModelId(transferInFriend.getId());
			mSelectorFieldTransferInFriend.setText(transferInFriend.getDisplayName());
		}
		mSelectorFieldTransferInFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyTransferFormFragment.this
				.openActivityWithFragmentForResult(FriendListFragment.class, R.string.friendListFragment_title_select_friend_transferIn, null, GET_TRANSFERIN_FRIEND_ID);
			}
		}); 
		
		mImageViewClearTransferInFriend = (ImageView) getView().findViewById(
				R.id.moneyTransferFormFragment_imageView_clear_transferInFriend);
		mImageViewClearTransferInFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSelectorFieldTransferInFriend.setModelId(null);
				mSelectorFieldTransferInFriend.setText("");
				MoneyAccount activeAccount = HyjModel.getModel(MoneyAccount.class, HyjApplication.getInstance().getCurrentUser().getUserData().getActiveMoneyAccountId());
				mSelectorFieldTransferIn.setText(activeAccount.getName());
				mSelectorFieldTransferIn.setModelId(activeAccount.getId());
				mViewSeparatorTransferIn.setVisibility(View.VISIBLE);
         		mSelectorFieldTransferIn.setVisibility(View.VISIBLE);
			}
		});
		
		mViewSeparatorTransferIn = (View) getView().findViewById(R.id.moneyTransferFormFragment_separatorField_transferIn);
		
		MoneyAccount transferIn = moneyTransfer.getTransferIn();
		mSelectorFieldTransferIn = (HyjSelectorField) getView().findViewById(R.id.moneyTransferFormFragment_selectorField_transferIn);
		if(transferIn != null){
			mSelectorFieldTransferIn.setModelId(transferIn.getId());
			mSelectorFieldTransferIn.setText(transferIn.getName() + "(" + transferIn.getCurrencyId() + ")");
		}
		mSelectorFieldTransferIn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("excludeType", "Debt");
				
				MoneyTransferFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, bundle, GET_TRANSFERIN_ID);
			}
		});	
		
		mNumericTransferInAmount = (HyjNumericField) getView().findViewById(R.id.moneyTransferFormFragment_textField_transferInAmount);		
		mNumericTransferInAmount.setNumber(moneyTransfer.getTransferInAmount());
		mNumericTransferInAmount.setEnabled(false);
		
		mViewSeparatorTransferInAmount = (View) getView().findViewById(R.id.moneyTransferFormFragment_separatorField_transferInamount);
		
		Project project = moneyTransfer.getProject();
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(R.id.moneyTransferFormFragment_selectorField_project);
		
		if(project != null){
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getDisplayName());
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyTransferFormFragment.this.openActivityWithFragmentForResult(ProjectListFragment.class, R.string.projectListFragment_title_select_project, null, GET_PROJECT_ID);
			}
		});	
		
		
		mNumericExchangeRate = (HyjNumericField) getView().findViewById(R.id.moneyTransferFormFragment_textField_exchangeRate);		
		mNumericExchangeRate.setNumber(moneyTransfer.getExchangeRate());
		mNumericExchangeRate.getEditText().addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if(s!= null && s.length()>0 && mNumericTransferOutAmount.getNumber() != null){
					mNumericTransferInAmount.setNumber(Double.valueOf(s.toString()) * mNumericTransferOutAmount.getNumber());
				}else{
					mNumericTransferInAmount.setNumber(null);
				}
			}
			
		});
		
		mViewSeparatorExchange = (View) getView().findViewById(R.id.moneyTransferFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(R.id.moneyTransferFormFragment_linearLayout_exchangeRate);
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyTransferFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyTransfer.getRemark());
		
		ImageView takePictureButton = (ImageView) getView().findViewById(R.id.moneyTransferFormFragment_imageView_camera);	
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
				
				if(mSelectorFieldTransferOutFriend.getModelId() == null && mSelectorFieldTransferInFriend.getModelId() == null && (mSelectorFieldTransferOut.getModelId()== null || mSelectorFieldTransferIn.getModelId() == null)){
					for(int i = 0; i<popup.getMenu().size();i++){
						popup.getMenu().setGroupEnabled(i, false);
					}
				}
				
				popup.show();	
			}
		});
		
		mImageViewRefreshRate = (ImageView) getView().findViewById(R.id.moneyTransferFormFragment_imageButton_refresh_exchangeRate);	
		mImageViewRefreshRate.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mSelectorFieldTransferOut.getModelId() != null && mSelectorFieldTransferIn.getModelId() != null){
					MoneyAccount transferOut = HyjModel.getModel(MoneyAccount.class, mSelectorFieldTransferOut.getModelId());
					MoneyAccount transferIn = HyjModel.getModel(MoneyAccount.class, mSelectorFieldTransferIn.getModelId());
					
					String fromCurrency = transferOut.getCurrencyId();
					String toCurrency = transferIn.getCurrencyId();
					if(fromCurrency != null && toCurrency != null){
						HyjUtil.startRoateView(mImageViewRefreshRate);
						mImageViewRefreshRate.setEnabled(false);
						HyjUtil.updateExchangeRate(fromCurrency, toCurrency, mImageViewRefreshRate, mNumericExchangeRate);
					} else {
						HyjUtil.displayToast(R.string.moneyTransferFormFragment_toast_select_currency);
					}
				}else{
					HyjUtil.displayToast(R.string.moneyTransferFormFragment_toast_select_currency);
				}
			}
		});
		
			setExchangeRate();
			setPermission();
			
			// 只在新增时才自动打开软键盘， 修改时不自动打开
			if (modelId == -1) {
				this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    super.onCreateOptionsMenu(menu, inflater);
	    if(mMoneyTransferEditor!= null && mMoneyTransferEditor.getModelCopy().get_mId() != null && (mSelectorFieldTransferOutFriend.getModelId() == null 
				&& mSelectorFieldTransferInFriend.getModelId() == null 
				&& (mSelectorFieldTransferOut.getModelId()== null || mSelectorFieldTransferIn.getModelId() == null))){
	    	hideSaveAction();
	    }
	}
	
	private void setPermission() {
		if(mSelectorFieldTransferOutFriend.getModelId() == null 
				&& mSelectorFieldTransferInFriend.getModelId() == null 
				&& (mSelectorFieldTransferOut.getModelId()== null || mSelectorFieldTransferIn.getModelId() == null)){
			
			mDateTimeFieldDate.setEnabled(false);
			mNumericTransferOutAmount.setEnabled(false);
			mSelectorFieldTransferOutFriend.setEnabled(false);
			mSelectorFieldTransferOut.setEnabled(false);
			mNumericTransferInAmount.setEnabled(false);
			mSelectorFieldTransferInFriend.setEnabled(false);
			mSelectorFieldTransferIn.setEnabled(false);
			mNumericExchangeRate.setEnabled(false);
			mSelectorFieldProject.setEnabled(false);
			mRemarkFieldRemark.setEnabled(false);
			
			if(this.mOptionsMenu != null){
		    	hideSaveAction();
			}
			
			if(mSelectorFieldTransferOut.getModelId() == null){
				mSelectorFieldTransferOutFriend.setText("无转出人");
			}else if(mSelectorFieldTransferIn.getModelId() == null){
				mSelectorFieldTransferInFriend.setText("无转入人");
			}
			
		}else if(mSelectorFieldTransferOutFriend.getModelId() != null){
     		mViewSeparatorTransferOut.setVisibility(View.GONE);
     		mSelectorFieldTransferOut.setVisibility(View.GONE);
		}else if(mSelectorFieldTransferInFriend.getModelId() != null){
     		mViewSeparatorTransferIn.setVisibility(View.GONE);
     		mSelectorFieldTransferIn.setVisibility(View.GONE);
		}
	}

	private void setupDeleteButton(HyjModelEditor<MoneyTransfer> moneyTransferEditor) {

		Button buttonDelete = (Button) getView().findViewById(R.id.button_delete);
		
		final MoneyTransfer moneyTransfer = moneyTransferEditor.getModelCopy();
		
		if (moneyTransfer.get_mId() == null) {
			buttonDelete.setVisibility(View.GONE);
		} else {
			buttonDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					((HyjActivity)getActivity()).displayDialog(R.string.app_action_delete_list_item, R.string.app_confirm_delete, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
							new DialogCallbackListener() {
								@Override
								public void doPositiveClick(Object object) {
									try {
										ActiveAndroid.beginTransaction();

										MoneyAccount transferOut = moneyTransfer.getTransferOut();
										MoneyAccount transferIn = moneyTransfer.getTransferIn();
										
										if(transferOut != null){
											HyjModelEditor<MoneyAccount> transferOutEditor = transferOut.newModelEditor();
											transferOutEditor.getModelCopy().setCurrentBalance(transferOut.getCurrentBalance() + moneyTransfer.getTransferOutAmount());
											transferOutEditor.save();
										}
										if(transferIn != null){
											HyjModelEditor<MoneyAccount> transferInEditor = transferIn.newModelEditor();
											transferInEditor.getModelCopy().setCurrentBalance(transferIn.getCurrentBalance() - moneyTransfer.getTransferInAmount());
											transferInEditor.save();
										}
										
										moneyTransfer.delete();

										HyjUtil.displayToast(R.string.app_delete_success);
										ActiveAndroid.setTransactionSuccessful();
										ActiveAndroid.endTransaction();
										getActivity().finish();
									} catch (Exception e) {
										ActiveAndroid.endTransaction();
										HyjUtil.displayToast(R.string.app_delete_failed);
									} 
								}
							});
				}
			});
		}
		
	}
	
	private void setExchangeRate(){
		if(mSelectorFieldTransferOut.getModelId() != null && mSelectorFieldTransferIn.getModelId()!= null){
			MoneyAccount transferOut = HyjModel.getModel(MoneyAccount.class,mSelectorFieldTransferOut.getModelId());
			MoneyAccount transferIn = HyjModel.getModel(MoneyAccount.class,mSelectorFieldTransferIn.getModelId());
			
			String fromCurrency = transferOut.getCurrencyId();
			String toCurrency = transferIn.getCurrencyId();
			
			if(fromCurrency.equals(toCurrency)){
				if(SET_EXCHANGE_RATE_FLAG != 1){//新增或修改打开时不做setNumber
					mNumericExchangeRate.setNumber(1.00);
					CREATE_EXCHANGE = 0;
				}
				mViewSeparatorExchange.setVisibility(View.GONE);
				mLinearLayoutExchangeRate.setVisibility(View.GONE);
				mViewSeparatorTransferInAmount.setVisibility(View.GONE);
				mNumericTransferInAmount.setVisibility(View.GONE);
			}else{
				mViewSeparatorExchange.setVisibility(View.VISIBLE);
				mLinearLayoutExchangeRate.setVisibility(View.VISIBLE);
				mViewSeparatorTransferInAmount.setVisibility(View.VISIBLE);
				mNumericTransferInAmount.setVisibility(View.VISIBLE);
				
				Double rate = Exchange.getExchangeRate(fromCurrency, toCurrency);
					if(rate != null){
						mNumericExchangeRate.setNumber(rate);
						CREATE_EXCHANGE = 0;
					}else{
						mNumericExchangeRate.setText(null);
						CREATE_EXCHANGE = 1;
					}
			}
			
		}else{
			mViewSeparatorExchange.setVisibility(View.GONE);
			mLinearLayoutExchangeRate.setVisibility(View.GONE);
			mViewSeparatorTransferInAmount.setVisibility(View.GONE);
			mNumericTransferInAmount.setVisibility(View.GONE);
		}
			SET_EXCHANGE_RATE_FLAG = 0;
			setTransferInAmount();
	}
	
	private void setTransferInAmount(){
		if(mNumericTransferOutAmount.getNumber() != null && mNumericExchangeRate.getNumber() != null){
			mNumericTransferInAmount.setNumber(mNumericTransferOutAmount.getNumber() * mNumericExchangeRate.getNumber());
		}else{
			mNumericTransferInAmount.setNumber(mNumericTransferOutAmount.getNumber());
		}
	}
	
	private void fillData(){
		MoneyTransfer modelCopy = (MoneyTransfer) mMoneyTransferEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setTransferOutAmount(mNumericTransferOutAmount.getNumber());
		
		modelCopy.setTransferOutId(mSelectorFieldTransferOut.getModelId());
		if(mSelectorFieldTransferOutFriend.getModelId() != null){
			Friend transferOutFriend = HyjModel.getModel(Friend.class, mSelectorFieldTransferOutFriend.getModelId());
			modelCopy.setTransferOutFriend(transferOutFriend);
			modelCopy.setTransferOutId(null);
		}else{
			modelCopy.setTransferOutFriend(null);
		}
		
		modelCopy.setTransferInId(mSelectorFieldTransferIn.getModelId());
		if(mSelectorFieldTransferInFriend.getModelId() != null){
			Friend transferInFriend = HyjModel.getModel(Friend.class, mSelectorFieldTransferInFriend.getModelId());
			modelCopy.setTransferInFriend(transferInFriend);
			modelCopy.setTransferInId(null);
		}else{
			modelCopy.setTransferInFriend(null);
		}
		
		modelCopy.setTransferInAmount(mNumericTransferInAmount.getNumber());
		modelCopy.setProjectId(mSelectorFieldProject.getModelId());
		modelCopy.setExchangeRate(mNumericExchangeRate.getNumber());
		
		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
		
		HyjUtil.displayToast(this.mDateTimeFieldDate.getText().toString());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		mDateTimeFieldDate.setError(mMoneyTransferEditor.getValidationError("datetime"));
		mNumericTransferOutAmount.setError(mMoneyTransferEditor.getValidationError("transferOutAmount"));
		if(mMoneyTransferEditor.getValidationError("transferOutAmount") != null){
			mNumericTransferOutAmount.showSoftKeyboard();
		}
		mSelectorFieldTransferOutFriend.setError(mMoneyTransferEditor.getValidationError("transferOutFriend"));
		mSelectorFieldTransferOut.setError(mMoneyTransferEditor.getValidationError("transferOut"));
		mSelectorFieldTransferInFriend.setError(mMoneyTransferEditor.getValidationError("transferInFriend"));
		mSelectorFieldTransferIn.setError(mMoneyTransferEditor.getValidationError("transferIn"));
		mNumericTransferInAmount.setError(mMoneyTransferEditor.getValidationError("transferInAmount"));
		mSelectorFieldProject.setError(mMoneyTransferEditor.getValidationError("project"));
		mNumericExchangeRate.setError(mMoneyTransferEditor.getValidationError("exchangeRate"));
		mRemarkFieldRemark.setError(mMoneyTransferEditor.getValidationError("remark"));
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		mMoneyTransferEditor.validate();
		
		if(mMoneyTransferEditor.hasValidationErrors()){
			showValidatioErrors();
		} else {
			try {
				ActiveAndroid.beginTransaction();
				HyjImageField.ImageGridAdapter adapter = mImageFieldPicture.getAdapter();
				int count = adapter.getCount();
				boolean mainPicSet = false;
				for(int i = 0; i < count; i++){
					PictureItem pi = adapter.getItem(i);
					if(pi.getState() == PictureItem.NEW){
						Picture newPic = pi.getPicture();
						newPic.setRecordId(mMoneyTransferEditor.getModel().getId());
						newPic.setRecordType("MoneyTransfer");
						newPic.save();
					} else if(pi.getState() == PictureItem.DELETED){
						pi.getPicture().delete();
					} else if(pi.getState() == PictureItem.CHANGED){

					}
					if(!mainPicSet && pi.getPicture() != null){
						mainPicSet = true;
						mMoneyTransferEditor.getModelCopy().setPicture(pi.getPicture());
					}
				}
				
				MoneyTransfer oldMoneyTransferModel = mMoneyTransferEditor.getModel();
				MoneyTransfer moneyTransferModel = mMoneyTransferEditor.getModelCopy();
				
				if(CREATE_EXCHANGE == 1){
					MoneyAccount transferOut = moneyTransferModel.getTransferOut();
					MoneyAccount transferIn = moneyTransferModel.getTransferIn();
					
					Exchange newExchange = new Exchange();
					newExchange.setLocalCurrencyId(transferOut.getCurrencyId());
					newExchange.setForeignCurrencyId(transferIn.getCurrencyId());
					newExchange.setRate(moneyTransferModel.getExchangeRate());
					newExchange.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
					newExchange.save();
				}
				
				MoneyAccount newTransferOut = moneyTransferModel.getTransferOut();
				MoneyAccount newTransferIn = moneyTransferModel.getTransferIn();
				
				if(moneyTransferModel.get_mId() == null){
				    if(newTransferOut != null){
				    	HyjModelEditor<MoneyAccount> newTransferOutEditor = newTransferOut.newModelEditor();
				    	newTransferOutEditor.getModelCopy().setCurrentBalance(newTransferOut.getCurrentBalance() - moneyTransferModel.getTransferOutAmount0());
				    	newTransferOutEditor.save();
				    }
				    if(newTransferIn != null){
						HyjModelEditor<MoneyAccount> newTransferInEditor = newTransferIn.newModelEditor();
				    	newTransferInEditor.getModelCopy().setCurrentBalance(newTransferIn.getCurrentBalance() + moneyTransferModel.getTransferInAmount0());
				    	newTransferInEditor.save();
				    }
				}else{
					MoneyAccount oldTransferOut = oldMoneyTransferModel.getTransferOut();
					MoneyAccount oldTransferIn = oldMoneyTransferModel.getTransferIn();
					
					if(oldTransferOut != null && newTransferOut != null && oldTransferOut.getId().equals(newTransferOut.getId())){
						HyjModelEditor<MoneyAccount> newTransferOutEditor = newTransferOut.newModelEditor();
						newTransferOutEditor.getModelCopy().setCurrentBalance(newTransferOut.getCurrentBalance() + oldMoneyTransferModel.getTransferOutAmount0() - moneyTransferModel.getTransferOutAmount0());
						newTransferOutEditor.save();
					}else{
						if(oldTransferOut != null){
							HyjModelEditor<MoneyAccount> oldTransferOutEditor = oldTransferOut.newModelEditor();
							oldTransferOutEditor.getModelCopy().setCurrentBalance(oldTransferOut.getCurrentBalance() + oldMoneyTransferModel.getTransferOutAmount0());
							oldTransferOutEditor.save();
						}
						if(newTransferOut != null){
							HyjModelEditor<MoneyAccount> newTransferOutEditor = newTransferOut.newModelEditor();
							newTransferOutEditor.getModelCopy().setCurrentBalance(newTransferOut.getCurrentBalance() - moneyTransferModel.getTransferOutAmount0());
							newTransferOutEditor.save();
						}
					}
					
					if(oldTransferIn != null && newTransferIn != null && oldTransferIn.getId().equals(newTransferIn.getId())){
						HyjModelEditor<MoneyAccount> newTransferInEditor = newTransferIn.newModelEditor();
						newTransferInEditor.getModelCopy().setCurrentBalance(newTransferIn.getCurrentBalance() - oldMoneyTransferModel.getTransferInAmount0() + moneyTransferModel.getTransferInAmount0());
						newTransferInEditor.save();
					}else{
						if(oldTransferIn != null){
							HyjModelEditor<MoneyAccount> oldTransferInEditor = oldTransferIn.newModelEditor();
							oldTransferInEditor.getModelCopy().setCurrentBalance(oldTransferIn.getCurrentBalance() - oldMoneyTransferModel.getTransferInAmount0());
							oldTransferInEditor.save();
						}
						if(newTransferIn != null){
							HyjModelEditor<MoneyAccount> newTransferInEditor = newTransferIn.newModelEditor();
							newTransferInEditor.getModelCopy().setCurrentBalance(newTransferIn.getCurrentBalance() + moneyTransferModel.getTransferInAmount0());
							newTransferInEditor.save();
						}
					}
				}
				
				mMoneyTransferEditor.save();
				ActiveAndroid.setTransactionSuccessful();
				HyjUtil.displayToast(R.string.app_save_success);
				getActivity().finish();
			} finally {
			    ActiveAndroid.endTransaction();
			}
		}
	}	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode){
             case GET_TRANSFEROUT_FRIEND_ID :
            	 if(resultCode == Activity.RESULT_OK){
             		long _id = data.getLongExtra("MODEL_ID", -1);
             		Friend friend = Friend.load(Friend.class, _id);
             		
             		if(friend.getFriendUserId() != null && friend.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
             			MoneyAccount activeAccount = HyjModel.getModel(MoneyAccount.class, HyjApplication.getInstance().getCurrentUser().getUserData().getActiveMoneyAccountId());
             			mSelectorFieldTransferOutFriend.setText("");
                 		mSelectorFieldTransferOutFriend.setModelId(null);
                 		mViewSeparatorTransferOut.setVisibility(View.VISIBLE);
                 		mSelectorFieldTransferOut.setVisibility(View.VISIBLE);
                 		mSelectorFieldTransferOut.setText(activeAccount.getName());
                 		mSelectorFieldTransferOut.setModelId(activeAccount.getId());
                 		return;
             		}
             		
             		mSelectorFieldTransferOutFriend.setText(friend.getDisplayName());
             		mSelectorFieldTransferOutFriend.setModelId(friend.getId());
             		mViewSeparatorTransferOut.setVisibility(View.GONE);
             		mSelectorFieldTransferOut.setVisibility(View.GONE);
             	 }
             	 break;
             case GET_TRANSFEROUT_ID:
	        	 if(resultCode == Activity.RESULT_OK){
	         		long _id = data.getLongExtra("MODEL_ID", -1);
	         		MoneyAccount moneyAccount = MoneyAccount.load(MoneyAccount.class, _id);
	         		mSelectorFieldTransferOut.setText(moneyAccount.getName() + "(" + moneyAccount.getCurrencyId() + ")");
	         		mSelectorFieldTransferOut.setModelId(moneyAccount.getId());
	         		setExchangeRate();
	        	 }
	        	 break;
             case GET_TRANSFERIN_FRIEND_ID :
            	 if(resultCode == Activity.RESULT_OK){
             		long _id = data.getLongExtra("MODEL_ID", -1);
             		Friend friend = Friend.load(Friend.class, _id);
             		
             		if(friend.getFriendUserId() != null && friend.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
             			MoneyAccount activeAccount = HyjModel.getModel(MoneyAccount.class, HyjApplication.getInstance().getCurrentUser().getUserData().getActiveMoneyAccountId());
             			mSelectorFieldTransferInFriend.setText("");
             			mSelectorFieldTransferInFriend.setModelId(null);
                 		mViewSeparatorTransferIn.setVisibility(View.VISIBLE);
                 		mSelectorFieldTransferIn.setVisibility(View.VISIBLE);
                 		mSelectorFieldTransferIn.setText(activeAccount.getName());
                 		mSelectorFieldTransferIn.setModelId(activeAccount.getId());
                 		return;
             		}
             		
             		mSelectorFieldTransferInFriend.setText(friend.getDisplayName());
             		mSelectorFieldTransferInFriend.setModelId(friend.getId());
             		mViewSeparatorTransferIn.setVisibility(View.GONE);
             		mSelectorFieldTransferIn.setVisibility(View.GONE);
             	 }
             	 break;
             case GET_TRANSFERIN_ID:
	        	 if(resultCode == Activity.RESULT_OK){
	         		long _id = data.getLongExtra("MODEL_ID", -1);
	         		MoneyAccount moneyAccount = MoneyAccount.load(MoneyAccount.class, _id);
	         		mSelectorFieldTransferIn.setText(moneyAccount.getName() + "(" + moneyAccount.getCurrencyId() + ")");
	         		mSelectorFieldTransferIn.setModelId(moneyAccount.getId());
	         		setExchangeRate();
	        	 }
	        	 break;
             case GET_PROJECT_ID:
	        	 if(resultCode == Activity.RESULT_OK){
	         		long _id = data.getLongExtra("MODEL_ID", -1);
	         		Project project = Project.load(Project.class, _id);
	         		mSelectorFieldProject.setText(project.getDisplayName());
	         		mSelectorFieldProject.setModelId(project.getId());
	        	 }
	        	 break;
          }
    }
}
