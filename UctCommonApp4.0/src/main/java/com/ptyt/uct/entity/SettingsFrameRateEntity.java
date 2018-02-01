package com.ptyt.uct.entity;

/**
 * Title: com.ptyt.uct.entity
 * Description:
 * Date: 2017/8/4
 * Author: ShaFei
 * Version: V1.0
 */

public class SettingsFrameRateEntity {

    private boolean isChecked = false;
    private int frameRate;

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public boolean getChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
