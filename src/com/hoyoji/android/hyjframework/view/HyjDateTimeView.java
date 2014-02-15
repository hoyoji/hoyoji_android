package com.hoyoji.android.hyjframework.view;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjUserActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.fragment.HyjDateTimePickerDialogFragment;
import com.hoyoji.android.hyjframework.fragment.HyjDialogFragment;
import com.hoyoji.hoyoji.LoginActivity;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.friend.FriendCategoryListFragment;
import com.hoyoji.hoyoji.friend.FriendFormFragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HyjDateTimeView extends TextView {
	public HyjDateTimeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	private Date mDate;
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	public void setText(String dateString){
		Date date;
		try {
			mDateFormat.setTimeZone(TimeZone.getDefault());
			date = mDateFormat.parse(dateString.replaceAll("Z$", "+0000"));
			setDate(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			super.setText(dateString);
			mDate = null;
		}
	}
	
	public void setDate(Date date){
		mDate = date;
		if(date == null){
			super.setText("");
		} else {
			DateFormat df = DateFormat.getDateTimeInstance();
			super.setText(df.format(date));
		}
	}
	
	public String getText(){
		if(mDate == null){
			return null;
		} 
		mDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return mDateFormat.format(mDate);
	}
	
}
