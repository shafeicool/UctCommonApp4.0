package com.ptyt.uct.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.adapter.BaseRecyAdapter;
import com.ptyt.uct.adapter.MessageMyLocationAdapter;
import com.ptyt.uct.callback.MapCallBack;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.entity.MessageMyLocationEntity;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.common.SettingsConstant;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.NetUtils;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.widget.SwipeRefreshView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class MessageMyLocationActivity extends BaseActionBarActivity implements
        BaseRecyAdapter.OnItemClickListener,
        View.OnClickListener,
        SwipeRefreshView.OnLoadMoreListener,
        PoiSearch.OnPoiSearchListener,
        GeocodeSearch.OnGeocodeSearchListener,
        AMap.OnMapScreenShotListener {

    private MapView mapView;
    private RecyclerView recyclerView;
    private SwipeRefreshView swipeRefreshLayout;
    private RelativeLayout relativeLayout;
    private ImageView locationIv;
    private ImageView myLocationSelectIv;
    private LinearLayoutManager linearLayoutManager;
    // 精准定位的地址
    private TextView myLocationTv;

    private AMap mMap;
    private MyLocationStyle myLocationStyle;
    private MessageMyLocationAdapter mAdapter;
    // 每次加载的周边地址数
    private static final int PAGE_SIZE = 20;
    // 最大加载的页数
    private static final int MAX_PAGE_NUM = 10;
    // 当前第几页
    private int currentPageNum = 1;
    // 缓存纬度
    private double latitudeCache;
    // 缓存经度
    private double longitudeCache;
    // 精确搜索对象
    private GeocodeSearch geocodeSearch;
    // 第一行item的地址
    private String formatAddress;
    // 是否点击了Adapter的Item
    private boolean isClickItem = false;
    // 是否第一次定位
    private boolean isFirstLoc = true;
    // 是否点击了定位按钮
    private boolean isClickLocationButton = true;
    // 即将发送的地址文本信息
    //    private String addressText;
    // 用于生成地图截图文件名的对象信息
    private String msgSrcNo;
    private String msgDstNo;
    private long conversationId;
    private float currentZoom;
    private Marker marker;
    private ImageView positionIv;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_message_my_location;
    }

    @Override
    protected void initWidget() {
        EventBus.getDefault().register(this);
        tv_actionBarTitle.setText(getResources().getString(R.string.string_my_location_title));
        tv_actionBarRight.setVisibility(View.VISIBLE);
        tv_actionBarRight.setText(getResources().getString(R.string.string_my_location_send));
        tv_actionBarRight.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_13));
        tv_actionBarRight.setClickable(false);
        tv_actionBarRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.y28));
        tv_actionBarRight.setOnClickListener(this);

        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        relativeLayout.setOnClickListener(this);
        myLocationSelectIv = (ImageView) findViewById(R.id.myLocationSelectIv);
        positionIv = (ImageView) findViewById(R.id.positionIv);
        myLocationTv = (TextView) findViewById(R.id.myLocationTv);
        locationIv = (ImageView) findViewById(R.id.locationIv);
        locationIv.setOnClickListener(this);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mMap = mapView.getMap();
        marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                .decodeResource(getResources(), R.mipmap.icon_position))).anchor(0.5f, 0.7f));
        swipeRefreshLayout = (SwipeRefreshView) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorBlue);
        swipeRefreshLayout.setItemCount(PAGE_SIZE);
        swipeRefreshLayout.setOnLoadMoreListener(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // 获取Adapter第一个位置
                int topRowVerticalPosition = (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                // 滑到最上面阻止SwipeRefreshLayout刷新图标
                if (dy <= 0 && topRowVerticalPosition == 0) {
                    swipeRefreshLayout.setEnabled(false);
                }
            }
        });

    }

    @Override
    protected void initData() {
        msgSrcNo = getIntent().getStringExtra("msgSrcNo");
        msgDstNo = getIntent().getStringExtra("msgDstNo");
        conversationId = getIntent().getLongExtra("conversationId", 0);
        mAdapter = new MessageMyLocationAdapter(this);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
        mAdapter.clear();
        initMap();
    }

    /**
     * @param
     * @return
     * @description 初始化地图数据
     */
    private void initMap() {
        // 开始定位
        MapCallBack.getMapCallBack().mLocationClient.startLocation();
        // 初始化精确定位对象
        geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(this);
        // 初始化定位蓝点样式类
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER); //连续定位、蓝点不会移动到地图中心点，并且蓝点会跟随设备移动。
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));//设置定位蓝点精度圆圈的边框颜色的方法。
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));//设置定位蓝点精度圆圈的填充颜色的方法。
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_location));// 自定义蓝点定位图标
        myLocationStyle.interval(((Integer) UctClientApi.getUserData(SettingsConstant.SETTINGS_LOCATION_INTERVAL, 5000)).intValue()); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        mMap.setMyLocationStyle(myLocationStyle);// 设置定位蓝点的Style
        mMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        mMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {// 当移动地图或扩大/缩小地图时回调
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                PrintLog.i("onCameraChangeFinish");
                LatLng target = cameraPosition.target;
                // 获取地图缩放级别
                currentZoom = cameraPosition.zoom;
                PrintLog.i("latitudeCache = " + FileUtils.FormatDouble(latitudeCache) + "---------longitudeCache = " + FileUtils.FormatDouble(longitudeCache));
                PrintLog.i("target.latitude = " + FileUtils.FormatDouble(target.latitude) + "---------target.longitude = " + FileUtils.FormatDouble(target.longitude));
                // 防止同一坐标下，多次回调onCameraChangeFinish导致频繁刷新数据
                if (FileUtils.FormatDouble(latitudeCache) == FileUtils.FormatDouble(target.latitude) && FileUtils.FormatDouble(longitudeCache) == FileUtils.FormatDouble(target.longitude)) {
                    PrintLog.i("坐标重复 return");
                    isClickItem = false;
                    if (latitudeCache == 0.0d && longitudeCache == 0.0d) {
                        closeSendFunc();
                    }
                    return;
                }
                PrintLog.i("isClickItem = " + isClickItem);
                // 缓存坐标
                latitudeCache = target.latitude;// 纬度
                longitudeCache = target.longitude;// 经度
                // 判断是否点击了Adapter的Item触发的onCameraChangeFinish回调，如果是则不刷新Adapter数据，否则通过滑动地图和定位按钮都需要刷新Adapter数据
                if (isClickItem) {
                    isClickItem = false;
                } else {
                    if (!NetUtils.isNetworkAvailable(UctApplication.getInstance())) {
                        closeSendFunc();
                        return;
                    }
                    openSendFunc();
                    // 精准定位item的位置
                    mAdapter.setClickPosition(-1);
                    // 给精准定位地址打勾
                    myLocationSelectIv.setVisibility(View.VISIBLE);
                    // 搜索精准定位地址
                    searchAddressByLatlng(target);
                    // 将显示页数置为1
                    currentPageNum = 1;
                    // 清空数据
                    mAdapter.clear();
                    // 搜索附近地理数据
                    searchNearBy();
                }
            }
        });
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);// 默认的缩放按钮
        uiSettings.setMyLocationButtonEnabled(false); // 默认的定位按钮
        uiSettings.setZoomGesturesEnabled(true);// 缩放手势
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEvent(EventBean eventBean) {
        if (eventBean.getAction().equals(ConstantUtils.ACTION_LOCATION_CHANGE)) {
            if (isFirstLoc) {
                // 设置缩放级别
                mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                // 将地图移动到定位原点
                isFirstLoc = false;
            }
            if (isClickLocationButton) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MapCallBack.getMapCallBack().aMapLocation.getLatitude(), MapCallBack.getMapCallBack().aMapLocation.getLongitude()), 17));
                isClickLocationButton = false;
            }
            PrintLog.i(MapCallBack.getMapCallBack().aMapLocation.getLatitude() + "---------" + MapCallBack.getMapCallBack().aMapLocation.getLongitude());
        } else if (eventBean.getAction().equals(ConstantUtils.ACTION_NETWORK_CHANGED)) {
            if (eventBean.isNetworkAvailable(UctApplication.getInstance())) {
                if (latitudeCache == 0.0d && longitudeCache == 0.0d) {
                    closeSendFunc();
                } else {
                    openSendFunc();
                }
                latitudeCache = 0.0d;
                longitudeCache = 0.0d;
                MapCallBack.getMapCallBack().mLocationClient.startLocation();
            } else {
                closeSendFunc();
            }
        }
    }

    private void openSendFunc() {
        if (StrUtils.isEmpty(myLocationTv.getText().toString())) {
            relativeLayout.setVisibility(View.GONE);
        } else {
            relativeLayout.setVisibility(View.VISIBLE);
        }
        tv_actionBarRight.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_12));
        tv_actionBarRight.setClickable(true);
        swipeRefreshLayout.setLoading(false);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void closeSendFunc() {
        if (StrUtils.isEmpty(myLocationTv.getText().toString())) {
            relativeLayout.setVisibility(View.GONE);
        } else {
            relativeLayout.setVisibility(View.VISIBLE);
        }
        tv_actionBarRight.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_13));
        tv_actionBarRight.setClickable(false);
        swipeRefreshLayout.setLoading(true);
        swipeRefreshLayout.setRefreshing(true);
    }

    /**
     * @param
     * @return
     * @description 搜索地图中心点附近的数据
     */
    private void searchNearBy() {
        closeSendFunc();
        //第一个参数keyWord表示搜索字符串，
        //第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
        //第三个参数cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
        PoiSearch.Query query = new PoiSearch.Query("", "", "");
        // 设置每页最多返回多少条poiitem
        query.setPageSize(PAGE_SIZE);
        // 设置查询页码
        query.setPageNum(currentPageNum);
        PrintLog.i("currentPageNum = " + currentPageNum);
        PoiSearch poiSearch = new PoiSearch(this, query);
        // 设置搜索范围，第一个参数是坐标，第二个参数是半径
        poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(latitudeCache, longitudeCache), 1000));//设置周边搜索的中心点以及半径
        // 设置搜索监听器
        poiSearch.setOnPoiSearchListener(this);
        // 开始异步搜索
        poiSearch.searchPOIAsyn();
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        // code1000为搜索成功
        if (i == 1000) {
            // 将周围数据添加到Adapter中
            List<PoiItem> poiList = poiResult.getPois();
            if (poiList != null && poiList.size() > 0) {
                mAdapter.addMoreData(poiList);
                PrintLog.i("Title = " + poiList.get(0).getTitle());
                //                return;
            }
        }
        if ((mAdapter.getDatas() != null && mAdapter.getDatas().size() > 0) || !StrUtils.isEmpty(formatAddress)) {
            tv_actionBarRight.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_12));
            tv_actionBarRight.setClickable(true);
        } else {
            tv_actionBarRight.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_13));
            tv_actionBarRight.setClickable(false);
        }
        swipeRefreshLayout.setLoading(false);
        swipeRefreshLayout.setRefreshing(false);
        //        closeSendFunc();
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    /**
     * @param latLng
     * @return
     * @description 逆地理转换，将坐标转换成地址
     */
    private void searchAddressByLatlng(LatLng latLng) {
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        // 逆地理编码查询条件：逆地理编码查询的地理坐标点、查询范围、坐标类型。（GeocodeSearch.AMAP为火系坐标类型）
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 50f, GeocodeSearch.AMAP);
        // 开始异步查询
        geocodeSearch.getFromLocationAsyn(query);
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
        formatAddress = regeocodeAddress.getFormatAddress();
        PrintLog.i("精确formatAddress = " + formatAddress);
        myLocationTv.setText(formatAddress);
        //        if (StrUtils.isEmpty(formatAddress)) {
        if ((mAdapter.getDatas() != null && mAdapter.getDatas().size() > 0) || !StrUtils.isEmpty(formatAddress)) {
            if (StrUtils.isEmpty(myLocationTv.getText().toString())) {
                relativeLayout.setVisibility(View.GONE);
            } else {
                relativeLayout.setVisibility(View.VISIBLE);
            }
            tv_actionBarRight.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_12));
            tv_actionBarRight.setClickable(true);
        } else {
            tv_actionBarRight.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_13));
            tv_actionBarRight.setClickable(false);
            relativeLayout.setVisibility(View.GONE);
        }
        mAdapter.setPlaceName(formatAddress);
        mAdapter.setSpecificPlaceName(formatAddress);
        //            addressText = formatAddress;
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void onMapScreenShot(Bitmap bitmap) {
        if (null == bitmap) {
            PrintLog.e("截屏失败 bitmap = null");
            return;
        }
        try {
            String photoPath = SDCardUtils.getChatRecordPath(conversationId, msgSrcNo + "_" + msgDstNo) + StrUtils.getSmsId(msgDstNo, msgSrcNo) + ".jpg";
            FileOutputStream fos = new FileOutputStream(photoPath);
            boolean b = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            try {
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (b) {
                PrintLog.i("截屏成功 bitmap压缩成功");
            } else {
                PrintLog.e("截屏失败 bitmap压缩失败");
            }

            MessageMyLocationEntity entity = new MessageMyLocationEntity();
            if (!StrUtils.isEmpty(photoPath)) {
                File file = new File(photoPath);
                if (file.exists()) {
                    entity.setSize(file.length());
                    entity.setLocalPath(photoPath);
                    entity.setType(MessageDBConstant.INFO_TYPE_MY_LOCATION);
                    entity.setZoom(currentZoom);
                    entity.setLatitude(latitudeCache);
                    entity.setLongitude(longitudeCache);
                    entity.setPlaceName(mAdapter.getPlaceName());
                    entity.setSpecificPlaceName(mAdapter.getSpecificPlaceName());
                    PrintLog.i("size = " + file.length() + " path = " + photoPath + " type = " + MessageDBConstant.INFO_TYPE_MY_LOCATION + " zoom = " + currentZoom +
                            " Latitude = " + latitudeCache + " Longitude = " + longitudeCache + " PlaceName = " + mAdapter.getPlaceName() + " SpecificPlaceName = " + mAdapter.getSpecificPlaceName());
                } else {
                    ToastUtils.getToast().showMessageShort(this, getString(R.string.string_my_location_unknown_path), -1);
                    return;
                }
            }
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putParcelable("MyLocationEntity", entity);
            intent.putExtras(bundle);
            setResult(MessageActivity.LOCATION_RESULT_CODE, intent);

            finish();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onMapScreenShot(Bitmap bitmap, int i) {

    }

    @Override
    public void onItemClick(int pos, View itemView) {
        // 点击了Adpter的item
        isClickItem = true;
        // 设置点击位置并刷新对勾
        mAdapter.setClickPosition(pos);
        // 将精确定位地址的对勾隐藏
        myLocationSelectIv.setVisibility(View.INVISIBLE);
        LatLonPoint point = mAdapter.getItem(pos).getLatLonPoint();
        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
        // 将地图移动到定位点
        mMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        // 获取地址文本信息
        //        addressText = mAdapter.getItem(pos).getProvinceName() + mAdapter.getItem(pos).getCityName() + mAdapter.getItem(pos).getAdName() + mAdapter.getItem(pos).getSnippet();
    }

    @Override
    public void onLoadMore() {
        if (currentPageNum < MAX_PAGE_NUM) {
            currentPageNum++;
            searchNearBy();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locationIv:
                PrintLog.i(MapCallBack.getMapCallBack().aMapLocation.getLatitude() + "---------" + MapCallBack.getMapCallBack().aMapLocation.getLongitude());
                isClickLocationButton = true;
                MapCallBack.getMapCallBack().mLocationClient.startLocation();
                break;
            case R.id.relativeLayout:
                // 没有点击了Adpter的item
                isClickItem = true;
                // 设置精确定位地址的位置
                mAdapter.setClickPosition(-1);
                mAdapter.setPlaceName(formatAddress);
                mAdapter.setSpecificPlaceName(formatAddress);
                // 显示精确定位地址的对勾
                myLocationSelectIv.setVisibility(View.VISIBLE);
                LatLng latLng = new LatLng(latitudeCache, longitudeCache);
                // 将地图移动到定位点
                mMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
                break;
            case R.id.tv_right1:
                if (!SDCardUtils.isAvailableInternalMemory()) {
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.msg_msg_send_error_2), -1);
                    return;
                }
                if (latitudeCache == 0.0d && longitudeCache == 0.0d) {
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_my_location_unknown_location), -1);
                    return;
                }
                PrintLog.i("发送数据啦");
                LatLng target = new LatLng(latitudeCache, longitudeCache);
                marker.setPosition(target);
                // 地图截屏
                mMap.getMapScreenShot(MessageMyLocationActivity.this);
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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
