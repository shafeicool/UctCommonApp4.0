package com.ptyt.uct.activity;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.callback.CallCallBack;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.AppManager;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.model.CallRecordDBManager;
import com.ptyt.uct.model.ContactDBManager;
import com.ptyt.uct.model.ConversationDBManager;
import com.ptyt.uct.model.GroupDBManager;
import com.ptyt.uct.model.GroupUserDBManager;
import com.ptyt.uct.model.LoginUserDBManager;
import com.ptyt.uct.model.MessageDBManager;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.widget.GroupCallWindow;

/**
 * @Description: 基础Activity类，所有的Activity需要继承该类，包含一些抽象公有方法，和Activity共用的方法
 * @Date: 2017/4/24
 * @Author: ShaFei
 * @Version: V1.0
 */
public class BaseActivity extends AppCompatActivity implements Handler.Callback {

    private HandlerThread uIhandlerThread;
    public Handler myHandler;
    private boolean isNewMyLooper = false;
    protected Context mContext;
    private boolean isCheck = true;
    protected Bundle savedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrintLog.i("onCreate()");
        this.savedInstanceState = savedInstanceState;
        AppManager.getAppManager().addActivity(this);
        if (isCheck) {
            boolean isHaveInitLib = AppContext.getAppContext().isHaveInitLib(this);
            PrintLog.i("isHaveInitLib = " + isHaveInitLib);
            if (!isHaveInitLib) {
                if (!isFinishing()) {
                    PrintLog.i("this = " + getClass().getSimpleName() + "   finish");
                    finish();
                }
                return;
            }
        }
        PrintLog.i("this = " + getClass().getSimpleName());
        setContentView(R.layout.activity_base);
        mContext = this;
        //全透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4 全透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0 全透明实现
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            /* ptyt begin, 解决虚拟按键不能把界面顶上去的问题_4025_shafei_20170906 */
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    //                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            /* ptyt end */
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        initView();
        initData();
        initEvent();
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    /**
     * 初始化控件
     */
    protected void initView() {
    }

    protected void initData() {
    }

    protected void initEvent() {
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

    public void sendMsg(int what, Messenger sendMessenger, Messenger messenger) {
        Message msg = myHandler.obtainMessage(what);
        msg.replyTo = sendMessenger;
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送Message消息
     */
    public void sendMsg(boolean bMoveOld, int what) {
        if (!isNewMyLooper) {
            return;
        }
        bMoveOld = false;
        if (bMoveOld)
            myHandler.removeMessages(what);
        Message msg = myHandler.obtainMessage(what);
        myHandler.sendMessage(msg);
    }

    public void sendMsgDelayed(boolean bMoveOld, int what, long delayMillis) {
        if (!isNewMyLooper) {
            return;
        }
        bMoveOld = false;
        if (bMoveOld)
            myHandler.removeMessages(what);
        Message msg = myHandler.obtainMessage(what);
        myHandler.sendMessageDelayed(msg, delayMillis);
    }

    public void sendMsg(boolean bMoveOld, int what, int arg1, String... strArgs) {
        if (!isNewMyLooper) {
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
        if (myHandler == null) {
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

    public void sendMsgDelayed(boolean bMoveOld, int what, long delayMillis, int arg1, int arg2, String... strArgs) {
        if (myHandler == null) {
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
        if (myHandler == null) {
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
        if (isNewMyLooper) {
            uIhandlerThread = new HandlerThread(getClass().getSimpleName());
            uIhandlerThread.setPriority(Thread.MAX_PRIORITY);
            uIhandlerThread.start();
            myHandler = new Handler(uIhandlerThread.getLooper(), this);
        } else {
            myHandler = new Handler(getMainLooper(), this);
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
    protected void onDestroy() {
        PrintLog.i("onDestroy()");
        CallRecordDBManager.getInstance(mContext).release();
        ContactDBManager.getInstance(mContext).release();
        ConversationDBManager.getInstance(mContext).release();
        GroupDBManager.getInstance().release();
        GroupUserDBManager.getInstance(mContext).release();
        LoginUserDBManager.getInstance(mContext).release();
        MessageDBManager.getInstance(mContext).release();
        ToastUtils.getToast().release();
        closeHandlerThread();
        super.onDestroy();
        AppManager.getAppManager().finishActivity(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        /**
         * 当在组呼时，需展示悬浮窗(除了在mainActivity中显示CallFragment中时,MainActivity的onResume会重写,里面会细分判断
         */
        String actClsName = AppManager.getAppManager().currentActivity().getComponentName().getClassName();
        if (TextUtils.isEmpty(actClsName) || actClsName.equals(LoginActivity.class.getName()) || actClsName.equals(SplashActivity.class.getName())) {
            return;
        }
        GroupCallWindow groupCallWindow = UctApplication.getInstance().getGroupCallWindow();
        if(groupCallWindow != null){
            if(UctApplication.getInstance().isInGroupCall){
                groupCallWindow.show(null);
            }else{
                groupCallWindow.hidePopupWindow();
            }
        }
    }

    /**
     * 按键监听，
     * 调节音量按键，根据CallCallBack.voiceCallMode声音模式:
     * 0- AudioManager.STREAM_VOICE_CALL
     * 1- AudioManager.STREAM_SYSTEM
     * 2- AudioManager.STREAM_RING
     * 3- AudioManager.STREAM_MUSIC
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        PrintLog.i("keyCode=" + keyCode + " VoiceCallMode=" + CallCallBack.voiceCallMode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP://增大声音
                //根据当前业务状态调节:来电铃声调节媒体音量，通话中则调节通话音量
                CallCallBack.getInstance().audioManager.adjustStreamVolume(
                        CallCallBack.voiceCallMode,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN://减小声音
                CallCallBack.getInstance().audioManager.adjustStreamVolume(
                        CallCallBack.voiceCallMode,
                        AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
