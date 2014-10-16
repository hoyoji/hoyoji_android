package com.hoyoji.hoyoji.message;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
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
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.MoneyExpenseCategory;

public class MessageListFragment extends HyjUserListFragment{
	
	private ChangeObserver mChangeObserver;
	private Button mAllMessageButton;
	private Button mUnReadMessageButton;
	private boolean mIsSelectUnreadMessages = false;

	@Override
	public Integer useContentView() {
		return R.layout.message_listfragment_message;
	}

	@Override
	public Integer useOptionsMenuView() {
		if(getActivity().getClass().getName().equals("com.hoyoji.hoyoji.MainActivity")){
			return null;
		}
		return R.menu.message_listfragment_message;
	}

	@Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.home_listitem_row,
				null,
				new String[] {"id", "id", "id", "id", "id", "id", "id"}, 
				new int[] {R.id.homeListItem_picture, R.id.homeListItem_subTitle, R.id.homeListItem_title, 
							R.id.homeListItem_remark, R.id.homeListItem_date,
							R.id.homeListItem_amount, R.id.homeListItem_owner},
				0); 
	}	


	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		int offset = arg1.getInt("OFFSET");
		int limit = arg1.getInt("LIMIT");
		if(limit == 0){
			limit = getListPageSize();
		}
		String selection = null;
		String[] selectionArgs = null;
		
		if(mIsSelectUnreadMessages == true){
			selection = "messageState <> ? and messageState <> ?";
			selectionArgs = new String[]{"read","closed"};
		}
		Object loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(Message.class, null),
				new String[]{"_id", "id"}, selection, selectionArgs, "date DESC LIMIT " + (limit + offset)
			);
		
		return (Loader<Object>)loader;
	}
	

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		mAllMessageButton = (Button)getView().findViewById(R.id.messageListFragment_action_all_messgae);
		mAllMessageButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mIsSelectUnreadMessages = false;
				getLoaderManager().restartLoader(0, new Bundle(), MessageListFragment.this);
    		}
		});
		mUnReadMessageButton = (Button)getView().findViewById(R.id.messageListFragment_action_un_read_message);
		mUnReadMessageButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mIsSelectUnreadMessages = true;
				getLoaderManager().restartLoader(0, new Bundle(), MessageListFragment.this);
    		}
		});
		if (mChangeObserver == null) {
			mChangeObserver = new ChangeObserver();
			this.getActivity().getContentResolver().registerContentObserver(ContentProvider.createUri(Message.class, null), true,
					mChangeObserver);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.messageListFragment_action_sync){
			HyjUtil.displayToast("正在检查新消息...");
			Intent startIntent = new Intent(getActivity(), MessageDownloadService.class);
			HyjApplication.getInstance().getApplicationContext().startService(startIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		if(id == -1) {
			 return;
		}
		if(getActivity().getCallingActivity() != null){
			Intent intent = new Intent();
			intent.putExtra("MODEL_ID", id);
			getActivity().setResult(Activity.RESULT_OK, intent);
			getActivity().finish();
		} else {
			Bundle bundle = new Bundle();
			bundle.putLong("MODEL_ID", id);
			Message msg = Message.load(Message.class, id);
			if(msg.getType().equals("System.Friend.AddRequest") ){
				openActivityWithFragment(FriendMessageFormFragment.class, R.string.friendAddRequestMessageFormFragment_title_addrequest, bundle);
			} else if(msg.getType().equals("System.Friend.AddResponse") ){
				openActivityWithFragment(FriendMessageFormFragment.class, R.string.friendAddRequestMessageFormFragment_title_addresponse, bundle);
			} else if(msg.getType().equals("System.Friend.Delete") ){
				openActivityWithFragment(FriendMessageFormFragment.class, R.string.friendAddRequestMessageFormFragment_title_delete, bundle);
			} else if(msg.getType().equals("Project.Share.AddRequest") ){
				openActivityWithFragment(ProjectMessageFormFragment.class, R.string.projectMessageFormFragment_title_addrequest, bundle);
			} else if(msg.getType().equals("Project.Share.Accept") ){
				openActivityWithFragment(ProjectMessageFormFragment.class, R.string.projectMessageFormFragment_title_accept, bundle);
			} else if(msg.getType().equals("Project.Share.Delete") ){
				openActivityWithFragment(ProjectMessageFormFragment.class, R.string.projectMessageFormFragment_title_delete, bundle);
			} else if(msg.getType().equals("Project.Share.Edit") ){
				openActivityWithFragment(ProjectMessageFormFragment.class, R.string.projectMessageFormFragment_title_edit, bundle);
			} else if(msg.getType().startsWith("Money.Share.Add") ){
				openActivityWithFragment(MoneyShareMessageFormFragment.class, msg.getMessageTitle(), bundle, false, null);
			}
		}
    }  
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		Message message = HyjModel.getModel(Message.class, cursor.getString(cursor.getColumnIndex("id")));
		if(view.getId() == R.id.homeListItem_date){
			((HyjDateTimeView)view).setText(message.getDate());
			return true;
		} else if(view.getId() == R.id.homeListItem_title){
			((TextView)view).setText(message.getMessageTitle());
			return true;
		} else if(view.getId() == R.id.homeListItem_subTitle){
			((TextView)view).setText(message.getFromUserDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_amount){
			HyjNumericView numericView = (HyjNumericView)view;
			numericView.setPrefix(null);
			numericView.setSuffix(null);
			numericView.setNumber(null);
			return true;
		} else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			if(message.getMessageState().equalsIgnoreCase("new") 
					|| message.getMessageState().equalsIgnoreCase("unread")){
				imageView.setBackgroundResource(R.drawable.ic_action_unread);
			} else {
				imageView.setBackgroundResource(R.drawable.ic_action_read);
			}
			imageView.setImage(message.getFromUserId());
			return true;
		}  else if(view.getId() == R.id.homeListItem_owner){
			if(message.getToUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
				((TextView)view).setText("");
			} else {
				((TextView)view).setText(message.getToUserDisplayName());
			}
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			try {
				JSONObject messageData = null;
				messageData = new JSONObject(message.getMessageData());
				double amount = 0;
				try{
					amount = messageData.getDouble("amount") * messageData.getDouble("exchangeRate");
				} catch(Exception e) {
					amount = messageData.optDouble("amount");
				}
				java.util.Currency localeCurrency = java.util.Currency
						.getInstance(messageData.optString("currencyCode"));
				String currencySymbol = "";
				currencySymbol = localeCurrency.getSymbol();
				if(currencySymbol.length() == 0){
					currencySymbol = messageData.optString("currencyCode");
				}
						
				((TextView)view).setText(String.format(message.getMessageDetail(), message.getFromUserDisplayName(), currencySymbol, amount));
			} catch (Exception e){
				((TextView)view).setText(message.getMessageDetail());
			}
			return true;
		} else {
			return false;
		}
	}
	
	private class ChangeObserver extends ContentObserver {
//		AsyncTask<String, Void, String> mTask = null;
		public ChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

//		@Override
//		public void onChange(boolean selfChange, Uri uri) {
//			super.onChange(selfChange, uri);
//		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			
			Handler handler = new Handler(Looper.getMainLooper());
			handler.postDelayed(new Runnable() {
				public void run() {
//					CursorAdapter adapter = ((CursorAdapter)getListView().getAdapter());
//					adapter.notifyDataSetChanged();
					((CursorAdapter)getListAdapter()).notifyDataSetChanged();
				}
			}, 50);
		}
	}
	@Override
	public void onDestroy() {
		if (mChangeObserver != null) {
			this.getActivity().getContentResolver()
					.unregisterContentObserver(mChangeObserver);
		}
		super.onDestroy();
	}
}
