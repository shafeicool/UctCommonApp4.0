package com.ptyt.uct.activity;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.uct.service.UctClientApi;
import com.ptyt.uct.R;
import com.ptyt.uct.adapter.BaseRecyAdapter;
import com.ptyt.uct.adapter.SettingsFrameRateAdapter;
import com.ptyt.uct.entity.SettingsFrameRateEntity;
import com.ptyt.uct.common.SettingsConstant;

import java.util.ArrayList;
import java.util.List;

public class SettingsFrameRateActivity extends BaseActionBarActivity implements BaseRecyAdapter.OnItemClickListener {

    private RecyclerView rv_list;
    private SettingsFrameRateAdapter mAdapter;
    private int currentFrameRate;
    private List<SettingsFrameRateEntity> list = new ArrayList<>();
    private int currentPos;
    private int lastPos;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_settings_frame_rate;
    }

    protected void initWidget() {
        rv_list = (RecyclerView) findViewById(R.id.rv_list);
        rv_list.setHasFixedSize(true);
        rv_list.setLayoutManager(new LinearLayoutManager(this));
        ((DefaultItemAnimator) rv_list.getItemAnimator()).setSupportsChangeAnimations(false);
        tv_actionBarTitle.setText(getResources().getString(R.string.string_settings_frame_rate));

        currentFrameRate = ((Integer) UctClientApi.getUserData(SettingsConstant.SETTINGS_VIDEO_FRAMERATE, 15)).intValue();
    }

    @Override
    protected void initData() {
        mAdapter = new SettingsFrameRateAdapter(this);
        mAdapter.setOnItemClickListener(this);
        rv_list.setAdapter(mAdapter);
        for (int i = 0; i < SettingsConstant.FRAME_RATE_MODE.length; i++) {
            SettingsFrameRateEntity entity = new SettingsFrameRateEntity();
            entity.setFrameRate(SettingsConstant.FRAME_RATE_MODE[i]);
            if (currentFrameRate == SettingsConstant.FRAME_RATE_MODE[i]) {
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
        int frameRate = mAdapter.getItem(pos).getFrameRate();
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_FRAMERATE, frameRate);
        finish();
    }
}
