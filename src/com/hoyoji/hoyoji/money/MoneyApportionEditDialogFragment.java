package com.hoyoji.hoyoji.money;

import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjSpinnerField;
import com.hoyoji.hoyoji_android.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class MoneyApportionEditDialogFragment extends DialogFragment {
	private HyjNumericField mNumericFieldApportionAmount;
	private HyjSpinnerField mSpinnerFieldApportionType;
	
	static MoneyApportionEditDialogFragment newInstance(Double apportionAmount, String apportionType, boolean isProjectMember) {
    	MoneyApportionEditDialogFragment f = new MoneyApportionEditDialogFragment();
        Bundle args = new Bundle();
        args.putDouble("apportionAmount", apportionAmount);
        args.putString("apportionType", apportionType);
	    args.putBoolean("isProjectMember", isProjectMember);
        f.setArguments(args);
        return f;
    }

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      	final String apportionType = getArguments().getString("apportionType");
    	Double apportionAmount = getArguments().getDouble("apportionAmount");
    	boolean isProjectMember = getArguments().getBoolean("isProjectMember");
    	
        // Inflate layout for the view
        // Pass null as the parent view because its going in the dialog layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
    	
    	View v = inflater.inflate(R.layout.money_dialogfragment_moneyapportion_edit, null);
    	final HyjNumericField numericFieldApportionAmount = (HyjNumericField)v.findViewById(R.id.moneyApportionDialogFragment_textField_amount);
        numericFieldApportionAmount.setNumber(apportionAmount);
        mNumericFieldApportionAmount = numericFieldApportionAmount;
        final HyjSpinnerField spinnerFieldApportionType = (HyjSpinnerField)v.findViewById(R.id.moneyApportionDialogFragment_spinnerField_type);
        mSpinnerFieldApportionType = spinnerFieldApportionType;
        if(isProjectMember){
        	spinnerFieldApportionType.setItems(R.array.moneyApportionDialogFragment_spinnerField_apportionType_array, new String[] {"Average", "Fixed", "Share"});
        } else {
        	spinnerFieldApportionType.setItems(R.array.moneyApportionDialogFragment_spinnerField_apportionType_array_non_project_member, new String[] {"Average", "Fixed"});
        }
        spinnerFieldApportionType.setSelectedValue(apportionType);
		numericFieldApportionAmount.setEnabled(apportionType == "Fixed");
        spinnerFieldApportionType.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int pos, long arg3) {
				if(pos == 1){
					numericFieldApportionAmount.setEnabled(true);
					numericFieldApportionAmount.showSoftKeyboard();
				} else {
					numericFieldApportionAmount.setEnabled(false);
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
        });
        

        v.findViewById(R.id.moneyApportionDialogFragment_button_delete).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((HyjActivity) getActivity()).dialogDoNegativeClick();
		        dismiss();
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
								((HyjActivity) getActivity()).dialogDoPositiveClick(args);
                            }
                        })
                // Set Cancel button
                .setNegativeButton(R.string.alert_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                		        ((HyjActivity) getActivity()).dialogDoNeutralClick();
                            }
                        }); 
        

        // Create the AlertDialog object and return it
        return builder.create();
    }
    

	public void setApportionAmount(Double amount) {
		mNumericFieldApportionAmount.setNumber(amount);
	}
	
	public String getApportionType(){
		return mSpinnerFieldApportionType.getSelectedValue();
	}
}