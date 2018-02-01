package com.ptyt.uct.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.ptyt.uct.R;

/**
 * Title: com.ptyt.uct.widget
 * Description:
 * Date: 2017/7/31
 * Author: ShaFei
 * Version: V1.0
 */

public class PhotoBrowseLayout extends FrameLayout {
    private boolean isVideo;

    public PhotoBrowseLayout(@NonNull Context context) {
        super(context);
    }

    public PhotoBrowseLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_message_photo_browse, null);
        removeAllViews();
        LayoutParams mLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(view, mLayoutParams);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isVideo) {
            return true;
        } else {
            return false;
        }
    }

    public void setVideo(boolean isVideo) {
        this.isVideo = isVideo;
    }
}
