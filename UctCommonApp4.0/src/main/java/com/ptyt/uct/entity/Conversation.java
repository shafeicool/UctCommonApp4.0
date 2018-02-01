package com.ptyt.uct.entity;


import com.android.uct.bean.BaseBean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 描述：消息会话
 * <p>
 * 创建时间 2017/5/11.
 * <p>
 * 创建者 崔晓锋
 * <p>
 * 版本：V1.0
 */
@Entity(nameInDb = "TB_CONVERSATION")
public class Conversation extends BaseBean {
    //id标识
    @Id(autoincrement = true) @Property(nameInDb = "id")
    private Long ID;
    //是否通知
    @Property(nameInDb = "is_notify")
    private Integer isNotify;
    //消息源号码
    @NotNull
    @Property(nameInDb = "msg_src_no")
    private String msgSrcNo;
    //消息目的号码
    @NotNull
    @Property(nameInDb = "msg_dst_no")
    private String msgDstNo;
    //组号码
    @Property(nameInDb = "group_no")
    private String groupNo;
    //登陆号码
    @Property(nameInDb = "login_no")
    private String loginNo;
    //最后一条消息类型
    @Property(nameInDb = "last_msg_type")
    private Integer lastMsgType;
    //最后一条消息内容
    @Property(nameInDb = "last_msg_content")
    private String lastMsgContent;
    //最后一条的时间
    @Property(nameInDb = "last_msg_time")
    private Long lastMsgTime;
    //最后一条消息发送状态
    @Property(nameInDb = "last_msg_status")
    private Integer lastMsgStatus;
    // /最后一条的方向
    @Property(nameInDb = "last_msg_direction")
    private Integer lastMsgDirection;
    //未读消息数
    @Property(nameInDb = "unread_msg_counts")
    private Integer unreadMsgCounts;
    //是否置顶
    @Property(nameInDb = "stick_time")
    private Long stickTime;
    public Long getStickTime() {
        return this.stickTime;
    }
    public void setStickTime(Long stickTime) {
        this.stickTime = stickTime;
    }
    public Integer getUnreadMsgCounts() {
        return this.unreadMsgCounts;
    }
    public void setUnreadMsgCounts(Integer unreadMsgCounts) {
        this.unreadMsgCounts = unreadMsgCounts;
    }
    public Integer getLastMsgDirection() {
        return this.lastMsgDirection;
    }
    public void setLastMsgDirection(Integer lastMsgDirection) {
        this.lastMsgDirection = lastMsgDirection;
    }
    public Integer getLastMsgStatus() {
        return this.lastMsgStatus;
    }
    public void setLastMsgStatus(Integer lastMsgStatus) {
        this.lastMsgStatus = lastMsgStatus;
    }
    public Long getLastMsgTime() {
        return this.lastMsgTime;
    }
    public void setLastMsgTime(Long lastMsgTime) {
        this.lastMsgTime = lastMsgTime;
    }
    public String getLastMsgContent() {
        return this.lastMsgContent;
    }
    public void setLastMsgContent(String lastMsgContent) {
        this.lastMsgContent = lastMsgContent;
    }
    public Integer getLastMsgType() {
        return this.lastMsgType;
    }
    public void setLastMsgType(Integer lastMsgType) {
        this.lastMsgType = lastMsgType;
    }
    public String getLoginNo() {
        return this.loginNo;
    }
    public void setLoginNo(String loginNo) {
        this.loginNo = loginNo;
    }
    public String getGroupNo() {
        return this.groupNo;
    }
    public void setGroupNo(String groupNo) {
        this.groupNo = groupNo;
    }
    public String getMsgDstNo() {
        return this.msgDstNo;
    }
    public void setMsgDstNo(String msgDstNo) {
        this.msgDstNo = msgDstNo;
    }
    public String getMsgSrcNo() {
        return this.msgSrcNo;
    }
    public void setMsgSrcNo(String msgSrcNo) {
        this.msgSrcNo = msgSrcNo;
    }
    public Integer getIsNotify() {
        return this.isNotify;
    }
    public void setIsNotify(Integer isNotify) {
        this.isNotify = isNotify;
    }
    public Long getID() {
        return this.ID;
    }
    public void setID(Long ID) {
        this.ID = ID;
    }
    @Generated(hash = 942418389)
    public Conversation(Long ID, Integer isNotify, @NotNull String msgSrcNo,
            @NotNull String msgDstNo, String groupNo, String loginNo,
            Integer lastMsgType, String lastMsgContent, Long lastMsgTime,
            Integer lastMsgStatus, Integer lastMsgDirection,
            Integer unreadMsgCounts, Long stickTime) {
        this.ID = ID;
        this.isNotify = isNotify;
        this.msgSrcNo = msgSrcNo;
        this.msgDstNo = msgDstNo;
        this.groupNo = groupNo;
        this.loginNo = loginNo;
        this.lastMsgType = lastMsgType;
        this.lastMsgContent = lastMsgContent;
        this.lastMsgTime = lastMsgTime;
        this.lastMsgStatus = lastMsgStatus;
        this.lastMsgDirection = lastMsgDirection;
        this.unreadMsgCounts = unreadMsgCounts;
        this.stickTime = stickTime;
    }
    @Generated(hash = 1893991898)
    public Conversation() {
    }
    

}
