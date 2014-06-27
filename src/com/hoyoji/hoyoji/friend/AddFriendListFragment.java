package com.hoyoji.hoyoji.friend;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.Base64;
import android.view.ContextMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.server.HyjHttpPostJSONLoader;
import com.hoyoji.android.hyjframework.server.HyjJSONListAdapter;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.message.FriendMessageFormFragment;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.Message;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.User;

public class AddFriendListFragment extends HyjUserListFragment implements
		OnQueryTextListener {
	protected SearchView mSearchView;
	protected String mSearchText = "";

	@Override
	public Integer useContentView() {
		return R.layout.friend_listfragment_add_friend;
	}

	@Override
	public Integer useToolbarView() {
		return super.useToolbarView();
	}

	@Override
	public void onInitViewData() {
		mSearchView = (SearchView) getView().findViewById(
				R.id.friendListFragment_addFriend_searchView);
		mSearchView.setOnQueryTextListener(this);
		mSearchView.setSubmitButtonEnabled(true);
		
		ImageView searchImage = (ImageView) mSearchView.findViewById(R.id.search_go_btn);
		if(searchImage != null){
			searchImage.setImageResource(R.drawable.ic_action_search);
		}
		ImageView magImage = (ImageView) mSearchView.findViewById(R.id.search_mag_icon);
		if(magImage != null){
			magImage.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
		}
//		getView().findViewById(
//				R.id.friendListFragment_searchFriend).setOnClickListener(new OnClickListener(){
//					@Override
//					public void onClick(View v) {
//						mSearchView.callOnClick();
//					}
//					
//				});
				
//        InputMethodManager inputMethodManager =  (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//        inputMethodManager.toggleSoftInputFromWindow(mSearchView.getApplicationWindowToken(),  InputMethodManager.SHOW_IMPLICIT, 0);

		this.getActivity()
				.getWindow()
				.setSoftInputMode(
						WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
	}

	@Override
	public void initLoader(int loaderId) {
		// do not init loader... wait for the user to start search
	}

	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		super.onCreateLoader(arg0, arg1);
		Object loader = new HyjHttpPostJSONLoader(getActivity(), arg1);
		return (Loader<Object>) loader;
	}

	@Override
	public void onLoadFinished(Loader<Object> loader, Object data) {
		super.onLoadFinished(loader, data);
		// Set the new data in the adapter.
		((HyjJSONListAdapter) this.getListAdapter())
				.addData((List<JSONObject>) data);
	}

	@Override
	public void onLoaderReset(Loader<Object> loader) {
		super.onLoaderReset(loader);
		// Clear the data in the adapter.
		((HyjJSONListAdapter) this.getListAdapter()).clear();
	}

	@Override
	public ListAdapter useListViewAdapter() {
		return new HyjJSONListAdapter(getActivity(),
				R.layout.friend_listitem_add_friend, new String[] {
						"pictureId", "nickName" }, new int[] {
						R.id.friendListItem_add_picture,
						R.id.friendListItem_add_nickName });
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (id >= 0) {
			final JSONObject jsonUser = (JSONObject) l.getAdapter().getItem(
					position);
			
			Bundle bundle = new Bundle();
			bundle.putString("USER_JSON_OBJECT", jsonUser.toString());
			openActivityWithFragment(UserFormFragment.class, R.string.friendFormFragment_title, bundle);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// do nothing, clear the delete item from super class
	}

	@Override
	public boolean onQueryTextChange(String arg0) {
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String searchText) {
		mSearchText = searchText.trim();
		if (searchText.length() == 0) {
			HyjUtil.displayToast("请输入查询条件");
			return true;
		}

	    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getWindow().getCurrentFocus().getWindowToken(), 0);
   
		JSONObject data = new JSONObject();
		try {
			data.put("userName", mSearchText);
			data.put("nickName", mSearchText);
			data.put("__dataType", "User");
			data.put("__limit", mListPageSize);
			data.put("__offset", 0);
			data.put("__orderBy", "userName ASC");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Bundle bundle = new Bundle();
		bundle.putString("target", "findData");
		bundle.putString("postData", (new JSONArray()).put(data).toString());
		if (getLoaderManager().getLoader(0) != null) {
			getLoaderManager().destroyLoader(0);
		}
		getLoaderManager().restartLoader(0, bundle, this);
		return true;
	}

	@Override
	public void doFetchMore(ListView l, int offset, int pageSize) {
		this.setFooterLoadStart(l);
		JSONObject data = new JSONObject();
		try {
			data.put("userName", mSearchText);
			data.put("nickName", mSearchText);
			data.put("__dataType", "User");
			data.put("__limit", pageSize);
			data.put("__offset", offset);
			data.put("__orderBy", "userName ASC");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Bundle bundle = new Bundle();
		bundle.putString("target", "findData");
		bundle.putString("postData", (new JSONArray()).put(data).toString());
		Loader loader = getLoaderManager().getLoader(0);
		((HyjHttpPostJSONLoader) loader).changePostQuery(bundle);
	}

	@Override
	public boolean setViewValue(View view, Object object, String field) {
		JSONObject jsonObj = (JSONObject) object;
		if (view.getId() == R.id.friendListItem_add_nickName) {
			try {
				((TextView) view).setText(HyjUtil.ifJSONNull(jsonObj,
						"nickName", jsonObj.optString("userName")));
			} catch (Exception e) {
				((TextView) view).setText("");
			}
			return true;
		} else if (view.getId() == R.id.friendListItem_add_picture) {
			if (!jsonObj.isNull(field)) {
				((HyjImageView) view).loadRemoteImage(jsonObj.optString(field));
			} else {
				((HyjImageView) view).setImage((Picture) null);
			}
			return true;
		} else {
			return false;
		}
	}

}
