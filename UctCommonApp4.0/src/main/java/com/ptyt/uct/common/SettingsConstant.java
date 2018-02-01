package com.ptyt.uct.common;

import com.android.uct.UctLibConfigKey;
import com.android.uct.service.UctClientApi;

/**
 * Title: com.ptyt.uct.model
 * Description:
 * Date: 2017/8/7
 * Author: ShaFei
 * Version: V1.0
 */

public class SettingsConstant {

    // 升级码 0x5F8，转换时前面加40，十进制263672
    public static final short VERSION_THREEPROOFING = 0x5F8;
    // 振铃模式 0-铃声 1-震动 2-铃声+震动
    public static final int[] RING_MODE = {0, 1, 2};
    // 分辨率宽
    public static final int[] RESOLUTION_WIDTH_MODE = {320, 640, 1280, 1920};
    // 分辨率高
    public static final int[] RESOLUTION_HEIGHT_MODE = {240, 480, 720, 1080};
    // 帧率
    public static final int[] FRAME_RATE_MODE = {5, 15, 20, 25};
    // 码率
    public static final int[] BIT_RATE_MODE = {128, 256, 512, 1024, 2048, 3000, 5000};
    // GPS上传间隔，普通情况上传每次/10s,省电每次/60s
    public static final int GPS_UPLOAD_TIME_COMMON = 10000, GPS_UPLOAD_TIME_POWER_SAVE = 60000;
    // 锁定组分隔符
    public static final String LOCK_GROUP_REGULAREXPRESSION = ",";
    // 登录用户名
    public static final String SETTINGS_USERNAME = "settings_username";
    // 登录密码
    public static final String SETTINGS_PASSWORD = "settings_password";
    // 登录IP
    public static final String SETTINGS_IP = "settings_ip";
    // 短信振铃
    public static final String SETTINGS_AUDIO_RING = "settings_audio_ring";
    // 锁定组号码
    public static final String SETTINGS_LOCK_GROUP = "settings_lock_group";
    // 自动接收视频 0-关闭 1-打开
    public static final String SETTINGS_AUTO_RECEIVE_VIDEO = "settings_auto_receive_video";
    // 自动升级开关 0-关闭 1-打开
    public static final String SETTINGS_AUTO_UPGRADE_SWITCH = "settings_auto_upgrate_switch";
    // 日志开关 0-关闭 1-打开
    public static final String SETTINGS_LOG_SWITCH = "settings_log_switch";
    // 组呼录音 0-关闭 1-开启
    public static final String SETTINGS_GROUP_CALL_RECORD = "settings_group_call_record";
    // 省电模式 0-关闭 1-开启
    public static final String SETTINGS_POWER_SAVING_MODE = "settings_power_saving_mode";
    // 地图定位间隔
    public static final String SETTINGS_LOCATION_INTERVAL = "settings_location_interval";
    // 通话记录未读数
    public static final String SETTINGS_CALL_RECORD_UNREAD = "settings_call_record_unread";
    // 视频码率 默认512
    public static final String SETTINGS_VIDEO_BITRATE = UctLibConfigKey.CONFIGLIB + UctLibConfigKey.UCT_INT_CFG_VIDEO_BITRATE;
    // 视频分辨率 宽度 默认480
    public static final String SETTINGS_VIDEO_WIDTH = UctLibConfigKey.CONFIGLIB + UctLibConfigKey.UCT_INT_CFG_VIDEO_WIDTH;
    // 视频分辨率 高度 默认640
    public static final String SETTINGS_VIDEO_HEIGHT = UctLibConfigKey.CONFIGLIB + UctLibConfigKey.UCT_INT_CFG_VIDEO_HEIGHT;
    // 视频前后置摄像头 0-后置 1-前置
    public static final String SETTINGS_VIDEO_CAMERA = UctLibConfigKey.CONFIGLIB + UctLibConfigKey.UCT_INT_CFG_VIDEO_CAMERA;
    // 视频帧率 默认15
    public static final String SETTINGS_VIDEO_FRAMERATE = UctLibConfigKey.CONFIGLIB + UctLibConfigKey.UCT_INT_CFG_VIDEO_FRAMERATE;
    // 语音通道 0-免提 1-听筒
    public static final String SETTINGS_AUDIO_ROUTE = UctLibConfigKey.CONFIGLIB + UctLibConfigKey.UCT_INT_CFG_AUDIO_ROUTE;
    // 手电筒 0-关闭 1-开启
    public static final String SETTINGS_CAMERA_SET_FLASHLIGHT = UctLibConfigKey.CONFIGLIB + UctLibConfigKey.UCT_INT_CFG_CAMERA_SET_FLASHLIGHT;
    // GPRS开关 0-关闭 1-开启
    public static final String SETTINGS_GPRS_SWITCH = UctLibConfigKey.GPRS_SWITCH;
    // GPRS信号 0-无信号 1-有信号
    public static final String SETTINGS_GPRS_SIGNAL = UctLibConfigKey.GPRS_SIGNAL;
    // GPRS上报间隔
    public static final String SETTINGS_GPRS_TIME = UctLibConfigKey.GPRS_TIME;
    // GPRS服务器IP
    public static final String SETTINGS_GPRS_IP = UctLibConfigKey.GPRS_IP;
    // GPRS服务器端口
    public static final String SETTINGS_GPRS_PORT = UctLibConfigKey.GPRS_PORT;

    // 恢复默认设置
    public static void restoreSettings() {
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_AUDIO_RING, SettingsConstant.RING_MODE[0]);
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_LOCK_GROUP, "");
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_AUTO_RECEIVE_VIDEO, 0);
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_AUTO_UPGRADE_SWITCH, 0);
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_LOG_SWITCH, 1);
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_GROUP_CALL_RECORD, 0);
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_BITRATE, 512);
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_WIDTH, 640);
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_HEIGHT, 480);
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_CAMERA, 0);
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_FRAMERATE, 15);
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_POWER_SAVING_MODE, 0);
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_GPRS_TIME, GPS_UPLOAD_TIME_COMMON);
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_LOCATION_INTERVAL, 5000);
    }

    // 省电模式设置
    public static void setPowerSaving(boolean isPowerSaving) {
        if (isPowerSaving) {
            UctClientApi.saveUserData(SettingsConstant.SETTINGS_POWER_SAVING_MODE, 1);
            UctClientApi.saveUserData(SettingsConstant.SETTINGS_GPRS_TIME, GPS_UPLOAD_TIME_POWER_SAVE);
            UctClientApi.saveUserData(SettingsConstant.SETTINGS_LOCATION_INTERVAL, 10000);
        } else {
            UctClientApi.saveUserData(SettingsConstant.SETTINGS_POWER_SAVING_MODE, 0);
            UctClientApi.saveUserData(SettingsConstant.SETTINGS_GPRS_TIME, GPS_UPLOAD_TIME_COMMON);
            UctClientApi.saveUserData(SettingsConstant.SETTINGS_LOCATION_INTERVAL, 5000);
        }
    }

    public static boolean isPowerSavingMode() {
        return (((Integer) UctClientApi.getUserData(SettingsConstant.SETTINGS_POWER_SAVING_MODE, 0)).intValue()) == 0 ? false : true;
    }
}
