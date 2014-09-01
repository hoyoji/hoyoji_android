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
import com.hoyoji.hoyoji.models.MoneyBorrow;
import com.hoyoji.hoyoji.models.MoneyExpense;
import com.hoyoji.hoyoji.models.MoneyExpenseApportion;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.MoneyApportionField.ApportionItem;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;

public class MoneyBorrowFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_FRIEND_ID = 3;
	private static final int GET_REMARK = 4;
	private static final int TAG_IS_PROJECT_MEMBER = R.id.moneyBorrowFormFragment_selectorField_friend;
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;

	private HyjModelEditor<MoneyBorrow> mMoneyBorrowEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericAmount = null;
	private HyjDateTimeField mDateTimeFieldReturnDate = null;
	private HyjNumericField mNumericFieldReturnedAmount = null;
	private View mSeparatorFieldReturnedAmount = null;
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
		return R.layout.money_formfragment_moneyborrow;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		MoneyBorrow moneyBorrow;

		Intent intent = getActivity().getIntent();
		long modelId = intent.getLongExtra("MODEL_ID", -1);
		if (modelId != -1) {
			moneyBorrow = HyjModel.load(MoneyBorrow.class, modelId); // new
																		// Select().from(MoneyBorrow.class).where("_id=?",
																		// modelId).executeSingle();
			hasEditPermission = moneyBorrow.hasEditPermission();
		} else {
			moneyBorrow = new MoneyBorrow();
			final String moneyAccountId = intent
					.getStringExtra("moneyAccountId");
			if (moneyAccountId != null) {
				MoneyAccount moneyAccount = HyjModel.getModel(
						MoneyAccount.class, moneyAccountId);
				moneyBorrow.setMoneyAccountId(moneyAccountId,
						moneyAccount.getCurrencyId());
			}
			if (intent.getStringExtra("counterpartId") != null) {
				moneyBorrow.setMoneyLendId(intent
						.getStringExtra("counterpartId"));
			}
		}
		mMoneyBorrowEditor = moneyBorrow.newModelEditor();

		setupDeleteButton(mMoneyBorrowEditor);

		mImageFieldPicture = (HyjImageField) getView().findViewById(
				R.id.moneyBorrowFormFragment_imageField_picture);
		mImageFieldPicture.setImages(moneyBorrow.getPictures());

		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(
				R.id.moneyBorrowFormFragment_textField_date);
		if (modelId != -1) {
			mDateTimeFieldDate.setText(moneyBorrow.getDate());
		}

		mNumericAmount = (HyjNumericField) getView().findViewById(
				R.id.moneyBorrowFormFragment_textField_amount);
		double amount = intent.getDoubleExtra("amount", -1.0);// 从分享消息导入的金额
		if (amount >= 0.0) {
			double exchangeRate = intent.getDoubleExtra("exchangeRate", 1.0);
			mNumericAmount.setNumber(amount * exchangeRate);
		} else {
			mNumericAmount.setNumber(moneyBorrow.getAmount());
		}

		mDateTimeFieldReturnDate = (HyjDateTimeField) getView().findViewById(
				R.id.moneyBorrowFormFragment_textField_returnDate);
		mDateTimeFieldReturnDate.setText(moneyBorrow.getReturnDate());

		mNumericFieldReturnedAmount = (HyjNumericField) getView().findViewById(
				R.id.moneyBorrowFormFragment_textField_returnedAmount);
		mNumericFieldReturnedAmount.setNumber(moneyBorrow.getReturnedAmount());
		mNumericFieldReturnedAmount.setEnabled(false);
		mSeparatorFieldReturnedAmount = (View) getView().findViewById(
				R.id.moneyBorrowFormFragment_separatorField_returnedAmount);
		mNumericFieldReturnedAmount.setVisibility(View.GONE);
		mSeparatorFieldReturnedAmount.setVisibility(View.GONE);

		MoneyAccount moneyAccount = moneyBorrow.getMoneyAccount();
		mSelectorFieldMoneyAccount = (HyjSelectorField) getView().findViewById(
				R.id.moneyBorrowFormFragment_selectorField_moneyAccount);

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
				MoneyBorrowFormFragment.this
						.openActivityWithFragmentForResult(
								MoneyAccountListFragment.class,
								R.string.moneyAccountListFragment_title_select_moneyAccount,
								bundle, GET_MONEYACCOUNT_ID);
			}
		});

		Project project;
		String projectId = intent.getStringExtra("projectId");// 从消息导入
		if (moneyBorrow.get_mId() == null && projectId != null) {
			project = HyjModel.getModel(Project.class, projectId);
		} else {
			project = moneyBorrow.getProject();
		}
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(
				R.id.moneyBorrowFormFragment_selectorField_project);

		if (project != null) {
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getDisplayName() + "("
					+ project.getCurrencyId() + ")");
		} else {
			mSelectorFieldProject.setText("共享来的收支");
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneyBorrowFormFragment.this.openActivityWithFragmentForResult(
						ProjectListFragment.class,
						R.string.projectListFragment_title_select_project,
						null, GET_PROJECT_ID);
			}
		});

		mNumericExchangeRate = (HyjNumericField) getView().findViewById(
				R.id.moneyBorrowFormFragment_textField_exchangeRate);
		mNumericExchangeRate.setNumber(moneyBorrow.getExchangeRate());

		mViewSeparatorExchange = (View) getView().findViewById(
				R.id.moneyBorrowFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(
				R.id.moneyBorrowFormFragment_linearLayout_exchangeRate);

		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(
				R.id.moneyBorrowFormFragment_selectorField_friend);
		if (moneyBorrow.get_mId() == null) {
			String friendUserId = intent.getStringExtra("friendUserId");// 从消息导入
			if (friendUserId != null) {
				// 看一下该好友是不是项目成员, 如果是，作为项目成员添加
				ProjectShareAuthorization psa = new Select()
						.from(ProjectShareAuthorization.class)
						.where("friendUserId=? AND projectId=?", friendUserId,
								mSelectorFieldProject.getModelId())
						.executeSingle();
				if (psa != null) {
					if (!psa.getState().equalsIgnoreCase("Accept")) {
						mSelectorFieldFriend
								.setModelId(psa.getFriend().getId());
						mSelectorFieldFriend
								.setText(psa.getFriendDisplayName());
						mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER,
								false);
					} else {
						mSelectorFieldFriend.setModelId(friendUserId);
						mSelectorFieldFriend
								.setText(psa.getFriendDisplayName());
						mSelectorFieldFriend
								.setTag(TAG_IS_PROJECT_MEMBER, true);
					}
				} else {
					Friend friend = new Select().from(Friend.class)
							.where("friendUserId=?", friendUserId)
							.executeSingle();
					if (friend != null) {
						mSelectorFieldFriend.setModelId(friend.getId());
						mSelectorFieldFriend.setText(friend.getDisplayName());
						mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER,
								false);
					}
					// else {
					// User user = HyjModel.getModel(User.class, friendUserId);
					// if(user != null){
					// mSelectorFieldFriend.setModelId(friendUserId);
					// mSelectorFieldFriend.setText(user.getDisplayName());
					// }
					// }
				}
			} 
