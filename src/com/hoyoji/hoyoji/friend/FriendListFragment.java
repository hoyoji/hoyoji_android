package com.hoyoji.hoyoji.friend;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.widget.ListAdapter;

import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFragment;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.hoyoji.R;

public class FriendListFragment extends HyjUserListFragment {

	@Override
	public Integer useContentView() {
		// TODO Auto-generated method stub
		return R.layout.friend_listfragment_friend;
	}
	
	
	
	@Override
	public Integer useToolbarView() {
		// TODO Auto-generated method stub
		return super.useToolbarView();
	}



	@Override
	public Integer useOptionsMenuView() {
		// TODO Auto-generated method stub
		return R.menu.friend_listfragment_friend;
	}



	@Override
	public void onInitViewData() {
		// TODO Auto-generated method stub
		super.onInitViewData();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.friendListFragment_action_friend_addnew){
			HyjUtil.displayToast(R.string.friendListFragment_action_friend_addnew);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}



	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public ListAdapter useListViewAdapter() {
		// TODO Auto-generated method stub
		return null;
	}	
	
	
}
