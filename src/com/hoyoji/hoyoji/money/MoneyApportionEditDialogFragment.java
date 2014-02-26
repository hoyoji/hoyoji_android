package com.hoyoji.hoyoji.money;

import java.util.Calendar;

import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.fragment.HyjDateTimePickerDialogFragment;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjSpinnerField;
import com.hoyoji.hoyoji.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      	final String apportionType = getArguments().getString("apportionType");
    	Double apportionAmount = getArguments().getDouble("apportionAmount");
    	
        // Inflate layout for the view
        // Pass null as the parent view because its going in the dialog layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
    	
    	View v = inflater.inflate(R.layout.money_dialogfragment_moneyapportion_edit, null);
        final HyjNumericField numericFieldApportionAmount = (HyjNumericField)v.findViewById(R.id.moneyApportionDialogFragment_textField_amount);
        numericFieldApportionAmount.setNumber(apportionAmount);
        
        final HyjSpinnerField spinnerFieldApportionType = (HyjSpinnerField)v.findViewById(R.id.moneyApportionDialogFragment_spinnerField_type);
        spinnerFieldApportionType.setItems(R.array.moneyApportionDialogFragment_spinnerField_apportionType_array, new String[] {"Average", "Share", "Fixed"});
        spinnerFieldApportionType.setSelectedValue(apportionType);
        

        v.findViewById(R.id.moneyApportionDialogFragment_button_delete).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		        ((HyjActivity) getActivity()).dialogDoNegativeClick();
			}
        });
         
        // Use the Builder class for convenient dialog construction
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the layout for the dialog
        builder.setView(v);

        // Set title of dialog
        builder.setTitle("修改分摊")
                // Set Ok button
                .setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
            			        Bundle args = new Bundle();
            			        args.putDouble("apportionAmount", numericFieldApportionAmount.getNumber());
            			        args.putString("apportionType", spinnerFieldApportionType.getSelectedValue());
            					getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            			        ((HyjActivity) getActivity()).dialogDoPositiveClick(args);
                            }
                        })
                // Set Cancel button
                .setNegativeButton(R.string.alert_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                		        ((HyjActivity) getActivity()).dialogDoNeutralClick();
                            }
                        }); 
        
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // Create the AlertDialog object and return it
        return builder.create();
    }
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//            Bundle savedInstanceState) {
//    	
//    	final String apportionType = getArguments().getString("apportionType");
//    	Double apportionAmount = getArguments().getDouble("apportionAmount");
//    	
//    	getDialog().setTitle("修改分摊");
//    	
//    	
//    	View v = inflater.inflate(R.layout.money_dialogfragment_moneyapportion_edit, container, false);
//        final HyjNumericField numericFieldApportionAmount = (HyjNumericField)v.findViewById(R.id.moneyApportionDialogFragment_textField_amount);
//        numericFieldApportionAmount.setNumber(apportionAmount);
//        
//        final HyjSpinnerField spinnerFieldApportionType = (HyjSpinnerField)v.findViewById(R.id.moneyApportionDialogFragment_spinnerField_type);
//        spinnerFieldApportionType.setItems(R.array.moneyApportionDialogFragment_spinnerField_apportionType_array, new String[] {"Average", "Share", "Fixed"});
//        spinnerFieldApportionType.setSelectedValue(apportionType);
//        
//        v.findViewById(R.id.moneyApportionDialogFragment_button_cancel).setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//		        ((HyjActivity) getActivity()).dialogDoNeutralClick();
//			}
//        });
//        v.findViewById(R.id.moneyApportionDialogFragment_button_delete).setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//		        ((HyjActivity) getActivity()).dialogDoNegativeClick();
//			}
//        });
//        v.findViewById(R.id.moneyApportionDialogFragment_button_yes).setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//			        Bundle args = new Bundle();
//			        args.putDouble("apportionAmount", numericFieldApportionAmount.getNumber());
//			        args.putString("apportionType", spinnerFieldApportionType.getSelectedValue());
//					getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//			        ((HyjActivity) getActivity()).dialogDoPositiveClick(args);
//			}
//        });
//         
//         
//		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//        return v;
//    }
}