package com.ptyt.uct.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.callback.MapCallBack;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.entity.MessageMyLocationEntity;
import com.ptyt.uct.common.SettingsConstant;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;

import java.io.File;
import java.net.URISyntaxException;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;


public class MessageLookOverLocationActivity extends BaseActionBarActivity implements
        View.OnClickListener {

    private TextView name_tv;
    private TextView location_tv;
    private MapView mapView;
    private ImageView locationIv;
    private AMap mMap;
    private MyLocationStyle myLocationStyle;
    private MessageMyLocationEntity entity;
    private boolean isClickLocationButton = false;
    private ImageView navigation_iv;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_message_look_over_location;
    }

    @Override
    protected void initWidget() {
        EventBus.getDefault().register(this);
        tv_actionBarTitle.setText(getResources().getString(R.string.string_my_location_title));
        name_tv = (TextView) findViewById(R.id.name_tv);
        location_tv = (TextView) findViewById(R.id.location_tv);
        mapView = (MapView) findViewById(R.id.mapView);
        locationIv = (ImageView) findViewById(R.id.locationIv);
        locationIv.setOnClickListener(this);
        navigation_iv = (ImageView) findViewById(R.id.navigation_iv);
        navigation_iv.setOnClickListener(this);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mMap = mapView.getMap();
    }

    @Override
    protected void initData() {
        initMap();
        initMapData();
    }

    /**
     * @param
     * @return
     * @description 初始化地图数据
     */
    private void initMap() {
        // 开始定位
        //        UctApplication.getInstance().mLocationClient.startLocation();
        // 初始化定位蓝点样式类
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(((Integer) UctClientApi.getUserData(SettingsConstant.SETTINGS_LOCATION_INTERVAL, 5000)).intValue()); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));//设置定位蓝点精度圆圈的边框颜色的方法。
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));//设置定位蓝点精度圆圈的填充颜色的方法。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER); //连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
        mMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        mMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);// 默认的缩放按钮
        uiSettings.setMyLocationButtonEnabled(false); // 默认的定位按钮
        uiSettings.setZoomGesturesEnabled(true);// 缩放手势
    }

    private void initMapData() {
        entity = getIntent().getParcelableExtra("myLocation");
        if (entity == null) {
            ToastUtils.getToast().showMessageShort(this, getString(R.string.string_message_unknown_data), -1);
            return;
        }
        PrintLog.i("size = " + entity.getSize() + " path = " + entity.getLocalPath() + " type = " + entity.getType() + " zoom = " + entity.getZoom() +
                " latitude = " + entity.getLatitude() + " longitude = " + entity.getLongitude() + " placeName = " + entity.getPlaceName() + " specificPlaceName = " +
                entity.getSpecificPlaceName());
        String placeName = entity.getPlaceName();
        String specificPlaceName = entity.getSpecificPlaceName();
        if (StrUtils.isEmpty(placeName)) {
            name_tv.setText(getString(R.string.string_my_location_place_name));
        } else {
            name_tv.setText(placeName);
        }
        if (StrUtils.isEmpty(specificPlaceName)) {
            location_tv.setText(getString(R.string.string_my_location_place_name));
        } else {
            location_tv.setText(specificPlaceName);
        }
        double latitude = entity.getLatitude();
        double longitude = entity.getLongitude();
        // 必须在开始定位后的回调函数获取Marker，否则aMapLocation可能为null
        if (latitude == 0.0d && longitude == 0.0d) {
            ToastUtils.getToast().showMessageShort(this, getString(R.string.string_message_unknown_coordinates), -1);
        }
        MarkerOptions options = getMarkerOptions(latitude, longitude, R.mipmap.icon_position);
        // 添加图钉
        mMap.addMarker(options);
        PrintLog.i("addMarker");
        float zoom = entity.getZoom();
        if (zoom == 0.0f) {
            mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        } else {
            mMap.moveCamera(CameraUpdateFactory.zoomTo(entity.getZoom()));
        }
        mMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(latitude, longitude)));
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEvent(EventBean eventBean) {
        if (eventBean.getAction().equals(ConstantUtils.ACTION_LOCATION_CHANGE)) {
            if (isClickLocationButton) {
                isClickLocationButton = false;
                mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                mMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(MapCallBack.getMapCallBack().aMapLocation.getLatitude(), MapCallBack.getMapCallBack().aMapLocation.getLongitude())));
            }
            PrintLog.i(MapCallBack.getMapCallBack().aMapLocation.getLatitude() + "---------" + MapCallBack.getMapCallBack().aMapLocation.getLongitude());
        }
    }

    /**
     * @param latitude
     * @param longitude
     * @return 返回图钉对象
     * @description 自定义一个图钉，并且设置图标，当我们点击图钉时，显示设置的信息
     */
    private MarkerOptions getMarkerOptions(double latitude, double longitude, int res) {
        //设置图钉选项
        MarkerOptions options = new MarkerOptions();
        //图标
        options.icon(BitmapDescriptorFactory.fromResource(res));
        //位置
        options.position(new LatLng(latitude, longitude));
        //设置锚点比例
        options.anchor(0.5f, 0.7f);
        //设置多少帧刷新一次图片资源
        options.period(60);
        return options;

    }

    /**
     * 导航
     *
     * @param destination 目的地
     */
    private void setUpGaodeNavigation(LatLng destination) {
        try {
            if (isInstallByread("com.autonavi.minimap")) {//高德地图
                Intent intent = Intent.getIntent("androidamap://route?sourceApplication=softname&sname=我的位置&dlat="
                        + destination.latitude + "&dlon=" + destination.longitude + "&dname=" + "" + "&dev=0&m=0&t=1");
                startActivity(intent);
            } else if (isInstallByread("com.baidu.BaiduMap")) {//百度地图
                destination = gaode2baiduCoordinate(destination.latitude, destination.longitude);
                Intent intent = Intent.getIntent("intent://map/direction?origin=我的位置&destination=latlng:" + destination.latitude + "," + destination.longitude + "|name:东郡华城广场|A座" +
                        "&mode=driving&src=yourCompanyName|yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
                startActivity(intent);
            } else {
                ToastUtils.getToast().showMessageShort(mContext, "高德或百度未安装，不能打开", -1);
            }
            //            else if(isInstallByread("com.tencent.map")){//腾讯地图
            ////                Uri uri = Uri.parse("http://apis.map.qq.com/uri/v1/routeplan?type=drive&from=" + chufaText + "&to=" + distinationText + "&policy=0&referer=myapp");
            ////                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            ////                startActivity(intent);
            //
            //                PrintLog.i("腾讯地图");
            //                Intent intent = Intent.getIntent("http://apis.map.qq.com/uri/v1/routeplan?type=bus&from=&fromcoord="+currentLatLng.latitude+","+currentLatLng.longitude+"&to=&tocoord="+destination.latitude+","+destination.longitude+"&policy=0&referer=myapp");
            //                startActivity(intent);
            //            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否安装目标应用
     *
     * @param packageName 目标应用安装后的包名
     * @return 是否已安装目标应用
     */
    private boolean isInstallByread(String packageName) {
        return new File("/data/data/" + packageName).exists();
    }

    /**
     * @description  高德坐标转百度坐标（火系转百度系）
     * @param
     * @return
     */
    private LatLng gaode2baiduCoordinate(double latitude, double longitude) {
        double x = longitude, y = latitude;
        double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double tempLon = z * Math.cos(theta) + 0.0065;
        double tempLat = z * Math.sin(theta) + 0.006;
        return new LatLng(tempLat, tempLon);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locationIv:
                PrintLog.i(MapCallBack.getMapCallBack().aMapLocation.getLatitude() + "---------" + MapCallBack.getMapCallBack().aMapLocation.getLongitude());
                isClickLocationButton = true;
                MapCallBack.getMapCallBack().mLocationClient.startLocation();
                break;
            case R.id.navigation_iv:
                setUpGaodeNavigation(new LatLng(entity.getLatitude(), entity.getLongitude()));
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);

    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        MapCallBack.getMapCallBack().mLocationClient.stopLocation();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


}
