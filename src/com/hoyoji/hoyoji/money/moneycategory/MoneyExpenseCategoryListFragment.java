package com.hoyoji.hoyoji.money.moneycategory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjSimpleCursorAdapter;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.MoneyExpenseCategory;

public class MoneyExpenseCategoryListFragment extends HyjUserListFragment implements 
	OnItemClickListener,  OnItemLongClickListener {
	
	ListView childrenList;
	SimpleCursorAdapter childrenListAdapter;
	private View mFooterView;
	private TextView mFrecentCategory;
	private long lastSelectedMainCategoryId = AdapterView.INVALID_ROW_ID;
	
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
		return new MainCategorySimpleCursorAdapter(getActivity(),
				R.layout.moneycategory_listitem_moneycategory,
				null,
				new String[] {"name"}, 
				new int[] {R.id.moneyCategoryListitem_name, },
				0); 
	}	

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		getListView().setOnItemLongClickListener(this);
		
		mFrecentCategory = (TextView)getLayoutInflater(savedInstanceState).inflate(R.layout.moneycategory_listitem_moneycategory, null);
		mFrecentCategory.setText("最近使用");
		float px = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 48,
				getView().getResources().getDisplayMetrics());
		mFrecentCategory.setHeight((int)px);


		mFrecentCategory.setBackgroundResource(R.drawable.abc_tab_selected_focused_holo);
		px = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 10,
				this.getResources().getDisplayMetrics());
		mFrecentCategory.setPadding((int)px, 0, 0, 0);
		
		getListView().addHeaderView(mFrecentCategory);
		
		View mainCategoryPanel = getView().findViewById(R.id.moneyExpenseCategory_list_panel_mainCategory);
		mainCategoryPanel.setBackgroundColor(Color.LTGRAY);
		View addMainCategory = getView().findViewById(R.id.moneyExpenseCategory_list_button_addMainCategory);
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
						Bundle b = (Bundle)bundle;
				    	final String categoryName = b.getString("categoryName");
				    	final MoneyExpenseCategory category = new MoneyExpenseCategory();
				    	category.setParentExpenseCategoryId(null);
				    	category.setName(categoryName);
				    	category.save();
					}
				};
				
				activity.mDialogFragment = MoneyCategoryFormDialogFragment.newInstance("新增主分类", "", 0);
				activity.mDialogFragment.show(activity.getSupportFragmentManager(), "dialog");
			}
		});		
		
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
		this.registerForContextMenu(childrenList);
		childrenListAdapter = (SimpleCursorAdapter) useListViewAdapter();
		childrenListAdapter.setViewBinder(this);
		childrenList.setAdapter(childrenListAdapter); 
		childrenList.setHeaderDividersEnabled(true);
		childrenList.setOnItemClickListener((OnItemClickListener) this);
		View addChildCategory = getView().findViewById(R.id.moneyExpenseCategory_list_button_addChildCategory);
		addChildCategory.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				final HyjActivity activity = (HyjActivity) getActivity();
				if(lastSelectedMainCategoryId == AdapterView.INVALID_ROW_ID){
					activity.mDialogCallback = null;
					activity.displayDialog("新增子分类", "请先选择主分类");
					return;
				}
			
				final MoneyExpenseCategory mainCategory = MoneyExpenseCategory.load(MoneyExpenseCategory.class, lastSelectedMainCategoryId);
				
				activity.mDialogCallback = new HyjActivity.DialogCallbackListener() {
					@Override
					public void doPositiveClick(Object bundle) {
						Bundle b = (Bundle)bundle;
				    	final String categoryName = b.getString("categoryName");
				    	final MoneyExpenseCategory category = new MoneyExpenseCategory();
				    	category.setParentExpenseCategoryId(mainCategory.getId());
				    	category.setName(categoryName);
				    	category.save();
					}
				};
				
				activity.mDialogFragment = MoneyCategoryFormDialogFragment.newInstance("新增子分类", "", 0);
				activity.mDialogFragment.show(activity.getSupportFragmentManager(), "dialog");
			}
		});
	}
	

	public void onInitViewData(){
		super.onInitViewData();
		initLoader(1);
	}
	
	@Override
	public Loader<Object> onCreateLoader(int id, Bundle arg1) {
		Object loader;
		String selection = null;
		String orderBy = null;
		String[] selectionArgs = null;
		if(id == 0){
			setFooterLoadStart(getListView());
			selection = "parentExpenseCategoryId IS NULL";
			loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(MoneyExpenseCategory.class, null),
				null, selection, selectionArgs, "name ASC"
			);
		} else {
			setFooterLoadStart(childrenList);
			if(arg1 != null){
				selection = "parentExpenseCategoryId=?";
				selectionArgs = new String[]{arg1.getString("parentCategoryId")};
				orderBy = "name DESC";
			} else {
				selection = "parentExpenseCategoryId IS NOT NULL";
//				selectionArgs = new String[]{};
				orderBy = "lastClientUpdateTime DESC LIMIT 10";
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
		if(count >= mListPageSize){
	        ((TextView)mFooterView).setText(R.string.app_listview_footer_fetch_more);
		} else if(count == 0){
	        ((TextView)mFooterView).setText(R.string.app_listview_no_content);
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
		} else {
			mFrecentCategory.setBackground(null);
		}
		
		v.setBackgroundResource(R.drawable.abc_tab_selected_focused_holo);
		float px = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 10,
				this.getResources().getDisplayMetrics());
		v.setPadding((int)px, 0, 0, 0);
		
		Bundle bundle = null;
		Loader<Object> loader = getLoaderManager().getLoader(1);
		
		if(id != -1){
			lastSelectedMainCategoryId = id;
			((MainCategorySimpleCursorAdapter)getListAdapter()).setSelectedId(id);
			
			bundle = new Bundle();
			MoneyExpenseCategory moneyExpenseCategory = MoneyExpenseCategory.load(MoneyExpenseCategory.class, id);
			bundle.putString("parentCategoryId", moneyExpenseCategory.getId());
		} else {
			lastSelectedMainCategoryId = AdapterView.INVALID_ROW_ID;
			((MainCategorySimpleCursorAdapter)getListAdapter()).setSelectedId(AdapterView.INVALID_ROW_ID);
		}
	
	    if (loader != null && !loader.isReset() ) { 
	    	getLoaderManager().restartLoader(1, bundle, this);
	    } else {
	    	getLoaderManager().initLoader(1, bundle, this);
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
			if(id < 0){
				return;
			}
			final HyjActivity activity = (HyjActivity) getActivity();
			final MoneyExpenseCategory category = MoneyExpenseCategory.load(MoneyExpenseCategory.class, id);
			if(activity.mDialogFragment != null){
				activity.mDialogFragment.dismiss();
			}
			
			activity.mDialogCallback = new HyjActivity.DialogCallbackListener() {
				@Override
				public void doPositiveClick(Object bundle) {
					Bundle b = (Bundle)bundle;
			    	final String categoryName = b.getString("categoryName");
			    	category.setName(categoryName);
			    	category.save();
				}
				@Override
				public void doNegativeClick() {
					activity.displayDialog(R.string.app_action_delete_list_item, R.string.app_confirm_delete, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
						new DialogCallbackListener() {
							@Override
							public void doPositiveClick(Object object) {
								category.delete();
								HyjUtil.displayToast(R.string.app_delete_success);
							}
						});
				}
			};
			
			activity.mDialogFragment = MoneyCategoryFormDialogFragment.newInstance("修改子分类", category.getName(), 1);
			activity.mDialogFragment.show(activity.getSupportFragmentManager(), "dialog");
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
	
	private static class MainCategorySimpleCursorAdapter extends HyjSimpleCursorAdapter {
		
		public MainCategorySimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
			
		}


		@Override
		public View getView(int position, View arg1, ViewGroup arg2) {
			View v = super.getView(position, arg1, arg2);
			if(getSelectedId() != AdapterView.INVALID_ROW_ID && this.getItemId(position) == getSelectedId()){
				v.setBackgroundResource(R.drawable.abc_tab_selected_focused_holo);
				float px = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 10,
						v.getResources().getDisplayMetrics());
				v.setPadding((int)px, 0, 0, 0);
			} else {
				int sdk = android.os.Build.VERSION.SDK_INT;
				if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
				    v.setBackgroundDrawable(null);
				} else {
				   v.setBackground(null);
				}
			}
			return v;
		}
		
		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long id) {
		if(id < 0){
			return false;
		} 
		if(getActivity().getCallingActivity() == null){
			final HyjActivity activity = (HyjActivity) getActivity();
			final MoneyExpenseCategory category = MoneyExpenseCategory.load(MoneyExpenseCategory.class, id);
			if(activity.mDialogFragment != null){
				activity.mDialogFragment.dismiss();
			}
			
			activity.mDialogCallback = new HyjActivity.DialogCallbackListener() {
				@Override
				public void doPositiveClick(Object bundle) {
					Bundle b = (Bundle)bundle;
			    	final String categoryName = b.getString("categoryName");
			    	category.setName(categoryName);
			    	category.save();
				}
				@Override
				public void doNegativeClick() {
					activity.displayDialog(R.string.app_action_delete_list_item, R.string.app_confirm_delete, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
						new DialogCallbackListener() {
							@Override
							public void doPositiveClick(Object object) {
								category.delete();
								HyjUtil.displayToast(R.string.app_delete_success);
							}
						});
				}
			};
			
			activity.mDialogFragment = MoneyCategoryFormDialogFragment.newInstance("修改主分类", category.getName(), 1);
			activity.mDialogFragment.show(activity.getSupportFragmentManager(), "dialog");
			return true;
		}
		return false;
	}

}
