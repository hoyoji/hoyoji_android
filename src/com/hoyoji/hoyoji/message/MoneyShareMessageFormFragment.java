package com.hoyoji.hoyoji.message;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeField;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.money.MoneyBorrowFormFragment;
import com.hoyoji.hoyoji.money.MoneyExpenseFormFragment;
import com.hoyoji.hoyoji.money.MoneyIncomeFormFragment;
import com.hoyoji.hoyoji.money.MoneyLendFormFragment;
import com.hoyoji.hoyoji.money.MoneyPaybackFormFragment;
import com.hoyoji.hoyoji.money.MoneyReturnFormFragment;

public class MoneyShareMessageFormFragment extends HyjUserFormFragment {

	protected static final int IMPORT_MONEY = 0;
	private HyjModelEditor<Message> mMessageEditor = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjTextField mEditTextToUser = null;
	private HyjTextField mEditTextTitle = null;
	private HyjRemarkField mEditTextDetail = null;

	@Override
	public Integer useContentView() {
		return R.layout.message_formfragment_moneysharemessage;
	}

	@Override
	public Integer useOptionsMenuView() {
		return null;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		Message shareAddMessage;

		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if (modelId != -1) {
			shareAddMessage = new Select().from(Message.class)
					.where("_id=?", modelId).executeSingle();
			if(shareAddMessage.getMessageState().equalsIgnoreCase("unread") || 
					shareAddMessage.getMessageState().equalsIgnoreCase("new")){
				shareAddMessage.setMessageState("read");
				shareAddMessage.save();
			}
		} else {
			shareAddMessage = new Message();
		}
		mMessageEditor = shareAddMessage.newModelEditor();

		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(
				R.id.moneyShareMessageFormFragment_editText_date);
		mDateTimeFieldDate.setText(shareAddMessage.getDate());
		mDateTimeFieldDate.setEnabled(false);

		mEditTextToUser = (HyjTextField) getView().findViewById(
				R.id.moneyShareMessageFormFragment_editText_toUser);
		mEditTextToUser.setEnabled(false);
		mEditTextTitle = (HyjTextField) getView().findViewById(
				R.id.moneyShareMessageFormFragment_editText_title);
		mEditTextTitle.setText(shareAddMessage.getMessageTitle());
		mEditTextTitle.setEnabled(false);

		mEditTextDetail = (HyjRemarkField) getView().findViewById(
				R.id.moneyShareMessageFormFragment_editText_detail);

		try {
			JSONObject messageData = null;
			messageData = new JSONObject(mMessageEditor.getModel().getMessageData());
			mEditTextDetail.setText(String.format(shareAddMessage.getMessageDetail(), shareAddMessage.getFromUserDisplayName(), messageData.optString("currencyCode"), messageData.optDouble("amount")));
		} catch (Exception e){
		}
		
		Button actionButton = (Button) getView().findViewById(R.id.button_save);
		
		mDateTimeFieldDate
				.setLabel(R.string.moneyShareMessageFormFragment_textView_date_receive);
		mEditTextToUser
				.setLabel(R.string.moneyShareMessageFormFragment_textView_fromUser);
		mEditTextToUser.setText(shareAddMessage.getFromUserDisplayName());
		mEditTextDetail.setEnabled(false);

		
		if (shareAddMessage.getType().equalsIgnoreCase("Money.Share.AddExpense")){
			actionButton.setText(R.string.moneyShareMessageFormFragment_button_import_income);
		} else if (shareAddMessage.getType().equalsIgnoreCase("Money.Share.AddIncome")){
			actionButton.setText(R.string.moneyShareMessageFormFragment_button_import_expense);
		} else if (shareAddMessage.getType().equalsIgnoreCase("Money.Share.AddBorrow")){
			actionButton.setText(R.string.moneyShareMessageFormFragment_button_import_lend);
		} else if (shareAddMessage.getType().equalsIgnoreCase("Money.Share.AddLend")){
			actionButton.setText(R.string.moneyShareMessageFormFragment_button_import_borrow);
		} else if (shareAddMessage.getType().equalsIgnoreCase("Money.Share.AddReturn")){
			actionButton.setText(R.string.moneyShareMessageFormFragment_button_import_payback);
		} else if (shareAddMessage.getType().equalsIgnoreCase("Money.Share.AddPayback")){
			actionButton.setText(R.string.moneyShareMessageFormFragment_button_import_return);
		}
	}
//
//	private void fillData() {
//		Message modelCopy = (Message) mMessageEditor.getModelCopy();
//		modelCopy.setMessageDetail(mEditTextDetail.getText().toString().trim());
//	}
//
//	private void showValidatioErrors() {
//		HyjUtil.displayToast(R.string.app_validation_error);
//
//		mEditTextDetail.setError(mMessageEditor
//				.getValidationError("messageDetail"));
//	}

