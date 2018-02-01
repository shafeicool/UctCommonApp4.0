package com.ptyt.uct.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.common.AppContext;

/**
 * @Description:
 * @Date: 2017/5/31
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public abstract class BaseActionBarActivity extends BaseActivity {

    //标题栏layout
    protected RelativeLayout rl_layout;
    //标题栏右侧图标(默认gone)
    protected ImageView iv_actionBarRight;
    //标题栏右侧文字(默认gone)
    protected TextView tv_actionBarRight, tv_actionBarRight2;
    //标题栏中间文字
    protected TextView tv_actionBarTitle;
    //标题栏右侧Layout
    protected RelativeLayout rl_relative;
    //标题栏右侧文字,带背景(默认gone)
    protected TextView tv_rightButton;
    //左侧取消文字
    protected TextView tv_cancel;
    protected ImageView iv_back;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setCheck(false);
        super.onCreate(savedInstanceState);
        boolean isHaveInitLib = AppContext.getAppContext().isHaveInitLib(this);
        PrintLog.i("isHaveInitLib = " + isHaveInitLib);
        if (!isHaveInitLib) {
            if (!isFinishing()) {
                PrintLog.i("this = " + getClass().getSimpleName() + "   finish");
                finish();
            }
            return;
        }
        PrintLog.i("this = " + getClass().getSimpleName());
    }

    /**
     * 0 设置布局id
     *
     * @return
     */
    protected abstract int setLayoutId();

    /**
     * 1 初始化view
     */
    @Override
    protected final void initView() {
        PrintLog.e("this = " + getClass().getSimpleName());
        setContentView(R.layout.activity_actionbar_base);
        initActionBar();
        initWidget();
    }
    /**
     * 0 设置布局id
     *
     * @return
     */
    protected void initWidget(){}

    /**
     * 设置actionbar右侧图标,及点击事件
     *
     * @param drwableResId 图片资源
     * @param listener     点击事件,不设置时null
     */
    public void setActionBarRightIcon(int drwableResId, View.OnClickListener listener) {
        iv_actionBarRight.setVisibility(View.VISIBLE);
        iv_actionBarRight.setImageResource(drwableResId);
        iv_actionBarRight.setOnClickListener(listener);
    }

    /**
     * 设置actionbar右侧textview内容
     *
     * @param charSequence
     */
    public void setActionBarRightText(CharSequence charSequence) {
        tv_actionBarRight.setVisibility(View.VISIBLE);
        tv_actionBarRight.setText(charSequence);
    }

    /**
     * 设置标题
     *
     * @param charSequence
     */
    public void setActionBarTitle(CharSequence charSequence) {
        tv_actionBarTitle.setText(charSequence);
    }

    /**
     * 初始化actionbar
     */
    private void initActionBar() {
        rl_layout = (RelativeLayout) findViewById(R.id.rl_layout);
        iv_actionBarRight = (ImageView) findViewById(R.id.iv_right);
        rl_relative = (RelativeLayout) findViewById(R.id.rl_relative);
        tv_actionBarRight = ((TextView) findViewById(R.id.tv_right1));
        tv_actionBarRight2 = ((TextView) findViewById(R.id.tv_right2));
        tv_actionBarTitle = ((TextView) findViewById(R.id.tv_title));
        tv_rightButton = ((TextView) findViewById(R.id.tv_rightButton));
        tv_cancel = ((TextView) findViewById(R.id.tv_cancel));
        iv_back = ((ImageView) findViewById(R.id.iv_back));
        //返回
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //向父容器中添加自己的布局
        View view;
        if (setLayoutId() != 0) {
            FrameLayout view_root = (FrameLayout) findViewById(R.id.fl_root);
            view = View.inflate(this, setLayoutId(), null);
            view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            view_root.addView(view);
        }
    }

    /**
     * 添加一个frament到当前view中当需要时，使用该方法，此时设置setLayoutId返回0
     *
     * @param fragment
     */
    public void addFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().add(R.id.fl_root, fragment).commit();

    }
}
