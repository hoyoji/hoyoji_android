package com.hoyoji.android.hyjframework.activity;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.fragment.HyjDialogFragment;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.project.ProjectFormFragment;

public abstract class HyjActivity extends ActionBarActivity 
//implements GestureDetector.OnGestureListener 
{
	public static final int REQUEST_TAKE_PHOTO = 1024;
	
	private ProgressDialog mProgressDialog;

//	private GestureDetector gestureScanner;
	protected abstract Integer getContentView();
	
	protected Integer getOptionsMenuView(){
		return null;
	}
	
	protected void onInitViewData() {
		// init view data here
	}
	
	public ProgressDialog displayProgressDialog(String title, String msg){
		dismissProgressDialog();
		mProgressDialog = ProgressDialog.show(this, title, msg, true, false);  
		return mProgressDialog;
	}
	
	public ProgressDialog displayProgressDialog(int title, int msg){
		dismissProgressDialog();
		mProgressDialog = ProgressDialog.show(this, this.getString(title), this.getString(msg), true, false); 
		return mProgressDialog;
	}
	
	public void dismissProgressDialog(){
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
			mProgressDialog.dismiss();
		}
	}
	
	public void displayDialog(String title, String msg) {
		if(mDialogFragment != null){
			mDialogFragment.dismiss();
		}
		
		mDialogFragment = HyjDialogFragment.newInstance(title, msg, R.string.alert_dialog_ok, -1, -1);
		mDialogFragment.show(getSupportFragmentManager(), "dialog");
	} 
	
	public void displayDialog(int title, int msg) {
		if(mDialogFragment != null){
			mDialogFragment.dismiss();
		}
		
		mDialogFragment = HyjDialogFragment.newInstance(title, msg, R.string.alert_dialog_ok, -1, -1);
		mDialogFragment.show(getSupportFragmentManager(), "dialog");
	} 

	public void displayDialog(int title, int msg, int positiveButton, int negativeButton, int NeutralButton, DialogCallbackListener dialogCallback) {
		this.mDialogCallback = dialogCallback;
		
		if(mDialogFragment != null){
			mDialogFragment.dismiss();
		}
		
		if(positiveButton == -1){
			positiveButton = R.string.alert_dialog_ok;
		}
		
		mDialogFragment = HyjDialogFragment.newInstance(title, msg, positiveButton, negativeButton, NeutralButton);
		mDialogFragment.show(getSupportFragmentManager(), "dialog");
	} 

	
	public static class DialogCallbackListener {
		public void doNegativeClick() {}
		public void doNeutralClick() {}
		public void doPositiveClick(Object object) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public DialogCallbackListener mDialogCallback;
	public DialogFragment mDialogFragment;
	private boolean mIsViewInited = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//        gestureScanner = new GestureDetector(HyjActivity.this,this);
		if(getContentView() != null){
			setContentView(getContentView());
	    }
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(!mIsViewInited){
			onInitViewData();	
			mIsViewInited = true;
		}
	}

	protected void onStartWithoutInitViewData() {
		super.onStart();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
		if(getOptionsMenuView() != null){
			MenuInflater inflater = this.getMenuInflater();
			inflater.inflate(getOptionsMenuView(), menu);
		}
	    return super.onCreateOptionsMenu(menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (item.getItemId() == android.R.id.home) {
        	finish();
        	return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }
	
	public void dialogDoPositiveClick(Object object) {
		if(mDialogCallback != null){
			mDialogCallback.doPositiveClick(object);
		}
	}
	
	public void dialogDoNegativeClick() {
		if(mDialogCallback != null){
			mDialogCallback.doNegativeClick();
		}
	}
	
	public void dialogDoNeutralClick() {      
		if(mDialogCallback != null){
			mDialogCallback.doNeutralClick();
		}
	}

	public void openActivityWithFragment(Class<? extends Fragment> fragmentClass, int titleRes, Bundle bundle){
		openActivityWithFragment(fragmentClass, getString(titleRes), bundle, false, null);
	}
	
	public void openActivityWithFragmentForResult(Class<? extends Fragment> fragmentClass, int titleRes, Bundle bundle, int requestCode){
		openActivityWithFragment(fragmentClass, getString(titleRes), bundle, true, requestCode);
	}
	
	public void openActivityWithFragment(Class<? extends Fragment> fragmentClass, String title, Bundle bundle, boolean forResult, Integer requestCode){
		Intent intent = new Intent(this, HyjBlankUserActivity.class);
		HyjApplication.getInstance().addFragmentClassMap(fragmentClass.toString(), fragmentClass);
		intent.putExtra("FRAGMENT_NAME", fragmentClass.toString());
		intent.putExtra("TITLE", title);
		if(bundle != null){
			intent.putExtras(bundle);
		}
		if(forResult){
			startActivityForResult(intent, requestCode);
		} else {
			startActivity(intent);
		}
	}
	
	public void addFragment(Class<? extends Fragment> fragment){
		FragmentManager fragmentManager = getSupportFragmentManager();
		try {
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			Fragment newFragment = fragment.newInstance();
			fragmentTransaction.add(android.R.id.content, newFragment, "fragment");
			fragmentTransaction.commit();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_TAKE_PHOTO) {
	    	Intent intent = new Intent("REQUEST_TAKE_PHOTO");
	    	intent.putExtra("resultCode", resultCode);
	    	if(data != null){
		    	intent.putExtra("selectedImage", data.getDataString());
	    	}
	    	this.sendBroadcast(intent);
	    } else {
	    	super.onActivityResult(requestCode, resultCode, data);
	    }
	}
	
	@Override
    public boolean dispatchTouchEvent(MotionEvent event) {
//        gestureScanner.onTouchEvent(ev);
//        return super.dispatchTouchEvent(ev);

	    View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (v instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            if (event.getAction() == MotionEvent.ACTION_DOWN && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) ) { 
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }

//    @Override
//    public boolean onSingleTapUp(MotionEvent event) {
//        View v = getCurrentFocus();
//
//        if (v instanceof EditText) {
//            View w = getCurrentFocus();
//            int scrcoords[] = new int[2];
//            w.getLocationOnScreen(scrcoords);
//            boolean hide = true;
//
//            View view = ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
//            ArrayList<View> editTexts = view.getFocusables(0);     // Get All EditTexts in view
//
//            for(int i=0; i< editTexts.size(); i++){
//                View editText = editTexts.get(i);
//                editText.getLocationOnScreen(scrcoords);
//                float x = event.getRawX();
//                float y = event.getRawY();
//                int viewX = scrcoords[0];
//                int viewY = scrcoords[1];
//
//                // If touch is in any of EditText, keep keyboard active, otherwise hide it.
//                if (event.getAction() == MotionEvent.ACTION_UP  && ( x > viewX && x < (viewX + editText.getWidth())) && ( y > viewY && y < (viewY + editText.getHeight())) ) {
//                    hide = false;
//                }
//            }
//
//            if (hide) {
//                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
//            }
//        }
//        return true;
//    }

//    @Override
//    public boolean onScroll(MotionEvent event, MotionEvent e2, float distanceX, float distanceY) {
//        return true;
//    } 
//
//	@Override
//	public boolean onDown(MotionEvent e) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
//			float velocityY) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public void onLongPress(MotionEvent e) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onShowPress(MotionEvent e) {
//		// TODO Auto-generated method stub
//		
//	}
//	
}
