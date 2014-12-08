package com.hoyoji.hoyoji.project;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.activeandroid.Model;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.models.Event;
import com.hoyoji.hoyoji.models.EventMember;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyTemplate;
import com.hoyoji.hoyoji.models.Project;

public class ProjectEventListFragment extends HyjUserListFragment {
	private ContentObserver mChangeObserver = null;

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
				new String[] {"_id", "id", "startDate", "name", "id", "ownerUserId" ,"id", "id"},
				new int[] {R.id.homeListItem_picture, R.id.homeListItem_owner, R.id.homeListItem_date, R.id.homeListItem_title, R.id.homeListItem_remark, R.id.homeListItem_subTitle, R.id.homeListItem_owner, R.id.homeListItem_amount},
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
				"startDate DESC LIMIT " + (limit + offset) 
			);
		return (Loader<Object>)loader;
	}


	@Override
	public void onInitViewData() {
		super.onInitViewData();
		
		if (mChangeObserver == null) {
			mChangeObserver = new ChangeObserver();
			this.getActivity().getContentResolver().registerContentObserver(
					ContentProvider.createUri(
							EventMember.class, null), true,
							mChangeObserver);
		}
	}
	
	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		if(l.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE){
			return;
		}
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
			
			openActivityWithFragment(ProjectEventViewPagerFragment.class, R.string.projectEventMemberViewPagerFragment_title, bundle);
		}
    }
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(view.getId() == R.id.homeListItem_picture){
			ImageView imageView= (ImageView)view;
//			Project project = HyjModel.getModel(Project.class, cursor.getString(cursor.getColumnIndex("id")));
//			if(project.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
//				imageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_yellow));
//				imageView.setImageBitmap(HyjUtil.getCommonBitmap(R.drawable.ic_action_event_white));
//			} else {
				imageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_yellow));
				imageView.setImageBitmap(HyjUtil.getCommonBitmap(R.drawable.event));
//			}
			
//			if(view.getTag() == null){
//				view.setOnClickListener(new OnClickListener(){
//					@Override
//					public void onClick(View v) {
//						Bundle bundle = new Bundle();
//						bundle.putLong("MODEL_ID", (Long) v.getTag());
//						openActivityWithFragment(ProjectFormFragment.class, R.string.projectFormFragment_title_edit, bundle);
//					}
//				});
//			}
//			view.setTag(cursor.getLong(columnIndex));
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			Event event = HyjModel.getModel(Event.class, cursor.getString(columnIndex));
			((TextView)view).setText("" + event.getBalance());
			return true;
		} else if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setText(cursor.getString(columnIndex));
			return true;
		} else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText(cursor.getString(columnIndex));
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
//			if(cursor.getString(columnIndex) == null || "".equals(cursor.getString(columnIndex))){
//				((TextView)view).setText("无备注");
//			} else {
//				((TextView)view).setText(cursor.getString(columnIndex));
//			}
			String date = cursor.getString(cursor.getColumnIndex("date"));
			String startDate = cursor.getString(cursor.getColumnIndex("startDate"));
			String endDate = cursor.getString(cursor.getColumnIndex("endDate")); 
			String dt = HyjUtil.formatDateToIOS(new Date());
			List<EventMember> ems = new Select().from(EventMember.class).where("eventId = ? AND state <> ?", cursor.getString(columnIndex), "UnSignUp").execute();
			if(dt.compareTo(date)>=0 && dt.compareTo(startDate)<0) {
				((TextView)view).setText("[报名中]" + ems.size() + "人");
			} else if(dt.compareTo(startDate)>=0 && dt.compareTo(endDate)<0) {
				((TextView)view).setText("[进行中]" + ems.size() + "人");
			} else if(dt.compareTo(endDate)>=0) {
				((TextView)view).setText("[已结束]" + ems.size() + "人");
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_subTitle){
			Friend friend = new Select().from(Friend.class).where("friendUserId=?", cursor.getString(columnIndex)).executeSingle();
			((TextView)view).setText(friend.getFriendUserDisplayName(cursor.getString(columnIndex)));
//			String date = cursor.getString(cursor.getColumnIndex("date"));
//			String startDate = cursor.getString(cursor.getColumnIndex("startDate"));
//			String endDate = cursor.getString(cursor.getColumnIndex("endDate")); 
//			String dt = HyjUtil.formatDateToIOS(new Date());
//			if(dt.compareTo(date)>=0 && dt.compareTo(startDate)<0) {
//				((TextView)view).setText("[报名中]");
//			} else if(dt.compareTo(startDate)>=0 && dt.compareTo(endDate)<0) {
//				((TextView)view).setText("[进行中]");
//			} else if(dt.compareTo(endDate)>=0) {
//				((TextView)view).setText("[已结束]");
//			}
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			EventMember em = new Select().from(EventMember.class).where("eventId=? AND friendUserId=?", cursor.getString(columnIndex), HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
			if(em != null){
				if("UnSignUp".equals(em.getState())){
					((TextView)view).setText("[未报名]");
				} else if("SignUp".equals(em.getState())){
					((TextView)view).setText("[已报名]");
				} else if("SignIn".equals(em.getState())){
					((TextView)view).setText("[已签到]");
				} 
			} else {
				((TextView)view).setText("[未报名]");
			}
			return true;
		} else {
			return true;
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		Project project = Project.load(Project.class, modelId);
		if(!project.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId()) && getOptionsMenu().findItem(R.id.projectEventListFragment_action_add) != null){
			getOptionsMenu().findItem(R.id.projectEventListFragment_action_add).setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		Project project = Project.load(Project.class, modelId);
		if(!project.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
			HyjUtil.displayToast("您不能在共享来的账本添加活动");
			return true;
		}
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
	
	private class ChangeObserver extends ContentObserver {
		AsyncTask<String, Void, String> mTask = null;
		public ChangeObserver() {
			super(new Handler());
		}
	
		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}
	
	//	@Override
	//	public void onChange(boolean selfChange, Uri uri) {
	//		super.onChange(selfChange, uri);
	//	}
	
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
	
	//			    	getLoaderManager().restartLoader(0, new Bundle(), SubProjectListFragment.this);
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
	
}
