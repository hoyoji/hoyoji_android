package com.hoyoji.android.hyjframework.view;

import com.hoyoji.android.hyjframework.activity.HyjActivity;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

public class HyjViewPager extends ViewPager {
	 public HyjViewPager(Context context) {
		 super(context);
	 }
	 
	 public HyjViewPager(Context context, AttributeSet attrs) {
		 super(context, attrs);
	 }
	 
	 public void setCurrentItem(int item, boolean smoothScroll) {
		 if(((HyjActivity)this.getContext()).getChoiceMode() != ListView.CHOICE_MODE_MULTIPLE){
			 super.setCurrentItem(item, smoothScroll);
		 }
	 }
	 
	 public void setCurrentItem(int item) {
		 if(((HyjActivity)this.getContext()).getChoiceMode() != ListView.CHOICE_MODE_MULTIPLE){
			 super.setCurrentItem(item);
		 }
	 }
	 
//	 public boolean canScrollHorizontally(int direction) {
//		 
//		 return super.canScrollHorizontally(direction);
//	 }
	 
//	 @Override
//	 protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
//
//		 if(((HyjActivity)this.getContext()).getChoiceModel() == ListView.CHOICE_MODE_MULTIPLE){
//			 return false;
//		 }
//		 return super.canScroll(v, checkV, dx, x, y);
//	 }

	 @Override
	 public boolean onInterceptTouchEvent(MotionEvent ev) {
		 if(((HyjActivity)this.getContext()).getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE){
			 return false;
		 }
		 return super.onInterceptTouchEvent(ev);
	 }
}
