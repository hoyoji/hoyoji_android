package com.hoyoji.hoyoji.project;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Layout;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.activeandroid.Model;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.AppConstants;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.models.UserData;
import com.tencent.connect.auth.QQAuth;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.tencent.sample.BaseUIListener;
import com.tencent.sample.Util;
import com.tencent.tauth.UiError;

public class MemberListFragment extends HyjUserListFragment{
	public final static int ADD_SUB_PROJECT = 0;
	public final static int VIEW_PROJECT_MEMBERS = 1;
	private ContentObserver mUserDataChangeObserver = null;
	private IWXAPI api;
	private QQShare mQQShare = null;
	public static QQAuth mQQAuth;
	
	@Override
	public Integer useContentView() {
		return R.layout.project_listfragment_member;
	}

	@Override
	public Integer useOptionsMenuView() {
		return R.menu.project_listfragment_member;
	}

	@Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.project_listitem_member,
				null,
				new String[] { "friendUserId", "friendUserId", "sharePercentage", "state", "id", "id", "id"},
				new int[] { R.id.memberListItem_picture, R.id.memberListItem_name, R.id.memberListItem_percentage, R.id.memberListItem_remark, R.id.memberListItem_actualTotal, R.id.memberListItem_apportionTotal, R.id.memberListItem_settlement},
				0); 
	}	


	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		super.onCreateLoader(arg0, arg1);

		int offset = arg1.getInt("OFFSET");
		int limit = arg1.getInt("LIMIT");
		if(limit == 0){
			limit = getListPageSize();
		}
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		Project project =  Model.load(Project.class, modelId);
		Object loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(ProjectShareAuthorization.class, null),
				null,
				"projectId=? AND state <> ?", 
				new String[]{project.getId(), "Delete"}, 
				"friendUserId LIMIT " + (limit + offset) 
			);
		return (Loader<Object>)loader;
	}


	@Override
	protected View useHeaderView(Bundle savedInstanceState){
		Intent intent = getActivity().getIntent();
		String nullItemName = intent.getStringExtra("NULL_ITEM");
		if(nullItemName == null){
			return null;
		}
		RelativeLayout view =  (RelativeLayout) getLayoutInflater(savedInstanceState).inflate(R.layout.project_listitem_member, null);
		TextView nameView = (TextView)view.findViewById(R.id.memberListItem_name);
		nameView.setText(nullItemName);
		view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(getActivity().getCallingActivity() != null){
					Intent intent = new Intent();
					intent.putExtra("MODEL_ID", -1);
					intent.putExtra("MODEL_TYPE", "ProjectShareAuthorization");
					getActivity().setResult(Activity.RESULT_OK, intent);
					getActivity().finish();
				}
			}
			
		});
		return view;
	}
	
	@Override
	public void onInitViewData() {
		super.onInitViewData();
		if (mUserDataChangeObserver == null) {
			mUserDataChangeObserver = new ChangeObserver();
			this.getActivity().getContentResolver()
					.registerContentObserver(
							ContentProvider.createUri(
									UserData.class, null), true,
									mUserDataChangeObserver);
		}
		mQQAuth = QQAuth.createInstance(AppConstants.TENTCENT_CONNECT_APP_ID, getActivity());
		mQQShare = new QQShare(getActivity(), mQQAuth.getQQToken());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		
		Project project = Project.load(Project.class, modelId);
		if(!project.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
			HyjUtil.displayToast("您不能在共享来的项目添加共享成员");
			return true;
		}
		if(item.getItemId() == R.id.memberListFragment_action_member_addnew){
				Bundle bundle = new Bundle();
				bundle.putLong("PROJECT_ID", modelId);
				openActivityWithFragment(MemberFormFragment.class, R.string.memberFormFragment_title_addnew, bundle);
				return true;
		} else if(item.getItemId() == R.id.memberListFragment_action_member_invite){
			Bundle bundle = new Bundle();
			bundle.putLong("PROJECT_ID", modelId);
			bundle.putString("INVITE_TYPE", "Other");
			openActivityWithFragment(InviteMemberFormFragment.class, R.string.inviteMemberFormFragment_send_title, bundle);
//			inviteFriend("Other");
//			return true;
		} else if(item.getItemId() == R.id.memberListFragment_action_member_invite_wxFriend){
			Bundle bundle = new Bundle();
			bundle.putLong("PROJECT_ID", modelId);
			bundle.putString("INVITE_TYPE", "WX");
			openActivityWithFragment(InviteMemberFormFragment.class, R.string.inviteMemberFormFragment_send_title, bundle);
//			inviteFriend("WX");
//			return true;
		} else if(item.getItemId() == R.id.memberListFragment_action_member_invite_qqFriend){
			Bundle bundle = new Bundle();
			bundle.putLong("PROJECT_ID", modelId);
			bundle.putString("INVITE_TYPE", "QQ");
			openActivityWithFragment(InviteMemberFormFragment.class, R.string.inviteMemberFormFragment_send_title, bundle);
//			inviteFriend("QQ");
//			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void inviteFriend(final String way) {
		JSONObject inviteFriendObject = new JSONObject();
		final String id = UUID.randomUUID().toString();
   		try {
   				Intent intent = getActivity().getIntent();
	   			Long modelId = intent.getLongExtra("MODEL_ID", -1);
	   			Project project =  Model.load(Project.class, modelId);
	   			inviteFriendObject.put("data", project.getId());
	   			inviteFriendObject.put("id", id);
				inviteFriendObject.put("__dataType", "InviteLink");
				inviteFriendObject.put("title", "邀请加入项目");
				inviteFriendObject.put("type", "ProjectShare");
				inviteFriendObject.put("description", HyjApplication.getInstance().getCurrentUser().getDisplayName() + " 邀请您加入项目: "+project.getName()+"，一起参与记账。");
				inviteFriendObject.put("state", "Open");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
   	 
   	// 从服务器上下载用户数据
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
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
		Intent activityIntent = getActivity().getIntent();
		Long modelId = activityIntent.getLongExtra("MODEL_ID", -1);
		Project project =  Model.load(Project.class, modelId);
			
		Intent intent=new Intent(Intent.ACTION_SEND);   
        intent.setType("image/*");   
        intent.putExtra(Intent.EXTRA_TITLE, "邀请加入项目");  
        intent.putExtra(Intent.EXTRA_SUBJECT, "邀请加入项目");   
        intent.putExtra(Intent.EXTRA_TEXT, HyjApplication.getInstance().getCurrentUser().getDisplayName() + " 邀请您加入项目: "+project.getName()+"，一起参与记账。\n\n" + HyjApplication.getInstance().getServerUrl()+"m/invite.html?id=" + id);   
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
        startActivity(Intent.createChooser(intent, "邀请项目成员")); 
	}
	
	public void inviteWXFriend(String id) {
		Intent activityIntent = getActivity().getIntent();
		Long modelId = activityIntent.getLongExtra("MODEL_ID", -1);
		Project project =  Model.load(Project.class, modelId);
		
		api = WXAPIFactory.createWXAPI(getActivity(), AppConstants.WX_APP_ID);
		api.registerApp(AppConstants.WX_APP_ID);
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = HyjApplication.getInstance().getServerUrl()+"m/invite.html?id=" + id;
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = "邀请加入项目";
		msg.description = HyjApplication.getInstance().getCurrentUser().getDisplayName() + " 邀请您加入项目: "+project.getName()+"，一起参与记账。";
		Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		msg.thumbData = Util.bmpToByteArray(thumb, true);
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("webpage");
		req.message = msg;
//		req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		api.sendReq(req);
		
	}
	
	public void inviteQQFriend(String id) {
		Intent activityIntent = getActivity().getIntent();
		Long modelId = activityIntent.getLongExtra("MODEL_ID", -1);
		Project project =  Model.load(Project.class, modelId);
		
		final Bundle params = new Bundle();
	    params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
	    params.putString(QQShare.SHARE_TO_QQ_TITLE, "邀请加入项目");
	    params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  HyjApplication.getInstance().getCurrentUser().getDisplayName() + " 邀请您加入项目: "+project.getName()+"，一起参与记账。");
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
                // TODO Auto-generated method stub
//                Util.toastMessage(getActivity(), "onComplete: " + response.toString());
            }

            @Override
            public void onError(UiError e) {
                // TODO Auto-generated method stub
//                Util.toastMessage(getActivity(), "onError: " + e.errorMessage, "e");
            }

        });
	}
	
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
	
	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		if(id == -1) {
			 return;
		}
		if(getActivity().getCallingActivity() != null){
			Intent intent = new Intent();
			intent.putExtra("MODEL_ID", id);
			intent.putExtra("MODEL_TYPE", "ProjectShareAuthorization");
			getActivity().setResult(Activity.RESULT_OK, intent);
			getActivity().finish();
		} else {
			Bundle bundle = new Bundle();
			bundle.putLong("MODEL_ID", id);
			openActivityWithFragment(MemberFormFragment.class, R.string.memberFormFragment_title_edit, bundle);
		}
    }  

