package com.hoyoji.android.hyjframework.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.database.DataSetObserver;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.aaevent_android.R;
import com.hoyoji.hoyoji.models.Event;
import com.hoyoji.hoyoji.models.MoneyExpenseApportion;
import com.hoyoji.hoyoji.models.MoneyExpenseContainer;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.money.MoneyApportionField;

public class HyjFreeGameFormFragment extends HyjUserFormFragment {
	private MoneyApportionField mApportionFieldApportions = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.game_formfragment_freegame;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		Intent intent = getActivity().getIntent();
		String adapter = intent.getStringExtra("adapterArray");
		List<MoneyExpenseApportion> moneyApportions = new ArrayList<MoneyExpenseApportion>();
		if (adapter != null){
			
		}
		mApportionFieldApportions = (MoneyApportionField) getView().findViewById(R.id.moneyExpenseContainerFormFragment_apportionField);
		
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    super.onCreateOptionsMenu(menu, inflater);
	    
	}
	
	private void fillData() {
		
	}

	private void showValidatioErrors() {
		HyjUtil.displayToast(R.string.app_validation_error);

		
	}

	@Override
	public void onSave(View v) {
		super.onSave(v);

		fillData();

		
		
	}

	protected void doSave() {
	}
//	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		}
	}
}
