package com.hoyoji.hoyoji.project;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
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
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Currency;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.Picture;
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
				new String[] { "friendUserId", "friendUserId", "sharePercentage", "state", "id", "id", "id"},
				new int[] { R.id.memberListItem_picture, R.id.memberListItem_name, R.id.memberListItem_percentage, R.id.memberListItem_remark, R.id.memberListItem_actualTotal, R.id.memberListItem_apportionTotal, R.id.memberListItem_settlement},
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
			Friend friend = null;
			if(friendUserId != null){
				friend = new Select().from(Friend.class).where("friendUserId=?", friendUserId).executeSingle();
				if(friend != null){
					((TextView)view).setText(friend.getDisplayName());
				} else {
					User user = HyjModel.getModel(User.class, friendUserId);
					if(user != null){
						((TextView)view).setText(user.getDisplayName());
					} else {
						((TextView)view).setText(cursor.getString(cursor.getColumnIndex("friendUserName")));
					}
				}
			} else {
				((TextView)view).setText(null);
			}
			return true;
		} else if(view.getId() == R.id.memberListItem_picture) {
			String friendUserId = cursor.getString(columnIndex);
			if(friendUserId != null){
				User user = HyjModel.getModel(User.class, friendUserId);
				if(user == null){
					((HyjImageView)view).setImage((Picture)null);
				} else {
					((HyjImageView)view).setImage(user.getPictureId());
				}
			} else {
				((HyjImageView)view).setImage((Picture)null);
			}
			return true;
		} else if(view.getId() == R.id.memberListItem_percentage) {
			double percentage = cursor.getDouble(columnIndex);
			((HyjNumericView)view).setPrefix(null);
			((HyjNumericView)view).setSuffix("%");
			((HyjNumericView)view).setNumber(percentage);
			return true;
		} else if(view.getId() == R.id.memberListItem_remark) {
			String state = cursor.getString(columnIndex);
			if(state.equalsIgnoreCase("Wait")){
				((TextView)view).setText(R.string.memberListFragment_state_wait);
			} else {
				((TextView)view).setText("");
			}
			return true;
		} else if(view.getId() == R.id.memberListItem_actualTotal) {
			HyjNumericView numericView = (HyjNumericView)view;
			ProjectShareAuthorization projectShareAuthorization = HyjModel.getModel(ProjectShareAuthorization.class, cursor.getString(columnIndex));
			Double actualTotal = projectShareAuthorization.getActualTotal();
			String currencySymbol = projectShareAuthorization.getProject().getCurrencySymbol();
			if(actualTotal < 0){
				actualTotal = -actualTotal;
				numericView.setPrefix("已经收入:" + currencySymbol);
//				numericView.setTextColor(Color.parseColor("#339900"));
			}else{
				numericView.setPrefix("已经支出:" + currencySymbol);
//				if(actualTotal.equals(0.0)){
//					numericView.setTextColor(Color.parseColor("#000000"));
//				}else{
//			    	numericView.setTextColor(Color.parseColor("#FF0000"));
//				}
			}
			numericView.setSuffix(null);
			numericView.setNumber(actualTotal);
			return true;
		} else if(view.getId() == R.id.memberListItem_apportionTotal) {
			HyjNumericView numericView = (HyjNumericView)view;
			ProjectShareAuthorization projectShareAuthorization = HyjModel.getModel(ProjectShareAuthorization.class, cursor.getString(columnIndex));
			Double apportionTotal = projectShareAuthorization.getApportionTotal();
			String currencySymbol = projectShareAuthorization.getProject().getCurrencySymbol();
			if(apportionTotal < 0){
				apportionTotal = -apportionTotal;
				numericView.setPrefix("分摊收入:" + currencySymbol);
//				numericView.setTextColor(Color.parseColor("#339900"));
			}else{
				numericView.setPrefix("分摊支出:" + currencySymbol);
//				if(apportionTotal.equals(0.0)){
//					numericView.setTextColor(Color.parseColor("#000000"));
//				}else{
//				numericView.setTextColor(Color.parseColor("#FF0000"));
//				}
			}
			numericView.setSuffix(null);
			numericView.setNumber(apportionTotal);
			return true;
		} else if(view.getId() == R.id.memberListItem_settlement) {
			HyjNumericView numericView = (HyjNumericView)view;
			ProjectShareAuthorization projectShareAuthorization = HyjModel.getModel(ProjectShareAuthorization.class, cursor.getString(columnIndex));
			Double settlement = projectShareAuthorization.getSettlement();
			String currencySymbol = projectShareAuthorization.getProject().getCurrencySymbol();
			if(settlement < 0){
				settlement = -settlement;
				numericView.setPrefix("还要支出:" + currencySymbol);
				numericView.setTextColor(Color.parseColor("#FF0000"));
			}else{
				numericView.setPrefix("还要收入:" + currencySymbol);
				if(settlement.equals(0.0)){
					numericView.setTextColor(Color.parseColor("#000000"));
				}else{
				numericView.setTextColor(Color.parseColor("#339900"));
				}
			}
			numericView.setSuffix(null);
			numericView.setNumber(settlement);
			return true;
		} else {
			return false;
		}
	}	
}