//			else {
//				String localFriendId = intent.getStringExtra("localFriendId");// 从消息导入
//				if (localFriendId != null) {
//					Friend friend = HyjModel.getModel(Friend.class,
//							localFriendId);
//					if (friend != null) {
//						mSelectorFieldFriend.setModelId(friendUserId);
//						mSelectorFieldFriend.setText(friend.getDisplayName());
//						mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER,
//								false);
//					}
//				} else {
//					Friend friend = moneyBorrow.getLocalFriend();
//					if (friend != null) {
//						mSelectorFieldFriend.setModelId(friend.getId());
//						mSelectorFieldFriend.setText(friend.getDisplayName());
//						mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER,
//								false);
//					} else {
//						User user = moneyBorrow.getFriendUser();
//						if (user != null) {
//							mSelectorFieldFriend.setModelId(user.getId());
//							mSelectorFieldFriend.setText(user.getDisplayName());
//							mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER,
//									false);
//						}
//					}
//				}
//			}
		} else {
			Friend friend = moneyBorrow.getLocalFriend();
			if (friend != null) {
				mSelectorFieldFriend.setModelId(friend.getId());
				mSelectorFieldFriend.setText(friend.getDisplayName());
				mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, false);
			} else  if(moneyBorrow.getFriendUserId() != null){
				mSelectorFieldFriend.setModelId(moneyBorrow.getFriendUserId());
				mSelectorFieldFriend.setText(Friend.getFriendUserDisplayName1(moneyBorrow.getFriendUserId()));
				mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER, true);
			}
		}

		mSelectorFieldFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				Project project = HyjModel.getModel(Project.class,
						mSelectorFieldProject.getModelId());
				bundle.putLong("MODEL_ID", project.get_mId());
				openActivityWithFragmentForResult(
						SelectApportionMemberListFragment.class,
						R.string.friendListFragment_title_select_friend_creditor,
						bundle, GET_FRIEND_ID);
			}
		});

		mImageViewClearFriend = (ImageView) getView().findViewById(
				R.id.moneyBorrowFormFragment_imageView_clear_friend);
		mImageViewClearFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSelectorFieldFriend.setModelId(null);
				mSelectorFieldFriend.setText("");
			}
		});

		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(
				R.id.moneyBorrowFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyBorrow.getRemark());
		mRemarkFieldRemark.setEditable(false);
		mRemarkFieldRemark.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("TEXT", mRemarkFieldRemark.getText());
				bundle.putString("HINT",
						"请输入" + mRemarkFieldRemark.getLabelText());
				MoneyBorrowFormFragment.this.openActivityWithFragmentForResult(
						HyjTextInputFormFragment.class,
						R.string.moneyExpenseFormFragment_textView_remark,
						bundle, GET_REMARK);
			}
		});
		ImageView takePictureButton = (ImageView) getView().findViewById(
				R.id.moneyBorrowFormFragment_imageView_camera);
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

				if (!hasEditPermission) {
					for (int i = 0; i < popup.getMenu().size(); i++) {
						popup.getMenu().setGroupEnabled(i, false);
					}
				}

				popup.show();
			}
		});

		mImageViewRefreshRate = (ImageView) getView().findViewById(
				R.id.moneyBorrowFormFragment_imageButton_refresh_exchangeRate);
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
						HyjUtil.updateExchangeRate(fromCurrency, toCurrency,
								mImageViewRefreshRate, mNumericExchangeRate);
					} else {
						HyjUtil.displayToast(R.string.moneyBorrowFormFragment_toast_select_currency);
					}
				} else {
					HyjUtil.displayToast(R.string.moneyBorrowFormFragment_toast_select_currency);
				}
			}
		});

		// 只在新增时才自动打开软键盘， 修改时不自动打开
		if (modelId == -1) {
			setExchangeRate(false);
			this.getActivity()
					.getWindow()
					.setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		} else {
			setExchangeRate(true);
		}
		setPermission();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if (mMoneyBorrowEditor != null
				&& mMoneyBorrowEditor.getModelCopy().get_mId() != null
				&& !hasEditPermission) {
			hideSaveAction();
		}
	}

	private void setupDeleteButton(HyjModelEditor<MoneyBorrow> moneyBorrowEditor) {

		Button buttonDelete = (Button) getView().findViewById(
				R.id.button_delete);

		final MoneyBorrow moneyBorrow = moneyBorrowEditor.getModelCopy();

		if (moneyBorrow.get_mId() == null) {
			buttonDelete.setVisibility(View.GONE);
		} else {
			buttonDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (moneyBorrow.hasDeletePermission()) {
						((HyjActivity) getActivity()).displayDialog(
								R.string.app_action_delete_list_item,
								R.string.app_confirm_delete,
								R.string.alert_dialog_yes,
								R.string.alert_dialog_no, -1,
								new DialogCallbackListener() {
									@Override
									public void doPositiveClick(Object object) {
										try {
											ActiveAndroid.beginTransaction();

											MoneyAccount moneyAccount = moneyBorrow
													.getMoneyAccount();
											HyjModelEditor<MoneyAccount> moneyAccountEditor = moneyAccount
													.newModelEditor();
											moneyAccountEditor
													.getModelCopy()
													.setCurrentBalance(
															moneyAccount
																	.getCurrentBalance()
																	- moneyBorrow
																			.getAmount());
											moneyAccountEditor.save();

											// 更新项目余额
											Project newProject = moneyBorrow
													.getProject();
											HyjModelEditor<Project> newProjectEditor = newProject
													.newModelEditor();
											newProjectEditor
													.getModelCopy()
													.setIncomeTotal(
															newProject
																	.getIncomeTotal()
																	- moneyBorrow
																			.getProjectAmount());
											newProjectEditor.save();

											if (!newProject.isProjectMember(
													moneyBorrow
															.getLocalFriendId(),
													moneyBorrow
															.getFriendUserId())) {
												MoneyAccount debtAccount = MoneyAccount
														.getDebtAccount(
																moneyBorrow
																		.getProject()
																		.getCurrencyId(),
																moneyBorrow
																		.getLocalFriendId(),
																moneyBorrow
																		.getFriendUserId());
												HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount
														.newModelEditor();
												debtAccountEditor
														.getModelCopy()
														.setCurrentBalance(
																debtAccount
																		.getCurrentBalance()
																		+ moneyBorrow
																				.getProjectAmount());
												debtAccountEditor.save();
											}

											// 更新支出所有者的实际支出
											ProjectShareAuthorization projectAuthorization = ProjectShareAuthorization
													.getSelfProjectShareAuthorization(moneyBorrow
															.getProjectId());
											HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = projectAuthorization
													.newModelEditor();
											selfProjectAuthorizationEditor
													.getModelCopy()
													.setActualTotalBorrow(
															projectAuthorization
																	.getActualTotalBorrow()
																	- moneyBorrow
																			.getProjectAmount());
											selfProjectAuthorizationEditor
													.save();

											moneyBorrow.delete();

											HyjUtil.displayToast(R.string.app_delete_success);
											ActiveAndroid
													.setTransactionSuccessful();
											ActiveAndroid.endTransaction();
											getActivity().finish();
										} catch (Exception e) {
											ActiveAndroid.endTransaction();
											HyjUtil.displayToast(R.string.app_delete_failed);
										}
									}
								});
					} else {
						HyjUtil.displayToast(R.string.app_permission_no_delete);
					}
				}
			});
		}
	}

	private void setPermission() {

		if (mMoneyBorrowEditor.getModelCopy().get_mId() != null
				&& !hasEditPermission) {
			mDateTimeFieldDate.setEnabled(false);

			mNumericAmount.setNumber(mMoneyBorrowEditor.getModel()
					.getProjectAmount());
			mNumericAmount.setEnabled(false);

			mDateTimeFieldReturnDate.setEnabled(false);

			mSelectorFieldFriend.setEnabled(false);

			mSelectorFieldMoneyAccount.setEnabled(false);
			mSelectorFieldMoneyAccount.setVisibility(View.GONE);
			getView().findViewById(
					R.id.moneyBorrowFormFragment_separatorField_moneyAccount)
					.setVisibility(View.GONE);

			mSelectorFieldProject.setEnabled(false);

			mNumericExchangeRate.setEnabled(false);

			mNumericFieldReturnedAmount.setEnabled(false);

			mRemarkFieldRemark.setEnabled(false);

			if (this.mOptionsMenu != null) {
				hideSaveAction();
			}

			// getView().findViewById(R.id.button_save).setEnabled(false);
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

				if (!editInit) {// 修改时init不需要set Rate
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
		MoneyBorrow modelCopy = (MoneyBorrow) mMoneyBorrowEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setAmount(mNumericAmount.getNumber());
		modelCopy.setReturnDate(mDateTimeFieldReturnDate.getText());
		if (mSelectorFieldMoneyAccount.getModelId() != null) {
			MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class,
					mSelectorFieldMoneyAccount.getModelId());
			modelCopy.setMoneyAccountId(
					mSelectorFieldMoneyAccount.getModelId(),
					moneyAccount.getCurrencyId());
		} else {
			modelCopy.setMoneyAccountId(null, null);
		}
		modelCopy.setProject(HyjModel.getModel(Project.class,
				mSelectorFieldProject.getModelId()));
		modelCopy.setExchangeRate(mNumericExchangeRate.getNumber());

		if (mSelectorFieldFriend.getModelId() != null) {
			if ((Boolean) mSelectorFieldFriend.getTag(TAG_IS_PROJECT_MEMBER)) {
				modelCopy.setFriendUserId(mSelectorFieldFriend.getModelId());
				modelCopy.setLocalFriendId(null);
			} else {
				modelCopy.setLocalFriendId(mSelectorFieldFriend.getModelId());
				modelCopy.setFriendUserId(null);
			}
		} else {
			modelCopy.setLocalFriendId(null);
			modelCopy.setFriendUserId(null);
		}

		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
	}

	private void showValidatioErrors() {
		HyjUtil.displayToast(R.string.app_validation_error);
		mDateTimeFieldDate.setError(mMoneyBorrowEditor
				.getValidationError("datetime"));

		mNumericAmount
				.setError(mMoneyBorrowEditor.getValidationError("amount"));
		if (mMoneyBorrowEditor.getValidationError("amount") != null) {
			mNumericAmount.showSoftKeyboard();
		}
		mDateTimeFieldReturnDate.setError(mMoneyBorrowEditor
				.getValidationError("returnDate"));
		if (mMoneyBorrowEditor.getValidationError("returnDate") != null) {
			HyjUtil.displayToast(mMoneyBorrowEditor
					.getValidationError("returnDate"));
		}

		mSelectorFieldMoneyAccount.setError(mMoneyBorrowEditor
				.getValidationError("moneyAccount"));
		mSelectorFieldProject.setError(mMoneyBorrowEditor
				.getValidationError("project"));
		mNumericExchangeRate.setError(mMoneyBorrowEditor
				.getValidationError("exchangeRate"));
		mSelectorFieldFriend.setError(mMoneyBorrowEditor
				.getValidationError("friend"));
		mRemarkFieldRemark.setError(mMoneyBorrowEditor
				.getValidationError("remark"));
	}

	@Override
	public void onSave(View v) {
		super.onSave(v);

		fillData();

		if (mMoneyBorrowEditor.getModelCopy().get_mId() == null
				&& !mMoneyBorrowEditor.getModelCopy().hasAddNewPermission(
						mMoneyBorrowEditor.getModelCopy().getProjectId())) {
			HyjUtil.displayToast(R.string.app_permission_no_addnew);
		} else if (mMoneyBorrowEditor.getModelCopy().get_mId() != null
				&& !hasEditPermission) {
			HyjUtil.displayToast(R.string.app_permission_no_edit);
		} else {
			
			if(mMoneyBorrowEditor.getModelCopy().getFriendUserId() == null && mMoneyBorrowEditor.getModelCopy().getLocalFriendId() == null){
				mMoneyBorrowEditor.setValidationError("friend",R.string.moneyBorrowFormFragment_editText_hint_friend);
			}else{
				mMoneyBorrowEditor.removeValidationError("friend");
			}
			
			mMoneyBorrowEditor.validate();

			if (mMoneyBorrowEditor.hasValidationErrors()) {
				showValidatioErrors();
			} else {
				try {
					ActiveAndroid.beginTransaction();
					HyjImageField.ImageGridAdapter adapter = mImageFieldPicture
							.getAdapter();
					int count = adapter.getCount();
					boolean mainPicSet = false;
					for (int i = 0; i < count; i++) {
						PictureItem pi = adapter.getItem(i);
						if (pi.getState() == PictureItem.NEW) {
							Picture newPic = pi.getPicture();
							newPic.setRecordId(mMoneyBorrowEditor.getModel()
									.getId());
							newPic.setRecordType("MoneyBorrow");
							newPic.setDisplayOrder(i);
							newPic.save();
						} else if (pi.getState() == PictureItem.DELETED) {
							pi.getPicture().delete();
						} else if (pi.getState() == PictureItem.CHANGED) {

						}
						if (!mainPicSet && pi.getPicture() != null
								&& pi.getState() != PictureItem.DELETED) {
							mainPicSet = true;
							mMoneyBorrowEditor.getModelCopy().setPicture(
									pi.getPicture());
						}
					}

					MoneyBorrow oldMoneyBorrowModel = mMoneyBorrowEditor
							.getModel();
					MoneyBorrow moneyBorrowModel = mMoneyBorrowEditor
							.getModelCopy();

					UserData userData = HyjApplication.getInstance()
							.getCurrentUser().getUserData();
					if (moneyBorrowModel.get_mId() == null
							&& !userData.getActiveMoneyAccountId().equals(
									moneyBorrowModel.getMoneyAccountId())
							|| !userData.getActiveProjectId().equals(
									moneyBorrowModel.getProjectId())) {
						HyjModelEditor<UserData> userDataEditor = userData
								.newModelEditor();
						userDataEditor.getModelCopy().setActiveMoneyAccountId(
								moneyBorrowModel.getMoneyAccountId());
						userDataEditor.getModelCopy().setActiveProjectId(
								moneyBorrowModel.getProjectId());
						userDataEditor.save();
					}

					String localCurrencyId = moneyBorrowModel.getMoneyAccount()
							.getCurrencyId();
					String foreignCurrencyId = moneyBorrowModel.getProject()
							.getCurrencyId();
					if (CREATE_EXCHANGE == 1) {
						Exchange newExchange = new Exchange();
						newExchange.setLocalCurrencyId(localCurrencyId);
						newExchange.setForeignCurrencyId(foreignCurrencyId);
						newExchange.setRate(moneyBorrowModel.getExchangeRate());
						// newExchange.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
						newExchange.save();
					} else {
						if (!localCurrencyId
								.equalsIgnoreCase(foreignCurrencyId)) {
							Exchange exchange = null;
							Double exRate = null;
							Double rate = HyjUtil.toFixed2(moneyBorrowModel
									.getExchangeRate());
							exchange = Exchange.getExchange(localCurrencyId,
									foreignCurrencyId);
							if (exchange != null) {
								exRate = exchange.getRate();
								if (!rate.equals(exRate)) {
									HyjModelEditor<Exchange> exchangModelEditor = exchange
											.newModelEditor();
									exchangModelEditor.getModelCopy().setRate(
											rate);
									exchangModelEditor.save();
								}
							} else {
								exchange = Exchange.getExchange(
										foreignCurrencyId, localCurrencyId);
								if (exchange != null) {
									exRate = HyjUtil.toFixed2(1 / exchange
											.getRate());
									if (!rate.equals(exRate)) {
										HyjModelEditor<Exchange> exchangModelEditor = exchange
												.newModelEditor();
										exchangModelEditor.getModelCopy()
												.setRate(1 / rate);
										exchangModelEditor.save();
									}
								}
							}
						}
					}

					Double oldAmount = oldMoneyBorrowModel.getAmount0();
					MoneyAccount oldMoneyAccount = oldMoneyBorrowModel
							.getMoneyAccount();
					MoneyAccount newMoneyAccount = moneyBorrowModel
							.getMoneyAccount();
					HyjModelEditor<MoneyAccount> newMoneyAccountEditor = newMoneyAccount
							.newModelEditor();

					if (moneyBorrowModel.get_mId() == null
							|| oldMoneyAccount.getId().equals(
									newMoneyAccount.getId())) {
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(
								newMoneyAccount.getCurrentBalance() - oldAmount
										+ moneyBorrowModel.getAmount0());

					} else {
						HyjModelEditor<MoneyAccount> oldMoneyAccountEditor = oldMoneyAccount
								.newModelEditor();
						oldMoneyAccountEditor.getModelCopy()
								.setCurrentBalance(
										oldMoneyAccount.getCurrentBalance()
												- oldAmount);
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(
								newMoneyAccount.getCurrentBalance()
										+ moneyBorrowModel.getAmount0());
						oldMoneyAccountEditor.save();
					}
					newMoneyAccountEditor.save();

					Project oldProject = oldMoneyBorrowModel.getProject();
					Project newProject = moneyBorrowModel.getProject();
					HyjModelEditor<Project> newProjectEditor = newProject
							.newModelEditor();

					// 更新项目余额
					if (moneyBorrowModel.get_mId() == null
							|| oldProject.getId().equals(newProject.getId())) {
						newProjectEditor.getModelCopy().setIncomeTotal(
								newProject.getIncomeTotal()
										- oldMoneyBorrowModel
												.getProjectAmount()
										+ moneyBorrowModel.getProjectAmount());
					} else {
						HyjModelEditor<Project> oldProjectEditor = oldProject
								.newModelEditor();
						oldProjectEditor.getModelCopy().setIncomeTotal(
								oldProject.getIncomeTotal()
										- oldMoneyBorrowModel
												.getProjectAmount());
						newProjectEditor.getModelCopy().setIncomeTotal(
								newProject.getIncomeTotal()
										+ moneyBorrowModel.getProjectAmount());
						oldProjectEditor.save();
					}
					newProjectEditor.save();

					MoneyAccount newDebtAccount = null;
					boolean isNewProjectMember = newProject.isProjectMember(
							moneyBorrowModel.getLocalFriendId(),
							moneyBorrowModel.getFriendUserId());
					if (!isNewProjectMember) {
						// 如果不是项目成员，更新借贷账户
						newDebtAccount = MoneyAccount.getDebtAccount(
								moneyBorrowModel.getProject().getCurrencyId(),
								moneyBorrowModel.getLocalFriendId(),
								moneyBorrowModel.getFriendUserId());
					}
					if (moneyBorrowModel.get_mId() == null) {
						if (newDebtAccount != null) {
							HyjModelEditor<MoneyAccount> newDebtAccountEditor = newDebtAccount
									.newModelEditor();
							newDebtAccountEditor
									.getModelCopy()
									.setCurrentBalance(
											newDebtAccount.getCurrentBalance()
													- moneyBorrowModel
															.getProjectAmount());
							newDebtAccountEditor.save();
						} else if (!isNewProjectMember) {
							MoneyAccount.createDebtAccount(moneyBorrowModel
									.getLocalFriendId(), moneyBorrowModel
									.getFriendUserId(), moneyBorrowModel
									.getProject().getCurrencyId(),
									-moneyBorrowModel.getProjectAmount());
						}
					} else {
						MoneyAccount oldDebtAccount = null;
						if (!oldProject.isProjectMember(
								oldMoneyBorrowModel.getLocalFriendId(),
								oldMoneyBorrowModel.getFriendUserId())) {
							oldDebtAccount = MoneyAccount.getDebtAccount(
									oldMoneyBorrowModel.getProject()
											.getCurrencyId(),
									oldMoneyBorrowModel.getLocalFriendId(),
									oldMoneyBorrowModel.getFriendUserId());
						}
						if (newDebtAccount != null) {
							HyjModelEditor<MoneyAccount> newDebtAccountEditor = newDebtAccount
									.newModelEditor();
							if (oldDebtAccount != null
									&& oldDebtAccount.getId().equals(
											newDebtAccount.getId())) {
								newDebtAccountEditor
										.getModelCopy()
										.setCurrentBalance(
												newDebtAccount
														.getCurrentBalance()
														+ oldMoneyBorrowModel
																.getProjectAmount()
														- moneyBorrowModel
																.getProjectAmount());
							} else {
								newDebtAccountEditor
										.getModelCopy()
										.setCurrentBalance(
												newDebtAccount
														.getCurrentBalance()
														- moneyBorrowModel
																.getProjectAmount());
								if (oldDebtAccount != null) {
									HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount
											.newModelEditor();
									oldDebtAccountEditor
											.getModelCopy()
											.setCurrentBalance(
													oldDebtAccount
															.getCurrentBalance()
															+ oldMoneyBorrowModel
																	.getProjectAmount());
									oldDebtAccountEditor.save();
								}
							}
							newDebtAccountEditor.save();
						} else {
							if (oldDebtAccount != null) {
								HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount
										.newModelEditor();
								oldDebtAccountEditor
										.getModelCopy()
										.setCurrentBalance(
												oldDebtAccount
														.getCurrentBalance()
														+ oldMoneyBorrowModel
																.getProjectAmount());
								oldDebtAccountEditor.save();
							}
							if (!isNewProjectMember) {
								MoneyAccount.createDebtAccount(moneyBorrowModel
										.getLocalFriendId(), moneyBorrowModel
										.getFriendUserId(), moneyBorrowModel
										.getProject().getCurrencyId(),
										-moneyBorrowModel.getProjectAmount());
							}
						}
					}

					// 更新支出所有者的实际借入
					ProjectShareAuthorization selfProjectAuthorization = ProjectShareAuthorization
							.getSelfProjectShareAuthorization(moneyBorrowModel
									.getProjectId());
					HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = selfProjectAuthorization
							.newModelEditor();
					if (moneyBorrowModel.get_mId() == null
							|| oldMoneyBorrowModel.getProjectId().equals(
									moneyBorrowModel.getProjectId())) {
						selfProjectAuthorizationEditor.getModelCopy()
								.setActualTotalBorrow(
										selfProjectAuthorization
												.getActualTotalBorrow()
												- oldMoneyBorrowModel
														.getProjectAmount()
												+ moneyBorrowModel
														.getProjectAmount());
					} else {
						ProjectShareAuthorization oldSelfProjectAuthorization = ProjectShareAuthorization
								.getSelfProjectShareAuthorization(oldMoneyBorrowModel
										.getProjectId());
						HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization
								.newModelEditor();
						oldSelfProjectAuthorizationEditor.getModelCopy()
								.setActualTotalBorrow(
										oldSelfProjectAuthorization
												.getActualTotalBorrow()
												- oldMoneyBorrowModel
														.getProjectAmount());
						selfProjectAuthorizationEditor.getModelCopy()
								.setActualTotalBorrow(
										selfProjectAuthorization
												.getActualTotalBorrow()
												+ moneyBorrowModel
														.getProjectAmount());
						oldSelfProjectAuthorizationEditor.save();
					}
					selfProjectAuthorizationEditor.save();

					mMoneyBorrowEditor.save();
					ActiveAndroid.setTransactionSuccessful();
					if (getActivity().getCallingActivity() != null) {
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

				ProjectShareAuthorization psa = new Select()
						.from(ProjectShareAuthorization.class)
						.where("projectId = ? AND friendUserId=?",
								project.getId(),
								HyjApplication.getInstance().getCurrentUser()
										.getId()).executeSingle();

				if (mMoneyBorrowEditor.getModelCopy().get_mId() == null
						&& !psa.getProjectShareMoneyExpenseAddNew()) {
					HyjUtil.displayToast(R.string.app_permission_no_addnew);
					return;
				} else if (mMoneyBorrowEditor.getModelCopy().get_mId() != null
						&& !psa.getProjectShareMoneyExpenseEdit()) {
					HyjUtil.displayToast(R.string.app_permission_no_edit);
					return;
				}

				mSelectorFieldProject.setText(project.getDisplayName() + "("
						+ project.getCurrencyId() + ")");
				mSelectorFieldProject.setModelId(project.getId());
				setExchangeRate(false);

				// 看一下好友是不是新项目的成员
				if (mSelectorFieldFriend.getModelId() != null) {
					String friendUserId = null;
					ProjectShareAuthorization psaMember = null;
					Friend friend = null;
					if ((Boolean) mSelectorFieldFriend
							.getTag(TAG_IS_PROJECT_MEMBER)) {
						friendUserId = mSelectorFieldFriend.getModelId();
					} else {
						String localFriendId = mSelectorFieldFriend
								.getModelId();
						friend = HyjModel.getModel(Friend.class, localFriendId);
						friendUserId = friend.getFriendUserId();
					}
					if (friendUserId != null) {
						psaMember = new Select()
								.from(ProjectShareAuthorization.class)
								.where("projectId = ? AND friendUserId=?",
										project.getId(), friendUserId)
								.executeSingle();
					}

					if (psaMember != null) {
						mSelectorFieldFriend.setModelId(friendUserId);
						mSelectorFieldFriend
								.setTag(TAG_IS_PROJECT_MEMBER, true);
					} else {
						if (friendUserId != null) {
							friend = new Select().from(Friend.class)
									.where("friendUserId = ?", friendUserId)
									.executeSingle();
						}
						if (friend == null) {
							mSelectorFieldFriend.setText(null);
							mSelectorFieldFriend.setModelId(null);
							mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER,
									false);
						} else {
							String localFriendId = friend.getId();
							mSelectorFieldFriend.setModelId(localFriendId);
							mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER,
									false);
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
			// if(resultCode == Activity.RESULT_OK){
			// long _id = data.getLongExtra("MODEL_ID", -1);
			// Friend friend = Friend.load(Friend.class, _id);
			//
			// if(friend.getFriendUserId() != null &&
			// friend.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
			// HyjUtil.displayToast(R.string.moneyBorrowFormFragment_editText_error_friend);
			// return;
			// }
			//
			// mSelectorFieldFriend.setText(friend.getDisplayName());
			// mSelectorFieldFriend.setModelId(friend.getId());
			// }
			// break;
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				String type = data.getStringExtra("MODEL_TYPE");
				if ("ProjectShareAuthorization".equalsIgnoreCase(type)) {
					ProjectShareAuthorization psa = ProjectShareAuthorization
							.load(ProjectShareAuthorization.class, _id);
					if (psa.getFriendUserId() != null
							&& psa.getFriendUserId().equals(
									HyjApplication.getInstance()
											.getCurrentUser().getId())) {
						HyjUtil.displayToast(R.string.moneyBorrowFormFragment_editText_error_friend);
						return;
					}
					if (!psa.getState().equalsIgnoreCase("Accept")) {
						mSelectorFieldFriend
								.setText(psa.getFriendDisplayName());
						mSelectorFieldFriend
								.setModelId(psa.getFriend().getId());
						mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER,
								false);
					} else {
						mSelectorFieldFriend
								.setText(psa.getFriendDisplayName());
						mSelectorFieldFriend.setModelId(psa.getFriendUserId());
						mSelectorFieldFriend
								.setTag(TAG_IS_PROJECT_MEMBER, true);
					}
				} else {
					Friend friend = Friend.load(Friend.class, _id);
					if (friend.getFriendUserId() != null) {
						if (friend.getFriendUserId().equals(
								HyjApplication.getInstance().getCurrentUser()
										.getId())) {
							HyjUtil.displayToast(R.string.moneyBorrowFormFragment_editText_error_friend);
							return;
						}

						// 看一下该好友是不是项目成员, 如果是，作为项目成员添加
						ProjectShareAuthorization psa = new Select()
								.from(ProjectShareAuthorization.class)
								.where("friendUserId=? AND projectId=?",
										friend.getFriendUserId(),
										mSelectorFieldProject.getModelId())
								.executeSingle();
						if (psa != null) {
							if (!psa.getState().equalsIgnoreCase("Accept")) {
								mSelectorFieldFriend.setText(friend
										.getDisplayName());
								mSelectorFieldFriend.setModelId(friend.getId());
								mSelectorFieldFriend.setTag(
										TAG_IS_PROJECT_MEMBER, false);
							} else {
								mSelectorFieldFriend.setText(friend
										.getDisplayName());
								mSelectorFieldFriend.setModelId(friend
										.getFriendUserId());
								mSelectorFieldFriend.setTag(
										TAG_IS_PROJECT_MEMBER, true);
							}
						} else {
							mSelectorFieldFriend.setText(friend
									.getDisplayName());
							mSelectorFieldFriend.setModelId(friend.getId());
							mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER,
									false);
						}
					} else {
						mSelectorFieldFriend.setText(friend.getDisplayName());
						mSelectorFieldFriend.setModelId(friend.getId());
						mSelectorFieldFriend.setTag(TAG_IS_PROJECT_MEMBER,
								false);
					}
				}
			}
			break;
		}
	}
}
