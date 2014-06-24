package com.hoyoji.hoyoji.money;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
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
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyPayback;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;


public class MoneyDepositPaybackFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_FRIEND_ID = 3;
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;
	
	private HyjModelEditor<MoneyPayback> mMoneyPaybackEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericAmount = null;
//	private HyjDateTimeField mDateTimeFieldPaybackDate = null;
//	private HyjNumericField mNumericFieldPaybackedAmount = null;
//	private View mSeparatorFieldPaybackedAmount = null;
	private HyjSelectorField mSelectorFieldMoneyAccount = null;
	private HyjSelectorField mSelectorFieldProject = null;
	private HyjNumericField mNumericExchangeRate = null;
	private HyjSelectorField mSelectorFieldFriend = null;
//	private ImageView mImageViewClearFriend = null;
	private HyjRemarkField mRemarkFieldRemark = null;
	private ImageView mImageViewRefreshRate = null;
	private View mViewSeparatorExchange = null;
	private LinearLayout mLinearLayoutExchangeRate = null;
	
	private boolean hasEditPermission = true;
	
	@Override
	public Integer useContentView() {
		return R.layout.money_formfragment_moneydepositpayback;
	}
	 
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		MoneyPayback moneyPayback;
		
		Intent intent = getActivity().getIntent();
	    long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			moneyPayback =  new Select().from(MoneyPayback.class).where("_id=?", modelId).executeSingle();
			hasEditPermission = moneyPayback.hasEditPermission();
		} else {
			moneyPayback = new MoneyPayback();
			moneyPayback.setPaybackType("Deposit");
			final String moneyAccountId = intent.getStringExtra("moneyAccountId");
			if(moneyAccountId != null){
				MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, moneyAccountId);
				moneyPayback.setMoneyAccountId(moneyAccountId, moneyAccount.getCurrencyId());
			}
			if(intent.getStringExtra("counterpartId") != null){
				moneyPayback.setMoneyReturnId(intent.getStringExtra("counterpartId"));
			}
		}
		mMoneyPaybackEditor = moneyPayback.newModelEditor();
		
		setupDeleteButton(mMoneyPaybackEditor);
		
		mImageFieldPicture = (HyjImageField) getView().findViewById(R.id.moneyDepositPaybackFormFragment_imageField_picture);		
		mImageFieldPicture.setImages(moneyPayback.getPictures());
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyDepositPaybackFormFragment_textField_date);		
		if(modelId != -1){
			mDateTimeFieldDate.setText(moneyPayback.getDate());
		}
		
		mNumericAmount = (HyjNumericField) getView().findViewById(R.id.moneyDepositPaybackFormFragment_textField_amount);		
		double amount = intent.getDoubleExtra("amount", -1.0);//从分享消息导入的金额
		if(amount >= 0.0){
			mNumericAmount.setNumber(amount);
		}else{
			mNumericAmount.setNumber(moneyPayback.getAmount());
		}
		
