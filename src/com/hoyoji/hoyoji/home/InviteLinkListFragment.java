package com.hoyoji.hoyoji.home;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostJSONLoader;
import com.hoyoji.android.hyjframework.server.HyjJSONListAdapter;
import com.hoyoji.hoyoji.friend.UserFormFragment;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyExpenseApportion;
import com.hoyoji.hoyoji.models.MoneyExpenseCategory;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.project.MemberFormFragment;
import com.hoyoji.hoyoji_android.R;

public class InviteLinkListFragment extends HyjUserListFragment {
	private final static int INVITELINK_CHANGESTATE = 1;
	@Override
	public Integer useContentView() {
		return R.layout.link_listfragment_invite;
	}

	@Override
	public Integer useToolbarView() {
		return super.useToolbarView();
	}

	@Override
	public void onInitViewData() {
		onQueryLinkList();
	}
	
//	 @Override
//	public void onResume() {
//         // TODO Auto-generated method stub
//         super.onResume();
//         ((HyjJSONListAdapter) this.getListAdapter()).clear();
//         onQueryLinkList();
//     }

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
				R.layout.link_listitem_invite, 
						new String[] { "state", "date", "type", "description" }, 
						new int[] { R.id.inviteFriendLinkListItem_state, R.id.inviteFriendLinkListItem_date, R.id.inviteFriendLinkListItem_type, R.id.inviteFriendLinkListItem_description });
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if(id == -1) {
			 return;
		}
		if (id >= 0) {
			final JSONObject jsonInviteLink = (JSONObject) l.getAdapter().getItem(position);
			
			Bundle bundle = new Bundle();
			bundle.putString("INVITELINK_JSON_OBJECT", jsonInviteLink.toString());
			bundle.putInt("position", position);
			openActivityWithFragmentForResult(InviteLinkFormFragment.class, R.string.inviteLinkFormFragment_title, bundle, INVITELINK_CHANGESTATE);
		}
	}

	public void onQueryLinkList() {
		JSONObject data = new JSONObject();
		try {
			data.put("__dataType", "InviteLink");
			data.put("__limit", getListPageSize());
			data.put("ownerUserId", HyjApplication.getInstance().getCurrentUser().getId());
			data.put("__offset", 0);
			data.put("__orderBy", "date ASC");
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
	}

	@Override
	public void doFetchMore(ListView l, int offset, int pageSize) {
		Loader loader = getLoaderManager().getLoader(0);
		if(loader != null && ((HyjHttpPostJSONLoader)loader).isLoading()){
			return;
		}
		this.setFooterLoadStart(l);
		JSONObject data = new JSONObject();
		try {
			data.put("__dataType", "InviteLink");
			data.put("__limit", getListPageSize());
			data.put("ownerUserId", HyjApplication.getInstance().getCurrentUser().getId());
			data.put("__offset", offset);
			data.put("__orderBy", "date ASC");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Bundle bundle = new Bundle();
		bundle.putString("target", "findData");
		bundle.putString("postData", (new JSONArray()).put(data).toString());
		if(loader == null){
			getLoaderManager().restartLoader(0, bundle, this);
			loader = getLoaderManager().getLoader(0);
		}
		((HyjHttpPostJSONLoader) loader).changePostQuery(bundle);
	}

	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if (view.getId() == R.id.inviteFriendLinkListItem_state) {
			((TextView) view).setText(cursor.getColumnIndex("state"));
			return true;
		} else if (view.getId() == R.id.inviteFriendLinkListItem_date) {
			((TextView) view).setText(cursor.getColumnIndex("date"));
			return true;
		} else if (view.getId() == R.id.inviteFriendLinkListItem_type) {
			((TextView) view).setText(cursor.getColumnIndex("type"));
			return true;
		} else if (view.getId() == R.id.inviteFriendLinkListItem_description) {
			((TextView) view).setText(cursor.getColumnIndex("description"));
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void setFooterLoadFinished(ListView l, int count){
		int offset = l.getFooterViewsCount() + l.getHeaderViewsCount();
        super.setFooterLoadFinished(l, l.getAdapter().getCount() + count - offset);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case INVITELINK_CHANGESTATE:
			if (resultCode == Activity.RESULT_OK) {
				String state = data.getStringExtra("state");
				int position = data.getIntExtra("position", -1);
				JSONObject object = ((HyjJSONListAdapter) this.getListAdapter()).getItem(position);
				try {
					object.put("state", state);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				((HyjJSONListAdapter) this.getListAdapter()).notifyDataSetChanged();
			}
			break;
		}
	}
}
