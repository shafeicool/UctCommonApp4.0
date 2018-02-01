package com.ptyt.uct.entity;

/**
 * @Description:
 * @Date: 2017/10/25
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class UserGPSInfo {
    //
    private String dN;
    //字段信息
    private double longitude;
    private double latitude;
    //
    private double speed;
    private int direction;
    private double height;
    //
    private int status;
    private String startTime;
    private String endTime;
    private String totalRoute;
    private String route;
    private String clearTime;

    public String getDN() {
        return dN;
    }
    public void setDN(String dN) {
        dN = dN;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getSpeed() {
        return speed;
    }
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    public int getDirection() {
        return direction;
    }
    public void setDirection(int direction) {
        this.direction = direction;
    }
    public double getHeight() {
        return height;
    }
    public void setHeight(double height) {
        this.height = height;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public String getEndTime() {
        return endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    public String getTotalRoute() {
        return totalRoute;
    }
    public void setTotalRoute(String totalRoute) {
        this.totalRoute = totalRoute;
    }
    public String getRoute() {
        return route;
    }
    public void setRoute(String route) {
        this.route = route;
    }
    public String getClearTime() {
        return clearTime;
    }
    public void setClearTime(String clearTime) {
        this.clearTime = clearTime;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null && obj instanceof UserGPSInfo){
            if(((UserGPSInfo) obj).dN.equals(this.dN)){
                return true;
            }
        }
        // TODO Auto-generated method stub
        return false;
    }


}
