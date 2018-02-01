package com.ptyt.uct.callback;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.services.PtytService;
import com.ptyt.uct.services.ServiceBinder;

/**
 * @Description:
 * @Date: 2017/5/9
 * @Author: ShaFei
 * @Version: V1.0
 */

public class MessageCallBack extends BaseCallBack {

    private static MessageCallBack instance = null;
    private Context mContext = null;
    // 基础服务的对象
    private ServiceBinder serviceBinder;
    // 判断服务是否绑定成功
    private boolean isBinderSuccess = false;


    public static synchronized MessageCallBack getMessageCallBack() {
        if (instance == null) {
            instance = new MessageCallBack();
        }
        return instance;
    }

    @Override
    public void init(Context context) {
        mContext = context;
        PrintLog.w("注册MessageCallBack");
        // 启动基础服务
        bindService();
    }

    @Override
    public void release() {
        PrintLog.w("反注册MessageCallBack");
        //解绑服务
        unbindService();
        super.closeHandlerThread();
    }

    private void bindService() {
        if (isBinderSuccess) {
            PrintLog.e("The service already binder");
            throw new RuntimeException("service already binder");
        }
        PrintLog.i("bindService");
        Intent serviceIntent = new Intent(mContext, PtytService.class);
        mContext.startService(serviceIntent);
        mContext.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
    }

    private void unbindService() {
        if (isBinderSuccess) {
            PrintLog.i("unbindService");
            Intent serviceIntent = new Intent(mContext, PtytService.class);
            mContext.stopService(serviceIntent);
            mContext.unbindService(conn);
            isBinderSuccess = false;
            return;
        }
        PrintLog.e("The service is not yet bound");
        throw new RuntimeException("service already binder");
    }

    private ServiceConnection conn = new ServiceConnection() {
        /** 获取服务对象时的操作 */
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBinderSuccess = true;
            PrintLog.i("onServiceConnected {" + "name=" + name + ", service=" + service + '}');
            if (!(service instanceof ServiceBinder)) {
                throw new RuntimeException("class type error.");
            }
            serviceBinder = (ServiceBinder) service;
        }

        /** 异常退出或内存不足出现异常是*/
        public void onServiceDisconnected(ComponentName name) {
            PrintLog.i("onServiceDisconnected {" + "name=" + name + '}');
            serviceBinder = null;
            isBinderSuccess = false;
        }
    };

    public ServiceBinder getServiceBinder() {
        if (!isBinderSuccess) {
            PrintLog.e("serviceBinder is null");
            return null;
        }
        return serviceBinder;
    }

}
