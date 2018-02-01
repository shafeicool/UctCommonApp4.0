package com.ptyt.uct.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.android.uct.utils.PrintLog;

/**
 * 使用的公共服务
 */
public class PtytService extends Service {
    /**
     * 创建binder对象
     */
    private ServiceBinder ptytBinder=null;

    @Override
    public void onCreate() {
        super.onCreate();
        PrintLog.i("init service."+toString());
        ptytBinder = new ServiceBinder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PrintLog.i( "onStartCommand{" + "intent=" + intent + ", flags=" + flags + ", startId=" + startId + '}');
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        PrintLog.i("onBind {" + "intent=" + intent +"}");
        return ptytBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        PrintLog.i("onUnbind {" + "intent=" + intent +"}");
        return super.onUnbind(intent);
    }

    /**
     * 在service和旧的client之间的所有捆绑联系在onUnbind里面全都结束之后，如果有一个新的client用bind连接上service，就会启动onRebind（）
     */
    @Override
    public void onRebind(Intent intent) {
        PrintLog.i("onRebind {" + "intent=" + intent +"}");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        PrintLog.i("onDestroy");
        super.onDestroy();
    }

}



