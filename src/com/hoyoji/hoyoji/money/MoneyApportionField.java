package com.hoyoji.hoyoji.money;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Picture;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MoneyApportionField<T extends HyjModel> extends GridView {
	private ImageGridAdapter mImageGridAdapter;
	private	Resources r = getResources();
	
	public MoneyApportionField(Context context, AttributeSet attrs) {
		super(context, attrs);
		int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, r.getDisplayMetrics());
		this.setColumnWidth(px);
		this.setNumColumns(AUTO_FIT);
		this.setGravity(Gravity.CENTER);
		px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
		this.setVerticalSpacing(px);
		this.setHorizontalSpacing(px);
		//this.setStretchMode(STRETCH_COLUMN_WIDTH);
		this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		
		mImageGridAdapter = new ImageGridAdapter(context, 0);
		this.setAdapter(mImageGridAdapter);
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
	
	public void setApportions(List<T> apportions){
		//List<PictureItem> pis = new ArrayList<PictureItem>();
		for(int i=0; i < apportions.size(); i++){
			ApportionItem pi = new ApportionItem(apportions.get(i));
			mImageGridAdapter.add(pi);
			//pis.add(pi);
		}
		//mImageGridAdapter.addAll(pis);
	}
	
	public ImageGridAdapter getAdapter(){
		return mImageGridAdapter;
	}
	
	public void addApportion(){

	}
		
	public static class ImageGridAdapter extends ArrayAdapter<ApportionItem> {
		public ImageGridAdapter(Context context, int resource) {
			super(context, resource);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			HyjImageView iv;
			final MoneyApportionField self = (MoneyApportionField) parent;
			if (convertView != null) {
				iv = (HyjImageView) convertView;
			} else {
				iv = new HyjImageView(this.getContext());
				float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, self.r.getDisplayMetrics());
				iv.setLayoutParams(new LayoutParams((int)px, (int)px));
//				iv.setPadding(0, 0, 0, 0);
				iv.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						ApportionItem pic = (ApportionItem) v.getTag();
						HyjUtil.displayToast("Show large pic " + pic.getApportion().getId());
					}
				});
			}
			
			ApportionItem app = getItem(position);
//			iv.setTag(app);
//			iv.setApportion(app.getApportion());
			return iv;
		}

	}
	
	public static class ApportionItem<T extends HyjModel> {
		public static final int UNCHANGED = 0;
		public static final int NEW = 1;
		public static final int DELETED = 2;
		public static final int CHANGED = 3;
		
		private int mState = UNCHANGED;
		private T mApportion;
		
		ApportionItem(T apportion){
			mApportion = apportion;
		}
		
		ApportionItem(T apportion, int state){
			mApportion = apportion;
			mState = state;
		}
		
		public void setState(int state){
			mState = state;
		}
		
		public int getState(){
			return mState;
		}
		
		public T getApportion() {
			return mApportion;
		}
	}	
	
}
