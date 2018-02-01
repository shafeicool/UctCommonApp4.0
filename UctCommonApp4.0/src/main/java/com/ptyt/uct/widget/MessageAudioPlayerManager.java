package com.ptyt.uct.widget;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.adapter.MessageAdapter;
import com.ptyt.uct.callback.CallCallBack;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.model.MessageDBManager;
import com.ptyt.uct.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Title: com.ptyt.uct.widget
 * Description:
 * Date: 2017/8/16
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageAudioPlayerManager {

    private static MessageAudioPlayerManager instance;
    private Context mContext;
    private MessageAdapter mAdapter;
    private MessageAudioPlayer soundPlayer = null;
    // 所有未读语音列表
    private List<ConversationMsg> list = new ArrayList<>();
    // 当前正在播放的语音对象
    private ConversationMsg currentPlayAudio;

    public MessageAudioPlayerManager(Context mContext, MessageAdapter mAdapter) {
        this.mContext = mContext;
        this.mAdapter = mAdapter;
        soundPlayer = MessageAudioPlayer.getMessageAudioPlayer();
    }

    public static synchronized MessageAudioPlayerManager getInstance(Context mContext, MessageAdapter mAdapter) {
        if (instance == null) {
            instance = new MessageAudioPlayerManager(mContext, mAdapter);
        }
        return instance;
    }

    /**
     * @param
     * @return
     * @description 获取未读语音的列表
     */
    public void setAudioUnreadList() {
        PrintLog.d("setAudioUnreadList--mAdapter.getItemCount() = " + mAdapter.getItemCount());
        list.clear();
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            ConversationMsg conversationMsg = mAdapter.getItem(i);
            if (conversationMsg.getMsgDirection() == MessageDBConstant.IMVT_COM_MSG
                    && conversationMsg.getMsgType() == MessageDBConstant.INFO_TYPE_AUDIO
                    && conversationMsg.getAudioReadStatus() == MessageDBConstant.AUDIO_UNREAD_MSG) {
                list.add(conversationMsg);
                PrintLog.d("setAudioUnreadList--conversationMsg.getAudioLength() = " + conversationMsg.getAudioLength());
                PrintLog.d("setAudioUnreadList--conversationMsg.getMsgUctId() = " + conversationMsg.getMsgUctId());
            }
        }
    }

    /**
     * @param conversationMsg
     * @return
     * @description 开始播放音频
     */
    public void startPlayAudio(final ConversationMsg conversationMsg) {
        PrintLog.d("startPlayAudio1");
        stopPlayAudio(currentPlayAudio);
        if (currentPlayAudio != null && currentPlayAudio.getMsgUctId().equals(conversationMsg.getMsgUctId())) {
            currentPlayAudio = null;
            return;
        }
        PrintLog.d("startPlayAudio2");
        currentPlayAudio = conversationMsg;
        // 更新音频读状态为已读
        if ((conversationMsg.getMsgDirection() == MessageDBConstant.IMVT_COM_MSG)
                && (conversationMsg.getAudioReadStatus() == MessageDBConstant.AUDIO_UNREAD_MSG)) {
            conversationMsg.setAudioReadStatus(MessageDBConstant.AUDIO_ALREAD_MSG);
            MessageDBManager.getInstance(mContext).updateMessage(conversationMsg);
        }
        notifyDataChanged(conversationMsg);
        try {
//            ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE)).setMode(AudioManager.MODE_NORMAL);
            CallCallBack.getInstance().setVoiceCallMode(AudioManager.STREAM_MUSIC);
            soundPlayer.reset();
            soundPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            soundPlayer.setDataSource(conversationMsg.getLocalImgPath());
            soundPlayer.prepare();
            soundPlayer.setLooping(false);
            soundPlayer.start();
        } catch (Exception ex) {
            ToastUtils.getToast().showMessageShort(mContext, "播放的音源不存在或参数异常", -1);
            ex.printStackTrace();
            stopPlayAudio(conversationMsg);
            return;
        }
        conversationMsg.setAudioPlayStatus(MessageDBConstant.AUDIO_START_STATUS);
        PrintLog.d("开始播放语音短信");
        soundPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                PrintLog.d("播放录音完成");
                conversationMsg.setAudioPlayStatus(MessageDBConstant.AUDIO_STOP_STATUS);
                currentPlayAudio = null;
                notifyDataChanged(conversationMsg);
                playNextIfExists(conversationMsg);
            }
        });
    }

    /**
     * @param
     * @return
     * @description 停止播放音频
     */
    private void stopPlayAudio(ConversationMsg conversationMsg) {
        try {
            if (conversationMsg != null && soundPlayer != null && soundPlayer.isPlaying()) {
                conversationMsg.setAudioPlayStatus(MessageDBConstant.AUDIO_STOP_STATUS);
                notifyDataChanged(conversationMsg);
                soundPlayer.stop();
                PrintLog.d("stopPlayAudio");
                PrintLog.d("停止播放录音");
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param conversationMsg
     * @return
     * @description 如果仍有下一条未读，则继续播放
     */
    private void playNextIfExists(ConversationMsg conversationMsg) {
        if (list.contains(conversationMsg)) {
            int currentPlayPos = list.indexOf(conversationMsg);
            list.remove(conversationMsg);
            PrintLog.d("playNextIfExists--currentPlayPos = " + currentPlayPos);
            PrintLog.d("playNextIfExists--list.size() = " + list.size());
            if (currentPlayPos < list.size()) {
                startPlayAudio(list.get(currentPlayPos));
            }
        }

    }

    /**
     * @param conversationMsg
     * @return
     * @description 通知UI更新
     */
    private void notifyDataChanged(ConversationMsg conversationMsg) {
        int position = mAdapter.getDatas().indexOf(conversationMsg);
        mAdapter.notifyItemChanged(position);
    }

    /**
     * @param
     * @return
     * @description 释放音频管理对象
     */
    public void release() {
        stopPlayAudio(currentPlayAudio);
        soundPlayer.releaseAudioPlayer();
        instance = null;
    }
}
