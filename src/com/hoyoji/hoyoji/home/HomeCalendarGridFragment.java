package com.hoyoji.hoyoji.home;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONObject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.Loader;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;

import com.activeandroid.Cache;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjImagePreviewFragment;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.view.HyjCalendarGrid;
import com.hoyoji.android.hyjframework.view.HyjCalendarGridAdapter;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.message.FriendMessageFormFragment;
import com.hoyoji.hoyoji.message.MoneyShareMessageFormFragment;
import com.hoyoji.hoyoji.message.ProjectMessageFormFragment;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyBorrow;
import com.hoyoji.hoyoji.models.MoneyDepositExpenseContainer;
import com.hoyoji.hoyoji.models.MoneyDepositIncomeContainer;
import com.hoyoji.hoyoji.models.MoneyDepositPaybackContainer;
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
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.MoneyBorrowFormFragment;
import com.hoyoji.hoyoji.money.MoneyDepositExpenseContainerFormFragment;
import com.hoyoji.hoyoji.money.MoneyDepositIncomeContainerFormFragment;
import com.hoyoji.hoyoji.money.MoneyDepositPaybackContainerFormFragment;
import com.hoyoji.hoyoji.money.MoneyDepositReturnContainerFormFragment;
import com.hoyoji.hoyoji.money.MoneyExpenseContainerFormFragment;
import com.hoyoji.hoyoji.money.MoneyExpenseFormFragment;
import com.hoyoji.hoyoji.money.MoneyIncomeContainerFormFragment;
import com.hoyoji.hoyoji.money.MoneyIncomeFormFragment;
import com.hoyoji.hoyoji.money.MoneyLendFormFragment;
import com.hoyoji.hoyoji.money.MoneyPaybackFormFragment;
import com.hoyoji.hoyoji.money.MoneyReturnFormFragment;
import com.hoyoji.hoyoji.money.MoneySearchListFragment;
import com.hoyoji.hoyoji.money.MoneyTemplateListFragment;
import com.hoyoji.hoyoji.money.MoneyTopupFormFragment;
import com.hoyoji.hoyoji.money.MoneyTransferFormFragment;
import com.hoyoji.hoyoji.money.currency.CurrencyExchangeViewPagerFragment;
import com.hoyoji.hoyoji.money.moneycategory.ExpenseIncomeCategoryViewPagerFragment;
import com.hoyoji.hoyoji.money.report.MoneyReportFragment;
import com.hoyoji.hoyoji.setting.SystemSettingFormFragment;

public class HomeCalendarGridFragment extends HyjUserListFragment {
	private List<Map<String, Object>> mListGroupData = new ArrayList<Map<String, Object>>();
//	private List<Map<String, Object>> mListChildTitleData = new ArrayList<Map<String, Object>>();
	private List<HyjModel> mListChildData = new ArrayList<HyjModel>();
	private ContentObserver mChangeObserver = null;
	private Button mExpenseButton;
	private Button mIncomeButton;
	private TextView mIncomeStat;
	private TextView mExpenseStat;
	private TextView mCurrentMonth;
	private TextView mCurrentYear;

//	private HyjNumericView mGroupHeaderIncome;
//	private HyjNumericView mGroupHeaderExpense;
	private TextView mGroupHeaderDate;
	
	private HyjCalendarGrid mCalendarGridView;
	
	DateFormat df = SimpleDateFormat.getDateInstance();
//	private int mImageBackgroundColorExpense = Color.parseColor("#FF4C32");
//	private int mImageBackgroundColorIncome = Color.parseColor("#FF4C32");
	
//	private int mImageBackgroundColor = R.color.hoyoji_red;
	
	private DateFormat mDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	@Override
	public Integer useContentView() {
		return R.layout.home_listfragment_home_calendargrid;
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
	protected View useHeaderView(Bundle savedInstanceState){
		LinearLayout view =  (LinearLayout) getLayoutInflater(savedInstanceState).inflate(R.layout.home_calendar_grid_header, null);
		mExpenseStat = (TextView) view.findViewById(R.id.home_stat_expenseStat);
		mIncomeStat = (TextView) view.findViewById(R.id.home_stat_incomeStat);
		mCalendarGridView = (HyjCalendarGrid) view.findViewById(R.id.home_calendar_grid);
		mCalendarGridView.getAdapter().setData(mListGroupData);
		mCurrentMonth = (TextView) view.findViewById(R.id.home_stat_month);
		mCurrentYear = (TextView) view.findViewById(R.id.home_stat_year);

		mCurrentMonth.setText(mCalendarGridView.getAdapter().getCurrentMonth() + "月");
		mCurrentYear.setText(mCalendarGridView.getAdapter().getCurrentYear()+"");

//		mGroupHeaderIncome = (HyjNumericView) view.findViewById(R.id.home_stat_group_incomeTotal);
//		mGroupHeaderExpense = (HyjNumericView) view.findViewById(R.id.home_stat_group_expenseTotal);
		mGroupHeaderDate = (TextView) view.findViewById(R.id.home_stat_group_date);
		updateGroupHeader();
//		Calendar calToday = Calendar.getInstance();
//		calToday.set(Calendar.HOUR_OF_DAY, 0);
//		calToday.clear(Calendar.MINUTE);
//		calToday.clear(Calendar.SECOND);
//		calToday.clear(Calendar.MILLISECOND);
//		mGroupHeaderDate.setTag(calToday.getTimeInMillis());
//		mGroupHeaderDate.setText(df.format(calToday.getTime()));
		
		mCalendarGridView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				mCalendarGridView.getAdapter().setSelectedYear(mCalendarGridView.getAdapter().getYearAtPosition(arg2));
				mCalendarGridView.getAdapter().setSelectedMonth(mCalendarGridView.getAdapter().getMonthAtPosition(arg2));
				mCalendarGridView.getAdapter().setSelectedDay(mCalendarGridView.getAdapter().getDayAtPosition(arg2));
				mCalendarGridView.getAdapter().notifyDataSetChanged();
				
				updateGroupHeader();
				getLoaderManager().restartLoader(0, null, HomeCalendarGridFragment.this);
				
			}
		});
		view.findViewById(R.id.home_stat_group_calendarMode).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mCalendarGridView.getAdapter().getCalendarMode() == HyjCalendarGridAdapter.CALENDAR_MODE_MONTH){
					mCalendarGridView.getAdapter().setCalendarMode(HyjCalendarGridAdapter.CALENDAR_MODE_WEEK);
				} else {
					mCalendarGridView.getAdapter().setCalendarMode(HyjCalendarGridAdapter.CALENDAR_MODE_MONTH);
				}
				mCalendarGridView.getAdapter().getDayNumber();

				mListGroupData.clear();
//				updateHeaderStat();
				updateGroupHeader();
				mCalendarGridView.getAdapter().notifyDataSetChanged();
				getLoaderManager().restartLoader(-1, null, HomeCalendarGridFragment.this);
			}
		});
		view.findViewById(R.id.home_stat_layout_income).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
//				mCalendarGridView.getAdapter().setSelectedYear(mCalendarGridView.getAdapter().getCurrentYear());
//				mCalendarGridView.getAdapter().setSelectedMonth(mCalendarGridView.getAdapter().getCurrentMonth());
				mCalendarGridView.getAdapter().setJumpCalendar(-1, 0);

				mCurrentMonth.setText(mCalendarGridView.getAdapter().getCurrentMonth() + "月");
				mCurrentYear.setText(mCalendarGridView.getAdapter().getCurrentYear()+"");
				
				mListGroupData.clear();
//				updateHeaderStat();
				updateGroupHeader();
				mCalendarGridView.getAdapter().notifyDataSetChanged();
				getLoaderManager().restartLoader(-1, null, HomeCalendarGridFragment.this);
			}
		});
		view.findViewById(R.id.home_stat_layout_expense).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
//				mCalendarGridView.getAdapter().setSelectedYear(mCalendarGridView.getAdapter().getCurrentYear());
//				mCalendarGridView.getAdapter().setSelectedMonth(mCalendarGridView.getAdapter().getCurrentMonth());
				mCalendarGridView.getAdapter().setJumpCalendar(1, 0);
				
				mCurrentMonth.setText(mCalendarGridView.getAdapter().getCurrentMonth() + "月");
				mCurrentYear.setText(mCalendarGridView.getAdapter().getCurrentYear()+"");
				
				mListGroupData.clear();
