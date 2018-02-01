package com.ptyt.uct.activity;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.adapter.SettingsAdapter;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.model.ContactDBManager;
import com.ptyt.uct.model.ConversationDBManager;
import com.ptyt.uct.common.SettingsConstant;
import com.ptyt.uct.widget.SettingsDialog;

import java.util.ArrayList;
import java.util.List;


@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class SettingsRootActivity extends BasePreferenceActivity implements View.OnClickListener {

    private List<Header> mHeaders = new ArrayList<>();
    private TextView login_name_tv;
    private ListView listView;
    private ImageView iv_head;
    private SettingsAdapter mAdapter;
    private Context mContext;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_settings;
    }

    @Override
    protected void initView() {
        mContext = this;
        listView = (ListView) findViewById(android.R.id.list);
        login_name_tv = (TextView) findViewById(R.id.login_name_tv);
        iv_head = (ImageView) findViewById(R.id.iv_head);
        iv_head.setOnClickListener(this);
        listView.addFooterView(new ViewStub(this));
        tv_actionBarTitle.setText(getResources().getString(R.string.string_settings_title));
        String name = ContactDBManager.getInstance(this).queryContactName(AppContext.getAppContext().getLoginNumber());
        login_name_tv.setText(name);
    }

    @Override
    protected void initData() {
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.header_settings_root_preferences, target);
        mHeaders = target;
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        mAdapter = new SettingsAdapter(this, mHeaders);
        super.setListAdapter(mAdapter);
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        if (header.id == R.id.restore) {
            new SettingsDialog(this, R.style.dialog_non_transparent)
                    .setIcon(R.mipmap.icon_set)
                    .setTitle(getResources().getString(R.string.string_settings_restore_dialog_title1))
                    .setConfirm(getResources().getString(R.string.string_settings_restore_dialog_confirm))
                    .setCancel(getResources().getString(R.string.string_settings_restore_dialog_cancel))
                    .setOnCloseListener(new SettingsDialog.OnCloseListener() {
                        @Override
                        public void onClick(SettingsDialog dialog, boolean confirm) {
                            if (confirm) {
                                SettingsConstant.restoreSettings();
                                finish();
                            }
                        }
                    })
                    .show();
        } else if (header.id == R.id.clear_all_messages) {
            new SettingsDialog(this, R.style.dialog_non_transparent)
                    .setIcon(R.mipmap.icon_set)
                    .setTitle(getResources().getString(R.string.string_settings_clear_all_messages_title))
                    .setConfirm(getResources().getString(R.string.string_settings_clear_all_messages_confirm))
                    .setCancel(getResources().getString(R.string.string_settings_clear_all_messages_cancel))
                    .setOnCloseListener(new SettingsDialog.OnCloseListener() {
                        @Override
                        public void onClick(SettingsDialog dialog, boolean confirm) {
                            if (confirm) {
                                ConversationDBManager.getInstance(mContext).deleteAllConversation();
                            }
                        }
                    })
                    .show();
        } else if (header.id == R.id.power_saving) {
            if (SettingsConstant.isPowerSavingMode()) {
                // 关闭省电模式
                new SettingsDialog(this, R.style.dialog_non_transparent)
                        .setIcon(R.mipmap.icon_set)
                        .setTitle(getResources().getString(R.string.string_settings_power_saving_title2))
                        .setConfirm(getResources().getString(R.string.string_settings_power_saving_confirm2))
                        .setCancel(getResources().getString(R.string.string_settings_power_saving_cancel))
                        .setOnCloseListener(new SettingsDialog.OnCloseListener() {
                            @Override
                            public void onClick(SettingsDialog dialog, boolean confirm) {
                                if (confirm) {
                                    SettingsConstant.setPowerSaving(false);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        })
                        .show();
            } else {
                // 打开省电模式
                new SettingsDialog(this, R.style.dialog_non_transparent)
                        .setIcon(R.mipmap.icon_set)
                        .setTitle(getResources().getString(R.string.string_settings_power_saving_title1))
                        .setConfirm(getResources().getString(R.string.string_settings_power_saving_confirm1))
                        .setCancel(getResources().getString(R.string.string_settings_power_saving_cancel))
                        .setOnCloseListener(new SettingsDialog.OnCloseListener() {
                            @Override
                            public void onClick(SettingsDialog dialog, boolean confirm) {
                                if (confirm) {
                                    SettingsConstant.setPowerSaving(true);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        })
                        .show();
            }
        } else {
            super.onHeaderClick(header, position);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_head:
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
