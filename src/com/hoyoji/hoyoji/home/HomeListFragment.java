package com.hoyoji.hoyoji.home;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;

import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFragment;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.money.MoneyExpenseFormFragment;
import com.hoyoji.hoyoji.money.currency.ExchangeFormFragment;
import com.hoyoji.hoyoji.money.currency.ExchangeListFragment;


public class HomeListFragment extends HyjUserListFragment {

	@Override
	public Integer useContentView() {
		// TODO Auto-generated method stub
		return R.layout.home_listfragment_home;
	}
	
	
	
	@Override
	public Integer useToolbarView() {
		// TODO Auto-generated method stub
		return super.useToolbarView();
	}



	@Override
	public Integer useOptionsMenuView() {
		// TODO Auto-generated method stub
		return R.menu.home_listfragment_home;
	}



	@Override
	public void onInitViewData() {
		// TODO Auto-generated method stub
		super.onInitViewData();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.homeListFragment_action_display_transaction_type_project){
			HyjUtil.displayToast(R.string.homeListFragment_action_display_transaction_type);
			return true;
		}else if(item.getItemId() == R.id.homeListFragment_action_display_exchangeAll){
			openActivityWithFragment(ExchangeListFragment.class, R.string.exchangeFormFragment_title_addnew, null);
			return true;
		}else if(item.getItemId() == R.id.mainActivity_action_money_addnew_expense){
			openActivityWithFragment(MoneyExpenseFormFragment.class, R.string.moneyExpenseFormFragment_title_addnew, null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}



	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public ListAdapter useListViewAdapter() {
		// TODO Auto-generated method stub
		return null;
	}	
	
	
}
