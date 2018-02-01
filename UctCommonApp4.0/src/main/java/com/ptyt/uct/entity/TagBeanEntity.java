package com.ptyt.uct.entity;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @Description:
 * @Date: 2017/10/9
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class TagBeanEntity implements Serializable {

    private int hUserCall;

    private HashMap<Integer,TagBean> map = new HashMap<>();

    public TagBeanEntity() {
    }

    public HashMap<Integer, TagBean> getMap() {
        return map;
    }

    public int getHUserCall() {
        return hUserCall;
    }

    public void setHUserCall(int hUserCall) {
        this.hUserCall = hUserCall;
    }
}
