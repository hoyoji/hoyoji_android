package com.hoyoji.hoyoji.project;

import java.util.List;

import android.app.Activity;
import android.content.ClipData.Item;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.ClientSyncRecord;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.UserData;

public class ProjectListFragment extends HyjUserListFragment{
	public final static int ADD_SUB_PROJECT = 0;
	public final static int VIEW_PROJECT_MEMBERS = 1;
	private ContentObserver mChangeObserver = null;
	
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
				new String[] { "name", "id", "id"},
				new int[] { R.id.projectListItem_name, R.id.projectListItem_expenseTotal, R.id.projectListItem_incomeTotal },
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
		if (mChangeObserver == null) {
			mChangeObserver = new ChangeObserver();
			this.getActivity().getContentResolver()
					.registerContentObserver(
							ContentProvider.createUri(
									ProjectShareAuthorization.class, null), true,
							mChangeObserver);
		}
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
			openActivityWithFragment(ProjectFormFragment.class, R.string.projectFormFragment_title_edit, bundle);
		}
    }  

	@Override 
	public void onDeleteListItem(Long id){
		Project project = Project.load(Project.class, id);
		UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
		if(userData.getActiveProjectId().equals(project.getId())){
			HyjUtil.displayToast("默认项目不能删除");
			return;
		}
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
//			case ADD_SUB_PROJECT:
//			    HyjUtil.displayToast("创建子项目" + itemId);
//				break;
			case VIEW_PROJECT_MEMBERS:
				Bundle bundle = new Bundle();
				bundle.putLong("MODEL_ID", info.id);
				openActivityWithFragment(MemberListFragment.class, R.string.memberListFragment_title, bundle);
				break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, VIEW_PROJECT_MEMBERS, 0, "项目成员");
//		menu.add(0, ADD_SUB_PROJECT, 1, "创建子项目");
	}
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(view.getId() == R.id.projectListItem_name){
			((TextView)view).setText(cursor.getString(columnIndex));
			return true;
		} else if(view.getId() == R.id.projectListItem_expenseTotal) {
			HyjNumericView numericView = (HyjNumericView)view;
			Project project = HyjModel.getModel(Project.class, cursor.getString(columnIndex));
			List<ProjectShareAuthorization> projectShareAuthorizations = project.getProjectShareAuthorizations();
            Double projectExpenseTotal = 0.0;
			for(int i= 0; i<projectShareAuthorizations.size(); i++){
            	ProjectShareAuthorization psa = projectShareAuthorizations.get(i); 
            	projectExpenseTotal+= psa.getExpenseTotal();
            }
			numericView.setPrefix("支出:" + project.getCurrencySymbol());
			numericView.setSuffix(null);
			numericView.setTextColor(Color.parseColor("#FF0000"));
			numericView.setNumber(projectExpenseTotal);
			return true;
		}else if(view.getId() == R.id.projectListItem_incomeTotal) {
			HyjNumericView numericView = (HyjNumericView)view;
			Project project = HyjModel.getModel(Project.class, cursor.getString(columnIndex));
			List<ProjectShareAuthorization> projectShareAuthorizations = project.getProjectShareAuthorizations();
            Double projectIncomeTotal = 0.0;
			for(int i= 0; i<projectShareAuthorizations.size(); i++){
            	ProjectShareAuthorization psa = projectShareAuthorizations.get(i); 
            	projectIncomeTotal+= psa.getIncomeTotal();
            }
			numericView.setPrefix("收入:" + project.getCurrencySymbol());
			numericView.setSuffix(null);
			numericView.setTextColor(Color.parseColor("#339900"));
			numericView.setNumber(projectIncomeTotal);
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
							Thread.sleep(500);
						} catch (InterruptedException e) {}
						return null;
			        }
			        @Override
			        protected void onPostExecute(String result) {
						((SimpleCursorAdapter) getListAdapter()).notifyDataSetChanged();
			        }
			    };
			    mTask.execute();
			}
		}
	}


	@Override
	public void onDestroy() {
		if (mChangeObserver != null) {
			this.getActivity().getContentResolver()
					.unregisterContentObserver(mChangeObserver);
		}
		super.onDestroy();
	}
	
}
