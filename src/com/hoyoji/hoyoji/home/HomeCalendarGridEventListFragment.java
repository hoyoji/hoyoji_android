package com.hoyoji.hoyoji.home;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTask;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.view.HyjCalendarGrid;
import com.hoyoji.android.hyjframework.view.HyjCalendarGridAdapter;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.event.EventFormFragment;
import com.hoyoji.hoyoji.event.EventViewPagerFragment;
import com.hoyoji.hoyoji.models.Event;
import com.hoyoji.hoyoji.models.EventMember;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;

public class HomeCalendarGridEventListFragment extends HyjUserListFragment {
	private List<Map<String, Object>> mListGroupData = new ArrayList<Map<String, Object>>();
	private ContentObserver mChangeObserver = null;

	private TextView mCurrentMonth;
	private TextView mCurrentYear;
	private TextView mSelectedDay;
	private RelativeLayout mNearestEventLayout;
	private Event mNearestEvent;

	private HyjCalendarGrid mCalendarGridView;
	
	@Override
	public Integer useContentView() {
		return R.layout.home_listfragment_event_calendargrid;
	}

	@Override
	public Integer useToolbarView() {
		return super.useToolbarView();
	}

	@Override
	public Integer useOptionsMenuView() {
//		return R.menu.project_listfragment_event;
		return null;
	}

	@Override
	protected CharSequence getNoContentText() {
		return "无活动";
	}
	
