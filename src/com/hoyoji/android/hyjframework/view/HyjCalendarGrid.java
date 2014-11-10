package com.hoyoji.android.hyjframework.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.GridView;

public class HyjCalendarGrid extends GridView {
	private HyjCalendarGridAdapter mCalendarGridAdapter;
	private	Resources r = getResources();
	
	public HyjCalendarGrid(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setNumColumns(7);
//		this.setGravity(Gravity.CENTER);
//		this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		
		mCalendarGridAdapter = new HyjCalendarGridAdapter(context, r);
		this.setAdapter(mCalendarGridAdapter);
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
	
	
	
	public HyjCalendarGridAdapter getAdapter(){
		return mCalendarGridAdapter;
	}
	
}
