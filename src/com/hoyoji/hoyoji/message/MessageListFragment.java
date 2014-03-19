package com.hoyoji.hoyoji.message;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.MoneyAccount;

public class MessageListFragment extends HyjUserListFragment{
	
	@Override
	public Integer useContentView() {
		return R.layout.message_listfragment_message;
	}

	@Override
	public Integer useOptionsMenuView() {
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

		Object loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(Message.class, null),
				new String[]{"_id", "id"}, null, null, "date DESC"
			);
		
		return (Loader<Object>)loader;
	}


	@Override
	public void onInitViewData() {
		super.onInitViewData();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
			if(message.getMessageState().equalsIgnoreCase("new")){
				imageView.setBackgroundResource(R.drawable.ic_action_email);
			} else {
				imageView.setBackgroundResource(R.drawable.ic_action_read);
			}
			imageView.setImage(message.getFromUserId());
			return true;
		}  else if(view.getId() == R.id.homeListItem_owner){
			((TextView)view).setText(message.getToUserDisplayName());
			return true;
		} else if(view.getId() == R.id.homeListItem_remark){
			((TextView)view).setText(message.getMessageDetail());
			return true;
		} else {
			return false;
		}
	}
}
