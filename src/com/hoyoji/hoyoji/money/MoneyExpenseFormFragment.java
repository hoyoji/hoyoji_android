package com.hoyoji.hoyoji.money;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyApportion;
import com.hoyoji.hoyoji.models.MoneyExpense;
import com.hoyoji.hoyoji.models.MoneyExpenseApportion;
import com.hoyoji.hoyoji.models.MoneyExpenseCategory;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.MoneyApportionField.ApportionItem;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.money.moneycategory.MoneyExpenseCategoryListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;

public class MoneyExpenseFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_FRIEND_ID = 3;
	private final static int GET_CATEGORY_ID = 5;
	
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;

	private MoneyExpenseEditor mMoneyExpenseEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericAmount = null;
	private HyjSelectorField mSelectorFieldMoneyAccount = null;
	private HyjSelectorField mSelectorFieldProject = null;
	private HyjNumericField mNumericExchangeRate = null;
	private HyjSelectorField mSelectorFieldMoneyExpenseCategory = null;
	private HyjSelectorField mSelectorFieldFriend = null;
	private ImageView mImageViewClearFriend = null;
	private HyjRemarkField mRemarkFieldRemark = null;
	private ImageView mImageViewRefreshRate = null;
	private View mViewSeparatorExchange = null;
	private LinearLayout mLinearLayoutExchangeRate = null;
	
	private boolean hasEditPermission = true;

	@Override
	public Integer useContentView() {
		return R.layout.money_formfragment_moneyexpense;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		final MoneyExpense moneyExpense;

		Intent intent = getActivity().getIntent();
		final long modelId = intent.getLongExtra("MODEL_ID", -1);
		if (modelId != -1) {
			moneyExpense = new Select().from(MoneyExpense.class).where("_id=?", modelId).executeSingle();
			hasEditPermission = moneyExpense.hasEditPermission();
		} else {
			moneyExpense = new MoneyExpense();
			if(intent.getStringExtra("counterpartId") != null){
				moneyExpense.setMoneyIncomeId(intent.getStringExtra("counterpartId"));
			}
		}
				
//		mMoneyExpenseEditor = moneyExpense.newModelEditor();
		mMoneyExpenseEditor = new MoneyExpenseEditor(moneyExpense);

		setupDeleteButton(mMoneyExpenseEditor);

		mImageFieldPicture = (HyjImageField) getView().findViewById(
				R.id.moneyExpenseFormFragment_imageField_picture);
		mImageFieldPicture.setImages(moneyExpense.getPictures());
				
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyExpenseFormFragment_textField_date);
		if (modelId != -1) {
			mDateTimeFieldDate.setText(moneyExpense.getDate());
		}
		
		mNumericAmount = (HyjNumericField) getView().findViewById(R.id.moneyExpenseFormFragment_textField_amount);
		double amount = intent.getDoubleExtra("amount", -1.0);//从分享消息导入的金额
		if(amount >= 0.0){
			mNumericAmount.setNumber(amount);
		}else{
			mNumericAmount.setNumber(moneyExpense.getAmount());
		}
		if(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor() != null){
			mNumericAmount.getEditText().setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			mNumericAmount.getEditText().setHintTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
		}
		
		MoneyAccount moneyAccount = moneyExpense.getMoneyAccount();
		mSelectorFieldMoneyAccount = (HyjSelectorField) getView().findViewById(
				R.id.moneyExpenseFormFragment_selectorField_moneyAccount);

		if (moneyAccount != null) {
			mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
			mSelectorFieldMoneyAccount.setText(moneyAccount.getName() + "("
					+ moneyAccount.getCurrencyId() + ")");
		}
		mSelectorFieldMoneyAccount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("excludeType", "Debt");
				
				MoneyExpenseFormFragment.this
						.openActivityWithFragmentForResult(
								MoneyAccountListFragment.class,
								R.string.moneyAccountListFragment_title_select_moneyAccount,
								bundle, GET_MONEYACCOUNT_ID);
			}
		});

		Project project;
		String projectId = intent.getStringExtra("projectId");//从消息导入
		if(projectId != null){
			project = HyjModel.getModel(Project.class, projectId);
		}else{
			project = moneyExpense.getProject();
		}
		
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(
				R.id.moneyExpenseFormFragment_selectorField_project);

		if (project != null) {
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getDisplayName() + "("
					+ project.getCurrencyId() + ")");
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneyExpenseFormFragment.this
						.openActivityWithFragmentForResult(
								ProjectListFragment.class,
								R.string.projectListFragment_title_select_project,
								null, GET_PROJECT_ID);
			}
		});

		mNumericExchangeRate = (HyjNumericField) getView().findViewById(
				R.id.moneyExpenseFormFragment_textField_exchangeRate);
		mNumericExchangeRate.setNumber(moneyExpense.getExchangeRate());

		mViewSeparatorExchange = (View) getView().findViewById(
				R.id.moneyExpenseFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(
				R.id.moneyExpenseFormFragment_linearLayout_exchangeRate);

		mSelectorFieldMoneyExpenseCategory = (HyjSelectorField) getView().findViewById(
				R.id.moneyExpenseFormFragment_textField_moneyExpenseCategory);
		mSelectorFieldMoneyExpenseCategory.setText(moneyExpense
				.getMoneyExpenseCategory());
		if(moneyExpense.getMoneyExpenseCategoryMain() != null && moneyExpense.getMoneyExpenseCategoryMain().length() > 0){
			mSelectorFieldMoneyExpenseCategory.setLabel(moneyExpense.getMoneyExpenseCategoryMain());
		}
		mSelectorFieldMoneyExpenseCategory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneyExpenseFormFragment.this
						.openActivityWithFragmentForResult(
								MoneyExpenseCategoryListFragment.class,
								R.string.moneyExpenseFormFragment_editText_hint_moneyExpenseCategory,
								null, GET_CATEGORY_ID);
			}
		});

		Friend friend;
		String friendUserId = intent.getStringExtra("friendUserId");//从消息导入
		if(friendUserId != null){
			friend = new Select().from(Friend.class).where("friendUserId=?",friendUserId).executeSingle();
		}else{
			friend = moneyExpense.getFriend();
		}
		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.moneyExpenseFormFragment_selectorField_friend);

		if (friend != null) {
			mSelectorFieldFriend.setModelId(friend.getId());
			mSelectorFieldFriend.setText(friend.getDisplayName());
		}
		
		
		
		mSelectorFieldFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneyExpenseFormFragment.this
						.openActivityWithFragmentForResult(
								FriendListFragment.class,
								R.string.friendListFragment_title_select_friend_payee,
								null, GET_FRIEND_ID);
			}
		});
		
		mImageViewClearFriend = (ImageView) getView().findViewById(
				R.id.moneyExpenseFormFragment_imageView_clear_friend);
		mImageViewClearFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSelectorFieldFriend.setModelId(null);
				mSelectorFieldFriend.setText("");
			}
		});
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(
				R.id.moneyExpenseFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyExpense.getRemark());

		ImageView takePictureButton = (ImageView) getView().findViewById(R.id.moneyExpenseFormFragment_imageView_camera);
		takePictureButton.setOnClickListener(new OnClickListener() {
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

		mImageViewRefreshRate = (ImageView) getView().findViewById(
				R.id.moneyExpenseFormFragment_imageButton_refresh_exchangeRate);
		mImageViewRefreshRate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mSelectorFieldMoneyAccount.getModelId() != null && mSelectorFieldProject.getModelId() != null) {
					MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mSelectorFieldMoneyAccount.getModelId());
					Project project = HyjModel.getModel(Project.class, mSelectorFieldProject.getModelId());

					String fromCurrency = moneyAccount.getCurrencyId();
					String toCurrency = project.getCurrencyId();
					if (fromCurrency != null && toCurrency != null) {
						HyjUtil.startRoateView(mImageViewRefreshRate);
						mImageViewRefreshRate.setEnabled(false);
						HyjUtil.updateExchangeRate(fromCurrency, toCurrency, mImageViewRefreshRate, mNumericExchangeRate);
					} else {
						HyjUtil.displayToast(R.string.moneyExpenseFormFragment_toast_select_currency);
					}
				} else {
					HyjUtil.displayToast(R.string.moneyExpenseFormFragment_toast_select_currency);
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
	    if(mMoneyExpenseEditor!= null && mMoneyExpenseEditor.getModelCopy().get_mId() != null && !hasEditPermission){
	    	hideSaveAction();
	    }
	}
	
	private void setupDeleteButton(HyjModelEditor<MoneyExpense> moneyExpenseEditor) {

		Button buttonDelete = (Button) getView().findViewById(R.id.button_delete);
		
		final MoneyExpense moneyExpense = moneyExpenseEditor.getModelCopy();
		
		if (moneyExpense.get_mId() == null) {
			buttonDelete.setVisibility(View.GONE);
		} else {
			buttonDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(moneyExpense.hasDeletePermission()){
					((HyjActivity)getActivity()).displayDialog(R.string.app_action_delete_list_item, R.string.app_confirm_delete, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
							new DialogCallbackListener() {
								@Override
								public void doPositiveClick(Object object) {
									try {
										ActiveAndroid.beginTransaction();

										MoneyAccount moneyAccount = moneyExpense.getMoneyAccount();
										HyjModelEditor<MoneyAccount> moneyAccountEditor = moneyAccount.newModelEditor();
										moneyAccountEditor.getModelCopy().setCurrentBalance(moneyAccount.getCurrentBalance() + moneyExpense.getAmount());
										MoneyExpenseEditor moneyExpenseEditor = new MoneyExpenseEditor(moneyExpense);
										
										//更新支出所有者的实际支出
										MoneyExpense oldMoneyExpenseModel = moneyExpenseEditor.getModelCopy();
										ProjectShareAuthorization oldSelfProjectAuthorization = moneyExpenseEditor.getOldSelfProjectShareAuthorization();
										HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
										oldSelfProjectAuthorizationEditor.getModelCopy().setActualTotalExpense(oldSelfProjectAuthorization.getActualTotalExpense() - oldMoneyExpenseModel.getAmount0()*oldMoneyExpenseModel.getExchangeRate());
										oldSelfProjectAuthorizationEditor.save();
									
										moneyExpense.delete();
										moneyAccountEditor.save();

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

		if(mMoneyExpenseEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			mDateTimeFieldDate.setEnabled(false);
			
			mNumericAmount.setNumber(mMoneyExpenseEditor.getModel().getLocalAmount());
			mNumericAmount.setEnabled(false);
			
			mSelectorFieldMoneyExpenseCategory.setEnabled(false);

			mSelectorFieldFriend.setEnabled(false);
			
			mSelectorFieldMoneyAccount.setEnabled(false);
			mSelectorFieldMoneyAccount.setVisibility(View.GONE);

			mSelectorFieldProject.setEnabled(false);
			
			mNumericExchangeRate.setEnabled(false);

			mRemarkFieldRemark.setEnabled(false);

			if(this.mOptionsMenu != null){
		    	hideSaveAction();
			}
			
			getView().findViewById(R.id.moneyExpenseFormFragment_imageButton_apportion_add).setEnabled(false);
			getView().findViewById(R.id.moneyExpenseFormFragment_imageButton_apportion_add_all).setEnabled(false);
//			getView().findViewById(R.id.button_save).setEnabled(false);	
			getView().findViewById(R.id.button_delete).setEnabled(false);	
			getView().findViewById(R.id.button_delete).setVisibility(View.GONE);
		}
	}

	private void setExchangeRate(Boolean editInit) {
		if (mSelectorFieldMoneyAccount.getModelId() != null
				&& mSelectorFieldProject.getModelId() != null) {
			MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class,
					mSelectorFieldMoneyAccount.getModelId());
			Project project = HyjModel.getModel(Project.class,
					mSelectorFieldProject.getModelId());

			String fromCurrency = moneyAccount.getCurrencyId();
			String toCurrency = project.getCurrencyId();

			if (fromCurrency.equals(toCurrency)) {
				if (SET_EXCHANGE_RATE_FLAG != 1) {// 新增或修改打开时不做setNumber
					mNumericExchangeRate.setNumber(1.00);
					CREATE_EXCHANGE = 0;
				}
				mViewSeparatorExchange.setVisibility(View.GONE);
				mLinearLayoutExchangeRate.setVisibility(View.GONE);
			} else {
				mViewSeparatorExchange.setVisibility(View.VISIBLE);
				mLinearLayoutExchangeRate.setVisibility(View.VISIBLE);

				if(!editInit){//修改时init不需要set Rate
					Double rate = Exchange.getExchangeRate(fromCurrency,
							toCurrency);
					if (rate != null) {
						mNumericExchangeRate.setNumber(rate);
						CREATE_EXCHANGE = 0;
					} else {
						mNumericExchangeRate.setText(null);
						CREATE_EXCHANGE = 1;
					}
				}
			}

		} else {
			mViewSeparatorExchange.setVisibility(View.GONE);
			mLinearLayoutExchangeRate.setVisibility(View.GONE);
		}
		SET_EXCHANGE_RATE_FLAG = 0;
	}

	private void fillData() {
		MoneyExpense modelCopy = (MoneyExpense) mMoneyExpenseEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setAmount(mNumericAmount.getNumber());
		modelCopy.setMoneyAccountId(mSelectorFieldMoneyAccount.getModelId());
		modelCopy.setProjectId(mSelectorFieldProject.getModelId());
		modelCopy.setExchangeRate(mNumericExchangeRate.getNumber());
		modelCopy.setMoneyExpenseCategory(mSelectorFieldMoneyExpenseCategory.getText());
		modelCopy.setMoneyExpenseCategoryMain(mSelectorFieldMoneyExpenseCategory.getLabel());
		
		if(mSelectorFieldFriend.getModelId() != null){
			Friend friend = HyjModel.getModel(Friend.class, mSelectorFieldFriend.getModelId());
			modelCopy.setFriend(friend);
		}else{
			modelCopy.setFriend(null);
		}

		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
	}

	private void showValidatioErrors() {
		HyjUtil.displayToast(R.string.app_validation_error);
		mDateTimeFieldDate.setError(mMoneyExpenseEditor
				.getValidationError("datetime"));
		mNumericAmount.setError(mMoneyExpenseEditor
				.getValidationError("amount"));
		if(mMoneyExpenseEditor
		.getValidationError("amount") != null){
			mNumericAmount.showSoftKeyboard();
		}
		mSelectorFieldMoneyAccount.setError(mMoneyExpenseEditor
				.getValidationError("moneyAccount"));
		mSelectorFieldProject.setError(mMoneyExpenseEditor
				.getValidationError("project"));
		mNumericExchangeRate.setError(mMoneyExpenseEditor
				.getValidationError("exchangeRate"));
		mSelectorFieldMoneyExpenseCategory.setError(mMoneyExpenseEditor
				.getValidationError("moneyExpenseCategory"));
		mSelectorFieldFriend.setError(mMoneyExpenseEditor
				.getValidationError("friend"));
		mRemarkFieldRemark.setError(mMoneyExpenseEditor
				.getValidationError("remark"));
	}

	@Override
	public void onSave(View v) {
		super.onSave(v);

		fillData();

		if(mMoneyExpenseEditor.getModelCopy().get_mId() == null && !mMoneyExpenseEditor.getModelCopy().hasAddNewPermission(mMoneyExpenseEditor.getModelCopy().getProjectId())){
			HyjUtil.displayToast(R.string.app_permission_no_addnew);
		}else if(mMoneyExpenseEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			HyjUtil.displayToast(R.string.app_permission_no_edit);
		}else{
			mMoneyExpenseEditor.validate();
			
			if (mMoneyExpenseEditor.hasValidationErrors()) {
				showValidatioErrors();
			} else {
				try {
					ActiveAndroid.beginTransaction();

					savePictures();

					MoneyExpense oldMoneyExpenseModel = mMoneyExpenseEditor.getModel();
					MoneyExpense moneyExpenseModel = mMoneyExpenseEditor.getModelCopy();
					
					//设置默认项目和账户
					UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
					if(moneyExpenseModel.get_mId() == null && !userData.getActiveMoneyAccountId().equals(moneyExpenseModel.getMoneyAccountId()) || !userData.getActiveProjectId().equals(moneyExpenseModel.getProjectId())){
						HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
						userDataEditor.getModelCopy().setActiveMoneyAccountId(moneyExpenseModel.getMoneyAccountId());
						userDataEditor.getModelCopy().setActiveProjectId(moneyExpenseModel.getProjectId());
						userDataEditor.save();
					}
					
					// 更新项目的默认分类
					if(moneyExpenseModel.get_mId() == null){
						HyjModelEditor<Project> projectEditor = moneyExpenseModel.getProject().newModelEditor();
						projectEditor.getModelCopy().setDefaultExpenseCategory(moneyExpenseModel.getMoneyExpenseCategory());
						projectEditor.getModelCopy().setDefaultExpenseCategoryMain(moneyExpenseModel.getMoneyExpenseCategoryMain());
						projectEditor.save();
					}
					
					//当前汇率不存在时，创建汇率
					String localCurrencyId = moneyExpenseModel.getMoneyAccount().getCurrencyId();
					String foreignCurrencyId = moneyExpenseModel.getProject().getCurrencyId();
					if(CREATE_EXCHANGE == 1){
						MoneyAccount moneyAccount = moneyExpenseModel.getMoneyAccount();
						Project project = moneyExpenseModel.getProject();
						
						Exchange newExchange = new Exchange();
						newExchange.setLocalCurrencyId(moneyAccount.getCurrencyId());
						newExchange.setForeignCurrencyId(project.getCurrencyId());
						newExchange.setRate(moneyExpenseModel.getExchangeRate());
						newExchange.save();
					}else if(!localCurrencyId.equalsIgnoreCase(foreignCurrencyId)){
						Exchange exchange = Exchange.getExchange(localCurrencyId, foreignCurrencyId);
						Double rate = HyjUtil.toFixed2(moneyExpenseModel.getExchangeRate());
						if(exchange != null){
							if(exchange.getRate() != rate){
								HyjModelEditor<Exchange> exchangModelEditor = exchange.newModelEditor();
								exchangModelEditor.getModelCopy().setRate(rate);
								exchangModelEditor.save();
							}
						}
//						else{
//							exchange = Exchange.getExchange(localCurrencyId, foreignCurrencyId);
//							if(exchange.getRate() != 1/rate){
//								HyjModelEditor<Exchange> exchangModelEditor = exchange.newModelEditor();
//								exchangModelEditor.getModelCopy().setRate(1/rate);
//								exchangModelEditor.save();
//							}
//						}
					}
					
					    MoneyAccount oldMoneyAccount = oldMoneyExpenseModel.getMoneyAccount();
						MoneyAccount newMoneyAccount = moneyExpenseModel.getMoneyAccount();
						HyjModelEditor<MoneyAccount> newMoneyAccountEditor = newMoneyAccount.newModelEditor();
						
						//更新账户余额
						if(moneyExpenseModel.get_mId() == null || oldMoneyAccount.getId().equals(newMoneyAccount.getId())){
							newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() + oldMoneyExpenseModel.getAmount0() - moneyExpenseModel.getAmount0());
						}else{
							HyjModelEditor<MoneyAccount> oldMoneyAccountEditor = oldMoneyAccount.newModelEditor();
							oldMoneyAccountEditor.getModelCopy().setCurrentBalance(oldMoneyAccount.getCurrentBalance() + oldMoneyExpenseModel.getAmount0());
							newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() - moneyExpenseModel.getAmount0());
							oldMoneyAccountEditor.save();
						}
						newMoneyAccountEditor.save();
						
						
						//更新支出所有者的实际支出
						ProjectShareAuthorization selfProjectAuthorization = mMoneyExpenseEditor.getNewSelfProjectShareAuthorization();
						HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = selfProjectAuthorization.newModelEditor();
					    
						if(moneyExpenseModel.get_mId() == null || oldMoneyExpenseModel.getProjectId().equals(moneyExpenseModel.getProjectId())){
						    // 无旧项目可更新
							selfProjectAuthorizationEditor.getModelCopy().setActualTotalExpense(selfProjectAuthorization.getActualTotalExpense() - oldMoneyExpenseModel.getAmount0()*oldMoneyExpenseModel.getExchangeRate() + moneyExpenseModel.getAmount0()*moneyExpenseModel.getExchangeRate());
							
						} else {
							selfProjectAuthorizationEditor.getModelCopy().setActualTotalExpense(selfProjectAuthorization.getActualTotalExpense() + moneyExpenseModel.getAmount0()*moneyExpenseModel.getExchangeRate());
								
							ProjectShareAuthorization oldSelfProjectAuthorization = mMoneyExpenseEditor.getOldSelfProjectShareAuthorization();
							HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
							oldSelfProjectAuthorizationEditor.getModelCopy().setActualTotalExpense(oldSelfProjectAuthorization.getActualTotalExpense() - oldMoneyExpenseModel.getAmount0()*oldMoneyExpenseModel.getExchangeRate());
							oldSelfProjectAuthorizationEditor.save();
						
						}
						selfProjectAuthorizationEditor.save();
					
					//更新分类，使之成为最近使用过的
					if(this.mSelectorFieldMoneyExpenseCategory.getModelId() != null){
						MoneyExpenseCategory category = HyjModel.getModel(MoneyExpenseCategory.class, this.mSelectorFieldMoneyExpenseCategory.getModelId());
						if(category != null){
							category.newModelEditor().save();
						}
					}
					mMoneyExpenseEditor.save();
					ActiveAndroid.setTransactionSuccessful();
					HyjUtil.displayToast(R.string.app_save_success);
					getActivity().finish();
				} finally {
					ActiveAndroid.endTransaction();
				}
			}
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
				newPic.setRecordId(mMoneyExpenseEditor.getModel().getId());
				newPic.setRecordType("MoneyExpense");
				newPic.save();
			} else if (pi.getState() == PictureItem.DELETED) {
				pi.getPicture().delete();
			} else if (pi.getState() == PictureItem.CHANGED) {

			}
			if (!mainPicSet && pi.getPicture() != null) {
				mainPicSet = true;
				mMoneyExpenseEditor.getModelCopy().setPicture(pi.getPicture());
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case GET_MONEYACCOUNT_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				MoneyAccount moneyAccount = MoneyAccount.load(
						MoneyAccount.class, _id);
				mSelectorFieldMoneyAccount.setText(moneyAccount.getName() + "("
						+ moneyAccount.getCurrencyId() + ")");
				mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
				setExchangeRate(false);
			}
			break;
		case GET_PROJECT_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				Project project = Project.load(Project.class, _id);
				ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("projectId = ? AND friendUserId=?", project.getId(), HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
				
				if(mMoneyExpenseEditor.getModelCopy().get_mId() == null && !psa.getProjectShareMoneyExpenseAddNew()){
					HyjUtil.displayToast(R.string.app_permission_no_addnew);
					return;
				}else if(mMoneyExpenseEditor.getModelCopy().get_mId() != null && !psa.getProjectShareMoneyExpenseEdit()){
					HyjUtil.displayToast(R.string.app_permission_no_edit);
					return;
				}
				
				mSelectorFieldProject.setText(project.getDisplayName() + "(" + project.getCurrencyId() + ")");
				mSelectorFieldProject.setModelId(project.getId());
				setExchangeRate(false);
			}
			break;

		case GET_FRIEND_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				Friend friend = Friend.load(Friend.class, _id);
				
				if(friend.getFriendUserId() != null && friend.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
					HyjUtil.displayToast(R.string.moneyExpenseFormFragment_editText_error_friend);
					return;
				}
				mSelectorFieldFriend.setText(friend.getDisplayName());
				mSelectorFieldFriend.setModelId(friend.getId());
			}
			break;
			
		case GET_CATEGORY_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				MoneyExpenseCategory category = MoneyExpenseCategory.load(MoneyExpenseCategory.class, _id);
				mSelectorFieldMoneyExpenseCategory.setText(category.getName());
				mSelectorFieldMoneyExpenseCategory.setModelId(category.getId());
				if(category.getParentExpenseCategory() != null){
					mSelectorFieldMoneyExpenseCategory.setLabel(category.getParentExpenseCategory().getName());
				} else {
					mSelectorFieldMoneyExpenseCategory.setLabel(null);
				}
			}
			break;
		}
		
	}
	
	// inner class
	private static class MoneyExpenseEditor extends HyjModelEditor<MoneyExpense> {
		private  ProjectShareAuthorization mOldProjectShareAuthorization;
		private  ProjectShareAuthorization mNewProjectShareAuthorization;
		
		public MoneyExpenseEditor(MoneyExpense model) {
			super(model);
		}
		
		public ProjectShareAuthorization getOldSelfProjectShareAuthorization(){
			if(mOldProjectShareAuthorization == null){
				return new Select().from(ProjectShareAuthorization.class).where("friendUserId=? AND projectId=?", this.getModel().getOwnerUserId(), this.getModel().getProjectId()).executeSingle();
			}
			return mOldProjectShareAuthorization;
		}
		
		public ProjectShareAuthorization getNewSelfProjectShareAuthorization(){
			if(mNewProjectShareAuthorization == null){
				return new Select().from(ProjectShareAuthorization.class).where("friendUserId=? AND projectId=?", HyjApplication.getInstance().getCurrentUser().getId(), this.getModelCopy().getProjectId()).executeSingle();
			}
			return mNewProjectShareAuthorization;
		}
		
		
		
	}
}
