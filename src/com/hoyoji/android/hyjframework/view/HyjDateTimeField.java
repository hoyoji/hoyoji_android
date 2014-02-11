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

public class HyjDateTimeField extends LinearLayout {
	private String mLabelText;
	private String mEditText;
	private String mHintText;
	
	private TextView mTextViewLabel;
	private EditText mEditTextEdit;

	private Date mDate;
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	public HyjDateTimeField(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.HyjTextField, 0, 0);

		try {
			mLabelText = a.getString(R.styleable.HyjTextField_labelText);
			mEditText = a.getString(R.styleable.HyjTextField_editText);
			mHintText = a.getString(R.styleable.HyjTextField_hintText);
		} finally {
			a.recycle();
		}
		
		final LayoutInflater inflater = (LayoutInflater)
			       context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.datetime_field, this);
		mTextViewLabel = (TextView)findViewById(R.id.text_field_label);
		mEditTextEdit = (EditText)findViewById(R.id.text_field_edit);
		
		mEditTextEdit.setHint(mHintText);
		if(mEditText != null){
			setText(mEditText);
		} else {
			setDate(new Date());
		}
		
		mEditTextEdit.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				HyjActivity activity = ((HyjActivity) getContext());
				if(activity.mDialogFragment != null){
					activity.mDialogFragment.dismiss();
				}
				
				activity.mDialogCallback = new HyjActivity.DialogCallbackListener() {
					@Override
					public void doPositiveClick(Object date) {
						setDate((Date)date);
					}
				};
				
				activity.mDialogFragment = HyjDateTimePickerDialogFragment.newInstance(getContext().getString(R.string.app_please_select) + mLabelText, mDate);
				activity.mDialogFragment.show(((FragmentActivity) getContext()).getSupportFragmentManager(), "dateTimePicker");
			}
		});
		
		mTextViewLabel.setText(mLabelText);
	}

	public void setError(String error){
		mEditTextEdit.setError(error);
	}
	
	public void setText(String dateString){
		Date date;
		try {
			mDateFormat.setTimeZone(TimeZone.getDefault());
			date = mDateFormat.parse(dateString.replaceAll("Z$", "+0000"));
			setDate(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mEditTextEdit.setText(dateString);
			mDate = null;
		}
	}
	
	public void setDate(Date date){
		mDate = date;
		if(date == null){
			mEditTextEdit.setText("");
		} else {
			DateFormat df = DateFormat.getDateTimeInstance();
			mEditTextEdit.setText(df.format(date));
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
