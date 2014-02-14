package com.hoyoji.hoyoji.money.currency;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Currency;

public class CurrencyListFragment extends HyjUserListFragment{
	
	@Override
	public Integer useContentView() {
		return R.layout.currency_listfragment_currency;
	}

	@Override
	public Integer useOptionsMenuView() {
		return R.menu.currency_listfragment_currency;
	}

	@Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.currency_listitem_currency,
				null,
				new String[] { "name" },
				new int[] { R.id.currencyListItem_name },
				0); 
	}	


	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		Object loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(Currency.class, null),
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
		if(item.getItemId() == R.id.currencyListFragment_action_currency_addnew){
			openActivityWithFragment(CurrencyFormFragment.class, R.string.currencyFormFragment_title_addnew, null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		
		if(getActivity().getCallingActivity() != null){
			Intent intent = new Intent();
			intent.putExtra("MODEL_ID", id);
			getActivity().setResult(Activity.RESULT_OK, intent);
			getActivity().finish();
		} else {
			Bundle bundle = new Bundle();
			bundle.putLong("MODEL_ID", id);
			openActivityWithFragment(CurrencyFormFragment.class, R.string.currencyFormFragment_title_edit, bundle);
		}
    }  

	@Override 
	public void onDeleteListItem(Long id){
		Currency currency= Currency.load(Currency.class, id);
		currency.delete();
	    HyjUtil.displayToast("币种删除成功");
	}
}
