package com.ptyt.uct.entity;

/**
 * @Description:
 * @Date: 2017/10/30
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class MapAroundPeopleEntity {


    /**
     * clearTime : 2017-10-10 09:34:19
     * dN : 422
     * direction : 134
     * endTime : 2017-10-27 17:14:48
     * height : -47
     * latitude : 22.52001
     * longitude    : 113.92235
     * route : 0.090000
     * speed : 16
     * startTime : 2017-10-27 17:14:48
     * status : 3
     * totalRoute : 0.090000
     */

    private String clearTime;
    private String dN;
    private int direction;
    private String endTime;
    private int height;
    private double latitude;
    private double longitude;
    private String route;
    private int speed;
    private String startTime;
    private int status;
    private String totalRoute;

    public String getClearTime() {
        return clearTime;
    }

    public void setClearTime(String clearTime) {
        this.clearTime = clearTime;
    }

    public String getDN() {
        return dN;
    }

    public void setDN(String dN) {
        this.dN = dN;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
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

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTotalRoute() {
        return totalRoute;
    }

    public void setTotalRoute(String totalRoute) {
        this.totalRoute = totalRoute;
    }
}
