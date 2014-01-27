package com.hoyoji.android.hyjframework.fragment;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjSimpleCursorTreeAdapter.OnGetChildrenCursorListener;
import com.hoyoji.android.hyjframework.activity.HyjBlankUserActivity;
import com.hoyoji.hoyoji.R;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.SimpleCursorTreeAdapter;

public abstract class HyjUserExpandableListFragment extends Fragment implements 
	LoaderManager.LoaderCallbacks<Object>, 
	SimpleCursorTreeAdapter.ViewBinder, 
	OnChildClickListener,
	OnGroupClickListener,
	OnGroupCollapseListener,
	OnGroupExpandListener,
	OnGetChildrenCursorListener
{
	public final static int DELETE_LIST_ITEM = 1024;
	private boolean mIsViewInited = false;
	private ExpandableListView mExpandableListView;
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			//View v = super.onCreateView(inflater, container, savedInstanceState);
			ViewGroup rootView = (ViewGroup) inflater.inflate(useContentView(), container, false);
			//rootView.addView(v, 0);
			mExpandableListView = (ExpandableListView) rootView.findViewById(android.R.id.list);
			mExpandableListView.setOnChildClickListener(this);
			mExpandableListView.setOnGroupClickListener(this);
			mExpandableListView.setOnGroupCollapseListener(this);
			mExpandableListView.setOnGroupExpandListener(this);
			if(useToolbarView() != null){
				// populate bottom toolbar
			}
			return rootView;
	}
	
	public ExpandableListView getListView(){
		return mExpandableListView;
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
	    getListView().setEmptyView(getView().findViewById(android.R.id.empty));
		this.registerForContextMenu(getListView());
		SimpleCursorTreeAdapter adapter = (SimpleCursorTreeAdapter) getListView().getExpandableListAdapter();
		if(adapter == null){
			adapter = useListViewAdapter();
			adapter.setViewBinder(this);
			getListView().setAdapter(adapter); 
		}
	}
	
	@Override
	public void onStart(){
		super.onStart();
		if(HyjApplication.getInstance().isLoggedIn()) {
	        //setListShown(false);  
			if(!mIsViewInited){
				getLoaderManager().initLoader(-1, null,this);
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

	public abstract SimpleCursorTreeAdapter useListViewAdapter();
	
	public void onInitViewData(){
		
	}
	

	@Override
	public void onLoadFinished(Loader<Object> loader, Object cursor) {
		if(loader.getId() < 0){
			((SimpleCursorTreeAdapter) getListView().getExpandableListAdapter()).changeCursor((Cursor)cursor);
		} else {
			((SimpleCursorTreeAdapter) getListView().getExpandableListAdapter()).setChildrenCursor(loader.getId(), (Cursor)cursor);
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
		if(loader.getId() < 0){
			((SimpleCursorTreeAdapter) getListView().getExpandableListAdapter()).changeCursor(null);
		} else {
			((SimpleCursorTreeAdapter) getListView().getExpandableListAdapter()).setChildrenCursor(loader.getId(), null);
		}
	}	

	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		return null;
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
				ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
				int type = ExpandableListView
			            .getPackedPositionType(info.packedPosition);

			    if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			        int groupPos = ExpandableListView
			                .getPackedPositionGroup(info.packedPosition);
			        int childPos = ExpandableListView
			                .getPackedPositionChild(info.packedPosition);
				    Long itemId = getListView().getExpandableListAdapter().getChildId(groupPos, childPos);
					onDeleteListItem(itemId);
					return true;
			    } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			        return true;
			    }
				break;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		ExpandableListContextMenuInfo adapterContextMenuInfo = (ExpandableListContextMenuInfo) menuInfo;
		getListView();
		if(ExpandableListView.getPackedPositionType(adapterContextMenuInfo.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_CHILD){
			menu.add(DELETE_LIST_ITEM, DELETE_LIST_ITEM, DELETE_LIST_ITEM, R.string.app_action_delete_list_item);
		}
	}	
	
	public void onDeleteListItem(Long id){
	}
	

	@Override
	public boolean setViewValue(View arg0, Cursor arg1, int arg2) {
		return false;
	}

	@Override
	public void onGroupExpand(int arg0) {
		
	}

	@Override
	public void onGroupCollapse(int arg0) {
		
	}

	@Override
	public boolean onGroupClick(ExpandableListView arg0, View arg1, int arg2,
			long arg3) {
		return false;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		return false;
	}  	
	
}


