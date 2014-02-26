package com.hoyoji.android.hyjframework.view;

import com.hoyoji.hoyoji.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class HyjNumericField extends LinearLayout {
	private String mLabelText;
	private String mEditText;
	private String mHintText;
	
	private TextView mTextViewLabel;
	private EditText mEditTextEdit;

	public HyjNumericField(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.HyjTextField, 0, 0);

		String style;
		String border;
		int color = -1;
		boolean bold;
		try {
			mLabelText = a.getString(R.styleable.HyjTextField_labelText);
			mEditText = a.getString(R.styleable.HyjTextField_editText);
			mHintText = a.getString(R.styleable.HyjTextField_hintText);
			style  = a.getString(R.styleable.HyjTextField_style);
			if(style == null){
				style = "";
			}
			color  = a.getColor(R.styleable.HyjTextField_editTextColor, -1);
			bold  = a.getBoolean(R.styleable.HyjTextField_editTextBold, false);
//			border  = a.getString(R.styleable.HyjTextField_editTextBorder);
//			if(border == null){
//				border = "";
//			}
		} finally {
			a.recycle();
		}
		
		final LayoutInflater inflater = (LayoutInflater)
			       context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.numeric_field, this);
		mTextViewLabel = (TextView)findViewById(R.id.text_field_label);
		mEditTextEdit = (EditText)findViewById(R.id.text_field_edit);
		mEditTextEdit = (EditText)findViewById(R.id.text_field_edit);
		if(color != -1){
			mEditTextEdit.setTextColor(color);
			mEditTextEdit.setHintTextColor(color);
		}
		if(bold){
			mEditTextEdit.setTypeface(null, Typeface.BOLD);
		}
//		if(border.equals("none")){
//			if(android.os.Build.VERSION.SDK_INT >= 16){
//				mEditTextEdit.setBackground(null);
//			} else {
//				mEditTextEdit.setBackgroundDrawable(null);
//			}
//			border  = a.getString(R.styleable.HyjTextField_editTextBorder);
//			if(border == null){
//				border = "";
//			}
//		}		
		if(style.equals("no_label")){
			mTextViewLabel.setVisibility(GONE);
			mEditTextEdit.setGravity(Gravity.CENTER_HORIZONTAL);
		} else if(style.equals("top_label")){
			this.setOrientation(LinearLayout.VERTICAL);
			this.setGravity(Gravity.CENTER_HORIZONTAL);
			LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			mTextViewLabel.setLayoutParams(layoutParams);
			mEditTextEdit.setLayoutParams(layoutParams);
			
			mTextViewLabel.setTextSize(10);
			mTextViewLabel.setTextColor(Color.GRAY);
			mEditTextEdit.setGravity(Gravity.CENTER_HORIZONTAL);
		}
		mEditTextEdit.setHint(mHintText);
		mEditTextEdit.setText(mEditText);
		mTextViewLabel.setText(mLabelText);
		
		mEditTextEdit.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					mEditTextEdit.setSelection(mEditTextEdit.getText().toString().length());
				}
			}
		});
	}

	public void setError(String error){
		mEditTextEdit.setError(error);
	}
	
	public void addTextChangedListener(TextWatcher watcher){
		mEditTextEdit.addTextChangedListener(watcher);
	}
	
	public void setText(String text){
		mEditTextEdit.setText(text);
		if(text != null){
			mEditTextEdit.setSelection(text.length());
		}
	}
	
	public Editable getText(){
		return mEditTextEdit.getText();
	}
	
	public void setNumber(Double number){
		if(number == null){
			setText("");
		} else {
			setText(number.toString());
		}
	}
	
	public Double getNumber(){
		try{
			return Double.valueOf(getText().toString());
		} catch (NumberFormatException e){
			return null;
		}
	}
	
	public void setEnabled(boolean enabled){
		mEditTextEdit.setEnabled(enabled);
	}
	public EditText getEditText(){
		return mEditTextEdit;
	}
}
