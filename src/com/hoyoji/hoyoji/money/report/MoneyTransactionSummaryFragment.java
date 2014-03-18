package com.hoyoji.hoyoji.money.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjSimpleExpandableListAdapter;
import com.hoyoji.android.hyjframework.HyjSimpleExpandableListAdapter.OnFetchMoreListener;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.fragment.HyjUserExpandableListFragment;
import com.hoyoji.android.hyjframework.fragment.HyjUserFragment;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.friend.FriendFormFragment;
import com.hoyoji.hoyoji.message.FriendMessageFormFragment;
import com.hoyoji.hoyoji.message.ProjectMessageFormFragment;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
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
import com.hoyoji.hoyoji.money.MoneySearchFormFragment;
import com.hoyoji.hoyoji.money.MoneyTransferFormFragment;
import com.hoyoji.hoyoji.money.MoneyApportionField.ApportionItem;

public class MoneyTransactionSummaryFragment extends HyjUserFragment implements
LoaderManager.LoaderCallbacks<Object> {
	private static final int GET_SEARCH_QUERY = 0;
	
	private Project mProject;
	private MoneyAccount mMoneyAccount;
	private Friend mFriend;
	private Long mDateFrom;
	private Long mDateTo;
	private String mDisplayType;
	
	@Override
	public Integer useContentView() {
		return R.layout.money_fragment_transactionsummary;
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
		if(mMoneyAccount != null){
			queryParams.putString("moneyAccountId", mMoneyAccount.getId());
		}
		if(mFriend != null){
			if(mFriend.getFriendUserId() != null){
				queryParams.putString("friendUserId", mFriend.getFriendUserId());
			} else {
				queryParams.putString("localFriendId", mFriend.getId());
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
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.searchListFragment_action_search) {
			Bundle queryParams = buildQueryParams();
			openActivityWithFragmentForResult(MoneySearchFormFragment.class, R.string.searchDialogFragment_title, queryParams, GET_SEARCH_QUERY);
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Object> onCreateLoader(int groupPos, Bundle arg1) {
//		super.onCreateLoader(groupPos, arg1);
		Object loader  = new MoneyTransactionSummaryLoader(getActivity(), arg1);
		return (Loader<Object>) loader;
	}

	@Override
	public void onLoadFinished(Loader<Object> arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoaderReset(Loader<Object> arg0) {
		// TODO Auto-generated method stub
		
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

}
