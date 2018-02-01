package com.ptyt.uct.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.Surface;

/**
 * Created by you on 2016/10/24.
 * 由于拍摄跟播放都关联TextureView,停止播放时要释放mediaplayer
 */

public class MediaPlayerManager {

    private Context context;

    private MediaPlayer mPlayer;

    private MediaPlayerManager(Context context) {
        this.context = context;
    }

    private static MediaPlayerManager INSTANCE;

    public static MediaPlayerManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CameraManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MediaPlayerManager(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 播放Media
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void playMedia(Surface surface, String mediaPath) {
        try {
            if (mPlayer == null) {
                mPlayer = new MediaPlayer();
                mPlayer.setDataSource(mediaPath);
            } else {
                if (mPlayer.isPlaying()) {
                    mPlayer.stop();
                }
                mPlayer.reset();
                mPlayer.setDataSource(mediaPath);
            }
            mPlayer.setSurface(surface);
            mPlayer.setLooping(true);
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止播放Media
     */
    public void stopMedia() {
        try {
            if (mPlayer != null) {
                if (mPlayer.isPlaying()) {
                    mPlayer.stop();
                }
                mPlayer.release();
                mPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void releaseMediaPlayer() {
        try {
            if (mPlayer != null) {
                if (mPlayer.isPlaying()) {
                    mPlayer.stop();
                }
                mPlayer.release();
                mPlayer = null;
            }
            if (INSTANCE != null) {
                INSTANCE = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
