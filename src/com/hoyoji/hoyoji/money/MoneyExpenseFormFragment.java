package com.hoyoji.hoyoji.money;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyApportion;
import com.hoyoji.hoyoji.models.MoneyExpense;
import com.hoyoji.hoyoji.models.MoneyExpenseApportion;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.MoneyApportionField.ApportionItem;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.project.MemberListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;

public class MoneyExpenseFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_FRIEND_ID = 3;
	private final static int GET_APPORTION_MEMBER_ID = 4;
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;

	private HyjModelEditor<MoneyExpense> mMoneyExpenseEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private MoneyApportionField mApportionFieldApportions = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericAmount = null;
	private HyjSelectorField mSelectorFieldMoneyAccount = null;
	private HyjSelectorField mSelectorFieldProject = null;
	private HyjNumericField mNumericExchangeRate = null;
	private HyjTextField mTextFieldMoneyExpenseCategory = null;
	private HyjSelectorField mSelectorFieldFriend = null;
	private HyjRemarkField mRemarkFieldRemark = null;
	private ImageView mImageViewRefreshRate = null;
	private View mViewSeparatorExchange = null;
	private LinearLayout mLinearLayoutExchangeRate = null;

	@Override
	public Integer useContentView() {
		return R.layout.money_formfragment_moneyexpense;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		final MoneyExpense moneyExpense;

		Intent intent = getActivity().getIntent();
		long modelId = intent.getLongExtra("MODEL_ID", -1);
		if (modelId != -1) {
			moneyExpense = new Select().from(MoneyExpense.class).where("_id=?", modelId).executeSingle();
		} else {
			moneyExpense = new MoneyExpense();
		}

		mMoneyExpenseEditor = moneyExpense.newModelEditor();

		setupDeleteButton(mMoneyExpenseEditor);

		mImageFieldPicture = (HyjImageField) getView().findViewById(
				R.id.moneyExpenseFormFragment_imageField_picture);
		mImageFieldPicture.setImages(moneyExpense.getPictures());
		
		setupApportionField(moneyExpense);

		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(
				R.id.moneyExpenseFormFragment_textField_date);
		if (modelId != -1) {
			mDateTimeFieldDate.setText(moneyExpense.getDate());
		}

		mNumericAmount = (HyjNumericField) getView().findViewById(
				R.id.moneyExpenseFormFragment_textField_amount);
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
				MoneyExpenseFormFragment.this
						.openActivityWithFragmentForResult(
								MoneyAccountListFragment.class,
								R.string.moneyAccountListFragment_title_select_moneyAccount,
								null, GET_MONEYACCOUNT_ID);
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

		mTextFieldMoneyExpenseCategory = (HyjTextField) getView().findViewById(
				R.id.moneyExpenseFormFragment_textField_moneyExpenseCategory);
		mTextFieldMoneyExpenseCategory.setText(moneyExpense
				.getMoneyExpenseCategory());

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

		ImageView takePictureButton = (ImageView) getView().findViewById(
				R.id.moneyExpenseFormFragment_imageView_camera);
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
				popup.show();
			}
		});

		mImageViewRefreshRate = (ImageView) getView().findViewById(
				R.id.moneyExpenseFormFragment_imageButton_refresh_exchangeRate);
		mImageViewRefreshRate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mSelectorFieldMoneyAccount.getModelId() != null
						&& mSelectorFieldProject.getModelId() != null) {
					MoneyAccount moneyAccount = HyjModel.getModel(
							MoneyAccount.class,
							mSelectorFieldMoneyAccount.getModelId());
					Project project = HyjModel.getModel(Project.class,
							mSelectorFieldProject.getModelId());

					String fromCurrency = moneyAccount.getCurrencyId();
					String toCurrency = project.getCurrencyId();
					if (fromCurrency != null && toCurrency != null) {
						HyjUtil.startRoateView(mImageViewRefreshRate);
						mImageViewRefreshRate.setEnabled(false);
						HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
							@Override
							public void finishCallback(Object object) {
								HyjUtil.stopRoateView(mImageViewRefreshRate);
								mImageViewRefreshRate.setEnabled(true);
								mNumericExchangeRate.setNumber((Double) object);
							}

							@Override
							public void errorCallback(Object object) {
								HyjUtil.stopRoateView(mImageViewRefreshRate);
								mImageViewRefreshRate.setEnabled(true);
								if (object != null) {
									HyjUtil.displayToast(object.toString());
								} else {
									HyjUtil.displayToast(R.string.moneyExpenseFormFragment_toast_cannot_refresh_rate);
								}
							}
						};
						HyjHttpGetExchangeRateAsyncTask.newInstance(
								fromCurrency, toCurrency, serverCallbacks);
					} else {
						HyjUtil.displayToast(R.string.moneyExpenseFormFragment_toast_select_currency);
					}
				} else {
					HyjUtil.displayToast(R.string.moneyExpenseFormFragment_toast_select_currency);
				}
			}
		});

		setExchangeRate();

		getView().findViewById(
				R.id.moneyExpenseFormFragment_imageButton_apportion_add)
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Bundle bundle = new Bundle();
						Project project = HyjModel.getModel(Project.class,
								mSelectorFieldProject.getModelId());
						bundle.putLong("MODEL_ID", project.get_mId());
						openActivityWithFragmentForResult(
								MemberListFragment.class,
								R.string.moneyApportionField_select_apportion_member,
								bundle, GET_APPORTION_MEMBER_ID);
					}
				});

		getView().findViewById(R.id.moneyExpenseFormFragment_imageButton_apportion_more_actions).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						PopupMenu popup = new PopupMenu(getActivity(), v);
						MenuInflater inflater = popup.getMenuInflater();
						inflater.inflate(
								R.menu.money_apportionfield_more_actions,
								popup.getMenu());
						popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem item) {
								if (item.getItemId() == R.id.moneyApportionField_menu_moreActions_add_all) {
									Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
									List<ProjectShareAuthorization> projectShareAuthorizations = project.getShareAuthorizations();
									for (int i = 0; i < projectShareAuthorizations.size(); i++) {
										MoneyExpenseApportion apportion = new MoneyExpenseApportion();
										apportion.setAmount(0.0);
										apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
										apportion.setMoneyExpenseId(moneyExpense.getId());

										mApportionFieldApportions.addApportion(apportion, project.getId(), ApportionItem.NEW);
									}
									mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
									return true;
								} else if (item.getItemId() == R.id.moneyApportionField_menu_moreActions_clear) {
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
						popup.show();
					}
				});

		// 只在新增时才自动打开软键盘， 修改时不自动打开
		if (modelId == -1) {
			this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}
	}

	private void setupApportionField(MoneyExpense moneyExpense) {

		mApportionFieldApportions = (MoneyApportionField) getView()
				.findViewById(R.id.moneyExpenseFormFragment_apportionField);
		
		List<MoneyExpenseApportion> moneyApportions = null;
		if(mMoneyExpenseEditor.getModelCopy().get_mId() == null) {
			moneyApportions = new ArrayList<MoneyExpenseApportion>();
			if(moneyExpense.getProject() != null 
					&& moneyExpense.getProject().getAutoApportion()){
				List<ProjectShareAuthorization> projectShareAuthorizations = moneyExpense.getProject().getShareAuthorizations();
				for(int i=0; i < projectShareAuthorizations.size(); i++){
					MoneyExpenseApportion apportion = new MoneyExpenseApportion();
					apportion.setAmount(0.0);
					apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
					apportion.setMoneyExpenseId(moneyExpense.getId());
					apportion.setApportionType("Share");
					
					moneyApportions.add(apportion);
				}
			} else {
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
		
		mApportionFieldApportions.init(moneyExpense.getAmount0(),
				moneyApportions, moneyExpense.getProjectId(), moneyExpense.getId());
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
		MoneyExpense modelCopy = (MoneyExpense) mMoneyExpenseEditor
				.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setAmount(mNumericAmount.getNumber());
		modelCopy.setMoneyAccountId(mSelectorFieldMoneyAccount.getModelId());
		modelCopy.setProjectId(mSelectorFieldProject.getModelId());
		modelCopy.setExchangeRate(mNumericExchangeRate.getNumber());
		modelCopy.setMoneyExpenseCategory(mTextFieldMoneyExpenseCategory
				.getText().toString().trim());

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
		mSelectorFieldMoneyAccount.setError(mMoneyExpenseEditor
				.getValidationError("moneyAccount"));
		mSelectorFieldProject.setError(mMoneyExpenseEditor
				.getValidationError("project"));
		mNumericExchangeRate.setError(mMoneyExpenseEditor
				.getValidationError("exchangeRate"));
		mTextFieldMoneyExpenseCategory.setError(mMoneyExpenseEditor
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
			if (!mNumericAmount.getNumber().equals(
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
				
				UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
				if(moneyExpenseModel.get_mId() == null && !userData.getActiveMoneyAccountId().equals(moneyExpenseModel.getMoneyAccountId()) || !userData.getActiveProjectId().equals(moneyExpenseModel.getProjectId())){
					HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
					userDataEditor.getModelCopy().setActiveMoneyAccountId(moneyExpenseModel.getMoneyAccountId());
					userDataEditor.getModelCopy().setActiveProjectId(moneyExpenseModel.getProjectId());
					userDataEditor.save();
				}
				
				if(CREATE_EXCHANGE == 1){
					MoneyAccount moneyAccount = moneyExpenseModel.getMoneyAccount();
					Project project = moneyExpenseModel.getProject();
					
					Exchange newExchange = new Exchange();
					newExchange.setLocalCurrencyId(moneyAccount.getCurrencyId());
					newExchange.setForeignCurrencyId(project.getCurrencyId());
					newExchange.setRate(moneyExpenseModel.getExchangeRate());
//					newExchange.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
					newExchange.save();
				}
				    MoneyAccount oldMoneyAccount = oldMoneyExpenseModel.getMoneyAccount();
					MoneyAccount newMoneyAccount = moneyExpenseModel.getMoneyAccount();
					HyjModelEditor<MoneyAccount> newMoneyAccountEditor = newMoneyAccount.newModelEditor();
					
					if(moneyExpenseModel.get_mId() == null || oldMoneyAccount.getId().equals(newMoneyAccount.getId())){
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() + oldMoneyExpenseModel.getAmount0() - moneyExpenseModel.getAmount0());
					}else{
						HyjModelEditor<MoneyAccount> oldMoneyAccountEditor = oldMoneyAccount.newModelEditor();
						oldMoneyAccountEditor.getModelCopy().setCurrentBalance(oldMoneyAccount.getCurrentBalance() + oldMoneyExpenseModel.getAmount0());
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() - moneyExpenseModel.getAmount0());
						oldMoneyAccountEditor.save();
					}
					newMoneyAccountEditor.save();
					
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
		int nonDeleteCount = 0;
		for (int i = 0; i < count; i++) {
			ApportionItem<MoneyApportion> pi = adapter.getItem(i);
			MoneyExpenseApportion apportion = (MoneyExpenseApportion) pi.getApportion();
			HyjModelEditor<MoneyExpenseApportion> apportionEditor = apportion.newModelEditor();

			if (pi.getState() == ApportionItem.NEW || pi.getState() == ApportionItem.CHANGED) {
				pi.saveToCopy(apportionEditor.getModelCopy());
				apportionEditor.save();
				nonDeleteCount++;
			} else if (pi.getState() == ApportionItem.DELETED) {
				apportion.delete();
			}
		}

		// 把隐藏掉的分摊添加回去
		Iterator<ApportionItem<MoneyApportion>> it = mApportionFieldApportions.getHiddenApportions().iterator();
		while (it.hasNext()) {
			// Get element
			ApportionItem<MoneyApportion> item = it.next();
			if (item.getState() != ApportionItem.NEW) {
				((MoneyExpenseApportion) item.getApportion()).delete();
			}
		}
		if (nonDeleteCount == 0) {
			MoneyExpenseApportion apportion = new MoneyExpenseApportion();
			apportion.setAmount(mMoneyExpenseEditor.getModelCopy().getAmount0());
			apportion.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
			apportion.setMoneyExpenseId(mMoneyExpenseEditor.getModelCopy().getId());
			apportion.setApportionType("Average");
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

		case GET_APPORTION_MEMBER_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				ProjectShareAuthorization psa = ProjectShareAuthorization.load(
						ProjectShareAuthorization.class, _id);
				MoneyExpenseApportion apportion = new MoneyExpenseApportion();
				apportion.setAmount(0.0);
				apportion.setFriendUserId(psa.getFriendUserId());
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
}
