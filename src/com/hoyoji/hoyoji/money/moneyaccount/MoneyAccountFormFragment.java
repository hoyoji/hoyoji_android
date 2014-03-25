package com.hoyoji.hoyoji.money.moneyaccount;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjHttpGetExchangeRateAsyncTask;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.android.hyjframework.view.HyjSpinnerField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Currency;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyTransfer;
import com.hoyoji.hoyoji.money.currency.CurrencyListFragment;
import com.hoyoji.hoyoji.money.currency.ExchangeFormFragment;
import com.hoyoji.hoyoji.project.ProjectFormFragment;

public class MoneyAccountFormFragment extends HyjUserFormFragment {
	private final static int GET_CURRENCY_ID = 1;
	private final static int FETCH_PROJECT_TO_LOCAL_EXCHANGE = 2;

	private HyjModelEditor<MoneyAccount> mMoneyAccountEditor = null;
	private HyjTextField mTextFieldName = null;
	private HyjSelectorField mSelectorFieldCurrency = null;
	private HyjNumericField mNumericFieldCurrentBalance = null;
	private HyjSpinnerField mSpinnerFieldAccountType = null;
	private HyjRemarkField mRemarkFieldAccountNumber = null;
	private HyjRemarkField mRemarkFieldBankAddress = null;
	private HyjRemarkField mRemarkFieldRemark = null;

