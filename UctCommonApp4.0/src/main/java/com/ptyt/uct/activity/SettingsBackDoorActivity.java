package com.ptyt.uct.activity;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.CompoundButton;

import com.android.uct.service.UctClientApi;
import com.ptyt.uct.R;
import com.ptyt.uct.adapter.SettingsBackDoorAdapter;
import com.ptyt.uct.entity.SettingsBackDoorEntity;
import com.ptyt.uct.common.SettingsConstant;
import com.ptyt.uct.utils.SDCardUtils;

import java.util.ArrayList;
import java.util.List;


public class SettingsBackDoorActivity extends BaseActionBarActivity implements SettingsBackDoorAdapter.OnCheckedChangeListener {

    private RecyclerView rv_list;
    private SettingsBackDoorAdapter mAdapter;
    private List<SettingsBackDoorEntity> list = new ArrayList<>();

    @Override
    protected int setLayoutId() {
        return R.layout.activity_settings_back_door;
    }

    protected void initWidget() {
        rv_list = (RecyclerView) findViewById(R.id.rv_list);
        rv_list.setHasFixedSize(true);
        rv_list.setLayoutManager(new LinearLayoutManager(this));
        ((DefaultItemAnimator) rv_list.getItemAnimator()).setSupportsChangeAnimations(false);
        tv_actionBarTitle.setText(getResources().getString(R.string.string_settings_back_door));

    }

    @Override
    protected void initData() {
        mAdapter = new SettingsBackDoorAdapter(this);
        mAdapter.setOnCheckedChangeListener(this);
        rv_list.setAdapter(mAdapter);
        initItem();
        mAdapter.addAll(list);
    }

    private void initItem() {
        // 打印日志开关
        int logSwitch = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_LOG_SWITCH, 1);
        boolean isOpenLogSwitch = logSwitch == 0 ? false : true;
        setItem(getString(R.string.string_settings_back_door_log), "", isOpenLogSwitch);
    }

    /**
     * @param head
     * @param subHead
     * @param isOpen
     * @return
     * @description 设置item所需的标题、副标题、开关。如果不需要副标题，设置为null或"", 如果不需要按钮，设置为null
     */
    private void setItem(String head, String subHead, Boolean isOpen) {
        SettingsBackDoorEntity entity = new SettingsBackDoorEntity();
        entity.setHead(head);
        entity.setSubHead(subHead);
        entity.setOpen(isOpen);
        list.add(entity);
    }

    @Override
    public void onCheckedChange(int pos, CompoundButton buttonView, boolean isChecked) {
        switch (pos) {
            case 0:
                if (isChecked) {
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_LOG_SWITCH, 1);
//                    UctClientApi.openLog();
                    UctClientApi.saveLog(SDCardUtils.getLogBasePath());
                } else {
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_LOG_SWITCH, 0);
                    UctClientApi.cancelSaveLog();
//                    UctClientApi.closeLog();
                }
                break;
        }
    }
}
