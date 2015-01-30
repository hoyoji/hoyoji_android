package com.hoyoji.android.hyjframework.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.aaevent_android.R;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.Picture;

public class HyjFreeGameFormFragment extends HyjUserFragment {
	private GridView mGridView = null;
	private Button app_action_game_start = null;
	private Button app_action_game_free = null;
	private int oldPosition = -1;
	ArrayList<HashMap<String, Object>> lstItem = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.game_formfragment_freegame;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		Intent intent = getActivity().getIntent();
		String adapterString = intent.getStringExtra("adapterJSONArray");
		JSONArray templateApportions = null;
		try {
			if (adapterString != null){
					templateApportions = new JSONArray(adapterString);
			}
			 //生成动态数组，并且转入数据  
		      lstItem = new ArrayList<HashMap<String, Object>>();  
		      
		      for (int j = 0; j < templateApportions.length(); j++) {
		        JSONObject templateApportion = templateApportions.getJSONObject(j);
		        Friend friend = null;
		        if(templateApportion.optString("friendUserId") != null) {
		        	friend = new Select().from(Friend.class).where("friendUserId=?",templateApportion.optString("friendUserId")).executeSingle();
		        } else if(templateApportion.optString("localFriendId") != null) {
		        	friend = new Select().from(Friend.class).where("localFriendId=?",templateApportion.optString("localFriendId")).executeSingle();
		        }
		        if (friend != null) {
			        HashMap<String, Object> map = new HashMap<String, Object>();
//			        imageView.setDefaultImage(R.drawable.ic_action_person_white);
//					if(userId != null){
//						User user = HyjModel.getModel(User.class, userId);
//						if(user != null){
//							imageView.setImage(user.getPictureId());
//						} else {
//							imageView.setImage((Picture)null);
//						}
//						if(HyjApplication.getInstance().getCurrentUser().getId().equals(userId)){
//							imageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_red));
//						} else {
//							imageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_green));
//						}
//					} else {
//						imageView.setImage((Picture)null);
//						imageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_yellow));
//					}
			        map.put("isSelected", 0);
			        
					if(friend.getFriendUser() != null) {
						if(friend.getFriendUser().getPictureId() != null) {
							map.put("itemImage", friend.getFriendUser().getPicture());//添加图像资源的ID  
						} 
//						else {
//							map.put("itemImage", R.drawable.ic_action_person_white);//添加图像资源的ID  
//						}
					} 
//					else {
//						map.put("itemImage", R.drawable.ic_action_person_white);//添加图像资源的ID  
//					}
			        map.put("itemText", friend.getDisplayName());//按序号做ItemText  
			        map.put("friendUserId", templateApportion.optString("friendUserId"));
			        map.put("localFriendId", templateApportion.optString("localFriendId"));
			        lstItem.add(map); 
		        }
		      }  
	      //生成适配器的ImageItem <====> 动态数组的元素，两者一一对应  
		      SimpleAdapter saImageItems = new GameGridAdapter(getActivity(),
	                                                lstItem,
	                                                R.layout.hyjfreegame_grid_item,
	                                                new String[] {"itemImage","itemText","friendUserId","localFriendId"},   
	                                                new int[] {R.id.hyjfreegame_grid_item_imageView,R.id.hyjfreegame_grid_item_textView,R.id.hyjfreegame_grid_item_textView_friendUserId,R.id.hyjfreegame_grid_item_textView_localFriendId});  
	      //添加并且显示  
		    mGridView = (GridView) getView().findViewById(R.id.hyjFreeGameFormFragment_gridView);
		    mGridView.setAdapter(saImageItems); 
//		SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), listItems, R.layout.hyjFreeGameFormFragment_gridView_cell)
		
			app_action_game_start = (Button) getView().findViewById(R.id.button_start);
			app_action_game_start.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					if(mGridView.getCount() == 0){
						HyjUtil.displayToast("请选择分摊人员，再开始游戏");
					} else {
						for (int i = 0; i < 5; i++) {
							 selectFreePerson();
							 ((SimpleAdapter) mGridView.getAdapter()).notifyDataSetChanged();
						 }
					}
					 
				}
			});
			
			app_action_game_free = (Button) getView().findViewById(R.id.button_free);
			app_action_game_free.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					if (oldPosition != -1) {
						TextView friendUserIdTextView =(TextView) mGridView.getChildAt(oldPosition).findViewById(R.id.hyjfreegame_grid_item_textView_friendUserId);
						TextView localFriendIdTextView =(TextView) mGridView.getChildAt(oldPosition).findViewById(R.id.hyjfreegame_grid_item_textView_localFriendId);
						
						Intent intent = new Intent();
						intent.putExtra("friendUserId", friendUserIdTextView.getText());
						intent.putExtra("localFriendId", localFriendIdTextView.getText());
						
						getActivity().setResult(Activity.RESULT_OK, intent);
						getActivity().finish();
					} else {
						HyjUtil.displayToast("还没有免单人员，请先开始游戏");
					}
				}
			});
		 } catch (JSONException e) {
			// TODO Auto-generated catch block
			 e.printStackTrace();
		 }
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    super.onCreateOptionsMenu(menu, inflater);
//	    for (int i = 0; i < mGridView.getCount(); i++) {
//	    	if(mGridView.getChildAt(i) != null) {
//		    	TextView thisFriendUserIdTextView =(TextView) mGridView.getChildAt(i).findViewById(R.id.hyjfreegame_grid_item_textView_friendUserId);
////				TextView thisLocalFriendIdTextView =(TextView) mGridView.getChildAt(i).findViewById(R.id.hyjfreegame_grid_item_textView_localFriendId);
//				ImageView thisImageView = (ImageView) mGridView.getChildAt(i).findViewById(R.id.hyjfreegame_grid_item_imageView);
//				if (thisFriendUserIdTextView.getText() != null && !"".equals(thisFriendUserIdTextView.getText())) {
//					if(HyjApplication.getInstance().getCurrentUser().getId().equals(thisFriendUserIdTextView.getText())){
//						thisImageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_yellow));
//					} else {
//						thisImageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_green));
//					}
//				} else {
//					thisImageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_yellow));
//				}
//	    	}
//	    }
	    
	}
	
	@Override
	public Integer useOptionsMenuView(){
		return null;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		}
	}
	


	private void selectFreePerson() {
		// TODO Auto-generated method stub
		if (oldPosition != -1) {
			lstItem.get(oldPosition).put("isSelected", 0);
//			
//			if(mGridView.getChildAt(oldPosition) != null) {
//		    	TextView thisFriendUserIdTextView =(TextView) mGridView.getChildAt(oldPosition).findViewById(R.id.hyjfreegame_grid_item_textView_friendUserId);
////				TextView thisLocalFriendIdTextView =(TextView) mGridView.getChildAt(i).findViewById(R.id.hyjfreegame_grid_item_textView_localFriendId);
////				ImageView thisImageView = (ImageView) mGridView.getChildAt(oldPosition).findViewById(R.id.hyjfreegame_grid_item_imageView);
//				if (thisFriendUserIdTextView.getText() != null && !"".equals(thisFriendUserIdTextView.getText())) {
//					if(HyjApplication.getInstance().getCurrentUser().getId().equals(thisFriendUserIdTextView.getText())){
////						thisImageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_yellow));
//						lstItem.get(oldPosition).put("itemImage", getResources().getColor(R.color.hoyoji_yellow));
//					} else {
////						thisImageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_green));
//						lstItem.get(oldPosition).put("itemImage", getResources().getColor(R.color.hoyoji_green));
//					}
//				} else {
////					thisImageView.setBackgroundColor(getResources().getColor(R.color.hoyoji_green));
//					lstItem.get(oldPosition).put("itemImage", getResources().getColor(R.color.hoyoji_green));
//				}
//	    	}
		}
		Random random = new Random();
        int s = random.nextInt(mGridView.getCount())%(mGridView.getCount());
        
        lstItem.get(s).put("isSelected", 1);
//        thisPosition = s;
		oldPosition = s;
//        if (mGridView.getChildAt(s) != null) {
//			mGridView.getChildAt(s).findViewById(R.id.hyjfreegame_grid_item_imageView).setBackgroundColor(getResources().getColor(R.color.hoyoji_red));
//			oldPosition = s;
//        }
	}
	
	private static class GameGridAdapter extends SimpleAdapter{
		private Context mContext;
		private int[] mViewIds;
	    private String[] mFields;
	    private int mLayoutResource;
//	    private ViewBinder mViewBinder;
	    
		public GameGridAdapter(Context context,
	                    List<HashMap<String, Object>> childData,
	                    int childLayout, String[] childFrom,
	                    int[] childTo) {
			super(context, (List<? extends Map<String, ?>>) childData, childLayout, childFrom, childTo);

			mContext = context;
	        mLayoutResource = childLayout;
	        mViewIds = childTo;
	        mFields = childFrom;
		}
		
		@Override
		public boolean hasStableIds(){
			return true;
		}
	    
		/**
	     * Populate new items in the list.
	     */
	    @Override 
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View view = convertView;
	        View[] viewHolder;
	        if (view == null) {
	        	LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            view = vi.inflate(mLayoutResource, null);
	            viewHolder = new View[mViewIds.length];
	            for(int i=0; i<mViewIds.length; i++){
	            	View v = view.findViewById(mViewIds[i]);
	            	viewHolder[i] = v;
	            }
	            view.setTag(viewHolder);
	        } else {
	        	viewHolder = (View[])view.getTag();
	        }

	        Object item = getItem(position);
	        for(int i=0; i<mViewIds.length; i++){
	        	View v = viewHolder[i];
//	        	getViewBinder().setViewValue(v, item, mFields[i]);
	        	HashMap<String, Object> map = (HashMap<String, Object>) this.getItem(position);
	        	TextView thisNameTextView =(TextView) view.findViewById(R.id.hyjfreegame_grid_item_textView);
	        	thisNameTextView.setText(map.get("itemText").toString());
		    	TextView thisFriendUserIdTextView =(TextView) view.findViewById(R.id.hyjfreegame_grid_item_textView_friendUserId);
		    	thisFriendUserIdTextView.setText(map.get("friendUserId").toString());
				TextView thisLocalFriendIdTextView =(TextView) view.findViewById(R.id.hyjfreegame_grid_item_textView_localFriendId);
				thisLocalFriendIdTextView.setText(map.get("localFriendId").toString());
				HyjImageView thisImageView = (HyjImageView) view.findViewById(R.id.hyjfreegame_grid_item_imageView);
//				thisImageView.setImageResource((Integer) map.get("itemImage"));
				if(map.get("itemImage") != null) {
					thisImageView.setImage((Picture) map.get("itemImage"));
				} else {
					thisImageView.setDefaultImage(R.drawable.ic_action_person_white);
					thisImageView.setImage((Picture) null);
				}
				if((Integer)map.get("isSelected") == 1){
					thisImageView.setBackgroundColor(mContext.getResources().getColor(R.color.hoyoji_red));
				} else {
					if (thisFriendUserIdTextView.getText() != null && !"".equals(thisFriendUserIdTextView.getText())) {
						if(HyjApplication.getInstance().getCurrentUser().getId().equals(thisFriendUserIdTextView.getText())){
							thisImageView.setBackgroundColor(mContext.getResources().getColor(R.color.hoyoji_yellow));
						} else {
							thisImageView.setBackgroundColor(mContext.getResources().getColor(R.color.hoyoji_green));
						}
					} else {
						thisImageView.setBackgroundColor(mContext.getResources().getColor(R.color.hoyoji_yellow));
					}
				}
				
	        }
	        
	        return view;
	    }
	}
}