//		mDateTimeFieldPaybackDate = (HyjDateTimeField) getView().findViewById(R.id.moneyDepositPaybackFormFragment_textField_paybackDate);
//		mDateTimeFieldPaybackDate.setText(moneyPayback.getPaybackDate());
//	
//		mNumericFieldPaybackedAmount = (HyjNumericField) getView().findViewById(R.id.moneyDepositPaybackFormFragment_textField_paybackedAmount);	
//		mNumericFieldPaybackedAmount.setNumber(moneyPayback.getPaybackedAmount());
//		mNumericFieldPaybackedAmount.setEnabled(false);
//		mSeparatorFieldPaybackedAmount = (View) getView().findViewById(R.id.moneyDepositPaybackFormFragment_separatorField_paybackedAmount);
//		mNumericFieldPaybackedAmount.setVisibility(View.GONE);
//		mSeparatorFieldPaybackedAmount.setVisibility(View.GONE);
			
		MoneyAccount moneyAccount = moneyPayback.getMoneyAccount();
		mSelectorFieldMoneyAccount = (HyjSelectorField) getView().findViewById(R.id.moneyDepositPaybackFormFragment_selectorField_moneyAccount);

		if(moneyAccount != null){
			mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
			mSelectorFieldMoneyAccount.setText(moneyAccount.getName() + "(" + moneyAccount.getCurrencyId() + ")");
		}
		mSelectorFieldMoneyAccount.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("excludeType", "Debt");
				
				MoneyDepositPaybackFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, bundle, GET_MONEYACCOUNT_ID);
			}
		});	
		
		Project project;
		String projectId = intent.getStringExtra("projectId");//从消息导入
		if(moneyPayback.get_mId() == null && projectId != null){
			project = HyjModel.getModel(Project.class, projectId);
		}else{
			project = moneyPayback.getProject();
		}
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(R.id.moneyDepositPaybackFormFragment_selectorField_project);
		
		if(project != null){
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getDisplayName() + "(" + project.getCurrencyId() + ")");
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyDepositPaybackFormFragment.this.openActivityWithFragmentForResult(ProjectListFragment.class, R.string.projectListFragment_title_select_project, null, GET_PROJECT_ID);
			}
		});	
		
		
		mNumericExchangeRate = (HyjNumericField) getView().findViewById(R.id.moneyDepositPaybackFormFragment_textField_exchangeRate);		
		mNumericExchangeRate.setNumber(moneyPayback.getExchangeRate());
		
		mViewSeparatorExchange = (View) getView().findViewById(R.id.moneyDepositPaybackFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(R.id.moneyDepositPaybackFormFragment_linearLayout_exchangeRate);
		
		Friend friend;
		if(moneyPayback.get_mId() == null){
			String friendUserId = intent.getStringExtra("friendUserId");//从消息导入
			if(friendUserId != null){
				friend = new Select().from(Friend.class).where("friendUserId=?",friendUserId).executeSingle();
			} else {
				String localFriendId = intent.getStringExtra("localFriendId");//从消息导入
				if(localFriendId != null){
					friend = HyjModel.getModel(Friend.class, localFriendId);
				} else {
					friend = moneyPayback.getFriend();
				}
			}
		} else {
			friend = moneyPayback.getFriend();
		}
		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.moneyDepositPaybackFormFragment_selectorField_friend);
		
		if(friend != null){
			mSelectorFieldFriend.setModelId(friend.getId());
			mSelectorFieldFriend.setText(friend.getDisplayName());
		}
		mSelectorFieldFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyDepositPaybackFormFragment.this
				.openActivityWithFragmentForResult(FriendListFragment.class, R.string.friendListFragment_title_select_friend_debtor, null, GET_FRIEND_ID);
			}
		}); 
		
