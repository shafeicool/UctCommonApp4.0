package com.ptyt.uct.entity;

import java.io.Serializable;

/**
 * Title: com.ptyt.uct.entity
 * Description:
 * Date: 2017/5/27
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageFileListEntity implements Serializable {

    // 本地路径: /storage/emulated/0/234.rar
    private String path;
    // 文件类型: zip word excel
    private String type;
    // 文件显示名称: 234
    private String displayName;
    // 文件带后缀名称: 234.rar
    private String suffixName;
    // 文件大小
    private Long size;
    // 文件最后修改的时间
    private Long time;
    // 是否被选中
    private boolean isChecked = false;

    public MessageFileListEntity() {

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSuffixName() {
        return suffixName;
    }

    public void setSuffixName(String suffixName) {
        this.suffixName = suffixName;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public boolean getChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

}
