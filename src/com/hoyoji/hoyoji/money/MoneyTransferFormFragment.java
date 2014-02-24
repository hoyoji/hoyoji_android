package com.hoyoji.hoyoji.money;

import android.R.color;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjHttpGetExchangeRateAsyncTask;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeField;
import com.hoyoji.android.hyjframework.view.HyjImageField;
import com.hoyoji.android.hyjframework.view.HyjImageField.PictureItem;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
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
	private Double oldTransferOutAmount;
	private MoneyAccount oldTransferOut;
	private Double oldTransferInAmount;
	private MoneyAccount oldTransferIn;
	private Long modelId;
	
	private HyjModelEditor<MoneyTransfer> mMoneyTransferEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericTransferOutAmount = null;
	private HyjSelectorField mSelectorFieldTransferOutFriend = null;
	private View mViewSeparatorTransferOut = null;
	private HyjSelectorField mSelectorFieldTransferOut = null;
	private HyjSelectorField mSelectorFieldTransferInFriend = null;
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
		modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			moneyTransfer =  new Select().from(MoneyTransfer.class).where("_id=?", modelId).executeSingle();
		} else {
			moneyTransfer = new MoneyTransfer();
			
		}
		mMoneyTransferEditor = moneyTransfer.newModelEditor();
		
		mImageFieldPicture = (HyjImageField) getView().findViewById(R.id.moneyTransferFormFragment_imageField_picture);		
		mImageFieldPicture.setImages(moneyTransfer.getPictures());
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyTransferFormFragment_textField_date);		
		
		mNumericTransferOutAmount = (HyjNumericField) getView().findViewById(R.id.moneyTransferFormFragment_textField_transferOutAmount);		
		mNumericTransferOutAmount.setNumber(moneyTransfer.getTransferOutAmount());
		oldTransferOutAmount = moneyTransfer.getTransferOutAmount0();
		mNumericTransferOutAmount.addEditTextChangeListener(new TextWatcher(){
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
				if(s!= null && mNumericExchangeRate.getNumber() != null){
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
			mSelectorFieldTransferOutFriend.setText(transferOutFriend.getNickName());
		}
		mSelectorFieldTransferOutFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyTransferFormFragment.this
				.openActivityWithFragmentForResult(FriendListFragment.class, R.string.friendListFragment_title_select_friend_payee, null, GET_TRANSFEROUT_FRIEND_ID);
			}
		}); 
		
		mViewSeparatorTransferOut = (View) getView().findViewById(R.id.moneyTransferFormFragment_separatorField_transferOut);
		
		oldTransferOut = moneyTransfer.getTransferOut();
		mSelectorFieldTransferOut = (HyjSelectorField) getView().findViewById(R.id.moneyTransferFormFragment_selectorField_transferOut);
		if(oldTransferOut != null){
			mSelectorFieldTransferOut.setModelId(oldTransferOut.getId());
			mSelectorFieldTransferOut.setText(oldTransferOut.getName() + "(" + oldTransferOut.getCurrencyId() + ")");
		}
		mSelectorFieldTransferOut.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyTransferFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, null, GET_TRANSFEROUT_ID);
			}
		});	
		
		Friend transferInFriend = moneyTransfer.getTransferInFriend();
		mSelectorFieldTransferInFriend = (HyjSelectorField) getView().findViewById(R.id.moneyTransferFormFragment_selectorField_transferInFriend);
		if(transferInFriend != null){
			mSelectorFieldTransferInFriend.setModelId(transferInFriend.getId());
			mSelectorFieldTransferInFriend.setText(transferInFriend.getNickName());
		}
		mSelectorFieldTransferInFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyTransferFormFragment.this
				.openActivityWithFragmentForResult(FriendListFragment.class, R.string.friendListFragment_title_select_friend_payee, null, GET_TRANSFERIN_FRIEND_ID);
			}
		}); 
		
		mViewSeparatorTransferIn = (View) getView().findViewById(R.id.moneyTransferFormFragment_separatorField_transferIn);
		
		oldTransferIn = moneyTransfer.getTransferIn();
		mSelectorFieldTransferIn = (HyjSelectorField) getView().findViewById(R.id.moneyTransferFormFragment_selectorField_transferIn);
		if(oldTransferIn != null){
			mSelectorFieldTransferIn.setModelId(oldTransferIn.getId());
			mSelectorFieldTransferIn.setText(oldTransferIn.getName() + "(" + oldTransferIn.getCurrencyId() + ")");
		}
		mSelectorFieldTransferIn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyTransferFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, null, GET_TRANSFERIN_ID);
			}
		});	
		
		mNumericTransferInAmount = (HyjNumericField) getView().findViewById(R.id.moneyTransferFormFragment_textField_transferInAmount);		
		mNumericTransferInAmount.setNumber(moneyTransfer.getTransferInAmount());
		mNumericTransferInAmount.setEnabled(false);
		oldTransferInAmount = moneyTransfer.getTransferInAmount0();
		
		mViewSeparatorTransferInAmount = (View) getView().findViewById(R.id.moneyTransferFormFragment_separatorField_transferInamount);
		
		Project project = moneyTransfer.getProject();
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(R.id.moneyTransferFormFragment_selectorField_project);
		
		if(project != null){
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getName());
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyTransferFormFragment.this.openActivityWithFragmentForResult(ProjectListFragment.class, R.string.projectListFragment_title_select_project, null, GET_PROJECT_ID);
			}
		});	
		
		
		mNumericExchangeRate = (HyjNumericField) getView().findViewById(R.id.moneyTransferFormFragment_textField_exchangeRate);		
		mNumericExchangeRate.setNumber(moneyTransfer.getExchangeRate());
		
		mViewSeparatorExchange = (View) getView().findViewById(R.id.moneyTransferFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(R.id.moneyTransferFormFragment_linearLayout_exchangeRate);
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyTransferFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyTransfer.getRemark());
		
		ImageView takePictureButton = (ImageView) getView().findViewById(R.id.moneyTransferFormFragment_imageView_camera);	
		takePictureButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mImageFieldPicture.addPicture();		
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
						HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks(){
							@Override
							public void finishCallback(Object object) {
								HyjUtil.stopRoateView(mImageViewRefreshRate);
								mImageViewRefreshRate.setEnabled(true);
								mNumericExchangeRate.setNumber((Double)object);
							}
							@Override
							public void errorCallback(Object object) {
								HyjUtil.stopRoateView(mImageViewRefreshRate);
								mImageViewRefreshRate.setEnabled(true);
								if(object != null){
									HyjUtil.displayToast(object.toString());
								} else {
									HyjUtil.displayToast(R.string.moneyTransferFormFragment_toast_cannot_refresh_rate);
								}
							}
						};
						HyjHttpGetExchangeRateAsyncTask.newInstance(fromCurrency, toCurrency, serverCallbacks);
					} else {
						HyjUtil.displayToast(R.string.moneyTransferFormFragment_toast_select_currency);
					}
				}else{
					HyjUtil.displayToast(R.string.moneyTransferFormFragment_toast_select_currency);
				}
			}
		});
		
			setExchangeRate();
		
		this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
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
				
				Exchange exchange = Exchange.getExchange(fromCurrency, toCurrency);
					if(exchange != null){
						mNumericExchangeRate.setNumber(exchange.getRate());
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
	}
	
	private void setTransferInAmount(){
		if(mNumericTransferOutAmount.getNumber() != null && mNumericExchangeRate != null){
			mNumericTransferInAmount.setNumber(mNumericTransferOutAmount.getNumber() * mNumericExchangeRate.getNumber());
		}else{
			mNumericTransferInAmount.setNumber(null);
		}
	}
	
	private void fillData(){
		MoneyTransfer modelCopy = (MoneyTransfer) mMoneyTransferEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setTransferOutAmount(mNumericTransferOutAmount.getNumber());
		
		if(mSelectorFieldTransferOutFriend.getModelId() != null){
			Friend transferOutFriend = HyjModel.getModel(Friend.class, mSelectorFieldTransferOutFriend.getModelId());
			modelCopy.setTransferOutFriend(transferOutFriend);
		}
		
		modelCopy.setTransferOutId(mSelectorFieldTransferOut.getModelId());
		
		if(mSelectorFieldTransferInFriend.getModelId() != null){
			Friend transferInFriend = HyjModel.getModel(Friend.class, mSelectorFieldTransferInFriend.getModelId());
			modelCopy.setTransferInFriend(transferInFriend);
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
						newPic.setRecordType("Picture");
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
				
				mMoneyTransferEditor.save();
				
				if(CREATE_EXCHANGE == 1){
					MoneyAccount transferOut = HyjModel.getModel(MoneyAccount.class,mSelectorFieldTransferOut.getModelId());
					MoneyAccount transferIn = HyjModel.getModel(MoneyAccount.class,mSelectorFieldTransferIn.getModelId());
					
					Exchange newExchange = new Exchange();
					newExchange.setLocalCurrencyId(transferOut.getCurrencyId());
					newExchange.setForeignCurrencyId(transferIn.getCurrencyId());
					newExchange.setRate(mNumericExchangeRate.getNumber());
					newExchange.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
					newExchange.save();
				}
				
				MoneyAccount newTransferOut = HyjModel.getModel(MoneyAccount.class,mSelectorFieldTransferOut.getModelId());
				HyjModelEditor<MoneyAccount> newTransferOutEditor = newTransferOut.newModelEditor();
				
				MoneyAccount newTransferIn = HyjModel.getModel(MoneyAccount.class,mSelectorFieldTransferIn.getModelId());
				HyjModelEditor<MoneyAccount> newTransferInEditor = newTransferIn.newModelEditor();
				
				if(modelId == -1){
				    if(newTransferOut != null){
				    	newTransferOutEditor.getModelCopy().setCurrentBalance(newTransferOut.getCurrentBalance() - mNumericTransferOutAmount.getNumber());
				    	newTransferOutEditor.save();
				    }
				    if(newTransferIn != null){
				    	newTransferInEditor.getModelCopy().setCurrentBalance(newTransferIn.getCurrentBalance() - mNumericTransferInAmount.getNumber());
				    	newTransferInEditor.save();
				    }
				}else{
					HyjModelEditor<MoneyAccount> oldTransferOutEditor = oldTransferOut.newModelEditor();
					HyjModelEditor<MoneyAccount> oldTransferInEditor = oldTransferIn.newModelEditor();
					if(oldTransferOut != null && newTransferOut == null){
						oldTransferOutEditor.getModelCopy().setCurrentBalance(oldTransferOut.getCurrentBalance() + oldTransferOutAmount);
						oldTransferOutEditor.save();
					}else if(oldTransferOut == null && newTransferOut != null){
						newTransferOutEditor.getModelCopy().setCurrentBalance(newTransferOut.getCurrentBalance() - mNumericTransferOutAmount.getNumber());
						newTransferOutEditor.save();
					}else if(oldTransferOut != null && newTransferOut != null){
						if(oldTransferOut.getId().equals(newTransferIn.getId())){
							newTransferOutEditor.getModelCopy().setCurrentBalance(newTransferOut.getCurrentBalance() + oldTransferOutAmount - mNumericTransferOutAmount.getNumber());
							newTransferOutEditor.save();
						}else{
							oldTransferOutEditor.getModelCopy().setCurrentBalance(oldTransferOut.getCurrentBalance() + oldTransferOutAmount);
							oldTransferOutEditor.save();
							newTransferOutEditor.getModelCopy().setCurrentBalance(newTransferOut.getCurrentBalance() - mNumericTransferOutAmount.getNumber());
							newTransferOutEditor.save();
						}
					}
					
					if(oldTransferIn.getId().equals(newTransferIn.getId())){
						newTransferInEditor.getModelCopy().setCurrentBalance(newTransferIn.getCurrentBalance() + oldTransferInAmount - mNumericTransferInAmount.getNumber());
						newTransferInEditor.save();
					}else{
						if(oldTransferIn != null){
							oldTransferInEditor.getModelCopy().setCurrentBalance(oldTransferIn.getCurrentBalance() + oldTransferInAmount);
							oldTransferInEditor.save();
						}
						if(newTransferIn != null){
							newTransferInEditor.getModelCopy().setCurrentBalance(newTransferIn.getCurrentBalance() - mNumericTransferInAmount.getNumber());
							newTransferInEditor.save();
						}
					}
				}
				
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
             		mSelectorFieldTransferOutFriend.setText(friend.getNickName());
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
             		mSelectorFieldTransferInFriend.setText(friend.getNickName());
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
	         		mSelectorFieldProject.setText(project.getName());
	         		mSelectorFieldProject.setModelId(project.getId());
	        	 }
	        	 break;
          }
    }
}