//		mImageViewClearFriend = (ImageView) getView().findViewById(
//				R.id.moneyDepositPaybackFormFragment_imageView_clear_friend);
//		mImageViewClearFriend.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				mSelectorFieldFriend.setModelId(null);
//				mSelectorFieldFriend.setText("");
//			}
//		});

		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyDepositPaybackFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyPayback.getRemark());
		
		ImageView takePictureButton = (ImageView) getView().findViewById(R.id.moneyDepositPaybackFormFragment_imageView_camera);	
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
				
				if(!hasEditPermission){
					for(int i = 0; i<popup.getMenu().size();i++){
						popup.getMenu().setGroupEnabled(i, false);
					}
				}
				
				popup.show();		
			}
		});
		
		mImageViewRefreshRate = (ImageView) getView().findViewById(R.id.moneyDepositPaybackFormFragment_imageButton_refresh_exchangeRate);	
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
						HyjUtil.displayToast(R.string.moneyDepositPaybackFormFragment_toast_select_currency);
					}
				}else{
					HyjUtil.displayToast(R.string.moneyDepositPaybackFormFragment_toast_select_currency);
				}
			}
		});
		
	
		// 只在新增时才自动打开软键盘， 修改时不自动打开
		if (modelId == -1) {
			setExchangeRate(false);
			this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}else{
			setExchangeRate(true);
		}
		setPermission();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    super.onCreateOptionsMenu(menu, inflater);
	    if(mMoneyPaybackEditor!= null && mMoneyPaybackEditor.getModelCopy().get_mId() != null && !hasEditPermission){
	    	hideSaveAction();
	    }
	}
	
	private void setupDeleteButton(HyjModelEditor<MoneyPayback> moneyPaybackEditor) {

		Button buttonDelete = (Button) getView().findViewById(R.id.button_delete);
		
		final MoneyPayback moneyPayback = moneyPaybackEditor.getModelCopy();
		
		if (moneyPayback.get_mId() == null) {
			buttonDelete.setVisibility(View.GONE);
		} else {
			buttonDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(moneyPayback.hasDeletePermission()){
					((HyjActivity)getActivity()).displayDialog(R.string.app_action_delete_list_item, R.string.app_confirm_delete, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
							new DialogCallbackListener() {
								@Override
								public void doPositiveClick(Object object) {
									try {
										ActiveAndroid.beginTransaction();

										MoneyAccount moneyAccount = moneyPayback.getMoneyAccount();
										HyjModelEditor<MoneyAccount> moneyAccountEditor = moneyAccount.newModelEditor();
										moneyAccountEditor.getModelCopy().setCurrentBalance(moneyAccount.getCurrentBalance() - moneyPayback.getAmount());
										moneyAccountEditor.save();

										if(!moneyPayback.getProject().isProjectMember(moneyPayback.getLocalFriendId(), moneyPayback.getFriendUserId())){
											MoneyAccount debtAccount = MoneyAccount.getDebtAccount(moneyPayback.getProject().getCurrencyId(), moneyPayback.getLocalFriendId(), moneyPayback.getFriendUserId());
											HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
											debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + moneyPayback.getProjectAmount());
											debtAccountEditor.save();
										}

										ProjectShareAuthorization projectAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(moneyPayback.getProjectId());
										HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = projectAuthorization.newModelEditor();
									    selfProjectAuthorizationEditor.getModelCopy().setActualTotalPayback(projectAuthorization.getActualTotalPayback() - moneyPayback.getProjectAmount());
										selfProjectAuthorizationEditor.save();
										
										moneyPayback.delete();
										
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
					}else{
						HyjUtil.displayToast(R.string.app_permission_no_delete);
					}
				}
			});
		}
		
	}
	
	private void setPermission(){

		if(mMoneyPaybackEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			mDateTimeFieldDate.setEnabled(false);
			
			mNumericAmount.setNumber(mMoneyPaybackEditor.getModel().getProjectAmount());
			mNumericAmount.setEnabled(false);
			
//			mDateTimeFieldPaybackDate.setEnabled(false);
			
			mSelectorFieldFriend.setEnabled(false);
			
			mSelectorFieldMoneyAccount.setEnabled(false);
			mSelectorFieldMoneyAccount.setVisibility(View.GONE);

			mSelectorFieldProject.setEnabled(false);
			
			mNumericExchangeRate.setEnabled(false);

//			mNumericFieldPaybackedAmount.setEnabled(false);
			
			mRemarkFieldRemark.setEnabled(false);
			
			if(this.mOptionsMenu != null){
		    	hideSaveAction();
			}

//			getView().findViewById(R.id.button_save).setEnabled(false);	
			getView().findViewById(R.id.button_delete).setEnabled(false);
			getView().findViewById(R.id.button_delete).setVisibility(View.GONE);
		}
	}
	
	
	private void setExchangeRate(Boolean editInit){
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
		}
			SET_EXCHANGE_RATE_FLAG = 0;
	}
	
	private void fillData(){
		MoneyPayback modelCopy = (MoneyPayback) mMoneyPaybackEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setAmount(mNumericAmount.getNumber());
//		modelCopy.setPaybackDate(mDateTimeFieldPaybackDate.getText());
		if(mSelectorFieldMoneyAccount.getModelId() != null){
			MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mSelectorFieldMoneyAccount.getModelId());
			modelCopy.setMoneyAccountId(mSelectorFieldMoneyAccount.getModelId(), moneyAccount.getCurrencyId());
		} else {
			modelCopy.setMoneyAccountId(null, null);
		}
		modelCopy.setProject(HyjModel.getModel(Project.class, mSelectorFieldProject.getModelId()));
		modelCopy.setExchangeRate(mNumericExchangeRate.getNumber());
		
		if(mSelectorFieldFriend.getModelId() != null){
			Friend friend = HyjModel.getModel(Friend.class, mSelectorFieldFriend.getModelId());
			modelCopy.setFriend(friend);
		}else{
			modelCopy.setFriend(null);
		}
		
		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
		
		HyjUtil.displayToast(this.mDateTimeFieldDate.getText().toString());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		mDateTimeFieldDate.setError(mMoneyPaybackEditor.getValidationError("datetime"));
		mNumericAmount.setError(mMoneyPaybackEditor.getValidationError("amount"));
		if(mMoneyPaybackEditor.getValidationError("amount") != null){
			mNumericAmount.showSoftKeyboard();
		}
