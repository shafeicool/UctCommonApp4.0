package com.ptyt.uct.entity;

import com.android.uct.bean.BaseBean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 描述：联系人bin文件（从ucp下载的联系人文件）
 * <p>
 * 创建时间 2017/5/12.
 * <p>
 * 创建者 崔晓锋
 * <p>
 * 版本：V1.0
 */
@Entity(nameInDb = "TB_CONTACT_FILE")
public class ContactFile extends BaseBean {
    //id标识
    @Id(autoincrement = true) @Property(nameInDb = "id")
    private  Long ID;
    //Bin文件名
    @NotNull @Unique @Property(nameInDb = "file_name")
    private  String fileName;
    
    //最后更新时间
    @Property(nameInDb = "last_time")
    private  long lastTime;

    public long getLastTime() {
        return this.lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getID() {
        return this.ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    @Generated(hash = 1127759421)
    public ContactFile(Long ID, @NotNull String fileName, long lastTime) {
        this.ID = ID;
        this.fileName = fileName;
        this.lastTime = lastTime;
    }

    @Generated(hash = 1668218650)
    public ContactFile() {
    }




}
