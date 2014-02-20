package com.hoyoji.hoyoji.money;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
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
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.android.hyjframework.view.HyjImageField.PictureItem;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyIncome;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;


public class MoneyIncomeFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_FRIEND_ID = 3;
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;
	private Double oldAmount;
	private MoneyAccount oldMoneyAccount;
	
	private HyjModelEditor<MoneyIncome> mMoneyIncomeEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericAmount = null;
	private HyjSelectorField mSelectorFieldMoneyAccount = null;
	private HyjSelectorField mSelectorFieldProject = null;
	private HyjNumericField mNumericExchangeRate = null;
	private HyjTextField mTextFieldMoneyIncomeCategory = null;
	private HyjSelectorField mSelectorFieldFriend = null;
	private HyjRemarkField mRemarkFieldRemark = null;
	private ImageView mImageViewRefreshRate = null;
	private View mViewSeparatorExchange = null;
	private LinearLayout mLinearLayoutExchangeRate = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.money_formfragment_moneyincome;
	}
	 
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		MoneyIncome moneyIncome;
		
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			moneyIncome =  new Select().from(MoneyIncome.class).where("_id=?", modelId).executeSingle();
		} else {
			moneyIncome = new MoneyIncome();
		}
		mMoneyIncomeEditor = moneyIncome.newModelEditor();
		
		mImageFieldPicture = (HyjImageField) getView().findViewById(R.id.moneyIncomeFormFragment_imageField_picture);
		mImageFieldPicture.setImages(moneyIncome.getPictures());
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_date);		
		
		mNumericAmount = (HyjNumericField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_amount);		
		mNumericAmount.setNumber(moneyIncome.getAmount());
		oldAmount = moneyIncome.getAmount0();
		
		oldMoneyAccount = moneyIncome.getMoneyAccount();
		mSelectorFieldMoneyAccount = (HyjSelectorField) getView().findViewById(R.id.moneyIncomeFormFragment_selectorField_moneyAccount);
		
		if(oldMoneyAccount != null){
			mSelectorFieldMoneyAccount.setModelId(oldMoneyAccount.getId());
			mSelectorFieldMoneyAccount.setText(oldMoneyAccount.getName() + "(" + oldMoneyAccount.getCurrencyId() + ")");
		}
		mSelectorFieldMoneyAccount.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyIncomeFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, null, GET_MONEYACCOUNT_ID);
			}
		});	
		
		Project project = moneyIncome.getProject();
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(R.id.moneyIncomeFormFragment_selectorField_project);
		
		if(project != null){
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getName() + "(" + project.getCurrencyId() + ")");
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyIncomeFormFragment.this.openActivityWithFragmentForResult(ProjectListFragment.class, R.string.projectListFragment_title_select_project, null, GET_PROJECT_ID);
			}
		});	
		
		mNumericExchangeRate = (HyjNumericField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_exchangeRate);		
		mNumericExchangeRate.setNumber(moneyIncome.getExchangeRate());
		//mNumericExchangeRate.setVisibility(View.GONE);
		
		mViewSeparatorExchange = (View) getView().findViewById(R.id.moneyIncomeFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(R.id.moneyIncomeFormFragment_linearLayout_exchangeRate);
		
		mTextFieldMoneyIncomeCategory = (HyjTextField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_moneyIncomeCategory);
		mTextFieldMoneyIncomeCategory.setText(moneyIncome.getMoneyIncomeCategory());
		
		Friend friend = moneyIncome.getFriend();
		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.moneyIncomeFormFragment_selectorField_friend);
		
		if(friend != null){
			mSelectorFieldFriend.setModelId(friend.getId());
			mSelectorFieldFriend.setText(friend.getNickName());
		}
		mSelectorFieldFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyIncomeFormFragment.this
				.openActivityWithFragmentForResult(FriendListFragment.class, R.string.friendListFragment_title_select_friend_payee, null, GET_FRIEND_ID);
			}
		}); 
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyIncome.getRemark());
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyIncome.getRemark());
		
		ImageView takePictureButton = (ImageView) getView().findViewById(R.id.moneyIncomeFormFragment_imageView_camera);	
		takePictureButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mImageFieldPicture.addPicture();		
			}
		});
		
		mImageViewRefreshRate = (ImageView) getView().findViewById(R.id.moneyIncomeFormFragment_imageButton_refresh_exchangeRate);	
		mImageViewRefreshRate.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mSelectorFieldMoneyAccount.getModelId() != null && mSelectorFieldProject.getModelId() != null){
					MoneyAccount moneyAccount = (MoneyAccount)HyjModel.getModel(MoneyAccount.class, mSelectorFieldMoneyAccount.getModelId());
					Project project = HyjModel.getModel(Project.class, mSelectorFieldProject.getModelId());
					
					String fromCurrency = moneyAccount.getCurrencyId();
					String toCurrency = project.getCurrencyId();
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
									HyjUtil.displayToast(R.string.moneyIncomeFormFragment_toast_cannot_refresh_rate);
								}
							}
						};
						HyjHttpGetExchangeRateAsyncTask.newInstance(fromCurrency, toCurrency, serverCallbacks);
					} else {
						HyjUtil.displayToast(R.string.moneyIncomeFormFragment_toast_select_currency);
					}
				}else{
					HyjUtil.displayToast(R.string.moneyIncomeFormFragment_toast_select_currency);
				}
			}
		});
		
			setExchangeRate();
		
		this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}
	
	private void setExchangeRate(){
		if(mSelectorFieldMoneyAccount.getModelId() != null && mSelectorFieldProject.getModelId()!= null){
			MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class,mSelectorFieldMoneyAccount.getModelId());
			Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
			
			String fromCurrency = moneyAccount.getCurrencyId();
			String toCurrency = project.getCurrencyId();
			
			if(fromCurrency.equals(toCurrency)){
				if(SET_EXCHANGE_RATE_FLAG != 1){//新增或修改打开时不做setNumber
					mNumericExchangeRate.setNumber(1.00);
					CREATE_EXCHANGE = 0;
				}
				mLinearLayoutExchangeRate.setVisibility(View.GONE);
				mViewSeparatorExchange.setVisibility(View.GONE);
			}else{
				mLinearLayoutExchangeRate.setVisibility(View.VISIBLE);
				mViewSeparatorExchange.setVisibility(View.VISIBLE);
				
				Exchange exchange = new Select().from(Exchange.class).where("localCurrencyId=? AND foreignCurrencyId=?",new Object [] {fromCurrency, toCurrency}).executeSingle();
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
		}
			SET_EXCHANGE_RATE_FLAG = 0;
	}
	
	private void fillData(){
		MoneyIncome modelCopy =  mMoneyIncomeEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setAmount(mNumericAmount.getNumber());
		modelCopy.setMoneyAccountId(mSelectorFieldMoneyAccount.getModelId());
		modelCopy.setProjectId(mSelectorFieldProject.getModelId());
		modelCopy.setExchangeRate(mNumericExchangeRate.getNumber());
		modelCopy.setMoneyIncomeCategory(mTextFieldMoneyIncomeCategory.getText().toString().trim());
		
		if(mSelectorFieldFriend.getModelId() != null){
			Friend friend = HyjModel.getModel(Friend.class, mSelectorFieldFriend.getModelId());
			if(friend.getFriendUserId() != null){
				modelCopy.setFriendUserId(mSelectorFieldFriend.getModelId());
			}
			else{
				modelCopy.setLocalFriendId(mSelectorFieldFriend.getModelId());
			}
		}
		
		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
		
		HyjUtil.displayToast(this.mDateTimeFieldDate.getText().toString());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		mDateTimeFieldDate.setError(mMoneyIncomeEditor.getValidationError("datetime"));
		mNumericAmount.setError(mMoneyIncomeEditor.getValidationError("amount"));
		mSelectorFieldMoneyAccount.setError(mMoneyIncomeEditor.getValidationError("moneyAccount"));
		mSelectorFieldProject.setError(mMoneyIncomeEditor.getValidationError("project"));
		mNumericExchangeRate.setError(mMoneyIncomeEditor.getValidationError("exchangeRate"));
		mTextFieldMoneyIncomeCategory.setError(mMoneyIncomeEditor.getValidationError("moneyIncomeCategory"));
		mSelectorFieldFriend.setError(mMoneyIncomeEditor.getValidationError("friend"));
		mRemarkFieldRemark.setError(mMoneyIncomeEditor.getValidationError("remark"));
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		mMoneyIncomeEditor.validate();
		
		if(mMoneyIncomeEditor.hasValidationErrors()){
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
						newPic.setRecordId(mMoneyIncomeEditor.getModel().getId());
						newPic.setRecordType("Picture");
						newPic.save();
					} else if(pi.getState() == PictureItem.DELETED){
						pi.getPicture().delete();
					} else if(pi.getState() == PictureItem.CHANGED){

					}
					if(!mainPicSet && pi.getPicture() != null){
						mainPicSet = true;
						((MoneyIncome)mMoneyIncomeEditor.getModelCopy()).setPicture(pi.getPicture());
					}
				}
				
				mMoneyIncomeEditor.save();
				
				if(CREATE_EXCHANGE == 1){
					MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class,mSelectorFieldMoneyAccount.getModelId());
					Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
					
					Exchange newExchange = new Exchange();
					newExchange.setLocalCurrencyId(moneyAccount.getCurrencyId());
					newExchange.setForeignCurrencyId(project.getCurrencyId());
					newExchange.setRate(mNumericExchangeRate.getNumber());
					newExchange.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
					newExchange.save();
				}
				
				if(mSelectorFieldMoneyAccount.getModelId() != null){
					MoneyAccount newMoneyAccount = HyjModel.getModel(MoneyAccount.class,mSelectorFieldMoneyAccount.getModelId());
					HyjModelEditor<MoneyAccount> newMoneyAccountEditor = newMoneyAccount.newModelEditor();
					
					if(oldMoneyAccount == null || oldMoneyAccount.getId().equals(newMoneyAccount.getId())){
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() - oldAmount + mNumericAmount.getNumber());
							
					}else{
						HyjModelEditor<MoneyAccount> oldMoneyAccountEditor = oldMoneyAccount.newModelEditor();
						oldMoneyAccountEditor.getModelCopy().setCurrentBalance(oldMoneyAccount.getCurrentBalance() - oldAmount);
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() + mNumericAmount.getNumber());
						oldMoneyAccountEditor.save();
					}
					newMoneyAccountEditor.save();
				}	
				
				HyjUtil.displayToast(R.string.app_save_success);
				ActiveAndroid.setTransactionSuccessful();
				getActivity().finish();
			} finally {
			    ActiveAndroid.endTransaction();
			}
		}
	}	
	
	 @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode){
             case GET_MONEYACCOUNT_ID:
	        	 if(resultCode == Activity.RESULT_OK){
	         		long _id = data.getLongExtra("MODEL_ID", -1);
	         		MoneyAccount moneyAccount = MoneyAccount.load(MoneyAccount.class, _id);
	         		mSelectorFieldMoneyAccount.setText(moneyAccount.getName() + "(" + moneyAccount.getCurrencyId() + ")");
	         		mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
	         		setExchangeRate();
	         	 }
	        	 break;
             case GET_PROJECT_ID:
	        	 if(resultCode == Activity.RESULT_OK){
	         		long _id = data.getLongExtra("MODEL_ID", -1);
	         		Project project = Project.load(Project.class, _id);
	         		mSelectorFieldProject.setText(project.getName() + "(" + project.getCurrencyId() + ")");
	         		mSelectorFieldProject.setModelId(project.getId());
	         		setExchangeRate();
	         	 }
	        	 break;
             case GET_FRIEND_ID:
            	 if(resultCode == Activity.RESULT_OK){
            		long _id = data.getLongExtra("MODEL_ID", -1);
            		Friend friend = Friend.load(Friend.class, _id);
            		mSelectorFieldFriend.setText(friend.getNickName());
            		mSelectorFieldFriend.setModelId(friend.getId());
            	 }
            	 break;

          }
    }
}
