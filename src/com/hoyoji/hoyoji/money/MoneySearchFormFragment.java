package com.hoyoji.hoyoji.money;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjHttpGetExchangeRateAsyncTask;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeField;
import com.hoyoji.android.hyjframework.view.HyjImageField;
import com.hoyoji.android.hyjframework.view.HyjImageField.PictureItem;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.android.hyjframework.view.HyjSpinnerField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyApportion;
import com.hoyoji.hoyoji.models.MoneyExpense;
import com.hoyoji.hoyoji.models.MoneyExpenseApportion;
import com.hoyoji.hoyoji.models.MoneyExpenseCategory;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.MoneyApportionField.ApportionItem;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.money.moneycategory.MoneyExpenseCategoryListFragment;
import com.hoyoji.hoyoji.project.MemberListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;

public class MoneySearchFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_FRIEND_ID = 3;
	
	private HyjDateTimeField mDateTimeFieldStartDate = null;
	private HyjDateTimeField mDateTimeFieldEndDate = null;
	private HyjSelectorField mSelectorFieldMoneyAccount = null;
	private HyjSelectorField mSelectorFieldProject = null;
	private HyjSelectorField mSelectorFieldFriend = null;
	private HyjSpinnerField mSpinnerFieldDisplayType = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.money_dialogfragment_search;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();

		Intent intent = getActivity().getIntent();
		
		mDateTimeFieldStartDate = (HyjDateTimeField) getView().findViewById(R.id.searchDialogFragment_textField_startDate);
		final Long dateFrom = intent.getLongExtra("dateFrom", 0);
		if(dateFrom != 0){
			mDateTimeFieldStartDate.setDate(new Date(dateFrom));
		} else {
			mDateTimeFieldStartDate.setDate(null);
		}
		
		mDateTimeFieldEndDate = (HyjDateTimeField) getView().findViewById(R.id.searchDialogFragment_textField_endDate);
		final Long dateTo = intent.getLongExtra("dateTo", -1);
		if(dateTo != -1){
			mDateTimeFieldEndDate.setDate(new Date(dateTo));
		}
		
		mSelectorFieldMoneyAccount = (HyjSelectorField) getView().findViewById(R.id.searchDialogFragment_selectorField_moneyAccount);
		final String moneyAccountId = intent.getStringExtra("moneyAccountId");
		if(moneyAccountId != null){
			MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, moneyAccountId);
			if (moneyAccount != null) {
				mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
				mSelectorFieldMoneyAccount.setText(moneyAccount.getName() + "("
						+ moneyAccount.getCurrencyId() + ")");
			}
		}
		
		mSelectorFieldMoneyAccount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("excludeType", "Debt");
				
				MoneySearchFormFragment.this
						.openActivityWithFragmentForResult(
								MoneyAccountListFragment.class,
								R.string.moneyAccountListFragment_title_select_moneyAccount,
								bundle, GET_MONEYACCOUNT_ID);
			}
		});

		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(
				R.id.searchDialogFragment_selectorField_project);
		final String projectId = intent.getStringExtra("projectId");
		if(projectId != null){
			Project project = HyjModel.getModel(Project.class, projectId);
			if (project != null) {
				mSelectorFieldProject.setModelId(project.getId());
				mSelectorFieldProject.setText(project.getName() + "("
						+ project.getCurrencyId() + ")");
			}
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneySearchFormFragment.this
						.openActivityWithFragmentForResult(
								ProjectListFragment.class,
								R.string.projectListFragment_title_select_project,
								null, GET_PROJECT_ID);
			}
		});

		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(
				R.id.searchDialogFragment_selectorField_friend);
		final String friendUserId = intent.getStringExtra("friendUserId");
		Friend friend = null;
		if(friendUserId != null){
			friend = new Select().from(Friend.class).where("friendUserId=?", friendUserId).executeSingle();
		} else {
			final String localFriendId = intent.getStringExtra("localFriendId");
			if(localFriendId != null){
				friend = HyjModel.getModel(Friend.class, localFriendId);
			}	
		}
		if (friend != null) {
			mSelectorFieldFriend.setModelId(friend.getId());
			mSelectorFieldFriend.setText(friend.getDisplayName());
		}
		mSelectorFieldFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneySearchFormFragment.this
						.openActivityWithFragmentForResult(
								FriendListFragment.class,
								R.string.friendListFragment_title_select_friend_payee,
								null, GET_FRIEND_ID);
			}
		});
		
		mSpinnerFieldDisplayType = (HyjSpinnerField)getView().findViewById(R.id.searchDialogFragment_spinnerField_displayType);
		mSpinnerFieldDisplayType.setItems(R.array.searchDialogFragment_spinnerField_displayType_array, new String[] {"Project", "Personal"});
		final String displayType = intent.getStringExtra("displayType");
		if(displayType != null){
			mSpinnerFieldDisplayType.setSelectedValue(displayType);
		}
	}
	
	@Override
	public void onSave(View v) {
		Intent intent = new Intent();
		intent.putExtra("dateFrom", this.mDateTimeFieldStartDate.getDateInMillis());
		intent.putExtra("dateTo", this.mDateTimeFieldEndDate.getDateInMillis());
		intent.putExtra("projectId", this.mSelectorFieldProject.getModelId());
		intent.putExtra("moneyAccountId", this.mSelectorFieldMoneyAccount.getModelId());
		intent.putExtra("friendId", this.mSelectorFieldFriend.getModelId());
		intent.putExtra("displayType", this.mSpinnerFieldDisplayType.getSelectedValue());
		
		getActivity().setResult(Activity.RESULT_OK, intent);
		getActivity().finish();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case GET_MONEYACCOUNT_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				MoneyAccount moneyAccount = MoneyAccount.load(
						MoneyAccount.class, _id);
				mSelectorFieldMoneyAccount.setText(moneyAccount.getName() + "("
						+ moneyAccount.getCurrencyId() + ")");
				mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
			}
			break;
		case GET_PROJECT_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				Project project = Project.load(Project.class, _id);
				mSelectorFieldProject.setText(project.getName() + "(" + project.getCurrencyId() + ")");
				mSelectorFieldProject.setModelId(project.getId());
			}
			break;

		case GET_FRIEND_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				Friend friend = Friend.load(Friend.class, _id);
				mSelectorFieldFriend.setText(friend.getDisplayName());
				mSelectorFieldFriend.setModelId(friend.getId());
			}
			break;
		}
		
	}
	
}
