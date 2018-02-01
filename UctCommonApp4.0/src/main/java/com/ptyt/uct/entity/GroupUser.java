package com.ptyt.uct.entity;

import com.android.uct.bean.BaseBean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;

import java.io.Serializable;

/**
 * 描述   分组成员
 * <p>
 * 创建时间 2017/5/12.
 * <p>
 * 创建者 崔晓锋
 * <p>
 * 版本：V1.0
 */
@Entity(nameInDb = "TB_GROUP_USER")
public class GroupUser extends BaseBean implements Serializable{
    //id标识
    @Id(autoincrement = true) @Property(nameInDb = "id")
    private  Long ID;
    // 组id
    @NotNull @Property(nameInDb = "group_id")
    private  long  groupId;
    //用户号码
    @Property(nameInDb = "user_tel")
    private  String userTel;
    //用户名
    @Property(nameInDb = "user_name")
    private  String userName;
    //用户图标
    @Property(nameInDb = "user_icon")
    private  String userIcon;
    //用户类型
    @Property(nameInDb = "user_type")
    private  Integer userType;
    // 用户状态
    @Property(nameInDb = "user_status")
    private  Integer userStatus;
    //用户是否空闲
    @Property(nameInDb = "user_blink")
    private  Integer userBlink;

    //  用户是否在线
    @Property(nameInDb = "user_online")
    private  Integer userOnline;

    public Integer getUserOnline() {
        return this.userOnline;
    }

    public void setUserOnline(Integer userOnline) {
        this.userOnline = userOnline;
    }

    public Integer getUserBlink() {
        return this.userBlink;
    }

    public void setUserBlink(Integer userBlink) {
        this.userBlink = userBlink;
    }

    public Integer getUserStatus() {
        return this.userStatus;
    }

    public void setUserStatus(Integer userStatus) {
        this.userStatus = userStatus;
    }

    public Integer getUserType() {
        return this.userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getUserIcon() {
        return this.userIcon;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserTel() {
        return this.userTel;
    }

    public void setUserTel(String userTel) {
        this.userTel = userTel;
    }

    public long getGroupId() {
        return this.groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public Long getID() {
        return this.ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    @Generated(hash = 1768621478)
    public GroupUser(Long ID, long groupId, String userTel, String userName, String userIcon, Integer userType,
            Integer userStatus, Integer userBlink, Integer userOnline, String userNamePinYin, Double latitude,
            Double longitude) {
        this.ID = ID;
        this.groupId = groupId;
        this.userTel = userTel;
        this.userName = userName;
        this.userIcon = userIcon;
        this.userType = userType;
        this.userStatus = userStatus;
        this.userBlink = userBlink;
        this.userOnline = userOnline;
        this.userNamePinYin = userNamePinYin;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Generated(hash = 1548903865)
    public GroupUser() {
    }

    /**
     * 用于通讯录排序 KeChuanqi 2017/5/24添加******************************************************
     */
    @Keep
    private String userNamePinYin;
    /**
     * 经纬度 地图显示 KeChuanqi 2017/8/3添加
     */
    @Keep
    private Double latitude;
    @Keep
    private Double longitude;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * 用于地图显示
     * @param groupId
     * @param userTel
     * @param userName
     * @param userType
     * @param latitude
     * @param longitude
     */
    public GroupUser(long groupId, String userTel, String userName, Integer userType, Double latitude, Double longitude) {
        this.groupId = groupId;
        this.userTel = userTel;
        this.userName = userName;
        this.userType = userType;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Keep
    public String getUserNamePinYin() {
        return userNamePinYin;
    }

    @Keep
    public void setUserNamePinYin(String userNamePinYin) {
        this.userNamePinYin = userNamePinYin;
    }
    public GroupUser(String userTel, String userName,
                     String userIcon, Integer userType, Integer userStatus,
                     Integer userBlink, Integer userOnline,String userNamePinYin) {
        this.userTel = userTel;
        this.userName = userName;
        this.userIcon = userIcon;
        this.userType = userType;
        this.userStatus = userStatus;
        this.userBlink = userBlink;
        this.userOnline = userOnline;
        this.userNamePinYin = userNamePinYin;
    }
    public GroupUser(long groupId,String userTel, String userName,
                     String userIcon, Integer userType, Integer userStatus,
                     Integer userBlink, Integer userOnline,String userNamePinYin) {
        this.groupId = groupId;
        this.userTel = userTel;
        this.userName = userName;
        this.userIcon = userIcon;
        this.userType = userType;
        this.userStatus = userStatus;
        this.userBlink = userBlink;
        this.userOnline = userOnline;
        this.userNamePinYin = userNamePinYin;
    }
    public GroupUser(String userTel, String userName,
                     String userIcon, Integer userType, Integer userStatus,
                     Integer userBlink, Integer userOnline) {
        this.userTel = userTel;
        this.userName = userName;
        this.userIcon = userIcon;
        this.userType = userType;
        this.userStatus = userStatus;
        this.userBlink = userBlink;
        this.userOnline = userOnline;
    }

    @Keep
    public GroupUser(String userNameIndex) {
        this.userName = userNameIndex;
    }

    @Keep
    public GroupUser(String userTel, String userName) {
        this.userTel = userTel;
        this.userName = userName;
    }

}
