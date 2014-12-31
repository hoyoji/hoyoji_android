package com.hoyoji.hoyoji.event;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.activeandroid.Model;
import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.models.Event;
import com.hoyoji.hoyoji.models.EventMember;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.User;

public class EventMemberListFragment extends HyjUserListFragment {
//	private IWXAPI api;
//	private QQShare mQQShare = null;
//	public static QQAuth mQQAuth;
	private Button mAllEventMember;
	private Button mSignUpEventMember;
	private Button mUnSignInEventMember;
	private Button mSignInEventMember;
	private boolean mIsSelectSignUpEventMembers = false;
	private boolean mIsSelectUnSignInEventMembers = false;
	private boolean mIsSelectSignInEventMembers = false;

	@Override
	public Integer useContentView() {
		return R.layout.project_listfragment_event_member;
	}
	
	@Override
	public Integer useToolbarView() {
		return super.useToolbarView();
	}
	
	@Override
	public Integer useOptionsMenuView() {
		return R.menu.project_listfragment_event_member;
	}

	@Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.home_listitem_row,
				null,
				new String[] {"friendUserId", "id", "id", "id", "id"},
				new int[] {R.id.homeListItem_picture, R.id.homeListItem_title, R.id.homeListItem_subTitle, R.id.homeListItem_amount, R.id.homeListItem_remark},
				0); 
	}	

	@Override
	public Integer useMultiSelectMenuView() {
		return R.menu.project_listfragment_eventmember_multi_select;
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
		Event event;
		String eventId = null;
		if(getActivity().getCallingActivity() != null){
//			Project project =  Model.load(Project.class, modelId);
//			event = new Select().from(Event.class).where("projectId=?", project.getId()).executeSingle();
			eventId = intent.getStringExtra("EVENTID");
		} else {
			event =  Model.load(Event.class, modelId);
			eventId = event.getId();
		}
		String selection = "eventId = ?";
		String[] selectionArgs = new String[]{eventId};
		
		if(mIsSelectSignUpEventMembers == true){
			selection = selection + " and state = ?";
			selectionArgs = new String[]{eventId,"SignUp"};
		}
		
		if(mIsSelectUnSignInEventMembers == true){
			selection = selection + " and state = ?";
			selectionArgs = new String[]{eventId,"UnSignIn"};
		}
		
		if(mIsSelectSignInEventMembers == true){
			selection = selection + " and state = ?";
			selectionArgs = new String[]{eventId,"SignIn"};
		}
		Object loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(EventMember.class, null),
				null,
				selection, 
				selectionArgs, 
				"friendUserName LIMIT " + (limit + offset) 
			);
		return (Loader<Object>)loader;
	}


	@Override
	public void onInitViewData() {
		super.onInitViewData();
//		mQQAuth = QQAuth.createInstance(AppConstants.TENTCENT_CONNECT_APP_ID, getActivity());
//		mQQShare = new QQShare(getActivity(), mQQAuth.getQQToken());
		
		mAllEventMember = (Button)getView().findViewById(R.id.eventMemberListFragment_action_all_event_member);
		mSignUpEventMember = (Button)getView().findViewById(R.id.eventMemberListFragment_action_sign_up_member);
		mUnSignInEventMember = (Button)getView().findViewById(R.id.eventMemberListFragment_action_un_sign_in_member);
		mSignInEventMember = (Button)getView().findViewById(R.id.eventMemberListFragment_action_sign_in_member);
		
		mAllEventMember.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mIsSelectSignUpEventMembers = false;
				mIsSelectSignInEventMembers = false;
				mIsSelectUnSignInEventMembers = false;
				getLoaderManager().restartLoader(0, new Bundle(), EventMemberListFragment.this);
				mAllEventMember.setBackgroundColor(getResources().getColor(R.color.hoyoji_red));
				mAllEventMember.setTextColor(Color.WHITE);
				mSignUpEventMember.setBackgroundColor(Color.TRANSPARENT);
				mSignUpEventMember.setTextColor(Color.BLACK);
				mUnSignInEventMember.setBackgroundColor(Color.TRANSPARENT);
				mUnSignInEventMember.setTextColor(Color.BLACK);
				mSignInEventMember.setBackgroundColor(Color.TRANSPARENT);
				mSignInEventMember.setTextColor(Color.BLACK);
    		}
		});
		mAllEventMember.setBackgroundColor(getResources().getColor(R.color.hoyoji_red));
		mAllEventMember.setTextColor(Color.WHITE);
		
		mSignUpEventMember.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mIsSelectSignUpEventMembers = true;
				mIsSelectUnSignInEventMembers = false;
				mIsSelectSignInEventMembers = false;
				getLoaderManager().restartLoader(0, new Bundle(), EventMemberListFragment.this);
				mSignUpEventMember.setBackgroundColor(getResources().getColor(R.color.hoyoji_red));
				mSignUpEventMember.setTextColor(Color.WHITE);
				mAllEventMember.setBackgroundColor(Color.TRANSPARENT);
				mAllEventMember.setTextColor(Color.BLACK);
				mUnSignInEventMember.setBackgroundColor(Color.TRANSPARENT);
				mUnSignInEventMember.setTextColor(Color.BLACK);
				mSignInEventMember.setBackgroundColor(Color.TRANSPARENT);
				mSignInEventMember.setTextColor(Color.BLACK);
    		}
		});
		
		mUnSignInEventMember.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mIsSelectSignUpEventMembers = false;
				mIsSelectSignInEventMembers = false;
				mIsSelectUnSignInEventMembers = true;
				getLoaderManager().restartLoader(0, new Bundle(), EventMemberListFragment.this);
				mUnSignInEventMember.setBackgroundColor(getResources().getColor(R.color.hoyoji_red));
				mUnSignInEventMember.setTextColor(Color.WHITE);
				mSignUpEventMember.setBackgroundColor(Color.TRANSPARENT);
				mSignUpEventMember.setTextColor(Color.BLACK);
				mAllEventMember.setBackgroundColor(Color.TRANSPARENT);
				mAllEventMember.setTextColor(Color.BLACK);
				mSignInEventMember.setBackgroundColor(Color.TRANSPARENT);
				mSignInEventMember.setTextColor(Color.BLACK);
    		}
		});
		
		mSignInEventMember.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mIsSelectSignUpEventMembers = false;
				mIsSelectUnSignInEventMembers = false;
				mIsSelectSignInEventMembers = true;
				getLoaderManager().restartLoader(0, new Bundle(), EventMemberListFragment.this);
				mSignInEventMember.setBackgroundColor(getResources().getColor(R.color.hoyoji_red));
				mSignInEventMember.setTextColor(Color.WHITE);
				mSignUpEventMember.setBackgroundColor(Color.TRANSPARENT);
				mSignUpEventMember.setTextColor(Color.BLACK);
				mAllEventMember.setBackgroundColor(Color.TRANSPARENT);
				mAllEventMember.setTextColor(Color.BLACK);
				mUnSignInEventMember.setBackgroundColor(Color.TRANSPARENT);
				mUnSignInEventMember.setTextColor(Color.BLACK);
    		}
		});
	}
	
	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		if(l.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE){
			return;
		}
		if(id == -1) {
			 return;
		}
		if(getActivity().getCallingActivity() != null){
			Intent intent = new Intent();
			intent.putExtra("MODEL_ID", id);
			intent.putExtra("MODEL_TYPE", "EventMember");
			
			getActivity().setResult(Activity.RESULT_OK, intent);
			getActivity().finish();
		} else {
			Bundle bundle = new Bundle();
			bundle.putLong("MODEL_ID", id);
			EventMember memberToBeDetermined = EventMember.load(EventMember.class, id);

			bundle.putLong("project_id", memberToBeDetermined.getProject().get_mId());
			if(memberToBeDetermined.getFriend() != null){
				bundle.putLong("friend_id", memberToBeDetermined.getFriend().get_mId());
			} else if(memberToBeDetermined.getFriendUserId() != null){
				bundle.putString("friendUserId", memberToBeDetermined.getFriendUserId());
			}  else if(memberToBeDetermined.getLocalFriendId() != null){
				bundle.putString("localFriendId", memberToBeDetermined.getLocalFriendId());
			} 
			Long modelId = getActivity().getIntent().getLongExtra("MODEL_ID", -1);
			if(modelId != -1){
				bundle.putLong("event_id", modelId);
			}
			if(memberToBeDetermined.getOwnerUserId().equalsIgnoreCase(HyjApplication.getInstance().getCurrentUser().getId()) 
					&& memberToBeDetermined.getToBeDetermined()){
				openActivityWithFragment(EventMemberTBDViewPagerFragment.class, R.string.memberTBDFormFragment_title_split, bundle);
			} else {
				openActivityWithFragment(EventMemberViewPagerFragment.class, R.string.projectEventMemberFormFragment_title_edit, bundle);
			}
		}
	}
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(view.getId() == R.id.homeListItem_picture){
			String userId = cursor.getString(columnIndex);
			HyjImageView imageView = (HyjImageView)view;
			imageView.setDefaultImage(R.drawable.ic_action_person_white);
			if(cursor.getString(columnIndex) != null){
				User user = HyjModel.getModel(User.class, userId);
				if(user != null){
					imageView.setImage(user.getPictureId());
				} else {
					imageView.setImage((Picture)null);
				}
				if(HyjApplication.getInstance().getCurrentUser().getId().equals(userId)){
					imageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_red));
				} else {
					imageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_green));
				}
			} else {
				imageView.setImage((Picture)null);
				imageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_yellow));
			}
			return true;
		}else if(view.getId() == R.id.homeListItem_title){
			EventMember em = HyjModel.getModel(EventMember.class, cursor.getString(columnIndex));
			if("".equals(em.getNickName()) || em.getNickName() == null){
				((TextView)view).setText(em.getFriendDisplayName());
			} else {
				((TextView)view).setText(em.getNickName());
			}
			return true;
		}else if(view.getId() == R.id.homeListItem_subTitle){
			EventMember evtMember = HyjModel.getModel(EventMember.class, cursor.getString(columnIndex));
			if(!evtMember.getToBeDetermined()){
				if("SignUp".equals(evtMember.getState())){
					((TextView)view).setText("已报名");
				} else if("SignIn".equals(evtMember.getState())){
					((TextView)view).setText("已签到");
				} else{
					((TextView)view).setText("未报名");
				}
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_amount) {
			EventMember evtMember = HyjModel.getModel(EventMember.class, cursor.getString(columnIndex));
			HyjNumericView numericView = (HyjNumericView)view;
			Double apportionTotal = evtMember.getApportionTotal();
			String currencySymbol = evtMember.getEvent().getProject().getCurrencySymbol();
			if(apportionTotal < 0){
				apportionTotal = -apportionTotal;
				numericView.setPrefix("活动收入:" + currencySymbol);
//				numericView.setTextColor(Color.parseColor("#339900"));
			}else{
				if(apportionTotal.equals(0.0)){
//					numericView.setTextColor(Color.BLACK);
					numericView.setPrefix(currencySymbol);
				}else{
//					numericView.setTextColor(Color.parseColor(R.color.));
					numericView.setPrefix("活动支出:" + currencySymbol);
				}
			} 
			numericView.setSuffix(null);
			numericView.setNumber(apportionTotal);
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			EventMember evtMember = HyjModel.getModel(EventMember.class, cursor.getString(columnIndex));
			if(evtMember.getToBeDetermined()){
				((TextView)view).setText("可进行账务拆分");
			} else {
				((TextView)view).setText("");
			}
		}
		return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		Event event = Event.load(Event.class, modelId);
		if(!event.getProject().getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
			HyjUtil.displayToast("您不能在共享来的账本添加活动成员");
			return true;
		}
		if(item.getItemId() == R.id.projectEventMemberListFragment_invite_friend){
			Bundle bundle = new Bundle();
			bundle.putLong("EVENTID", modelId);
			bundle.putString("DIALOG_TYPE", "invite");
			EventMemberDialogFragment.newInstance(bundle).show(getActivity().getSupportFragmentManager(), "EventMemberDialogFragment");
			return true;
		} else if(item.getItemId() == R.id.projectEventMemberListFragment_signIn_friend){
			Bundle bundle = new Bundle();
			bundle.putLong("EVENTID", modelId);
			bundle.putString("DIALOG_TYPE", "signIn");
			EventMemberDialogFragment.newInstance(bundle).show(getActivity().getSupportFragmentManager(), "EventMemberDialogFragment");
			return true;
		} else if(item.getItemId() == R.id.projectEventMemberListFragment_action_add){
			Bundle bundle = new Bundle();
			bundle.putLong("EVENT_ID", modelId);
			openActivityWithFragment(EventMemberFormFragment.class, R.string.projectEventMemberFormFragment_action_addnew, bundle);
			return true;
		} 
//		else if(item.getItemId() == R.id.projectEventMemberListFragment_action_member_invite){
//			inviteFriend("Other", event, event.getName(), "invite");
//			return true;
//		} else if(item.getItemId() == R.id.projectEventMemberListFragment_action_member_invite_wxFriend){
//			inviteFriend("WX", event, event.getName(), "invite");
//			return true;
//		} else if(item.getItemId() == R.id.projectEventMemberListFragment_action_member_invite_qqFriend){
//			inviteFriend("QQ", event, event.getName(), "invite");
//			return true;
//		} else if(item.getItemId() == R.id.projectEventMemberListFragment_action_member_signIn){
//			inviteFriend("Other", event, event.getName(), "signIn");
//			return true;
//		} else if(item.getItemId() == R.id.projectEventMemberListFragment_action_member_signIn_wxFriend){
//			inviteFriend("WX", event, event.getName(), "signIn");
//			return true;
//		} else if(item.getItemId() == R.id.projectEventMemberListFragment_action_member_signIn_qqFriend){
//			inviteFriend("QQ", event, event.getName(), "signIn");
//			return true;
//		}
//		else if(item.getItemId() == R.id.projectEventMemberListFragment_action_member_edit){
//			Bundle bundle = new Bundle();
//			bundle.putLong("MODEL_ID", modelId);
//			openActivityWithFragment(ProjectEventFormFragment.class, R.string.projectEventFormFragment_title_edit, bundle);
//			return true;
//		} 
		else if(item.getItemId() == R.id.projectEventMemberListFragment_action_setUnSignUp){
			setUnSignUpEventMembers();
			this.exitMultiChoiceMode(getListView());
			return true;
		} else if(item.getItemId() == R.id.projectEventMemberListFragment_action_setSignUp){
			setSignUpEventMembers();
			this.exitMultiChoiceMode(getListView());
			return true;
		} else if(item.getItemId() == R.id.projectEventMemberListFragment_action_setSignIn){
			setSignInEventMembers();
			this.exitMultiChoiceMode(getListView());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
//	public void inviteFriend(final String way, Event event,final String event_name, final String type) {
//		((HyjActivity) getActivity()).displayProgressDialog(R.string.friendListFragment__action_invite_title,R.string.friendListFragment__action_invite_content);
//		String emTitle = null;
//		String emDescription = null;
//		if(type.equals("invite")){
//			emTitle = "邀请参加活动";
//			emDescription = HyjApplication.getInstance().getCurrentUser().getDisplayName() + " 邀请您参加活动    " +event_name;
//		} else if(type.equals("signIn")){
//			emTitle = "活动签到";
//			emDescription = HyjApplication.getInstance().getCurrentUser().getDisplayName() + " 邀请您进行活动    " +event_name +"签到";
//		}
//		final String emTitleSent = emTitle;
//		final String emDescriptionSent = emDescription;
//		
//		JSONObject inviteFriendObject = new JSONObject();
//		final String id = UUID.randomUUID().toString();
//		try {
//			inviteFriendObject.put("id", id);
//			inviteFriendObject.put("data", event.toJSON().toString());
//			inviteFriendObject.put("__dataType", "InviteLink");
//			inviteFriendObject.put("title", emTitle);
//			inviteFriendObject.put("type", "EventMember");
//			inviteFriendObject.put("date", (new Date()).getTime());
//			inviteFriendObject.put("description", emDescription);
//			inviteFriendObject.put("state", "Open");
//		} catch (JSONException e1) {
//			e1.printStackTrace();
//		}
//   	 
//   	// 从服务器上下载用户数据
//		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
//			@Override
//			public void finishCallback(Object object) {
//				((HyjActivity) getActivity()).dismissProgressDialog();
//				String linkUrl = null;
//				if(type.equals("invite")){
//					linkUrl = HyjApplication.getInstance().getServerUrl()+"m/invite.html?id=" + id;
//				} else if(type.equals("signIn")){
//					linkUrl = HyjApplication.getInstance().getServerUrl()+"m/eventSignIn.html?id=" + id;
//				}
//					if(way.equals("Other")){
//						inviteOtherFriend(linkUrl, event_name, emTitleSent, emDescriptionSent);
//					} else if(way.equals("WX")){
//						inviteWXFriend(linkUrl, event_name, emTitleSent, emDescriptionSent);
//					} else if(way.equals("QQ")){
//						inviteQQFriend(linkUrl, event_name, emTitleSent, emDescriptionSent);
//					}
//			}
//
//			@Override
//			public void errorCallback(Object object) {
//				((HyjActivity) getActivity()).dismissProgressDialog();
//				try {
//					JSONObject json = (JSONObject) object;
//					((HyjActivity) getActivity()).displayDialog(null,
//							json.getJSONObject("__summary")
//									.getString("msg"));
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		};
//   	 
//   	 	HyjHttpPostAsyncTask.newInstance(serverCallbacks, "[" + inviteFriendObject.toString() + "]", "postData");
//	 }
//
//	public void inviteOtherFriend(String linkUrl, String event_name, String emTitleSent, String emDescriptionSent) {
//		Intent intent=new Intent(Intent.ACTION_SEND);   
//        intent.setType("text/plain");   
//        
////      File f;
////		try {
////			f = HyjUtil.createImageFile("invite_friend", "PNG");
////			if(!f.exists()){
////		        Bitmap bmp = HyjUtil.getCommonBitmap(R.drawable.invite_friend);
////			    FileOutputStream out;
////				out = new FileOutputStream(f);
////				bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
////				out.close();
////			}
////	        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
//
////		} catch (IOException e) {
////			e.printStackTrace();
////		}
//	        intent.putExtra(Intent.EXTRA_TITLE, emTitleSent);  
//	        intent.putExtra(Intent.EXTRA_SUBJECT, emTitleSent);   
//	        intent.putExtra(Intent.EXTRA_TEXT, HyjApplication.getInstance().getCurrentUser().getDisplayName() + emTitleSent +event_name+"。\n\n" + linkUrl);  
//	        
//	        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
//	        startActivity(Intent.createChooser(intent, emTitleSent)); 
//	}
//	
//	public void inviteWXFriend(String linkUrl,String event_name, String emTitleSent, String emDescriptionSent) {
//		api = WXAPIFactory.createWXAPI(getActivity(), AppConstants.WX_APP_ID);
//		WXWebpageObject webpage = new WXWebpageObject();
//		webpage.webpageUrl = linkUrl;
//		WXMediaMessage msg = new WXMediaMessage(webpage);
//		msg.title = emTitleSent;
//		msg.description = emDescriptionSent;
//		Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
//		msg.thumbData = Util.bmpToByteArray(thumb, true);
//		
//		SendMessageToWX.Req req = new SendMessageToWX.Req();
//		req.transaction = buildTransaction("webpage");
//		req.message = msg;
////		req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
//		api.sendReq(req);
//		
//	}
//	
//	private String buildTransaction(final String type) {
//		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
//	}
//	
//	public void inviteQQFriend(String linkUrl,String event_name, String emTitleSent, String emDescriptionSent) {
//		final Bundle params = new Bundle();
//	    params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
//	    params.putString(QQShare.SHARE_TO_QQ_TITLE, emTitleSent);
//	    params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  emDescriptionSent);
//	    params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  linkUrl);
//	    params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, HyjApplication.getInstance().getServerUrl() + "imgs/invite_friend.png");
//	    params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  "好友AA记账");
////	    params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,  "其他附加功能");		
//	    mQQShare.shareToQQ(getActivity(), params, new BaseUIListener(getActivity()) {
//
//            @Override
//            public void onCancel() {
////            		Util.toastMessage(getActivity(), "onCancel: ");
//            }
//
//            @Override
//            public void onComplete(Object response) {
////                Util.toastMessage(getActivity(), "onComplete: " + response.toString());
//            }
//
//            @Override
//            public void onError(UiError e) {
////                Util.toastMessage(getActivity(), "onError: " + e.errorMessage, "e");
//            }
//
//        });
//	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		String eventId = intent.getStringExtra("EVENTID");
		Event event = null;
		if(eventId != null){
			event = Event.getModel(Event.class, eventId);
		} else {
			event = Event.load(Event.class, modelId);
		}
		if(!event.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId()) && getOptionsMenu().findItem(R.id.projectEventMemberListFragment_action_member_add) != null){
			getOptionsMenu().findItem(R.id.projectEventMemberListFragment_action_member_add).setVisible(false);
		}
	}

	private void setUnSignUpEventMembers() {
		long[] ids = this.getListView().getCheckedItemIds();
		if(ids.length == 0){
			HyjUtil.displayToast("请选择至少一个活动成员");
			return;
		}
		for(int i=0; i<ids.length; i++){
			EventMember em = Model.load(EventMember.class, ids[i]);
			if(em != null){
				em.setState("UnSignUp");
				em.save();
			}
		}
		
	}
	
	private void setSignUpEventMembers() {
		long[] ids = this.getListView().getCheckedItemIds();
		if(ids.length == 0){
			HyjUtil.displayToast("请选择至少一个活动成员");
			return;
		}
		for(int i=0; i<ids.length; i++){
			EventMember em = Model.load(EventMember.class, ids[i]);
			if(em != null){
				em.setState("SignUp");
				em.save();
			}
		}
		
	}
	
	private void setSignInEventMembers() {
		long[] ids = this.getListView().getCheckedItemIds();
		if(ids.length == 0){
			HyjUtil.displayToast("请选择至少一个活动成员");
			return;
		}
		for(int i=0; i<ids.length; i++){
			EventMember em = Model.load(EventMember.class, ids[i]);
			if(em != null){
				em.setState("SignIn");
				em.save();
			}
		}
		
	}
	
}
