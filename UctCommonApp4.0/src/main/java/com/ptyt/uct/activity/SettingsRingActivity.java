package com.ptyt.uct.activity;

import android.support.annotation.IdRes;
import android.widget.RadioGroup;

import com.android.uct.service.UctClientApi;
import com.ptyt.uct.R;
import com.ptyt.uct.common.SettingsConstant;


public class SettingsRingActivity extends BaseActionBarActivity {


    private RadioGroup radioGroup;


    @Override
    protected int setLayoutId() {
        return R.layout.activity_settings_ring;
    }

    @Override
    protected void initWidget() {
        tv_actionBarTitle.setText(getResources().getString(R.string.string_settings_ringing));
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        int mode = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_AUDIO_RING, SettingsConstant.RING_MODE[0]);
        if (mode == SettingsConstant.RING_MODE[0]) {
            radioGroup.check(R.id.rb1);
        } else if (mode == SettingsConstant.RING_MODE[1]) {
            radioGroup.check(R.id.rb2);
        } else if (mode == SettingsConstant.RING_MODE[2]) {
            radioGroup.check(R.id.rb3);
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    // 铃声
                    case R.id.rb1:
                        UctClientApi.saveUserData(SettingsConstant.SETTINGS_AUDIO_RING, SettingsConstant.RING_MODE[0]);
                        break;
                    // 震动
                    case R.id.rb2:
                        UctClientApi.saveUserData(SettingsConstant.SETTINGS_AUDIO_RING, SettingsConstant.RING_MODE[1]);
                        break;
                    // 铃声 + 震动
                    case R.id.rb3:
                        UctClientApi.saveUserData(SettingsConstant.SETTINGS_AUDIO_RING, SettingsConstant.RING_MODE[2]);
                        break;
                }
                finish();
            }
        });
    }

    @Override
    protected void initData() {

    }

}
