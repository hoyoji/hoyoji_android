package com.hoyoji.hoyoji.money.moneyaccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjSimpleExpandableListAdapter;
import com.hoyoji.android.hyjframework.fragment.HyjUserExpandableListFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.message.FriendMessageFormFragment;
import com.hoyoji.hoyoji.message.ProjectMessageFormFragment;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyBorrow;
import com.hoyoji.hoyoji.models.MoneyExpenseContainer;
import com.hoyoji.hoyoji.models.MoneyIncomeContainer;
import com.hoyoji.hoyoji.models.MoneyLend;
import com.hoyoji.hoyoji.models.MoneyPayback;
import com.hoyoji.hoyoji.models.MoneyReturn;
import com.hoyoji.hoyoji.models.MoneyTransfer;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.money.MoneyBorrowFormFragment;
import com.hoyoji.hoyoji.money.MoneyDepositExpenseFormFragment;
import com.hoyoji.hoyoji.money.MoneyDepositIncomeContainerFormFragment;
import com.hoyoji.hoyoji.money.MoneyExpenseContainerFormFragment;
import com.hoyoji.hoyoji.money._MoneyExpenseFormFragment;
import com.hoyoji.hoyoji.money.MoneyIncomeContainerFormFragment;
import com.hoyoji.hoyoji.money._MoneyIncomeFormFragment;
import com.hoyoji.hoyoji.money.MoneyLendFormFragment;
import com.hoyoji.hoyoji.money.MoneyPaybackFormFragment;
import com.hoyoji.hoyoji.money.MoneyReturnFormFragment;
import com.hoyoji.hoyoji.money.MoneyTransferFormFragment;

public class MoneyAccountDebtDetailsListFragment extends HyjUserExpandableListFragment {
	private static final int GET_SEARCH_QUERY = 0;
	private List<Map<String, Object>> mListGroupData = new ArrayList<Map<String, Object>>();
	private ArrayList<List<HyjModel>> mListChildData = new ArrayList<List<HyjModel>>();

	private Project mProject;
	private MoneyAccount mMoneyAccount;
	private Friend mFriend;
	private Long mDateFrom;
	private Long mDateTo;
	private String mDisplayType;
	
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
		return R.menu.money_listfragment_search_edit;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		Intent intent = getActivity().getIntent();
		String subTitle = null;
		final Long project_id = intent.getLongExtra("project_id", -1);
		if(project_id != -1){
			mProject =  new Select().from(Project.class).where("_id=?", project_id).executeSingle();
			subTitle = mProject.getDisplayName();
		}
		final Long moneyAccount_id = intent.getLongExtra("moneyAccount_id", -1);
		if(moneyAccount_id != -1){
			mMoneyAccount =  new Select().from(MoneyAccount.class).where("_id=?", moneyAccount_id).executeSingle();
			subTitle = mMoneyAccount.getDisplayName();
		}
		final Long friend_id = intent.getLongExtra("friend_id", -1);
		if(friend_id != -1){
			mFriend =  new Select().from(Friend.class).where("_id=?", friend_id).executeSingle();
			subTitle = mFriend.getDisplayName();
		}
		
