package com.hoyoji.hoyoji.money.currency;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.friend.FriendCategoryListFragment;
import com.hoyoji.hoyoji.friend.FriendFormFragment;
import com.hoyoji.hoyoji.models.Currency;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.FriendCategory;

public class ExchangeFormFragment extends HyjUserFormFragment {
	private final static int GET_LOCAL_CURRENCY_ID = 1;
	private final static int GET_FOREIGN_CURRENCY_ID = 2;

	private HyjModelEditor mExchangeEditor = null;
	private HyjSelectorField mEditTextLocalCurrency = null;
	private HyjSelectorField mEditTextForeignCurrency = null;
	private HyjNumericField mEditTextRate = null;
	private CheckBox mEditTextAutoUpdate = null;

	@Override
	public Integer useContentView() {
		return R.layout.exchange_formfragment_exchange;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		Exchange exchange;

		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if (modelId != -1) {
			exchange = new Select().from(Exchange.class)
					.where("_id=?", modelId).executeSingle();
		} else {
			exchange = new Exchange();
		}
		mExchangeEditor = exchange.newModelEditor();

		mEditTextLocalCurrency = (HyjSelectorField) getView().findViewById(
				R.id.exchangeFormFragment_editText_localCurrency);
		Currency localCurrency = exchange.getLocalCurrency();
		if (localCurrency != null) {
			mEditTextLocalCurrency.setText(localCurrency.getName());
			mEditTextLocalCurrency.setModelId(exchange.getLocalCurrencyId());
		}
		mEditTextLocalCurrency.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ExchangeFormFragment.this
						.openActivityWithFragmentForResult(
								CurrencyListFragment.class,
								R.string.exchangeListFragment_title_select_local_currency,
								null, GET_LOCAL_CURRENCY_ID);
			}
		});

		mEditTextForeignCurrency = (HyjSelectorField) getView().findViewById(
				R.id.exchangeFormFragment_editText_foreignCurrency);
		Currency foreignCurrency = exchange.getForeignCurrency();
		if (foreignCurrency != null) {
			mEditTextForeignCurrency.setText(foreignCurrency.getName());
			mEditTextForeignCurrency
					.setModelId(exchange.getForeignCurrencyId());
		}
		mEditTextForeignCurrency.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ExchangeFormFragment.this
						.openActivityWithFragmentForResult(
								CurrencyListFragment.class,
								R.string.exchangeListFragment_title_select_foreign_currency,
								null, GET_FOREIGN_CURRENCY_ID);
			}
		});

		mEditTextRate = (HyjNumericField) getView().findViewById(
				R.id.exchangeFormFragment_editText_rate);
		mEditTextRate.setNumber(exchange.getRate());

		mEditTextAutoUpdate = (CheckBox) getView().findViewById(
				R.id.exchangeFormFragment_checkBox_autoUpdate);
		mEditTextAutoUpdate.setChecked(exchange.getAutoUpdate());

	}

	 private void fillData(){
		 Exchange modelCopy = (Exchange) mExchangeEditor.getModelCopy();
		 modelCopy.setAutoUpdate(this.mEditTextAutoUpdate.isChecked());
		 modelCopy.setLocalCurrencyId(mEditTextLocalCurrency.getModelId());
		 modelCopy.setForeignCurrencyId(mEditTextForeignCurrency.getModelId());
		 modelCopy.setRate(mEditTextRate.getNumber());
	 }

	 private void showValidatioErrors(){
		 HyjUtil.displayToast(R.string.app_validation_error);
		 mEditTextLocalCurrency.setError(mExchangeEditor.getValidationError("localCurrency"));
		 mEditTextForeignCurrency.setError(mExchangeEditor.getValidationError("foreignCurrency"));
		 mEditTextRate.setError(mExchangeEditor.getValidationError("rate"));
	 }

	@Override
	public void onSave(View v) {
		super.onSave(v);

		 fillData();

		mExchangeEditor.validate();

		 if(mExchangeEditor.hasValidationErrors()){
			 showValidatioErrors();
		 } else {
			mExchangeEditor.save();
			HyjUtil.displayToast(R.string.app_save_success);
			getActivity().finish();
		 }
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case GET_LOCAL_CURRENCY_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				if(_id < 0){
					return;
				}
				Currency localCurrency = Currency.load(Currency.class, _id);
				mEditTextLocalCurrency.setText(localCurrency.getName());
				mEditTextLocalCurrency.setModelId(localCurrency.getId());
			}
			break;
		case GET_FOREIGN_CURRENCY_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				if(_id < 0){
					return;
				}
				Currency foreignCurrency = Currency.load(Currency.class, _id);
				mEditTextForeignCurrency.setText(foreignCurrency.getName());
				mEditTextForeignCurrency.setModelId(foreignCurrency.getId());
			}
			break;
		}
	}
}
