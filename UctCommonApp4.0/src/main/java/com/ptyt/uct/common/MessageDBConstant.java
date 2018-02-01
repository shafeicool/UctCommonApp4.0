package com.ptyt.uct.common;

/**
 * Title: com.ptyt.uct.model
 * Description:
 * Date: 2017/5/5
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageDBConstant {

    // 每个会话的消息最大保留数目
    public static final long MESSAGE_LIMIT_COUNT = 1000L;

    ////////////////////消息类型Begin///////////////////
    // 文本
    public static final int INFO_TYPE_TEXT = 0x00;

    // 语音
    public static final int INFO_TYPE_AUDIO = 0x01;

    // 图片
    public static final int INFO_TYPE_IMAGE = 0x02;

    // 小视频
    public static final int INFO_TYPE_CAMERA_VIDEO = 0x03;

    // 视频文件
    public static final int INFO_TYPE_VIDEO = 0x04;

    // 文件
    public static final int INFO_TYPE_FILE = 0x05;

    // 我的位置
    public static final int INFO_TYPE_MY_LOCATION = 0x06;

    // 老设备文本
    public static final int INFO_TYPE_OLD_DEVICE_TEXT = 0x10;

    // 老设备彩信
    public static final int INFO_TYPE_OLD_DEVICE_COLOR_MSG = 0x11;

    // 语音呼叫
    public static final int INFO_TYPE_AUDIO_CALL = 0x12;

    // PTT语音
    public static final int INFO_TYPE_PTT_AUDIO = 0x21;
    ////////////////////消息类型End///////////////////

    ////////////////////消息发送方向Begin///////////////////
    // 对方发来的信息
    public static final int IMVT_COM_MSG = 0;

    // 自己发出的信息
    public static final int IMVT_TO_MSG = 1;
    ////////////////////消息发送方向End///////////////////

    ////////////////////语音播放状态Begin///////////////////
    private static final int BASE_ID = 2006;

    // 停止状态
    public static final int AUDIO_STOP_STATUS = BASE_ID + 1;

    // 播放状态
    public static final int AUDIO_START_STATUS = BASE_ID + 2;
    ////////////////////语音播放状态End///////////////////

    ////////////////////消息发送状态Begin///////////////////
    // 等待发送
    public static final int MSG_STATUS_WAIT_SENDING = 0;

    // 发送中
    public static final int MSG_STATUS_SENDING = 1;

    // 发送或接收失败
    public static final int MSG_STATUS_FAIL = 2;

    // 发送成功
    public static final int MSG_STATUS_SEND_SUCCESS = 3;

    // 文件未下载状态
    public static final int FILE_STATUS_NOT_DOWNLOAD = 4;

    // 文件已下载状态（下载成功）
    public static final int FILE_STATUS_RECEIVE_SUCCESS = 5;

    // 文件正在接收
    public static final int FILE_STATUS_RECEIVING = 6;

    // 等待下载
    public static final int FILE_STATUS_WAIT_RECEIVING = 7;
    ////////////////////消息发送状态End///////////////////

    ////////////////////消息是否已读状态Begin///////////////////
    // 未读短信
    public static final int UNREAD_MSG = 0;

    // 已读短信
    public static final int ALREAD_MSG = 1;
    ////////////////////消息是否已读状态End///////////////////

    ////////////////////音频是否已读状态Begin///////////////////
    // 未读短信
    public static final int AUDIO_UNREAD_MSG = 0;

    // 已读短信
    public static final int AUDIO_ALREAD_MSG = 1;
    ////////////////////音频是否已读状态End///////////////////

    ////////////////////是否分割短信Begin///////////////////
    // 不分割短信
    public static final int UNSEGMENTED_MSG = 0;

    // 分割短信
    public static final int SEGMENTED_MSG = 1;
    ////////////////////是否分割短信End///////////////////

    ////////////////////是否需要对方用户的确认Begin///////////////////
    // 不需要，服务器回收到确认即可
    public static final int NEEDLESS_CFM = 0;

    // 需要接收方接收确认
    public static final int NEED_RECV_CFM = 1;

    // 需要接收方已阅确认
    public static final int NEED_READ_CFM = 2;

    // 需要接收方接收&已阅确认
    public static final int NEED_RECV_READ_CFM = 3;
    ////////////////////是否需要对方用户的确认End///////////////////

    ////////////////////提示Begin///////////////////
    // 基本提示
    public static final int BASIC_NOTIFY = 0;

    // 特别提示
    public static final int SPECIAL_NOTIFY = 1;
    ////////////////////提示End///////////////////

    ////////////////////确认类型Begin///////////////////
    // 服务器确认
    public static final int SERVER_CFM = 0;

    // 接收方自动确认
    public static final int RECEIVER_AUTO_CFM = 1;

    // 接收方手动确认
    public static final int RECEIVER_MANUAL_CFM = 2;
    ////////////////////确认类型End///////////////////

    ////////////////////是否压缩图片Begin////////////////////

    // 压缩后的图片
    public static final int MSG_THUMBNAIL_IMAGE = 0;

    // 原图
    public static final int MSG_ORIGINAL_IMAGE = 1;
    ////////////////////是否压缩图片End////////////////////
}
