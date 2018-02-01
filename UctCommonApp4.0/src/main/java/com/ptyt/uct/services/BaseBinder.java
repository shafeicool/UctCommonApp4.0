package com.ptyt.uct.services;

import android.os.Binder;


abstract class BaseBinder extends Binder {
    /**
     * 注册接口
     * @param observer 注册回调的接口
     * @param index 注册的索引
     */
    public abstract void registerObserver(BaseServiceCallBack observer, int index);
    /**
     * 反注册接口
     * @param observer 注册回调的接口
     * @param index 注册的索引
     */
    public abstract void unRegisterObserver(BaseServiceCallBack observer, int index);

    /**
     * 服务销毁
     */
    public abstract void serviceDestory();
}