//		mDateTimeFieldPaybackDate.setError(mMoneyPaybackEditor.getValidationError("paybackDate"));
//		if(mMoneyPaybackEditor.getValidationError("paybackDate") != null){
//			HyjUtil.displayToast(mMoneyPaybackEditor.getValidationError("paybackDate"));
//		}
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
		
		if(mMoneyPaybackEditor.getModelCopy().get_mId() == null && !mMoneyPaybackEditor.getModelCopy().hasAddNewPermission(mMoneyPaybackEditor.getModelCopy().getProjectId())){
			HyjUtil.displayToast(R.string.app_permission_no_addnew);
		}else if(mMoneyPaybackEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			HyjUtil.displayToast(R.string.app_permission_no_edit);
		}else{
			mMoneyPaybackEditor.validate();
		
			if(mMoneyPaybackEditor.getModelCopy().getFriend() == null){
				mMoneyPaybackEditor.setValidationError("friend",R.string.moneyDepositPaybackFormFragment_editText_hint_friend);
			}else{
				mMoneyPaybackEditor.removeValidationError("friend");
			}
			
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
						newPic.setRecordType("MoneyPayback");
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
				if(moneyPaybackModel.get_mId() == null && !userData.getActiveMoneyAccountId().equals(moneyPaybackModel.getMoneyAccountId()) || !userData.getActiveProjectId().equals(moneyPaybackModel.getProjectId())){
					HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
					userDataEditor.getModelCopy().setActiveMoneyAccountId(moneyPaybackModel.getMoneyAccountId());
					userDataEditor.getModelCopy().setActiveProjectId(moneyPaybackModel.getProjectId());
					userDataEditor.save();
				}
				
				String localCurrencyId = moneyPaybackModel.getMoneyAccount().getCurrencyId();
				String foreignCurrencyId = moneyPaybackModel.getProject().getCurrencyId();
				if(CREATE_EXCHANGE == 1){
					Exchange newExchange = new Exchange();
					newExchange.setLocalCurrencyId(moneyPaybackModel.getMoneyAccount().getCurrencyId());
					newExchange.setForeignCurrencyId(moneyPaybackModel.getProject().getCurrencyId());
					newExchange.setRate(moneyPaybackModel.getExchangeRate());
//					newExchange.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
					newExchange.save();
				}else if(!localCurrencyId.equalsIgnoreCase(foreignCurrencyId)){
					Exchange exchange = Exchange.getExchange(localCurrencyId, foreignCurrencyId);
					Double rate = HyjUtil.toFixed2(moneyPaybackModel.getExchangeRate());
					if(exchange != null){
						if(exchange.getRate() != rate){
							HyjModelEditor<Exchange> exchangModelEditor = exchange.newModelEditor();
							exchangModelEditor.getModelCopy().setRate(rate);
							exchangModelEditor.save();
						}
					}
//					else{
//						exchange = Exchange.getExchange(localCurrencyId, foreignCurrencyId);
//						if(exchange.getRate() != 1/rate){
//							HyjModelEditor<Exchange> exchangModelEditor = exchange.newModelEditor();
//							exchangModelEditor.getModelCopy().setRate(1/rate);
//							exchangModelEditor.save();
//						}
//					}
				}

//				if(mSelectorFieldMoneyAccount.getModelId() != null){
				    Double oldAmount = oldMoneyPaybackModel.getAmount0();
					MoneyAccount oldMoneyAccount = oldMoneyPaybackModel.getMoneyAccount();
					MoneyAccount newMoneyAccount = moneyPaybackModel.getMoneyAccount();
					HyjModelEditor<MoneyAccount> newMoneyAccountEditor = newMoneyAccount.newModelEditor();
					
					if(moneyPaybackModel.get_mId() == null || oldMoneyAccount.getId().equals(newMoneyAccount.getId())){
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() - oldAmount + moneyPaybackModel.getAmount0());
					}else{
						HyjModelEditor<MoneyAccount> oldMoneyAccountEditor = oldMoneyAccount.newModelEditor();
						oldMoneyAccountEditor.getModelCopy().setCurrentBalance(oldMoneyAccount.getCurrentBalance() - oldAmount);
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() + moneyPaybackModel.getAmount0());
						oldMoneyAccountEditor.save();
					}
					newMoneyAccountEditor.save();
