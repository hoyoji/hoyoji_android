package com.hoyoji.hoyoji.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjSimpleExpandableListAdapter;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserExpandableListFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Friend;

public class HomeListFragment extends HyjUserExpandableListFragment {

	private ArrayList<Map<String, Object>> mListGroupData = new ArrayList<Map<String, Object>>();
	private ArrayList<List<Map<String, Object>>> mListChildData = new ArrayList<List<Map<String, Object>>>();

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
	}

	@Override
	public ExpandableListAdapter useListViewAdapter() {
		HyjSimpleExpandableListAdapter adapter = new HyjSimpleExpandableListAdapter(
				getActivity(), mListGroupData, R.layout.home_listitem_group,
				new String[] { "date" },
				new int[] { R.id.homeListItem_group_date }, mListChildData,
				R.layout.home_listitem_row, new String[] {"title"}, new int[] {R.id.homeListItem_title});
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
		super.onCreateLoader(groupPos, arg1);
		Object loader;
		if (groupPos < 0) { // 这个是分类
			loader = new HomeGroupListLoader(getActivity(), null);
			return (Loader<Object>) loader;
		} else {
			loader = new HomeChildListLoader(getActivity(), null);
		}
		return (Loader<Object>) loader;
	}

	@Override
	public void onLoadFinished(Loader<Object> loader, Object list) {
		SimpleExpandableListAdapter adapter = (SimpleExpandableListAdapter) getListView()
				.getExpandableListAdapter();
		if (loader.getId() < 0) {
			boolean expandFirstGroup = adapter.getGroupCount() == 0;
			List<Map<String, Object>> groupList = (List<Map<String, Object>>) list;
			mListGroupData.addAll(groupList);
			List<Map<String, Object>> emptyChildList = new ArrayList<Map<String, Object>>();
			for(int i = 0; i < groupList.size(); i++){
				mListChildData.add(emptyChildList);
			}
			((SimpleExpandableListAdapter) getListView()
					.getExpandableListAdapter()).notifyDataSetChanged();
			if(expandFirstGroup){
				getListView().expandGroup(0);
			}
		} else {
			 if(adapter.getGroupCount() > loader.getId()){
				 mListChildData.set(loader.getId(), (List<Map<String, Object>>) list);
					((SimpleExpandableListAdapter) getListView()
							.getExpandableListAdapter()).notifyDataSetChanged();
			 } else {
				 getLoaderManager().destroyLoader(loader.getId());
			 }
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
		// SimpleCursorTreeAdapter adapter = (SimpleCursorTreeAdapter)
		// getListView().getExpandableListAdapter();
		// if(loader.getId() < 0){
		// adapter.setGroupCursor(null);
		// } else {
		// if(adapter.getGroupCount() > loader.getId()){
		// adapter.setChildrenCursor(loader.getId(), null);
		// } else {
		// getLoaderManager().destroyLoader(loader.getId());
		// }
		// }
		this.mListChildData.clear();
		this.mListGroupData.clear();
	}

	@Override
	public void onGroupExpand(int groupPosition) {
//		if(getLoaderManager().getLoader(groupPosition) != null){
//			getLoaderManager().destroyLoader(groupPosition);
//		}
		getLoaderManager().restartLoader(groupPosition, null, this);
	}

	
	@Override
	public boolean setViewValue(View view, Object object, String name) {
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setText(object.toString());
			return true;
		} else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText((CharSequence) ((Map)object).get(name));
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setCurrencySymbol("¥");
			//numericView.setNumber(object.toString());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setImage(object.toString());
			return true;
		} else {
			return false;
		}
	}
}
