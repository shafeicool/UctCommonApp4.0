package com.ptyt.uct.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.uct.IUCTAudioCallSuspend;
import com.android.uct.PtytPreferenceChangeListener;
import com.android.uct.ReleaseListener;
import com.android.uct.UctMeetListener;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.adapter.DialogPagerAdapter;
import com.ptyt.uct.callback.CallCallBack;
import com.ptyt.uct.callback.CallPresenterCallBack;
import com.ptyt.uct.callback.GCallCallback;
import com.ptyt.uct.common.HeadsetPlugBroadcastReceiver;
import com.ptyt.uct.common.SettingsConstant;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.entity.CallRecord;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.entity.Group;
import com.ptyt.uct.model.CallRecordDBManager;
import com.ptyt.uct.model.GroupDBManager;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.ScreenManager;
import com.ptyt.uct.utils.ScreenUtils;
import com.ptyt.uct.utils.SoundManager;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.viewinterface.ICallView;
import com.ptyt.uct.viewinterface.VideoGestureListener;
import com.ptyt.uct.widget.DividerItemDecoration;
import com.ptyt.uct.widget.WaveView2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

import static com.ptyt.uct.utils.ConstantUtils.AUDIO_SCALL;
import static com.ptyt.uct.utils.ConstantUtils.CALL_RECORD_ALREAD;
import static com.ptyt.uct.utils.ConstantUtils.CALL_RECORD_AUDIO_CALL_IN;
import static com.ptyt.uct.utils.ConstantUtils.CALL_RECORD_AUDIO_CALL_OUT;
import static com.ptyt.uct.utils.ConstantUtils.CALL_RECORD_UNREAD;
import static com.ptyt.uct.utils.ConstantUtils.CALL_RECORD_VIDEO_CALL_IN;
import static com.ptyt.uct.utils.ConstantUtils.CALL_RECORD_VIDEO_CALL_OUT;
import static com.ptyt.uct.utils.ConstantUtils.CALL_RECORD_VIDEO_DOWNLOAD_IN;
import static com.ptyt.uct.utils.ConstantUtils.CALL_RECORD_VIDEO_UPLOAD_IN;
import static com.ptyt.uct.utils.ConstantUtils.CALL_RECORD_VIDEO_UPLOAD_OUT;
import static com.ptyt.uct.utils.ConstantUtils.DOWNLOAD_VIDEO;
import static com.ptyt.uct.utils.ConstantUtils.MEETING_CALL;
import static com.ptyt.uct.utils.ConstantUtils.UPLOAD_VIDEO;
import static com.ptyt.uct.utils.ConstantUtils.VIDEO_DIRECTION_UPLOAD;
import static com.ptyt.uct.utils.ConstantUtils.VIDEO_SCALL;