//				}	
					MoneyAccount newDebtAccount = null;
					boolean isNewProjectMember = mMoneyPaybackEditor.getModel().getProject().isProjectMember(moneyPaybackModel.getLocalFriendId(), moneyPaybackModel.getFriendUserId());
					if(isNewProjectMember){
						newDebtAccount = MoneyAccount.getDebtAccount(moneyPaybackModel.getProject().getCurrencyId(), moneyPaybackModel.getLocalFriendId(), moneyPaybackModel.getFriendUserId());
					}
					if(moneyPaybackModel.get_mId() == null){
				    	if(newDebtAccount != null) {
				    		HyjModelEditor<MoneyAccount> newDebtAccountEditor = newDebtAccount.newModelEditor();
				    		newDebtAccountEditor.getModelCopy().setCurrentBalance(newDebtAccount.getCurrentBalance() - moneyPaybackModel.getProjectAmount());
				    		newDebtAccountEditor.save();
				    	}else{
				    		MoneyAccount.createDebtAccount(moneyPaybackModel.getLocalFriendId(), moneyPaybackModel.getFriendUserId(), moneyPaybackModel.getProject().getCurrencyId(), moneyPaybackModel.getProjectAmount());
				    	}
					}else{
						MoneyAccount oldDebtAccount = null;
						if(!mMoneyPaybackEditor.getModelCopy().getProject().isProjectMember(oldMoneyPaybackModel.getLocalFriendId(), oldMoneyPaybackModel.getFriendUserId())){
							oldDebtAccount = MoneyAccount.getDebtAccount(oldMoneyAccount.getCurrencyId(), oldMoneyPaybackModel.getLocalFriendId(), oldMoneyPaybackModel.getFriendUserId());
						}
						if(newDebtAccount != null){
							HyjModelEditor<MoneyAccount> newDebtAccountEditor = newDebtAccount.newModelEditor();
							if(oldDebtAccount != null && oldDebtAccount.getId().equals(newDebtAccount.getId())){
								newDebtAccountEditor.getModelCopy().setCurrentBalance(newDebtAccount.getCurrentBalance() +  oldMoneyPaybackModel.getProjectAmount() - moneyPaybackModel.getProjectAmount());
							}else{
								newDebtAccountEditor.getModelCopy().setCurrentBalance(newDebtAccount.getCurrentBalance() - moneyPaybackModel.getProjectAmount());
								if(oldDebtAccount != null){
									HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
									oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() + oldMoneyPaybackModel.getProjectAmount());
									oldDebtAccountEditor.save();
								}
							}
							newDebtAccountEditor.save();
						}else{
							if(oldDebtAccount != null){
								HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
								oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() + oldMoneyPaybackModel.getProjectAmount());
								oldDebtAccountEditor.save();
							}
							if(isNewProjectMember){
								MoneyAccount.createDebtAccount(moneyPaybackModel.getLocalFriendId(), moneyPaybackModel.getFriendUserId(), moneyPaybackModel.getProject().getCurrencyId(), moneyPaybackModel.getProjectAmount());
							}
						}
					}
					
					//更新支出所有者的实际借出
					ProjectShareAuthorization selfProjectAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(moneyPaybackModel.getProjectId());
					HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = selfProjectAuthorization.newModelEditor();
				    if(moneyPaybackModel.get_mId() == null || oldMoneyPaybackModel.getProjectId().equals(moneyPaybackModel.getProjectId())){
				    	selfProjectAuthorizationEditor.getModelCopy().setActualTotalPayback(selfProjectAuthorization.getActualTotalPayback() - oldMoneyPaybackModel.getAmount0()*oldMoneyPaybackModel.getExchangeRate() + moneyPaybackModel.getAmount0()*moneyPaybackModel.getExchangeRate());
					}else{
						ProjectShareAuthorization oldSelfProjectAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(oldMoneyPaybackModel.getProjectId());
						HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
						oldSelfProjectAuthorizationEditor.getModelCopy().setActualTotalPayback(oldSelfProjectAuthorization.getActualTotalPayback() - oldMoneyPaybackModel.getAmount0()*oldMoneyPaybackModel.getExchangeRate());
						selfProjectAuthorizationEditor.getModelCopy().setActualTotalPayback(selfProjectAuthorization.getActualTotalPayback() + moneyPaybackModel.getAmount0()*moneyPaybackModel.getExchangeRate());
						oldSelfProjectAuthorizationEditor.save();
					}
					selfProjectAuthorizationEditor.save();
					
					
					
				mMoneyPaybackEditor.save();
				ActiveAndroid.setTransactionSuccessful();
				HyjUtil.displayToast(R.string.app_save_success);
				getActivity().finish();
			} finally {
			    ActiveAndroid.endTransaction();
			}
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
	         		setExchangeRate(false);
	        	 }
	        	 break;
             case GET_PROJECT_ID:
	        	 if(resultCode == Activity.RESULT_OK){
	         		long _id = data.getLongExtra("MODEL_ID", -1);
	         		Project project = Project.load(Project.class, _id);
	         		ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("projectId = ? AND friendUserId=?", project.getId(), HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
					
					if(mMoneyPaybackEditor.getModelCopy().get_mId() == null && !psa.getProjectShareMoneyPaybackAddNew()){
						HyjUtil.displayToast(R.string.app_permission_no_addnew);
						return;
					}else if(mMoneyPaybackEditor.getModelCopy().get_mId() != null && !psa.getProjectShareMoneyPaybackEdit()){
						HyjUtil.displayToast(R.string.app_permission_no_edit);
						return;
					}
	         		
	         		mSelectorFieldProject.setText(project.getDisplayName() + "(" + project.getCurrencyId() + ")");
	         		mSelectorFieldProject.setModelId(project.getId());
	         		setExchangeRate(false);
	        	 }
	        	 break;
             case GET_FRIEND_ID:
            	 if(resultCode == Activity.RESULT_OK){
            		long _id = data.getLongExtra("MODEL_ID", -1);
            		Friend friend = Friend.load(Friend.class, _id);
            		
//            		if(friend.getFriendUserId() != null && friend.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
//    					HyjUtil.displayToast(R.string.moneyDepositPaybackFormFragment_editText_error_friend);
//    					return;
//    				}
            		
            		mSelectorFieldFriend.setText(friend.getDisplayName());
            		mSelectorFieldFriend.setModelId(friend.getId());
            	 }
            	 break;

          }
    }
}
