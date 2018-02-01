package com.ptyt.uct.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.model.CallRecordDBManager;
import com.ptyt.uct.model.ContactDBManager;
import com.ptyt.uct.model.ConversationDBManager;
import com.ptyt.uct.model.GroupDBManager;
import com.ptyt.uct.model.GroupUserDBManager;
import com.ptyt.uct.model.LoginUserDBManager;
import com.ptyt.uct.model.MessageDBManager;
import com.ptyt.uct.utils.ToastUtils;

import static android.os.Looper.getMainLooper;

/**
 * @Description:
 * @Date: 2017/5/9
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public abstract class BaseFragment extends Fragment implements Handler.Callback {

    private HandlerThread uIhandlerThread;
    public Handler myHandler;
    private boolean isNewMyLooper = false;
    public Context mContext;

    protected OnEventListener onEventListener = null;
    protected Bundle savedInstanceState;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(setLayoutId(),null);
        mContext = getActivity();
        initView(view);
        initData();
        initEvent();
        this.savedInstanceState = savedInstanceState;
        PrintLog.i("onCreateView");
//        // 是否使用子线程
//        if(isNewMyLooper){
//            uIhandlerThread= new HandlerThread(getClass().getSimpleName());
//            uIhandlerThread.setPriority(Thread.MAX_PRIORITY);
//            uIhandlerThread.start();
//            myHandler = new Handler(uIhandlerThread.getLooper(),this);
//        }else{
//            myHandler = new Handler(getMainLooper(),this);
//        }
        return view;
    }

    /**
     * 设置事件
     */
    protected void initEvent() {

    }

    /**
     * 初始化数据
     */
    protected void initData() {
    }

    /**
     * 初始化view
     * @param view
     */
    protected void initView(View view) {
    }

    /**
     * 设置布局id
     * @return
     */
    protected abstract int setLayoutId();

    public interface OnEventListener {
        public void onEvent(int what, Bundle data, Object object);
    }

    public void setOnEventListener(OnEventListener onEventListener) {
        this.onEventListener = onEventListener;
    }

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

    @Override
    public void onDestroyView() {
        CallRecordDBManager.getInstance(mContext).release();
        ContactDBManager.getInstance(mContext).release();
        ConversationDBManager.getInstance(mContext).release();
        GroupDBManager.getInstance().release();
        GroupUserDBManager.getInstance(mContext).release();
        LoginUserDBManager.getInstance(mContext).release();
        MessageDBManager.getInstance(mContext).release();
        ToastUtils.getToast().release();
        closeHandlerThread();
        super.onDestroyView();
    }
}
