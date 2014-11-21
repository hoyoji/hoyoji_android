package com.hoyoji.hoyoji.friend;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.content.ContentProvider;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserListFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostJSONLoader;
import com.hoyoji.android.hyjframework.server.HyjJSONListAdapter;
import com.hoyoji.android.hyjframework.view.HyjDateTimeView;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.models.MoneyTemplate;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji_android.R;

public class ImportFriendListFragment extends HyjUserListFragment implements OnQueryTextListener{
	private final static int INVITELINK_CHANGESTATE = 1;
	protected SearchView mSearchView;
	protected String mSearchText = "";
	
	Context mContext = null;  
	 
    /**获取库Phon表字段**/  
    private static final String[] PHONES_PROJECTION = new String[] { Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID,Phone.CONTACT_ID };
    /**联系人显示名称**/  
    private static final int PHONES_DISPLAY_NAME_INDEX = 0; 
    /**电话号码**/  
    private static final int PHONES_NUMBER_INDEX = 1;
    /**头像ID**/  
    private static final int PHONES_PHOTO_ID_INDEX = 2;
    /**联系人的ID**/  
    private static final int PHONES_CONTACT_ID_INDEX = 3; 
    /**联系人名称**/  
    private ArrayList<String> mContactsName = new ArrayList<String>();
    /**联系人电话号码**/  
    private ArrayList<String> mContactsNumber = new ArrayList<String>();
    /**联系人头像**/  
    private ArrayList<Bitmap> mContactsPhonto = new ArrayList<Bitmap>();
    ListView mListView = null;  
//    MyListAdapter myAdapter = null; 
	
	@Override
	public Integer useContentView() {
		return R.layout.friend_listfragment_import_friend;
	}

	@Override
	public Integer useToolbarView() {
		return super.useToolbarView();
	}

	@Override
	public void onInitViewData() {
		mSearchView = (SearchView) getView().findViewById(R.id.linkListFragment_inviteLink_searchView);
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

		this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
//		onQueryLinkList();
		importCommunicationList();
	}
	
	public void importCommunicationList(){
		mContext = getActivity();  
	    mListView = this.getListView();  
	    /**得到手机通讯录联系人信息**/  
	    getPhoneContacts();
//	    myAdapter = new MyListAdapter(getActivity());  
//	    setListAdapter(myAdapter);  
	 
//	    mListView.setOnItemClickListener(new OnItemClickListener() {  
//	 
//	        @Override  
//	        public void onItemClick(AdapterView<?> adapterView, View view,int position, long id) {  
//	        //调用系统方法拨打电话  
//		        Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mContactsNumber.get(position)));  
//		        startActivity(dialIntent);  
//	        }  
//	    }); 
	}
	
	/**得到手机通讯录联系人信息**/  
    private void getPhoneContacts() {  
	    ContentResolver resolver = mContext.getContentResolver();
	    // 获取手机联系人  
	    Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,PHONES_PROJECTION, null, null, null);
	    if (phoneCursor != null) {  
	        while (phoneCursor.moveToNext()) {
		        //得到手机号码  
		        String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);  
		        //当手机号码为空的或者为空字段 跳过当前循环  
		        if (TextUtils.isEmpty(phoneNumber))  
		            continue;  
		        //得到联系人名称  
		        String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);  
		        //得到联系人ID  
		        Long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);  
		        //得到联系人头像ID  
		        Long photoid = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);  
		        //得到联系人头像Bitamp  
		        Bitmap contactPhoto = null; 
		        //photoid 大于0 表示联系人有头像 如果没有给此人设置头像则给他一个默认的  
		        if(photoid > 0 ) {  
		            Uri uri =ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contactid);  
		            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);  
		            contactPhoto = BitmapFactory.decodeStream(input);  
		        }else {  
		            contactPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_person);  
		        }
		        mContactsName.add(contactName);  
		        mContactsNumber.add(phoneNumber);  
		        mContactsPhonto.add(contactPhoto);  
	        } 
	        phoneCursor.close();  
	    }  
    }  
      
    /**得到手机SIM卡联系人人信息**/  
    private void getSIMContacts() {  
	    ContentResolver resolver = mContext.getContentResolver();  
	    // 获取Sims卡联系人  
	    Uri uri = Uri.parse("content://icc/adn");  
	    Cursor phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null,null); 
	    if (phoneCursor != null) {  
	        while (phoneCursor.moveToNext()) {
	        // 得到手机号码  
	        String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);  
	        // 当手机号码为空的或者为空字段 跳过当前循环  
	        if (TextUtils.isEmpty(phoneNumber))  
	            continue;  
	        // 得到联系人名称  
	        String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX); 
	        //Sim卡中没有联系人头像  
	        mContactsName.add(contactName);  
	        mContactsNumber.add(phoneNumber);  
	        }
	        phoneCursor.close();  
	    }  
    }  
      
