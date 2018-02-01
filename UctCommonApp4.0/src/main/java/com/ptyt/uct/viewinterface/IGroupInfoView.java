package com.ptyt.uct.viewinterface;

import com.android.uct.bean.GroupData;

import java.util.List;

/**
 * @Description:
 * @Date: 2017/5/10
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public interface IGroupInfoView {

    //组呼挂断
    int GCALL_HANGUP = 1;
    //无人说话
    int NOBODY_SPEAK = 2;
    //自己说话
    int SELF_SPEAK = 3;
    //其他人说话
    int OTHER_SPEAK = 4;
    //组呼失败
    int GCALL_FAILED = 5;
    //组呼成功
    int GCALL_SUCCEED = 6;
    //组呼释放
    int GCALL_RELEASE = 7;
    //其他人发起组呼(被叫组呼)
    int OTHER_LAUCHER_GCALL = 8;
    // 组呼发起确认
    int GCALL_CONFIRM = 9;
    //平常状态,不关心当前有无组呼,用于组呼主界面滑动列表时组呼大界面缩小成组呼悬浮框的组名显示(当前无组呼)
    int NO_GROUP_CALL = -1;
    //离线
    int USER_OFFLINE = 404;

    //组呼状态改变
    void gCallStateChanged(int state,String groupId,String cTalkingGID);

    void onLoadSucceed(List<GroupData> groupList);
    //组信息改变(组添加或删除)
    void onRefresh(List<GroupData> groupList);
}
