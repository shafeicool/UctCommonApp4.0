package com.ptyt.uct.callback;

import android.content.Context;
import android.os.Build;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.TextView;

import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.activity.VideoCallActivity;
import com.ptyt.uct.common.SettingsConstant;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.entity.TagBean;
import com.ptyt.uct.entity.TagBeanEntity;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.DateUtils;
import com.ptyt.uct.viewinterface.ICallView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.ptyt.uct.utils.ConstantUtils.AUDIO_SCALL;
import static com.ptyt.uct.utils.ConstantUtils.DOWNLOAD_VIDEO;
import static com.ptyt.uct.utils.ConstantUtils.UPLOAD_VIDEO;
import static com.ptyt.uct.utils.ConstantUtils.VIDEO_SCALL;

/**
 * @Description: 视频呼叫,语音呼叫，业务接口回调
 * @Date: 2017/9/29
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class CallPresenterCallBack {

    private static final int WHAT_TIME_KEEP_VIDEO = 1,WHAT_TIME_KEEP_AUDIO = 2;
    private static CallPresenterCallBack instance;
    private Context mContext;
    private ICallView iCallView;

    private VideoCallActivity mActivity;

    public static synchronized CallPresenterCallBack getInstance(){

        if(instance==null){
            instance = new CallPresenterCallBack();
        }
        return instance;
    }

    public void init(Context context,ICallView iCallView) {
        mContext = context;
        PrintLog.w("注册CallPresenterCallBack");
        mActivity = ((VideoCallActivity) context);
        this.iCallView = iCallView;
        CallCallBack.getInstance().isCallActivityOnCurrent = true;
        //默认后置摄像头
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_CAMERA, 0);
        //默认不打开闪光灯
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_CAMERA_SET_FLASHLIGHT, 0);
        //为了通知CallCallBack界面已开启
        CallCallBack.getInstance().onCallActivityCreated();
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void doActivityOnDestroy() {
        LinkedHashMap<Integer, EventBean> videoBusinessMap = CallCallBack.getInstance().callBusinessMap;
        Set<Map.Entry<Integer, EventBean>> entries = videoBusinessMap.entrySet();
        for (Map.Entry<Integer, EventBean> map:entries) {
            EventBean value = map.getValue();
            int hUserCall = value.gethUserCall();
            if(value.getBusinessTag() == AUDIO_SCALL || value.getBusinessTag() == VIDEO_SCALL){
                UctClientApi.uctSCallRelReq(0, hUserCall);
            }else if(value.getBusinessTag() == DOWNLOAD_VIDEO || value.getBusinessTag() == UPLOAD_VIDEO){
                UctClientApi.UctSVideoRelReq(0, hUserCall);
            }
        }
        for (int i = 0; i < mTimers.size(); i++) {
            Timer t = mTimers.get(i);
            if (t != null) {
                t.cancel();
                t.purge();
                t = null;
            }
        }
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_CAMERA_SET_FLASHLIGHT, 0);
        AppUtils.stopPlayMedia();
        tagBeanEntity = null;
        CallCallBack.getInstance().isCallActivityOnCurrent = false;
        if (instance != null) {
            instance = null;
        }
    }

    //开始时间
    private long speakStartTime;
    /**
     * 初始化计时器,以对呼叫时间进行计时
     */
    private List<Timer> mTimers = new ArrayList<>();
    private List<TimerTask> mTimerTasks = new ArrayList<>();
    private TagBeanEntity tagBeanEntity = null;
    public void initTimer(final TextView[] textView, final int hUserCall,final int WHAT) {
        textView[0].setVisibility(View.VISIBLE);
        textView[0].setText("");
        Timer timer = null;

        if(WHAT == WHAT_TIME_KEEP_AUDIO ){//audio sCall
            if(textView[0].getTag() != null && (((TagBeanEntity) textView[0].getTag()).getMap().get(-1)) != null){
                return;
            }
        }
        if(textView[0].getTag() == null || (((TagBeanEntity) textView[0].getTag()).getMap().get(hUserCall)) == null){
            speakStartTime = System.currentTimeMillis();
            timer = new Timer(true);
            TagBean tagBean = new TagBean(speakStartTime, timer,hUserCall);

            if(tagBeanEntity == null){
                tagBeanEntity = new TagBeanEntity();
            }
            if(WHAT == WHAT_TIME_KEEP_AUDIO){
                tagBeanEntity.setHUserCall(currentAudioHUerCall);
            }else{
                tagBeanEntity.setHUserCall(currentPureVideoHUserCall);
            }
            tagBeanEntity.getMap().put(hUserCall,tagBean);
            textView[0].setTag(tagBeanEntity);
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    Message msg = mHandler.obtainMessage();
                    msg.obj = textView;
                    msg.what = WHAT;
                    mHandler.sendMessage(msg);
                }
            };
            timer.schedule(timerTask,1000,1000);
            mTimers.add(timer);
            mTimerTasks.add(timerTask);
        }
    }
    public void setTimerRelease(TextView textView,final int hUserCall,final int callType){
        PrintLog.w("反注册CallPresenterCallBack");
//        textView.setVisibility(View.INVISIBLE);
        TagBeanEntity tagBeanEntity = (TagBeanEntity) textView.getTag();
        if(tagBeanEntity == null){
            return;
        }
        TagBean tagBean = tagBeanEntity.getMap().get(hUserCall);
        if(tagBean != null){
            Timer t = tagBean.getTimer();
//        textView.setTag(null);
            if (t != null) {
                t.cancel();
                t.purge();
                t = null;
            }
        }
//        if(callType == AUDIO_SCALL){
//            tagBeanEntity.getMap().remove(-1);
//        }else {
//        }
        tagBeanEntity.getMap().remove(hUserCall);
    }

    //用于区分视频业务的时间显示
    public int currentPureVideoHUserCall;
    public int currentAudioHUerCall;
    //说话时间长度
    android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what == WHAT_TIME_KEEP_VIDEO){
                TextView[] textView = (TextView[]) msg.obj;
                TagBeanEntity tagEntity = (TagBeanEntity) textView[0].getTag();
                HashMap<Integer, TagBean> tagBeanMap = tagEntity.getMap();
                TagBean tagBean = tagBeanMap.get(currentPureVideoHUserCall);
                if (tagBean != null) {
                    long duration = System.currentTimeMillis() - tagBean.getSpeakStartTime();
                    String minuteTime = DateUtils.getVideoDuring(duration);
                    textView[0].setText(minuteTime);
                }
            }else if(msg.what == WHAT_TIME_KEEP_AUDIO){
                TextView[] textViews = (TextView[]) msg.obj;
                TagBeanEntity tagEntity = (TagBeanEntity) textViews[0].getTag();
                HashMap<Integer, TagBean> tagBeanMap = tagEntity.getMap();
                TagBean tag = tagBeanMap.get(currentAudioHUerCall);
             if(tag != null){
                    long duration = System.currentTimeMillis() - tag.getSpeakStartTime();
                    String minuteTime = DateUtils.getVideoDuring(duration);
                    textViews[0].setText(minuteTime);
                    textViews[1].setText(minuteTime);
                }
            }
        }
    };

}
