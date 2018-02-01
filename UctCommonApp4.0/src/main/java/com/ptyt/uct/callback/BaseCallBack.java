package com.ptyt.uct.callback;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import static android.os.Looper.getMainLooper;

/**
 * Title: com.ptyt.uct.callback
 * Description:
 * Date: 2017/5/8
 * Author: ShaFei
 * Version: V1.0
 */

public abstract class BaseCallBack implements Handler.Callback {

    private HandlerThread uIhandlerThread;
    public Handler myHandler;
    private boolean isNewMyLooper = false;

    /**
     * 初始化回调接口
     */
    public abstract void init(Context context);

    /**
     * 释放回调接口
     */
    public abstract void release();

    /**
     * 关闭handler线程
     */
    public void closeHandlerThread() {
        if(isNewMyLooper){
            if(uIhandlerThread != null){
                myHandler.getLooper().quit();
                uIhandlerThread=null;
                myHandler=null;
            }
        }
    }

    /**
     * 发送Message消息
     */
    public void sendMsg(boolean bMoveOld, int what) {
        if(!isNewMyLooper){
            return;
        }
        bMoveOld=false;
        if (bMoveOld)
            myHandler.removeMessages(what);
        Message msg = myHandler.obtainMessage(what);
        myHandler.sendMessage(msg);
    }

    public void sendMsgDelayed(boolean bMoveOld, int what, long delayMillis) {
        if(!isNewMyLooper){
            return;
        }
        bMoveOld=false;
        if (bMoveOld)
            myHandler.removeMessages(what);
        Message msg = myHandler.obtainMessage(what);
        myHandler.sendMessageDelayed(msg, delayMillis);
    }

    public void sendMsg(boolean bMoveOld, int what, int arg1, String... strArgs) {
        if(!isNewMyLooper){
            return;
        }
        if (bMoveOld)
            myHandler.removeMessages(what);
        Message msg = myHandler.obtainMessage(what, arg1, 0);
        for (int i = 0; i < strArgs.length; ++i) {
            msg.getData().putString("strArg_" + i, strArgs[i]);
        }
        myHandler.sendMessage(msg);
    }

    public void sendMsg(boolean bMoveOld, int what, int arg1, int arg2, String... strArgs) {
        if(myHandler==null){
            return;
        }
        if (bMoveOld)
            myHandler.removeMessages(what);
        Message msg = myHandler.obtainMessage(what, arg1, arg2);
        for (int i = 0; i < strArgs.length; ++i) {
            msg.getData().putString("strArg_" + i, strArgs[i]);
        }
        myHandler.sendMessage(msg);
    }
    public void sendMsgDelayed(boolean bMoveOld, int what, long delayMillis,int arg1, int arg2, String... strArgs) {
        if(myHandler==null){
            return;
        }
        if (bMoveOld)
            myHandler.removeMessages(what);
        Message msg = myHandler.obtainMessage(what, arg1, arg2);
        for (int i = 0; i < strArgs.length; ++i) {
            msg.getData().putString("strArg_" + i, strArgs[i]);
        }
        myHandler.sendMessageDelayed(msg, delayMillis);
    }

    public void sendMsg(boolean bMoveOld, int what, int type, int arg1, int arg2, int arg3, String... strArgs) {
        if(myHandler==null){
            return;
        }
        if (bMoveOld)
            myHandler.removeMessages(what);
        Message msg = myHandler.obtainMessage(what, type, 0);
        for (int i = 0; i < strArgs.length; ++i) {
            msg.getData().putString("strArg_" + i, strArgs[i]);
        }
        msg.getData().putInt("arg1", arg1);
        msg.getData().putInt("arg2", arg2);
        msg.getData().putInt("arg3", arg3);
        myHandler.sendMessage(msg);
    }

    /**
     * 选择是否使用Handler
     */
    public void setNewMyLooper(boolean isNewMyLooper) {
        this.isNewMyLooper = isNewMyLooper;
        if(isNewMyLooper){
            uIhandlerThread= new HandlerThread(getClass().getSimpleName());
            uIhandlerThread.setPriority(Thread.MAX_PRIORITY);
            uIhandlerThread.start();
            myHandler = new Handler(uIhandlerThread.getLooper(),this);
        }else{
            myHandler = new Handler(getMainLooper(),this);
        }

    }

    @Override
    public boolean handleMessage(Message message) {
        return false;
    }

    public String getStrArg(Message msg, int index) {
        String tmp = msg.getData().getString("strArg_" + index);
        return tmp == null ? "" : tmp;
    }

}
