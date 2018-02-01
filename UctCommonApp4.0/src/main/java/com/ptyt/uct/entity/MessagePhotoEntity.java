package com.ptyt.uct.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Title: com.ptyt.uct.entity
 * Description:
 * Date: 2017/6/27
 * Author: ShaFei
 * Version: V1.0
 */

public class MessagePhotoEntity implements Parcelable {

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Long getDuring() {
        return during;
    }

    public void setDuring(Long during) {
        this.during = during;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public boolean getChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    // 本地路径
    private String path;
    // 文件类型
    private int type;
    // 视频时长
    private Long during;
    // 最后修改日期
    private Long time;
    // 文件大小
    private Long size;
    // 是否被选中
    private boolean isChecked = false;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (path == null) {
            dest.writeString("");
        } else {
            dest.writeString(path);
        }
        dest.writeInt(type);
        if (during == null) {
            dest.writeLong(0);
        } else {
            dest.writeLong(during);
        }
        if (time == null) {
            dest.writeLong(0);
        } else {
            dest.writeLong(time);
        }
        if (size == null) {
            dest.writeLong(0);
        } else {
            dest.writeLong(size);
        }
        dest.writeByte((byte) (isChecked ? 1 : 0));

    }

    public static final Parcelable.Creator<MessagePhotoEntity> CREATOR = new Creator<MessagePhotoEntity>(){

        @Override
        public MessagePhotoEntity createFromParcel(Parcel source) {
            return new MessagePhotoEntity(source);
        }

        @Override
        public MessagePhotoEntity[] newArray(int size) {
            return new MessagePhotoEntity[size];
        }
    };

    public MessagePhotoEntity(){

    }

    public MessagePhotoEntity(Parcel source){
        //如果元素数据是list类型的时候需要： lits = new ArrayList<?> in.readList(list);
        //否则会出现空指针异常.并且读出和写入的数据类型必须相同.如果不想对部分关键字进行序列化,可以使用transient关键字来修饰以及static修饰.
        path = source.readString();
        type = source.readInt();
        during = source.readLong();
        time = source.readLong();
        size = source.readLong();
        isChecked = source.readByte() != 0;
    }

}
