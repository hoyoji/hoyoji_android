package com.hoyoji.hoyoji.money.moneyaccount;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Currency;
import com.hoyoji.hoyoji.models.MoneyAccount;

public class MoneyAccountFormFragment extends HyjUserFormFragment {
	private final static int GET_CURRENCY_ID = 1;
	
	private HyjModelEditor mMoneyAccountEditor = null;
	private HyjTextField mTextFieldName = null;
	private HyjSelectorField mSelectorFieldCurrency = null;
	private HyjNumericField mNumericFieldCurrentBalance = null;
	private HyjTextField mTextFieldAccountType = null;
	private HyjRemarkField mRemarkFieldAccountNumber = null;
	private HyjRemarkField mRemarkFieldBankAddress = null;
	private HyjRemarkField mRemarkFieldRemark = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.moneyaccount_formfragment_moneyaccount;
	}
	
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		MoneyAccount moneyAccount;
		
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			moneyAccount =  new Select().from(MoneyAccount.class).where("_id=?", modelId).executeSingle();
		} else {
			moneyAccount = new MoneyAccount();
		}
		mMoneyAccountEditor = moneyAccount.newModelEditor();
		
		mTextFieldName = (HyjTextField) getView().findViewById(R.id.moneyAccountFormFragment_textField_name);
		mTextFieldName.setText(moneyAccount.getName());
		
		Currency currency = moneyAccount.getCurrency();
		mSelectorFieldCurrency = (HyjSelectorField) getView().findViewById(R.id.moneyAccountFormFragment_selectorField_currency);
		
		if(currency != null){
			mSelectorFieldCurrency.setModelId(currency.getId());
			mSelectorFieldCurrency.setText(currency.getName());
		}
		mSelectorFieldCurrency.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyAccountFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, null, GET_CURRENCY_ID);
			}
		});	
		
		mNumericFieldCurrentBalance = (HyjNumericField) getView().findViewById(R.id.moneyAccountFormFragment_textField_currentBalance);		
		mNumericFieldCurrentBalance.setNumber(moneyAccount.getCurrentBalance());
		
		mTextFieldAccountType = (HyjTextField) getView().findViewById(R.id.moneyAccountFormFragment_textField_accountType);
		mTextFieldAccountType.setText(moneyAccount.getAccountType());
		
		mRemarkFieldAccountNumber = (HyjRemarkField) getView().findViewById(R.id.moneyAccountFormFragment_textField_accountNumber);
		mRemarkFieldAccountNumber.setText(moneyAccount.getAccountNumber());
		
		mRemarkFieldBankAddress = (HyjRemarkField) getView().findViewById(R.id.moneyAccountFormFragment_textField_bankAddress);
		mRemarkFieldBankAddress.setText(moneyAccount.getBankAddress());
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyAccountFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyAccount.getRemark());
		
	}
	
	private void fillData(){
		MoneyAccount modelCopy = (MoneyAccount) mMoneyAccountEditor.getModelCopy();
		modelCopy.setName(mTextFieldName.getText().toString().trim());
		modelCopy.setCurrencyId(mSelectorFieldCurrency.getModelId());
		modelCopy.setCurrentBalance(mNumericFieldCurrentBalance.getNumber());
		modelCopy.setAccountType(mTextFieldAccountType.getText().toString().trim());
		modelCopy.setAccountNumber(mRemarkFieldAccountNumber.getText().toString().trim());
		modelCopy.setBankAddress(mRemarkFieldBankAddress.getText().toString().trim());
		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		
		mTextFieldName.setError(mMoneyAccountEditor.getValidationError("name"));
		mSelectorFieldCurrency.setError(mMoneyAccountEditor.getValidationError("currency"));
		mNumericFieldCurrentBalance.setError(mMoneyAccountEditor.getValidationError("currentBalance"));
		mTextFieldAccountType.setError(mMoneyAccountEditor.getValidationError("accountType"));
		mRemarkFieldAccountNumber.setError(mMoneyAccountEditor.getValidationError("accountNumber"));
		mRemarkFieldBankAddress.setError(mMoneyAccountEditor.getValidationError("bankAddress"));
		mRemarkFieldRemark.setError(mMoneyAccountEditor.getValidationError("remark"));
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		mMoneyAccountEditor.validate();
		
		if(mMoneyAccountEditor.hasValidationErrors()){
			showValidatioErrors();
		} else {
			mMoneyAccountEditor.save();
			HyjUtil.displayToast(R.string.app_save_success);
			getActivity().finish();
		}
	}	
	
	 @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode){
             case GET_CURRENCY_ID:
            	 if(resultCode == Activity.RESULT_OK){
            		 HyjUtil.displayToast(String.valueOf(data.getLongExtra("MODEL_ID", -1)));
            		 long _id = data.getLongExtra("MODEL_ID", -1);
 	         		 Currency currency = Currency.load(Currency.class, _id);
 	         		 mSelectorFieldCurrency.setText(currency.getName());
 	         		 mSelectorFieldCurrency.setModelId(currency.getId());
            	 }
             case 2:

          }
    }
}
