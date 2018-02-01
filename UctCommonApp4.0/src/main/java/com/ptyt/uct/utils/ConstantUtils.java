package com.ptyt.uct.utils;

/**
 * @Description:
 * @Date: 2017/5/24
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class ConstantUtils {

    public static final String ACTION_TAB_TO_GCALL = "android.intent.action.TAB_TO_GROUP_CALL";
    public static final String ACTION_UPLOAD_VIDEO_COME_IN = "android.intent.action.UPLOAD_VIDEO";
    public static final String ACTION_DOWNLOAD_VIDEO_COME_IN = "android.intent.action.DOWNLOAD_VIDEO";
    public static final String ACTION_AUDIO_CALL_COME_IN = "android.intent.action.AUDIO_CALL";
    public static final String ACTION_VIDEO_CALL_COME_IN = "android.intent.action.VIDEO_CALL";
    public static final String ACTION_GROUP_CALL_COME_IN = "android.intent.action.GROUP_CALL";
    public static final String ACTION_LOCATION_CHANGE = "android.intent.action.MAP_LOCATION_CHANGE";
    public static final String ACTION_GROUP_INFO_REFRESH = "android.intent.action.GROUP_INFO_REFRESH";
    public static final String ACTION_MESSAGE_TO_MAP = "android.intent.action.MESSAGE_TO_MAP";
    public static final String ACTION_INSERT_CONTACT = "android.intent.action.INSERT_CONTACT";
    public static final String ACTION_NETWORK_CHANGED = "android.intent.action.NETWORK_CHANGED";
    public static final String ACTION_FRAGMENT_CHANGED = "android.intent.action.FRAGMENT_CHANGED";
    public static final String ACTION_TO_MAP_SHOW_USER = "android.intent.action.MAP_SHOW_USER";
    //查看视频对方接听
    public static final String ACTION_OTHER_ACCEPT = "android.intent.action.BUSINESS_OTHER_ACCEPT";
    //查看视频对方挂断
    public static final String ACTION_OTHER_HANGUP = "android.intent.action.BUSINESS_OTHER_HANGUP";
    //呼叫业务未读数更新通知
    public static final String ACTION_CALL_UNREAD_NOTIFY = "android.intent.action.CALL_UNREAD_NOTIFY";
    public static final java.lang.String ACTION_OFFLINE_MAP_DOWNLOAD_FINISH = "android.intent.action.OFFLINE_MAP_DOWNLOAD_FINISH";
    //终端类型
    //调度台
    public static final int UT_TYPE_DISPATCH = 1;
    //专业终端
    public static final int UT_TYPE_EXDISPATCH = 3;
    //通用智能终端
    public static final int UT_TYPE_COMMONIN = 5;
    // 摄像头
    public static final int UT_TYPE_CAMERA = 6;
    //无屏机
    public static final int UT_TYPE_NOSCREEN = 9;
    //三防机
    public static final int UT_TYPE_3PROOF = 10;
    //4G执法记录仪
    public static final int UT_TYPE_4GRECORDER = 11;
    //无人机
    public static final int UT_TYPE_VEHICLE = 12;
    //布控球
    public static final int UT_TYPE_CONTROLBALL = 13;
    public static final int TYPE_NO_SCREEN_MACHINE = 19;

    //区分 上传视频,视频呼叫
    public static final int UPLOAD_VIDEO = 2, DOWNLOAD_VIDEO = 3;
    //区分 语音单呼,视频呼叫：
    public static final int AUDIO_SCALL = 0,VIDEO_SCALL = 1,MEETING_CALL = 7;

    //区分呼叫方向 0为主叫 1为被叫
    public static final int CALL_DIRECTION_ACTIVE = 0,CALL_DIRECTION_PASSIVE = 1;
    /**
     * 视频方向 上传 视频方向 1:接收(下载) 2:发送(上传) 3:收发
     */
    public static final int VIDEO_DIRECTION_UPLOAD=2;

    public static final String INTENT_ADDRESS_BOOK = "addressBook";
    /**
     *  视频业务类型 0:普通视频
     */
    public static final int NORMAL_VIDEO=0;
    /**
     * 视频业务类型 1:视频转发
     */
    public static final int FORWARD_VIDEO=1;
    /**
     *  视频业务类型 2:视频监视
     */
    public static final int MONITOR_VIDEO=2;

    //1 组呼挂断;2 无人说话;3 自己说话;4 其他人说话;5 组呼失败;6 组呼成功;7 组呼释放;8 其他人发起组呼(被叫组呼)
    public static final int GCALL_HANGUP = 1,NOBODY_SPEAK = 2,SELF_SPEAK = 3,OTHER_SPEAK = 4,
            GCALL_FAILED = 5,GCALL_SUCCEED = 6,GCALL_RELEASE = 7,OTHER_LAUCHER_GCALL = 8;
    public static final int HEADVIEW_TYPE = 100;
    public static final int FOOTVIEW_TYPE = 101;
    public static final int COMMON_TYPE = 102;
    public static final int STATE_LOAD_MORE = 110;
    public static final int STATE_NO_MORE = 111;
    public static final int STATE_EMPTY_ITEM = 112;
    public static final int STATE_NETWORK_ERROR = 113;

    // 通话记录：0-语音呼叫打入 1-语音呼叫打出 2-视频呼叫打入 3-视频呼叫打出 4-视频上传打出 5-视频上传打入 6-视频下载(只有入)
    public static final int CALL_RECORD_AUDIO_CALL_IN = 0;
    public static final int CALL_RECORD_AUDIO_CALL_OUT = 1;
    public static final int CALL_RECORD_VIDEO_CALL_IN = 2;
    public static final int CALL_RECORD_VIDEO_CALL_OUT = 3;
    public static final int CALL_RECORD_VIDEO_UPLOAD_OUT = 4;
    public static final int CALL_RECORD_VIDEO_UPLOAD_IN = 5;
    public static final int CALL_RECORD_VIDEO_DOWNLOAD_IN = 6;
    // 通话记录是否已读 0-对方打入未接听 1-已接听/自己打出
    public static final int CALL_RECORD_UNREAD = 0;
    public static final int CALL_RECORD_ALREAD = 1;
    //add in 20171213,为在music通道的情况下调节会话音量的情况
    public static final int STREAM_MUSIC_AND_ADJUST_VOICE_VOLUME = 30;

}
