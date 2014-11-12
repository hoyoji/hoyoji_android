package com.hoyoji.android.hyjframework.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.activeandroid.util.Log;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.hoyoji_android.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class HyjCalendarGridAdapter extends BaseAdapter {
	private boolean isLeapyear = false;  //是否为闰年
	private int daysOfMonth = 0;      //某月的天数
	private int dayOfWeek = 0;        //具体某一天是星期几
	private int lastDaysOfMonth = 0;  //上一个月的总天数
	private Context context;
	private int[] dayNumber = new int[42];  //一个gridview中的日期存入此数组中
//	private static String week[] = {"周日","周一","周二","周三","周四","周五","周六"};

	private Resources res = null;
//	private Drawable drawable = null;
	
	private int currentYear = -1;
	private int currentMonth = -1;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
//	private int currentFlag = -1;     //用于标记当天
	
	
	private int selectedYear = -1;   //用于在头部显示的年份
	private int selectedMonth = -1;  //用于在头部显示的月份
	private int selectedDay = -1;
//	private String animalsYear = ""; 
	private String leapMonth = "";   //闰哪一个月
//	private String cyclical = "";   //天干地支
	//系统当前时间
	private String sysDate = "";  
	private int sys_year = -1;
	private int sys_month = -1;
	private int sys_day = -1;
	private SpecialCalendar sc;
	private List<Map<String, Object>> mListGroupData;
	private static Drawable drawableSelectedBackground;
	
	public HyjCalendarGridAdapter(Context context, Resources rs){
		this.context= context;
		this.sc = new SpecialCalendar();
		this.res = rs;
		drawableSelectedBackground = res.getDrawable(R.drawable.button_rectangle_round_5);
		Date date = new Date();
		sysDate = sdf.format(date);  //当期日期
		sys_year = Integer.parseInt(sysDate.split("-")[0]);
		sys_month = Integer.parseInt(sysDate.split("-")[1]);
		sys_day = Integer.parseInt(sysDate.split("-")[2]);
		
//		currentDay = Integer.parseInt(sys_day);
		setCalendar(sys_year, sys_month);
	}
	
	
	public HyjCalendarGridAdapter(Context context,Resources rs,int year, int month, int day){
		this(context, rs);
		
//		currentYear = year;  //得到跳转到的年份
//		currentMonth = month;  //得到跳转到的月份
//		currentDay = day;  //得到跳转到的天
		
		setCalendar(year, month);
		
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return dayNumber.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	private static class ViewCache {
		TextView tvExpense;
		TextView tvIncome;
		TextView tvDay;
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewCache viewCache;
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.calendar_grid_item, null);
			viewCache = new ViewCache();
			viewCache.tvDay = (TextView) convertView.findViewById(R.id.tvtext);
			viewCache.tvExpense = (TextView) convertView.findViewById(R.id.tvexpense);
			viewCache.tvIncome = (TextView) convertView.findViewById(R.id.tvincome);
			convertView.setTag(viewCache);
		 } else {
			 viewCache = (ViewCache) convertView.getTag();
		 }
		int d = dayNumber[position];

		if (position < daysOfMonth + dayOfWeek && position >= dayOfWeek) {
			// 当前月信息显示
			convertView.setVisibility(View.VISIBLE);
//			textView.setTextColor(Color.LTGRAY);// 当月字体设黑
			viewCache.tvDay.setText(d+"");
			if(mListGroupData != null && mListGroupData.size() > position - dayOfWeek){
				Map<String, Object> data = mListGroupData.get(position - dayOfWeek);

				Double expenseTotal = Double.valueOf(data.get("expenseTotal").toString());
				if(expenseTotal > 0.0){
					viewCache.tvExpense.setVisibility(View.VISIBLE);
					if(Double.compare(expenseTotal, expenseTotal.longValue()) == 0){
						viewCache.tvExpense.setText(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol() + expenseTotal.longValue());
					} else {
						viewCache.tvExpense.setText(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol() + expenseTotal);
					}
					viewCache.tvExpense.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
				} else {
					viewCache.tvExpense.setVisibility(View.INVISIBLE);
				}
				
				Double incomeTotal = Double.valueOf(data.get("incomeTotal").toString());
				if(incomeTotal > 0.0){
					viewCache.tvIncome.setVisibility(View.VISIBLE);
					if(Double.compare(incomeTotal, incomeTotal.longValue()) == 0){
						viewCache.tvIncome.setText(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol() + incomeTotal.longValue());
					} else {
						viewCache.tvIncome.setText(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol() + incomeTotal);
					}
					viewCache.tvIncome.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
				} else {
					viewCache.tvIncome.setVisibility(View.INVISIBLE);
				}
			} else {
				viewCache.tvExpense.setVisibility(View.INVISIBLE);
				viewCache.tvIncome.setVisibility(View.INVISIBLE);
			}
			
			// 显示当天
			if(this.sys_day == d && this.sys_month == this.currentMonth && this.sys_year == this.currentYear){
				viewCache.tvDay.setTextColor(Color.BLACK);
			} else {
				viewCache.tvDay.setTextColor(Color.GRAY);
			}
			
			// 显示选定的日期
			if(this.selectedDay == d && this.selectedMonth == this.currentMonth && this.selectedYear == this.currentYear){
				int sdk = android.os.Build.VERSION.SDK_INT;
				if(sdk  < Build.VERSION_CODES.JELLY_BEAN){
					convertView.setBackgroundDrawable(drawableSelectedBackground);
				} else {
					convertView.setBackground(drawableSelectedBackground);
				}
			} else {
				convertView.setBackgroundColor(Color.TRANSPARENT);
			}
		} else {
			convertView.setVisibility(View.INVISIBLE);
		}
		return convertView;
	}
	
	//得到某年的某月的天数且这月的第一天是星期几
	public void setCalendar(int year, int month){
		isLeapyear = sc.isLeapYear(year);              //是否为闰年
		daysOfMonth = sc.getDaysOfMonth(isLeapyear, month);  //某月的总天数
		dayOfWeek = sc.getWeekdayOfMonth(year, month);      //某月第一天为星期几
		lastDaysOfMonth = sc.getDaysOfMonth(isLeapyear, month-1);  //上一个月的总天数
		getDayNumber(year,month);
	}
	
	public void setJumpCalendar(int jumpMonth,int jumpYear){
		
		int stepYear = currentYear+jumpYear;
		int stepMonth = currentMonth+jumpMonth ;
		if(stepMonth > 0){
			//往下一个月滑动
			if(stepMonth%12 == 0){
				stepYear = currentYear + stepMonth/12 -1;
				stepMonth = 12;
			}else{
				stepYear = currentYear + stepMonth/12;
				stepMonth = stepMonth%12;
			}
		}else{
			//往上一个月滑动
			stepYear = currentYear - 1 + stepMonth/12;
			stepMonth = stepMonth%12 + 12;
			if(stepMonth%12 == 0){
				
			}
		}
	
//		currentYear = stepYear;  //得到当前的年份
//		currentMonth = stepMonth;  //得到本月 （jumpMonth为滑动的次数，每滑动一次就增加一月或减一月）
//		currentDay = day_c;  //得到当前日期是哪天
		
		setCalendar(stepYear, stepMonth);
		
	}

	public void setJumpCalendar(int jumpMonth,int jumpYear,int year_c,int month_c,int day_c){
		
		int stepYear = year_c+jumpYear;
		int stepMonth = month_c+jumpMonth ;
		if(stepMonth > 0){
			//往下一个月滑动
			if(stepMonth%12 == 0){
				stepYear = year_c + stepMonth/12 -1;
				stepMonth = 12;
			}else{
				stepYear = year_c + stepMonth/12;
				stepMonth = stepMonth%12;
			}
		}else{
			//往上一个月滑动
			stepYear = year_c - 1 + stepMonth/12;
			stepMonth = stepMonth%12 + 12;
			if(stepMonth%12 == 0){
				
			}
		}
	
//		currentYear = stepYear;  //得到当前的年份
//		currentMonth = stepMonth;  //得到本月 （jumpMonth为滑动的次数，每滑动一次就增加一月或减一月）
//		currentDay = day_c;  //得到当前日期是哪天
		
		setCalendar(stepYear, stepMonth);
		
	}
	
	//将一个月中的每一天的值添加入数组dayNumber中
	private void getDayNumber(int year, int month) {
		int j = 1;
		this.currentMonth = month;
		this.currentYear = year;
		
		//得到当前月的所有日程日期(这些日期需要标记)

		for (int i = 0; i < dayNumber.length; i++) {
			// 周一
//			if(i<7){
//				dayNumber[i]=week[i]+"."+" ";
//			}
			 if(i < dayOfWeek){  //前一个月
				int temp = lastDaysOfMonth - dayOfWeek+1;
				dayNumber[i] = temp + i;
			}else if(i < daysOfMonth + dayOfWeek){   //本月
				int day = i-dayOfWeek+1;   //得到的日期
				dayNumber[i] = day;
				//对于当前月才去标记当前日期
				if(sys_year == year && sys_month == month && sys_day == day){
					//标记当前日期
					if(selectedDay == -1){
						selectedYear = year;
						selectedMonth = month;
						selectedDay = day;
					}
				}	
			}else{   //下一个月
				dayNumber[i] = j;
				j++;
			}
		}
//        
//        String abc = "";
//        for(int i = 0; i < dayNumber.length; i++){
//        	 abc = abc+dayNumber[i]+":";
//        }
//        Log.d("DAYNUMBER",abc);


	}
	
	
	
	public int getSelectedDay(){
		return selectedDay;
	}

	/**
	 * 点击每一个item时返回item中的日期
	 * @param position
	 * @return
	 */
	public int getDayAtPosition(int position){
		return dayNumber[position];
	}
	
	/**
	 * 在点击gridView时，得到这个月中第一天的位置
	 * @return
	 */
	public int getStartPositon(){
		return dayOfWeek+7;
	}
	
	/**
	 * 在点击gridView时，得到这个月中最后一天的位置
	 * @return
	 */
	public int getEndPosition(){
		return  (dayOfWeek+daysOfMonth+7)-1;
	}
	
	public int getCurrentYear() {
		return currentYear;
	}

