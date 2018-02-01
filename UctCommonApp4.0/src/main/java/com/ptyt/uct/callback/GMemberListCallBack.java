package com.ptyt.uct.callback;

import android.content.Context;

import com.android.uct.IUCTGroupSubscibeListener;
import com.android.uct.bean.GroupData;
import com.android.uct.bean.GroupOrganizationBean;
import com.android.uct.bean.User;
import com.android.uct.exception.UctLibException;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.model.GroupUserDBManager;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.viewinterface.IGMemberView;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Date: 2017/5/12
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class GMemberListCallBack {

    private static GMemberListCallBack instance;
    private List<IGMemberView> viewInterfaces = new ArrayList<>();

    public static synchronized GMemberListCallBack getInstance() {

        if (instance == null) {
            instance = new GMemberListCallBack();
        }
        return instance;
    }

    public void init(Context context) {
        mContext = context;
        PrintLog.w("注册GMemberListCallback");
        //注册组信息
        UctClientApi.registerObserver(subscibeListener, IUCTGroupSubscibeListener.IUCTGROUPSUBSCIBELISTENER_INDEX);
    }

    /**
     * 订阅数据上报
     */
    private IUCTGroupSubscibeListener subscibeListener = new IUCTGroupSubscibeListener() {

        @Override
        public int UCT_GetUctGrpDataFormIdx(int Result, int subType, String cGid, int onLineSize, List<User> userList, List<GroupData> groupList)
                throws UctLibException {
            PrintLog.i("UCT_GetUctGrpDataFormIdx Result = " + Result + ", subType = " + subType + ", cGid = " + ", onLineSize = "
                    + onLineSize + ", userList.size = " + userList.size() + ", groupList.size = " + groupList.size());
            if (Result == 0) {
                for (int i = 0; i < viewInterfaces.size(); i++) {
                    IGMemberView igMemberView = viewInterfaces.get(i);
                    if (igMemberView != null) {
                        igMemberView.onLoadSucceed(groupList, userList);
                    }
                }
            } else if (Result == 2) {// 无权限
                ToastUtils.getToast().showMessageLong(mContext, mContext.getString(R.string.response_no_permission), -1);
                PrintLog.i(mContext.getString(R.string.response_no_permission));
            } else if (Result == 3) {// 对象不存在
                ToastUtils.getToast().showMessageLong(mContext, mContext.getString(R.string.response_not_exists), -1);
                PrintLog.i(mContext.getString(R.string.response_not_exists));
            }
            return 0;
        }

        @Override
        public int UCT_GetUctAllGrpDataFormIdx(int result, int i1, String s, List<GroupOrganizationBean> list, ArrayList<String> arrayList) throws UctLibException {
            PrintLog.i("UCT_GetUctAllGrpDataFormIdx result = " + result + ", i1 = " + i1 + ", s = " + s
                    + ", list.size = " + list.size() + ", arrayList.size = " + arrayList);
            UctClientApi.CancelQueryGListFormIdx();
            if (result == 0) {
                for (int j = 0; j < viewInterfaces.size(); j++) {
                    IGMemberView igMemberView = viewInterfaces.get(j);
                    if (igMemberView != null) {
                        igMemberView.onAllUserLoadSucceed(list, arrayList);
                    }
                }
            } else {
                PrintLog.i(mContext.getString(R.string.response_failed));
            }
            return 0;
        }
    };

    private Context mContext;

    /**
     * 绑定view，view中可以得到数据的回调(方便处:可以在多处得到回调)
     *
     * @param gMemberView
     */
    public void bindView(IGMemberView gMemberView) {
        synchronized (this) {
            viewInterfaces.add(gMemberView);
        }
    }

    /**
     * 解绑
     *
     * @param gMemberView
     */
    public void unbindView(IGMemberView gMemberView) {
        synchronized (this) {
            viewInterfaces.remove(gMemberView);
        }
    }

    public void release() {
        PrintLog.w("反注册GMemberListCallback");
        UctClientApi.unregisterObserver(subscibeListener, IUCTGroupSubscibeListener.IUCTGROUPSUBSCIBELISTENER_INDEX);
        GroupUserDBManager.getInstance(mContext).deleteAll();
        GroupUserDBManager.getInstance(mContext).release();
    }

}
