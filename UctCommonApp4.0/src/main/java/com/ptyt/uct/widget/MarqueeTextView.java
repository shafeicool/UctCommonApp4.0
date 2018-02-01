package com.ptyt.uct.widget;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Title: com.ptyt.uct.widget
 * Description:
 * Date: 2017/5/27
 * Author: ShaFei
 * Version: V1.0
 */

public class MarqueeTextView extends android.support.v7.widget.AppCompatTextView
{
    public MarqueeTextView(Context context)
    {
        this(context, null);
    }

    public MarqueeTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        setFocusable(true);
        setFocusableInTouchMode(true);

        setSingleLine();
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        setFocusable(true);
        setFocusableInTouchMode(true);

        setSingleLine();
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
    }


    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect)
    {
        if (focused)
        {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean focused)
    {
        if (focused)
        {
            super.onWindowFocusChanged(focused);
        }
    }

    @Override
    public boolean isFocused()
    {
        return true;
    }
}