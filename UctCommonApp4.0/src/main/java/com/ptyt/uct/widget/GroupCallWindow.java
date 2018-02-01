package com.ptyt.uct.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.uct.bean.GroupData;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.MainActivity;
import com.ptyt.uct.callback.GCallCallback;
import com.ptyt.uct.callback.GroupInfoCallback;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.utils.FloatWindowManager;
import com.ptyt.uct.utils.NetUtils;
import com.ptyt.uct.utils.ScreenUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.viewinterface.IGroupInfoView;

import java.util.List;

import de.greenrobot.event.EventBus;

import static com.ptyt.uct.utils.ConstantUtils.ACTION_TAB_TO_GCALL;

public class GroupCallWindow implements View.OnTouchListener, View.OnLongClickListener {

	public static final int OVERLAY_PERMISSION_CODE = 5001;
	private final Context mContext;
	private final WindowManager mWindowManager;
	private WindowManager.LayoutParams params;
	private boolean isShow = false;
	private View mView;
	private MarqueeTextView tv_groupName;
	private ImageView iv_soundWave;
	private TextView tv_otherSpeak;
	private ImageView iv_groupCall;
	private View view_otherSpeak;
	private ImageView iv_otherSpeak;
	private AnimationDrawable drawableOtherSpeak;
	private AnimationDrawable drawableSoundWave;
	private int screenWid,screenHei;
	private boolean isLongClick;
	public View rl_miniParent;
	private View rl_bigParent;
	private SettingsDialog settingsPpwPermissionDialog;

	public GroupCallWindow(Context context) {
		PrintLog.i("构造GroupCallWindow");
		mContext = context.getApplicationContext();
		// 获取WindowManager
		mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		screenWid = ScreenUtils.getScreenWidth(mContext);
		screenHei = ScreenUtils.getScreenHeight(mContext);
		onCreate();
	}

	private void onCreate() {
		PrintLog.i("onCreate()");
		mView = LayoutInflater.from(mContext).inflate(R.layout.view_gcall_floating_window,null);
		tv_groupName = ((MarqueeTextView) mView.findViewById(R.id.tv_groupName_shrink));
		tv_otherSpeak = ((TextView) mView.findViewById(R.id.tv_otherSpeak));
		iv_soundWave = ((ImageView) mView.findViewById(R.id.iv_soundWave_shrink));
		iv_groupCall = ((ImageView) mView.findViewById(R.id.iv_groupCall_shrink));
		rl_miniParent = mView.findViewById(R.id.rl_miniParent);
		rl_bigParent = mView.findViewById(R.id.rl_bigParent);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rl_bigParent.getLayoutParams();
		layoutParams.width = ScreenUtils.getScreenWidth(mContext);
		rl_bigParent.setLayoutParams(layoutParams);
		view_otherSpeak = mView.findViewById(R.id.ll_otherSpeak);
		iv_otherSpeak = ((ImageView) mView.findViewById(R.id.iv_otherSpeak));
		drawableOtherSpeak = ((AnimationDrawable) iv_otherSpeak.getDrawable());
		drawableSoundWave = ((AnimationDrawable) iv_soundWave.getDrawable());
		// 点击窗口外部区域可消除
		// 这点的实现主要将悬浮窗设置为全屏大小，外层有个透明背景，中间一部分视为内容区域
		// 所以点击内容区域外部视为点击悬浮窗外部
		params = new WindowManager.LayoutParams();
		params.gravity = Gravity.BOTTOM|Gravity.LEFT;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.format = PixelFormat.TRANSLUCENT;
		params.width = WindowManager.LayoutParams.MATCH_PARENT;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N){//避免在没悬浮窗权限时不显示 >7.0
			params.type = WindowManager.LayoutParams.TYPE_PHONE;//电话窗口。它用于电话交互（特别是呼入）。它置于所有应用程序之上，状态栏之下。
			//params.type = WindowManager.LayoutParams.TYPE_TOAST;
		}else if(Build.VERSION.SDK_INT > 19){
			params.type = WindowManager.LayoutParams.TYPE_TOAST;
		}else {
			params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;//系统提示。它总是出现在应用程序窗口之上。
		}
		//系统顶层窗口。显示在其他一切内容之上。此窗口不能获得输入焦点，否则影响锁屏。TYPE_SYSTEM_OVERLAY