//	public void setSelectedYear(int showYear) {
//		this.selectedYear = showYear;
//	}

	public int getCurrentMonth() {
		return currentMonth;
	}

//	public void setSelectedMonth(int showMonth) {
//		this.selectedMonth = showMonth;
//	}
	
//	public String getAnimalsYear() {
//		return animalsYear;
//	}
//
//	public void setAnimalsYear(String animalsYear) {
//		this.animalsYear = animalsYear;
//	}
	
	public String getLeapMonth() {
		return leapMonth;
	}

	public void setLeapMonth(String leapMonth) {
		this.leapMonth = leapMonth;
	}
	
//	public String getCyclical() {
//		return cyclical;
//	}
//
//	public void setCyclical(String cyclical) {
//		this.cyclical = cyclical;
//	}

private static class SpecialCalendar {

	private int daysOfMonth = 0;      //某月的天数
	private int dayOfWeek = 0;        //具体某一天是星期几

	
	
	
	// 判断是否为闰年
	public boolean isLeapYear(int year) {
		if (year % 100 == 0 && year % 400 == 0) {
			return true;
		} else if (year % 100 != 0 && year % 4 == 0) {
			return true;
		}
		return false;
	}

	//得到某月有多少天数
	public int getDaysOfMonth(boolean isLeapyear, int month) {
		switch (month) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			daysOfMonth = 31;
			break;
		case 4:
		case 6:
		case 9:
		case 11:
			daysOfMonth = 30;
			break;
		case 2:
			if (isLeapyear) {
				daysOfMonth = 29;
			} else {
				daysOfMonth = 28;
			}

		}
		return daysOfMonth;
	}
	
	//指定某年中的某月的第一天是星期几
	public int getWeekdayOfMonth(int year, int month){
		Calendar cal = Calendar.getInstance();
		cal.set(year, month-1, 1);
		dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)-1;
		return dayOfWeek;
	}
	
	
}

public void setData(List<Map<String, Object>> listGroupData) {
	mListGroupData = listGroupData;
}


public void setSelectedDay(int d) {
	selectedDay = d;
}


public int getSelectedYear() {
	return selectedYear;
}
public int getSelectedMonth() {
	return selectedMonth;
}


public void setSelectedYear(int year) {
	selectedYear = year;
}


public void setSelectedMonth(int month) {
	selectedMonth = month;
}


public Map<String, Object> getSelectedDayData() {
	if(selectedDay == -1 || selectedMonth != currentMonth || selectedYear != currentYear){
		return null;
	}
	if(selectedDay <= mListGroupData.size()){
		return mListGroupData.get(selectedDay-1);
	}
	return null;
}
}
