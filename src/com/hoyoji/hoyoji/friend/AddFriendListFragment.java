package com.hoyoji.hoyoji.friend;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.ContextMenu;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjUserActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.server.HyjHttpPostJSONLoader;
import com.hoyoji.android.hyjframework.server.HyjJSONListAdapter;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.hoyoji.LoginActivity;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.RegisterActivity;
import com.hoyoji.hoyoji.models.Friend;
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
			HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
				@Override
				public void finishCallback(Object object) {
					JSONArray jsonArray = (JSONArray) object;
					if (jsonArray.optJSONArray(0).length() > 0) {
						// 好友已经在服务器上存在
						((HyjActivity) AddFriendListFragment.this.getActivity())
								.dismissProgressDialog();
						HyjUtil.displayToast(R.string.friendListFragment_addFriend_error_exists);
						
					} else if (jsonUser.optString("id").equals(
							HyjApplication.getInstance().getCurrentUser()
									.getId())) {
						// 添加自己为好友
						addSelfAsFriend();
						((HyjActivity) AddFriendListFragment.this.getActivity())
								.dismissProgressDialog();
						
					} else {
						
						addFriendWithoutAuthorization(jsonUser);
						
					}
				}

				@Override
				public void errorCallback(Object object) {
					((HyjActivity) AddFriendListFragment.this.getActivity())
							.dismissProgressDialog();
					JSONObject json = (JSONObject) object;
					HyjUtil.displayToast(json.optJSONObject("__summary")
							.optString("msg"));
				}
			};

			try {
				JSONObject data = new JSONObject();
				data.put("__dataType", "Friend");
				data.put("friendUserId", jsonUser.optString("id"));
				data.put("ownerUserId", HyjApplication.getInstance()
						.getCurrentUser().getId());
				HyjHttpPostAsyncTask.newInstance(serverCallbacks,
						"[" + data.toString() + "]", "getData");
				((HyjActivity) this.getActivity()).displayProgressDialog(
						R.string.addFriendListFragment_title_add,
						R.string.friendListFragment_addFriend_progress_adding);
			} catch (JSONException e) {
				e.printStackTrace();
			}
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
	public void doFetchMore(int offset, int pageSize) {
		this.setFooterLoadStart();
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
	
	private void addSelfAsFriend(){
		((HyjActivity) AddFriendListFragment.this.getActivity())
		.displayDialog(
				-1,
				R.string.friendListFragment_addFriend_addSelf_title,
				R.string.alert_dialog_yes,
				R.string.alert_dialog_no, -1,
				new DialogCallbackListener() {
					@Override
					public void doPositiveClick(
							Object object) {
						final Friend newFriend = new Friend();
						newFriend
								.setFriendUser(HyjApplication
										.getInstance()
										.getCurrentUser());
						newFriend
								.setOwnerUserId(HyjApplication
										.getInstance()
										.getCurrentUser()
										.getId());
						newFriend
								.setFriendCategoryId(HyjApplication
										.getInstance()
										.getCurrentUser()
										.getUserData()
										.getDefaultFriendCategoryId());
						HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
							@Override
							public void finishCallback(
									Object object) {
								newFriend.save();
								((HyjActivity) AddFriendListFragment.this
										.getActivity())
										.dismissProgressDialog();
								HyjUtil.displayToast(R.string.friendListFragment_addFriend_progress_add_success);
							}

							@Override
							public void errorCallback(
									Object object) {
								((HyjActivity) AddFriendListFragment.this
										.getActivity())
										.dismissProgressDialog();
								JSONObject json = (JSONObject) object;
								HyjUtil.displayToast(json
										.optJSONObject(
												"__summary")
										.optString(
												"msg"));
							}
						};

						HyjHttpPostAsyncTask
								.newInstance(
										serverCallbacks,
										"["
												+ newFriend
														.toJSON()
														.toString()
												+ "]",
										"postData");
						((HyjActivity) AddFriendListFragment.this
								.getActivity())
								.displayProgressDialog(
										R.string.addFriendListFragment_title_add,
										R.string.friendListFragment_addFriend_progress_adding);
					}

					@Override
					public void doNegativeClick() {
					}
				});
	}
	
	private void addFriendWithoutAuthorization(final JSONObject jsonUser){
		// send message to server to request add new friend
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				loadNewlyAddedFriend(jsonUser);
			}

			@Override
			public void errorCallback(Object object) {
				((HyjActivity) AddFriendListFragment.this
						.getActivity()).dismissProgressDialog();
				JSONObject json = (JSONObject) object;
				HyjUtil.displayToast(json.optJSONObject(
						"__summary").optString("msg"));
			}
		};

		JSONObject data = new JSONObject();
		try {
			data.put("__dataType", "Message");
			data.put("id", UUID.randomUUID().toString());
			data.put("toUserId", jsonUser.optString("id"));
			data.put("fromUserId", HyjApplication.getInstance()
					.getCurrentUser().getId());
			data.put("type", "System.Friend.AddResponse");
			data.put("messageState", "new");
			data.put("messageTitle", "好友请求");
			data.put("date",
					HyjUtil.formatDateToIOS(new Date()));
			data.put("detail", "用户"
					+ HyjApplication.getInstance()
							.getCurrentUser().getDisplayName()
					+ "成功添加您为好友");
			data.put("messageBoxId",
					jsonUser.optString("messageBoxId"));
			data.put("ownerUserId", jsonUser.optString("id"));
			HyjHttpPostAsyncTask.newInstance(serverCallbacks,
					"[" + data.toString() + "]", "postData");

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void loadNewlyAddedFriend(final JSONObject jsonUser) {
		// load new friend from server
		HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
			@Override
			public void finishCallback(Object object) {
				JSONObject jsonFriend;
				try {
					jsonFriend = ((JSONArray) object).getJSONArray(0).getJSONObject(0);
					Friend newFriend = HyjModel.getModel(Friend.class,
							jsonFriend.optString("id"));
					if (newFriend == null) {
						newFriend = new Friend();
					}
					newFriend.loadFromJSON(jsonFriend);

					User newUser = HyjModel.getModel(User.class,
							jsonUser.optString("id"));
					if (newUser == null) {
						newUser = new User();
					}
					newUser.loadFromJSON(jsonUser);

					newUser.save();
					newFriend.save();
					
					((HyjActivity) AddFriendListFragment.this.getActivity())
					.dismissProgressDialog();
					HyjUtil.displayToast(R.string.friendListFragment_addFriend_progress_add_success);
					AddFriendListFragment.this.getActivity().finish();
				} catch (JSONException e) {
					e.printStackTrace();
					((HyjActivity) AddFriendListFragment.this.getActivity())
					.dismissProgressDialog();
				}
			}

			@Override
			public void errorCallback(Object object) {
				((HyjActivity) AddFriendListFragment.this.getActivity())
						.dismissProgressDialog();
				JSONObject json = (JSONObject) object;
				HyjUtil.displayToast(json.optJSONObject("__summary").optString(
						"msg"));
			}
		};

		JSONObject data = new JSONObject();
		try {
			data.put("__dataType", "Friend");
			data.put("ownerUserId", HyjApplication.getInstance()
					.getCurrentUser().getId());
			data.put("friendUserId", jsonUser.optString("id"));
			HyjHttpPostAsyncTask.newInstance(serverCallbacks,
					"[" + data.toString() + "]", "getData");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
