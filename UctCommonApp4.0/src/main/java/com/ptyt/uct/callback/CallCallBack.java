package com.ptyt.uct.callback;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.SurfaceView;

import com.android.uct.IUCTSCallListener;
import com.android.uct.IUCTSCallMtIndListener;
import com.android.uct.IUctSVideoCallBack;
import com.android.uct.IUctSVideoMtIndListener;
import com.android.uct.exception.UctLibException;
import com.android.uct.exception.UctLibInitializationException;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.common.AppManager;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.model.ContactDBManager;
import com.ptyt.uct.utils.ActivitySkipUtils;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.ConstantUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import de.greenrobot.event.EventBus;

import static android.content.Context.POWER_SERVICE;
import static com.ptyt.uct.utils.ConstantUtils.AUDIO_SCALL;
import static com.ptyt.uct.utils.ConstantUtils.MEETING_CALL;
import static com.ptyt.uct.utils.ConstantUtils.UPLOAD_VIDEO;

/**
 * @Description: 视频呼叫,语音呼叫，业务接口回调
 * @Date: 2017/9/29
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class CallCallBack{

    private static CallCallBack instance;
    private Context mContext;
    //业务集合 key 为 句柄,(含视频/语音呼叫，视频上传/下载)
    public LinkedHashMap<Integer, EventBean> callBusinessMap = new LinkedHashMap<>();
    //用于视频业务过来时当前界面是否为VideoCallActivity  true：通知VideoCallActivity显示控制  false：跳转至VideoCallActivity
    public boolean isCallActivityOnCurrent = false;
    //调节声音模式
    public static int voiceCallMode = AudioManager.STREAM_MUSIC;
    public AudioManager audioManager;
    /**
     * 本地视频窗口
     */
    private SurfaceView localSurface;
    /**
     * 远程视频窗口
     */
    private SurfaceView remoteSurface;
    private KeyguardManager km;

    public static synchronized CallCallBack getInstance(){

        if(instance==null){
            instance = new CallCallBack();
        }
        return instance;
    }

    public void init(Context context) {
        mContext = context;
        PrintLog.w("注册CallCallBack");
        audioManager = (AudioManager) UctApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
        //亮屏设置
        PowerManager pm = (PowerManager)mContext.getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());
        //键盘锁管理器对象
        km= (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardLock = km.newKeyguardLock("unLock");
        // 纯视频被叫呼叫监听
        UctClientApi.registerObserver(iUctSVideoMtIndListener, IUctSVideoMtIndListener.IUCTSVIDEOMTINDLISTENER_INDEX);
        //注册单呼或视频呼叫的接口
        UctClientApi.registerObserver(iUCTSCallMtIndListener, IUCTSCallMtIndListener.IUCTSCALLMTINDLISTENER_INDEX);
        //注册语音呼叫 视频呼叫  自己/对方挂断/接听的回调
        UctClientApi.registerObserver(iUCTSCallListener, IUCTSCallListener.IUCTSCALLLISTENER_INDEX);
        //上传视频 对方挂断/接听的回调
        UctClientApi.registerObserver(iUctSVideoCallBack, IUctSVideoCallBack.IUCTSVIDEOCALLBACK_INDEX);
        //堵塞的时间集合(来的事件callActivity还来不及开启时的情况)
        blockEventBeans = new ArrayList<>();
    }

    public void initSurfaceVideo(Context mContext) {
        remoteSurface = new SurfaceView(mContext);
        localSurface = new SurfaceView(mContext);
        remoteSurface.setBackgroundColor(Color.TRANSPARENT);
        localSurface.setBackgroundColor(Color.TRANSPARENT);
        //设置视频窗口
        int setWindowHandler = UctClientApi.UctVideoSetWindowHandle(localSurface, remoteSurface);
        if (setWindowHandler == -1) {
            throw new UctLibInitializationException();
        }
    }

    public SurfaceView getLocalSurface() {
        return localSurface;
    }

    public SurfaceView getRemoteSurface() {
        return remoteSurface;
    }

    /**
     * 语音呼叫 视频呼叫  自己/对方挂断/接听的回调
     */
    private IUCTSCallListener iUCTSCallListener = new IUCTSCallListener() {

        //记住 这里语音呼叫/视频呼叫时不管是被叫还是主叫 只要挂断都会收到释放通知 所以处理主叫挂断是在这个接口里释放资源
        @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
        @Override
        public int UCT_SCallRelInd(int usCause, int hUserCall)throws UctLibException {
            //根据hUserRelInd 一个个挂断
            PrintLog.e("视频/语音呼叫 挂断--UCT_SCallRelInd   currentHUserCall="+hUserCall +"  usCause="+usCause);
            AppUtils.stopPlayMedia();
            EventBean eventBean = new EventBean(ConstantUtils.ACTION_OTHER_HANGUP);
            eventBean.sethUserCall(hUserCall);
             /*ptyt start 4421 对端业务发起后立刻挂断，本端接收后来不及启动activity产生的挂断收不到问题 kechuanqi_20171129*/
            if(isCallActivityOnCurrent){
                EventBus.getDefault().post(eventBean);
            }else{
                //等待界面开启后通知
                isEventBlock = true;
                blockEventBeans.add(eventBean);
            }
            /*end*/
            return 0;
        }

        @Override
        public int UCT_SCallMoCfm(String pcCfmDn, String pMpIp, int CallRefID,int hUserCall, int isVideo) throws UctLibException {
            PrintLog.e("视频/语音呼叫对方已接听--UCT_SVideoMoCfm   currentHUserCall="+hUserCall +"  isVideo="+isVideo);
            EventBean eventBean = new EventBean(ConstantUtils.ACTION_OTHER_ACCEPT);
            eventBean.sethUserCall(hUserCall);
            EventBus.getDefault().post(eventBean);
            return 0;
        }
    };

    /**
     * 事件阻塞
     */
    private boolean isEventBlock = false;

    /**
     * 阻塞的事件集合
     */
    public ArrayList<EventBean> blockEventBeans;

    /**
     * 上传视频 对方挂断/接听的回调
     */
    private IUctSVideoCallBack iUctSVideoCallBack = new IUctSVideoCallBack() {

        @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
        @Override
        public int UCT_SVideoRelInd(int usCause, int hUserCall) throws UctLibException {
            PrintLog.e("查看视频对方挂断--UCT_SVideoRelInd   currentHUserCall="+hUserCall + "  usCause="+usCause);
            //ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_look_video_other_hangup), -1);
            AppUtils.stopPlayMedia();
            EventBean eventBean = new EventBean(ConstantUtils.ACTION_OTHER_HANGUP);
            eventBean.sethUserCall(hUserCall);
            /*ptyt start 4421 对端业务发起后立刻挂断，本端接收后来不及启动activity产生的挂断收不到问题 kechuanqi_20171129*/
            PrintLog.e("isCallActivityOnCurrent="+isCallActivityOnCurrent);
            if(isCallActivityOnCurrent){
                EventBus.getDefault().post(eventBean);
            }else{
                //等待界面开启后通知
                isEventBlock = true;
                blockEventBeans.add(eventBean);
            }
            /*end*/
            return 0;
        }

        @Override
        public int UCT_SVideoMoCfm(String pcCfmDn, int hUserCall)throws UctLibException {
            PrintLog.e("查看视频对方已接听--UCT_SVideoMoCfm  currentHUserCall="+hUserCall);
            //ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_look_video_other_answer), -1);
            EventBean eventBean = new EventBean(ConstantUtils.ACTION_OTHER_ACCEPT);
            eventBean.sethUserCall(hUserCall);
            EventBus.getDefault().post(eventBean);
            return 0;
        }
    };

    /**
     * 单呼或视频呼叫  被叫接口
     */
    //视频下载业务数量
    public int videoDownloadNum = 0;
    private IUCTSCallMtIndListener iUCTSCallMtIndListener = new IUCTSCallMtIndListener() {

        @Override
        public int UCT_SCallMtInd(String pcCalling, String pcDn, int flag, String pMpIp, String pcName, int CallRefID, int hUserCall) throws UctLibException {
            synchronized (instance){
//                wakeLock.acquire();
                //这里参数”unLock”作为调试时LogCat中的Tag
//                if(KeyguardLock != null){
//                    KeyguardLock.disableKeyguard();  //解锁
//                }
                PrintLog.e("AppContext呼或视频呼叫被叫接口-UCT_SCallMtInd中----()--pcCalling=" + pcCalling + "    pcDn=" + pcDn + "   flag=" + flag + "   pMpIp" + pMpIp + "   pcName=" + pcName + "  CallRefID=" + CallRefID + "  currentHUserCall=" + hUserCall + "  videoDownloadNum:" + videoDownloadNum);
                if(TextUtils.isEmpty(pcCalling)){
                    return 0;
                }
                EventBean eventBean = null;
                if (flag == 0 || flag == 9) {//语音呼叫  或  强插语音呼叫
                    eventBean = new EventBean(ConstantUtils.ACTION_AUDIO_CALL_COME_IN,
                            pcCalling,
                            pcDn,
                            TextUtils.isEmpty(pcName)? ContactDBManager.getInstance(mContext).queryContactName(pcCalling):pcName,
                            ConstantUtils.CALL_DIRECTION_PASSIVE,
                            hUserCall,
                            AUDIO_SCALL);
                }else if(flag == ConstantUtils.MEETING_CALL){//会议
                    eventBean = new EventBean(ConstantUtils.ACTION_AUDIO_CALL_COME_IN,
                            pcCalling,
                            pcDn,
                            TextUtils.isEmpty(pcName)?ContactDBManager.getInstance(mContext).queryContactName(pcCalling):pcName,
                            ConstantUtils.CALL_DIRECTION_PASSIVE,
                            hUserCall,
                            ConstantUtils.MEETING_CALL);
                }
                else if(flag == 1){//视频呼叫
                    eventBean = new EventBean(ConstantUtils.ACTION_VIDEO_CALL_COME_IN,
                            pcCalling,
                            pcDn,
                            TextUtils.isEmpty(pcName)?ContactDBManager.getInstance(mContext).queryContactName(pcCalling):pcName,
                            ConstantUtils.CALL_DIRECTION_PASSIVE,
                            hUserCall,
                            ConstantUtils.VIDEO_SCALL);
                }
                if(eventBean == null){
                    PrintLog.e("return 0");
                    return 0;
                }
                if(videoDownloadNum < 1 || flag == AUDIO_SCALL || flag == MEETING_CALL || flag == 9){//视频下载一路
                    Activity currentActivity = AppManager.getAppManager().currentActivity();
                    AppUtils.startRingtone(currentActivity, AUDIO_SCALL);
                    //同时来多路，且都还未进入通话界面
                    callBusinessMap.put(hUserCall, eventBean);
                    if(flag == 1){
                        videoDownloadNum++;
                    }
                    if(isCallActivityOnCurrent){
                        //通知视频界面VideoCallActivity
                        EventBus.getDefault().post(eventBean);
                    }else {
                        if(flag == 9){
                            flag = 0;
                        }
                        ActivitySkipUtils.intent2CallActivity(mContext, ConstantUtils.CALL_DIRECTION_PASSIVE,0, pcCalling,  TextUtils.isEmpty(pcName)?pcCalling:pcName,  hUserCall,flag);
                    }
                }
                return 0;
            }
        }
    };
    public PowerManager.WakeLock wakeLock;
    public KeyguardManager.KeyguardLock KeyguardLock;
    /**
     * 纯视频业务被叫监听  被叫接口
     */
    private IUctSVideoMtIndListener iUctSVideoMtIndListener = new IUctSVideoMtIndListener() {

        @Override
        public int UCT_SVideoMtInd(String pcCalling, String pcDn, String pcName,int iDirection, int hUserCall) throws UctLibException {


            synchronized (instance){

               /* wakeLock.acquire();
                if(KeyguardLock != null){
                    KeyguardLock.disableKeyguard();  //解锁
                }*/
                pcName =  TextUtils.isEmpty(pcName)? ContactDBManager.getInstance(mContext).queryContactName(pcCalling):pcName;

                PrintLog.e("AppContext纯视频业务被叫监听-UCT_SVideoMtInd----()--pcCalling="+pcCalling+"    pcDn="+pcDn+"   pcName="+pcName+"  iDirection="+iDirection+"  currentHUserCall="+hUserCall+"  videoDownloadNum:"+videoDownloadNum);
                //视频业务添加到集合
                //iDirection:(被叫)1-接收视频 2-pc查看视频
                Activity currentActivity = AppManager.getAppManager().currentActivity();
                AppUtils.startRingtone(currentActivity, UPLOAD_VIDEO);
                if(TextUtils.isEmpty(pcCalling)){
                    return 0;
                }
                EventBean eventBean;
                if(iDirection == 1 && videoDownloadNum < 1){//接收视频 即 下载视频
                    eventBean = new EventBean(ConstantUtils.ACTION_DOWNLOAD_VIDEO_COME_IN, pcCalling, pcDn, TextUtils.isEmpty(pcName)?pcCalling:pcName, ConstantUtils.CALL_DIRECTION_PASSIVE, iDirection, hUserCall, ConstantUtils.DOWNLOAD_VIDEO);
                    //视频上传多路，下载一路,当前存在视频呼叫或视频下载
                    callBusinessMap.put(hUserCall, eventBean);
                    //如果当前界面是VideoCallActivity
                    videoDownloadNum++;
                    if (isCallActivityOnCurrent) {
                        //通知视频界面VideoCallActivity
                        EventBus.getDefault().post(eventBean);
                    } else {
                        ActivitySkipUtils.intent2CallActivity(mContext, ConstantUtils.CALL_DIRECTION_PASSIVE, iDirection, pcCalling, TextUtils.isEmpty(pcName)?pcCalling:pcName, hUserCall, ConstantUtils.DOWNLOAD_VIDEO);
                    }
                }else {
                    eventBean = new EventBean(ConstantUtils.ACTION_UPLOAD_VIDEO_COME_IN, pcCalling, pcDn, TextUtils.isEmpty(pcName)?pcCalling:pcName, ConstantUtils.CALL_DIRECTION_PASSIVE,iDirection, hUserCall, UPLOAD_VIDEO);
                    //如果当前界面是VideoCallActivity
                    callBusinessMap.put(hUserCall,eventBean);
                    if(isCallActivityOnCurrent){
                        //通知视频界面VideoCallActivity
                        EventBus.getDefault().post(eventBean);
                    }else {
                        ActivitySkipUtils.intent2CallActivity(mContext, ConstantUtils.CALL_DIRECTION_PASSIVE,iDirection,pcCalling, TextUtils.isEmpty(pcName)?pcCalling:pcName, hUserCall, UPLOAD_VIDEO);
                    }
                }
                return 0;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void release(){
        PrintLog.w("反注册CallCallBack");
        AppUtils.stopPlayMedia();
        UctClientApi.unregisterObserver(iUCTSCallListener, IUCTSCallListener.IUCTSCALLLISTENER_INDEX);
        UctClientApi.unregisterObserver(iUctSVideoCallBack, IUctSVideoCallBack.IUCTSVIDEOCALLBACK_INDEX);
        UctClientApi.unregisterObserver(iUCTSCallMtIndListener, IUCTSCallMtIndListener.IUCTSCALLMTINDLISTENER_INDEX);
        UctClientApi.unregisterObserver(iUctSVideoMtIndListener, IUctSVideoMtIndListener.IUCTSVIDEOMTINDLISTENER_INDEX);
    }

    /**
     * 设置声音模式
     * @param mode
     */
    public void setVoiceCallMode(int mode) {
        PrintLog.i("setVoiceCallMode() mode="+mode);
        if(mode == ConstantUtils.STREAM_MUSIC_AND_ADJUST_VOICE_VOLUME){//当前是媒体通道，但调节音量的是会话
            voiceCallMode = AudioManager.STREAM_VOICE_CALL;
        }else {
            voiceCallMode = mode;
        }
        if(mode == AudioManager.STREAM_VOICE_CALL){//0 通话中模式:当调节声音为STREAM_VOICE_CALL时，设置为通话模式
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        }else if(mode == AudioManager.STREAM_MUSIC || mode == ConstantUtils.STREAM_MUSIC_AND_ADJUST_VOICE_VOLUME){//媒体
            audioManager.setMode(AudioManager.MODE_NORMAL);
        } else {
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
    }

    /**
     * add in 20171129 避免界面还来不及开启时又来了其它业务流程(新业务，挂断),此时界面未开启EventBus时收不到的
     */
    public void onCallActivityCreated() {
        PrintLog.e("isEventBlock="+isEventBlock);
        if(isEventBlock){//如果是界面还未开启而来了其他业务通知的情况，则在业务开启后依次把业务集合发送出去
            PrintLog.e("isEventBlock=true  blockEventBeans.size()="+blockEventBeans.size());
            for (int i = 0; i < blockEventBeans.size(); i++) {
                EventBus.getDefault().post(blockEventBeans.get(i));
            }
            blockEventBeans.clear();
            isEventBlock = false;
        }
    }
}
