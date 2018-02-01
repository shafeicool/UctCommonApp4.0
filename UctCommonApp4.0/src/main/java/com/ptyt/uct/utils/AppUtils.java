package com.ptyt.uct.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.callback.CallCallBack;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.common.SettingsConstant;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.services.MessageManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

//
//import android.app.Activity;
//import android.app.ActivityManager;
//import android.app.ActivityManager.RunningAppProcessInfo;
//import android.app.ActivityManager.RunningTaskInfo;
//import android.app.AlertDialog;
//import android.app.KeyguardManager;
//import android.app.KeyguardManager.KeyguardLock;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.ApplicationInfo;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;

//import android.content.pm.ResolveInfo;
//import android.content.pm.Signature;
//import android.graphics.Point;
//import android.hardware.Camera;
//import android.hardware.Camera.Parameters;
//import android.hardware.Camera.Size;
//import android.net.ConnectivityManager;
//import android.net.wifi.WifiManager.WifiLock;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.PowerManager;
//import android.os.PowerManager.WakeLock;
//import android.os.Vibrator;
//import android.provider.Settings;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.Display;
//import android.view.Gravity;
//import android.view.KeyEvent;
//import android.view.View;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import com.android.uct.UctApi;
//import com.android.uct.util.PreferenceConstant;
//import com.android.uct.util.PrintLog;
//import com.android.uct.util.ReadPreference;
//import com.android.uct.util.StrUtils;
//import com.android.uct.util.StringUtils;
//import com.android.uct.util.UCTUtils;
//import com.ptyt.lib.db.GroupData;
//import com.ptyt.lib.db.User;
//import com.ptyt.uct.activity.CallScreenActivity;
//import com.ptyt.uct.activity.MsgChatActivity;
//import com.ptyt.uct.common.CallManager;
//import com.ptyt.uct.common.PtytContext;
//import com.ptyt.uct.common.UctApplication;
//import com.ptyt.uct.common.UctProxy;
//import com.ptyt.uct.exception.ActionNotFoundException;
//import com.ptyt.uct.fragement.BaseFragement;
//import com.ptyt.uct.fragement.CallGAudioFragement;
//import com.ptyt.uct.fragement.CallGVideoFragement;
//import com.ptyt.uct.fragement.CallSAVideoFragement;
//import com.ptyt.uct.fragement.CallSAudioFragement;
//import com.ptyt.uct.fragement.CallSVideoFragement;
//import com.ptyt.uct.model.BaseFragementBean;
//import com.xigu.uct.ui.R;
//
//import java.io.ByteArrayInputStream;
//import java.security.cert.CertificateException;
//import java.security.cert.CertificateFactory;
//import java.security.cert.X509Certificate;
//import java.util.ArrayList;
//import java.util.List;
//

/**
 * @Description: 应用工具类
 * @Date: 2017/4/26
 * @Author: ShaFei
 * @Version: V1.0
 */

public class AppUtils {
    //	private static Context curActivety = null;
    //	private static WakeLock powerManagerWakeLock = null;
    //	// 定义一个WifiLock
    //	private static WifiLock mWifiLock = null;
    //	private static Toast pre_toast = null;
    //	private static int m_isLaunchAsHome = -1;
    ////	private static BackgroundTaskThead backgroundTaskThread = new BackgroundTaskThead();
    //	private static AlertDialog g_alertDialog = null;
    //	private static KeyguardLock keyguardLock;
    private static long lastClickTime1;
    private static long lastClickTime2;
    private static long lastClickTime3;
    private static long lastDownloadTime;
    //	private static CallMessageListener callMessageListner;
    //	private static Handler myHandler;
    //	private static HandlerThread myHandlerThread;
    //	private static List<Size> sizes = null;
    //	/**
    //	 * 呼叫打开页面
    //	 */
    //	private static final int Call_NEW=1;
    //	/**
    //	 * 呼叫错误
    //	 */
    //	private static final int CALL_ERROR=2;
    //	/**
    //	 * 呼叫更新页面
    //	 */
    //	private static final int CALL_UPDATE=3;
    //
    //

    /**
     * @param
     * @return true   快速点击
     * false  非快速点击
     * @description 防止快速点击
     */
    public synchronized static boolean isFastClick(int duration) {
        long time = System.currentTimeMillis();
        long range = Math.abs(time - lastClickTime1);
        if (range < duration) {
            return true;
        }
        lastClickTime1 = time;
        return false;
    }

