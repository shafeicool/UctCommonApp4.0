package com.ptyt.uct.fragment;


import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.view.View;

import com.ptyt.uct.R;
import com.ptyt.uct.activity.MainActivity;
import com.ptyt.uct.activity.MessageActivity;
import com.ptyt.uct.adapter.BaseRecyAdapter;
import com.ptyt.uct.adapter.MessageFuctionAdapter;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.entity.MessageFunctionEntity;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.utils.StrUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * @Description:
 * @Date: 2017/5/10
 * @Author: ShaFei
 * @Version: V1.0
 */
public class MessageFunctionFragment extends BaseGridFragment {

    private MessageFuctionAdapter mAdapter;
    private List<MessageFunctionEntity> list = null;
    private TypedArray icons = null;
    private String[] text = null;
    private String[] actions = null;
    private static final int REQUEST_CODE = 100;
    private MessageActivity messageActivity;
    private String msgSrcNo;
    private String msgDstNo;
    private Long conversationId;
    private String groupNo;
    private String groupName;
    private static final String MESSAGE_MAP = "android.intent.action.MESSAGE_MAP";
    private static final String MESSAGE_GROUP_CALL = "android.intent.action.MESSAGE_GROUP_CALL";
    private static final String MESSAGE_PHOTOGRAPH = "android.intent.action.MESSAGE_PHOTOGRAPH";
    private static final String MESSAGE_RECORD = "android.intent.action.MESSAGE_RECORD";
    private String mPath;

    public MessageFunctionFragment() {
        // Required empty public constructor
    }

    @Override
    protected BaseRecyAdapter getAdapter() {
        mAdapter = new MessageFuctionAdapter(context);
        return mAdapter;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        swipeRefreshLayout.setEnabled(false);
        list = new ArrayList<>();
        messageActivity = (MessageActivity) getActivity();
        if (messageActivity.isGroupNo()) {
            icons = getResources().obtainTypedArray(R.array.gmessage_select_file_icons);
            text = getResources().getStringArray(R.array.gmessage_select_file_text);
            actions = getResources().getStringArray(R.array.gmessage_select_file_action);
        } else {
            icons = getResources().obtainTypedArray(R.array.smessage_select_file_icons);
            text = getResources().getStringArray(R.array.smessage_select_file_text);
            actions = getResources().getStringArray(R.array.smessage_select_file_action);
        }
        for (int i = 0; i < icons.length(); i++) {
            MessageFunctionEntity sf = new MessageFunctionEntity(text[i], icons.getResourceId(i, 0), actions[i]);
            list.add(sf);
        }
        if (list != null && list.size() > 0) {
            mAdapter.addAll(list);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onItemClick(int pos, View itemView) {
        super.onItemClick(pos, itemView);
        String action = mAdapter.getItem(pos).getAction();
        if (action.equals(MESSAGE_MAP)) {
            /* ptyt begin, 统一eventbus发送的都是eventbean对象_4053_shafei_20170906 */
            EventBean bean = new EventBean(ConstantUtils.ACTION_MESSAGE_TO_MAP);
            EventBus.getDefault().post(bean);
            Intent intent = new Intent(messageActivity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            messageActivity.finish();
            /* ptyt end */
        } else if (action.equals(MESSAGE_GROUP_CALL)) {
            EventBean bean = new EventBean(ConstantUtils.ACTION_TAB_TO_GCALL);
            bean.setGroupId(groupNo);
            bean.setGroupName(groupName);
            EventBus.getDefault().post(bean);
            messageActivity.finish();
        } else if (action.equals(MESSAGE_PHOTOGRAPH)) {// Android5.0以下(不包含)使用系统照相机拍照
            mPath = SDCardUtils.getChatRecordPath(conversationId, msgSrcNo + "_" + msgDstNo) + StrUtils.getSmsId(msgDstNo, msgSrcNo) + ".jpg";
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri uri = Uri.fromFile(new File(mPath));
            intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, this.REQUEST_CODE);
        } else if (action.equals(MESSAGE_RECORD)) {// Android5.0以下(不包含)使用系统照相机录像
            mPath = SDCardUtils.getChatRecordPath(conversationId, msgSrcNo + "_" + msgDstNo) + StrUtils.getSmsId(msgDstNo, msgSrcNo) + ".mp4";
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            Uri uri = Uri.fromFile(new File(mPath));
            intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, this.REQUEST_CODE);
        }
//        else if (action.equals("android.intent.action.MESSAGE_SHOOT")) {// Android5.0以下(不包含)使用系统照相机录像
//            mPath = SDCardUtils.getChatRecordPath(conversationId, msgSrcNo + "_" + msgDstNo) + StrUtils.getSmsId(msgDstNo, msgSrcNo) + ".jpg";
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            Uri uri = Uri.fromFile(new File(mPath));
//            intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//            intent.putExtra("return-data", true);
//            startActivityForResult(intent, this.REQUEST_CODE);
//        }
        else {
            Intent intent = new Intent();
            intent.putExtra("msgSrcNo", msgSrcNo);
            intent.putExtra("msgDstNo", msgDstNo);
            intent.putExtra("conversationId", conversationId);
            intent.setAction(action);
            //            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 一定不能加这句，否则startActivityForResult不生效
            startActivityForResult(intent, this.REQUEST_CODE);
        }
    }

    public void setSrcNo(String msgSrcNo) {
        this.msgSrcNo = msgSrcNo;
    }

    public void setDstNo(String msgDstNo) {
        this.msgDstNo = msgDstNo;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public void setGroupNo(String groupNo) {
        this.groupNo = groupNo;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPath() {
        return mPath;
    }
}
