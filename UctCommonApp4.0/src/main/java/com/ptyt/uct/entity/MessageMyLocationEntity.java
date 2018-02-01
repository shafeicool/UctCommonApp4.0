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

public class MessageMyLocationEntity implements Parcelable {

    // 本地路径
    private String localPath;
    // 远程路径
    private String remotePath;
    // 文件类型
    private int type;
    // 文件大小
    private Long size;
    // 地图级别
    private float zoom;
    // 纬度
    private double latitude;
    // 经度
    private double longitude;
    // 地名
    private String placeName;
    // 精确地名
    private String specificPlaceName;

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Long getSize() {
        return size;
    }

    public float getZoom() {
        return zoom;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getSpecificPlaceName() {
        return specificPlaceName;
    }

    public void setSpecificPlaceName(String specificPlaceName) {
        this.specificPlaceName = specificPlaceName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (localPath == null) {
            dest.writeString("");
        } else {
            dest.writeString(localPath);
        }
        if (remotePath == null) {
            dest.writeString("");
        } else {
            dest.writeString(remotePath);
        }
        dest.writeInt(type);
        if (size == null) {
            dest.writeLong(0);
        } else {
            dest.writeLong(size);
        }
        dest.writeFloat(zoom);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        if (placeName == null) {
            dest.writeString("");
        } else {
            dest.writeString(placeName);
        }
        if (specificPlaceName == null) {
            dest.writeString("");
        } else {
            dest.writeString(specificPlaceName);
        }
    }

    public static final Creator<MessageMyLocationEntity> CREATOR = new Creator<MessageMyLocationEntity>(){

        @Override
        public MessageMyLocationEntity createFromParcel(Parcel source) {
            return new MessageMyLocationEntity(source);
        }

        @Override
        public MessageMyLocationEntity[] newArray(int size) {
            return new MessageMyLocationEntity[size];
        }
    };

    public MessageMyLocationEntity(){

    }

    public MessageMyLocationEntity(Parcel source){
        //如果元素数据是list类型的时候需要： lits = new ArrayList<?> in.readList(list);
        //否则会出现空指针异常.并且读出和写入的数据类型必须相同.如果不想对部分关键字进行序列化,可以使用transient关键字来修饰以及static修饰.
        localPath = source.readString();
        remotePath = source.readString();
        type = source.readInt();
        size = source.readLong();
        zoom = source.readFloat();
        latitude = source.readDouble();
        longitude = source.readDouble();
        placeName = source.readString();
        specificPlaceName = source.readString();
    }

}
