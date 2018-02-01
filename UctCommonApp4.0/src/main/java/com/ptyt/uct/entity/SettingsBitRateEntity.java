package com.ptyt.uct.entity;

/**
 * Title: com.ptyt.uct.entity
 * Description:
 * Date: 2017/8/4
 * Author: ShaFei
 * Version: V1.0
 */

public class SettingsBitRateEntity {

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public boolean getChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    private int bitRate;
    private boolean isChecked = false;
}
