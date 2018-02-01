package com.ptyt.uct.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.activity.MessageActivity;
import com.ptyt.uct.activity.VideoCallActivity;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.entity.GroupUser;
import com.ptyt.uct.model.ContactDBManager;
import com.ptyt.uct.services.MessageManager;
import com.ptyt.uct.utils.ActivitySkipUtils;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.utils.ScreenUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.widget.MessageAudioLayout;
import com.ptyt.uct.widget.MessageFileLayout;
import com.ptyt.uct.widget.MessageImageLayout;
import com.ptyt.uct.widget.MessageMyLocationLayout;
import com.ptyt.uct.widget.MessageTextLayout;
import com.ptyt.uct.widget.MessageVideoLayout;

/**
 * Title: com.ptyt.uct.adapter
 * Description:
 * Date: 2017/5/4
 * Author: ShaFei
 * Version: V1.0
 */


public class MessageAdapter extends BaseRecyAdapter<ConversationMsg> {

    private Context mContext;
    //    private MessageActivity mContext;
    private int[] resid = {R.drawable.animation_message_sending, R.mipmap.icon_download_cancel, R.mipmap.icon_failure};
    private static final int TYPE_TEXT_LEFT = -1;
    private static final int TYPE_AUDIO_LEFT = -2;
    private static final int TYPE_IMAGE_LEFT = -3;
    private static final int TYPE_VIDEO_LEFT = -4;
    private static final int TYPE_FILE_LEFT = -5;
    private static final int TYPE_MY_LOCATION_LEFT = -6;
    private static final int TYPE_TEXT_RIGHT = 1;
    private static final int TYPE_AUDIO_RIGHT = 2;
    private static final int TYPE_IMAGE_RIGHT = 3;
    private static final int TYPE_VIDEO_RIGHT = 4;
    private static final int TYPE_FILE_RIGHT = 5;
    private static final int TYPE_MY_LOCATION_RIGHT = 6;


    // 上一条item的时间
    private Long preTime = 0L;
    // 当前item的时间
    private Long curTime = 0L;

