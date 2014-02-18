package com.hoyoji.android.hyjframework.activity;

import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.fragment.HyjDialogFragment;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.project.ProjectFormFragment;

public abstract class HyjActivity extends ActionBarActivity {
	public static final int REQUEST_TAKE_PHOTO = 1024;
	
	private ProgressDialog mProgressDialog;
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
		mDialogFragment.dismiss();
		mDialogFragment = null;
		if(mDialogCallback != null){
			mDialogCallback.doPositiveClick(object);
		}
	}
	
	public void dialogDoNegativeClick() {
		mDialogFragment.dismiss();
		mDialogFragment = null;
		if(mDialogCallback != null){
			mDialogCallback.doNegativeClick();
		}
	}
	
	public void dialogDoNeutralClick() {
		mDialogFragment.dismiss();
		mDialogFragment = null;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_TAKE_PHOTO) {
	    	Intent intent = new Intent("REQUEST_TAKE_PHOTO");
	    	intent.putExtra("resultCode", resultCode);
	    	this.sendBroadcast(intent);
	    } else {
	    	super.onActivityResult(requestCode, resultCode, data);
	    }
	}
}
