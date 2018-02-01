package com.ptyt.uct.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.android.uct.utils.PrintLog;
import com.bumptech.glide.Glide;
import com.ptyt.uct.R;
import com.ptyt.uct.callback.CallCallBack;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.entity.MessageBrowsePhotoEntity;
import com.ptyt.uct.entity.MessagePhotoEntity;
import com.ptyt.uct.services.MessageManager;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.widget.PhotoBrowseLayout;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.senab.photoview.PhotoView;

/**
 * Title: com.ptyt.uct.adapter
 * Description:
 * Date: 2017/7/27
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageBrowsePhotoAdapter extends PagerAdapter {


    private Context mContext;
    private List<MessagePhotoEntity> datas;
    private List<ConversationMsg> msgs;
    private Map<Integer, View> photoViews;
    //    private FrameLayout mFrameLayout;
    //    private ImageView mVideoPlayIv, mAnimation;
    //    private PhotoView mPhotoView;
    //    private VideoView mVideoView;
    private MediaController mMediaController;
    //    private TextView mProgress;
    //    private SurfaceView mSurfaceView;
    //    private SurfaceHolder mSurfaceHolder;
    //    private MediaPlayer mediaPlayer;
    private AnimationDrawable sendingAnimation;
    // 是否是视频
    private boolean isVideo = false;
    // 是否暂停
    private boolean isPaused = true;
    // 播放位置
    private int curIndex = 0;
    // 当前显示的图片或视频的位置
    private int currentPosition;
    // 缓存当前显示的图片或视频的控件、路径、是否是视频等信息
    public Map<Integer, MessageBrowsePhotoEntity> entities = new HashMap<>();

    public MessageBrowsePhotoAdapter(Context mContext, List<MessagePhotoEntity> datas, List<ConversationMsg> msgs) {
        this.mContext = mContext;
        this.datas = datas;
        this.msgs = msgs;
        photoViews = new HashMap<>();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(photoViews.get(position));
        photoViews.remove(position);
        entities.remove(position);
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PrintLog.i("instantiateItem begin position = " + position);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_message_browse_photo, null);
        photoViews.put(position, view);
        PhotoBrowseLayout mFrameLayout = (PhotoBrowseLayout) view.findViewById(R.id.fl_layout);
        final PhotoView mPhotoView = (PhotoView) view.findViewById(R.id.iv_mylocation);
        final ImageView mVideoPlayIv = (ImageView) view.findViewById(R.id.iv_video_play);
        final VideoView mVideoView = (VideoView) view.findViewById(R.id.vv_video);
        ImageView mAnimation = (ImageView) view.findViewById(R.id.iv_animation);
        TextView mProgress = (TextView) view.findViewById(R.id.tv_progress);
        container.addView(view);

        final String photoPath = datas.get(position).getPath();
        PrintLog.i("photoPath = " + photoPath);
        int photoType = datas.get(position).getType();
        if (photoType == MessageDBConstant.INFO_TYPE_VIDEO || photoType == MessageDBConstant.INFO_TYPE_CAMERA_VIDEO) {
            isVideo = true;
            if (msgs != null && msgs.size() > 0) {
                if (msgs.get(position).getMsgStatus() == MessageDBConstant.FILE_STATUS_RECEIVING || msgs.get(position).getMsgStatus() == MessageDBConstant.FILE_STATUS_WAIT_RECEIVING) {
                    mVideoPlayIv.setVisibility(View.GONE);
                } else {
                    mVideoPlayIv.setVisibility(View.VISIBLE);
                }
            } else {
                mVideoPlayIv.setVisibility(View.VISIBLE);
            }
        } else {
            isVideo = false;
            mVideoPlayIv.setVisibility(View.GONE);
        }
        PrintLog.i("isVideo = " + isVideo);
        if (!StrUtils.isEmpty(photoPath)) {
            if (msgs == null || msgs.size() == 0) {// 如果等于空，是从本地照片文件列表点击进入，所以可以直接播放
                PrintLog.i("从相册进入浏览");
                Glide.with(mContext).load(photoPath).asBitmap().fitCenter().into(mPhotoView);
//                Glide.with(mContext).load(photoPath).fitCenter().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mPhotoView);
            } else {// 如果不等于空，是从消息列表中点击进入
                // 发送出去的文件和接进来并已下载完成的文件是可以直接播放的，不需要再下载
                PrintLog.i("从微信进入浏览");
                if (msgs.get(position).getMsgDirection() == MessageDBConstant.IMVT_TO_MSG || (msgs.get(position).getMsgDirection() == MessageDBConstant.IMVT_COM_MSG && (msgs.get(position).getMsgStatus() == MessageDBConstant.MSG_STATUS_SEND_SUCCESS || msgs.get(position).getMsgStatus() == MessageDBConstant.FILE_STATUS_RECEIVE_SUCCESS))) {
                    if (isVideo && (position == currentPosition)) {
                        isPaused = false;
                        //                        ((AudioManager) UctApplication.getInstance().getSystemService(Context.AUDIO_SERVICE)).setMode(AudioManager.MODE_NORMAL);
                        CallCallBack.getInstance().setVoiceCallMode(AudioManager.STREAM_MUSIC);
                        mVideoPlayIv.setVisibility(View.GONE);
                        mPhotoView.setVisibility(View.GONE);
                        mVideoView.setVisibility(View.VISIBLE);
                        mVideoView.setVideoPath(photoPath);
                        mVideoView.seekTo(0);
                        mVideoView.start();
                        PrintLog.i("点击进入后播放");
                    } else {
                        Glide.with(mContext).load(photoPath).asBitmap().fitCenter().into(mPhotoView);
//                        Glide.with(mContext).load(photoPath).fitCenter().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mPhotoView);
                    }
                } else {
                    if (isVideo && (currentPosition == position)) {
                        if (!SDCardUtils.isAvailableInternalMemory()) {
                            ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.msg_msg_send_error_2), -1);
                        } else {
                            ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_photo_browser_downloading), -1);
                            MessageManager.getInstane().downloadMessage((msgs.get(position)));
                            mVideoPlayIv.setVisibility(View.GONE);
                            mProgress.setVisibility(View.VISIBLE);
                            mAnimation.setVisibility(View.VISIBLE);
                            mAnimation.setBackgroundResource(R.drawable.animation_message_sending);
                            sendingAnimation = (AnimationDrawable) mAnimation.getBackground();
                            if (sendingAnimation != null && !sendingAnimation.isRunning()) {
                                sendingAnimation.start();
                            }
                        }
                    }
                }
            }
        } else {
            if (position == currentPosition) {
                ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_photo_browser_file_path_inexistence1), -1);
            }
        }

        mFrameLayout.setVideo(isVideo);
        if (isVideo) {
            mFrameLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (StrUtils.isEmpty(photoPath)) {
                        ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_photo_browser_file_path_inexistence2), -1);
                        return;
                    }
                    if (!new File(photoPath).exists()) {
                        PrintLog.i("没有这个视频，或是视频没下载完成都不让点击");
                        return;
                    }
                    if (isPaused) {
                        isPaused = false;
                        //                        ((AudioManager) UctApplication.getInstance().getSystemService(Context.AUDIO_SERVICE)).setMode(AudioManager.MODE_NORMAL);
                        CallCallBack.getInstance().setVoiceCallMode(AudioManager.STREAM_MUSIC);
                        mVideoPlayIv.setVisibility(View.GONE);
                        mPhotoView.setVisibility(View.GONE);
                        mVideoView.setVisibility(View.VISIBLE);
                        mVideoView.setVideoPath(photoPath);
                        mVideoView.seekTo(curIndex);
                        mVideoView.start();
                        PrintLog.i("点击播放");
                    } else {
                        isPaused = true;
                        mVideoView.pause();
                        mVideoPlayIv.setVisibility(View.VISIBLE);
                        curIndex = mVideoView.getCurrentPosition();
                    }
                }
            });
        }

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPaused = true;
                curIndex = 0;
                mVideoPlayIv.setVisibility(View.VISIBLE);
                mPhotoView.setVisibility(View.VISIBLE);
            }
        });

        MessageBrowsePhotoEntity entity = new MessageBrowsePhotoEntity();
        entity.setVideoView(mVideoView);
        entity.setPhotoView(mPhotoView);
        entity.setVideoPlayIv(mVideoPlayIv);
        entity.setAnimationView(mAnimation);
        entity.setProgressView(mProgress);
        entity.setPath(photoPath);
        entity.setVideo(isVideo);
        entities.put(position, entity);

        PrintLog.i("instantiateItem end");
        return view;
    }


    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void setPause(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public void setCurrentIndex(int curIndex) {
        this.curIndex = curIndex;
    }

    public int getCurrentIndex() {
        return curIndex;
    }

    //    /**
    //     * @param
    //     * @return
    //     * @description 显示进度条
    //     */
    //    private void showProgressUI() {
    //        mProgress.setVisibility(View.VISIBLE);
    //        mAnimation.setVisibility(View.VISIBLE);
    //        mAnimation.setBackgroundResource(R.drawable.animation_message_sending);
    //        startAnimation();
    //    }
    //
    //    /**
    //     * @param
    //     * @return
    //     * @description 隐藏进度条
    //     */
    //    private void dismissProgressUI() {
    //        mProgress.setVisibility(View.INVISIBLE);
    //        mAnimation.setVisibility(View.INVISIBLE);
    //        stopAnimation();
    //    }
    //
    //    /**
    //     * @param
    //     * @return
    //     * @description 开启动画
    //     */
    //    private void startAnimation() {
    //        sendingAnimation = (AnimationDrawable) mAnimation.getBackground();
    //        if (sendingAnimation != null) {
    //            sendingAnimation.start();
    //        }
    //    }
    //
    //    /**
    //     * @param
    //     * @return
    //     * @description 关闭动画
    //     */
    //    public void stopAnimation() {
    //        if (sendingAnimation != null && sendingAnimation.isRunning()) {
    //            sendingAnimation.selectDrawable(0);
    //            sendingAnimation.stop();
    //        }
    //    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }
}
