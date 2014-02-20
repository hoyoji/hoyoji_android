package com.hoyoji.android.hyjframework;

import java.lang.ref.WeakReference;

import com.hoyoji.hoyoji.R;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

public class HyjBitmapWorkerAsyncTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private String data = null;
    
    public HyjBitmapWorkerAsyncTask(ImageView imageView) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
    }
    
    public static void loadBitmap(String path, ImageView imageView) {
	    if (cancelPotentialWork(path, imageView)) {
	        final HyjBitmapWorkerAsyncTask task = new HyjBitmapWorkerAsyncTask(imageView);
	        final AsyncDrawable asyncDrawable =
	                new AsyncDrawable(HyjApplication.getInstance().getResources(), HyjUtil.getCommonBitmap(R.drawable.ic_action_refresh), task);
	        imageView.setImageDrawable(asyncDrawable);
	        task.execute(path);
	    }
	}
    
    // Decode image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
    	data = params[0];
    	Integer w = null;
    	Integer h = null;
    	if(params.length == 3){
    		w = Integer.parseInt(params[1]);
    		h = Integer.parseInt(params[2]);
    	}
        return HyjUtil.decodeSampledBitmapFromFile(data, w, h);
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            final HyjBitmapWorkerAsyncTask bitmapWorkerTask =
                    getBitmapWorkerTask(imageView);
            if (this == bitmapWorkerTask && imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
    
    private static HyjBitmapWorkerAsyncTask getBitmapWorkerTask(ImageView imageView) {
    	   if (imageView != null) {
    	       final Drawable drawable = imageView.getDrawable();
    	       if (drawable instanceof AsyncDrawable) {
    	           final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
    	           return asyncDrawable.getBitmapWorkerTask();
    	       }
    	    }
    	    return null;
    	}
    
    public static boolean cancelPotentialWork(int data, ImageView imageView) {
        final HyjBitmapWorkerAsyncTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == null || !bitmapData.equals(data)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }
    
    public static boolean cancelPotentialWork(String path, ImageView imageView) {
	    final HyjBitmapWorkerAsyncTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

	    if (bitmapWorkerTask != null) {
	        final String bitmapData = bitmapWorkerTask.data;
	        // If bitmapData is not yet set or it differs from the new data
	        if (bitmapData == null || !bitmapData.equals(path)) {
	            // Cancel previous task
	            bitmapWorkerTask.cancel(true);
	        } else {
	            // The same work is already in progress
	            return false;
	        }
	    }
	    // No task associated with the ImageView, or an existing task was cancelled
	    return true;
	}
    
    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<HyjBitmapWorkerAsyncTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
        		HyjBitmapWorkerAsyncTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                new WeakReference<HyjBitmapWorkerAsyncTask>(bitmapWorkerTask);
        }

        public HyjBitmapWorkerAsyncTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
}