    public MessageAdapter(Context context) {
        super(context);
        this.mContext = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MessageChatViewHolder holder = null;
        switch (viewType) {
            case TYPE_TEXT_LEFT:
                holder = new MessageChatViewHolder(inflater.inflate(R.layout.item_message_text_left, null), viewType);
                break;
            case TYPE_AUDIO_LEFT:
                holder = new MessageChatViewHolder(inflater.inflate(R.layout.item_message_audio_left, null), viewType);
                break;
            case TYPE_IMAGE_LEFT:
                holder = new MessageChatViewHolder(inflater.inflate(R.layout.item_message_image_left, null), viewType);
                break;
            case TYPE_VIDEO_LEFT:
                holder = new MessageChatViewHolder(inflater.inflate(R.layout.item_message_video_left, null), viewType);
                break;
            case TYPE_FILE_LEFT:
                holder = new MessageChatViewHolder(inflater.inflate(R.layout.item_message_file_left, null), viewType);
                break;
            case TYPE_MY_LOCATION_LEFT:
                holder = new MessageChatViewHolder(inflater.inflate(R.layout.item_message_mylocation_left, null), viewType);
                break;
            case TYPE_TEXT_RIGHT:
                holder = new MessageChatViewHolder(inflater.inflate(R.layout.item_message_text_right, null), viewType);
                break;
            case TYPE_AUDIO_RIGHT:
                holder = new MessageChatViewHolder(inflater.inflate(R.layout.item_message_audio_right, null), viewType);
                break;
            case TYPE_IMAGE_RIGHT:
                holder = new MessageChatViewHolder(inflater.inflate(R.layout.item_message_image_right, null), viewType);
                break;
            case TYPE_VIDEO_RIGHT:
                holder = new MessageChatViewHolder(inflater.inflate(R.layout.item_message_video_right, null), viewType);
                break;
            case TYPE_FILE_RIGHT:
                holder = new MessageChatViewHolder(inflater.inflate(R.layout.item_message_file_right, null), viewType);
                break;
            case TYPE_MY_LOCATION_RIGHT:
                holder = new MessageChatViewHolder(inflater.inflate(R.layout.item_message_mylocation_right, null), viewType);
                break;
        }
        return holder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MessageChatViewHolder viewHolder = (MessageChatViewHolder) holder;
        Long sendTime = getItem(position).getMsgTime();
        if (showTime(position, sendTime)) {
            viewHolder.mSendTime.setVisibility(View.VISIBLE);
            viewHolder.mSendTime.setText(StrUtils.formatChatTime(mContext, sendTime) + "");
        } else {
            viewHolder.mSendTime.setVisibility(View.GONE);
        }

        Integer msgStatus = getItem(position).getMsgStatus();
        Integer msgDirection = getItem(position).getMsgDirection();
        Integer msgType = getItem(position).getMsgType();
        Integer msgResult = getItem(position).getResult();
        if (msgDirection == MessageDBConstant.IMVT_TO_MSG) {
            switch (msgStatus) {
                case MessageDBConstant.MSG_STATUS_WAIT_SENDING:
                case MessageDBConstant.MSG_STATUS_SENDING:
                    if (msgType == MessageDBConstant.INFO_TYPE_TEXT
                            || msgType == MessageDBConstant.INFO_TYPE_OLD_DEVICE_TEXT
                            || msgType == MessageDBConstant.INFO_TYPE_AUDIO
                            || msgType == MessageDBConstant.INFO_TYPE_OLD_DEVICE_COLOR_MSG
                            || msgType == MessageDBConstant.INFO_TYPE_MY_LOCATION) {// 文本、语音在旁边转圈
                        viewHolder.mSendStatus.setVisibility(View.VISIBLE);
                        viewHolder.mSendStatus.setBackgroundResource(R.drawable.animation_message_sending);
                        startAnimation(viewHolder);
                    } else if (msgType == MessageDBConstant.INFO_TYPE_IMAGE) {// 图片旁边什么也没有
                        viewHolder.mSendStatus.setVisibility(View.INVISIBLE);
                    } else {// 视频和文件旁边有红X，可以取消发送
                        viewHolder.mSendStatus.setVisibility(View.VISIBLE);
                        viewHolder.mSendStatus.setBackgroundResource(R.mipmap.icon_download_cancel);
                    }
                    break;
                case MessageDBConstant.MSG_STATUS_FAIL:
                    // 照片、视频、文件不在控件旁边转圈圈
                    viewHolder.mSendStatus.setVisibility(View.VISIBLE);
                    viewHolder.mSendStatus.setBackgroundResource(R.mipmap.icon_failure);
                    stopAnimation(viewHolder);
                    break;
                case MessageDBConstant.MSG_STATUS_SEND_SUCCESS:
                    //                    if (msgResult != null && msgResult == 0) {
                    viewHolder.mSendStatus.setVisibility(View.INVISIBLE);
                    //                    } else {
                    //                        viewHolder.mSendStatus.setVisibility(View.VISIBLE);
                    //                        viewHolder.mSendStatus.setBackgroundResource(R.mipmap.icon_failure);
                    //                    }
                    stopAnimation(viewHolder);
                    break;
                default:
                    viewHolder.mSendStatus.setBackgroundResource(R.mipmap.icon_failure);
                    break;
            }
        } else {
            switch (msgStatus) {
                case MessageDBConstant.FILE_STATUS_NOT_DOWNLOAD:
                case MessageDBConstant.FILE_STATUS_RECEIVE_SUCCESS:
                    viewHolder.mSendStatus.setVisibility(View.INVISIBLE);
                    break;
                case MessageDBConstant.FILE_STATUS_WAIT_RECEIVING:
                case MessageDBConstant.FILE_STATUS_RECEIVING:
                    if (msgType == MessageDBConstant.INFO_TYPE_VIDEO
                            || msgType == MessageDBConstant.INFO_TYPE_CAMERA_VIDEO
                            || msgType == MessageDBConstant.INFO_TYPE_FILE) {
                        viewHolder.mSendStatus.setVisibility(View.VISIBLE);
                        viewHolder.mSendStatus.setBackgroundResource(R.mipmap.icon_download_cancel);
                    } else {
                        viewHolder.mSendStatus.setVisibility(View.INVISIBLE);
                    }
                    break;
                case MessageDBConstant.MSG_STATUS_FAIL:
                    viewHolder.mSendStatus.setVisibility(View.VISIBLE);
                    viewHolder.mSendStatus.setBackgroundResource(R.mipmap.icon_failure);
                    break;
            }
        }

        String msgSrcNo = getItem(position).getMsgSrcNo() + "";
        String name = ContactDBManager.getInstance(mContext).queryContactName(msgSrcNo);
        //        if (StrUtils.isEmpty(name)) {
        //            viewHolder.mUserName.setText(msgSrcNo);
        //        } else {
        viewHolder.mUserName.setText(name);
        //        }

        switch (msgType) {
            case MessageDBConstant.INFO_TYPE_TEXT:
            case MessageDBConstant.INFO_TYPE_OLD_DEVICE_TEXT:
                viewHolder.textLayout.show(getItem(position), position);
                break;
            case MessageDBConstant.INFO_TYPE_AUDIO:
                viewHolder.audioLayout.show(getItem(position), position, this);
                break;
            case MessageDBConstant.INFO_TYPE_IMAGE:
                viewHolder.imageLayout.show(getItem(position), position);
                break;
            case MessageDBConstant.INFO_TYPE_VIDEO:
            case MessageDBConstant.INFO_TYPE_CAMERA_VIDEO:
                viewHolder.videoLayout.show(getItem(position), position);
                break;
            case MessageDBConstant.INFO_TYPE_FILE:
                viewHolder.fileLayout.show(getItem(position), position);
                break;
            case MessageDBConstant.INFO_TYPE_MY_LOCATION:
                viewHolder.myLocationLayout.show(getItem(position), position);
                break;
        }
    }

