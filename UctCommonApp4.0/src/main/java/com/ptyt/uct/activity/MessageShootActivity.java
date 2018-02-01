package com.ptyt.uct.activity;

import android.content.Intent;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.entity.MessagePhotoEntity;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.widget.CameraManager;
import com.ptyt.uct.widget.CameraProgressBar;
import com.ptyt.uct.widget.CameraView;
import com.ptyt.uct.widget.MediaPlayerManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Title: com.ptyt.uct.activity
 * Description:
 * Date: 2017/6/27
 * Author: ShaFei
 * Version: V1.0
 */

/**
 * 拍摄界面
 */
@RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MessageShootActivity extends BaseActivity implements View.OnClickListener {
    /**
     * 获取相册
     */
    public static final int REQUEST_PHOTO = 1;
    /**
     * 获取视频
     */
    public static final int REQUEST_VIDEO = 2;
    /**
     * 最小录制时间
     */
    private static final int MIN_RECORD_TIME = 1000;
    /**
     * 最长录制时间
     */
    private static final int MAX_RECORD_TIME = 15 * 1000;
    /**
     * 刷新进度的间隔时间
     */
    private static final int PLUSH_PROGRESS = 50;

    /**
     * TextureView
     */
    private TextureView mTextureView;
    /**
     * 带手势识别
     */
    private CameraView mCameraView;
    /**
     * 录制按钮
     */
    private CameraProgressBar mProgressbar;
    /**
     * 顶部像机设置
     */
    private RelativeLayout rl_camera;
    /**
     * 关闭,选择,前后置
     */
    private ImageView iv_close, iv_facing, iv_choice, iv_cancel;
    /**
     * 闪光
     */
    private TextView tv_flash, tv_character;
    /**
     * camera manager
     */
    private CameraManager cameraManager;
    /**
     * player manager
     */
    private MediaPlayerManager playerManager;
    /**
     * true代表视频录制,否则拍照
     */
    private boolean isSupportRecord;
    /**
     * 视频录制地址
     */
    private String recorderPath;
    /**
     * 图片地址
     */
    private String photoPath;
    /**
     * 录制视频的时间,毫秒
     */
    private int recordSecond;
    /**
     * 获取照片订阅, 进度订阅
     */
    private Subscription takePhotoSubscription, progressSubscription;
    /**
     * 是否正在录制
     */
    private boolean isRecording;
    /**
     * 是否为点了拍摄状态(没有拍照预览的状态)
     */
    private boolean isPhotoTakingState;
    /**
     * 是否拍摄或录制完成
     */
    private boolean isFinished = false;
    /**
     * 源号码，目的号码，会话ID，用于生成文件名
     */
    private String msgSrcNo;
    private String msgDstNo;
    private Long conversationId;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_camera);
        mTextureView = (TextureView) findViewById(R.id.mTextureView);
        mCameraView = (CameraView) findViewById(R.id.mCameraView);
        mProgressbar = (CameraProgressBar) findViewById(R.id.mProgressbar);
        tv_character = (TextView) findViewById(R.id.tv_character);
        rl_camera = (RelativeLayout) findViewById(R.id.rl_camera);
        iv_close = (ImageView) findViewById(R.id.iv_close);
        iv_close.setOnClickListener(this);
        iv_choice = (ImageView) findViewById(R.id.iv_choice);
        iv_choice.setOnClickListener(this);
        iv_facing = (ImageView) findViewById(R.id.iv_facing);
        iv_facing.setOnClickListener(this);
        tv_flash = (TextView) findViewById(R.id.tv_flash);
        tv_flash.setOnClickListener(this);
        iv_cancel = (ImageView) findViewById(R.id.iv_cancel);
        iv_cancel.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        msgSrcNo = getIntent().getStringExtra("msgSrcNo");
        msgDstNo = getIntent().getStringExtra("msgDstNo");
        conversationId = getIntent().getLongExtra("conversationId", 0);
        cameraManager = CameraManager.getInstance(this);
        playerManager = MediaPlayerManager.getInstance(this);
        cameraManager.setCameraType(isSupportRecord ? 1 : 0);

        tv_flash.setVisibility(cameraManager.isSupportFlashCamera() ? View.VISIBLE : View.GONE);
        setCameraFlashState();
        iv_facing.setVisibility(cameraManager.isSupportFrontCamera() ? View.VISIBLE : View.GONE);
        rl_camera.setVisibility(cameraManager.isSupportFlashCamera() || cameraManager.isSupportFrontCamera() ? View.VISIBLE : View.GONE);

        final int max = MAX_RECORD_TIME / PLUSH_PROGRESS;
        mProgressbar.setMaxProgress(max);

        /**
         * 拍照，拍摄按钮监听
         */
        mProgressbar.setOnProgressTouchListener(new CameraProgressBar.OnProgressTouchListener() {
            @Override
            public void onClick(CameraProgressBar progressBar) {
                /* ptyt begin, 防止拍摄过快导致相机功能异常_4126_shafei_20170912 */
                if (AppUtils.isFastClick2()) {
                    return;
                }
                /* ptyt end */
                tv_character.setVisibility(View.INVISIBLE);
                cameraManager.takePhoto(callback);
                isSupportRecord = false;
            }

            @Override
            public void onLongClick(CameraProgressBar progressBar) {
                /* ptyt begin, 防止拍摄过快导致相机功能异常_4126_shafei_20170912 */
                if (AppUtils.isFastClick2()) {
                    return;
                }
                /* ptyt end */
                isSupportRecord = true;
                cameraManager.setCameraType(1);
                recorderPath = SDCardUtils.getChatRecordPath(conversationId, msgSrcNo + "_" + msgDstNo) + StrUtils.getSmsId(msgDstNo, msgSrcNo) + ".mp4";
                PrintLog.i("onLongClick--recorderPath = " + recorderPath);
                cameraManager.startMediaRecord1(recorderPath);
                if (cameraManager.isRecordError()) {
                    cameraManager.setCameraType(0);
                    isSupportRecord = false;
                    isRecording = false;
                    FileUtils.delteFiles(new File(recorderPath));
                    recorderPath = null;
                    return;
                }
                isRecording = true;
                rl_camera.setVisibility(View.GONE);
                tv_character.setVisibility(View.INVISIBLE);
                progressSubscription = Observable.interval(PLUSH_PROGRESS, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).take(max).subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {
                        stopRecorder(true);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        mProgressbar.setProgress(mProgressbar.getProgress() + 1);
                    }
                });
            }

            @Override
            public void onZoom(boolean zoom) {
                cameraManager.handleZoom(zoom);
            }

            @Override
            public void onLongClickUp(CameraProgressBar progressBar) {
                //                isSupportRecord = false;
                if (cameraManager.isRecordError()) {
                    return;
                }
                cameraManager.setCameraType(0);
                stopRecorder(true);
                if (progressSubscription != null) {
                    progressSubscription.unsubscribe();
                }
            }

            @Override
            public void onPointerDown(float rawX, float rawY) {
                if (mTextureView != null) {
                    mCameraView.setFoucsPoint(new PointF(rawX, rawY));
                }
            }
        });

        /*
         *点击预览图聚焦
         */
        mCameraView.setOnViewTouchListener(new CameraView.OnViewTouchListener() {
            @Override
            public void handleFocus(float x, float y) {
                if (!isFinished) {
                    cameraManager.handleFocusMetering(x, y);
                }
            }

            @Override
            public void handleZoom(boolean zoom) {
                if (!isFinished) {
                    cameraManager.handleZoom(zoom);
                }
            }
        });
    }

    /**
     * 设置闪光状态
     */
    private void setCameraFlashState() {
        int flashState = cameraManager.getCameraFlash();
        switch (flashState) {
            case 0: //自动
                tv_flash.setSelected(true);
                tv_flash.setText("自动");
                break;
            case 1://open
                tv_flash.setSelected(true);
                tv_flash.setText("开启");
                break;
            case 2: //close
                tv_flash.setSelected(false);
                tv_flash.setText("关闭");
                break;
        }
    }

    /**
     * 是否显示录制按钮
     *
     * @param isShow
     */
    private void setTakeButtonShow(boolean isShow) {
        if (isShow) {
            mProgressbar.setVisibility(View.VISIBLE);
            rl_camera.setVisibility(cameraManager.isSupportFlashCamera()
                    || cameraManager.isSupportFrontCamera() ? View.VISIBLE : View.GONE);
        } else {
            mProgressbar.setVisibility(View.GONE);
            rl_camera.setVisibility(View.GONE);
        }
    }

    /**
     * 停止拍摄
     */
    private void stopRecorder(boolean play) {
        try {
            isRecording = false;
            cameraManager.stopMediaRecord();
            recordSecond = mProgressbar.getProgress() * PLUSH_PROGRESS;//录制多少毫秒
            mProgressbar.reset();
            if (recordSecond < MIN_RECORD_TIME) {//小于最小录制时间变为拍照
                if (recorderPath != null) {
                    FileUtils.delteFiles(new File(recorderPath));
                    recorderPath = null;
                    recordSecond = 0;
                }
                cameraManager.takePhoto(callback);
                isSupportRecord = false;
                setTakeButtonShow(false);
            } else if (play && mTextureView != null && mTextureView.isAvailable()) {
                isFinished = true;
                mCameraView.setVisibility(View.GONE);
                setTakeButtonShow(false);
                mProgressbar.setVisibility(View.GONE);
                tv_character.setVisibility(View.INVISIBLE);
                iv_choice.setVisibility(View.VISIBLE);
                iv_cancel.setVisibility(View.VISIBLE);
                iv_close.setVisibility(View.GONE);
                cameraManager.closeCamera();
                playerManager.playMedia(new Surface(mTextureView.getSurfaceTexture()), recorderPath);
            }
        } catch (Exception e) {
            cameraManager.stopMediaRecord();
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        if (mTextureView.isAvailable()) {
            if (recorderPath != null) {//优先播放视频
                isFinished = true;
                mCameraView.setVisibility(View.GONE);
                iv_choice.setVisibility(View.VISIBLE);
                iv_cancel.setVisibility(View.VISIBLE);
                setTakeButtonShow(false);
                playerManager.playMedia(new Surface(mTextureView.getSurfaceTexture()), recorderPath);
            } else {
                isFinished = false;
                mCameraView.setVisibility(View.VISIBLE);
                iv_choice.setVisibility(View.GONE);
                iv_cancel.setVisibility(View.GONE);
                setTakeButtonShow(true);
                cameraManager.openCamera(mTextureView.getSurfaceTexture(),
                        mTextureView.getWidth(), mTextureView.getHeight());
            }
        } else {
            mTextureView.setSurfaceTextureListener(listener);
        }
    }

    @Override
    protected void onPause() {
        if (progressSubscription != null) {
            progressSubscription.unsubscribe();
        }
        if (takePhotoSubscription != null) {
            takePhotoSubscription.unsubscribe();
        }
        if (isRecording) {
            stopRecorder(false);
        }
        cameraManager.closeCamera();
        playerManager.stopMedia();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mCameraView.removeOnZoomListener();
        playerManager.releaseMediaPlayer();
        cameraManager.releaseCameraManager();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_close) {
            finish();
            //            if (recorderPath != null) {//有拍摄好的正在播放,重新拍摄
            //                FileUtils.delteFiles(new File(recorderPath));
            //                recorderPath = null;
            //                recordSecond = 0;
            //                playerManager.stopMedia();
            //                setTakeButtonShow(true);
            //                iv_choice.setVisibility(View.GONE);
            //                cameraManager.openCamera(mTextureView.getSurfaceTexture(), mTextureView.getWidth(), mTextureView.getHeight());
            //            } else if (isPhotoTakingState) {
            //                isPhotoTakingState = false;
            //                iv_choice.setVisibility(View.GONE);
            //                setTakeButtonShow(true);
            //                cameraManager.restartPreview();
            //            } else {
            //            }

        } else if (i == R.id.iv_choice) {//拿到图片或视频路径
            if (!SDCardUtils.isAvailableInternalMemory()) {
                ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.msg_msg_send_error_2), -1);
                return;
            }
            List<MessagePhotoEntity> selectedList = new ArrayList<>();
            MessagePhotoEntity entity = new MessagePhotoEntity();
            if (isSupportRecord) {
                if (!StrUtils.isEmpty(recorderPath)) {
                    File file = new File(recorderPath);
                    if (file.exists()) {
                        entity.setSize(file.length());
                        entity.setPath(recorderPath);
                        entity.setDuring((long) recordSecond);
                        entity.setType(MessageDBConstant.INFO_TYPE_CAMERA_VIDEO);
                        FileUtils.scanFile(mContext, recorderPath);
                    } else {
                        ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.msg_msg_send_error_5), -1);
                    }
                } else {
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.msg_msg_send_error_4), -1);
                }
            } else {
                if (!StrUtils.isEmpty(photoPath)) {
                    File file = new File(photoPath);
                    if (file.exists()) {
                        entity.setSize(file.length());
                        entity.setPath(photoPath);
                        entity.setType(MessageDBConstant.INFO_TYPE_IMAGE);
                        FileUtils.scanFile(mContext, photoPath);
                    } else {
                        ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.msg_msg_send_error_7), -1);
                    }
                } else {
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.msg_msg_send_error_6), -1);
                }
            }
            selectedList.add(entity);
            Intent intent = new Intent();
            //            intent.putExtra("PhotoList", (Serializable) selectedList);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("PhotoList", (ArrayList<? extends Parcelable>) selectedList);
            intent.putExtras(bundle);
            setResult(MessageActivity.PHOTO_RESULT_CODE, intent);
            finish();

        } else if (i == R.id.tv_flash) {
            cameraManager.changeCameraFlash(mTextureView.getSurfaceTexture(),
                    mTextureView.getWidth(), mTextureView.getHeight());
            setCameraFlashState();

        } else if (i == R.id.iv_facing) {
            cameraManager.changeCameraFacing(mTextureView.getSurfaceTexture(),
                    mTextureView.getWidth(), mTextureView.getHeight());
            if (cameraManager.isSupportFrontCamera()) {
                int facing = cameraManager.getCameraFacing();
                switch (facing) {
                    case Camera.CameraInfo.CAMERA_FACING_BACK:
                        tv_flash.setVisibility(View.VISIBLE);
                        break;
                    case Camera.CameraInfo.CAMERA_FACING_FRONT:
                        tv_flash.setVisibility(View.INVISIBLE);
                        break;
                }
            }
        } else if (i == R.id.iv_cancel) {
            if (recorderPath != null) {//有拍摄好的正在播放,重新拍摄
                isFinished = false;
                mCameraView.setVisibility(View.VISIBLE);
                FileUtils.delteFiles(new File(recorderPath));
                recorderPath = null;
                recordSecond = 0;
                playerManager.stopMedia();
                setTakeButtonShow(true);
                tv_character.setVisibility(View.VISIBLE);
                iv_choice.setVisibility(View.GONE);
                iv_cancel.setVisibility(View.GONE);
                iv_close.setVisibility(View.VISIBLE);

                cameraManager.openCamera(mTextureView.getSurfaceTexture(), mTextureView.getWidth(), mTextureView.getHeight());
            } else if (isPhotoTakingState) {
                isFinished = false;
                mCameraView.setVisibility(View.VISIBLE);
                FileUtils.delteFiles(new File(photoPath));
                photoPath = null;
                isPhotoTakingState = false;
                tv_character.setVisibility(View.VISIBLE);
                iv_choice.setVisibility(View.GONE);
                iv_cancel.setVisibility(View.GONE);
                iv_close.setVisibility(View.VISIBLE);
                setTakeButtonShow(true);
                cameraManager.restartPreview();
            }
        }
    }

    /**
     * camera回调监听
     */
    private TextureView.SurfaceTextureListener listener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            if (recorderPath != null) {
                isFinished = true;
                mCameraView.setVisibility(View.GONE);
                iv_choice.setVisibility(View.VISIBLE);
                iv_cancel.setVisibility(View.VISIBLE);
                tv_character.setVisibility(View.INVISIBLE);
                setTakeButtonShow(false);
                playerManager.playMedia(new Surface(texture), recorderPath);
            } else {
                isFinished = false;
                mCameraView.setVisibility(View.VISIBLE);
                setTakeButtonShow(true);
                iv_choice.setVisibility(View.GONE);
                iv_cancel.setVisibility(View.GONE);
                tv_character.setVisibility(View.VISIBLE);
                iv_close.setVisibility(View.VISIBLE);
                cameraManager.openCamera(texture, width, height);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };

    private Camera.PictureCallback callback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            setTakeButtonShow(false);
            takePhotoSubscription = Observable.create(new Observable.OnSubscribe<Boolean>() {
                @Override
                public void call(Subscriber<? super Boolean> subscriber) {
                    if (!subscriber.isUnsubscribed()) {
                        photoPath = SDCardUtils.getChatRecordPath(conversationId, msgSrcNo + "_" + msgDstNo) + StrUtils.getSmsId(msgDstNo, msgSrcNo) + ".jpg";
                        isPhotoTakingState = FileUtils.savePhoto(photoPath, data, cameraManager.isCameraFrontFacing());
                        PrintLog.i("onPictureTaken--photoPath = " + photoPath);
                        subscriber.onNext(isPhotoTakingState);
                    }
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Boolean>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(Boolean aBoolean) {
                    if (aBoolean != null && aBoolean) {
                        isFinished = true;
                        mCameraView.setVisibility(View.GONE);
                        tv_character.setVisibility(View.INVISIBLE);
                        iv_choice.setVisibility(View.VISIBLE);
                        iv_cancel.setVisibility(View.VISIBLE);
                        iv_close.setVisibility(View.GONE);
                    } else {
                        setTakeButtonShow(true);
                    }
                }
            });
        }
    };

}
