package com.hoyoji.hoyoji.money.moneyaccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjSimpleExpandableListAdapter;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserExpandableListFragment;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.UserData;

public class MoneyAccountListFragment extends HyjUserExpandableListFragment {
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
		HyjSimpleExpandableListAdapter adapter = new HyjSimpleExpandableListAdapter(
				getActivity(), mListGroupData, R.layout.moneyaccount_listitem_group,
				new String[] { "name" },
				new int[] { R.id.moneyAccountListItem_group_name }, 
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
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		
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
			bundle.putLong("MODEL_ID", id);
			openActivityWithFragment(MoneyAccountFormFragment.class, R.string.moneyAccountFormFragment_title_edit, bundle);
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
	
}