    private void startAnimation(MessageChatViewHolder viewHolder) {
        viewHolder.sendingAnimation = (AnimationDrawable) viewHolder.mSendStatus.getBackground();
        if (viewHolder.sendingAnimation != null && !viewHolder.sendingAnimation.isRunning()) {
            viewHolder.sendingAnimation.start();
        }
    }

    private void stopAnimation(MessageChatViewHolder viewHolder) {
        if (viewHolder.sendingAnimation != null && viewHolder.sendingAnimation.isRunning()) {
            viewHolder.sendingAnimation.selectDrawable(0);
            viewHolder.sendingAnimation.stop();
        }
    }

    public class MessageChatViewHolder extends BaseViewHolder implements View.OnClickListener {

        private ImageButton mUserHead;
        private TextView mSendTime;
        private TextView mUserName;
        private ImageButton mSendStatus;
        private MessageTextLayout textLayout;
        private MessageAudioLayout audioLayout;
        private MessageImageLayout imageLayout;
        private MessageVideoLayout videoLayout;
        private MessageFileLayout fileLayout;
        private MessageMyLocationLayout myLocationLayout;

        private AnimationDrawable sendingAnimation;
        private PopupWindow mLeftItemPw;
        private ConstantState sendingDrawable, cancelDrawable, failedDrawable;

        public MessageChatViewHolder(final View itemView, int viewType) {
            super(itemView);
            mUserHead = (ImageButton) itemView.findViewById(R.id.iv_userhead);
            mSendTime = (TextView) itemView.findViewById(R.id.tv_sendtime);
            mUserName = (TextView) itemView.findViewById(R.id.tv_username);
            mSendStatus = (ImageButton) itemView.findViewById(R.id.ibtn_sendStatus);
            switch (viewType) {
                case TYPE_TEXT_LEFT:
                case TYPE_TEXT_RIGHT:
                    textLayout = (MessageTextLayout) itemView.findViewById(R.id.btn_chatcontent);
                    break;
                case TYPE_AUDIO_LEFT:
                case TYPE_AUDIO_RIGHT:
                    audioLayout = (MessageAudioLayout) itemView.findViewById(R.id.btn_chatcontent);
                    break;
                case TYPE_IMAGE_LEFT:
                case TYPE_IMAGE_RIGHT:
                    imageLayout = (MessageImageLayout) itemView.findViewById(R.id.btn_chatcontent);
                    break;
                case TYPE_VIDEO_LEFT:
                case TYPE_VIDEO_RIGHT:
                    videoLayout = (MessageVideoLayout) itemView.findViewById(R.id.btn_chatcontent);
                    break;
                case TYPE_FILE_LEFT:
                case TYPE_FILE_RIGHT:
                    fileLayout = (MessageFileLayout) itemView.findViewById(R.id.btn_chatcontent);
                    break;
                case TYPE_MY_LOCATION_LEFT:
                case TYPE_MY_LOCATION_RIGHT:
                    myLocationLayout = (MessageMyLocationLayout) itemView.findViewById(R.id.btn_chatcontent);
                    break;
            }
            mSendStatus.setOnClickListener(this);

            if (viewType < 0) {
                mUserHead.setOnClickListener(this);
            }
            sendingDrawable = ContextCompat.getDrawable(mContext, resid[0]).getConstantState();
            cancelDrawable = ContextCompat.getDrawable(mContext, resid[1]).getConstantState();
            failedDrawable = ContextCompat.getDrawable(mContext, resid[2]).getConstantState();
        }

