package com.ptyt.uct.widget;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import static android.os.Looper.getMainLooper;

/**
 * Title: com.ptyt.uct.widget
 * Description:
 * Date: 2017/8/1
 * Author: ShaFei
 * Version: V1.0
 */

public abstract class MessageBaseLayout extends FrameLayout implements Handler.Callback {

    private View view;
    private HandlerThread uIhandlerThread;
    public Handler myHandler;
    private boolean isNewMyLooper = false;

    public MessageBaseLayout(@NonNull Context context) {
        super(context);
    }

    public MessageBaseLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageBaseLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    final void initWindow(Context context) {
        view = LayoutInflater.from(context).inflate(setLayoutId(), null);
        removeAllViews();
        LayoutParams mLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(view, mLayoutParams);
        initView(view);
        initData();
    }

    protected abstract int setLayoutId();

    protected abstract void initView(View view);

    protected abstract void initData();

    /**
     * 选择是否使用Handler
     */
    public void setNewMyLooper(boolean isNewMyLooper) {
        this.isNewMyLooper = isNewMyLooper;
        if (isNewMyLooper) {
            uIhandlerThread = new HandlerThread(getClass().getSimpleName());
            uIhandlerThread.setPriority(Thread.MAX_PRIORITY);
            uIhandlerThread.start();
            myHandler = new Handler(uIhandlerThread.getLooper(), this);
        } else {
            myHandler = new Handler(getMainLooper(), this);
        }
    }

    /**
     * 关闭handler线程
     */
    public void closeHandlerThread() {
        if (isNewMyLooper) {
            if (uIhandlerThread != null) {
                myHandler.getLooper().quit();
                uIhandlerThread = null;
                myHandler = null;
            }
        }
    }

    /**
     * 发送Message消息
     */
    public void sendMsg(int what) {
        if (!isNewMyLooper) {
            return;
        }
        Message msg = myHandler.obtainMessage(what);
        myHandler.sendMessage(msg);
    }


    @Override
    public boolean handleMessage(Message message) {
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        closeHandlerThread();
        super.onDetachedFromWindow();
    }
}
