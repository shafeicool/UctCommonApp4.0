package com.ptyt.uct.entity;

import com.android.uct.bean.BaseBean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;
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
@Entity(nameInDb = "TB_CALL_RECORD")
public class CallRecord extends BaseBean {
    // id标识
    @Id(autoincrement = true)
    @Property(nameInDb = "id")
    private Long ID;
    // 名字
    @Property(nameInDb = "name")
    private String name;
    // 号码
    @NotNull @Property(nameInDb = "number")
    private String number;
    // 通话时长
    @Property(nameInDb = "call_time")
    private String callTime;
    // 记录时间
    @Property(nameInDb = "record_time")
    private Long recordTime;
    // 记录类型
    @Property(nameInDb = "type")
    private Integer type;
    // 是否已读
    @Property(nameInDb = "is_read")
    private Integer isRead;
    // 是否被选中
    @Keep @Transient
    private boolean isChecked = false;

    public Integer getIsRead() {
        return this.isRead;
    }
    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }
    public Integer getType() {
        return this.type;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public Long getRecordTime() {
        return this.recordTime;
    }
    public void setRecordTime(Long recordTime) {
        this.recordTime = recordTime;
    }
    public String getCallTime() {
        return this.callTime;
    }
    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }
    public String getNumber() {
        return this.number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getID() {
        return this.ID;
    }
    public void setID(Long ID) {
        this.ID = ID;
    }
    @Generated(hash = 823747123)
    public CallRecord(Long ID, String name, @NotNull String number,
            String callTime, Long recordTime, Integer type, Integer isRead) {
        this.ID = ID;
        this.name = name;
        this.number = number;
        this.callTime = callTime;
        this.recordTime = recordTime;
        this.type = type;
        this.isRead = isRead;
    }
    @Generated(hash = 1744672525)
    public CallRecord() {
    }

    @Keep
    public boolean getChecked() {
        return this.isChecked;
    }

    @Keep
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}
