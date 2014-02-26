package com.hoyoji.hoyoji.money;

import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.hoyoji.R;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

public class MoneyApportionEditDialogFragment extends DialogFragment {
    static MoneyApportionEditDialogFragment newInstance(Double apportionAmount, String apportionType) {
    	MoneyApportionEditDialogFragment f = new MoneyApportionEditDialogFragment();
        Bundle args = new Bundle();
        args.putDouble("apportionAmount", apportionAmount);
        args.putString("apportionType", apportionType);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	final String apportionType = getArguments().getString("apportionType");
    	Double apportionAmount = getArguments().getDouble("apportionAmount");
    	
    	getDialog().setTitle("修改分摊");
    	
    	
    	View v = inflater.inflate(R.layout.money_dialogfragment_moneyapportion_edit, container, false);
        final HyjNumericField numericFieldApportionAmount = (HyjNumericField)v.findViewById(R.id.moneyApportionDialogFragment_textField_amount);
        numericFieldApportionAmount.setNumber(apportionAmount);
        
        v.findViewById(R.id.moneyApportionDialogFragment_button_cancel).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		        ((HyjActivity) getActivity()).dialogDoNeutralClick();
			}
        });
        v.findViewById(R.id.moneyApportionDialogFragment_button_delete).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		        ((HyjActivity) getActivity()).dialogDoNegativeClick();
			}
        });
        v.findViewById(R.id.moneyApportionDialogFragment_button_yes).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
			        Bundle args = new Bundle();
			        args.putDouble("apportionAmount", numericFieldApportionAmount.getNumber());
			        args.putString("apportionType", "Fixed");
					getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			        ((HyjActivity) getActivity()).dialogDoPositiveClick(args);
			}
        });
         
         
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return v;
    }
}