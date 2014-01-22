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

import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Project;

public class ProjectListFragment extends HyjUserListFragment{
	
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
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(getActivity(),
				ContentProvider.createUri(Project.class, null),
				null, null, null, null
			);
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
	}
	
}
