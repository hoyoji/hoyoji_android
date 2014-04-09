package com.hoyoji.hoyoji.money.currency;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjHttpGetExchangeRateAsyncTask;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Currency;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.project.ProjectFormFragment;

public class CurrencyFormFragment extends HyjUserFormFragment {

	private HyjModelEditor mCurrencyEditor = null;
	private HyjTextField mTextFieldName = null;
	private HyjTextField mTextFieldSymbol = null;
	private HyjTextField mTextFieldCode = null;

	@Override
	public Integer useContentView() {
		return R.layout.currency_formfragment_currency;
	}

	@Override
	public Integer useOptionsMenuView() {
		return null;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		Currency currency;

		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if (modelId != -1) {
			currency = new Select().from(Currency.class)
					.where("_id=?", modelId).executeSingle();
		} else {
			currency = new Currency();
		}
		mCurrencyEditor = currency.newModelEditor();

		mTextFieldName = (HyjTextField) getView().findViewById(
				R.id.currencyFormFragment_textField_name);
		mTextFieldName.setText(currency.getName());
		mTextFieldName.setEnabled(modelId == -1);

		mTextFieldSymbol = (HyjTextField) getView().findViewById(
				R.id.currencyFormFragment_textField_symbol);
		mTextFieldSymbol.setText(currency.getSymbol());
		mTextFieldSymbol.setEnabled(modelId == -1);

		mTextFieldCode = (HyjTextField) getView().findViewById(
				R.id.currencyFormFragment_textField_code);
		mTextFieldCode.setText(currency.getCode());
		mTextFieldCode.setEnabled(modelId == -1);

		Button setAsLocalCurrency = (Button) getView().findViewById(
				R.id.currencyFormFragment_button_setAsLocalCurrency);
		if (currency.getId().equalsIgnoreCase(
				HyjApplication.getInstance().getCurrentUser().getUserData()
						.getActiveCurrencyId())) {
			setAsLocalCurrency.setClickable(false);
			setAsLocalCurrency.setTextColor(Color.GRAY);
		}
		setAsLocalCurrency.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setLocalCurrency();
			}
		});

		// if(modelId == -1){
		// getView().findViewById(R.id.button_save).setVisibility(View.GONE);
		// }

	}

	protected void setLocalCurrency() {
		final String currentCurrencyId = mCurrencyEditor.getModelCopy().getId();

		try {
			ActiveAndroid.beginTransaction();
//			((HyjActivity)CurrencyFormFragment.this.getActivity()).displayProgressDialog(R.string.currencyFormFragment_addShare_fetch_exchange, R.string.currencyFormFragment_addShare_fetching_exchange);
//			if (!currentCurrencyId.equalsIgnoreCase(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId())) {
//				
//					List<Currency> currencies = HyjApplication.getInstance().getCurrentUser().getUserData().getCurrencies();
//					for (Iterator<Currency> it = currencies.iterator(); it.hasNext();) {
//						final Currency currency = it.next();
//						if (Exchange.getExchangeRate(currency.getId(),currentCurrencyId) == null) {
//							// 尝试到网上获取汇率
//							HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
//								@Override
//								public void finishCallback(Object object) {
//									// 到网上获取汇率成功，新建汇率然后保存
//									((HyjActivity)CurrencyFormFragment.this.getActivity()).dismissProgressDialog();
//									Double exchangeRate = (Double) object;
//									Exchange newExchange = new Exchange();
//									newExchange.setForeignCurrencyId(currency.getId());
//									newExchange.setLocalCurrencyId(currentCurrencyId);
//									newExchange.setRate(exchangeRate);
//									newExchange.save();
//								}
//
//								@Override
//								public void errorCallback(Object object) {
//									
//									((HyjActivity)CurrencyFormFragment.this.getActivity()).dismissProgressDialog();
//									if (object != null) {
//										HyjUtil.displayToast(object.toString());
//									} else {
//										HyjUtil.displayToast(R.string.currencyFormFragment_addShare_cannot_fetch_exchange);
//									}
//								}
//							};
//							HyjHttpGetExchangeRateAsyncTask.newInstance(currency.getId(),currentCurrencyId,serverCallbacks);
//						}
//					}
//			}
				HyjModelEditor<UserData> userDataEditor = HyjApplication.getInstance().getCurrentUser().getUserData().newModelEditor();
				userDataEditor.getModelCopy().setActiveCurrencyId(currentCurrencyId);
				userDataEditor.save();
				ActiveAndroid.endTransaction();
				getActivity().finish();
			} catch (Exception e) {
//				((HyjActivity)CurrencyFormFragment.this.getActivity()).dismissProgressDialog();
				ActiveAndroid.endTransaction();
				HyjUtil.displayToast(R.string.currencyFormFragment_addShare_cannot_fetch_exchange);
			}

	}

	// private void fillData(){
	// Currency modelCopy = (Currency) mCurrencyEditor.getModelCopy();
	// modelCopy.setName(mTextFieldName.getText().toString().trim());
	// modelCopy.setSymbol(mTextFieldSymbol.getText().toString().trim());
	// modelCopy.setCode(mTextFieldCode.getText().toString().trim());
	// }
	//
	// private void showValidatioErrors(){
	// HyjUtil.displayToast(R.string.app_validation_error);
	//
	// mTextFieldName.setError(mCurrencyEditor.getValidationError("name"));
	// mTextFieldSymbol.setError(mCurrencyEditor.getValidationError("symbol"));
	// mTextFieldCode.setError(mCurrencyEditor.getValidationError("code"));
	// }

	// @Override
	// public void onSave(View v){
	// super.onSave(v);
	//
	// if(mCurrencyEditor.getModel().get_mId() != null){
	// return;
	// }
	//
	// fillData();
	//
	// mCurrencyEditor.validate();
	//
	// if(mCurrencyEditor.hasValidationErrors()){
	// showValidatioErrors();
	// } else {
	// mCurrencyEditor.save();
	// HyjUtil.displayToast(R.string.app_save_success);
	// getActivity().finish();
	// }
	// }
}
