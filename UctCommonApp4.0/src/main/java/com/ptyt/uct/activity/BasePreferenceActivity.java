package com.ptyt.uct.activity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.AppManager;
import com.ptyt.uct.common.UctApplication;

/**
 * Title: com.ptyt.uct.activity
 * Description:
 * Date: 2017/8/3
 * Author: ShaFei
 * Version: V1.0
 */

public abstract class BasePreferenceActivity extends PreferenceActivity {

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
        super.onCreate(savedInstanceState);
        AppManager.getAppManager().addActivity(this);
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
        setContentView(R.layout.activity_actionbar_base);
        //全透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4 全透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0 全透明实现
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        initActionBar();

        initView();

        initData();

        initEvent();

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
    protected abstract void initView();

    /**
     * 2 初始化数据
     */
    protected void initData() {
    }

    /**
     * 3.事件
     */
    protected void initEvent() {
    }


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
            view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view_root.addView(view);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getAppManager().finishActivity(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        /**
         * 当在组呼时，需展示悬浮窗(除了在mainActivity中显示CallFragment中时,MainActivity的onResume会重写,里面会细分判断
         */
        if(UctApplication.getInstance().isInGroupCall){
            UctApplication.getInstance().getGroupCallWindow().show(this);
        }else{
            UctApplication.getInstance().getGroupCallWindow().hidePopupWindow();
        }
    }
}
