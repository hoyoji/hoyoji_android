package com.hoyoji.hoyoji.money.moneycategory;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.MoneyExpenseCategory;

public class MoneyExpenseCategoryListFragment extends HyjUserListFragment implements OnItemClickListener {
	
	ListView childrenList;
	SimpleCursorAdapter childrenListAdapter;
	private View mFooterView;
	long lastSelectedMainCategoryId = AdapterView.INVALID_ROW_ID;
	
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
		Button addMainCategory = new Button(this.getActivity());
		addMainCategory.setText("新增主分类");
		addMainCategory.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				final HyjActivity activity = (HyjActivity) getActivity();
				
				if(activity.mDialogFragment != null){
					activity.mDialogFragment.dismiss();
				}
				
				activity.mDialogCallback = new HyjActivity.DialogCallbackListener() {
					@Override
					public void doPositiveClick(Object bundle) {
//						if(bundle != null){
						Bundle b = (Bundle)bundle;
				    	final String categoryName = b.getString("categoryName");
				    	final MoneyExpenseCategory category = new MoneyExpenseCategory();
				    	category.setParentExpenseCategoryId(null);
				    	category.setName(categoryName);
				    	category.save();
				    	((SimpleCursorAdapter)getListAdapter()).notifyDataSetChanged();
					}
					@Override
					public void doNegativeClick() {
//						activity.displayDialog(R.string.app_action_delete_list_item, R.string.app_confirm_delete, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
//							new DialogCallbackListener() {
//								@Override
//								public void doPositiveClick(Object object) {
//									category.delete();
//								}
//							});
					}
				};
				
