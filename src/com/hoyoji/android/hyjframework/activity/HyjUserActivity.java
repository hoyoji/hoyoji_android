package com.hoyoji.android.hyjframework.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.hoyoji.LoginActivity;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.SettingsActivity;

public abstract class HyjUserActivity extends HyjActivity {
	private boolean mIsFirstTimeStart = true;
	
	@Override
	protected void onStart() {
		if(HyjApplication.getInstance().isLoggedIn()) {
			super.onStart();
		} else {
			super.onStartWithoutInitViewData();
			if(mIsFirstTimeStart){ 
				Intent intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
			} else {
				displayDialog(R.string.loginActivity_alert_require_login, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
						new DialogCallbackListener() {
							@Override
							public void doPositiveClick() {
								Intent intent = new Intent(
										HyjUserActivity.this,
										LoginActivity.class);
								startActivity(intent);
							}

							@Override
							public void doNegativeClick() {
								finish();
							}
						});
			}
		}
		mIsFirstTimeStart = false;
	}
}
