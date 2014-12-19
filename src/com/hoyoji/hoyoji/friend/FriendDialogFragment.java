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
	private IWXAPI api;
	private QQShare mQQShare = null;
	public static QQAuth mQQAuth;
	
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
        
        mQQAuth = QQAuth.createInstance(AppConstants.TENTCENT_CONNECT_APP_ID, getActivity());
		mQQShare = new QQShare(getActivity(), mQQAuth.getQQToken());

		final Bundle bundle = getArguments();
    	View v = inflater.inflate(R.layout.friend_dialogfragment_friend, null);
    	v.findViewById(R.id.friendDialogFragment_add_online).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((HyjActivity)getActivity()).openActivityWithFragment(AddFriendListFragment.class, R.string.addFriendListFragment_title_add, null);
//				((HyjActivity)getActivity()).openActivityWithFragment(MoneyExpenseContainerFormFragment.class, R.string.moneyExpenseFormFragment_title_addnew, bundle);
				dismiss();
			}
    	});
    	v.findViewById(R.id.friendDialogFragment_add_local).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((HyjActivity)getActivity()).openActivityWithFragment(FriendFormFragment.class, R.string.friendFormFragment_title_create, null);
//				((HyjActivity)getActivity()).openActivityWithFragment(MoneyIncomeContainerFormFragment.class, R.string.moneyIncomeFormFragment_title_addnew, bundle);
				dismiss();
			}
    	});    	
    	v.findViewById(R.id.friendDialogFragment_add_phoneList).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((HyjActivity)getActivity()).openActivityWithFragment(ImportFriendListFragment.class, R.string.friendFormFragment_title_import, null);
//				((HyjActivity)getActivity()).openActivityWithFragment(MoneyDepositExpenseContainerFormFragment.class, R.string.moneyDepositExpenseFormFragment_title_addnew, bundle);
				dismiss();
			}
    	});
    	v.findViewById(R.id.friendDialogFragment_add_category).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((HyjActivity)getActivity()).openActivityWithFragment(FriendCategoryFormFragment.class, R.string.friendCategoryFormFragment_title_create, null);
//				((HyjActivity)getActivity()).openActivityWithFragment(MoneyDepositIncomeContainerFormFragment.class, R.string.moneyDepositIncomeContainerFormFragment_title_addnew, bundle);
				dismiss();
			}
    	});
    	v.findViewById(R.id.friendDialogFragment_invite_qq).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				inviteFriend("QQ");
//				((HyjActivity)getActivity()).openActivityWithFragment(MoneyDepositReturnContainerFormFragment.class, R.string.moneyDepositReturnContainerFormFragment_title_addnew, bundle);
//				dismiss();
			}
    	});
    	v.findViewById(R.id.friendDialogFragment_invite_wx).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				inviteFriend("WX");
//				((HyjActivity)getActivity()).openActivityWithFragment(MoneyDepositPaybackContainerFormFragment.class, R.string.moneyDepositPaybackFormFragment_title_addnew, bundle);
//				dismiss();
			}
    	});
    	v.findViewById(R.id.friendDialogFragment_invite_other).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				inviteFriend("Other");
