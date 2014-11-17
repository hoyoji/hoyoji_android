package com.hoyoji.hoyoji.money;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.models.MoneyTemplate;

public class MoneyTemplateListFragment extends HyjUserListFragment {

	@Override
	public Integer useContentView() {
		return R.layout.money_listfragment_moneytemplate;
	}

	@Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.home_listitem_row,
				null,
				new String[] {"data", "data", "data"},
				new int[] {R.id.homeListItem_picture, R.id.homeListItem_date, R.id.homeListItem_amount},
				0); 
	}	

	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		super.onCreateLoader(arg0, arg1);
		Object loader = new CursorLoader(getActivity(),
				ContentProvider.createUri(MoneyTemplate.class, null),
				null, null, null, null
			);
		return (Loader<Object>)loader;
	}


	@Override
	public void onInitViewData() {
		super.onInitViewData();
	}
	
	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		if(id == -1) {
			 return;
		}
		MoneyTemplate template = HyjModel.load(MoneyTemplate.class, id);
		if(template.getType().equals("MoneyExpense")) {
			openActivityWithFragment(MoneyExpenseContainerFormFragment.class, R.string.moneyExpenseFormFragment_title_addnew, null);
		} else if(template.getType().equals("MoneyIncome")) {
			openActivityWithFragment(MoneyIncomeContainerFormFragment.class, R.string.moneyIncomeFormFragment_title_addnew, null);
		}
		
    }
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
//		String id = cursor.getString(cursor.getColumnIndex("id"));
//		String type = cursor.getString(cursor.getColumnIndex("type")); 
		String data = cursor.getString(cursor.getColumnIndex("data"));
		JSONObject jsonObj = null;
		try {
			jsonObj = new JSONObject(data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
//		HyjModel model;
//		if(type.equals("MoneyExpense")){
//			model = new MoneyExpense();
//			model.loadFromJSON(jsonObj, true);
//		}
		
		if(view.getId() == R.id.homeListItem_date){
//			if(type.equals("MoneyExpense")){
				((HyjDateTimeView)view).setText(jsonObj.optString("date"));
//			}
			return true;
			
		
		} else if(view.getId() == R.id.homeListItem_amount){
			((HyjNumericView)view).setPrefix("¥");
			((HyjNumericView)view).setNumber((jsonObj.optDouble("amount")));
			return true;
		}else if(view.getId() == R.id.homeListItem_picture){
//			HyjImageView imageView = (HyjImageView)view;
//			imageView.setImage(cursor.getString(columnIndex));
			((HyjImageView)view).setImage(jsonObj.optString("pictureId"));
			return true;
		} else {
			return true;
		}
	}
}