//	@Override 
//	public void onDeleteListItem(Long id){
//		Project project = Project.load(Project.class, id);
//		project.delete();
//	    HyjUtil.displayToast("项目删除成功");
//	}
//	
//	@Override
//	public boolean onContextItemSelected(MenuItem item) {
//		if(!getUserVisibleHint()){
//			return super.onContextItemSelected(item);
//		}
//	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
//	    Long itemId = getListAdapter().getItemId(info.position);
//		switch (item.getItemId()) {
//			case ADD_SUB_PROJECT:
//			    HyjUtil.displayToast("创建子项目" + itemId);
//				break;
//			case VIEW_PROJECT_MEMBERS:
//			    HyjUtil.displayToast("项目成员" + itemId);
//				break;
//		}
//		return super.onContextItemSelected(item);
//	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//		super.onCreateContextMenu(menu, v, menuInfo);
//		menu.add(0, VIEW_PROJECT_MEMBERS, 0, "项目成员");
//		menu.add(0, ADD_SUB_PROJECT, 1, "创建子项目");
//		menu.add(CANCEL_LIST_ITEM, CANCEL_LIST_ITEM, CANCEL_LIST_ITEM, R.string.app_action_cancel_list_item);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		Project project = Project.load(Project.class, modelId);
		if(!project.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
			getOptionsMenu().findItem(R.id.memberListFragment_action_member_add).setVisible(false);
		}
	}
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(view.getId() == R.id.memberListItem_name) {
			String friendUserId = cursor.getString(columnIndex);
			Friend friend = null;
			if(friendUserId != null){
				friend = new Select().from(Friend.class).where("friendUserId=?", friendUserId).executeSingle();
				if(friend != null){
					((TextView)view).setText(friend.getDisplayName());
				} else {
					User user = HyjModel.getModel(User.class, friendUserId);
					if(user != null){
						((TextView)view).setText(user.getDisplayName());
					} else {
						((TextView)view).setText(cursor.getString(cursor.getColumnIndex("friendUserName")));
					}
				}
			} else {
				String localFriendId = cursor.getString(cursor.getColumnIndex("localFriendId"));
				if(localFriendId != null){
					friend = HyjModel.getModel(Friend.class, localFriendId);
					if(friend != null){
						((TextView)view).setText(friend.getDisplayName());
					} else {
						((TextView)view).setText(cursor.getString(cursor.getColumnIndex("friendUserName")));
					}
				} else {
					((TextView)view).setText(cursor.getString(cursor.getColumnIndex("friendUserName")));
				}
			}
			return true;
		} else if(view.getId() == R.id.memberListItem_picture) {
			String friendUserId = cursor.getString(columnIndex);
			if(friendUserId != null){
				User user = HyjModel.getModel(User.class, friendUserId);
				if(user == null){
					((HyjImageView)view).setImage((Picture)null);
				} else {
					((HyjImageView)view).setImage(user.getPictureId());
				}
			} else {
				((HyjImageView)view).setImage((Picture)null);
			}
			return true;
		} else if(view.getId() == R.id.memberListItem_percentage) {
			double percentage = cursor.getDouble(columnIndex);
			((HyjNumericView)view).setPrefix(null);
			((HyjNumericView)view).setSuffix("%");
			((HyjNumericView)view).setNumber(percentage);
			return true;
		} else if(view.getId() == R.id.memberListItem_remark) {
			String friendUserId = cursor.getString(cursor.getColumnIndex("friendUserId"));
			if(friendUserId == null){
				((TextView)view).setText("");
			} else {
				String state = cursor.getString(columnIndex);
				if(state.equalsIgnoreCase("Wait")){
					((TextView)view).setText(R.string.memberListFragment_state_wait);
				} else if(state.equalsIgnoreCase("NotInvite")){
					((TextView)view).setText(R.string.memberListFragment_state_notinvite);
				} else {
					((TextView)view).setText("");
				}
			}
			return true;
		} else if(view.getId() == R.id.memberListItem_actualTotal) {
			HyjNumericView numericView = (HyjNumericView)view;
			ProjectShareAuthorization projectShareAuthorization = HyjModel.getModel(ProjectShareAuthorization.class, cursor.getString(columnIndex));
			if(!HyjApplication.getInstance().getCurrentUser().getId().equals(projectShareAuthorization.getFriendUserId())){
				ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", projectShareAuthorization.getProjectId(), HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
				if(psa != null && psa.getProjectShareMoneyExpenseOwnerDataOnly() == true){
					numericView.setSuffix(null);
					numericView.setTextColor(Color.BLACK);
					numericView.setPrefix("-");
					numericView.setText(null);
					return true;
				}
			}
			
			Double actualTotal = projectShareAuthorization.getActualTotal();
			String currencySymbol = projectShareAuthorization.getProject().getCurrencySymbol();
			if(actualTotal < 0){
				actualTotal = -actualTotal;
				numericView.setPrefix("已经收入:" + currencySymbol);
//				numericView.setTextColor(Color.parseColor("#339900"));
			}else{
				numericView.setPrefix("已经支出:" + currencySymbol);
//				if(actualTotal.equals(0.0)){
//					numericView.setTextColor(Color.parseColor("#000000"));
//				}else{
//			    	numericView.setTextColor(Color.parseColor("#FF0000"));
//				}
			}
			numericView.setSuffix(null);
			numericView.setNumber(actualTotal);
			return true;
		} else if(view.getId() == R.id.memberListItem_apportionTotal) {
			HyjNumericView numericView = (HyjNumericView)view;
			ProjectShareAuthorization projectShareAuthorization = HyjModel.getModel(ProjectShareAuthorization.class, cursor.getString(columnIndex));
			if(!HyjApplication.getInstance().getCurrentUser().getId().equals(projectShareAuthorization.getFriendUserId())){
				ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", projectShareAuthorization.getProjectId(), HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
				if(psa != null && psa.getProjectShareMoneyExpenseOwnerDataOnly() == true){
					numericView.setSuffix(null);
					numericView.setTextColor(Color.BLACK);
					numericView.setPrefix("-");
					numericView.setText(null);
					return true;
				}
			}
			Double apportionTotal = projectShareAuthorization.getApportionTotal();
			String currencySymbol = projectShareAuthorization.getProject().getCurrencySymbol();
			if(apportionTotal < 0){
				apportionTotal = -apportionTotal;
				numericView.setPrefix("分摊收入:" + currencySymbol);
//				numericView.setTextColor(Color.parseColor("#339900"));
			}else{
				numericView.setPrefix("分摊支出:" + currencySymbol);
//				if(apportionTotal.equals(0.0)){
//					numericView.setTextColor(Color.parseColor("#000000"));
//				}else{
//				numericView.setTextColor(Color.parseColor("#FF0000"));
//				}
			}
			numericView.setSuffix(null);
			numericView.setNumber(apportionTotal);
			return true;
		}
		else if(view.getId() == R.id.memberListItem_settlement) {
			HyjNumericView numericView = (HyjNumericView)view;
			ProjectShareAuthorization projectShareAuthorization = HyjModel.getModel(ProjectShareAuthorization.class, cursor.getString(columnIndex));
			if(!HyjApplication.getInstance().getCurrentUser().getId().equals(projectShareAuthorization.getFriendUserId())){
				ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", projectShareAuthorization.getProjectId(), HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
				if(psa != null && psa.getProjectShareMoneyExpenseOwnerDataOnly() == true){
					numericView.setSuffix(null);
					numericView.setTextColor(Color.BLACK);
					numericView.setPrefix("-");
					numericView.setText(null);
					return true;
				}
			}
			Double settlement = projectShareAuthorization.getSettlement();
			String currencySymbol = projectShareAuthorization.getProject().getCurrencySymbol();
//			TextView labelText = (TextView) ((ViewGroup)view.getParent()).findViewById(R.id.memberListItem_settlement_label);

//			numericView.setPrefix(currencySymbol);
			if(settlement < 0){
				settlement = -settlement;
//				labelText.setText("还要支付");
				numericView.setPrefix("还要支付" + currencySymbol);
				if(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor() != null){
					numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
				}

			}else{
				if(settlement.equals(0.0)){
//					labelText.setText("结余");
					numericView.setPrefix(currencySymbol);
					numericView.setTextColor(Color.parseColor("#000000"));
				}else if(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor() != null){
//					labelText.setText("应该收回");
					numericView.setPrefix("应该收回" + currencySymbol);
					numericView.setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
				 }

			}
			numericView.setSuffix(null);
			numericView.setNumber(settlement);
			return true;
		} else {
			return false;
		}
	}	
	private class ChangeObserver extends ContentObserver {
		AsyncTask<String, Void, String> mTask = null;
		public ChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}
//
//		@Override
//		public void onChange(boolean selfChange, Uri uri) {
//			super.onChange(selfChange, uri);
//		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if(mTask == null){
				mTask = new AsyncTask<String, Void, String>() {
			        @Override
			        protected String doInBackground(String... params) {
						try {
							//等待其他的更新都到齐后再更新界面
							Thread.sleep(200);
						} catch (InterruptedException e) {}
						return null;
			        }
			        @Override
			        protected void onPostExecute(String result) {
						((SimpleCursorAdapter) getListAdapter()).notifyDataSetChanged();
						mTask = null;
			        }
			    };
			    mTask.execute();
			}
		}
	}


	@Override
	public void onDestroy() {
		if (mUserDataChangeObserver != null) {
			this.getActivity().getContentResolver()
					.unregisterContentObserver(mUserDataChangeObserver);
		}
		super.onDestroy();
	}
}
