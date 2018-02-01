package com.ptyt.uct.activity;

import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.uct.service.UctClientApi;
import com.ptyt.uct.R;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.utils.AppUtils;

@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class SettingsAboutActivity extends BaseActionBarActivity implements View.OnClickListener {

    private TextView version_tv;
    private RelativeLayout rl_feedback, rl_check_new_version;
    private ImageView icon_iv;
    private TextView tv_uctlib;
    private static final int COUNTS = 5;// 点击次数
    private static final long DURATION = 3 * 1000;// 规定有效时间
    private long[] mHits = new long[COUNTS];

    @Override
    protected int setLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void initWidget() {
        icon_iv = (ImageView) findViewById(R.id.icon_iv);
        version_tv = (TextView) findViewById(R.id.version_tv);
        rl_feedback = (RelativeLayout) findViewById(R.id.rl_feedback);
        rl_check_new_version = (RelativeLayout) findViewById(R.id.rl_check_new_version);
        tv_uctlib = (TextView) findViewById(R.id.tv_uctlib);
        version_tv.setText(AppUtils.getVersionName(UctApplication.getInstance()));
        tv_actionBarTitle.setText(getResources().getString(R.string.string_settings_about));
        tv_uctlib.setText(String.format(getResources().getString(R.string.string_settings_about_uctlib_number), UctClientApi.getSoVersion() + ""));

        icon_iv.setOnClickListener(this);
        rl_feedback.setOnClickListener(this);
        rl_check_new_version.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.icon_iv:
                if (isFastClick()) {
                    startActivity(new Intent(this, SettingsBackDoorActivity.class));
                }
                break;
            case R.id.rl_feedback:
                startActivity(new Intent(SettingsAboutActivity.this, SettingsFeedbackActivity.class));
                break;
            case R.id.rl_check_new_version:
                startActivity(new Intent(SettingsAboutActivity.this, SettingsSoftUpgradeActivity.class));
                break;
        }
    }

    /**
     * @param
     * @return true   快速点击
     * false  非快速点击
     * @description 防止快速点击
     */
    private boolean isFastClick() {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
            return true;
        }
        return false;
    }
}
