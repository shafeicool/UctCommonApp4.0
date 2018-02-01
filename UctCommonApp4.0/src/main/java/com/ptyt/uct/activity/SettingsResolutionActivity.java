package com.ptyt.uct.activity;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.uct.service.UctClientApi;
import com.ptyt.uct.R;
import com.ptyt.uct.adapter.BaseRecyAdapter;
import com.ptyt.uct.adapter.SettingsResolutionAdapter;
import com.ptyt.uct.entity.SettingsResolutionEntity;
import com.ptyt.uct.common.SettingsConstant;

import java.util.ArrayList;
import java.util.List;

public class SettingsResolutionActivity extends BaseActionBarActivity implements BaseRecyAdapter.OnItemClickListener {

    private RecyclerView rv_list;
    private SettingsResolutionAdapter mAdapter;
    private int currentResolutionWidth;
    private int currentResolutionHeight;
    private List<SettingsResolutionEntity> list = new ArrayList<>();
    private int currentPos;
    private int lastPos;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_settings_resolution;
    }

    protected void initWidget() {
        rv_list = (RecyclerView) findViewById(R.id.rv_list);
        rv_list.setHasFixedSize(true);
        rv_list.setLayoutManager(new LinearLayoutManager(this));
        ((DefaultItemAnimator) rv_list.getItemAnimator()).setSupportsChangeAnimations(false);
        tv_actionBarTitle.setText(getResources().getString(R.string.string_settings_resolution));

        currentResolutionWidth = ((Integer) UctClientApi.getUserData(SettingsConstant.SETTINGS_VIDEO_WIDTH, 640)).intValue();
        currentResolutionHeight = ((Integer) UctClientApi.getUserData(SettingsConstant.SETTINGS_VIDEO_HEIGHT, 480)).intValue();
    }

    @Override
    protected void initData() {
        mAdapter = new SettingsResolutionAdapter(this);
        mAdapter.setOnItemClickListener(this);
        rv_list.setAdapter(mAdapter);
        for (int i = 0; i < SettingsConstant.RESOLUTION_WIDTH_MODE.length; i++) {
            SettingsResolutionEntity entity = new SettingsResolutionEntity();
            entity.setWidth(SettingsConstant.RESOLUTION_WIDTH_MODE[i]);
            entity.setHeight(SettingsConstant.RESOLUTION_HEIGHT_MODE[i]);
            if (currentResolutionWidth == SettingsConstant.RESOLUTION_WIDTH_MODE[i] && currentResolutionHeight == SettingsConstant.RESOLUTION_HEIGHT_MODE[i]) {
                entity.setChecked(true);
                currentPos = i;
            } else {
                entity.setChecked(false);
            }
            list.add(entity);
        }
        mAdapter.addAll(list);
    }

    @Override
    public void onItemClick(int pos, View itemView) {
        lastPos = currentPos;
        currentPos = pos;
        if (!mAdapter.getItem(pos).getChecked()) {
            mAdapter.getItem(pos).setChecked(true);
            mAdapter.getItem(lastPos).setChecked(false);
            mAdapter.updateItem(pos, mAdapter.getItem(pos));
            mAdapter.updateItem(lastPos, mAdapter.getItem(lastPos));
        }
        int width = mAdapter.getItem(pos).getWidth();
        int height = mAdapter.getItem(pos).getHeight();
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_WIDTH, width);
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_HEIGHT, height);
        finish();
    }
}
