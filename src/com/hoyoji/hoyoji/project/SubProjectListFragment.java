package com.hoyoji.hoyoji.project;

import java.util.List;

import android.app.Activity;
import android.content.ClipData.Item;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;

import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.ClientSyncRecord;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.MoneySearchListFragment;

public class SubProjectListFragment extends HyjUserListFragment {
//	public final static int ADD_SUB_PROJECT = 0;
	private static final int EDIT_PROJECT_DETAILS = 0;
	public final static int VIEW_PROJECT_MEMBERS = 1;
	private ContentObserver mChangeObserver = null;
	
	private OnSelectSubProjectsListener mOnSelectSubProjectsListener;
	
	public interface OnSelectSubProjectsListener {
		public void onSelectSubProjectsListener(String parentProject, String title);
	}
	
	public static SubProjectListFragment newInstance(String parentProjectId, String title){
		SubProjectListFragment fragment = new SubProjectListFragment();
		Bundle args = new Bundle();
		args.putString("parentProjectId", parentProjectId);
		args.putString("title", title);
		fragment.setArguments(args);
		return fragment;
	}
	
	
	public String getTitle(){
		String title = getArguments().getString("title");
		if(title == null){
			return "顶级项目";
		}
		return title;
	}
	
	@Override
	public Integer useContentView() {
		return R.layout.project_listfragment_subproject;
	}