		mView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PrintLog.i("点击悬浮窗--此时界面是否在MainActivity:"+UctApplication.getInstance().isInMainActivity);
				//1.隐藏悬浮窗
				UctApplication.getInstance().getGroupCallWindow().hidePopupWindow();
				//2.1点击悬浮窗时悬浮时，如果此时界面显示在MainActivity中:只需切换至CallFragment
				if(UctApplication.getInstance().isInMainActivity){
					/* ptyt begin, 统一eventbus发送的都是eventbean对象_4053_shafei_20170906 */
					EventBean eventBean = new EventBean(ACTION_TAB_TO_GCALL);
					EventBus.getDefault().post(eventBean);
					/* ptyt end */
				}else{//2.2如果不在，需跳转至MainActivity,并且需将fragment切换至CallFragment
					Intent intent = new Intent(mContext, MainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
				}
			}
		});
		mView.setOnTouchListener(this);
		mView.setOnLongClickListener(this);
		iv_groupCall.setOnTouchListener(this);
		iv_groupCall.setOnLongClickListener(this);
		mView.setVisibility(View.GONE);
		PrintLog.i("mWindowManager.addView(mView, params)");
		mWindowManager.addView(mView, params);
	}

	/**
	 * 显示
	 */
//	@RequiresApi(api = Build.VERSION_CODES.M)
	@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR2)
	public void show(Activity activity) {
		//判断是否登陆成功
		PrintLog.i("show() isShow="+isShow);
		if (isShow) {
			return;
		}
		params.y = 0;
		params.width = WindowManager.LayoutParams.MATCH_PARENT;
		rl_miniParent.setVisibility(View.GONE);
		rl_bigParent.setVisibility(View.VISIBLE);
		/*start -执行顺序修改，避免不更新 */
		mView.setVisibility(View.VISIBLE);
		mWindowManager.updateViewLayout(mView, params);
		/*end*/
		isShow = true;
		FloatWindowManager.getInstance().applyOrShowFloatWindow(activity);
	}

	/**
	 * 隐藏弹出框
	 */
	public void hidePopupWindow() {
		PrintLog.i("hidePopupWindow() isShow="+isShow);
		if (isShow && null != mView) {
			/*start modify by KeChuanqi in 20170928. addView 和removeView切换时，声波贞动画本应静止又动了*/
			isShow = false;
			mView.setVisibility(View.GONE);
			mWindowManager.updateViewLayout(mView,params);
			//mWindowManager.removeView(mView);
			/*end*/
		}
	}

	GCallCallback gCallCallback = GCallCallback.getInstance();
	/**
	 * 组呼
	 * @param v
	 * @param event
	 * @return
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event){
		if (v.getId() == R.id.view_call_shrink && !(MainActivity.currentFragmentPosition == 1 && UctApplication.getInstance().isInMainActivity)) {//其他位置移动悬浮窗
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					PrintLog.i("MotionEvent.ACTION_DOWN");
					break;
				case MotionEvent.ACTION_MOVE:
					PrintLog.i("MotionEvent.ACTION_MOVE  getRawY()="+(int) event.getRawY() +"  event.getRawX()="+event.getRawX()+"  mView.getMeasuredHeight() / 2="+mView.getMeasuredHeight() / 2+ " params.y="+params.y);
					//getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
					params.y = screenHei - (int) event.getRawY() - mView.getMeasuredHeight() / 2;
					params.x = (int) event.getRawX() - mView.getMeasuredWidth() / 2;
					if (params.y > 200) {
						params.width = WindowManager.LayoutParams.WRAP_CONTENT;
						rl_miniParent.setVisibility(View.VISIBLE);
						rl_bigParent.setVisibility(View.GONE);
					} else {
						params.width = WindowManager.LayoutParams.MATCH_PARENT;
						rl_miniParent.setVisibility(View.GONE);
						rl_bigParent.setVisibility(View.VISIBLE);
					}
					mWindowManager.updateViewLayout(mView, params);
					break;
				case MotionEvent.ACTION_UP:
					PrintLog.i("MotionEvent.ACTION_UP");
					if(isLongClick){
						if(params.y <= 200){
							params.y = 0;
							rl_miniParent.setVisibility(View.GONE);
							rl_bigParent.setVisibility(View.VISIBLE);
						}else{
							if(params.x <= ScreenUtils.getScreenWidth(mContext) / 2 - mView.getMeasuredWidth()){
								params.x = mView.getMeasuredWidth() / 8;
							}else{
								params.x = ScreenUtils.getScreenWidth(mContext) - mView.getMeasuredWidth()*9/8;
							}
						}
						mWindowManager.updateViewLayout(mView, params);
					}
					isLongClick = false;
					break;
			}
			return false;
		}else if(v.getId() == R.id.iv_groupCall_shrink){//组呼按钮

			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					PrintLog.i("gCallCallback.currentGroupId="+gCallCallback.currentGroupId);
					//发起组呼
					if(gCallCallback.currentGroupId != null){
						if(!NetUtils.isNetworkAvailable(mContext)){
							ToastUtils.getToast().showMessageShort(mContext,mContext.getString(R.string.net_error),-1);
						}else{
							gCallCallback.startGroupCall(gCallCallback.currentGroupId);
						}
					}
					break;
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					//释放话权
					gCallCallback.releaseGCallReq();
					break;
			}
		}
		return false;
	}
	@Override
	public boolean onLongClick(View v) {
		PrintLog.i(" onLongClick()");
		switch (v.getId()){
			case R.id.view_call_shrink:
				isLongClick = true;
				break;
			case R.id.iv_groupCall_shrink:
				break;
		}
		return true;
	}
	/**
	 *
	 * @param speakStatus
	 */
	public void speakState(int speakStatus,String groupId,String talkingNickname) {
		PrintLog.i("组呼悬浮窗中：speakStatus="+speakStatus+"   talkingNickname="+talkingNickname + "  groupId="+groupId);
		switch (speakStatus){
			case IGroupInfoView.USER_OFFLINE:
			case IGroupInfoView.GCALL_FAILED://5
			case IGroupInfoView.GCALL_HANGUP://1
				params.y = 0;
				params.width = WindowManager.LayoutParams.MATCH_PARENT;
				rl_miniParent.setVisibility(View.GONE);
				rl_bigParent.setVisibility(View.VISIBLE);
				mWindowManager.updateViewLayout(mView, params);
				drawableSoundWave.stop();
				drawableOtherSpeak.stop();
				iv_soundWave.setVisibility(View.VISIBLE);
				view_otherSpeak.setVisibility(View.INVISIBLE);
				if(speakStatus == IGroupInfoView.GCALL_FAILED && isLongClick){
					iv_groupCall.setImageResource(R.mipmap.btn_intercom_gcall_fail);
				}else {
					iv_groupCall.setImageResource(R.mipmap.ic_gcall_voice_shrink_nor);
				}
			break;
			case IGroupInfoView.GCALL_RELEASE://7
				PrintLog.i("组呼悬浮窗中：speakStatus=pInfoView.GCALL_RELEASE");
                iv_groupCall.setImageResource(R.mipmap.ic_gcall_voice_shrink_nor);
				drawableSoundWave.stop();
				break;
			case IGroupInfoView.NOBODY_SPEAK://2
				PrintLog.i("组呼悬浮窗中：speakStatus=pInfoView.NOBODY_SPEAK drawableSoundWave.isRunning()="+drawableSoundWave.isRunning());
				//modify by KeChuanqi in 20170927 drawableSoundWave.stop()避免有时不起作用(此时drawableSoundWave.isRunning() == false,但其实在run的)
				iv_soundWave.setVisibility(View.INVISIBLE);
				view_otherSpeak.setVisibility(View.VISIBLE);
				drawableSoundWave.stop();
				drawableOtherSpeak.stop();
				tv_otherSpeak.setText("组呼/无人说话");
				iv_groupCall.setImageResource(R.mipmap.ic_gcall_voice_shrink_nor);
				break;
			case IGroupInfoView.SELF_SPEAK://3
				//自己说时：声音wave波动，显示组呼的图，隐藏显示别人说的标识state
				setGroupNickName(groupId);
				iv_groupCall.setImageResource(R.mipmap.btn_intercom_success_shrink);
				iv_soundWave.setVisibility(View.VISIBLE);
				view_otherSpeak.setVisibility(View.INVISIBLE);
				drawableOtherSpeak.stop();
				tv_otherSpeak.setText("");
				if(!drawableSoundWave.isRunning()){
					drawableSoundWave.start();
				}
				break;
			case IGroupInfoView.OTHER_SPEAK://4
				setGroupNickName(groupId);
				iv_groupCall.setImageResource(R.mipmap.ic_gcall_voice_shrink);
				iv_soundWave.setVisibility(View.INVISIBLE);
				view_otherSpeak.setVisibility(View.VISIBLE);
				drawableOtherSpeak.start();
				tv_otherSpeak.setText(talkingNickname+" "+mContext.getString(R.string.gcall_speaking));
				if(drawableSoundWave.isRunning()){
					drawableSoundWave.stop();
				}
				break;
			case IGroupInfoView.OTHER_LAUCHER_GCALL://8
				setGroupNickName(groupId);
				iv_groupCall.setImageResource(R.mipmap.ic_gcall_voice_shrink);
				iv_soundWave.setVisibility(View.INVISIBLE);
				view_otherSpeak.setVisibility(View.VISIBLE);
				drawableOtherSpeak.start();
				tv_otherSpeak.setText(talkingNickname+" "+mContext.getString(R.string.gcall_speaking));
				drawableSoundWave.stop();
				break;
			case IGroupInfoView.NO_GROUP_CALL:
				setGroupNickName(groupId);
				if(UctApplication.getInstance().isInGroupCall){
					//是在组呼状态
				}else {
					iv_groupCall.setImageResource(R.mipmap.ic_gcall_voice_shrink_nor);
					drawableSoundWave.stop();
					drawableOtherSpeak.stop();
					iv_soundWave.setVisibility(View.VISIBLE);
					view_otherSpeak.setVisibility(View.INVISIBLE);
					tv_otherSpeak.setText("");
				}
				break;
			default:
				drawableSoundWave.stop();
				drawableOtherSpeak.stop();
				iv_soundWave.setVisibility(View.VISIBLE);
				view_otherSpeak.setVisibility(View.INVISIBLE);
				iv_groupCall.setImageResource(R.mipmap.ic_gcall_voice_shrink_nor);
				break;
		}
	}

    private boolean isFind = false;
    public void setGroupNickName(String groupId){
		PrintLog.i("groupId="+groupId);
		if(TextUtils.isEmpty(groupId)){
			return;
		}
		List<GroupData> groupDatas = GroupInfoCallback.getInstance().getmGroupList();
		GroupData groupData = null;
		for (int i = 0; i < groupDatas.size(); i++) {
			groupData = groupDatas.get(i);
			if(groupData.groupId .equals(groupId)){
				if(!TextUtils.isEmpty(groupData.groupName)){
					tv_groupName.setText(groupData.groupName);
				}else {
					tv_groupName.setText(groupId);
				}
				isFind = true;
			}
		}
		if(groupData == null || !isFind){
			tv_groupName.setText(groupId);
		}
	}

	/*ptyt start_解决在组呼缩略框状态发起组呼，切换至对讲界面之外的任一界面，拖动缩略框切换至悬浮框，再将界面切回对讲界面时没有恢复为缩略框，悬浮窗也无法拖动，#4925_kechuanqi_20171215*/
	@RequiresApi(api = Build.VERSION_CODES.M)
	public void initWindowShow() {
		if(UctApplication.getInstance().getGroupCallWindow().rl_miniParent.getVisibility() == View.VISIBLE && isShow){//如果当前悬浮框缩略框是悬浮mini状态，则还原到底部
			rl_miniParent.setVisibility(View.GONE);
			rl_bigParent.setVisibility(View.VISIBLE);
			params.y = 0;
			params.width = WindowManager.LayoutParams.MATCH_PARENT;
			mWindowManager.updateViewLayout(mView, params);
		}else {
			UctApplication.getInstance().getGroupCallWindow().show(null);
		}
	}
	/*ptyt end*/

	public boolean isShow() {
		return isShow;
	}

}
