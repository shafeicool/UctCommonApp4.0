package com.ptyt.uct.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.view.WindowManager;

import com.android.uct.utils.PrintLog;

public class ScreenManager {
	private static ScreenManager instance;
	private ScreenManager (){
		
	}
	
	public static synchronized ScreenManager getInstance() {
		if(instance == null){
			instance = new ScreenManager();
		}
		return instance;
	}
	
	@TargetApi(20)
	public boolean isScreen(Context mContext){
		PowerManager powerManager =(PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		 boolean isShow =false;
		if(Build.VERSION.SDK_INT >= 20.) {  
			isShow = powerManager.isInteractive();
		} else {  
		   isShow = powerManager.isScreenOn();
		}  
		PrintLog.i("当前屏幕是否亮屏 ："+(isShow?"亮屏":"不亮屏"));
		return isShow;
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public boolean isLock(Context mContext){
		boolean isLock=false;
		KeyguardManager keyguardManager= (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			isLock = keyguardManager.inKeyguardRestrictedInputMode();
		}else{
			isLock = keyguardManager.isKeyguardLocked();
		}
		PrintLog.i("当前是否锁屏："+(isLock?"锁屏":"不锁屏"));
		return isLock;
	}
	
	public void prepare(Activity context){
		boolean isLock = ScreenManager.getInstance().isLock(context);
		boolean isScreenOn = ScreenManager.getInstance().isScreen(context);
		PrintLog.e("prepare() isLock="+isLock + "  isScreenOn="+isScreenOn);
		if(!isScreenOn && !isLock){//屏幕不亮 && 没锁屏
			context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		}else if(isScreenOn && isLock){//屏幕亮 && 锁屏
			context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		}else if(!isScreenOn && isLock){//屏幕不亮 && 锁屏
			context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		}
	}

	public void clear(Activity context){
		PrintLog.e("clear()");
		context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}
	
}
