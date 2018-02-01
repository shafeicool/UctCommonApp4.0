package com.ptyt.uct.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.android.uct.CreateGroupCfmListener;
import com.android.uct.DeleteGroupCfmListener;
import com.android.uct.PtytPreferenceChangeListener;
import com.android.uct.bean.GroupData;
import com.android.uct.exception.UctLibException;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.GMemberListActivity;
import com.ptyt.uct.activity.MainActivity;
import com.ptyt.uct.activity.MessageActivity;
import com.ptyt.uct.adapter.BaseRecyAdapter;
import com.ptyt.uct.adapter.CallListAdapter;
import com.ptyt.uct.callback.GCallCallback;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.SettingsConstant;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.entity.Group;
import com.ptyt.uct.model.GroupDBManager;
import com.ptyt.uct.utils.ActivitySkipUtils;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.NetUtils;
import com.ptyt.uct.utils.ScreenUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.viewinterface.IGroupInfoView;
import com.ptyt.uct.widget.CommonPromptDialog;
import com.ptyt.uct.widget.DividerItemDecoration;
import com.ptyt.uct.widget.PTYTCallWindowView;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

import static com.ptyt.uct.common.SettingsConstant.SETTINGS_LOCK_GROUP;
import static com.ptyt.uct.utils.ConstantUtils.ACTION_TAB_TO_GCALL;


