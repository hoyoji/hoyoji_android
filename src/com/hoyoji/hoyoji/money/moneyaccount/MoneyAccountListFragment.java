package com.hoyoji.hoyoji.money.moneyaccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjSimpleExpandableListAdapter;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserExpandableListFragment;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.friend.FriendCategoryFormFragment;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.SearchListFragment;
import com.hoyoji.hoyoji.project.MemberListFragment;
import com.hoyoji.hoyoji.project.ProjectFormFragment;

public class MoneyAccountListFragment extends HyjUserExpandableListFragment {
	private static final int EDIT_MONEYACCOUNT_DETAILS = 0;
	private List<Map<String, Object>> mListGroupData = new ArrayList<Map<String, Object>>();
	private ArrayList<List<HyjModel>> mListChildData = new ArrayList<List<HyjModel>>();

	@Override
	public Integer useContentView() {
		return R.layout.moneyaccount_listfragment_moneyaccount;
	}

	@Override
	public Integer useOptionsMenuView() {
		return R.menu.moneyaccount_listfragment_moneyaccount;
	}
	
	@Override
	public void onInitViewData() {
		super.onInitViewData();
		getListView().setGroupIndicator(null);
	}
	
	@Override
	public ExpandableListAdapter useListViewAdapter() {
		MoneyAccountGroupListAdapter adapter = new MoneyAccountGroupListAdapter(
				getActivity(), mListGroupData, R.layout.moneyaccount_listitem_group,
				new String[] { "name", "balanceTotal" },
				new int[] { R.id.moneyAccountListItem_group_name, R.id.moneyAccountListItem_group_balanceTotal }, 
				mListChildData,
				R.layout.moneyaccount_listitem_moneyaccount, 
				new String[] {"id", "currentBalance"}, 
				new int[] {R.id.moneyAccountListItem_name, R.id.moneyAccountListItem_currentBalance});
		return adapter;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.moneyAccountListFragment_action_moneyAccount_addnew){
			openActivityWithFragment(MoneyAccountFormFragment.class, R.string.moneyAccountFormFragment_title_addnew, null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(!getUserVisibleHint()){
			return super.onContextItemSelected(item);
		}
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		Bundle bundle = new Bundle();
		bundle.putLong("MODEL_ID", info.id);
		switch (item.getItemId()) {
			case EDIT_MONEYACCOUNT_DETAILS:
				openActivityWithFragment(MoneyAccountFormFragment.class, R.string.moneyAccountFormFragment_title, bundle);
				break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		ExpandableListContextMenuInfo adapterContextMenuInfo = (ExpandableListContextMenuInfo) menuInfo;
		if(ExpandableListView.getPackedPositionType(adapterContextMenuInfo.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_CHILD){
			if(adapterContextMenuInfo.id != -1){
				menu.add(0, EDIT_MONEYACCOUNT_DETAILS, 0, "账户资料");
				menu.add(CANCEL_LIST_ITEM, CANCEL_LIST_ITEM, CANCEL_LIST_ITEM, R.string.app_action_cancel_list_item);
			}
		}
	}
	@Override
	public Loader<Object> onCreateLoader(int groupPos, Bundle arg1) {
//		super.onCreateLoader(groupPos, arg1);
		Object loader;
		if (groupPos < 0) { // 这个是分类
			loader = new MoneyAccountGroupListLoader(getActivity(), arg1);
		} else {
			loader = new MoneyAccountChildListLoader(getActivity(), arg1);
		}
		return (Loader<Object>) loader;
	}

	@Override
	public void onLoadFinished(Loader loader, Object list) {
		HyjSimpleExpandableListAdapter adapter = (HyjSimpleExpandableListAdapter) getListView()
				.getExpandableListAdapter();
		if (loader.getId() < 0) {
			ArrayList<Map<String, Object>> groupList = (ArrayList<Map<String, Object>>) list;
			mListGroupData.clear();
			mListGroupData.addAll(groupList);
			for(int i = 0; i < groupList.size(); i++){
				if(mListChildData.size() <= i){
					mListChildData.add(null);
					getListView().expandGroup(i);
				} else if(getListView().collapseGroup(i)){
					getListView().expandGroup(i);
				}
			}
			adapter.notifyDataSetChanged();
			this.setFooterLoadFinished(((MoneyAccountGroupListLoader)loader).hasMoreData() ? this.mListPageSize : 0);
		} else {
				ArrayList<HyjModel> childList = (ArrayList<HyjModel>) list;
				mListChildData.set(loader.getId(), childList);
				adapter.notifyDataSetChanged();
		}
		// The list should now be shown.
		if (isResumed()) {
			// setListShown(true);
		} else {
			// setListShownNoAnimation(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<Object> loader) {
		HyjSimpleExpandableListAdapter adapter = (HyjSimpleExpandableListAdapter)
		 getListView().getExpandableListAdapter();
		 if(loader.getId() < 0){
				this.mListGroupData.clear();
		 } else {
			 if(adapter.getGroupCount() > loader.getId()){
					this.mListChildData.set(loader.getId(), null);
			 } else {
				 getLoaderManager().destroyLoader(loader.getId());
			 }
		 }
		
	}

	@Override
	public void onGroupExpand(int groupPosition) {
		String accountType = mListGroupData.get(groupPosition).get("accountType").toString();
		Bundle bundle = new Bundle();
		bundle.putString("accountType", accountType);
		getLoaderManager().restartLoader(groupPosition, bundle, this);
	}
	
	@Override
	public boolean setViewValue(View view, Object object, String name) {
		MoneyAccount moneyAccount = (MoneyAccount)object;
		if(view.getId() == R.id.moneyAccountListItem_name){
			TextView nameView = (TextView)view;
			nameView.setText(moneyAccount.getDisplayName());
			return true;
		} else if(view.getId() == R.id.moneyAccountListItem_currentBalance){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix(moneyAccount.getCurrencySymbol());
			numericView.setNumber(moneyAccount.getCurrentBalance());
			return true;
		} else {
			return false;
		}
	}
	
	@Override  
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		if(id == -1) {
			 return false;
		}
		if(getActivity().getCallingActivity() != null){
			Intent intent = new Intent();
			intent.putExtra("MODEL_ID", id);
			getActivity().setResult(Activity.RESULT_OK, intent);
			getActivity().finish();
		} else {
			Bundle bundle = new Bundle();
			bundle.putLong("moneyAccount_id", id);
			openActivityWithFragment(SearchListFragment.class, R.string.moneyAccountListFragment_title_moneyAccount_transactions, bundle);
		}
		return true;
    } 
	

	@Override 
	public void onDeleteListItem(Long id){
		MoneyAccount moneyAccount = MoneyAccount.load(MoneyAccount.class, id);
		UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
		if(userData.getActiveMoneyAccountId().equals(moneyAccount.getId())){
			HyjUtil.displayToast("默认账户不能删除");
			return;
		}
		moneyAccount.delete();
	    HyjUtil.displayToast("账户删除成功");
	}
	
	private static class MoneyAccountGroupListAdapter extends HyjSimpleExpandableListAdapter{

		public MoneyAccountGroupListAdapter(Context context,
	            List<Map<String, Object>> groupData, int expandedGroupLayout,
	                    String[] groupFrom, int[] groupTo,
	                    List<? extends List<? extends HyjModel>> childData,
	                    int childLayout, String[] childFrom,
	                    int[] childTo) {
			super( context, groupData, expandedGroupLayout, groupFrom, groupTo,childData, childLayout, 
					childFrom, childTo) ;
		}
		
		@Override
		 public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
		            ViewGroup parent) {
		        View v;
		        if (convertView == null) {
		            v = newGroupView(isExpanded, parent);
		        } else {
		            v = convertView;
		        }
		        bindGroupView(v, (Map<String, ?>) this.getGroup(groupPosition), mGroupFrom, mGroupTo);
		        
		        return v;
		    }
		 
		 private void bindGroupView(View view, Map<String, ?> data, String[] from, int[] to) {
		        int len = to.length;

		        for (int i = 0; i < len; i++) {
		            View v = view.findViewById(to[i]);
		            if (v != null) {
		            	if(v instanceof HyjNumericView){
		            		HyjNumericView balanceTotalView = (HyjNumericView)v;
		            		balanceTotalView.setPrefix(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol());
		            		balanceTotalView.setNumber(Double.valueOf(data.get(from[i]).toString()));
		            	} else if(v instanceof TextView){
		            		((TextView)v).setText((String)data.get(from[i]));
		            	}
		            }
		        }
		    }
	}
}
