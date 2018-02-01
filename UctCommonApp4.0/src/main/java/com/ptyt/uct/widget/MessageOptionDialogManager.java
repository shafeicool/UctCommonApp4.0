package com.ptyt.uct.widget;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.activity.CreateGroupAndMessageForwardActivity;
import com.ptyt.uct.activity.MessageActivity;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.services.MessageManager;
import com.ptyt.uct.utils.ActivitySkipUtils;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;

import java.io.File;

/**
 * Title: com.ptyt.uct.widget
 * Description: 微信长按选项
 * Date: 2017/7/24
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageOptionDialogManager implements View.OnClickListener {

    private ConversationMsg conversationMsg;
    private int position;
    private Context mContext;
    private AlertDialog dialog;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public MessageOptionDialogManager(Context mContext, final ConversationMsg conversationMsg, int position) {
        this.mContext = mContext;
        this.conversationMsg = conversationMsg;
        this.position = position;
        /* ptyt begin, 在发送中或接收中的微信不弹框_4307_shafei_20171010 */
        if (conversationMsg.getMsgDirection() == MessageDBConstant.IMVT_COM_MSG) {
            if (conversationMsg.getMsgStatus() == MessageDBConstant.FILE_STATUS_RECEIVE_SUCCESS) {
                showListDialog();
            }
        } else {
            if (conversationMsg.getMsgStatus() == MessageDBConstant.MSG_STATUS_FAIL
                    || conversationMsg.getMsgStatus() == MessageDBConstant.MSG_STATUS_SEND_SUCCESS) {
                showListDialog();
            }
        }
        /* ptyt end */
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void showListDialog() {
        AlertDialog.Builder listDialog = new AlertDialog.Builder(mContext, R.style.dialog_transparent);
        View view = View.inflate(mContext, R.layout.dialog_message_item_option, null);
        TextView copyView = (TextView) view.findViewById(R.id.copy_tv);
        TextView forwardPersonView = (TextView) view.findViewById(R.id.forward_person_tv);
        TextView forwardGroupView = (TextView) view.findViewById(R.id.forward_group_tv);
        TextView deleteView = (TextView) view.findViewById(R.id.delete_tv);
        LinearLayout line = (LinearLayout) view.findViewById(R.id.line);
        if (conversationMsg.getMsgType() == MessageDBConstant.INFO_TYPE_TEXT || conversationMsg.getMsgType() == MessageDBConstant.INFO_TYPE_OLD_DEVICE_TEXT) {
            copyView.setVisibility(View.VISIBLE);
            copyView.setOnClickListener(this);
            line.setVisibility(View.VISIBLE);
        } else {
            copyView.setVisibility(View.GONE);
            line.setVisibility(View.GONE);
        }
        forwardPersonView.setOnClickListener(this);
        forwardGroupView.setOnClickListener(this);
        deleteView.setOnClickListener(this);
        listDialog.setView(view);
        dialog = listDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.copy_tv:
                ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = null;
                switch (conversationMsg.getMsgType()) {
                    case MessageDBConstant.INFO_TYPE_TEXT:
                    case MessageDBConstant.INFO_TYPE_OLD_DEVICE_TEXT:
                        mClipData = ClipData.newPlainText("Text", conversationMsg.getContent());
                        break;
                    case MessageDBConstant.INFO_TYPE_AUDIO:
                    case MessageDBConstant.INFO_TYPE_IMAGE:
                    case MessageDBConstant.INFO_TYPE_VIDEO:
                    case MessageDBConstant.INFO_TYPE_CAMERA_VIDEO:
                    case MessageDBConstant.INFO_TYPE_FILE:
                    case MessageDBConstant.INFO_TYPE_MY_LOCATION:
                        mClipData = ClipData.newRawUri("File", Uri.fromFile(new File(conversationMsg.getLocalImgPath())));
                        break;
                    case MessageDBConstant.INFO_TYPE_OLD_DEVICE_COLOR_MSG:
                        break;
                    case MessageDBConstant.INFO_TYPE_AUDIO_CALL:
                        break;
                    case MessageDBConstant.INFO_TYPE_PTT_AUDIO:
                        break;

                }
                if (mClipData != null) {
                    cm.setPrimaryClip(mClipData);
                }
                break;
            case R.id.forward_person_tv:
                if (conversationMsg == null) {
                    ToastUtils.getToast().showMessageLong(mContext, "转发的数据为空", -1);
                } else {
                    String path = conversationMsg.getLocalImgPath();
                    if (!StrUtils.isEmpty(path)) {
                        File file = new File(path);
                        if (!file.exists()) {
                            ToastUtils.getToast().showMessageShort(mContext, "文件不存在，不能转发", -1);
                        } else {
                            Intent intent = new Intent(mContext, CreateGroupAndMessageForwardActivity.class);
                            intent.putExtra("conversationMsg", conversationMsg);
                            intent.putExtra("isGroup", false);
                            ActivitySkipUtils.toNextActivity(mContext, intent);
                        }
                    } else {
                        Intent intent = new Intent(mContext, CreateGroupAndMessageForwardActivity.class);
                        intent.putExtra("conversationMsg", conversationMsg);
                        intent.putExtra("isGroup", false);
                        ActivitySkipUtils.toNextActivity(mContext, intent);
                    }
                }
                break;
            case R.id.forward_group_tv:
                if (conversationMsg == null) {
                    ToastUtils.getToast().showMessageLong(mContext, "转发的数据为空", -1);
                } else {
                    String path = conversationMsg.getLocalImgPath();
                    if (!StrUtils.isEmpty(path)) {
                        File file = new File(path);
                        if (!file.exists()) {
                            ToastUtils.getToast().showMessageShort(mContext, "文件不存在，不能转发", -1);
                        } else {
                            Intent intent = new Intent(mContext, CreateGroupAndMessageForwardActivity.class);
                            intent.putExtra("conversationMsg", conversationMsg);
                            intent.putExtra("isGroup", true);
                            ActivitySkipUtils.toNextActivity(mContext, intent);
                        }
                    } else {
                        Intent intent = new Intent(mContext, CreateGroupAndMessageForwardActivity.class);
                        intent.putExtra("conversationMsg", conversationMsg);
                        intent.putExtra("isGroup", true);
                        ActivitySkipUtils.toNextActivity(mContext, intent);
                    }
                }
                break;
            case R.id.delete_tv:
                MessageManager.getInstane().deleteMsg(conversationMsg);
                String localImgPath = conversationMsg.getLocalImgPath();
                if (!StrUtils.isEmpty(localImgPath)) {
                    FileUtils.scanFile(mContext, localImgPath);
                }
                ((MessageActivity) mContext).removeMessageAdapter(position);
                break;
        }
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
