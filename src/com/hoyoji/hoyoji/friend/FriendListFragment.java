package com.hoyoji.hoyoji.friend;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjSimpleCursorTreeAdapter;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserExpandableListFragment;
import com.hoyoji.android.hyjframework.view.HyjBooleanView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Currency;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.FriendCategory;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.User;

public class FriendListFragment extends HyjUserExpandableListFragment {
	public final static int EDIT_CATEGORY_ITEM = 1;
	
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
			openActivityWithFragment(AddFriendListFragment.class, R.string.addFriendListFragment_title_add, null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}



	@Override
	public Loader<Object> onCreateLoader(int groupPos, Bundle arg1) {
		super.onCreateLoader(groupPos, arg1);
		Object loader;
		if(groupPos < 0){ // 这个是分类
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
				new String[] { "friendUserId", "nickName" },
				new int[] { R.id.friendListItem_picture, R.id.friendListItem_nickName });
		adapter.setGetChildrenCursorListener(this);
		return adapter;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(!getUserVisibleHint()){
			return super.onContextItemSelected(item);
		}
		switch (item.getItemId()) {
			case EDIT_CATEGORY_ITEM:
				ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
				int type = ExpandableListView
			            .getPackedPositionType(info.packedPosition);

			    if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			        int groupPos = ExpandableListView
			                .getPackedPositionGroup(info.packedPosition);
				    Long itemId = getListView().getExpandableListAdapter().getGroupId(groupPos);
				    Bundle bundle = new Bundle();
					bundle.putLong("MODEL_ID", itemId);
					openActivityWithFragment(FriendCategoryFormFragment.class, R.string.friendCategoryFormFragment_title_edit, bundle);
					return true;
			    } 
				break;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		ExpandableListContextMenuInfo adapterContextMenuInfo = (ExpandableListContextMenuInfo) menuInfo;
		if(ExpandableListView.getPackedPositionType(adapterContextMenuInfo.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_GROUP){
			menu.add(EDIT_CATEGORY_ITEM, EDIT_CATEGORY_ITEM, EDIT_CATEGORY_ITEM, R.string.friendCategoryFormFragment_title_edit);
		}
	}		
	
//	@Override  
//	public boolean onGroupClick(ExpandableListView parent, View v,
//			int groupPosition, long id) {
////		if(getActivity().getCallingActivity() != null){
////			Intent intent = new Intent();
////			intent.putExtra("MODEL_ID", id);
////			getActivity().setResult(Activity.RESULT_OK, intent);
////			getActivity().finish();
////			return true;
////		} else {
////			Bundle bundle = new Bundle();
////			bundle.putLong("MODEL_ID", id);
////			openActivityWithFragment(FriendCategoryFormFragment.class, R.string.friendCategoryFormFragment_title_edit, bundle);
////			return true;
////		}
//    } 

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
	public void onDeleteListGroup(int groupPos, Long id){
		if(getListView().getExpandableListAdapter().getChildrenCount(groupPos) > 0){
			//new Select().from(Friend.class).where("friendCategoryId=?", id);
			HyjUtil.displayToast("该好友分类下包含好友，不能被删除");
		} else {
			FriendCategory friendCategory = FriendCategory.load(FriendCategory.class, id);
			friendCategory.delete();
		    HyjUtil.displayToast("好友分类删除成功");
		}
	}


	@Override
	public void onGetChildrenCursor(Cursor groupCursor) {
		Bundle bundle = new Bundle();
		bundle.putString("friendCategoryId", groupCursor.getString(groupCursor.getColumnIndex("id")));
		int groupPos = groupCursor.getPosition();
		Loader<Object> loader = getLoaderManager().getLoader(groupPos);
	    if (loader != null && !loader.isReset() ) { 
	    	getLoaderManager().restartLoader(groupPos, bundle, this);
	    } else {
	    	getLoaderManager().initLoader(groupPos, bundle, this);
	    }
	}
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(view.getId() == R.id.friendListItem_nickName){
			if(cursor.getString(columnIndex) != null){
				((TextView)view).setText(cursor.getString(columnIndex));
			} else {
				User user = HyjModel.getModel(User.class, cursor.getString(cursor.getColumnIndex("friendUserId")));
				((TextView)view).setText(user.getDisplayName());
			}
			return true;
		} else if(view.getId() == R.id.friendListItem_picture){
			if(cursor.getString(columnIndex) != null){
				((HyjImageView)view).setImage(HyjModel.getModel(User.class, cursor.getString(columnIndex)).getPictureId());
			} else {
				((HyjImageView)view).setImage((Picture)null);
			}
			return true;
		} else {
			return false;
		}
	}
}
