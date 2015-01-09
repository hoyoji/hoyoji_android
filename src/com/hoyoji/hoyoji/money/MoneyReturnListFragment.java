package com.hoyoji.hoyoji.money;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.aaevent_android.R;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyReturn;

public class MoneyReturnListFragment extends HyjUserListFragment {

	@Override
	public Integer useContentView() {
		return R.layout.money_listfragment_moneyreturn;
	}

	@Override
	public Integer useOptionsMenuView() {
		return R.menu.money_listfragment_moneyreturn;
	}

	@Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.home_listitem_row,
				null,
				new String[] {"pictureId", "date", "amount" },
				new int[] { R.id.homeListItem_picture, R.id.homeListItem_date, R.id.homeListItem_amount },
				0); 
	}	

	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		super.onCreateLoader(arg0, arg1);
		Object loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(MoneyReturn.class, null),
				null, null, null, null
			);
		return (Loader<Object>)loader;
	}


	@Override
	public void onInitViewData() {
		super.onInitViewData();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.moneyReturnListFragment_action_moneyReturn_addnew){
			openActivityWithFragment(MoneyReturnFormFragment.class, R.string.moneyReturnFormFragment_title_addnew, null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		if(l.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE){
			return;
		}
		if(id == -1) {
			 return;
		}
		if(getActivity().getCallingActivity() != null){
			Intent intent = new Intent();
			intent.putExtra("MODEL_ID", id);
			getActivity().setResult(Activity.RESULT_OK, intent);
			getActivity().finish();
		} else {
			Bundle bundle = new Bundle();
			bundle.putLong("MODEL_ID", id);
			openActivityWithFragment(MoneyReturnFormFragment.class, R.string.moneyReturnFormFragment_title_edit, bundle);
		}
    }  

//	@Override 
//	public void onDeleteListItem(Long id){
//		try {
//				ActiveAndroid.beginTransaction();
//				
//				MoneyReturn moneyReturn = MoneyReturn.load(MoneyReturn.class, id);
//				MoneyAccount moneyAccount = moneyReturn.getMoneyAccount();
//				HyjModelEditor<MoneyAccount> moneyAccountEditor = moneyAccount.newModelEditor();
//				MoneyAccount debtAccount = MoneyAccount.getDebtAccount(moneyReturn.getProject().getCurrencyId(), moneyReturn.getLocalFriendId(), moneyReturn.getFriendUserId());
//				HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
//				moneyReturn.delete();
//				moneyAccountEditor.getModelCopy().setCurrentBalance(moneyAccount.getCurrentBalance() + moneyReturn.getAmount() + moneyReturn.getInterest0());
//				debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - moneyReturn.getProjectAmount());
//				moneyAccountEditor.save();
//				debtAccountEditor.save();
//				
//			    HyjUtil.displayToast("还款删除成功");
//			    ActiveAndroid.setTransactionSuccessful();
//		} finally {
//		    ActiveAndroid.endTransaction();
//		}
//	}
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setText(cursor.getString(columnIndex));
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix("¥");
			numericView.setNumber(cursor.getDouble(columnIndex));
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setImage(cursor.getString(columnIndex));
			return true;
		} else {
			return false;
		}
	}
}
