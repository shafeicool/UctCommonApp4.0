package com.ptyt.uct.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.MessageBrowsePhotoActivity;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.entity.MessagePhotoEntity;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.model.MessageDBManager;
import com.ptyt.uct.utils.BitmapUtil;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.utils.ToastUtils;

/**
 * Title: com.ptyt.uct.widget
 * Description:
 * Date: 2017/5/22
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageVideoLayout extends MessageBaseLayout implements View.OnClickListener, View.OnLongClickListener {

    private Context mContext;
    private BubblePhotoView iv_photo;
    private ImageView iv_animation, iv_play;
    private TextView tv_progress;
    private AnimationDrawable sendingAnimation;

    // 当前视频的对象
    private ConversationMsg conversationMsg;
    //    // 在当前会话，所有图片和视频的对象集合
    //    private List<ConversationMsg> msgs = new ArrayList<>();
    //    // 在当前会话，由msgs转换的对象集合
    //    private List<MessagePhotoEntity> datas = new ArrayList<>();
    // 当前视频在当前会话中的位置
    private int position;
    // 当前视频在当前会话中所有图片和视频中的位置
    private int currentPos;
    // 第一次添加该Layout
    private boolean isFirstEnter = true;
    private int progress = 0;
    //    private static final int INIT_BROWSE_PHOTO = 0;

    public MessageVideoLayout(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public MessageVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void show(ConversationMsg conversationMsg, int position) {
        this.conversationMsg = conversationMsg;
        this.position = position;
        initWindow(mContext);
        updateProgress();
    }

    @Override
    protected int setLayoutId() {
        if (conversationMsg.getMsgDirection() == MessageDBConstant.IMVT_COM_MSG) {
            return R.layout.view_message_photo_content_left;
        } else {
            return R.layout.view_message_photo_content_right;
        }
    }

    @Override
    protected void initView(View view) {
        iv_photo = (BubblePhotoView) view.findViewById(R.id.iv_photo);
        iv_animation = (ImageView) view.findViewById(R.id.iv_animation);
        tv_progress = (TextView) view.findViewById(R.id.tv_progress);
        iv_play = (ImageView) view.findViewById(R.id.iv_play);

        iv_photo.setOnClickListener(this);
        iv_photo.setOnLongClickListener(this);
    }

    @Override
    protected void initData() {
        ImageLoader.getInstance().displayImage("file://" + conversationMsg.getLocalImgPath(), iv_photo, BitmapUtil.videoVerticalOptions);
        //        sendMsg(INIT_BROWSE_PHOTO);
    }

    /**
     * @param
     * @return
     * @description 不同发送状态下，UI的显示
     */
    private void updateProgress() {
        switch (conversationMsg.getMsgStatus()) {
            case MessageDBConstant.MSG_STATUS_WAIT_SENDING:
            case MessageDBConstant.MSG_STATUS_SENDING:
            case MessageDBConstant.FILE_STATUS_RECEIVING:
            case MessageDBConstant.FILE_STATUS_WAIT_RECEIVING:
                iv_play.setVisibility(View.GONE);
                iv_photo.setColorFilter(ContextCompat.getColor(mContext, R.color.colorBackground04), PorterDuff.Mode.MULTIPLY);
                tv_progress.setVisibility(View.VISIBLE);
                iv_animation.setVisibility(View.VISIBLE);
                //                iv_animation.setBackgroundResource(R.drawable.animation_message_sending);
                startAnimation();
                Long offsize = conversationMsg.getOffSize();
                Long fileSize = conversationMsg.getFileSize();
                // 在数据插入数据库之后，上传文件之前，文件大小和偏移量可能为空
                progress = FileUtils.getProgress(offsize, fileSize);
                tv_progress.setText(String.format(getResources().getString(R.string.string_photo_progress), progress));
                break;
            case MessageDBConstant.MSG_STATUS_FAIL:
            case MessageDBConstant.MSG_STATUS_SEND_SUCCESS:
            case MessageDBConstant.FILE_STATUS_NOT_DOWNLOAD:
                iv_play.setVisibility(View.VISIBLE);
                iv_photo.clearColorFilter();
                tv_progress.setVisibility(View.INVISIBLE);
                iv_animation.setVisibility(View.INVISIBLE);
                stopAnimation();
                break;
            case MessageDBConstant.FILE_STATUS_RECEIVE_SUCCESS:
                iv_play.setVisibility(View.VISIBLE);
                iv_photo.clearColorFilter();
                tv_progress.setVisibility(View.INVISIBLE);
                iv_animation.setVisibility(View.INVISIBLE);
                stopAnimation();
                FileUtils.scanFile(mContext, conversationMsg.getLocalImgPath());
                break;
            default:
                break;
        }
    }

    private void startAnimation() {
        sendingAnimation = (AnimationDrawable) iv_animation.getBackground();
        if (sendingAnimation != null && !sendingAnimation.isRunning()) {
            sendingAnimation.start();
        }
    }

    private void stopAnimation() {
        if (sendingAnimation != null && sendingAnimation.isRunning()) {
            sendingAnimation.selectDrawable(0);
            sendingAnimation.stop();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_photo:
                if (!SDCardUtils.isAvailableInternalMemory()
                        && conversationMsg.getMsgDirection() == MessageDBConstant.IMVT_COM_MSG
                        && (conversationMsg.getMsgStatus() == MessageDBConstant.MSG_STATUS_FAIL || conversationMsg.getMsgStatus() == MessageDBConstant.FILE_STATUS_NOT_DOWNLOAD)) {
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.msg_msg_send_error_2), -1);
                    return;
                }
                getAllImageAndVideo();
                Intent intent = new Intent(mContext, MessageBrowsePhotoActivity.class);
                //                intent.putExtra("photo_list", (Serializable) datas);
                //                Bundle bundle = new Bundle();
                //                bundle.putParcelableArrayList("photo_list", (ArrayList<? extends Parcelable>) datas);
                //                intent.putExtras(bundle);
                intent.putExtra("photo_position", currentPos);
                intent.putExtra("photo_progress", progress);
                //                intent.putExtra("photo_conversationMsg", (Serializable) msgs);
                //                intent.putExtra("is_show_title", false);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.iv_photo:
                new MessageOptionDialogManager(mContext, conversationMsg, position);
                break;
        }
        return true;
    }

    private void getAllImageAndVideo() {
        FileUtils.datas.clear();
        FileUtils.msgs.clear();
        FileUtils.msgs = MessageDBManager.getInstance(mContext).queryMessagePhotoById(conversationMsg.getMsgConversationId());
        for (int i = 0; i < FileUtils.msgs.size(); i++) {
            MessagePhotoEntity messagePhotoEntity = new MessagePhotoEntity();
            messagePhotoEntity.setType(FileUtils.msgs.get(i).getMsgType());
            messagePhotoEntity.setPath(FileUtils.msgs.get(i).getLocalImgPath());
            if (FileUtils.msgs.get(i).getMsgUctId().equals(conversationMsg.getMsgUctId())) {
                currentPos = i;
            }
            FileUtils.datas.add(messagePhotoEntity);
        }
    }

}
