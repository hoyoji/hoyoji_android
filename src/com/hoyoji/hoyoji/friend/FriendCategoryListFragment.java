package com.hoyoji.hoyoji.friend;

import android.app.Activity;
import android.content.Intent;
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

import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.FriendCategory;
import com.hoyoji.hoyoji.models.Project;

public class FriendCategoryListFragment extends HyjUserListFragment{
	
	@Override
	public Integer useContentView() {
		return R.layout.friend_listfragment_friend_category;
	}

	@Override
	public Integer useOptionsMenuView() {
		return R.menu.friend_listfragment_friend_category;
	}

	@Override
	public ListAdapter useListViewAdapter() {
 		return new SimpleCursorAdapter(getActivity(),
				R.layout.friend_listitem_friend_category,
				null,
				new String[] { "name" },
				new int[] { R.id.friendListItem_category_name },
				0); 
	}	


	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		super.onCreateLoader(arg0, arg1);
		Object loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(FriendCategory.class, null),
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
		if(item.getItemId() == R.id.friendCategoryListFragment_action_create){
			openActivityWithFragment(FriendCategoryFormFragment.class, R.string.friendCategoryFormFragment_title_create, null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		if(id < 0){
			super.onListItemClick(l, v, position, id);
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
			openActivityWithFragment(FriendCategoryFormFragment.class, R.string.friendCategoryFormFragment_title_edit, bundle);
		}
    }  

	@Override 
	public void onDeleteListItem(Long id){
		FriendCategory friendCategory = FriendCategory.load(FriendCategory.class, id);
		friendCategory.delete();
	}
	
}
