package com.ptyt.uct.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 地图请求周边人的bean对象
 * @Date: 2017/10/25
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class MapResultUserInfo {


    /**
     * user_id : 420
     * latitude : 130
     * longitude : 120
     * radius : 10
     * members : ["421","422","423"]
     */
    //用户id
    private String user_id;
    //纬度
    private double latitude;
    //经度
    private double longitude;
    //搜索范围的半径(km)
    private int radius;
    //所有组成员id的集合
    private List<String> numbers;

    private List<UserGPSInfo> infos = new ArrayList<UserGPSInfo>();

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public List<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<String> numbers) {
        this.numbers = numbers;
    }

    public List<UserGPSInfo> getInfos() {
        return infos;
    }

    public void setInfos(List<UserGPSInfo> infos) {
        this.infos = infos;
    }
}
