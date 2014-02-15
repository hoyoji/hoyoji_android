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

public class HyjNumericView extends TextView {
	public HyjNumericView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	private Double mNumber;
	
	public void setText(String number){
		mNumber = Double.parseDouble(number);
		super.setText(number);
	}
	
	public void setNumber(Double number){
		mNumber = number;
		super.setText(String.valueOf(number));
	}
	
	public String getText(){
		return String.valueOf(mNumber);
	}
	
}
