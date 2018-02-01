package com.ptyt.uct.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ptyt.uct.R;

/**
 * @Description: 请求数据  过程中|返回数据为空  的显示
 * @Date: 2017/8/7
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class EmptyLayout extends FrameLayout {
    private View rl_showEmpty;
    //private View rl_showLoading;

    public EmptyLayout(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public EmptyLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public EmptyLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View view = View.inflate(context, R.layout.layout_empty, null);
        rl_showEmpty = view.findViewById(R.id.rl_showEmpty);
        //rl_showLoading = view.findViewById(R.id.rl_showLoading);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(view);
    }

    public void setEmptyVisible(boolean visible){
        if(visible){
            rl_showEmpty.setVisibility(VISIBLE);
//            rl_showLoading.setVisibility(INVISIBLE);
        }else {
            rl_showEmpty.setVisibility(INVISIBLE);
        }
    }
//    public void setLoadingVisible(boolean visible){
//        if(visible){
//            rl_showLoading.setVisibility(VISIBLE);
//            rl_showEmpty.setVisibility(INVISIBLE);
//        }else {
//            rl_showEmpty.setVisibility(INVISIBLE);
//            rl_showLoading.setVisibility(INVISIBLE);
//        }
//    }
}
