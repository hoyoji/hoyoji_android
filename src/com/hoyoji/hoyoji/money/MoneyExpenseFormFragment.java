package com.hoyoji.hoyoji.money;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.hoyoji.hoyoji.R;
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
	private final static int GET_APPORTION_MEMBER_ID = 4;
	private final static int GET_CATEGORY_ID = 5;
	
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;

	private MoneyExpenseEditor mMoneyExpenseEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private MoneyApportionField mApportionFieldApportions = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericAmount = null;
	private HyjSelectorField mSelectorFieldMoneyAccount = null;
	private HyjSelectorField mSelectorFieldProject = null;
	private HyjNumericField mNumericExchangeRate = null;
	private HyjSelectorField mSelectorFieldMoneyExpenseCategory = null;
	private HyjSelectorField mSelectorFieldFriend = null;
	private HyjRemarkField mRemarkFieldRemark = null;
	private ImageView mImageViewRefreshRate = null;
	private View mViewSeparatorExchange = null;
	private LinearLayout mLinearLayoutExchangeRate = null;
	
	private boolean authority = true;

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
			authority = moneyExpense.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId());
		} else {
			moneyExpense = new MoneyExpense();
		}
		
//		mMoneyExpenseEditor = moneyExpense.newModelEditor();
		mMoneyExpenseEditor = new MoneyExpenseEditor(moneyExpense);

		setupDeleteButton(mMoneyExpenseEditor);

		mImageFieldPicture = (HyjImageField) getView().findViewById(
				R.id.moneyExpenseFormFragment_imageField_picture);
		mImageFieldPicture.setImages(moneyExpense.getPictures());
		
		setupApportionField(moneyExpense);
				
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyExpenseFormFragment_textField_date);
		if (modelId != -1) {
			mDateTimeFieldDate.setText(moneyExpense.getDate());
		}

		mNumericAmount = (HyjNumericField) getView().findViewById(R.id.moneyExpenseFormFragment_textField_amount);
		mNumericAmount.setNumber(moneyExpense.getAmount());
		
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

		Project project = moneyExpense.getProject();
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(
				R.id.moneyExpenseFormFragment_selectorField_project);

		if (project != null) {
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getName() + "("
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

		Friend friend = moneyExpense.getFriend();
		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(
				R.id.moneyExpenseFormFragment_selectorField_friend);

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
				
				if(!authority){
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

		setExchangeRate();

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
				addAllProjectMemberIntoApportionsField(moneyExpense);
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
						
						if(!authority){
							for(int i = 0; i<popup.getMenu().size();i++){
								popup.getMenu().setGroupEnabled(i, false);
							}
						}
						
						popup.show();
					}
				});

		// 只在新增时才自动打开软键盘， 修改时不自动打开
		if (modelId == -1) {
			this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}
		setPermission();
	}
	
	private void addAllProjectMemberIntoApportionsField(MoneyExpense moneyExpense) {
		Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
		List<ProjectShareAuthorization> projectShareAuthorizations = project.getShareAuthorizations();
		for (int i = 0; i < projectShareAuthorizations.size(); i++) {
			if(!projectShareAuthorizations.get(i).getState().equalsIgnoreCase("Accept")){
				continue;
			}
			MoneyExpenseApportion apportion = new MoneyExpenseApportion();
			apportion.setAmount(0.0);
			apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
			apportion.setMoneyExpenseId(moneyExpense.getId());

			mApportionFieldApportions.addApportion(apportion, project.getId(), ApportionItem.NEW);
		}
		mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
		
	}
	
	private void setupApportionField(MoneyExpense moneyExpense) {

		mApportionFieldApportions = (MoneyApportionField) getView().findViewById(R.id.moneyExpenseFormFragment_apportionField);
		
		List<MoneyExpenseApportion> moneyApportions = null;
		
		if(mMoneyExpenseEditor.getModelCopy().get_mId() == null) {
			
			moneyApportions = new ArrayList<MoneyExpenseApportion>();
			if(moneyExpense.getProject() != null && moneyExpense.getProject().getAutoApportion()){
				List<ProjectShareAuthorization> projectShareAuthorizations = moneyExpense.getProject().getShareAuthorizations();
				for(int i=0; i < projectShareAuthorizations.size(); i++){
					MoneyExpenseApportion apportion = new MoneyExpenseApportion();
					apportion.setAmount(0.0);
					apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
					apportion.setMoneyExpenseId(moneyExpense.getId());
					apportion.setApportionType("Share");
					
					moneyApportions.add(apportion);
				}
			} else if(moneyExpense.getProject() != null) {
				MoneyExpenseApportion apportion = new MoneyExpenseApportion();
				apportion.setAmount(0.0);
				apportion.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
				apportion.setMoneyExpenseId(moneyExpense.getId());
				apportion.setApportionType("Average");
				moneyApportions.add(apportion);
			}
			
		} else {
			moneyApportions = moneyExpense.getApportions();
		}
		
		mApportionFieldApportions.init(moneyExpense.getAmount0(), moneyApportions, moneyExpense.getProjectId(), moneyExpense.getId());
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
										
										//删除支出的同时删除分摊
										Iterator<MoneyExpenseApportion> moneyExpenseApportions = moneyExpense.getApportions().iterator();
										while (moneyExpenseApportions.hasNext()) {
											MoneyExpenseApportion moneyExpenseAportion = moneyExpenseApportions.next();
											ProjectShareAuthorization oldProjectShareAuthorization;

											// 非项目好友不用更新项目分摊
											if(moneyExpenseAportion.getFriendUserId() != null){
												if(moneyExpenseAportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
													// 更新旧项目的分摊支出
													oldProjectShareAuthorization = moneyExpenseEditor.getOldSelfProjectShareAuthorization();
												} else {
													// 更新旧项目分摊支出
													oldProjectShareAuthorization = moneyExpenseAportion.getProjectShareAuthorization();
												}
												
												HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
												oldProjectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getApportionedTotalExpense() - (moneyExpenseAportion.getAmount0() * moneyExpenseAportion.getMoneyExpense().getExchangeRate()));
												oldProjectShareAuthorizationEditor.save();
											}
											
											moneyExpenseAportion.delete();
										}
										
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
				}
			});
		}
		
	}
	
	private void setPermission(){

		if(mMoneyExpenseEditor.getModelCopy().get_mId() != null && !authority){
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
			
			getView().findViewById(R.id.moneyExpenseFormFragment_imageButton_apportion_add).setEnabled(false);
			getView().findViewById(R.id.moneyExpenseFormFragment_imageButton_apportion_add_all).setEnabled(false);
			getView().findViewById(R.id.button_save).setEnabled(false);	
			getView().findViewById(R.id.button_delete).setEnabled(false);
		}
	}

	private void setExchangeRate() {
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

				Exchange exchange = Exchange.getExchange(fromCurrency,
						toCurrency);
				if (exchange != null) {
					mNumericExchangeRate.setNumber(exchange.getRate());
					CREATE_EXCHANGE = 0;
				} else {
					mNumericExchangeRate.setText(null);
					CREATE_EXCHANGE = 1;
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
		
		if (mSelectorFieldFriend.getModelId() != null) {
			Friend friend = HyjModel.getModel(Friend.class,
					mSelectorFieldFriend.getModelId());
			if (friend.getFriendUserId() != null) {
				modelCopy.setFriendUserId(mSelectorFieldFriend.getModelId());
			} else {
				modelCopy.setLocalFriendId(mSelectorFieldFriend.getModelId());
			}
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
		mApportionFieldApportions.setError(mMoneyExpenseEditor
				.getValidationError("apportionTotalAmount"));
	}

	@Override
	public void onSave(View v) {
		super.onSave(v);

		fillData();

		mMoneyExpenseEditor.validate();
		
		if (mApportionFieldApportions.getCount() > 0) {
			if (mNumericAmount.getNumber() != null && !mNumericAmount.getNumber().equals(
					mApportionFieldApportions.getTotalAmount())) {
				mMoneyExpenseEditor.setValidationError("apportionTotalAmount",R.string.moneyApportionField_select_toast_apportion_totalAmount_not_equal);
			} else {
				mMoneyExpenseEditor.removeValidationError("apportionTotalAmount");
			}
		} else {
			mMoneyExpenseEditor.removeValidationError("apportionTotalAmount");
		}

		if (mMoneyExpenseEditor.hasValidationErrors()) {
			showValidatioErrors();
		} else {
			try {
				ActiveAndroid.beginTransaction();

				savePictures();

				saveApportions();

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
				if(CREATE_EXCHANGE == 1){
					MoneyAccount moneyAccount = moneyExpenseModel.getMoneyAccount();
					Project project = moneyExpenseModel.getProject();
					
					Exchange newExchange = new Exchange();
					newExchange.setLocalCurrencyId(moneyAccount.getCurrencyId());
					newExchange.setForeignCurrencyId(project.getCurrencyId());
					newExchange.setRate(moneyExpenseModel.getExchangeRate());
					newExchange.save();
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

	private void savePictures() {
		HyjImageField.ImageGridAdapter adapter = mImageFieldPicture.getAdapter();
		int count = adapter.getCount();
		boolean mainPicSet = false;
		for (int i = 0; i < count; i++) {
			PictureItem pi = adapter.getItem(i);
			if (pi.getState() == PictureItem.NEW) {
				Picture newPic = pi.getPicture();
				newPic.setRecordId(mMoneyExpenseEditor.getModel().getId());
				newPic.setRecordType("Picture");
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

	private void saveApportions() {
		MoneyApportionField.ImageGridAdapter adapter = mApportionFieldApportions.getAdapter();
		int count = adapter.getCount();
		int savedCount = 0;
		for (int i = 0; i < count; i++) {
			ApportionItem<MoneyApportion> api = adapter.getItem(i);
			MoneyExpenseApportion apportion = (MoneyExpenseApportion) api.getApportion();
			HyjModelEditor<MoneyExpenseApportion> apportionEditor = apportion.newModelEditor();
			
			if(apportion.getFriendUserId() == null) {
				// 该好友不是项目成员
				if(api.getState() == ApportionItem.DELETED ){
					apportion.delete();
				} else {
					if(api.getState() != ApportionItem.UNCHANGED) {
						api.saveToCopy(apportionEditor.getModelCopy());
					}
					
					// 该好友是网络好友 或 该好友是本地好友
					Friend friend = HyjModel.getModel(Friend.class, apportion.getLocalFriendId());
					MoneyAccount debtAccount = MoneyAccount.getDebtAccount(mMoneyExpenseEditor.getModelCopy().getMoneyAccount().getCurrencyId(), friend);
					if(api.getState() == ApportionItem.NEW){
		                if(debtAccount == null){
		                	MoneyAccount.createDebtAccount(friend, mMoneyExpenseEditor.getModelCopy().getMoneyAccount().getCurrencyId(), apportionEditor.getModelCopy().getAmount0());
		                }else{
		                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
		                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + apportionEditor.getModelCopy().getAmount0());
		                	debtAccountEditor.save();
		                }
					} else{
						MoneyAccount oldDebtAccount = MoneyAccount.getDebtAccount(mMoneyExpenseEditor.getModel().getMoneyAccount().getCurrencyId(), friend);
						HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
						if(debtAccount == null){
							oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - apportionEditor.getModel().getAmount0());
		                	MoneyAccount.createDebtAccount(friend, mMoneyExpenseEditor.getModelCopy().getMoneyAccount().getCurrencyId(), apportionEditor.getModelCopy().getAmount0());
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
					if(api.getState() != ApportionItem.UNCHANGED) {
						apportionEditor.save();
					}
					savedCount++;
				}
			} else {
					if(api.getState() == ApportionItem.DELETED ){
						
						ProjectShareAuthorization oldProjectShareAuthorization;
						
						if(apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
							// 更新旧项目的分摊支出
							oldProjectShareAuthorization = mMoneyExpenseEditor.getOldSelfProjectShareAuthorization();
						} else {
							// 更新旧项目分摊支出
							oldProjectShareAuthorization = apportion.getProjectShareAuthorization();
						}
						
						HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
						oldProjectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getApportionedTotalExpense() - (apportion.getAmount0() * apportion.getMoneyExpense().getExchangeRate()));
						oldProjectShareAuthorizationEditor.save();
						
						apportion.delete();
						
					} else {
						 if(api.getState() != ApportionItem.UNCHANGED) {
								api.saveToCopy(apportionEditor.getModelCopy());
							 }
						Double oldRate = mMoneyExpenseEditor.getModel().getExchangeRate(); 
						Double rate = mMoneyExpenseEditor.getModelCopy().getExchangeRate();
						Double oldApportionAmount = apportionEditor.getModel().getAmount0();
						
						ProjectShareAuthorization projectShareAuthorization;
							//维护项目成员金额
						if(apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
							projectShareAuthorization = mMoneyExpenseEditor.getNewSelfProjectShareAuthorization();
						} else {
							projectShareAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", 
									mMoneyExpenseEditor.getModelCopy().getProjectId(), apportion.getFriendUserId()).executeSingle();
						}
							HyjModelEditor<ProjectShareAuthorization> projectShareAuthorizationEditor = projectShareAuthorization.newModelEditor();
							
							
							if(mMoneyExpenseEditor.getModelCopy().get_mId() == null || 
									mMoneyExpenseEditor.getModel().getProjectId().equals(mMoneyExpenseEditor.getModelCopy().getProjectId())){
								 // 无旧项目可更新
								projectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(projectShareAuthorization.getApportionedTotalExpense() - (oldApportionAmount * oldRate) + (apportionEditor.getModelCopy().getAmount0() * rate));
							}else{
								//更新新项目分摊支出
								projectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(projectShareAuthorization.getApportionedTotalExpense() + (apportionEditor.getModelCopy().getAmount0() * rate));

								//更新老项目分摊支出
								ProjectShareAuthorization oldProjectAuthorization;

								if(apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
									oldProjectAuthorization = mMoneyExpenseEditor.getOldSelfProjectShareAuthorization();
								} else {
									oldProjectAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", 
											mMoneyExpenseEditor.getModel().getProjectId(), apportion.getFriendUserId()).executeSingle();
									
								}
								if(oldProjectAuthorization != null){
									HyjModelEditor<ProjectShareAuthorization> oldProjectAuthorizationEditor = oldProjectAuthorization.newModelEditor();
									oldProjectAuthorizationEditor.getModelCopy().setApportionedTotalExpense(oldProjectAuthorization.getApportionedTotalExpense() - (oldApportionAmount * oldRate));
									oldProjectAuthorizationEditor.save();
								}
							}
							projectShareAuthorizationEditor.save();
							
							//更新相关好友的借贷账户
							if(!apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
								MoneyAccount debtAccount = MoneyAccount.getDebtAccount(mMoneyExpenseEditor.getModelCopy().getMoneyAccount().getCurrencyId(), apportionEditor.getModelCopy().getFriendUserId());
								if(api.getState() == ApportionItem.NEW){
					                if(debtAccount == null){
					                	MoneyAccount.createDebtAccount(apportionEditor.getModelCopy().getFriendUserId(), mMoneyExpenseEditor.getModelCopy().getMoneyAccount().getCurrencyId(), apportionEditor.getModelCopy().getAmount0());
					                }else{
					                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
					                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + apportionEditor.getModelCopy().getAmount0());
					                	debtAccountEditor.save();
					                }
								} else{
									MoneyAccount oldDebtAccount = MoneyAccount.getDebtAccount(mMoneyExpenseEditor.getModel().getMoneyAccount().getCurrencyId(), apportionEditor.getModelCopy().getFriendUserId());
									HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
									if(debtAccount == null){
										oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - apportionEditor.getModel().getAmount0());
					                	MoneyAccount.createDebtAccount(apportionEditor.getModelCopy().getFriendUserId(), mMoneyExpenseEditor.getModelCopy().getMoneyAccount().getCurrencyId(), apportionEditor.getModelCopy().getAmount0());
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
							
							 if(api.getState() != ApportionItem.UNCHANGED) {
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
				
				if(apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
					// 更新旧项目的分摊支出
					oldProjectShareAuthorization = mMoneyExpenseEditor.getOldSelfProjectShareAuthorization();
				} else {
					// 更新旧项目分摊支出
					oldProjectShareAuthorization = apportion.getProjectShareAuthorization();
				}
				
				HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
				oldProjectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getApportionedTotalExpense() - (apportion.getAmount0() * apportion.getMoneyExpense().getExchangeRate()));
				oldProjectShareAuthorizationEditor.save();
				
				apportion.delete();
			}
		}
		
		// 如果列表里一个都没有被保存，我们生成一个默认的分摊
		if (savedCount == 0) {
			MoneyExpenseApportion apportion = new MoneyExpenseApportion();
			apportion.setAmount(mMoneyExpenseEditor.getModelCopy().getAmount0());
			apportion.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
			apportion.setMoneyExpenseId(mMoneyExpenseEditor.getModelCopy().getId());
			apportion.setApportionType("Average");
			
			//更新项目成员的分摊金额
			ProjectShareAuthorization projectShareAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", 
					mMoneyExpenseEditor.getModelCopy().getProjectId(), apportion.getFriendUserId()).executeSingle();
			HyjModelEditor<ProjectShareAuthorization> projectShareAuthorizationEditor = projectShareAuthorization.newModelEditor();
			projectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(projectShareAuthorizationEditor.getModelCopy().getApportionedTotalExpense() + (apportion.getAmount0() * mMoneyExpenseEditor.getModelCopy().getExchangeRate()));
			
			projectShareAuthorizationEditor.save();
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
				setExchangeRate();
			}
			break;
		case GET_PROJECT_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				Project project = Project.load(Project.class, _id);
				mSelectorFieldProject.setText(project.getName() + "(" + project.getCurrencyId() + ")");
				mSelectorFieldProject.setModelId(project.getId());
				setExchangeRate();
				mApportionFieldApportions.changeProject(project, MoneyExpenseApportion.class);
				mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
			}
			break;

		case GET_FRIEND_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				Friend friend = Friend.load(Friend.class, _id);
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
				apportion.setMoneyExpenseId(mMoneyExpenseEditor.getModel().getId());
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
