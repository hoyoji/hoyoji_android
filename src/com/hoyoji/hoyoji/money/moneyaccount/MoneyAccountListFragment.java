package com.hoyoji.hoyoji.money.moneyaccount;

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

import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.UserData;

public class MoneyAccountListFragment extends HyjUserListFragment{
	
	@Override
	public Integer useContentView() {
		return R.layout.moneyaccount_listfragment_moneyaccount;
	}

	@Override
	public Integer useOptionsMenuView() {
		return R.menu.moneyaccount_listfragment_moneyaccount;
	}

	@Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.moneyaccount_listitem_moneyaccount,
				null,
				new String[] { "name", "currentBalance" },
				new int[] { R.id.moneyAccountListItem_name, R.id.moneyAccountListItem_currentBalance },
				0); 
	}	


	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		Object loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(MoneyAccount.class, null),
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
		if(item.getItemId() == R.id.moneyAccountListFragment_action_moneyAccount_addnew){
			openActivityWithFragment(MoneyAccountFormFragment.class, R.string.moneyAccountFormFragment_title_addnew, null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
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
			openActivityWithFragment(MoneyAccountFormFragment.class, R.string.moneyAccountFormFragment_title_edit, bundle);
		}
    }  

	@Override 
	public void onDeleteListItem(Long id){
		MoneyAccount moneyAccount = MoneyAccount.load(MoneyAccount.class, id);
		UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
		if(userData.getActiveMoneyAccountId().equals(moneyAccount.getId())){
			HyjUtil.displayToast("默认账户不能删除");
			return;
		}
		moneyAccount.delete();
	    HyjUtil.displayToast("账户删除成功");
	}
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(view.getId() == R.id.moneyAccountListItem_currentBalance){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setCurrencySymbol("¥");
			numericView.setNumber(cursor.getDouble(columnIndex));
			return true;
		} else {
			return false;
		}
	}
}
