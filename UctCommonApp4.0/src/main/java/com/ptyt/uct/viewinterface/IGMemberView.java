package com.ptyt.uct.viewinterface;

import com.android.uct.bean.GroupData;
import com.android.uct.bean.GroupOrganizationBean;
import com.android.uct.bean.User;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Date: 2017/5/12
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public interface IGMemberView {

    void onLoadSucceed(List<GroupData> groupList, List<User> userList);

    void onAllUserLoadSucceed(List<GroupOrganizationBean> list, ArrayList<String> arrayList);
}
