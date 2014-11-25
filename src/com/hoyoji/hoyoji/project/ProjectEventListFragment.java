package com.hoyoji.hoyoji.project;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.Model;
import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.models.Event;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.MoneyTemplate;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;

public class ProjectEventListFragment extends HyjUserListFragment {

	@Override
	public Integer useContentView() {
		return R.layout.project_listfragment_event;
	}
	
	@Override
	public Integer useToolbarView() {
		return super.useToolbarView();
	}
	
	@Override
	public Integer useOptionsMenuView() {
		return R.menu.project_listfragment_event;
	}

	@Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.home_listitem_row,
				null,
				new String[] {"date", "name", "description", "startDate", "endDate"},
				new int[] {R.id.homeListItem_date, R.id.homeListItem_title, R.id.homeListItem_remark, R.id.homeListItem_subTitle, R.id.homeListItem_subTitle},
				0); 
	}	

	@Override
	public Integer useMultiSelectMenuView() {
		return R.menu.multi_select_menu;
	}
	
	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		super.onCreateLoader(arg0, arg1);
		int offset = arg1.getInt("OFFSET");
		int limit = arg1.getInt("LIMIT");
		if(limit == 0){
			limit = getListPageSize();
		}
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		Project project =  Model.load(Project.class, modelId);
		Object loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(Event.class, null),
				null,
				"projectId=?", 
				new String[]{project.getId()}, 
				"date LIMIT " + (limit + offset) 
			);
		return (Loader<Object>)loader;
	}


	@Override
	public void onInitViewData() {
		super.onInitViewData();
	}
	
	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		if(l.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE){
			return;
		}
		if(id == -1) {
			 return;
		}
		
    }
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setText(cursor.getString(columnIndex));
			return true;
		}else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText(cursor.getString(columnIndex));
			return true;
		}else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(cursor.getString(columnIndex));
			return true;
		}else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(cursor.getString(columnIndex));
			return true;
		}else {
			return false;
		}
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(item.getItemId() == R.id.projectEventListFragment_action_add){
			Bundle bundle = new Bundle();
			bundle.putLong("PROJECT_ID", modelId);
			openActivityWithFragment(ProjectEventFormFragment.class, R.string.projectEventListFragment_action_addnew, bundle);
			return true;
		} else if(item.getItemId() == R.id.multi_select_menu_delete){
			deleteSelectedMessages();
			this.exitMultiChoiceMode(getListView());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void deleteSelectedMessages() {
		long[] ids = this.getListView().getCheckedItemIds();
		if(ids.length == 0){
			HyjUtil.displayToast("请选择至少一条快记模版");
			return;
		}
		for(int i=0; i<ids.length; i++){
			MoneyTemplate template = Model.load(MoneyTemplate.class, ids[i]);
			if(template != null){
				template.delete();
			}
		}
		
	}
	
}
