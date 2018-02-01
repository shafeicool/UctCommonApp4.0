package com.ptyt.uct.entity;

import java.io.Serializable;
import java.util.Timer;

/**
 * @Description: 用于view.setTag(bean) 的 bean
 * @Date: 2017/9/2
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class TagBean implements Serializable {
    //开始计时时间
    private long speakStartTime;
    //计时对象
    private Timer timer;
    //hUserCall 作标识
    private int hUserCall;

    public TagBean(long speakStartTime, Timer timer,int hUserCall) {
        this.speakStartTime = speakStartTime;
        this.timer = timer;
        this.hUserCall = hUserCall;
    }

    public TagBean() {
    }

    public long getSpeakStartTime() {
        return speakStartTime;
    }

    public void setSpeakStartTime(long speakStartTime) {
        this.speakStartTime = speakStartTime;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public int getHUserCall() {
        return hUserCall;
    }

    public void setHUserCall(int hUserCall) {
        this.hUserCall = hUserCall;
    }
}
