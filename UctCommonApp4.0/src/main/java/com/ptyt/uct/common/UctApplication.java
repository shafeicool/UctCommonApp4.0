package com.ptyt.uct.common;

import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.os.Build;
import android.support.multidex.MultiDexApplication;

import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.widget.GroupCallWindow;

import static com.ptyt.uct.common.AppContext.getAppContext;

/**
 * @Description: Application管理类，用以初始化SDK、一些工具类、环境相关的东西
 * @Date: 2017/4/26
 * @Author: ShaFei
 * @Version: V1.0
 */

public class UctApplication extends MultiDexApplication {

    private static UctApplication app;
    //当前组呼悬浮窗在不在MainActivity,
    public boolean isInMainActivity = true;
    public boolean isInGroupCall = false;
    private GroupCallWindow instance;

    @Override
    public void onCreate() {
        UctClientApi.saveLog(SDCardUtils.getLogBasePath());
        PrintLog.i("onCreate");
        super.onCreate();
        app = this;
        initErrorHandler();
        getAppContext().init(this);
        PrintLog.i("onCreate Build.MANUFACTURER="+ Build.MANUFACTURER + "  Build.BRAND="+Build.BRAND +"  Build.PRODUCT="+Build.PRODUCT);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static synchronized UctApplication getInstance() {
        return app;
    }

    public GroupCallWindow getGroupCallWindow() {
        if (instance == null) {
            instance = new GroupCallWindow(getApplicationContext());
        }
        return instance;
    }

    private void initErrorHandler() {
        CrashHandler handler = CrashHandler.getInstance();
        handler.init(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        PrintLog.i("onConfigurationChanged");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        PrintLog.i("onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        PrintLog.i("onTrimMemory");
    }

    @Override
    public void registerComponentCallbacks(ComponentCallbacks callback) {
        super.registerComponentCallbacks(callback);
        PrintLog.i("registerComponentCallbacks");
    }

    @Override
    public void unregisterComponentCallbacks(ComponentCallbacks callback) {
        super.unregisterComponentCallbacks(callback);
        PrintLog.i("unregisterComponentCallbacks");
    }

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.registerActivityLifecycleCallbacks(callback);
        PrintLog.i("registerActivityLifecycleCallbacks");
    }

    @Override
    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.unregisterActivityLifecycleCallbacks(callback);
        PrintLog.i("unregisterActivityLifecycleCallbacks");
    }

    @Override
    public void registerOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        super.registerOnProvideAssistDataListener(callback);
        PrintLog.i("registerOnProvideAssistDataListener");
    }

    @Override
    public void unregisterOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        super.unregisterOnProvideAssistDataListener(callback);
        PrintLog.i("unregisterOnProvideAssistDataListener");
    }
}
