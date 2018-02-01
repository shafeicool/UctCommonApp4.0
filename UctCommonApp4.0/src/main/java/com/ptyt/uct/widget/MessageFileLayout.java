package com.ptyt.uct.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.uct.utils.UctLibStringUtils;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.MessageFileDownloadActivity;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;

import java.io.File;

import static com.ptyt.uct.utils.FileUtils.MIME_MapTable;

/**
 * Title: com.ptyt.uct.widget
 * Description:
 * Date: 2017/5/22
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageFileLayout extends MessageBaseLayout implements View.OnClickListener, View.OnLongClickListener {

    private Context mContext;

    private TextView tv_name, tv_size;
    private ProgressBar pbar_progress;
    private ImageView iv_image;
    private RelativeLayout rl_relative;

    private int msgDirection;
    private int position;
    // 第一次添加该Layout
    private boolean isFirstEnter = true;

    private String path;
    private String suffix;
    private String name;
    private Long fileSize;
    private Long offsize;
    private String type;

    private ConversationMsg conversationMsg;

    public MessageFileLayout(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public MessageFileLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void show(ConversationMsg conversationMsg, int position) {
        this.conversationMsg = conversationMsg;
        this.position = position;
        msgDirection = conversationMsg.getMsgDirection();
        initWindow(mContext);
        updateProgress();
    }

    @Override
    protected int setLayoutId() {
        if (msgDirection == MessageDBConstant.IMVT_COM_MSG) {
            return R.layout.view_message_file_content_left;
        } else {
            return R.layout.view_message_file_content_right;
        }
    }

    @Override
    protected void initView(View view) {
        rl_relative = (RelativeLayout) view.findViewById(R.id.rl_relative);
        tv_name = (TextView) view.findViewById(R.id.tv_name);
        tv_size = (TextView) view.findViewById(R.id.tv_size);
        pbar_progress = (ProgressBar) view.findViewById(R.id.pbar_progress);
        iv_image = (ImageView) view.findViewById(R.id.iv_image);

        rl_relative.setOnLongClickListener(this);
        rl_relative.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        path = conversationMsg.getLocalImgPath();
        name = getFileName(path);
        tv_name.setText(name);

        // 获取后缀名，不带"."
        suffix = FileUtils.getSuffix(path);
        // 根据后缀名设置图片类型
        setFileTypeBackgroundResource(suffix);
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
                tv_size.setVisibility(View.GONE);
                pbar_progress.setVisibility(View.VISIBLE);
                offsize = conversationMsg.getOffSize();
                fileSize = conversationMsg.getFileSize();
                // 在数据插入数据库之后，上传文件之前，文件大小和偏移量可能为空
                pbar_progress.setProgress(FileUtils.getProgress(offsize, fileSize));
                break;
            case MessageDBConstant.MSG_STATUS_FAIL:
            case MessageDBConstant.MSG_STATUS_SEND_SUCCESS:
            case MessageDBConstant.FILE_STATUS_NOT_DOWNLOAD:
                pbar_progress.setVisibility(View.GONE);
                tv_size.setVisibility(View.VISIBLE);
                tv_size.setText(getFileSize(conversationMsg.getMsgUctId()));
                break;
            case MessageDBConstant.FILE_STATUS_RECEIVE_SUCCESS:
                pbar_progress.setVisibility(View.GONE);
                tv_size.setVisibility(View.VISIBLE);
                tv_size.setText(getFileSize(conversationMsg.getMsgUctId()));
                FileUtils.scanFile(mContext, conversationMsg.getLocalImgPath());
                break;
            default:
                break;
        }
    }

    private String getFileName(String path) {
        String name = "";
        if (!StrUtils.isEmpty(path)) {
            name = path.substring(path.lastIndexOf(File.separator) + 1, path.length());
        }
        return name;
    }

    private String getFileSize(String uctId) {
        String size = "";
        if (!StrUtils.isEmpty(uctId)) {
            try {
                int underlineCount = UctLibStringUtils.appearNumber(uctId, UctLibStringUtils.SPLIT_MARK_UNDERLINE);
                if (underlineCount == 3) {
                    size = FileUtils.FormatFileSize(FileUtils.UnformatFileSize(uctId.substring(uctId.lastIndexOf("_") + 1, uctId.length())));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return mContext.getResources().getString(R.string.string_message_unknown_size);
            }
        }
        if (StrUtils.isEmpty(size)) {
            if (conversationMsg.getMsgDirection() == MessageDBConstant.IMVT_COM_MSG && conversationMsg.getMsgStatus() == MessageDBConstant.FILE_STATUS_RECEIVE_SUCCESS) {
                try {
                    size = FileUtils.FormatFileSize(FileUtils.getFileSize(conversationMsg.getLocalImgPath()));
                    if (StrUtils.isEmpty(size)) {
                        return mContext.getResources().getString(R.string.string_message_unknown_size);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return mContext.getResources().getString(R.string.string_message_unknown_size);
                }
                return size;
            }
            return mContext.getResources().getString(R.string.string_message_unknown_size);
        } else {
            return size;
        }
    }

    private void setFileTypeBackgroundResource(String suffix) {
        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (!StrUtils.isEmpty(suffix) && suffix.equals(MIME_MapTable[i][0])) {
                switch (MIME_MapTable[i][2]) {
                    case FileUtils.EXCEL:
                        iv_image.setBackgroundResource(R.mipmap.icon_excel);
                        type = FileUtils.EXCEL;
                        break;
                    case FileUtils.WORD:
                        iv_image.setBackgroundResource(R.mipmap.icon_word);
                        type = FileUtils.WORD;
                        break;
                    case FileUtils.ZIP:
                        iv_image.setBackgroundResource(R.mipmap.icon_zip);
                        type = FileUtils.ZIP;
                        break;
                    default:
                        iv_image.setBackgroundResource(R.mipmap.icon_file_default);
                        type = FileUtils.OTHER;
                        break;
                }
                break;
            } else {
                iv_image.setBackgroundResource(R.mipmap.icon_file_default);
                type = FileUtils.OTHER;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_relative:
                if (msgDirection == MessageDBConstant.IMVT_COM_MSG) {
                    if (!SDCardUtils.isAvailableInternalMemory()
                            && (conversationMsg.getMsgStatus() == MessageDBConstant.MSG_STATUS_FAIL || conversationMsg.getMsgStatus() == MessageDBConstant.FILE_STATUS_NOT_DOWNLOAD)) {
                        ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.msg_msg_send_error_2), -1);
                        return;
                    }
                    Intent intent = new Intent(mContext, MessageFileDownloadActivity.class);
                    intent.putExtra("file_name", name);
                    intent.putExtra("file_path", path);
                    intent.putExtra("file_type", type);
                    intent.putExtra("file_conversationMsg", conversationMsg);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } else {
                    try {
                        Intent intent = FileUtils.openFile(path);
                        mContext.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtils.getToast().showMessageShort(mContext, "无法打开文件", -1);
                    }
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.rl_relative:
                new MessageOptionDialogManager(mContext, conversationMsg, position);
                break;
        }
        return true;
    }

    //    private String getFileSizeFromUctId(String uctId) {
    //        String size = "";
    //        if (!StrUtils.isEmpty(uctId)) {
    //            String[] a = uctId.split(":");
    //            String[] b = a[0].split("_");
    //            String c = b[b.length - 1];
    //            size = FileUtils.FormatFileSize(Long.parseLong(c));
    //        }
    //        return size;
    //    }
    //
    //    private String getFileNameFromUctId(String uctId) {
    //        String name = "";
    //        if (!StrUtils.isEmpty(uctId)) {
    //            String[] a = uctId.split(":");
    //            name = a[1];
    //        }
    //        return name;
    //    }

}
