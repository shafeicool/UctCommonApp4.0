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

import com.alibaba.fastjson.JSON;
import com.android.uct.utils.PrintLog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.MessageLookOverLocationActivity;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.entity.MessageMyLocationEntity;
import com.ptyt.uct.entity.MessagePhotoEntity;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.utils.BitmapUtil;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.StrUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Title: com.ptyt.uct.widget
 * Description:
 * Date: 2017/5/22
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageMyLocationLayout extends MessageBaseLayout implements View.OnClickListener, View.OnLongClickListener {

    private Context mContext;
    private BubblePhotoView iv_mylocation;
    private ImageView iv_animation;
    private TextView tv_progress;
    private TextView tv_name;
    private TextView tv_location;

    private AnimationDrawable sendingAnimation;
    // 当前图片的对象
    private ConversationMsg conversationMsg;
    // 在当前会话，所有图片和视频的对象集合
    private List<ConversationMsg> msgs = new ArrayList<>();
    // 在当前会话，由msgs转换的对象集合
    private List<MessagePhotoEntity> datas = new ArrayList<>();
    // 当前图片在当前会话中的位置
    private int position;
    // 当前图片在当前会话中所有图片和视频中的位置
    private int currentPos;
    //    private String[] content;
    private MessageMyLocationEntity entity;

    public MessageMyLocationLayout(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public MessageMyLocationLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
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
            return R.layout.view_message_mylocation_content_left;
        } else {
            return R.layout.view_message_mylocation_content_right;
        }
    }

    @Override
    protected void initView(View view) {
        tv_name = (TextView) view.findViewById(R.id.tv_name);
        tv_location = (TextView) view.findViewById(R.id.tv_location);
        iv_mylocation = (BubblePhotoView) view.findViewById(R.id.iv_mylocation);
        iv_animation = (ImageView) view.findViewById(R.id.iv_animation);
        tv_progress = (TextView) view.findViewById(R.id.tv_progress);

        iv_mylocation.setOnClickListener(this);
        iv_mylocation.setOnLongClickListener(this);
    }

    @Override
    protected void initData() {
        ImageLoader.getInstance().displayImage("file://" + conversationMsg.getLocalImgPath(), iv_mylocation, BitmapUtil.imageVerticalOptions);
        try {
            entity = JSON.parseObject(conversationMsg.getContent(), MessageMyLocationEntity.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PrintLog.i("entity = " + entity);
        if (entity == null ) {
            tv_name.setText(mContext.getString(R.string.string_message_unknown_place));
            tv_location.setText(mContext.getString(R.string.string_message_unknown_location));
        } else {
            String placeName = entity.getPlaceName();
            String specificPlacename = entity.getSpecificPlaceName();
            if (StrUtils.isEmpty(placeName)) {
                tv_name.setText(mContext.getString(R.string.string_message_unknown_place));
            } else {
                tv_name.setText(placeName);
            }
            if (StrUtils.isEmpty(specificPlacename)) {
                tv_location.setText(mContext.getString(R.string.string_message_unknown_location));
            } else {
                tv_location.setText(specificPlacename);
            }
        }
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
                iv_mylocation.setColorFilter(ContextCompat.getColor(mContext, R.color.colorBackground04), PorterDuff.Mode.MULTIPLY);
                tv_progress.setVisibility(View.VISIBLE);
                iv_animation.setVisibility(View.VISIBLE);
                startAnimation();
                Long offsize = conversationMsg.getOffSize();
                Long fileSize = conversationMsg.getFileSize();
                // 在数据插入数据库之后，上传文件之前，文件大小和偏移量可能为空
                tv_progress.setText(String.format(getResources().getString(R.string.string_photo_progress), FileUtils.getProgress(offsize, fileSize)));
                break;
            case MessageDBConstant.MSG_STATUS_FAIL:
            case MessageDBConstant.MSG_STATUS_SEND_SUCCESS:
            case MessageDBConstant.FILE_STATUS_NOT_DOWNLOAD:
            case MessageDBConstant.FILE_STATUS_RECEIVE_SUCCESS:
                iv_mylocation.clearColorFilter();
                tv_progress.setVisibility(View.INVISIBLE);
                iv_animation.setVisibility(View.INVISIBLE);
                stopAnimation();
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
            case R.id.iv_mylocation:
                //                getAllImageAndVideo();
                //                Intent intent = new Intent(mContext, MessageBrowsePhotoActivity.class);
                //                Bundle bundle = new Bundle();
                //                bundle.putParcelableArrayList("photo_list", (ArrayList<? extends Parcelable>) datas);
                //                intent.putExtras(bundle);
                //                intent.putExtra("photo_position", currentPos);
                //                intent.putExtra("photo_conversationMsg", (Serializable) msgs);
                //                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //                mContext.startActivity(intent);
                if (conversationMsg.getMsgDirection() == MessageDBConstant.IMVT_COM_MSG) {
                    if (conversationMsg.getMsgStatus() == MessageDBConstant.FILE_STATUS_RECEIVE_SUCCESS) {
                        Intent intent = new Intent(mContext, MessageLookOverLocationActivity.class);
                        intent.putExtra("myLocation", entity);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(mContext, MessageLookOverLocationActivity.class);
                    intent.putExtra("myLocation", entity);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.iv_mylocation:
                new MessageOptionDialogManager(mContext, conversationMsg, position);
                break;
        }
        return true;
    }

    //    private void getAllImageAndVideo() {
    //        msgs = MessageDBManager.getInstance(mContext).queryMessagePhotoById(conversationMsg.getMsgConversationId());
    //        datas.clear();
    //        for (int i = 0; i < msgs.size(); i++) {
    //            MessagePhotoEntity messagePhotoEntity = new MessagePhotoEntity();
    //            messagePhotoEntity.setType(msgs.get(i).getMsgType());
    //            messagePhotoEntity.setLocalPath(msgs.get(i).getLocalImgPath());
    //            if (msgs.get(i).getMsgUctId().equals(conversationMsg.getMsgUctId())) {
    //                currentPos = i;
    //            }
    //            datas.add(messagePhotoEntity);
    //        }
    //    }
}