/**
 * @Description: 1视频呼叫,2上传视频.(呼入  呼出) ,3来组呼 ,4来语音单呼
 * 原则:上传视频多路, 下载视频一路(意味着有视频呼叫时就不能有视频接收)，语音一路
 *
 * 追加业务分析：
 * A 视频呼叫接听后
 *   a1:+上传视频呼入
 *
 * B 上传视频接听后
 *   b1:+视频呼叫呼入
 *   b2:+上传视频呼入
 *   b3:+组呼呼入
 *   b4:+语音单呼呼入
 *
 * C 组呼接通中   (C 和 D 相当于 在进入VideoCallActivity时需判断当前业务是否有单呼和组呼的存在.)
 *   c1:+ 上传视频呼出
 *   c2:+ 上传视频呼入
 *
 * D 语音单呼接通中
 *   d1:+ 上传视频呼入   （问题--> 在单呼界面时，来了上传视频呼入,怎么处理?）
 *
 * 以上分析:由于D中存在的弊端，单呼来了上传视频呼入。将语音单呼界面和视频相关界面合并是否可行?
 *
 *
 * ...
 *
 * 继续分析:将语音单呼界面和视频相关界面合并在一个Activity是否可行?　　　--> 可行(20170714)
 * 语音通话界面简单，没什么问题，需做好不同业务的判断，控制view的隐藏显示
 *
 * 单呼来时，不管是否当前还有其他业务，都显示接听界面
 *
 *
 * @Date: 2017/5/15
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class VideoCallActivity extends BasePermissionActivity implements View.OnClickListener, View.OnTouchListener,ICallView,PtytPreferenceChangeListener,UctMeetListener,IUCTAudioCallSuspend,ReleaseListener{
    private static final int WHAT_TIME_KEEP_VIDEO = 1,WHAT_TIME_KEEP_AUDIO = 2,CAMERA_ORIENTATION_FRONT = 1,CAMERA_ORIENTATION_BACK = 0;
    private static final int VOICE_INIT = 0,VOICE_SWITCH = 1;
    private static final int VOICE_NO_PLUGIN_HANDS_FREE = 2;
    private  boolean switchFlishLight = false;
    //声音模式  1-有声音  0-没声音
    private int soundMode;
    //呼叫方向：0为主叫 1为被叫   iDirection:(被叫)1-接收视频 2-pc查看视频
    private int callDirection;//,iDirection;
    //视频细分业务：0-语音呼叫  1-视频呼叫 2-上传视频
    private int currentBusinessTag;
    private TextView tv_businessTypeRequest;
    private ImageView iv_hangup;
    private FrameLayout fl_remote;
    private FrameLayout fl_locate;
    //当前主界面显示的句柄
    private int currentHUserCall = -1;
    //语音/视频呼叫句柄
    private int audioSCallHandle,videoSCallHandle;
    private String callNumber;
    private String userName;
    private View view_camera;
    private View view_handsFree;
    private TextView tv_speakTime,tv_audioSpeakTimeRequest;
    //接听前的view
    private View view_videoRequest;
    //接听成功后的view
    private View view_videoAnswering;
    private View ll_answerRequest;
    //视频类型
    private ImageView iv_videoTypeAnswering;
    //上传视频列表
    private RecyclerView mRVUpVideo;
    private DialogPagerAdapter dialogPagerAdapter;
    //业务来时dialog中的集合,等待接听的集合
    public static List<EventBean> unAnsweredBusinessList;
    //左侧列表lineLayout的集合
    private List<EventBean> videoUploadList;
    private View ll_handsFreeRequest,ll_voiceSilenceRequest;
    private View view_audioCall;
    private TextView tv_SCallUserName;
    private View view_groupCall;
    private TextView tv_groupName;
    private ImageView iv_groupCall;
    private ImageView iv_soundWave;
    private GCallCallback gCallCallback;
    private AnimationDrawable drawableSoundWave;
    private LinkedHashMap<Integer, EventBean> callBusinessMap;
    private LinearLayout ll_videoUploadList;
    private TextView tv_voice_video;
    private ImageView iv_voice_video;
    private TextView tv_answeringUserName;
    private ImageView iv_handsFreeRequest;
    private TextView tv_SCallTimeShrink;
    private TextView tv_cancelRequest;
    private TextView tv_answerRequest;
    private ImageView iv_audioSCallShrink;
    private CallPresenterCallBack callPresenter;
    private ImageView iv_voiceSilenceRequest;
    private TextView tv_requestUserName;
    private TextView tv_userNameGCall;
    private TextView tv_userTelGCall;
    private WaveView2 waveView;
    private ImageView iv_otherSpeak;
    private AnimationDrawable iv_otherSpeakDrawable;
    private View view_meeting;
    private TextView tv_videoCallBusy;
    private AudioManager mAudioManager;
    private GestureDetector gestureDetector;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEvent(EventBean eventBean){
        String action = eventBean.getAction();
        if(action != null){
            //如果应用退到后台，来业务时需要唤醒.(已在VideoCallActivity时，退到后台来了业务需从新startActivity来唤醒)
            //组呼来时，当当前业务只有组呼则启动MainActivity
            boolean home = AppUtils.isHome(mContext);
            PrintLog.i("追加业务　currentHUserCall home="+home+" action="+action);
            if(home){
                Intent intent = new Intent(mContext, VideoCallActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
            switch (action){
                //A 语音呼叫进来
                case ConstantUtils.ACTION_AUDIO_CALL_COME_IN:
                    ScreenManager.getInstance().prepare(this);
                    SoundManager.setSpeakerphoneOn(mContext,true);
                    CallCallBack.getInstance().setVoiceCallMode(AudioManager.STREAM_MUSIC);
                    unAnsweredBusinessList.add(eventBean);
                    dialogPagerAdapter.notifyDataSetChanged();
                    tv_answerRequest.setText(getString(R.string.string_answer));
                    audioSCallHandle = eventBean.gethUserCall();
                    currentHUserCall = eventBean.gethUserCall();
                    setVideoInDialogShow();
                    PrintLog.i("A 语音呼叫add进来  currentHUserCall="+eventBean.gethUserCall() + "  PcName="+eventBean.getPcName() + "  unAnsweredBusinessList.size()="+unAnsweredBusinessList.size());
                    break;
                //B 视频呼叫进来
                case ConstantUtils.ACTION_VIDEO_CALL_COME_IN:
                    ScreenManager.getInstance().prepare(this);
                    SoundManager.setSpeakerphoneOn(mContext,true);
                    CallCallBack.getInstance().setVoiceCallMode(AudioManager.STREAM_MUSIC);
                    tv_answerRequest.setText(getString(R.string.string_answer));
                    currentHUserCall = eventBean.gethUserCall();
                    videoSCallHandle = eventBean.gethUserCall();
                    unAnsweredBusinessList.add(eventBean);
                    dialogPagerAdapter.notifyDataSetChanged();
                    setVideoInDialogShow();
                    PrintLog.i("B 视频呼叫add进来  currentHUserCall="+eventBean.gethUserCall() + "  PcName="+eventBean.getPcName() + "  unAnsweredBusinessList.size()="+unAnsweredBusinessList.size());
                    break;
                //C 下载视频进来
                case ConstantUtils.ACTION_DOWNLOAD_VIDEO_COME_IN:
                    PrintLog.i("C 下载视频add进来  currentHUserCall="+eventBean.gethUserCall() + "  PcName="+eventBean.getPcName());
                //D 上传视频进来
                case ConstantUtils.ACTION_UPLOAD_VIDEO_COME_IN:
                    ScreenManager.getInstance().prepare(this);
                    SoundManager.setSpeakerphoneOn(mContext,true);
                    CallCallBack.getInstance().setVoiceCallMode(AudioManager.STREAM_MUSIC);
                    tv_answerRequest.setText(getString(R.string.string_accept));
                    unAnsweredBusinessList.add(eventBean);
                    PrintLog.i("D 上传视频add进来  currentHUserCall="+eventBean.gethUserCall() + "  PcName="+eventBean.getPcName()+ "  unAnsweredBusinessList.size()="+unAnsweredBusinessList.size());
                    dialogPagerAdapter.notifyDataSetChanged();
                    setVideoInDialogShow();
                    /*ptyt begin _KeChuanqi_20171013*/
                    if(videoSCallHandle == 0){
                        currentHUserCall = eventBean.gethUserCall();
                    }
                    /*ptyt end*/
                    break;
                //E 组呼-状态改变
                case ConstantUtils.ACTION_GROUP_CALL_COME_IN:
                    PrintLog.i("E 组呼add进来 状态改变currentHUserCall");
                    //控制组呼的view显示
                    setGroupCallWindowView(eventBean);
                    break;
                case ConstantUtils.ACTION_OTHER_ACCEPT://对方接听
                    PrintLog.i("F 对方接听 eventBean.gethUserCall()="+eventBean.gethUserCall());
                    onAnswer(eventBean.gethUserCall(),false);
                    break;
                case ConstantUtils.ACTION_OTHER_HANGUP://对方挂断
                    PrintLog.i("G 对方挂断 eventBean.gethUserCall()="+eventBean.gethUserCall());
                    doHangUp(eventBean.gethUserCall(),false,false);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                PrintLog.i("ACTION_HEADSET_PLUG");
                if(intent.hasExtra("state")) {
                    PrintLog.i("ACTION_HEADSET_PLUG  state="+intent.getIntExtra("state", 0));
                    if(intent.getIntExtra("state", 0) == 1) {//耳机插入时强制取消免提
                        iv_voice_video.setImageResource(R.mipmap.btn_handsfree_nor);
                        iv_handsFreeRequest.setImageResource(R.mipmap.btn_handsfree_nor);
                    }else if(intent.getIntExtra("state", 0) == 0){//耳机拔出,免提
                        iv_voice_video.setImageResource(R.mipmap.btn_handsfree_pre);
                        iv_handsFreeRequest.setImageResource(R.mipmap.btn_handsfree_pre);
                    }
                }
            }
        }
    };

    @Override
    protected void initView() {
        super.initView();
        ScreenManager.getInstance().prepare(VideoCallActivity.this);
        registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_video_scall);
        //呼叫方向：0为主叫 1为被叫
        callDirection = getIntent().getIntExtra("callDirection",ConstantUtils.CALL_DIRECTION_ACTIVE);
        currentBusinessTag = getIntent().getIntExtra("businessTag",0);//视频细分业务：0-语音呼叫  1-视频呼叫 2-上传视频 3-下载
        callNumber = getIntent().getStringExtra("callNumber");
        userName = getIntent().getStringExtra("userName");
        initWidget();
        //配置改变
        UctClientApi.registerObserver(this, PtytPreferenceChangeListener.PTYTPREFERENCECHANGELISTENER);
        //会议
        UctClientApi.registerObserver(this, PtytPreferenceChangeListener.UCTMEETLISTENER_INDEX);
        //强插强拆
        UctClientApi.registerObserver(this, PtytPreferenceChangeListener.IUCTAUDIOCALLSUSPEND_INDEX);
        //网络断开注册
        UctClientApi.registerObserver(this, PtytPreferenceChangeListener.RELEASELISTENER_INDEX);
        VideoGestureListener gestureListener = new VideoGestureListener();
        gestureDetector = new GestureDetector(mContext, gestureListener);

    }

    private void initWidget() {
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        callBusinessMap = CallCallBack.getInstance().callBusinessMap;
        gCallCallback = GCallCallback.getInstance();
        soundMode = 1;
        //是否静音
        switchFlishLight = false;
        videoUploadList = new ArrayList<>();
        view_videoAnswering = findViewById(R.id.view_videoAnswering);
        view_videoAnswering.setVisibility(View.GONE);
        view_videoRequest = findViewById(R.id.view_videoRequest);
        initVideoRequestView();
        initVideoAnsweringView();
        initVideoInDialog();
        initFirstEnterBusinessView(callDirection,currentBusinessTag,-2);
        /**
         * eventBus注册必须放在callPresenter.init前面,避免在Activity还在onCreate()时来了事件，此时如果发送堵塞的事件,提前注册了点话能够接收;
         * 同时要放在初始化view的后面，因为接收后进行了view操作.
         */
        EventBus.getDefault().register(this);
        callPresenter = CallPresenterCallBack.getInstance();
        callPresenter.init(this,this);
    }

    /**
     * 进入该界面时，根据业务显示界面
     */
    private EventBean firstEventBean = null;
    private void initFirstEnterBusinessView(int callDirection,int currentBusinessTag,int TAG) {
        PrintLog.i("initWidget()   currentBusinessTag="+currentBusinessTag +"  callDirection="+callDirection);
        if (callDirection == ConstantUtils.CALL_DIRECTION_ACTIVE) {//A 主叫(主叫没有视频查看; request界面显示:静音+挂断+免提(视频上传只有挂断)   隐藏:接听)
            CallCallBack.getInstance().setVoiceCallMode(AudioManager.STREAM_VOICE_CALL);
            ll_answerRequest.setVisibility(View.GONE);
            //主叫免提取消
            SoundManager.setSpeakerphoneOn(this,false);
            iv_voice_video.setImageResource(R.mipmap.btn_handsfree_nor);
            switch (currentBusinessTag) {
                case VIDEO_SCALL://视频呼叫
                    //第一个参数 呼叫号码;第二个参数0表示语音呼叫 1表示视频呼叫
                    ll_voiceSilenceRequest.setVisibility(View.VISIBLE);
                    ll_handsFreeRequest.setVisibility(View.VISIBLE);
                    currentHUserCall = UctClientApi.uctSCallMoReq(callNumber, VIDEO_SCALL);
                    videoSCallHandle = currentHUserCall;
                    if (currentHUserCall < 0) {
                        ToastUtils.getToast().showMessageShort(this, mContext.getString(R.string.string_video_call_fail), -1);
                        videoInDialog.hide();
                        finish();
                    } else {
                        tv_businessTypeRequest.setText(mContext.getString(R.string.string_video_calling));
                    }
                    firstEventBean = new EventBean(null, callNumber, "", userName, ConstantUtils.CALL_DIRECTION_ACTIVE, currentHUserCall, ConstantUtils.VIDEO_SCALL);
                    CallCallBack.getInstance().videoDownloadNum++;
                    break;
                case UPLOAD_VIDEO://上传视频
                    ll_voiceSilenceRequest.setVisibility(View.GONE);
                    ll_handsFreeRequest.setVisibility(View.GONE);
                    //第一个参数 呼叫号码;第二个适配的类型
                    currentHUserCall = UctClientApi.UctSVideoMoReq(callNumber, ConstantUtils.NORMAL_VIDEO, VIDEO_DIRECTION_UPLOAD);
                    if (currentHUserCall < 0) {
                        ToastUtils.getToast().showMessageShort(this, mContext.getString(R.string.string_video_upload_fail), -1);
                        videoInDialog.hide();
                        finish();
                    } else {
                        tv_businessTypeRequest.setText(mContext.getString(R.string.string_video_upload_to_other_requesting));
                    }
                    firstEventBean = new EventBean(null, callNumber, "", userName, ConstantUtils.CALL_DIRECTION_ACTIVE, currentHUserCall, ConstantUtils.UPLOAD_VIDEO);
                    break;
                case MEETING_CALL://会议的语音
                case AUDIO_SCALL://语音单呼
                    ll_voiceSilenceRequest.setVisibility(View.VISIBLE);
                    ll_handsFreeRequest.setVisibility(View.VISIBLE);
                    //参数1:呼叫号码;参数2:  0语音呼叫 1视频呼叫
                    currentHUserCall = UctClientApi.uctSCallMoReq(callNumber, AUDIO_SCALL);
                    audioSCallHandle = currentHUserCall;
                    if (currentHUserCall < 0) {
                        ToastUtils.getToast().showMessageShort(this, mContext.getString(R.string.string_audio_call_fail), -1);
                        videoInDialog.hide();
                        finish();
                    } else {
                        tv_businessTypeRequest.setText(mContext.getString(R.string.string_audio_calling));
                    }
                    firstEventBean = new EventBean(null, callNumber, "", userName, ConstantUtils.CALL_DIRECTION_ACTIVE, currentHUserCall, ConstantUtils.AUDIO_SCALL);
                    break;
            }
            if(callBusinessMap.size() > 0){
                Set<Map.Entry<Integer, EventBean>> entries = callBusinessMap.entrySet();
                for (Map.Entry<Integer, EventBean> map : entries) {
                    unAnsweredBusinessList.add(map.getValue());
                }
                dialogPagerAdapter.notifyDataSetChanged();
                setVideoInDialogShow();
            }
            callBusinessMap.put(currentHUserCall,firstEventBean);
        }
        else if(callDirection == ConstantUtils.CALL_DIRECTION_PASSIVE){//B 被叫 (request界面显示:挂断+接听   隐藏:静音+免提)
            CallCallBack.getInstance().setVoiceCallMode(AudioManager.STREAM_MUSIC);

            initVoiceHandsFree(VOICE_NO_PLUGIN_HANDS_FREE);
            if(TAG == -2){
                currentHUserCall = getIntent().getIntExtra("hUserCall",-1);
            }else {
                currentHUserCall = TAG;
            }
            ll_answerRequest.setVisibility(View.VISIBLE);
            ll_voiceSilenceRequest.setVisibility(View.GONE);
            ll_handsFreeRequest.setVisibility(View.GONE);
            tv_cancelRequest.setText(mContext.getString(R.string.string_hang_up));
            view_videoRequest.setVisibility(View.VISIBLE);
            view_videoAnswering.setVisibility(View.GONE);

            switch (currentBusinessTag){
                case VIDEO_SCALL:
                    videoSCallHandle = currentHUserCall;
                    tv_businessTypeRequest.setText(mContext.getString(R.string.string_video_call_other_requesting));
                    tv_answerRequest.setText(getString(R.string.string_answer));
                    break;
                case UPLOAD_VIDEO:
                    tv_businessTypeRequest.setText(mContext.getString(R.string.string_video_upload_to_other_requesting));
                    tv_answerRequest.setText(getString(R.string.string_accept));
                    break;
                case MEETING_CALL:
                case AUDIO_SCALL:
                    audioSCallHandle = currentHUserCall;
                    tv_businessTypeRequest.setText(mContext.getString(R.string.string_audio_call_other_requesting));
                    tv_answerRequest.setText(getString(R.string.string_answer));
                    break;
                case DOWNLOAD_VIDEO:
                    tv_businessTypeRequest.setText(mContext.getString(R.string.string_video_download_other_requesting));
                    tv_answerRequest.setText(getString(R.string.string_accept));
                    break;
            }
            if(TAG == -2){
                firstEventBean = callBusinessMap.get(currentHUserCall);
                if(callBusinessMap.size() > 1){
                    Set<Map.Entry<Integer, EventBean>> entries = callBusinessMap.entrySet();
                    for (Map.Entry<Integer, EventBean> map : entries) {
                        if(!firstEventBean.equals(map.getValue())){
                            unAnsweredBusinessList.add(map.getValue());
                        }
                    }
                    dialogPagerAdapter.notifyDataSetChanged();
                    setVideoInDialogShow();
                }
            }
        }
    }

    /**
     * 初始化视频接听前的view
     */
    private void initVideoRequestView() {
        findViewById(R.id.iv_answerRequest).setOnClickListener(this);
        findViewById(R.id.iv_cancelRequest).setOnClickListener(this);
        tv_requestUserName = ((TextView) findViewById(R.id.tv_requestUserName));
        if(TextUtils.isEmpty(userName)){
            tv_requestUserName.setText(callNumber+"");
        }else{
            tv_requestUserName.setText(userName);
        }
        tv_answerRequest = ((TextView) findViewById(R.id.tv_answerRequest));
        tv_businessTypeRequest = (TextView) findViewById(R.id.tv_businessTypeRequest);
        iv_audioSCallShrink = ((ImageView) findViewById(R.id.iv_audioSCallShrink));
        ll_answerRequest = findViewById(R.id.ll_answerRequest);
        ll_handsFreeRequest = findViewById(R.id.ll_handsFreeRequest);
        ll_voiceSilenceRequest = findViewById(R.id.ll_voiceSilenceRequest);
        iv_handsFreeRequest = ((ImageView) findViewById(R.id.iv_handsFreeRequest));
        iv_handsFreeRequest.setOnClickListener(this);
        iv_voiceSilenceRequest = ((ImageView) findViewById(R.id.iv_voiceSilenceRequest));
        iv_voiceSilenceRequest.setOnClickListener(this);
        tv_audioSpeakTimeRequest = ((TextView) findViewById(R.id.tv_audioSpeakTimeRequest));
        tv_cancelRequest = ((TextView) findViewById(R.id.tv_cancelRequest));
        //语音呼叫全屏时缩小点击
        iv_audioSCallShrink.setOnClickListener(this);
    }

    /**
     * 初始化视频接听成功后的view
     */
    private void initVideoAnsweringView() {
        tv_answeringUserName = ((TextView) findViewById(R.id.tv_answeringUserName));
        if(TextUtils.isEmpty(userName)){
            tv_answeringUserName.setText(callNumber+"");
        }else{
            tv_answeringUserName.setText(userName);
        }
        iv_hangup = ((ImageView) findViewById(R.id.iv_hangup));
        fl_locate = ((FrameLayout) findViewById(R.id.fl_locate));
        fl_remote = ((FrameLayout) findViewById(R.id.fl_remote));
        ll_videoUploadList = ((LinearLayout) findViewById(R.id.ll_videoUploadList));
        mRVUpVideo = ((RecyclerView) findViewById(R.id.recyclerView_upVideo));
        mRVUpVideo.setHasFixedSize(true);
        mRVUpVideo.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRVUpVideo.setLayoutManager(new LinearLayoutManager(this));
        iv_hangup.setOnClickListener(this);
        findViewById(R.id.iv_camera).setOnClickListener(this);
        iv_voice_video = ((ImageView) findViewById(R.id.iv_voice_video));
        tv_voice_video = ((TextView) findViewById(R.id.tv_voice_video));
        iv_voice_video.setOnClickListener(this);
        view_camera = findViewById(R.id.rl_camera);
        view_handsFree = findViewById(R.id.rl_hands_free);
        tv_speakTime = ((TextView) findViewById(R.id.tv_speakTime));
        iv_videoTypeAnswering = ((ImageView) findViewById(R.id.iv_videoTypeAnswering));
        //语音呼叫缩略框点
        view_audioCall = findViewById(R.id.cardView_SCall);
        view_meeting = findViewById(R.id.view_meeting);
        view_audioCall.setVisibility(View.GONE);
        view_audioCall.setOnClickListener(this);
        tv_SCallUserName = ((TextView) findViewById(R.id.tv_SCallUserName));
        tv_SCallTimeShrink = ((TextView) findViewById(R.id.tv_SCallTimeShrink));
        view_groupCall = findViewById(R.id.view_groupCall);
        tv_groupName = ((TextView) findViewById(R.id.tv_groupName));
        iv_groupCall = ((ImageView) findViewById(R.id.iv_groupCall));
        iv_soundWave = ((ImageView) findViewById(R.id.iv_soundWave));
        drawableSoundWave = ((AnimationDrawable) iv_soundWave.getDrawable());
        iv_groupCall.setOnTouchListener(this);
        tv_userNameGCall = ((TextView) findViewById(R.id.tv_userNameGCall));
        tv_userTelGCall = ((TextView) findViewById(R.id.tv_userTelGCall));
        waveView = ((WaveView2) findViewById(R.id.wave_view));
        iv_otherSpeak = ((ImageView) findViewById(R.id.iv_otherSpeak));
        iv_otherSpeakDrawable = ((AnimationDrawable) iv_otherSpeak.getDrawable());
        tv_videoCallBusy = ((TextView) findViewById(R.id.tv_videoCallBusy));
    }

    /**
     * 初始化组呼状态显示view
     */
    private void setGroupCallWindowView(EventBean eventBean) {
        if(eventBean == null){
            return;
        }
        if(tv_groupName.getText().toString().trim().length() < 1 && eventBean.getcGid() != null){
            Group group = GroupDBManager.getInstance().queryGroupByID(eventBean.getcGid());
            tv_groupName.setText(group.getGroupName());
        }
        int detailAction = eventBean.getDetailAction();
        switch (detailAction){
            case ConstantUtils.OTHER_LAUCHER_GCALL://其他人发起组呼
                view_groupCall.setVisibility(View.VISIBLE);
            case ConstantUtils.OTHER_SPEAK://其他人说话
                iv_groupCall.setImageResource(R.mipmap.ic_gcall_voice_shrink);
                if(drawableSoundWave.isRunning()){
                    drawableSoundWave.stop();
                }
                iv_soundWave.setVisibility(View.GONE);
                tv_userNameGCall.setVisibility(View.VISIBLE);
                tv_userTelGCall.setVisibility(View.VISIBLE);
                iv_otherSpeak.setVisibility(View.VISIBLE);
                tv_userNameGCall.setText(eventBean.getPcName());
                tv_userTelGCall.setText("("+eventBean.getPcCalling()+")");
                waveView.start();
                if(!iv_otherSpeakDrawable.isRunning()){
                    iv_otherSpeakDrawable.start();
                }
                break;
            case ConstantUtils.GCALL_HANGUP://组呼挂断
                view_groupCall.setVisibility(View.GONE);
                tv_groupName.setText("");
                if (drawableSoundWave.isRunning()) {
                    drawableSoundWave.stop();
                }
                waveView.stop();
                iv_otherSpeakDrawable.stop();
                break;
            case ConstantUtils.SELF_SPEAK://自己说话
                iv_groupCall.setImageResource(R.mipmap.ic_gcall_voice_shrink);
                if(!drawableSoundWave.isRunning()){
                    drawableSoundWave.start();
                }
                iv_soundWave.setVisibility(View.VISIBLE);
                tv_userNameGCall.setVisibility(View.INVISIBLE);
                tv_userTelGCall.setVisibility(View.INVISIBLE);
                iv_otherSpeak.setVisibility(View.INVISIBLE);
                waveView.start();
                break;
            case ConstantUtils.NOBODY_SPEAK://没人说话
                iv_groupCall.setImageResource(R.mipmap.ic_gcall_voice_shrink_nor);
                if(drawableSoundWave.isRunning()){
                    drawableSoundWave.stop();
                }
                iv_soundWave.setVisibility(View.VISIBLE);
                tv_userNameGCall.setVisibility(View.INVISIBLE);
                tv_userTelGCall.setVisibility(View.INVISIBLE);
                iv_otherSpeak.setVisibility(View.INVISIBLE);
                waveView.stop();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        PrintLog.i("onResume()");
        super.onResume();
        //如果进来之前就有组呼存在，则应该显示组呼缩略框(不同组呼状态),同时隐藏悬浮窗
        if(UctApplication.getInstance().isInGroupCall){
            UctApplication.getInstance().getGroupCallWindow().hidePopupWindow();
            view_groupCall.setVisibility(View.VISIBLE);
            //不同组呼状态
            setGroupCallWindowView(GCallCallback.getInstance().currentEventBean);
        }
    }

    @Override
    protected void onRestart() {
        PrintLog.i("onRestart()");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        PrintLog.i("onStart()");
        super.onStart();
    }

    /**
     * 初始化上传视频被叫的谈出对话框
     */
    Dialog videoInDialog = null;ViewPager vp_videoInDialog;
    private int dialogDimensionX,dialogDimensionY;
    private void initVideoInDialog() {
        dialogDimensionX = (int) getResources().getDimension(R.dimen.y700);
        dialogDimensionY = (int) getResources().getDimension(R.dimen.x514);
        View dialog_videoComeIn = View.inflate(this, R.layout.dialog_video_come_in, null);
        videoInDialog = new Dialog(this,R.style.dialog_transparent);
        videoInDialog.setContentView(dialog_videoComeIn);
        videoInDialog.setCanceledOnTouchOutside(false);
        vp_videoInDialog = ((ViewPager) dialog_videoComeIn.findViewById(R.id.vp_videoIn));
        unAnsweredBusinessList = new ArrayList<>();
        dialogPagerAdapter = new DialogPagerAdapter(VideoCallActivity.this);
        vp_videoInDialog.setAdapter(dialogPagerAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_answerRequest://接听
                PrintLog.i("接听点击 currentHUserCall="+currentHUserCall +"    callBusinessMap.size()"+ callBusinessMap.size());
                startReqPermOfCamera();
                UctClientApi.uctSCallMtResponse(currentHUserCall);
                firstEventBean.setAnswered(true);
                onAnswer(currentHUserCall,false);
                break;
            case R.id.iv_cancelRequest:
            case R.id.iv_hangup://挂断设置通话成功时的界面状态
                PrintLog.i("挂断点击 currentHUserCall="+currentHUserCall +"    callBusinessMap.size()"+ callBusinessMap.size());
                doHangUp(currentHUserCall, true,false);
                break;
            case R.id.iv_voice_video://视频 免提 或 手电筒
                String state = tv_voice_video.getText().toString().trim();
                if(state.equals(mContext.getString(R.string.string_flashlight))){//手电筒
                    if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
                    {
                        ToastUtils.getToast().showMessageShort(this, getString(R.string.string_video_call_prompt2),-1);
                    }else {
                        if(switchFlishLight){
                            UctClientApi.saveUserData(SettingsConstant.SETTINGS_CAMERA_SET_FLASHLIGHT, 0);
                            iv_voice_video.setImageResource(R.mipmap.btn_flashlight_nor);
                            switchFlishLight = false;
                        }else{
                            int cameraOrientation = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_VIDEO_CAMERA, CAMERA_ORIENTATION_BACK);
                            if(cameraOrientation == CAMERA_ORIENTATION_BACK){
                                UctClientApi.saveUserData(SettingsConstant.SETTINGS_CAMERA_SET_FLASHLIGHT, 1);
                                iv_voice_video.setImageResource(R.mipmap.btn_flashlight_on_pre);
                                switchFlishLight = true;
                            }else{
                                ToastUtils.getToast().showMessageShort(this, getString(R.string.string_video_call_prompt1),-1);
                            }
                        }
                    }
                }else{
                    initVoiceHandsFree(VOICE_SWITCH);
                }
                break;
            case R.id.iv_camera://摄像头切换
                /*ptyt start 解决切换摄像头时闪光灯关掉而图标未更新的bug kechuanqi_20180104*/
                String state1 = tv_voice_video.getText().toString().trim();
                if(state1.equals(mContext.getString(R.string.string_flashlight))){//手电筒
                    iv_voice_video.setImageResource(R.mipmap.btn_flashlight_nor);
                    switchFlishLight = false;
                }
                /*ptyt end*/
                //默认后摄像头
                int cameraOrientation = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_VIDEO_CAMERA, CAMERA_ORIENTATION_BACK);
                if(cameraOrientation == CAMERA_ORIENTATION_BACK){//设置后置摄像头
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_CAMERA, CAMERA_ORIENTATION_FRONT);
                }else {//设置前置摄像头
                    PrintLog.i("currentHUserCall getNumberOfCameras()="+android.hardware.Camera.getNumberOfCameras());
                    if(android.hardware.Camera.getNumberOfCameras() >= 2){
                        UctClientApi.saveUserData(SettingsConstant.SETTINGS_VIDEO_CAMERA, CAMERA_ORIENTATION_BACK);
                    }else {
                        ToastUtils.getToast().showMessageShort(mContext,getString(R.string.string_video_call_prompt3),-1);
                    }
                }
                break;
            case R.id.iv_voiceSilenceRequest://语音呼叫 静音
                if(soundMode == 1){
                    UctClientApi.UctAudioCtrl(currentHUserCall,0);//1:有声音; 0:没声没声音
                    iv_voiceSilenceRequest.setImageResource(R.drawable.btn_mute_pre);
                    soundMode = 0;
                }else{
                    UctClientApi.UctAudioCtrl(currentHUserCall,1);
                    iv_voiceSilenceRequest.setImageResource(R.drawable.btn_mute_nor);
                    soundMode = 1;
                }
                break;
            case R.id.iv_handsFreeRequest://语音呼叫 免提
                initVoiceHandsFree(VOICE_SWITCH);
                break;
            case R.id.cardView_SCall://语音单呼缩略框
                //切换界面到语音呼叫界面
                currentHUserCall = audioSCallHandle;
                ScreenUtils.setScreenOrientation(this,Configuration.ORIENTATION_PORTRAIT);
                view_videoRequest.setVisibility(View.VISIBLE);
                view_videoAnswering.setVisibility(View.GONE);
                if(callBusinessMap.size() > 1){
                    iv_audioSCallShrink.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.iv_audioSCallShrink://语音单呼全屏中的缩小当含有视频业务时
                ScreenUtils.setScreenOrientation(this,Configuration.ORIENTATION_LANDSCAPE);
                view_videoRequest.setVisibility(View.GONE);
                view_videoAnswering.setVisibility(View.VISIBLE);
                iv_audioSCallShrink.setVisibility(View.GONE);
                setNextHUserCall(true,false);
                break;
        }
    }
    /**
     * 挂断时操作，先从列表移除
     * @param hUserCall
     * @param isActiveHangUp 是否主动挂断,主动挂断uctSCallRelReq()释放方法
     * @param isPreHangUp 还未接通，是否接听对话框中选择挂断(但在未接听时对方挂断时，你不知道当前是否已接听)
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void doHangUp(int hUserCall, boolean isActiveHangUp, boolean isPreHangUp) {
        //首个业务未接听 && 当前挂断的不是第一个业务  && map中还未移除  &&  当前主界面不为该业务
        if(!firstEventBean.isAnswered() && firstEventBean.gethUserCall() != hUserCall && callBusinessMap.get(firstEventBean.gethUserCall()) != null){
            if(!unAnsweredBusinessList.contains(firstEventBean)){
                unAnsweredBusinessList.add(firstEventBean);
                dialogPagerAdapter.notifyDataSetChanged();
            }
        }
        AppUtils.stopPlayMedia();//挂断时停止语音提醒
        EventBean eventBean = callBusinessMap.get(hUserCall);
        if(eventBean == null){
            return;
        }
        //保存通话记录到数据库
        CallRecord callRecord = new CallRecord();
        callRecord.setName(eventBean.getPcName());
        callRecord.setNumber(eventBean.getPcCalling());
        if(!eventBean.isAnswered() && eventBean.getCallDirection() == 1){//呼叫方向：0为主叫 1为被叫
            callRecord.setIsRead(CALL_RECORD_UNREAD);
        }else{
            callRecord.setIsRead(CALL_RECORD_ALREAD);
        }
        callRecord.setRecordTime(new Long(System.currentTimeMillis()));

        //避免在未接听时对方挂断
        boolean isRemoved = unAnsweredBusinessList.remove(eventBean);

        if(isRemoved){
            dialogPagerAdapter.notifyDataSetChanged();
            if (unAnsweredBusinessList.size() == 0) {
                videoInDialog.hide();
            } else {
                setVideoInDialogShow();
            }
        }
        PrintLog.i("挂断点击doHangUp() currentHUserCall=" + hUserCall + "   callBusinessMap.size()=" + callBusinessMap.size()+"  isRemoved="+isRemoved);
        if(eventBean == null){
            return;
        }
        int businessTag = eventBean.getBusinessTag();
        switch (businessTag) {
            case MEETING_CALL:
            case AUDIO_SCALL:
                ToastUtils.getToast().showMessageShort(VideoCallActivity.this, mContext.getString(R.string.string_audio_call_hangup), -1);
                view_audioCall.setVisibility(View.GONE);
                audioSCallHandle = 0;
                if(eventBean.getCallDirection() == 0){//0为主叫 1为被叫
                    callRecord.setType(CALL_RECORD_AUDIO_CALL_OUT);
                }else{
                    callRecord.setType(CALL_RECORD_AUDIO_CALL_IN);
                }
                callSuspendMap.remove(hUserCall);
                break;
            case VIDEO_SCALL:
                ToastUtils.getToast().showMessageShort(VideoCallActivity.this, mContext.getString(R.string.string_video_call_hangup), -1);
                view_audioCall.setVisibility(View.GONE);
                if(CallCallBack.getInstance().videoDownloadNum > 0){
                    CallCallBack.getInstance().videoDownloadNum--;
                }
                videoSCallHandle = 0;
                if(eventBean.getCallDirection() == 0){//0为主叫 1为被叫
                    callRecord.setType(CALL_RECORD_VIDEO_CALL_OUT);
                }else{
                    callRecord.setType(CALL_RECORD_VIDEO_CALL_IN);
                }
                break;
            case UPLOAD_VIDEO:
                ToastUtils.getToast().showMessageShort(VideoCallActivity.this, mContext.getString(R.string.string_video_upload_hangup), -1);
                if(eventBean.getCallDirection() == 0){//0为主叫 1为被叫
                    callRecord.setType(CALL_RECORD_VIDEO_UPLOAD_OUT);
                }else{
                    callRecord.setType(CALL_RECORD_VIDEO_UPLOAD_IN);
                }
                videoUploadList.remove(eventBean);
                break;
            case DOWNLOAD_VIDEO:
                ToastUtils.getToast().showMessageShort(VideoCallActivity.this, mContext.getString(R.string.string_video_download_hangup), -1);
                CallCallBack.getInstance().videoDownloadNum--;
                videoUploadList.remove(eventBean);
                callRecord.setType(CALL_RECORD_VIDEO_DOWNLOAD_IN);
                break;
        }
        if((businessTag == AUDIO_SCALL || businessTag == MEETING_CALL) && !isPreHangUp){
            String time = tv_audioSpeakTimeRequest.getText().toString();
            if(!TextUtils.isEmpty(time)){
                callRecord.setCallTime(time);
            }else {
                callRecord.setCallTime(null);
            }
            callPresenter.setTimerRelease(tv_audioSpeakTimeRequest,hUserCall,AUDIO_SCALL);
            tv_audioSpeakTimeRequest.setText("");
            tv_SCallTimeShrink.setText("");
        }else if(!isPreHangUp){
            String time = tv_speakTime.getText().toString();
            if(!TextUtils.isEmpty(time)){
                callRecord.setCallTime(time);
            }else {
                callRecord.setCallTime(null);
            }
            callPresenter.setTimerRelease(tv_speakTime,hUserCall,VIDEO_SCALL);
        }else{
            callRecord.setCallTime(null);
        }
        //将通话记录保存至数据库
        CallRecordDBManager.getInstance(mContext).insertCallRecord(callRecord);
        if(callRecord.getIsRead() == CALL_RECORD_UNREAD){//被叫未接听
            EventBus.getDefault().post(new EventBean(ConstantUtils.ACTION_CALL_UNREAD_NOTIFY));
        }
        /**
         *
         * 语音呼叫/视频呼叫   自己挂断:doHangUp() -> UCT_SCallRelInd()
         *                   对方挂断:              UCT_SCallRelInd()
         *  (不管是自己还是对方挂断都会走UCT_SCallRelInd(),so在UCT_SCallRelInd()里处理释放业务map移除)
         *
         *
         * 纯视频    自己挂断: doHangUp()          (移除map)
         *          对方挂断: UCT_SVideoRelInd()  (移除map)
         *
         */
        callBusinessMap.remove(hUserCall);
        if(isActiveHangUp){
            switch (businessTag) {
                case MEETING_CALL:
                case AUDIO_SCALL://语音呼叫
                case VIDEO_SCALL://视频呼叫
                    int result = UctClientApi.uctSCallRelReq(0, hUserCall);
                    PrintLog.i("挂断点击doHangUp()2-0  result=" + result);
                    break;
                case DOWNLOAD_VIDEO:
                case UPLOAD_VIDEO://上传视频
                    if(callBusinessMap.size() > 0){
                        UctClientApi.UctSVideoRelReq(0, hUserCall);
                    }
                    break;
            }
        }
        if(callBusinessMap.size() <= 0){
            videoInDialog.hide();
            PrintLog.i("挂断点击doHangUp()2 currentHUserCall=" + hUserCall + "   callBusinessMap.size()=" + callBusinessMap.size());
            finish();
        }else{
            PrintLog.i("挂断点击doHangUp()2-2");
            if(isPreHangUp){
                if(callSuspendMap.size() > 0 && (businessTag == AUDIO_SCALL || businessTag == VIDEO_SCALL)){//如果还有待激活且没有其他语音业务
                    Set<Map.Entry<Integer, EventBean>> es = callSuspendMap.entrySet();
                    EventBean value = null;
                    for (Map.Entry<Integer, EventBean> map : es) {
                        value = map.getValue();
                    }
                    UctClientApi.UctActiveCall(value.gethUserCall());//主动激活
                    tv_businessTypeRequest.setText(getString(R.string.string_audio_call_answering));
                    tv_videoCallBusy.setVisibility(View.GONE);
                    callSuspendMap.remove(currentHUserCall);
                }
            }
            setNextHUserCall(true,isPreHangUp);
        }
        PrintLog.i("挂断点击doHangUp()2-3");
    }

    @Override
    public void finish() {
        super.finish();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onDestroy() {
        //销毁时关掉所有业务
        PrintLog.i("onDestroy()");
        localSurface = null;
        remoteSurface = null;
        if (localParent != null) {
            localParent.removeAllViews();
            localParent = null;
        }
        if (remoteParent != null) {
            remoteParent.removeAllViews();
            remoteParent = null;
        }
        if (fl_locate != null) {
            fl_locate.removeAllViews();
            fl_locate = null;
        }
        if (fl_remote != null) {
            fl_remote.removeAllViews();
            fl_remote = null;
        }
        if (ll_videoUploadList != null) {
            ll_videoUploadList.removeAllViews();
            ll_videoUploadList = null;
        }
        callPresenter.doActivityOnDestroy();
        videoInDialog.dismiss();
        unAnsweredBusinessList.clear();
        dialogPagerAdapter.notifyDataSetChanged();
        //调度台调用视频接通后终端主动挂断,调度台需要自动再一次调用_KeChuanqi_20171106
        // 这样做的目的是onDestroy() 之后再释放，避免再次调起来业务时界面还没消毁而直接走onDestroy了
        UctClientApi.UctSVideoRelReq(0, currentHUserCall);
        super.onDestroy();
        if(CallCallBack.getInstance().wakeLock.isHeld()){
            CallCallBack.getInstance().wakeLock.release();
        }
        EventBus.getDefault().unregister(this);
        CallCallBack.getInstance().blockEventBeans.clear();
        CallCallBack.getInstance().setVoiceCallMode(AudioManager.STREAM_MUSIC);
        callBusinessMap.clear();
        CallCallBack.getInstance().videoDownloadNum = 0;
        waveView.destroy();
        //配置信息改变监听
        UctClientApi.unregisterObserver(this, PtytPreferenceChangeListener.PTYTPREFERENCECHANGELISTENER);
        UctClientApi.unregisterObserver(this, PtytPreferenceChangeListener.UCTMEETLISTENER_INDEX);
        UctClientApi.unregisterObserver(this, PtytPreferenceChangeListener.IUCTAUDIOCALLSUSPEND_INDEX);
        UctClientApi.unregisterObserver(this, PtytPreferenceChangeListener.RELEASELISTENER_INDEX);
        unregisterReceiver(headsetReceiver);
    }


    /**
     * 设置最新当前显示的HUserCall,挂断时使用
     * @param isSwitchView 是否是切换界面操作
     * @param isPreHangUp 是否是还未接听的挂断操作
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void setNextHUserCall(boolean isSwitchView,boolean isPreHangUp){
        PrintLog.i( "   callBusinessMap.size()=" + callBusinessMap.size());
        if (isPreHangUp) {//如果是未接听的挂断,只切回到上一个hUserCall
            //LinkedHashMap遍历先进先出，获取的最后一个即为上一个
            Set<Map.Entry<Integer, EventBean>> entries = callBusinessMap.entrySet();
            for (Map.Entry<Integer, EventBean> map : entries) {
                EventBean value = map.getValue();
                if(value != null){
                    currentHUserCall = value.gethUserCall();
                }
            }
        }else {//
            if (videoSCallHandle != 0) {//如果有视频呼叫,则主界面不变
                currentHUserCall = videoSCallHandle;
                if(callSuspendMap.get(currentHUserCall) != null){//如果查到的视频呼叫是之前被挂起的，则主动激活
                    callSuspendMap.remove(currentHUserCall);
                    UctClientApi.UctActiveCall(currentHUserCall);
                    tv_videoCallBusy.setVisibility(View.GONE);
                }
            } else {
                /**
                 * 设置当前的currentHUserCall
                 */
                //1.查找挂断后是否还有其他视频业务
                Set<Map.Entry<Integer, EventBean>> entries = callBusinessMap.entrySet();
                boolean isFind = false;
                for (Map.Entry<Integer, EventBean> map : entries) {
                    EventBean value = map.getValue();
                    if (!isFind) {
                        if (value.getBusinessTag() == ConstantUtils.VIDEO_SCALL) {
                            currentHUserCall = value.gethUserCall();
                            if(callSuspendMap.get(currentHUserCall) != null){//如果查到的视频呼叫是之前被挂起的，则主动激活
                                callSuspendMap.remove(currentHUserCall);
                                UctClientApi.UctActiveCall(currentHUserCall);
                                tv_videoCallBusy.setVisibility(View.GONE);
                            }
                            isFind = true;
                        } else if (value.getBusinessTag() == ConstantUtils.UPLOAD_VIDEO || value.getBusinessTag() == ConstantUtils.DOWNLOAD_VIDEO) {
                            currentHUserCall = value.gethUserCall();
                            isFind = true;
                        }
                    }
                }
                if (!isFind) {//没找到视频，则为语音(最后一个即已经finish())
                    for (Map.Entry<Integer, EventBean> map : entries) {
                        currentHUserCall = map.getValue().gethUserCall();
                    }
                    //切换界面到语音呼叫界面
                    ScreenUtils.setScreenOrientation(this, Configuration.ORIENTATION_PORTRAIT);
                    view_videoRequest.setVisibility(View.VISIBLE);
                    view_videoAnswering.setVisibility(View.GONE);
                    if(callSuspendMap.size() > 0){
                       UctClientApi.UctActiveCall(currentHUserCall);//主动激活
                        callSuspendMap.remove(currentHUserCall);
                        callPresenter.currentAudioHUerCall = currentHUserCall;
                        tv_businessTypeRequest.setText(getString(R.string.string_audio_call_answering));
                    }
                } else {
                    //切换界面到视频界面
                    view_videoRequest.setVisibility(View.GONE);
                    view_videoAnswering.setVisibility(View.VISIBLE);
                }
                PrintLog.i("刷新界面 currentHUserCall=" + currentHUserCall);
            }
            onAnswer(currentHUserCall, isSwitchView);
        }

    }

    /**
     * 视频接听时view的显示控制
     * 当有多个视频时，左侧列表需显示
     * 一个视频时，左侧列表不显示，只控制当前全屏显示视频
     * @param businessTag 业务tag
     * @param eventBean 业务事件数据bean
     *
     * 原则:
     * 1、多个视频上传:可切换，主要用于挂掉，考虑到手电筒和切换摄像头等附加功能(不能将这些附加功能去掉),同时左侧列表支持删除
     *
     * 2、一个视频下载，多个视频上传：同时左侧列表支持删除
     *    1.一个纯视频下载，多个上传：默认主界面显示对方画面,上传放于左侧(除非点击左侧列表进行切换).能切换，主界面本地与远程切换
     *    2.一个视频呼叫，多个上传：主界面只能显示视频呼叫界面,左侧列表不能切换
     *
     * 接听，当前业务是视频相关。surfaceView 是否已存在?  y切换隐藏显示  n新建
     * 纯视频(上传A,下载A") +  视频呼叫B
     * A:     AA+:
     * B:   BA:   BAA:   AB:    AAB:
     */
    private SurfaceView localSurface,remoteSurface;
    private ViewGroup localParent,remoteParent;
    public void setStateOnAnswerOfVideo(EventBean eventBean, boolean isSwitchView){

        PrintLog.i("setStateOnAnswerOfVideo() businessTag="+eventBean.getBusinessTag()+"  videoSCallHandle="+videoSCallHandle+"  audioSCallHandle="+audioSCallHandle+"  videoUploadList.size()="+videoUploadList.size()+"  callBusinessMap.size()="+ callBusinessMap.size()+"  currentHUserCall="+eventBean.gethUserCall());
        ScreenUtils.setScreenOrientation(this,Configuration.ORIENTATION_LANDSCAPE);
        callPresenter.currentPureVideoHUserCall = eventBean.gethUserCall();
        view_videoRequest.setVisibility(View.GONE);
        view_videoAnswering.setVisibility(View.VISIBLE);
        callPresenter.initTimer(new TextView[]{tv_speakTime},eventBean.gethUserCall(),WHAT_TIME_KEEP_VIDEO);
        if(!isSwitchView && eventBean.getBusinessTag() != VIDEO_SCALL){//不是切换操作，且不为视频呼叫
            videoUploadList.add(eventBean);
        }
        localSurface = CallCallBack.getInstance().getLocalSurface();
        remoteSurface = CallCallBack.getInstance().getRemoteSurface();
        localParent = (ViewGroup) localSurface.getParent();
        remoteParent = (ViewGroup) remoteSurface.getParent();

        switch (eventBean.getBusinessTag()){
            case VIDEO_SCALL://视频呼叫
                iv_videoTypeAnswering.setImageResource(R.mipmap.ic_video_play);
                if(!TextUtils.isEmpty(eventBean.getPcName())){
                    tv_answeringUserName.setText(eventBean.getPcName());
                }else {
                    tv_answeringUserName.setText(eventBean.getPcCalling());
                }
                if (localParent != null) {
                    localParent.removeAllViews();
                }
                if (remoteParent != null) {
                    remoteParent.removeAllViews();
                }
                fl_locate.removeAllViews();
                fl_locate.addView(localSurface);
                fl_remote.removeAllViews();
                fl_remote.addView(remoteSurface);
                initVoiceHandsFree(VOICE_NO_PLUGIN_HANDS_FREE);
                view_camera.setVisibility(View.VISIBLE);
                view_handsFree.setVisibility(View.VISIBLE);
                //权限
                //如果audioSCallHandle不为0则表示当前有语音呼叫存在
                if (audioSCallHandle != 0 ) {
                    EventBean eventBean1 = callBusinessMap.get(audioSCallHandle);
                    if(eventBean1 != null && eventBean1.isAnswered()){
                        view_audioCall.setVisibility(View.VISIBLE);
                        tv_SCallUserName.setText(eventBean1.getPcName());
                        if(eventBean1.getBusinessTag() == MEETING_CALL){//有会议
                            view_meeting.setVisibility(View.VISIBLE);
                        }else{
                            view_meeting.setVisibility(View.INVISIBLE);
                        }
                    }
                }
                break;
            case UPLOAD_VIDEO://视频上传
                // TODO: 2017/10/13 视频下载还未判断
                if(CallCallBack.getInstance().videoDownloadNum <= 0 || (videoSCallHandle != 0 && !callBusinessMap.get(videoSCallHandle).isAnswered())){//没有视频下载 || 有下载但还未接听

                    if (localParent != null) {
                        localParent.removeAllViews();
                    }
                    if (remoteParent != null) {
                        remoteParent.removeAllViews();
                    }
                    fl_remote.removeAllViews();
                    fl_remote.addView(localSurface);
                    if(!TextUtils.isEmpty(eventBean.getPcName())){
                        tv_answeringUserName.setText(eventBean.getPcName());
                    }else {
                        tv_answeringUserName.setText(eventBean.getPcCalling());
                    }
                    iv_videoTypeAnswering.setImageResource(R.mipmap.ic_video_upload);
                    //如果audioSCallHandle不为0则表示当前有语音呼叫存在
                    if (audioSCallHandle != 0 ) {
                        EventBean eventBean1 = callBusinessMap.get(audioSCallHandle);
                        if(eventBean1 != null && eventBean1.isAnswered()){
                            view_audioCall.setVisibility(View.VISIBLE);
                            tv_SCallUserName.setText(eventBean1.getPcName());
                            if(eventBean1.getBusinessTag() == MEETING_CALL){//有会议
                                view_meeting.setVisibility(View.VISIBLE);
                            }else{
                                view_meeting.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                    initFlashlight();
                    view_camera.setVisibility(View.VISIBLE);
                    view_handsFree.setVisibility(View.VISIBLE);
                }
                else {//有已接听的视频下载,主界面不切换，只左侧显示上传列表
                    //1.查找挂断后是否还有其他视频业务
                    Set<Map.Entry<Integer, EventBean>> entries = callBusinessMap.entrySet();
                    for (Map.Entry<Integer, EventBean> map : entries) {
                        EventBean value = map.getValue();
                        if (value.getBusinessTag() == ConstantUtils.DOWNLOAD_VIDEO || value.getBusinessTag() == ConstantUtils.VIDEO_SCALL && value.isAnswered()) {
                            currentHUserCall = value.gethUserCall();
                            callPresenter.currentPureVideoHUserCall = currentHUserCall;
                        }
                    }
                    if(videoSCallHandle == 0 && isSwitchView){//没有视频呼叫
                        if(!TextUtils.isEmpty(eventBean.getPcName())){
                            tv_answeringUserName.setText(eventBean.getPcName());
                        }else {
                            tv_answeringUserName.setText(eventBean.getPcCalling());
                        }
                        iv_videoTypeAnswering.setImageResource(R.mipmap.ic_video_upload);
                    }
                }

                break;
            case DOWNLOAD_VIDEO://下载视频
                iv_videoTypeAnswering.setImageResource(R.mipmap.ic_video_download);
                if(!TextUtils.isEmpty(eventBean.getPcName())){
                    tv_answeringUserName.setText(eventBean.getPcName());
                }else {
                    tv_answeringUserName.setText(eventBean.getPcCalling());
                }
                if (localParent != null) {
                    localParent.removeAllViews();
                }
                if (remoteParent != null) {
                    remoteParent.removeAllViews();
                }
                fl_remote.removeAllViews();
                fl_remote.addView(remoteSurface);
                //如果audioSCallHandle不为0则表示当前有语音呼叫存在(存在,若还未接听呢?)
                if (audioSCallHandle != 0 ) {
                    EventBean eventBean1 = callBusinessMap.get(audioSCallHandle);
                    if(eventBean1!= null && callBusinessMap.get(audioSCallHandle).isAnswered()) {
                        view_audioCall.setVisibility(View.VISIBLE);
                        tv_SCallUserName.setText(eventBean1.getPcName());
                        if(eventBean1.getBusinessTag() == MEETING_CALL){//有会议
                            view_meeting.setVisibility(View.VISIBLE);
                        }else{
                            view_meeting.setVisibility(View.INVISIBLE);
                        }
                    }
                }
                view_camera.setVisibility(View.GONE);
                view_handsFree.setVisibility(View.GONE);
                break;
        }

        ll_videoUploadList.removeAllViews();
        PrintLog.i("currentHUserCall videoUploadList.size()=" + videoUploadList.size() + "   eventBean.getPcName()=" + eventBean.getPcName());
        /**
         * 一个视频上传界面时全屏显示(左侧列表隐藏)，两个及以上则全屏显示一个，同时左侧列表显示。如果有视频呼叫则左侧列表显示
         */
        //if(videoUploadList.size() > 1 || videoSCallHandle != 0){
        EventBean videoSCallBean = callBusinessMap.get(videoSCallHandle);
        /*start 纯视频呼叫*/
        if(videoUploadList.size() > 1 || (videoUploadList.size() == 1 && videoSCallHandle != 0 && (videoSCallBean != null) && videoSCallBean.isAnswered())){

            for (int i = 0; i < videoUploadList.size(); i++) {
                final EventBean bean = videoUploadList.get(i);
                View view = View.inflate(this, R.layout.item_upload_video, null);
                ((TextView) view.findViewById(R.id.tv_userName)).setText(bean.getPcName());
                ImageView iv_videoState_item = (ImageView) view.findViewById(R.id.iv_videoState_item);
                if(bean.getBusinessTag() == UPLOAD_VIDEO){
                    iv_videoState_item.setImageResource(R.drawable.animation_list_upload_item);
                }else if(bean.getBusinessTag() == DOWNLOAD_VIDEO){
                    iv_videoState_item.setImageResource(R.drawable.animation_list_download_item);
                }
                AnimationDrawable animation = (AnimationDrawable) iv_videoState_item.getDrawable();
                ((AnimationDrawable) ((ImageView) view.findViewById(R.id.iv_videoWave_item)).getDrawable()).start();
                animation.start();

                if(layoutParams == null){
                    layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                }
                layoutParams.weight = 1;
                view.setLayoutParams(layoutParams);
                //点击切换主界面
                view.setTag(bean);
                //点击进行切换
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //切换界面
                        switchPureVideoView((EventBean) v.getTag());
                    }
                });
                //长按是否挂断
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final EventBean eb = (EventBean) v.getTag();
                        //是否挂断
                        AlertDialog.Builder dialog = new AlertDialog.Builder(VideoCallActivity.this);
                        dialog.setMessage(mContext.getString(R.string.string_hand_up_session))
                                .setPositiveButton(mContext.getString(R.string.string_hang_up), new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                doHangUp(eb.gethUserCall(),true,false);
                                dialog.dismiss();
                            }
                        }).setNegativeButton(mContext.getString(R.string.string_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                        return true;
                    }
                });
                ll_videoUploadList.addView(view);
            }
        }

    }
    //左侧列表
    LinearLayout.LayoutParams layoutParams;
    /**
     * 视频上传是下载画面切换
     * @param eventBean
     */
    private void switchPureVideoView(EventBean eventBean){
        //如果当前界面有视频呼叫，就不能切换；
        if(videoSCallHandle != 0){
            return;
        }
        int businessTag = eventBean.getBusinessTag();
        if(!TextUtils.isEmpty(eventBean.getPcName())){
            tv_answeringUserName.setText(eventBean.getPcName());
        }else {
            tv_answeringUserName.setText(eventBean.getPcCalling());
        }
        currentHUserCall = eventBean.gethUserCall();
        callPresenter.currentPureVideoHUserCall = currentHUserCall;
        switch (businessTag){
            case UPLOAD_VIDEO://视频上传
                //当AAB时即纯视频数量 >= 2，且刚挂掉视频呼叫时，视频呼叫ui需切换到纯视频ui
                if (localParent != null) {
                    localParent.removeAllViews();
                }
                if (remoteParent != null) {
                    remoteParent.removeAllViews();
                }
                fl_remote.removeAllViews();
                fl_remote.addView(localSurface);
                view_camera.setVisibility(View.VISIBLE);
                view_handsFree.setVisibility(View.VISIBLE);
                iv_videoTypeAnswering.setImageResource(R.mipmap.ic_video_upload);
                initFlashlight();
                break;
            case DOWNLOAD_VIDEO://视频下载
                if (localParent != null) {
                    localParent.removeAllViews();
                }
                if (remoteParent != null) {
                    remoteParent.removeAllViews();
                }
                fl_remote.removeAllViews();
                fl_remote.addView(remoteSurface);

                iv_videoTypeAnswering.setImageResource(R.mipmap.ic_video_download);
                view_camera.setVisibility(View.GONE);
                view_handsFree.setVisibility(View.GONE);
                break;
        }
    }

    private void initFlashlight(){
         /*ptyt start_解决切换后，闪光灯已打开的情况下，显示还原问题*/
        tv_voice_video.setText(getString(R.string.string_flashlight));
        int flashlightMode = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_CAMERA_SET_FLASHLIGHT, 0);
        if(flashlightMode == 0){
            iv_voice_video.setImageResource(R.mipmap.btn_flashlight_nor);
        }else{
            iv_voice_video.setImageResource(R.mipmap.btn_flashlight_on_pre);
        }
        /*ptyt end*/
    }

    private void initVoiceHandsFree(int type){
        boolean checkControlIsConnected = SoundManager.checkControlIsConnected(mContext);
        tv_voice_video.setText(getString(R.string.string_handsFree));
        switch (type) {
            case VOICE_INIT://根据当前声音免提还是听筒进行view显示
                //被叫时免提打开(耳机未介入情况下)
                if (!checkControlIsConnected) {
                    mAudioManager.setSpeakerphoneOn(true);
                    iv_voice_video.setImageResource(R.mipmap.btn_handsfree_pre);
                } else {
                    mAudioManager.setSpeakerphoneOn(false);
                    iv_voice_video.setImageResource(R.mipmap.btn_handsfree_nor);
                }
                break;
            case VOICE_NO_PLUGIN_HANDS_FREE://没插入耳机时默认放音
                //被叫时免提打开(耳机未介入情况下)
                PrintLog.i("checkControlIsConnected=" + checkControlIsConnected);
                if (!checkControlIsConnected) {
                    mAudioManager.setSpeakerphoneOn(true);
                    iv_voice_video.setImageResource(R.mipmap.btn_handsfree_pre);
                } else {
                    mAudioManager.setSpeakerphoneOn(false);
                    iv_voice_video.setImageResource(R.mipmap.btn_handsfree_nor);
                }
                break;
            case VOICE_SWITCH://切换听筒与免提
                if (checkControlIsConnected) {//耳机插进时，不能放音
                    ToastUtils.getToast().showMessageShort(mContext, getString(R.string.headset_cannot_handsfree), -1);
                    return;
                }
                boolean isSpeakerphoneOn = mAudioManager.isSpeakerphoneOn();
                if (!isSpeakerphoneOn) {
                    mAudioManager.setSpeakerphoneOn(true);
                    iv_handsFreeRequest.setImageResource(R.mipmap.btn_handsfree_pre);
                    iv_voice_video.setImageResource(R.mipmap.btn_handsfree_pre);
                } else {//听筒
                    mAudioManager.setSpeakerphoneOn(false);
                    iv_handsFreeRequest.setImageResource(R.mipmap.btn_handsfree_nor);
                    iv_voice_video.setImageResource(R.mipmap.btn_handsfree_nor);
                }
                break;
        }
    }

    /**
     * 组呼 触摸
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //发起组呼
                if(gCallCallback != null && gCallCallback.currentGroupId != null){
                    gCallCallback.startGroupCall(gCallCallback.currentGroupId);
                }
                break;
            case MotionEvent.ACTION_UP:
                //释放话权
                if(gCallCallback != null){
                    gCallCallback.releaseGCallReq();
                }
                break;
        }
        return true;
    }

    /**
     * 设置通话成功时的界面状态
     * @param iUserHandle 要显示的业务句柄
     * @param isSwitchView 是否是挂断时的切换显示
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void onAnswer(int iUserHandle,boolean isSwitchView) {
        currentHUserCall = iUserHandle;

        PrintLog.i("通话成功时currentHUserCall="+iUserHandle);
        AppUtils.stopPlayMedia();
        //音道设置
        CallCallBack.getInstance().setVoiceCallMode(AudioManager.STREAM_VOICE_CALL);
        //如果第一个事件未监听，且后面这个事件没有挂断
        if(!firstEventBean.isAnswered() && callBusinessMap.get(firstEventBean.gethUserCall()) != null){
            if(!unAnsweredBusinessList.contains(firstEventBean)){
                unAnsweredBusinessList.add(firstEventBean);
                dialogPagerAdapter.notifyDataSetChanged();
                setVideoInDialogShow();
            }
        }
        EventBean eventBean = callBusinessMap.get(iUserHandle);
        if(eventBean == null){
            return;
        }
        boolean remove = unAnsweredBusinessList.remove(eventBean);
        if(remove){
            dialogPagerAdapter.notifyDataSetChanged();
            if (unAnsweredBusinessList.size() == 0) {
                videoInDialog.hide();
            } else {
                setVideoInDialogShow();
            }
        }
        /*ptyt begin KeChuanqi_20171009*/

        if(!isSwitchView){
            eventBean.setAnswered(true);
//            callBusinessMap.put(eventBean.gethUserCall(),eventBean);
        }
        if(!eventBean.isAnswered()){//如果未接通则只切回currentHUserCall
            /*ptyt start,来了语音呼叫和视频上传都未接听，对方挂断语音呼叫，显示*/
            //如果只剩一个业务
            if(callBusinessMap.size() == 1){
                ScreenUtils.setScreenOrientation(VideoCallActivity.this,Configuration.ORIENTATION_PORTRAIT);
            }
            tv_requestUserName.setText(eventBean.getPcName());
            initFirstEnterBusinessView(1,eventBean.getBusinessTag(),eventBean.gethUserCall());
            /*ptyt end*/
            return;
        }
        /*ptyt end*/
        int businessTag = eventBean.getBusinessTag();
        PrintLog.i("通话成功时currentHUserCall="+iUserHandle +"   是否是挂断时切换界面isSwitch="+isSwitchView+"  businessTag="+businessTag +"  callBusinessMap.size()="+ callBusinessMap.size()+"  unAnsweredBusinessList="+unAnsweredBusinessList.size());
        tv_cancelRequest.setText(mContext.getString(R.string.string_hang_up));
        switch (businessTag){
            case MEETING_CALL:
            case AUDIO_SCALL://语音单呼
                //扬声器打开
                if(HeadsetPlugBroadcastReceiver.isHeadsetPlugIn){
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_AUDIO_ROUTE, 1);
                    iv_handsFreeRequest.setImageResource(R.mipmap.btn_handsfree_nor);
                }else {
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_AUDIO_ROUTE, 0);
                    iv_handsFreeRequest.setImageResource(R.mipmap.btn_handsfree_pre);
                }
                callPresenter.currentAudioHUerCall = iUserHandle;
                /**
                 * 如果当前没其他视频业务，则显示该语音通话界面
                 * 情景分析：
                 *   a:视频上传时 -->  来了语音单呼请求 -> 接听 -> 隐藏单呼全屏的view，以缩略框形式显示
                 *   b:没其他视频业务，来了语音单呼请求 -> 全屏显示
                 */
                audioSCallHandle = iUserHandle;
                if(callBusinessMap.size() > 1){
                    if(callBusinessMap.size() > callSuspendMap.size()+1){//有其他业务且有强插

                        boolean isHaveSomeAnswer = false;
                        Set<Integer> keys = callBusinessMap.keySet();
                        for (Integer key : keys) {
                            EventBean eb = callBusinessMap.get(key);
                            int call = eb.gethUserCall();
                            if(eb.isAnswered() && call != audioSCallHandle){
                                isHaveSomeAnswer = true;//有已接听
                            }
                            if (!isSwitchView) {
                                if (call != audioSCallHandle) {
                                    currentHUserCall = call;
                                }
                            }
                        }
                        if(isHaveSomeAnswer){
                            ScreenUtils.setScreenOrientation(VideoCallActivity.this,Configuration.ORIENTATION_LANDSCAPE);
                            view_videoRequest.setVisibility(View.GONE);
                            view_videoAnswering.setVisibility(View.VISIBLE);
                            view_audioCall.setVisibility(View.VISIBLE);
                            if(businessTag == MEETING_CALL){//有会议
                                view_meeting.setVisibility(View.VISIBLE);
                            }else{
                                view_meeting.setVisibility(View.INVISIBLE);
                            }
                            tv_SCallUserName.setText(eventBean.getPcName());
                        }
                    }else{
                        currentHUserCall = iUserHandle;
                        PrintLog.i("callBusinessMap.size() == callSuspendMap.size()+1)="+(callBusinessMap.size() == callSuspendMap.size()+1) +"  CallCallBack.getInstance().videoDownloadNum="+CallCallBack.getInstance().videoDownloadNum);
                        if((callBusinessMap.size() == callSuspendMap.size()+1) && CallCallBack.getInstance().videoDownloadNum > 0){//视频呼叫被插入
                            ScreenUtils.setScreenOrientation(VideoCallActivity.this,Configuration.ORIENTATION_PORTRAIT);
                            view_videoAnswering.setVisibility(View.GONE);
                            view_videoRequest.setVisibility(View.VISIBLE);
                            if(callBusinessMap.size() > 1){
                                iv_audioSCallShrink.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
                else{
                    view_videoAnswering.setVisibility(View.GONE);
                    view_videoRequest.setVisibility(View.VISIBLE);
                }
                //size > 1 一定还有其他视频业务 将视频业务放于当前 单呼以缩略框形式展示
                tv_businessTypeRequest.setText(getString(R.string.string_audio_call_answering));
                tv_requestUserName.setText(eventBean.getPcName());
                ll_voiceSilenceRequest.setVisibility(View.VISIBLE);
                ll_handsFreeRequest.setVisibility(View.VISIBLE);
                ll_answerRequest.setVisibility(View.GONE);
                callPresenter.initTimer(new TextView[]{tv_audioSpeakTimeRequest,tv_SCallTimeShrink},audioSCallHandle,WHAT_TIME_KEEP_AUDIO);
                break;
            case VIDEO_SCALL://视频呼叫
                setStateOnAnswerOfVideo(eventBean,isSwitchView);
                //扬声器打开
                if(HeadsetPlugBroadcastReceiver.isHeadsetPlugIn){
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_AUDIO_ROUTE, 1);
                    iv_handsFreeRequest.setImageResource(R.mipmap.btn_handsfree_nor);
                }else {
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_AUDIO_ROUTE, 0);
                    iv_handsFreeRequest.setImageResource(R.mipmap.btn_handsfree_pre);
                }
                break;
            case UPLOAD_VIDEO://视频上传
                setStateOnAnswerOfVideo(eventBean,isSwitchView);
                break;
            case DOWNLOAD_VIDEO://视频下载
                setStateOnAnswerOfVideo(eventBean,isSwitchView);
                break;
        }
    }

    /**
     * 配置信息改变监听
     * @param sharedPreferences
     * @param key
     * @param value
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key, Object value) {
        PrintLog.i("onSharedPreferenceChanged() key="+key +"  value="+value);
        switch (key){
            case SettingsConstant.SETTINGS_VIDEO_CAMERA:
//                if(((Integer) value).intValue() == 0){//后置摄像头
//                }else {
//                }
                break;
        }
    }

    /**
     *  话筒授予或回收指示 UCT_SpeakerReq的回调函数
     *输入:
     *  hUserCall:     呼叫句柄
     *  type:          0: 收回话筒  1:授予话筒
     *
     *返回:
     *  0:              成功
     *  -1:             失败
     *调用时刻:
     *  会议中，会议主席将话筒授予本用户或者收回话筒时，uct.dll回调此接口通知界面
     **/

    @Override
    public int UCT_SpeakerInd(int hUserCall, int type) {

        if(callBusinessMap.get(hUserCall).getBusinessTag() == MEETING_CALL){//会议
            if(type == 0){//收回话筒

            }else if(type == 1){//授予话筒

            }
        }
        return 0;
    }

    /**
     * 强插时呼叫业务集合
     */
    private HashMap<Integer,EventBean> callSuspendMap = new HashMap<>();

    /**
     * 对端激活成功的通知
     * @param hUserCall:被插入时的hUserCall
     * @return
     */
    @Override
    public int UCT_CallActive(int hUserCall) {
        PrintLog.i("hUserCall="+hUserCall);
        EventBean eventBean = callBusinessMap.get(hUserCall);
        if(eventBean == null){
            return 0;
        }
        if(eventBean.getBusinessTag() == VIDEO_SCALL){
            tv_videoCallBusy.setVisibility(View.GONE);
        }else {
            tv_businessTypeRequest.setText(getString(R.string.string_audio_call_answering));
        }
        return 0;
    }

    /**
     * 挂起 只有视频呼叫或语音呼叫被占线
     * @param hUserCall
     * @param i1
     * @return
     */
    @Override
    public int UCT_CallSuspend(int hUserCall, int i1) {
        PrintLog.i("hUserCall="+hUserCall+"  i1="+i1);
        if(callBusinessMap.get(hUserCall).getBusinessTag() == VIDEO_SCALL){
            tv_videoCallBusy.setVisibility(View.VISIBLE);
        }else {
            tv_businessTypeRequest.setText(getString(R.string.string_audio_calling_hanging));
        }
        callSuspendMap.put(hUserCall,callBusinessMap.get(hUserCall));
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void pagerDialogHangup(View v) {
        EventBean eb = unAnsweredBusinessList.get(((Integer) v.getTag()).intValue());
        PrintLog.i("弹框里点击挂断  currentHUserCall="+ eb.gethUserCall());
        //为清除视频来时的对话框
        if(unAnsweredBusinessList.remove(eb)){
            dialogPagerAdapter.notifyDataSetChanged();
            if(unAnsweredBusinessList.size() == 0){
                videoInDialog.hide();
            }else {
                setVideoInDialogShow();
            }
        }
        doHangUp(eb.gethUserCall(),true,true);
    }

    public void setVideoInDialogShow(){
        videoInDialog.show();
        videoInDialog.getWindow().setLayout(dialogDimensionX, dialogDimensionY);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void pagerDialogAnswer(View v) {
        int pos = ((Integer) v.getTag()).intValue();
        int userCall = unAnsweredBusinessList.get(pos).gethUserCall();
        boolean remove = unAnsweredBusinessList.remove(unAnsweredBusinessList.get(pos));
        PrintLog.i("弹框里点击接听  currentHUserCall="+userCall + "  pos="+pos +"   unAnsweredBusinessList.size()="+ unAnsweredBusinessList.size());
        if(remove){
            dialogPagerAdapter.notifyDataSetChanged();
        }
        if (unAnsweredBusinessList.size() == 0) {
            videoInDialog.hide();
        }
        UctClientApi.uctSCallMtResponse(userCall);
        onAnswer(userCall,false);
    }

    @Override
    protected void onPause() {
        PrintLog.i("onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        PrintLog.i("onStop()");
        super.onStop();
        ScreenManager.getInstance().clear(this);
    }

    @Override
    public void releaseAll(int i, boolean b, String s) {
        PrintLog.i("releaseAll() i="+i+"  b="+b +"  s="+s);
        finish();
    }

    private float startY,startX;
    private boolean isMute;
    private int mVol;
    private int mMaxVolume;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PrintLog.e(""+event.getAction());
        return gestureDetector.onTouchEvent(event);
//        switch (event.getAction()) {//声音及亮度调节
//            case MotionEvent.ACTION_DOWN:
//                //1.按下记录值
//                startY = event.getY();
//                startX = event.getX();
//                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
////                touchRang = Math.min(screenHeight, screenWidth);//screenHeight
////                handler.removeMessages(HIDE_MEDIACONTROLLER);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                //2.移动的记录相关值
//
//                if (endX < ScreenUtils.getScreenHeight(mContext) / 2) {
//                    //左边屏幕-调节亮度
//                    final double FLING_MIN_DISTANCE = 0.5;
//                float endY = event.getY();
//                float endX = event.getX();
//                float distanceY = startY - endY;
//                    final double FLING_MIN_VELOCITY = 0.5;
//                    if (distanceY > FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
//                        setBrightness(10);
//                    }
//                    if (distanceY < FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
//                        setBrightness(-10);
//                    }
//                } else {
//                    //右边屏幕-调节声音
//                    //改变声音 = （滑动屏幕的距离： 总距离）*音量最大值
//                    float delta = (distanceY / ScreenUtils.getScreenWidth(mContext)) * mMaxVolume;
//                    //最终声音 = 原来的 + 改变声音；
//                    int voice = (int) Math.min(Math.max(mVol + delta, 0), mMaxVolume);
//                    if (delta != 0) {
//                        isMute = false;
//                        updateVoice(voice, isMute);
//                    }
//                }
//                break;
//            case MotionEvent.ACTION_UP:
////                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
//                break;
//        }
//        return super.onTouchEvent(event);
    }
    /**
     * 设置音量的大小
     *
     * @param progress
     */
    private void updateVoice(int progress, boolean isMute) {
        if (isMute) {
            CallCallBack.getInstance().audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
//            seekbarVoice.setProgress(0);
        } else {
            CallCallBack.getInstance().audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
//            seekbarVoice.setProgress(progress);
//            currentVoice = progress;
        }
    }

    /*
 *
 * 设置屏幕亮度 lp = 0 全暗 ，lp= -1,根据系统设置， lp = 1; 最亮
 */
    public void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();

        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
        } else if (lp.screenBrightness < 0.1) {
            lp.screenBrightness = (float) 0.1;
        }
        getWindow().setAttributes(lp);
    }
}
