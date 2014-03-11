package com.hoyoji.hoyoji.money.currency;

import org.json.JSONObject;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.RotateDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjHttpGetExchangeRateAsyncTask;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.RegisterActivity;
import com.hoyoji.hoyoji.friend.FriendCategoryListFragment;
import com.hoyoji.hoyoji.friend.FriendFormFragment;
import com.hoyoji.hoyoji.models.Currency;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.FriendCategory;

public class ExchangeFormFragment extends HyjUserFormFragment {
	private final static int GET_LOCAL_CURRENCY_ID = 1;
	private final static int GET_FOREIGN_CURRENCY_ID = 2;

	private HyjModelEditor mExchangeEditor = null;
	private HyjSelectorField mSelectorFieldLocalCurrency = null;
	private HyjSelectorField mSelectorFieldForeignCurrency = null;
	private HyjNumericField mNumericFieldRate = null;
	private CheckBox mCheckBoxAutoUpdate = null;
	private ImageView mImageViewRefreshRate = null;

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

		mSelectorFieldLocalCurrency = (HyjSelectorField) getView().findViewById(
				R.id.exchangeFormFragment_editText_localCurrency);
		Currency localCurrency = exchange.getLocalCurrency();
		if (localCurrency != null) {
			mSelectorFieldLocalCurrency.setText(localCurrency.getName());
			mSelectorFieldLocalCurrency.setModelId(exchange.getLocalCurrencyId());
		}
		mSelectorFieldLocalCurrency.setEnabled(modelId == -1);
		mSelectorFieldLocalCurrency.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ExchangeFormFragment.this
						.openActivityWithFragmentForResult(
								CurrencyListFragment.class,
								R.string.exchangeListFragment_title_select_local_currency,
								null, GET_LOCAL_CURRENCY_ID);
			}
		});
		
		mSelectorFieldForeignCurrency = (HyjSelectorField) getView().findViewById(
				R.id.exchangeFormFragment_editText_foreignCurrency);
		Currency foreignCurrency = exchange.getForeignCurrency();
		if (foreignCurrency != null) {
			mSelectorFieldForeignCurrency.setText(foreignCurrency.getName());
			mSelectorFieldForeignCurrency
					.setModelId(exchange.getForeignCurrencyId());
		}
		mSelectorFieldForeignCurrency.setEnabled(modelId == -1);
		mSelectorFieldForeignCurrency.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ExchangeFormFragment.this
						.openActivityWithFragmentForResult(
								CurrencyListFragment.class,
								R.string.exchangeListFragment_title_select_foreign_currency,
								null, GET_FOREIGN_CURRENCY_ID);
			}
		});

		mNumericFieldRate = (HyjNumericField) getView().findViewById(
				R.id.exchangeFormFragment_editText_rate);
		mNumericFieldRate.setNumber(exchange.getRate());

		mCheckBoxAutoUpdate = (CheckBox) getView().findViewById(
				R.id.exchangeFormFragment_checkBox_autoUpdate);
		mCheckBoxAutoUpdate.setChecked(exchange.getAutoUpdate());
		mCheckBoxAutoUpdate.setChecked(exchange.getAutoUpdate());

		setupRefreshRateButton();
	}

	private void setupRefreshRateButton(){
		mImageViewRefreshRate = (ImageView) getView().findViewById(
				R.id.exchangeFormFragment_imageView_refresh_rate);
		mImageViewRefreshRate.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String fromCurrency = mSelectorFieldLocalCurrency.getModelId();
				String toCurrency = mSelectorFieldForeignCurrency.getModelId();
				if(fromCurrency != null && toCurrency != null){
//					HyjUtil.startRoateView(mImageViewRefreshRate);
//					mImageViewRefreshRate.setEnabled(false);
//					HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks(){
//						@Override
//						public void finishCallback(Object object) {
//							HyjUtil.stopRoateView(mImageViewRefreshRate);
//							mImageViewRefreshRate.setEnabled(true);
//							mNumericFieldRate.setNumber((Double)object);
//						}
//						@Override
//						public void errorCallback(Object object) {
//							HyjUtil.stopRoateView(mImageViewRefreshRate);
//							mImageViewRefreshRate.setEnabled(true);
//							if(object != null){
//								HyjUtil.displayToast(object.toString());
//							} else {
//								HyjUtil.displayToast(R.string.exchangeFormFragment_toast_cannot_refresh_rate);
//							}
//						}
//					};
//					HyjHttpGetExchangeRateAsyncTask.newInstance(fromCurrency, toCurrency, serverCallbacks);
					
					HyjUtil.updateExchangeRate(fromCurrency, toCurrency, mImageViewRefreshRate, mNumericFieldRate);
					
				} else {
					HyjUtil.displayToast(R.string.exchangeFormFragment_toast_select_currency);
				}
			}
		});
	}
	
	 private void fillData(){
		 Exchange modelCopy = (Exchange) mExchangeEditor.getModelCopy();
		 modelCopy.setAutoUpdate(this.mCheckBoxAutoUpdate.isChecked());
		 modelCopy.setLocalCurrencyId(mSelectorFieldLocalCurrency.getModelId());
		 modelCopy.setForeignCurrencyId(mSelectorFieldForeignCurrency.getModelId());
		 modelCopy.setRate(mNumericFieldRate.getNumber());
	 }

	 private void showValidatioErrors(){
		 HyjUtil.displayToast(R.string.app_validation_error);
		 mSelectorFieldLocalCurrency.setError(mExchangeEditor.getValidationError("localCurrency"));
		 mSelectorFieldForeignCurrency.setError(mExchangeEditor.getValidationError("foreignCurrency"));
		 mNumericFieldRate.setError(mExchangeEditor.getValidationError("rate"));
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
				Currency localCurrency = Currency.load(Currency.class, _id);
				mSelectorFieldLocalCurrency.setText(localCurrency.getName());
				mSelectorFieldLocalCurrency.setModelId(localCurrency.getId());
			}
			break;
		case GET_FOREIGN_CURRENCY_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				Currency foreignCurrency = Currency.load(Currency.class, _id);
				mSelectorFieldForeignCurrency.setText(foreignCurrency.getName());
				mSelectorFieldForeignCurrency.setModelId(foreignCurrency.getId());
			}
			break;
		}
	}
}
