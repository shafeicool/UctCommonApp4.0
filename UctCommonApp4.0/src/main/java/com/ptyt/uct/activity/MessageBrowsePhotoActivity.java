package com.ptyt.uct.activity;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.android.uct.utils.PrintLog;
import com.bumptech.glide.Glide;
import com.ptyt.uct.R;
import com.ptyt.uct.adapter.MessageBrowsePhotoAdapter;
import com.ptyt.uct.callback.CallCallBack;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.services.BaseServiceCallBack;
import com.ptyt.uct.services.MessageManager;
import com.ptyt.uct.services.MsgBinder;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.widget.MessageBrowsePhotoViewPager;

import uk.co.senab.photoview.PhotoView;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MessageBrowsePhotoActivity extends BaseActionBarActivity implements
        ViewPager.OnPageChangeListener,
        MsgBinder.MsgCallBackListener {

    private Context mContext;
    private MessageBrowsePhotoViewPager vp_browse;

    private VideoView videoView;
    private PhotoView photoView;
    private ImageView videoPlay;
    private ImageView animationView;
    private TextView progressView;
    private String photoPath;
    private boolean isVideo;
    private AnimationDrawable sendingAnimation;
    private boolean hasInit = false;

    private MessageBrowsePhotoAdapter mAdapter;
    // 消息对象list，只能从微信列表中查看照片时能传进来该list，如果从相册进入查看照片，该list为null，不需要用到
    //    private List<ConversationMsg> msgs = new ArrayList<>();
    // 接收来的图片和视频列表
    //    private List<MessagePhotoEntity> datas = new ArrayList<>();
    //    // 是否有照片或视频被勾选，用于激活发送按钮
    //    private boolean hasSelected = false;
    //    // 是否显示发送，从微信列表中点击进入时，不显示发送按钮
    //    private boolean isShowSend = false;
    // 当前图片或视频的位置
    private int currentPos = -1;
    // 上一个图片或视频的位置
    private int lastPos = -1;
    private int progress = 0;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_browse_photo;
    }

    @Override
    protected void initWidget() {
        PrintLog.i("registerObserver BaseServiceCallBack.INDEX_IMESSAGEVIEW");
        MessageManager.getInstane().registerObserver(this, BaseServiceCallBack.INDEX_IMESSAGEVIEW);
        mContext = this;
        vp_browse = (MessageBrowsePhotoViewPager) findViewById(R.id.vp_browse);
        tv_actionBarTitle.setText(getResources().getString(R.string.string_photo_browser_title));
        //        tv_actionBarRight.setVisibility(View.VISIBLE);
        //        tv_actionBarRight.setText(getResources().getString(R.string.string_photo_browser_send));
        //        tv_actionBarRight.setTextColor(ContextCompat.getColor(this, R.color.colorTextView_13));
        //        tv_actionBarRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.y28));
        //        tv_actionBarRight.setEnabled(false);

        //        tv_actionBarRight.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        //        datas = (List<MessagePhotoEntity>) getIntent().getSerializableExtra("photo_list");
        currentPos = getIntent().getIntExtra("photo_position", 0);
        progress = getIntent().getIntExtra("photo_progress", 0);
        //        datas = getIntent().getParcelableArrayListExtra("photo_list");
        //        datas = MessagePhotoActivity.datas;
        //        msgs = (List<ConversationMsg>) getIntent().getSerializableExtra("photo_conversationMsg");
        PrintLog.i("initData--currentPos = " + currentPos);
        if (FileUtils.datas != null) {
            PrintLog.i("initData--datas.size() = " + FileUtils.datas.size());
        }
        if (FileUtils.msgs != null) {
            PrintLog.i("initData--msgs.size() = " + FileUtils.msgs.size());
        }

        //        hasSelected = getIntent().getBooleanExtra("has_selected", false);
        //        isShowSend = getIntent().getBooleanExtra("is_show_title", false);

        //        if (!isShowSend) {
        //            tv_actionBarRight.setVisibility(View.GONE);
        //        }
        //        if (hasSelected) {
        //            tv_actionBarRight.setTextColor(ContextCompat.getColor(this, R.color.colorTextView_12));
        //            tv_actionBarRight.setEnabled(true);
        //        }

        mAdapter = new MessageBrowsePhotoAdapter(this, FileUtils.datas, FileUtils.msgs);
        mAdapter.setCurrentPosition(currentPos);
        vp_browse.setAdapter(mAdapter);
        vp_browse.setCurrentItem(currentPos);
        // addOnPageChangeListener必须放在初始化Adapter之后，因为有可能在数据未初始化好就回调onPageScrolled或onPageSelected导致数据显示异常（空指针）
        vp_browse.addOnPageChangeListener(this);
    }

    @Override
    public void onPageSelected(int position) {
        PrintLog.i("onPageSelected");
        hasInit = false;
        lastPos = currentPos;
        currentPos = position;
        mAdapter.setCurrentPosition(currentPos);
        if (mAdapter.entities != null && mAdapter.entities.size() > 0) {
            // 在滑动后，如果上一张是视频，则停止视频
            PrintLog.i("onPageSelected--lastPos = " + lastPos);
            PrintLog.i("onPageSelected--currentPos = " + currentPos);
            boolean isVideo = mAdapter.entities.get(lastPos).isVideo();
            VideoView videoView = mAdapter.entities.get(lastPos).getVideoView();
            PhotoView photoView = mAdapter.entities.get(lastPos).getPhotoView();
            ImageView videoPlay = mAdapter.entities.get(lastPos).getVideoPlayIv();
            String videoPath = mAdapter.entities.get(lastPos).getPath();
            TextView progressView = mAdapter.entities.get(lastPos).getProgressView();
            ImageView animationView = mAdapter.entities.get(lastPos).getAnimationView();
            if (isVideo && videoView.isPlaying()) {
                try {
                    videoView.stopPlayback();
                    videoView.setVisibility(View.GONE);
                    progressView.setVisibility(View.GONE);
                    animationView.setVisibility(View.GONE);
                    photoView.setVisibility(View.VISIBLE);
                    videoPlay.setVisibility(View.VISIBLE);
                    mAdapter.setCurrentIndex(0);
                    mAdapter.setPause(true);
                    Glide.with(mContext).load(videoPath).asBitmap().fitCenter().into(photoView);
                    //                    Glide.with(mContext).load(videoPath).fitCenter().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(photoView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        if (positionOffset == 0) {

            if (mAdapter.entities != null && mAdapter.entities.size() > 0) {
                PrintLog.i("onPageScrolled--currentPos = " + currentPos);
                // 在滑动后，当前如果是接到的视频，且处于未下载或下载失败 状态，则下载
                videoView = mAdapter.entities.get(currentPos).getVideoView();
                photoView = mAdapter.entities.get(currentPos).getPhotoView();
                videoPlay = mAdapter.entities.get(currentPos).getVideoPlayIv();
                animationView = mAdapter.entities.get(currentPos).getAnimationView();
                progressView = mAdapter.entities.get(currentPos).getProgressView();
                photoPath = mAdapter.entities.get(currentPos).getPath();
                isVideo = mAdapter.entities.get(currentPos).isVideo();
                hasInit = true;
                if (FileUtils.msgs != null && FileUtils.msgs.size() != 0) {
                    if (FileUtils.msgs.get(currentPos).getMsgDirection() == MessageDBConstant.IMVT_COM_MSG
                            && (FileUtils.msgs.get(currentPos).getMsgType() == MessageDBConstant.INFO_TYPE_VIDEO
                            || FileUtils.msgs.get(currentPos).getMsgType() == MessageDBConstant.INFO_TYPE_CAMERA_VIDEO)) {
                        if (FileUtils.msgs.get(currentPos).getMsgStatus() == MessageDBConstant.FILE_STATUS_NOT_DOWNLOAD
                                || FileUtils.msgs.get(currentPos).getMsgStatus() == MessageDBConstant.MSG_STATUS_FAIL) {
                            if (!SDCardUtils.isAvailableInternalMemory()) {
                                ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.msg_msg_send_error_2), -1);
                                return;
                            }
                            ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_photo_browser_downloading), -1);
                            showProgressUI();
                            MessageManager.getInstane().downloadMessage((FileUtils.msgs.get(currentPos)));
                        } else if (FileUtils.msgs.get(currentPos).getMsgStatus() == MessageDBConstant.FILE_STATUS_RECEIVING) {
                            progressView.setText(String.format(getResources().getString(R.string.string_photo_progress), progress));
                        } else if (FileUtils.msgs.get(currentPos).getMsgStatus() == MessageDBConstant.FILE_STATUS_RECEIVE_SUCCESS) {
                            dismissProgressUI();
                            videoPlay.setVisibility(View.VISIBLE);
                            videoView.setVisibility(View.GONE);
                            photoView.setVisibility(View.VISIBLE);
                            Glide.with(mContext).load(photoPath).asBitmap().fitCenter().into(photoView);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //    @Override
    //    public void onClick(View v) {
    //        switch (v.getId()) {
    //            case R.id.tv_right1:
    //                Intent intent = new Intent();
    //                setResult(MessagePhotoActivity.SEND_PHOTO_RESULT_CODE, intent);
    //                finish();
    //                break;
    //        }
    //    }

    @Override
    public int notifyDataChangedListener(final ConversationMsg mConversationMsg, int type) {
        PrintLog.i("notifyDataChangedListener");
        /* ptyt begin, 如果在微信列表中正在下载或接收一个文件，然后从相册去查看照片，msgs就为空，不需要上报进度_4208_shafei_20170925 */
        if (FileUtils.msgs == null || FileUtils.msgs.size() == 0) {
            return 0;
        }
        /* ptyt end */
        ConversationMsg conversationMsg = FileUtils.msgs.get(currentPos);
        if (hasInit && conversationMsg != null && conversationMsg.getMsgDirection() == MessageDBConstant.IMVT_COM_MSG
                && mConversationMsg.getMsgConversationId().longValue() == conversationMsg.getMsgConversationId().longValue()) {
            // 更新视频接收状态的同时更新缓存msgs中对应的视频状态
            if ((mConversationMsg.getMsgType() == MessageDBConstant.INFO_TYPE_VIDEO || mConversationMsg.getMsgType() == MessageDBConstant.INFO_TYPE_CAMERA_VIDEO)
                    && (type == MsgBinder.MsgCallBackListener.MSG_STATUS_CHANGE)) {
                for (int i = FileUtils.msgs.size() - 1; i < FileUtils.msgs.size() && i >= 0; i--) {
                    if (FileUtils.msgs.get(i).getMsgUctId().equals(mConversationMsg.getMsgUctId())) {
                        FileUtils.msgs.get(i).setMsgStatus(mConversationMsg.getMsgStatus());
                        break;
                    }
                }
            }
            if (mConversationMsg.getMsgUctId().equals(conversationMsg.getMsgUctId())) {
                switch (type) {
                    case MsgBinder.MsgCallBackListener.MSG_SEND_INSERT_DB:
                        PrintLog.i("MSG_S END_INSERT_DB");
                        break;
                    case MsgBinder.MsgCallBackListener.MSG_STATUS_CHANGE:
                        PrintLog.i("MSG_STATUS_CHANGE--status = " + conversationMsg.getMsgStatus());
                        switch (mConversationMsg.getMsgStatus()) {
                            case MessageDBConstant.MSG_STATUS_FAIL:
                                PrintLog.i("MSG_STATUS_FAIL");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dismissProgressUI();
                                        ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_photo_browser_download_fail), -1);
                                    }
                                });
                                break;
                            case MessageDBConstant.MSG_STATUS_SEND_SUCCESS:
                            case MessageDBConstant.FILE_STATUS_RECEIVE_SUCCESS:
                                PrintLog.i("MSG_STATUS_SEND_SUCCESS");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mConversationMsg.getMsgType() == MessageDBConstant.INFO_TYPE_IMAGE) {
                                            dismissProgressUI();
                                            videoPlay.setVisibility(View.GONE);
                                            videoView.setVisibility(View.GONE);
                                            photoView.setVisibility(View.VISIBLE);
                                            Glide.with(mContext).load(photoPath).asBitmap().fitCenter().into(photoView);
                                            //                                        Glide.with(mContext).load(photoPath).fitCenter().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(photoView);
                                            PrintLog.i("播放图片");
                                        } else if (mConversationMsg.getMsgType() == MessageDBConstant.INFO_TYPE_VIDEO || mConversationMsg.getMsgType() == MessageDBConstant.INFO_TYPE_CAMERA_VIDEO) {
                                            //                                        ((AudioManager) UctApplication.getInstance().getSystemService(Context.AUDIO_SERVICE)).setMode(AudioManager.MODE_NORMAL);
                                            CallCallBack.getInstance().setVoiceCallMode(AudioManager.STREAM_MUSIC);
                                            dismissProgressUI();
                                            mAdapter.setPause(false);
                                            videoPlay.setVisibility(View.GONE);
                                            photoView.setVisibility(View.GONE);
                                            videoView.setVisibility(View.VISIBLE);
                                            videoView.setVideoPath(photoPath);
                                            videoView.seekTo(0);
                                            videoView.start();
                                            PrintLog.i("滑动后播放");
                                        }
                                    }
                                });
                                break;
                        }
                        break;
                    case MsgBinder.MsgCallBackListener.MSG_EASTONECFM:
                        break;
                    case MsgBinder.MsgCallBackListener.MSG_UPDATE_PROGRESS:
                        // 小于500ms不刷新
                        if (!AppUtils.isFastClick()) {
                            final Long offsize = mConversationMsg.getOffSize();
                            final Long fileSize = mConversationMsg.getFileSize();
                            // 在数据插入数据库之后，上传文件之前，文件大小和偏移量可能为空
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressView.setText(String.format(getResources().getString(R.string.string_photo_progress), FileUtils.getProgress(offsize, fileSize)));
                                    PrintLog.i("progressView1 = " + progressView.getText().toString());
                                    showProgressUI();
                                }
                            });

                        }
                        break;
                }
            }
        }
        return 0;
    }

    /**
     * @param
     * @return
     * @description 显示进度条
     */
    private void showProgressUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoPlay.setVisibility(View.GONE);
                progressView.setVisibility(View.VISIBLE);
                animationView.setVisibility(View.VISIBLE);
                animationView.setBackgroundResource(R.drawable.animation_message_sending);
                startAnimation();
            }
        });
    }

    /**
     * @param
     * @return
     * @description 隐藏进度条
     */
    private void dismissProgressUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressView.setVisibility(View.INVISIBLE);
                animationView.setVisibility(View.INVISIBLE);
                stopAnimation();
            }
        });
    }

    /**
     * @param
     * @return
     * @description 开启动画
     */
    private void startAnimation() {
        sendingAnimation = (AnimationDrawable) animationView.getBackground();
        if (sendingAnimation != null && !sendingAnimation.isRunning()) {
            sendingAnimation.start();
        }
    }

    /**
     * @param
     * @return
     * @description 关闭动画
     */
    public void stopAnimation() {
        if (sendingAnimation != null && sendingAnimation.isRunning()) {
            sendingAnimation.selectDrawable(0);
            sendingAnimation.stop();
        }
    }

    @Override
    protected void onPause() {
        PrintLog.i("onPause");
        if (isVideo && videoView.isPlaying()) {
            try {
                videoView.pause();
                mAdapter.setCurrentIndex(videoView.getCurrentPosition());
                mAdapter.setPause(true);
                videoPlay.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        PrintLog.i("onResume");
        if (isVideo && videoView != null) {
            videoView.seekTo(mAdapter.getCurrentIndex());
        }
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        PrintLog.i("unRegisterObserver BaseServiceCallBack.INDEX_IMESSAGEVIEW");
        MessageManager.getInstane().unRegisterObserver(this, BaseServiceCallBack.INDEX_IMESSAGEVIEW);
        stopAnimation();
        Glide.get(mContext).clearMemory();
        if (photoView != null) {
            Glide.clear(photoView);
        }
        videoView = null;
        photoView = null;
        videoPlay = null;
        animationView = null;
        progressView = null;
        photoPath = null;
        mAdapter = null;
        vp_browse = null;
        super.onDestroy();
    }

}