	@Override
	public Integer useContentView() {
		return R.layout.moneyaccount_formfragment_moneyaccount;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		MoneyAccount moneyAccount;

		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if (modelId != -1) {
			moneyAccount = new Select().from(MoneyAccount.class)
					.where("_id=?", modelId).executeSingle();

		} else {
			moneyAccount = new MoneyAccount();
		}
		mMoneyAccountEditor = moneyAccount.newModelEditor();

		mTextFieldName = (HyjTextField) getView().findViewById(
				R.id.moneyAccountFormFragment_textField_name);
		mTextFieldName.setText(moneyAccount.getDisplayName());
		mTextFieldName.setEnabled(modelId == -1
				|| !moneyAccount.getAccountType().equalsIgnoreCase("Debt"));

		Currency currency = moneyAccount.getCurrency();
		mSelectorFieldCurrency = (HyjSelectorField) getView().findViewById(
				R.id.moneyAccountFormFragment_selectorField_currency);
		mSelectorFieldCurrency.setEnabled(modelId == -1);
		if (currency != null) {
			mSelectorFieldCurrency.setModelId(currency.getId());
			mSelectorFieldCurrency.setText(currency.getName());
		}
		mSelectorFieldCurrency.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneyAccountFormFragment.this
						.openActivityWithFragmentForResult(
								CurrencyListFragment.class,
								R.string.currencyListFragment_title_select_currency,
								null, GET_CURRENCY_ID);
			}
		});

		mNumericFieldCurrentBalance = (HyjNumericField) getView().findViewById(
				R.id.moneyAccountFormFragment_textField_currentBalance);
		mNumericFieldCurrentBalance.setNumber(moneyAccount.getCurrentBalance());
		mNumericFieldCurrentBalance.setEnabled(modelId == -1
				|| !moneyAccount.getAccountType().equalsIgnoreCase("Debt"));

		mSpinnerFieldAccountType = (HyjSpinnerField) getView().findViewById(
				R.id.moneyAccountFormFragment_textField_accountType);
		mSpinnerFieldAccountType
				.setItems(
						R.array.moneyAccountFormFragment_spinnerField_accountType_array,
						new String[] { "Cash", "Deposit", "Credit", "Online"/*
																			 * ,
																			 * "Debt"
																			 */});
		mSpinnerFieldAccountType
				.setSelectedValue(moneyAccount.getAccountType());
		mSpinnerFieldAccountType.setEnabled(modelId == -1
				|| !moneyAccount.getAccountType().equalsIgnoreCase("Debt"));

		mRemarkFieldAccountNumber = (HyjRemarkField) getView().findViewById(
				R.id.moneyAccountFormFragment_textField_accountNumber);
		mRemarkFieldAccountNumber.setText(moneyAccount.getAccountNumber());
		mRemarkFieldAccountNumber.setEnabled(modelId == -1
				|| !moneyAccount.getAccountType().equalsIgnoreCase("Debt"));

		mRemarkFieldBankAddress = (HyjRemarkField) getView().findViewById(
				R.id.moneyAccountFormFragment_textField_bankAddress);
		mRemarkFieldBankAddress.setText(moneyAccount.getBankAddress());
		mRemarkFieldBankAddress.setEnabled(modelId == -1
				|| !moneyAccount.getAccountType().equalsIgnoreCase("Debt"));

		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(
				R.id.moneyAccountFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyAccount.getRemark());
		mRemarkFieldRemark.setEnabled(modelId == -1
				|| !moneyAccount.getAccountType().equalsIgnoreCase("Debt"));

		if (modelId == -1){
			this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}
	}

	private void fillData() {
		MoneyAccount modelCopy = (MoneyAccount) mMoneyAccountEditor
				.getModelCopy();
		modelCopy.setName(mTextFieldName.getText().toString().trim());
		modelCopy.setCurrencyId(mSelectorFieldCurrency.getModelId());
		modelCopy.setCurrentBalance(mNumericFieldCurrentBalance.getNumber());
		modelCopy.setAccountType(mSpinnerFieldAccountType.getSelectedValue());
		modelCopy.setAccountNumber(mRemarkFieldAccountNumber.getText()
				.toString().trim());
		modelCopy.setBankAddress(mRemarkFieldBankAddress.getText().toString()
				.trim());
		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
	}

	private void showValidatioErrors() {
		HyjUtil.displayToast(R.string.app_validation_error);

		mTextFieldName.setError(mMoneyAccountEditor.getValidationError("name"));
		mSelectorFieldCurrency.setError(mMoneyAccountEditor
				.getValidationError("currency"));
		mNumericFieldCurrentBalance.setError(mMoneyAccountEditor
				.getValidationError("currentBalance"));
		mSpinnerFieldAccountType.setError(mMoneyAccountEditor
				.getValidationError("accountType"));
		mRemarkFieldAccountNumber.setError(mMoneyAccountEditor
				.getValidationError("accountNumber"));
		mRemarkFieldBankAddress.setError(mMoneyAccountEditor
				.getValidationError("bankAddress"));
		mRemarkFieldRemark.setError(mMoneyAccountEditor
				.getValidationError("remark"));
	}

	@Override
	public void onSave(View v) {
		super.onSave(v);

		fillData();

		mMoneyAccountEditor.validate();

		if (mMoneyAccountEditor.hasValidationErrors()) {
			showValidatioErrors();
		} else {
			// 检查汇率存不存在
			final String moneyAccountCurrencyId = mMoneyAccountEditor.getModelCopy()
					.getCurrencyId();
			((HyjActivity) MoneyAccountFormFragment.this.getActivity())
					.displayProgressDialog(
							R.string.projectMessageFormFragment_addShare_fetch_exchange,
							R.string.projectMessageFormFragment_addShare_fetching_exchange);
			if (moneyAccountCurrencyId.equalsIgnoreCase(HyjApplication.getInstance()
					.getCurrentUser().getUserData().getActiveCurrencyId())) {
				// 币种是一样的，不用新增汇率
				doSave();
			} else {
				Exchange exchange = new Select().from(Exchange.class).where("foreignCurrencyId=? AND localCurrencyId=?",
								moneyAccountCurrencyId,
								HyjApplication.getInstance().getCurrentUser()
										.getUserData().getActiveCurrencyId())
						.executeSingle();
				if (exchange != null) {
					// 汇率已经存在，直接保存新项目
					doSave();
					return;
				}
				// 尝试到网上获取汇率
				HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
					@Override
					public void finishCallback(Object object) {
						// 到网上获取汇率成功，新建汇率然后保存
						((HyjActivity) MoneyAccountFormFragment.this
								.getActivity()).dismissProgressDialog();
						Double exchangeRate = (Double) object;
						Exchange newExchange = new Exchange();
						newExchange.setForeignCurrencyId(moneyAccountCurrencyId);
						newExchange.setLocalCurrencyId(HyjApplication
								.getInstance().getCurrentUser().getUserData()
								.getActiveCurrencyId());
						newExchange.setRate(exchangeRate);
						newExchange.save();
						doSave();
					}

					@Override
					public void errorCallback(Object object) {
						((HyjActivity) MoneyAccountFormFragment.this
								.getActivity()).dismissProgressDialog();
						if (object != null) {
							HyjUtil.displayToast(object.toString());
						} else {
							HyjUtil.displayToast(R.string.moneyExpenseFormFragment_toast_cannot_refresh_rate);
						}

						// 到网上获取汇率失败，问用户是否要手工添加该汇率
						((HyjActivity) MoneyAccountFormFragment.this
								.getActivity())
								.displayDialog(
										-1,
										R.string.projectMessageFormFragment_addShare_cannot_fetch_exchange,
										R.string.alert_dialog_yes,
										R.string.alert_dialog_no, -1,
										new DialogCallbackListener() {
											@Override
											public void doPositiveClick(
													Object object) {
												Bundle bundle = new Bundle();
												bundle.putString(
														"localCurrencyId",
														HyjApplication
																.getInstance()
																.getCurrentUser()
																.getUserData()
																.getActiveCurrencyId());
												bundle.putString(
														"foreignCurrencyId",
														moneyAccountCurrencyId);
												openActivityWithFragmentForResult(
														ExchangeFormFragment.class,
														R.string.exchangeFormFragment_title_addnew,
														bundle,
														FETCH_PROJECT_TO_LOCAL_EXCHANGE);
											}

											@Override
											public void doNegativeClick() {
												HyjUtil.displayToast("未能获取项目币种到本币的汇率");
											}

										});
					}
				};
				HyjHttpGetExchangeRateAsyncTask.newInstance(HyjApplication.getInstance().getCurrentUser()
						.getUserData().getActiveCurrencyId(),
						moneyAccountCurrencyId,
						serverCallbacks);
			}
		}
	}

	protected void doSave() {
		Double changeAmount = mMoneyAccountEditor.getModelCopy().getCurrentBalance0() - mMoneyAccountEditor.getModel().getCurrentBalance0();
		if(mMoneyAccountEditor.getModelCopy().get_mId() != null && changeAmount != 0){
			MoneyTransfer newMoneyTransfer = new MoneyTransfer();
			if(changeAmount > 0){
				newMoneyTransfer.setTransferOutAmount(changeAmount);
				newMoneyTransfer.setTransferOutId("null");
				newMoneyTransfer.setTransferInAmount(changeAmount);
				newMoneyTransfer.setTransferInId(mMoneyAccountEditor.getModelCopy().getId());
			}else{
				newMoneyTransfer.setTransferOutAmount(-changeAmount);
				newMoneyTransfer.setTransferOutId(mMoneyAccountEditor.getModelCopy().getId());
				newMoneyTransfer.setTransferInAmount(-changeAmount);
				newMoneyTransfer.setTransferInId("null");
			}
			newMoneyTransfer.setDate(HyjUtil.formatDateToIOS(new Date()));
			newMoneyTransfer.setTransferOutFriendUserId(null);
			newMoneyTransfer.setTransferInFriendUserId(null);
			newMoneyTransfer.setRemark("修改账户金额");
			newMoneyTransfer.save();
		}
		
		mMoneyAccountEditor.save();
		HyjUtil.displayToast(R.string.app_save_success);
		getActivity().finish();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case GET_CURRENCY_ID:
			if (resultCode == Activity.RESULT_OK) {
				HyjUtil.displayToast(String.valueOf(data.getLongExtra(
						"MODEL_ID", -1)));
				long _id = data.getLongExtra("MODEL_ID", -1);
				Currency currency = Currency.load(Currency.class, _id);
				mSelectorFieldCurrency.setText(currency.getName());
				mSelectorFieldCurrency.setModelId(currency.getId());
			}
			break;

		case FETCH_PROJECT_TO_LOCAL_EXCHANGE:
			if (resultCode == Activity.RESULT_OK) {
				// 检查该汇率是否添加成功，如果是保存
				Exchange exchange = new Select()
						.from(Exchange.class)
						.where("foreignCurrencyId=? AND localCurrencyId=?",
								mMoneyAccountEditor.getModelCopy()
										.getCurrencyId(),
								HyjApplication.getInstance().getCurrentUser()
										.getUserData().getActiveCurrencyId())
						.executeSingle();
				if (exchange != null) {
					doSave();
				} else {
					HyjUtil.displayToast("未能获取项目币种到本币的汇率");
				}
			}
			break;

		}
	}
}
