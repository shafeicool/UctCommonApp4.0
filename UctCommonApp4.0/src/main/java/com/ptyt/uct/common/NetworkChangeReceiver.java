package com.ptyt.uct.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.utils.ConstantUtils;

import de.greenrobot.event.EventBus;

public class NetworkChangeReceiver extends BroadcastReceiver {

//    private CommonPromptDialog.Builder builder = new CommonPromptDialog.Builder(AppManager.getAppManager().currentActivity());

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
//            if (!NetUtils.isNetworkAvailable(context)) {
//                builder = new CommonPromptDialog.Builder(AppManager.getAppManager().currentActivity());
//                LoginCallBack.getLoginCallBack().showReloginDialog(builder, AppManager.getAppManager().currentActivity(), context.getResources().getString(R.string.dialog_prompt_content2));
//                PrintLog.e("网络不能用，弹框啦");
//            } else {
//                LoginCallBack.getLoginCallBack().isShowing = false;
//                /* ptyt begin, 创建dialog跟关闭dialog的对象要相同_4469_shafei_20171025 */
//                builder.dismiss();
//                /* ptyt end */
//                boolean isUserOnline = UctClientApi.isUserOnline();
//                if (isUserOnline) {
//                    return;
//                }
//                String strUsername = AppContext.getAppContext().getLoginNumber();
//                String strPassword = AppContext.getAppContext().getLoginPassword();
//                String strIp = AppContext.getAppContext().getLoginIp();
//                PrintLog.e("网络好了，重新登录啦");
//                int result = UctClientApi.uctLoginRequest(strUsername, strPassword, strIp, 0);
//                if (result == -1) {
//                    ToastUtils.getToast().showMessageShort(context, context.getResources().getString(R.string.string_login_request_fail) + result, -1);
//                    ActivitySkipUtils.toNextActivityAndFinish(context, LoginActivity.class);
//                    PrintLog.e("登录请求失败啦 result = " + result);
//                } else {
//                    PrintLog.e("登录请求成功啦 result = " + result);
//                }
//            }
            EventBus.getDefault().post(new EventBean(ConstantUtils.ACTION_NETWORK_CHANGED));
        }
    }
}
