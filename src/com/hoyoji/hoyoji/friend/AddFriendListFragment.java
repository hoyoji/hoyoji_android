package com.hoyoji.hoyoji.friend;

import java.util.List;

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
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostJSONLoader;
import com.hoyoji.android.hyjframework.server.HyjJSONListAdapter;
import com.hoyoji.hoyoji.R;

public class AddFriendListFragment extends HyjUserListFragment implements OnQueryTextListener {
	protected SearchView mSearchView;
	protected String mSearchText = "";
	
	@Override
	public Integer useContentView() {
		return R.layout.friend_listfragment_add_friend;
	}
	
	
	@Override
	public Integer useToolbarView() {
		return super.useToolbarView();
	}
	
	@Override
	public void onInitViewData(){
		mSearchView = (SearchView)getView().findViewById(R.id.friendListFragment_addFriend_searchView);
		mSearchView.setOnQueryTextListener(this);
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
				R.layout.friend_listitem_add_friend,
				new String[] { "userName" },
				new int[] { R.id.friendListItem_nickName }); 
	}



	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(id > 0){
			HyjUtil.displayToast("adding friend " + position + " : " + id);
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
			data.put("userName", mSearchText);
			data.put("nickName", mSearchText);
			data.put("__dataType", "User");
			data.put("__limit", mListPageSize);
			data.put("__offset", 0);
			data.put("__orderBy", "userName ASC");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Bundle bundle = new Bundle();
		bundle.putString("target", "findData");
		bundle.putString("postData", (new JSONArray()).put(data).toString());
		getLoaderManager().destroyLoader(0);
		getLoaderManager().restartLoader(0, bundle, this);
		return true;
	}
	
	@Override
	public void doFetchMore(int offset, int pageSize){
		this.setFooterLoadStart();
		JSONObject data = new JSONObject();
		try {
			data.put("userName", mSearchText);
			data.put("nickName", mSearchText);
			data.put("__dataType", "User");
			data.put("__limit", pageSize);
			data.put("__offset", offset);
			data.put("__orderBy", "userName ASC");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Bundle bundle = new Bundle();
		bundle.putString("target", "findData");
		bundle.putString("postData", (new JSONArray()).put(data).toString());
		Loader loader = getLoaderManager().getLoader(0);
		((HyjHttpPostJSONLoader)loader).changePostQuery(bundle);		
	}
}
