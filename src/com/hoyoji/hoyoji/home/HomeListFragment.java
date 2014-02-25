package com.hoyoji.hoyoji.home;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;

import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjSimpleExpandableListAdapter;
import com.hoyoji.android.hyjframework.HyjSimpleExpandableListAdapter.OnFetchMoreListener;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserExpandableListFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.MoneyExpense;
import com.hoyoji.hoyoji.models.MoneyIncome;

public class HomeListFragment extends HyjUserExpandableListFragment implements OnFetchMoreListener {
	private List<Map<String, Object>> mListGroupData = new ArrayList<Map<String, Object>>();
	private ArrayList<List<HyjModel>> mListChildData = new ArrayList<List<HyjModel>>();

	@Override
	public Integer useContentView() {
		
		return R.layout.home_listfragment_home;
	}

	@Override
	public Integer useToolbarView() {
		return super.useToolbarView();
	}

	@Override
	public Integer useOptionsMenuView() {
		return R.menu.home_listfragment_home;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		((HyjSimpleExpandableListAdapter)getListView().getExpandableListAdapter()).setOnFetchMoreListener(this);
		getListView().setGroupIndicator(null);
	}

	@Override
	public ExpandableListAdapter useListViewAdapter() {
		HyjSimpleExpandableListAdapter adapter = new HyjSimpleExpandableListAdapter(
				getActivity(), mListGroupData, R.layout.home_listitem_group,
				new String[] { "date", "expenseTotal", "incomeTotal" },
				new int[] { R.id.homeListItem_group_date, 
							R.id.homeListItem_group_expenseTotal, 
							R.id.homeListItem_group_incomeTotal }, 
				mListChildData,
				R.layout.home_listitem_row, 
				new String[] {"picture", "subTitle", "title", "remark", "date", "amount", "owner"}, 
				new int[] {R.id.homeListItem_picture, R.id.homeListItem_subTitle, R.id.homeListItem_title, 
							R.id.homeListItem_remark, R.id.homeListItem_date,
							R.id.homeListItem_amount, R.id.homeListItem_owner});
		return adapter;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.homeListFragment_action_display_transaction_type_project) {
			HyjUtil.displayToast(R.string.homeListFragment_action_display_transaction_type);
			return true;
		}

		// 以下在 MainActivity.java 里面设置!
		//
		// else if(item.getItemId() ==
		// R.id.mainActivity_action_money_addnew_expense){
		// openActivityWithFragment(MoneyExpenseFormFragment.class,
		// R.string.moneyExpenseFormFragment_title_addnew, null);
		// return true;
		// }
		// else if(item.getItemId() ==
		// R.id.mainActivity_action_money_addnew_income){
		// openActivityWithFragment(MoneyExpenseListFragment.class,
		// R.string.moneyExpenseListFragment_title_all, null);
		// return true;
		// }
		// else if(item.getItemId() ==
		// R.id.mainActivity_action_money_addnew_transfer){
		// openActivityWithFragment(MoneyExpenseListFragment.class,
		// R.string.moneyExpenseListFragment_title_all, null);
		// return true;
		// }
		// else if(item.getItemId() ==
		// R.id.mainActivity_action_money_addnew_topup){
		// openActivityWithFragment(MoneyIncomeListFragment.class,
		// R.string.moneyIncomeListFragment_title_all, null);
		// return true;
		// }
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Object> onCreateLoader(int groupPos, Bundle arg1) {
//		super.onCreateLoader(groupPos, arg1);
		Object loader;
		if (groupPos < 0) { // 这个是分类
			loader = new HomeGroupListLoader(getActivity(), arg1);
			return (Loader<Object>) loader;
		} else {
			loader = new HomeChildListLoader(getActivity(), arg1);
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
			this.setFooterLoadFinished(((HomeGroupListLoader)loader).hasMoreData() ? this.mListPageSize : 0);
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
//		int i = 0;
//		for(Map.Entry<String, Map<String, Object>> entry : mListGroupData.entrySet()){
//			if(i == groupPosition){
				long dateInMilliSeconds = (Long) mListGroupData.get(groupPosition).get("dateInMilliSeconds");
				Bundle bundle = new Bundle();
				bundle.putLong("dateFrom", dateInMilliSeconds);
				bundle.putLong("dateTo", dateInMilliSeconds + 24*3600000);
				getLoaderManager().restartLoader(groupPosition, bundle, this);
//			}
//			i++;
//		}
	}

	
	@Override
	public boolean setViewValue(View view, Object object, String name) {
		if(object instanceof MoneyExpense){
			return setMoneyExpenseItemValue(view, object, name);
		} else if(object instanceof MoneyIncome){
			return setMoneyIncomeItemValue(view, object, name);
			
		}
		return false;
	}
	
	private boolean setMoneyExpenseItemValue(View view, Object object, String name){
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setText(((MoneyExpense)object).getDate());
			return true;
		} else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText(((MoneyExpense)object).getMoneyExpenseCategory());
			return true;
		} else if(view.getId() == R.id.homeListItem_subTitle){
//			((TextView)view).setText(((MoneyExpense)object).getProject().getName());
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setTextColor(Color.parseColor("#FF0000"));
			numericView.setCurrencySymbol("¥");
			numericView.setNumber(((MoneyExpense)object).getAmount());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setImage(((MoneyExpense)object).getPicture());
			return true;
		}  else if(view.getId() == R.id.homeListItem_owner){
//			((TextView)view).setText(((MoneyExpense)object).getOwnerUser().getDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyExpense)object).getDisplayRemark());
			return true;
		} else {
			return false;
		}
	}
	
	private boolean setMoneyIncomeItemValue(View view, Object object, String name){
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setText(((MoneyIncome)object).getDate());
			return true;
		} else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText(((MoneyIncome)object).getMoneyIncomeCategory());
			return true;
		} else if(view.getId() == R.id.homeListItem_subTitle){
//			((TextView)view).setText(((MoneyIncome)object).getProject().getName());
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setTextColor(Color.parseColor("#339900"));
			numericView.setCurrencySymbol("¥");
			numericView.setNumber(((MoneyIncome)object).getAmount());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setImage(((MoneyIncome)object).getPicture());
			return true;
		}  else if(view.getId() == R.id.homeListItem_owner){
//			((TextView)view).setText(((MoneyIncome)object).getOwnerUser().getDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyIncome)object).getDisplayRemark());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onFetchMore() {
//		Bundle bundle = new Bundle();
//		bundle.putString("target", "findData");
//		bundle.putString("postData", (new JSONArray()).put(data).toString());
//		Loader loader = getLoaderManager().getLoader(-1);
//		((HomeGroupListLoader)loader).fetchMore(null);	
	}

	@Override
	public void doFetchMore(int offset, int pageSize){
		Loader loader = getLoaderManager().getLoader(-1);
		((HomeGroupListLoader)loader).fetchMore(null);	
	}
}
