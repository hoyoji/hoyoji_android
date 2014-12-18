package com.hoyoji.hoyoji.friend;

import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.hoyoji.AppConstants;
import com.hoyoji.hoyoji.models.Event;
import com.hoyoji.hoyoji_android.R;
import com.tencent.connect.auth.QQAuth;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.tencent.mm.sdk.platformtools.Util;
import com.tencent.sample.BaseUIListener;
import com.tencent.tauth.UiError;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

public class FriendDialogFragment extends DialogFragment {
	
	public static FriendDialogFragment newInstance(Bundle args) {
    	FriendDialogFragment f = new FriendDialogFragment();
        f.setArguments(args);
        return f;
    }

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	
        // Inflate layout for the view
        // Pass null as the parent view because its going in the dialog layout
        LayoutInflater inflater = getActivity().getLayoutInflater();

		final Bundle bundle = getArguments();
    	View v = inflater.inflate(R.layout.event_dialogfragment_member, null);
    	v.findViewById(R.id.friendDialogFragment_add_online).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
//				((HyjActivity)getActivity()).openActivityWithFragment(MoneyExpenseContainerFormFragment.class, R.string.moneyExpenseFormFragment_title_addnew, bundle);
				dismiss();
			}
    	});
    	v.findViewById(R.id.friendDialogFragment_add_local).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
//				((HyjActivity)getActivity()).openActivityWithFragment(MoneyIncomeContainerFormFragment.class, R.string.moneyIncomeFormFragment_title_addnew, bundle);
//				dismiss();
			}
    	});    	
    	v.findViewById(R.id.friendDialogFragment_add_phoneList).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
//				((HyjActivity)getActivity()).openActivityWithFragment(MoneyDepositExpenseContainerFormFragment.class, R.string.moneyDepositExpenseFormFragment_title_addnew, bundle);
				dismiss();
			}
    	});
    	v.findViewById(R.id.friendDialogFragment_add_category).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
//				((HyjActivity)getActivity()).openActivityWithFragment(MoneyDepositIncomeContainerFormFragment.class, R.string.moneyDepositIncomeContainerFormFragment_title_addnew, bundle);
				dismiss();
			}
    	});
    	v.findViewById(R.id.friendDialogFragment_invite_qq).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
//				((HyjActivity)getActivity()).openActivityWithFragment(MoneyDepositReturnContainerFormFragment.class, R.string.moneyDepositReturnContainerFormFragment_title_addnew, bundle);
//				dismiss();
			}
    	});
    	v.findViewById(R.id.friendDialogFragment_invite_wx).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
//				((HyjActivity)getActivity()).openActivityWithFragment(MoneyDepositPaybackContainerFormFragment.class, R.string.moneyDepositPaybackFormFragment_title_addnew, bundle);
				dismiss();
			}
    	});
    	v.findViewById(R.id.friendDialogFragment_invite_other).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
//				((HyjActivity)getActivity()).openActivityWithFragment(MoneyTemplateListFragment.class, R.string.moneyTemplateListFragment_title, null);
				dismiss();
			}
    	});

        // Use the Builder class for convenient dialog construction
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the layout for the dialog
        builder.setView(v);

        // Set title of dialog
        builder.setTitle("发送链接")
//                // Set Ok button
//                .setPositiveButton(R.string.alert_dialog_ok,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//            			        
//                            }
//                        })
                // Set Cancel button
                .setNegativeButton(R.string.alert_dialog_cancel, null); 

        // Create the AlertDialog object and return it
        return builder.create();
    }
    
    protected void openActivityWithFragment(
			Class<FriendDialogFragment> class1,
			int projecteventmemberformfragmentActionAddnew, Bundle bundle) {
		// TODO Auto-generated method stub
		
	}
	
}