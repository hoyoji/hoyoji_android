package com.hoyoji.hoyoji.models;

import java.util.List;
import java.util.UUID;

import android.provider.BaseColumns;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.hoyoji.R;

@Table(name = "MoneyExpense", id = BaseColumns._ID)

public class MoneyExpense extends HyjModel{

	@Column(name = "id", index = true, unique = true)
	private String mId;
	
	@Column(name = "amount")
	private Double mAmount;
	
	
	
	@Override
	public void validate(HyjModelEditor hyjModelEditor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

}
