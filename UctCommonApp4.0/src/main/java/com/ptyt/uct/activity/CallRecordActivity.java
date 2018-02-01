package com.ptyt.uct.activity;

import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.uct.service.UctClientApi;
import com.ptyt.uct.R;
import com.ptyt.uct.adapter.BaseRecyAdapter;
import com.ptyt.uct.adapter.CallRecordAdapter;
import com.ptyt.uct.common.SettingsConstant;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.entity.CallRecord;
import com.ptyt.uct.entity.GroupUser;
import com.ptyt.uct.model.CallRecordDBManager;
import com.ptyt.uct.utils.ActivitySkipUtils;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.ScreenUtils;
import com.ptyt.uct.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class CallRecordActivity extends BaseActionBarActivity implements View.OnClickListener, BaseRecyAdapter.OnItemClickListener {

    private CallRecordAdapter mAdapter;
    private List<CallRecord> allList = new ArrayList<>();
    private List<CallRecord> selectedList = new ArrayList<>();
    private RecyclerView recycler_view;
    private PopupWindow ppw_itemFunction;
    private PopupWindow ppw_itemFunction2;
    private int distanceY;
    private int distanceY2;
    private int currentPosition;
    private View view_parent;
    private TextView no_record_tv;
    private static final int QUERY_ALL_CALL_RECORD = 0;
    private static final int DELETE_SELECT_CALL_RECORD = 1;
    private boolean hasSelectAll = false;
    private TextView check_all_tv;
    private BitmapDrawable popBitmapDrawable1;
    private BitmapDrawable popBitmapDrawable2;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_call_record;
    }

    @Override
    protected void initWidget() {
        view_parent = findViewById(R.id.view_parent);
        no_record_tv = (TextView) findViewById(R.id.no_record_tv);
        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
        recycler_view.setHasFixedSize(true);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        tv_actionBarTitle.setText(getResources().getString(R.string.string_call_record_title));
        tv_actionBarRight.setVisibility(View.VISIBLE);
        tv_actionBarRight.setText(getResources().getString(R.string.string_call_record_editor));
        tv_actionBarRight.setOnClickListener(this);
        initItemFucWindow();
        initEditorFucWindow();
    }

    @Override
    protected void initData() {
        setNewMyLooper(true);
        selectedList.clear();
        mAdapter = new CallRecordAdapter(this);
        mAdapter.setOnItemClickListener(this);
        recycler_view.setAdapter(mAdapter);
    }

    private void initItemFucWindow() {
        View popView = View.inflate(mContext, R.layout.layout_dialog_user_fuction, null);
        popView.findViewById(R.id.iv_audio_dialog).setOnClickListener(this);
        popView.findViewById(R.id.iv_video_dialog).setOnClickListener(this);
        popView.findViewById(R.id.iv_message_dialog).setOnClickListener(this);
        popView.findViewById(R.id.iv_upload_dialog).setOnClickListener(this);
        // popView即popupWindow的布局，ture设置focusAble.
        ppw_itemFunction = new PopupWindow(popView, RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT, true);
        // 必须设置BackgroundDrawable后setOutsideTouchable(true)才会有效。这里在XML中定义背景，所以这里设置为null;
        popBitmapDrawable2 = new BitmapDrawable();
        ppw_itemFunction.setBackgroundDrawable(popBitmapDrawable2);
        // 点击外部关闭。
        ppw_itemFunction.setOutsideTouchable(true);
        // 设置一个动画。
        ppw_itemFunction.setAnimationStyle(android.R.style.Animation_Dialog);
        // 状态栏高 + actionbar高(y120)
        distanceY = ScreenUtils.getStatusHeight(this) + getResources().getDimensionPixelOffset(R.dimen.y120);
//        ppw_itemFunction.setOnDismissListener(new PopupWindow.OnDismissListener() {
//
//            @Override
//            public void onDismiss() {
//                WindowManager.LayoutParams lp = getWindow().getAttributes();
//                lp.alpha = 1f;
//                getWindow().setAttributes(lp);
//            }
//        });
    }

    private void initEditorFucWindow() {
        View popView = View.inflate(mContext, R.layout.layout_dialog_call_record_popup_window, null);
        check_all_tv = (TextView) popView.findViewById(R.id.check_all_tv);
        check_all_tv.setOnClickListener(this);
        popView.findViewById(R.id.delete_tv).setOnClickListener(this);
        // popView即popupWindow的布局，ture设置focusAble.
        ppw_itemFunction2 = new PopupWindow(popView, RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT, false);
        // 必须设置BackgroundDrawable后setOutsideTouchable(true)才会有效。这里在XML中定义背景，所以这里设置为null;
        popBitmapDrawable1 = new BitmapDrawable();
        ppw_itemFunction2.setBackgroundDrawable(popBitmapDrawable1);
        // 点击外部关闭。
        ppw_itemFunction2.setOutsideTouchable(false);
        // 设置一个动画。
        ppw_itemFunction2.setAnimationStyle(android.R.style.Animation_Dialog);
        // 偏移150
        distanceY2 = getResources().getDimensionPixelOffset(R.dimen.y150);
    }

    private void setNoRecordView() {
        recycler_view.setVisibility(View.GONE);
        no_record_tv.setVisibility(View.VISIBLE);
        tv_actionBarRight.setVisibility(View.GONE);
    }

    private void setHasRecordView() {
        no_record_tv.setVisibility(View.GONE);
        recycler_view.setVisibility(View.VISIBLE);
        tv_actionBarRight.setVisibility(View.VISIBLE);
    }

    private void enterEditorMode() {
        ppw_itemFunction2.showAtLocation(view_parent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, distanceY2);
        selectedList.clear();
        tv_actionBarRight.setText(getResources().getString(R.string.string_call_record_cancel));
        mAdapter.enterEditorMode(true);
        for (int i = 0; i < allList.size(); i++) {
            allList.get(i).setChecked(false);
        }
    }

    private void exitEditorMode() {
        if (ppw_itemFunction.isShowing()) {
            ppw_itemFunction.dismiss();
        }
        if (ppw_itemFunction2.isShowing()) {
            ppw_itemFunction2.dismiss();
        }
        selectedList.clear();
        tv_actionBarRight.setText(getResources().getString(R.string.string_call_record_editor));
        mAdapter.enterEditorMode(false);
        check_all_tv.setText(getResources().getString(R.string.string_call_record_select_all));
        hasSelectAll = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right1:
                if (mAdapter.getEditorMode()) {
                    exitEditorMode();
                } else {
                    enterEditorMode();
                }
                mAdapter.addAll(allList);
                break;
            case R.id.iv_audio_dialog://语音呼叫
                if(UctApplication.getInstance().isInGroupCall){//当前正在组呼时，不能发起语音呼叫
                    ToastUtils.getToast().showMessageShort(mContext,getString(R.string.gcalling_cannot_audio_call),-1);
                    return;
                }
                CallRecord record1 = mAdapter.getItem(currentPosition);
                GroupUser user1 = new GroupUser();
                user1.setUserName(record1.getName());
                user1.setUserTel(record1.getNumber());
                ActivitySkipUtils.intent2CallActivity(this, VideoCallActivity.class, ConstantUtils.AUDIO_SCALL, user1);
                break;
            case R.id.iv_message_dialog://信息
                CallRecord record2 = mAdapter.getItem(currentPosition);
                GroupUser user2 = new GroupUser();
                user2.setUserName(record2.getName());
                user2.setUserTel(record2.getNumber());
                ActivitySkipUtils.intent2CallActivity(this, MessageActivity.class, -1, user2);
                break;
            case R.id.iv_video_dialog://视频呼叫
                if(UctApplication.getInstance().isInGroupCall){//当前正在组呼时，不能发起视频呼叫
                    ToastUtils.getToast().showMessageShort(mContext,getString(R.string.gcalling_cannot_video_call),-1);
                    return;
                }
                CallRecord record3 = mAdapter.getItem(currentPosition);
                GroupUser user3 = new GroupUser();
                user3.setUserName(record3.getName());
                user3.setUserTel(record3.getNumber());
                ActivitySkipUtils.intent2CallActivity(this, VideoCallActivity.class, ConstantUtils.VIDEO_SCALL, user3);
                break;
            case R.id.iv_upload_dialog://上传视频
                CallRecord record4 = mAdapter.getItem(currentPosition);
                GroupUser user4 = new GroupUser();
                user4.setUserName(record4.getName());
                user4.setUserTel(record4.getNumber());
                ActivitySkipUtils.intent2CallActivity(this, VideoCallActivity.class, ConstantUtils.UPLOAD_VIDEO, user4);
                break;
            case R.id.check_all_tv:
                if (hasSelectAll) {// 取消全选
                    check_all_tv.setText(getResources().getString(R.string.string_call_record_select_all));
                    selectedList.clear();
                    for (int i = 0; i < allList.size(); i++) {
                        allList.get(i).setChecked(false);
                    }
                    hasSelectAll = false;
                } else {// 全选
                    check_all_tv.setText(getResources().getString(R.string.string_call_record_unselect_all));
                    selectedList.clear();
                    selectedList.addAll(allList);
                    for (int i = 0; i < allList.size(); i++) {
                        allList.get(i).setChecked(true);
                    }
                    hasSelectAll = true;
                }
                mAdapter.addAll(allList);
                break;
            case R.id.delete_tv:
                if (selectedList == null || selectedList.size() == 0) {
                    ToastUtils.getToast().showMessageShort(this, getResources().getString(R.string.prompt_call_record_select), -1);
                } else {
                    sendMsg(true, DELETE_SELECT_CALL_RECORD);
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onItemClick(int pos, View itemView) {
        currentPosition = pos;
        if (mAdapter.getEditorMode()) {
            if (mAdapter.getItem(pos).getChecked()) {
                mAdapter.getItem(pos).setChecked(false);
                selectedList.remove(mAdapter.getItem(pos));
                if (selectedList.size() < allList.size()) {
                    check_all_tv.setText(getResources().getString(R.string.string_call_record_select_all));
                    hasSelectAll = false;
                }
            } else {
                mAdapter.getItem(pos).setChecked(true);
                selectedList.add(mAdapter.getItem(pos));
                if (selectedList.size() == allList.size()) {
                    check_all_tv.setText(getResources().getString(R.string.string_call_record_unselect_all));
                    hasSelectAll = true;
                }
            }
            mAdapter.updateItem(pos, mAdapter.getItem(pos));
        } else {
//            WindowManager.LayoutParams lp = getWindow().getAttributes();
//            lp.alpha = 0.5f;//设置阴影透明度
//            getWindow().setAttributes(lp);
            int distanceX = getResources().getDimensionPixelOffset(R.dimen.x120);
            ppw_itemFunction.showAtLocation(view_parent, Gravity.RIGHT | Gravity.TOP, distanceX, distanceY + ((int) itemView.getY()));
        }

    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case QUERY_ALL_CALL_RECORD:
                allList = CallRecordDBManager.getInstance(this).queryCallRecordList();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (allList == null || allList.size() == 0) {
                            setNoRecordView();
                        } else {
                            setHasRecordView();
                            mAdapter.addAll(allList);
                        }
                    }
                });
                break;
            case DELETE_SELECT_CALL_RECORD:
                for (int i = 0; i < selectedList.size(); i++) {
                    CallRecordDBManager.getInstance(this).deleteCallRecord(selectedList.get(i));
                }
                allList = CallRecordDBManager.getInstance(this).queryCallRecordList();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        exitEditorMode();
                        mAdapter.addAll(allList);
                        if (allList == null || allList.size() == 0) {
                            setNoRecordView();
                        } else {
                            setHasRecordView();
                        }
                    }
                });
                break;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_CALL_RECORD_UNREAD, 0);
        exitEditorMode();
        sendMsg(true, QUERY_ALL_CALL_RECORD);
    }

    @Override
    protected void onDestroy() {
        popBitmapDrawable1 = null;
        popBitmapDrawable2 = null;
        if (ppw_itemFunction.isShowing()) {
            ppw_itemFunction.dismiss();
        }
        if (ppw_itemFunction2.isShowing()) {
            ppw_itemFunction2.dismiss();
        }
        super.onDestroy();
    }
}
