package com.hoyoji.android.hyjframework.fragment;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji_android.R;

public class HyjCalculatorFormFragment extends HyjUserFormFragment {

	private TextView mHyjRemarkFieldAmount = null;
	boolean mTextCannotbeEmpty = false;
	
	private Button button_1;
	private Button button_2;
	private Button button_3;
	private Button button_4;
	private Button button_5;
	private Button button_6;
	private Button button_7;
	private Button button_8;
	private Button button_9;
	private Button button_0;
	private Button button_point;
	private Button button_clear;
	private Button button_plus;
	private Button button_subtract;
	private Button button_multiply;
	private Button button_divide;
	private Button button_delete;
	private Button button_equal;
	
	
	
	
	@Override
	public Integer useContentView() {
		return R.layout.calculator_formfragment;
	}

//	@Override
//	public Integer useOptionsMenuView() {
//		return null;
//	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		buttonInitialization();
		Intent intent = getActivity().getIntent();
		String amount = intent.getStringExtra("AMOUNT");
		
		mHyjRemarkFieldAmount = (TextView) getView().findViewById(R.id.hyjCalculatorFormFragment_textField_amount);
//		String title = intent.getStringExtra("TITLE");
//		mTextCannotbeEmpty = intent.getBooleanExtra("NOT_EMPTY", false);
//		
//		mTextFieldRemark = (EditText) getView().findViewById(R.id.textInputFormFragment_textField_remark);
//		mTextFieldRemark.setText(text);
//		mTextFieldRemark.setHint(hint);
//		
//		this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}
	
	private void buttonInitialization() {
		button_0 = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_0);
		button_1 = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_1);
		button_2 = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_2);
		button_3 = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_3);
		button_4 = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_4);
		button_5 = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_5);
		button_6 = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_6);
		button_7 = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_7);
		button_8 = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_8);
		button_9 = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_9);
		button_clear = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_clear);
		button_delete = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_delete);
		button_plus = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_plus);
		button_subtract = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_subtract);
		button_multiply = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_multiply);
		button_divide = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_divide);
		button_equal = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_equal);
		button_point = (Button)getView().findViewById(R.id.hyjCalculatorFromFragment_button_point);
		
		button_0.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"0");
			}
		});
		
		button_1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"1");
			}
		});
		
		button_2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"2");
			}
		});
		button_3.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"3");
			}
		});
		
		button_4.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"4");
			}
		});
		
		button_5.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"5");
			}
		});
		
		button_6.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"6");
			}
		});
		
		button_7.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"7");
			}
		});
		
		button_8.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"8");
			}
		});
		
		button_9.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"9");
			}
		});
		
		button_clear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"c");
			}
		});
		
		button_delete.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"d");
			}
		});
		
		button_point.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"p");
			}
		});
		
		button_plus.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"pl");
			}
		});
		
		button_subtract.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"j");
			}
		});
		
		button_multiply.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"ch");
			}
		});
		
		button_divide.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"chu");
			}
		});
		
		button_equal.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHyjRemarkFieldAmount.setText(mHyjRemarkFieldAmount.getText()+"\n"+"0");
			}
		});


	}

//	 @Override
//	 public void onSave(View v){
//		 super.onSave(v);
// 		
//		 if(this.mTextCannotbeEmpty && mTextFieldRemark.getText().toString().isEmpty()){
//			 mTextFieldRemark.setError("请输入内容");
//			 return;
//		 }
//		 Intent intent = new Intent();
//		 intent.putExtra("TEXT", mTextFieldRemark.getText().toString());
//		 getActivity().setResult(Activity.RESULT_OK, intent);
//		 
//		 getActivity().finish();
//	 }
}
