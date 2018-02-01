package com.ptyt.uct.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @Description: 网络处理工具类
 * @Date: 2017/4/26
 * @Author: ShaFei
 * @Version: V1.0
 */

public class NetUtils {
    private NetUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    static ConnectivityManager connectivity;

    public static boolean isNetworkAvailable(Context mActivity) {
        if (connectivity == null) {
            connectivity = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        if (connectivity == null) {
            return false;
        } else {
            //			NetworkInfo[] info = connectivity.getAllNetworkInfo();
            //			if (info != null) {
            //				for (int i = 0; i < info.length; i++) {
            //					if (info[i].isConnected()) {
            //						return true;
            //					}
            //				}
            //			}
            NetworkInfo mNetworkInfo = connectivity.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断是否是wifi连接
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null)
            return false;
        return cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;

    }

    /**
     * 打开网络设置界面
     */
    public static void openSetting(Activity activity) {
        Intent intent = new Intent("/");
        ComponentName cm = new ComponentName("com.android.settings",
                "com.android.settings.WirelessSettings");
        intent.setComponent(cm);
        intent.setAction("android.intent.action.VIEW");
        activity.startActivityForResult(intent, 0);
    }

}
