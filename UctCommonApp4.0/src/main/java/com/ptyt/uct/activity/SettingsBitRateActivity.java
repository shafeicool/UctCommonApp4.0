package com.ptyt.uct.activity;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.android.uct.service.UctClientApi;
import com.ptyt.uct.R;
import com.ptyt.uct.adapter.BaseRecyAdapter;
import com.ptyt.uct.adapter.SettingsBitRateAdapter;
import com.ptyt.uct.entity.SettingsBitRateEntity;
import com.ptyt.uct.common.SettingsConstant;

import java.util.ArrayList;
import java.util.List;


public class SettingsBitRateActivity extends BaseActionBarActivity implements BaseRecyAdapter.OnItemClickListener {

    private TextView tv_current;
    private RecyclerView rv_list;
    private SettingsBitRateAdapter mAdapter;
    private List<SettingsBitRateEntity> list = new ArrayList<>();
    private int currentBitRate;
    private int lastPos;
    private int currentPos;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_settings_bit_rate;
    }

    @Override
    protected void initWidget() {
        tv_current = (TextView) findViewById(R.id.tv_current);
        rv_list = (RecyclerView) findViewById(R.id.rv_list);
        rv_list.setHasFixedSize(true);
        rv_list.setLayoutManager(new GridLayoutManager(this, 3));
        ((DefaultItemAnimator) rv_list.getItemAnimator()).setSupportsChangeAnimations(false);
        tv_actionBarTitle.setText(getResources().getString(R.string.string_settings_bit_rate));

        currentBitRate = ((Integer) UctClientApi.getUserData(SettingsConstant.SETTINGS_VIDEO_BITRATE, 512)).intValue();
        tv_current.setText(currentBitRate + "");
    }

    @Override
    protected void initData() {
        mAdapter = new SettingsBitRateAdapter(this);
        mAdapter.setOnItemClickListener(this);
        rv_list.setAdapter(mAdapter);
        for (int i = 0; i < SettingsConstant.BIT_RATE_MODE.length; i++) {
            SettingsBitRateEntity entity = new SettingsBitRateEntity();
            entity.setBitRate(SettingsConstant.BIT_RATE_MODE[i]);
            if (currentBitRate == SettingsConstant.BIT_RATE_MODE[i]) {
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
            tv_current.setText(mAdapter.getItem(pos).getBitRate() + "");
            mAdapter.getItem(pos).setChecked(true);
            mAdapter.getItem(lastPos).setChecked(false);
            mAdapter.updateItem(pos, mAdapter.getItem(pos));
            mAdapter.updateItem(lastPos, mAdapter.getItem(lastPos));
        }
        int bitRate = mAdapter.getItem(pos).getBitRate();
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_BITRATE, bitRate);
        finish();
    }
}
