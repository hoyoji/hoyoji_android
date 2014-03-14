package com.hoyoji.hoyoji.money.currency;

import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.ContextMenu;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjHttpGetExchangeRateAsyncTask;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostJSONLoader;
import com.hoyoji.android.hyjframework.server.HyjJSONListAdapter;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Currency;
import com.hoyoji.hoyoji.models.Exchange;

public class AddCurrencyListFragment extends HyjUserListFragment implements OnQueryTextListener {
	protected SearchView mSearchView;
	protected String mSearchText = "";
	
	@Override
	public Integer useContentView() {
		return R.layout.currency_listfragment_add_currency;
	}
	
	
	@Override
	public Integer useToolbarView() {
		return super.useToolbarView();
	}
	
	@Override
	public void onInitViewData(){
		mSearchView = (SearchView)getView().findViewById(R.id.currencyListFragment_addCurrency_searchView);
		mSearchView.setOnQueryTextListener(this);
		this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}
	
	@Override
	public void initLoader(int loaderId){
		// do not init loader... wait for the user to start search
	}

	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		super.onCreateLoader(arg0, arg1);
		Object loader = new HyjHttpPostJSONLoader(getActivity(), arg1);
		return (Loader<Object>)loader;
	}

    @Override 
    public void onLoadFinished(Loader<Object> loader, Object data) {
    	super.onLoadFinished(loader, data);
        // Set the new data in the adapter.
    	((HyjJSONListAdapter)this.getListAdapter()).addData((List<JSONObject>) data);
    }

    @Override 
    public void onLoaderReset(Loader<Object> loader) {
    	super.onLoaderReset(loader);
        // Clear the data in the adapter.
    	((HyjJSONListAdapter)this.getListAdapter()).clear();
    }
    
    
	@Override
	public ListAdapter useListViewAdapter() {
		return new HyjJSONListAdapter(getActivity(),
				R.layout.currency_listitem_currency,
				new String[] { "name" },
				new int[] { R.id.currencyListItem_name }); 
	}
	
	private int loading = 0;
	
	 public void createExchange(final Currency foreignCurrency){
		 Currency activeCurrency = HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrency();
		 if(!foreignCurrency.getId().equals(activeCurrency.getId())){

			 Exchange exchange = Exchange.getExchange(activeCurrency.getId(), foreignCurrency.getId());
			 if(exchange == null && loading == 0){
				 HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
						@Override
						public void finishCallback(Object object) {
							Exchange newExchange = new Exchange();
							newExchange.setLocalCurrencyId(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId());
							newExchange.setForeignCurrencyId(foreignCurrency.getId());
							newExchange.setRate((Double) object);
							foreignCurrency.save();
							newExchange.save();
							HyjUtil.displayToast(R.string.currencyListFragment_addCurrency_toast_success);
						}

						@Override
						public void errorCallback(Object object) {
							if (object != null) {
								HyjUtil.displayToast(object.toString());
							} else {
								HyjUtil.displayToast("无法获取汇率");
							}
						}
					};

				 loading = 1;
				 HyjHttpGetExchangeRateAsyncTask.newInstance(activeCurrency.getId(), foreignCurrency.getId(), serverCallbacks);
			 }
		 }
	 }

	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(id >= 0){
			try {
				ActiveAndroid.beginTransaction();
				JSONObject object = (JSONObject) getListAdapter().getItem(position);
				Currency newCurrency = new Currency();
				newCurrency.loadFromJSON(object, true);
				
				java.util.Currency localeCurrency = java.util.Currency.getInstance(newCurrency.getCode());
				newCurrency.setSymbol(localeCurrency.getSymbol());
				
				createExchange(newCurrency);
				this.getActivity().finish();
			} finally {
			    ActiveAndroid.endTransaction();
			}
		}
    }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// do nothing, clear the delete item from super class 
	}	

	@Override
	public boolean onQueryTextChange(String arg0) {
		return false;
	}



	@Override
	public boolean onQueryTextSubmit(String searchText) {
		mSearchText = searchText.trim();
		if(searchText.length() == 0){
			HyjUtil.displayToast("请输入查询条件");
			return true;
		}
		JSONObject data = new JSONObject();
		try {
			data.put("name", mSearchText);
			data.put("code", mSearchText);
			data.put("__dataType", "CurrencyAll");
			data.put("__limit", mListPageSize);
			data.put("__offset", 0);
			data.put("__orderBy", "name ASC");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Bundle bundle = new Bundle();
		bundle.putString("target", "findCurrency");
		bundle.putString("postData", (new JSONArray()).put(data).toString());
		if(getLoaderManager().getLoader(0) != null){
			getLoaderManager().destroyLoader(0);
		}
		getLoaderManager().restartLoader(0, bundle, this);
		return true;
	}
	
	@Override
	public void doFetchMore(ListView l, int offset, int pageSize){
		this.setFooterLoadStart(l);
		JSONObject data = new JSONObject();
		try {
			data.put("name", mSearchText);
			data.put("name", mSearchText);
			data.put("__dataType", "CurrencyAll");
			data.put("__limit", pageSize);
			data.put("__offset", offset);
			data.put("__orderBy", "name ASC");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Bundle bundle = new Bundle();
		bundle.putString("target", "findCurrency");
		bundle.putString("postData", (new JSONArray()).put(data).toString());
		Loader loader = getLoaderManager().getLoader(0);
		((HyjHttpPostJSONLoader)loader).changePostQuery(bundle);		
	}
}
