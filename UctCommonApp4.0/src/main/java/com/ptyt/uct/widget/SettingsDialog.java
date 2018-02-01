package com.ptyt.uct.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.utils.StrUtils;

/**
 * Title: com.ptyt.uct.widget
 * Description:
 * Date: 2017/8/3
 * Author: ShaFei
 * Version: V1.0
 */


public class SettingsDialog extends Dialog implements View.OnClickListener {

    private Context context;
    private static SettingsDialog instance;
    private ImageView iv_icon;
    private TextView tv_title, tv_cancel, tv_confirm;
    private String title;
    private int icon;
    private OnCloseListener listener;
    private String confirm;
    private String cancel;

    public SettingsDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        setCancelable(false);
    }

    public static synchronized SettingsDialog getDialog(Context context, int themeResId) {
        if (instance == null) {
            instance = new SettingsDialog(context, themeResId);
        }
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_settings_restore);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        tv_confirm = (TextView) findViewById(R.id.tv_confirm);

        tv_cancel.setOnClickListener(this);
        tv_confirm.setOnClickListener(this);

        if (icon != 0) {
            iv_icon.setImageResource(icon);
        }
        if (!StrUtils.isEmpty(title)) {
            tv_title.setText(title);
        }
        if (!StrUtils.isEmpty(confirm)) {
            tv_confirm.setText(confirm);
        }
        if (!StrUtils.isEmpty(cancel)) {
            tv_cancel.setText(cancel);
        }

    }

    public SettingsDialog setIcon(int icon) {
        this.icon = icon;
        return this;
    }

    public SettingsDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public SettingsDialog setConfirm(String confirm) {
        this.confirm = confirm;
        return this;
    }

    public SettingsDialog setCancel(String cancel) {
        this.cancel = cancel;
        return this;
    }

    public SettingsDialog setOnCloseListener(OnCloseListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                if (listener != null) {
                    listener.onClick(this, false);
                }
                this.dismiss();
                break;
            case R.id.tv_confirm:
                if (listener != null) {
                    listener.onClick(this, true);
                }
                this.dismiss();
                break;
        }
    }

    public interface OnCloseListener {
        void onClick(SettingsDialog dialog, boolean confirm);
    }
}

