package com.hoyoji.android.hyjframework;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.hoyoji.hoyoji.friend.FriendListFragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;


public class HyjSimpleExpandableListAdapter extends SimpleExpandableListAdapter {
	private SimpleAdapter.ViewBinder mViewBinder;
    private int[] mViewIds;
    private String[] mFields;
    private int mLayoutResource;
    private Context mContext;
    
	public HyjSimpleExpandableListAdapter(Context context,
			List<? extends Map<String, ?>> groupData, int groupLayout,
			String[] groupFrom, int[] groupTo,
			List<? extends List<? extends Map<String, ?>>> childData,
			int childLayout, String[] childFrom,
			int[] childTo) {
		super(context, groupData, groupLayout, groupFrom,
				groupTo, childData, childLayout, childFrom, childTo);
		mLayoutResource = childLayout;
		mViewIds = childTo;
		mFields = childFrom;
		mContext = context;
	}

    
	public void setViewBinder(SimpleAdapter.ViewBinder viewBinder){
		mViewBinder = viewBinder;
	}
	
	
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if(mViewBinder == null){
			return super.getChildView(groupPosition, childPosition, isLastChild,
					convertView, parent);
		}
		View view = convertView;
		View[] viewHolder;
        if (view == null) {
        	LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(mLayoutResource, null);
            viewHolder = new View[mViewIds.length];
            for(int i=0; i<mViewIds.length; i++){
            	View v = view.findViewById(mViewIds[i]);
            	viewHolder[i] = v;
            }
            view.setTag(viewHolder);
        } else {
        	viewHolder = (View[])view.getTag();
        }

		Object data = this.getChild(groupPosition, childPosition); 
        for(int i=0; i<mViewIds.length; i++){
        	View v = viewHolder[i];
        	if(!mViewBinder.setViewValue(v, data, mFields[i])){
        		((TextView)v).setText(data.toString());
            }
        }
        
        return view;
	}


}
