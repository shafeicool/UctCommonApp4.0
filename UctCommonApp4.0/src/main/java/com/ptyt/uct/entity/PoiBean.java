package com.ptyt.uct.entity;

import java.io.Serializable;

/**
 * @Description: 地图检索
 * @Date: 2017/8/7
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class PoiBean implements Serializable {
    private String address;
    private int type;
    private String name;

    public PoiBean() {
    }

    public PoiBean(int type, String address, String name) {
        this.address = address;
        this.type = type;
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
