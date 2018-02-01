package com.ptyt.uct.common;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.activity.LoginActivity;
import com.ptyt.uct.callback.CallCallBack;
import com.ptyt.uct.callback.DataTransportInfoCallBack;
import com.ptyt.uct.callback.GMemberListCallBack;
import com.ptyt.uct.callback.GroupInfoCallback;
import com.ptyt.uct.callback.LoginCallBack;
import com.ptyt.uct.callback.MapCallBack;
import com.ptyt.uct.callback.MessageCallBack;
import com.ptyt.uct.callback.PhoneConfigCallBack;
import com.ptyt.uct.callback.SoftUpgradeCallBack;
import com.ptyt.uct.entity.LoginUserEntity;
import com.ptyt.uct.model.ContactDBManager;
import com.ptyt.uct.utils.ActivitySkipUtils;

import java.util.LinkedList;

/**
 * @Description: 应用程序Activity管理类：用于Activity管理和应用程序退出
 * @Date: 2017/4/26
 * @Author: ShaFei
 * @Version: V1.0
 */
public class AppManager {

    private static LinkedList<Activity> activityStack;
    private static AppManager instance;

    private AppManager() {
    }

    /**
     * 单一实例
     */
    public static AppManager getAppManager() {
        if (instance == null) {
            instance = new AppManager();
        }
        return instance;
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new LinkedList<Activity>();
        }
        activityStack.add(activity);
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public Activity currentActivity() {
        Activity activity = activityStack.getLast();
        return activity;
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    public void finishActivity() {
        Activity activity = activityStack.getLast();
        finishActivity(activity);
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            if (activityStack != null) {
                activityStack.remove(activity);
            }
            activity = null;
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        if (activityStack == null) {
            return;
        }
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity2() {
        if (activityStack == null) {
            return;
        }
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                if (activityStack.get(i).getComponentName().getClassName().equals(LoginActivity.class.getName())) {
                    continue;
                }
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }

    /**
     * 退出应用程序
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void AppExit(Context context) {
        PrintLog.i("AppExit()");
        try {
            // 关闭Activity
            finishAllActivity();
            // 释放回调监听器
            release();
            // 首先注销
            UctClientApi.uctLogOut(0);
            // 释放动态库资源
            UctClientApi.UCTEnd();
            // 关闭强启应用服务
            //            JobSchedulerManager.getJobSchedulerInstance(UctApplication.getInstance()).stopJobScheduler();
            // 杀死后台进程
            ActivityManager activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            activityMgr.killBackgroundProcesses(context.getPackageName());
            // 应用退出
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出登录
     */
    public void AppLogOut(Context context) {
        LoginUserEntity user = LoginUserEntity.getUserData();
        user.setPassword("");
        UctClientApi.uctLogOut(0);// 登出，停止发包
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_LOCK_GROUP, "");
        ContactDBManager.getInstance(context).deleteContacts();
        ActivitySkipUtils.toNextActivityAndFinish(context, LoginActivity.class);
        finishAllActivity2();
    }

    public int ActivityStackSize() {
        return activityStack == null ? 0 : activityStack.size();
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void release() {
        // 释放登录回调
        LoginCallBack.getLoginCallBack().release();
        // 释放地图回调
        MapCallBack.getMapCallBack().release();
        // 释放数据传输回调
        DataTransportInfoCallBack.getDataTransportInfoCallBack().release();
        // 释放升级回调
        SoftUpgradeCallBack.getSoftUpgradeCallBack().release();
        // 释放手机配置参数回调
        PhoneConfigCallBack.getPhoneConfigCallBack().release();
        // 释放信息回调
        MessageCallBack.getMessageCallBack().release();
        // 释放呼叫业务回调(除组呼外)
        CallCallBack.getInstance().release();
        // 释放用户组通知
        GroupInfoCallback.getInstance().release();
        // 释放组数据回调
        GMemberListCallBack.getInstance().release();
    }
}