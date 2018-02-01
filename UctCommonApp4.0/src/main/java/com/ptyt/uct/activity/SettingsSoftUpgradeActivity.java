package com.ptyt.uct.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.uct.bean.NewVersionBean;
import com.android.uct.service.AppUpgrade.UpgradeResponse;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.common.SettingsConstant;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.utils.ToastUtils;

import java.io.File;


public class SettingsSoftUpgradeActivity extends BaseActionBarActivity implements View.OnClickListener {


    private LinearLayout ll_checking;
    private ImageView iv_progress;
    private LinearLayout ll_finish;
    private TextView tv_version;
    private TextView tv_alert;
    private TextView tv_dowload;
    private AnimationDrawable sendingAnimation;
    private LinearLayout ll_progress;
    private ProgressBar pbar_progress;
    private ImageView iv_cancel;
    private TextView tv_loading;
    private boolean hasDownloaded = false;
    //    private static final int HAS_NEW_VERSION = 0;
    //    private static final int NO_NEW_VERSION = 1;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_settings_soft_update;
    }

    @Override
    protected void initWidget() {
        UctClientApi.registerObserver(upgradeResponse, UpgradeResponse.UPGRADERESPONSE_INDEX);
        ll_checking = (LinearLayout) findViewById(R.id.ll_checking);
        iv_progress = (ImageView) findViewById(R.id.iv_progress);
        ll_finish = (LinearLayout) findViewById(R.id.ll_finish);
        tv_version = (TextView) findViewById(R.id.tv_version);
        tv_alert = (TextView) findViewById(R.id.tv_alert);
        tv_dowload = (TextView) findViewById(R.id.tv_dowload);
        ll_progress = (LinearLayout) findViewById(R.id.ll_progress);
        pbar_progress = (ProgressBar) findViewById(R.id.pbar_progress);
        iv_cancel = (ImageView) findViewById(R.id.iv_cancel);
        tv_loading = (TextView) findViewById(R.id.tv_loading);
        tv_actionBarTitle.setText(getResources().getString(R.string.string_settings_about_check_title));
        tv_dowload.setOnClickListener(this);
        iv_cancel.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        boolean isAutoUpgrade = getIntent().getBooleanExtra("auto_upgrade", false);
        if (isAutoUpgrade) {
            stopCheckVersionAnimation();
            tv_alert.setText(getResources().getString(R.string.string_settings_about_prompt2));
            String newVersionId = getIntent().getStringExtra("new_version_id");
            tv_version.setText(newVersionId);
            startDownloadApk();
        } else {
            startAnimation();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean isSuccessed = UctClientApi.isHaveNewVersion(
                            AppContext.getAppContext().getLoginIp(),
                            AppContext.getAppContext().getLoginNumber(),
                            SettingsConstant.VERSION_THREEPROOFING,
                            AppUtils.getVersionName(UctApplication.getInstance()),
                            SettingsSoftUpgradeActivity.this);
                    if (!isSuccessed) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopCheckVersionAnimation();
                                ToastUtils.getToast().showMessageShort(SettingsSoftUpgradeActivity.this, getResources().getString(R.string.string_settings_about_prompt7), -1);
                                finish();
                            }
                        });
                    }
                }
            }).start();
        }
    }

    private void startAnimation() {
        sendingAnimation = (AnimationDrawable) iv_progress.getBackground();
        if (sendingAnimation != null && !sendingAnimation.isRunning()) {
            sendingAnimation.start();
        }
    }

    private void stopCheckVersionAnimation() {
        stopAnimation();
        ll_checking.setVisibility(View.GONE);
        ll_finish.setVisibility(View.VISIBLE);
        tv_version.setText(AppUtils.getVersionName(UctApplication.getInstance()));
    }

    private void stopAnimation() {
        if (sendingAnimation != null && sendingAnimation.isRunning()) {
            sendingAnimation.selectDrawable(0);
            sendingAnimation.stop();
        }
    }

    private UpgradeResponse upgradeResponse = new UpgradeResponse() {
        @Override
        public void checkNewVersionCfm(final int result, final NewVersionBean newVersionBean) {
            PrintLog.i("checkNewVersionCfm result=" + result + ", versionID=" + newVersionBean.getVersionNotify().versionID);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopCheckVersionAnimation();
                    if (result == UpgradeResponse.HAVE_NEW_VERSION) {// 有新版本
                        tv_alert.setText(getResources().getString(R.string.string_settings_about_prompt2));
                        tv_dowload.setVisibility(View.VISIBLE);
                        tv_version.setText(newVersionBean.getVersionNotify().versionID);
                    } else if (result == UpgradeResponse.SD_CARD_MOUNT) {// SD卡已挂起
                        tv_alert.setText(getResources().getString(R.string.string_settings_about_prompt3));
                        tv_dowload.setVisibility(View.GONE);
                    } else if (result == UpgradeResponse.SD_FREE_SIZE) {// SD卡空间不足100M
                        tv_alert.setText(getResources().getString(R.string.string_settings_about_prompt4));
                        tv_dowload.setVisibility(View.GONE);
                    } else if (result == UpgradeResponse.CHECK_VERSION_FAIL) {// 检测新版本失败
                        tv_alert.setText(getResources().getString(R.string.string_settings_about_prompt5));
                        tv_dowload.setVisibility(View.GONE);
                    } else if (result == UpgradeResponse.ALREADY_NEW_VERSION) {// 已经是最新版本
                        tv_alert.setText(getResources().getString(R.string.string_settings_about_prompt1));
                        tv_dowload.setVisibility(View.GONE);
                    }
                }
            });
        }

        @Override
        public void apkDownloadListener(final int result, final long fileSize, final long offSet, final NewVersionBean newVersionBean) {
            PrintLog.i("apkDownloadListener result=" + result + ", fileSize=" + fileSize + ", offSet=" + offSet + ", versionID=" + newVersionBean.getVersionNotify().versionID);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (result == UpgradeResponse.SD_CARD_MOUNT) {// SD卡已挂起
                        tv_alert.setText(getResources().getString(R.string.string_settings_about_prompt3));
                        tv_dowload.setVisibility(View.GONE);
                    } else if (result == UpgradeResponse.SD_FREE_SIZE) {// SD卡空间不足100M
                        tv_alert.setText(getResources().getString(R.string.string_settings_about_prompt4));
                        tv_dowload.setVisibility(View.GONE);
                    } else if (result == UpgradeResponse.APK_DOWNLOAD_FAIL) {// App下载失败
                        tv_alert.setText(getResources().getString(R.string.string_settings_about_prompt6));
                        tv_dowload.setVisibility(View.VISIBLE);
                        pbar_progress.setProgress(0);
                        tv_loading.setText(String.format(getResources().getString(R.string.string_settings_about_loading), 0));
                        ll_progress.setVisibility(View.GONE);
                        tv_loading.setVisibility(View.GONE);
                    } else if (result == UpgradeResponse.NOT_SUPPORT) {// 不支持版本回退
                        hasDownloaded = true;
                        tv_alert.setText(getResources().getString(R.string.string_settings_about_prompt9));
                        tv_dowload.setVisibility(View.GONE);
                        pbar_progress.setProgress(0);
                        tv_loading.setText(String.format(getResources().getString(R.string.string_settings_about_loading), 0));
                        ll_progress.setVisibility(View.GONE);
                        tv_loading.setVisibility(View.GONE);
                    } else if (result == UpgradeResponse.APK_DOWNLOADING) {// APK下载更新进度
                        int progress = FileUtils.getProgress(offSet, fileSize);
                        pbar_progress.setProgress(progress);
                        tv_loading.setText(String.format(getResources().getString(R.string.string_settings_about_loading), progress));
                    } else if (result == UpgradeResponse.DOWNLOADING_FINISH) {// 下载完成
                        hasDownloaded = true;
                        tv_dowload.setVisibility(View.GONE);
                        pbar_progress.setProgress(0);
                        tv_loading.setText(String.format(getResources().getString(R.string.string_settings_about_loading), 0));
                        ll_progress.setVisibility(View.GONE);
                        tv_loading.setVisibility(View.GONE);
                        tv_alert.setText(getResources().getString(R.string.string_settings_about_prompt8));
                        tv_version.setText(newVersionBean.getVersionNotify().versionID);
                        // 安装APK
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(SDCardUtils.getUpgradePath() + File.separator + newVersionBean.getVersionNotify().versionID + ".apk")), "application/vnd.android.package-archive");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_dowload:
                startDownloadApk();
                break;
            case R.id.iv_cancel:
                tv_dowload.setVisibility(View.VISIBLE);
                pbar_progress.setProgress(0);
                tv_loading.setText(String.format(getResources().getString(R.string.string_settings_about_loading), 0));
                ll_progress.setVisibility(View.GONE);
                tv_loading.setVisibility(View.GONE);
                boolean isCancelSuccess = UctClientApi.cancelDownload();
                if (isCancelSuccess) {
                    PrintLog.i("取消下载成功");
                } else {
                    PrintLog.e("取消下载失败");
                }
                break;
        }
    }

    private void startDownloadApk() {
        UctClientApi.downloadApk(SDCardUtils.getUpgradePath());
        tv_dowload.setVisibility(View.GONE);
        if (!hasDownloaded) {
            ll_progress.setVisibility(View.VISIBLE);
            tv_loading.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        stopAnimation();
        UctClientApi.unregisterObserver(upgradeResponse, UpgradeResponse.UPGRADERESPONSE_INDEX);
        super.onDestroy();
    }
}
