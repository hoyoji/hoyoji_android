package com.hoyoji.android.hyjframework.activity;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.activity.HyjUserActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class HyjBlankUserActivity extends HyjUserActivity {

	@Override
	protected Integer getContentView() {
		//return R.layout.activity_blank_user;
		return null;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String title = intent.getStringExtra("TITLE");
		//setTitle(title);
		this.getSupportActionBar().setTitle(title);
		String fragmentClassName = getIntent().getStringExtra("FRAGMENT_NAME");
		Class<? extends Fragment> fragmentClass = HyjApplication.getInstance().getFragmentClassMap(fragmentClassName);
		addFragment(fragmentClass);
	}
	
	
}
