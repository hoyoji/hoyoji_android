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
import com.hoyoji.hoyoji.models.MoneyBorrow;
import com.hoyoji.hoyoji.models.MoneyExpense;
import com.hoyoji.hoyoji.models.MoneyExpenseApportion;
import com.hoyoji.hoyoji.models.MoneyExpenseCategory;
import com.hoyoji.hoyoji.models.MoneyExpenseContainer;
import com.hoyoji.hoyoji.models.MoneyLend;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.MoneyApportionField.ApportionItem;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.money.moneycategory.MoneyExpenseCategoryListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;

public class MoneyExpenseContainerFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_FRIEND_ID = 3;
	private final static int GET_APPORTION_MEMBER_ID = 4;
	private final static int GET_CATEGORY_ID = 5;
	
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;

	private MoneyExpenseContainerEditor mMoneyExpenseContainerEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private MoneyApportionField mApportionFieldApportions = null;
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
		final MoneyExpenseContainer moneyExpenseContainer;

		Intent intent = getActivity().getIntent();
		final long modelId = intent.getLongExtra("MODEL_ID", -1);
		if (modelId != -1) {
			moneyExpenseContainer = new Select().from(MoneyExpenseContainer.class).where("_id=?", modelId).executeSingle();
			hasEditPermission = moneyExpenseContainer.hasEditPermission();
		} else {
			moneyExpenseContainer = new MoneyExpenseContainer();
			final String moneyAccountId = intent.getStringExtra("moneyAccountId");
			if(moneyAccountId != null){
				moneyExpenseContainer.setMoneyAccountId(moneyAccountId);
			}
			if(intent.getStringExtra("counterpartId") != null){
				moneyExpenseContainer.setMoneyIncomeId(intent.getStringExtra("counterpartId"));
			}
		}
				
		mMoneyExpenseContainerEditor = new MoneyExpenseContainerEditor(moneyExpenseContainer);

		setupDeleteButton(mMoneyExpenseContainerEditor);

		mImageFieldPicture = (HyjImageField) getView().findViewById(
				R.id.moneyExpenseFormFragment_imageField_picture);
		mImageFieldPicture.setImages(moneyExpenseContainer.getPictures());
				
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyExpenseFormFragment_textField_date);
		if (modelId != -1) {
			mDateTimeFieldDate.setText(moneyExpenseContainer.getDate());
		}

		setupApportionField(moneyExpenseContainer);
		
		mNumericAmount = (HyjNumericField) getView().findViewById(R.id.moneyExpenseFormFragment_textField_amount);
		double amount = intent.getDoubleExtra("amount", -1.0);//从分享消息导入的金额
		if(amount >= 0.0){
			mNumericAmount.setNumber(amount);
			mApportionFieldApportions.setTotalAmount(amount);
		}else{
			mNumericAmount.setNumber(moneyExpenseContainer.getAmount());
		}
		if(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor() != null){
			mNumericAmount.getEditText().setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			mNumericAmount.getEditText().setHintTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
		}
		
		mNumericAmount.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				mApportionFieldApportions.setTotalAmount(mNumericAmount
						.getNumber());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
		
		MoneyAccount moneyAccount = moneyExpenseContainer.getMoneyAccount();
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
				
				MoneyExpenseContainerFormFragment.this
						.openActivityWithFragmentForResult(
								MoneyAccountListFragment.class,
								R.string.moneyAccountListFragment_title_select_moneyAccount,
								bundle, GET_MONEYACCOUNT_ID);
			}
		});

		Project project;
		String projectId = intent.getStringExtra("projectId");//从消息导入
		if(moneyExpenseContainer.get_mId() == null && projectId != null){
			project = HyjModel.getModel(Project.class, projectId);
		}else{
			project = moneyExpenseContainer.getProject();
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
				MoneyExpenseContainerFormFragment.this
						.openActivityWithFragmentForResult(
								ProjectListFragment.class,
								R.string.projectListFragment_title_select_project,
								null, GET_PROJECT_ID);
			}
		});

		mNumericExchangeRate = (HyjNumericField) getView().findViewById(
				R.id.moneyExpenseFormFragment_textField_exchangeRate);
		mNumericExchangeRate.setNumber(moneyExpenseContainer.getExchangeRate());

		mViewSeparatorExchange = (View) getView().findViewById(
				R.id.moneyExpenseFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(
				R.id.moneyExpenseFormFragment_linearLayout_exchangeRate);

		mSelectorFieldMoneyExpenseCategory = (HyjSelectorField) getView().findViewById(
				R.id.moneyExpenseFormFragment_textField_moneyExpenseCategory);
		mSelectorFieldMoneyExpenseCategory.setText(moneyExpenseContainer
				.getMoneyExpenseCategory());
		if(moneyExpenseContainer.getMoneyExpenseCategoryMain() != null && moneyExpenseContainer.getMoneyExpenseCategoryMain().length() > 0){
			mSelectorFieldMoneyExpenseCategory.setLabel(moneyExpenseContainer.getMoneyExpenseCategoryMain());
		}
		mSelectorFieldMoneyExpenseCategory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneyExpenseContainerFormFragment.this
						.openActivityWithFragmentForResult(
								MoneyExpenseCategoryListFragment.class,
								R.string.moneyExpenseFormFragment_editText_hint_moneyExpenseCategory,
								null, GET_CATEGORY_ID);
			}
		});

		Friend friend;
		if(moneyExpenseContainer.get_mId() == null){
			String friendUserId = intent.getStringExtra("friendUserId");//从消息导入
			if(friendUserId != null){
				friend = new Select().from(Friend.class).where("friendUserId=?",friendUserId).executeSingle();
			} else {
				String localFriendId = intent.getStringExtra("localFriendId");//从消息导入
				if(localFriendId != null){
					friend = HyjModel.getModel(Friend.class, localFriendId);
				} else {
					friend = moneyExpenseContainer.getFriend();
				}
			}
		}else{
			friend = moneyExpenseContainer.getFriend();
		}
		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.moneyExpenseFormFragment_selectorField_friend);

		if (friend != null) {
			mSelectorFieldFriend.setModelId(friend.getId());
			mSelectorFieldFriend.setText(friend.getDisplayName());
		}
		mSelectorFieldFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneyExpenseContainerFormFragment.this
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
		mRemarkFieldRemark.setText(moneyExpenseContainer.getRemark());

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

		getView().findViewById(R.id.moneyExpenseFormFragment_imageButton_apportion_add).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Bundle bundle = new Bundle();
						Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
						bundle.putLong("MODEL_ID", project.get_mId());
						openActivityWithFragmentForResult(SelectApportionMemberListFragment.class, R.string.moneyApportionField_select_apportion_member, bundle, GET_APPORTION_MEMBER_ID);
					}
				});

		getView().findViewById(R.id.moneyExpenseFormFragment_imageButton_apportion_add_all).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addAllProjectMemberIntoApportionsField(moneyExpenseContainer);
			}
		});
		
		getView().findViewById(R.id.moneyExpenseFormFragment_imageButton_apportion_more_actions).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						PopupMenu popup = new PopupMenu(getActivity(), v);
						MenuInflater inflater = popup.getMenuInflater();
						inflater.inflate(R.menu.money_apportionfield_more_actions, popup.getMenu());
						popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem item) {
//								if (item.getItemId() == R.id.moneyApportionField_menu_moreActions_add_non_project_member) {
//									Bundle bundle = new Bundle();
//									Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
//									bundle.putLong("PROJECT_ID", project.get_mId());
//									openActivityWithFragmentForResult(FriendListFragment.class, R.string.moneyApportionField_select_apportion_member, bundle, GET_APPORTION_FRIEND_ID);
//									return true;
//								} else 
								if (item.getItemId() == R.id.moneyApportionField_menu_moreActions_clear) {
									mApportionFieldApportions.clearAll();
									mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
									return true;
								} else if (item.getItemId() == R.id.moneyApportionField_menu_moreActions_all_average) {
									mApportionFieldApportions.setAllApportionAverage();
									mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
									return true;
								} else if (item.getItemId() == R.id.moneyApportionField_menu_moreActions_all_share) {
									mApportionFieldApportions.setAllApportionShare();
									mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
									return true;
								}
								return false;
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
	    if(mMoneyExpenseContainerEditor!= null && mMoneyExpenseContainerEditor.getModelCopy().get_mId() != null && !hasEditPermission){
	    	hideSaveAction();
	    }
	}
	
	private void addAllProjectMemberIntoApportionsField(MoneyExpenseContainer moneyExpenseContainer) {
		Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
		List<ProjectShareAuthorization> projectShareAuthorizations = project.getShareAuthorizations();
		for (int i = 0; i < projectShareAuthorizations.size(); i++) {
			if(!projectShareAuthorizations.get(i).getState().equalsIgnoreCase("Accept")){
				continue;
			}
			MoneyExpenseApportion apportion = new MoneyExpenseApportion();
			apportion.setAmount(0.0);
			apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
			apportion.setMoneyExpenseContainerId(moneyExpenseContainer.getId());

			mApportionFieldApportions.addApportion(apportion, project.getId(), ApportionItem.NEW);
		}
		mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
		
	}
	
	private void setupApportionField(MoneyExpenseContainer moneyExpenseContainer) {

		mApportionFieldApportions = (MoneyApportionField) getView().findViewById(R.id.moneyExpenseFormFragment_apportionField);
		
		List<MoneyExpenseApportion> moneyApportions = null;
		
		if(mMoneyExpenseContainerEditor.getModelCopy().get_mId() == null) {
			
			moneyApportions = new ArrayList<MoneyExpenseApportion>();
			if(moneyExpenseContainer.getProject() != null && moneyExpenseContainer.getProject().getAutoApportion()){
				List<ProjectShareAuthorization> projectShareAuthorizations = moneyExpenseContainer.getProject().getShareAuthorizations();
				for(int i=0; i < projectShareAuthorizations.size(); i++){
					MoneyExpenseApportion apportion = new MoneyExpenseApportion();
					apportion.setAmount(0.0);
					apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
					apportion.setMoneyExpenseContainerId(moneyExpenseContainer.getId());
					apportion.setApportionType("Share");
					
					moneyApportions.add(apportion);
				}
			} else if(moneyExpenseContainer.getProject() != null) {
				MoneyExpenseApportion apportion = new MoneyExpenseApportion();
				apportion.setAmount(moneyExpenseContainer.getAmount0());
				apportion.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
				apportion.setMoneyExpenseContainerId(moneyExpenseContainer.getId());
				apportion.setApportionType("Average");
				moneyApportions.add(apportion);
			}
			
		} else {
			moneyApportions = moneyExpenseContainer.getApportions();
		}
		
		mApportionFieldApportions.init(moneyExpenseContainer.getAmount0(), moneyApportions, moneyExpenseContainer.getProjectId(), moneyExpenseContainer.getId());
	}

	private void setupDeleteButton(MoneyExpenseContainerEditor moneyExpenseContainerEditor) {

		Button buttonDelete = (Button) getView().findViewById(R.id.button_delete);
		
		final MoneyExpenseContainer moneyExpenseContainer = moneyExpenseContainerEditor.getModelCopy();
		
		if (moneyExpenseContainer.get_mId() == null) {
			buttonDelete.setVisibility(View.GONE);
		} else {
			buttonDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(moneyExpenseContainer.hasDeletePermission()){
					((HyjActivity)getActivity()).displayDialog(R.string.app_action_delete_list_item, R.string.app_confirm_delete, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
							new DialogCallbackListener() {
								@Override
								public void doPositiveClick(Object object) {
									try {
										ActiveAndroid.beginTransaction();

										MoneyAccount moneyAccount = moneyExpenseContainer.getMoneyAccount();
										HyjModelEditor<MoneyAccount> moneyAccountEditor = moneyAccount.newModelEditor();
										moneyAccountEditor.getModelCopy().setCurrentBalance(moneyAccount.getCurrentBalance() + moneyExpenseContainer.getAmount());
										MoneyExpenseContainerEditor moneyExpenseContainerEditor = new MoneyExpenseContainerEditor(moneyExpenseContainer);
										
										//更新项目余额
										Project newProject = moneyExpenseContainer.getProject();
										HyjModelEditor<Project> newProjectEditor = newProject.newModelEditor();
										newProjectEditor.getModelCopy().setExpenseTotal(newProject.getExpenseTotal() + moneyExpenseContainer.getAmount0());
										newProjectEditor.save();
										
										//删除支出的同时删除分摊
										Iterator<MoneyExpenseApportion> moneyExpenseApportions = moneyExpenseContainer.getApportions().iterator();
										while (moneyExpenseApportions.hasNext()) {
											MoneyExpenseApportion moneyExpenseAportion = moneyExpenseApportions.next();
											ProjectShareAuthorization oldProjectShareAuthorization;

											if(moneyExpenseAportion.getFriendUserId() == null){
												MoneyLend moneyLend = new Select().from(MoneyLend.class).where("moneyExpenseApportionId=?", moneyExpenseAportion.getId()).executeSingle();
												if(moneyLend != null){
													moneyLend.delete();
													//这里不用维护账户余额，因为该笔借出包括在实际支出了
												}
												// 更新旧项目分摊支出
												oldProjectShareAuthorization = moneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
												HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
												oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalLend(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalLend() - (moneyExpenseAportion.getAmount0() * moneyExpenseAportion.getMoneyExpenseContainer().getExchangeRate()));
												oldProjectShareAuthorizationEditor.save();
											} else {
												if(moneyExpenseAportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
													// 更新旧项目的分摊支出
													oldProjectShareAuthorization = moneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
													HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
													oldProjectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getApportionedTotalExpense() - (moneyExpenseAportion.getAmount0() * moneyExpenseAportion.getMoneyExpenseContainer().getExchangeRate()));
													oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalExpense() - (moneyExpenseAportion.getAmount0() * moneyExpenseAportion.getMoneyExpenseContainer().getExchangeRate()));
													oldProjectShareAuthorizationEditor.save();
													
													MoneyExpense moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", moneyExpenseAportion.getId()).executeSingle();
													if(moneyExpense != null){
														moneyExpense.delete();
													}
												} else {
													// 更新旧项目分摊支出
													oldProjectShareAuthorization = moneyExpenseAportion.getProjectShareAuthorization();
													HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
													
													oldProjectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getApportionedTotalExpense() - (moneyExpenseAportion.getAmount0() * moneyExpenseAportion.getMoneyExpenseContainer().getExchangeRate()));
													oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalExpense() - (moneyExpenseAportion.getAmount0() * moneyExpenseAportion.getMoneyExpenseContainer().getExchangeRate()));
													MoneyExpense moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", moneyExpenseAportion.getId()).executeSingle();
													if(moneyExpense != null){
														moneyExpense.delete();
													} 
												
													oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalBorrow(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalBorrow() - (moneyExpenseAportion.getAmount0() * moneyExpenseAportion.getMoneyExpenseContainer().getExchangeRate()));
													MoneyBorrow moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyExpenseApportionId=?", moneyExpenseAportion.getId()).executeSingle();
													if(moneyBorrow != null){
														moneyBorrow.delete();
													} 
													oldProjectShareAuthorizationEditor.save();
													
													oldProjectShareAuthorization = moneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
													oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
													oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalLend(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalLend() - (moneyExpenseAportion.getAmount0() * moneyExpenseAportion.getMoneyExpenseContainer().getExchangeRate()));
													oldProjectShareAuthorizationEditor.save();
													MoneyLend moneyLend = new Select().from(MoneyLend.class).where("moneyExpenseApportionId=?", moneyExpenseAportion.getId()).executeSingle();
													if(moneyLend != null){
														moneyLend.delete();
													} 
												}
											}
																					
											moneyExpenseAportion.delete();
										}
										/*
										//更新支出所有者的实际支出
										MoneyExpenseContainer oldMoneyExpenseModel = moneyExpenseContainerEditor.getModelCopy();
										ProjectShareAuthorization oldSelfProjectAuthorization = moneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
										HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
										oldSelfProjectAuthorizationEditor.getModelCopy().setActualTotalExpense(oldSelfProjectAuthorization.getActualTotalExpense() - oldMoneyExpenseModel.getAmount0()*oldMoneyExpenseModel.getExchangeRate());
										oldSelfProjectAuthorizationEditor.save();
										*/
										moneyExpenseContainer.delete();
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

		if(mMoneyExpenseContainerEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			mDateTimeFieldDate.setEnabled(false);
			
			mNumericAmount.setNumber(mMoneyExpenseContainerEditor.getModel().getLocalAmount());
			mNumericAmount.setEnabled(false);
			
			mSelectorFieldMoneyExpenseCategory.setEnabled(false);

			mSelectorFieldFriend.setEnabled(false);
			
			mSelectorFieldMoneyAccount.setEnabled(false);
			mSelectorFieldMoneyAccount.setVisibility(View.GONE);

			mSelectorFieldProject.setEnabled(false);
			
			mNumericExchangeRate.setEnabled(false);
			
			mApportionFieldApportions.setEnabled(false);

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
		MoneyExpenseContainer modelCopy = (MoneyExpenseContainer) mMoneyExpenseContainerEditor.getModelCopy();
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
		mDateTimeFieldDate.setError(mMoneyExpenseContainerEditor
				.getValidationError("datetime"));
		mNumericAmount.setError(mMoneyExpenseContainerEditor
				.getValidationError("amount"));
		if(mMoneyExpenseContainerEditor
		.getValidationError("amount") != null){
			mNumericAmount.showSoftKeyboard();
		}
		mSelectorFieldMoneyAccount.setError(mMoneyExpenseContainerEditor
				.getValidationError("moneyAccount"));
		mSelectorFieldProject.setError(mMoneyExpenseContainerEditor
				.getValidationError("project"));
		mNumericExchangeRate.setError(mMoneyExpenseContainerEditor
				.getValidationError("exchangeRate"));
		mSelectorFieldMoneyExpenseCategory.setError(mMoneyExpenseContainerEditor
				.getValidationError("moneyExpenseCategory"));
		mSelectorFieldFriend.setError(mMoneyExpenseContainerEditor
				.getValidationError("friend"));
		mRemarkFieldRemark.setError(mMoneyExpenseContainerEditor
				.getValidationError("remark"));
		mApportionFieldApportions.setError(mMoneyExpenseContainerEditor
				.getValidationError("apportionTotalAmount"));
	}

	@Override
	public void onSave(View v) {
		super.onSave(v);

		fillData();

		if(mMoneyExpenseContainerEditor.getModelCopy().get_mId() == null 
				&& !mMoneyExpenseContainerEditor.getModelCopy().hasAddNewPermission(mMoneyExpenseContainerEditor.getModelCopy().getProjectId())){
			HyjUtil.displayToast(R.string.app_permission_no_addnew);
		}else if(mMoneyExpenseContainerEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			HyjUtil.displayToast(R.string.app_permission_no_edit);
		}else{
			mMoneyExpenseContainerEditor.validate();
			
			if (mApportionFieldApportions.getCount() > 0) {
				if (mNumericAmount.getNumber() != null && !mNumericAmount.getNumber().equals(
						mApportionFieldApportions.getTotalAmount())) {
					mMoneyExpenseContainerEditor.setValidationError("apportionTotalAmount",R.string.moneyApportionField_select_toast_apportion_totalAmount_not_equal);
				} else {
					mMoneyExpenseContainerEditor.removeValidationError("apportionTotalAmount");
				}
			} else {
				mMoneyExpenseContainerEditor.removeValidationError("apportionTotalAmount");
			}

			if (mMoneyExpenseContainerEditor.hasValidationErrors()) {
				showValidatioErrors();
			} else {
				try {
					ActiveAndroid.beginTransaction();

					savePictures();

					saveApportions();

					MoneyExpenseContainer oldMoneyExpenseContainerModel = mMoneyExpenseContainerEditor.getModel();
					MoneyExpenseContainer moneyExpenseContainerModel = mMoneyExpenseContainerEditor.getModelCopy();
					
					//设置默认项目和账户
					UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
					if(moneyExpenseContainerModel.get_mId() == null 
							&& !userData.getActiveMoneyAccountId().equals(moneyExpenseContainerModel.getMoneyAccountId()) 
							|| !userData.getActiveProjectId().equals(moneyExpenseContainerModel.getProjectId())){
						HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
						userDataEditor.getModelCopy().setActiveMoneyAccountId(moneyExpenseContainerModel.getMoneyAccountId());
						userDataEditor.getModelCopy().setActiveProjectId(moneyExpenseContainerModel.getProjectId());
						userDataEditor.save();
					}
					
					// 更新项目的默认分类
					if(moneyExpenseContainerModel.get_mId() == null){
						HyjModelEditor<Project> projectEditor = moneyExpenseContainerModel.getProject().newModelEditor();
						projectEditor.getModelCopy().setDefaultExpenseCategory(moneyExpenseContainerModel.getMoneyExpenseCategory());
						projectEditor.getModelCopy().setDefaultExpenseCategoryMain(moneyExpenseContainerModel.getMoneyExpenseCategoryMain());
						projectEditor.save();
					}
					
					//当前汇率不存在时，创建汇率
					String localCurrencyId = moneyExpenseContainerModel.getMoneyAccount().getCurrencyId();
					String foreignCurrencyId = moneyExpenseContainerModel.getProject().getCurrencyId();
					if(CREATE_EXCHANGE == 1){
						MoneyAccount moneyAccount = moneyExpenseContainerModel.getMoneyAccount();
						Project project = moneyExpenseContainerModel.getProject();
						
						Exchange newExchange = new Exchange();
						newExchange.setLocalCurrencyId(moneyAccount.getCurrencyId());
						newExchange.setForeignCurrencyId(project.getCurrencyId());
						newExchange.setRate(moneyExpenseContainerModel.getExchangeRate());
						newExchange.save();
					}else if(!localCurrencyId.equalsIgnoreCase(foreignCurrencyId)){
						Exchange exchange = Exchange.getExchange(localCurrencyId, foreignCurrencyId);
						Double rate = HyjUtil.toFixed2(moneyExpenseContainerModel.getExchangeRate());
						if(exchange != null){
							if(exchange.getRate() != rate){
								HyjModelEditor<Exchange> exchangModelEditor = exchange.newModelEditor();
								exchangModelEditor.getModelCopy().setRate(rate);
								exchangModelEditor.save();
							}
						}
					}
					
				    MoneyAccount oldMoneyAccount = oldMoneyExpenseContainerModel.getMoneyAccount();
					MoneyAccount newMoneyAccount = moneyExpenseContainerModel.getMoneyAccount();
					HyjModelEditor<MoneyAccount> newMoneyAccountEditor = newMoneyAccount.newModelEditor();
					
					//更新账户余额
					if(moneyExpenseContainerModel.get_mId() == null || oldMoneyAccount.getId().equals(newMoneyAccount.getId())){
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() + oldMoneyExpenseContainerModel.getAmount0() - moneyExpenseContainerModel.getAmount0());
					} else {
						HyjModelEditor<MoneyAccount> oldMoneyAccountEditor = oldMoneyAccount.newModelEditor();
						oldMoneyAccountEditor.getModelCopy().setCurrentBalance(oldMoneyAccount.getCurrentBalance() + oldMoneyExpenseContainerModel.getAmount0());
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() - moneyExpenseContainerModel.getAmount0());
						oldMoneyAccountEditor.save();
					}
					newMoneyAccountEditor.save();
				
				    Project oldProject = oldMoneyExpenseContainerModel.getProject();
					Project newProject = moneyExpenseContainerModel.getProject();
					HyjModelEditor<Project> newProjectEditor = newProject.newModelEditor();
					
					//更新项目余额
					if(moneyExpenseContainerModel.get_mId() == null || oldProject.getId().equals(newProject.getId())){
						newProjectEditor.getModelCopy().setExpenseTotal(newProject.getExpenseTotal() + oldMoneyExpenseContainerModel.getAmount0() - moneyExpenseContainerModel.getAmount0());
					} else {
						HyjModelEditor<Project> oldProjectEditor = oldProject.newModelEditor();
						oldProjectEditor.getModelCopy().setExpenseTotal(oldProject.getExpenseTotal() + oldMoneyExpenseContainerModel.getAmount0());
						newProjectEditor.getModelCopy().setExpenseTotal(newProject.getExpenseTotal() - moneyExpenseContainerModel.getAmount0());
						oldProjectEditor.save();
					}
					newProjectEditor.save();
					
					/*
					//更新支出所有者的实际支出
					ProjectShareAuthorization selfProjectAuthorization = mMoneyExpenseContainerEditor.getNewSelfProjectShareAuthorization();
					HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = selfProjectAuthorization.newModelEditor();
				    
					if(moneyExpenseContainerModel.get_mId() == null || oldMoneyExpenseContainerModel.getProjectId().equals(moneyExpenseContainerModel.getProjectId())){
					    // 无旧项目可更新
						selfProjectAuthorizationEditor.getModelCopy().setActualTotalExpense(selfProjectAuthorization.getActualTotalExpense() - oldMoneyExpenseContainerModel.getAmount0()*oldMoneyExpenseContainerModel.getExchangeRate() + moneyExpenseContainerModel.getAmount0()*moneyExpenseContainerModel.getExchangeRate());
						
					} else {
						selfProjectAuthorizationEditor.getModelCopy().setActualTotalExpense(selfProjectAuthorization.getActualTotalExpense() + moneyExpenseContainerModel.getAmount0()*moneyExpenseContainerModel.getExchangeRate());
							
						ProjectShareAuthorization oldSelfProjectAuthorization = mMoneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
						HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
						oldSelfProjectAuthorizationEditor.getModelCopy().setActualTotalExpense(oldSelfProjectAuthorization.getActualTotalExpense() - oldMoneyExpenseContainerModel.getAmount0()*oldMoneyExpenseContainerModel.getExchangeRate());
						oldSelfProjectAuthorizationEditor.save();
					
					}
					selfProjectAuthorizationEditor.save();
					*/
					
					//更新分类，使之成为最近使用过的
					if(this.mSelectorFieldMoneyExpenseCategory.getModelId() != null){
						MoneyExpenseCategory category = HyjModel.getModel(MoneyExpenseCategory.class, this.mSelectorFieldMoneyExpenseCategory.getModelId());
						if(category != null){
							category.newModelEditor().save();
						}
					}
					mMoneyExpenseContainerEditor.save();
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
				newPic.setRecordId(mMoneyExpenseContainerEditor.getModel().getId());
				newPic.setRecordType("MoneyExpenseContainer");
				newPic.save();
			} else if (pi.getState() == PictureItem.DELETED) {
				pi.getPicture().delete();
			} else if (pi.getState() == PictureItem.CHANGED) {

			}
			if (!mainPicSet && pi.getPicture() != null) {
				mainPicSet = true;
				mMoneyExpenseContainerEditor.getModelCopy().setPicture(pi.getPicture());
			}
		}
	}

	private void saveApportions() {
		MoneyApportionField.ImageGridAdapter adapter = mApportionFieldApportions.getAdapter();
		int count = adapter.getCount();
		int savedCount = 0;
		for (int i = 0; i < count; i++) {
			ApportionItem<MoneyApportion> api = adapter.getItem(i);
			MoneyExpenseApportion apportion = (MoneyExpenseApportion) api.getApportion();
			HyjModelEditor<MoneyExpenseApportion> apportionEditor = apportion.newModelEditor();
			
			if(apportion.getFriendUserId() == null) {
				// 该分摊好友不是项目成员
				if(api.getState() == ApportionItem.DELETED ){
					MoneyLend moneyLend = new Select().from(MoneyLend.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
					if(moneyLend != null){
						moneyLend.delete();
						//这里不用维护账户余额，因为该笔借出包括在实际支出了
					}
					// 更新旧项目分摊支出
					ProjectShareAuthorization oldProjectShareAuthorization = mMoneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
					HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
					oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalLend(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalLend() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
					oldProjectShareAuthorizationEditor.save();
					apportion.delete();
				} else {
					if(api.getState() != ApportionItem.UNCHANGED 
							|| !mMoneyExpenseContainerEditor.getModelCopy().getProjectId().equals(mMoneyExpenseContainerEditor.getModel().getProjectId())
							|| !mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId().equals(mMoneyExpenseContainerEditor.getModel().getMoneyAccountId())) {
						api.saveToCopy(apportionEditor.getModelCopy());
					}
					
					// 该好友是网络好友 或 该好友是本地好友 (非项目成员)，我们要维护借贷账户
					Friend friend = HyjModel.getModel(Friend.class, apportion.getLocalFriendId());
					MoneyAccount debtAccount = MoneyAccount.getDebtAccount(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccount().getCurrencyId(), friend);
					if(api.getState() == ApportionItem.NEW){
		                if(debtAccount == null){
		                	MoneyAccount.createDebtAccount(friend, mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccount().getCurrencyId(), apportionEditor.getModelCopy().getAmount0());
		                }else{
		                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
		                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + apportionEditor.getModelCopy().getAmount0());
		                	debtAccountEditor.save();
		                }
					} else{
						MoneyAccount oldDebtAccount = MoneyAccount.getDebtAccount(mMoneyExpenseContainerEditor.getModel().getMoneyAccount().getCurrencyId(), friend);
						HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
						if(debtAccount == null){
							oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - apportionEditor.getModel().getAmount0());
		                	MoneyAccount.createDebtAccount(friend, mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccount().getCurrencyId(), apportionEditor.getModelCopy().getAmount0());
		                }else if(debtAccount.getId().equals(oldDebtAccount.getId())){
		                	oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - apportionEditor.getModel().getAmount0() + apportionEditor.getModelCopy().getAmount0());
		                }else{
		                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
		                	oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - apportionEditor.getModel().getAmount0());
		                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + apportionEditor.getModelCopy().getAmount0());
		                	debtAccountEditor.save();
		                }
						oldDebtAccountEditor.save();
					}
					
					MoneyLend moneyLend = null;
					if(apportion.get_mId() == null){
						moneyLend = new MoneyLend();
					} else {
						moneyLend = new Select().from(MoneyLend.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
					}
					moneyLend.setMoneyExpenseApportionId(apportionEditor.getModelCopy().getId());
					moneyLend.setAmount(apportionEditor.getModelCopy().getAmount0());
					moneyLend.setDate(mMoneyExpenseContainerEditor.getModelCopy().getDate());
					moneyLend.setRemark(mMoneyExpenseContainerEditor.getModelCopy().getRemark());
					moneyLend.setFriendAccountId(mMoneyExpenseContainerEditor.getModelCopy().getFriendAccountId());
					moneyLend.setFriendUserId(apportionEditor.getModelCopy().getFriendUserId());
					moneyLend.setExchangeRate(mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
					moneyLend.setGeoLat(mMoneyExpenseContainerEditor.getModelCopy().getGeoLat());
					moneyLend.setGeoLon(mMoneyExpenseContainerEditor.getModelCopy().getGeoLon());
					moneyLend.setLocalFriendId(apportionEditor.getModelCopy().getLocalFriendId());
					moneyLend.setMoneyAccountId(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId());
					moneyLend.setLocation(mMoneyExpenseContainerEditor.getModelCopy().getLocation());
					moneyLend.setAddress(mMoneyExpenseContainerEditor.getModelCopy().getAddress());
					moneyLend.setPictureId(mMoneyExpenseContainerEditor.getModelCopy().getPictureId());
					moneyLend.setProjectId(mMoneyExpenseContainerEditor.getModelCopy().getProjectId());
					moneyLend.save();

					if(mMoneyExpenseContainerEditor.getModelCopy().get_mId() == null || 
							mMoneyExpenseContainerEditor.getModel().getProjectId().equals(mMoneyExpenseContainerEditor.getModelCopy().getProjectId())){
						// 更新新项目分摊支出
						ProjectShareAuthorization newProjectShareAuthorization = mMoneyExpenseContainerEditor.getNewSelfProjectShareAuthorization();
						HyjModelEditor<ProjectShareAuthorization> newProjectShareAuthorizationEditor = newProjectShareAuthorization.newModelEditor();
						newProjectShareAuthorizationEditor.getModelCopy().setActualTotalLend(newProjectShareAuthorizationEditor.getModelCopy().getActualTotalLend()  - (apportionEditor.getModel().getAmount0() * mMoneyExpenseContainerEditor.getModel().getExchangeRate()) + (apportionEditor.getModelCopy().getAmount0() * mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate()));
						newProjectShareAuthorizationEditor.save();
					} else {
						ProjectShareAuthorization oldProjectShareAuthorization = mMoneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
						HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
						oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalLend(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalLend() - (apportionEditor.getModel().getAmount0() * mMoneyExpenseContainerEditor.getModel().getExchangeRate()));
						oldProjectShareAuthorizationEditor.save();
						
						// 更新新项目分摊支出
						ProjectShareAuthorization newProjectShareAuthorization = mMoneyExpenseContainerEditor.getNewSelfProjectShareAuthorization();
						HyjModelEditor<ProjectShareAuthorization> newProjectShareAuthorizationEditor = newProjectShareAuthorization.newModelEditor();
						newProjectShareAuthorizationEditor.getModelCopy().setActualTotalLend(newProjectShareAuthorizationEditor.getModelCopy().getActualTotalLend() + (apportionEditor.getModelCopy().getAmount0() * mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate()));
						newProjectShareAuthorizationEditor.save();
					}
						
						
					if(api.getState() != ApportionItem.UNCHANGED
							|| !mMoneyExpenseContainerEditor.getModelCopy().getProjectId().equals(mMoneyExpenseContainerEditor.getModel().getProjectId())
							|| !mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId().equals(mMoneyExpenseContainerEditor.getModel().getMoneyAccountId())) {
						apportionEditor.save();
					}
					savedCount++;
				}
			} else {
					// 该分摊好友是项目成员
					if(api.getState() == ApportionItem.DELETED ){
						
						ProjectShareAuthorization oldProjectShareAuthorization;
						if(apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
							// 更新旧项目的分摊支出
							oldProjectShareAuthorization = mMoneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
							HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
							oldProjectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getApportionedTotalExpense() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
							oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalExpense() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
							oldProjectShareAuthorizationEditor.save();
							
							MoneyExpense moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
							if(moneyExpense != null){
								moneyExpense.delete();
							}
						} else {
							// 更新旧项目分摊支出
							oldProjectShareAuthorization = apportion.getProjectShareAuthorization();
							HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
							
							oldProjectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getApportionedTotalExpense() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
							oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalExpense() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
							MoneyExpense moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
							if(moneyExpense != null){
								moneyExpense.delete();
							} 
						
							oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalBorrow(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalBorrow() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
							MoneyBorrow moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
							if(moneyBorrow != null){
								moneyBorrow.delete();
							} 
							oldProjectShareAuthorizationEditor.save();
							
							oldProjectShareAuthorization = mMoneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
							oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
							oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalLend(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalLend() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
							oldProjectShareAuthorizationEditor.save();
							MoneyLend moneyLend = new Select().from(MoneyLend.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
							if(moneyLend != null){
								moneyLend.delete();
							} 
						}
						apportion.delete();
						
					} else {
						 	if(api.getState() != ApportionItem.UNCHANGED
									|| !mMoneyExpenseContainerEditor.getModelCopy().getProjectId().equals(mMoneyExpenseContainerEditor.getModel().getProjectId())
									|| !mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId().equals(mMoneyExpenseContainerEditor.getModel().getMoneyAccountId())) {
								api.saveToCopy(apportionEditor.getModelCopy());
							}
							Double oldRate = mMoneyExpenseContainerEditor.getModel().getExchangeRate(); 
							Double rate = mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate();
							Double oldApportionAmount = apportionEditor.getModel().getAmount0();
							
							ProjectShareAuthorization projectShareAuthorization;
							//维护项目成员金额
							if(apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
								projectShareAuthorization = mMoneyExpenseContainerEditor.getNewSelfProjectShareAuthorization();
							} else {
								projectShareAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", 
										mMoneyExpenseContainerEditor.getModelCopy().getProjectId(), apportion.getFriendUserId()).executeSingle();
							}
							HyjModelEditor<ProjectShareAuthorization> projectShareAuthorizationEditor = projectShareAuthorization.newModelEditor();
							
							if(mMoneyExpenseContainerEditor.getModelCopy().get_mId() == null || 
									mMoneyExpenseContainerEditor.getModel().getProjectId().equals(mMoneyExpenseContainerEditor.getModelCopy().getProjectId())){
								 // 无旧项目可更新
								projectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(projectShareAuthorization.getApportionedTotalExpense() - (oldApportionAmount * oldRate) + (apportionEditor.getModelCopy().getAmount0() * rate));
								projectShareAuthorizationEditor.getModelCopy().setActualTotalExpense(projectShareAuthorization.getActualTotalExpense() - (oldApportionAmount * oldRate) + (apportionEditor.getModelCopy().getAmount0() * rate));
								if(!apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
									projectShareAuthorizationEditor.getModelCopy().setActualTotalBorrow(projectShareAuthorization.getActualTotalBorrow() - (oldApportionAmount * oldRate) + (apportionEditor.getModelCopy().getAmount0() * rate));
									projectShareAuthorizationEditor.save();
									
									projectShareAuthorization = mMoneyExpenseContainerEditor.getNewSelfProjectShareAuthorization();
									projectShareAuthorizationEditor = projectShareAuthorization.newModelEditor();
									projectShareAuthorizationEditor.getModelCopy().setActualTotalLend(projectShareAuthorization.getActualTotalLend() - (oldApportionAmount * oldRate) + (apportionEditor.getModelCopy().getAmount0() * rate));
									projectShareAuthorizationEditor.save();
								} else {
									projectShareAuthorizationEditor.save();
								}
							} else {
								//更新新项目分摊支出
								projectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(projectShareAuthorization.getApportionedTotalExpense() + (apportionEditor.getModelCopy().getAmount0() * rate));
								projectShareAuthorizationEditor.getModelCopy().setActualTotalExpense(projectShareAuthorization.getActualTotalExpense() + (apportionEditor.getModelCopy().getAmount0() * rate));
								if(!apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
									projectShareAuthorizationEditor.getModelCopy().setActualTotalBorrow(projectShareAuthorization.getActualTotalBorrow() + (apportionEditor.getModelCopy().getAmount0() * rate));
									projectShareAuthorizationEditor.save();
										
									projectShareAuthorization = mMoneyExpenseContainerEditor.getNewSelfProjectShareAuthorization();
									projectShareAuthorizationEditor = projectShareAuthorization.newModelEditor();
									projectShareAuthorizationEditor.getModelCopy().setActualTotalLend(projectShareAuthorization.getActualTotalLend() + (apportionEditor.getModelCopy().getAmount0() * rate));
									projectShareAuthorizationEditor.save();
								} else {
									projectShareAuthorizationEditor.save();
								}
								
								//更新老项目分摊支出
								ProjectShareAuthorization oldProjectAuthorization;

								if(apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
									oldProjectAuthorization = mMoneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
								} else {
									oldProjectAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", 
											mMoneyExpenseContainerEditor.getModel().getProjectId(), apportion.getFriendUserId()).executeSingle();
									
								}
								if(oldProjectAuthorization != null){
									HyjModelEditor<ProjectShareAuthorization> oldProjectAuthorizationEditor = oldProjectAuthorization.newModelEditor();
									oldProjectAuthorizationEditor.getModelCopy().setApportionedTotalExpense(oldProjectAuthorization.getApportionedTotalExpense() - (oldApportionAmount * oldRate));
									oldProjectAuthorizationEditor.getModelCopy().setActualTotalExpense(oldProjectAuthorization.getActualTotalExpense() - (oldApportionAmount * oldRate));
									if(!apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
										oldProjectAuthorizationEditor.getModelCopy().setActualTotalBorrow(oldProjectAuthorization.getActualTotalBorrow() - (oldApportionAmount * oldRate));
										oldProjectAuthorizationEditor.save();

										oldProjectAuthorization = mMoneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
										oldProjectAuthorizationEditor = oldProjectAuthorization.newModelEditor();
										oldProjectAuthorizationEditor.getModelCopy().setActualTotalLend(oldProjectAuthorization.getActualTotalLend() - (oldApportionAmount * oldRate));
										oldProjectAuthorizationEditor.save();
									} else {
										oldProjectAuthorizationEditor.save();
									}
								}
							}
							
							//更新相关好友的借贷账户
							if(!apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
								MoneyAccount debtAccount = MoneyAccount.getDebtAccount(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccount().getCurrencyId(), apportionEditor.getModelCopy().getFriendUserId());
								if(api.getState() == ApportionItem.NEW){
					                if(debtAccount == null){
					                	MoneyAccount.createDebtAccount(apportionEditor.getModelCopy().getFriendUserId(), mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccount().getCurrencyId(), apportionEditor.getModelCopy().getAmount0());
					                }else{
					                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
					                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + apportionEditor.getModelCopy().getAmount0());
					                	debtAccountEditor.save();
					                }
								} else{
									MoneyAccount oldDebtAccount = MoneyAccount.getDebtAccount(mMoneyExpenseContainerEditor.getModel().getMoneyAccount().getCurrencyId(), apportionEditor.getModelCopy().getFriendUserId());
									HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
									if(debtAccount == null){
										oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - apportionEditor.getModel().getAmount0());
					                	MoneyAccount.createDebtAccount(apportionEditor.getModelCopy().getFriendUserId(), mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccount().getCurrencyId(), apportionEditor.getModelCopy().getAmount0());
					                }else if(debtAccount.getId().equals(oldDebtAccount.getId())){
					                	oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - apportionEditor.getModel().getAmount0() + apportionEditor.getModelCopy().getAmount0());
					                }else{
					                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
					                	oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - apportionEditor.getModel().getAmount0());
					                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + apportionEditor.getModelCopy().getAmount0());
					                	debtAccountEditor.save();
					                }
									oldDebtAccountEditor.save();
							    }
						    }
							
							if(apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
								MoneyExpense moneyExpense = null;
								if(apportion.get_mId() == null){
									moneyExpense = new MoneyExpense();
								} else {
									moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
								}
								moneyExpense.setMoneyExpenseApportionId(apportionEditor.getModelCopy().getId());
								moneyExpense.setAmount(apportionEditor.getModelCopy().getAmount0());
								moneyExpense.setDate(mMoneyExpenseContainerEditor.getModelCopy().getDate());
								moneyExpense.setRemark(mMoneyExpenseContainerEditor.getModelCopy().getRemark());
								moneyExpense.setFriendAccountId(mMoneyExpenseContainerEditor.getModelCopy().getFriendAccountId());
								moneyExpense.setFriendUserId(mMoneyExpenseContainerEditor.getModelCopy().getFriendUserId());
								moneyExpense.setExchangeRate(mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
								moneyExpense.setGeoLat(mMoneyExpenseContainerEditor.getModelCopy().getGeoLat());
								moneyExpense.setGeoLon(mMoneyExpenseContainerEditor.getModelCopy().getGeoLon());
								moneyExpense.setLocalFriendId(mMoneyExpenseContainerEditor.getModelCopy().getLocalFriendId());
								moneyExpense.setMoneyAccountId(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId());
								moneyExpense.setLocation(mMoneyExpenseContainerEditor.getModelCopy().getLocation());
								moneyExpense.setAddress(mMoneyExpenseContainerEditor.getModelCopy().getAddress());
								moneyExpense.setMoneyExpenseCategory(mMoneyExpenseContainerEditor.getModelCopy().getMoneyExpenseCategory());
								moneyExpense.setMoneyExpenseCategoryMain(mMoneyExpenseContainerEditor.getModelCopy().getMoneyExpenseCategoryMain());
								moneyExpense.setPictureId(mMoneyExpenseContainerEditor.getModelCopy().getPictureId());
								moneyExpense.setProjectId(mMoneyExpenseContainerEditor.getModelCopy().getProjectId());
								moneyExpense.save();
							} else {
								MoneyLend moneyLend = null;
								if(apportion.get_mId() == null){
									moneyLend = new MoneyLend();
								} else {
									moneyLend = new Select().from(MoneyLend.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
								}
								moneyLend.setMoneyExpenseApportionId(apportionEditor.getModelCopy().getId());
								moneyLend.setAmount(apportionEditor.getModelCopy().getAmount0());
								moneyLend.setDate(mMoneyExpenseContainerEditor.getModelCopy().getDate());
								moneyLend.setRemark(mMoneyExpenseContainerEditor.getModelCopy().getRemark());
								moneyLend.setFriendAccountId(mMoneyExpenseContainerEditor.getModelCopy().getFriendAccountId());
								moneyLend.setFriendUserId(apportionEditor.getModelCopy().getFriendUserId());
								moneyLend.setExchangeRate(mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
								moneyLend.setGeoLat(mMoneyExpenseContainerEditor.getModelCopy().getGeoLat());
								moneyLend.setGeoLon(mMoneyExpenseContainerEditor.getModelCopy().getGeoLon());
								moneyLend.setLocalFriendId(apportionEditor.getModelCopy().getLocalFriendId());
								moneyLend.setMoneyAccountId(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId());
								moneyLend.setLocation(mMoneyExpenseContainerEditor.getModelCopy().getLocation());
								moneyLend.setAddress(mMoneyExpenseContainerEditor.getModelCopy().getAddress());
								moneyLend.setPictureId(mMoneyExpenseContainerEditor.getModelCopy().getPictureId());
								moneyLend.setProjectId(mMoneyExpenseContainerEditor.getModelCopy().getProjectId());
								moneyLend.save();
								

								MoneyBorrow moneyBorrow = null;
								if(apportion.get_mId() == null){
									moneyBorrow = new MoneyBorrow();
								} else {
									moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
								}
								moneyBorrow.setMoneyExpenseApportionId(apportionEditor.getModelCopy().getId());
								moneyBorrow.setAmount(apportionEditor.getModelCopy().getAmount0());
								moneyBorrow.setOwnerUserId(apportionEditor.getModelCopy().getFriendUserId());
								moneyBorrow.setDate(mMoneyExpenseContainerEditor.getModelCopy().getDate());
								moneyBorrow.setRemark(mMoneyExpenseContainerEditor.getModelCopy().getRemark());
								moneyBorrow.setFriendAccountId(mMoneyExpenseContainerEditor.getModelCopy().getFriendAccountId());
								moneyBorrow.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
								moneyBorrow.setExchangeRate(mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
								moneyBorrow.setGeoLat(mMoneyExpenseContainerEditor.getModelCopy().getGeoLat());
								moneyBorrow.setGeoLon(mMoneyExpenseContainerEditor.getModelCopy().getGeoLon());
								moneyBorrow.setLocalFriendId(null);
								moneyBorrow.setMoneyAccountId(null);
								moneyBorrow.setLocation(mMoneyExpenseContainerEditor.getModelCopy().getLocation());
								moneyBorrow.setAddress(mMoneyExpenseContainerEditor.getModelCopy().getAddress());
								moneyBorrow.setPictureId(mMoneyExpenseContainerEditor.getModelCopy().getPictureId());
								moneyBorrow.setProjectId(mMoneyExpenseContainerEditor.getModelCopy().getProjectId());
								moneyBorrow.save();
								
								MoneyExpense moneyExpense = null;
								if(apportion.get_mId() == null){
									moneyExpense = new MoneyExpense();
								} else {
									moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
								}
								moneyExpense.setMoneyExpenseApportionId(apportionEditor.getModelCopy().getId());
								moneyExpense.setAmount(apportionEditor.getModelCopy().getAmount0());
								moneyExpense.setOwnerUserId(apportionEditor.getModelCopy().getFriendUserId());
								moneyExpense.setDate(mMoneyExpenseContainerEditor.getModelCopy().getDate());
								moneyExpense.setRemark(mMoneyExpenseContainerEditor.getModelCopy().getRemark());
								moneyExpense.setFriendAccountId(mMoneyExpenseContainerEditor.getModelCopy().getFriendAccountId());
								moneyExpense.setFriendUserId(mMoneyExpenseContainerEditor.getModelCopy().getFriendUserId());
								moneyExpense.setExchangeRate(mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
								moneyExpense.setGeoLat(mMoneyExpenseContainerEditor.getModelCopy().getGeoLat());
								moneyExpense.setGeoLon(mMoneyExpenseContainerEditor.getModelCopy().getGeoLon());
								moneyExpense.setLocalFriendId(null);
								moneyExpense.setMoneyAccountId(null);
								moneyExpense.setLocation(mMoneyExpenseContainerEditor.getModelCopy().getLocation());
								moneyExpense.setAddress(mMoneyExpenseContainerEditor.getModelCopy().getAddress());
								moneyExpense.setPictureId(mMoneyExpenseContainerEditor.getModelCopy().getPictureId());
								moneyExpense.setProjectId(mMoneyExpenseContainerEditor.getModelCopy().getProjectId());
								moneyExpense.save();

								
							}		
							
							if(api.getState() != ApportionItem.UNCHANGED
										|| !mMoneyExpenseContainerEditor.getModelCopy().getProjectId().equals(mMoneyExpenseContainerEditor.getModel().getProjectId())
										|| !mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId().equals(mMoneyExpenseContainerEditor.getModel().getMoneyAccountId())) {
								apportionEditor.save();
							}
							savedCount++;
				}
			}
		}
		
		// 从隐藏掉的分摊里面删除原来的分摊
		Iterator<ApportionItem<MoneyApportion>> it = mApportionFieldApportions.getHiddenApportions().iterator();
		while (it.hasNext()) {
			// Get element
			ApportionItem<MoneyApportion> item = it.next();
			if (item.getState() != ApportionItem.NEW) {
				MoneyExpenseApportion apportion = ((MoneyExpenseApportion) item.getApportion());
					ProjectShareAuthorization oldProjectShareAuthorization;
					if(apportion.getFriendUserId() != null){
						if(apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
							// 更新旧项目的分摊支出
							oldProjectShareAuthorization = mMoneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
						} else {
							// 更新旧项目分摊支出
							oldProjectShareAuthorization = apportion.getProjectShareAuthorization();
						}
						
						HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
						oldProjectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getApportionedTotalExpense() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
						oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalExpense() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
						if(!apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
							oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalBorrow(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalBorrow() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
							oldProjectShareAuthorizationEditor.save();
					
							oldProjectShareAuthorization = mMoneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
							oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
							oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalLend(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalLend() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
							oldProjectShareAuthorizationEditor.save();
						} else {
							oldProjectShareAuthorizationEditor.save();
						}
					}
					
					if(apportion.getFriendUserId() != null && apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
						MoneyExpense moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
						if(moneyExpense != null){
							moneyExpense.delete();
						}
					} else {
						MoneyLend moneyLend = new Select().from(MoneyLend.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
						if(moneyLend != null){
							moneyLend.delete();
						} 
						if(apportion.getFriendUserId() != null){
							MoneyBorrow moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
							if(moneyBorrow != null){
								moneyBorrow.delete();
							} 
							MoneyExpense moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
							if(moneyExpense != null){
								moneyExpense.delete();
							} 
						}
					}
				apportion.delete();
			}
		}
		
		// 如果列表里一个都没有被保存，我们生成一个默认的分摊
		if (savedCount == 0) {
			MoneyExpenseApportion apportion = new MoneyExpenseApportion();
			apportion.setAmount(mMoneyExpenseContainerEditor.getModelCopy().getAmount0());
			apportion.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
			apportion.setMoneyExpenseContainerId(mMoneyExpenseContainerEditor.getModelCopy().getId());
			apportion.setApportionType("Average");
			
			//更新项目成员的分摊金额
			ProjectShareAuthorization projectShareAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", 
					mMoneyExpenseContainerEditor.getModelCopy().getProjectId(), apportion.getFriendUserId()).executeSingle();
			HyjModelEditor<ProjectShareAuthorization> projectShareAuthorizationEditor = projectShareAuthorization.newModelEditor();
			projectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(projectShareAuthorizationEditor.getModelCopy().getApportionedTotalExpense() + (apportion.getAmount0() * mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate()));
			projectShareAuthorizationEditor.getModelCopy().setActualTotalExpense(projectShareAuthorizationEditor.getModelCopy().getActualTotalExpense() + (apportion.getAmount0() * mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate()));
			projectShareAuthorizationEditor.save();
			
			MoneyExpense moneyExpense = null;
			moneyExpense = new MoneyExpense();
			
			moneyExpense.setMoneyExpenseApportionId(apportion.getId());
			moneyExpense.setAmount(apportion.getAmount0());
			moneyExpense.setDate(mMoneyExpenseContainerEditor.getModelCopy().getDate());
			moneyExpense.setRemark(mMoneyExpenseContainerEditor.getModelCopy().getRemark());
			moneyExpense.setFriendAccountId(mMoneyExpenseContainerEditor.getModelCopy().getFriendAccountId());
			moneyExpense.setFriendUserId(mMoneyExpenseContainerEditor.getModelCopy().getFriendUserId());
			moneyExpense.setExchangeRate(mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
			moneyExpense.setGeoLat(mMoneyExpenseContainerEditor.getModelCopy().getGeoLat());
			moneyExpense.setGeoLon(mMoneyExpenseContainerEditor.getModelCopy().getGeoLon());
			moneyExpense.setLocalFriendId(mMoneyExpenseContainerEditor.getModelCopy().getLocalFriendId());
			moneyExpense.setMoneyAccountId(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId());
			moneyExpense.setLocation(mMoneyExpenseContainerEditor.getModelCopy().getLocation());
			moneyExpense.setAddress(mMoneyExpenseContainerEditor.getModelCopy().getAddress());
			moneyExpense.setMoneyExpenseCategory(mMoneyExpenseContainerEditor.getModelCopy().getMoneyExpenseCategory());
			moneyExpense.setMoneyExpenseCategoryMain(mMoneyExpenseContainerEditor.getModelCopy().getMoneyExpenseCategoryMain());
			moneyExpense.setPictureId(mMoneyExpenseContainerEditor.getModelCopy().getPictureId());
			moneyExpense.setProjectId(mMoneyExpenseContainerEditor.getModelCopy().getProjectId());
			moneyExpense.save();
			apportion.save();
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
				
				if(mMoneyExpenseContainerEditor.getModelCopy().get_mId() == null && !psa.getProjectShareMoneyExpenseAddNew()){
					HyjUtil.displayToast(R.string.app_permission_no_addnew);
					return;
				}else if(mMoneyExpenseContainerEditor.getModelCopy().get_mId() != null && !psa.getProjectShareMoneyExpenseEdit()){
					HyjUtil.displayToast(R.string.app_permission_no_edit);
					return;
				}
				
				mSelectorFieldProject.setText(project.getDisplayName() + "(" + project.getCurrencyId() + ")");
				mSelectorFieldProject.setModelId(project.getId());
				setExchangeRate(false);
				mApportionFieldApportions.changeProject(project, MoneyExpenseApportion.class);
				mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
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

		case GET_APPORTION_MEMBER_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				String type = data.getStringExtra("MODEL_TYPE");
				MoneyExpenseApportion apportion = new MoneyExpenseApportion();
				if("ProjectShareAuthorization".equalsIgnoreCase(type)){
					ProjectShareAuthorization psa = ProjectShareAuthorization.load(ProjectShareAuthorization.class, _id);
					if(!psa.getState().equalsIgnoreCase("Accept")){
						HyjUtil.displayToast(R.string.moneyApportionField_select_toast_apportion_user_not_accepted);
						break;
					}
					apportion.setFriendUserId(psa.getFriendUserId());
				} else {
					Friend friend = Friend.load(Friend.class, _id);
					if(friend.getFriendUserId() != null){
						//看一下该好友是不是项目成员, 如果是，作为项目成员添加
						ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("friendUserId=? AND projectId=?", friend.getFriendUserId(), mSelectorFieldProject.getModelId()).executeSingle();
						if(psa != null){
							if(!psa.getState().equalsIgnoreCase("Accept")){
								HyjUtil.displayToast(R.string.moneyApportionField_select_toast_apportion_user_not_accepted);
								break;
							} else {
								apportion.setFriendUserId(psa.getFriendUserId());
							}
						} else {
							apportion.setLocalFriendId(friend.getId());
							apportion.setApportionType("Average");
						}
					} else {
						apportion.setLocalFriendId(friend.getId());
						apportion.setApportionType("Average");
					}
				}
				apportion.setAmount(0.0);
				apportion.setMoneyExpenseContainerId(mMoneyExpenseContainerEditor.getModel().getId());
				if (mApportionFieldApportions.addApportion(apportion,mSelectorFieldProject.getModelId(), ApportionItem.NEW)) {
					mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
				} else {
					HyjUtil.displayToast(R.string.moneyApportionField_select_toast_apportion_user_already_exists);
				}
			
			}
			break;
		}
		
	}
	
	// inner class
	private static class MoneyExpenseContainerEditor extends HyjModelEditor<MoneyExpenseContainer> {
		private  ProjectShareAuthorization mOldProjectShareAuthorization;
		private  ProjectShareAuthorization mNewProjectShareAuthorization;
		
		public MoneyExpenseContainerEditor(MoneyExpenseContainer moneyExpenseContainer) {
			super(moneyExpenseContainer);
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
