package com.ptyt.uct.callback;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.telecom.TelecomManager;
import android.text.TextUtils;

import com.android.uct.IUCTGCallListener;
import com.android.uct.PtytPreferenceChangeListener;
import com.android.uct.ReleaseListener;
import com.android.uct.exception.UctLibException;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.MainActivity;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.model.ContactDBManager;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.ScreenManager;
import com.ptyt.uct.utils.SoundManager;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.viewinterface.IGroupInfoView;

import java.util.List;

import de.greenrobot.event.EventBus;

import static android.content.Context.POWER_SERVICE;
import static android.media.AudioManager.STREAM_MUSIC;
import static com.ptyt.uct.common.SettingsConstant.SETTINGS_LOCK_GROUP;
import static com.ptyt.uct.utils.ConstantUtils.STREAM_MUSIC_AND_ADJUST_VOICE_VOLUME;

/**
 * @Description: 组呼接口回调
 * @Date: 2017/5/11
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class GCallCallback implements ReleaseListener{

    private static GCallCallback instance;
    private Context mContext;
    private IGroupInfoView iGroupInfoView;
    //当前组呼是否发起成功
    private boolean isGCallMoReqSuccess=false;
    // 当前呼叫的通道ID
    private int userCall;
    /**
     * 当前发起组呼的组号码
     */
    public String currentGroupId =null;
    //当前组呼状态信息，供其它地方判断使用(如通话界面进入时判断当前组呼状态)
    public EventBean currentEventBean;
    public int currentSpeakState;
    private boolean isRegistered = false;
    private PowerManager.WakeLock wakeLock;
    private KeyguardManager.KeyguardLock keyguardLock;

    public static synchronized GCallCallback getInstance(){

        if(instance==null){
            instance = new GCallCallback();
        }
        return instance;
    }

    public void init(Context context,IGroupInfoView iGroupInfoView) {
        PrintLog.w("注册GCallCallback");
        mContext = context;
        this.iGroupInfoView = iGroupInfoView;
        //组呼
        if (!isRegistered) {
            isRegistered = true;
            UctClientApi.registerObserver(groupCallListener, IUCTGCallListener.IUCTGCALLLISTENER_INDEX);
        }
        //组呼事件数据bean
        currentEventBean = new EventBean(ConstantUtils.ACTION_GROUP_CALL_COME_IN);
        //亮屏设置
        PowerManager pm = (PowerManager)mContext.getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());
        //键盘锁管理器对象
        KeyguardManager km= (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        //这里参数”unLock”作为调试时LogCat中的Tag
        keyguardLock = km.newKeyguardLock("unLock");
        UctClientApi.registerObserver(this, PtytPreferenceChangeListener.RELEASELISTENER_INDEX);
    }

    /**
     * 自己挂断或对方挂断
     */
    private void gCallRelease(String gid) {
        PrintLog.i("gCallRelease");
        UctApplication.getInstance().isInGroupCall = false;
        isGCallHangUp = true;
        CallCallBack.getInstance().setVoiceCallMode(STREAM_MUSIC);
        if(isGCallMoReqSuccess){
            ToastUtils.getToast().showMessageShort(mContext, gid + mContext.getString(R.string.string_main_group_call_hangup), -1);
            isGCallMoReqSuccess=false;
            userCall=0;
        }else{
            PrintLog.i("组呼未发起不需要挂断");
        }
        isActiveLauncher = false;
    }

    /**
     * 组呼是否挂断
     */
    private boolean isGCallHangUp = true,isActiveLauncher = false;
    /**
     * 组呼监听
     */
    private IUCTGCallListener groupCallListener = new IUCTGCallListener() {

        /**
         * 
         * @param ret
         * @param gid
         * @param hUserCall
         * @return
         * @throws UctLibException
         */
        @Override
        public int UCT_GCallRelInd(int ret, String gid, int hUserCall)throws UctLibException {
            PrintLog.i("---5)组呼已挂断  hUserCall="+hUserCall+"   gid="+gid + "  ret="+ret);
            gCallRelease(gid);
            if(ret > 0 && ret != 16 && ret!=102){
                iGroupInfoView.gCallStateChanged(IGroupInfoView.GCALL_FAILED,gid,null);
                UctApplication.getInstance().getGroupCallWindow().speakState(IGroupInfoView.GCALL_FAILED,gid,null);
            }else{
                iGroupInfoView.gCallStateChanged(IGroupInfoView.GCALL_HANGUP,gid,null);
                UctApplication.getInstance().getGroupCallWindow().speakState(IGroupInfoView.GCALL_HANGUP,gid,null);
            }
            //在MainActivity且在组呼主界面则不隐藏
            if(!UctApplication.getInstance().isInMainActivity || MainActivity.currentFragmentPosition != 1){
                UctApplication.getInstance().getGroupCallWindow().hidePopupWindow();
            }
            //通知组呼挂断 （通话界面）
            postEventBean(ConstantUtils.GCALL_HANGUP,null,null);
            if(wakeLock.isHeld()){
                wakeLock.release();
            }
            return 0;
        }

        @Override
        public int UCT_GCallPressRelCfm(int ret, int hUserCall) {
            PrintLog.i("---4-2) 组呼组呼按键抬起 组呼释放确认回调hUserCall="+hUserCall + "  currentSpeakState="+currentSpeakState+"  isGCallHangUp="+ isGCallHangUp);
            if(isGCallHangUp){//如果当前没人说话,避免未发起成功时，
                iGroupInfoView.gCallStateChanged(IGroupInfoView.GCALL_RELEASE,currentGroupId,null);
                UctApplication.getInstance().getGroupCallWindow().speakState(IGroupInfoView.GCALL_RELEASE,currentGroupId,"");
            }
            return 0;
        }

        /**
         * 当当前说话人改变时调用: 换人说话，没人说话
         * @param ret
         * @param cTalkingGID
         * @param cTalkingName
         * @param hUserCall
         * @return
         * @throws UctLibException
         */
        @Override
        public int UCT_GCallPressChagInd(int ret, String cTalkingGID,String cTalkingName, int hUserCall) throws UctLibException {
            PrintLog.i("----3) 当前说话人改变:cTalkingGID="+cTalkingGID+"  cTalkingName="+cTalkingName+"   hUserCall="+hUserCall);
            String loginNumber = AppContext.getAppContext().getLoginNumber();
            isGCallMoReqSuccess = true;
            isGCallHangUp = false;
            if(TextUtils.isEmpty(cTalkingGID)){
                //没人说话
                iGroupInfoView.gCallStateChanged(IGroupInfoView.NOBODY_SPEAK,null,cTalkingGID);
                UctApplication.getInstance().getGroupCallWindow().speakState(IGroupInfoView.NOBODY_SPEAK,null,cTalkingGID);
                //通知组呼说话状态说话状态 （通话界面）
                postEventBean(ConstantUtils.NOBODY_SPEAK,null,null);
                CallCallBack.getInstance().setVoiceCallMode(STREAM_MUSIC);
                AppUtils.startGCallPlayMedia(R.raw.ppt_stop);
            }else{
                //设置扬声器
                SoundManager.setSpeakerphoneOn(mContext,true);
                cTalkingName = ContactDBManager.getInstance(mContext).queryContactName(cTalkingGID);
                //讲话方是自己
                if(!TextUtils.isEmpty(loginNumber) && cTalkingGID.equals(loginNumber)){
                    iGroupInfoView.gCallStateChanged(IGroupInfoView.SELF_SPEAK,currentGroupId,cTalkingName);
                    UctApplication.getInstance().getGroupCallWindow().speakState(IGroupInfoView.SELF_SPEAK,currentGroupId,cTalkingName);
                    //通知组呼说话状态说话状态 （通话界面）
                    postEventBean(ConstantUtils.SELF_SPEAK,cTalkingName,cTalkingGID);
                    AppUtils.startGCallPlayMedia(R.raw.ppt_start);
                }else{
                    //讲话方是别人
                    CallCallBack.getInstance().setVoiceCallMode(STREAM_MUSIC_AND_ADJUST_VOICE_VOLUME);
                    AppUtils.startGCallPlayMedia(R.raw.ppt_start);
//                    SoundManager.getInstance().doSoundByCase(SoundManager.GROUP_CALL_OTHER_SPEAK);
                    iGroupInfoView.gCallStateChanged(IGroupInfoView.OTHER_SPEAK,currentGroupId,cTalkingName);
                    UctApplication.getInstance().getGroupCallWindow().speakState(IGroupInfoView.OTHER_SPEAK,currentGroupId,cTalkingName);
                    //通知组呼说话状态说话状态 （通话界面）
                    postEventBean(ConstantUtils.OTHER_SPEAK,cTalkingName,cTalkingGID);
                    wakeLock.acquire();
                    keyguardLock.disableKeyguard();  //解锁
                    if(!wakeLock.isHeld()){//判断是否已经获取WakeLock：boolean isHeld()
                        wakeLock.acquire();
                    }
                }
            }
            return 0;
        }

        @Override
        public int UCT_GCallPressCfm(int ret, int hUserCall) {
            PrintLog.i("----2-2) UCT_GCallPressCfm()-------话权申请确认");
            /*ptyt start _4559_当组呼讲话时间超过60秒时提示录音失败 kechuanqi_20171114*/
            iGroupInfoView.gCallStateChanged(IGroupInfoView.GCALL_CONFIRM,currentGroupId,null);
            /*ptyt end*/
            return 0;
        }

        /**
         * 组呼发起确认
         * @param ret
         * @param CallRefID
         * @param hUserCall
         * @return
         * @throws UctLibException
         */
        @Override
        public int UCT_GCallMoCfm(int ret, int CallRefID, int hUserCall)throws UctLibException {
            PrintLog.i("----2-1)---UCT_GCallMoCfm()组呼发起确认  ret="+ret+"  CallRefID="+CallRefID+"  hUserCall="+hUserCall);
            AppUtils.startGCallPlayMedia(R.raw.ppt_start);
            isGCallMoReqSuccess=true;
            String loginNumber = AppContext.getAppContext().getLoginNumber();
            iGroupInfoView.gCallStateChanged(IGroupInfoView.GCALL_SUCCEED,null,null);
            iGroupInfoView.gCallStateChanged(IGroupInfoView.SELF_SPEAK,currentGroupId,loginNumber);
            UctApplication.getInstance().getGroupCallWindow().speakState(IGroupInfoView.SELF_SPEAK,currentGroupId,loginNumber);
            UctApplication.getInstance().isInGroupCall = true;
            postEventBean(ConstantUtils.SELF_SPEAK,null,null);
            isGCallHangUp = false;
            isActiveLauncher = true;
            return 0;
        }

        /**
         * 被叫组呼
         * @param type 0:
         * @param ret :释放原因值   21:呼叫拒绝(可能调度台已设置禁止组呼)
         * @param cGid 组id:10051314150
         * @param cTalkingGID 正在说话的人id:1005
         * @param pcName 正在说话的人 name :1005
         * @param CallRefID:817967712
         * @param hUserCall :1
         * @return
         * @throws UctLibException
         */
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public int UCT_GCallMtInd(int type, int ret, String cGid,String cTalkingGID, String pcName, int CallRefID, int hUserCall)throws UctLibException {

            PrintLog.i("--1 )被叫组呼--type="+type+"   ret="+ret+"  cGid="+cGid+"  cTalkingGID="+cTalkingGID+"   pcName="+pcName+"   CallRefID="+CallRefID+"   hUserCall="+hUserCall +
                    "  isInGroupCall"+UctApplication.getInstance().isInGroupCall + "  isGCallMoReqSuccess="+isGCallMoReqSuccess + "  isActiveLauncher="+isActiveLauncher);
            //当当前正在打电话时，来组呼无效
            if (phoneIsInUse(mContext)){
                PrintLog.i("phoneIsInUse");
                UctClientApi.UCTGCallRelReq(0,cGid,hUserCall);
                return 0;
            }
            /*ptyt start 解决锁定组无效问题_kechuanqi_20171213*/
            //如果有锁定组，且被叫组呼不在锁定组列表内，则return
            String lockGroups = (String) UctClientApi.getUserData(SETTINGS_LOCK_GROUP, "");
            String[] lockGroup = lockGroups.split(",");
            boolean isFind = false;
            for (int i = 0; i < lockGroup.length; i++) {
                String s = lockGroup[i];
                if(cGid.equals(s)){
                    isFind = true;
                    break;
                }
            }
            if (lockGroup.length > 0 && !TextUtils.isEmpty(lockGroup[0]) && !isFind){//有锁定组 && 当前组呼不是锁定组
                return 0;
            }
            /*ptyt end*/
            if(isActiveLauncher){//如果组呼还在进行中时 && 没人说话
                return 0;
            }
            isGCallHangUp = false;
            if(isGCallMoReqSuccess){
                UctClientApi.UCTGCallRelReq(0, cGid, hUserCall);
                PrintLog.i("组呼已经发起，直接释放了");
                return 0;
            }
            boolean isAppOnForeground = isAppOnForeground();
            if(!isAppOnForeground && CallCallBack.getInstance().callBusinessMap.size() == 0){//在Home界面且当前只有组呼时启动MainActivity
                PrintLog.i("startActivity");
                Intent intent = new Intent(mContext, com.ptyt.uct.activity.MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
            ScreenManager.getInstance().prepare(((MainActivity) mContext));
            isGCallMoReqSuccess=true;
            userCall = hUserCall;
            currentGroupId = cGid;
            UctApplication.getInstance().isInGroupCall = true;
            /**
             * 控制组呼悬浮窗是否显示
             * 当组呼来时，除了在CallFragment页面和呼叫界面CallActivity中不展示组呼悬浮窗，其他界面都需展示
             */
            PrintLog.i("UctApplication.getInstance().isInMainActivity="+UctApplication.getInstance().isInMainActivity + "  ((MainActivity) mContext).currentFragmentPosition="+((MainActivity) mContext).currentFragmentPosition);
            if(((((MainActivity) mContext).currentFragmentPosition != 1 || !UctApplication.getInstance().isInMainActivity)) && !CallCallBack.getInstance().isCallActivityOnCurrent){
                UctApplication.getInstance().getGroupCallWindow().show((MainActivity) mContext);
            }
            String talkingNickname = ContactDBManager.getInstance(mContext).queryContactName(cTalkingGID);
            UctApplication.getInstance().getGroupCallWindow().speakState(IGroupInfoView.OTHER_LAUCHER_GCALL,cGid,talkingNickname);
            iGroupInfoView.gCallStateChanged(IGroupInfoView.OTHER_LAUCHER_GCALL,cGid,talkingNickname);
            //通知组呼来了 （通话界面）
            postEventBean(ConstantUtils.OTHER_LAUCHER_GCALL,talkingNickname,cTalkingGID);
            /*被叫组呼刚来时没声音问题*/
            SoundManager.setSpeakerphoneOn(mContext,true);
            AppUtils.startGCallPlayMedia(R.raw.ppt_start);
            return 0;
        }
    };
    /**
     * 发送组呼变化的事件数据
     * @param speakState 说话状态
     */
    private void postEventBean(int speakState,String speakUserName,String speakUserTel) {
        currentSpeakState = speakState;
        currentEventBean.setDetailAction(speakState);
        currentEventBean.setcGid(currentGroupId);
        if(!TextUtils.isEmpty(speakUserTel)){
            currentEventBean.setPcCalling(speakUserTel);
        }
        if(!TextUtils.isEmpty(speakUserName)){
            currentEventBean.setPcName(speakUserName);
        }
        EventBus.getDefault().post(currentEventBean);
    }

    /**
     *发起组呼
     */
    public void startGroupCall(String groupId) {
        currentGroupId = groupId;
        PrintLog.i("isGCallMoReqSuccess="+isGCallMoReqSuccess + "  isInGroupCall="+UctApplication.getInstance().isInGroupCall);
        if (!isGCallMoReqSuccess) {
            int result = UctClientApi.uctGCallMoReq(groupId);
            PrintLog.i("-----1-1)组呼键按下--发起组呼currentGroupId="+currentGroupId +"  result="+result);
            //表示组呼发起失败
            if (result < 0) {
                /*ptyt start_组呼缩略框状态中，解决组呼自动挂断时按键变为红色_4903_kechuanqi_20171215*/
                //iGroupInfoView.gCallStateChanged(IGroupInfoView.GCALL_FAILED, currentGroupId,null);
            } else {
//                if(result == ){
//                    // TODO: 2017/12/26 打电话时不能组呼
//                    ToastUtils.getToast().showMessageShort(mContext,mContext.getString(R.string.in_calling_phone),-1);
//                    return;
//                }
                userCall = result;
            }

        } else {
            //申请话权
            PrintLog.i("-----1-2)组呼按下--申请话权");
            UctClientApi.UCTGCallPressReq(userCall);
        }
    }

    /**
     * 组呼组呼按键抬起,释放话权
     */
    public void releaseGCallReq() {
        PrintLog.i("---4-1)组呼组呼按键抬起,主动去释放话权------");
        UctClientApi.UCTGCallPressRelReq(userCall);
        try{
        }catch (Exception e){
            e.printStackTrace();
            PrintLog.i("Exception="+e.getMessage());
        }
    }

    /**
     * 组呼挂断
     */
    public void hangUpCall() {
        UctClientApi.UCTGCallRelReq(0, currentGroupId,userCall);
        gCallRelease(currentGroupId);
        iGroupInfoView.gCallStateChanged(IGroupInfoView.GCALL_HANGUP,currentGroupId,null);
        UctApplication.getInstance().getGroupCallWindow().speakState(IGroupInfoView.GCALL_HANGUP,currentGroupId,null);

        //  不在主界面    在主界面
        if(!UctApplication.getInstance().isInMainActivity || MainActivity.currentFragmentPosition != 1){
            UctApplication.getInstance().getGroupCallWindow().hidePopupWindow();
        }
        //通知组呼挂断 （通话界面）
        postEventBean(ConstantUtils.GCALL_HANGUP,null,null);
    }

    public void release() {
        PrintLog.w("反注册GCallCallback");
        //组呼释放
        if(isRegistered){
            isRegistered = false;
            PrintLog.i("AA GCallCallback.release()----组呼释放---isRegistered="+isRegistered);
            UctClientApi.unregisterObserver(groupCallListener, IUCTGCallListener.IUCTGCALLLISTENER_INDEX);
        }
        isGCallMoReqSuccess = false;
        UctApplication.getInstance().isInGroupCall = false;
        SoundManager.setSpeakerphoneOn(mContext,false);
        CallCallBack.getInstance().setVoiceCallMode(STREAM_MUSIC);
        UctClientApi.unregisterObserver(this, PtytPreferenceChangeListener.RELEASELISTENER_INDEX);
    }


    @Override
    public void releaseAll(int i,boolean b, String s) {
        //离线释放资源
        PrintLog.i("releaseAll() s="+s + "  b="+b +"  i="+ i + "   UctApplication.getInstance().isInGroupCall="+UctApplication.getInstance().isInGroupCall);
        //组呼挂断
        hangUpCall();
    }

    //在进程中去寻找当前APP的信息，判断是否在前台运行
    private boolean isAppOnForeground() {
        ActivityManager activityManager =(ActivityManager) mContext.getApplicationContext().getSystemService(
                Context.ACTIVITY_SERVICE);
        String packageName =mContext.getApplicationContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null)
            return false;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean phoneIsInUse(Context context){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            return false;
        }
        try {
            TelecomManager tm = (TelecomManager)context.getSystemService(Context.TELECOM_SERVICE);
            return tm.isInCall();
        }catch (Exception e){}
        return false;
    }
}
