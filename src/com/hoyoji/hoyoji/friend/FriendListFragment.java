package com.hoyoji.hoyoji.friend;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjSimpleCursorTreeAdapter;
import com.hoyoji.android.hyjframework.HyjSimpleExpandableListAdapter;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserExpandableListFragment;
import com.hoyoji.android.hyjframework.view.HyjBooleanView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.models.Currency;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.FriendCategory;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.money.MoneySearchListFragment;
import com.hoyoji.hoyoji.project.ProjectFormFragment;

public class FriendListFragment extends HyjUserExpandableListFragment {
	public final static int EDIT_CATEGORY_ITEM = 1;
	private static final int EDIT_FRIEND_DETAILS = 0;
	private ContentObserver mUserChangeObserver = null;
	
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
		if (mUserChangeObserver == null) {
			mUserChangeObserver = new ChangeObserver();
			this.getActivity().getContentResolver().registerContentObserver(ContentProvider.createUri(User.class, null), true,
					mUserChangeObserver);
		}
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
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		int type = ExpandableListView
	            .getPackedPositionType(info.packedPosition);

	    switch (item.getItemId()) {
			case EDIT_CATEGORY_ITEM:
				if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			        int groupPos = ExpandableListView
			                .getPackedPositionGroup(info.packedPosition);
				    Long itemId = getListView().getExpandableListAdapter().getGroupId(groupPos);
				    Bundle bundle = new Bundle();
					bundle.putLong("MODEL_ID", itemId);
					openActivityWithFragment(FriendCategoryFormFragment.class, R.string.friendCategoryFormFragment_title_edit, bundle);
				} 
				break;
			case EDIT_FRIEND_DETAILS:
				if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			        int groupPos = ExpandableListView
			                .getPackedPositionGroup(info.packedPosition);
			        int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
				    Long itemId = getListView().getExpandableListAdapter().getChildId(groupPos, childPos);
					Bundle bundle = new Bundle();
					bundle.putLong("MODEL_ID", itemId);
					openActivityWithFragment(FriendFormFragment.class, R.string.friendFormFragment_title, bundle);
				} 
				break;
				
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		ExpandableListContextMenuInfo adapterContextMenuInfo = (ExpandableListContextMenuInfo) menuInfo;
		if(ExpandableListView.getPackedPositionType(adapterContextMenuInfo.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_GROUP){
			menu.add(EDIT_CATEGORY_ITEM, EDIT_CATEGORY_ITEM, EDIT_CATEGORY_ITEM, R.string.friendCategoryFormFragment_title_edit);
			super.onCreateContextMenu(menu, v, menuInfo);
		} else {
			menu.add(0, EDIT_FRIEND_DETAILS, 0, "好友资料");
			menu.add(CANCEL_LIST_ITEM, CANCEL_LIST_ITEM, CANCEL_LIST_ITEM, R.string.app_action_cancel_list_item);
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
			intent.putExtra("MODEL_TYPE", "Friend");
			getActivity().setResult(Activity.RESULT_OK, intent);
			getActivity().finish();
			return true;
		} else {
			Bundle bundle = new Bundle();
			bundle.putLong("friend_id", id);
			openActivityWithFragment(MoneySearchListFragment.class, R.string.friendListFragment_title_friend_transactions, bundle);
			return true;
		}
    }  

//	@Override 
//	public void onDeleteListItem(Long id){
//		Friend friend = Friend.load(Friend.class, id);
//		friend.delete();
//	    HyjUtil.displayToast("好友删除成功");
//	}

	@Override 
	public void onDeleteListGroup(int groupPos, Long id){
		List<Friend> friends = HyjModel.load(FriendCategory.class, id).getFriends();
		if(getListView().getExpandableListAdapter().getChildrenCount(groupPos) > 0 || !friends.isEmpty()){
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
			if(cursor.getString(columnIndex) != null && cursor.getString(columnIndex).length() > 0){
				((TextView)view).setText(cursor.getString(columnIndex));
			} else {
				User user = HyjModel.getModel(User.class, cursor.getString(cursor.getColumnIndex("friendUserId")));
				if(user != null){
					((TextView)view).setText(user.getDisplayName());
				} else {
					((TextView)view).setText(cursor.getString(cursor.getColumnIndex("friendUserName")));
				}
			}
			return true;
		} else if(view.getId() == R.id.friendListItem_picture){
			if(cursor.getString(columnIndex) != null){
				User user = HyjModel.getModel(User.class, cursor.getString(columnIndex));
				if(user != null){
					((HyjImageView)view).setImage(user.getPictureId());
				} else {
					((HyjImageView)view).setImage((Picture)null);
				}
			} else {
				((HyjImageView)view).setImage((Picture)null);
			}
	 		if(view.getTag() == null){
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						Bundle bundle = new Bundle();
						bundle.putLong("MODEL_ID", (Long) v.getTag());
						openActivityWithFragment(FriendFormFragment.class, R.string.friendFormFragment_title, bundle);
					}
				});
			}
			view.setTag(cursor.getLong(cursor.getColumnIndex("_id")));
			return true;
		} else {
			return false;
		}
	}
	
	private class ChangeObserver extends ContentObserver {
		AsyncTask<String, Void, String> mTask = null;
		public ChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if(mTask == null){
				mTask = new AsyncTask<String, Void, String>() {
			        @Override
			        protected String doInBackground(String... params) {
						try {
							//等待其他的更新都到齐后再更新界面
							Thread.sleep(0);
						} catch (InterruptedException e) {}
						return null;
			        }
			        @Override
			        protected void onPostExecute(String result) {
						((CursorTreeAdapter) getListView().getExpandableListAdapter()).notifyDataSetChanged();
						mTask = null;
			        }
			    };
			    mTask.execute();
			}
		}
	}
	@Override
	public void onDestroy() {
		if (mUserChangeObserver != null) {
			this.getActivity().getContentResolver()
					.unregisterContentObserver(mUserChangeObserver);
		}
		super.onDestroy();
	}


}
