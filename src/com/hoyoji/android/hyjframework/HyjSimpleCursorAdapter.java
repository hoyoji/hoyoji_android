package com.hoyoji.android.hyjframework;

import com.hoyoji.hoyoji.R;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;


public class HyjSimpleCursorAdapter extends SimpleCursorAdapter {
	long mSelectedId = AdapterView.INVALID_ROW_ID;
	
	public HyjSimpleCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		
	}

	public void setSelectedId(long id){
		mSelectedId = id;
	}
	
	public long getSelectedId(){
		return mSelectedId;
	}

	@Override
	public View getView(int position, View arg1, ViewGroup arg2) {
		View v = super.getView(position, arg1, arg2);
		if(mSelectedId != AdapterView.INVALID_ROW_ID) {
			if(this.getItemId(position) == mSelectedId){
				v.setBackgroundResource(R.drawable.abc_tab_selected_focused_holo);
			} else {
				v.setBackground(null);
			}
		}
		return v;
	}
	
	
}