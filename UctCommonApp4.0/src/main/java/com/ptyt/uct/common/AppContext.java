package com.ptyt.uct.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.activity.SplashActivity;

/**
 * Title: com.ptyt.uct.common
 * Description: 应用回调接口初始化，每个接口最多只能注册十次
 * Date: 2017/5/8
 * Author: ShaFei
 * Version: V1.0
 */

public class AppContext {
    private static AppContext instance = null;
    private Context mContext;
    private String loginNumber;
    private String loginPassword;
    private String loginIp;
    private String currentNodeDn;

    public static synchronized AppContext getAppContext() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public void init(Context context) {
        mContext = context;

    }

    public boolean isHaveInitLib(final Activity activity) {
        PrintLog.i("isHaveInitLib = " + UctClientApi.isHaveInitLib());
        if (!UctClientApi.isHaveInitLib() && !(activity instanceof SplashActivity)) {
            Intent intent = new Intent(activity, SplashActivity.class);
            activity.startActivity(intent);
            return false;
        }
        return true;
    }

    public void setLoginNumber(String loginNumber) {
        this.loginNumber = loginNumber;
    }

    public String getLoginNumber() {
        return loginNumber;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public void setCurrentNodeDn(String currentNodeDn) {
        this.currentNodeDn = currentNodeDn;
    }

    public String getCurrentNodeDn() {
        return currentNodeDn;
    }

}
