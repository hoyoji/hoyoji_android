package com.hoyoji.hoyoji.money;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjSimpleExpandableListAdapter;
import com.hoyoji.android.hyjframework.HyjSimpleExpandableListAdapter.OnFetchMoreListener;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserExpandableListFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.friend.FriendFormFragment;
import com.hoyoji.hoyoji.message.FriendMessageFormFragment;
import com.hoyoji.hoyoji.message.ProjectMessageFormFragment;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyBorrow;
import com.hoyoji.hoyoji.models.MoneyExpense;
import com.hoyoji.hoyoji.models.MoneyIncome;
import com.hoyoji.hoyoji.models.MoneyLend;
import com.hoyoji.hoyoji.models.MoneyPayback;
import com.hoyoji.hoyoji.models.MoneyReturn;
import com.hoyoji.hoyoji.models.MoneyTransfer;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.money.MoneyApportionField;
import com.hoyoji.hoyoji.money.MoneyBorrowFormFragment;
import com.hoyoji.hoyoji.money.MoneyExpenseFormFragment;
import com.hoyoji.hoyoji.money.MoneyExpenseListFragment;
import com.hoyoji.hoyoji.money.MoneyIncomeFormFragment;
import com.hoyoji.hoyoji.money.MoneyLendFormFragment;
import com.hoyoji.hoyoji.money.MoneyPaybackFormFragment;
import com.hoyoji.hoyoji.money.MoneyReturnFormFragment;
import com.hoyoji.hoyoji.money.MoneyTransferFormFragment;

public class SearchListFragment extends HyjUserExpandableListFragment implements OnFetchMoreListener {
	private List<Map<String, Object>> mListGroupData = new ArrayList<Map<String, Object>>();
	private ArrayList<List<HyjModel>> mListChildData = new ArrayList<List<HyjModel>>();

	Project mProject;
	
	@Override
	public Integer useContentView() {
		
		return R.layout.money_listfragment_search;
	}

	@Override
	public Integer useToolbarView() {
		return super.useToolbarView();
	}

