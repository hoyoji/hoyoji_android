package com.hoyoji.hoyoji.friend;

import java.util.List;

import org.json.JSONObject;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostJSONLoader;
import com.hoyoji.android.hyjframework.server.HyjJSONListAdapter;
import com.hoyoji.hoyoji.R;

public class AddFriendListFragment extends HyjUserListFragment implements OnQueryTextListener {

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
		SearchView searchView = (SearchView)getView().findViewById(R.id.friendListFragment_addFriend_searchView);
		searchView.setOnQueryTextListener(this);
	}

	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		Object loader = new HyjHttpPostJSONLoader(getActivity(),
				"findData", null
			);
		return (Loader<Object>)loader;
	}

    @Override 
    public void onLoadFinished(Loader<Object> loader, Object data) {
        // Set the new data in the adapter.
    	((HyjJSONListAdapter)this.getListAdapter()).setData((List<JSONObject>) data);

        // The list should now be shown.
        if (isResumed()) {
        //    setListShown(true);
        } else {
        //    setListShownNoAnimation(true);
        }
    }

    @Override 
    public void onLoaderReset(Loader<Object> loader) {
        // Clear the data in the adapter.
    	((HyjJSONListAdapter)this.getListAdapter()).setData(null);
    }
    
    
	@Override
	public ListAdapter useListViewAdapter() {
		return new HyjJSONListAdapter(getActivity(),
				R.layout.friend_listitem_add_friend,
				new String[] { "nickName" },
				new int[] { R.id.friendListItem_nickName }); 
	}



	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		HyjUtil.displayToast("adding friend " + position + " : " + id);
    }



	@Override
	public boolean onQueryTextChange(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public boolean onQueryTextSubmit(String arg0) {
//		Loader<Object> loader = getActivity().getSupportLoaderManager().getLoader(0);
//		if(loader.isStarted()){
//			loader.forceLoad();
//		}
		HyjUtil.displayToast("正在查找好友...");
		return true;
	}  

}
