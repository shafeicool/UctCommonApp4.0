package com.ptyt.uct.callback;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.android.uct.IUCTDispatchListener;
import com.android.uct.bean.GroupData;
import com.android.uct.exception.UctLibException;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.common.SettingsConstant;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.SDCardUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @Description:
 * @Date: 2017/5/9
 * @Author: ShaFei
 * @Version: V1.0
 */

public class PhoneConfigCallBack extends BaseCallBack {

    private static PhoneConfigCallBack instance = null;
    private Context mContext = null;

    public static synchronized PhoneConfigCallBack getPhoneConfigCallBack() {
        if (instance == null) {
            instance = new PhoneConfigCallBack();
        }
        return instance;
    }

    @Override
    public void init(Context context) {
        mContext = context;
        PrintLog.w("注册PhoneConfigCallBack");
        UctClientApi.registerObserver(dispatchListener, IUCTDispatchListener.IUCTDISPATCHLISTENER_INDEX);
        //        SettingsConstant.restoreSettings();
    }

    @Override
    public void release() {
        PrintLog.w("反注册PhoneConfigCallBack");
        UctClientApi.unregisterObserver(dispatchListener, IUCTDispatchListener.IUCTDISPATCHLISTENER_INDEX);
    }

    IUCTDispatchListener dispatchListener = new IUCTDispatchListener() {
        @Override
        public int UCT_COOM_Notify(int type, int i1, int i2, int i3, String s1, String s2, String s3) throws UctLibException {
            PrintLog.i("UCT_COOM_Notify [type = " + type + ", i1 = " + i1 + ", i2 = " + i2 + ", i3 = " + i3 + ", s1 = " + s1 + ", s2 = " + s2 + ", s3 = " + s3 + "]");
            switch (type) {
                case IUCTDispatchListener.DISPATCH_VIDEO_SETTING:
                    int frameRate = i2;
                    int bitRate = i3;
                    int width = (i1 & 0xffff0000) >> 16;
                    int height = i1 & 0xffff;
                    // 0-成功 1-分辨率不支持 2-帧率有误 3-码率有误
                    // 判断设置的分辨率再终端上是否支持
                    if (!isSupportResolution(width + "X" + height)) {
                        return 1;
                    }
                    // 判断设置的帧率是否在终端上支持
                    if (!isSupportFrameRate(frameRate)) {
                        return 2;
                    }
                    // 判断设置的码率是否在终端上支持
                    if (!isSupportBitRate(bitRate)) {
                        return 3;
                    }
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_FRAMERATE, frameRate);
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_BITRATE, bitRate);
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_WIDTH, width);
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_HEIGHT, height);
                    break;
                case IUCTDispatchListener.DISPATCH_GPS_SETTING:
                    int GpsSwitch = "1".equals(s1) ? 1 : 0;
                    int GpsSignal = "1".equals(s2) ? 1 : 0;
                    int GpsTime = i1;
                    int GpsIp = i2;
                    int GpsPort = i3;
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_GPRS_SWITCH, GpsSwitch);
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_GPRS_SIGNAL, GpsSignal);
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_GPRS_TIME, GpsTime);
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_GPRS_IP, GpsIp);
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_GPRS_PORT, GpsPort);
                    break;
                case IUCTDispatchListener.DISPATCH_LOG_SETTING:
                    switch (i1) {
                        case 1:// 打开终端日志功能
                            UctClientApi.saveUserData(SettingsConstant.SETTINGS_LOG_SWITCH, 1);
//                            UctClientApi.openLog();
                            UctClientApi.saveLog(SDCardUtils.getLogBasePath());
                            break;
                        case 2:// 关闭终端日志功能
                            UctClientApi.saveUserData(SettingsConstant.SETTINGS_LOG_SWITCH, 0);
                            UctClientApi.cancelSaveLog();
//                            UctClientApi.closeLog();
                            break;
                        case 3:
                            FileUtils.deleteDirectory(mContext, SDCardUtils.getLogPath());
                            int logSwitch = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_LOG_SWITCH, 1);
                            boolean isOpenLogSwitch = logSwitch == 0 ? false : true;
                            if (isOpenLogSwitch) {
                                UctClientApi.saveLog(SDCardUtils.getLogBasePath());
                            } else {
                                UctClientApi.cancelSaveLog();
                            }
                            break;
                    }
                    break;
            }
            return 0;
        }

        @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public int UCT_PhoneComment(String pcDstId, String pcSrcId, byte[] pcInfoIn, String pcInfoOut, int InfoOutLen) throws UctLibException {
            PrintLog.i("UCT_PhoneComment [pcDstId = " + pcDstId + ", pcSrcId = " + pcSrcId + ", pcInfoIn = " + Arrays.toString(pcInfoIn) + ", pcInfoOut = " + pcInfoOut + ", InfoOutLen = " + InfoOutLen + "]");
            if (pcInfoIn[1] == IUCTDispatchListener.DISPTCH_SELECT_VIDEO) {// 查询自动接收视频开关
                selectVideoSett(pcSrcId);
            } else if (pcInfoIn[1] == IUCTDispatchListener.DISPTCH_BASE_SETTING) {// 终端基本设置
                if (pcInfoIn[2] == 1) {
                    // 自动接收视频开关打开
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_AUTO_RECEIVE_VIDEO, 1);
                } else {
                    //  自动接收视频开关关闭
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_AUTO_RECEIVE_VIDEO, 0);
                }
                //跟调度台定好的协议 0-后置 1-外置 2-前置 3-HDMI
                if (pcInfoIn[3] == 0) {
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_CAMERA, 0);
                } else if (pcInfoIn[3] == 1) {
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_CAMERA, 0);
                } else if (pcInfoIn[3] == 2) {
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_CAMERA, 1);
                } else if (pcInfoIn[3] == 3) {
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_CAMERA, 0);
                } else {
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_CAMERA, 0);
                }
                selectVideoSett(pcSrcId);
            } else if (pcInfoIn[1] == IUCTDispatchListener.DISPTCH_LOCK_GROUP_SETTING) {// 锁组设置
                if (pcInfoIn[2] == 1) {// 锁定组
                    String tel = "";
                    for (int i = 1; i < pcInfoIn.length; i++) {
                        if (pcInfoIn[i] == 0)
                            break;
                        if (i >= 3)
                            tel += (pcInfoIn[i] - 48) + "";
                    }
                    if (TextUtils.isEmpty(tel)) {
                        return 0;
                    }
                    //                    List<GroupData> list = AppContext.getAppContext().getmGroupList();
                    //                    boolean isGroup = false;
                    //                    for (int i = 0; i < list.size(); i++) {
                    //                        if (tel.equals(list.get(i).groupId)) {
                    //                            isGroup = true;
                    //                            break;
                    //                        }
                    //                    }
                    if (!isGroup(tel)) {
                        return 0;
                    }
                    //                    if (!UctApi.isGroup(tel))
                    //                        return 0;
                    lockGroup(tel, 1);

                } else if (pcInfoIn[2] == 0) {// 取消锁定组
                    String tel = "";
                    for (int i = 3; i < pcInfoIn.length; i++) {
                        if (pcInfoIn[i] == 0)
                            break;
                        if (i >= 3)
                            tel += (pcInfoIn[i] - 48) + "";
                    }
                    if (TextUtils.isEmpty(tel)) {
                        return 0;
                    }
                    lockGroup(tel, -1);
                }
                groupLockRespon(pcSrcId);
            } else if (pcInfoIn[1] == IUCTDispatchListener.DISPTCH_SELECT_LOCK_GROUP) {// 查询锁定组设置
                groupLockRespon(pcSrcId);
            } else if (pcInfoIn[1] == IUCTDispatchListener.DISPTCH_SELECT_VERSION_UPGRADE) {// 查询自动升级开关
                selectAutoUpgradeSwitch(pcSrcId);
            } else if (pcInfoIn[1] == 3) {// 设置自动升级开关
                int value = pcInfoIn[2];
                UctClientApi.saveUserData(SettingsConstant.SETTINGS_AUTO_UPGRADE_SWITCH, value);
                byte buffer[] = new byte[4];
                int action = 4;
                //指令
                buffer[1] = (byte) ((action >> 0) & 0xFF);
                buffer[0] = (byte) ((action >> 8) & 0xFF);
                buffer[2] = (byte) 1;
                buffer[3] = (byte) 0;
                UctClientApi.setPhoneComment(pcSrcId, "", "", buffer, buffer.length);
            } else if (pcInfoIn[1] == IUCTDispatchListener.DISPTCH_SELECT_VERSION) {// 终端版本查询
                setPhoneComment(pcSrcId);
            }
            return 0;
        }
    };

    private boolean isSupportResolution(String resolution) {
        for (int i = 0; i < SettingsConstant.RESOLUTION_WIDTH_MODE.length; i++) {
            if (resolution.equals(SettingsConstant.RESOLUTION_WIDTH_MODE[i] + "X" + SettingsConstant.RESOLUTION_HEIGHT_MODE[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean isSupportFrameRate(int frameRate) {
        for (int i = 0; i < SettingsConstant.FRAME_RATE_MODE.length; i++) {
            if (frameRate == SettingsConstant.FRAME_RATE_MODE[i]) {
                return true;
            }
        }
        return false;
    }

    private boolean isSupportBitRate(int bitRate) {
        for (int i = 0; i < SettingsConstant.BIT_RATE_MODE.length; i++) {
            if (bitRate == SettingsConstant.BIT_RATE_MODE[i]) {
                return true;
            }
        }
        return false;
    }

    private void selectVideoSett(String pcSrcId) {
        int value = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_AUTO_RECEIVE_VIDEO, 0);
        int cameraId = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_VIDEO_CAMERA, 0);
        if (cameraId == 1) {
            cameraId = 2;
        }
        //表示关闭
        if (value == 0) {
            byte buffer[] = new byte[4];
            int action = 8;
            //指令
            buffer[1] = (byte) ((action >> 0) & 0xFF);
            buffer[0] = (byte) ((action >> 8) & 0xFF);
            buffer[2] = (byte) 0;
            buffer[3] = (byte) cameraId;
            UctClientApi.setPhoneComment(pcSrcId, "", "", buffer, buffer.length);
        } else {//表示打开
            byte buffer[] = new byte[4];
            int action = 8;
            //指令
            buffer[1] = (byte) ((action >> 0) & 0xFF);
            buffer[0] = (byte) ((action >> 8) & 0xFF);
            buffer[2] = (byte) 1;
            buffer[3] = (byte) cameraId;
            UctClientApi.setPhoneComment(pcSrcId, "", "", buffer, buffer.length);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setPhoneComment(String pcNumber) {
        /* 设置手机相关信息 */
        StringBuilder sb = new StringBuilder();
        sb.append("PRODUCT: ").append(Build.PRODUCT).append("\n");
        sb.append("UCTAPP: ").append(AppUtils.getVersionName(UctApplication.getInstance())).append("\n");
        sb.append("dll: ").append(UctClientApi.getSoVersion()).append("\n");
        sb.append("MODEL: ").append(Build.MODEL).append("\n");
        sb.append("RELEASE: ").append(Build.VERSION.RELEASE).append("\n");
        sb.append("SYSTEM: ").append(Build.DISPLAY).append("\n");
        sb.append("RADIOVERSION: ").append(Build.getRadioVersion()).append("\n");

        String val = sb.toString();
        byte[] conte = val.getBytes();
        int length = conte.length;
        byte buffer[] = new byte[2 + length];
        /*buffer[3]= (byte)((length>>0) & 0xFF);
        buffer[2]= (byte)((length>>8) & 0xFF);
		buffer[1]= (byte)((length>>16) & 0xFF);
		buffer[0]= (byte)((length>>24) & 0xFF);*/
        //命令+内容
        int action = 6;
        buffer[1] = (byte) ((action >> 0) & 0xFF);
        buffer[0] = (byte) ((action >> 8) & 0xFF);
        System.arraycopy(conte, 0, buffer, 2, length);
        UctClientApi.setPhoneComment(pcNumber, "", "", buffer, buffer.length);
    }

    private void selectAutoUpgradeSwitch(String pcSrcId) {
        int value = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_AUTO_UPGRADE_SWITCH, 0);
        if (value == 0) {// 表示关闭
            byte buffer[] = new byte[4];
            int action = 2;
            //指令
            buffer[1] = (byte) ((action >> 0) & 0xFF);
            buffer[0] = (byte) ((action >> 8) & 0xFF);
            buffer[2] = (byte) 0;
            buffer[3] = (byte) 0;
            UctClientApi.setPhoneComment(pcSrcId, "", "", buffer, buffer.length);
        } else {// 表示打开
            byte buffer[] = new byte[4];
            int action = 2;
            //指令
            buffer[1] = (byte) ((action >> 0) & 0xFF);
            buffer[0] = (byte) ((action >> 8) & 0xFF);
            buffer[2] = (byte) 1;
            buffer[3] = (byte) 0;
            UctClientApi.setPhoneComment(pcSrcId, "", "", buffer, buffer.length);
        }
    }

    private void groupLockRespon(String pcSrcId) {
        String lockGroupInfo = (String) UctClientApi.getUserData(SettingsConstant.SETTINGS_LOCK_GROUP, "");
        //        String[] lockGroupList = UctApi.lockGroupSize(lockGroupInfo);
        //没锁定组
        //        if (lockGroupList == null || TextUtils.isEmpty(lockGroupInfo)) {
        if (TextUtils.isEmpty(lockGroupInfo)) {
            /**
             * 1byte（0-无锁定，1锁定）+Nbyte（锁定的组号码，0结尾字符串形式）
             0x1D: 设置终端锁定组信息
             1byte（0-解锁定，1锁定）+Nbyte（锁定的组号码，0结尾字符串形式）
             */
            //命令+锁定标志+组号码 未锁定是只添加标志
            byte buffer[] = new byte[3];
            int action = 28;
            //指令
            buffer[1] = (byte) ((action >> 0) & 0xFF);
            buffer[0] = (byte) ((action >> 8) & 0xFF);
            buffer[2] = (byte) 0;
            UctClientApi.setPhoneComment(pcSrcId, "", "", buffer, buffer.length);

        } else {
            //锁定组
            byte[] conte = lockGroupInfo.getBytes();
            byte buffer[] = new byte[2 + 1 + conte.length];
            int action = 28;
            //指令
            buffer[1] = (byte) ((action >> 0) & 0xFF);
            buffer[0] = (byte) ((action >> 8) & 0xFF);
            buffer[2] = (byte) 1;
            System.arraycopy(conte, 0, buffer, 3, conte.length);
            UctClientApi.setPhoneComment(pcSrcId, "", "", buffer, buffer.length);
        }
    }

    private void lockGroup(String tel, int addOrRemove) {
        //锁定组
        String sbuffer = addOrRemoveGroup(tel, addOrRemove);
        PrintLog.d("【锁定组号码】tel=" + tel + "【锁定组后在组信息 lockGroup=】" + sbuffer);
        if (!TextUtils.isEmpty(sbuffer)) {
            UctClientApi.lockGroup(sbuffer);
        } else {
            UctClientApi.lockGroup("");
        }
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_LOCK_GROUP, sbuffer);
    }

    /**
     * @param tel
     * @param addOrRemove 1为添加到字符串 -1为删除
     * @return
     */
    private String addOrRemoveGroup(String tel, int addOrRemove) {
        if (TextUtils.isEmpty(tel)) {
            PrintLog.w("【组号码为空】addOrRemove=" + addOrRemove);
            return "";
        }
        //首先判断是否已经锁定组
        String lockGroupValue = (String) UctClientApi.getUserData(SettingsConstant.SETTINGS_LOCK_GROUP, "");
        //如果是锁定组 判断当前组号码是否已经锁定
        if (addOrRemove == 1) {
            boolean isLockGroup = isLockGroup(lockGroupValue, tel);
            //如果已经锁定直接返回..
            if (isLockGroup) {
                PrintLog.d("【tel】=" + tel + "已经锁定");
                return lockGroupValue;
            }
        }

        //已经存在锁定组 并且已经存在多组锁定
        StringBuffer sbuffer = new StringBuffer();
        //如果锁定组不为空
        if (!TextUtils.isEmpty(lockGroupValue)) {
            if (lockGroupValue.contains(SettingsConstant.LOCK_GROUP_REGULAREXPRESSION)) {
                String lockgroupList[] = lockGroupValue.split(SettingsConstant.LOCK_GROUP_REGULAREXPRESSION);
                int len = lockgroupList.length;
                PrintLog.d("【锁定组的个数 size=】" + len);
                for (int i = 0; i < len; i++) {
                    String groupTel = lockgroupList[i];
                    if (isGroup(groupTel)) {
                        //锁定组
                        if (addOrRemove != -1) {
                            sbuffer.append(groupTel + SettingsConstant.LOCK_GROUP_REGULAREXPRESSION);
                        } else {
                            //解除锁定组
                            if (!groupTel.equals(tel)) {
                                sbuffer.append(groupTel + SettingsConstant.LOCK_GROUP_REGULAREXPRESSION);
                            }
                        }
                    }
                }
            } else {
                PrintLog.e("【锁定组不包含字符串:】" + SettingsConstant.LOCK_GROUP_REGULAREXPRESSION + "【当前锁定组 value=】" + lockGroupValue);
            }
        }
        if (addOrRemove == 1) {
            sbuffer.append(tel + SettingsConstant.LOCK_GROUP_REGULAREXPRESSION);
        }
        String lockGroupStr = sbuffer.toString();
        return lockGroupStr;
    }

    /**
     * 当前组是否在锁定组里
     *
     * @param lockGroupTel
     * @param currentTel
     * @return
     */
    private boolean isLockGroup(String lockGroupTel, String currentTel) {
        if (TextUtils.isEmpty(lockGroupTel) || TextUtils.isEmpty(currentTel)) {
            PrintLog.w("【lockGroupTel=】" + lockGroupTel + " 【currentTel==】" + currentTel);
            return false;
        }
        if (lockGroupTel.contains(SettingsConstant.LOCK_GROUP_REGULAREXPRESSION)) {
            String lockgroupList[] = lockGroupTel.split(SettingsConstant.LOCK_GROUP_REGULAREXPRESSION);
            int len = lockgroupList.length;
            PrintLog.d("【锁定组的个数 size=】" + len);
            for (int i = 0; i < len; i++) {
                String groupTel = lockgroupList[i];
                if (groupTel.equals(currentTel)) {
                    return true;
                }
            }
        } else {
            return false;
        }
        return false;
    }

    private boolean isGroup(String tel) {
        List<GroupData> groupDatas = GroupInfoCallback.getInstance().getmGroupList();
        if (groupDatas != null && groupDatas.size() > 0) {
            for (int i = 0; i < groupDatas.size(); i++) {
                if (groupDatas.get(i).groupId.equals(tel)) {
                    return true;
                }
            }
        }
        return false;
    }

}