//    class MyListAdapter extends BaseAdapter {  
//	    public MyListAdapter(Context context) {  
//	        mContext = context;  
//	    }  
//	 
//	    public int getCount() {  
//	        //设置绘制数量  
//	        return mContactsName.size();  
//	    }  
//	 
//	    @Override  
//	    public boolean areAllItemsEnabled() {  
//	        return false;  
//	    }  
//	 
//	    public Object getItem(int position) {  
//	        return position;  
//	    }  
//	 
//	    public long getItemId(int position) {  
//	        return position;  
//	    }  
//	 
//	    public View getView(int position, View convertView, ViewGroup parent) {  
//	        ImageView iamge = null;  
//	        TextView title = null;  
//	        TextView text = null;  
//	        if (convertView == null) {  
//	        convertView = LayoutInflater.from(mContext).inflate(R.layout.colorlist, null);  
//	        iamge = (ImageView) convertView.findViewById(R.id.color_image);  
//	        title = (TextView) convertView.findViewById(R.id.color_title);  
//	        text = (TextView) convertView.findViewById(R.id.color_text);  
//	        }  
//	        //绘制联系人名称  
//	        title.setText(mContactsName.get(position));  
//	        //绘制联系人号码  
//	        text.setText(mContactsNumber.get(position));  
//	        //绘制联系人头像  
//	        iamge.setImageBitmap(mContactsPhonto.get(position));  
//	        return convertView;  
//	    }  
// 
//    }  

    @Override
	public ListAdapter useListViewAdapter() {
		return new SimpleCursorAdapter(getActivity(),
				R.layout.friend_listitem_import,
				null,
				new String[] { "pictureId", "name", "phoneNumber" },
				new int[] {R.id.importFriendListFragment_picture,  R.id.importFriendListFragment_name ,R.id.importFriendListFragment_phoneNumber},
				0); 
	}	

	@Override
	public Loader<Object> onCreateLoader(int arg0, Bundle arg1) {
		super.onCreateLoader(arg0, arg1);
		Object loader = new CursorLoader(getActivity(),
				Phone.CONTENT_URI, new String[] { Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID, Phone.CONTACT_ID },
				null, null, Phone.DISPLAY_NAME
			);
		return (Loader<Object>)loader;
	}

	@Override
	public void onLoadFinished(Loader<Object> loader, Object data) {
		super.onLoadFinished(loader, data);
		// Set the new data in the adapter.
		((HyjJSONListAdapter) this.getListAdapter()).addData((List<JSONObject>) data);
	}

	@Override
	public void onLoaderReset(Loader<Object> loader) {
		super.onLoaderReset(loader);
		// Clear the data in the adapter.
		((HyjJSONListAdapter) this.getListAdapter()).clear();
	}

//	@Override
//	public ListAdapter useListViewAdapter() {
//		return new HyjJSONListAdapter(getActivity(),
//				R.layout.friend_listitem_import, 
//						new String[] { "pictureId", "name", "phoneNumber"}, 
//						new int[] { R.id.inviteFriendLinkListItem_state, R.id.inviteFriendLinkListItem_date, R.id.inviteFriendLinkListItem_type, R.id.inviteFriendLinkListItem_description });
//	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if(l.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE){
			return;
		}
		if(id == -1) {
			 return;
		}
		if (id >= 0) {
			final JSONObject jsonInviteLink = (JSONObject) l.getAdapter().getItem(position);
			
			Bundle bundle = new Bundle();
			bundle.putString("INVITELINK_JSON_OBJECT", jsonInviteLink.toString());
			bundle.putInt("position", position);
			openActivityWithFragmentForResult(ImportFriendListFragment.class, R.string.inviteLinkFormFragment_title, bundle, INVITELINK_CHANGESTATE);
		}
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
			data.put("title", mSearchText);
			data.put("description", mSearchText);
			data.put("__dataType", "InviteLink");
			data.put("__limit", getListPageSize());
			data.put("ownerUserId", HyjApplication.getInstance().getCurrentUser().getId());
			data.put("__offset", 0);
			data.put("__orderBy", "date DESC");
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

