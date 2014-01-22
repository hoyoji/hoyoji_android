package com.hoyoji.android.hyjframework.fragment;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjBlankUserActivity;
import com.hoyoji.hoyoji.R;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListAdapter;

public abstract class HyjUserListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
	public final static int DELETE_LIST_ITEM = 1024;
	private boolean mIsViewInited = false;
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			//View v = super.onCreateView(inflater, container, savedInstanceState);
			ViewGroup rootView = (ViewGroup) inflater.inflate(useContentView(), container, false);
			//rootView.addView(v, 0);
			if(useToolbarView() != null){
				// populate bottom toolbar
			}
			return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		getListView().setEmptyView(getView().findViewById(android.R.id.empty));
		this.registerForContextMenu(getListView());
		if(this.getListAdapter() == null){
			setListAdapter(useListViewAdapter()); 
		}
	}
	
	@Override
	public void onStart(){
		super.onStart();
		if(HyjApplication.getInstance().isLoggedIn()) {
	        //setListShown(false);  
			if(!mIsViewInited){
				getLoaderManager().initLoader(0, null,this);
				onInitViewData();
				mIsViewInited = true;
			}
		}
	}
	
	@Override
	public void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if(useOptionsMenuView() != null){
			setHasOptionsMenu (true);
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    // Inflate the menu items for use in the action bar
		if(useOptionsMenuView() != null){
			inflater.inflate(useOptionsMenuView(), menu);
		}
	    super.onCreateOptionsMenu(menu, inflater);
	}

	public Integer useToolbarView(){
		return null;
	}
	

	public Integer useOptionsMenuView(){
		return null;
	}
	
	public abstract Integer useContentView();

	public abstract ListAdapter useListViewAdapter();
	
	public void onInitViewData(){
		
	}
	

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		((SimpleCursorAdapter) this.getListAdapter()).swapCursor(cursor);
		// The list should now be shown. 
        if (isResumed()) {
          //  setListShown(true);  
        } else {  
          //  setListShownNoAnimation(true);  
        }  
	}


	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		((SimpleCursorAdapter) this.getListAdapter()).swapCursor(null);
	}	

	public void openActivityWithFragment(Class<? extends Fragment> fragmentClass, int titleRes, Bundle bundle){
		openActivityWithFragment(fragmentClass, getString(titleRes), bundle, false, null);
	}
	
	public void openActivityWithFragmentForResult(Class<? extends Fragment> fragmentClass, int titleRes, Bundle bundle, int requestCode){
		openActivityWithFragment(fragmentClass, getString(titleRes), bundle, true, requestCode);
	}
	
	public void openActivityWithFragment(Class<? extends Fragment> fragmentClass, String title, Bundle bundle, boolean forResult, Integer requestCode){
		Intent intent = new Intent(this.getActivity(), HyjBlankUserActivity.class);
		HyjApplication.getInstance().addFragmentClassMap(fragmentClass.toString(), fragmentClass);
		intent.putExtra("FRAGMENT_NAME", fragmentClass.toString());
		intent.putExtra("TITLE", title);
		if(bundle != null){
			intent.putExtras(bundle);
		}
		if(forResult){
			startActivityForResult(intent, requestCode);
		} else {
			startActivity(intent);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(!getUserVisibleHint()){
			return super.onContextItemSelected(item);
		}
		switch (item.getItemId()) {
			case DELETE_LIST_ITEM:
			    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			    Long itemId = getListAdapter().getItemId(info.position);
				onDeleteListItem(itemId);
				break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(DELETE_LIST_ITEM, DELETE_LIST_ITEM, DELETE_LIST_ITEM, R.string.app_action_delete_list_item);
	}	
	
	public void onDeleteListItem(Long id){
	}
}


