package com.ptyt.uct.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.widget.ImageView;

import com.android.uct.IUCTInitListener;
import com.android.uct.UctLibConfigKey;
import com.android.uct.exception.UctLibException;
import com.android.uct.exception.UctLibInitializationException;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.callback.CallCallBack;
import com.ptyt.uct.callback.DataTransportInfoCallBack;
import com.ptyt.uct.callback.GMemberListCallBack;
import com.ptyt.uct.callback.GroupInfoCallback;
import com.ptyt.uct.callback.LoginCallBack;
import com.ptyt.uct.callback.MapCallBack;
import com.ptyt.uct.callback.MessageCallBack;
import com.ptyt.uct.callback.PhoneConfigCallBack;
import com.ptyt.uct.callback.SoftUpgradeCallBack;
import com.ptyt.uct.common.SettingsConstant;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.utils.ActivitySkipUtils;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.SDCardUtils;

import pub.devrel.easypermissions.AppSettingsDialog;

/**
 * @Description: 欢迎页面，用以跳转登录界面或是主界面
 * @Date: 2017/4/24
 * @Author: ShaFei
 * @Version: V1.0
 */
public class SplashActivity extends BasePermissionActivity {

    private ImageView mSplashView;
    private int reqTimes = 1;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrintLog.i("onCreate()");
        // 开启强启应用服务
        //        JobSchedulerManager.getJobSchedulerInstance(UctApplication.getInstance()).startJobScheduler();
        if (!isTaskRoot()) {
            PrintLog.i("不是task根部，finish掉");
            boolean isUserOnline = UctClientApi.isUserOnline();
            if (isUserOnline) {
                ActivitySkipUtils.toNextActivityAndFinish(SplashActivity.this, MainActivity.class);
            } else {
                ActivitySkipUtils.toNextActivityAndFinish(SplashActivity.this, LoginActivity.class);
            }
            return;
        } else {
            PrintLog.i("是task根部，正常初始化流程");
            initViews();
        }
    }

    public void initViews() {
        PrintLog.i("initViews()");
        setContentView(R.layout.activity_splash);
        mSplashView = (ImageView) findViewById(R.id.splash_iv);
        //申请初始化app所需要的权限
        startReqPermOfInitApp();
    }

    public void initLib(Context mContext) {
        if (UctClientApi.isHaveInitLib()) {
            boolean isUserOnline = UctClientApi.isUserOnline();
            if (isUserOnline) {
                ActivitySkipUtils.toNextActivityAndFinish(SplashActivity.this, MainActivity.class);
            } else {
                ActivitySkipUtils.toNextActivityAndFinish(SplashActivity.this, LoginActivity.class);
            }
            return;
        }
        int isSuccessInitSdk = UctClientApi.initSdk(false, initListener, mContext);
        if (isSuccessInitSdk == -1) {
            throw new UctLibInitializationException();
        }
        int logSwitch = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_LOG_SWITCH, 1);
        boolean isOpenLogSwitch = logSwitch == 0 ? false : true;
        if (isOpenLogSwitch) {
            UctClientApi.saveLog(SDCardUtils.getLogBasePath());
        } else {
            UctClientApi.cancelSaveLog();
        }
        int setAndroidVm = UctClientApi.setUctAndroidVm(mContext);
        if (setAndroidVm == -1) {
            throw new UctLibInitializationException();
        }
        CallCallBack.getInstance().initSurfaceVideo(mContext);
    }

    private IUCTInitListener initListener = new IUCTInitListener() {

        @Override
        public int UCT_Read_Int_Cfg(int type) throws UctLibException {
            switch (type) {
                // 返回终端类型->智能终端5
                case UctLibConfigKey.UCT_INT_CFG_FORWARD_DEVICE_TYPE:
                    return 5;
                default:
                    Object value = UctClientApi.getUserData(UctLibConfigKey.CONFIGLIB + type, -1);
                    return ((Integer) value).intValue();
            }
        }

        @Override
        public void intSdkCfm(int result, long initTime) {
            // 释放初始化库监听接口
            UctClientApi.unregisterObserver(initListener, IUCTInitListener.IUCTINITLISTENER_INDEX);
            // 初始化登录回调
            LoginCallBack.getLoginCallBack().init(UctApplication.getInstance());
            // 初始化地图回调
            MapCallBack.getMapCallBack().init(UctApplication.getInstance());
            // 初始化数据传输回调
            DataTransportInfoCallBack.getDataTransportInfoCallBack().init(UctApplication.getInstance());
            // 初始化升级回调
            SoftUpgradeCallBack.getSoftUpgradeCallBack().init(UctApplication.getInstance());
            // 初始化手机配置参数回调
            PhoneConfigCallBack.getPhoneConfigCallBack().init(UctApplication.getInstance());
            // 初始化信息回调
            MessageCallBack.getMessageCallBack().init(UctApplication.getInstance());
            // 初始化呼叫业务(组呼除外)
            CallCallBack.getInstance().init(UctApplication.getInstance());
            // 用户组通知
            GroupInfoCallback.getInstance().init(UctApplication.getInstance());
            // 组数据回调
            GMemberListCallBack.getInstance().init(UctApplication.getInstance());
            boolean isUserOnline = UctClientApi.isUserOnline();
            if (isUserOnline) {
                ActivitySkipUtils.toNextActivityAndFinish(SplashActivity.this, MainActivity.class);
            } else {
                ActivitySkipUtils.toNextActivityAndFinish(SplashActivity.this, LoginActivity.class);
            }
        }
    };


    @Override
    public void appPermissionDeny() {
        PrintLog.i("appPermissionDeny()");
        //申请初始化app所需要的权限
        startReqPermOfInitApp();
    }

    /**
     * app库权限申请成功的回调
     */
    @Override
    public void appPermissionGranted() {
        PrintLog.i("appPermissionGranted 1()");
        if (!AppUtils.isFastClick2()) {
            PrintLog.i("appPermissionGranted 2()");
            initLib(UctApplication.getInstance());
        }
    }

    /**
     * 提醒在设置中权限设置的弹框，点击“取消”,"确定"的回调
     * 1.点击取消时，3次申请权限，还是取消则退出应用；
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PrintLog.i("onActivityResult()");
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            if (reqTimes < 5) {
                reqTimes++;
                startReqPermOfInitApp();
            } else {
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
