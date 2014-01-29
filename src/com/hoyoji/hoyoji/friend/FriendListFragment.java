package com.hoyoji.hoyoji.friend;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;

import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjSimpleCursorTreeAdapter;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserExpandableListFragment;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.FriendCategory;

public class FriendListFragment extends HyjUserExpandableListFragment {

	@Override
	public Integer useContentView() {
		return R.layout.friend_listfragment_friend;
	}
	
	
	
	@Override
	public Integer useToolbarView() {
		return super.useToolbarView();
	}



	@Override
	public Integer useOptionsMenuView() {
		return R.menu.friend_listfragment_friend;
	}



	@Override
	public void onInitViewData() {
		super.onInitViewData();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.friendListFragment_action_friend_create){
			openActivityWithFragment(FriendFormFragment.class, R.string.friendFormFragment_title_create, null);
			return true;
		} else if(item.getItemId() == R.id.friendListFragment_action_friend_add){
			openActivityWithFragment(AddFriendListFragment.class, R.string.AddFriendListFragment_title_add, null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}



	@Override
	public Loader<Object> onCreateLoader(int groupPos, Bundle arg1) {
		super.onCreateLoader(groupPos, arg1);
		Object loader;
		if(groupPos < 0){
			loader = new CursorLoader(getActivity(),
					ContentProvider.createUri(FriendCategory.class, null),
					null, null, null, null
				);
		} else {
			loader = new CursorLoader(getActivity(),
					ContentProvider.createUri(Friend.class, null),
					null, "friendCategoryId=?", new String[]{arg1.getString("friendCategoryId")}, null
				);
		}
		return (Loader<Object>)loader;
	}



	@Override
	public SimpleCursorTreeAdapter useListViewAdapter() {
		HyjSimpleCursorTreeAdapter adapter =  new HyjSimpleCursorTreeAdapter(getActivity(),
				null, 
				R.layout.friend_listitem_friend_group, 
				new String[] { "name" },
				new int[] { R.id.friendListItem_category_name }, 
				R.layout.friend_listitem_friend,
				new String[] { "nickName" },
				new int[] { R.id.friendListItem_nickName });
		adapter.setGetChildrenCursorListener(this);
		return adapter;
	}



	@Override  
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		if(getActivity().getCallingActivity() != null){
			Intent intent = new Intent();
			intent.putExtra("MODEL_ID", id);
			getActivity().setResult(Activity.RESULT_OK, intent);
			getActivity().finish();
			return true;
		} else {
			Bundle bundle = new Bundle();
			bundle.putLong("MODEL_ID", id);
			openActivityWithFragment(FriendFormFragment.class, R.string.friendFormFragment_title_edit, bundle);
			return true;
		}
    }  

	@Override 
	public void onDeleteListItem(Long id){
		Friend friend = Friend.load(Friend.class, id);
		friend.delete();
	    HyjUtil.displayToast("好友删除成功");
	}



	@Override
	public void onGetChildrenCursor(Cursor groupCursor) {
		Bundle bundle = new Bundle();
		bundle.putString("friendCategoryId", groupCursor.getString(groupCursor.getColumnIndex("id")));
		getLoaderManager().restartLoader(groupCursor.getPosition(), bundle, this);
	}

}