//				updateHeaderStat();
				updateGroupHeader();
				mCalendarGridView.getAdapter().notifyDataSetChanged();
				getLoaderManager().restartLoader(-1, null, HomeCalendarGridFragment.this);
			}
		});
		view.findViewById(R.id.home_stat_center).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {  
				    //下面的参数是用户设置完之后的时间  
				    @Override  
				    public void onDateSet(DatePicker view, int year, int monthOfYear,  
				            int dayOfMonth) {  
				    	if(monthOfYear + 1 != mCalendarGridView.getAdapter().getCurrentMonth() 
				    			|| mCalendarGridView.getAdapter().getCurrentYear() != year
				    			|| mCalendarGridView.getAdapter().getSelectedDay() != dayOfMonth){
							
//							mCalendarGridView.getAdapter().setSelectedYear(year);
//							mCalendarGridView.getAdapter().setSelectedMonth(monthOfYear+1);
							mCalendarGridView.getAdapter().setSelectedDay(dayOfMonth);
							
							mCalendarGridView.getAdapter().setCalendar(year, monthOfYear+1);
							
							
							mCurrentMonth.setText(mCalendarGridView.getAdapter().getCurrentMonth() + "月");
							mCurrentYear.setText(mCalendarGridView.getAdapter().getCurrentYear()+"");
							
							mListGroupData.clear();
//							updateHeaderStat();
							updateGroupHeader();
							mCalendarGridView.getAdapter().notifyDataSetChanged();
							getLoaderManager().restartLoader(-1, null, HomeCalendarGridFragment.this);
				    	}
				    }  
				};  
				Calendar calendar = Calendar.getInstance();
				DatePickerDialog dialog = new DatePickerDialog(getActivity(),  
	                    mDateSetListener,  
	                    calendar.get(Calendar.YEAR), 
	                    calendar.get(Calendar.MONTH),
	                    calendar.get(Calendar.DAY_OF_MONTH));
				dialog.show();
			}
		});
		return view;
	}
	
	@Override
	public void onInitViewData() {
		super.onInitViewData();
		mDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		mExpenseButton = (Button)getView().findViewById(R.id.homeListFragment_action_money_expense);
		mExpenseButton.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
		mExpenseButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				Calendar calToday = Calendar.getInstance();
				if(calToday.get(Calendar.YEAR) != mCalendarGridView.getAdapter().getSelectedYear() 
						|| calToday.get(Calendar.MONTH) != mCalendarGridView.getAdapter().getSelectedMonth() - 1 
						|| calToday.get(Calendar.DAY_OF_MONTH) != mCalendarGridView.getAdapter().getSelectedDay() ){
					calToday.set(Calendar.YEAR, mCalendarGridView.getAdapter().getSelectedYear());
					calToday.set(Calendar.MONTH, mCalendarGridView.getAdapter().getSelectedMonth()-1);
					calToday.set(Calendar.DAY_OF_MONTH, mCalendarGridView.getAdapter().getSelectedDay());
					
					bundle.putLong("DATE_IN_MILLISEC", calToday.getTimeInMillis());
				}
				openActivityWithFragment(MoneyExpenseContainerFormFragment.class, R.string.moneyExpenseFormFragment_title_addnew, bundle);
    		}
		});
		
		mIncomeButton = (Button)getView().findViewById(R.id.homeListFragment_action_money_income);
		mIncomeButton.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
		mIncomeButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Calendar calToday = Calendar.getInstance();
				Bundle bundle = new Bundle();
				if(calToday.get(Calendar.YEAR) != mCalendarGridView.getAdapter().getSelectedYear() 
						|| calToday.get(Calendar.MONTH) != mCalendarGridView.getAdapter().getSelectedMonth() - 1 
						|| calToday.get(Calendar.DAY_OF_MONTH) != mCalendarGridView.getAdapter().getSelectedDay() ){
					calToday.set(Calendar.YEAR, mCalendarGridView.getAdapter().getSelectedYear());
					calToday.set(Calendar.MONTH, mCalendarGridView.getAdapter().getSelectedMonth()-1);
					calToday.set(Calendar.DAY_OF_MONTH, mCalendarGridView.getAdapter().getSelectedDay());
					
					bundle.putLong("DATE_IN_MILLISEC", calToday.getTimeInMillis());
				}
				openActivityWithFragment(MoneyIncomeContainerFormFragment.class, R.string.moneyIncomeFormFragment_title_addnew, bundle);
    		}
		});
		
		mExpenseStat.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
		mIncomeStat.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
		
//		Calendar calToday = Calendar.getInstance();
//		calToday.set(Calendar.HOUR_OF_DAY, 0);
//		calToday.clear(Calendar.MINUTE);
//		calToday.clear(Calendar.SECOND);
//		calToday.clear(Calendar.MILLISECOND);
//		String ds = SimpleDateFormat.getDateInstance().format(calToday.getTime());
////		ds = ds.replaceAll("Z$", "+0000");
//		HashMap<String, Object> groupObject = new HashMap<String, Object>();
//		groupObject.put("date", ds);
//		groupObject.put("dateInMilliSeconds", calToday.getTimeInMillis());
//		groupObject.put("expenseTotal", 0.0);
//		groupObject.put("incomeTotal", 0.0);
//		mListChildTitleData.add(groupObject);
//		mListChildData.add(null);
		
//		getView().findViewById(R.id.homeListFragment_action_money_transfer).setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				openActivityWithFragment(MoneyTransferFormFragment.class, R.string.moneyTransferFormFragment_title_addnew, null);
//    		}
//		});
		
//		getView().findViewById(R.id.homeListFragment_action_money_debt).setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				PopupMenu popup = new PopupMenu(getActivity(), v);
//				MenuInflater inflater = popup.getMenuInflater();
//				inflater.inflate(R.menu.home_debt_actions, popup.getMenu());
//				popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
//					@Override
//					public boolean onMenuItemClick(MenuItem item) {
//						if (item.getItemId() == R.id.homeDebt_action_money_addnew_borrow) {
//							openActivityWithFragment(MoneyBorrowFormFragment.class, R.string.moneyBorrowFormFragment_title_addnew, null);
//						} 
//						else if (item.getItemId() == R.id.homeDebt_action_money_addnew_lend) {
//							openActivityWithFragment(MoneyLendFormFragment.class, R.string.moneyLendFormFragment_title_addnew, null);
//						} 
//						else if (item.getItemId() == R.id.homeDebt_action_money_addnew_return) {
//							openActivityWithFragment(MoneyReturnFormFragment.class, R.string.moneyReturnFormFragment_title_addnew, null);
//						} 
//						else if (item.getItemId() == R.id.homeDebt_action_money_addnew_payback) {
//							openActivityWithFragment(MoneyPaybackFormFragment.class, R.string.moneyPaybackFormFragment_title_addnew, null);
//						} 
//						return false;
//					}
//				});
//				popup.show();
//			}
//		});
		
		getView().findViewById(R.id.homeListFragment_action_money_template).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				openActivityWithFragment(MoneyTemplateListFragment.class, R.string.moneyTemplateListFragment_title, null);
    		}
		});
		
		getView().findViewById(R.id.homeListFragment_action_money_topup).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				PopupMenu popup = new PopupMenu(getActivity(), v);
				MenuInflater inflater = popup.getMenuInflater();
				inflater.inflate(R.menu.home_topup_actions, popup.getMenu());
				popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						Bundle bundle = new Bundle();
						Calendar calToday = Calendar.getInstance();
						if(calToday.get(Calendar.YEAR) != mCalendarGridView.getAdapter().getSelectedYear() 
								|| calToday.get(Calendar.MONTH) != mCalendarGridView.getAdapter().getSelectedMonth() - 1 
								|| calToday.get(Calendar.DAY_OF_MONTH) != mCalendarGridView.getAdapter().getSelectedDay() ){
							calToday.set(Calendar.YEAR, mCalendarGridView.getAdapter().getSelectedYear());
							calToday.set(Calendar.MONTH, mCalendarGridView.getAdapter().getSelectedMonth()-1);
							calToday.set(Calendar.DAY_OF_MONTH, mCalendarGridView.getAdapter().getSelectedDay());
	
							bundle.putLong("DATE_IN_MILLISEC", calToday.getTimeInMillis());
						}
						
						if (item.getItemId() == R.id.homeTopup_action_money_addnew_depositExpense) {
							openActivityWithFragment(MoneyDepositExpenseContainerFormFragment.class, R.string.moneyDepositExpenseFormFragment_title_addnew, bundle);
						} else if (item.getItemId() == R.id.homeTopup_action_money_addnew_depositIncome) {
							openActivityWithFragment(MoneyDepositIncomeContainerFormFragment.class, R.string.moneyDepositIncomeContainerFormFragment_title_addnew, bundle);
						} else if (item.getItemId() == R.id.homeTopup_action_money_addnew_depositReturn) {
							openActivityWithFragment(MoneyDepositReturnContainerFormFragment.class, R.string.moneyDepositReturnContainerFormFragment_title_addnew, bundle);
						} else if (item.getItemId() == R.id.homeTopup_action_money_addnew_depositPayback) {
							openActivityWithFragment(MoneyDepositPaybackContainerFormFragment.class, R.string.moneyDepositPaybackFormFragment_title_addnew, bundle);
						}
						return false;
					}
				});
				popup.show();
			}
		});
		
		getView().findViewById(R.id.homeListFragment_action_more).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(final View v) {
				PopupMenu popup = new PopupMenu(getActivity(), v);
				MenuInflater inflater = popup.getMenuInflater();
				inflater.inflate(R.menu.home_more_actions, popup.getMenu());
				popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {

						final Bundle bundle = new Bundle();
						Calendar calToday = Calendar.getInstance();
						if(calToday.get(Calendar.YEAR) != mCalendarGridView.getAdapter().getSelectedYear() 
								|| calToday.get(Calendar.MONTH) != mCalendarGridView.getAdapter().getSelectedMonth() - 1 
								|| calToday.get(Calendar.DAY_OF_MONTH) != mCalendarGridView.getAdapter().getSelectedDay() ){
							calToday.set(Calendar.YEAR, mCalendarGridView.getAdapter().getSelectedYear());
							calToday.set(Calendar.MONTH, mCalendarGridView.getAdapter().getSelectedMonth()-1);
							calToday.set(Calendar.DAY_OF_MONTH, mCalendarGridView.getAdapter().getSelectedDay());
							bundle.putLong("DATE_IN_MILLISEC", calToday.getTimeInMillis());
						}
						if (item.getItemId() == R.id.homeTopup_action_money_addnew_debt) {

							PopupMenu popup = new PopupMenu(getActivity(), v);
							MenuInflater inflater = popup.getMenuInflater();
							inflater.inflate(R.menu.home_debt_actions, popup.getMenu());
							popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
								@Override
								public boolean onMenuItemClick(MenuItem item) {

									if (item.getItemId() == R.id.homeDebt_action_money_addnew_borrow) {
										openActivityWithFragment(MoneyBorrowFormFragment.class, R.string.moneyBorrowFormFragment_title_addnew, bundle);
									} 
									else if (item.getItemId() == R.id.homeDebt_action_money_addnew_lend) {
										openActivityWithFragment(MoneyLendFormFragment.class, R.string.moneyLendFormFragment_title_addnew, bundle);
									} 
									else if (item.getItemId() == R.id.homeDebt_action_money_addnew_return) {
										openActivityWithFragment(MoneyReturnFormFragment.class, R.string.moneyReturnFormFragment_title_addnew, bundle);
									} 
									else if (item.getItemId() == R.id.homeDebt_action_money_addnew_payback) {
										openActivityWithFragment(MoneyPaybackFormFragment.class, R.string.moneyPaybackFormFragment_title_addnew, bundle);
									} 
									return false;
								}
							});
							popup.show();
						
						} else if (item.getItemId() == R.id.homeTopup_action_money_addnew_transfer) {
							openActivityWithFragment(MoneyTransferFormFragment.class, R.string.moneyTransferFormFragment_title_addnew, bundle);
						} else if (item.getItemId() == R.id.homeTopup_action_money_addnew_topup) {
							openActivityWithFragment(MoneyTopupFormFragment.class, R.string.moneyTopupFormFragment_title_addnew, bundle);
						} 
						return false;
					}
				});
				popup.show();
			}
		});
		
