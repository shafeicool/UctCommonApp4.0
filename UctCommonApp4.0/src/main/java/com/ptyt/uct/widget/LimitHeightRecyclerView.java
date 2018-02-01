package com.ptyt.uct.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.ptyt.uct.R;

/**
 * @Description: RecyclerView
 * @Date: 2017/11/3
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class LimitHeightRecyclerView extends RecyclerView {
    private final int maxHeight;

    public LimitHeightRecyclerView(Context context) {
        this(context,null);
    }

    public LimitHeightRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaxHeight);
        maxHeight = (int) typedArray.getDimension(R.styleable.MaxHeight_maxHeight, 0);

    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        heightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthSpec, heightSpec);
    }
}
