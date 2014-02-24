package com.hoyoji.hoyoji.project;

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
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.User;

public class MemberListFragment extends HyjUserListFragment{
	public final static int ADD_SUB_PROJECT = 0;
	public final static int VIEW_PROJECT_MEMBERS = 1;
	
	@Override
	public Integer useContentView() {
		return R.layout.project_listfragment_member;
	}

	@Override
	public Integer useOptionsMenuView() {
		return R.menu.project_listfragment_member;
	}

	@Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.project_listitem_member,
				null,
				new String[] { "friendUserId", "sharePercentage", "remark" },
				new int[] { R.id.memberListItem_name, R.id.memberListItem_percentage, R.id.memberListItem_remark },
				0); 
	}	


	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		super.onCreateLoader(arg0, arg1);
		
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		Project project =  new Select().from(Project.class).where("_id=?", modelId).executeSingle();
		Object loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(ProjectShareAuthorization.class, null),
				null,
				"projectId=? AND state <> ?", 
				new String[]{project.getId(), "Deleted"}, 
				null
			);
		return (Loader<Object>)loader;
	}


	@Override
	public void onInitViewData() {
		super.onInitViewData();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.memberListFragment_action_member_addnew){
			Intent intent = getActivity().getIntent();
			Long modelId = intent.getLongExtra("MODEL_ID", -1);
			Bundle bundle = new Bundle();
			bundle.putLong("PROJECT_ID", modelId);
			openActivityWithFragment(MemberFormFragment.class, R.string.memberFormFragment_title_addnew, bundle);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		if(id == -1) {
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
			openActivityWithFragment(MemberFormFragment.class, R.string.memberFormFragment_title_edit, bundle);
		}
    }  

//	@Override 
//	public void onDeleteListItem(Long id){
//		Project project = Project.load(Project.class, id);
//		project.delete();
//	    HyjUtil.displayToast("项目删除成功");
//	}
//	
//	@Override
//	public boolean onContextItemSelected(MenuItem item) {
//		if(!getUserVisibleHint()){
//			return super.onContextItemSelected(item);
//		}
//	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
//	    Long itemId = getListAdapter().getItemId(info.position);
//		switch (item.getItemId()) {
//			case ADD_SUB_PROJECT:
//			    HyjUtil.displayToast("创建子项目" + itemId);
//				break;
//			case VIEW_PROJECT_MEMBERS:
//			    HyjUtil.displayToast("项目成员" + itemId);
//				break;
//		}
//		return super.onContextItemSelected(item);
//	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//		super.onCreateContextMenu(menu, v, menuInfo);
//		menu.add(0, VIEW_PROJECT_MEMBERS, 0, "项目成员");
//		menu.add(0, ADD_SUB_PROJECT, 1, "创建子项目");
//		menu.add(CANCEL_LIST_ITEM, CANCEL_LIST_ITEM, CANCEL_LIST_ITEM, R.string.app_action_cancel_list_item);
	}
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(view.getId() == R.id.memberListItem_name) {
			String friendUserId = cursor.getString(columnIndex);
			Friend friend = new Select().from(Friend.class).where("friendUserId=?", friendUserId).executeSingle();
			if(friend != null){
				((TextView)view).setText(friend.getDisplayName());
			} else {
				User user = new Select().from(User.class).where("id=?", friendUserId).executeSingle();
				if(user != null){
					((TextView)view).setText(user.getDisplayName());
				}
			}
			return true;
		} else if(view.getId() == R.id.memberListItem_percentage) {
			double percentage = cursor.getDouble(columnIndex);
			((TextView)view).setText(percentage + "%");
			return true;
		} else {
			return false;
		}
	}	
}