//		updateHeaderStat();
		
		if (mChangeObserver == null) {
			mChangeObserver = new ChangeObserver();
			this.getActivity().getContentResolver().registerContentObserver(ContentProvider.createUri(UserData.class, null), true,
					mChangeObserver);
			this.getActivity().getContentResolver().registerContentObserver(ContentProvider.createUri(Project.class, null), true,
					mChangeObserver);
			this.getActivity().getContentResolver().registerContentObserver(ContentProvider.createUri(Friend.class, null), true,
					mChangeObserver);
			this.getActivity().getContentResolver().registerContentObserver(ContentProvider.createUri(User.class, null), true,
					mChangeObserver);
		}
		
		
		// 加载日历
		initLoader(-1);
	}
	
	private void updateGroupHeader(){
		mGroupHeaderDate.setText(mCalendarGridView.getAdapter().getSelectedYear()+"年"+mCalendarGridView.getAdapter().getSelectedMonth()+"月"+mCalendarGridView.getAdapter().getSelectedDay()+"日");
		
//		Calendar calToday = Calendar.getInstance();
//		calToday.set(Calendar.YEAR, mCalendarGridView.getAdapter().getSelectedYear());
//		calToday.set(Calendar.MONTH, mCalendarGridView.getAdapter().getSelectedMonth()-1);
//		calToday.set(Calendar.DATE, mCalendarGridView.getAdapter().getSelectedDay());
//		calToday.set(Calendar.HOUR_OF_DAY, 0);
//		calToday.clear(Calendar.MINUTE);
//		calToday.clear(Calendar.SECOND);
//		calToday.clear(Calendar.MILLISECOND);
//		mGroupHeaderDate.setTag(calToday.getTimeInMillis());
//		mGroupHeaderDate.setText(df.format(calToday.getTime()));
		
//		if(mCalendarGridView.getAdapter().getCurrentMonth() == mCalendarGridView.getAdapter().getSelectedMonth() 
//				&& mCalendarGridView.getAdapter().getCurrentYear() == mCalendarGridView.getAdapter().getSelectedYear()){
//			for(int i = 0; i < groupList.size(); i++){
//				Map<String, Object> groupData = mCalendarGridView.getAdapter().getSelectedDayData();
//				if(groupData != null){
//					if(groupData.get("dateInMilliSeconds").toString().equals(mGroupHeaderDate.getTag().toString())){

//						mGroupHeaderDate.setText(groupData.get("date").toString());
//						mGroupHeaderExpense.setPrefix("支出"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol());
//		            	mGroupHeaderExpense.setNumber(Double.valueOf(groupData.get("expenseTotal").toString()));
//						
//		            	mGroupHeaderIncome.setPrefix("收入"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol());
//		            	mGroupHeaderIncome.setNumber(Double.valueOf(groupData.get("incomeTotal").toString()));
////					}
//				}
//			}
//		}
	}

	private void updateHeaderStat() {
		String currentUserId = HyjApplication.getInstance().getCurrentUser().getId();
		String localCurrencyId = HyjApplication.getInstance().getCurrentUser()
				.getUserData().getActiveCurrencyId();
		String localCurrencySymbol = HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol();

		Calendar calToday = Calendar.getInstance();
		calToday.set(Calendar.YEAR, mCalendarGridView.getAdapter().getCurrentYear());
		calToday.set(Calendar.MONTH, mCalendarGridView.getAdapter().getCurrentMonth()-1);
		calToday.set(Calendar.HOUR_OF_DAY, 0);
		calToday.clear(Calendar.MINUTE);
		calToday.clear(Calendar.SECOND);
		calToday.clear(Calendar.MILLISECOND);
		
		calToday.set(Calendar.DATE, 1);
		long dateFrom = calToday.getTimeInMillis();
		
		calToday.add(Calendar.MONTH, 1);// 加一个月，变为下月的1号  
		calToday.add(Calendar.DATE, -1);// 减去一天，变为当月最后一天  
		long dateTo = calToday.getTimeInMillis() + 3600000*24;
		
//		long dateFrom = mCalendarGridView.getAdapter().getDateFrom();
//		long dateTo = mCalendarGridView.getAdapter().getDateTo();
		String[] args = new String[] {String.valueOf(dateFrom), String.valueOf(dateTo)};
		DecimalFormat df=new DecimalFormat("#0.00"); 
		double expenseTotal = 0.0;
		double incomeTotal = 0.0;
		Cursor cursor = Cache
				.openDatabase()
				.rawQuery(
						"SELECT COUNT(*) AS count, SUM(main.amount * main.exchangeRate * CASE WHEN ex.localCurrencyId = '" + localCurrencyId + "' THEN 1/IFNULL(ex.rate,1) ELSE IFNULL(ex.rate, 1) END) AS total " 
								+ "FROM MoneyExpense main LEFT JOIN Exchange ex ON (ex.foreignCurrencyId = main.projectCurrencyId AND ex.localCurrencyId = '"
								+ localCurrencyId
								+ "' ) OR (ex.localCurrencyId = main.projectCurrencyId AND ex.foreignCurrencyId = '"
								+ localCurrencyId + "') "
								+ "WHERE date > ? AND date <= ? AND main.ownerUserId = '" + currentUserId + "'", args);
		if (cursor != null) {
			cursor.moveToFirst();
			expenseTotal += cursor.getDouble(1);
			cursor.close();
			cursor = null;
		}
		cursor = Cache
				.openDatabase()
				.rawQuery(
						"SELECT COUNT(*) AS count, SUM(main.amount * main.exchangeRate * CASE WHEN ex.localCurrencyId = '" + localCurrencyId + "' THEN 1/IFNULL(ex.rate,1) ELSE IFNULL(ex.rate, 1) END) AS total " 
								+ "FROM MoneyLend main LEFT JOIN Exchange ex ON (ex.foreignCurrencyId = main.projectCurrencyId AND ex.localCurrencyId = '"
								+ localCurrencyId
								+ "' ) OR (ex.localCurrencyId = main.projectCurrencyId AND ex.foreignCurrencyId = '"
								+ localCurrencyId + "') "
								+ "WHERE date > ? AND date <= ? AND main.ownerUserId = '" + currentUserId + "'", args);
		if (cursor != null) {
			cursor.moveToFirst();
			expenseTotal += cursor.getDouble(1);
			cursor.close();
			cursor = null;
		}
		cursor = Cache
				.openDatabase()
				.rawQuery(
						"SELECT COUNT(*) AS count, SUM(main.amount * main.exchangeRate * CASE WHEN ex.localCurrencyId = '" + localCurrencyId + "' THEN 1/IFNULL(ex.rate,1) ELSE IFNULL(ex.rate, 1) END) AS total " 
								+ "FROM MoneyReturn main LEFT JOIN Exchange ex ON (ex.foreignCurrencyId = main.projectCurrencyId AND ex.localCurrencyId = '"
								+ localCurrencyId
								+ "' ) OR (ex.localCurrencyId = main.projectCurrencyId AND ex.foreignCurrencyId = '"
								+ localCurrencyId + "') "
								+ "WHERE date > ? AND date <= ? AND main.ownerUserId = '" + currentUserId + "'", args);
		if (cursor != null) {
			cursor.moveToFirst();
			expenseTotal += cursor.getDouble(1);
			cursor.close();
			cursor = null;
		}
		cursor = Cache
				.openDatabase()
				.rawQuery(
						"SELECT COUNT(*) AS count, SUM(main.transferOutAmount * main.transferOutExchangeRate * CASE WHEN ex.localCurrencyId = '" + localCurrencyId + "' THEN 1/IFNULL(ex.rate,1) ELSE IFNULL(ex.rate, 1) END) AS total " 
								+ "FROM MoneyTransfer main LEFT JOIN Exchange ex ON (ex.foreignCurrencyId = main.projectCurrencyId AND ex.localCurrencyId = '"
								+ localCurrencyId
								+ "' ) OR (ex.localCurrencyId = main.projectCurrencyId AND ex.foreignCurrencyId = '"
								+ localCurrencyId + "') "
								+ "WHERE main.transferOutId IS NOT NULL AND date > ? AND date <= ? AND main.ownerUserId = '" + currentUserId + "'", args);
		if (cursor != null) {
			cursor.moveToFirst();
			expenseTotal += cursor.getDouble(1);
			cursor.close();
			cursor = null;
		}
		
		this.mExpenseStat.setText(localCurrencySymbol + df.format(expenseTotal));
		cursor = Cache
				.openDatabase()
				.rawQuery(
						"SELECT COUNT(*) AS count, SUM(main.amount * main.exchangeRate * CASE WHEN ex.localCurrencyId = '" + localCurrencyId + "' THEN 1/IFNULL(ex.rate,1) ELSE IFNULL(ex.rate, 1) END) AS total " 
								+ "FROM MoneyIncome main LEFT JOIN Exchange ex ON (ex.foreignCurrencyId = main.projectCurrencyId AND ex.localCurrencyId = '"
								+ localCurrencyId
								+ "' ) OR (ex.localCurrencyId = main.projectCurrencyId AND ex.foreignCurrencyId = '"
								+ localCurrencyId + "') "
								+ "WHERE date > ? AND date <= ?  AND main.ownerUserId = '" + currentUserId + "'", args);
		if (cursor != null) {
			cursor.moveToFirst();
			incomeTotal += cursor.getDouble(1);
			cursor.close();
			cursor = null;
		}
		cursor = Cache
				.openDatabase()
				.rawQuery(
						"SELECT COUNT(*) AS count, SUM(main.amount * main.exchangeRate * CASE WHEN ex.localCurrencyId = '" + localCurrencyId + "' THEN 1/IFNULL(ex.rate,1) ELSE IFNULL(ex.rate, 1) END) AS total " 
								+ "FROM MoneyBorrow main LEFT JOIN Exchange ex ON (ex.foreignCurrencyId = main.projectCurrencyId AND ex.localCurrencyId = '"
								+ localCurrencyId
								+ "' ) OR (ex.localCurrencyId = main.projectCurrencyId AND ex.foreignCurrencyId = '"
								+ localCurrencyId + "') "
								+ "WHERE date > ? AND date <= ?  AND main.ownerUserId = '" + currentUserId + "'", args);
		if (cursor != null) {
			cursor.moveToFirst();
			incomeTotal += cursor.getDouble(1);
			cursor.close();
			cursor = null;
		}
		
//		List<MoneyBorrow> list = new Select().from(MoneyBorrow.class).where("ownerUserId = '" + currentUserId + "'").execute();
		
		cursor = Cache
				.openDatabase()
				.rawQuery(
						"SELECT COUNT(*) AS count, SUM(main.amount * main.exchangeRate * CASE WHEN ex.localCurrencyId = '" + localCurrencyId + "' THEN 1/IFNULL(ex.rate,1) ELSE IFNULL(ex.rate, 1) END) AS total " 
								+ "FROM MoneyPayback main LEFT JOIN Exchange ex ON (ex.foreignCurrencyId = main.projectCurrencyId AND ex.localCurrencyId = '"
								+ localCurrencyId
								+ "' ) OR (ex.localCurrencyId = main.projectCurrencyId AND ex.foreignCurrencyId = '"
								+ localCurrencyId + "') "
								+ "WHERE date > ? AND date <= ?  AND main.ownerUserId = '" + currentUserId + "'", args);
		if (cursor != null) {
			cursor.moveToFirst();
			incomeTotal += cursor.getDouble(1);
			cursor.close();
			cursor = null;
		}
		cursor = Cache
				.openDatabase()
				.rawQuery(
						"SELECT COUNT(*) AS count, SUM(main.transferInAmount * main.transferInExchangeRate * CASE WHEN ex.localCurrencyId = '" + localCurrencyId + "' THEN 1/IFNULL(ex.rate,1) ELSE IFNULL(ex.rate, 1) END) AS total " 
								+ "FROM MoneyTransfer main LEFT JOIN Exchange ex ON (ex.foreignCurrencyId = main.projectCurrencyId AND ex.localCurrencyId = '"
								+ localCurrencyId
								+ "' ) OR (ex.localCurrencyId = main.projectCurrencyId AND ex.foreignCurrencyId = '"
								+ localCurrencyId + "') "
								+ "WHERE main.transferInId IS NOT NULL AND date > ? AND date <= ? AND main.ownerUserId = '" + currentUserId + "'", args);
		if (cursor != null) {
			cursor.moveToFirst();
			incomeTotal += cursor.getDouble(1);
			cursor.close();
			cursor = null;
		}
		this.mIncomeStat.setText(localCurrencySymbol + df.format(incomeTotal));
		
		
	}

	@Override
	public ListAdapter useListViewAdapter() {
		HomeListAdapter adapter = new HomeListAdapter(
				getActivity(), 
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
//		if (item.getItemId() == R.id.homeListFragment_action_display_transaction_type_project) {
//			
//			return true;
//		}
		// Handle your other action bar items...
		
		switch (item.getItemId()) {
		 case R.id.homeListFragment_action_transactions :
				 openActivityWithFragment(MoneySearchListFragment.class,
				 R.string.moneySearchListFragment_title, null);
				return true;
		 case R.id.homeListFragment_action_report :
				 openActivityWithFragment(MoneyReportFragment.class,
				 R.string.moneyReportFragment_title, null);
					return true;
//		case 2:
//			openActivityWithFragment(MessageListFragment.class,
//					R.string.friendListFragment_title_manage_message, null);
//			break;
		
		case R.id.homeListFragment_action_currency:
			openActivityWithFragment(CurrencyExchangeViewPagerFragment.class,
					R.string.currency_exchang_eviewpager_listFragment_title, null);
			return true;
		
//		case 3:
//			openActivityWithFragment(ExchangeListFragment.class,
//					R.string.exchangeListFragment_title_manage_exchange, null);
//			break;
//		case 4:
//			openActivityWithFragment(CurrencyListFragment.class,
//					R.string.currencyListFragment_title_manage_currency, null);
//			break;
//		case 4:
//			openActivityWithFragment(MoneyExpenseCategoryListFragment.class,
//					R.string.moneyCategoryFormDialogFragment_title_manage_expense, null);
//			break;
//		case 5:
//			openActivityWithFragment(MoneyIncomeCategoryListFragment.class,
//					R.string.moneyCategoryFormDialogFragment_title_manage_income, null);
//			break;
		case R.id.homeListFragment_action_category:
			openActivityWithFragment(ExpenseIncomeCategoryViewPagerFragment.class,
					R.string.expense_income_viewpager_listFragment_title, null);
			return true;

		case R.id.friendListFragment_action_friend_invite_linksmanage:
			openActivityWithFragment(InviteLinkListFragment.class, R.string.systemSettingFormFragment_invite_linksmanage, null);
			return true;
			
		case R.id.homeListFragment_action_setting:
			openActivityWithFragment(SystemSettingFormFragment.class,
					R.string.systemSettingFormFragment_title, null);
			return true;
//		case 5:
//			HyjApplication.getInstance().switchUser();
//			break;
		}
		
		
		
//		if (item.getItemId() == R.id.mainActivity_action_money_addnew_expense) {
//			openActivityWithFragment(MoneyExpenseContainerFormFragment.class,
//					R.string.moneyExpenseFormFragment_title_addnew, null);
//			return true;
//		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_income) {
//			openActivityWithFragment(MoneyIncomeContainerFormFragment.class,
//					R.string.moneyIncomeFormFragment_title_addnew, null);
//			return true;
//		} 
//		else if (item.getItemId() == R.id.mainActivity_action_money_addnew_transfer) {
//			openActivityWithFragment(MoneyTransferFormFragment.class,
//					R.string.moneyTransferFormFragment_title_addnew, null);
//			return true;
//		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_borrow) {
//			openActivityWithFragment(MoneyBorrowFormFragment.class,
//					R.string.moneyBorrowFormFragment_title_addnew, null);
//			return true;
//		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_lend) {
//			openActivityWithFragment(MoneyLendFormFragment.class,
//					R.string.moneyLendFormFragment_title_addnew, null);
//			return true;
//		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_return) {
//			openActivityWithFragment(MoneyReturnFormFragment.class,
//					R.string.moneyReturnFormFragment_title_addnew, null);
//			return true;
//		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_payback) {
//			openActivityWithFragment(MoneyPaybackFormFragment.class,
//					R.string.moneyPaybackFormFragment_title_addnew, null);
//			return true;
//		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_depositExpense) {
//			openActivityWithFragment(MoneyDepositExpenseContainerFormFragment.class,
//					R.string.moneyDepositExpenseFormFragment_title_addnew, null);
//			return true;
//		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_depositIncome) {
//			openActivityWithFragment(MoneyDepositIncomeContainerFormFragment.class,
//					R.string.moneyDepositIncomeContainerFormFragment_title_addnew, null);
//			return true;
//		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_depositReturn) {
//			openActivityWithFragment(MoneyDepositReturnContainerFormFragment.class,
//					R.string.moneyDepositReturnContainerFormFragment_title_addnew, null);
//			return true;
//		} else if (item.getItemId() == R.id.mainActivity_action_money_addnew_depositPayback) {
//			openActivityWithFragment(MoneyDepositPaybackFormFragment.class,
//					R.string.moneyDepositPaybackFormFragment_title_addnew, null);
//			return true;
//		}
		return super.onOptionsItemSelected(item);
	}
	
//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//		
//	}
	
	@Override
	public Loader<Object> onCreateLoader(int groupPos, Bundle arg1) {
		super.onCreateLoader(groupPos, arg1);
		Object loader;
		if(arg1 == null){
			arg1 = new Bundle();
		}

		if (groupPos < 0) { 
			long dateFrom = mCalendarGridView.getAdapter().getDateFrom();
			long dateTo = mCalendarGridView.getAdapter().getDateTo();
			
			arg1.putLong("startDateInMillis", dateFrom);
			arg1.putLong("endDateInMillis", dateTo);
			
			loader = new HomeCalendarGridGroupListLoader(getActivity(), arg1);
		} else {
			Calendar calToday = Calendar.getInstance();
			calToday.set(Calendar.HOUR_OF_DAY, 0);
			calToday.clear(Calendar.MINUTE);
			calToday.clear(Calendar.SECOND);
			calToday.clear(Calendar.MILLISECOND);
			int year = mCalendarGridView.getAdapter().getSelectedYear();
			int month = mCalendarGridView.getAdapter().getSelectedMonth()-1;
			int day = mCalendarGridView.getAdapter().getSelectedDay();
			calToday.set(Calendar.YEAR, year);
			calToday.set(Calendar.MONTH, month);
			calToday.set(Calendar.DATE, day);
			arg1.putLong("dateFrom", calToday.getTimeInMillis());
			arg1.putLong("dateTo", calToday.getTimeInMillis() + 24 * 3600000);
			

			loader = new HomeCalendarGridChildListLoader(getActivity(), arg1);
		}
		return (Loader<Object>) loader;
	}

	@Override
	public void onLoadFinished(Loader loader, Object list) {
		if (loader.getId() < 0) {
			
			ArrayList<Map<String, Object>> groupList = (ArrayList<Map<String, Object>>) list;
			mListGroupData.clear();
			mListGroupData.addAll(groupList);
			mCalendarGridView.getAdapter().notifyDataSetChanged();
//			adapter.notifyDataSetChanged();
//			updateGroupHeader();
			updateHeaderStat();
			getLoaderManager().restartLoader(0, null, this);
		} else {
			ArrayList<HyjModel> childList = (ArrayList<HyjModel>) list;
			mListChildData.clear();
			mListChildData.addAll(childList);

			((HomeListAdapter)getListAdapter()).notifyDataSetChanged();
	        setFooterLoadFinished(getListView(), childList.size());
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
		 if(loader.getId() < 0){
				this.mListGroupData.clear();
		 } else {
				this.mListChildData.clear();
		 }
	}

	
	@Override
	public boolean setViewValue(View view, Object object, String name) {
		if(object instanceof MoneyExpense){
			return setMoneyExpenseItemValue(view, object, name);
		} else if(object instanceof MoneyIncome){
			return setMoneyIncomeItemValue(view, object, name);
		} else if(object instanceof MoneyExpenseContainer){
			return setMoneyExpenseContainerItemValue(view, object, name);
		} else if(object instanceof MoneyDepositExpenseContainer){
			return setMoneyDepositExpenseContainerItemValue(view, object, name);
		} else if(object instanceof MoneyIncomeContainer){
			return setMoneyIncomeContainerItemValue(view, object, name);
		} else if(object instanceof MoneyDepositIncomeContainer){
			return setMoneyDepositIncomeItemValue(view, object, name);
		}  else if(object instanceof MoneyDepositReturnContainer){
			return setMoneyDepositReturnItemValue(view, object, name);
		} else if(object instanceof MoneyTransfer){
			return setMoneyTransferItemValue(view, object, name);
		} else if(object instanceof MoneyBorrow){
			return setMoneyBorrowItemValue(view, object, name);
		} else if(object instanceof MoneyLend){
			return setMoneyLendItemValue(view, object, name);
		} else if(object instanceof MoneyReturn){
			return setMoneyReturnItemValue(view, object, name);
		} else if(object instanceof MoneyDepositPaybackContainer){
			return setMoneyDepositPaybackContainerItemValue(view, object, name);
		} else if(object instanceof MoneyDepositPaybackContainer){
			return setMoneyDepositPaybackContainerItemValue(view, object, name);
		} else if(object instanceof MoneyPayback){
			return setMoneyPaybackItemValue(view, object, name);
		}else if(object instanceof Message){
			return setMessageItemValue(view, object, name);
		}
		return false;
	}
	private boolean setMessageItemValue(View view, Object object, String name){
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setTime(((Message)object).getDate());
			return true;
		} else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText(((Message)object).getMessageTitle());
			return true;
		} else if(view.getId() == R.id.homeListItem_subTitle){
			Message msg = (Message) object;
			((TextView)view).setText(msg.getFromUserDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix(null);
			numericView.setSuffix(null);
			numericView.setNumber(null);
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setBackgroundResource(R.drawable.ic_action_unread);
			imageView.setImage(((Message)object).getFromUserId());
			return true;
		}  else if(view.getId() == R.id.homeListItem_owner){
			Message message = (Message) object;
			if(message.getToUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
				((TextView)view).setText("");
			} else {
				((TextView)view).setText(message.getToUserDisplayName());
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			Message msg = (Message)object;
			try {
				JSONObject messageData = null;
				messageData = new JSONObject(msg.getMessageData());
				double amount = 0;
				try{
					amount = messageData.getDouble("amount") * messageData.getDouble("exchangeRate");
				} catch(Exception e) {
					amount = messageData.optDouble("amount");
				}
				java.util.Currency localeCurrency = java.util.Currency
						.getInstance(messageData.optString("currencyCode"));
				String currencySymbol = "";
				currencySymbol = localeCurrency.getSymbol();
				if(currencySymbol.length() == 0){
					currencySymbol = messageData.optString("currencyCode");
				}
				((TextView)view).setText(String.format(msg.getMessageDetail(), msg.getFromUserDisplayName(), currencySymbol, amount));
			} catch (Exception e){
				((TextView)view).setText(msg.getMessageDetail());
			}

			return true;
		} else {
			return false;
		}
	}
	private boolean setMoneyExpenseItemValue(View view, Object object, String name){
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setTime(((MoneyExpense)object).getDate());
			return true;
		} else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText(((MoneyExpense)object).getMoneyExpenseCategory());
			((TextView)view).setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			return true;
		} else if(view.getId() == R.id.homeListItem_subTitle){
			MoneyExpense moneyExpense = (MoneyExpense)object;
				Project project = moneyExpense.getProject();
			if(project == null){
				((TextView)view).setText("共享来的收支");
			} else {
				((TextView)view).setText(project.getDisplayName());
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			Project project = ((MoneyExpense)object).getProject();
			if(project != null){
				numericView.setPrefix(project.getCurrencySymbol());
			} else {
				numericView.setPrefix(((MoneyExpense)object).getProjectCurrencySymbol());
			}
			numericView.setSuffix(null);
			numericView.setNumber(((MoneyExpense)object).getProjectAmount());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			MoneyExpense moneyExpense = (MoneyExpense)object;
			HyjImageView imageView = (HyjImageView)view;
			imageView.setDefaultImage(R.drawable.ic_action_picture_white);
			imageView.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			imageView.setImage(moneyExpense .getPicture());
			
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
			view.setTag(moneyExpense .getPicture());
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
			((HyjDateTimeView)view).setTime(((MoneyIncome)object).getDate());
			return true;
		} else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText(((MoneyIncome)object).getMoneyIncomeCategory());
			((TextView)view).setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			return true;
		} else if(view.getId() == R.id.homeListItem_subTitle){
			MoneyIncome moneyIncome = (MoneyIncome)object;
			Project project = moneyIncome.getProject();
			if(project == null){
				((TextView)view).setText("共享来的收支");
			} else {
				((TextView)view).setText(project.getDisplayName());
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			
			Project project = ((MoneyIncome)object).getProject();
			if(project != null){
				numericView.setPrefix(project.getCurrencySymbol());
			} else {
				numericView.setPrefix(((MoneyIncome)object).getProjectCurrencySymbol());
			}
			numericView.setSuffix(null);
			numericView.setNumber(((MoneyIncome)object).getProjectAmount());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setDefaultImage(R.drawable.ic_action_picture_white);
			imageView.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
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
	private boolean setMoneyExpenseContainerItemValue(View view, Object object, String name){
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setTime(((MoneyExpenseContainer)object).getDate());
			return true;
		} else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText(((MoneyExpenseContainer)object).getMoneyExpenseCategory());
			((TextView)view).setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			return true;
		} else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyExpenseContainer)object).getProject().getDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			
			numericView.setPrefix(((MoneyExpenseContainer)object).getProject().getCurrencySymbol());
			numericView.setSuffix(null);
			numericView.setNumber(((MoneyExpenseContainer)object).getProjectAmount());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setDefaultImage(R.drawable.ic_action_picture_white);
			imageView.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			imageView.setImage(((MoneyExpenseContainer)object).getPicture());
			
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
			view.setTag(((MoneyExpenseContainer)object).getPicture());
			return true;
		}  else if(view.getId() == R.id.homeListItem_owner){
			if(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId().equalsIgnoreCase(((MoneyExpenseContainer)object).getProject().getCurrencyId())){
				((TextView)view).setText("");
			} else {
				((TextView)view).setText("折合:"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol() + String.format("%.2f", HyjUtil.toFixed2(((MoneyExpenseContainer)object).getLocalAmount())));
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyExpenseContainer)object).getDisplayRemark());
			return true;
		} else {
			return false;
		}
	}
	
	private boolean setMoneyIncomeContainerItemValue(View view, Object object, String name){
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setTime(((MoneyIncomeContainer)object).getDate());
			return true;
		} else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText(((MoneyIncomeContainer)object).getMoneyIncomeCategory());
			((TextView)view).setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			return true;
		} else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyIncomeContainer)object).getProject().getDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			
			numericView.setPrefix(((MoneyIncomeContainer)object).getProject().getCurrencySymbol());
			numericView.setNumber(((MoneyIncomeContainer)object).getProjectAmount());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setDefaultImage(R.drawable.ic_action_picture_white);
			imageView.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()) );
			imageView.setImage(((MoneyIncomeContainer)object).getPicture());

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
			view.setTag(((MoneyIncomeContainer)object).getPicture());
			return true;
		}  else if(view.getId() == R.id.homeListItem_owner){
			if(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId().equalsIgnoreCase(((MoneyIncomeContainer)object).getProject().getCurrencyId())){
				((TextView)view).setText("");
			} else {
				((TextView)view).setText("折合:"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol() + String.format("%.2f", HyjUtil.toFixed2(((MoneyIncomeContainer)object).getLocalAmount())));
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyIncomeContainer)object).getDisplayRemark());
			return true;
		} else {
			return false;
		}
	}
	private boolean setMoneyDepositIncomeItemValue(View view, Object object, String name){
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setTime(((MoneyDepositIncomeContainer)object).getDate());
			return true;
		} else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText("预收会费");
			((TextView)view).setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			return true;
		} else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyDepositIncomeContainer)object).getProject().getDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			
			numericView.setPrefix(((MoneyDepositIncomeContainer)object).getProject().getCurrencySymbol());
			numericView.setNumber(((MoneyDepositIncomeContainer)object).getProjectAmount());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setDefaultImage(R.drawable.ic_action_picture_white);
			imageView.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			imageView.setImage(((MoneyDepositIncomeContainer)object).getPicture());

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
			view.setTag(((MoneyDepositIncomeContainer)object).getPicture());
			return true;
		}  else if(view.getId() == R.id.homeListItem_owner){
			if(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId().equalsIgnoreCase(((MoneyDepositIncomeContainer)object).getProject().getCurrencyId())){
				((TextView)view).setText("");
			} else {
				((TextView)view).setText("折合:"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol() + String.format("%.2f", HyjUtil.toFixed2(((MoneyDepositIncomeContainer)object).getLocalAmount())));
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyDepositIncomeContainer)object).getDisplayRemark());
			return true;
		} else {
			return false;
		}
	}
	
	private boolean setMoneyDepositReturnItemValue(View view, Object object, String name){
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setTime(((MoneyDepositReturnContainer)object).getDate());
			return true;
		} else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText("会费还款");
			((TextView)view).setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			return true;
		} else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyDepositReturnContainer)object).getProject().getDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			
			numericView.setPrefix(((MoneyDepositReturnContainer)object).getProject().getCurrencySymbol());
			numericView.setNumber(((MoneyDepositReturnContainer)object).getProjectAmount());
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setDefaultImage(R.drawable.ic_action_picture_white);
			imageView.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			imageView.setImage(((MoneyDepositReturnContainer)object).getPicture());

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
			view.setTag(((MoneyDepositReturnContainer)object).getPicture());
			return true;
		}  else if(view.getId() == R.id.homeListItem_owner){
			if(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId().equalsIgnoreCase(((MoneyDepositReturnContainer)object).getProject().getCurrencyId())){
				((TextView)view).setText("");
			} else {
				((TextView)view).setText("折合:"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol() + String.format("%.2f", HyjUtil.toFixed2(((MoneyDepositReturnContainer)object).getLocalAmount())));
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyDepositReturnContainer)object).getDisplayRemark());
			return true;
		} else {
			return false;
		}
	}
	
	
	private boolean setMoneyTransferItemValue(View view, Object object, String name) {
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setTime(((MoneyTransfer)object).getDate());
			return true;
		}  else if(view.getId() == R.id.homeListItem_title){
			MoneyTransfer moneyTransfer = (MoneyTransfer) object;
			if(moneyTransfer.getTransferType().equalsIgnoreCase("Topup")){
				((TextView)view).setText("充值卡充值");
				((TextView)view).setTextColor(getResources().getColor(R.color.hoyoji_yellow));
			} else {
				if(moneyTransfer.getTransferIn() != null && moneyTransfer.getTransferOut() != null){
					((TextView)view).setText("从"+moneyTransfer.getTransferOut().getName()+"转到"+moneyTransfer.getTransferIn().getName());
					((TextView)view).setTextColor(getResources().getColor(R.color.hoyoji_yellow));
				} else if(moneyTransfer.getTransferOut() != null){
					((TextView)view).setText("从"+moneyTransfer.getTransferOut().getName()+"转出");
					((TextView)view).setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
				} else if(moneyTransfer.getTransferIn() != null){
					((TextView)view).setText("转入到"+moneyTransfer.getTransferIn().getName());
					((TextView)view).setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
				} else {
					((TextView)view).setText("转账");
					((TextView)view).setTextColor(getResources().getColor(R.color.hoyoji_yellow));
				}
			}
			return true;
		}  else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyTransfer)object).getProject().getDisplayName());
			return true;
	    } else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			MoneyTransfer moneyTransfer = (MoneyTransfer)object;
			numericView.setPrefix(moneyTransfer.getProject().getCurrencySymbol());
			numericView.setNumber(moneyTransfer.getTransferProjectAmount());
			if(moneyTransfer.getTransferIn() != null && moneyTransfer.getTransferOut() != null){
				numericView.setTextColor(getResources().getColor(R.color.hoyoji_yellow));
			} else if(moneyTransfer.getTransferOut() != null){
				numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			} else if(moneyTransfer.getTransferIn() != null){
				numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			} else {
				numericView.setTextColor(getResources().getColor(R.color.hoyoji_yellow));
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			MoneyTransfer moneyTransfer = (MoneyTransfer)object;
			imageView.setDefaultImage(R.drawable.ic_action_picture_white);
			imageView.setImage(moneyTransfer.getPicture());

			if(moneyTransfer.getTransferIn() != null && moneyTransfer.getTransferOut() != null){
				imageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_yellow));
			} else if(moneyTransfer.getTransferOut() != null){
				imageView.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			} else if(moneyTransfer.getTransferIn() != null){
				imageView.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			} else {
				imageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_yellow));
			}
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
			view.setTag(((MoneyTransfer)object).getPicture());
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			MoneyTransfer moneyTransfer = ((MoneyTransfer)object);
			if(moneyTransfer.getTransferType().equalsIgnoreCase("Topup")){
				((TextView)view).setText(moneyTransfer.getTransferInFriend().getDisplayName());
			} else {
				((TextView)view).setText("");
			}
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
			((HyjDateTimeView)view).setTime(((MoneyBorrow)object).getDate());
			return true;
		}  else if(view.getId() == R.id.homeListItem_title){
			if(((MoneyBorrow)object).getMoneyDepositIncomeApportionId() != null){
				((TextView)view).setText("预收会费");
			} else {
				((TextView)view).setText("向" + ((MoneyBorrow)object).getFriendDisplayName() + "借入");
			}
			((TextView)view).setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			return true;
		}  else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyBorrow)object).getProject().getDisplayName());
			return true;
	    } else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix(((MoneyBorrow)object).getProject().getCurrencySymbol());
			numericView.setNumber(((MoneyBorrow)object).getProjectAmount());
			numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setDefaultImage(R.drawable.ic_action_picture_white);
			imageView.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			imageView.setImage(((MoneyBorrow)object).getPicture());

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
			view.setTag(((MoneyBorrow)object).getPicture());
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			if(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId().equalsIgnoreCase(((MoneyBorrow)object).getProject().getCurrencyId())){
				((TextView)view).setText("");
			} else {
				((TextView)view).setText("折合:"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol() + String.format("%.2f", HyjUtil.toFixed2(((MoneyBorrow)object).getLocalAmount())));
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyBorrow)object).getDisplayRemark());
			return true;
		} else{
			return false;
		}
	}
	private boolean setMoneyDepositExpenseContainerItemValue(View view, Object object, String name) {
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setTime(((MoneyDepositExpenseContainer)object).getDate());
			return true;
		}  else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText("预缴会费");
			((TextView)view).setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			return true;
		}  else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyDepositExpenseContainer)object).getProject().getDisplayName());
			return true;
	    } else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix(((MoneyDepositExpenseContainer)object).getProject().getCurrencySymbol());
			numericView.setNumber(((MoneyDepositExpenseContainer)object).getProjectAmount());
			numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setDefaultImage(R.drawable.ic_action_picture_white);
			imageView.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			imageView.setImage(((MoneyDepositExpenseContainer)object).getPicture());

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
			view.setTag(((MoneyDepositExpenseContainer)object).getPicture());
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			if(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId().equalsIgnoreCase(((MoneyDepositExpenseContainer)object).getProject().getCurrencyId())){
				((TextView)view).setText("");
			} else {
				((TextView)view).setText("折合:"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol() + String.format("%.2f", HyjUtil.toFixed2(((MoneyDepositExpenseContainer)object).getLocalAmount())));
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyDepositExpenseContainer)object).getDisplayRemark());
			return true;
		} else{
			return false;
		}
	}
	private boolean setMoneyLendItemValue(View view, Object object, String name) {
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setTime(((MoneyLend)object).getDate());
			return true;
		}  else if(view.getId() == R.id.homeListItem_title){
			if(((MoneyLend)object).getMoneyDepositExpenseContainerId() != null){
				((TextView)view).setText("预缴会费");
			}else{
				((TextView)view).setText("借出给" + ((MoneyLend)object).getFriendDisplayName());
			}
			((TextView)view).setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			return true;
		}  else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyLend)object).getProject().getDisplayName());
			return true;
	    } else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix(((MoneyLend)object).getProject().getCurrencySymbol());
			numericView.setNumber(((MoneyLend)object).getProjectAmount());
			numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setDefaultImage(R.drawable.ic_action_picture_white);
			imageView.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			imageView.setImage(((MoneyLend)object).getPicture());

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
			view.setTag(((MoneyLend)object).getPicture());
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			if(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId().equalsIgnoreCase(((MoneyLend)object).getProject().getCurrencyId())){
				((TextView)view).setText("");
			} else {
				((TextView)view).setText("折合:"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol() + String.format("%.2f", HyjUtil.toFixed2(((MoneyLend)object).getLocalAmount())));
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
			((HyjDateTimeView)view).setTime(((MoneyReturn)object).getDate());
			return true;
		}  else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText("还款给" + ((MoneyReturn)object).getFriendDisplayName());
			((TextView)view).setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			return true;
		}  else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyReturn)object).getProject().getDisplayName());
			return true;
	    } else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix(((MoneyReturn)object).getProject().getCurrencySymbol());
			numericView.setNumber(((MoneyReturn)object).getProjectAmount());
			numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setDefaultImage(R.drawable.ic_action_picture_white);
			imageView.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			imageView.setImage(((MoneyReturn)object).getPicture());

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
			view.setTag(((MoneyReturn)object).getPicture());
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			if(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId().equalsIgnoreCase(((MoneyReturn)object).getProject().getCurrencyId())){
				((TextView)view).setText("");
			} else {
				((TextView)view).setText("折合:"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol() + String.format("%.2f", HyjUtil.toFixed2(((MoneyReturn)object).getLocalAmount())));
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyReturn)object).getDisplayRemark());
			return true;
		} else{
			return false;
		}
	}
	
	private boolean setMoneyDepositPaybackContainerItemValue(View view, Object object, String name) {
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setTime(((MoneyDepositPaybackContainer)object).getDate());
			return true;
		}  else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText("会费退回");
			((TextView)view).setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			return true;
		}  else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyDepositPaybackContainer)object).getProject().getDisplayName());
			return true;
	    } else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix(((MoneyDepositPaybackContainer)object).getProject().getCurrencySymbol());
			numericView.setNumber(((MoneyDepositPaybackContainer)object).getProjectAmount());
			numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setDefaultImage(R.drawable.ic_action_picture_white);
			imageView.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			imageView.setImage(((MoneyDepositPaybackContainer)object).getPicture());

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
			view.setTag(((MoneyDepositPaybackContainer)object).getPicture());
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			if(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId().equalsIgnoreCase(((MoneyDepositPaybackContainer)object).getProject().getCurrencyId())){
				((TextView)view).setText("");
			} else {
				((TextView)view).setText("折合:"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol() + String.format("%.2f", HyjUtil.toFixed2(((MoneyDepositPaybackContainer)object).getLocalAmount())));
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyDepositPaybackContainer)object).getDisplayRemark());
			return true;
		} else{
			return false;
		}
	}
	
	private boolean setMoneyPaybackItemValue(View view, Object object, String name) {
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setTime(((MoneyPayback)object).getDate());
			return true;
		}  else if(view.getId() == R.id.homeListItem_title){
			if(((MoneyPayback)object).getMoneyDepositPaybackContainerId() != null){
				((TextView)view).setText("会费退回");
			}else{
				((TextView)view).setText("向" + ((MoneyPayback)object).getFriendDisplayName()+"收款");
			}
			((TextView)view).setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			return true;
		}  else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(((MoneyPayback)object).getProject().getDisplayName());
			return true;
	    } else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix(((MoneyPayback)object).getProject().getCurrencySymbol());
			numericView.setNumber(((MoneyPayback)object).getProjectAmount());
			numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			imageView.setDefaultImage(R.drawable.ic_action_picture_white);
			imageView.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			imageView.setImage(((MoneyPayback)object).getPicture());

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
			view.setTag(((MoneyPayback)object).getPicture());
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			if(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId().equalsIgnoreCase(((MoneyPayback)object).getProject().getCurrencyId())){
				((TextView)view).setText("");
			} else {
				((TextView)view).setText("折合:"+HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol() + String.format("%.2f", HyjUtil.toFixed2(((MoneyPayback)object).getLocalAmount())));
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(((MoneyPayback)object).getDisplayRemark());
			return true;
		} else{
			return false;
		}
	}

	
	@Override  
	public void onListItemClick(ListView parent, View v,
			int position, long id) {
		if(parent.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE){
			return;
		}
		if(id == -1) {
			 return;
		}
		if(getActivity().getCallingActivity() != null){
			Intent intent = new Intent();
			intent.putExtra("MODEL_ID", id);
			getActivity().setResult(Activity.RESULT_OK, intent);
			getActivity().finish();
			return;
		} else {
			HyjModel object = (HyjModel) getListAdapter().getItem(position-1);
			Bundle bundle = new Bundle();
			bundle.putLong("MODEL_ID", id);
			if(object instanceof MoneyExpense){
					openActivityWithFragment(MoneyExpenseFormFragment.class, R.string.moneyExpenseFormFragment_title_edit, bundle);
				return ;
			} else if(object instanceof MoneyIncome){
					openActivityWithFragment(MoneyIncomeFormFragment.class, R.string.moneyIncomeFormFragment_title_edit, bundle);
				return ;
			} else if(object instanceof MoneyExpenseContainer){
					openActivityWithFragment(MoneyExpenseContainerFormFragment.class, R.string.moneyExpenseFormFragment_title_edit, bundle);
				return ;
			} else if(object instanceof MoneyIncomeContainer){
					openActivityWithFragment(MoneyIncomeContainerFormFragment.class, R.string.moneyIncomeFormFragment_title_edit, bundle);
				return ;
			} else if(object instanceof MoneyDepositIncomeContainer){
				openActivityWithFragment(MoneyDepositIncomeContainerFormFragment.class, R.string.moneyDepositIncomeContainerFormFragment_title_edit, bundle);
				return ;
			} else if(object instanceof MoneyDepositReturnContainer){
				openActivityWithFragment(MoneyDepositReturnContainerFormFragment.class, R.string.moneyDepositReturnContainerFormFragment_title_edit, bundle);
				return ;
			} else if(object instanceof MoneyTransfer){
				MoneyTransfer moneyTransfer = (MoneyTransfer) object;
				if(moneyTransfer.getTransferType().equalsIgnoreCase("Topup")){
					openActivityWithFragment(MoneyTopupFormFragment.class, R.string.moneyTopupFormFragment_title_edit, bundle);
				} else {
					openActivityWithFragment(MoneyTransferFormFragment.class, R.string.moneyTransferFormFragment_title_edit, bundle);
				}
				return ;
			} else if(object instanceof MoneyBorrow){
				MoneyBorrow moneyBorrow = (MoneyBorrow) object;
				if(moneyBorrow.getMoneyDepositIncomeApportionId() != null){
					bundle.putLong("MODEL_ID", moneyBorrow.getMoneyDepositIncomeApportion().getMoneyDepositIncomeContainer().get_mId());
					openActivityWithFragment(MoneyDepositIncomeContainerFormFragment.class, R.string.moneyDepositIncomeContainerFormFragment_title_edit, bundle);
				}else{
					openActivityWithFragment(MoneyBorrowFormFragment.class, R.string.moneyBorrowFormFragment_title_edit, bundle);
				}
				return ;
			} else if(object instanceof MoneyLend){
				MoneyLend moneyLend = (MoneyLend) object;
				if(moneyLend.getMoneyDepositExpenseContainerId() != null){
					MoneyDepositExpenseContainer moneyDepositExpenseContainer = HyjModel.getModel(MoneyDepositExpenseContainer.class, moneyLend.getMoneyDepositExpenseContainerId());
					bundle.putLong("MODEL_ID", moneyDepositExpenseContainer.get_mId());
					openActivityWithFragment(MoneyDepositExpenseContainerFormFragment.class, R.string.moneyDepositExpenseFormFragment_title_edit, bundle);
				} else {
					openActivityWithFragment(MoneyLendFormFragment.class, R.string.moneyLendFormFragment_title_edit, bundle);
				}
				return ;
			}  else if(object instanceof MoneyDepositExpenseContainer){
				openActivityWithFragment(MoneyDepositExpenseContainerFormFragment.class, R.string.moneyDepositExpenseFormFragment_title_edit, bundle);
				return ;
			} else if(object instanceof MoneyReturn){
				MoneyReturn moneyReturn = (MoneyReturn) object;
				if(moneyReturn.getMoneyDepositReturnApportionId() != null){
					bundle.putLong("MODEL_ID", moneyReturn.getMoneyDepositReturnApportion().getMoneyDepositReturnContainer().get_mId());
					openActivityWithFragment(MoneyDepositReturnContainerFormFragment.class, R.string.moneyDepositIncomeContainerFormFragment_title_edit, bundle);
				}else{
					openActivityWithFragment(MoneyReturnFormFragment.class, R.string.moneyReturnFormFragment_title_edit, bundle);
				}
				return ;
			} else if(object instanceof MoneyPayback){
				MoneyPayback moneyPayback = (MoneyPayback) object;
				if(moneyPayback.getMoneyDepositPaybackContainerId() != null){
					MoneyDepositPaybackContainer moneyDepositPaybackContainer = HyjModel.getModel(MoneyDepositPaybackContainer.class, moneyPayback.getMoneyDepositPaybackContainerId());
					bundle.putLong("MODEL_ID", moneyDepositPaybackContainer.get_mId());
					openActivityWithFragment(MoneyDepositPaybackContainerFormFragment.class, R.string.moneyDepositPaybackFormFragment_title_edit, bundle);
				} else {
					openActivityWithFragment(MoneyPaybackFormFragment.class, R.string.moneyPaybackFormFragment_title_edit, bundle);
				}
				return ;
			} else if(object instanceof MoneyDepositPaybackContainer){
				openActivityWithFragment(MoneyDepositPaybackContainerFormFragment.class, R.string.moneyDepositPaybackFormFragment_title_edit, bundle);
				return ;
			} else if(object instanceof Message){
				Message msg = (Message)object;
				if(msg.getType().equals("System.Friend.AddRequest") ){
					openActivityWithFragment(FriendMessageFormFragment.class, R.string.friendAddRequestMessageFormFragment_title_addrequest, bundle);
					return ;
				} else if(msg.getType().equals("System.Friend.AddResponse") ){
					openActivityWithFragment(FriendMessageFormFragment.class, R.string.friendAddRequestMessageFormFragment_title_addresponse, bundle);
					return ;
				} else if(msg.getType().equals("System.Friend.Delete") ){
					openActivityWithFragment(FriendMessageFormFragment.class, R.string.friendAddRequestMessageFormFragment_title_delete, bundle);
					return ;
				} else if(msg.getType().equals("Project.Share.AddRequest") ){
					openActivityWithFragment(ProjectMessageFormFragment.class, R.string.projectMessageFormFragment_title_addrequest, bundle);
					return ;
				} else if(msg.getType().equals("Project.Share.Accept") ){
					openActivityWithFragment(ProjectMessageFormFragment.class, R.string.projectMessageFormFragment_title_accept, bundle);
					return ;
				} else if(msg.getType().equals("Project.Share.Delete") ){
					openActivityWithFragment(ProjectMessageFormFragment.class, R.string.projectMessageFormFragment_title_delete, bundle);
					return ;
				} else if(msg.getType().startsWith("Money.Share.Add") ){
					openActivityWithFragment(MoneyShareMessageFormFragment.class, msg.getMessageTitle(), bundle, false, null);
					return ;
				}
			}
		}
		return ;
    } 
	private static class HomeListAdapter extends SimpleAdapter{
		private Context mContext;
		private int[] mViewIds;
	    private String[] mFields;
	    private int mLayoutResource;
//	    private ViewBinder mViewBinder;
	    
		public HomeListAdapter(Context context,
	                    List<? extends HyjModel> childData,
	                    int childLayout, String[] childFrom,
	                    int[] childTo) {
			super(context, (List<? extends Map<String, ?>>) childData, childLayout, childFrom, childTo);

			mContext = context;
	        mLayoutResource = childLayout;
	        mViewIds = childTo;
	        mFields = childFrom;
		}
	    
	    public long getItemId(int position) {
	        return ((HyjModel)getItem(position)).get_mId();
	    }
	    
		/**
	     * Populate new items in the list.
	     */
	    @Override public View getView(int position, View convertView, ViewGroup parent) {
	        View view = convertView;
	        View[] viewHolder;
	        if (view == null) {
	        	LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            view = vi.inflate(mLayoutResource, null);
	            viewHolder = new View[mViewIds.length];
	            for(int i=0; i<mViewIds.length; i++){
	            	View v = view.findViewById(mViewIds[i]);
	            	viewHolder[i] = v;
	            }
	            view.setTag(viewHolder);
	        } else {
	        	viewHolder = (View[])view.getTag();
	        }

	        Object item = getItem(position);
	        for(int i=0; i<mViewIds.length; i++){
	        	View v = viewHolder[i];
	        	getViewBinder().setViewValue(v, item, mFields[i]);
	        }
	        
	        return view;
	    }
	}
	private class ChangeObserver extends ContentObserver {
//		AsyncTask<String, Void, String> mTask = null;
		public ChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

//		@Override
//		public void onChange(boolean selfChange, Uri uri) {
//			super.onChange(selfChange, uri);
////			if(uri.toString().startsWith("content://com.hoyoji.hoyoji_android/userdata")){
////				expenseButton.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
////				incomeButton.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
////			}
//		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
//			if(mTask == null){
//				mTask = new AsyncTask<String, Void, String>() {
//			        @Override
//			        protected String doInBackground(String... params) {
//						try {
//							//等待其他的更新都到齐后再更新界面
//							Thread.sleep(0);
//						} catch (InterruptedException e) {}
//						return null;
//			        }
//			        @Override
//			        protected void onPostExecute(String result) {
//						((HyjSimpleExpandableListAdapter) getListView().getExpandableListAdapter()).notifyDataSetChanged();
//						mTask = null;
//			        }
//			    };
//			    mTask.execute();
//			}	
			int incomeColor = Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor());
			int expenseColor = Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor());
			
			mExpenseButton.setTextColor(expenseColor);
			mIncomeButton.setTextColor(incomeColor);

			mExpenseStat.setTextColor(expenseColor);
			mIncomeStat.setTextColor(incomeColor);
			
			Handler handler = new Handler(Looper.getMainLooper());
			handler.postDelayed(new Runnable() {
				public void run() {
					((HomeListAdapter) getListAdapter()).notifyDataSetChanged();
					mCalendarGridView.getAdapter().notifyDataSetChanged();
				}
			}, 50);
//			updateHeaderStat();
		}
	}
	@Override
	public void onDestroy() {
		if (mChangeObserver != null) {
			this.getActivity().getContentResolver()
					.unregisterContentObserver(mChangeObserver);
		}
	
		super.onDestroy();
	}
}
