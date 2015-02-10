package com.hoyoji.hoyoji.event;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.hoyoji.aaevent_android.R;
import com.hoyoji.android.hyjframework.fragment.HyjUserFragment;

public class BaseMapFragment extends HyjUserFragment {
	@SuppressWarnings("unused")
	private static final String LTAG = BaseMapDemo.class.getSimpleName();
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private static final LatLng GEO_SHENGZHENG = new LatLng(22.560, 114.064);
	private Marker marker;
	private double latitude;
	private double longitude;
	
	private LocationClient locationClient;
	
	@Override
	public Integer useContentView() {
		SDKInitializer.initialize(getActivity().getApplication());
		return R.layout.activity_map;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		Intent intent = getActivity().getIntent();
		String adapterString = intent.getStringExtra("adapterJSONArray");
		
//		locationClient = new LocationClient(getActivity());
//		// 设置定位条件
//		LocationClientOption option = new LocationClientOption();
//		option.setOpenGps(true); // 是否打开GPS
//		option.setCoorType("bd09ll"); // 设置返回值的坐标类型。
////		option.setPriority(LocationClientOption.MIN_SCAN_SPAN); // 设置定位优先级
//		// option.setProdName("LocationDemo"); //
//		// 设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
//		// option.setScanSpan(UPDATE_TIME);// 设置定时定位的时间间隔。单位毫秒
//		locationClient.setLocOption(option);
//
//		// 注册位置监听器
//		locationClient.registerLocationListener(new BDLocationListener() {
//
//			@Override
//			public void onReceiveLocation(BDLocation location) {
//				// TODO Auto-generated method stub
//				if (location == null) {
//					return;
//				}
//				latitude = location.getLatitude();
//				longitude = location.getLongitude();
//			}
//		});
		
		
//		app_action_game_free = (Button) getView().findViewById(R.id.button_free);
//		app_action_game_free.setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				if (oldPosition != -1) {
//					TextView friendUserIdTextView =(TextView) mGridView.getChildAt(oldPosition).findViewById(R.id.hyjfreegame_grid_item_textView_friendUserId);
//					TextView localFriendIdTextView =(TextView) mGridView.getChildAt(oldPosition).findViewById(R.id.hyjfreegame_grid_item_textView_localFriendId);
//					
//					Intent intent = new Intent();
//					intent.putExtra("friendUserId", friendUserIdTextView.getText());
//					intent.putExtra("localFriendId", localFriendIdTextView.getText());
//					
//					getActivity().setResult(Activity.RESULT_OK, intent);
//					getActivity().finish();
//				} else {
//					HyjUtil.displayToast("还没有选中免单人员，请先开始游戏");
//				}
//			}
//		});
		mMapView = (MapView) getView().findViewById(R.id.bmapView);
		MapStatusUpdate u4 = MapStatusUpdateFactory.newLatLng(GEO_SHENGZHENG);
		mBaiduMap = mMapView.getMap();  
		//普通地图  
		mBaiduMap.setMapStatus(u4);
		
		
//		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL); 
//		SupportMapFragment map4 = (SupportMapFragment) (getSupportFragmentManager().findFragmentById(R.id.map4));
//		map4.getBaiduMap().setMapStatus(u4);
		
		mBaiduMap.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
            	mapPoi.getName(); //名称
            	mapPoi.getPosition(); //坐标 
                return false;
            }

            @Override
            public void onMapClick(LatLng point) {
            	latitude = point.latitude;
            	longitude = point.longitude;
            	
            	MapStatusUpdate u4 = MapStatusUpdateFactory.newLatLng(point);
            	mBaiduMap.setMapStatus(u4); 
            	if(marker != null){
            		marker.remove();
            	}
            	LatLng latLng = mBaiduMap.getMapStatus().target;  
        		//准备 marker 的图片  
        		BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher);  
        		//准备 marker option 添加 marker 使用  
        		MarkerOptions markerOptions = new MarkerOptions().icon(bitmap).position(latLng);  
        		//获取添加的 marker 这样便于后续的操作  
        		marker = (Marker) mBaiduMap.addOverlay(markerOptions);  
            }
		});
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    super.onCreateOptionsMenu(menu, inflater);
	    
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
	
}
