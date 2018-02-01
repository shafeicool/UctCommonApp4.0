package com.ptyt.uct.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.MainActivity;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.SettingsConstant;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.entity.Group;
import com.ptyt.uct.model.ConversationDBManager;
import com.ptyt.uct.model.GroupDBManager;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.DateUtils;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.NetUtils;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.utils.ScreenUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;

import java.util.Timer;
import java.util.TimerTask;

import static com.ptyt.uct.common.SettingsConstant.SETTINGS_LOCK_GROUP;
import static com.ptyt.uct.viewinterface.IGroupInfoView.GCALL_CONFIRM;
import static com.ptyt.uct.viewinterface.IGroupInfoView.GCALL_FAILED;
import static com.ptyt.uct.viewinterface.IGroupInfoView.GCALL_HANGUP;
import static com.ptyt.uct.viewinterface.IGroupInfoView.GCALL_RELEASE;
import static com.ptyt.uct.viewinterface.IGroupInfoView.GCALL_SUCCEED;
import static com.ptyt.uct.viewinterface.IGroupInfoView.NOBODY_SPEAK;
import static com.ptyt.uct.viewinterface.IGroupInfoView.OTHER_LAUCHER_GCALL;
import static com.ptyt.uct.viewinterface.IGroupInfoView.OTHER_SPEAK;
import static com.ptyt.uct.viewinterface.IGroupInfoView.SELF_SPEAK;
import static com.ptyt.uct.viewinterface.IGroupInfoView.USER_OFFLINE;

