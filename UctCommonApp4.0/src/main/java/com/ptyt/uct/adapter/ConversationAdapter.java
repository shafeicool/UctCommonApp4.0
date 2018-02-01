package com.ptyt.uct.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.uct.bean.GroupData;
import com.ptyt.uct.R;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.entity.Conversation;
import com.ptyt.uct.model.ContactDBManager;
import com.ptyt.uct.utils.DateUtils;
import com.ptyt.uct.utils.StrUtils;

import java.util.List;

/**
 * Title: com.ptyt.uct.adapter
 * Description:
 * Date: 2017/6/6
 * Author: ShaFei
 * Version: V1.0
 */

public class ConversationAdapter extends BaseRecyAdapter<Conversation> {

    private Context mContext;
    private List<GroupData> groupDatas;

    public ConversationAdapter(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ConversationViewHolder(inflater.inflate(R.layout.item_conversation_list, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ConversationViewHolder viewHolder = (ConversationViewHolder) holder;
        String groupNo = getItem(position).getGroupNo();
        String msgSrcNo = getItem(position).getMsgSrcNo();
        String loginNumber = AppContext.getAppContext().getLoginNumber();
        Integer lastMsgType = getItem(position).getLastMsgType();
        String lastMsgContent = getItem(position).getLastMsgContent() + "";
        String msgDstNo = getItem(position).getMsgDstNo() + "";
        String lastMsgTime = DateUtils.getTimePoint(getItem(position).getLastMsgTime()) + "";
        Integer lastMsgStatus = getItem(position).getLastMsgStatus();
        Integer lastMsgDirection = getItem(position).getLastMsgDirection();
        String unreadMsgCounts = getItem(position).getUnreadMsgCounts() + "";
        Long stickTime = getItem(position).getStickTime();
        if (stickTime != null && stickTime > 0) {
            viewHolder.mConversationItem.setBackgroundResource(R.drawable.selector_conversation_stick_background);
        } else {
            viewHolder.mConversationItem.setBackgroundResource(R.drawable.selector_press_background);
        }

        if (!StrUtils.isEmpty(groupNo)) {// 如果是组聊天，显示目的号码即组号码
            /* ptyt begin, 优先显示组名称，如果没有则显示组号码_4094_shafei_20170908 */
            viewHolder.mName.setText(msgDstNo);
            if (groupDatas != null && groupDatas.size() > 0) {
                for (int i = 0; i < groupDatas.size(); i++) {
                    if (groupDatas.get(i).groupId.equals(groupNo)) {
                        if (StrUtils.isEmpty(groupDatas.get(i).groupName)) {
                            viewHolder.mName.setText(groupDatas.get(i).groupId);
                        } else {
                            viewHolder.mName.setText(groupDatas.get(i).groupName);
                        }
                        break;
                    }
                }
            }
            //            String name = ContactDBManager.getInstance(mContext).queryContactName(msgDstNo);
            //            if (StrUtils.isEmpty(name)) {
            //                viewHolder.mName.setText(msgDstNo);
            //            } else {
            //                viewHolder.mName.setText(name);
            //            }
            /* ptyt end */
            viewHolder.mHead.setBackgroundResource(R.mipmap.icon_message_group);
        } else {// 如果是一对一聊天，显示对方号码
            if (msgSrcNo.equals(loginNumber)) {
                String name = ContactDBManager.getInstance(mContext).queryContactName(msgDstNo);
                //                if (StrUtils.isEmpty(name)) {
                //                    viewHolder.mName.setText(msgDstNo);
                //                } else {
                viewHolder.mName.setText(name);
                //                }
            } else {
                String name = ContactDBManager.getInstance(mContext).queryContactName(msgSrcNo);
                //                if (StrUtils.isEmpty(name)) {
                //                    viewHolder.mName.setText(msgSrcNo);
                //                } else {
                viewHolder.mName.setText(name);
                //                }
            }
            viewHolder.mHead.setBackgroundResource(R.mipmap.icon_message_person);
        }
        viewHolder.mTime.setText(lastMsgTime);

        setLastMessage(viewHolder.mLastMessage, 0);
        // 只有发送消息时有箭头，接来的消息不会有箭头
        if (lastMsgDirection == MessageDBConstant.IMVT_TO_MSG) {
            switch (lastMsgStatus) {
                case MessageDBConstant.MSG_STATUS_WAIT_SENDING:
                case MessageDBConstant.MSG_STATUS_SENDING:
                    setLastMessage(viewHolder.mLastMessage, R.mipmap.icon_message_sending);
                    break;
                case MessageDBConstant.MSG_STATUS_FAIL:
                    setLastMessage(viewHolder.mLastMessage, R.mipmap.icon_failure);
                    break;
                case MessageDBConstant.MSG_STATUS_SEND_SUCCESS:
                case MessageDBConstant.FILE_STATUS_RECEIVE_SUCCESS:
                case MessageDBConstant.FILE_STATUS_RECEIVING:
                case MessageDBConstant.FILE_STATUS_WAIT_RECEIVING:
                    setLastMessage(viewHolder.mLastMessage, 0);
                    break;
            }
        }

        switch (lastMsgType) {
            case MessageDBConstant.INFO_TYPE_TEXT:
            case MessageDBConstant.INFO_TYPE_OLD_DEVICE_TEXT:
                viewHolder.mLastMessage.setText(StrUtils.faceHandler(mContext, lastMsgContent));
                break;
            case MessageDBConstant.INFO_TYPE_AUDIO:
                viewHolder.mLastMessage.setText(mContext.getString(R.string.string_last_message_audio));
                break;
            case MessageDBConstant.INFO_TYPE_IMAGE:
                viewHolder.mLastMessage.setText(mContext.getString(R.string.string_last_message_image));
                break;
            case MessageDBConstant.INFO_TYPE_VIDEO:
            case MessageDBConstant.INFO_TYPE_CAMERA_VIDEO:
                viewHolder.mLastMessage.setText(mContext.getString(R.string.string_last_message_video));
                break;
            case MessageDBConstant.INFO_TYPE_FILE:
                viewHolder.mLastMessage.setText(mContext.getString(R.string.string_last_message_file));
                break;
            case MessageDBConstant.INFO_TYPE_MY_LOCATION:
                viewHolder.mLastMessage.setText(mContext.getString(R.string.string_last_message_my_location));
                break;
            case MessageDBConstant.INFO_TYPE_OLD_DEVICE_COLOR_MSG:
                break;
            case MessageDBConstant.INFO_TYPE_AUDIO_CALL:
                viewHolder.mLastMessage.setText(mContext.getString(R.string.string_last_message_audio_call));
                break;
            default:
                viewHolder.mLastMessage.setText(mContext.getString(R.string.string_last_message_unknown));
                break;
        }
        // 如果未读数为0，则不显示
        if (StrUtils.isEmpty(unreadMsgCounts) || Integer.parseInt(unreadMsgCounts) == 0) {
            viewHolder.mUnreadCount.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.mUnreadCount.setVisibility(View.VISIBLE);
            if (Integer.parseInt(unreadMsgCounts) >= 100) {
                viewHolder.mUnreadCount.setText("...");
            } else {
                viewHolder.mUnreadCount.setText(unreadMsgCounts);
            }
        }

    }

    private void setLastMessage(TextView tv, int resId) {
        if (resId == 0) {
            tv.setCompoundDrawables(null, null, null, null);
            return;
        }
        Drawable drawable = ContextCompat.getDrawable(mContext, resId);
        int width = drawable.getMinimumWidth();
        int height = drawable.getMinimumHeight();
        if (width > 45 || height > 45) {
            drawable.setBounds(0, 0, 45, 45);
        } else {
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        }
        tv.setCompoundDrawables(drawable, null, null, null);
    }

    public void setGroupDatas(List<GroupData> groupDatas) {
        this.groupDatas = groupDatas;
    }

    public class ConversationViewHolder extends BaseViewHolder {
        private RelativeLayout mConversationItem;
        private ImageView mHead;
        private TextView mName;
        private TextView mLastMessage;
        private TextView mTime;
        private TextView mUnreadCount;

        public ConversationViewHolder(View itemView) {
            super(itemView);
            mConversationItem = (RelativeLayout) itemView.findViewById(R.id.conversation_rl);
            mHead = (ImageView) itemView.findViewById(R.id.head_iv);
            mName = (TextView) itemView.findViewById(R.id.name_tv);
            mLastMessage = (TextView) itemView.findViewById(R.id.last_message_tv);
            mTime = (TextView) itemView.findViewById(R.id.time_tv);
            mUnreadCount = (TextView) itemView.findViewById(R.id.unread_count_tv);
        }
    }
}
