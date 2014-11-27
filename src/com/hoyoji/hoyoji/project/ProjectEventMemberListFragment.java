package com.hoyoji.hoyoji.project;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.activeandroid.Model;
import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.friend.FriendFormFragment;
import com.hoyoji.hoyoji.models.Event;
import com.hoyoji.hoyoji.models.EventMember;
import com.hoyoji.hoyoji.models.MoneyTemplate;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.User;

public class ProjectEventMemberListFragment extends HyjUserListFragment {

	@Override
	public Integer useContentView() {
		return R.layout.project_listfragment_event_member;
	}
	
	@Override
	public Integer useToolbarView() {
		return super.useToolbarView();
	}
	
	@Override
	public Integer useOptionsMenuView() {
		return R.menu.project_listfragment_event_member;
	}

	@Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.home_listitem_row,
				null,
				new String[] {"friendUserId", "friendUserId", "state"},
				new int[] {R.id.homeListItem_picture, R.id.homeListItem_title, R.id.homeListItem_subTitle},
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
		Event event =  Model.load(Event.class, modelId);
		Object loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(EventMember.class, null),
				null,
				"eventId=?", 
				new String[]{event.getId()}, 
				"state LIMIT " + (limit + offset) 
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
		Bundle bundle = new Bundle();
		bundle.putLong("MODEL_ID", id);
		
		openActivityWithFragment(ProjectEventMemberFormFragment.class, R.string.projectEventMemberFormFragment_title_edit, bundle);
    }
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(view.getId() == R.id.homeListItem_picture){
			String userId = cursor.getString(columnIndex);
			HyjImageView imageView = (HyjImageView)view;
			imageView.setDefaultImage(R.drawable.ic_action_person_white);
			if(cursor.getString(columnIndex) != null){
				User user = HyjModel.getModel(User.class, userId);
				if(user != null){
					imageView.setImage(user.getPictureId());
				} else {
					imageView.setImage((Picture)null);
				}
				if(HyjApplication.getInstance().getCurrentUser().getId().equals(userId)){
					imageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_red));
				} else {
					imageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_green));
				}
			} else {
				imageView.setImage((Picture)null);
				imageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_yellow));
			}
			return true;
		}else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText(cursor.getString(cursor.getColumnIndex("friendUserName")));
			return true;
		}else if(view.getId() == R.id.homeListItem_subTitle){
			if("signUp".equals(cursor.getString(columnIndex))){
				((TextView)view).setText("已报名");
			} else if("signIn".equals(cursor.getString(columnIndex))){
				((TextView)view).setText("已签到");
			} else{
				((TextView)view).setText("未报名");
			}
			return true;
		} else {
			return false;
		}
	   
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(item.getItemId() == R.id.projectEventMemberListFragment_action_add){
			Bundle bundle = new Bundle();
			bundle.putLong("EVENT_ID", modelId);
			openActivityWithFragment(ProjectEventMemberFormFragment.class, R.string.projectEventMemberFormFragment_action_addnew, bundle);
			return true;
		} 
//		else if(item.getItemId() == R.id.projectEventMemberListFragment_action_member_edit){
//			Bundle bundle = new Bundle();
//			bundle.putLong("MODEL_ID", modelId);
//			openActivityWithFragment(ProjectEventFormFragment.class, R.string.projectEventFormFragment_title_edit, bundle);
//			return true;
//		} 
		else if(item.getItemId() == R.id.multi_select_menu_delete){
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
