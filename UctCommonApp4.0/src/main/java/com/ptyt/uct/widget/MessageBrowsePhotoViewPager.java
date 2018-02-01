package com.ptyt.uct.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Title: com.ptyt.uct.widget
 * Description:
 * Date: 2017/7/31
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageBrowsePhotoViewPager extends ViewPager {
    public MessageBrowsePhotoViewPager(Context context) {
        super(context);
    }

    public MessageBrowsePhotoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return false;
    }
}