        @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
        private void showLeftItemWindow() {
            View view = View.inflate(mContext, R.layout.dialog_message_left_item_option, null);
            view.findViewById(R.id.audio_call_ibtn).setOnClickListener(this);
            view.findViewById(R.id.video_call_ibtn).setOnClickListener(this);
            view.findViewById(R.id.video_up_ibtn).setOnClickListener(this);
            ImageButton message_ibtn = (ImageButton) view.findViewById(R.id.message_ibtn);
            if (((MessageActivity) mContext).isGroupNo()) {
                message_ibtn.setVisibility(View.VISIBLE);
                message_ibtn.setOnClickListener(this);
            } else {
                message_ibtn.setVisibility(View.GONE);
            }
            mLeftItemPw = new PopupWindow(view, RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT, true);
            mLeftItemPw.setBackgroundDrawable(new BitmapDrawable());
            mLeftItemPw.setOutsideTouchable(true);
            mLeftItemPw.setAnimationStyle(android.R.style.Animation_Dialog);
            int distanceX = ScreenUtils.getDimensionPixelSize(mContext, R.dimen.x20) + mUserHead.getWidth();
            int distanceY;
            if (mSendTime.getVisibility() == View.GONE) {
                distanceY = ScreenUtils.getDimensionPixelSize(mContext, R.dimen.y130) + mUserHead.getHeight() + (int) itemView.getY();
            } else {
                distanceY = ScreenUtils.getDimensionPixelSize(mContext, R.dimen.y180) + mUserHead.getHeight() + (int) itemView.getY();
            }
            mLeftItemPw.showAtLocation(mUserHead, Gravity.TOP | Gravity.LEFT, distanceX, distanceY);
        }

