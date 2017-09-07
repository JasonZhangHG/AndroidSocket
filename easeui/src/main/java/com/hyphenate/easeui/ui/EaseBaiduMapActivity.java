/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.easeui.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.hyphenate.easeui.R;

public class EaseBaiduMapActivity extends EaseBaseActivity {

    private final static String TAG = "map";
    static MapView mMapView = null;
    FrameLayout mMapViewContainer = null;
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();

    Button sendButton = null;

    EditText indexText = null;
    int index = 0;
    // LocationData locData = null;
    static BDLocation lastLocation = null;
    public static EaseBaiduMapActivity instance = null;
    ProgressDialog progressDialog;
    private BaiduMap mBaiduMap;
    private double back_lat, back_long;//最后返回出去的地址坐标
    private String back_address;//返回的地址

    public class BaiduSDKReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            String st1 = getResources().getString(R.string.Network_error);
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                String st2 = "z1Duyj5H9MqPAdsGRk3puj4WsitnM1Vv";
               // System.out.println("----st2--" + st2);
               // Toast.makeText(instance, st2, Toast.LENGTH_SHORT).show();
            }
            else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
              //  Toast.makeText(instance, st1, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private BaiduSDKReceiver mBaiduReceiver;

    private BitmapDescriptor bdL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        //initialize SDK with context, should call this before setContentView
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.ease_activity_baidumap);
        mMapView = (MapView) findViewById(R.id.bmapView);
        sendButton = (Button) findViewById(R.id.btn_location_send);
        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra("latitude", 0);
        LocationMode mCurrentMode = LocationMode.NORMAL;
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);
        initMapView();
        if (latitude == 0) {
            mMapView = new MapView(this, new BaiduMapOptions());
            bdL = BitmapDescriptorFactory.fromResource(R.drawable.icon_addess);
            mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                    mCurrentMode, true, bdL));//不显示定位原点的
            showMapWithLocationClient();
        }
        else {//点击发送坐标的时候
            double longtitude = intent.getDoubleExtra("longitude", 0);
            String address = intent.getStringExtra("address");
            LatLng p = new LatLng(latitude, longtitude);
            mMapView = new MapView(this,
                    new BaiduMapOptions().mapStatus(new MapStatus.Builder()
                            .target(p).build()));
            showMap(latitude, longtitude, address);
        }
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mBaiduReceiver = new BaiduSDKReceiver();
        registerReceiver(mBaiduReceiver, iFilter);
        MAPListen();//地图滑动监听
    }

    private void showMap(double latitude, double longtitude, String address) {
        sendButton.setVisibility(View.GONE);
		LatLng llA = new LatLng(latitude, longtitude);

		OverlayOptions ooA = new MarkerOptions().position(llA).icon(BitmapDescriptorFactory
				.fromResource(R.drawable.loca_end))
				.zIndex(4).draggable(true);
		mBaiduMap.addOverlay(ooA);
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(llA, 17.0f);
		mBaiduMap.animateMapStatus(u);
    }

    private void showMapWithLocationClient() {
        String str1 = getResources().getString(R.string.Making_sure_your_location);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(str1);

        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            public void onCancel(DialogInterface arg0) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.d("map", "cancel retrieve location");
                finish();
            }
        });

        progressDialog.show();
