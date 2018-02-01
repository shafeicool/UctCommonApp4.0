package com.ptyt.uct.entity;

import java.io.Serializable;

/**
 * @Description:
 * @Date: 2017/7/14
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class VideoBusinessBean implements Serializable{

    public String pcCalling;
    public String pcDn;
    public String pcName;
    public int hUserCall;
    public int callDirection;

    public VideoBusinessBean(String pcCalling, String pcDn, String pcName, int hUserCall,int callDirection) {
        this.pcCalling = pcCalling;
        this.pcDn = pcDn;
        this.pcName = pcName;
        this.hUserCall = hUserCall;
        this.callDirection = callDirection;
    }

    public VideoBusinessBean() {
    }
}
