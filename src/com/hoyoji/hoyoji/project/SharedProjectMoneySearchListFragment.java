package com.hoyoji.hoyoji.project;

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
import android.view.View.OnClickListener;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjSimpleExpandableListAdapter;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjImagePreviewFragment;
import com.hoyoji.android.hyjframework.fragment.HyjUserExpandableListFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.friend.FriendFormFragment;
import com.hoyoji.hoyoji.message.FriendMessageFormFragment;
import com.hoyoji.hoyoji.message.ProjectMessageFormFragment;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyBorrow;
import com.hoyoji.hoyoji.models.MoneyDepositIncomeContainer;
import com.hoyoji.hoyoji.models.MoneyDepositReturnContainer;
import com.hoyoji.hoyoji.models.MoneyExpense;
import com.hoyoji.hoyoji.models.MoneyExpenseContainer;
import com.hoyoji.hoyoji.models.MoneyIncome;
import com.hoyoji.hoyoji.models.MoneyIncomeContainer;
import com.hoyoji.hoyoji.models.MoneyLend;
import com.hoyoji.hoyoji.models.MoneyPayback;
import com.hoyoji.hoyoji.models.MoneyReturn;
import com.hoyoji.hoyoji.models.MoneyTransfer;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.money.MoneyBorrowFormFragment;
import com.hoyoji.hoyoji.money.MoneyDepositExpenseFormFragment;
import com.hoyoji.hoyoji.money.MoneyDepositIncomeContainerFormFragment;
import com.hoyoji.hoyoji.money.MoneyDepositPaybackFormFragment;
import com.hoyoji.hoyoji.money.MoneyDepositReturnContainerFormFragment;
import com.hoyoji.hoyoji.money.MoneyExpenseContainerFormFragment;
import com.hoyoji.hoyoji.money.MoneyIncomeContainerFormFragment;
import com.hoyoji.hoyoji.money.MoneyLendFormFragment;
import com.hoyoji.hoyoji.money.MoneyPaybackFormFragment;
import com.hoyoji.hoyoji.money.MoneyReturnFormFragment;
import com.hoyoji.hoyoji.money.MoneySearchFormFragment;
import com.hoyoji.hoyoji.money.MoneyTopupFormFragment;
import com.hoyoji.hoyoji.money.MoneyTransferFormFragment;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountFormFragment;

