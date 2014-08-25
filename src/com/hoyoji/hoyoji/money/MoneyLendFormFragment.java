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
import com.hoyoji.android.hyjframework.fragment.HyjTextInputFormFragment;
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
import com.hoyoji.hoyoji.models.MoneyLend;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;


public class MoneyLendFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_FRIEND_ID = 3;
	private static final int GET_REMARK = 4;
	private static final int TAG_IS_PROJECT_MEMBER = R.id.moneyLendFormFragment_selectorField_friend;
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;
	
	private HyjModelEditor<MoneyLend> mMoneyLendEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericAmount = null;
	private HyjDateTimeField mDateTimeFieldPaybackDate = null;
	private HyjNumericField mNumericFieldPaybackedAmount = null;
	private View mSeparatorFieldPaybackedAmount = null;
	private HyjSelectorField mSelectorFieldMoneyAccount = null;
	private HyjSelectorField mSelectorFieldProject = null;
	private HyjNumericField mNumericExchangeRate = null;
	private HyjSelectorField mSelectorFieldFriend = null;
	private ImageView mImageViewClearFriend = null;
	private HyjRemarkField mRemarkFieldRemark = null;
	private ImageView mImageViewRefreshRate = null;
	private View mViewSeparatorExchange = null;
	private LinearLayout mLinearLayoutExchangeRate = null;
	
	private boolean hasEditPermission = true;
	
	@Override
	public Integer useContentView() {
		return R.layout.money_formfragment_moneylend;
	}
	 
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		MoneyLend moneyLend;
		
		Intent intent = getActivity().getIntent();
	    long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			moneyLend =  new Select().from(MoneyLend.class).where("_id=?", modelId).executeSingle();
			hasEditPermission = moneyLend.hasEditPermission();
		} else {
			moneyLend = new MoneyLend();
			final String moneyAccountId = intent.getStringExtra("moneyAccountId");
			if(moneyAccountId != null){
				MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, moneyAccountId);
				moneyLend.setMoneyAccountId(moneyAccountId, moneyAccount.getCurrencyId());
			}
			if(intent.getStringExtra("counterpartId") != null){
				moneyLend.setMoneyBorrowId(intent.getStringExtra("counterpartId"));
			}
		}
		mMoneyLendEditor = moneyLend.newModelEditor();
		
		setupDeleteButton(mMoneyLendEditor);
		
		mImageFieldPicture = (HyjImageField) getView().findViewById(R.id.moneyLendFormFragment_imageField_picture);		
		mImageFieldPicture.setImages(moneyLend.getPictures());
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyLendFormFragment_textField_date);		
		if(modelId != -1){
			mDateTimeFieldDate.setText(moneyLend.getDate());
		}
		
		mNumericAmount = (HyjNumericField) getView().findViewById(R.id.moneyLendFormFragment_textField_amount);		
		double amount = intent.getDoubleExtra("amount", -1.0);//从分享消息导入的金额
		if(amount >= 0.0){
			double exchangeRate = intent.getDoubleExtra("exchangeRate", 1.0);
			mNumericAmount.setNumber(amount*exchangeRate);
		}else{
			mNumericAmount.setNumber(moneyLend.getAmount());
		}
		
		mDateTimeFieldPaybackDate = (HyjDateTimeField) getView().findViewById(R.id.moneyLendFormFragment_textField_paybackDate);
		mDateTimeFieldPaybackDate.setText(moneyLend.getPaybackDate());
	
		mNumericFieldPaybackedAmount = (HyjNumericField) getView().findViewById(R.id.moneyLendFormFragment_textField_paybackedAmount);	
		mNumericFieldPaybackedAmount.setNumber(moneyLend.getPaybackedAmount());
		mNumericFieldPaybackedAmount.setEnabled(false);
		mSeparatorFieldPaybackedAmount = (View) getView().findViewById(R.id.moneyLendFormFragment_separatorField_paybackedAmount);
		mNumericFieldPaybackedAmount.setVisibility(View.GONE);
		mSeparatorFieldPaybackedAmount.setVisibility(View.GONE);
			
		MoneyAccount moneyAccount = moneyLend.getMoneyAccount();
		mSelectorFieldMoneyAccount = (HyjSelectorField) getView().findViewById(R.id.moneyLendFormFragment_selectorField_moneyAccount);

		if(moneyAccount != null){
			mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
			mSelectorFieldMoneyAccount.setText(moneyAccount.getName() + "(" + moneyAccount.getCurrencyId() + ")");
		}
		mSelectorFieldMoneyAccount.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("excludeType", "Debt");
				
				MoneyLendFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, bundle, GET_MONEYACCOUNT_ID);
			}
		});	
		
		Project project;
		String projectId = intent.getStringExtra("projectId");//从消息导入
		if(moneyLend.get_mId() == null && projectId != null){
			project = HyjModel.getModel(Project.class, projectId);
		} else {
			project = moneyLend.getProject();
		}
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(R.id.moneyLendFormFragment_selectorField_project);
		if(project != null){
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getDisplayName() + "(" + project.getCurrencyId() + ")");
		} else {
			mSelectorFieldProject.setText("共享来的收支");
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyLendFormFragment.this.openActivityWithFragmentForResult(ProjectListFragment.class, R.string.projectListFragment_title_select_project, null, GET_PROJECT_ID);
			}
		});	
		
		
		mNumericExchangeRate = (HyjNumericField) getView().findViewById(R.id.moneyLendFormFragment_textField_exchangeRate);		
		mNumericExchangeRate.setNumber(moneyLend.getExchangeRate());
		
		mViewSeparatorExchange = (View) getView().findViewById(R.id.moneyLendFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(R.id.moneyLendFormFragment_linearLayout_exchangeRate);
		
//		Friend friend;
//		if(moneyLend.get_mId() == null){
//			String friendUserId = intent.getStringExtra("friendUserId");//从消息导入
//			if(friendUserId != null){
//				friend = new Select().from(Friend.class).where("friendUserId=?",friendUserId).executeSingle();
//			} else {
//				String localFriendId = intent.getStringExtra("localFriendId");//从消息导入
//				if(localFriendId != null){
//					friend = HyjModel.getModel(Friend.class, localFriendId);
//				} else {
//					friend = moneyLend.getFriend();
//				}
//			}
//		} else {
//			friend = moneyLend.getFriend();
//		}
//		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.moneyLendFormFragment_selectorField_friend);
//		
//		if(friend != null){
//			mSelectorFieldFriend.setModelId(friend.getId());
//			mSelectorFieldFriend.setText(friend.getDisplayName());
//		}
//		mSelectorFieldFriend.setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				MoneyLendFormFragment.this
//				.openActivityWithFragmentForResult(FriendListFragment.class, R.string.friendListFragment_title_select_friend_debtor, null, GET_FRIEND_ID);
//			}
//		}); 
		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.moneyLendFormFragment_selectorField_friend);
		if(moneyLend.get_mId() == null){
			String friendUserId = intent.getStringExtra("friendUserId");//从消息导入
			if(friendUserId != null){
				//看一下该好友是不是项目成员, 如果是，作为项目成员添加
				ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("friendUserId=? AND projectId=?", friendUserId, mSelectorFieldProject.getModelId()).executeSingle();
				if(psa != null){
					if(!psa.getState().equalsIgnoreCase("Accept")){
						mSelectorFieldFriend.setModelId(psa.getFriend().getId());
						mSelectorFieldFriend.setText(psa.getFriendDisplayName());
						mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, false);
   					} else {
						mSelectorFieldFriend.setModelId(friendUserId);
						mSelectorFieldFriend.setText(psa.getFriendDisplayName());
	                	mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, true);
   					}
				} else {
					Friend friend = new Select().from(Friend.class).where("friendUserId=?", friendUserId).executeSingle();
					if(friend != null){
						mSelectorFieldFriend.setModelId(friend.getId());
						mSelectorFieldFriend.setText(friend.getDisplayName());
						mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, false);
					} 
