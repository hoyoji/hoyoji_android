package com.hoyoji.hoyoji.friend;

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
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.fragment.HyjUserFragment;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.project.ProjectFormFragment;

public class FriendListFragment extends HyjUserListFragment {

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
		if(item.getItemId() == R.id.friendListFragment_action_friend_addnew){
			openActivityWithFragment(FriendFormFragment.class, R.string.friendFormFragment_title_addnew, null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}



	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(getActivity(),
				ContentProvider.createUri(Friend.class, null),
				null, null, null, null
			);
	}



	@Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.friend_listitem_friend,
				null,
				new String[] { "friendCategory" },
				new int[] { R.id.friendListItem_friend_category },
				0); 
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
			openActivityWithFragment(FriendFormFragment.class, R.string.friendFormFragment_title_edit, bundle);
		}
    }  

	@Override 
	public void onDeleteListItem(Long id){
		Friend friend = Friend.load(Friend.class, id);
		friend.delete();
	    HyjUtil.displayToast("好友删除成功");
	}
}