	@Override
	public Integer useOptionsMenuView() {
		return R.menu.money_listfragment_search;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		Intent intent = getActivity().getIntent();
		final Long project_id = intent.getLongExtra("project_id", -1);
		if(project_id != -1){
			mProject =  new Select().from(Project.class).where("_id=?", project_id).executeSingle();
			String subTitle = mProject.getDisplayName();
			if(subTitle != null){
				((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(subTitle);
			}
		}
		
		
		((HyjSimpleExpandableListAdapter)getListView().getExpandableListAdapter()).setOnFetchMoreListener(this);
		getListView().setGroupIndicator(null);
	}
	
	public void initLoader(int loaderId){
		Bundle queryParams = new Bundle();
		if(mProject != null){
			queryParams.putString("projectId", mProject.getId());
		}
		Loader<Object> loader = getLoaderManager().getLoader(loaderId);
		if(loader != null && !loader.isReset()){
			getLoaderManager().restartLoader(loaderId, queryParams,this);
		} else {
			getLoaderManager().initLoader(loaderId, queryParams,this);
		}
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
		if (item.getItemId() == R.id.searchListFragment_action_display_transaction_type_project) {
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Object> onCreateLoader(int groupPos, Bundle arg1) {
//		super.onCreateLoader(groupPos, arg1);
		Object loader;
		if (groupPos < 0) { // 这个是分类
			loader = new SearchGroupListLoader(getActivity(), arg1);
		} else {
			loader = new SearchChildListLoader(getActivity(), arg1);
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
			this.setFooterLoadFinished(((SearchGroupListLoader)loader).hasMoreData() ? this.mListPageSize : 0);
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
		} else if(object instanceof MoneyTransfer){
			return setMoneyTransferItemValue(view, object, name);
		} else if(object instanceof MoneyBorrow){
			return setMoneyBorrowItemValue(view, object, name);
		} else if(object instanceof MoneyLend){
			return setMoneyLendItemValue(view, object, name);
		} else if(object instanceof MoneyReturn){
			return setMoneyReturnItemValue(view, object, name);
		} else if(object instanceof MoneyPayback){
			return setMoneyPaybackItemValue(view, object, name);
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
			((TextView)view).setText(((MoneyExpense)object).getProject().getDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setTextColor(Color.parseColor("#FF0000"));
			numericView.setPrefix(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrency().getSymbol());
			numericView.setSuffix(null);
			numericView.setNumber(((MoneyExpense)object).getAmount());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setBackgroundResource(R.drawable.ic_action_picture);
			imageView.setImage(((MoneyExpense)object).getPicture());
			return true;
		}  else if(view.getId() == R.id.homeListItem_owner){
			((TextView)view).setText(((MoneyExpense)object).getOwnerUser().getDisplayName());
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
			((TextView)view).setText(((MoneyIncome)object).getProject().getDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setTextColor(Color.parseColor("#339900"));
			numericView.setPrefix(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrency().getSymbol());
			numericView.setNumber(((MoneyIncome)object).getAmount());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setBackgroundResource(R.drawable.ic_action_picture);
			imageView.setImage(((MoneyIncome)object).getPicture());
			return true;
		}  else if(view.getId() == R.id.homeListItem_owner){
			((TextView)view).setText(((MoneyIncome)object).getOwnerUser().getDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyIncome)object).getDisplayRemark());
			return true;
		} else {
			return false;
		}
	}
	
	private boolean setMoneyTransferItemValue(View view, Object object, String name) {
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setText(((MoneyTransfer)object).getDate());
			return true;
		}  else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText("转账");
			return true;
		}  else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyTransfer)object).getProject().getDisplayName());
			return true;
	    } else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrency().getSymbol());
			numericView.setNumber(((MoneyTransfer)object).getTransferOutAmount());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setBackgroundResource(R.drawable.ic_action_picture);
			imageView.setImage(((MoneyTransfer)object).getPicture());
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			((TextView)view).setText(((MoneyTransfer)object).getOwnerUser().getDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyTransfer)object).getDisplayRemark());
			return true;
		} else{
			return false;
		}
	}
	
	private boolean setMoneyBorrowItemValue(View view, Object object, String name) {
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setText(((MoneyBorrow)object).getDate());
			return true;
		}  else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText("借入");
			return true;
		}  else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyBorrow)object).getProject().getDisplayName());
			return true;
	    } else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrency().getSymbol());
			numericView.setNumber(((MoneyBorrow)object).getAmount());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setBackgroundResource(R.drawable.ic_action_picture);
			imageView.setImage(((MoneyBorrow)object).getPicture());
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			((TextView)view).setText(((MoneyBorrow)object).getOwnerUser().getDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyBorrow)object).getDisplayRemark());
			return true;
		} else{
			return false;
		}
	}
	
	private boolean setMoneyLendItemValue(View view, Object object, String name) {
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setText(((MoneyLend)object).getDate());
			return true;
		}  else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText("借出");
			return true;
		}  else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyLend)object).getProject().getDisplayName());
			return true;
	    } else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrency().getSymbol());
			numericView.setNumber(((MoneyLend)object).getAmount());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setBackgroundResource(R.drawable.ic_action_picture);
			imageView.setImage(((MoneyLend)object).getPicture());
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			((TextView)view).setText(((MoneyLend)object).getOwnerUser().getDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyLend)object).getDisplayRemark());
			return true;
		} else{
			return false;
		}
	}
	
	private boolean setMoneyReturnItemValue(View view, Object object, String name) {
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setText(((MoneyReturn)object).getDate());
			return true;
		}  else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText("还款");
			return true;
		}  else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyReturn)object).getProject().getDisplayName());
			return true;
	    } else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrency().getSymbol());
			numericView.setNumber(((MoneyReturn)object).getAmount());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setBackgroundResource(R.drawable.ic_action_picture);
			imageView.setImage(((MoneyReturn)object).getPicture());
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			((TextView)view).setText(((MoneyReturn)object).getOwnerUser().getDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyReturn)object).getDisplayRemark());
			return true;
		} else{
			return false;
		}
	}
	
	private boolean setMoneyPaybackItemValue(View view, Object object, String name) {
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setText(((MoneyPayback)object).getDate());
			return true;
		}  else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText("收款");
			return true;
		}  else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyPayback)object).getProject().getDisplayName());
			return true;
	    } else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrency().getSymbol());
			numericView.setNumber(((MoneyPayback)object).getAmount());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setBackgroundResource(R.drawable.ic_action_picture);
			imageView.setImage(((MoneyPayback)object).getPicture());
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			((TextView)view).setText(((MoneyPayback)object).getOwnerUser().getDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyPayback)object).getDisplayRemark());
			return true;
		} else{
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
		((SearchGroupListLoader)loader).fetchMore(null);	
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
			return true;
		} else {
			HyjModel object = (HyjModel) ((HyjSimpleExpandableListAdapter)parent.getExpandableListAdapter()).getChild(groupPosition, childPosition);
			Bundle bundle = new Bundle();
			bundle.putLong("MODEL_ID", object.get_mId());
			if(object instanceof MoneyExpense){
				openActivityWithFragment(MoneyExpenseFormFragment.class, R.string.moneyExpenseFormFragment_title_edit, bundle);
				return true;
			} else if(object instanceof MoneyIncome){
				openActivityWithFragment(MoneyIncomeFormFragment.class, R.string.moneyIncomeFormFragment_title_edit, bundle);
				return true;
			} else if(object instanceof MoneyTransfer){
				openActivityWithFragment(MoneyTransferFormFragment.class, R.string.moneyTransferFormFragment_title_edit, bundle);
				return true;
			} else if(object instanceof MoneyBorrow){
				openActivityWithFragment(MoneyBorrowFormFragment.class, R.string.moneyBorrowFormFragment_title_edit, bundle);
				return true;
			} else if(object instanceof MoneyLend){
				openActivityWithFragment(MoneyLendFormFragment.class, R.string.moneyLendFormFragment_title_edit, bundle);
				return true;
			} else if(object instanceof MoneyReturn){
				openActivityWithFragment(MoneyReturnFormFragment.class, R.string.moneyReturnFormFragment_title_edit, bundle);
				return true;
			} else if(object instanceof MoneyPayback){
				openActivityWithFragment(MoneyPaybackFormFragment.class, R.string.moneyPaybackFormFragment_title_edit, bundle);
				return true;
			} else if(object instanceof Message){
				Message msg = (Message)object;
				if(msg.getType().equals("System.Friend.AddRequest") ){
					openActivityWithFragment(FriendMessageFormFragment.class, R.string.friendAddRequestMessageFormFragment_title_addrequest, bundle);
					return true;
				} else if(msg.getType().equals("System.Friend.AddResponse") ){
					openActivityWithFragment(FriendMessageFormFragment.class, R.string.friendAddRequestMessageFormFragment_title_addresponse, bundle);
					return true;
				} else if(msg.getType().equals("System.Friend.Delete") ){
					openActivityWithFragment(FriendMessageFormFragment.class, R.string.friendAddRequestMessageFormFragment_title_delete, bundle);
					return true;
				} else if(msg.getType().equals("Project.Share.AddRequest") ){
					openActivityWithFragment(ProjectMessageFormFragment.class, R.string.projectMessageFormFragment_title_addrequest, bundle);
					return true;
				} else if(msg.getType().equals("Project.Share.Accept") ){
					openActivityWithFragment(ProjectMessageFormFragment.class, R.string.projectMessageFormFragment_title_accept, bundle);
					return true;
				} else if(msg.getType().equals("Project.Share.Delete") ){
					openActivityWithFragment(ProjectMessageFormFragment.class, R.string.projectMessageFormFragment_title_delete, bundle);
					return true;
				}
			}
		}
		return false;
    } 
	
}