	@Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.project_listitem_project,
				null,
				new String[] {"_id", "id", "id", "id", "id"},
				new int[] {R.id.projectListItem_picture, R.id.projectListItem_name, R.id.projectListItem_expenseTotal, R.id.projectListItem_incomeTotal, R.id.projectListItem_action_viewSubProjects },
				0); 
	}	


	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		super.onCreateLoader(arg0, arg1);
		String[] projections = null;
		String selection = null;
		String[] selectionArgs = null;

		String parentProjectId = getArguments().getString("parentProjectId");
		if(parentProjectId != null){
			selection = "id IN (SELECT subProjectId FROM ParentProject WHERE parentProjectId = ?)";
			selectionArgs = new String[]{ parentProjectId };
		} else {
			projections = new String[]{ "_id", "name", "id", "id AS _subProjectId" };
			selection = "NOT EXISTS (SELECT id FROM ParentProject WHERE subProjectId = _subProjectId) OR EXISTS (SELECT id FROM ParentProject WHERE subProjectId = _subProjectId AND parentProjectId IS NULL)";
		}
		Object loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(Project.class, null),
				projections, selection, selectionArgs, null
			);
		return (Loader<Object>)loader;
	}

	public void requery(String parentProjectId, String title){
		
		getArguments().putString("parentProjectId", parentProjectId);
		getArguments().putString("title", title);
		
		Loader<Object> loader = getLoaderManager().getLoader(0);
	    if (loader != null && !loader.isReset() ) { 
	    	getLoaderManager().restartLoader(0, null, this);
	    } else {
	    	getLoaderManager().initLoader(0, null, this);
	    }
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
			bundle.putLong("project_id", id);
			openActivityWithFragment(MoneySearchListFragment.class, R.string.projectListFragment_view_transactions, bundle);
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
		if(!getUserVisibleHint() || !getParentFragment().getUserVisibleHint()){
			return super.onContextItemSelected(item);
		}
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Bundle bundle = new Bundle();
		bundle.putLong("MODEL_ID", info.id);
		switch (item.getItemId()) {
			case EDIT_PROJECT_DETAILS:
				openActivityWithFragment(ProjectFormFragment.class, R.string.projectFormFragment_title_edit, bundle);
				break;
			case VIEW_PROJECT_MEMBERS:
				openActivityWithFragment(MemberListFragment.class, R.string.memberListFragment_title, bundle);
				break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo mi =(AdapterContextMenuInfo) menuInfo;
		if(mi.id == -1){
			return;
		}
		menu.add(0, EDIT_PROJECT_DETAILS, 0, "项目资料");
		menu.add(0, VIEW_PROJECT_MEMBERS, 1, "项目成员");
		menu.add(CANCEL_LIST_ITEM, CANCEL_LIST_ITEM, CANCEL_LIST_ITEM, R.string.app_action_cancel_list_item);
//		menu.add(0, ADD_SUB_PROJECT, 1, "创建子项目");
	}
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(view.getId() == R.id.projectListItem_name){
			Project project = HyjModel.getModel(Project.class, cursor.getString(columnIndex));
			((TextView)view).setText(project.getDisplayName());
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
			numericView.setPrefix(project.getCurrencySymbol());
			numericView.setSuffix(null);
			
			if(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor() != null){
				numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			}else{
				numericView.setTextColor(Color.parseColor("#FF0000"));
			}
			
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
			numericView.setPrefix(project.getCurrencySymbol());
			numericView.setSuffix(null);
			if(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor() != null){
				numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			}else{
				numericView.setTextColor(Color.parseColor("#339900"));
			}
			
			numericView.setNumber(projectIncomeTotal);
			return true;
		} else if(view.getId() == R.id.projectListItem_action_viewSubProjects){
			if(view.getTag() == null){
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						String parentProjectId = v.getTag().toString();
						Project project = HyjModel.getModel(Project.class, parentProjectId);
						mOnSelectSubProjectsListener.onSelectSubProjectsListener(parentProjectId, project.getDisplayName());
					}
				});
			}
			view.setTag(cursor.getString(columnIndex));
			return true;
		} else if(view.getId() == R.id.projectListItem_picture){
			Project project = HyjModel.getModel(Project.class, cursor.getString(cursor.getColumnIndex("id")));
			if(project.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
				((ImageView)view).setImageBitmap(HyjUtil.getCommonBitmap(R.drawable.ic_action_local_project));
			} else {
				((ImageView)view).setImageBitmap(HyjUtil.getCommonBitmap(R.drawable.ic_action_share));
			}
			
			if(view.getTag() == null){
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						Bundle bundle = new Bundle();
						bundle.putLong("MODEL_ID", (Long) v.getTag());
						openActivityWithFragment(ProjectFormFragment.class, R.string.projectFormFragment_title_edit, bundle);
					}
				});
			}
			view.setTag(cursor.getLong(columnIndex));
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
							Thread.sleep(200);
						} catch (InterruptedException e) {}
						return null;
			        }
			        @Override
			        protected void onPostExecute(String result) {
						((SimpleCursorAdapter) getListAdapter()).notifyDataSetChanged();
						mTask = null;
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


	public void setOnSelectSubProjectsListener(
			OnSelectSubProjectsListener onSelectSubProjectsListener) {
		mOnSelectSubProjectsListener = onSelectSubProjectsListener;
	}


	
//	private static class ProjectListSimpleCursorAdapter extends SimpleCursorAdapter {
//		int mLayout;
//		int[] mTo;
//		public ProjectListSimpleCursorAdapter(Context context, int layout,
//				Cursor c, String[] from, int[] to, int flags) {
//			super(context, layout, c, from, to, flags);
//			mLayout = layout;
//			mTo = to;
//		}
//		
//		@Override
//		public View getView(final int position, View convertView,
//                ViewGroup parent) {
//
//			View view = convertView;
//			View[] viewHolder;
//	        if (view == null) {
//	        	LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//	            view = vi.inflate(mLayout, parent);
//	            viewHolder = new View[mTo.length];
//	            for(int i=0; i<mTo.length; i++){
//	            	View v = view.findViewById(mTo[i]);
//	            	viewHolder[i] = v;
//	            }
//	            view.setTag(viewHolder);
//	        } else {
//	        	viewHolder = (View[])view.getTag();
//	        }
//	        return view;
//		}
//	}
}