//	public void onQueryLinkList() {
//		JSONObject data = new JSONObject();
//		try {
//			if (mSearchText.length() == 0) {
//				data.put("__dataType", "InviteLink");
//				data.put("ownerUserId", HyjApplication.getInstance().getCurrentUser().getId());
//				data.put("__limit", getListPageSize());
//				data.put("__offset", 0);
//				data.put("__orderBy", "date DESC");
//			}else{
//				data.put("title", mSearchText);
//				data.put("description", mSearchText);
//				data.put("__dataType", "InviteLink");
//				data.put("ownerUserId", HyjApplication.getInstance().getCurrentUser().getId());
//				data.put("__limit", getListPageSize());
//				data.put("__offset", 0);
//				data.put("__orderBy", "date ASC");
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//
//		Bundle bundle = new Bundle();
//		bundle.putString("target", "findData");
//		bundle.putString("postData", (new JSONArray()).put(data).toString());
//		if (getLoaderManager().getLoader(0) != null) {
//			getLoaderManager().destroyLoader(0);
//		}
//		getLoaderManager().restartLoader(0, bundle, this);
//	}

	@Override
	public void doFetchMore(ListView l, int offset, int pageSize) {
		Loader loader = getLoaderManager().getLoader(0);
		if(loader != null && ((HyjHttpPostJSONLoader)loader).isLoading()){
			return;
		}
		this.setFooterLoadStart(l);
		JSONObject data = new JSONObject();
		try {
			data.put("title", mSearchText);
			data.put("description", mSearchText);
			data.put("__dataType", "InviteLink");
			data.put("__limit", pageSize);
			data.put("ownerUserId", HyjApplication.getInstance().getCurrentUser().getId());
			data.put("__offset", offset);
			data.put("__orderBy", "date DESC");
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
		String id = cursor.getString(cursor.getColumnIndex("id"));
		String type = cursor.getString(cursor.getColumnIndex("type")); 
		String data = cursor.getString(cursor.getColumnIndex("data"));
		JSONObject jsonObj = null;
		try {
			jsonObj = new JSONObject(data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if(view.getId() == R.id.homeListItem_amount){
			((HyjNumericView)view).setPrefix(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencySymbol());
			((HyjNumericView)view).setNumber((jsonObj.optDouble("amount")*jsonObj.optDouble("exchangeRate")));
			return true;
		}else if(view.getId() == R.id.homeListItem_picture){
			HyjImageView imageView = (HyjImageView)view;
			
			imageView.setDefaultImage(R.drawable.ic_action_picture_white);
			if(type.equals("MoneyExpense")){
				imageView.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			} else {
				imageView.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			}
			imageView.setImage((Picture)null);
			return true;
		}else if(view.getId() == R.id.homeListItem_remark){
			if(jsonObj.optString("remark").equals("") || jsonObj.optString("remark") == null){
				((TextView)view).setText("无备注");
			}else{
				((TextView)view).setText(jsonObj.optString("remark"));
			}
			return true;
		}else if(view.getId() == R.id.homeListItem_subTitle){
			Project project = HyjModel.getModel(Project.class, jsonObj.optString("projectId"));
			((TextView)view).setText(project.getDisplayName());
			return true;
		}else if(view.getId() == R.id.homeListItem_title){
			if(type.equals("MoneyExpense")){
				((TextView)view).setText(jsonObj.optString("moneyExpenseCategoryMain"));
				((TextView)view).setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			}else{
				((TextView)view).setText(jsonObj.optString("moneyIncomeCategoryMain"));
				((TextView)view).setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			}
			return true;
		}else {
			return false;
		}
	}

//	@Override
//	public boolean setViewValue(View view, Object json, String name) {
//		JSONObject jsonObject = (JSONObject)json;
//		if (view.getId() == R.id.inviteFriendLinkListItem_state) {
//			((TextView) view).setText(jsonObject.optString(name));
//			return true;
//		} else if (view.getId() == R.id.inviteFriendLinkListItem_date) {
//			((HyjDateTimeView) view).setText(jsonObject.optString(name));
//			return true;
//		} else if (view.getId() == R.id.inviteFriendLinkListItem_type) {
//			((TextView) view).setText(jsonObject.optString(name));
//			return true;
//		} else if (view.getId() == R.id.inviteFriendLinkListItem_description) {
//			((TextView) view).setText(jsonObject.optString(name));
//			return true;
//		} else {
//			return false;
//		}
//	}
	
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