//					else {
//						User user = HyjModel.getModel(User.class, friendUserId);
//						if(user != null){
//							mSelectorFieldFriend.setModelId(friendUserId);
//							mSelectorFieldFriend.setText(user.getDisplayName());
//						}
//					}
				}
			} 
//			else {
//				String localFriendId = intent.getStringExtra("localFriendId");//从消息导入
//				if(localFriendId != null){
//					Friend friend = HyjModel.getModel(Friend.class, localFriendId);
//					if(friend != null){
//						mSelectorFieldFriend.setModelId(friend.getId());
//						mSelectorFieldFriend.setText(friend.getDisplayName());
//						mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, false);
//					}
//				} else {
//					Friend friend = moneyLend.getLocalFriend();
//					if(friend != null){
//						mSelectorFieldFriend.setModelId(friend.getId());
//						mSelectorFieldFriend.setText(friend.getDisplayName());
//						mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, false);
//					} else {
//						User user = moneyLend.getFriendUser();
//						if(user != null){
//							mSelectorFieldFriend.setModelId(user.getId());
//							mSelectorFieldFriend.setText(user.getDisplayName());
//							mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, true);
//						}
//					}
//				}
//			}
		} else {
			Friend friend = moneyLend.getLocalFriend();
			if(friend != null){
				mSelectorFieldFriend.setModelId(friend.getId());
				mSelectorFieldFriend.setText(friend.getDisplayName());
				mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, false);
			} else if(moneyLend.getFriendUserId() != null){
				mSelectorFieldFriend.setModelId(moneyLend.getFriendUserId());
				mSelectorFieldFriend.setText(Friend.getFriendUserDisplayName(moneyLend.getFriendUserId()));
				mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, true);
			}
		}
		
		mSelectorFieldFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
				bundle.putLong("MODEL_ID", project.get_mId());
				openActivityWithFragmentForResult(SelectApportionMemberListFragment.class, R.string.friendListFragment_title_select_friend_debtor, bundle, GET_FRIEND_ID);
			}
		}); 
		
		mImageViewClearFriend = (ImageView) getView().findViewById(
				R.id.moneyLendFormFragment_imageView_clear_friend);
		mImageViewClearFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSelectorFieldFriend.setModelId(null);
				mSelectorFieldFriend.setText("");
			}
		});
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyLendFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyLend.getRemark());
		mRemarkFieldRemark.setEditable(false);
		mRemarkFieldRemark.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("TEXT", mRemarkFieldRemark.getText());
				bundle.putString("HINT", "请输入" + mRemarkFieldRemark.getLabelText());
				MoneyLendFormFragment.this
						.openActivityWithFragmentForResult(
								HyjTextInputFormFragment.class,
								R.string.moneyExpenseFormFragment_textView_remark,
								bundle, GET_REMARK);
			}
		});
		
		ImageView takePictureButton = (ImageView) getView().findViewById(R.id.moneyLendFormFragment_imageView_camera);	
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
		
		mImageViewRefreshRate = (ImageView) getView().findViewById(R.id.moneyLendFormFragment_imageButton_refresh_exchangeRate);	
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
						HyjUtil.displayToast(R.string.moneyLendFormFragment_toast_select_currency);
					}
				}else{
					HyjUtil.displayToast(R.string.moneyLendFormFragment_toast_select_currency);
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
	    if(mMoneyLendEditor!= null && mMoneyLendEditor.getModelCopy().get_mId() != null && !hasEditPermission){
	    	hideSaveAction();
	    }
	}
	
	private void setupDeleteButton(HyjModelEditor<MoneyLend> moneyLendEditor) {

		Button buttonDelete = (Button) getView().findViewById(R.id.button_delete);
		
		final MoneyLend moneyLend = moneyLendEditor.getModelCopy();
		
		if (moneyLend.get_mId() == null) {
			buttonDelete.setVisibility(View.GONE);
		} else {
			buttonDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(moneyLend.hasDeletePermission()){
					((HyjActivity)getActivity()).displayDialog(R.string.app_action_delete_list_item, R.string.app_confirm_delete, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
							new DialogCallbackListener() {
								@Override
								public void doPositiveClick(Object object) {
									try {
										ActiveAndroid.beginTransaction();

										MoneyAccount moneyAccount = moneyLend.getMoneyAccount();
										HyjModelEditor<MoneyAccount> moneyAccountEditor = moneyAccount.newModelEditor();
										moneyAccountEditor.getModelCopy().setCurrentBalance(moneyAccount.getCurrentBalance() + moneyLend.getAmount());
										moneyAccountEditor.save();

										//更新项目余额
										Project newProject = moneyLend.getProject();
										HyjModelEditor<Project> newProjectEditor = newProject.newModelEditor();
										newProjectEditor.getModelCopy().setExpenseTotal(newProject.getExpenseTotal() - moneyLend.getAmount0()*moneyLend.getExchangeRate());
										newProjectEditor.save();
										
										if(!newProject.isProjectMember(moneyLend.getLocalFriendId(), moneyLend.getFriendUserId())){
											MoneyAccount debtAccount = MoneyAccount.getDebtAccount(moneyLend.getProject().getCurrencyId(), moneyLend.getLocalFriendId(), moneyLend.getFriendUserId());
											HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
											debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - moneyLend.getProjectAmount());
											debtAccountEditor.save();
										}
										
										ProjectShareAuthorization projectAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(moneyLend.getProjectId());
										HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = projectAuthorization.newModelEditor();
									    selfProjectAuthorizationEditor.getModelCopy().setActualTotalLend(projectAuthorization.getActualTotalLend() - moneyLend.getAmount0()*moneyLend.getExchangeRate());
										selfProjectAuthorizationEditor.save();
										
										moneyLend.delete();
										
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

		if(mMoneyLendEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			mDateTimeFieldDate.setEnabled(false);
			
			mNumericAmount.setNumber(mMoneyLendEditor.getModel().getProjectAmount());
			mNumericAmount.setEnabled(false);
			
			mDateTimeFieldPaybackDate.setEnabled(false);
			
			mSelectorFieldFriend.setEnabled(false);
			
			mSelectorFieldMoneyAccount.setEnabled(false);
			mSelectorFieldMoneyAccount.setVisibility(View.GONE);
			getView().findViewById(R.id.moneyLendFormFragment_separatorField_moneyAccount).setVisibility(View.GONE);

			mSelectorFieldProject.setEnabled(false);
			
			mNumericExchangeRate.setEnabled(false);

			mNumericFieldPaybackedAmount.setEnabled(false);
			
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
		MoneyLend modelCopy = (MoneyLend) mMoneyLendEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setAmount(mNumericAmount.getNumber());
		modelCopy.setPaybackDate(mDateTimeFieldPaybackDate.getText());
		if(mSelectorFieldMoneyAccount.getModelId() != null){
			MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mSelectorFieldMoneyAccount.getModelId());
			modelCopy.setMoneyAccountId(mSelectorFieldMoneyAccount.getModelId(), moneyAccount.getCurrencyId());
		} else {
			modelCopy.setMoneyAccountId(null, null);
		}
		modelCopy.setProject(HyjModel.getModel(Project.class, mSelectorFieldProject.getModelId()));
		modelCopy.setExchangeRate(mNumericExchangeRate.getNumber());
		
//		if(mSelectorFieldFriend.getModelId() != null){
//			Friend friend = HyjModel.getModel(Friend.class, mSelectorFieldFriend.getModelId());
//			modelCopy.setFriend(friend);
//		}else{
//			modelCopy.setFriend(null);
//		}
		if(mSelectorFieldFriend.getModelId() != null){
			if((Boolean) mSelectorFieldFriend.getTag(TAG_IS_PROJECT_MEMBER)){
				modelCopy.setFriendUserId(mSelectorFieldFriend.getModelId());
				modelCopy.setLocalFriendId(null);
			} else {
				modelCopy.setLocalFriendId(mSelectorFieldFriend.getModelId());
				modelCopy.setFriendUserId(null);
			}
		}else{
			modelCopy.setLocalFriendId(null);
			modelCopy.setFriendUserId(null);
		}
		
		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		mDateTimeFieldDate.setError(mMoneyLendEditor.getValidationError("datetime"));
		mNumericAmount.setError(mMoneyLendEditor.getValidationError("amount"));
		if(mMoneyLendEditor.getValidationError("amount") != null){
			mNumericAmount.showSoftKeyboard();
		}
		mDateTimeFieldPaybackDate.setError(mMoneyLendEditor.getValidationError("paybackDate"));
		if(mMoneyLendEditor.getValidationError("paybackDate") != null){
			HyjUtil.displayToast(mMoneyLendEditor.getValidationError("paybackDate"));
		}
		mSelectorFieldMoneyAccount.setError(mMoneyLendEditor.getValidationError("moneyAccount"));
		mSelectorFieldProject.setError(mMoneyLendEditor.getValidationError("project"));
		mNumericExchangeRate.setError(mMoneyLendEditor.getValidationError("exchangeRate"));
		mSelectorFieldFriend.setError(mMoneyLendEditor.getValidationError("friend"));
		mRemarkFieldRemark.setError(mMoneyLendEditor.getValidationError("remark"));
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		if(mMoneyLendEditor.getModelCopy().get_mId() == null && !mMoneyLendEditor.getModelCopy().hasAddNewPermission(mMoneyLendEditor.getModelCopy().getProjectId())){
			HyjUtil.displayToast(R.string.app_permission_no_addnew);
		}else if(mMoneyLendEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			HyjUtil.displayToast(R.string.app_permission_no_edit);
		}else{
		mMoneyLendEditor.validate();
		if(mMoneyLendEditor.getModelCopy().getFriendUserId() == null && mMoneyLendEditor.getModelCopy().getLocalFriendId() == null){
			mMoneyLendEditor.setValidationError("friend",R.string.moneyLendFormFragment_editText_hint_friend);
		}else{
			mMoneyLendEditor.removeValidationError("friend");
		}
		
		if(mMoneyLendEditor.hasValidationErrors()){
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
						newPic.setRecordId(mMoneyLendEditor.getModel().getId());
						newPic.setRecordType("MoneyLend");
						newPic.setDisplayOrder(i);
						newPic.save();
					} else if(pi.getState() == PictureItem.DELETED){
						pi.getPicture().delete();
					} else if(pi.getState() == PictureItem.CHANGED){

					}
					if (!mainPicSet && pi.getPicture() != null && pi.getState() != PictureItem.DELETED) {
						mainPicSet = true;
						mMoneyLendEditor.getModelCopy().setPicture(pi.getPicture());
					}
				}
				
				MoneyLend oldMoneyLendModel = mMoneyLendEditor.getModel();
				MoneyLend moneyLendModel = mMoneyLendEditor.getModelCopy();
				
				UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
				if(moneyLendModel.get_mId() == null && !userData.getActiveMoneyAccountId().equals(moneyLendModel.getMoneyAccountId()) || !userData.getActiveProjectId().equals(moneyLendModel.getProjectId())){
					HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
					userDataEditor.getModelCopy().setActiveMoneyAccountId(moneyLendModel.getMoneyAccountId());
					userDataEditor.getModelCopy().setActiveProjectId(moneyLendModel.getProjectId());
					userDataEditor.save();
				}
				
				String localCurrencyId = moneyLendModel.getMoneyAccount().getCurrencyId();
				String foreignCurrencyId = moneyLendModel.getProject().getCurrencyId();
				if(CREATE_EXCHANGE == 1){
					Exchange newExchange = new Exchange();
					newExchange.setLocalCurrencyId(moneyLendModel.getMoneyAccount().getCurrencyId());
					newExchange.setForeignCurrencyId(moneyLendModel.getProject().getCurrencyId());
					newExchange.setRate(moneyLendModel.getExchangeRate());
//					newExchange.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
					newExchange.save();
				}else {
					if(!localCurrencyId.equalsIgnoreCase(foreignCurrencyId)){
						Exchange exchange = null;
						Double exRate = null;
						Double rate = HyjUtil.toFixed2(moneyLendModel.getExchangeRate());
						exchange = Exchange.getExchange(localCurrencyId, foreignCurrencyId);
						if(exchange != null){
							exRate = exchange.getRate();
							if(!rate.equals(exRate)){
								HyjModelEditor<Exchange> exchangModelEditor = exchange.newModelEditor();
								exchangModelEditor.getModelCopy().setRate(rate);
								exchangModelEditor.save();
							}
						} else {
							exchange = Exchange.getExchange(foreignCurrencyId, localCurrencyId);
							if(exchange != null){
								exRate = HyjUtil.toFixed2(1 / exchange.getRate());
								if(!rate.equals(exRate)){
									HyjModelEditor<Exchange> exchangModelEditor = exchange.newModelEditor();
									exchangModelEditor.getModelCopy().setRate(1/rate);
									exchangModelEditor.save();
								}
							}
						}
					}
			}
				
//				if(mSelectorFieldMoneyAccount.getModelId() != null){
				    Double oldAmount = oldMoneyLendModel.getAmount0();
					MoneyAccount oldMoneyAccount = oldMoneyLendModel.getMoneyAccount();
					MoneyAccount newMoneyAccount = moneyLendModel.getMoneyAccount();
					HyjModelEditor<MoneyAccount> newMoneyAccountEditor = newMoneyAccount.newModelEditor();
					
					if(moneyLendModel.get_mId() == null || oldMoneyAccount.getId().equals(newMoneyAccount.getId())){
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() + oldAmount - moneyLendModel.getAmount0());
					}else{
						HyjModelEditor<MoneyAccount> oldMoneyAccountEditor = oldMoneyAccount.newModelEditor();
						oldMoneyAccountEditor.getModelCopy().setCurrentBalance(oldMoneyAccount.getCurrentBalance() + oldAmount);
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() - moneyLendModel.getAmount0());
						oldMoneyAccountEditor.save();
					}
					newMoneyAccountEditor.save();

				    Project oldProject = oldMoneyLendModel.getProject();
					Project newProject = moneyLendModel.getProject();
					HyjModelEditor<Project> newProjectEditor = newProject.newModelEditor();
					
					//更新项目余额
					if(moneyLendModel.get_mId() == null || oldProject.getId().equals(newProject.getId())){
						newProjectEditor.getModelCopy().setExpenseTotal(newProject.getExpenseTotal() - oldMoneyLendModel.getAmount0()*moneyLendModel.getExchangeRate() + moneyLendModel.getAmount0()*moneyLendModel.getExchangeRate());
					} else {
						HyjModelEditor<Project> oldProjectEditor = oldProject.newModelEditor();
						oldProjectEditor.getModelCopy().setExpenseTotal(oldProject.getExpenseTotal() - oldMoneyLendModel.getAmount0()*oldMoneyLendModel.getExchangeRate());
						newProjectEditor.getModelCopy().setExpenseTotal(newProject.getExpenseTotal() + moneyLendModel.getAmount0()*moneyLendModel.getExchangeRate());
						oldProjectEditor.save();
					}
					newProjectEditor.save();
					
//				}	
					MoneyAccount newDebtAccount = null;
					boolean isNewProjectMember = newProject.isProjectMember(moneyLendModel.getLocalFriendId(), moneyLendModel.getFriendUserId());
					if(!isNewProjectMember){
						newDebtAccount = MoneyAccount.getDebtAccount(moneyLendModel.getProject().getCurrencyId(), moneyLendModel.getLocalFriendId(), moneyLendModel.getFriendUserId());
					}
					if(moneyLendModel.get_mId() == null){
				    	if(newDebtAccount != null) {
				    		HyjModelEditor<MoneyAccount> newDebtAccountEditor = newDebtAccount.newModelEditor();
				    		newDebtAccountEditor.getModelCopy().setCurrentBalance(newDebtAccount.getCurrentBalance() + moneyLendModel.getProjectAmount());
				    		newDebtAccountEditor.save();
				    	}else if(!isNewProjectMember){
				    		MoneyAccount.createDebtAccount(moneyLendModel.getLocalFriendId(), moneyLendModel.getFriendUserId(), moneyLendModel.getProject().getCurrencyId(), moneyLendModel.getProjectAmount());
				    	}
					}else{
						MoneyAccount oldDebtAccount = null;
						if(!oldProject.isProjectMember(oldMoneyLendModel.getLocalFriendId(), oldMoneyLendModel.getFriendUserId())){
							oldDebtAccount = MoneyAccount.getDebtAccount(oldMoneyLendModel.getProject().getCurrencyId(), oldMoneyLendModel.getLocalFriendId(), oldMoneyLendModel.getFriendUserId());
						}
						if(newDebtAccount != null){
							HyjModelEditor<MoneyAccount> newDebtAccountEditor = newDebtAccount.newModelEditor();
							if(oldDebtAccount != null && oldDebtAccount.getId().equals(newDebtAccount.getId())){
								newDebtAccountEditor.getModelCopy().setCurrentBalance(newDebtAccount.getCurrentBalance() - oldMoneyLendModel.getProjectAmount() + moneyLendModel.getProjectAmount());
							} else {
								newDebtAccountEditor.getModelCopy().setCurrentBalance(newDebtAccount.getCurrentBalance() + moneyLendModel.getProjectAmount());
								if(oldDebtAccount != null){
									HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
									oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - oldMoneyLendModel.getProjectAmount());
									oldDebtAccountEditor.save();
								}
							}
							newDebtAccountEditor.save();
						}else{
							if(oldDebtAccount != null){
								HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
								oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - oldMoneyLendModel.getProjectAmount());
								oldDebtAccountEditor.save();
							}
							if(!isNewProjectMember){
								MoneyAccount.createDebtAccount(moneyLendModel.getLocalFriendId(), moneyLendModel.getFriendUserId(), moneyLendModel.getProject().getCurrencyId(), moneyLendModel.getProjectAmount());
							}
						}
					}
					
					//更新支出所有者的实际借出
						ProjectShareAuthorization selfProjectAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(moneyLendModel.getProjectId());
						HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = selfProjectAuthorization.newModelEditor();
					    if(moneyLendModel.get_mId() == null || oldMoneyLendModel.getProjectId().equals(moneyLendModel.getProjectId())){
					    	selfProjectAuthorizationEditor.getModelCopy().setActualTotalLend(selfProjectAuthorization.getActualTotalLend() - oldMoneyLendModel.getAmount0()*oldMoneyLendModel.getExchangeRate() + moneyLendModel.getAmount0()*moneyLendModel.getExchangeRate());
						}else{
							ProjectShareAuthorization oldSelfProjectAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(oldMoneyLendModel.getProjectId());
							HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
							oldSelfProjectAuthorizationEditor.getModelCopy().setActualTotalLend(oldSelfProjectAuthorization.getActualTotalLend() - oldMoneyLendModel.getAmount0()*oldMoneyLendModel.getExchangeRate());
							selfProjectAuthorizationEditor.getModelCopy().setActualTotalLend(selfProjectAuthorization.getActualTotalLend() + moneyLendModel.getAmount0()*moneyLendModel.getExchangeRate());
							oldSelfProjectAuthorizationEditor.save();
						}
						 selfProjectAuthorizationEditor.save();
					
					
					
				mMoneyLendEditor.save();
				ActiveAndroid.setTransactionSuccessful();
				if(getActivity().getCallingActivity() != null){
					getActivity().setResult(Activity.RESULT_OK);
				} else {
					HyjUtil.displayToast(R.string.app_save_success);
				}
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
					
					if(mMoneyLendEditor.getModelCopy().get_mId() == null && !psa.getProjectShareMoneyLendAddNew()){
						HyjUtil.displayToast(R.string.app_permission_no_addnew);
						return;
					}else if(mMoneyLendEditor.getModelCopy().get_mId() != null && !psa.getProjectShareMoneyLendEdit()){
						HyjUtil.displayToast(R.string.app_permission_no_edit);
						return;
					}
	         		
	         		mSelectorFieldProject.setText(project.getDisplayName() + "(" + project.getCurrencyId() + ")");
	         		mSelectorFieldProject.setModelId(project.getId());
	         		setExchangeRate(false);
	         		
	         	// 看一下好友是不是新项目的成员
	         		if(mSelectorFieldFriend.getModelId() != null) {
	        			String friendUserId = null;
	        			ProjectShareAuthorization psaMember = null;
	        			Friend friend = null;
	        			if((Boolean) mSelectorFieldFriend.getTag(TAG_IS_PROJECT_MEMBER)){
	        				friendUserId = mSelectorFieldFriend.getModelId();
	        			} else {
	        				String localFriendId = mSelectorFieldFriend.getModelId();
	        				friend = HyjModel.getModel(Friend.class, localFriendId);
	        				friendUserId = friend.getFriendUserId();
	        			}
	        			if(friendUserId != null){
	        				psaMember = new Select().from(ProjectShareAuthorization.class).where("projectId = ? AND friendUserId=?", project.getId(), friendUserId).executeSingle();
	        			}
	        			
	    				if(psaMember != null){
                    		mSelectorFieldFriend.setModelId(friendUserId);
                    		mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, true);
	    				} else {
	    					if(friendUserId != null){
	    						friend = new Select().from(Friend.class).where("friendUserId = ?", friendUserId).executeSingle();
	    					}
	    					if(friend == null){
	    						mSelectorFieldFriend.setText(null);
	    						mSelectorFieldFriend.setModelId(null);
	                    		mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, false);
	    					} else {
		    					String localFriendId = friend.getId();
	                    		mSelectorFieldFriend.setModelId(localFriendId);
	                    		mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, false);
	    					}
	    				}
	        		}
	        	 }
	        	 break;

     		case GET_REMARK:
     			if (resultCode == Activity.RESULT_OK) {
     				String text = data.getStringExtra("TEXT");
     				mRemarkFieldRemark.setText(text);
     			}
     			break;
             case GET_FRIEND_ID:
