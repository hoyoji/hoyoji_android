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

public class HyjBooleanView extends TextView {
	public HyjBooleanView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.HyjTextField, 0, 0);

		try {
			mTrueText = a.getString(R.styleable.HyjTextField_trueText);
			mFalseText = a.getString(R.styleable.HyjTextField_falseText);
		} finally {
			a.recycle();
		}
	}

	private Boolean mBoolean;
	private String mTrueText;
	private String mFalseText;
	
	public void setBoolean(Boolean value){
		mBoolean = value;
		this.setText(getText());
	}
	
	public void setBoolean(int value){
		if(value == 0){
			mBoolean = false;
		} else {
			mBoolean = true;
		}
		this.setText(getText());
	}
	
	public String getText(){
		if(mBoolean){
			return mTrueText;
		} else {
			return mFalseText;
		}
	}
	
	public Boolean getBoolean(){
		return mBoolean;
	}
}
