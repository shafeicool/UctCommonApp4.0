package com.ptyt.uct.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.callback.MapCallBack;

import static com.ptyt.uct.common.SettingsConstant.SETTINGS_LOCATION_INTERVAL;

/**
 * @Description: 默认每5秒定位
 * TODO 待整合至PtytService
 * @Date: 2017/11/28
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class MapService extends Service {

    private Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        PrintLog.i("init service."+toString());
        startLocation();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        PrintLog.i("onBind {" + "intent=" + intent +"}");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        PrintLog.i("onUnbind {" + "intent=" + intent +"}");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PrintLog.i( "onStartCommand{" + "intent=" + intent + ", flags=" + flags + ", startId=" + startId + '}');
        return START_NOT_STICKY;
    }

    private void startLocation() {
        mHandler.post(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            MapCallBack.getMapCallBack().mLocationClient.startLocation();
            mHandler.postDelayed(this, ((Integer) UctClientApi.getUserData(SETTINGS_LOCATION_INTERVAL, 5000)).intValue());
        }
    };

    @Override
    public void onDestroy() {
        PrintLog.e("onDestroy()");
        mHandler.removeCallbacks(runnable);
        super.onDestroy();
    }
}