        @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_userhead:
                    showLeftItemWindow();
                    break;
                case R.id.ibtn_sendStatus:
                    ConstantState current = mSendStatus.getBackground().getConstantState();
                    if (current.equals(sendingDrawable)) {// 发送中
                    } else if (current.equals(cancelDrawable)) {// 取消发送
                        // 取消发送或下载
                        mSendStatus.setBackgroundResource(R.mipmap.icon_failure);
                        MessageManager.getInstane().cancelMsg(getItem(getLayoutPosition()));
                    } else if (current.equals(failedDrawable)) {// 发送失败
                        // 重发或重下
                        if (getItemViewType() > 0) {
                            MessageManager.getInstane().reSendMsg(getItem(getLayoutPosition()));
                        } else {
                            if (!SDCardUtils.isAvailableInternalMemory()) {
                                ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.msg_msg_send_error_2), -1);
                                return;
                            }
                            MessageManager.getInstane().downloadMessage(getItem(getLayoutPosition()));
                        }
                    }
                    break;
                // 语音按钮
                case R.id.audio_call_ibtn:
                    intent2Activity(getLayoutPosition(), ConstantUtils.AUDIO_SCALL);
                    break;
                // 信息按钮
                case R.id.message_ibtn:
                    intent2Activity(getLayoutPosition(), -1);
                    break;
                // 视频按钮
                case R.id.video_call_ibtn:
                    intent2Activity(getLayoutPosition(), ConstantUtils.VIDEO_SCALL);
                    break;
                // 视频上传按钮
                case R.id.video_up_ibtn:
                    intent2Activity(getLayoutPosition(), ConstantUtils.UPLOAD_VIDEO);
                    break;
            }
        }

        /**
         * @param tag 视频细分业务：0-语音呼叫  1-视频呼叫 2-上传视频
         * @return
         * @description 跳转语音视频业务
         */
        private void intent2Activity(int position, int tag) {
            if (mLeftItemPw.isShowing()) {
                mLeftItemPw.dismiss();
            }
            GroupUser groupUser = new GroupUser();
            String msgSrcNo = getItem(position).getMsgSrcNo() + "";
            groupUser.setUserTel(msgSrcNo);
            String name = ContactDBManager.getInstance(mContext).queryContactName(msgSrcNo);
            groupUser.setUserName(name);
            if (tag == -1) {
                ActivitySkipUtils.intent2CallActivity(mContext, MessageActivity.class, tag, groupUser);
            } else {
                ActivitySkipUtils.intent2CallActivity(mContext, VideoCallActivity.class, tag, groupUser);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        int msgDirection = getItem(position).getMsgDirection();
        int msgType = getItem(position).getMsgType();
        int viewType = 0;
        if (msgDirection == MessageDBConstant.IMVT_COM_MSG) {
            switch (msgType) {
                case MessageDBConstant.INFO_TYPE_TEXT:
                case MessageDBConstant.INFO_TYPE_OLD_DEVICE_TEXT:
                    viewType = TYPE_TEXT_LEFT;
                    break;
                case MessageDBConstant.INFO_TYPE_AUDIO:
                    viewType = TYPE_AUDIO_LEFT;
                    break;
                case MessageDBConstant.INFO_TYPE_IMAGE:
                    viewType = TYPE_IMAGE_LEFT;
                    break;
                case MessageDBConstant.INFO_TYPE_VIDEO:
                case MessageDBConstant.INFO_TYPE_CAMERA_VIDEO:
                    viewType = TYPE_VIDEO_LEFT;
                    break;
                case MessageDBConstant.INFO_TYPE_FILE:
                    viewType = TYPE_FILE_LEFT;
                    break;
                case MessageDBConstant.INFO_TYPE_MY_LOCATION:
                    viewType = TYPE_MY_LOCATION_LEFT;
                    break;
            }
        } else {
            switch (msgType) {
                case MessageDBConstant.INFO_TYPE_TEXT:
                case MessageDBConstant.INFO_TYPE_OLD_DEVICE_TEXT:
                    viewType = TYPE_TEXT_RIGHT;
                    break;
                case MessageDBConstant.INFO_TYPE_AUDIO:
                    viewType = TYPE_AUDIO_RIGHT;
                    break;
                case MessageDBConstant.INFO_TYPE_IMAGE:
                    viewType = TYPE_IMAGE_RIGHT;
                    break;
                case MessageDBConstant.INFO_TYPE_VIDEO:
                case MessageDBConstant.INFO_TYPE_CAMERA_VIDEO:
                    viewType = TYPE_VIDEO_RIGHT;
                    break;
                case MessageDBConstant.INFO_TYPE_FILE:
                    viewType = TYPE_FILE_RIGHT;
                    break;
                case MessageDBConstant.INFO_TYPE_MY_LOCATION:
                    viewType = TYPE_MY_LOCATION_RIGHT;
                    break;
            }
        }
        return viewType;
    }

    /**
     * @param position
     * @param sendTime
     * @return 显示/不显示
     * @description 发送消息时是否显示时间，超出3分钟就显示，3分钟之内就不显示
     */
    private boolean showTime(int position, Long sendTime) {
        // 当前item的时间
        curTime = sendTime;
        // 当前列表第一条显示时间
        if (position == 0) {
            return true;
        } else {
            // 前一条item的时间
            preTime = getItem(position - 1).getMsgTime();
            // 超过3分钟则显示时间，否则不显示
            if (curTime - preTime >= 180000L) {
                return true;
            } else {
                return false;
            }
        }
    }

}