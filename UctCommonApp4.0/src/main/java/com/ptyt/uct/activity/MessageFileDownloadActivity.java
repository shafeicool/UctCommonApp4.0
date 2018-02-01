package com.ptyt.uct.activity;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.services.BaseServiceCallBack;
import com.ptyt.uct.services.MessageManager;
import com.ptyt.uct.services.MsgBinder;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;

import static com.ptyt.uct.services.MessageManager.getInstane;

public class MessageFileDownloadActivity extends BaseActionBarActivity implements View.OnClickListener, MsgBinder.MsgCallBackListener {

    private LinearLayout ll_progress;
    private TextView tv_name, tv_loading, tv_download, tv_download_finish;
    private ImageView iv_image, iv_cancel;
    private ProgressBar pbar_progress;

    private ConversationMsg conversationMsg;
    private String path;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_message_file_download;
    }

    @Override
    protected void initWidget() {
        PrintLog.i("registerObserver BaseServiceCallBack.INDEX_IMESSAGEVIEW");
        getInstane().registerObserver(this, BaseServiceCallBack.INDEX_IMESSAGEVIEW);
        ll_progress = (LinearLayout) findViewById(R.id.ll_progress);
        iv_image = (ImageView) findViewById(R.id.iv_image);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_download = (TextView) findViewById(R.id.tv_download);
        pbar_progress = (ProgressBar) findViewById(R.id.pbar_progress);
        iv_cancel = (ImageView) findViewById(R.id.iv_cancel);
        tv_loading = (TextView) findViewById(R.id.tv_loading);
        tv_download_finish = (TextView) findViewById(R.id.tv_download_finish);
        tv_actionBarTitle.setText(getResources().getString(R.string.string_file_download_start));

        tv_download.setOnClickListener(this);
        iv_cancel.setOnClickListener(this);
    }
    @Override
    protected void initData() {
        String name = getIntent().getStringExtra("file_name");
        path = getIntent().getStringExtra("file_path");
        String type = getIntent().getStringExtra("file_type");
        conversationMsg = (ConversationMsg) getIntent().getSerializableExtra("file_conversationMsg");

        tv_name.setText(name);
        switch (type) {
            case FileUtils.EXCEL:
                iv_image.setBackgroundResource(R.mipmap.icon_excel);
                break;
            case FileUtils.WORD:
                iv_image.setBackgroundResource(R.mipmap.icon_word);
                break;
            case FileUtils.ZIP:
                iv_image.setBackgroundResource(R.mipmap.icon_zip);
                break;
            default:
                iv_image.setBackgroundResource(R.mipmap.icon_file_default);
                break;
        }

        if (!StrUtils.isEmpty(conversationMsg.getLocalImgPath())) {
            PrintLog.i("initData status = " + conversationMsg.getMsgStatus());
            if (conversationMsg.getMsgStatus() == MessageDBConstant.MSG_STATUS_SEND_SUCCESS || conversationMsg.getMsgStatus() == MessageDBConstant.FILE_STATUS_RECEIVE_SUCCESS) {
                finishDownload(true);
                finish();
                try {
                    Intent intent = FileUtils.openFile(path);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.getToast().showMessageShort(this, mContext.getString(R.string.string_file_download_error1), -1);
                }
            }
            if (conversationMsg.getMsgStatus() == MessageDBConstant.FILE_STATUS_WAIT_RECEIVING || conversationMsg.getMsgStatus() == MessageDBConstant.FILE_STATUS_RECEIVING) {
                showProgressUI();
            }
            if (conversationMsg.getMsgStatus() == MessageDBConstant.MSG_STATUS_FAIL) {
                finishDownload(false);
            }
            if (conversationMsg.getMsgStatus() == MessageDBConstant.FILE_STATUS_NOT_DOWNLOAD) {
                initDownloadUI();
            }
//            else {
//                MessageManager.getInstane().downloadMessage(conversationMsg);
//                showProgressUI();
//            }
        } else {
            ToastUtils.getToast().showMessageShort(this, mContext.getString(R.string.string_file_download_error2), -1);
        }
    }

    private void initDownloadUI() {
        tv_download_finish.setVisibility(View.GONE);
        tv_download.setVisibility(View.VISIBLE);
        tv_download.setText(getResources().getString(R.string.string_file_download_start));
        ll_progress.setVisibility(View.GONE);
        tv_loading.setVisibility(View.GONE);
    }

    private void showProgressUI() {
        tv_download.setVisibility(View.GONE);
        ll_progress.setVisibility(View.VISIBLE);
        tv_loading.setVisibility(View.VISIBLE);
        tv_download_finish.setVisibility(View.GONE);
    }

