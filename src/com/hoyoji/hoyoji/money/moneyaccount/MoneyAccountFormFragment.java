package com.hoyoji.hoyoji.money.moneyaccount;

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
import com.hoyoji.hoyoji.models.MoneyAccount;


public class MoneyAccountFormFragment extends HyjUserFormFragment {
	private final static int GET_CURRENCY_ID = 1;
	
	private HyjModelEditor mMoneyAccountEditor = null;
	private EditText mEditTextname = null;
	private EditText mEditTextCurrency = null;
	
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
		
		mEditTextname = (EditText) getView().findViewById(R.id.moneyAccountFormFragment_editText_name);
		mEditTextname.setText(moneyAccount.getName());
		
		mEditTextCurrency = (EditText) getView().findViewById(R.id.moneyAccountFormFragment_editText_currency);
		mEditTextCurrency.setText("");
		mEditTextCurrency.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyAccountFormFragment.this
				.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_currency, null, GET_CURRENCY_ID);
			}
		});
		
		
	}
	
	private void fillData(){
		MoneyAccount modelCopy = (MoneyAccount) mMoneyAccountEditor.getModelCopy();
		modelCopy.setName(mEditTextname.getText().toString().trim());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		
		mEditTextname.setError(mMoneyAccountEditor.getValidationError("name"));
		
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
            	//	 ((Project)mProjectEditor.getModelCopy()).s
            	 }
             case 2:

          }
    }
}