/**
 * @Description:
 * @Date: 2017/5/9
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class CallFragment extends BasePermissionFragment implements IGroupInfoView, View.OnClickListener,PtytPreferenceChangeListener {

    private RecyclerView recyclerView;
    private CallListAdapter callAdapter;
    private PopupWindow ppw_itemFunction;
    private Context context;
    private GCallCallback gCallCallback;
    //当前点击的item位置
    private int currentItemPosition;
    private LinearLayoutManager layoutManager;
    //组呼框是否在全屏状态
    private boolean isFullScreen = false;
    private PTYTCallWindowView callWindowView;
    private View parentView;
    private int screenHeight;
    private boolean isShowing = false;
    private ImageView iv_delete_group;
    private BitmapDrawable popBitmapDrawable1;
    private ImageView iv_lock_dialog;

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEvent(EventBean eventBean) {
        String action = eventBean.getAction();
        boolean isInGroupCall = UctApplication.getInstance().isInGroupCall;
        if (action.equals(ConstantUtils.ACTION_GROUP_INFO_REFRESH)) {//重新登录|删除、增加、修改组时都会引起组信息变化
            this.groupDatas = GroupDBManager.getInstance().queryGroupList();
            if (groupDatas != null && groupDatas.size() > 0) {
                callAdapter.addAll(groupDatas);
                PrintLog.i("callWindowView.currentGroupData="+callWindowView.currentGroupData + "   gCallCallback.currentGroupId="+gCallCallback.currentGroupId);
                if(!isInGroupCall){
                    //先要判断有没有锁定组
                    String oldLockGroupName = (String) UctClientApi.getUserData(SETTINGS_LOCK_GROUP, "");
                    if(!TextUtils.isEmpty(oldLockGroupName)){//有锁定组
                        doLockGroupLogic(2,null);
                    }else{
                        callWindowView.setData(this.groupDatas.get(0));
                        gCallCallback.currentGroupId = this.groupDatas.get(0).getGrouTel();
                    /*ptyt start 解决删除临时组，组呼界面和组呼缩略框仍显示该组信息_#4905_kechuanqi_20171214*/
                        if (!TextUtils.isEmpty(gCallCallback.currentGroupId)) {
                            UctApplication.getInstance().getGroupCallWindow().speakState(IGroupInfoView.NO_GROUP_CALL, gCallCallback.currentGroupId, "");
                        }
                    /*ptyt end*/
                    }
                }
            }
        } else if (action.equals(ACTION_TAB_TO_GCALL)) {
            if (!isInGroupCall) {
                String groupId = eventBean.getGroupId();
                if (!TextUtils.isEmpty(groupId)) {
                    String groupName = eventBean.getGroupName();
                    Group currentGroupData = new Group();
                    currentGroupData.setGroupName(groupName);
                    currentGroupData.setGrouTel(groupId);
                    callWindowView.setData(currentGroupData);
                }
            }
            //在组呼主界面时点击悬浮框，悬浮框展开;
            /*start  modify by KeChuanqi in 20170926*/
            callWindowView.viewExtent(recyclerView, true);
            UctApplication.getInstance().getGroupCallWindow().hidePopupWindow();
            if(ppw_itemFunction.isShowing()){
                ppw_itemFunction.dismiss();
            }
            /*end */
        }
    }

    @Override
    protected int setLayoutId() {
        return R.layout.fragment_call;
    }

    @Override
    protected void initView(View view) {
        context = getActivity();
        callWindowView = ((PTYTCallWindowView) view.findViewById(R.id.callWindowView));
        parentView = view.findViewById(R.id.parentView);
        recyclerView = ((RecyclerView) view.findViewById(R.id.recyclerView));
        //固定RecyclerView的大小
        recyclerView.setHasFixedSize(true);
        //设置RecyclerView的分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
        //设置布局管理器
        layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        callAdapter = new CallListAdapter(context);
        recyclerView.setAdapter(callAdapter);
        initItemFucWindow();
        EventBus.getDefault().register(this);
        //配置信息改变监听
        UctClientApi.registerObserver(this, PtytPreferenceChangeListener.PTYTPREFERENCECHANGELISTENER);
        UctClientApi.registerObserver(deleteGroupCfmListener, CreateGroupCfmListener.DELETEGROUPCFMLISTENER_INDEX);
    }

    /**
     * 初始化功能展开的popupWindow的view
     */
    private void initItemFucWindow() {
        View popView = View.inflate(context, R.layout.dialog_group_fuc_option, null);
        popView.findViewById(R.id.iv_group_people_dialog).setOnClickListener(this);
        popView.findViewById(R.id.iv_location_dialog).setOnClickListener(this);
        popView.findViewById(R.id.iv_message_dialog).setOnClickListener(this);
        iv_lock_dialog = (ImageView) popView.findViewById(R.id.iv_lock_dialog);
        iv_lock_dialog.setOnClickListener(this);
        iv_delete_group = (ImageView) popView.findViewById(R.id.iv_delete_group);
        iv_delete_group.setOnClickListener(this);
        //popView即popupWindow的布局，ture设置focusAble.
        ppw_itemFunction = new PopupWindow(popView, RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT, true);
        //必须设置BackgroundDrawable后setOutsideTouchable(true)才会有效。这里在XML中定义背景，所以这里设置为null;
        popBitmapDrawable1 = new BitmapDrawable();
        ppw_itemFunction.setBackgroundDrawable(popBitmapDrawable1);
        //点击外部关闭。
        ppw_itemFunction.setOutsideTouchable(true);
        //设置一个动画。
        ppw_itemFunction.setAnimationStyle(android.R.style.Animation_Dialog);
        //设置Gravity，让它显示在右上角。
        screenHeight = ScreenUtils.getScreenHeight(context);
    }

    /**
     * 判断是否是锁定组
     * @return
     */
    private boolean judgeLockGroup(String groupNumber){
        //判断当前组是否已锁定
        String oldLockGroupName = (String) UctClientApi.getUserData(SETTINGS_LOCK_GROUP, "");
        if(!TextUtils.isEmpty(oldLockGroupName)) {
            String[] lockGroup = oldLockGroupName.split(",");
            for (int i = 0; i < lockGroup.length; i++) {
                if (groupNumber.equals(lockGroup[i])) {//当前是锁定组
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 处理锁定组逻辑
     */
    private void doLockGroupLogic(int logicType,String groupNumber){
        String oldLockGroupName = (String) UctClientApi.getUserData(SETTINGS_LOCK_GROUP, "");
        switch (logicType){
            case 0://从锁定组字符串移除
                boolean isLockGroup = judgeLockGroup(groupNumber);
                if(isLockGroup){
                    String newLockGroupName = StrUtils.getString(oldLockGroupName, groupNumber + SettingsConstant.LOCK_GROUP_REGULAREXPRESSION);
                    UctClientApi.saveUserData(SETTINGS_LOCK_GROUP, newLockGroupName);
                    UctClientApi.lockGroup(newLockGroupName);
                }
                break;
            case 1://添加至锁定组字符串
                boolean isLockGroup2 = judgeLockGroup(groupNumber);
                String newLockGroupName;
                if(isLockGroup2){//是锁定组就解锁(移除字符串该组),否则添加
                    newLockGroupName = StrUtils.getString(oldLockGroupName, groupNumber + SettingsConstant.LOCK_GROUP_REGULAREXPRESSION);
                } else {
                    newLockGroupName = oldLockGroupName + groupNumber + SettingsConstant.LOCK_GROUP_REGULAREXPRESSION;
                }
                UctClientApi.saveUserData(SETTINGS_LOCK_GROUP, newLockGroupName);
                UctClientApi.lockGroup(newLockGroupName);
                callAdapter.notifyDataSetChanged();
                //组呼框锁状态修改
                callWindowView.notifyLockGroupChanged(groupNumber);
                break;
            case 2://锁定组变化时,更换组显示
                if(!TextUtils.isEmpty(oldLockGroupName)){//有锁定组时,只能切换锁定组
                    String[] lockGroup = oldLockGroupName.split(",");
                    for (int i = 0; i < lockGroup.length; i++) {
                        String groupId = lockGroup[i];
                        for (int j = 0; j < CallFragment.this.groupDatas.size(); j++) {
                            Group group = CallFragment.this.groupDatas.get(j);
                            if(groupId.equals(group.getGrouTel())){//在groupDatas中找到该group对象
                                callWindowView.setData(group);
                                gCallCallback.currentGroupId = group.getGrouTel();
                                UctApplication.getInstance().getGroupCallWindow().speakState(IGroupInfoView.NO_GROUP_CALL,group.getGrouTel(),"");
                                break;
                            }
                        }
                    }
                }
                break;
            case 3://展开功能菜单，根据是否锁定组来显示锁开关状态
                String lockGroups = (String) UctClientApi.getUserData(SETTINGS_LOCK_GROUP, "");
                String[] lockGroup = lockGroups.split(",");
                boolean isFind = false;
                for (int i = 0; i < lockGroup.length; i++) {
                    String s = lockGroup[i];
                    if(groupNumber.equals(s)){
                        isFind = true;
                        iv_lock_dialog.setImageResource(R.drawable.selector_dialog_unlock);
                        break;
                    }
                }
                if (!isFind){
                    iv_lock_dialog.setImageResource(R.drawable.selector_dialog_lock);
                }
                break;
        }
    }

    private void showDeleteTempGroupDialog(final String groupNumber, final String loginUser) {
        if (isShowing) {
            return;
        }
        isShowing = true;
        CommonPromptDialog.Builder builder = new CommonPromptDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.string_delete_group_dialog_title));
        builder.setMessage(context.getResources().getString(R.string.string_delete_group_dialog_message));
        builder.setPositiveButton(context.getResources().getString(R.string.string_delete_group_dialog_confim), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                isShowing = false;
                int result = UctClientApi.UctDeleteGroupRequest(loginUser, groupNumber);
                PrintLog.i("UctDeleteGroupRequest result = " + result);
                if (result != 0) {
                    ToastUtils.getToast().showMessageShort(context, context.getString(R.string.string_delete_group_fail) + " result = " + result, -1);
                }else{
                    doLockGroupLogic(0,groupNumber);
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(context.getResources().getString(R.string.string_delete_group_dialog_cancel), new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                isShowing = false;
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * @description  删除组响应
     * @param        result 0-删除成功 非0-删除失败
     * @param        groupDn 删除当前组的组号码
     * @param        dn 删除当前组的用户号码
     * @return       0-调用成功 -1-调用失败
     */
    private DeleteGroupCfmListener deleteGroupCfmListener = new DeleteGroupCfmListener() {
        @Override
        public int UCT_DeleteGroupCfm(int result, String groupDn, String dn) throws UctLibException {
            if (result == 0) {
                PrintLog.i("删除组成功 [result=" + result + ", groupDn=" + groupDn + ", dn=" + dn + "]");
            } else {
                PrintLog.i("删除组失败 [result=" + result + ", groupDn=" + groupDn + ", dn=" + dn + "]");
                ToastUtils.getToast().showMessageShort(context, mContext.getString(R.string.string_main_delete_group_fail) + result, -1);
            }
            return 0;
        }
    };

    /* ptyt begin, 区分组列表和组呼框中的功能菜单_kechuanqi_20171214 */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        Group selectGroup = null;
        ppw_itemFunction.dismiss();
        if(isGroupCallWindowPPW){//组呼框内的展开菜单
            selectGroup = callWindowView.currentGroupData;
        }else{
            if(groupDatas != null && groupDatas.size() > currentItemPosition){
                selectGroup = groupDatas.get(currentItemPosition);
            }
        }
        if (selectGroup == null){return;}
        switch (v.getId()) {
            case R.id.iv_group_people_dialog://组
                ActivitySkipUtils.intent2GMemberListActivity(getActivity(), GMemberListActivity.class, selectGroup);
                break;
            case R.id.iv_location_dialog://定位:切换到地图,同时显示当前组数据
                ((MainActivity) getActivity()).getViewPager().setCurrentItem(2);
                ((MainActivity) getActivity()).getMapFragment().showGroupData(selectGroup);
                callWindowView.dealWithWindowFullScreenCancel();
                break;
            case R.id.iv_lock_dialog://锁定组
                if(!NetUtils.isNetworkAvailable(mContext)){
                    ToastUtils.getToast().showMessageShort(mContext,getString(R.string.net_error),-1);
                    return;
                }
                if(UctApplication.getInstance().isInGroupCall){//组呼中
                    ToastUtils.getToast().showMessageShort(mContext,getString(R.string.group_calling_no_lock),-1);
                    return;
                }
                doLockGroupLogic(1,selectGroup.getGrouTel());
                break;
            case R.id.iv_message_dialog://信息
                ActivitySkipUtils.intent2GMemberListActivity(getActivity(), MessageActivity.class, selectGroup);
                break;
            case R.id.iv_delete_group://删除组
                if(!NetUtils.isNetworkAvailable(mContext)){
                    ToastUtils.getToast().showMessageShort(mContext,getString(R.string.net_error),-1);
                    return;
                }
                /*ptyt start,修改为在组呼时，不能删除组_kechuanqi_20171014*/
                if(!UctApplication.getInstance().isInGroupCall){
                    showDeleteTempGroupDialog(selectGroup.getGrouTel(), AppContext.getAppContext().getLoginNumber());
                }else{
                    if(!gCallCallback.currentGroupId.equals(selectGroup.getGrouTel())){
                        showDeleteTempGroupDialog(selectGroup.getGrouTel(), AppContext.getAppContext().getLoginNumber());
                    }else {
                        ToastUtils.getToast().showMessageShort(mContext,getString(R.string.gcalling_can_not_delete_group),-1);
                    }
                }
                /*ptyt end*/
                break;
        }
    }
    /*ptyt end*/

    @Override
    protected void initData() {
        //组呼
        gCallCallback = GCallCallback.getInstance();
        gCallCallback.init(context, this);
        try {
            this.groupDatas = GroupDBManager.getInstance().queryGroupList();
        }catch (Exception e){
            e.printStackTrace();
        }
        if (groupDatas != null && groupDatas.size() > 0) {
            gCallCallback.currentGroupId = this.groupDatas.get(0).getGrouTel();
            callAdapter.addAll(groupDatas);
            callWindowView.setData(this.groupDatas.get(0));
        }
        callWindowView.init();
    }

    @Override
    public void onStart() {
        PrintLog.i("onStart()");
        super.onStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        PrintLog.i("onResume()");
        super.onResume();
        /* ptyt begin,避免onResume时，其他tab如信息·地图时显示，4374_KeChuanqi_20171017*/
        if (PTYTCallWindowView.isExtentMode == false && MainActivity.currentFragmentPosition == 1) {
            UctApplication.getInstance().getGroupCallWindow().show(null);
        } else if (MainActivity.currentFragmentPosition == 1) {
            UctApplication.getInstance().getGroupCallWindow().hidePopupWindow();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        PrintLog.i("-----------CallFragment.onStop()");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PrintLog.i("-----------CallFragment.onDestroyView()");
        popBitmapDrawable1 = null;
        gCallCallback.release();
        EventBus.getDefault().unregister(this);
        UctClientApi.unregisterObserver(this, PtytPreferenceChangeListener.PTYTPREFERENCECHANGELISTENER);
        UctClientApi.unregisterObserver(deleteGroupCfmListener, CreateGroupCfmListener.DELETEGROUPCFMLISTENER_INDEX);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PrintLog.i("onDestroy()");
    }

    /**
     * 组呼状态改变
     *
     * @param state
     * @param cTalkingGID
     */
    @Override
    public void gCallStateChanged(int state, String groupId, String cTalkingGID) {
        PrintLog.i("groupId=" + groupId);
        callWindowView.setGCallState(state, groupId, cTalkingGID);
        if(state == SELF_SPEAK){
            if(ppw_itemFunction.isShowing()){
                ppw_itemFunction.dismiss();
            }
        }
    }

    /**
     * 订阅加载数据
     */
    private List<Group> groupDatas = new ArrayList<>();

    /**
     * 组列表
     *
     * @param groupList
     */
    @Override
    public void onLoadSucceed(List<GroupData> groupList) {
    }

    /**
     * 组列表刷新,初始化
     *
     * @param groupList
     */
    @Override
    public void onRefresh(List<GroupData> groupList) {
        PrintLog.i("CallFragment   onRefresh()");
    }

    private View iv_pullUp;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void initEvent() {
        super.initEvent();
        //展开
        callAdapter.setOnUnfoldClickListener(new CallListAdapter.OnUnfoldClickListener() {
            @Override
            public void onClick(View view, View itemView, int position) {
                iv_pullUp = view;
                currentItemPosition = position;
                int ppwLocationX = getActivity().getResources().getDimensionPixelOffset(R.dimen.x45);
                //状态栏高度+actionBar = 50+96+78
                int ppwLocationDisY;
                if (((MainActivity)getActivity()).getOfflineView() == View.VISIBLE) {
                    ppwLocationDisY = getActivity().getResources().getDimensionPixelOffset(R.dimen.y344);
                } else {
                    ppwLocationDisY = getActivity().getResources().getDimensionPixelOffset(R.dimen.y224);
                }
                if (groupDatas != null && groupDatas.size() > currentItemPosition) {
                    Group groupData = groupDatas.get(currentItemPosition);
                    String groupCreateUser = groupData.getGroupCreateUser();
                    if (AppContext.getAppContext().getLoginNumber().equals(groupCreateUser)) {
                        iv_delete_group.setVisibility(View.VISIBLE);
                    } else {
                        iv_delete_group.setVisibility(View.GONE);
                    }
                    //锁定组判断
                    doLockGroupLogic(3,groupData.getGrouTel());
                }
                ppw_itemFunction.showAtLocation(recyclerView, Gravity.RIGHT | Gravity.TOP, ppwLocationX, ppwLocationDisY + ((int) itemView.getY()));
                ((ImageView) view).setImageResource(R.mipmap.ic_pull_up);
                isGroupCallWindowPPW = false;
            }
        });
        /**
         * popupWindow取消时监听
         */
        ppw_itemFunction.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (iv_pullUp != null) {
                    ((ImageView) iv_pullUp).setImageResource(R.mipmap.ic_pull_down);
                }
                //告诉PPYTCallWindowView 已取消
                callWindowView.setOnWindowDismiss();
            }
        });
        /**
         * item点击事件
         */
        callAdapter.setOnItemClickListener(new BaseRecyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos, View itemView) {
                Group currentGroupData = callAdapter.getItem(pos);
                if (!UctApplication.getInstance().isInGroupCall) {//不在组呼中
                    String lockGroups = (String) UctClientApi.getUserData(SETTINGS_LOCK_GROUP, "");
                    if(!TextUtils.isEmpty(lockGroups) && !judgeLockGroup(currentGroupData.getGrouTel())){
                        ToastUtils.getToast().showMessageShort(mContext,getString(R.string.lock_group_no_switch_group),-1);
                    } else{
                        callWindowView.setData(currentGroupData);
                        gCallCallback.currentGroupId = currentGroupData.getGrouTel();
                        UctApplication.getInstance().getGroupCallWindow().speakState(IGroupInfoView.NO_GROUP_CALL,currentGroupData.getGrouTel(),"");
                    }
                }else{
                    if(!currentGroupData.getGrouTel().equals(gCallCallback.currentGroupId)){
                        ToastUtils.getToast().showMessageShort(mContext,getString(R.string.group_calling_no_switch_group),-1);
                    }
                }
            }
        });
        /**中
         * 滚动
         */
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if(AbsListView.OnScrollListener.SCROLL_STATE_IDLE == newState){
                    callWindowView.isExtending = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (dy > 40 && firstVisibleItemPosition > 0) {//上滑且滑动速度 > 30时，组呼window回收
                    if(!callWindowView.isExtending){
                        callWindowView.isExtending = true;
                        callWindowView.viewExtent(recyclerView, false);
                        if (!TextUtils.isEmpty(gCallCallback.currentGroupId)) {
                            UctApplication.getInstance().getGroupCallWindow().speakState(IGroupInfoView.NO_GROUP_CALL, gCallCallback.currentGroupId, "");
                        }
                    }
                }
            }
        });

        /**
         * tab切换至callFragment时调用,用于在全屏时隐藏导航栏
         */
        ((MainActivity) context).setOnTabChangeListener(new MainActivity.OnTabChangeListener() {
            @Override
            public void onChanged() {
                if (isFullScreen) {
                    ((MainActivity) context).showCardView(false);
                }
            }
        });
        /**
         * 设置组呼框全屏与否的监听
         */
        callWindowView.setOnWindowScreenChangeListener(new PTYTCallWindowView.OnWindowScreenChangeListener() {
            @Override
            public void onChanged(boolean isFullscreen) {
                if (isFullscreen) {
                    ((MainActivity) context).showCardView(false);
                } else {
                    ((MainActivity) context).showCardView(true);
                }
            }
        });

        /**
         * ACTION_DOWN与ACTION_UP and 挂断
         */
        callWindowView.setOnGroupCallListener(new PTYTCallWindowView.OnGroupCallListener() {
            @Override
            public void startGroupCall(String groupId) {//按下
                pressGroupId = groupId;
                startReqPermOfRecordAudio();
            }

            @Override
            public void releaseGCallReq() {//抬起
                gCallCallback.releaseGCallReq();
            }

            @Override
            public void onHangup() {
                gCallCallback.hangUpCall();
            }
        });

        /**
         * 组呼窗口内"展开^"监听
         */
        callWindowView.setOnGroupFucUnfoldListener(new PTYTCallWindowView.OnGroupFucUnfoldListener() {
            @Override
            public void onClick(View view) {
                iv_delete_group.setVisibility(View.GONE);
                //锁定组判断
                doLockGroupLogic(3,callWindowView.currentGroupData.getGrouTel());
                int ppwLocationX = getActivity().getResources().getDimensionPixelOffset(R.dimen.x45);
                //12:阴影高度 18:margin高度 20:距点击的view高度（自己定）
                int ppwLocationDisY = getActivity().getResources().getDimensionPixelOffset(R.dimen.y78);
                ppw_itemFunction.showAtLocation(parentView, Gravity.TOP | Gravity.RIGHT, ppwLocationX, screenHeight - callWindowView.getHeight() + (int) view.getY() + ppwLocationDisY);
                /*ptyt start 解决切换组后来其他组的组呼，组呼框内的功能菜单的组数据未更新_kechuanqi_20171214*/
                isGroupCallWindowPPW = true;
                /*ptyt end*/
            }
        });
    }
    /* ptyt start 区分组列表和组呼框中的功能菜单_kechuanqi_20171214*/
    boolean isGroupCallWindowPPW;
    /*ptyt end*/
    String pressGroupId = "";

    /**
     * 权限授予时的回调
     */
    @Override
    public void doPermGrantedOfRecordAudio() {
        super.doPermGrantedOfRecordAudio();
        gCallCallback.startGroupCall(pressGroupId);
    }

    /**
     * 配置信息改变监听
     * @param sharedPreferences
     * @param key
     * @param value
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key, Object value) {
        PrintLog.i("onSharedPreferenceChanged() key="+key +"  value="+value +"  thread="+Thread.currentThread().getName());
        switch (key){
            case SETTINGS_LOCK_GROUP://锁定组变化
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.isComputingLayout()){
                            callAdapter.notifyDataSetChanged();
                            /*ptyt start 锁定组修改(无组呼时)，当锁定组时，组呼界面切换至锁定组_kechuanqi_20171218*/
                             //有锁定组时且不在组呼中->组呼框切换至锁定组
                            if(UctApplication.getInstance().isInGroupCall){
                                return;
                            }
                            doLockGroupLogic(2,null);
                            /*ptyt end*/
                        }
                        PrintLog.i("锁定组变化");
                    }
                });
                break;
        }
    }

    /**
     * 用户登录状态改变
     */
    public void notifyLoginStatusChanged() {
        if (ppw_itemFunction != null && ppw_itemFunction.isShowing()) {
            ppw_itemFunction.dismiss();
        }
    }

    public void showGCallWindow(){
        if(callWindowView != null){
            callWindowView.viewExtent(recyclerView, true);
        }
        UctApplication.getInstance().getGroupCallWindow().hidePopupWindow();
    }
}
