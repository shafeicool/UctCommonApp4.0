package com.ptyt.uct.callback;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.android.uct.bean.NewVersionBean;
import com.android.uct.service.AppUpgrade.UpgradeResponse;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.SettingsSoftUpgradeActivity;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.AppManager;
import com.ptyt.uct.common.SettingsConstant;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.widget.CommonPromptDialog;

/**
 * Title: com.ptyt.uct.callback
 * Description:
 * Date: 2017/10/11
 * Author: ShaFei
 * Version: V1.0
 */

public class SoftUpgradeCallBack extends BaseCallBack {

    private static SoftUpgradeCallBack instance = null;
    private Context mContext;
    private boolean isShowing = false;

    public static synchronized SoftUpgradeCallBack getSoftUpgradeCallBack() {
        if (instance == null) {
            instance = new SoftUpgradeCallBack();
        }
        return instance;
    }

    @Override
    public void init(Context context) {
        mContext = context;
        PrintLog.w("注册SoftUpgradeCallBack");
        UctClientApi.registerObserver(upgradeResponse, UpgradeResponse.UPGRADERESPONSE_INDEX);
    }

    @Override
    public void release() {
        PrintLog.w("反注册SoftUpgradeCallBack");
        UctClientApi.unregisterObserver(upgradeResponse, UpgradeResponse.UPGRADERESPONSE_INDEX);
    }

    public void checkNewVersion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isSuccessed = UctClientApi.isHaveNewVersion(
                        AppContext.getAppContext().getLoginIp(),
                        AppContext.getAppContext().getLoginNumber(),
                        SettingsConstant.VERSION_THREEPROOFING,
                        AppUtils.getVersionName(mContext),
                        mContext);
                if (!isSuccessed) {
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_settings_about_prompt7), -1);
                }
            }
        }).start();
    }

    private UpgradeResponse upgradeResponse = new UpgradeResponse() {
        @Override
        public void checkNewVersionCfm(int result, NewVersionBean newVersionBean) {
            String actClsName = AppManager.getAppManager().currentActivity().getComponentName().getClassName();
            // 防止跟SettingsSoftUpdateActivity的回调冲突
            if (!StrUtils.isEmpty(actClsName) && actClsName.equals(SettingsSoftUpgradeActivity.class.getName())) {
                return;
            }
            PrintLog.i("checkNewVersionCfm result=" + result + ", versionID=" + newVersionBean.getVersionNotify().versionID);
            if (result == UpgradeResponse.HAVE_NEW_VERSION) {// 有新版本
                showUpgradeDialog(AppManager.getAppManager().currentActivity(), newVersionBean.getVersionNotify().versionID);
            } else if (result == UpgradeResponse.ALREADY_NEW_VERSION) {// 已经是最新版本

            } else {
                ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_settings_about_prompt5), -1);
            }
        }

        @Override
        public void apkDownloadListener(int result, long fileSize, long offSet, NewVersionBean newVersionBean) {
            PrintLog.i("apkDownloadListener result=" + result + ", fileSize=" + fileSize + ", offSet=" + offSet);
        }
    };

    private void showUpgradeDialog(final Context context, final String newVersionId) {
        if (isShowing) {
            return;
        }
        isShowing = true;
        //        CommonPromptDialog.Builder builder = CommonPromptDialog.getInstance(context);
        CommonPromptDialog.Builder builder = new CommonPromptDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.dialog_prompt_title2));
        builder.setMessage(context.getResources().getString(R.string.dialog_prompt_content4));
        builder.setPositiveButton(context.getResources().getString(R.string.dialog_prompt_confim2), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                isShowing = false;
                Intent intent = new Intent(context, SettingsSoftUpgradeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("auto_upgrade", true);
                intent.putExtra("new_version_id", newVersionId);
                context.startActivity(intent);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(context.getResources().getString(R.string.dialog_prompt_cancel2), new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                isShowing = false;
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