//
//		mLocClient = new LocationClient(this);
//		mLocClient.registerLocationListener(myListener);
//
//		LocationClientOption option = new LocationClientOption();
//		option.setOpenGps(true);// open gps
//		// option.setCoorType("bd09ll");
//		// Johnson change to use gcj02 coordination. chinese national standard
//		// so need to conver to bd09 everytime when draw on baidu map
//		option.setCoorType("gcj02");
//		option.setScanSpan(30000);
//		option.setAddrType("all");
//		mLocClient.setLocOption(option);
//		mLocClient.start();

        LocationMy();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        if (mLocClient != null) {
            mLocClient.stop();
        }
        super.onPause();
        lastLocation = null;
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        if (mLocClient != null) {
            mLocClient.start();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mLocClient != null)
            mLocClient.stop();
        mMapView.onDestroy();
        unregisterReceiver(mBaiduReceiver);
        super.onDestroy();
    }

    private void initMapView() {
        mMapView.setLongClickable(true);
    }

    /**
     * format new location to string and show on screen
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                return;
            }
            sendButton.setEnabled(true);
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (lastLocation != null) {
                if (lastLocation.getLatitude() == location.getLatitude() && lastLocation.getLongitude() == location.getLongitude()) {
                    Log.d("map", "same location, skip refresh");
//					 mMapView.refresh(); //need this refresh?
                   // Toast.makeText(EaseBaiduMapActivity.this, "定位成功" + location.getLatitude(), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            lastLocation = location;
            mBaiduMap.clear();
//			LatLng llA = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
//			CoordinateConverter converter= new CoordinateConverter();
//			converter.coord(llA);
//			converter.from(CoordinateConverter.CoordType.COMMON);
//			LatLng convertLatLng = converter.convert();
//			OverlayOptions ooA = new MarkerOptions().position(convertLatLng).icon(BitmapDescriptorFactory
//					.fromResource(R.drawable.ease_icon_marka))
//					.zIndex(4).draggable(true);
//			mBaiduMap.addOverlay(ooA);
//			MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(convertLatLng, 17.0f);
//			mBaiduMap.animateMapStatus(u);
        }

        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null) {
                return;
            }
        }
    }

    public void back(View v) {
        finish();
    }

    public void sendLocation(View view) {//地址返回
        Intent intent = this.getIntent();
        intent.putExtra("latitude", back_lat);
        intent.putExtra("longitude", back_long);
        intent.putExtra("address", back_address);
       // Toast.makeText(EaseBaiduMapActivity.this, back_address + "  " + back_lat + "  " + back_long, Toast.LENGTH_SHORT).show();
        this.setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    //***********************************
    private LocationManager locationManager;
    private LocationClient locationClient;//定位SDK的核心类
    private double locLat, locLog;

    private void LocationMy() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);//获得系统定位服务
        locationClient = new LocationClient(getApplicationContext());//实例化定位服务 LocationClient类必须在主线程中进行声明
        locationClient.registerLocationListener(new BDlocationListenImple());
        //LocationClientOption 该类用来设计SDK定位的方式
        LocationClientOption locationClientOption = new LocationClientOption();
        locationClientOption.setOpenGps(true);//设置打开GPS
        locationClientOption.setAddrType("all");//设置定位返回的结果包含地址信息
        locationClientOption.setCoorType("bd09ll"); //返回的定位结果是百度经纬度,默认值gcj02
        locationClientOption.setPriority(LocationClientOption.GpsFirst); // 设置GPS优先
        locationClientOption.setScanSpan(500); //设置发起定位请求的间隔时间为5000ms
        locationClientOption.disableCache(false); //禁止启用缓存定位
        int span = 1001;
        locationClientOption.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        locationClientOption.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        locationClientOption.setOpenGps(true);//可选，默认false,设置是否使用gps
        locationClientOption.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locationClientOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        locationClientOption.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        locationClientOption.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationClientOption.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        locationClientOption.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        locationClient.setLocOption(locationClientOption);//设置定位参数
        locationClient.start();//调用此方法开始定位
    }

    //定位监听接口
    private int num = 1;//用于判断定位的次数

    public class BDlocationListenImple implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null) {
                return;
            }
            sendButton.setEnabled(true);
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            mBaiduMap.clear();
            progressDialog.dismiss();
            MyLocationData locdata = new MyLocationData.Builder().accuracy(200).direction(100).latitude(bdLocation.getLatitude()).longitude(bdLocation.getLongitude()).build();
            mBaiduMap.setMyLocationData(locdata);
            LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            locLat = bdLocation.getLatitude();
            locLog = bdLocation.getLongitude();
            if (num == 1) {
                back_address = bdLocation.getAddrStr();
                back_lat = locLat;
                back_long = locLat;
            }
            num++;
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(latLng).zoom(14);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            locationClient.unRegisterLocationListener(this);
            locationClient.stop();
        }
    }

    //获得地图移动的监听的方法
    private void MAPListen() {
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                //地图状态发生改变更新地图UI
                UpDateMapUi(mapStatus);
            }
        });
    }

    private LatLng mylatlng;

    private void UpDateMapUi(MapStatus mapStatus) {
        LatLng latLng = mapStatus.target;
        back_lat = latLng.latitude;
        back_long = latLng.longitude;
        mylatlng = new LatLng(back_lat, back_long);
        myaddress();
    }

    private void myaddress() {
        ReverseGeoCodeOption mCodeOption = new ReverseGeoCodeOption();
        mCodeOption.location(mylatlng);
        String address = mCodeOption.toString();
        GeoCoder geoCoder = GeoCoder.newInstance();
        geoCoder.reverseGeoCode(mCodeOption);
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                String text = geoCodeResult.getAddress();
            }
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
              // String text =    /*  reverseGeoCodeResult.getAddressDetail().city+ */"  " + reverseGeoCodeResult.getAddress();
             //   Toast.makeText(EaseBaiduMapActivity.this, text, Toast.LENGTH_SHORT).show();
                back_address = reverseGeoCodeResult.getAddress();
            }
        });
        ReverseGeoCodeResult.AddressComponent addressComponent = new ReverseGeoCodeResult.AddressComponent();
        String srt = addressComponent.city + "  " + addressComponent.province + "  " + addressComponent.street + "  " + addressComponent.streetNumber + " " + addressComponent.toString();
        int it = addressComponent.describeContents();
    }
}

