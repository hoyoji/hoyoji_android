package com.hoyoji.android.hyjframework.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.aaevent_android.R;
import com.hoyoji.hoyoji.models.Event;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyExpenseApportion;
import com.hoyoji.hoyoji.models.MoneyExpenseContainer;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.money.MoneyApportionField;
import com.hoyoji.hoyoji.money.MoneyExpenseContainerFormFragment;

public class HyjFreeGameFormFragment extends HyjUserFormFragment {
	private GridView mGridView = null;
	private Button app_action_game_start = null;
	private Button app_action_game_free = null;
	private int oldPosition = -1;
	
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
		      ArrayList<HashMap<String, Object>> lstItem = new ArrayList<HashMap<String, Object>>();  
		      
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
					if(friend.getFriendUser() != null) {
						if(friend.getFriendUser().getPictureId() != null) {
							map.put("itemImage", friend.getFriendUser().getPictureId());//添加图像资源的ID  
						} else {
							map.put("itemImage", R.drawable.ic_action_person_white);//添加图像资源的ID  
						}
					} else {
						map.put("itemImage", R.drawable.ic_action_person_white);//添加图像资源的ID  
					}
			        map.put("itemText", friend.getDisplayName());//按序号做ItemText  
			        map.put("friendUserId", templateApportion.optString("friendUserId"));
			        map.put("localFriendId", templateApportion.optString("localFriendId"));
			        lstItem.add(map); 
		        }
		      }  
	      //生成适配器的ImageItem <====> 动态数组的元素，两者一一对应  
		      SimpleAdapter saImageItems = new SimpleAdapter(getActivity(),
	                                                lstItem,
	                                                R.layout.hyjfreegame_grid_item,
	                                                new String[] {"itemImage","itemText","friendUserId","localFriendId"},   
	                                                new int[] {R.id.hyjfreegame_grid_item_imageView,R.id.hyjfreegame_grid_item_textView,R.id.hyjfreegame_grid_item_textView_friendUserId,R.id.hyjfreegame_grid_item_textView_localFriendId});  
	      //添加并且显示  
	      mGridView = (GridView) getView().findViewById(R.id.hyjFreeGameFormFragment_gridView);
	      mGridView.setAdapter(saImageItems); 
		
//		SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), listItems, R.layout.hyjFreeGameFormFragment_gridView_cell)
		 } catch (JSONException e) {
			// TODO Auto-generated catch block
			 e.printStackTrace();
		 }
		
		app_action_game_start = (Button) getView().findViewById(R.id.button_start);
		app_action_game_start.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (oldPosition != -1) {
					mGridView.getChildAt(oldPosition).findViewById(R.id.hyjfreegame_grid_item_imageView).setBackgroundColor(Color.WHITE);
				}
				Random random = new Random();
		        int s = random.nextInt(mGridView.getCount())%(mGridView.getCount());
		        
				mGridView.getChildAt(s).findViewById(R.id.hyjfreegame_grid_item_imageView).setBackgroundColor(Color.RED);
				oldPosition = s;
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