		if(subTitle != null){
			((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(subTitle);
		}
		
//		((HyjSimpleExpandableListAdapter)getListView().getExpandableListAdapter()).setOnFetchMoreListener(this);
		getListView().setGroupIndicator(null);
	}
	
	public void initLoader(int loaderId){
		Bundle queryParams = buildQueryParams();
		if(!queryParams.isEmpty()){
			Loader<Object> loader = getLoaderManager().getLoader(loaderId);
			if(loader != null && !loader.isReset()){
				getLoaderManager().restartLoader(loaderId, queryParams, this);
			} else {
				getLoaderManager().initLoader(loaderId, queryParams, this);
			}
		}
	}
	
	private Bundle buildQueryParams() {
		Bundle queryParams = new Bundle();
		if(mProject != null){
			queryParams.putString("projectId", mProject.getId());
		}
		
		if(mFriend != null){
			if(mFriend.getFriendUserId() != null){
				queryParams.putString("friendUserId", mFriend.getFriendUserId());
			} else {
				queryParams.putString("localFriendId", mFriend.getId());
			}
		} else {
			if(mMoneyAccount != null){
				queryParams.putString("moneyAccountId", mMoneyAccount.getId());
				if(mMoneyAccount.getFriendId() != null){
					queryParams.putString("localFriendId", mMoneyAccount.getFriendId());
				} else if(!mMoneyAccount.getName().equals("__ANONYMOUS__")){
					queryParams.putString("friendUserId", mMoneyAccount.getName());
				}
			}
		}

		if(mDateFrom != null){
			queryParams.putLong("dateFrom", mDateFrom);
		}
		if(mDateTo != null){
			queryParams.putLong("dateTo", mDateTo);
		}
		if(mDisplayType != null){
			queryParams.putString("displayType", mDisplayType);
		}
		return queryParams;
	}

	@Override
	public ExpandableListAdapter useListViewAdapter() {
		SearchGroupListAdapter adapter = new SearchGroupListAdapter(
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
		Bundle queryParams = buildQueryParams();
		queryParams.remove("moneyAccountId"); // 借贷账户不用传入Forms, 只需传入借贷账户相关的好友即可
		if (item.getItemId() == R.id.searchListFragment_action_search) {
			openActivityWithFragmentForResult(MoneyAccountDebtSearchFormFragment.class, R.string.searchDialogFragment_title, queryParams, GET_SEARCH_QUERY);
			return true;
		} else if (item.getItemId() == R.id.searchListFragment_action_edit) {
			Bundle bundle = new Bundle();
			bundle.putLong("MODEL_ID", mMoneyAccount.get_mId());
			openActivityWithFragment(MoneyAccountFormFragment.class, R.string.moneyAccountFormFragment_title, bundle);
			return true;
		}
		// Handle your other action bar items...
		if (item.getItemId() == R.id.mainActivity_action_money_addnew_expense) {
			openActivityWithFragment(MoneyExpenseContainerFormFragment.class,
					R.string.moneyExpenseFormFragment_title_addnew, queryParams);
			return true;
		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_income) {
			openActivityWithFragment(MoneyIncomeContainerFormFragment.class,
					R.string.moneyIncomeFormFragment_title_addnew, queryParams);
			return true;
		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_transfer) {
			openActivityWithFragment(MoneyTransferFormFragment.class,
					R.string.moneyTransferFormFragment_title_addnew, queryParams);
			return true;
		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_borrow) {
			openActivityWithFragment(MoneyBorrowFormFragment.class,
					R.string.moneyBorrowFormFragment_title_addnew, queryParams);
			return true;
		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_lend) {
			openActivityWithFragment(MoneyLendFormFragment.class,
					R.string.moneyLendFormFragment_title_addnew, queryParams);
			return true;
		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_return) {
			openActivityWithFragment(MoneyReturnFormFragment.class,
					R.string.moneyReturnFormFragment_title_addnew, queryParams);
			return true;
		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_payback) {
			openActivityWithFragment(MoneyPaybackFormFragment.class,
					R.string.moneyPaybackFormFragment_title_addnew, queryParams);
			return true;
		}
		else if (item.getItemId() == R.id.mainActivity_action_money_addnew_depositeExpense) {
			openActivityWithFragment(MoneyDepositExpenseFormFragment.class,
					R.string.moneyDepositExpenseFormFragment_title_addnew, queryParams);
			return true;
		}else if (item.getItemId() == R.id.mainActivity_action_money_addnew_depositeIncome) {
			openActivityWithFragment(MoneyDepositIncomeContainerFormFragment.class,
					R.string.moneyDepositIncomeContainerFormFragment_title_addnew, queryParams);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Object> onCreateLoader(int groupPos, Bundle arg1) {
//		super.onCreateLoader(groupPos, arg1);
		Object loader;
		if (groupPos < 0) { // 这个是分类
			loader = new MoneyAccountDebtDetailsGroupListLoader(getActivity(), arg1);
		} else {
			loader = new MoneyAccountDebtDetailsChildListLoader(getActivity(), arg1);
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
			this.setFooterLoadFinished(((MoneyAccountDebtDetailsGroupListLoader)loader).hasMoreData() ? this.mListPageSize : 0);
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
		long dateInMilliSeconds = (Long) mListGroupData.get(groupPosition).get("dateInMilliSeconds");
		Bundle bundle = buildQueryParams();
		bundle.putLong("dateFrom", dateInMilliSeconds);
		bundle.putLong("dateTo", dateInMilliSeconds + 24*3600000);

		getLoaderManager().restartLoader(groupPosition, bundle, this);
	}

	
	@Override
	public boolean setViewValue(View view, Object object, String name) {
		if(object instanceof MoneyBorrow){
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

	
	private boolean setMoneyBorrowItemValue(View view, Object object, String name) {
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setText(((MoneyBorrow)object).getDate());
			return true;
		}  else if(view.getId() == R.id.homeListItem_title){
			if(((MoneyBorrow)object).getMoneyIncomeApportionId() != null){
				((TextView)view).setText("(分摊)借入");
			} else if(((MoneyBorrow)object).getMoneyExpenseApportionId() != null){
				((TextView)view).setText("(分摊)借入");
			} else if(((MoneyBorrow)object).getBorrowType().equalsIgnoreCase("Deposit")){
				((TextView)view).setText("预收会费");
			} else {
				((TextView)view).setText("借入");
			}
			return true;
		}  else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyBorrow)object).getProject().getDisplayName());
			return true;
	    } else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrency().getSymbol());
			numericView.setNumber(((MoneyBorrow)object).getLocalAmount());
			numericView.setTextColor(Color.BLACK);
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setBackgroundResource(R.drawable.ic_action_picture);
			imageView.setImage(((MoneyBorrow)object).getPicture());
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			String ownerUserId = ((MoneyBorrow)object).getOwnerUserId();
			if(ownerUserId.equalsIgnoreCase(HyjApplication.getInstance().getCurrentUser().getId())){
				((TextView)view).setText("");
			}else{
				Friend friend = new Select().from(Friend.class).where("friendUserId=?",ownerUserId).executeSingle();
				((TextView)view).setText(friend.getDisplayName());
			}
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
			if(((MoneyLend)object).getMoneyExpenseApportionId() != null){
				((TextView)view).setText("(分摊)借出");
			} else if(((MoneyLend)object).getMoneyIncomeApportionId() != null){
				((TextView)view).setText("(分摊)借出");
			} else if(((MoneyLend)object).getLendType().equalsIgnoreCase("Deposit")){
				((TextView)view).setText("预缴会费");
			}else {
				((TextView)view).setText("借出");
			}
			return true;
		}  else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyLend)object).getProject().getDisplayName());
			return true;
	    } else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrency().getSymbol());
			numericView.setNumber(((MoneyLend)object).getLocalAmount());
			numericView.setTextColor(Color.BLACK);
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setBackgroundResource(R.drawable.ic_action_picture);
			imageView.setImage(((MoneyLend)object).getPicture());
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			String ownerUserId = ((MoneyLend)object).getOwnerUserId();
			if(ownerUserId.equalsIgnoreCase(HyjApplication.getInstance().getCurrentUser().getId())){
				((TextView)view).setText("");
			}else{
				Friend friend = new Select().from(Friend.class).where("friendUserId=?",ownerUserId).executeSingle();
				((TextView)view).setText(friend.getDisplayName());
			}
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
			numericView.setNumber(((MoneyReturn)object).getLocalAmount());
			numericView.setTextColor(Color.BLACK);
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setBackgroundResource(R.drawable.ic_action_picture);
			imageView.setImage(((MoneyReturn)object).getPicture());
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			String ownerUserId = ((MoneyReturn)object).getOwnerUserId();
			if(ownerUserId.equalsIgnoreCase(HyjApplication.getInstance().getCurrentUser().getId())){
				((TextView)view).setText("");
			}else{
				Friend friend = new Select().from(Friend.class).where("friendUserId=?",ownerUserId).executeSingle();
				((TextView)view).setText(friend.getDisplayName());
			}
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
			numericView.setNumber(((MoneyPayback)object).getLocalAmount());
			numericView.setTextColor(Color.BLACK);
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setBackgroundResource(R.drawable.ic_action_picture);
			imageView.setImage(((MoneyPayback)object).getPicture());
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			String ownerUserId = ((MoneyPayback)object).getOwnerUserId();
			if(ownerUserId.equalsIgnoreCase(HyjApplication.getInstance().getCurrentUser().getId())){
				((TextView)view).setText("");
			}else{
				Friend friend = new Select().from(Friend.class).where("friendUserId=?",ownerUserId).executeSingle();
				((TextView)view).setText(friend.getDisplayName());
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyPayback)object).getDisplayRemark());
			return true;
		} else{
			return false;
		}
	}

