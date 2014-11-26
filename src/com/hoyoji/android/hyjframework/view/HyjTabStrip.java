package com.hoyoji.android.hyjframework.view;

import java.util.ArrayList;

import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.hoyoji_android.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class HyjTabStrip extends LinearLayout {

	public interface OnTabSelectedListener {
		void onTabSelected(int tag);
	}
	
	private LinearLayout mTabContainer;
	private ArrayList<LinearLayout> mListTabs = new ArrayList<LinearLayout>();
	private OnTabSelectedListener onTabSelectedListener;
	private DisplayMetrics mDisplayMetrics;
	private int intTabLineWidth = 0;
	private double dblTabLineWidth = 0.0;
	private int currentIndex = 0;
	private View mTabLine;

	public HyjTabStrip(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.setOrientation(LinearLayout.VERTICAL);
		mDisplayMetrics = context.getResources().getDisplayMetrics();
		final LayoutInflater inflater = (LayoutInflater)
			       context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.tabstrip, this);
		mTabContainer = (LinearLayout)this.findViewById(R.id.lllayout); 
		mTabLine = findViewById(R.id.id_tab_line);
	}
	
	public void initTabLine(int numberOfPages) {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTabLine.getLayoutParams();
		lp.width = mDisplayMetrics.widthPixels / numberOfPages;
		mTabLine.setLayoutParams(lp);
		intTabLineWidth = lp.width;
		dblTabLineWidth = mDisplayMetrics.widthPixels * 1.0 / numberOfPages;
	}
	
	public void addTab(String title){
//		<LinearLayout
//        android:id="@+id/id_tab_account_ly"
//        android:layout_width="match_parent"
//        android:layout_height="wrap_content"
//        android:layout_weight="1"
//        android:gravity="center"
//        android:orientation="horizontal"
//        android:padding="5dip"
//        android:layout_margin="0dp" >
//
//        <TextView
//            android:id="@+id/id_account"
//            android:layout_width="wrap_content"
//            android:layout_height="wrap_content"
//            android:gravity="center"
//            android:text="账户"
//            android:textColor="@color/black"
//            android:textSize="15sp" />
//    </LinearLayout>
		
		LinearLayout newTab = new LinearLayout(this.getContext());
		LayoutParams lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
		lp.weight = 1;
		newTab.setLayoutParams(lp);
		int padding = (int) (5 * mDisplayMetrics.density);
		newTab.setPadding(padding, padding, padding, padding);
		newTab.setOrientation(LinearLayout.HORIZONTAL);
		newTab.setGravity(Gravity.CENTER);
		
		TextView newTextView = new TextView(getContext());
		newTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		newTextView.setGravity(Gravity.CENTER);
		newTextView.setTextSize(15.0f);
		newTextView.setText(title);
		
		newTab.addView(newTextView);
		
		mListTabs.add(newTab);
		newTab.setTag(newTextView);
		newTextView.setTag(mListTabs.size());
		if(this.onTabSelectedListener != null){
			this.onTabSelectedListener.onTabSelected((Integer)((TextView)newTab.getTag()).getTag());
		}
		
		mTabContainer.addView(newTab);
	}
	
	public void setOnTabSelectedListener(OnTabSelectedListener listener){
		this.onTabSelectedListener = listener;
	}
	
	protected void resetTextView() {
		for(int i=0; i < mListTabs.size(); i++){
			((TextView)mListTabs.get(i).getTag()).setTextColor(getResources().getColor(R.color.black));
		}
	}

	
	public void setTabSelected(int position)
	{
		resetTextView();
		((TextView)mListTabs.get(position).getTag()).setTextColor(getResources().getColor(R.color.hoyoji_red));
		currentIndex = position;
	}
	
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
	{
		LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) mTabLine.getLayoutParams();
		if (currentIndex == position)// 0->1
		{
			lp.leftMargin = (int) (positionOffset * dblTabLineWidth + currentIndex * intTabLineWidth);
			mTabLine.setLayoutParams(lp);
		} else if (currentIndex > position) // 1->0
		{
			lp.leftMargin = (int) (-(1-positionOffset) * dblTabLineWidth + currentIndex * intTabLineWidth);
			mTabLine.setLayoutParams(lp);
		}
	}

	public void setBadgeCount(int position, int count) {
		
		
	}
	
}
