package com.ptyt.uct.entity;

/**
 * Title: com.ptyt.uct.entity
 * Description:
 * Date: 2017/8/7
 * Author: ShaFei
 * Version: V1.0
 */

public class SettingsAccountEntity {

    private String name;
    private String number;
    private boolean isChecked;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public boolean getChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
