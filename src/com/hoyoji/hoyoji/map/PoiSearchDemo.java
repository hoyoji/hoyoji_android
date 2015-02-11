package com.hoyoji.hoyoji.map;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.hoyoji.aaevent_android.R;

/**
 * 演示poi搜索功能
 */
public class PoiSearchDemo extends FragmentActivity implements OnGetPoiSearchResultListener, OnGetSuggestionResultListener, OnGetGeoCoderResultListener {

	private PoiSearch mPoiSearch = null;
	private SuggestionSearch mSuggestionSearch = null;
	private BaiduMap mBaiduMap = null;
	/**
	 * 搜索关键字输入窗口
	 */
	private AutoCompleteTextView keyWorldsView = null;
	private ArrayAdapter<String> sugAdapter = null;
	private int load_Index = 0;
	
	private LatLng SHEN_ZHEN = new LatLng(22.560, 114.064);
	private LatLng THIS_POINT;
	private Marker marker,thisMarker;
	private double latitude;
	private double longitude;
	private String address;
	
	public GeofenceClient mGeofenceClient;
	private LocationClient mLocationClient;
	public MyLocationListener mMyLocationListener;
	
	GeoCoder mSearch = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplication());
		
		setContentView(R.layout.activity_poisearch);
		
		// 初始化搜索模块，注册搜索事件监听
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		mSuggestionSearch = SuggestionSearch.newInstance();
		mSuggestionSearch.setOnGetSuggestionResultListener(this);
		keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchkey);
		sugAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line);
		keyWorldsView.setAdapter(sugAdapter);
		mBaiduMap = ((SupportMapFragment) (getSupportFragmentManager().findFragmentById(R.id.map))).getBaiduMap();
		
		MapStatusUpdate u4 = MapStatusUpdateFactory.newLatLng(SHEN_ZHEN);
		mBaiduMap.setMapStatus(u4);
		
		// 初始化搜索模块，注册事件监听
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		
		geo();
		
		setLocation();
		

		/**
		 * 当输入关键字变化时，动态更新建议列表
		 */
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
        		BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
        		//准备 marker option 添加 marker 使用  
        		MarkerOptions markerOptions = new MarkerOptions().icon(bitmap).position(latLng);  
        		//获取添加的 marker 这样便于后续的操作  
        		marker = (Marker) mBaiduMap.addOverlay(markerOptions);  
            }
		});
		
		keyWorldsView.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				if (cs.length() <= 0) {
					return;
				}
				String city = ((EditText) findViewById(R.id.city)).getText()
						.toString();
				/**
				 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
				 */
				mSuggestionSearch
						.requestSuggestion((new SuggestionSearchOption())
								.keyword(cs.toString()).city(city));
			}
		});

	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		mLocationClient.stop();
		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mPoiSearch.destroy();
		mSuggestionSearch.destroy();
		mSearch.destroy();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	/**
	 * 影响搜索按钮点击事件
	 * 
	 * @param v
	 */
	public void searchButtonProcess(View v) {
		EditText editCity = (EditText) findViewById(R.id.city);
		EditText editSearchKey = (EditText) findViewById(R.id.searchkey);
		mPoiSearch.searchInCity((new PoiCitySearchOption())
				.city(editCity.getText().toString())
				.keyword(editSearchKey.getText().toString())
				.pageNum(load_Index));
	}

	public void goToNextPage(View v) {
		load_Index++;
		searchButtonProcess(null);
	}

	public void onGetPoiResult(PoiResult result) {
		if (result == null
				|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			Toast.makeText(PoiSearchDemo.this, "未找到结果", Toast.LENGTH_LONG).show();
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			mBaiduMap.clear();
			PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
			mBaiduMap.setOnMarkerClickListener(overlay);
			overlay.setData(result);
			overlay.addToMap();
			overlay.zoomToSpan();
			return;
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

			// 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
			String strInfo = "在";
			for (CityInfo cityInfo : result.getSuggestCityList()) {
				strInfo += cityInfo.city;
				strInfo += ",";
			}
			strInfo += "找到结果";
			Toast.makeText(PoiSearchDemo.this, strInfo, Toast.LENGTH_LONG).show();
		}
	}

	public void onGetPoiDetailResult(PoiDetailResult result) {
		if (result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(PoiSearchDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(PoiSearchDemo.this, result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onGetSuggestionResult(SuggestionResult res) {
		if (res == null || res.getAllSuggestions() == null) {
			return;
		}
		sugAdapter.clear();
		for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
			if (info.key != null)
				sugAdapter.add(info.key);
		}
		sugAdapter.notifyDataSetChanged();
	}

	private class MyPoiOverlay extends PoiOverlay {

		public MyPoiOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public boolean onPoiClick(int index) {
			super.onPoiClick(index);
			PoiInfo poi = getPoiResult().getAllPoi().get(index);
			// if (poi.hasCaterDetails) {
				mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUid(poi.uid));
			// }
			return true;
		}
	}
	
	private void setLocation(){
		mLocationClient = new LocationClient(this.getApplicationContext());
		mMyLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mMyLocationListener);
		mGeofenceClient = new GeofenceClient(getApplicationContext());
		
		InitLocation();
		mLocationClient.start();
	}
	
	private void InitLocation(){
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式
		option.setCoorType("gcj02");//返回的定位结果是百度经纬度，默认值gcj02
		int span=1000;
		try {
			span = Integer.valueOf(1000);
		} catch (Exception e) {
			// TODO: handle exception
		}
		option.setScanSpan(span);//设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);
		mLocationClient.setLocOption(option);
	}
	
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			//Receive Location 
			THIS_POINT = new LatLng(location.getLatitude(), location.getLongitude());
			logMsg();
			
		}


	}
	
	
	/**
	 * ��ʾ�����ַ�
	 * @param str
	 */
	public void logMsg() {
//		MapStatusUpdate u4 = MapStatusUpdateFactory.newLatLng(THIS_POINT);
//    	mBaiduMap.setMapStatus(u4); 
    	

		if(thisMarker != null){
			thisMarker.remove();
    	}
		//准备 marker 的图片  
		BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
		//准备 marker option 添加 marker 使用  
		MarkerOptions markerOptions = new MarkerOptions().icon(bitmap).position(THIS_POINT);  
		//获取添加的 marker 这样便于后续的操作  
		marker = (Marker) mBaiduMap.addOverlay(markerOptions);  
//		Log.i("BaiduLocationApiDem", sb.toString());
	}
	
	private void geo() {
		// 反Geo搜索
		mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(SHEN_ZHEN));
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult arg0) {
		// TODO Auto-generated method stub
		address = arg0.getAddress();
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
		// TODO Auto-generated method stub
		address = arg0.getAddress();
	}
}