//	@Override
//	public void onFetchMore() {
////		Bundle bundle = new Bundle();
////		bundle.putString("target", "findData");
////		bundle.putString("postData", (new JSONArray()).put(data).toString());
////		Loader loader = getLoaderManager().getLoader(-1);
////		((HomeGroupListLoader)loader).fetchMore(null);	
//	}

	@Override
	public void doFetchMore(int offset, int pageSize){
		Loader loader = getLoaderManager().getLoader(-1);
		((MoneyAccountDebtDetailsGroupListLoader)loader).fetchMore(null);	
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
			if(object instanceof MoneyBorrow){
				if(((MoneyBorrow)object).getMoneyIncomeApportionId() != null){
					bundle.putLong("MODEL_ID", ((MoneyBorrow) object).getMoneyIncomeApportion().getMoneyIncomeContainer().get_mId());
					openActivityWithFragment(MoneyIncomeContainerFormFragment.class, R.string.moneyIncomeFormFragment_title_edit, bundle);
				} else if(((MoneyBorrow)object).getMoneyExpenseApportionId() != null){
					bundle.putLong("MODEL_ID", ((MoneyBorrow) object).getMoneyExpenseApportion().getMoneyExpenseContainer().get_mId());
					openActivityWithFragment(MoneyExpenseContainerFormFragment.class, R.string.moneyExpenseFormFragment_title_edit, bundle);
				} else if(((MoneyBorrow)object).getMoneyDepositIncomeApportionId() != null){
					bundle.putLong("MODEL_ID", ((MoneyBorrow) object).getMoneyDepositIncomeApportion().getMoneyDepositIncomeContainer().get_mId());
					openActivityWithFragment(MoneyDepositIncomeContainerFormFragment.class, R.string.moneyDepositIncomeContainerFormFragment_title_edit, bundle);
				} else {
					bundle.putLong("MODEL_ID", object.get_mId());
					openActivityWithFragment(MoneyBorrowFormFragment.class, R.string.moneyBorrowFormFragment_title_edit, bundle);
				}
				return true;
			} else if(object instanceof MoneyLend){
				if(((MoneyLend)object).getMoneyExpenseApportionId() != null){
					bundle.putLong("MODEL_ID", ((MoneyLend) object).getMoneyExpenseApportion().getMoneyExpenseContainer().get_mId());
					openActivityWithFragment(MoneyExpenseContainerFormFragment.class, R.string.moneyExpenseFormFragment_title_edit, bundle);
				} else {
					bundle.putLong("MODEL_ID", object.get_mId());
					openActivityWithFragment(MoneyLendFormFragment.class, R.string.moneyLendFormFragment_title_edit, bundle);
				}
				return true;
			} else if(object instanceof MoneyReturn){
				bundle.putLong("MODEL_ID", object.get_mId());
				openActivityWithFragment(MoneyReturnFormFragment.class, R.string.moneyReturnFormFragment_title_edit, bundle);
				return true;
			} else if(object instanceof MoneyPayback){
				bundle.putLong("MODEL_ID", object.get_mId());
				openActivityWithFragment(MoneyPaybackFormFragment.class, R.string.moneyPaybackFormFragment_title_edit, bundle);
				return true;
			} 
		}
		return false;
    } 
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case GET_SEARCH_QUERY:
			if (resultCode == Activity.RESULT_OK) {
				mDateFrom = data.getLongExtra("dateFrom", 0);
				if(mDateFrom == 0){
					mDateFrom = null;
				}
				mDateTo= data.getLongExtra("dateTo", 0);
				if(mDateTo == 0){
					mDateTo = null;
				}
				mDisplayType = data.getStringExtra("displayType");
				
				String friendId = data.getStringExtra("friendId");
				if(friendId != null){
					mFriend = HyjModel.getModel(Friend.class, friendId);
				}
				String projectId = data.getStringExtra("projectId");
				if(projectId != null){
					mProject = HyjModel.getModel(Project.class, projectId);
				}
				String moneyAccountId = data.getStringExtra("moneyAccountId");
				if(moneyAccountId != null){
					mMoneyAccount = HyjModel.getModel(MoneyAccount.class, moneyAccountId);
				}

				initLoader(-1);
			}
			break;
		}
		
	}
	private static class SearchGroupListAdapter extends HyjSimpleExpandableListAdapter{

		public SearchGroupListAdapter(Context context,
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
		            		if(v.getId() == R.id.homeListItem_group_expenseTotal){
		            			balanceTotalView.setPrefix("流出"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol());
			            	} else if(v.getId() == R.id.homeListItem_group_incomeTotal){
		            			balanceTotalView.setPrefix("流入"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol());
				            }
		            		balanceTotalView.setNumber(Double.valueOf(data.get(from[i]).toString()));
		            	} else if(v instanceof TextView){
		            		((TextView)v).setText((String)data.get(from[i]));
		            	}
		            }
		        }
		    }
	}
}
