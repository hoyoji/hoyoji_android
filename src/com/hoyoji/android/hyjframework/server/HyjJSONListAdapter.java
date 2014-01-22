package com.hoyoji.android.hyjframework.server;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class HyjJSONListAdapter extends ArrayAdapter<JSONObject> {
    private int[] mViewIds;
    private String[] mFields;
    
    public HyjJSONListAdapter(Context context, int layoutResource, int[] viewIds, String[] fields) {
        super(context, layoutResource);
        mViewIds = viewIds;
        mFields = fields;
    }

    public void setData(List<JSONObject> data) {
        clear();
        if (data != null) {
        	for(JSONObject o : data){
        		add(o);
        	}
        }
    }

    /**
     * Populate new items in the list.
     */
    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = super.getView(position, convertView, parent);
        } else {
            view = convertView;
        }

        JSONObject item = getItem(position);
       // ((ImageView)view.findViewById(mIconView)).setImageDrawable(item.getString(name));
        for(int i=0; i<mViewIds.length; i++){
        	((TextView)view.findViewById(mViewIds[i])).setText(item.optString(mFields[i]));
        }
        
        return view;
    }
}