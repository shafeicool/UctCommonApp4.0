package com.ptyt.uct.callback;

import android.content.Context;

import com.android.uct.IUCTGroupInfoListener;
import com.android.uct.bean.GroupData;
import com.android.uct.bean.GroupOrganizationBean;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.entity.Group;
import com.ptyt.uct.model.GroupDBManager;
import com.ptyt.uct.utils.ConstantUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * @Description: 组信息
 * @Date: 2017/5/10
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class GroupInfoCallback{

    private static GroupInfoCallback instance;
    private Context mContext;
    private List<GroupData> mGroupList = new ArrayList<>();

    public static synchronized GroupInfoCallback getInstance(){

        if(instance==null){
            instance = new GroupInfoCallback();
        }
        return instance;
    }

    public void init(Context context) {
        mContext = context;
        PrintLog.w("注册GroupInfoCallback");
        // 监听组信息改变
        UctClientApi.registerObserver(groupInfoListener, IUCTGroupInfoListener.IUCTGROUPINFOLISTENER_INDEX);
    }

    /**
     * 用户组通知,组列表有变化(如调度台新建了组)
     */
    private IUCTGroupInfoListener groupInfoListener = new IUCTGroupInfoListener() {

        @Override
        public int UCT_UctGDataInd(List<GroupOrganizationBean> organizationList, List<GroupData> groupAllList) {
            PrintLog.e("AppContext 1 IUCTGroupInfoListener  groupAllList.size()=" + groupAllList.size() + "   organizationList.size()=" + organizationList.size());
            if(groupAllList == null || groupAllList.size() == 0){
                return 0;
            }
            GroupDBManager.getInstance().deleteAll();
            mGroupList.clear();
            mGroupList.addAll(groupAllList);
            ArrayList<Group> groups = new ArrayList<>();
            for (int i = 0; i < groupAllList.size(); i++) {
                GroupData groupData = groupAllList.get(i);
                Group group = new Group();
                group.setGrouTel(groupData.groupId);
                group.setGroupName(groupData.groupName);
                group.setAdminTel(groupData.groupAdmin);
                PrintLog.e("groupData.groupName="+groupData.groupName+"  groupData.groupId="+groupData.groupId);
                group.setGroupCreateUser(groupData.groupNewUser);
                groups.add(group);
            }
            GroupDBManager.getInstance().insertGroupList(groups);
            EventBus.getDefault().post(new EventBean(ConstantUtils.ACTION_GROUP_INFO_REFRESH));
            return 0;
        }
    };

    public void release() {
        PrintLog.w("反注册GroupInfoCallback");
        // 组信息
        UctClientApi.unregisterObserver(groupInfoListener, IUCTGroupInfoListener.IUCTGROUPINFOLISTENER_INDEX);
    }


    /**
     * 获取组信息数据
     *
     * @return
     */
    public List<GroupData> getmGroupList() {
        return mGroupList;
    }

}
