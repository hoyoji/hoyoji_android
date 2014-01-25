package com.hoyoji.hoyoji.project;

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
import com.hoyoji.hoyoji.models.Project;

public class ProjectListFragment extends HyjUserListFragment{
	public final static int ADD_SUB_PROJECT = 0;
	public final static int VIEW_PROJECT_MEMBERS = 1;
	
	@Override
	public Integer useContentView() {
		return R.layout.project_listfragment_project;
	}

	@Override
	public Integer useOptionsMenuView() {
		return R.menu.project_listfragment_project;
	}

	@Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.project_listitem_project,
				null,
				new String[] { "name" },
				new int[] { R.id.projectListItem_name },
				0); 
	}	


	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		super.onCreateLoader(arg0, arg1);
		Object loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(Project.class, null),
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
		if(item.getItemId() == R.id.projectListFragment_action_project_addnew){
			openActivityWithFragment(ProjectFormFragment.class, R.string.projectFormFragment_title_addnew, null);
			return true;
		}
		return super.onOptionsItemSelected(item);
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
			openActivityWithFragment(ProjectFormFragment.class, R.string.projectFormFragment_title_edit, bundle);
		}
    }  

	@Override 
	public void onDeleteListItem(Long id){
		Project project = Project.load(Project.class, id);
		project.delete();
	    HyjUtil.displayToast("项目删除成功");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(!getUserVisibleHint()){
			return super.onContextItemSelected(item);
		}
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    Long itemId = getListAdapter().getItemId(info.position);
		switch (item.getItemId()) {
			case ADD_SUB_PROJECT:
			    HyjUtil.displayToast("创建子项目" + itemId);
				break;
			case VIEW_PROJECT_MEMBERS:
			    HyjUtil.displayToast("项目成员" + itemId);
				break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, VIEW_PROJECT_MEMBERS, 0, "项目成员");
		menu.add(0, ADD_SUB_PROJECT, 1, "创建子项目");
	}
}
