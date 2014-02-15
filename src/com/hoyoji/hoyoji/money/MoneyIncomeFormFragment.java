package com.hoyoji.hoyoji.money;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeField;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyIncome;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;


public class MoneyIncomeFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_FRIEND_ID = 3;
	
	private HyjModelEditor mMoneyIncomeEditor = null;
	private HyjTextField mTextFieldPicture = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericAmount = null;
	private HyjSelectorField mSelectorFieldMoneyAccount = null;
	private HyjSelectorField mSelectorFieldProject = null;
	private HyjNumericField mNumericExchangeRate = null;
	private HyjTextField mTextFieldMoneyIncomeCategory = null;
	private HyjSelectorField mSelectorFieldFriend = null;
	private HyjRemarkField mRemarkFieldRemark = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.money_formfragment_moneyincome;
	}
	 
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		MoneyIncome moneyIncome;
		
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			moneyIncome =  new Select().from(MoneyIncome.class).where("_id=?", modelId).executeSingle();
		} else {
			moneyIncome = new MoneyIncome();
		}
		mMoneyIncomeEditor = moneyIncome.newModelEditor();
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_date);		
		
		mNumericAmount = (HyjNumericField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_amount);		
		mNumericAmount.setNumber(moneyIncome.getAmount());
		
		MoneyAccount moneyAccount = moneyIncome.getMoneyAccount();
		mSelectorFieldMoneyAccount = (HyjSelectorField) getView().findViewById(R.id.moneyIncomeFormFragment_selectorField_moneyAccount);
		
		if(moneyAccount != null){
			mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
			mSelectorFieldMoneyAccount.setText(moneyAccount.getName());
		}
		mSelectorFieldMoneyAccount.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyIncomeFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, null, GET_MONEYACCOUNT_ID);
			}
		});	
		
		Project project = moneyIncome.getProject();
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(R.id.moneyIncomeFormFragment_selectorField_project);
		
		if(project != null){
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getName());
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyIncomeFormFragment.this.openActivityWithFragmentForResult(ProjectListFragment.class, R.string.projectListFragment_title_select_project, null, GET_PROJECT_ID);
			}
		});	
		
		mNumericExchangeRate = (HyjNumericField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_exchangeRate);		
		mNumericExchangeRate.setNumber(moneyIncome.getExchangeRate());
		//mNumericExchangeRate.setVisibility(View.GONE);
		
		mTextFieldMoneyIncomeCategory = (HyjTextField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_moneyIncomeCategory);
		mTextFieldMoneyIncomeCategory.setText(moneyIncome.getMoneyIncomeCategory());
		
		Friend friend = moneyIncome.getFriend();
		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.moneyIncomeFormFragment_selectorField_friend);
		
		if(friend != null){
			mSelectorFieldFriend.setModelId(friend.getId());
			mSelectorFieldFriend.setText(friend.getNickName());
		}
		mSelectorFieldFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyIncomeFormFragment.this
				.openActivityWithFragmentForResult(FriendListFragment.class, R.string.friendListFragment_title_select_friend_payee, null, GET_FRIEND_ID);
			}
		}); 
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyIncome.getRemark());
		
		this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}
	
	private void fillData(){
		MoneyIncome modelCopy = (MoneyIncome) mMoneyIncomeEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setAmount(mNumericAmount.getNumber());
		modelCopy.setMoneyAccountId(mSelectorFieldMoneyAccount.getModelId());
		modelCopy.setProjectId(mSelectorFieldProject.getModelId());
		modelCopy.setExchangeRate(mNumericExchangeRate.getNumber());
		modelCopy.setMoneyIncomeCategory(mTextFieldMoneyIncomeCategory.getText().toString().trim());
		
		if(mSelectorFieldFriend.getModelId() != null){
			Friend friend = (Friend)HyjModel.getModel(Friend.class, mSelectorFieldFriend.getModelId());
			if(friend.getFriendUserId() != null){
				modelCopy.setFriendUserId(mSelectorFieldFriend.getModelId());
			}
			else{
				modelCopy.setLocalFriendId(mSelectorFieldFriend.getModelId());
			}
		}
		
		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
		
		HyjUtil.displayToast(this.mDateTimeFieldDate.getText().toString());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		mDateTimeFieldDate.setError(mMoneyIncomeEditor.getValidationError("datetime"));
		mNumericAmount.setError(mMoneyIncomeEditor.getValidationError("amount"));
		mSelectorFieldMoneyAccount.setError(mMoneyIncomeEditor.getValidationError("moneyAccount"));
		mSelectorFieldProject.setError(mMoneyIncomeEditor.getValidationError("project"));
		mNumericExchangeRate.setError(mMoneyIncomeEditor.getValidationError("exchangeRate"));
		mTextFieldMoneyIncomeCategory.setError(mMoneyIncomeEditor.getValidationError("moneyIncomeCategory"));
		mSelectorFieldFriend.setError(mMoneyIncomeEditor.getValidationError("friend"));
		mRemarkFieldRemark.setError(mMoneyIncomeEditor.getValidationError("remark"));
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		mMoneyIncomeEditor.validate();
		
		if(mMoneyIncomeEditor.hasValidationErrors()){
			showValidatioErrors();
		} else {
			mMoneyIncomeEditor.save();
			HyjUtil.displayToast(R.string.app_save_success);
			getActivity().finish();
		}
	}	
	
	 @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode){
             case GET_MONEYACCOUNT_ID:
	        	 if(resultCode == Activity.RESULT_OK){
	         		long _id = data.getLongExtra("MODEL_ID", -1);
	         		MoneyAccount moneyAccount = MoneyAccount.load(MoneyAccount.class, _id);
	         		mSelectorFieldMoneyAccount.setText(moneyAccount.getName());
	         		mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
	         	 }
	        	 break;
             case GET_PROJECT_ID:
	        	 if(resultCode == Activity.RESULT_OK){
	         		long _id = data.getLongExtra("MODEL_ID", -1);
	         		Project project = Project.load(Project.class, _id);
	         		mSelectorFieldProject.setText(project.getName());
	         		mSelectorFieldProject.setModelId(project.getId());
	         	 }
	        	 break;
             case GET_FRIEND_ID:
            	 if(resultCode == Activity.RESULT_OK){
            		long _id = data.getLongExtra("MODEL_ID", -1);
            		Friend friend = Friend.load(Friend.class, _id);
            		mSelectorFieldFriend.setText(friend.getNickName());
            		mSelectorFieldFriend.setModelId(friend.getId());
            	 }
            	 break;

          }
    }
}
