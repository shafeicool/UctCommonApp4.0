package com.ptyt.uct.widget;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Handler.Callback;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.adapter.MessageAdapter;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.utils.DensityUtils;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.ToastUtils;

/**
 * Title: com.ptyt.uct.widget
 * Description:
 * Date: 2017/5/22
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageAudioLayout extends MessageBaseLayout implements
        OnClickListener,
        View.OnLongClickListener,
        Callback {

    private MessageAdapter mAdapter;
    private Context mContext;
    private RelativeLayout ll_audio;
    private TextView tv_audio_time;
    private ImageView ib_other;
    private ImageView iv_audio_read;
    private AnimationDrawable audioAnimation;

    private ConversationMsg conversationMsg;
    private int position;
    private int msgDirection;
    private int audioLength;

    private MessageAudioPlayerManager manager;

    public MessageAudioLayout(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public MessageAudioLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void show(ConversationMsg conversationMsg, int position, MessageAdapter mAdapter) {
        this.conversationMsg = conversationMsg;
        this.position = position;
        this.mAdapter = mAdapter;
        msgDirection = conversationMsg.getMsgDirection();
        initWindow(mContext);
    }

    @Override
    protected int setLayoutId() {
        if (msgDirection == MessageDBConstant.IMVT_COM_MSG) {
            return R.layout.view_message_audio_content_left;
        } else {
            return R.layout.view_message_audio_content_right;
        }
    }

    @Override
    protected void initView(View view) {
        ll_audio = (RelativeLayout) view.findViewById(R.id.rl_audio);
        ib_other = (ImageView) view.findViewById(R.id.ib_other);
        tv_audio_time = (TextView) view.findViewById(R.id.tv_audio_time);
        iv_audio_read = (ImageView) view.findViewById(R.id.iv_audio_read);
        audioAnimation = (AnimationDrawable) ib_other.getBackground();

        ll_audio.setOnClickListener(this);
        ll_audio.setOnLongClickListener(this);
    }

    @Override
    protected void initData() {
        audioLength = conversationMsg.getAudioLength();
        if (msgDirection == MessageDBConstant.IMVT_COM_MSG) {
            tv_audio_time.setText(audioLength + "\"");
            if (conversationMsg.getAudioReadStatus() == MessageDBConstant.AUDIO_UNREAD_MSG) {
                iv_audio_read.setVisibility(View.VISIBLE);
            } else {
                iv_audio_read.setVisibility(View.INVISIBLE);
            }
            if (conversationMsg.getMsgStatus() == MessageDBConstant.FILE_STATUS_RECEIVE_SUCCESS) {
                FileUtils.scanFile(mContext, conversationMsg.getLocalImgPath());
            }
        } else {
            tv_audio_time.setText(audioLength + "\"");
            iv_audio_read.setVisibility(View.INVISIBLE);
        }

        if (conversationMsg.getAudioPlayStatus() == MessageDBConstant.AUDIO_START_STATUS) {
            startAnimation();
        } else {
            stopAnimation();
        }

        showAudioLength();
    }


    /**
     * @param
     * @return
     * @description 显示语音短信长度
     */
    private void showAudioLength() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ll_audio.getLayoutParams();
        /* ptyt begin, 解决发超过30秒的语音短信，语音信息时长被边距截断的问题_4050_shafei_20170906 */
        if (audioLength <= 25) {
            params.width = DensityUtils.dp2px(mContext, audioLength * 3 + 60);
            //            params.width = audioLength * ScreenUtils.getDimensionPixelSize(mContext, R.dimen.x15) + ScreenUtils.getDimensionPixelSize(mContext, R.dimen.x60);
        } else {
            params.width = DensityUtils.dp2px(mContext, 25 * 3 + 60);
            //            params.width = 25 * ScreenUtils.getDimensionPixelSize(mContext, R.dimen.x15) + ScreenUtils.getDimensionPixelSize(mContext, R.dimen.x60);
        }
        /* ptyt end */
        ll_audio.setLayoutParams(params);
    }

    private void startAnimation() {
        if (audioAnimation != null && !audioAnimation.isRunning()) {
            audioAnimation.start();
        }
    }

    private void stopAnimation() {
        if (audioAnimation != null && audioAnimation.isRunning()) {
            audioAnimation.selectDrawable(0);
            audioAnimation.stop();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_audio:
                manager = MessageAudioPlayerManager.getInstance(mContext, mAdapter);
                if (msgDirection == MessageDBConstant.IMVT_TO_MSG) {
                    manager.startPlayAudio(conversationMsg);
                } else {
                    if (conversationMsg.getMsgStatus() == MessageDBConstant.FILE_STATUS_RECEIVE_SUCCESS) {
                        manager.setAudioUnreadList();
                        manager.startPlayAudio(conversationMsg);
                    } else {
                        ToastUtils.getToast().showMessageShort(mContext, "语音未下载成功，无法播放", -1);
                    }
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.rl_audio:
                new MessageOptionDialogManager(mContext, conversationMsg, position);
                break;
        }
        return true;
    }

}
