package com.ptyt.uct.entity;

import android.content.Context;

import com.amap.api.services.poisearch.PoiResult;
import com.ptyt.uct.utils.NetUtils;

import java.io.Serializable;

/**
 * @Description: EventBus 传递的bean
 * @Date: 2017/6/12
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class EventBean implements Serializable {
    public Object getObject() {
        return object;
    }

    private  Object object;
    //用以区分EventBus传递的不同事件
    private String action;

    //区分不同动作的细分操作
    private int detailAction;
    private String pcCalling;
    private String pcDn;
    private String pcName;
    private int businessTag;
    //呼叫方向：0为主叫 1为被叫
    private int callDirection;
    //(被叫)1-接收视频 2-pc查看视频
    private int iDirection;
    private int hUserCall;
    private UserEntity userEntity;
    private String groupId;
    private String groupName;
    private boolean isAnswered;//是否已经接听

    public boolean isNetworkAvailable(Context context) {
        return NetUtils.isNetworkAvailable(context);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public PoiResult getPoiResult() {
        return poiResult;
    }

    public void setPoiResult(PoiResult poiResult) {
        this.poiResult = poiResult;
    }

    private PoiResult poiResult;

    private String cGid;

    public EventBean() {
    }

    public EventBean(String action) {
        this.action = action;
    }
    public EventBean(String action,Object o) {
        this.action = action;
        this.object = o;
    }
    public EventBean(String action, int detailAction) {
        this.action = action;
        this.detailAction = detailAction;
    }

    /**
     * @param action
     * @param pcCalling
     * @param pcDn
     * @param pcName
     * @param callDirection 呼叫方向：0为主叫 1为被叫
     * @param iDirection    (被叫)1-接收视频 2-pc查看视频
     * @param hUserCall
     * @param businessTag   视频细分业务：0-语音呼叫  1-视频呼叫 2-上传视频
     */
    public EventBean(String action, String pcCalling, String pcDn, String pcName, int callDirection, int iDirection, int hUserCall, int businessTag) {
        this.action = action;
        this.pcCalling = pcCalling;
        this.pcDn = pcDn;
        this.pcName = pcName;
        this.hUserCall = hUserCall;
        this.callDirection = callDirection;
        this.businessTag = businessTag;
        this.iDirection = iDirection;
    }

    public EventBean(String action, String pcCalling, String pcDn, String pcName, int callDirection, int hUserCall, int businessTag) {
        this.action = action;
        this.pcCalling = pcCalling;
        this.pcDn = pcDn;
        this.pcName = pcName;
        this.hUserCall = hUserCall;
        this.callDirection = callDirection;
        this.businessTag = businessTag;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPcCalling() {
        return pcCalling;
    }

    public void setPcCalling(String pcCalling) {
        this.pcCalling = pcCalling;
    }

    public String getPcDn() {
        return pcDn;
    }

    public void setPcDn(String pcDn) {
        this.pcDn = pcDn;
    }

    public String getPcName() {
        return pcName;
    }

    public void setPcName(String pcName) {
        this.pcName = pcName;
    }

    public int getiDirection() {
        return iDirection;
    }

    public void setiDirection(int iDirection) {
        this.iDirection = iDirection;
    }

    public int gethUserCall() {
        return hUserCall;
    }

    public void sethUserCall(int hUserCall) {
        this.hUserCall = hUserCall;
    }

    public int getDetailAction() {
        return detailAction;
    }

    public void setDetailAction(int detailAction) {
        this.detailAction = detailAction;
    }


    public void setcGid(String cGid) {
        this.cGid = cGid;
    }

    public String getcGid() {
        return cGid;
    }

    public int getCallDirection() {
        return callDirection;
    }

    public void setCallDirection(int callDirection) {
        this.callDirection = callDirection;
    }

    public int getBusinessTag() {
        return businessTag;
    }

    public void setBusinessTag(int businessTag) {
        this.businessTag = businessTag;
    }

    public boolean isAnswered() {
        return isAnswered;
    }

    public void setAnswered(boolean answered) {
        isAnswered = answered;
    }
}
