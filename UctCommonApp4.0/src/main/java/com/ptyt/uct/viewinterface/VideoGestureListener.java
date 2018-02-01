package com.ptyt.uct.viewinterface;

import android.view.GestureDetector;
import android.view.MotionEvent;

import com.android.uct.utils.PrintLog;

/**
 * @Description:
 * @Date: 2018/1/22
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class VideoGestureListener implements GestureDetector.OnGestureListener {
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        PrintLog.i("onDown"+motionEvent.getAction());
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
        PrintLog.i("onShowPress"+motionEvent.getAction());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        PrintLog.i("onSingleTapUp"+motionEvent.getAction());
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        PrintLog.i("onScroll motionEvent.getAction()="+motionEvent.getAction()+"   motionEvent1.getAction()="+motionEvent1.getAction() + "  v="+v+"  v1="+v1);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        PrintLog.i("onLongPress"+motionEvent.getAction());
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        PrintLog.i("onFling"+motionEvent.getAction()+"  motionEvent1.getAction()="+motionEvent1.getAction());
        return false;
    }
}