    /**
     * @param
     * @return true   快速点击
     * false  非快速点击
     * @description 防止快速点击
     */
    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        long range = Math.abs(time - lastClickTime1);
        if (range < 500) {
            return true;
        }
        lastClickTime1 = time;
        return false;
    }

    /**
     * @param
     * @return true   快速点击
     * false  非快速点击
     * @description 防止快速点击
     */
    public synchronized static boolean isFastClick2() {
        long time = System.currentTimeMillis();
        long range = Math.abs(time - lastClickTime2);
        if (range < 1000) {
            return true;
        }
        lastClickTime2 = time;
        return false;
    }

    /**
     * @param
     * @return true   快速点击
     * false  非快速点击
     * @description 防止快速点击
     */
    public synchronized static boolean isFastClick3() {
        long time = System.currentTimeMillis();
        long range = Math.abs(time - lastClickTime3);
        if (range < 3600000) {
            return true;
        }
        lastClickTime3 = time;
        return false;
    }

    /**
     * @param
     * @return true   已经下载了
     * false  需要下载了
     * @description 防止频繁下载通讯录
     */
    public synchronized static boolean isDownloadContact() {
        long time = System.currentTimeMillis();
        long range = Math.abs(time - lastDownloadTime);
        if (range < 60000) {
            return true;
        }
        lastDownloadTime = time;
        return false;
    }

    /**
     * @param
     * @return
     * @description 获取本地IP地址
     */
    public static int getLocalIp(Context context) {
        int ret = 0x7f000001; // 127.0.0.1
        boolean hasSiteLocalAddress = false;
        // 获取wifi服务
        if (context != null) {
            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                WifiInfo info = wifiManager.getConnectionInfo();
                if (info != null
                        && SupplicantState.COMPLETED.equals(info
                        .getSupplicantState())) {
                    int ipAddress = info.getIpAddress();
                    if (ipAddress != 0) {
                        ret = ipAddress;
                        hasSiteLocalAddress = true;
                    }
                }
            }
        }

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();

                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        Inet4Address inet4Address = (Inet4Address) inetAddress;
                        byte[] data = inet4Address.getAddress();
                        if (!inet4Address.isSiteLocalAddress()) {
                            return bytesToInt(data, 0);
                        } else if (!hasSiteLocalAddress) {
                            hasSiteLocalAddress = true;
                            ret = bytesToInt(data, 0);
                        }

                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        int result = ret == 0 ? 0x7f000001 : ret;
        return result;
    }

    public static int bytesToInt(byte[] bytes, int start) {
        int value = ((bytes[start + 3] & 255))
                | ((bytes[start + 2] & 255) << 8)
                | ((bytes[start + 1] & 255) << 16)
                | ((bytes[start] & 255) << 24);
        return value;
    }

    private static Vibrator vibrator;

    /**
     * @param isRepeat 是否反复震动，如果是true，反复震动，如果是false，只震动一次
     * @return
     * @description 手机震动方法
     */
    public static void startVibrate(boolean isRepeat) {
        vibrator = (Vibrator) UctApplication.getInstance().getSystemService(Service.VIBRATOR_SERVICE);
        //等待1秒，震动1秒
        long[] pattern = {1000, 1000};
        // 第二个参数为重复次数，-1为不重复，0为一直震动
        vibrator.vibrate(pattern, isRepeat ? 0 : -1);
    }

    /**
     * @param time 震动指定时间 ，数据类型long，单位为毫秒
     * @return
     * @description 手机震动方法
     */
    public static void startVibrate(long time) {
        vibrator = (Vibrator) UctApplication.getInstance().getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(time);
    }

    /**
     * @param
     * @return
     * @description 手机停止震动方法
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public static void stopVibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.cancel();
            vibrator = null;
        }
    }


    /**
     * @param res 播放源
     * @return
     * @description 开始响铃
     */
    public static void startRinging2(int res) {

        PrintLog.i("startRinging2()");
        if (mediaPlayer2 == null) {
            mediaPlayer2 = MediaPlayer.create(UctApplication.getInstance(), res);
            mediaPlayer2.setLooping(false);
            mediaPlayer2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    PrintLog.i("onCompletion()");
                    //mediaPlayer2.start();
                }
            });
        }
        mediaPlayer2.start();
    }

    private static MediaPlayer mediaPlayer2;
    private static MediaPlayer mMediaPlayer;

    /**
     * @param res      播放源
     * @param isRepeat 是否重复播放
     * @return
     * @description 开始响铃
     */
    public static void startRinging(int res, boolean isRepeat) {
        try {
            Uri uri = Uri.parse("android.resource://" + UctApplication.getInstance().getPackageName() + "/" + res);
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(UctApplication.getInstance(), uri);
            } else {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(UctApplication.getInstance(), uri);
            }
            mMediaPlayer.setLooping(isRepeat); //循环播放
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param path     播放源
     * @param isRepeat 是否重复播放
     * @return
     * @description 开始响铃
     */
    public static void startRinging(String path, boolean isRepeat) {
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(path);
            } else {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(path);
            }
            mMediaPlayer.setLooping(isRepeat); //循环播放
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param
     * @return
     * @description 停止响铃
     */
    public static void stopRinging() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * @param isRepeat 是否重复
     * @return
     * @description 开始播放振铃
     */
    public static void startPlayMedia(int res, boolean isRepeat) {
        int mode = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_AUDIO_RING, SettingsConstant.RING_MODE[0]);
        if (mode == SettingsConstant.RING_MODE[0]) {
            startRinging(res, isRepeat);
        } else if (mode == SettingsConstant.RING_MODE[1]) {
            startVibrate(isRepeat);
        } else if (mode == SettingsConstant.RING_MODE[2]) {
            startRinging(res, isRepeat);
            startVibrate(isRepeat);
        }
    }

    /**
     * 业务来时铃声提醒
     *
     * @param businessTag 业务类别
     */
    public static void startRingtone(Activity activity, int businessTag) {

        //音道控制
        //        UctClientApi.saveUserData(SettingsConstant.SETTINGS_AUDIO_ROUTE, 0);//免提
        CallCallBack.getInstance().setVoiceCallMode(AudioManager.STREAM_MUSIC);
        int mode = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_AUDIO_RING, SettingsConstant.RING_MODE[0]);
        switch (businessTag) {
            case ConstantUtils.AUDIO_SCALL://呼叫
            case ConstantUtils.VIDEO_SCALL:
                if (mode == SettingsConstant.RING_MODE[0]) {//振铃模式 0-铃声 1-震动 2-铃声+震动
                    startRinging(R.raw.sound_ring1, true);
                } else if (mode == SettingsConstant.RING_MODE[1]) {
                    startVibrate(true);
                } else if (mode == SettingsConstant.RING_MODE[2]) {
                    startRinging(R.raw.sound_ring1, true);
                    startVibrate(true);
                }
                break;
            case ConstantUtils.UPLOAD_VIDEO://纯视频
            case ConstantUtils.DOWNLOAD_VIDEO:
                if (mode == SettingsConstant.RING_MODE[0]) {//振铃模式 0-铃声 1-震动 2-铃声+震动
                    startRinging(R.raw.sound_video1, true);
                } else if (mode == SettingsConstant.RING_MODE[1]) {
                    startVibrate(true);
                } else if (mode == SettingsConstant.RING_MODE[2]) {
                    startRinging(R.raw.sound_video1, true);
                    startVibrate(true);
                }
                break;
        }
    }

    /**
     * @return
     * @description 开始播放振铃
     */
    public static void startGCallPlayMedia(int res) {
        PrintLog.e("startGCallPlayMedia()");
        /*ptyt start 加了这句话之后播放一次之后后面没有声音*/
        //        UctClientApi.saveUserData(SettingsConstant.SETTINGS_AUDIO_ROUTE, 0);//免提
        /*ptyt end*/
        ((AudioManager) UctApplication.getInstance().getSystemService(Context.AUDIO_SERVICE)).setMode(AudioManager.MODE_NORMAL);
        int mode = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_AUDIO_RING, SettingsConstant.RING_MODE[0]);
        if (mode == SettingsConstant.RING_MODE[0]) {
            startRinging(res, false);
        } else if (mode == SettingsConstant.RING_MODE[1]) {
            vibrator = (Vibrator) UctApplication.getInstance().getSystemService(Service.VIBRATOR_SERVICE);
            //等待1秒，震动1秒
            long[] pattern = {1, 300};
            // 第二个参数为重复次数，-1为不重复，0为一直震动
            vibrator.vibrate(pattern, -1);
        } else if (mode == SettingsConstant.RING_MODE[2]) {
            startRinging(res, false);
            vibrator = (Vibrator) UctApplication.getInstance().getSystemService(Service.VIBRATOR_SERVICE);
            //等待1秒，震动1秒
            long[] pattern = {1, 300};
            // 第二个参数为重复次数，-1为不重复，0为一直震动
            vibrator.vibrate(pattern, -1);
        }
    }


    //    /**
    //     * @param isStartPtt
    //     * @return
    //     * @description 开始播放振铃
    //     */
    //    public static void startPttRing(int res, boolean isStartPtt) {
    //        int mode = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_AUDIO_RING, SettingsConstant.RING_MODE);
    //        switch (mode) {
    //            case SettingsConstant.RING_MODE:
    //                startRinging(res, isRepeat);
    //                break;
    //            case SettingsConstant.VIBRATE_MODE:
    //                startVibrate(isRepeat);
    //                break;
    //            case SettingsConstant.RING_VIBRATE_MODE:
    //                startRinging(res, isRepeat);
    //                startVibrate(isRepeat);
    //                break;
    //        }
    //    }


    /**
     * @param
     * @return
     * @description 停止播放振铃
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public static void stopPlayMedia() {
        stopRinging();
        stopVibrate();
    }

    public static void playMessageRingtone() {
        int mode = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_AUDIO_RING, SettingsConstant.RING_MODE[0]);
        if (mode == SettingsConstant.RING_MODE[0]) {
            playRingtone();
        } else if (mode == SettingsConstant.RING_MODE[1]) {
            startVibrate(300);
        } else if (mode == SettingsConstant.RING_MODE[2]) {
            playRingtone();
            startVibrate(300);
        }
    }

    private static Uri ringtoneUri;
    private static Ringtone rt;

    private static void playRingtone() {
        if (ringtoneUri == null) {
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        if (rt == null) {
            rt = RingtoneManager.getRingtone(UctApplication.getInstance(), ringtoneUri);
        }
        if (rt != null) {
            if (rt.isPlaying()) {
                return;
            } else {
                CallCallBack.getInstance().setVoiceCallMode(AudioManager.STREAM_NOTIFICATION);
                rt.play();
            }
        }
    }

    public static void releaseRingtone() {
        if (rt != null && rt.isPlaying()) {
            rt.stop();
            rt = null;
        }
        if (ringtoneUri != null) {
            ringtoneUri = null;
        }
    }

    public static int getAudioMode() {
        AudioManager audioManager = (AudioManager) UctApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getMode();
    }

    /**
     * 获取应用程序名称
     */
    @Nullable
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * [获取应用程序版本名称信息]
     *
     * @param context
     * @return 当前应用的版本名称
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断当前界面是否是桌面,如果是桌面则跳转到MainActivity中
     */
    public static boolean isHome(Context mContext) {
        ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        return getHomes(mContext).contains(rti.get(0).topActivity.getPackageName());
    }

    /**
     * 获得属于桌面的应用的应用包名称
     *
     * @return 返回包含所有包名的字符串列表
     */
    private static List<String> getHomes(Context mContext) {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = mContext.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }

    public static void sendMsg(Long conversationId, String second, String localPath, String msgId, String msgDstNo, boolean isGroupNo) {
        String userName = AppContext.getAppContext().getLoginNumber();
        String remotePath = SDCardUtils.getChatRemotePath(msgDstNo, msgId, "mp3");
        String msgContent = msgId + ".mp3";
        byte[] _tmpContent = null;
        try {
            _tmpContent = (msgContent).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ConversationMsg conversationMsg = new ConversationMsg();
        conversationMsg.setMsgConversationId(conversationId);
        conversationMsg.setMsgTime(StrUtils.getCurrentTimes());
        conversationMsg.setMsgSrcNo(userName);
        conversationMsg.setMsgDstNo(msgDstNo);
        //判断是不是一对多消息
        if (isGroupNo) {
            conversationMsg.setGroupNo(msgDstNo);
        }
        conversationMsg.setMsgUctId(msgId);
        conversationMsg.setMsgType(MessageDBConstant.INFO_TYPE_AUDIO);
        conversationMsg.setMsgTxtSplit(MessageDBConstant.UNSEGMENTED_MSG);
        conversationMsg.setRecvCfm(MessageDBConstant.NEEDLESS_CFM);
        conversationMsg.setContent(msgContent);
        conversationMsg.setContentLength(_tmpContent.length);
        conversationMsg.setMsgDirection(MessageDBConstant.IMVT_TO_MSG);
        conversationMsg.setLocalImgPath(localPath);
        conversationMsg.setRemoteImgPath(remotePath);
        conversationMsg.setMsgStatus(MessageDBConstant.MSG_STATUS_WAIT_SENDING);
        conversationMsg.setReadStatus(MessageDBConstant.ALREAD_MSG);
        conversationMsg.setRecvNotify(MessageDBConstant.BASIC_NOTIFY);
        conversationMsg.setAudioLength(Integer.parseInt(second));
        conversationMsg.setAudioPlayStatus(MessageDBConstant.AUDIO_STOP_STATUS);
        conversationMsg.setAudioReadStatus(MessageDBConstant.AUDIO_ALREAD_MSG);
        MessageManager.getInstane().sendMessage(conversationMsg);
        PrintLog.d("SEND_PHOTO_MESSAGE--conversationId=" + conversationId + ", msgSrcNo=" + userName + ", msgDstNo=" + msgDstNo
                + ", groupNo=" + conversationMsg.getGroupNo() + ", smsid=" + msgId + ", msgType=" + conversationMsg.getMsgType()
                + ", localImgPath=" + conversationMsg.getLocalImgPath());
    }

    //
    //	public static ApplicationInfo checkApkExist(Context context, String packageName) {
    //		if (TextUtils.isEmpty(packageName))
    //			return null;
    //		try {
    //			ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,PackageManager.GET_UNINSTALLED_PACKAGES);
    //			if(info!=null){
    //				return info;
    //			}else{
    //				return null;
    //			}
    //		} catch (NameNotFoundException e) {
    //			return null;
    //		}
    //	}
    //
    //	static {
    //		myHandlerThread= new HandlerThread("AppUtils");
    //		myHandlerThread.start();
    //		myHandler = new Handler(myHandlerThread.getLooper());
    //	}
    //
    //	public static void runOnMyThread(int delayMs, Runnable command) {
    //		if (myHandler != null) {
    //			if (delayMs <= 0)
    //				myHandler.post(command);
    //			else
    //				myHandler.postDelayed(command, delayMs);
    //		}
    //}
    //
    //
    public static boolean int2Boolean(int value) {
        if (value == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断 悬浮窗口权限是否打开
     *
     * @param context
     * @return true 允许  false禁止
     */
    public static boolean getAppOps(Context context) {
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW;
            arrayOfObject1[1] = Integer.valueOf(Binder.getCallingUid());
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1)).intValue();//checkOp(String op, int uid, String packageName)
            PrintLog.e("m=" + m);
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    //	public static boolean string2Boolean(String value){
    //		int value1;
    //		boolean isMatch = StrUtils.isMatchs(value);
    //		if(isMatch){
    //			value1 = Integer.parseInt(value);
    //		}else{
    //			return false;
    //		}
    //		if(value1==0){
    //			return false;
    //		}else{
    //			return true;
    //		}
    //	}
    //
    //	 /**
    //     * 判断某个界面是否在前台
    //     *
    //     * @param context
    //     * @param className
    //     *            某个界面名称
    //     */
    //    public static boolean isForegroundCamera(Context context) {
    //        if (context == null) {
    //            return false;
    //        }
    //        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    //        List<RunningTaskInfo> list = am.getRunningTasks(1);
    //        if (list != null && list.size() > 0) {
    //            ComponentName cpn = list.get(0).topActivity;
    //            String className1 = cpn.getClassName();
    //            PrintLog.d("className1=="+className1);
    //            if(className1.equals("com.android.camera.Camera") || className1.equals("com.android.camera.VideoCamera")){
    //            	return true;
    //            }
    //           /* if (className.equals(cpn.getClassName())) {
    //                return true;
    //            }*/
    //        }
    //        return false;
    //    }
    //
    //    public static boolean isMainActivity(Context context) {
    //        if (context == null) {
    //            return false;
    //        }
    //        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    //        List<RunningTaskInfo> list = am.getRunningTasks(1);
    //        if (list != null && list.size() > 0) {
    //            ComponentName cpn = list.get(0).topActivity;
    //            String className1 = cpn.getClassName();
    //            PrintLog.d("className1=="+className1);
    //            if(className1.equals("com.ptyt.uct.activity.MainActivity") ||className1.equals("com.ptyt.uct.activity.CallScreenActivity")){
    //            	      return true;
    //            }
    //           /* if (className.equals(cpn.getClassName())) {
    //                return true;
    //            }*/
    //        }
    //        return false;
    //    }
    //
    //
    //	public static void delayedRun(Runnable r, int delayMillis) {
    //		com.android.uct.util.UCTUtils.getMainLoopHander().postDelayed(r, delayMillis);
    //	}
    //
    //	public static void runOnMyThread(Runnable command) {
    //		runOnMyThread(0, command);
    //	}
    //
    //	public static void cancelDelayRun(Runnable command) {
    //		myHandler.removeCallbacks(command);
    //	}
    //
    //	public static void initActivity(Context pcurActivety) {
    //		curActivety = pcurActivety.getApplicationContext();
    //	}
    //
    //	public static void showOrCloseMainFps(String action,Context mContext){
    //		Intent intent = new Intent(action);
    //		mContext.sendBroadcast(intent);
    //	}
    //
    //	/**
    //	 * "1"组呼使用MIC，"0"组呼不用MIC，"2"停止录像
    //	 * @param mContext
    //	 * @param flag
    //	 */
    //	public static void closeCamera(Context mContext,String flag,long sleep){
    //		Intent cameraIntent = new Intent("com.xigu.uct.ui.sendflagtocamera");
    //		cameraIntent.putExtra("isGroup", flag);
    //		mContext.sendBroadcast(cameraIntent);
    //		PrintLog.d("【关闭摄像头前】flag:"+flag);
    //		long startTime = System.currentTimeMillis();
    //		try {
    //			Thread.sleep(sleep);
    //		} catch (InterruptedException e) {
    //			e.printStackTrace();
    //		}
    //		long endTime = System.currentTimeMillis();
    //		PrintLog.d("【关闭摄像头后->1】flag:"+flag+"【时间间隔】"+(endTime-startTime));
    //	}
    //
    //	public static void sendMessage(String tel){
    //		User user = ReadPreference.readUserInfo();
    //		if(user!=null && user.getUserName().equals(tel)){
    //			com.ptyt.uct.utils.ToastUtils.getToast().showMessage(UctApplication.getInstances(), "不支持此号码", -1);
    //			return;
    //		}
    //		Intent intents = new Intent(UctApplication.getInstances(),MsgChatActivity.class);
    //		intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //		intents.putExtra(MsgChatActivity.MSY_TYPE,MsgChatActivity.MSY_VALUE2);
    //		intents.putExtra(MsgChatActivity.TEL_NUMBER,tel);
    //		UctApplication.getInstances().startActivity(intents);
    //	}
    //
    //	public static void setCallMessageListner(CallMessageListener mcallMessageListner) {
    //		callMessageListner = mcallMessageListner;
    //	}
    //
    //	/**
    //	 * 所有呼叫启动的入口
    //	 * Intent.FLAG_ACTIVITY_NEW_TASK
    //					| Intent.FLAG_ACTIVITY_SINGLE_TOP
    //					| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
    //					| Intent.FLAG_ACTIVITY_NO_USER_ACTION
    //	 */
    //	static UctProxy uctProxy;
    //	public static void startCallActivity(BaseFragementBean fragement){
    //		//每次进入，都需要清空以前设置的数据
    //		Intent intents = new Intent();
    //		if(fragement==null){
    //			intents.setClassName(UctApplication.getInstances().getPackageName(), CallScreenActivity.class.getName());
    //			intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //			UctApplication.getInstances().startActivity(intents);
    //			return;
    //		}
    //		int callAction = 0;
    //		if(fragement!=null){
    //			callAction = fragement.getAction();
    //		}
    //		if(uctProxy==null){
    //			uctProxy = UctProxy.getInstance();
    //		}
    //		if(callAction != CallSAVideoFragement.UCT_STARTRECORDREQ && (uctProxy == null || !uctProxy.isUserLogin()) ){
    //			com.ptyt.uct.utils.ToastUtils.getToast().show(UctApplication.getInstances(),UctApplication.getInstances().getString(R.string.user_has_no_login));
    //			return;
    //		}
    //
    //		int callCode = verifyCall((BaseFragementBean) fragement);
    //		PrintLog.d("【callCode = "+callCode+"】");
    //		if(callCode == CALL_ERROR){
    //			return;
    //		}
    //		String fragementName = fragement.getFragementName();
    //		//组呼的时候是否亮屏是根据配置来设置的
    //		if(callCode == Call_NEW && (!TextUtils.isEmpty(fragementName) &&fragementName.equals(CallGAudioFragement.class.getName()))){
    //			boolean isMainAct = isMainActivity(UctApplication.getInstances());
    //			fragement.setMainAct(isMainAct);
    //		}else{
    //			AppUtils.acquireWakeLock();
    //			fragement.setMainAct(true);
    //		}
    //
    //		//第一路呼叫 或者新创建的要显示页面
    //		if(callMessageListner == null/* || callCode == Call_NEW*/){
    //			PrintLog.d("【callMessageListner为空】");
    //			intents.setClassName(UctApplication.getInstances().getPackageName(), CallScreenActivity.class.getName());
    //			intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //			intents.putExtra(CallScreenActivity.FRAGEMENTNAME_KEY,fragement);
    //			UctApplication.getInstances().startActivity(intents);
    //		}else{
    //			PrintLog.d("【callMessageListner不为空】");
    //			if(callCode == Call_NEW){
    //				intents.setClassName(UctApplication.getInstances().getPackageName(), CallScreenActivity.class.getName());
    //				intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //				UctApplication.getInstances().startActivity(intents);
    //			}
    //			callMessageListner.callMessage(CallScreenActivity.FRAGEMENTNAME_KEY,fragement);
    //		}
    //	}
    //
    //	public static BaseFragement newFragement(BaseFragementBean fragement){
    //		BaseFragement baseFragement = null;
    //		try {
    //			baseFragement = (BaseFragement) Class.forName(fragement.getFragementName()).newInstance();
    //			baseFragement.setFragementBean(fragement);
    //		} catch (InstantiationException e) {
    //			e.printStackTrace();
    //		} catch (IllegalAccessException e) {
    //			e.printStackTrace();
    //		} catch (ClassNotFoundException e) {
    //			e.printStackTrace();
    //		}
    //		return baseFragement;
    //	}
    //
    //	public static String getTopActivity(Context context)
    //	{
    //	     ActivityManager manager = (ActivityManager)context.getSystemService(Activity.ACTIVITY_SERVICE) ;
    //	     List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1) ;
    //
    //	     if(runningTaskInfos != null)
    //	       return (runningTaskInfos.get(0).topActivity).toString() ;
    //	          else
    //	       return null ;
    //	}
    //
    //	/**
    //	 * Description: 校验呼叫是否合法
    //	 * @param fragementBean
    //	 * @return  1表示合法的呼叫 新呼叫要显示页面 2为不合法的呼叫直接拒绝 3为合法呼叫但是页面已经创建只需要更新
    //	 * @see
    //	 */
    //	private static synchronized int verifyCall(BaseFragementBean fragementBean){
    //		int callAction = fragementBean.getAction();
    //		CallManager callManager = PtytContext.getInstance().getCallManager();
    //		String  currentFragements  = callManager.getUserCallByKey(fragementBean.getKey());
    //		PrintLog.d("callAction=【"+callAction + "】，currentFragements = "+currentFragements);
    //		switch (callAction) {
    //		//单呼被叫
    //		case CallSAudioFragement.UCT_SCALLMTIND:
    //			if(currentFragements==null){
    //				callManager.addUserCall(fragementBean.getKey());
    //				return Call_NEW;
    //			}
    //			return CALL_ERROR;
    //
    //		//单呼主叫处理
    //		case CallSAudioFragement.UCTSCALLMOREQ:
    //			if(currentFragements == null){
    //				callManager.addUserCall(fragementBean.getKey());
    //				callManager.addOperateCall(fragementBean.getOperateCall());
    //				return Call_NEW;
    //			}
    //			return CALL_ERROR;
    //
    //		//会议 主叫
    //		case CallSAudioFragement.UCT_SCALLMOREQMEET:
    //			if(currentFragements == null){
    //				callManager.addUserCall(fragementBean.getKey());
    //				callManager.addOperateCall(fragementBean.getOperateCall());
    //				return Call_NEW;
    //			}
    //			return CALL_ERROR;
    //
    //		case CallSAudioFragement.UCT_SCALLMTINDMEET:
    //			if(currentFragements == null){
    //				callManager.addUserCall(fragementBean.getKey());
    //				return Call_NEW;
    //			}
    //			return CALL_ERROR;
    //
    //		//单呼主叫确认
    //		case CallSAudioFragement.UCT_SCALLMOCFM:
    //			if(currentFragements != null){
    //				return CALL_UPDATE;
    //			}
    //			return CALL_ERROR;
    //
    //		//组呼主叫接口
    //		case CallGAudioFragement.UCTGCALLMOREQ:
    //			if(currentFragements == null){
    //				callManager.addUserCall(fragementBean.getKey());
    //				return Call_NEW;
    //			}
    //			return CALL_ERROR;
    //
    //		case CallGAudioFragement.UCT_GCALLMTIND:
    //			if(currentFragements == null){
    //				callManager.addUserCall(fragementBean.getKey());
    //				return Call_NEW;
    //			}
    //			return CALL_ERROR;
    //
    //		//广播
    //		case CallGAudioFragement.UCT_GCALLMTIND_BROADCAST:
    //			if(currentFragements == null){
    //				callManager.addUserCall(fragementBean.getKey());
    //				return Call_NEW;
    //			}
    //			return CALL_ERROR;
    //
    //		//组呼释放话权
    //		case CallGAudioFragement.UCTGCALLPRESSRELREQ:
    //			if(currentFragements!=null){
    //				return CALL_UPDATE;
    //			}
    //			return CALL_ERROR;
    //
    //		//主叫发起组呼确认
    //		case CallGAudioFragement.UCT_GCALLMOCFM:
    //			if(currentFragements!=null){
    //				return CALL_UPDATE;
    //			}
    //			return CALL_ERROR;
    //
    //		//话权申请确认
    //		case CallGAudioFragement.UCT_GCALLPRESSCFM:
    //			if(currentFragements!=null){
    //				return CALL_UPDATE;
    //			}
    //			return CALL_ERROR;
    //
    //		//组呼话权申请
    //		case CallGAudioFragement.UCTGCALLPRESSREQ:
    //			if(currentFragements!=null){
    //				return CALL_UPDATE;
    //			}
    //			return CALL_ERROR;
    //
    //		case CallGAudioFragement.UCT_GCALLPRESSRELCFM:
    //			//获取已经存在的组呼对象
    //			if(currentFragements!=null){
    //				return CALL_UPDATE;
    //			}
    //			return CALL_ERROR;
    //
    //		//组呼被叫挂断
    //		case CallGAudioFragement.UCT_GCALLRELIND:
    //		case CallGAudioFragement.MSG_UCT_SCALLCUSTOMRELIND:
    //			if(currentFragements!=null){
    //				return CALL_UPDATE;
    //			}
    //			return CALL_ERROR;
    //
    //		//话权变更
    //		case CallGAudioFragement.UCT_GCALLPRESSCHAGIND:
    //			if(currentFragements!=null){
    //				return CALL_UPDATE;
    //			}
    //			return CALL_ERROR;
    //
    //		//被叫单呼挂断
    //		case CallSAudioFragement.UCT_SCALLRELIND:
    //			if(currentFragements != null){
    //				return CALL_UPDATE;
    //			}
    //			return CALL_ERROR;
    //
    //		//单呼视频呼叫主叫请求
    //		case CallSAVideoFragement.UCTSCALLMOREQ://
    //			if(currentFragements == null){
    //				callManager.addUserCall(fragementBean.getKey());
    //
    //				callManager.addOperateCall(fragementBean.getOperateCall());
    //				return Call_NEW;
    //			}
    //			return CALL_ERROR;
    //
    //		//本地录像
    //		case CallSAVideoFragement.UCT_STARTRECORDREQ:
    //			if(currentFragements == null){
    //				PrintLog.d("开启本地录像");
    //				callManager.addUserCall(fragementBean.getKey());
    //				return Call_NEW;
    //			}
    //			com.ptyt.uct.utils.ToastUtils.getToast().showMessage(UctApplication.getInstances(), null, R.string.started_record);
    //			return CALL_ERROR;
    //
    //		//单呼视频主叫确认
    //		case CallSAVideoFragement.UCT_SCALLMOCFM://
    //			if(currentFragements != null){
    //				return CALL_UPDATE;
    //			}
    //			return CALL_ERROR;
    //
    //		//单呼视频被叫
    //		case CallSAVideoFragement.UCT_SCALLMTIND:
    //			if(currentFragements == null){
    //				callManager.addUserCall(fragementBean.getKey());
    //				return Call_NEW;
    //			}
    //			return CALL_ERROR;
    //
    //		/**
    //		 * 远程录像
    //		 */
    //		case CallSAVideoFragement.REMOTE_VIDEO:
    //			if(currentFragements == null){
    //				callManager.addUserCall(fragementBean.getKey());
    //				return Call_NEW;
    //			}
    //			return CALL_ERROR;
    //
    //		//单呼视频被叫挂断
    //		case CallSAVideoFragement.UCT_SCALLRELIND:
    //			if(currentFragements != null){
    //				return CALL_UPDATE;
    //			}
    //			return CALL_ERROR;
    //
    //		//主叫查看视频请求
    //		case CallSVideoFragement.UCTSVIDEOMOREQ:
    //			if(currentFragements == null){
    //				callManager.addUserCall(fragementBean.getKey());
    //
    //				callManager.addOperateCall(fragementBean.getOperateCall());
    //				return Call_NEW;
    //			}
    //			return CALL_ERROR;
    //
    //		//主叫查看视频确认
    //		case CallSVideoFragement.UCT_SVIDEOMOCFM:
    //			if(currentFragements != null){
    //				return CALL_UPDATE;
    //			}
    //			return CALL_ERROR;
    //
    //		//被叫查看视频请求
    //		case CallSVideoFragement.UCT_SVIDEOMTIND:
    //			if(currentFragements == null){
    //				callManager.addUserCall(fragementBean.getKey());
    //				return Call_NEW;
    //			}
    //			return CALL_ERROR;
    //
    //		//查看视频被叫挂断
    //		case CallSVideoFragement.UCT_SVIDEORELIND:
    //			if(currentFragements != null){
    //				return CALL_UPDATE;
    //			}
    //			return CALL_ERROR;
    //
    //		/**
    //		 * 组呼视频上传
    //		 */
    //		case CallGVideoFragement.UCTGVIDEOMOREQ:
    //			if(currentFragements == null){
    //				callManager.addUserCall(fragementBean.getKey());
    //				callManager.addOperateCall(fragementBean.getOperateCall());
    //				return Call_NEW;
    //			}
    //			return CALL_ERROR;
    //
    //		case CallGVideoFragement.UCT_GVIDEOMOCFM:
    //			if(currentFragements != null){
    //				return CALL_UPDATE;
    //			}
    //			return CALL_ERROR;
    //
    //		case CallGVideoFragement.UCT_GVIDEORELIND:
    //			if(currentFragements != null){
    //				return CALL_UPDATE;
    //			}
    //			return CALL_ERROR;
    //
    //		//视频转发
    //		case CallSVideoFragement.UCT_MSGVIDEOTRANSFERIND:
    //			if(currentFragements == null){
    //				callManager.addUserCall(fragementBean.getKey());
    //				return Call_NEW;
    //			}
    //			return CALL_ERROR;
    //
    //			//环境监测 公开模式
    //		case CallSAudioFragement.UCT_SCALLMTINTERCEPTIONIND_PUBLIC:
    //			if(currentFragements == null){
    //				callManager.addUserCall(fragementBean.getKey());
    //				return Call_NEW;
    //			}
    //			return CALL_ERROR;
    //
    //		default:
    //			PrintLog.e("【callAction=="+callAction);
    //			throw new ActionNotFoundException("action="+callAction);
    //
    //		}
    //	}
    //
    //	public static void dispose() {
    //
    //	}
    //
    //	public static void hideToast() {
    //		if (pre_toast != null) {
    //			pre_toast.cancel();
    //			pre_toast = null;
    //		}
    //	}
    //
    //	public static void showToast(View view, String msg) {
    //		hideToast();
    //
    //		int[] location = new int[2];
    //		view.getLocationOnScreen(location);
    //		int x = location[0];
    //		int y = location[1];
    //
    //		Toast toast = Toast.makeText(view.getContext(), msg, Toast.LENGTH_LONG);
    //		toast.setGravity(Gravity.TOP | Gravity.LEFT, x, y);
    //		pre_toast = toast;
    //		toast.show();
    //	}
    //
    //	public static AlertDialog showAlertDialog(AlertDialog.Builder builder) {
    //		if (g_alertDialog != null) {
    //			if (g_alertDialog.isShowing()) {
    //				g_alertDialog.dismiss();
    //			}
    //		}
    //		g_alertDialog = builder.show();
    //		return g_alertDialog;
    //	}
    //
    //	private void checkNetworkInfo(Context mActivity) {
    //		ConnectivityManager conMan = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
    //		// mobile 3G Data Network
    //		State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
    //		// wifi
    //		State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
    //		// 如果3G网络和wifi网络都未连接，且不是处于正在连接状态 则进入Network Setting界面 由用户配置网络连接
    //		if (mobile == State.CONNECTED || mobile == State.CONNECTING)
    //			return;
    //		if (wifi == State.CONNECTED || wifi == State.CONNECTING)
    //			return;
    //		mActivity.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));// 进入无线网络配置界面
    //		// startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    //		// //进入手机中的wifi网络设置界面
    //	}
    //
    //	private static ConnectivityManager s_connectivity = null;
    //
    //	public static boolean isNetworkAvailable(Context mActivity) {
    //		if (s_connectivity == null) {
    //			Context context = mActivity.getApplicationContext();
    //			s_connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    //		}
    //
    //		if (s_connectivity == null) {
    //			return false;
    //		} else {
    //			NetworkInfo[] info = s_connectivity.getAllNetworkInfo();
    //			if (info != null) {
    //				for (int i = 0; i < info.length; i++) {
    //					if (info[i].isConnected()) {
    //						return true;
    //					}
    //				}
    //			}
    //		}
    //		return false;
    //	}
    //
    //	public static int getNetworkType(Context mActivity) {
    //		if (s_connectivity == null) {
    //			Context context = mActivity.getApplicationContext();
    //			s_connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    //
    //		}
    //		if (s_connectivity == null) {
    //			return -1;
    //		} else {
    //			NetworkInfo[] info = s_connectivity.getAllNetworkInfo();
    //			if (info != null) {
    //				for (int i = 0; i < info.length; i++) {
    //					if (info[i].isConnected()) {
    //						return info[i].getType();
    //					}
    //				}
    //			}
    //		}
    //		return -1;
    //	}
    //
    //	public static int readIntCfg(String key, int defaultValue) {
    //		String value = com.android.uct.UctApi.getUserData(key, defaultValue + "");
    //		if (TextUtils.isDigitsOnly(value)) {
    //			return Integer.parseInt(value);
    //		} else
    //			return defaultValue;
    //	}
    //
    //	private static Vibrator vibrator = null;
    //
    //	public static void updateRingSetting(Context context) {
    //	}
    //
    //	public static void stopMsgRing() {
    //		// mIsMsgRing = false;
    //		UCTUtils.stopPlaySound();
    //
    //		if (vibrator != null) {
    //			vibrator.cancel();
    //		}
    //	}
    //
    //	public static boolean isLaunchAsHome(Context context) {
    //		if (m_isLaunchAsHome != -1)
    //			return m_isLaunchAsHome == 1;
    //		if (_isLaunchAsHome(context))
    //			m_isLaunchAsHome = 1;
    //		else
    //			m_isLaunchAsHome = 0;
    //		return m_isLaunchAsHome == 1;
    //	}
    //
    //	private static boolean _isLaunchAsHome(Context context) {
    //		final Intent intent = new Intent(Intent.ACTION_MAIN);
    //		intent.addCategory(Intent.CATEGORY_HOME);
    //		List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(intent, 0);
    //		int size = infos.size();
    //		for (int i = 0; i < size; i++) {
    //			ApplicationInfo info = infos.get(i).activityInfo.applicationInfo;
    //			if (context.getPackageName().equals(info.packageName))
    //				return true;
    //		}
    //		return false;
    //	}
    //
    //	public static String getStringFromResources(int id) {
    //		String str = curActivety.getString(id);
    //		return str;
    //	}
    //	 static KeyguardManager km;
    //	 static KeyguardLock kl;
    //	 static PowerManager pm;
    //	 static WakeLock wl;
    //	 static boolean isAcquire=false;
    //	public static void acquireWakeLock() {
    //		km= (KeyguardManager) UctApplication.getInstances().getSystemService(Context.KEYGUARD_SERVICE);
    //	    kl = km.newKeyguardLock("unLock");
    //	    //解锁
    //	    if(km.inKeyguardRestrictedInputMode()){
    //	    		kl.disableKeyguard();
    //	    }
    //	    //获取电源管理器对象
    //	   pm =(PowerManager) UctApplication.getInstances().getSystemService(Context.POWER_SERVICE);
    //	    //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
    //	   boolean isScreen =  pm.isScreenOn();
    ////	   true: 屏幕是唤醒的  返回false:屏幕是休眠的
    //	   PrintLog.d("[屏幕是否唤醒 ]"+(isScreen?"屏幕唤醒":"屏幕休眠"));
    //	    if(wl==null){
    //    	    wl= pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
    //    		wl.setReferenceCounted(false);
    //	    	//点亮屏幕
    //	    	wl.acquire();
    //		    isAcquire=true;
    //	    }else{
    //	    	isAcquire=false;
    //	    }
    //	}
    //
    //	public static void lockScreen() {
    //        // release screen
    //        if (km!=null && !km.inKeyguardRestrictedInputMode()) {
    //                // 锁键盘
    //              kl.reenableKeyguard();
    //        }
    //        if(pm!=null){
    //        	// 使屏幕休眠
    //        	PrintLog.d("[待机]="+isAcquire);
    //            if (wl!=null && wl.isHeld()) {
    //                  wl.release();
    //                  isAcquire=false;
    //                  wl=null;
    //            }
    //        }
    //}
    ////	public static void acquireWakeLock(boolean isScreenOn) {
    ////		try {
    ////			PowerManager pm = (PowerManager) UctApplication.getInstances().getSystemService(Context.POWER_SERVICE);
    ////				powerManagerWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
    ////						| PowerManager.ACQUIRE_CAUSES_WAKEUP, "PTM9100");
    ////				KeyguardManager keyguardManager = (KeyguardManager) UctApplication.getInstances()
    ////						.getSystemService(Context.KEYGUARD_SERVICE);
    ////				keyguardLock = keyguardManager.newKeyguardLock("");
    ////				keyguardLock.disableKeyguard(); // 这里就是取消系统默认的锁屏
    ////				powerManagerWakeLock.setReferenceCounted(false);
    ////				powerManagerWakeLock.acquire();
    ////		} catch (Exception e) {
    ////			PrintLog.d(""+e);
    ////		}
    ////	}
    //
    //	public static void releaseWakeLock() {
    //		if (powerManagerWakeLock != null) {
    //			powerManagerWakeLock.release();
    //			powerManagerWakeLock = null;
    //		}
    //	}
    //
    //	/**
    //	 * Description:打开键盘
    //	 */
    //	public static void showSoftKeyBoard(Context mContext,EditText editText) {
    //		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
    //				Context.INPUT_METHOD_SERVICE);
    //		if (imm != null) {
    //			imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
    //		}
    //	}
    //
    //	/**
    //	 * Description:隐藏键盘
    //	 */
    //	public static void hideSoftKeyBoard(Context mContext,EditText editText) {
    //		editText.clearFocus();
    //		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    //		if (imm != null) {
    //			imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    //		}
    //	}
    //
    //	public static String getDefaultGroupTel() {
    //		String currentGroup=null;
    //		String mChooseGroup=null;
    //		//首先获取锁定组的号码
    //		String mLockGroupCallTel = UctApi.getUserData(PreferenceConstant.PREFERENCE_MSG_LOCK_GROUP, "");
    //		String[]  lockGroupList = UctApi.lockGroupSize(mLockGroupCallTel);
    //		//判断是否还有多个锁定组
    //		if(lockGroupList != null && lockGroupList.length > 0){
    //			int lockSize = lockGroupList.length;
    //			if(lockSize==1){
    //				currentGroup = lockGroupList[0];
    //				return currentGroup;
    //			}else{
    //				mChooseGroup = UctApi.getUserData(PreferenceConstant.PREFERENCE_MSG_CALL_DEFAULT_TEL, "");
    //				//当前选中组是否在锁定组里
    //				boolean isLockGroup = UctApi.isLockGroup(mLockGroupCallTel, mChooseGroup);
    //				//如果选中组在锁定组里 返回这个选中组
    //				if(isLockGroup){
    //					currentGroup = mChooseGroup;
    //					return currentGroup;
    //				}else{
    //					//如果选中组不在锁定组里 返回锁定组的第一个组
    //					currentGroup = lockGroupList[0];
    //					return currentGroup;
    //				}
    //			}
    //		}else{
    //			mChooseGroup = UctApi.getUserData(PreferenceConstant.PREFERENCE_MSG_CALL_DEFAULT_TEL, "");
    //		}
    //
    //		//其次获取选定组的号码
    //		if (!TextUtils.isEmpty(mChooseGroup) && UctApi.isGroup(mChooseGroup)) {
    //			return mChooseGroup;
    //		}
    //		//最后获取组的第一个号码
    //		List<GroupData> groupList = UctApi.getGroupDataManager().getmGroupList();
    //		if(groupList!=null && groupList.size()>0){
    //			GroupData group = groupList.get(0);
    //			return group.getGroupTel();
    //		}
    //		return null;
    //	}
    //
    //	public static boolean exist(String number){
    //		List<GroupData> groupList = UctApi.getGroupDataManager().getmGroupList();
    //		if(groupList!=null&& !groupList.isEmpty()){
    //			for(int i=0;i<groupList.size();i++){
    //				GroupData group = groupList.get(i);
    //				if(group.getGroupTel().equals(number)){
    //					return true;
    //				}
    //			}
    //		}
    //		return false;
    //	}
    //
    //	public static boolean isBackground(Context context) {
    //	    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    //	    List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
    //	    for (RunningAppProcessInfo appProcess : appProcesses) {
    //	         if (appProcess.processName.equals(context.getPackageName())) {
    //	                if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
    //	                          Log.i("后台", appProcess.processName);
    //	                          return true;
    //	                }else{
    //	                          Log.i("前台", appProcess.processName);
    //	                          return false;
    //	                }
    //	           }
    //	    }
    //	    return false;
    //	}
    //
    //	public static  String getAppVersionName(Context context) {
    //		String versionName = "";
    //		try {
    //			PackageManager packageManager = context.getPackageManager();
    //			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
    //			versionName = packageInfo.versionName;
    //			if (TextUtils.isEmpty(versionName)) {
    //				return "";
    //			}
    //		} catch (Exception e) {
    //			e.printStackTrace();
    //		}
    //		return versionName;
    //	}
    //
    //	public interface CallMessageListener{
    //		public void callMessage(String msg, BaseFragementBean fragementBean);
    //	}
    //
    //	public static List<String> getHomes(Activity act) {
    //		List<String> names = new ArrayList<String>();
    //		PackageManager packageManager = act.getPackageManager();
    //	    Intent intent = new Intent(Intent.ACTION_MAIN);
    //		intent.addCategory(Intent.CATEGORY_HOME);
    //		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
    //		for(ResolveInfo ri : resolveInfo){
    //			names.add(ri.activityInfo.packageName);
    //			PrintLog.d(ri.activityInfo.packageName);
    //		}
    //		return names;
    //	}
    //
    //	/**
    //	 * 通过对比得到与宽高比最接近的尺寸（如果有相同尺寸，优先选择）
    //	 *
    //	 * @param surfaceWidth
    //	 *            需要被进行对比的原宽
    //	 * @param surfaceHeight
    //	 *            需要被进行对比的原高
    //	 * @param preSizeList
    //	 *            需要对比的预览尺寸列表
    //	 * @return 得到与原宽高比例最接近的尺寸
    //	 */
    //	protected Size getCloselyPreSize(int surfaceWidth, int surfaceHeight,List<Size> preSizeList) {
    //		int ReqTmpWidth;
    //		int ReqTmpHeight;
    //		// 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
    //		boolean mIsPortrait=true;
    //		if (mIsPortrait) {
    //			ReqTmpWidth = surfaceHeight;
    //			ReqTmpHeight = surfaceWidth;
    //		} else {
    //			ReqTmpWidth = surfaceWidth;
    //			ReqTmpHeight = surfaceHeight;
    //		}
    //		//先查找preview中是否存在与surfaceview相同宽高的尺寸
    //		for(Size size : preSizeList){
    //			if((size.width == ReqTmpWidth) && (size.height == ReqTmpHeight)){
    //				return size;
    //			}
    //		}
    //
    //		// 得到与传入的宽高比最接近的size
    //		float reqRatio = ((float) ReqTmpWidth) / ReqTmpHeight;
    //		float curRatio, deltaRatio;
    //		float deltaRatioMin = Float.MAX_VALUE;
    //		Size retSize = null;
    //		for (Size size : preSizeList) {
    //			curRatio = ((float) size.width) / size.height;
    //			deltaRatio = Math.abs(reqRatio - curRatio);
    //			if (deltaRatio < deltaRatioMin) {
    //				deltaRatioMin = deltaRatio;
    //				retSize = size;
    //			}
    //		}
    //		return retSize;
    //	}
    //
    //	public static boolean isCameraCanUse() {
    //        boolean canUse = true;
    //        Camera mCamera = null;
    //        try {
    //        	mCamera= Camera.open();
    //        } catch (Exception e) {
    //            canUse = false;
    //        }
    //        if (canUse) {
    //        	if(mCamera!=null)
    //        	{
    //        		mCamera.release();
    //        		mCamera = null;
    //        	}
    //        }
    //
    //        return canUse;
    //    }
    //
    //	public static List<Size> getAllSize(){
    //		Camera mCamera = null;
    //        try {
    //        	mCamera= Camera.open();
    //        } catch (Exception e) {
    //        	if(mCamera!=null)
    //        	{
    //        		mCamera.release();
    //        		mCamera=null;
    //        	}
    //            return null;
    //        }
    //        	if(mCamera!=null)
    //        	{
    //        		Parameters parameters = mCamera.getParameters();
    //        		if(parameters != null){
    //        			sizes = parameters.getSupportedPreviewSizes();
    //        		}
    //        		mCamera.release();
    //        		mCamera = null;
    //        	}
    //			return sizes;
    //	}
    //
    //	 public static Size getOptimalPreviewSize(int w, int h) {
    //	        final double ASPECT_TOLERANCE = 0.1;
    //	        double targetRatio = (double) w / h;
    //	        if (sizes == null) return null;
    //
    //	        Size optimalSize = null;
    //	        double minDiff = Double.MAX_VALUE;
    //
    //	        int targetHeight = h;
    //
    //	        // Try to find an size match aspect ratio and size
    //	        for (Size size : sizes) {
    //	            double ratio = (double) size.width / size.height;
    //	            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
    //	            if (Math.abs(size.height - targetHeight) < minDiff) {
    //	                optimalSize = size;
    //	                minDiff = Math.abs(size.height - targetHeight);
    //	            }
    //	        }
    //
    //	        // Cannot find the one match the aspect ratio, ignore the requirement
    //	        if (optimalSize == null) {
    //	            minDiff = Double.MAX_VALUE;
    //	            for (Size size : sizes) {
    //	                if (Math.abs(size.height - targetHeight) < minDiff) {
    //	                    optimalSize = size;
    //	                    minDiff = Math.abs(size.height - targetHeight);
    //	                }
    //	            }
    //	        }
    //	        return optimalSize;
    //	    }
    //
    //	public static Size getOptimalPreviewSize(Activity currentActivity,double targetRatio)
    //	{
    //		final double ASPECT_TOLERANCE = 0.001;
    //		if ( sizes == null )
    //			return null;
    //		Size optimalSize = null;
    //		double minDiff = Double.MAX_VALUE;
    //		Display display = currentActivity.getWindowManager().getDefaultDisplay();
    //		Point point = new Point();
    //		display.getSize(point);
    //		int targetHeight = Math.min(point.y, point.x);
    //		if ( targetHeight <= 0 )
    //		{
    //			targetHeight = point.y;
    //		}
    //		// Try to find an size match aspect ratio and size
    //		for(Size size : sizes)
    //		{
    //			double ratio = (double) size.width / size.height;
    //			if ( Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE )
    //				continue;
    //			if ( Math.abs(size.height - targetHeight) < minDiff || size.width == targetHeight )
    //			{
    //				optimalSize = size;
    //				minDiff = Math.abs(size.height - targetHeight);
    //			}
    //		}
    //		if ( optimalSize == null )
    //		{
    //			minDiff = Double.MAX_VALUE;
    //			for(Size size : sizes)
    //			{
    //				if ( Math.abs(size.height - targetHeight) < minDiff )
    //				{
    //					optimalSize = size;
    //					minDiff = Math.abs(size.height - targetHeight);
    //				}
    //			}
    //		}
    //		return optimalSize;
    //	}
    //
    //	public void getSingInfo(Context context) {
    //		try {
    //			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
    //			Signature[] signs = packageInfo.signatures;
    //			Signature sign = signs[0];
    //			parseSignature(sign.toByteArray());
    //		} catch (Exception e) {
    //			e.printStackTrace();
    //		}
    //	}
    //
    //	public void parseSignature(byte[] signature) {
    //		try {
    //			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    //			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(signature));
    //			String pubKey = cert.getPublicKey().toString();
    //			String signNumber = cert.getSerialNumber().toString();
    //			System.out.println("signName:" + cert.getSigAlgName());
    //			System.out.println("pubKey:" + pubKey);
    //			System.out.println("signNumber:" + signNumber);
    //			System.out.println("subjectDN:"+cert.getSubjectDN().toString());
    //		} catch (CertificateException e) {
    //			e.printStackTrace();
    //		}
    //	}
    //
    //	private static final String DEBUG_KEY = "你的debug签名";
    //    public static boolean isDebuggable(Context ctx) {
    //            String TAG = "isDebuggable";
    //            boolean debuggable = false;
    //            try {
    //                    PackageInfo pinfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNATURES);
    //                    Signature signatures[] = pinfo.signatures;
    //                    for (int i = 0; i < signatures.length; i++)
    //                            Log.d(TAG, signatures[i].toCharsString());
    //                    if (DEBUG_KEY.equals(signatures[0].toCharsString())) {
    //                            debuggable = true;
    //                    }
    //            } catch (NameNotFoundException e) {
    //                    // debuggable variable will remain false
    //            }
    //
    //            return debuggable;
    //    }
    //
    //    /**
    //     * 显示或隐藏状态栏
    //     *| View.SYSTEM_UI_FLAG_IMMERSIVE;
    //     * @param mContext
    //     */
    //    public static void hideNavigationBar(Activity mContext) {
    //        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    //                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    //                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    //                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
    //                | View.SYSTEM_UI_FLAG_FULLSCREEN;// hide status bar
    //
    //        if (android.os.Build.VERSION.SDK_INT >= 19) {
    //            uiFlags |= 0x00001000;    //SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide navigation bars - compatibility: building API level is lower thatn 19, use magic number directly for higher API target level
    //        } else {
    //            uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
    //        }
    //        mContext.getWindow().getDecorView().setSystemUiVisibility(uiFlags);
    //    }
}
