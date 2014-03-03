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
import com.hoyoji.android.hyjframework.view.HyjImageField.PictureItem;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyLend;
import com.hoyoji.hoyoji.models.MoneyPayback;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;


public class MoneyPaybackFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_FRIEND_ID = 3;
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;
	Long modelId;
	
	private HyjModelEditor<MoneyPayback> mMoneyPaybackEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericFieldAmount = null;
	private HyjNumericField mNumericFieldInterest = null;
	private HyjSelectorField mSelectorFieldMoneyAccount = null;
	private HyjSelectorField mSelectorFieldProject = null;
	private HyjNumericField mNumericExchangeRate = null;
	private HyjSelectorField mSelectorFieldFriend = null;
	private HyjRemarkField mRemarkFieldRemark = null;
	private ImageView mImageViewRefreshRate = null;
	private View mViewSeparatorExchange = null;
	private LinearLayout mLinearLayoutExchangeRate = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.money_formfragment_moneypayback;
	}
	 
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		MoneyPayback moneyPayback;
		
		Intent intent = getActivity().getIntent();
	    modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			moneyPayback =  new Select().from(MoneyPayback.class).where("_id=?", modelId).executeSingle();
		} else {
			moneyPayback = new MoneyPayback();
			
		}
		mMoneyPaybackEditor = moneyPayback.newModelEditor();
		
		mImageFieldPicture = (HyjImageField) getView().findViewById(R.id.moneyPaybackFormFragment_imageField_picture);		
		mImageFieldPicture.setImages(moneyPayback.getPictures());
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyPaybackFormFragment_textField_date);		
		if(modelId != -1){
			mDateTimeFieldDate.setText(moneyPayback.getDate());
		}
		
		mNumericFieldAmount = (HyjNumericField) getView().findViewById(R.id.moneyPaybackFormFragment_textField_amount);		
		mNumericFieldAmount.setNumber(moneyPayback.getAmount());
		
		mNumericFieldInterest = (HyjNumericField) getView().findViewById(R.id.moneyPaybackFormFragment_textField_interest);		
		mNumericFieldInterest.setNumber(moneyPayback.getInterest());
		
		MoneyAccount moneyAccount = moneyPayback.getMoneyAccount();
		mSelectorFieldMoneyAccount = (HyjSelectorField) getView().findViewById(R.id.moneyPaybackFormFragment_selectorField_moneyAccount);
		
		if(moneyAccount != null){
			mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
			mSelectorFieldMoneyAccount.setText(moneyAccount.getName() + "(" + moneyAccount.getCurrencyId() + ")");
		}
		mSelectorFieldMoneyAccount.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyPaybackFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, null, GET_MONEYACCOUNT_ID);
			}
		});	
		
		Project project = moneyPayback.getProject();
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(R.id.moneyPaybackFormFragment_selectorField_project);
		
		if(project != null){
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getName() + "(" + project.getCurrencyId() + ")");
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyPaybackFormFragment.this.openActivityWithFragmentForResult(ProjectListFragment.class, R.string.projectListFragment_title_select_project, null, GET_PROJECT_ID);
			}
		});	
		
		
		mNumericExchangeRate = (HyjNumericField) getView().findViewById(R.id.moneyPaybackFormFragment_textField_exchangeRate);		
		mNumericExchangeRate.setNumber(moneyPayback.getExchangeRate());
		
		mViewSeparatorExchange = (View) getView().findViewById(R.id.moneyPaybackFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(R.id.moneyPaybackFormFragment_linearLayout_exchangeRate);
		
		Friend friend = moneyPayback.getFriend();
		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.moneyPaybackFormFragment_selectorField_friend);
		
		if(friend != null){
			mSelectorFieldFriend.setModelId(friend.getId());
			mSelectorFieldFriend.setText(friend.getNickName());
		}
		mSelectorFieldFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyPaybackFormFragment.this
				.openActivityWithFragmentForResult(FriendListFragment.class, R.string.friendListFragment_title_select_friend_debtor, null, GET_FRIEND_ID);
			}
		}); 
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyPaybackFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyPayback.getRemark());
		
		ImageView takePictureButton = (ImageView) getView().findViewById(R.id.moneyPaybackFormFragment_imageView_camera);	
		takePictureButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mImageFieldPicture.takePictureFromCamera();		
			}
		});
		
		mImageViewRefreshRate = (ImageView) getView().findViewById(R.id.moneyPaybackFormFragment_imageButton_refresh_exchangeRate);	
		mImageViewRefreshRate.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mSelectorFieldMoneyAccount.getModelId() != null && mSelectorFieldProject.getModelId() != null){
					MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mSelectorFieldMoneyAccount.getModelId());
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
									HyjUtil.displayToast(R.string.moneyPaybackFormFragment_toast_cannot_refresh_rate);
								}
							}
						};
						HyjHttpGetExchangeRateAsyncTask.newInstance(fromCurrency, toCurrency, serverCallbacks);
					} else {
						HyjUtil.displayToast(R.string.moneyPaybackFormFragment_toast_select_currency);
					}
				}else{
					HyjUtil.displayToast(R.string.moneyPaybackFormFragment_toast_select_currency);
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
				mViewSeparatorExchange.setVisibility(View.GONE);
				mLinearLayoutExchangeRate.setVisibility(View.GONE);
			}else{
				mViewSeparatorExchange.setVisibility(View.VISIBLE);
				mLinearLayoutExchangeRate.setVisibility(View.VISIBLE);
				
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
		}
			SET_EXCHANGE_RATE_FLAG = 0;
	}
	
	private void fillData(){
		MoneyPayback modelCopy = (MoneyPayback) mMoneyPaybackEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setAmount(mNumericFieldAmount.getNumber());
		modelCopy.setInterest(mNumericFieldInterest.getNumber());
		modelCopy.setMoneyAccountId(mSelectorFieldMoneyAccount.getModelId());
		modelCopy.setProjectId(mSelectorFieldProject.getModelId());
		modelCopy.setExchangeRate(mNumericExchangeRate.getNumber());
		
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
		mDateTimeFieldDate.setError(mMoneyPaybackEditor.getValidationError("datetime"));
		mNumericFieldAmount.setError(mMoneyPaybackEditor.getValidationError("amount"));
		mNumericFieldInterest.setError(mMoneyPaybackEditor.getValidationError("interest"));
		mSelectorFieldMoneyAccount.setError(mMoneyPaybackEditor.getValidationError("moneyAccount"));
		mSelectorFieldProject.setError(mMoneyPaybackEditor.getValidationError("project"));
		mNumericExchangeRate.setError(mMoneyPaybackEditor.getValidationError("exchangeRate"));
		mSelectorFieldFriend.setError(mMoneyPaybackEditor.getValidationError("friend"));
		mRemarkFieldRemark.setError(mMoneyPaybackEditor.getValidationError("remark"));
	}
	
	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		mMoneyPaybackEditor.validate();
		
		if(mMoneyPaybackEditor.hasValidationErrors()){
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
						newPic.setRecordId(mMoneyPaybackEditor.getModel().getId());
						newPic.setRecordType("Picture");
						newPic.save();
					} else if(pi.getState() == PictureItem.DELETED){
						pi.getPicture().delete();
					} else if(pi.getState() == PictureItem.CHANGED){

					}
					if(!mainPicSet && pi.getPicture() != null){
						mainPicSet = true;
						mMoneyPaybackEditor.getModelCopy().setPicture(pi.getPicture());
					}
				}
				
				MoneyPayback oldMoneyPaybackModel = mMoneyPaybackEditor.getModel();
				MoneyPayback moneyPaybackModel = mMoneyPaybackEditor.getModelCopy();
				
				UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
				if(modelId == -1 && !userData.getActiveMoneyAccountId().equals(moneyPaybackModel.getMoneyAccountId()) || !userData.getActiveProjectId().equals(moneyPaybackModel.getProjectId())){
					HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
					userDataEditor.getModelCopy().setActiveMoneyAccountId(moneyPaybackModel.getMoneyAccountId());
					userDataEditor.getModelCopy().setActiveProjectId(moneyPaybackModel.getProjectId());
					userDataEditor.save();
				}
				
				if(CREATE_EXCHANGE == 1){
					Exchange newExchange = new Exchange();
					newExchange.setLocalCurrencyId(moneyPaybackModel.getMoneyAccount().getCurrencyId());
					newExchange.setForeignCurrencyId(moneyPaybackModel.getProject().getCurrencyId());
					newExchange.setRate(moneyPaybackModel.getExchangeRate());
//					newExchange.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
					newExchange.save();
				}
				
