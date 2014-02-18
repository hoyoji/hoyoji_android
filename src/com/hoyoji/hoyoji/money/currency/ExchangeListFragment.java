package com.hoyoji.hoyoji.money.currency;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;

import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Currency;
import com.hoyoji.hoyoji.models.Exchange;

public class ExchangeListFragment extends HyjUserListFragment{
	public final static int ADD_SUB_PROJECT = 0;
	public final static int VIEW_PROJECT_MEMBERS = 1;
	
	@Override
	public Integer useContentView() {
		return R.layout.exchange_listfragment_exchange;
	}

	@Override
	public Integer useOptionsMenuView() {
		return R.menu.exchange_listfragment_exchange;
	}

	@Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.exchange_listitem_exchange,
				null,
				new String[] { "localCurrencyId", "foreignCurrencyId", "rate", "autoUpdate" },
				new int[] { R.id.exchangeListItem_localCurrency, R.id.exchangeListItem_foreignCurrency, R.id.exchangeListItem_rate, R.id.exchangeListItem_autoUpdate },
				0); 
	}	


	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		Object loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(Exchange.class, null),
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
		if(item.getItemId() == R.id.exchangeListFragment_action_exchange_addnew){
			openActivityWithFragment(ExchangeFormFragment.class, R.string.exchangeFormFragment_title_addnew, null);
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
			openActivityWithFragment(ExchangeFormFragment.class, R.string.exchangeFormFragment_title_edit, bundle);
		}
    }  

	@Override 
	public void onDeleteListItem(Long id){
		Exchange exchange = Exchange.load(Exchange.class, id);
		exchange.delete();
	    HyjUtil.displayToast("汇率删除成功");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(!getUserVisibleHint()){
			return super.onContextItemSelected(item);
		}
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    Long itemId = getListAdapter().getItemId(info.position);
		switch (item.getItemId()) {
			case ADD_SUB_PROJECT:
			    HyjUtil.displayToast("创建子项目" + itemId);
				break;
			case VIEW_PROJECT_MEMBERS:
			    HyjUtil.displayToast("项目成员" + itemId);
				break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, VIEW_PROJECT_MEMBERS, 0, "项目成员");
		menu.add(0, ADD_SUB_PROJECT, 1, "创建子项目");
	}
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(view.getId() == R.id.exchangeListItem_foreignCurrency || 
				view.getId() == R.id.exchangeListItem_localCurrency){
			((TextView)view).setText(((Currency)(HyjModel.getModel(Currency.class, cursor.getString(columnIndex)))).getName());
			return true;
		} else if(view.getId() == R.id.exchangeListItem_rate){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setNumber(cursor.getDouble(columnIndex));
			return true;
		} else if(view.getId() == R.id.exchangeListItem_autoUpdate){
			((TextView)view).setText(String.valueOf(cursor.getInt(columnIndex)));
			return true;
		} else {
			return false;
		}
	}
}
