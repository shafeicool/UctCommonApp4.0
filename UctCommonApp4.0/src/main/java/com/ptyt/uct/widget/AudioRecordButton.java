package com.ptyt.uct.widget;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;

import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.viewinterface.IMessageView;

/**
 * Title: com.ptyt.uct.widget
 * Description:
 * Date: 2017/5/22
 * Author: ShaFei
 * Version: V1.0
 */

public class AudioRecordButton extends Button implements OnLongClickListener {
    private Context mContext;
    private AudioRecordDialogManager mDialogManager;
    //    private LoginUserEntity loginUserBean;
    private static final int MSG_AUDIO_PREPARED = 0X110;
    private static final int MSG_VOICE_CHANGE = 0X111;
    private static final int MSG_DIALOG_DIMISS = 0X112;

    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_WANT_TO_CANCEL = 3;

    private static final int DISTANCE_Y_CANCEL = 50;

    private int mCurrentState = STATE_NORMAL;
    // 已经开始录音
    private boolean isRecording = false;
    // 是否触发了onlongclick，准备好了
    private boolean mReady;
    private float mTime = 0;
    private String recordPath = "";
    private String userName;
    private String recordFileName = "";
    private IMessageView audioFinishRecorder;
    private String smsgId = "";
    /**
     * 短信录音最长时间为1分钟
     */
    private static final float MAX_RECORD = 60f;
    private long conversationId;
    private String mSendTel;