public class SharedProjectMoneySearchListFragment extends HyjUserExpandableListFragment {
	private static final int GET_SEARCH_QUERY = 0;
	private List<Map<String, Object>> mListGroupData = new ArrayList<Map<String, Object>>();
	private ArrayList<List<HyjModel>> mListChildData = new ArrayList<List<HyjModel>>();

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
//		return R.menu.money_listfragment_search;
		return null;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		Intent intent = getActivity().getIntent();
		String subTitle = null;
		
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
//		if(!queryParams.isEmpty()){
			Loader<Object> loader = getLoaderManager().getLoader(loaderId);
			if(loader != null && !loader.isReset()){
				getLoaderManager().restartLoader(loaderId, queryParams, this);
			} else {
				getLoaderManager().initLoader(loaderId, queryParams, this);
			}
//		}
	}
	
	private Bundle buildQueryParams() {
		Bundle queryParams = new Bundle();
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
		if (item.getItemId() == R.id.searchListFragment_action_search) {
			openActivityWithFragmentForResult(MoneySearchFormFragment.class, R.string.searchDialogFragment_title, queryParams, GET_SEARCH_QUERY);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Object> onCreateLoader(int groupPos, Bundle arg1) {
//		super.onCreateLoader(groupPos, arg1);
		Object loader;
		if (groupPos < 0) { // 这个是分类
			loader = new SharedProjectMoneySearchGroupListLoader(getActivity(), arg1);
		} else {
			loader = new SharedProjectMoneySearchChildListLoader(getActivity(), arg1);
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
			this.setFooterLoadFinished(((SharedProjectMoneySearchGroupListLoader)loader).hasMoreData());
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
			Project project = ((MoneyExpense)object).getProject();
			if(project == null){
				((TextView)view).setText("共享来的收支");
			} else {
				((TextView)view).setText(project.getDisplayName());
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			if(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor() != null){
				numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			}
			Project project = ((MoneyExpense)object).getProject();
			if(project != null){
				numericView.setPrefix(project.getCurrencySymbol());
			} else {
				numericView.setPrefix(((MoneyExpense)object).getProjectCurrencySymbol());
			}
			numericView.setSuffix(null);
			numericView.setNumber(((MoneyExpense)object).getProjectAmount());
			numericView.setTextColor(Color.BLACK);
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
//			imageView.setBackgroundResource(R.drawable.ic_action_picture);
			imageView.setImage(((MoneyExpense)object).getPicture());
			
			if(view.getTag() == null){
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						Picture pic = (Picture)v.getTag();
						if(pic == null){
							return;
						}
						Bundle bundle = new Bundle();
						bundle.putString("pictureName", pic.getId());
						bundle.putString("pictureType", pic.getPictureType());
						openActivityWithFragment(HyjImagePreviewFragment.class, R.string.app_preview_picture, bundle);
					}
				});
			}
			view.setTag(((MoneyExpense)object).getPicture());
			return true;
		}  else if(view.getId() == R.id.homeListItem_owner){
			if(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId().equalsIgnoreCase(((MoneyExpense)object).getProjectCurrencyId())){
				((TextView)view).setText("");
			} else {
				Double localAmount = ((MoneyExpense)object).getLocalAmount();
				if(localAmount == null){
					((TextView)view).setText("折合:［无汇率］");
				} else {
					((TextView)view).setText("折合:"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol() + String.format("%.2f", HyjUtil.toFixed2(localAmount)));
				}
			}
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
			Project project = ((MoneyIncome)object).getProject();
			if(project == null){
				((TextView)view).setText("共享来的收支");
			} else {
				((TextView)view).setText(project.getDisplayName());
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			
			if(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor() != null){
				numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			}

			Project project = ((MoneyIncome)object).getProject();
			if(project != null){
				numericView.setPrefix(project.getCurrencySymbol());
			} else {
				numericView.setPrefix(((MoneyIncome)object).getProjectCurrencySymbol());
			}
			numericView.setSuffix(null);
			numericView.setNumber(((MoneyIncome)object).getProjectAmount());
			numericView.setTextColor(Color.BLACK);
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setBackgroundResource(R.drawable.ic_action_picture);
			imageView.setImage(((MoneyIncome)object).getPicture());

			if(view.getTag() == null){
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						Picture pic = (Picture)v.getTag();
						if(pic == null){
							return;
						}
						Bundle bundle = new Bundle();
						bundle.putString("pictureName", pic.getId());
						bundle.putString("pictureType", pic.getPictureType());
						openActivityWithFragment(HyjImagePreviewFragment.class, R.string.app_preview_picture, bundle);
					}
				});
			}
			view.setTag(((MoneyIncome)object).getPicture());
			return true;
		}  else if(view.getId() == R.id.homeListItem_owner){
			if(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId().equalsIgnoreCase(((MoneyIncome)object).getProjectCurrencyId())){
				((TextView)view).setText("");
			} else {
				Double localAmount = ((MoneyIncome)object).getLocalAmount();
				if(localAmount == null){
					((TextView)view).setText("折合:［无汇率］");
				} else {
					((TextView)view).setText("折合:"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol() + String.format("%.2f", HyjUtil.toFixed2(localAmount)));
				}
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyIncome)object).getDisplayRemark());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void doFetchMore(int offset, int pageSize){
		setFooterLoadStart();
		Loader loader = getLoaderManager().getLoader(-1);
		((SharedProjectMoneySearchGroupListLoader)loader).fetchMore(null);	
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
//				openActivityWithFragment(MoneyExpenseFormFragment.class, R.string.moneyExpenseFormFragment_title_edit, bundle);
				return true;
			} else if(object instanceof MoneyIncome){
//				openActivityWithFragment(MoneyIncomeFormFragment.class, R.string.moneyIncomeFormFragment_title_edit, bundle);
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
		            			balanceTotalView.setPrefix("支出"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol());
			            	} else if(v.getId() == R.id.homeListItem_group_incomeTotal){
		            			balanceTotalView.setPrefix("收入"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol());
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