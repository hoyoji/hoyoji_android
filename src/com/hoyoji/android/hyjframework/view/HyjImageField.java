package com.hoyoji.android.hyjframework.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
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

public class HyjImageField extends GridView {
	private ImageGridAdapter mImageGridAdapter;
	private	Resources r = getResources();
	
	public HyjImageField(Context context, AttributeSet attrs) {
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
	
	public void setImages(List<Picture> pictures){
		List<PictureItem> pis = new ArrayList<PictureItem>();
		for(int i=0; i < pictures.size(); i++){
			PictureItem pi = new PictureItem(pictures.get(i));
			pis.add(pi);
		}
		mImageGridAdapter.addAll(pis);
	}
	
	public ImageGridAdapter getAdapter(){
		return mImageGridAdapter;
	}
		
	public static class ImageGridAdapter extends ArrayAdapter<PictureItem> {
		private PictureItem mPictureCamera;
		public ImageGridAdapter(Context context, int resource) {
			super(context, resource);
			mPictureCamera = new PictureItem(null);
			this.add(mPictureCamera);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView iv;
			final HyjImageField self = (HyjImageField) parent;
			if (convertView != null) {
				iv = (ImageView) convertView;
			} else {
				iv = new ImageView(this.getContext());
				float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, self.r.getDisplayMetrics());
				iv.setLayoutParams(new LayoutParams((int)px, (int)px));
//				iv.setPadding(0, 0, 0, 0);
				iv.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						PictureItem pic = (PictureItem) v.getTag();
						if(pic ==  mPictureCamera){
							self.dispatchTakePictureIntent();
						} else {
							HyjUtil.displayToast("Show large pic " + pic.getPicture().getId());
						}
					}
				});
			}
			PictureItem pic = getItem(position);
			iv.setTag(pic);
			if (pic == mPictureCamera) {
				iv.setImageResource(R.drawable.ic_action_camera);
			} else {
				File imageFile;
				try {
					imageFile = HyjUtil.createImageFile(pic.getPicture().getId()+"_icon", pic.getPicture().getPictureType());
					iv.setImageURI(Uri.fromFile(imageFile));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return iv;
		}

	}
	
	public static class PictureItem {
		public static final int UNCHANGED = 0;
		public static final int NEW = 1;
		public static final int DELETED = 2;
		public static final int CHANGED = 3;
		
		private int mState = UNCHANGED;
		private Picture mPicture;
		
		PictureItem(Picture picture){
			mPicture = picture;
		}
		
		PictureItem(Picture picture, int state){
			mPicture = picture;
			mState = state;
		}
		
		public void setState(int state){
			mState = state;
		}
		
		public int getState(){
			return mState;
		}
		
		public Picture getPicture() {
			return mPicture;
		}
	}	
	

	public void dispatchTakePictureIntent() {
		Picture newPicture = new Picture();
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    // Ensure that there's a camera activity to handle the intent
	    if (takePictureIntent.resolveActivity(this.getContext().getPackageManager()) != null) {
	        // Create the File where the photo should go
	        File photoFile = null;
	        try {
	            photoFile = HyjUtil.createImageFile(newPicture.getId());
		        // Continue only if the File was successfully created
		        if (photoFile != null) {
		            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
		           ((HyjActivity)getContext()).startActivityForResult(takePictureIntent, HyjActivity.REQUEST_TAKE_PHOTO);
		           IntentFilter intentFilter = new IntentFilter("REQUEST_TAKE_PHOTO");
		           BroadcastReceiver receiver = new TakePhotoBroadcastReceiver(getContext(), photoFile, newPicture);
		           getContext().registerReceiver(receiver,intentFilter); 
		        }
	        } catch (IOException ex) {
	            // Error occurred while creating the File
	        	HyjUtil.displayToast("无法创建图片文件");
	        }
	    }
	}
	

	
	private Bitmap getScaledBitmap(String photoPath, int targetW, int targetH){
	    // Get the dimensions of the bitmap
	    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	    bmOptions.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(photoPath, bmOptions);
	    int photoW = bmOptions.outWidth;
	    int photoH = bmOptions.outHeight;

	    // Determine how much to scale down the image
	    int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

	    // Decode the image file into a Bitmap sized to fill the View
	    bmOptions.inJustDecodeBounds = false;
	    bmOptions.inSampleSize = scaleFactor;
	    bmOptions.inPurgeable = true;

	    return BitmapFactory.decodeFile(photoPath, bmOptions);
	}
	
//	private void galleryAddPic(String picturePath) {
//	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//	    File f = new File(picturePath);
//	    Uri contentUri = Uri.fromFile(f);
//	    mediaScanIntent.setData(contentUri);
//	    this.getContext().sendBroadcast(mediaScanIntent);
//	}
	
	private class TakePhotoBroadcastReceiver extends BroadcastReceiver {
		File mPhotoFile;
		Context mContext;
		Picture mPicture;
		
		TakePhotoBroadcastReceiver(Context context, File photoFile, Picture picture){
			mPhotoFile = photoFile;
			mContext = context;
			mPicture = picture;
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {  
			if(intent.getAction().equals("REQUEST_TAKE_PHOTO")) {
				int result = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED);
				if(result == Activity.RESULT_OK){
					float pxW = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 500, r.getDisplayMetrics());
					float pxH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 800, r.getDisplayMetrics());
					FileOutputStream out = null;
					Bitmap scaled = getScaledBitmap(mPhotoFile.getAbsolutePath(), (int)pxW, (int)pxH);
					try {
					    out = new FileOutputStream(mPhotoFile);
					    scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
					    out.close();
					    out = null;
					    
					    int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, r.getDisplayMetrics());
					    Bitmap thumbnail = ThumbnailUtils.extractThumbnail(scaled, px, px);
					    
					    out = new FileOutputStream(HyjUtil.createImageFile(mPicture.getId()+"_icon"));
					    thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, out);
					    out.close();
					    out = null;
					    thumbnail.recycle();
					} catch (Exception e) {
					    e.printStackTrace();
					}
					
					scaled.recycle();
					
					mPicture.setPictureType("JPEG");
					PictureItem pi = new PictureItem(mPicture, PictureItem.NEW);
					mImageGridAdapter.add(pi);
				} else {
					mPhotoFile.delete();
				}
				mContext.unregisterReceiver(this);
				
			}
		}
	}
}
