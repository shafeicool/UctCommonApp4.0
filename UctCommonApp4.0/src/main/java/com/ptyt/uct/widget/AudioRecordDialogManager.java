package com.ptyt.uct.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;

public class AudioRecordDialogManager {

    /**
     * 以下为dialog的初始化控件，包括其中的布局文件
     */
//    private static AudioRecordDialogManager instance = null;
    private Dialog mDialog;
    private ImageView mIcon;
    private ImageView mVoice;
    private TextView mLable;
    private Context mContext;

    public AudioRecordDialogManager(Context context) {
        mContext = context;
    }

//    public static synchronized AudioRecordDialogManager getInstance(Context context) {
//        if (instance == null) {
//            instance = new AudioRecordDialogManager(context);
//        }
//        return instance;
//    }

    public boolean showRecordingDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            PrintLog.e("mDialog1 = " + mDialog);
            return false;
        }
        PrintLog.e("mDialog2 = " + mDialog);
        mDialog = new Dialog(mContext, R.style.Theme_audioDialog);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_message_audio, null);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(false);

        mIcon = (ImageView) mDialog.findViewById(R.id.dialog_icon);
        mVoice = (ImageView) mDialog.findViewById(R.id.dialog_voice);
        mLable = (TextView) mDialog.findViewById(R.id.recorder_dialogtext);
        mDialog.show();
        return true;

    }

    /**
     * 设置正在录音时的dialog界面
     */
    public void recording() {
        if (mDialog != null && mDialog.isShowing()) {
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.VISIBLE);
            mLable.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.mipmap.icon_vioce);
            mLable.setText(R.string.string_message_audio_button_want_to_cancel2);
            mLable.setBackgroundResource(android.R.color.transparent);
        }
    }

    /**
     * 取消界面
     */
    public void wantToCancel() {
        // TODO Auto-generated method stub
        if (mDialog != null && mDialog.isShowing()) {
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLable.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.mipmap.icon_cancel);
            mLable.setText(R.string.string_message_audio_button_want_to_cancel1);
            mLable.setBackgroundResource(R.drawable.shape_message_audio_dialog_red);
        }

    }

    // 时间过短
    public void tooShort() {
        // TODO Auto-generated method stub
        if (mDialog != null && mDialog.isShowing()) {
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLable.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.mipmap.icon_remind);
            mLable.setText(R.string.string_message_audio_button_tooshort);
            mLable.setBackgroundResource(android.R.color.transparent);
        }

    }

    // 隐藏dialog
    public void dimissDialog() {
        // TODO Auto-generated method stub
        if (mDialog == null) {
            PrintLog.i("Dialog空");
        }
        if (mDialog != null && mDialog.isShowing()) {
            PrintLog.i("Dialog消失");
            mDialog.dismiss();
            mDialog = null;
        }

    }

    public void updateVoiceLevel(int level) {
        if (mDialog != null && mDialog.isShowing()) {
            if (level <= 0) {
                level = 1;
                mVoice.setImageResource(android.R.color.transparent);
            }
            if (level > 7) {
                level = 7;
            }
            int resId = mContext.getResources().getIdentifier("level_" + level, "mipmap", mContext.getPackageName());
            mVoice.setImageResource(resId);
        }

    }

}
