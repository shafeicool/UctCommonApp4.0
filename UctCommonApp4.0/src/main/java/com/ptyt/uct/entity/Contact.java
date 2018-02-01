package com.ptyt.uct.entity;

import com.android.uct.bean.BaseBean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 描述：联系人
 * <p>
 * 创建时间 2017/5/12.
 * <p>
 * 创建者 崔晓锋
 * <p>
 * 版本：V1.0
 */
@Entity(nameInDb = "TB_CONTACT")
public class Contact extends BaseBean {
    //id标识
    @Id(autoincrement = true)
    @Property(nameInDb = "id")
    private Long ID;
    // 联系人文件id
    @NotNull
    @Property(nameInDb = "contact_file_id")
    private long contactFileID;
    // 号码
    @NotNull
    @Property(nameInDb = "number")
    private String number;
    //名字
    @Property(nameInDb = "name")
    private String name;
    //描述
    @Property(nameInDb = "desc")
    private String desc;
    //父号码
    @Property(nameInDb = "parent_num")
    private String parentNum;
    //通讯录类型，是组还是是用户： type=1 用户类型   type=2 组类型
    @Property(nameInDb = "type")
    private Integer type;
    //通讯录来源类型，分为我司调度台创建的，以及客户创建的： SourceType=1 我们自己创建的，即调度台创建的   SourceType=2 客户创建的，比如：黄埔海关的Excel表格中的通讯录
    @Property(nameInDb = "source_type")
    private Integer sourceType;

    @Keep
    public Contact(String userNameIndex) {
        this.name = userNameIndex;
    }

    public Integer getSourceType() {
        return this.sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getParentNum() {
        return this.parentNum;
    }

    public void setParentNum(String parentNum) {
        this.parentNum = parentNum;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public long getContactFileID() {
        return this.contactFileID;
    }

    public void setContactFileID(long contactFileID) {
        this.contactFileID = contactFileID;
    }

    public Long getID() {
        return this.ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    @Generated(hash = 682804971)
    public Contact(Long ID, long contactFileID, @NotNull String number, String name, String desc,
            String parentNum, Integer type, Integer sourceType) {
        this.ID = ID;
        this.contactFileID = contactFileID;
        this.number = number;
        this.name = name;
        this.desc = desc;
        this.parentNum = parentNum;
        this.type = type;
        this.sourceType = sourceType;
    }

    @Generated(hash = 672515148)
    public Contact() {
    }

}
