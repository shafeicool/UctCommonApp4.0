package com.ptyt.uct.entity;

import java.io.Serializable;

/**
 * @Description:
 * @Date: 2017/7/4
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class UserEntity implements Serializable {
    //
    private String userName;
    private int hUserCall;
    private int iDirection;

    public UserEntity() {
    }

    public UserEntity(String userName, int hUserCall) {
        this.userName = userName;
        this.hUserCall = hUserCall;
    }

    public UserEntity(String userName, int hUserCall, int iDirection) {
        this.userName = userName;
        this.hUserCall = hUserCall;
        this.iDirection = iDirection;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int gethUserCall() {
        return hUserCall;
    }

    public void sethUserCall(int hUserCall) {
        this.hUserCall = hUserCall;
    }

    public int getiDirection() {
        return iDirection;
    }

    public void setiDirection(int iDirection) {
        this.iDirection = iDirection;
    }
}
