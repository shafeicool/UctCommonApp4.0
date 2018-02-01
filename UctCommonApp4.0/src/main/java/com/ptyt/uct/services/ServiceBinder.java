package com.ptyt.uct.services;

import android.content.Context;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.common.MessageDBConstant;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ptyt.uct.common.MessageDBConstant.FILE_STATUS_WAIT_RECEIVING;
import static com.ptyt.uct.common.MessageDBConstant.MSG_STATUS_WAIT_SENDING;

public class ServiceBinder extends BaseBinder implements IShortMsgInterface {
    private MsgBinder msgBinder;
    /**
     * 线程池
     */
    private ExecutorService mThreadPool = null;
    /**
     * 线程池个数
     */
    private static final int THREAD_COUNTS = 3;

    ServiceBinder(Context mContext) {
        msgBinder = new MsgBinder(mContext);
        mThreadPool = Executors.newFixedThreadPool(THREAD_COUNTS);
    }

    @Override
    public void registerObserver(BaseServiceCallBack observer, int index) {
        PrintLog.i("registerObserver observer=[ " + observer + " ]; index=[ " + index + " ]");
        if (observer == null && index < 0) {
            throw new IllegalArgumentException("observer=[ null ]; index=[ " + index + " ]");
        }
        switch (index) {
            case BaseServiceCallBack.INDEX_IMESSAGEVIEW:
                msgBinder.registerObserver(observer, index);
                break;
        }
    }

    @Override
    public void unRegisterObserver(BaseServiceCallBack observer, int index) {
        PrintLog.i("unRegisterObserver observer=[ " + observer + " ]; index=[ " + index + " ]");
        if (observer == null && index < 0) {
            throw new IllegalArgumentException("observer=[ null ]; index=[ " + index + " ]");
        }
        switch (index) {
            case BaseServiceCallBack.INDEX_IMESSAGEVIEW:
                msgBinder.unRegisterObserver(observer, index);
                break;
        }
    }

    @Override
    public void serviceDestory() {
        msgBinder.serviceDestory();
    }

    @Override
    public int sendMsg(final ConversationMsg conversationMsg) {
        if (conversationMsg == null) {
            PrintLog.e("sendMsg conversationMsg={ " + null + " }");
            throw new NullPointerException();
        }
        Integer msgType = conversationMsg.getMsgType();
        if (msgType == null) {
            PrintLog.e("sendMsg msgType={" + null + "}");
            throw new NullPointerException();
        }
        if (msgType == MessageDBConstant.INFO_TYPE_TEXT || msgType == MessageDBConstant.INFO_TYPE_OLD_DEVICE_TEXT) {
            sendTextMsg(conversationMsg);
        } else {
            try {
                conversationMsg.setMsgStatus(MSG_STATUS_WAIT_SENDING);
                //设置状态等待发送
                final ConversationMsg conversationMsg1 = msgBinder.insertDb(conversationMsg);
                msgBinder.updateConversationBySmsId(conversationMsg1.getMsgUctId(), MSG_STATUS_WAIT_SENDING);
                //通知界面状态
                msgBinder.notifyDataChangedListener(conversationMsg, MsgBinder.MsgCallBackListener.MSG_STATUS_CHANGE);
                //更新数据库状态信息
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        msgBinder.sendMsg(conversationMsg1);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public int reSendMsg(final ConversationMsg conversationMsg) {
        try {
            // 重发时将进度清零
            conversationMsg.setOffSize(0L);
            //等待发送...
            conversationMsg.setMsgStatus(MSG_STATUS_WAIT_SENDING);
            //更新数据库状态信息
            msgBinder.updateConversationBySmsId(conversationMsg.getMsgUctId(), MSG_STATUS_WAIT_SENDING);
            //通知界面状态
            msgBinder.notifyDataChangedListener(conversationMsg, MsgBinder.MsgCallBackListener.MSG_STATUS_CHANGE);
        } catch (Exception e) {
            PrintLog.e("reSendMsg", e);
            throw new RuntimeException("" + e.getMessage());
        }
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                msgBinder.reSendMsg(conversationMsg);
            }
        });
        return 0;
    }

    @Override
    public int sendTextMsg(final ConversationMsg conversationMsg) {
        msgBinder.sendTextMsg(conversationMsg);
        return 0;
    }

    @Override
    public int downloadMsg(final ConversationMsg conversationMsg) {
        try {
            // 重接时将进度清零
            conversationMsg.setOffSize(0L);
            //等待接收
            conversationMsg.setMsgStatus(FILE_STATUS_WAIT_RECEIVING);
            //更新数据库状态信息
            msgBinder.updateConversationBySmsId(conversationMsg.getMsgUctId(), FILE_STATUS_WAIT_RECEIVING);
            //通知界面状态
            msgBinder.notifyDataChangedListener(conversationMsg, MsgBinder.MsgCallBackListener.MSG_STATUS_CHANGE);
        } catch (Exception e) {
            PrintLog.e("downloadMsg", e);
            throw new RuntimeException("" + e.getMessage());
        }
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                msgBinder.downloadMsg(conversationMsg);
            }
        });
        return 0;
    }

    @Override
    public int cancelMsg(final ConversationMsg conversationMsg) {
        msgBinder.cancelMsg(conversationMsg);
        return 0;
    }

    @Override
    public int deleteMsg(final ConversationMsg conversationMsg) {
        msgBinder.deleteMsg(conversationMsg);
        return 0;
    }
}