//				if(mSelectorFieldMoneyAccount.getModelId() != null){
				    Double oldAmount = oldMoneyPaybackModel.getAmount0();
				    Double oldInterest = oldMoneyPaybackModel.getInterest0();
				    MoneyAccount oldMoneyAccount = oldMoneyPaybackModel.getMoneyAccount();
					MoneyAccount newMoneyAccount = moneyPaybackModel.getMoneyAccount();
					HyjModelEditor<MoneyAccount> newMoneyAccountEditor = newMoneyAccount.newModelEditor();
					
					if(modelId == -1 || oldMoneyAccount.getId().equals(newMoneyAccount.getId())){
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() - oldAmount - oldInterest + moneyPaybackModel.getAmount0() + moneyPaybackModel.getInterest0());
							
					}else{
						HyjModelEditor<MoneyAccount> oldMoneyAccountEditor = oldMoneyAccount.newModelEditor();
						oldMoneyAccountEditor.getModelCopy().setCurrentBalance(oldMoneyAccount.getCurrentBalance() - oldAmount - oldInterest);
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() + moneyPaybackModel.getAmount0() + moneyPaybackModel.getInterest0());
						oldMoneyAccountEditor.save();
					}
					newMoneyAccountEditor.save();
//				}	
					MoneyAccount newDebtAccount = MoneyAccount.getDebtAccount(moneyPaybackModel.getMoneyAccount().getCurrencyId(), moneyPaybackModel.getFriend());
					if(modelId == -1){
				    	if(newDebtAccount != null) {
				    		HyjModelEditor<MoneyAccount> newDebtAccountEditor = newDebtAccount.newModelEditor();
				    		newDebtAccountEditor.getModelCopy().setCurrentBalance(newDebtAccount.getCurrentBalance() - moneyPaybackModel.getAmount0());
				    		newDebtAccountEditor.save();
				    	}else{
				    		MoneyAccount.createDebtAccount(moneyPaybackModel.getFriend(), moneyPaybackModel.getMoneyAccount().getCurrencyId(), -moneyPaybackModel.getAmount0());
				    	}
					}else{
						MoneyAccount oldDebtAccount = MoneyAccount.getDebtAccount(oldMoneyAccount.getCurrencyId(), oldMoneyPaybackModel.getFriend());
						HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
						if(newDebtAccount != null){
							HyjModelEditor<MoneyAccount> newDebtAccountEditor = newDebtAccount.newModelEditor();
							if(oldDebtAccount.getId().equals(newDebtAccount.getId())){
								newDebtAccountEditor.getModelCopy().setCurrentBalance(newDebtAccount.getCurrentBalance() + oldAmount - moneyPaybackModel.getAmount0());
							}else{
								newDebtAccountEditor.getModelCopy().setCurrentBalance(newDebtAccount.getCurrentBalance() - moneyPaybackModel.getAmount0());
								oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() + oldAmount);
								oldDebtAccountEditor.save();
							}
							newDebtAccountEditor.save();
						}else{
							oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() + oldAmount);
							oldDebtAccountEditor.save();
							
							MoneyAccount.createDebtAccount(moneyPaybackModel.getFriend(), moneyPaybackModel.getMoneyAccount().getCurrencyId(), -moneyPaybackModel.getAmount0());
						}
					}
				
				mMoneyPaybackEditor.save();
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
