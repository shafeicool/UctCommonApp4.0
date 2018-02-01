package com.ptyt.uct.entity;

import com.android.uct.bean.BaseBean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 描述：分组
 * <p>
 * 创建时间 2017/5/12.
 * <p>
 * 创建者 崔晓锋
 * <p>
 * 版本：V1.0
 */
@Entity(nameInDb = "TB_GROUP")
public class Group extends BaseBean {


    //id标识
    @Id(autoincrement = true) @Property(nameInDb = "id")
    private Long ID;
    //组号码
    @NotNull @Unique @Property(nameInDb = "group_tel")
    private String grouTel;
    //组名称
    @Property(nameInDb = "group_name")
    private String  groupName;
    //组图标
    @Property(nameInDb = "group_icon")
    private  String groupIcon;
    //管理员号码
    @Property(nameInDb = "admin_tel")
    private String adminTel;
    //创建组的用户
    @Property(nameInDb = "group_creat_user")
    private String groupCreateUser;
    //组状态
    @Property(nameInDb = "group_status")
    private  String  groupStatus;
    //组是否空闲
    @Property(nameInDb = "group_blink")
    private  String groupBlink;
    //父组
    @Property(nameInDb = "group_parent")
    private  String groupParent;
    public String getGroupParent() {
        return this.groupParent;
    }
    public void setGroupParent(String groupParent) {
        this.groupParent = groupParent;
    }
    public String getGroupBlink() {
        return this.groupBlink;
    }
    public void setGroupBlink(String groupBlink) {
        this.groupBlink = groupBlink;
    }
    public String getGroupStatus() {
        return this.groupStatus;
    }
    public void setGroupStatus(String groupStatus) {
        this.groupStatus = groupStatus;
    }
    public String getGroupCreateUser() {
        return this.groupCreateUser;
    }
    public void setGroupCreateUser(String groupCreateUser) {
        this.groupCreateUser = groupCreateUser;
    }
    public String getAdminTel() {
        return this.adminTel;
    }
    public void setAdminTel(String adminTel) {
        this.adminTel = adminTel;
    }
    public String getGroupIcon() {
        return this.groupIcon;
    }
    public void setGroupIcon(String groupIcon) {
        this.groupIcon = groupIcon;
    }
    public String getGroupName() {
        return this.groupName;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public String getGrouTel() {
        return this.grouTel;
    }
    public void setGrouTel(String grouTel) {
        this.grouTel = grouTel;
    }
    public Long getID() {
        return this.ID;
    }
    public void setID(Long ID) {
        this.ID = ID;
    }
    @Generated(hash = 1445678005)
    public Group(Long ID, @NotNull String grouTel, String groupName,
            String groupIcon, String adminTel, String groupCreateUser,
            String groupStatus, String groupBlink, String groupParent) {
        this.ID = ID;
        this.grouTel = grouTel;
        this.groupName = groupName;
        this.groupIcon = groupIcon;
        this.adminTel = adminTel;
        this.groupCreateUser = groupCreateUser;
        this.groupStatus = groupStatus;
        this.groupBlink = groupBlink;
        this.groupParent = groupParent;
    }
    @Generated(hash = 117982048)
    public Group() {
    }

}
