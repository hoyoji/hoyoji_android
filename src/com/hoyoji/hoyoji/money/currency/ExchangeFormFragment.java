package com.hoyoji.hoyoji.money.currency;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Exchange;


public class ExchangeFormFragment extends HyjUserFormFragment {
	private final static int GET_LOCAL_CURRENCY_ID = 1;
	private final static int GET_FOREIGN_CURRENCY_ID = 1;
	
	private HyjModelEditor mExchangeEditor = null;
	private EditText mEditTextExchangeLoaclhostCurrency = null;
	private EditText mEditTextExchangeForeignCurrency = null;
	private EditText mEditTextExchangeRate = null;
	private EditText mEditTextExchangeAutoUpdate = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.exchange_formfragment_exchange;
	}
	
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		Exchange exchange;
		
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			exchange =  new Select().from(Exchange.class).where("_id=?", modelId).executeSingle();
		} else {
			exchange = new Exchange();
		}
		mExchangeEditor = exchange.newModelEditor();
		
		mEditTextExchangeLoaclhostCurrency = (EditText) getView().findViewById(R.id.exchangeFormFragment_editText_localCurrency);
		mEditTextExchangeLoaclhostCurrency.setText(exchange.getLocalCurrencyId());
		
		mEditTextExchangeForeignCurrency = (EditText) getView().findViewById(R.id.exchangeFormFragment_editText_foreignCurrency);
		mEditTextExchangeForeignCurrency.setText("");
		mEditTextExchangeForeignCurrency.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ExchangeFormFragment.this
				.openActivityWithFragmentForResult(ExchangeListFragment.class, R.string.exchangeListFragment_title_select_foreign_currency, null, GET_FOREIGN_CURRENCY_ID);
			}
		});
		mEditTextExchangeRate = (EditText) getView().findViewById(R.id.exchangeFormFragment_editText_rate);
		mEditTextExchangeRate.setText(exchange.getRate());
		
		mEditTextExchangeAutoUpdate = (EditText) getView().findViewById(R.id.exchangeFormFragment_editText_autoUpdate);
		mEditTextExchangeAutoUpdate.setText(exchange.getAutoUpdate());
		
	}
	
//	private void fillData(){
//		Exchange modelCopy = (Exchange) mExchangeEditor.getModelCopy();
//		modelCopy.setName(mEditTextExchangeLoaclhostCurrency.getText().toString().trim());
//	}
	
//	private void showValidatioErrors(){
//		HyjUtil.displayToast(R.string.app_validation_error);
//		
//		mEditTextExchangeLoaclhostCurrency.setError(mExchangeEditor.getValidationError("name"));
//		
//	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
//		fillData();
		
		mExchangeEditor.validate();
		
//		if(mExchangeEditor.hasValidationErrors()){
//			showValidatioErrors();
//		} else {
			mExchangeEditor.save();
			HyjUtil.displayToast(R.string.app_save_success);
			getActivity().finish();
//		}
	}	
	
	 @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode){
             case GET_LOCAL_CURRENCY_ID:
            	 if(resultCode == Activity.RESULT_OK){
            		 HyjUtil.displayToast(String.valueOf(data.getLongExtra("MODEL_ID", -1)));
            	//	 ((Project)mProjectEditor.getModelCopy()).s
            	 }
             case 2:

          }
    }
}
