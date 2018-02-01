package com.ptyt.uct.widget.mapcluster;

import com.amap.api.maps.model.LatLng;

/**
 * Created by yiyi.qi on 16/10/10.
 */

public class RegionItem implements ClusterItem {
    private LatLng mLatLng;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    private String userName;

    public String getUserTel() {
        return userTel;
    }

    public void setUserTel(String userTel) {
        this.userTel = userTel;
    }

    private String userTel;
    private String uploadTime;

    public RegionItem() {
    }

    public RegionItem(LatLng latLng, String userName, String userTel, String uploadTime) {
        mLatLng=latLng;
        this.userTel = userTel;
        this.userName = userName;
        this.uploadTime = uploadTime;
    }

    @Override
    public LatLng getPosition() {
        return mLatLng;
    }

}
