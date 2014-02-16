package com.hoyoji.android.hyjframework.view;

import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Picture;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class HyjImageField extends GridView {
	private ImageGridAdapter mImageGridAdapter;
	
	public HyjImageField(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setColumnWidth(58);
		this.setNumColumns(AUTO_FIT);
		this.setGravity(Gravity.CENTER);
		this.setVerticalSpacing(5);
		this.setHorizontalSpacing(5);
		this.setStretchMode(STRETCH_COLUMN_WIDTH);
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
	
	public ImageGridAdapter getAdapter(){
		return mImageGridAdapter;
	}
		
	public static class ImageGridAdapter extends ArrayAdapter<Picture> {
		private Picture mPictureCamera;
		public ImageGridAdapter(Context context, int resource) {
			super(context, resource);
			mPictureCamera = new Picture();
			this.add(mPictureCamera);
			Picture picture1 = new Picture();
			this.add(picture1);
			Picture picture2 = new Picture();
			this.add(picture2);
			Picture picture3 = new Picture();
			this.add(picture3);
			Picture picture4 = new Picture();
			this.add(picture4);
			Picture picture5 = new Picture();
			this.add(picture5);
			Picture picture6 = new Picture();
			this.add(picture6);
			Picture picture7 = new Picture();
			this.add(picture7);
			Picture picture8 = new Picture();
			this.add(picture8);
			Picture picture9 = new Picture();
			this.add(picture9);
			Picture picture10 = new Picture();
			this.add(picture10);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView iv;
			if(convertView != null){
				iv = (ImageView) convertView; 
			} else {
				iv = new ImageView(this.getContext());
				iv.setAdjustViewBounds(true);
				iv.setBackgroundColor(this.getContext().getResources().getColor(android.R.color.darker_gray));
			}
			Picture pic = getItem(position);
			if(pic == mPictureCamera){
				iv.setImageResource(R.drawable.ic_action_camera);
			} else {
				iv.setImageResource(R.drawable.ic_action_map);
			}
			return iv;
		}

		
	}
}
