package com.ptyt.uct.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.View;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.fragment.ContactFragment;
import com.ptyt.uct.fragment.GMemberListFragment;
import com.ptyt.uct.utils.ConstantUtils;

/**
 * @Description: 组成员
 * @Date: 2017/5/11
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class GMemberListActivity extends BaseActionBarActivity {


    private GMemberListFragment gMemberListFragment;
    private ContactFragment contactFragment;

    @Override
    protected int setLayoutId() {
        return 0;
    }

    @Override
    public void initWidget() {
        super.initWidget();
    }


    @Override
    protected void initData() {
        String intent = getIntent().getStringExtra("intent");
        if (intent != null && intent.equals(ConstantUtils.INTENT_ADDRESS_BOOK)) {//通讯录
            tv_actionBarTitle.setText(getResources().getString(R.string.string_contact_title));
            tv_actionBarRight.setVisibility(View.VISIBLE);
            tv_actionBarRight.setText(getResources().getString(R.string.string_contact_subtitle));
            contactFragment = new ContactFragment();
            addFragment(contactFragment);
        } else {
            String groupId = getIntent().getStringExtra("groupId");
            String groupName = getIntent().getStringExtra("groupName");
            tv_actionBarTitle.setText(groupName);
            gMemberListFragment = new GMemberListFragment();
            Bundle bundle = new Bundle();
            bundle.putString("groupId", groupId);
            gMemberListFragment.setArguments(bundle);
            addFragment(gMemberListFragment);
        }
    }

    /**
     * 设置标题:组名（人数）
     *
     * @param titleName
     */
    public void setTitleName(String titleName) {
        tv_actionBarTitle.setText(titleName);
    }

    public void setOnRightTitleClickListener(View.OnClickListener listener) {
        tv_actionBarRight.setOnClickListener(listener);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        PrintLog.i("onResume()");
        super.onResume();
    }

    @Override
    protected void onStop() {
        PrintLog.i("onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        PrintLog.i("onDestroy()");
        super.onDestroy();
    }
}