	@Override
	public void onSave(View v) {
		super.onSave(v);
		Bundle bundle = new Bundle();
		JSONObject messageData;
		try {
			messageData = new JSONObject(mMessageEditor.getModel().getMessageData());
			bundle.putDouble("amount", messageData.optDouble("amount"));
			bundle.putString("currencyId", messageData.optString("currencyId"));
			bundle.putString("counterpartId", messageData.optString("counterpartId"));
			bundle.putString("friendUserId", mMessageEditor.getModel().getFromUserId());
			String projectId = messageData.optString("projectId");
			if(projectId != null){
				Project project = HyjModel.getModel(Project.class, projectId);
				if(project != null){
					bundle.putString("projectId", projectId);
				}
			}
			
			if (mMessageEditor.getModel().getType().equalsIgnoreCase("Money.Share.AddExpense")){
				openActivityWithFragmentForResult(MoneyIncomeFormFragment.class, R.string.moneyShareMessageFormFragment_button_import_income, bundle, IMPORT_MONEY);
			} else if (mMessageEditor.getModel().getType().equalsIgnoreCase("Money.Share.AddIncome")){
				openActivityWithFragmentForResult(MoneyExpenseFormFragment.class, R.string.moneyShareMessageFormFragment_button_import_expense, bundle, IMPORT_MONEY);
			} else if (mMessageEditor.getModel().getType().equalsIgnoreCase("Money.Share.AddBorrow")){
				openActivityWithFragmentForResult(MoneyLendFormFragment.class, R.string.moneyShareMessageFormFragment_button_import_lend, bundle, IMPORT_MONEY);
			} else if (mMessageEditor.getModel().getType().equalsIgnoreCase("Money.Share.AddLend")){
				openActivityWithFragmentForResult(MoneyBorrowFormFragment.class, R.string.moneyShareMessageFormFragment_button_import_borrow, bundle, IMPORT_MONEY);
			} else if (mMessageEditor.getModel().getType().equalsIgnoreCase("Money.Share.AddReturn")){
				openActivityWithFragmentForResult(MoneyPaybackFormFragment.class, R.string.moneyShareMessageFormFragment_button_import_payback, bundle, IMPORT_MONEY);
			} else if (mMessageEditor.getModel().getType().equalsIgnoreCase("Money.Share.AddPayback")){
				openActivityWithFragmentForResult(MoneyReturnFormFragment.class, R.string.moneyShareMessageFormFragment_button_import_return, bundle, IMPORT_MONEY);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case IMPORT_MONEY:
	        	 if(resultCode == Activity.RESULT_OK){
	 				mMessageEditor.getModelCopy().setMessageState("closed");
	 				mMessageEditor.save();
	 				HyjUtil.displayToast("导入成功");
	        	 }
	        	 break;
         }
   }
	
	private void displayError(Object object) {
		((HyjActivity) MoneyShareMessageFormFragment.this.getActivity())
				.dismissProgressDialog();
		JSONObject json = (JSONObject) object;
		HyjUtil.displayToast(json.optJSONObject("__summary").optString("msg"));
	}
}
