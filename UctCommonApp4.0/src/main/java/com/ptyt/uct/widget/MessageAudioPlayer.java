package com.ptyt.uct.widget;

import android.media.MediaPlayer;

import com.ptyt.uct.entity.ConversationMsg;

/**
 * Title: com.ptyt.uct.widget
 * Description:
 * Date: 2017/5/24
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageAudioPlayer extends MediaPlayer {

    private static MessageAudioPlayer instance = null;
    private ConversationMsg conversationMsg;

    public MessageAudioPlayer() {
        super();
    }

    public static synchronized MessageAudioPlayer getMessageAudioPlayer() {
        if (instance == null) {
            instance = new MessageAudioPlayer();
        }
        return instance;
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
    }

    public void releaseAudioPlayer() {
        if (instance.isPlaying()) {
            instance.stop();
        }
        instance.release();
        instance = null;
    }

}
