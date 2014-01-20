package com.hoyoji.android.hyjframework.fragment;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.hoyoji.hoyoji.R;

public abstract class HyjUserFormFragment extends HyjUserFragment {

	@Override
	public Integer useOptionsMenuView(){
		return R.menu.form_fragment;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.formFragment_action_save:
			onSave();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onInitViewData (){
			final Button button = (Button) getView().findViewById(R.id.button_save);
		    button.setOnClickListener(
		        new OnClickListener() {
		            @Override
		            public void onClick(View v) {
		                onSave();
		            }
		        }
		    );
	}
	
	public void onSave(){
		onSave(null);
	}
	
	public void onSave(View v){
		
	}
}