				activity.mDialogFragment = MoneyCategoryFormDialogFragment.newInstance("新增主分类", "", 0);
				activity.mDialogFragment.show(activity.getSupportFragmentManager(), "dialog");
			}
		});		
		getListView().addHeaderView(addMainCategory);
		
		childrenList = (ListView)getView().findViewById(R.id.moneyExpenseCategory_list_children);
		childrenList.setFooterDividersEnabled(true);
	    mFooterView = getLayoutInflater(savedInstanceState).inflate(R.layout.list_view_footer_fetch_more, null);
	    mFooterView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				doFetchMore(childrenList,childrenListAdapter.getCount()-1, mListPageSize);
			}
	    });
	    childrenList.addFooterView(mFooterView, null, false);
	   // childrenList.setEmptyView(getView().findViewById(android.R.id.empty));
		this.registerForContextMenu(childrenList);
		childrenListAdapter = (SimpleCursorAdapter) useListViewAdapter();
		childrenListAdapter.setViewBinder(this);
		childrenList.setAdapter(childrenListAdapter); 
		childrenList.setHeaderDividersEnabled(true);
		childrenList.setOnItemClickListener((OnItemClickListener) this);
		Button addChildCategory = new Button(this.getActivity());
		addChildCategory.setText("新增子分类");
		addChildCategory.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				final HyjActivity activity = (HyjActivity) getActivity();
				if(lastSelectedMainCategoryId == AdapterView.INVALID_ROW_ID){
					activity.displayDialog("新增子分类", "请先选择主分类");
					return;
				}
			
				final MoneyExpenseCategory mainCategory = MoneyExpenseCategory.load(MoneyExpenseCategory.class, lastSelectedMainCategoryId);
				
				activity.mDialogCallback = new HyjActivity.DialogCallbackListener() {
					@Override
					public void doPositiveClick(Object bundle) {
//						if(bundle != null){
						Bundle b = (Bundle)bundle;
				    	final String categoryName = b.getString("categoryName");
				    	final MoneyExpenseCategory category = new MoneyExpenseCategory();
				    	category.setParentExpenseCategoryId(mainCategory.getId());
				    	category.setName(categoryName);
				    	category.save();
				    	childrenListAdapter.notifyDataSetChanged();
					}
					@Override
					public void doNegativeClick() {
//						activity.displayDialog(R.string.app_action_delete_list_item, R.string.app_confirm_delete, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
//							new DialogCallbackListener() {
//								@Override
//								public void doPositiveClick(Object object) {
//									category.delete();
//								}
//							});
					}
				};
				
				activity.mDialogFragment = MoneyCategoryFormDialogFragment.newInstance("新增子分类", "", 0);
				activity.mDialogFragment.show(activity.getSupportFragmentManager(), "dialog");
			}
		});
		childrenList.addHeaderView(addChildCategory);
		
	}
	

	public void onInitViewData(){
		super.onInitViewData();
		initLoader(1);
	}
	
	@Override
	public Loader<Object> onCreateLoader(int id, Bundle arg1) {
		Object loader;
		if(id == 0){
			setFooterLoadStart(getListView());
			loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(MoneyExpenseCategory.class, null),
				null, null, null, "name ASC"
			);
		} else {
			setFooterLoadStart(childrenList);
			String selection = null;
			String orderBy = null;
			String[] selectionArgs = null;
			if(arg1 != null){
				selection = "parentExpenseCategoryId=?";
				selectionArgs = new String[]{arg1.getString("parentCategoryId")};
				orderBy = "name DESC";
			} else {
				selection = "parentExpenseCategoryId IS NOT NULL";
//				selectionArgs = new String[]{};
//				orderBy = "parentExpenseCategoryId DESC";
			}
			loader = new CursorLoader(getActivity(),
					ContentProvider.createUri(MoneyExpenseCategory.class, null),
					null, selection, selectionArgs, orderBy
			);
		}
		return (Loader<Object>)loader;
	}

	@Override
	public void setFooterLoadStart(ListView l){
		if(l == getListView()){
			super.setFooterLoadStart(l);
			return;
		}
//        if(childrenList.getItemAtPosition(0) == null){
//        	((TextView)mFooterView).setText(R.string.app_listview_footer_fetching_more);
//        } else {
            ((TextView)mFooterView).setText(R.string.app_listview_footer_fetching_more);
//        }
        ((TextView)mFooterView).setEnabled(false);
	}

	@Override
	public void setFooterLoadFinished(ListView l, int count){
		if(l == getListView()){
			super.setFooterLoadFinished(l, count);
			return;
		}
        ((TextView)mFooterView).setEnabled(true);
        ((TextView)mFooterView).setText(R.string.app_listview_no_content);
		if(count >= mListPageSize){
	        ((TextView)mFooterView).setText(R.string.app_listview_footer_fetch_more);
		} else {
		    ((TextView)mFooterView).setText(R.string.app_listview_footer_fetch_no_more);
		}
	}

	@Override
	public void onLoadFinished(Loader<Object> loader, Object cursor) {
		int count = 0;
        if(cursor != null){
	        count = ((Cursor) cursor).getCount();
        }
        if(loader.getId() == 0){
			super.onLoadFinished(loader, cursor);
	        setFooterLoadFinished(getListView(), count);
		} else {
			childrenListAdapter.swapCursor((Cursor)cursor);
	        setFooterLoadFinished(childrenList, count);
//	        ((SimpleCursorAdapter)getListAdapter()).notifyDataSetChanged();
		}
		
		// The list should now be shown. 
        if (isResumed()) {
          //  setListShown(true);  
        } else {  
          //  setListShownNoAnimation(true);  
        } 
	}
	
	
	@Override
	public void onLoaderReset(Loader<Object> loader) {
		if(loader.getId() == 0){
			super.onLoaderReset(loader);
		} else {
			childrenListAdapter.swapCursor(null);
		}
	}	

	@Override
	public void doFetchMore(ListView l, int offset, int pageSize){
	}
		
	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		super.onListItemClick(l, v, position, id);
		if(lastSelectedMainCategoryId != AdapterView.INVALID_ROW_ID){
			for(int i=l.getFirstVisiblePosition(); i <= l.getLastVisiblePosition(); i++){
				if(l.getItemIdAtPosition(i) == lastSelectedMainCategoryId){
					l.getChildAt(i-l.getFirstVisiblePosition()).setBackground(null);
					break;
				}
			}
		}
		lastSelectedMainCategoryId = id;
		v.setBackgroundResource(R.drawable.abc_tab_selected_focused_holo);
		float px = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 10,
				this.getResources().getDisplayMetrics());
		v.setPadding((int)px, 0, 0, 0);
		
		if(getActivity().getCallingActivity() != null){
			Bundle bundle = new Bundle();
			MoneyExpenseCategory moneyExpenseCategory = MoneyExpenseCategory.load(MoneyExpenseCategory.class, id);
			bundle.putString("parentCategoryId", moneyExpenseCategory.getId());
			Loader<Object> loader = getLoaderManager().getLoader(1);
		    if (loader != null && !loader.isReset() ) { 
		    	getLoaderManager().restartLoader(1, bundle, this);
		    } else {
		    	getLoaderManager().initLoader(1, bundle, this);
		    }
		} else {
			Bundle bundle = new Bundle();
			bundle.putLong("MODEL_ID", id);
	//		Message msg = Message.load(Message.class, id);
	//		if(msg.getType().equals("System.Friend.AddRequest") ){
	//			openActivityWithFragment(FriendMessageFormFragment.class, R.string.friendAddRequestMessageFormFragment_title_addrequest, bundle);
	//		} else if(msg.getType().equals("System.Friend.AddResponse") ){
	//			openActivityWithFragment(FriendMessageFormFragment.class, R.string.friendAddRequestMessageFormFragment_title_addresponse, bundle);
	//		} else if(msg.getType().equals("System.Friend.Delete") ){
	//			openActivityWithFragment(FriendMessageFormFragment.class, R.string.friendAddRequestMessageFormFragment_title_delete, bundle);
	//		} else if(msg.getType().equals("Project.Share.AddRequest") ){
	//			openActivityWithFragment(ProjectMessageFormFragment.class, R.string.projectMessageFormFragment_title_addrequest, bundle);
	//		} else if(msg.getType().equals("Project.Share.Accept") ){
	//			openActivityWithFragment(ProjectMessageFormFragment.class, R.string.projectMessageFormFragment_title_accept, bundle);
	//		} else if(msg.getType().equals("Project.Share.Delete") ){
	//			openActivityWithFragment(ProjectMessageFormFragment.class, R.string.projectMessageFormFragment_title_delete, bundle);
	//		}
		}
		
    }

	@Override
	public void onItemClick(AdapterView<?> l, View v, int position, long id) {
		if(getActivity().getCallingActivity() != null){
				Intent intent = new Intent();
				intent.putExtra("MODEL_ID", id);
				getActivity().setResult(Activity.RESULT_OK, intent);
				getActivity().finish();
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
	
//	@Override
//	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
//		MoneyExpenseCategory category = HyjModel.getModel(MoneyExpenseCategory.class, cursor.getString(cursor.getColumnIndex("id")));
//		if(cursor.getLong(cursor.getColumnIndex("_id")) == selectedMainCategoryId){
//			
//			return true;
//		}
//		return false;
//	}
}