//				((HyjActivity)getActivity()).openActivityWithFragment(MoneyTemplateListFragment.class, R.string.moneyTemplateListFragment_title, null);
//				dismiss();
			}
    	});

        // Use the Builder class for convenient dialog construction
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the layout for the dialog
        builder.setView(v);

        // Set title of dialog
        builder.setTitle("好友管理")
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
    
    public void inviteFriend(final String way) {
		((HyjActivity) getActivity()).displayProgressDialog(R.string.friendListFragment__action_invite_title,R.string.friendListFragment__action_invite_content);
		
		JSONObject inviteFriendObject = new JSONObject();
		final String id = UUID.randomUUID().toString();
		try {
			inviteFriendObject.put("id", id);
			inviteFriendObject.put("__dataType", "InviteLink");
			inviteFriendObject.put("title", "邀请成为好友");
			inviteFriendObject.put("type", "Friend");
			inviteFriendObject.put("date", (new Date()).getTime());
			inviteFriendObject.put("description", HyjApplication.getInstance().getCurrentUser().getDisplayName() + " 邀请您成为好友，一起参与记账。");
			inviteFriendObject.put("state", "Open");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
   	 
   	// 从服务器上下载用户数据
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				((HyjActivity) getActivity()).dismissProgressDialog();
					if(way.equals("Other")){
						inviteOtherFriend(id);
					} else if(way.equals("WX")){
						inviteWXFriend(id);
					} else if(way.equals("QQ")){
						inviteQQFriend(id);
					}
			}

			@Override
			public void errorCallback(Object object) {
				((HyjActivity) getActivity()).dismissProgressDialog();
				try {
					JSONObject json = (JSONObject) object;
					((HyjActivity) getActivity()).displayDialog(null,
							json.getJSONObject("__summary")
									.getString("msg"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
   	 
   	 	HyjHttpPostAsyncTask.newInstance(serverCallbacks, "[" + inviteFriendObject.toString() + "]", "postData");
	 }

	public void inviteOtherFriend(String id) {
		Intent intent=new Intent(Intent.ACTION_SEND);   
        intent.setType("text/plain");   
        
//      File f;
//		try {
//			f = HyjUtil.createImageFile("invite_friend", "PNG");
//			if(!f.exists()){
//		        Bitmap bmp = HyjUtil.getCommonBitmap(R.drawable.invite_friend);
//			    FileOutputStream out;
//				out = new FileOutputStream(f);
//				bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
//				out.close();
//			}
//	        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));

//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	        intent.putExtra(Intent.EXTRA_TITLE, "邀请成为好友");  
	        intent.putExtra(Intent.EXTRA_SUBJECT, "邀请成为好友");   
	        intent.putExtra(Intent.EXTRA_TEXT, HyjApplication.getInstance().getCurrentUser().getDisplayName() + " 邀请您成为好友，一起参与记账。\n\n" + HyjApplication.getServerUrl()+"m/invite.html?id=" + id);  
	        
	        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
	        startActivity(Intent.createChooser(intent, "邀请成为好友")); 
	}
	
	public void inviteWXFriend(String id) {
		api = WXAPIFactory.createWXAPI(getActivity(), AppConstants.WX_APP_ID);
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = HyjApplication.getInstance().getServerUrl()+"m/invite.html?id=" + id;
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = "邀请成为好友";
		msg.description = HyjApplication.getInstance().getCurrentUser().getDisplayName() + " 邀请您成为好友，一起参与记账。";
		Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		msg.thumbData = Util.bmpToByteArray(thumb, true);
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("webpage");
		req.message = msg;
//		req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		api.sendReq(req);
		
	}
	
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
	
	public void inviteQQFriend(String id) {
		final Bundle params = new Bundle();
	    params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
	    params.putString(QQShare.SHARE_TO_QQ_TITLE, "邀请成为好友");
	    params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  HyjApplication.getInstance().getCurrentUser().getDisplayName() + " 邀请您成为好友，一起参与记账。");
	    params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  HyjApplication.getInstance().getServerUrl()+"m/invite.html?id=" + id);
	    params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, HyjApplication.getInstance().getServerUrl() + "imgs/invite_friend.png");
	    params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  "好友AA记账");
//	    params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,  "其他附加功能");		
	    mQQShare.shareToQQ(getActivity(), params, new BaseUIListener(getActivity()) {

            @Override
            public void onCancel() {
//            		Util.toastMessage(getActivity(), "onCancel: ");
            }

            @Override
            public void onComplete(Object response) {
//                Util.toastMessage(getActivity(), "onComplete: " + response.toString());
            }

            @Override
            public void onError(UiError e) {
//                Util.toastMessage(getActivity(), "onError: " + e.errorMessage, "e");
            }

        });
	}
	
}