//            	 if(resultCode == Activity.RESULT_OK){
//            		long _id = data.getLongExtra("MODEL_ID", -1);
//            		Friend friend = Friend.load(Friend.class, _id);
//            		
//            		if(friend.getFriendUserId() != null && friend.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
//    					HyjUtil.displayToast(R.string.moneyLendFormFragment_editText_error_friend);
//    					return;
//    				}
//            		
//            		mSelectorFieldFriend.setText(friend.getDisplayName());
//            		mSelectorFieldFriend.setModelId(friend.getId());
//            	 }
//            	 break;
            	 if (resultCode == Activity.RESULT_OK) {
      				long _id = data.getLongExtra("MODEL_ID", -1);
      				String type = data.getStringExtra("MODEL_TYPE");
      				if("ProjectShareAuthorization".equalsIgnoreCase(type)){
      					ProjectShareAuthorization psa = ProjectShareAuthorization.load(ProjectShareAuthorization.class, _id);
      					if(psa.getFriendUserId() != null && psa.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
         					HyjUtil.displayToast(R.string.moneyLendFormFragment_editText_error_friend);
         					return;
  	            		}
                 		if(!psa.getState().equalsIgnoreCase("Accept")){
      	            		mSelectorFieldFriend.setText(psa.getFriendDisplayName());
                     		mSelectorFieldFriend.setModelId(psa.getFriend().getId());
                     		mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, false);
      					} else {
 	                		mSelectorFieldFriend.setText(psa.getFriendDisplayName());
 	                		mSelectorFieldFriend.setModelId(psa.getFriendUserId());
 	                		mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, true);
      					}
      				} else {
      					Friend friend = Friend.load(Friend.class, _id);
      					if(friend.getFriendUserId() != null){
      						if(friend.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
             					HyjUtil.displayToast(R.string.moneyLendFormFragment_editText_error_friend);
             					return;
      	            		}
      						
      						//看一下该好友是不是项目成员, 如果是，作为项目成员添加
      						ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("friendUserId=? AND projectId=?", friend.getFriendUserId(), mSelectorFieldProject.getModelId()).executeSingle();
      						if(psa != null){
      							if(!psa.getState().equalsIgnoreCase("Accept")){
      	                    		mSelectorFieldFriend.setText(friend.getDisplayName());
      	                    		mSelectorFieldFriend.setModelId(friend.getId());
      	                    		mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, false);
      	     					} else {
      		                		mSelectorFieldFriend.setText(friend.getDisplayName());
      		                		mSelectorFieldFriend.setModelId(friend.getFriendUserId());
      		                		mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, true);
      	     					}
      						} else {
  	                    		mSelectorFieldFriend.setText(friend.getDisplayName());
  	                    		mSelectorFieldFriend.setModelId(friend.getId());
  	                    		mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, false);
      						}
      					} else {
 	                    		mSelectorFieldFriend.setText(friend.getDisplayName());
 	                    		mSelectorFieldFriend.setModelId(friend.getId());
 	                    		mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, false);
      					}
      				}
      			}
      			break;
          }
    }
}
