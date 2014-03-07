package com.hoyoji.hoyoji.money.moneycategory;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.server.HyjJSONListAdapter;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyExpenseCategory;

public class MoneyExpenseCategoryListFragment extends HyjUserListFragment implements OnItemClickListener {
	
	ListView childrenList;
	SimpleCursorAdapter childrenListAdapter;
	protected View mFooterView;
	
	@Override
	public Integer useContentView() {
		return R.layout.moneyexpensecategory_listfragment_moneyexpensecategory;
	}

	@Override
	public Integer useOptionsMenuView() {
		return R.menu.moneyexpensecategory_listfragment_moneyexpensecategory;
	}

	@Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.moneycategory_listitem_moneycategory,
				null,
				new String[] {"name"}, 
				new int[] {R.id.moneyCategoryListitem_name, },
				0); 
	}	

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		getListView().setSelector(R.drawable.abc_tab_selected_focused_holo);
		getListView().setBackgroundColor(Color.LTGRAY);
		
		childrenList = (ListView)getView().findViewById(R.id.moneyExpenseCategory_list_children);
		childrenList.setFooterDividersEnabled(true);
	    mFooterView = getLayoutInflater(savedInstanceState).inflate(R.layout.list_view_footer_fetch_more, null);
	    mFooterView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				HyjUtil.displayToast("hello");
			}
	    });
	    
	    childrenList.addFooterView(mFooterView, null, false);
	    childrenList.setEmptyView(getView().findViewById(android.R.id.empty));
		this.registerForContextMenu(childrenList);
		childrenListAdapter = (SimpleCursorAdapter) useListViewAdapter();
		childrenListAdapter.setViewBinder(this);
		childrenList.setAdapter(childrenListAdapter); 
		childrenList.setHeaderDividersEnabled(true);
		childrenList.setOnItemClickListener((OnItemClickListener) this);
		
	}
	

	public void onInitViewData(){
		super.onInitViewData();
		initLoader(1);
	}
	
	@Override
	public Loader<Object> onCreateLoader(int id, Bundle arg1) {
		setFooterLoadStart();
		Object loader;
		if(id == 0){
			loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(MoneyExpenseCategory.class, null),
				null, null, null, "name ASC"
			);
		} else {
			loader = new CursorLoader(getActivity(),
					ContentProvider.createUri(MoneyExpenseCategory.class, null),
					null, null, null, "name DESC"
			);
		}
		return (Loader<Object>)loader;
	}

	public void setFooterLoadStart(){
        if(getListView().getItemAtPosition(0) == null){
        	((TextView)getListView().getEmptyView()).setText(R.string.app_listview_footer_fetching_more);
        } else {
            ((TextView)mFooterView).setText(R.string.app_listview_footer_fetching_more);
        }
        ((TextView)mFooterView).setEnabled(false);
	}
	
	public void setFooterLoadFinished(int count){
        ((TextView)mFooterView).setEnabled(true);
        ((TextView)getListView().getEmptyView()).setText(R.string.app_listview_no_content);
		if(count >= mListPageSize){
	        ((TextView)mFooterView).setText(R.string.app_listview_footer_fetch_more);
		} else {
		    ((TextView)mFooterView).setText(R.string.app_listview_footer_fetch_no_more);
		}
	}

	@Override
	public void onLoadFinished(Loader<Object> loader, Object cursor) {
		if(loader.getId() == 0){
			if(this.getListAdapter() instanceof CursorAdapter){
				((SimpleCursorAdapter) this.getListAdapter()).swapCursor((Cursor)cursor);
			}
		} else {
			childrenListAdapter.swapCursor((Cursor)cursor);
		}
		
		// The list should now be shown. 
        if (isResumed()) {
          //  setListShown(true);  
        } else {  
          //  setListShownNoAnimation(true);  
        } 
        int count = 0;
        if(cursor != null){
	        if(cursor instanceof Cursor){
	        	count = ((Cursor) cursor).getCount();
	        } else if(cursor instanceof List){
	        	count = ((List)cursor).size();
	        }
        }
        setFooterLoadFinished(count);
	}
	
	
	@Override
	public void onLoaderReset(Loader<Object> loader) {
		if(loader.getId() == 0){
			if(this.getListAdapter() instanceof CursorAdapter){
				((SimpleCursorAdapter) this.getListAdapter()).swapCursor(null);
			}
		} else {
			childrenListAdapter.swapCursor(null);
		}
	}	

	
	public void doFetchMore(int offset, int pageSize){
	}
		
	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		super.onListItemClick(l, v, position, id);
		
		if(v == mFooterView){
			doFetchMore(l.getAdapter().getCount()-1, this.mListPageSize);
		}
		if(getActivity().getCallingActivity() != null){
			if(l != childrenList){
				return;
			} else {
				Intent intent = new Intent();
				intent.putExtra("MODEL_ID", id);
				getActivity().setResult(Activity.RESULT_OK, intent);
				getActivity().finish();
			}
		} else {
			Bundle bundle = new Bundle();
			bundle.putLong("MODEL_ID", id);
//			Message msg = Message.load(Message.class, id);
//			if(msg.getType().equals("System.Friend.AddRequest") ){
//				openActivityWithFragment(FriendMessageFormFragment.class, R.string.friendAddRequestMessageFormFragment_title_addrequest, bundle);
//			} else if(msg.getType().equals("System.Friend.AddResponse") ){
//				openActivityWithFragment(FriendMessageFormFragment.class, R.string.friendAddRequestMessageFormFragment_title_addresponse, bundle);
//			} else if(msg.getType().equals("System.Friend.Delete") ){
//				openActivityWithFragment(FriendMessageFormFragment.class, R.string.friendAddRequestMessageFormFragment_title_delete, bundle);
//			} else if(msg.getType().equals("Project.Share.AddRequest") ){
//				openActivityWithFragment(ProjectMessageFormFragment.class, R.string.projectMessageFormFragment_title_addrequest, bundle);
//			} else if(msg.getType().equals("Project.Share.Accept") ){
//				openActivityWithFragment(ProjectMessageFormFragment.class, R.string.projectMessageFormFragment_title_accept, bundle);
//			} else if(msg.getType().equals("Project.Share.Delete") ){
//				openActivityWithFragment(ProjectMessageFormFragment.class, R.string.projectMessageFormFragment_title_delete, bundle);
//			}
		}
    }

	@Override
	public void onItemClick(AdapterView<?> l, View v, int position, long id) {
		this.onListItemClick((ListView) l, v, position, id);
	}  
	
//	@Override
//	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
//		MoneyExpenseCategory category = HyjModel.getModel(MoneyExpenseCategory.class, cursor.getString(cursor.getColumnIndex("id")));
//		if(view.getId() == R.id.moneyCategoryListItem_name){
//			((HyjDateTimeView)view).setText(category.getName());
//			return true;
//		} else {
//			return false;
//		}
//	}
}