    public AudioRecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        mDialogManager = new AudioRecordDialogManager(context);
        setOnLongClickListener(this);
        setText(R.string.string_message_audio_button_normal);
        //        loginUserBean = new LoginUserEntity();
        //        userName = ReadPreference.readUserInfo().getUserName();
        if (!isInEditMode()) {
            userName = AppContext.getAppContext().getLoginNumber();
        }
    }

    private void changeState(int state) {
        if (mCurrentState != state) {
            mCurrentState = state;
            switch (mCurrentState) {
                case STATE_NORMAL:
                    setText(R.string.string_message_audio_button_normal);
                    mDialogManager.dimissDialog();
                    PrintLog.e("mDialogManager .dimissDialog()");
                    break;
                case STATE_RECORDING:
                    setText(R.string.string_message_audio_button_recording);
                    if (isRecording) {
                        mDialogManager.recording();
                    }
                    break;

                case STATE_WANT_TO_CANCEL:
                    setText(R.string.string_message_audio_button_want_to_cancel1);
                    mDialogManager.wantToCancel();
                    break;

            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                changeState(STATE_RECORDING);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isRecording) {
                    // 根据x，y来判断用户是否想要取消
                    if (wantToCancel(x, y)) {
                        changeState(STATE_WANT_TO_CANCEL);
                    } else {
                        changeState(STATE_RECORDING);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                // 首先判断是否有触发onlongclick事件，没有的话直接返回reset
                if (!mReady) {
                    reset();
                    return super.onTouchEvent(event);
                }
                stopRecord();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!mReady) {
                    reset();
                    return super.onTouchEvent(event);
                }
                stopRecord();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void stopRecord() {
        int result = UctClientApi.UctAudioVideoRecordStop();
        if (result != 0) {
            PrintLog.e("result = " + result);
            mDialogManager.dimissDialog();
            PrintLog.e("mDialogManager .dimissDialog()");
            SDCardUtils.deleteFile(recordFileName);
            reset();
            return;
        }
        // 如果按的时间太短，还没准备好或者时间录制太短，就离开了，则显示这个dialog
        if (!isRecording || mTime < 1f) {
            PrintLog.i("太短");
            PrintLog.i("isRecording = " + isRecording + "  mTime = " + mTime);
            mDialogManager.tooShort();
            mhandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 0);// 持续1.3s
            SDCardUtils.deleteFile(recordFileName);
        } else if (mCurrentState == STATE_RECORDING) {//正常录制结束
            PrintLog.i("正常录制结束");
            PrintLog.i("isRecording = " + isRecording + "  mTime = " + mTime);
            mDialogManager.dimissDialog();
            PrintLog.e("mDialogManager .dimissDialog()");
            if (audioFinishRecorder != null) {
                int seconds;
                if (mTime >= MAX_RECORD) {
                    seconds = (int) MAX_RECORD;
                } else {
                    seconds = (int) mTime;

                }
                FileUtils.scanFile(mContext, recordFileName);
                audioFinishRecorder.onRecordFinished(seconds, recordFileName, smsgId);
            }
        } else if (mCurrentState == STATE_WANT_TO_CANCEL) {
            PrintLog.i("取消");
            mDialogManager.dimissDialog();
            PrintLog.e("mDialogManager .dimissDialog()");
            SDCardUtils.deleteFile(recordFileName);
        }
        reset();// 恢复标志位
    }

    /**
     * 回复标志位以及状态
     */
    private void reset() {
        PrintLog.i("reset");
        recordFileName = "";
        isRecording = false;
        post(new Runnable() {

            @Override
            public void run() {
                changeState(STATE_NORMAL);
            }
        });
        mReady = false;
        mTime = 0;
        smsgId = "";
    }

    private boolean wantToCancel(int x, int y) {
        if (x < 0 || x > getWidth()) {// 判断是否在左边，右边，上边，下边
            return true;
        }
        if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true;
        }

        return false;
    }

    private String value2 = null;
    private String value3 = null;

    private boolean setMsgAudioPrepared() {
        // 显示应该是在audio end prepare之后回调
        boolean isShow = mDialogManager.showRecordingDialog();
        if(!isShow){
            return false;
        }
        if (value2 == null) {
            //                value2= UctApi.getUserData(PreferenceConstant.NOISE_REDUCTION, "1");
            //                value3 = UctApi.getUserData(PreferenceConstant.MIKE_MICHAEL_ZY, "0");
            value2 = "1";
            value3 = "0";
        }
        if (StrUtils.isMatchs(value2) && StrUtils.isMatchs(value3)) {
            UctClientApi.setAudioProcess(0, Integer.parseInt(value2), Integer.parseInt(value3), 1);
        }
        isRecording = true;
        new Thread(mGetVoiceLevelRunnable).start();
        return true;
    }

    private Handler mhandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (value2 == null) {
                value2 = "1";
                value3 = "0";
            }
            switch (msg.what) {
                case MSG_VOICE_CHANGE:
                    int l = UctClientApi.GetAudioLevel();
                    int level = (l - 55) / 10;
                    PrintLog.d(l + "recording......" + level);
                    mDialogManager.updateVoiceLevel(level);
                    break;
                case MSG_DIALOG_DIMISS:
                    //降噪
                    if (StrUtils.isMatchs(value2) && StrUtils.isMatchs(value3)) {
                        UctClientApi.setAudioProcess(0, Integer.parseInt(value2), Integer.parseInt(value3), 0);
                    }
                    mDialogManager.dimissDialog();
                    PrintLog.e("mDialogManager .dimissDialog()");
                    break;
            }
        }

        ;
    };

    // 获取音量大小的runnable
    private Runnable mGetVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
            while (isRecording) {
                try {
                    Thread.sleep(100);
                    mTime += 0.1f;
                    mhandler.sendEmptyMessage(MSG_VOICE_CHANGE);
                    if (mTime >= MAX_RECORD) {
                        stopRecord();
                    }
                } catch (InterruptedException e) {
                    mDialogManager.dimissDialog();
                    PrintLog.e("mDialogManager .dimissDialog()");
                    SDCardUtils.deleteFile(recordFileName);
                    reset();
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_message_audio_button_exception), -1);
                    e.printStackTrace();
                }
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onLongClick(View v) {
        if (SDCardUtils.isSDCardEnable()) {
            if (!SDCardUtils.isAvailableInternalMemory()) {
                mReady = false;
                ToastUtils.getToast().showMessageShort(getContext(), getContext().getString(R.string.msg_msg_send_error_2), -1);
                return false;
            }
            recordPath = SDCardUtils.getChatRecordPath(conversationId, userName + "_" + mSendTel);
            boolean isSuccess = SDCardUtils.mkdir2(recordPath);
            PrintLog.d("isSuccess==" + isSuccess);
        } else if (!UctClientApi.isUserOnline()) {
            mReady = false;
            ToastUtils.getToast().showMessageShort(getContext(), getContext().getString(R.string.msg_msg_send_error_1), -1);
            return false;
        } else {
            mReady = false;
            ToastUtils.getToast().showMessageShort(getContext(), getContext().getString(R.string.sdcard_no_exist), -1);
            return false;
        }
        /* ptyt begin, 解决当录音时，播放的语音不停止的问题_0000_shafei_20170929 */
        MessageAudioPlayerManager.getInstance(mContext, null).release();
        /* ptyt end */
        smsgId = StrUtils.getSmsId(mSendTel, userName);
        recordFileName = recordPath + smsgId + ".mp3";

        int result = UctClientApi.UctAudioVideoRecordStart(1, 0, recordFileName);
        if (result == 0) {
           boolean isSuccess =  setMsgAudioPrepared();
            if(!isSuccess){
                mDialogManager.dimissDialog();
                PrintLog.e("mDialogManager .dimissDialog()");
                SDCardUtils.deleteFile(recordFileName);
                mReady = false;
            }else{
                mReady = true;
            }
        } else {
            mDialogManager.dimissDialog();
            PrintLog.e("mDialogManager .dimissDialog()");
            SDCardUtils.deleteFile(recordFileName);
            mReady = false;
            ToastUtils.getToast().showMessageShort(getContext(), getContext().getString(R.string.record_recording_equipment_is_not_available), -1);
        }
        PrintLog.d("result==" + result);
        return false;
    }

    public void setAudioFinishRecorder(IMessageView audioFinishRecorder) {
        this.audioFinishRecorder = audioFinishRecorder;
    }

    public void setConversationId(long conversationId) {
        this.conversationId = conversationId;
    }

    public void setRecordSendTel(String sendTel) {
        this.mSendTel = sendTel;
    }
}