/**
 * @Description: 组呼框,带动画
 * @Date: 2017/5/22
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class PTYTCallWindowView extends FrameLayout implements View.OnClickListener, View.OnTouchListener, View.OnLongClickListener {
    
    private Context mContext = null;
    private View view_callNormal;
    private View view_drag;
    private ImageView iv_groupCall,iv_hangup,iv_record;
    private MarqueeTextView tv_groupName;
    private TextView tv_groupCallState;
    private View view_callShrink;
    private TextView tv_groupNameShrink;
    private View view_space1,view_space2;
    private View rl_fullscreen_avatar;
    private View rl_fullscreen_title;
    private View rl_title;
    private View view_shadow;
    private ObjectAnimator anim_bigWindowHide;
    private ValueAnimator anim_space1Extent,anim_space1Shrink;
    private ValueAnimator anim_space2Extent,anim_space2Shrink;
    private ValueAnimator anim_avatarExtent,anim_avatarShrink;
    private ObjectAnimator anim_titleHide,anim_titleShow;
    //组呼框是否在全屏状态
    private boolean isFullScreen = false;
    //屏幕高度
    private int screenHeight;
    //状态栏高度
    private int statusHeight;
    private int windowShrinkDistance;
    private AnimationDrawable drawable_soundWaveBig;
    private ImageView iv_groupCallShrink;
    private AnimationDrawable drawable_soundWaveShrink;
    private ImageView iv_fullScreen,iv_soundWave,iv_soundWaveShrink;
    private PopupWindow ppw_itemFuc;
    private View rl_parent;
    //展开
    private ImageView iv_unfoldFullScreen;
    private ImageView iv_unfold;
    //当其他人说话时，控制显示的view
    private AnimationDrawable drawable_otherSpeak;
    private View view_otherSpeak,view_selfSpeak;
    private TextView tv_otherSpeak;
    //说话计时
    private TextView tv_speakTime;
    private long speakStartTime;
    private TextView tv_press2speak;
    private MarqueeTextView tv_groupNameFullScreen;
    private TextView tv_groupCallStateFullscreen;
    //开始录音时间
    private long startRecordTime;
    //录音是否成功标志
    private boolean isSuccessRecord;
    //会话id
    private Long conversationId;
    //本地录音路径
    String recordFileName;
    private String smsgId;
    //组呼录音开关
    boolean isSupportGCallRecord = true;
    private WaveView waveView;
    private int gCallState;
    private BitmapDrawable popBitmapDrawable;
    private ImageView iv_lockItem_headView;

    public PTYTCallWindowView(@NonNull Context context) {
        this(context,null,-1);
    }

    public PTYTCallWindowView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public PTYTCallWindowView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        onCreateView();
    }

    private void onCreateView() {
        LayoutInflater inflater= LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.view_call_window, null);
        removeAllViews();
        LayoutParams mLayoutParams  = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(view, mLayoutParams);

        initView(view);

        initAnimation();

    }
    //说话时间长度
    android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                long duration = System.currentTimeMillis() - speakStartTime;
                String minuteTime = DateUtils.getMinuteTime(duration);
                /*ptyt begin 解决挂断后时间还在显示的问题 _kechuanqi_20171212*/
                if(gCallState == GCALL_HANGUP || gCallState == USER_OFFLINE){
                    tv_speakTime.setText("");
                    return;
                }
                /*ptyt end*/
                tv_speakTime.setText(minuteTime);
            }
        }
    };
    /**
     * 初始化计时器,以对呼叫时间进行计时
     */
    private TimerTask timerTask;
    private Timer timer;
    private void initTimer() {
        timer = new Timer(true);
        if (timerTask != null) {
            timerTask.cancel();
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                mHandler.sendMessage(msg);
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }
    public void setTimerRelease(){
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }
    /**
     * 初始化呼叫框view
     * @param view
     */
    private void initView(View view) {
        rl_parent = view.findViewById(R.id.rl_parent);
        view_callNormal = view.findViewById(R.id.view_callWindow);
        view_drag = view.findViewById(R.id.view_drag);
        iv_groupCall = ((ImageView) view.findViewById(R.id.iv_group_call));
        iv_hangup = ((ImageView) view.findViewById(R.id.iv_hangup));
        iv_record = ((ImageView) view.findViewById(R.id.iv_record));
        tv_groupName = ((MarqueeTextView) view.findViewById(R.id.tv_groupName));
        tv_groupNameFullScreen = ((MarqueeTextView) view.findViewById(R.id.tv_groupName_fullscreen));
        tv_groupCallState = ((TextView) view.findViewById(R.id.tv_groupCallState));
        tv_groupCallStateFullscreen = ((TextView) view.findViewById(R.id.tv_groupCallState_fullscreen));
        iv_fullScreen = ((ImageView) view.findViewById(R.id.iv_fullScreen));
        iv_unfoldFullScreen = ((ImageView) view.findViewById(R.id.iv_unfold_fullscreen));
        iv_unfold = ((ImageView) view.findViewById(R.id.iv_unfold));
        iv_unfold.setOnClickListener(this);
        iv_unfoldFullScreen.setOnClickListener(this);
        view_callShrink = view.findViewById(R.id.view_call_shrink);
        iv_groupCallShrink = ((ImageView) view.findViewById(R.id.iv_groupCall_shrink));
        tv_groupNameShrink = ((TextView) view.findViewById(R.id.tv_groupName_shrink));
        iv_soundWaveShrink = (ImageView) view.findViewById(R.id.iv_soundWave_shrink);
        view_space1 = view.findViewById(R.id.view_space1);
        view_space2 = view.findViewById(R.id.view_space2);
        rl_fullscreen_avatar = view.findViewById(R.id.rl_avatar_fullscreen);
        rl_fullscreen_title = view.findViewById(R.id.rl_title_fullscreen);
        rl_title = view.findViewById(R.id.rl_title);
        view_shadow = view.findViewById(R.id.view_shadow);
        view_otherSpeak = findViewById(R.id.ll_otherSpeak);
        view_selfSpeak = findViewById(R.id.ll_selfSpeak);
        tv_otherSpeak = ((TextView) findViewById(R.id.tv_otherSpeak));
        iv_soundWave = (ImageView) findViewById(R.id.iv_soundWave);
        ImageView iv_otherSpeak = (ImageView) findViewById(R.id.iv_otherSpeak);
        tv_speakTime = ((TextView) findViewById(R.id.tv_speakTime));
        tv_press2speak = ((TextView) findViewById(R.id.tv_press2speak));
        drawable_soundWaveBig = ((AnimationDrawable) iv_soundWave.getDrawable());
        drawable_soundWaveShrink = ((AnimationDrawable) iv_soundWaveShrink.getDrawable());
        drawable_otherSpeak = ((AnimationDrawable) iv_otherSpeak.getDrawable());
        waveView = ((WaveView) view.findViewById(R.id.wave_view));
        iv_lockItem_headView = ((ImageView) view.findViewById(R.id.iv_lockItem_headView));
        view_callShrink.setOnClickListener(this);
        iv_groupCall.setOnTouchListener(this);
        iv_groupCall.setOnLongClickListener(this);
        iv_groupCallShrink.setOnTouchListener(this);
        iv_hangup.setOnClickListener(this);
        iv_record.setOnClickListener(this);
        iv_fullScreen.setOnClickListener(this);
        screenHeight = ScreenUtils.getScreenHeight(mContext);
        statusHeight = ScreenUtils.getStatusHeight(mContext);
        initPopupWindow();
        dimensionStartH = mContext.getResources().getDimensionPixelOffset(R.dimen.y100);
        dimensionEndH = mContext.getResources().getDimensionPixelOffset(R.dimen.y380);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_record://录音
                //组呼录音开关
                if(isSupportGCallRecord){
                    iv_record.setImageResource(R.mipmap.ic_audio_record_on);
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_GROUP_CALL_RECORD, 1);
                    isSupportGCallRecord = false;
                }else{
                    iv_record.setImageResource(R.mipmap.btn_audio_record_nor);
                    UctClientApi.saveUserData(SettingsConstant.SETTINGS_GROUP_CALL_RECORD, 0);
                    isSupportGCallRecord = true;
                }
                break;
            case R.id.iv_hangup://挂断
                onGroupCallListener.onHangup();
                break;
            case R.id.iv_fullScreen://全屏or取消全屏
                if(!isFullScreen){ //全屏
                    onWindowScreenChangeListener.onChanged(true);
                    dealWithWindowFullScreen();
                }else {//取消全屏
                    onWindowScreenChangeListener.onChanged(false);
                    dealWithWindowFullScreenCancel();
                }
                break;
            case R.id.view_call_shrink://点击底部组呼框
                if(triggerView != null){
                    viewExtent(triggerView,false);
                }
                break;
            case R.id.iv_unfold_fullscreen://组呼框内的展开
                if(onGroupFucUnfoldListener != null){
                    onGroupFucUnfoldListener.onClick(iv_unfoldFullScreen);
                    iv_unfoldFullScreen.setImageResource(R.mipmap.ic_pull_up);
                }
                break;
            case R.id.iv_unfold:
                if(onGroupFucUnfoldListener != null){
                    onGroupFucUnfoldListener.onClick(iv_unfold);
                    iv_unfold.setImageResource(R.mipmap.ic_pull_up);
                }
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void dealWithWindowFullScreenCancel() {
        iv_fullScreen.setImageResource(R.mipmap.btn_fullscreen_nor);
        anim_space1Shrink.start();
        anim_space2Shrink.start();
        anim_avatarShrink.start();
        anim_titleShow.start();
        isFullScreen = false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void dealWithWindowFullScreen() {
        view_shadow.setVisibility(GONE);
        iv_fullScreen.setImageResource(R.mipmap.btn_shrink_down_nor);
        anim_space2Extent.start();
        anim_space1Extent.start();
        anim_avatarExtent.start();
        anim_titleHide.start();
        isFullScreen = true;
    }

    /**
     * 组呼
     * @param v
     * @param event
     * @return
     */
    int dimensionStartH,dimensionEndH;
    public boolean isLongClick = false;//确保onLongClick()在ACTION_DOWN|ACTION_CANCEL之后响应无效
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //控制在中间区域按下才有效
        if(event.getY() < dimensionEndH && event.getY() > dimensionStartH && event.getX() < dimensionEndH && event.getX() > dimensionStartH){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isLongClick = true;
//                    AppUtils.startGCallPlayMedia(R.raw.sound_video1);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    PrintLog.i("releaseGCallReq()");
                    isLongClick = false;
                    //释放话权
                    if (onGroupCallListener != null) {
                        onGroupCallListener.releaseGCallReq();
                    }
                    if (drawable_soundWaveBig != null) {
                        drawable_soundWaveBig.stop();
                        drawable_soundWaveShrink.stop();
                    }
                    break;
            }
            return false;//return false ,否则imageview设置selector无效
        }else{//ACTION_CANCEL 滑出时相应
            if(event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP){
                isLongClick = false;
                //释放话权
                if (onGroupCallListener != null) {
                    onGroupCallListener.releaseGCallReq();
                }
                if (drawable_soundWaveBig != null) {
                    drawable_soundWaveBig.stop();
                    drawable_soundWaveShrink.stop();
                }
            }
        }
        return false;
    }

    /**
     * 按下发起组呼在长按事件监听，避免用户一触碰就会发起组呼的弊端，释放组呼在ACTION_UP | ACTION_CANCEL里
     * 执行顺序:
     *  长按: MotionEvent.ACTION_DOWN -> onLongClick() -> ACTION_UP | ACTION_CANCEL
     *  短按: MotionEvent.ACTION_DOWN -> ACTION_UP | ACTION_CANCEL
     * @param v
     * @return
     */
    @Override
    public boolean onLongClick(View v) {
        //发起组呼
        if (onGroupCallListener != null && currentGroupData != null && isLongClick) {
            PrintLog.i("组呼onLongClick onGroupCallListener.startGroupCall()");
            if(gCallState != OTHER_SPEAK){

            }
            if(!NetUtils.isNetworkAvailable(mContext)){
                ToastUtils.getToast().showMessageShort(mContext,mContext.getString(R.string.net_error),-1);
                return true;
            }
            onGroupCallListener.startGroupCall(currentGroupData.getGrouTel());
        }
        return true;
    }
    /**
     * 创建动画封装,可进行动态改变view的高度
     * @param v 作用的view
     * @param start 开始值
     * @param end
     * @return
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private ValueAnimator creatAnimator(final View v, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                Integer animatedValue = (Integer) arg0.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.height = animatedValue;
                //重新设置layout的参数
                v.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }
    /**
     * 初始化功能展开的popupWindow的view
     */
    private void initPopupWindow() {
        View popView = View.inflate(mContext, R.layout.dialog_group_fuc_option, null);
        popView.findViewById(R.id.iv_group_people_dialog).setOnClickListener(this);
        popView.findViewById(R.id.iv_location_dialog).setOnClickListener(this);
        popView.findViewById(R.id.iv_message_dialog).setOnClickListener(this);
        popView.findViewById(R.id.iv_lock_dialog).setOnClickListener(this);
        //popView即popupWindow的布局，ture设置focusAble.
        ppw_itemFuc = new PopupWindow(popView, RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT, true);
        //必须设置BackgroundDrawable后setOutsideTouchable(true)才会有效。这里在XML中定义背景，所以这里设置为null;
        popBitmapDrawable = new BitmapDrawable();
        ppw_itemFuc.setBackgroundDrawable(popBitmapDrawable);
        //点击外部关闭。
        ppw_itemFuc.setOutsideTouchable(true);
        //设置一个动画。
        ppw_itemFuc.setAnimationStyle(android.R.style.Animation_Dialog);
    }

    /**
     * 动画初始化
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initAnimation() {

        anim_bigWindowHide = ObjectAnimator.ofFloat(view_callNormal, "alpha", 1f, 0f);
        anim_bigWindowHide.setDuration(200);
        //组呼框全屏时动画
        view_callNormal.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(anim_space1Extent == null){
                    int height = view_callNormal.getHeight();
                    int moveDistance;
                    moveDistance = screenHeight - ((height - mContext.getResources().getDimensionPixelOffset(R.dimen.y12))) - mContext.getResources().getDimensionPixelOffset(R.dimen.y336);//146+190
                    /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){//4.4 全透明状态栏
                        moveDistance = screenHeight - height ;//- mContext.getResources().getDimensionPixelOffset(R.dimen.y168);
                    }else{
                        moveDistance = screenHeight - (height + statusHeight) - mContext.getResources().getDimensionPixelOffset(R.dimen.y168);
                    }*/
                    anim_space1Extent = creatAnimator(view_space1,0,moveDistance);
                    anim_space1Shrink = creatAnimator(view_space1,moveDistance,0);
                    windowShrinkDistance = height - mContext.getResources().getDimensionPixelOffset(R.dimen.y168);
                    PrintLog.i("screenHeight="+screenHeight+"  height="+height+"  moveDistance="+moveDistance + "  statusHeight="+statusHeight);
                }
            }
        });
        anim_space2Extent = creatAnimator(view_space2,0,0);
        anim_space2Shrink = creatAnimator(view_space2,0,0);
        anim_avatarExtent = creatAnimator(rl_fullscreen_avatar,0,mContext.getResources().getDimensionPixelOffset(R.dimen.y190));
        anim_avatarShrink = creatAnimator(rl_fullscreen_avatar,mContext.getResources().getDimensionPixelOffset(R.dimen.y190),0);
        anim_titleHide = ObjectAnimator.ofFloat(rl_title, "alpha", 1f, 0f);
        anim_titleShow = ObjectAnimator.ofFloat(rl_title, "alpha", 0f, 1f);
        anim_titleHide.setDuration(200);
        anim_titleShow.setDuration(200);

        anim_space2Extent.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                rl_title.setVisibility(View.GONE);
                rl_fullscreen_title.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim_space2Shrink.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                rl_title.setVisibility(View.VISIBLE);
                rl_fullscreen_title.setVisibility(View.GONE);
                view_shadow.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });



    }

    /**
     * 呼叫框收缩，触发view空间拉伸
     * @param triggerView 触发的view
     */
    private ValueAnimator anim_viewExtent,anim_viewShrink;
    private View triggerView;
    public static boolean isExtentMode = true;
    public boolean isExtending = false;
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void viewExtent(View triggerView, boolean isExtent){
        //触发的view拉伸,收缩view显示
        isExtentMode = isExtent;
        PrintLog.i("viewExtent()");
        if(!isExtent){
            this.triggerView = triggerView;
            if(anim_viewExtent == null){
                anim_viewExtent = creatAnimator(triggerView, 0, windowShrinkDistance);
                anim_viewExtent.setDuration(500);
            }
            anim_viewExtent.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onAnimationEnd(Animator animation) {
                    PrintLog.i("viewExtent() onAnimationEnd");
                    view_callNormal.setVisibility(GONE);
                    isExtentMode = false;
                    UctApplication.getInstance().getGroupCallWindow().show(((MainActivity) mContext));
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            anim_viewExtent.start();
        }else if(view_callNormal.getVisibility() == View.GONE){
            view_callNormal.setVisibility(VISIBLE);
            if(anim_viewShrink == null){
                anim_viewShrink = creatAnimator(triggerView, windowShrinkDistance, 0);
                anim_viewShrink.setDuration(500);
            }
            anim_viewShrink.start();
        }
    }

    public void notifyLockGroupChanged(String groupId){
        if(currentGroupData.getGrouTel().equals(groupId)){
            setLockView();
        }
    }
    private void setLockView(){
        //判断是否是锁定组
        String lockGroups = (String) UctClientApi.getUserData(SETTINGS_LOCK_GROUP, "");
        boolean isFind = false;
        if(!TextUtils.isEmpty(lockGroups)) {//有锁定组时,只能切换锁定组
            String[] lockGroup = lockGroups.split(",");
            for (int i = 0; i < lockGroup.length; i++) {
                if (currentGroupData.getGrouTel().equals(lockGroup[i])) {//当前是锁定组
                    isFind = true;
                    break;
                }
            }
        }
        if(isFind){
            iv_lockItem_headView.setVisibility(VISIBLE);
        }else{
            iv_lockItem_headView.setVisibility(GONE);
        }
    }
    public Group currentGroupData;
    public void setData(Group data) {
        this.currentGroupData = data;
        setLockView();
        if(!TextUtils.isEmpty(data.getGroupName())){
            tv_groupName.setText(data.getGroupName());
            tv_groupNameFullScreen.setText(data.getGroupName());
        }else if(!TextUtils.isEmpty(data.getGrouTel())){
            tv_groupName.setText(data.getGrouTel());
            tv_groupNameFullScreen.setText(data.getGrouTel());
        }
    }

    public void setGCallState(int GCallState,String groupId,String talkingNickname) {
        this.gCallState = GCallState;
        switch (GCallState) {
            case USER_OFFLINE://离线
                PrintLog.i("-----setGCallState USER_OFFLINE");
            case GCALL_FAILED://5:组呼失败
                PrintLog.i("-----setGCallState GCALL_FAILED");
            case GCALL_HANGUP://1:组呼挂断
                PrintLog.i("-----setGCallState GCALL_HANGUP 1");
                tv_groupCallState.setText("");
                PrintLog.i("-----setGCallState GCALL_HANGUP 2");
                iv_hangup.setImageResource(R.mipmap.btn_gcall_hang_up_grey);
                PrintLog.i("-----setGCallState GCALL_HANGUP 3");
                tv_groupCallStateFullscreen.setText("");
                if(drawable_otherSpeak.isRunning()){
                    drawable_otherSpeak.stop();
                }
                stopGCallRecord();
                PrintLog.i("-----setGCallState GCALL_HANGUP 4");
                view_otherSpeak.setVisibility(INVISIBLE);
                if(drawable_soundWaveBig.isRunning()){
                    drawable_soundWaveBig.stop();
                }
                if(drawable_soundWaveShrink.isRunning()){
                    drawable_soundWaveShrink.stop();
                }
                view_selfSpeak.setVisibility(VISIBLE);
                tv_press2speak.setVisibility(VISIBLE);
                iv_soundWave.setVisibility(INVISIBLE);
                if(GCallState == GCALL_FAILED && isLongClick){
                    iv_groupCall.setImageResource(R.mipmap.icon_gcall_state_fail);
                }else{
                    iv_groupCall.setImageResource(R.drawable.selector_btn_call_group);
                }
                setTimerRelease();
                tv_speakTime.setText("");
                waveView.stop();
                waveView.setVisibility(INVISIBLE);
                PrintLog.i("-----setGCallState GCALL_HANGUP 5");
                break;
            case NOBODY_SPEAK://2:没人说话
                PrintLog.i("-----setGCallState NOBODY_SPEAK");
                stopGCallRecord();
                tv_groupCallState.setText(R.string.string_gcall_nobody_speak);
                tv_groupCallStateFullscreen.setText(R.string.string_gcall_nobody_speak);
                if(drawable_otherSpeak.isRunning()){
                    drawable_otherSpeak.stop();
                }
                view_otherSpeak.setVisibility(INVISIBLE);
                if(drawable_soundWaveBig.isRunning()){
                    drawable_soundWaveBig.stop();
                }
                if(drawable_soundWaveShrink.isRunning()){
                    drawable_soundWaveShrink.stop();
                }
                view_selfSpeak.setVisibility(VISIBLE);
                tv_press2speak.setVisibility(VISIBLE);
                iv_soundWave.setVisibility(INVISIBLE);
                iv_groupCall.setImageResource(R.drawable.selector_btn_call_group);
                waveView.stop();
                waveView.setVisibility(INVISIBLE);
                break;
            case GCALL_CONFIRM:
                //停止录音
                stopGCallRecord();//持续说话超过60s时，GCALL_CONFIRM组呼：GCALL_CONFIRM -> SELF_SPEAK
                break;
            case SELF_SPEAK://3:自己说
                PrintLog.i("-----setGCallState SELF_SPEAK");
                view_selfSpeak.setVisibility(VISIBLE);
                iv_soundWave.setVisibility(VISIBLE);
                iv_soundWaveShrink.setVisibility(VISIBLE);
                view_otherSpeak.setVisibility(INVISIBLE);
                tv_press2speak.setVisibility(INVISIBLE);
                iv_soundWave.setVisibility(VISIBLE);
                iv_groupCall.setImageResource(R.mipmap.icon_gcall_state_succeed);
                if(drawable_soundWaveBig != null){
                    if(drawable_otherSpeak.isRunning()){
                        drawable_otherSpeak.stop();
                    }
                    drawable_soundWaveBig.start();
                    drawable_soundWaveShrink.start();
                }
                tv_groupCallState.setText(mContext.getString(R.string.group_calling));
                tv_groupCallStateFullscreen.setText(mContext.getString(R.string.group_calling));
                //录音打开则开始录音
                startGCallRecord(currentGroupData.getGrouTel());
                waveView.start();
                waveView.setVisibility(VISIBLE);
                iv_hangup.setImageResource(R.drawable.selector_hang_up);
                break;
            case OTHER_SPEAK://4:别人说
                //停止录音
                stopGCallRecord();
                PrintLog.i("-----setGCallState OTHER_SPEAK");
                view_selfSpeak.setVisibility(INVISIBLE);
                iv_soundWaveShrink.setVisibility(INVISIBLE);
                view_otherSpeak.setVisibility(VISIBLE);
                iv_groupCall.setImageResource(R.drawable.btn_gcall_voice_nor);
                tv_otherSpeak.setText(talkingNickname +"  "+ mContext.getString(R.string.in_speaking));
                drawable_otherSpeak.start();
                if(drawable_soundWaveBig.isRunning()){
                    drawable_soundWaveBig.stop();
                }
                if(drawable_soundWaveShrink.isRunning()){
                    drawable_soundWaveShrink.stop();
                }
                waveView.start();
                waveView.setVisibility(VISIBLE);
                tv_groupCallState.setText(mContext.getString(R.string.group_calling));
                tv_groupCallStateFullscreen.setText(mContext.getString(R.string.group_calling));
                iv_hangup.setImageResource(R.drawable.selector_hang_up);
                break;
            case GCALL_SUCCEED://6:组呼发起成功，但还未确认
                PrintLog.i("-----setGCallState GCALL_SUCCEED");
                speakStartTime = System.currentTimeMillis();
                initTimer();
                break;
            case GCALL_RELEASE://7:组呼释放
                PrintLog.i("-----setGCallState GCALL_RELEASE");
                if(drawable_soundWaveBig.isRunning()){
                    drawable_soundWaveBig.stop();
                }
                if(drawable_soundWaveShrink.isRunning()){
                    drawable_soundWaveShrink.stop();
                }
                waveView.stop();
                waveView.setVisibility(INVISIBLE);
                iv_groupCall.setImageResource(R.drawable.selector_btn_call_group);
                break;
            case OTHER_LAUCHER_GCALL://8:被叫组呼
                PrintLog.i("-----setGCallState OTHER_LAUCHER_GCALL");
                view_selfSpeak.setVisibility(INVISIBLE);
                iv_soundWaveShrink.setVisibility(INVISIBLE);
                view_otherSpeak.setVisibility(VISIBLE);
                iv_groupCall.setImageResource(R.drawable.btn_gcall_voice_nor);
                waveView.start();
                waveView.setVisibility(VISIBLE);
                tv_otherSpeak.setText(talkingNickname +"  "+ mContext.getString(R.string.in_speaking));
                drawable_otherSpeak.start();
                if(drawable_soundWaveBig.isRunning()){
                    drawable_soundWaveBig.stop();
                }
                if(drawable_soundWaveShrink.isRunning()){
                    drawable_soundWaveShrink.stop();
                }
                Group groupData = GroupDBManager.getInstance().queryGroupByID(groupId);
                setData(groupData);
                if(groupData == null){
                    tv_groupName.setText(groupId);
                    tv_groupNameShrink.setText(groupId);
                    tv_groupNameFullScreen.setText(groupId);
                }
                tv_groupCallState.setText(mContext.getString(R.string.group_calling));
                tv_groupCallStateFullscreen.setText(mContext.getString(R.string.group_calling));
                iv_hangup.setImageResource(R.drawable.selector_hang_up);
                speakStartTime = System.currentTimeMillis();
                initTimer();
                //判断是否是锁定组
                String lockGroups = (String) UctClientApi.getUserData(SETTINGS_LOCK_GROUP, "");
                boolean isFind = false;
                if(!TextUtils.isEmpty(lockGroups)) {//有锁定组时,只能切换锁定组
                    String[] lockGroup = lockGroups.split(",");
                    for (int i = 0; i < lockGroup.length; i++) {
                        if (groupId.equals(lockGroup[i])) {//当前是锁定组
                            isFind = true;
                            break;
                        }
                    }
                }
                if(isFind){
                    iv_lockItem_headView.setVisibility(VISIBLE);
                }else{
                    iv_lockItem_headView.setVisibility(GONE);
                }
                break;

        }
    }

    /**
     * 开始组呼录音
     */
    private void startGCallRecord(String mSendTel) {
        int supportGCallRecord = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_GROUP_CALL_RECORD, 0);
        if(supportGCallRecord == 1){
            String userName = AppContext.getAppContext().getLoginNumber();
            String recordPath;
            if (SDCardUtils.isSDCardEnable()) {
                if (!SDCardUtils.isAvailableInternalMemory()) {
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.msg_msg_send_error_2), -1);
                    return;
                }
                conversationId = ConversationDBManager.getInstance(mContext).queryConversationId(userName, mSendTel, mSendTel);
                recordPath = SDCardUtils.getChatRecordPath(conversationId,userName + "_" + mSendTel);
                boolean isSuccess = SDCardUtils.mkdir2(recordPath);
            } else if (!UctClientApi.isUserOnline()) {
                ToastUtils.getToast().showMessageShort(getContext(), getContext().getString(R.string.msg_msg_send_error_1), -1);
                return;
            } else {
                ToastUtils.getToast().showMessageShort(getContext(), getContext().getString(R.string.sdcard_no_exist), -1);
                return;
            }
            smsgId = StrUtils.getSmsId(mSendTel, userName);
            recordFileName = recordPath + smsgId + ".mp3";
            int result = UctClientApi.UctAudioVideoRecordStart(1, 0, recordFileName);
            PrintLog.i("startGCallRecord() UctAudioVideoRecordStart result=" + result);
            if (result == 0) {
                startRecordTime = System.currentTimeMillis();
                isSuccessRecord=true;
            } else {
                ToastUtils.getToast().showMessageShort(getContext(), getContext().getString(R.string.record_recording_equipment_is_not_available), -1);
                isSuccessRecord=false;
            }
        }
    }

    /**
     * 停止组呼录音
     */
    private void stopGCallRecord() {
        if(isSuccessRecord){
            int result = UctClientApi.UctAudioVideoRecordStop();
            if (result == 0) {
                long duration = (System.currentTimeMillis() - startRecordTime)/1000;
                PrintLog.i("stopGCallRecord() duration="+duration);
                if(duration >= 1){//录音时间大于1s
                    String smgId = smsgId + "_" + duration;
                    AppUtils.sendMsg(conversationId,""+duration,recordFileName,smgId,currentGroupData.getGrouTel(),true);
                    FileUtils.scanFile(mContext, recordFileName);
                }
            } else {
                SDCardUtils.deleteFile(recordFileName);
                recordFileName = "";
                return;
            }
            isSuccessRecord = false;
        }

    }

    /**
     * 组呼框是否全屏切换时监听
     */
    private OnWindowScreenChangeListener onWindowScreenChangeListener;

    public void setOnWindowScreenChangeListener(OnWindowScreenChangeListener onWindowScreenChangeListener) {
        this.onWindowScreenChangeListener = onWindowScreenChangeListener;
    }

    /**
     * popupWindow取消设置时的反应,PTYTCallWindowView中PPW取消时回掉到Fragment中了
     */
    public void setOnWindowDismiss() {
        iv_unfold.setImageResource(R.mipmap.ic_pull_down);
        iv_unfoldFullScreen.setImageResource(R.mipmap.ic_pull_down);
    }

    public void init() {
         /* ptyt begin, 解决重新登录组呼录音开关初始化_4561_chuanqi_20170906 */
        //默认关闭组呼录音
        iv_record.setImageResource(R.mipmap.btn_audio_record_nor);
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_GROUP_CALL_RECORD, 0);
        isSupportGCallRecord = true;
        /* ptyt end*/
        //解决重新登录组呼框显示问题
        view_callNormal.setVisibility(VISIBLE);
        isExtentMode = true;
        UctApplication.getInstance().getGroupCallWindow().hidePopupWindow();
    }

    /**
     * 屏幕
     */
    public interface OnWindowScreenChangeListener{

        void onChanged(boolean isFullscreen);
    }

    /**
     * 组呼监听
     */
    private OnGroupCallListener onGroupCallListener;

    public void setOnGroupCallListener(OnGroupCallListener onGroupCallListener) {
        this.onGroupCallListener = onGroupCallListener;
    }

    public interface OnGroupCallListener{
        void releaseGCallReq();//释放
        void startGroupCall(String groupId);
        void onHangup();//挂断
    }

    /**
     * 点击展开监听
     */
    OnGroupFucUnfoldListener onGroupFucUnfoldListener;

    public void setOnGroupFucUnfoldListener(OnGroupFucUnfoldListener onGroupFucUnfoldListener) {
        this.onGroupFucUnfoldListener = onGroupFucUnfoldListener;
    }

    public interface OnGroupFucUnfoldListener{
        void onClick(View view);
    }



}
