package com.hoyoji.android.hyjframework.view;

import com.hoyoji.hoyoji.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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

		try {
			mLabelText = a.getString(R.styleable.HyjTextField_labelText);
			mEditText = a.getString(R.styleable.HyjTextField_editText);
			mHintText = a.getString(R.styleable.HyjTextField_hintText);
		} finally {
			a.recycle();
		}
		
		final LayoutInflater inflater = (LayoutInflater)
			       context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.numeric_field, this);
		mTextViewLabel = (TextView)findViewById(R.id.text_field_label);
		mEditTextEdit = (EditText)findViewById(R.id.text_field_edit);
		
		mEditTextEdit.setHint(mHintText);
		mEditTextEdit.setText(mEditText);
		mTextViewLabel.setText(mLabelText);
	}

	public void setError(String error){
		mEditTextEdit.setError(error);
	}
	
	public void setText(String text){
		mEditTextEdit.setText(text);
	}
	
	public Editable getText(){
		return mEditTextEdit.getText();
	}
	
	public void setNumber(Double number){
		if(number == null){
			mEditTextEdit.setText("");
		} else {
			mEditTextEdit.setText(number.toString());
		}
	}
	
	public Double getNumber(){
		try{
			return Double.valueOf(getText().toString());
		} catch (NumberFormatException e){
			return null;
		}

	}
}
