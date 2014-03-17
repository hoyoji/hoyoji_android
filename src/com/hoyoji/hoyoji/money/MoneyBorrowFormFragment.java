package com.hoyoji.hoyoji.money;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
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
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjHttpGetExchangeRateAsyncTask;
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
import com.hoyoji.hoyoji.models.MoneyBorrow;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;


public class MoneyBorrowFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_FRIEND_ID = 3;
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;
	
	private HyjModelEditor<MoneyBorrow> mMoneyBorrowEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericAmount = null;
	private HyjDateTimeField mDateTimeFieldReturnDate = null;
	private HyjNumericField mNumerFieldReturnedAmount = null;
	private View mSeparatorFieldReturnedAmount = null;
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
		return R.layout.money_formfragment_moneyborrow;
	}
	 
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		MoneyBorrow moneyBorrow;
		
		Intent intent = getActivity().getIntent();
		long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			moneyBorrow =  new Select().from(MoneyBorrow.class).where("_id=?", modelId).executeSingle();
		} else {
			moneyBorrow = new MoneyBorrow();
			
		}
		mMoneyBorrowEditor = moneyBorrow.newModelEditor();
		
		setupDeleteButton(mMoneyBorrowEditor);
		
		mImageFieldPicture = (HyjImageField) getView().findViewById(R.id.moneyBorrowFormFragment_imageField_picture);		
		mImageFieldPicture.setImages(moneyBorrow.getPictures());
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyBorrowFormFragment_textField_date);		
		if(modelId != -1){
			mDateTimeFieldDate.setText(moneyBorrow.getDate());
		}
		
		mNumericAmount = (HyjNumericField) getView().findViewById(R.id.moneyBorrowFormFragment_textField_amount);		
		mNumericAmount.setNumber(moneyBorrow.getAmount());
		
		mDateTimeFieldReturnDate = (HyjDateTimeField) getView().findViewById(R.id.moneyBorrowFormFragment_textField_returnDate);
		mDateTimeFieldReturnDate.setText(moneyBorrow.getReturnDate());
		
		mNumerFieldReturnedAmount = (HyjNumericField) getView().findViewById(R.id.moneyBorrowFormFragment_textField_returnedAmount);	
		mNumerFieldReturnedAmount.setNumber(moneyBorrow.getReturnedAmount());
		mNumerFieldReturnedAmount.setEnabled(false);
		mSeparatorFieldReturnedAmount = (View) getView().findViewById(R.id.moneyBorrowFormFragment_separatorField_returnedAmount);
		if(modelId == -1){
			mNumerFieldReturnedAmount.setVisibility(View.GONE);
			mSeparatorFieldReturnedAmount.setVisibility(View.GONE);
		}else{
			mNumerFieldReturnedAmount.setVisibility(View.VISIBLE);
			mSeparatorFieldReturnedAmount.setVisibility(View.VISIBLE);
		}
		
		MoneyAccount moneyAccount = moneyBorrow.getMoneyAccount();
		mSelectorFieldMoneyAccount = (HyjSelectorField) getView().findViewById(R.id.moneyBorrowFormFragment_selectorField_moneyAccount);
		
		if(moneyAccount != null){
			mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
			mSelectorFieldMoneyAccount.setText(moneyAccount.getName() + "(" + moneyAccount.getCurrencyId() + ")");
		}
		mSelectorFieldMoneyAccount.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("excludeType", "Debt");
				MoneyBorrowFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, bundle, GET_MONEYACCOUNT_ID);
			}
		});	
		
		Project project = moneyBorrow.getProject();
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(R.id.moneyBorrowFormFragment_selectorField_project);
		
		if(project != null){
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getName() + "(" + project.getCurrencyId() + ")");
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyBorrowFormFragment.this.openActivityWithFragmentForResult(ProjectListFragment.class, R.string.projectListFragment_title_select_project, null, GET_PROJECT_ID);
			}
		});	
		
		
		mNumericExchangeRate = (HyjNumericField) getView().findViewById(R.id.moneyBorrowFormFragment_textField_exchangeRate);		
		mNumericExchangeRate.setNumber(moneyBorrow.getExchangeRate());
		
		mViewSeparatorExchange = (View) getView().findViewById(R.id.moneyBorrowFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(R.id.moneyBorrowFormFragment_linearLayout_exchangeRate);
		
		Friend friend = moneyBorrow.getFriend();
		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.moneyBorrowFormFragment_selectorField_friend);
		
		if(friend != null){
			mSelectorFieldFriend.setModelId(friend.getId());
			mSelectorFieldFriend.setText(friend.getDisplayName());
		}
		mSelectorFieldFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyBorrowFormFragment.this
				.openActivityWithFragmentForResult(FriendListFragment.class, R.string.friendListFragment_title_select_friend_creditor, null, GET_FRIEND_ID);
			}
		}); 
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyBorrowFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyBorrow.getRemark());
		
		ImageView takePictureButton = (ImageView) getView().findViewById(R.id.moneyBorrowFormFragment_imageView_camera);	
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
		
		mImageViewRefreshRate = (ImageView) getView().findViewById(R.id.moneyBorrowFormFragment_imageButton_refresh_exchangeRate);	
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
						HyjUtil.updateExchangeRate(fromCurrency, toCurrency, mImageViewRefreshRate, mNumericExchangeRate);
					} else {
						HyjUtil.displayToast(R.string.moneyBorrowFormFragment_toast_select_currency);
					}
				}else{
					HyjUtil.displayToast(R.string.moneyBorrowFormFragment_toast_select_currency);
				}
			}
		});
		
		setExchangeRate();
		
		// 只在新增时才自动打开软键盘， 修改时不自动打开
		if (modelId == -1) {
			this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}
    }
	
	private void setupDeleteButton(HyjModelEditor<MoneyBorrow> moneyBorrowEditor) {

		Button buttonDelete = (Button) getView().findViewById(R.id.button_delete);
		
		final MoneyBorrow moneyBorrow = moneyBorrowEditor.getModelCopy();
		
		if (moneyBorrow.get_mId() == null) {
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

										MoneyAccount moneyAccount = moneyBorrow.getMoneyAccount();
										HyjModelEditor<MoneyAccount> moneyAccountEditor = moneyAccount.newModelEditor();
										MoneyAccount debtAccount = MoneyAccount.getDebtAccount(moneyBorrow.getMoneyAccount().getCurrencyId(), moneyBorrow.getFriend());
										HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
										moneyAccountEditor.getModelCopy().setCurrentBalance(moneyAccount.getCurrentBalance() - moneyBorrow.getAmount());
										debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + moneyBorrow.getAmount());
										moneyBorrow.delete();
										moneyAccountEditor.save();
										debtAccountEditor.save();

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
		MoneyBorrow modelCopy = (MoneyBorrow) mMoneyBorrowEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setAmount(mNumericAmount.getNumber());
		modelCopy.setReturnDate(mDateTimeFieldReturnDate.getText());
		modelCopy.setMoneyAccountId(mSelectorFieldMoneyAccount.getModelId());
		modelCopy.setProjectId(mSelectorFieldProject.getModelId());
		modelCopy.setExchangeRate(mNumericExchangeRate.getNumber());
		
		if(mSelectorFieldFriend.getModelId() != null){
			Friend friend = HyjModel.getModel(Friend.class, mSelectorFieldFriend.getModelId());
			modelCopy.setFriend(friend);
		}
		
		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		mDateTimeFieldDate.setError(mMoneyBorrowEditor.getValidationError("datetime"));
		
		mNumericAmount.setError(mMoneyBorrowEditor.getValidationError("amount"));
		if(mMoneyBorrowEditor.getValidationError("amount") != null){
			mNumericAmount.showSoftKeyboard();
		}
		mDateTimeFieldReturnDate.setError(mMoneyBorrowEditor.getValidationError("returnDate"));
		if(mMoneyBorrowEditor.getValidationError("returnDate") != null){
			HyjUtil.displayToast(mMoneyBorrowEditor.getValidationError("returnDate"));
		}
		
		mSelectorFieldMoneyAccount.setError(mMoneyBorrowEditor.getValidationError("moneyAccount"));
		mSelectorFieldProject.setError(mMoneyBorrowEditor.getValidationError("project"));
		mNumericExchangeRate.setError(mMoneyBorrowEditor.getValidationError("exchangeRate"));
		mSelectorFieldFriend.setError(mMoneyBorrowEditor.getValidationError("friend"));
		mRemarkFieldRemark.setError(mMoneyBorrowEditor.getValidationError("remark"));
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		mMoneyBorrowEditor.validate();
		
		if(mMoneyBorrowEditor.hasValidationErrors()){
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
						newPic.setRecordId(mMoneyBorrowEditor.getModel().getId());
						newPic.setRecordType("Picture");
						newPic.save();
					} else if(pi.getState() == PictureItem.DELETED){
						pi.getPicture().delete();
					} else if(pi.getState() == PictureItem.CHANGED){

					}
					if(!mainPicSet && pi.getPicture() != null){
						mainPicSet = true;
						mMoneyBorrowEditor.getModelCopy().setPicture(pi.getPicture());
					}
				}
				
				MoneyBorrow oldMoneyBorrowModel = mMoneyBorrowEditor.getModel();
				MoneyBorrow moneyBorrowModel = mMoneyBorrowEditor.getModelCopy();
				
				UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
				if(moneyBorrowModel.get_mId() == null && !userData.getActiveMoneyAccountId().equals(moneyBorrowModel.getMoneyAccountId()) || !userData.getActiveProjectId().equals(moneyBorrowModel.getProjectId())){
					HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
					userDataEditor.getModelCopy().setActiveMoneyAccountId(moneyBorrowModel.getMoneyAccountId());
					userDataEditor.getModelCopy().setActiveProjectId(moneyBorrowModel.getProjectId());
					userDataEditor.save();
				}
				
				if(CREATE_EXCHANGE == 1){
					Exchange newExchange = new Exchange();
					newExchange.setLocalCurrencyId(moneyBorrowModel.getMoneyAccount().getCurrencyId());
					newExchange.setForeignCurrencyId(moneyBorrowModel.getProject().getCurrencyId());
					newExchange.setRate(moneyBorrowModel.getExchangeRate());
//					newExchange.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
					newExchange.save();
				}
				    Double oldAmount = oldMoneyBorrowModel.getAmount0();
					MoneyAccount oldMoneyAccount = oldMoneyBorrowModel.getMoneyAccount();
					MoneyAccount newMoneyAccount = moneyBorrowModel.getMoneyAccount();
					HyjModelEditor<MoneyAccount> newMoneyAccountEditor = newMoneyAccount.newModelEditor();
					
					if(moneyBorrowModel.get_mId() == null || oldMoneyAccount.getId().equals(newMoneyAccount.getId())){
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() - oldAmount + moneyBorrowModel.getAmount0());
							
					}else{
						HyjModelEditor<MoneyAccount> oldMoneyAccountEditor = oldMoneyAccount.newModelEditor();
						oldMoneyAccountEditor.getModelCopy().setCurrentBalance(oldMoneyAccount.getCurrentBalance() - oldAmount);
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() + moneyBorrowModel.getAmount0());
						oldMoneyAccountEditor.save();
					}
					newMoneyAccountEditor.save();
				
				
				MoneyAccount newDebtAccount = MoneyAccount.getDebtAccount(moneyBorrowModel.getMoneyAccount().getCurrencyId(), moneyBorrowModel.getFriend());
				if(moneyBorrowModel.get_mId() == null){
			    	if(newDebtAccount != null) {
			    		HyjModelEditor<MoneyAccount> newDebtAccountEditor = newDebtAccount.newModelEditor();
			    		newDebtAccountEditor.getModelCopy().setCurrentBalance(newDebtAccount.getCurrentBalance() - moneyBorrowModel.getAmount0());
			    		newDebtAccountEditor.save();
			    	}else{
			    		MoneyAccount.createDebtAccount(moneyBorrowModel.getFriend(), moneyBorrowModel.getMoneyAccount().getCurrencyId(), -moneyBorrowModel.getAmount0());
			    	}
				}else{
					MoneyAccount oldDebtAccount = MoneyAccount.getDebtAccount(oldMoneyAccount.getCurrencyId(), oldMoneyBorrowModel.getFriend());
					HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
					if(newDebtAccount != null){
						HyjModelEditor<MoneyAccount> newDebtAccountEditor = newDebtAccount.newModelEditor();
						if(oldDebtAccount.getId().equals(newDebtAccount.getId())){
							newDebtAccountEditor.getModelCopy().setCurrentBalance(newDebtAccount.getCurrentBalance() + oldAmount - moneyBorrowModel.getAmount0());
						}else{
							newDebtAccountEditor.getModelCopy().setCurrentBalance(newDebtAccount.getCurrentBalance() - moneyBorrowModel.getAmount0());
							oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() + oldAmount);
							oldDebtAccountEditor.save();
						}
						newDebtAccountEditor.save();
					}else{
						oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() + oldAmount);
						oldDebtAccountEditor.save();
						
						MoneyAccount.createDebtAccount(moneyBorrowModel.getFriend(), moneyBorrowModel.getMoneyAccount().getCurrencyId(), -moneyBorrowModel.getAmount0());
					}
				}
				
				//更新支出所有者的实际借入
					ProjectShareAuthorization selfProjectAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(moneyBorrowModel.getProjectId());
					HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = selfProjectAuthorization.newModelEditor();
				    if(moneyBorrowModel.get_mId() == null || oldMoneyBorrowModel.getProjectId().equals(moneyBorrowModel.getProjectId())){
				    	selfProjectAuthorizationEditor.getModelCopy().setActualTotalBorrow(selfProjectAuthorization.getActualTotalBorrow() - oldMoneyBorrowModel.getAmount0()*oldMoneyBorrowModel.getExchangeRate() + moneyBorrowModel.getAmount0()*moneyBorrowModel.getExchangeRate());
					}else{
						ProjectShareAuthorization oldSelfProjectAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(oldMoneyBorrowModel.getProjectId());
						HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
						oldSelfProjectAuthorizationEditor.getModelCopy().setActualTotalBorrow(oldSelfProjectAuthorization.getActualTotalBorrow() - oldMoneyBorrowModel.getAmount0()*oldMoneyBorrowModel.getExchangeRate());
						selfProjectAuthorizationEditor.getModelCopy().setActualTotalBorrow(selfProjectAuthorization.getActualTotalBorrow() + moneyBorrowModel.getAmount0()*moneyBorrowModel.getExchangeRate());
						oldSelfProjectAuthorizationEditor.save();
					}
					 selfProjectAuthorizationEditor.save();
				
				
				mMoneyBorrowEditor.save();
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
            		mSelectorFieldFriend.setText(friend.getDisplayName());
            		mSelectorFieldFriend.setModelId(friend.getId());
            	 }
            	 break;

          }
    }
}
