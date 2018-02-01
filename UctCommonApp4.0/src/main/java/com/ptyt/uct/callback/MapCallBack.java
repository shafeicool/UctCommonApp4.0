package com.ptyt.uct.callback;

import android.content.Context;
import android.content.Intent;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.services.MapService;
import com.ptyt.uct.utils.ConstantUtils;

import de.greenrobot.event.EventBus;

/**
 * Title: com.ptyt.uct.callback
 * Description:
 * Date: 2017/10/11
 * Author: ShaFei
 * Version: V1.0
 */

public class MapCallBack extends BaseCallBack {

    private static MapCallBack instance = null;
    private Context mContext;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    public AMapLocation aMapLocation = null;
    private Intent locationIntent;

    public static synchronized MapCallBack getMapCallBack() {
        if (instance == null) {
            instance = new MapCallBack();
        }
        return instance;
    }

    @Override
    public void init(Context context) {
        mContext = context;
        PrintLog.w("注册MapCallBack");
        initGaodeMap();
        //开始定位
        PrintLog.i("开启服务");
        locationIntent = new Intent(mContext, MapService.class);
        mContext.startService(locationIntent);
    }

    @Override
    public void release() {
        PrintLog.w("反注册MapCallBack");
        if (null != MapCallBack.getMapCallBack().mLocationClient) {
            MapCallBack.getMapCallBack().mLocationClient.onDestroy();
        }
        //关掉定位服务
        mContext.stopService(locationIntent);
        mLocationClient.stopLocation();
    }

    private void initGaodeMap() {

        //初始化定位
        mLocationClient = new AMapLocationClient(mContext);
        //设置定位回调监听
        mLocationClient.setLocationListener(new AMapLocationListener() {

            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
//                PrintLog.e("xigu12233445--onLocationChanged--Latitude=" + aMapLocation.getLatitude() + "  Longitude=" + aMapLocation.getLongitude());
                //如果位置改变
                MapCallBack.this.aMapLocation = aMapLocation;
                EventBus.getDefault().post(new EventBean(ConstantUtils.ACTION_LOCATION_CHANGE));
            }
        });
        //给定位客户端对象设置定位参数
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        //mLocationOption.setOnceLocationLatest(true);
        mLocationOption.setOnceLocation(true);
//        mLocationOption.setInterval(((Integer) UctClientApi.getUserData(SettingsConstant.SETTINGS_LOCATION_INTERVAL, 5000)).intValue());
        mLocationOption.setNeedAddress(true);
        //设置为高精度定位模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationClient.setLocationOption(mLocationOption);
    }
}
