package com.ptyt.uct.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ptyt.uct.utils.SoundManager;

/**
 * @Description: 耳机插入的监听
 * @Date: 2018/1/4
 * @Author: KeChuanqi
 * @Version:V1.0
 *
 * 1.插入耳机后不能免提，否则声音就外放;
 * 2.重新拔出耳机后，免提能起效;
 * 呼叫：插入耳机,免提关掉,有免提图标的变灰
 */

public class HeadsetPlugBroadcastReceiver extends BroadcastReceiver {

    public static boolean isHeadsetPlugIn = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
            if(intent.hasExtra("state")) {
                if(intent.getIntExtra("state", 0) == 0) {
//                    ToastUtils.getToast().showMessageShort(context,"耳机拔出",-1);
                    isHeadsetPlugIn = false;
                    SoundManager.setSpeakerphoneOn(context,true);
                } else if(intent.getIntExtra("state", 0) == 1) {
//                    ToastUtils.getToast().showMessageShort(context,"耳机插入",-1);
                    isHeadsetPlugIn = true;
                    SoundManager.setSpeakerphoneOn(context,false);
                }
            }
        }
    }
}
