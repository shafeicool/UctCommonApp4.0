package com.ptyt.uct.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.adapter.BaseRecyAdapter;
import com.ptyt.uct.adapter.SettingsAccountAdapter;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.AppManager;
import com.ptyt.uct.entity.SettingsAccountEntity;
import com.ptyt.uct.model.ContactDBManager;
import com.ptyt.uct.model.GroupUserDBManager;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.widget.PTYTCallWindowView;
import com.ptyt.uct.widget.SettingsDialog;

import java.util.ArrayList;
import java.util.List;

public class SettingsAccountActivity extends BaseActionBarActivity implements View.OnClickListener, BaseRecyAdapter.OnItemClickListener {

    private Context mContext;
    private RecyclerView rv_list;
    private List<SettingsAccountEntity> list = new ArrayList<>();
    private SettingsAccountAdapter mAdapter;
    private RelativeLayout rl_modify;
    private TextView tv_exit;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_settings_account;
    }

    @Override
    protected void initWidget() {
        mContext = this;
        rv_list = (RecyclerView) findViewById(R.id.rv_list);
        rl_modify = (RelativeLayout) findViewById(R.id.rl_modify);
        tv_exit = (TextView) findViewById(R.id.tv_exit);
        rv_list.setHasFixedSize(true);
        rv_list.setLayoutManager(new LinearLayoutManager(this));
        ((DefaultItemAnimator) rv_list.getItemAnimator()).setSupportsChangeAnimations(false);
        tv_actionBarTitle.setText(getResources().getString(R.string.string_settings_account));
        //        tv_actionBarRight.setVisibility(View.VISIBLE);
        //        tv_actionBarRight.setText(getResources().getString(R.string.string_settings_account_editor));
        //        tv_actionBarRight.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        mAdapter = new SettingsAccountAdapter(this);
        rv_list.setAdapter(mAdapter);
        SettingsAccountEntity entity = new SettingsAccountEntity();
        String name = ContactDBManager.getInstance(this).queryContactName(AppContext.getAppContext().getLoginNumber());
        entity.setName(name);
        entity.setNumber(AppContext.getAppContext().getLoginNumber());
        list.add(entity);
        mAdapter.addAll(list);
    }

    @Override
    protected void initEvent() {
        mAdapter.setOnItemClickListener(this);
        rl_modify.setOnClickListener(this);
        tv_exit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_modify:
                startActivity(new Intent(SettingsAccountActivity.this, SettingsModifyPwdActivity.class));
                break;
            case R.id.tv_exit:
                new SettingsDialog(this, R.style.dialog_non_transparent)
                        .setIcon(R.mipmap.icon_power)
                        .setTitle(getResources().getString(R.string.string_settings_account_dialog_title))
                        .setConfirm(getResources().getString(R.string.string_settings_account_dialog_confirm))
                        .setCancel(getResources().getString(R.string.string_settings_account_dialog_cancel))
                        .setOnCloseListener(new SettingsDialog.OnCloseListener() {
                            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
                            @Override
                            public void onClick(SettingsDialog dialog, boolean confirm) {
                                if (confirm) {
                                    /* ptyt begin, 退出登录改为退出到登陆界面_4125_shafei_20170912 */
                                    AppManager.getAppManager().AppLogOut(mContext);
                                    /* ptyt end */
                                    /* ptyt begin,重置状态，避免切换账号时组呼悬浮窗出现_KeChuanqi_20171017*/
                                    PTYTCallWindowView.isExtentMode = true;
                                    AppUtils.stopPlayMedia();//关掉提示音
                                    GroupUserDBManager.getInstance(mContext).deleteAll();//清空组用户数据库
                                    /* ptyt end*/
                                } else {
                                }
                            }
                        })
                        .show();

                break;
            //            case R.id.tv_right1:
            //                if (mAdapter.editorMode) {
            //                    mAdapter.editorMode = false;
            //                } else {
            //                    mAdapter.editorMode = true;
            //                }
            //                mAdapter.notifyDataSetChanged();
            //                break;

        }
    }

    @Override
    public void onItemClick(int pos, View itemView) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