	@Override
	protected View useHeaderView(Bundle savedInstanceState){
		LinearLayout view =  (LinearLayout) getLayoutInflater(savedInstanceState).inflate(R.layout.home_calendargrid_event_header, null);
		mCalendarGridView = (HyjCalendarGrid) view.findViewById(R.id.home_calendar_grid);
		mCalendarGridView.setAdapter(new HomeCalendarGridEventAdapter(getActivity(), getResources()));
		mCalendarGridView.getAdapter().setData(mListGroupData);
		mCurrentMonth = (TextView) view.findViewById(R.id.home_stat_month);
		mCurrentYear = (TextView) view.findViewById(R.id.home_stat_year);
		mSelectedDay = (TextView) view.findViewById(R.id.home_stat_day);

		mCurrentMonth.setText(mCalendarGridView.getAdapter().getCurrentMonth() + "月");
		mCurrentYear.setText(mCalendarGridView.getAdapter().getCurrentYear()+"年");
		mSelectedDay.setText(mCalendarGridView.getAdapter().getSelectedDay()+"日");
		
		mCalendarGridView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				mCalendarGridView.getAdapter().setSelectedYear(mCalendarGridView.getAdapter().getYearAtPosition(arg2));
				mCalendarGridView.getAdapter().setSelectedMonth(mCalendarGridView.getAdapter().getMonthAtPosition(arg2));
				mCalendarGridView.getAdapter().setSelectedDay(mCalendarGridView.getAdapter().getDayAtPosition(arg2));
				mCalendarGridView.getAdapter().notifyDataSetChanged();

				mSelectedDay.setText(mCalendarGridView.getAdapter().getSelectedDay()+"日");
				
				getLoaderManager().restartLoader(0, null, HomeCalendarGridEventListFragment.this);
				
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
				mCalendarGridView.getAdapter().notifyDataSetChanged();
				getLoaderManager().restartLoader(-1, null, HomeCalendarGridEventListFragment.this);
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
							
							mCalendarGridView.getAdapter().setSelectedDay(dayOfMonth);
							
							mCalendarGridView.getAdapter().setCalendar(year, monthOfYear+1);
							
							
							mCurrentMonth.setText(mCalendarGridView.getAdapter().getCurrentMonth() + "月");
							mCurrentYear.setText(mCalendarGridView.getAdapter().getCurrentYear()+"年");
							mSelectedDay.setText(mCalendarGridView.getAdapter().getSelectedDay()+"日");
							
							mListGroupData.clear();
							mCalendarGridView.getAdapter().notifyDataSetChanged();
							getLoaderManager().restartLoader(-1, null, HomeCalendarGridEventListFragment.this);
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
		
		mNearestEventLayout = (RelativeLayout) view.findViewById(R.id.home_listfragment_event_nearestevent);
		mNearestEventLayout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mNearestEvent != null){
					onListItemClick(getListView(), mNearestEventLayout, -1, mNearestEvent.get_mId());
				}
			}
		});
		
		view.findViewById(R.id.home_listfragment_event_addnew).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				openActivityWithFragment(EventFormFragment.class, R.string.projectEventListFragment_action_addnew, null);
			}
		});
		return view;
	}
	
	
	@Override
	public void onInitViewData() {
		super.onInitViewData();
		updateNearestEvent();
//		if (mChangeObserver == null) {
//			mChangeObserver = new ChangeObserver();
//			this.getActivity().getContentResolver().registerContentObserver(ContentProvider.createUri(EventMember.class, null), true,
//					mChangeObserver);
//		}
		// 加载日历
		initLoader(-1);
	}
	
	
	@Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.home_listitem_row,
				null,
				new String[] {"_id", "id", "startDate", "name", "id", "ownerUserId" ,"id", "id"},
				new int[] {R.id.homeListItem_picture, R.id.homeListItem_owner, R.id.homeListItem_date, R.id.homeListItem_title, R.id.homeListItem_remark, R.id.homeListItem_subTitle, R.id.homeListItem_owner, R.id.homeListItem_amount},
				0); 
	}	
	

	@Override
	public Loader<Object> onCreateLoader(int groupPos, Bundle arg1) {
		super.onCreateLoader(groupPos, arg1);
		Object loader;
		if(arg1 == null){
			arg1 = new Bundle();
		}
		int offset = arg1.getInt("OFFSET");
		int limit = arg1.getInt("LIMIT");
		if(limit == 0){
			limit = getListPageSize();
		}

		if (groupPos < 0) { 
			long dateFrom = mCalendarGridView.getAdapter().getDateFrom();
			long dateTo = mCalendarGridView.getAdapter().getDateTo();
			
			arg1.putLong("startDateInMillis", dateFrom);
			arg1.putLong("endDateInMillis", dateTo);
			
			loader = new HomeCalendarGridGroupEventListLoader(getActivity(), arg1);
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
//			arg1.putLong("dateFrom", calToday.getTimeInMillis());
//			arg1.putLong("dateTo", calToday.getTimeInMillis() + 24 * 3600000);
			long dateFrom = calToday.getTimeInMillis();
			long dateTo = dateFrom + 24 * 3600000;
			loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(Event.class, null),
				null,
				"startDate > ? AND startDate <= ?",
				new String[]{String.valueOf(dateFrom), String.valueOf(dateTo)}, 
				"startDate DESC LIMIT " + (limit + offset) 
			);
		}
		return (Loader<Object>)loader;
	}
	
	

	@Override
	public void onLoadFinished(Loader loader, Object list) {
		if (loader.getId() < 0) {
			
			ArrayList<Map<String, Object>> groupList = (ArrayList<Map<String, Object>>) list;
			mListGroupData.clear();
			mListGroupData.addAll(groupList);
			mCalendarGridView.getAdapter().notifyDataSetChanged();
			getLoaderManager().restartLoader(0, null, this);
		} else {
			updateNearestEvent();
	        super.onLoadFinished(loader, list);
//			((CursorAdapter)getListAdapter()).notifyDataSetChanged();
		}
	}

	@Override
	public void onLoaderReset(Loader<Object> loader) {
		 if(loader.getId() < 0){
				this.mListGroupData.clear();
		 } else {
				super.onLoaderReset(loader);
		 }
	}

	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		if(l.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE){
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
		} else {
			Bundle bundle = new Bundle();
			bundle.putLong("event_id", id);
			bundle.putLong("MODEL_ID", id);
			openActivityWithFragment(EventViewPagerFragment.class, R.string.projectEventMemberViewPagerFragment_title, bundle);
		}
    }
	
	HyjAsyncTask mNearestEventLoader = null;
	HyjAsyncTaskCallbacks mNearestEventLoaderCallback = new HyjAsyncTaskCallbacks(){
		@Override
		public void finishCallback(Object object) {
			if(object != null){
				Event event = (Event)object;
				mNearestEvent = event;
				ImageView imageView= (ImageView)mNearestEventLayout.findViewById(R.id.homeListItem_picture);
				imageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_yellow));
				imageView.setImageBitmap(HyjUtil.getCommonBitmap(R.drawable.event));
				
				HyjNumericView textView = (HyjNumericView)mNearestEventLayout.findViewById(R.id.homeListItem_amount);
				Project project = event.getProject();
				String projectId = event.getProjectId();
				ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", projectId, HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
				if(psa != null && psa.getProjectShareMoneyExpenseOwnerDataOnly() == true){
					textView.setTextColor(Color.BLACK);
					textView.setText("-");
				} else {
					Double depositBalance = event.getBalance();
					if(depositBalance == 0){
						textView.setTextColor(Color.BLACK);
						textView.setPrefix(project.getCurrencySymbol());
					} else if(depositBalance < 0){
						textView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
						textView.setPrefix("支出"+project.getCurrencySymbol());
					}else{
						textView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
						textView.setPrefix("收入"+project.getCurrencySymbol());
					}
					
					textView.setNumber(Math.abs(depositBalance));
				}
				
				((HyjDateTimeView)mNearestEventLayout.findViewById(R.id.homeListItem_date)).setTime(event.getStartDate());
				
				((TextView)mNearestEventLayout.findViewById(R.id.homeListItem_title)).setText(event.getName());
				
					
					long date = event.getDate();
					long startDate = event.getStartDate();
					long endDate = event.getEndDate(); 
					long dt = (new Date()).getTime();
					if(dt >= date && dt < startDate) {
						((TextView)mNearestEventLayout.findViewById(R.id.homeListItem_remark)).setText("[报名中]" + event.getAlreadySignUpCount() + "人");
					} else if(dt >= startDate && dt < endDate) {
						((TextView)mNearestEventLayout.findViewById(R.id.homeListItem_remark)).setText("[进行中]" + event.getAlreadySignUpCount() + "人");
					} else if(dt >= endDate) {
						((TextView)mNearestEventLayout.findViewById(R.id.homeListItem_remark)).setText("[已结束]" + event.getAlreadySignUpCount() + "人");
					}

					((TextView)mNearestEventLayout.findViewById(R.id.homeListItem_subTitle)).setText(Friend.getFriendUserDisplayName(event.getOwnerUserId()));


					EventMember em = new Select().from(EventMember.class).where("eventId=? AND friendUserId=?", event.getId(), HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
					if(em != null){
						if("UnSignUp".equals(em.getState())){
							((TextView)mNearestEventLayout.findViewById(R.id.homeListItem_owner)).setText("[未报名]");
						} else if("SignUp".equals(em.getState())){
							((TextView)mNearestEventLayout.findViewById(R.id.homeListItem_owner)).setText("[已报名]");
						} else if("SignIn".equals(em.getState())){
							((TextView)mNearestEventLayout.findViewById(R.id.homeListItem_owner)).setText("[已签到]");
						} 
					} else {
						((TextView)mNearestEventLayout.findViewById(R.id.homeListItem_owner)).setText("[未报名]");
					}
			}
			mNearestEventLoader = null;
		}

		@Override
		public Object doInBackground(String... string) {
			long currentTime = (new Date()).getTime();
			Event event;
			// 我报了名，而且进行中的活动
			event = new Select("ev.*").from(Event.class).as("ev").join(EventMember.class).as("em").on("ev.id = em.eventId AND em.state <> 'UnSignUp'").where("startDate < ? AND endDate > ?", currentTime, currentTime).orderBy("startDate DESC").limit(1).executeSingle();
			if(event == null){
				// 我报了名，下一个开始的活动
				event = new Select("ev.*").from(Event.class).as("ev").join(EventMember.class).as("em").on("ev.id = em.eventId AND em.state <> 'UnSignUp'").where("startDate > ?", currentTime).orderBy("startDate").limit(1).executeSingle();
				if(event == null){
					// 我没报名，下一个开始的活动
					event = new Select().from(Event.class).where("startDate > ?", currentTime).orderBy("startDate").limit(1).executeSingle();
					if(event == null){
						// 我报了名，刚结束的活动
						event = new Select("ev.*").from(Event.class).as("ev").join(EventMember.class).as("em").on("ev.id = em.eventId AND em.state <> 'UnSignUp'").where("endDate < ?", currentTime).orderBy("endDate DESC").limit(1).executeSingle();
						if(event == null){
							// 我没报名，刚开始的活动
							event = new Select().from(Event.class).where("startDate < ?", currentTime).orderBy("endDate DESC").limit(1).executeSingle();
						}
					}
				}
			}
			return event;
		}
	};
	public void updateNearestEvent(){
		if(mNearestEventLoader == null){
			mNearestEventLoader = HyjAsyncTask.newInstance(mNearestEventLoaderCallback);
		}
	}
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(view.getId() == R.id.homeListItem_picture){
			ImageView imageView= (ImageView)view;
//			Project project = HyjModel.getModel(Project.class, cursor.getString(cursor.getColumnIndex("id")));
//			if(project.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
//				imageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_yellow));
//				imageView.setImageBitmap(HyjUtil.getCommonBitmap(R.drawable.ic_action_event_white));
//			} else {
				imageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_yellow));
				imageView.setImageBitmap(HyjUtil.getCommonBitmap(R.drawable.event));
