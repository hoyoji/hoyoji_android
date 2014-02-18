package com.hoyoji.hoyoji.money.currency;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Currency;


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
	public void onInitViewData(){
		super.onInitViewData();
		Currency currency;
		
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			currency =  new Select().from(Currency.class).where("_id=?", modelId).executeSingle();
		} else {
			currency = new Currency();
		}
		mCurrencyEditor = currency.newModelEditor();
		
		mTextFieldName = (HyjTextField) getView().findViewById(R.id.currencyFormFragment_textField_name);
		mTextFieldName.setText(currency.getName());
		
		mTextFieldSymbol = (HyjTextField) getView().findViewById(R.id.currencyFormFragment_textField_symbol);
		mTextFieldSymbol.setText(currency.getSymbol());
		
		mTextFieldCode = (HyjTextField) getView().findViewById(R.id.currencyFormFragment_textField_code);
		mTextFieldCode.setText(currency.getCode());
		
	}
	
	private void fillData(){
		Currency modelCopy = (Currency) mCurrencyEditor.getModelCopy();
		modelCopy.setName(mTextFieldName.getText().toString().trim());
		modelCopy.setSymbol(mTextFieldSymbol.getText().toString().trim());
		modelCopy.setCode(mTextFieldCode.getText().toString().trim());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		
		mTextFieldName.setError(mCurrencyEditor.getValidationError("name"));
		mTextFieldSymbol.setError(mCurrencyEditor.getValidationError("symbol"));
		mTextFieldCode.setError(mCurrencyEditor.getValidationError("code"));
		
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		mCurrencyEditor.validate();
		
		if(mCurrencyEditor.hasValidationErrors()){
			showValidatioErrors();
		} else {
			mCurrencyEditor.save();
			HyjUtil.displayToast(R.string.app_save_success);
			getActivity().finish();
		}
	}	
}
