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
import com.hoyoji.hoyoji.models.MoneyPayback;
import com.hoyoji.hoyoji.models.MoneyReturn;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;


public class MoneyReturnFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_FRIEND_ID = 3;
	private static final int GET_REMARK = 4;
	private static final int TAG_IS_PROJECT_MEMBER = R.id.moneyLendFormFragment_selectorField_friend;
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;
	
	private HyjModelEditor<MoneyReturn> mMoneyReturnEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericFieldAmount = null;
	private HyjNumericField mNumericFieldInterest = null;
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
		return R.layout.money_formfragment_moneyreturn;
	}
	 
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		MoneyReturn moneyReturn;
		
		Intent intent = getActivity().getIntent();
		long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			moneyReturn =  new Select().from(MoneyReturn.class).where("_id=?", modelId).executeSingle();
			hasEditPermission = moneyReturn.hasEditPermission();
		} else {
			moneyReturn = new MoneyReturn();
			final String moneyAccountId = intent.getStringExtra("moneyAccountId");
			if(moneyAccountId != null){
				MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, moneyAccountId);
				moneyReturn.setMoneyAccountId(moneyAccountId, moneyAccount.getCurrencyId());
			}
			if(intent.getStringExtra("counterpartId") != null){
				moneyReturn.setMoneyPaybackId(intent.getStringExtra("counterpartId"));
			}
		}
		mMoneyReturnEditor = moneyReturn.newModelEditor();
		
		setupDeleteButton(mMoneyReturnEditor);
		
		mImageFieldPicture = (HyjImageField) getView().findViewById(R.id.moneyReturnFormFragment_imageField_picture);		
		mImageFieldPicture.setImages(moneyReturn.getPictures());
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyReturnFormFragment_textField_date);		
		if(modelId != -1){
			mDateTimeFieldDate.setText(moneyReturn.getDate());
		}
		
		mNumericFieldAmount = (HyjNumericField) getView().findViewById(R.id.moneyReturnFormFragment_textField_amount);		
		double amount = intent.getDoubleExtra("amount", -1.0);//从分享消息导入的金额
		if(amount >= 0.0){
			double exchangeRate = intent.getDoubleExtra("exchangeRate", 1.0);
			mNumericFieldAmount.setNumber(amount*exchangeRate);
		}else{
			mNumericFieldAmount.setNumber(moneyReturn.getAmount());
		}
		
		mNumericFieldInterest = (HyjNumericField) getView().findViewById(R.id.moneyReturnFormFragment_textField_interest);		
		mNumericFieldInterest.setNumber(moneyReturn.getInterest());
		
		MoneyAccount moneyAccount = moneyReturn.getMoneyAccount();
		mSelectorFieldMoneyAccount = (HyjSelectorField) getView().findViewById(R.id.moneyReturnFormFragment_selectorField_moneyAccount);

		if(moneyAccount != null){
			mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
			mSelectorFieldMoneyAccount.setText(moneyAccount.getName() + "(" + moneyAccount.getCurrencyId() + ")");
		}
		mSelectorFieldMoneyAccount.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("excludeType", "Debt");
				
				MoneyReturnFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, bundle, GET_MONEYACCOUNT_ID);
			}
		});	
		
		Project project;
		String projectId = intent.getStringExtra("projectId");//从消息导入
		if(moneyReturn.get_mId() == null && projectId != null){
			project = HyjModel.getModel(Project.class, projectId);
		}else{
			project = moneyReturn.getProject();
		}
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(R.id.moneyReturnFormFragment_selectorField_project);
		
		if(project != null){
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getDisplayName() + "(" + project.getCurrencyId() + ")");
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyReturnFormFragment.this.openActivityWithFragmentForResult(ProjectListFragment.class, R.string.projectListFragment_title_select_project, null, GET_PROJECT_ID);
			}
		});	
		
		
		mNumericExchangeRate = (HyjNumericField) getView().findViewById(R.id.moneyReturnFormFragment_textField_exchangeRate);		
		mNumericExchangeRate.setNumber(moneyReturn.getExchangeRate());
		
		mViewSeparatorExchange = (View) getView().findViewById(R.id.moneyReturnFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(R.id.moneyReturnFormFragment_linearLayout_exchangeRate);
		
//		Friend friend;
//		if(moneyReturn.get_mId() == null){
//			String friendUserId = intent.getStringExtra("friendUserId");//从消息导入
//			if(friendUserId != null){
//				friend = new Select().from(Friend.class).where("friendUserId=?",friendUserId).executeSingle();
//			} else {
//				String localFriendId = intent.getStringExtra("localFriendId");//从消息导入
//				if(localFriendId != null){
//					friend = HyjModel.getModel(Friend.class, localFriendId);
//				} else {
//					friend = moneyReturn.getFriend();
//				}
//			}
//		} else {
//			friend = moneyReturn.getFriend();
//		}
//		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.moneyReturnFormFragment_selectorField_friend);
//		
//		if(friend != null){
//			mSelectorFieldFriend.setModelId(friend.getId());
//			mSelectorFieldFriend.setText(friend.getDisplayName());
//		}
//		mSelectorFieldFriend.setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				MoneyReturnFormFragment.this
//				.openActivityWithFragmentForResult(FriendListFragment.class, R.string.friendListFragment_title_select_friend_creditor, null, GET_FRIEND_ID);
//			}
//		}); 
		

		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.moneyReturnFormFragment_selectorField_friend);
		if(moneyReturn.get_mId() == null){
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
			} else {
				String localFriendId = intent.getStringExtra("localFriendId");//从消息导入
				if(localFriendId != null){
					Friend friend = HyjModel.getModel(Friend.class, localFriendId);
					if(friend != null){
						mSelectorFieldFriend.setModelId(friend.getId());
						mSelectorFieldFriend.setText(friend.getDisplayName());
						mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, false);
					}
				} else {
					Friend friend = moneyReturn.getLocalFriend();
					if(friend != null){
						mSelectorFieldFriend.setModelId(friend.getId());
						mSelectorFieldFriend.setText(friend.getDisplayName());
						mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, false);
					} else {
						User user = moneyReturn.getFriendUser();
						if(user != null){
							mSelectorFieldFriend.setModelId(user.getId());
							mSelectorFieldFriend.setText(user.getDisplayName());
							mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, true);
						}
					}
				}
			}
		} else {
			Friend friend = moneyReturn.getLocalFriend();
			if(friend != null){
				mSelectorFieldFriend.setModelId(friend.getId());
				mSelectorFieldFriend.setText(friend.getDisplayName());
				mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, false);
			} else {
				User user = moneyReturn.getFriendUser();
				if(user != null){
					mSelectorFieldFriend.setModelId(user.getId());
					mSelectorFieldFriend.setText(user.getDisplayName());
					mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, true);
				}
			}
		}
		
		mSelectorFieldFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
				bundle.putLong("MODEL_ID", project.get_mId());
				openActivityWithFragmentForResult(SelectApportionMemberListFragment.class, R.string.friendListFragment_title_select_friend_creditor, bundle, GET_FRIEND_ID);
			}
		}); 
		
		
		mImageViewClearFriend = (ImageView) getView().findViewById(
				R.id.moneyReturnFormFragment_imageView_clear_friend);
		mImageViewClearFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSelectorFieldFriend.setModelId(null);
				mSelectorFieldFriend.setText("");
			}
		});
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyReturnFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyReturn.getRemark());
		mRemarkFieldRemark.setEditable(false);
		mRemarkFieldRemark.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("TEXT", mRemarkFieldRemark.getText());
				bundle.putString("HINT", "请输入" + mRemarkFieldRemark.getLabelText());
				MoneyReturnFormFragment.this
						.openActivityWithFragmentForResult(
								HyjTextInputFormFragment.class,
								R.string.moneyExpenseFormFragment_textView_remark,
								bundle, GET_REMARK);
			}
		});
		
		ImageView takePictureButton = (ImageView) getView().findViewById(R.id.moneyReturnFormFragment_imageView_camera);	
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
		
		mImageViewRefreshRate = (ImageView) getView().findViewById(R.id.moneyReturnFormFragment_imageButton_refresh_exchangeRate);	
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
						HyjUtil.displayToast(R.string.moneyReturnFormFragment_toast_select_currency);
					}
				}else{
					HyjUtil.displayToast(R.string.moneyReturnFormFragment_toast_select_currency);
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
	    if(mMoneyReturnEditor!= null && mMoneyReturnEditor.getModelCopy().get_mId() != null && !hasEditPermission){
	    	hideSaveAction();
	    }
	}
	
	private void setupDeleteButton(HyjModelEditor<MoneyReturn> moneyReturnEditor) {

		Button buttonDelete = (Button) getView().findViewById(R.id.button_delete);
		
		final MoneyReturn moneyReturn = moneyReturnEditor.getModelCopy();
		
		if (moneyReturn.get_mId() == null) {
			buttonDelete.setVisibility(View.GONE);
		} else {
			buttonDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(moneyReturn.hasDeletePermission()){
					((HyjActivity)getActivity()).displayDialog(R.string.app_action_delete_list_item, R.string.app_confirm_delete, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
							new DialogCallbackListener() {
								@Override
								public void doPositiveClick(Object object) {
									try {
										ActiveAndroid.beginTransaction();

										MoneyAccount moneyAccount = moneyReturn.getMoneyAccount();
										HyjModelEditor<MoneyAccount> moneyAccountEditor = moneyAccount.newModelEditor();
										moneyAccountEditor.getModelCopy().setCurrentBalance(moneyAccount.getCurrentBalance() + moneyReturn.getAmount() + moneyReturn.getInterest0());
										moneyAccountEditor.save();
										
										//更新项目余额
										Project newProject = moneyReturn.getProject();
										HyjModelEditor<Project> newProjectEditor = newProject.newModelEditor();
										newProjectEditor.getModelCopy().setExpenseTotal(newProject.getExpenseTotal() - moneyReturn.getAmount0()*moneyReturn.getExchangeRate());
										newProjectEditor.save();

										if(!newProject.isProjectMember(moneyReturn.getLocalFriendId(), moneyReturn.getFriendUserId())){
											MoneyAccount debtAccount = MoneyAccount.getDebtAccount(moneyReturn.getProject().getCurrencyId(), moneyReturn.getLocalFriendId(), moneyReturn.getFriendUserId());
											HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
											debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - moneyReturn.getProjectAmount());
											debtAccountEditor.save();
										}
										
										ProjectShareAuthorization projectAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(moneyReturn.getProjectId());
										HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = projectAuthorization.newModelEditor();
									    selfProjectAuthorizationEditor.getModelCopy().setActualTotalReturn(projectAuthorization.getActualTotalReturn() - moneyReturn.getAmount0()*moneyReturn.getExchangeRate());
										
									    selfProjectAuthorizationEditor.save();
										
										moneyReturn.delete();
										
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

		if(mMoneyReturnEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			mDateTimeFieldDate.setEnabled(false);
			
			mNumericFieldAmount.setNumber(mMoneyReturnEditor.getModel().getProjectAmount());
			mNumericFieldAmount.setEnabled(false);
			
			mSelectorFieldFriend.setEnabled(false);
			
			mSelectorFieldMoneyAccount.setEnabled(false);
			mSelectorFieldMoneyAccount.setVisibility(View.GONE);
			getView().findViewById(R.id.moneyReturnFormFragment_separatorField_moneyAccount).setVisibility(View.GONE);

			mSelectorFieldProject.setEnabled(false);
			
			mNumericExchangeRate.setEnabled(false);

			mNumericFieldInterest.setEnabled(false);
			
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
		MoneyReturn modelCopy = (MoneyReturn) mMoneyReturnEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setAmount(mNumericFieldAmount.getNumber());
		modelCopy.setInterest(mNumericFieldInterest.getNumber());
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
		mDateTimeFieldDate.setError(mMoneyReturnEditor.getValidationError("datetime"));
		mNumericFieldAmount.setError(mMoneyReturnEditor.getValidationError("amount"));
		if(mMoneyReturnEditor.getValidationError("amount") != null){
			mNumericFieldAmount.showSoftKeyboard();
		}
		mNumericFieldInterest.setError(mMoneyReturnEditor.getValidationError("interest"));
		mSelectorFieldMoneyAccount.setError(mMoneyReturnEditor.getValidationError("moneyAccount"));
		mSelectorFieldProject.setError(mMoneyReturnEditor.getValidationError("project"));
		mNumericExchangeRate.setError(mMoneyReturnEditor.getValidationError("exchangeRate"));
		mSelectorFieldFriend.setError(mMoneyReturnEditor.getValidationError("friend"));
		mRemarkFieldRemark.setError(mMoneyReturnEditor.getValidationError("remark"));
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		if(mMoneyReturnEditor.getModelCopy().get_mId() == null && !mMoneyReturnEditor.getModelCopy().hasAddNewPermission(mMoneyReturnEditor.getModelCopy().getProjectId())){
			HyjUtil.displayToast(R.string.app_permission_no_addnew);
		}else if(mMoneyReturnEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			HyjUtil.displayToast(R.string.app_permission_no_edit);
		}else{
		mMoneyReturnEditor.validate();
		
		if(mMoneyReturnEditor.hasValidationErrors()){
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
						newPic.setRecordId(mMoneyReturnEditor.getModel().getId());
						newPic.setRecordType("MoneyReturn");
						newPic.setDisplayOrder(i);
						newPic.save();
					} else if(pi.getState() == PictureItem.DELETED){
						pi.getPicture().delete();
					} else if(pi.getState() == PictureItem.CHANGED){

					}
					if (!mainPicSet && pi.getPicture() != null && pi.getState() != PictureItem.DELETED) {
						mainPicSet = true;
						mMoneyReturnEditor.getModelCopy().setPicture(pi.getPicture());
					}
				}
				
				MoneyReturn oldMoneyReturnModel = mMoneyReturnEditor.getModel();
				MoneyReturn moneyReturnModel = mMoneyReturnEditor.getModelCopy();
				
				UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
				if(moneyReturnModel.get_mId() == null && !userData.getActiveMoneyAccountId().equals(moneyReturnModel.getMoneyAccountId()) || !userData.getActiveProjectId().equals(moneyReturnModel.getProjectId())){
					HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
					userDataEditor.getModelCopy().setActiveMoneyAccountId(moneyReturnModel.getMoneyAccountId());
					userDataEditor.getModelCopy().setActiveProjectId(moneyReturnModel.getProjectId());
					userDataEditor.save();
				}
				
				String localCurrencyId = moneyReturnModel.getMoneyAccount().getCurrencyId();
				String foreignCurrencyId = moneyReturnModel.getProject().getCurrencyId();
				if(CREATE_EXCHANGE == 1){
					Exchange newExchange = new Exchange();
					newExchange.setLocalCurrencyId(moneyReturnModel.getMoneyAccount().getCurrencyId());
					newExchange.setForeignCurrencyId(moneyReturnModel.getProject().getCurrencyId());
					newExchange.setRate(moneyReturnModel.getExchangeRate());
					newExchange.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
					newExchange.save();
				}else {
					if(!localCurrencyId.equalsIgnoreCase(foreignCurrencyId)){
						Exchange exchange = null;
						Double exRate = null;
						Double rate = HyjUtil.toFixed2(moneyReturnModel.getExchangeRate());
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
					Double oldAmount = oldMoneyReturnModel.getAmount0();
//					Double oldInterest = oldMoneyReturnModel.getInterest0();
					MoneyAccount oldMoneyAccount = oldMoneyReturnModel.getMoneyAccount();
					MoneyAccount newMoneyAccount = moneyReturnModel.getMoneyAccount();
					HyjModelEditor<MoneyAccount> newMoneyAccountEditor = newMoneyAccount.newModelEditor();
					
					if(moneyReturnModel.get_mId() == null || oldMoneyAccount.getId().equals(newMoneyAccount.getId())){
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() + oldAmount - moneyReturnModel.getAmount0());
							
					}else{
						HyjModelEditor<MoneyAccount> oldMoneyAccountEditor = oldMoneyAccount.newModelEditor();
						oldMoneyAccountEditor.getModelCopy().setCurrentBalance(oldMoneyAccount.getCurrentBalance() + oldAmount);
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() - moneyReturnModel.getAmount0());
						oldMoneyAccountEditor.save();
					}
					newMoneyAccountEditor.save();
					
					Project oldProject = oldMoneyReturnModel.getProject();
					Project newProject = moneyReturnModel.getProject();
					HyjModelEditor<Project> newProjectEditor = newProject.newModelEditor();
					
					//更新项目余额
					if(moneyReturnModel.get_mId() == null || oldProject.getId().equals(newProject.getId())){
						newProjectEditor.getModelCopy().setExpenseTotal(newProject.getExpenseTotal() - oldMoneyReturnModel.getAmount0()*oldMoneyReturnModel.getExchangeRate() + moneyReturnModel.getAmount0()*moneyReturnModel.getExchangeRate());
					} else {
						HyjModelEditor<Project> oldProjectEditor = oldProject.newModelEditor();
						oldProjectEditor.getModelCopy().setExpenseTotal(oldProject.getExpenseTotal() - oldMoneyReturnModel.getAmount0()*oldMoneyReturnModel.getExchangeRate());
						newProjectEditor.getModelCopy().setExpenseTotal(newProject.getExpenseTotal() + moneyReturnModel.getAmount0()*moneyReturnModel.getExchangeRate());
						oldProjectEditor.save();
					}
					newProjectEditor.save();
//				}	

					MoneyAccount newDebtAccount = null;
					boolean isNewProjectMember = newProject.isProjectMember(moneyReturnModel.getLocalFriendId(), moneyReturnModel.getFriendUserId());
					if(!isNewProjectMember){
						newDebtAccount = MoneyAccount.getDebtAccount(moneyReturnModel.getProject().getCurrencyId(), moneyReturnModel.getLocalFriendId(), moneyReturnModel.getFriendUserId());
					}
					if(moneyReturnModel.get_mId() == null){
				    	if(newDebtAccount != null) {
				    		HyjModelEditor<MoneyAccount> newDebtAccountEditor = newDebtAccount.newModelEditor();
				    		newDebtAccountEditor.getModelCopy().setCurrentBalance(newDebtAccount.getCurrentBalance() + moneyReturnModel.getProjectAmount());
				    		newDebtAccountEditor.save();
				    	}else if(!isNewProjectMember){
				    		MoneyAccount.createDebtAccount(moneyReturnModel.getLocalFriendId(), moneyReturnModel.getFriendUserId(), moneyReturnModel.getProject().getCurrencyId(), moneyReturnModel.getProjectAmount());
				    	}
					}else{
						MoneyAccount oldDebtAccount = null;
						if(!oldProject.isProjectMember(oldMoneyReturnModel.getLocalFriendId(), oldMoneyReturnModel.getFriendUserId())){
							oldDebtAccount = MoneyAccount.getDebtAccount(oldMoneyAccount.getCurrencyId(), oldMoneyReturnModel.getLocalFriendId(), oldMoneyReturnModel.getFriendUserId());
						}
						if(newDebtAccount != null){
							HyjModelEditor<MoneyAccount> newDebtAccountEditor = newDebtAccount.newModelEditor();
							if(oldDebtAccount != null && oldDebtAccount.getId().equals(newDebtAccount.getId())){
								newDebtAccountEditor.getModelCopy().setCurrentBalance(newDebtAccount.getCurrentBalance() - oldMoneyReturnModel.getProjectAmount() + moneyReturnModel.getProjectAmount());
							}else{
								newDebtAccountEditor.getModelCopy().setCurrentBalance(newDebtAccount.getCurrentBalance() + moneyReturnModel.getProjectAmount());
								if(oldDebtAccount != null){
									HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
									oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - oldMoneyReturnModel.getProjectAmount());
									oldDebtAccountEditor.save();
								}
							}
							newDebtAccountEditor.save();
						}else{
							if(oldDebtAccount != null){
								HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
								oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - oldMoneyReturnModel.getProjectAmount());
								oldDebtAccountEditor.save();
							}
							if(!isNewProjectMember){
								MoneyAccount.createDebtAccount(moneyReturnModel.getLocalFriendId(), moneyReturnModel.getFriendUserId(), moneyReturnModel.getProject().getCurrencyId(), moneyReturnModel.getProjectAmount());
							}
						}
					}

					//更新支出所有者的实际还款
						ProjectShareAuthorization selfProjectAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(moneyReturnModel.getProjectId());
						HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = selfProjectAuthorization.newModelEditor();
					    if(moneyReturnModel.get_mId() == null || oldMoneyReturnModel.getProjectId().equals(moneyReturnModel.getProjectId())){
					    	selfProjectAuthorizationEditor.getModelCopy().setActualTotalReturn(selfProjectAuthorization.getActualTotalReturn() - oldMoneyReturnModel.getAmount0()*oldMoneyReturnModel.getExchangeRate() + moneyReturnModel.getAmount0()*moneyReturnModel.getExchangeRate());
						}else{
							ProjectShareAuthorization oldSelfProjectAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(oldMoneyReturnModel.getProjectId());
							HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
							oldSelfProjectAuthorizationEditor.getModelCopy().setActualTotalReturn(oldSelfProjectAuthorization.getActualTotalReturn() - oldMoneyReturnModel.getAmount0()*oldMoneyReturnModel.getExchangeRate());
							selfProjectAuthorizationEditor.getModelCopy().setActualTotalReturn(selfProjectAuthorization.getActualTotalReturn() + moneyReturnModel.getAmount0()*moneyReturnModel.getExchangeRate());
							oldSelfProjectAuthorizationEditor.save();
						}
						 selfProjectAuthorizationEditor.save();
					
					
				mMoneyReturnEditor.save();
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
					
					if(mMoneyReturnEditor.getModelCopy().get_mId() == null && !psa.getProjectShareMoneyReturnAddNew()){
						HyjUtil.displayToast(R.string.app_permission_no_addnew);
						return;
					}else if(mMoneyReturnEditor.getModelCopy().get_mId() != null && !psa.getProjectShareMoneyReturnEdit()){
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
//    					HyjUtil.displayToast(R.string.moneyReturnFormFragment_editText_error_friend);
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
          					HyjUtil.displayToast(R.string.moneyReturnFormFragment_editText_error_friend);
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
              					HyjUtil.displayToast(R.string.moneyReturnFormFragment_editText_error_friend);
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