//			}
			
//			if(view.getTag() == null){
//				view.setOnClickListener(new OnClickListener(){
//					@Override
//					public void onClick(View v) {
//						Bundle bundle = new Bundle();
//						bundle.putLong("MODEL_ID", (Long) v.getTag());
//						openActivityWithFragment(ProjectFormFragment.class, R.string.projectFormFragment_title_edit, bundle);
//					}
//				});
//			}
//			view.setTag(cursor.getLong(columnIndex));
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			Event event = HyjModel.getModel(Event.class, cursor.getString(columnIndex));
			HyjNumericView textView = (HyjNumericView)view;
			Project project = event.getProject();
			String projectId = event.getProjectId();
			ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", projectId, HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
			if(psa != null && psa.getProjectShareMoneyExpenseOwnerDataOnly() == true){
				textView.setTextColor(Color.BLACK);
				textView.setText("-");
				return true;
			}
			Double depositBalance = event.getBalance();
			if(depositBalance == 0){
				textView.setTextColor(Color.BLACK);
				textView.setPrefix(project.getCurrencySymbol());
			} else if(depositBalance < 0){
				textView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
				textView.setPrefix("支出"+project.getCurrencySymbol());
			}else{
				textView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
				textView.setPrefix("收入"+project.getCurrencySymbol());
			}
			
			textView.setNumber(Math.abs(depositBalance));
			return true;
		} else if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setTime(cursor.getLong(columnIndex));
			return true;
		} else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText(cursor.getString(columnIndex));
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
//			if(cursor.getString(columnIndex) == null || "".equals(cursor.getString(columnIndex))){
//				((TextView)view).setText("无备注");
//			} else {
//				((TextView)view).setText(cursor.getString(columnIndex));
//			}
			
			long date = cursor.getLong(cursor.getColumnIndex("date"));
			long startDate = cursor.getLong(cursor.getColumnIndex("startDate"));
			long endDate = cursor.getLong(cursor.getColumnIndex("endDate")); 
			long dt = (new Date()).getTime();
			List<EventMember> ems = new Select().from(EventMember.class).where("eventId = ? AND state <> ?", cursor.getString(columnIndex), "UnSignUp").execute();
			if(dt >= date && dt < startDate) {
				((TextView)view).setText("[报名中]" + ems.size() + "人");
			} else if(dt >= startDate && dt < endDate) {
				((TextView)view).setText("[进行中]" + ems.size() + "人");
			} else if(dt >= endDate) {
				((TextView)view).setText("[已结束]" + ems.size() + "人");
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(Friend.getFriendUserDisplayName(cursor.getString(columnIndex)));
//			String date = cursor.getString(cursor.getColumnIndex("date"));
//			String startDate = cursor.getString(cursor.getColumnIndex("startDate"));
//			String endDate = cursor.getString(cursor.getColumnIndex("endDate")); 
//			String dt = HyjUtil.formatDateToIOS(new Date());
//			if(dt.compareTo(date)>=0 && dt.compareTo(startDate)<0) {
//				((TextView)view).setText("[报名中]");
//			} else if(dt.compareTo(startDate)>=0 && dt.compareTo(endDate)<0) {
//				((TextView)view).setText("[进行中]");
//			} else if(dt.compareTo(endDate)>=0) {
//				((TextView)view).setText("[已结束]");
//			}
			return true;
		} else if(view.getId() == R.id.homeListItem_owner){
			EventMember em = new Select().from(EventMember.class).where("eventId=? AND friendUserId=?", cursor.getString(columnIndex), HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
			if(em != null){
				if("UnSignUp".equals(em.getState())){
					((TextView)view).setText("[未报名]");
				} else if("SignUp".equals(em.getState())){
					((TextView)view).setText("[已报名]");
				} else if("SignIn".equals(em.getState())){
					((TextView)view).setText("[已签到]");
				} 
			} else {
				((TextView)view).setText("[未报名]");
			}
			return true;
		} else {
			return true;
		}
	}
	

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		if(item.getItemId() == R.id.projectEventListFragment_action_add){
//			Bundle bundle = new Bundle();
////			bundle.putLong("PROJECT_ID", modelId);
//			openActivityWithFragment(EventFormFragment.class, R.string.projectEventListFragment_action_addnew, bundle);
//			return true;
//		} 
//		return super.onOptionsItemSelected(item);
//	}
	
	private class ChangeObserver extends ContentObserver {
		public ChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			Handler handler = new Handler(Looper.getMainLooper());
			handler.postDelayed(new Runnable() {
				public void run() {
					((CursorAdapter) getListAdapter()).notifyDataSetChanged();
//					mCalendarGridView.getAdapter().notifyDataSetChanged();
				}
			}, 50);
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
