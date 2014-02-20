package com.hoyoji.hoyoji.home;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

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
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjSimpleExpandableListAdapter;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserExpandableListFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyExpense;

public class HomeListFragment extends HyjUserExpandableListFragment {

	private ArrayList<Map<String, Object>> mListGroupData = new ArrayList<Map<String, Object>>();
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
			loader = new HomeGroupListLoader(getActivity(), arg1);
			return (Loader<Object>) loader;
		} else {
			loader = new HomeChildListLoader(getActivity(), arg1);
		}
		return (Loader<Object>) loader;
	}

	@Override
	public void onLoadFinished(Loader<Object> loader, Object list) {
		HyjSimpleExpandableListAdapter adapter = (HyjSimpleExpandableListAdapter) getListView()
				.getExpandableListAdapter();
		if (loader.getId() < 0) {
//			boolean expandFirstGroup = adapter.getGroupCount() == 0;
			List<Map<String, Object>> groupList = (List<Map<String, Object>>) list;
			mListGroupData.addAll(groupList);
			List<HyjModel> emptyChildList = new ArrayList<HyjModel>();
			for(int i = 0; i < groupList.size(); i++){
				mListChildData.add(emptyChildList);
				getListView().expandGroup(i);
			}
			adapter.notifyDataSetChanged();
//			if(expandFirstGroup){
//				getListView().expandGroup(0);
//			}
		} else {
			 if(adapter.getGroupCount() > loader.getId()){
				 mListChildData.set(loader.getId(), (List<HyjModel>) list);
				adapter.notifyDataSetChanged();
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

//		DateFormat dateFormat =  new SimpleDateFormat("yyyy-MM-dd");
//		Date d = new Date();
//		try {
//			d = dateFormat.parse((String) mListGroupData.get(groupPosition).get("date"));
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//		Calendar cal = Calendar.getInstance();
//		cal.setTimeInMillis(d.getTime())
//		cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
//		cal.clear(Calendar.MINUTE);
//		cal.clear(Calendar.SECOND);
//		cal.clear(Calendar.MILLISECOND);

		long dateInMilliSeconds = (Long) mListGroupData.get(groupPosition).get("dateInMilliSeconds");
		Bundle bundle = new Bundle();
		bundle.putLong("dateFrom", dateInMilliSeconds);
		bundle.putLong("dateTo", dateInMilliSeconds + 24*60*60*1000);
		getLoaderManager().restartLoader(groupPosition, bundle, this);
	}

	
	@Override
	public boolean setViewValue(View view, Object object, String name) {
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setText(((MoneyExpense)object).getDate());
			return true;
		} else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText(((MoneyExpense)object).getMoneyExpenseCategory());
			return true;
		} else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyExpense)object).getProject().getName());
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setCurrencySymbol("¥");
			numericView.setNumber(((MoneyExpense)object).getAmount());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setImage(((MoneyExpense)object).getPicture());
			return true;
		} else {
			return false;
		}
	}
}