//    private void dismissProgressUI() {
//        ll_progress.setVisibility(View.GONE);
//        tv_loading.setVisibility(View.GONE);
//        tv_download.setVisibility(View.VISIBLE);
//        tv_download.setText(getResources().getString(R.string.string_file_download_restart));
//    }

    private void finishDownload(final boolean isSuccessed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ll_progress.setVisibility(View.GONE);
                tv_loading.setVisibility(View.GONE);
                tv_download_finish.setVisibility(View.VISIBLE);
                if (isSuccessed) {
                    tv_download.setVisibility(View.GONE);
                    tv_download_finish.setText(getResources().getString(R.string.string_file_download_success));
                } else {
                    pbar_progress.setProgress(0);
                    tv_loading.setText(String.format(getResources().getString(R.string.string_file_download_loading), 0));
                    tv_download.setVisibility(View.VISIBLE);
                    tv_download.setText(getResources().getString(R.string.string_file_download_restart));
                    tv_download_finish.setText(getResources().getString(R.string.string_file_download_fail));
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_download:
                int result = MessageManager.getInstane().downloadMessage(conversationMsg);
                if (result == 0) {
                    showProgressUI();
                } else {
                    finishDownload(false);
                }
                break;
            case R.id.iv_cancel:
                MessageManager.getInstane().cancelMsg(conversationMsg);
//                dismissProgressUI();
                break;
        }
    }

    @Override
    public int notifyDataChangedListener(ConversationMsg mConversationMsg, int type) {
        if (conversationMsg != null
                && conversationMsg.getMsgDirection() == MessageDBConstant.IMVT_COM_MSG
                && mConversationMsg.getMsgUctId().equals(conversationMsg.getMsgUctId())) {
            switch (type) {
                case MsgBinder.MsgCallBackListener.MSG_SEND_INSERT_DB:
                    break;
                case MsgBinder.MsgCallBackListener.MSG_STATUS_CHANGE:
                    PrintLog.i("MSG_STATUS_CHANGE--status = " + mConversationMsg.getMsgStatus());
                    switch (mConversationMsg.getMsgStatus()) {
                        case MessageDBConstant.MSG_STATUS_FAIL:
                            finishDownload(false);
                            break;
                        case MessageDBConstant.MSG_STATUS_SEND_SUCCESS:
                        case MessageDBConstant.FILE_STATUS_RECEIVE_SUCCESS:
                            finishDownload(true);
                            finish();
                            try {
                                Intent intent = FileUtils.openFile(path);
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastUtils.getToast().showMessageShort(this, mContext.getString(R.string.string_file_download_error1), -1);
                            }
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
                                int progress = FileUtils.getProgress(offsize, fileSize);
                                PrintLog.i("MSG_UPDATE_PROGRESS--progress = " + progress);
                                showProgressUI();
                                pbar_progress.setProgress(progress);
                                tv_loading.setText(String.format(getResources().getString(R.string.string_file_download_loading), progress));
                            }
                        });

                    }
                    break;
            }
        }
        return 0;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        PrintLog.i("unRegisterObserver BaseServiceCallBack.INDEX_IMESSAGEVIEW");
        getInstane().unRegisterObserver(this, BaseServiceCallBack.INDEX_IMESSAGEVIEW);
        super.onDestroy();
    }
}
