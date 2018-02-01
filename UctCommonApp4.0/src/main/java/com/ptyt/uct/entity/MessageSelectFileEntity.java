package com.ptyt.uct.entity;

/**
 * Title: com.ptyt.uct.entity
 * Description:
 * Date: 2017/5/27
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageSelectFileEntity {

    private String name;
    private int icon;
    private String action;

    public MessageSelectFileEntity(String name, int icon, String action) {
        this.name = name;
        this.icon = icon;
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
