package com.hoyoji.hoyoji.money;

import android.app.Activity;
import android.content.Intent;
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
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyTransfer;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountTopupListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;


public class MoneyTopupFormFragment extends HyjUserFormFragment {
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
	private HyjSelectorField mSelectorFieldTransferOut = null;
	private HyjSelectorField mSelectorFieldTransferInFriend = null;
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
		return R.layout.money_formfragment_moneytopup;
	}
	 
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		MoneyTransfer moneyTopup;
		
		Intent intent = getActivity().getIntent();
		long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			moneyTopup =  new Select().from(MoneyTransfer.class).where("_id=?", modelId).executeSingle();
		} else {
			moneyTopup = new MoneyTransfer();
			moneyTopup.setTransferInId(null);
			moneyTopup.setTransferType("Topup");
			
			final String moneyAccountId = intent.getStringExtra("moneyAccountId");
			if(moneyAccountId != null){
				moneyTopup.setTransferOutId(moneyAccountId);
			}
			
//			String friendUserId = intent.getStringExtra("friendUserId");//从消息导入
//			if(friendUserId != null){
//				moneyTopup.setTransferInFriendUserId(friendUserId);
//				moneyTopup.setTransferOutFriendUserId(friendUserId);
//			} else {
//				String localFriendId = intent.getStringExtra("localFriendId");//从消息导入
//				if(localFriendId != null){
//					moneyTopup.setTransferInLocalFriendId(localFriendId);
//					moneyTopup.setTransferOutLocalFriendId(localFriendId);
//				}
//			}
		}
		mMoneyTransferEditor = moneyTopup.newModelEditor();
		
		setupDeleteButton(mMoneyTransferEditor);
		
		mImageFieldPicture = (HyjImageField) getView().findViewById(R.id.moneyTopupFormFragment_imageField_picture);		
		mImageFieldPicture.setImages(moneyTopup.getPictures());
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyTopupFormFragment_textField_date);	
		if(modelId != -1){
			mDateTimeFieldDate.setText(moneyTopup.getDate());
		}
		
		mNumericTransferOutAmount = (HyjNumericField) getView().findViewById(R.id.moneyTopupFormFragment_textField_transferOutAmount);		
		mNumericTransferOutAmount.setNumber(moneyTopup.getTransferOutAmount());
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
		
		MoneyAccount transferOut = moneyTopup.getTransferOut();
		mSelectorFieldTransferOut = (HyjSelectorField) getView().findViewById(R.id.moneyTopupFormFragment_selectorField_transferOut);

		if(transferOut != null){
			mSelectorFieldTransferOut.setModelId(transferOut.getId());
			mSelectorFieldTransferOut.setText(transferOut.getName() + "(" + transferOut.getCurrencyId() + ")");
		}
		mSelectorFieldTransferOut.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("excludeType", "Debt");
				
				MoneyTopupFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, bundle, GET_TRANSFEROUT_ID);
			}
		});	
		
		Friend transferInFriend = moneyTopup.getTransferInFriend();
		mSelectorFieldTransferInFriend = (HyjSelectorField) getView().findViewById(R.id.moneyTopupFormFragment_selectorField_transferInFriend);
		if(transferInFriend != null){
			mSelectorFieldTransferInFriend.setModelId(transferInFriend.getId());
			mSelectorFieldTransferInFriend.setText(transferInFriend.getDisplayName());
		}
		mSelectorFieldTransferInFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyTopupFormFragment.this
				.openActivityWithFragmentForResult(FriendListFragment.class, R.string.moneyTopupFormFragment_editText_hint_transferInFriend, null, GET_TRANSFERIN_FRIEND_ID);
			}
		}); 
		
		MoneyAccount transferIn = moneyTopup.getTransferIn();
		mSelectorFieldTransferIn = (HyjSelectorField) getView().findViewById(R.id.moneyTopupFormFragment_selectorField_transferIn);

		if(transferIn != null){
			mSelectorFieldTransferIn.setModelId(transferIn.getId());
			mSelectorFieldTransferIn.setText(transferIn.getName() + "(" + transferIn.getCurrencyId() + ")");
		}
		mSelectorFieldTransferIn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mSelectorFieldTransferInFriend.getModelId() == null){
					HyjUtil.displayToast(R.string.moneyTopupFormFragment_editText_hint_transferInFriend);
					return;
				} 
				Bundle bundle = new Bundle();
				bundle.putString("excludeType", "Debt");
				bundle.putString("friendId", mSelectorFieldTransferInFriend.getModelId());
				bundle.putString("friendDisplayName", mSelectorFieldTransferInFriend.getText());
				
				MoneyTopupFormFragment.this.openActivityWithFragmentForResult(MoneyAccountTopupListFragment.class, R.string.moneyTopupFormFragment_editText_hint_transferIn, bundle, GET_TRANSFERIN_ID);
			}
		});	
		
		mNumericTransferInAmount = (HyjNumericField) getView().findViewById(R.id.moneyTopupFormFragment_textField_transferInAmount);		
		mNumericTransferInAmount.setNumber(moneyTopup.getTransferInAmount());
		mNumericTransferInAmount.setEnabled(false);
		
		mViewSeparatorTransferInAmount = (View) getView().findViewById(R.id.moneyTopupFormFragment_separatorField_transferInamount);
		
		Project project;
		String projectId = intent.getStringExtra("projectId");//从消息导入
		if(moneyTopup.get_mId() == null && projectId != null){
			project = HyjModel.getModel(Project.class, projectId);
		}else{
			project = moneyTopup.getProject();
		}
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(R.id.moneyTopupFormFragment_selectorField_project);
		
		if(project != null){
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getDisplayName());
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyTopupFormFragment.this.openActivityWithFragmentForResult(ProjectListFragment.class, R.string.moneyTopupFormFragment_editText_hint_project, null, GET_PROJECT_ID);
			}
		});	
		
		mNumericExchangeRate = (HyjNumericField) getView().findViewById(R.id.moneyTopupFormFragment_textField_exchangeRate);		
		mNumericExchangeRate.setNumber(moneyTopup.getExchangeRate());
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
		
		mViewSeparatorExchange = (View) getView().findViewById(R.id.moneyTopupFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(R.id.moneyTopupFormFragment_linearLayout_exchangeRate);
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyTopupFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyTopup.getRemark());
		
		ImageView takePictureButton = (ImageView) getView().findViewById(R.id.moneyTopupFormFragment_imageView_camera);	
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
				
				if(mSelectorFieldTransferInFriend.getModelId() == null && (mSelectorFieldTransferOut.getModelId()== null || mSelectorFieldTransferIn.getModelId() == null)){
					for(int i = 0; i<popup.getMenu().size();i++){
						popup.getMenu().setGroupEnabled(i, false);
					}
				}
				
				popup.show();	
			}
		});
		
		mImageViewRefreshRate = (ImageView) getView().findViewById(R.id.moneyTopupFormFragment_imageButton_refresh_exchangeRate);	
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
						HyjUtil.displayToast(R.string.moneyTopupFormFragment_toast_select_currency);
					}
				}else{
					HyjUtil.displayToast(R.string.moneyTopupFormFragment_toast_select_currency);
				}
			}
		});
		
		setPermission();
		
		// 只在新增时才自动打开软键盘， 修改时不自动打开
		if (modelId == -1) {
			setExchangeRate(false);
			this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}else{
			setExchangeRate(true);
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    super.onCreateOptionsMenu(menu, inflater);
	    if(mMoneyTransferEditor!= null && mMoneyTransferEditor.getModelCopy().get_mId() != null && (mSelectorFieldTransferInFriend.getModelId() == null 
				&& (mSelectorFieldTransferOut.getModelId()== null || mSelectorFieldTransferIn.getModelId() == null))){
	    	hideSaveAction();
	    }
	}
	
	private void setPermission() {
//		if(mSelectorFieldTransferInFriend.getModelId() == null 
//				&& (mSelectorFieldTransferOut.getModelId()== null || mSelectorFieldTransferIn.getModelId() == null)){
//			
//			mDateTimeFieldDate.setEnabled(false);
//			mNumericTransferOutAmount.setEnabled(false);
//			mSelectorFieldTransferOut.setEnabled(false);
//			mNumericTransferInAmount.setEnabled(false);
//			mSelectorFieldTransferInFriend.setEnabled(false);
//			mSelectorFieldTransferIn.setEnabled(false);
//			mNumericExchangeRate.setEnabled(false);
//			mSelectorFieldProject.setEnabled(false);
//			mRemarkFieldRemark.setEnabled(false);
//			
//			if(this.mOptionsMenu != null){
//		    	hideSaveAction();
//			}
//			
////			if(mSelectorFieldTransferIn.getModelId() == null){
////				mSelectorFieldTransferInFriend.setText("请选择商家好友");
////			}
//		} 
//		else if(mSelectorFieldTransferInFriend.getModelId() != null){
//     		mViewSeparatorTransferIn.setVisibility(View.GONE);
//     		mSelectorFieldTransferIn.setVisibility(View.GONE);
//		}
	}

	private void setupDeleteButton(HyjModelEditor<MoneyTransfer> moneyTopupEditor) {

		Button buttonDelete = (Button) getView().findViewById(R.id.button_delete);
		
		final MoneyTransfer moneyTopup = moneyTopupEditor.getModelCopy();
		
		if (moneyTopup.get_mId() == null) {
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

										MoneyAccount transferOut = moneyTopup.getTransferOut();
										MoneyAccount transferIn = moneyTopup.getTransferIn();
										
										if(transferOut != null){
											HyjModelEditor<MoneyAccount> transferOutEditor = transferOut.newModelEditor();
											transferOutEditor.getModelCopy().setCurrentBalance(transferOut.getCurrentBalance() + moneyTopup.getTransferOutAmount());
											transferOutEditor.save();
										}
										if(transferIn != null){
											HyjModelEditor<MoneyAccount> transferInEditor = transferIn.newModelEditor();
											transferInEditor.getModelCopy().setCurrentBalance(transferIn.getCurrentBalance() - moneyTopup.getTransferInAmount());
											transferInEditor.save();
										}
										
										moneyTopup.delete();

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
	
	private void setExchangeRate(Boolean editInit){
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
				
				if(!editInit){//修改时init不需要set Rate
					Double rate = Exchange.getExchangeRate(fromCurrency, toCurrency);
					if(rate != null){
						mNumericExchangeRate.setNumber(rate);
						CREATE_EXCHANGE = 0;
					}else{
						mNumericExchangeRate.setText(null);
						CREATE_EXCHANGE = 1;
					}
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
		modelCopy.setTransferOutFriend(null);
		
		modelCopy.setTransferInId(mSelectorFieldTransferIn.getModelId());
		Friend transferInFriend = HyjModel.getModel(Friend.class, mSelectorFieldTransferInFriend.getModelId());
		modelCopy.setTransferInFriend(transferInFriend);
		
		modelCopy.setTransferInAmount(mNumericTransferInAmount.getNumber());
		modelCopy.setProjectId(mSelectorFieldProject.getModelId());
		modelCopy.setExchangeRate(mNumericExchangeRate.getNumber());
		
		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		mDateTimeFieldDate.setError(mMoneyTransferEditor.getValidationError("datetime"));
		mNumericTransferOutAmount.setError(mMoneyTransferEditor.getValidationError("transferOutAmount"));
		if(mMoneyTransferEditor.getValidationError("transferOutAmount") != null){
			mNumericTransferOutAmount.showSoftKeyboard();
		}
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
		
//		mMoneyTransferEditor.validate();
		if(mMoneyTransferEditor.getModelCopy().getDate() == null){
			mMoneyTransferEditor.setValidationError("date",R.string.moneyTopupFormFragment_editText_hint_date);
		}else{
			mMoneyTransferEditor.removeValidationError("date");
		}
		
		if(mMoneyTransferEditor.getModelCopy().getTransferOutAmount() == null){
			mMoneyTransferEditor.setValidationError("transferOutAmount",R.string.moneyTopupFormFragment_editText_hint_transferOutAmount);
		}else if(mMoneyTransferEditor.getModelCopy().getTransferOutAmount() < 0){
			mMoneyTransferEditor.setValidationError("transferOutAmount",R.string.moneyTopupFormFragment_editText_validationError_negative_transferOutAmount);
		}else if(mMoneyTransferEditor.getModelCopy().getTransferOutAmount() > 99999999){
			mMoneyTransferEditor.setValidationError("transferOutAmount",R.string.moneyTopupFormFragment_editText_validationError_beyondMAX_transferOutAmount);
		}else{
			mMoneyTransferEditor.removeValidationError("transferOutAmount");
		}


		if(mMoneyTransferEditor.getModelCopy().getTransferOutId() == null){
			mMoneyTransferEditor.setValidationError("transferOut",R.string.moneyTopupFormFragment_editText_hint_transferOut);
		}else{
			mMoneyTransferEditor.removeValidationError("transferOut");
		}
		
		if(mMoneyTransferEditor.getModelCopy().getTransferInFriendUserId() == null
			&& mMoneyTransferEditor.getModelCopy().getTransferInLocalFriendId() == null){
			mMoneyTransferEditor.setValidationError("transferInFriend", R.string.moneyTopupFormFragment_editText_hint_transferInFriend);
		} else {
			mMoneyTransferEditor.removeValidationError("transferInFriend");
		}

		if(mMoneyTransferEditor.getModelCopy().getTransferInId() == null){
			mMoneyTransferEditor.setValidationError("transferIn",R.string.moneyTopupFormFragment_editText_hint_transferIn);
		}else{
			mMoneyTransferEditor.removeValidationError("transferIn");
		}
		
		if(mMoneyTransferEditor.getModelCopy().getProjectId() == null){
			mMoneyTransferEditor.setValidationError("project",R.string.moneyTopupFormFragment_editText_hint_project);
		}else{
			mMoneyTransferEditor.removeValidationError("project");
		}
				
		if(mMoneyTransferEditor.getModelCopy().getExchangeRate() == null){
			mMoneyTransferEditor.setValidationError("exchangeRate",R.string.moneyTopupFormFragment_editText_hint_exchangeRate);
		}else if(mMoneyTransferEditor.getModelCopy().getExchangeRate() == 0){
			mMoneyTransferEditor.setValidationError("exchangeRate",R.string.moneyTopupFormFragment_editText_validationError_zero_exchangeRate);
		}else if(mMoneyTransferEditor.getModelCopy().getExchangeRate() < 0){
			mMoneyTransferEditor.setValidationError("exchangeRate",R.string.moneyTopupFormFragment_editText_validationError_negative_exchangeRate);
		}else if(mMoneyTransferEditor.getModelCopy().getExchangeRate() > 99999999){
			mMoneyTransferEditor.setValidationError("exchangeRate",R.string.moneyTopupFormFragment_editText_validationError_beyondMAX_exchangeRate);
		}
		else{
			mMoneyTransferEditor.removeValidationError("exchangeRate");
		}
		
		
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
				MoneyTransfer moneyTopupModel = mMoneyTransferEditor.getModelCopy();

				if(CREATE_EXCHANGE == 1){
					MoneyAccount transferOut = moneyTopupModel.getTransferOut();
					MoneyAccount transferIn = moneyTopupModel.getTransferIn();
					
					Exchange newExchange = new Exchange();
					newExchange.setLocalCurrencyId(transferOut.getCurrencyId());
					newExchange.setForeignCurrencyId(transferIn.getCurrencyId());
					newExchange.setRate(moneyTopupModel.getExchangeRate());
					newExchange.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
					newExchange.save();
				}else if(moneyTopupModel.getTransferOut() != null && moneyTopupModel.getTransferIn() != null){
					String localCurrencyId = moneyTopupModel.getTransferOut().getCurrencyId();
					String foreignCurrencyId = moneyTopupModel.getTransferIn().getCurrencyId();
					if(!localCurrencyId.equalsIgnoreCase(foreignCurrencyId)){
						Exchange exchange = Exchange.getExchange(localCurrencyId, foreignCurrencyId);
						Double rate = HyjUtil.toFixed2(moneyTopupModel.getExchangeRate());
						if(exchange != null){
							if(exchange.getRate() != rate){
								HyjModelEditor<Exchange> exchangModelEditor = exchange.newModelEditor();
								exchangModelEditor.getModelCopy().setRate(rate);
								exchangModelEditor.save();
							}
						}
					}
				}
				
				MoneyAccount newTransferOut = moneyTopupModel.getTransferOut();
				MoneyAccount newTransferIn = moneyTopupModel.getTransferIn();
				
				if(moneyTopupModel.get_mId() == null){
				    if(newTransferOut != null){
				    	HyjModelEditor<MoneyAccount> newTransferOutEditor = newTransferOut.newModelEditor();
				    	newTransferOutEditor.getModelCopy().setCurrentBalance(newTransferOut.getCurrentBalance() - moneyTopupModel.getTransferOutAmount0());
				    	newTransferOutEditor.save();
				    }
				    if(newTransferIn != null){
						HyjModelEditor<MoneyAccount> newTransferInEditor = newTransferIn.newModelEditor();
				    	newTransferInEditor.getModelCopy().setCurrentBalance(newTransferIn.getCurrentBalance() + moneyTopupModel.getTransferInAmount0());
				    	newTransferInEditor.save();
				    }
				}else{
					MoneyAccount oldTransferOut = oldMoneyTransferModel.getTransferOut();
					MoneyAccount oldTransferIn = oldMoneyTransferModel.getTransferIn();
					
					if(oldTransferOut != null && newTransferOut != null && oldTransferOut.getId().equals(newTransferOut.getId())){
						HyjModelEditor<MoneyAccount> newTransferOutEditor = newTransferOut.newModelEditor();
						newTransferOutEditor.getModelCopy().setCurrentBalance(newTransferOut.getCurrentBalance() + oldMoneyTransferModel.getTransferOutAmount0() - moneyTopupModel.getTransferOutAmount0());
						newTransferOutEditor.save();
					}else{
						if(oldTransferOut != null){
							HyjModelEditor<MoneyAccount> oldTransferOutEditor = oldTransferOut.newModelEditor();
							oldTransferOutEditor.getModelCopy().setCurrentBalance(oldTransferOut.getCurrentBalance() + oldMoneyTransferModel.getTransferOutAmount0());
							oldTransferOutEditor.save();
						}
						if(newTransferOut != null){
							HyjModelEditor<MoneyAccount> newTransferOutEditor = newTransferOut.newModelEditor();
							newTransferOutEditor.getModelCopy().setCurrentBalance(newTransferOut.getCurrentBalance() - moneyTopupModel.getTransferOutAmount0());
							newTransferOutEditor.save();
						}
					}
					
					if(oldTransferIn != null && newTransferIn != null && oldTransferIn.getId().equals(newTransferIn.getId())){
						HyjModelEditor<MoneyAccount> newTransferInEditor = newTransferIn.newModelEditor();
						newTransferInEditor.getModelCopy().setCurrentBalance(newTransferIn.getCurrentBalance() - oldMoneyTransferModel.getTransferInAmount0() + moneyTopupModel.getTransferInAmount0());
						newTransferInEditor.save();
					}else{
						if(oldTransferIn != null){
							HyjModelEditor<MoneyAccount> oldTransferInEditor = oldTransferIn.newModelEditor();
							oldTransferInEditor.getModelCopy().setCurrentBalance(oldTransferIn.getCurrentBalance() - oldMoneyTransferModel.getTransferInAmount0());
							oldTransferInEditor.save();
						}
						if(newTransferIn != null){
							HyjModelEditor<MoneyAccount> newTransferInEditor = newTransferIn.newModelEditor();
							newTransferInEditor.getModelCopy().setCurrentBalance(newTransferIn.getCurrentBalance() + moneyTopupModel.getTransferInAmount0());
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
             case GET_TRANSFEROUT_ID:
	        	 if(resultCode == Activity.RESULT_OK){
	         		long _id = data.getLongExtra("MODEL_ID", -1);
	         		MoneyAccount moneyAccount = MoneyAccount.load(MoneyAccount.class, _id);
	         		mSelectorFieldTransferOut.setText(moneyAccount.getName() + "(" + moneyAccount.getCurrencyId() + ")");
	         		mSelectorFieldTransferOut.setModelId(moneyAccount.getId());
	         		setExchangeRate(false);
	        	 }
	        	 break;
             case GET_TRANSFERIN_FRIEND_ID :
            	 if(resultCode == Activity.RESULT_OK){
             		long _id = data.getLongExtra("MODEL_ID", -1);
             		Friend friend = Friend.load(Friend.class, _id);
         			mSelectorFieldTransferInFriend.setText(friend.getDisplayName());
             		mSelectorFieldTransferInFriend.setModelId(friend.getId());
        
             		MoneyAccount moneyAccount = new Select().from(MoneyAccount.class).where("accountType = ? AND friendId = ?", "Topup", friend.getId()).executeSingle();
             		if(moneyAccount != null){
                 		mSelectorFieldTransferIn.setText(moneyAccount.getDisplayName());
                 		mSelectorFieldTransferIn.setModelId(moneyAccount.getId());
             		} else {
             			mSelectorFieldTransferIn.setText(friend.getDisplayName()+"储值卡1");
                 		mSelectorFieldTransferIn.setModelId(null);
             		}
             	 }
             	 break;
             case GET_TRANSFERIN_ID:
	        	 if(resultCode == Activity.RESULT_OK){
	         		long _id = data.getLongExtra("MODEL_ID", -1);
	         		MoneyAccount moneyAccount = MoneyAccount.load(MoneyAccount.class, _id);
	         		mSelectorFieldTransferIn.setText(moneyAccount.getName() + "(" + moneyAccount.getCurrencyId() + ")");
	         		mSelectorFieldTransferIn.setModelId(moneyAccount.getId());
	         		setExchangeRate(false);
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
