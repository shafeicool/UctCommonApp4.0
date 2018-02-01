package com.ptyt.uct.callback;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import com.android.uct.IUCTLoginListener;
import com.android.uct.exception.UctLibException;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.LoginActivity;
import com.ptyt.uct.activity.MainActivity;
import com.ptyt.uct.activity.SplashActivity;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.AppManager;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.utils.ActivitySkipUtils;
import com.ptyt.uct.utils.AssertUtils;
import com.ptyt.uct.utils.BitmapUtil;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.viewinterface.ILoginView;
import com.ptyt.uct.viewinterface.IMainView;
import com.ptyt.uct.widget.CommonPromptDialog;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * Title: com.ptyt.uct.common
 * Description:
 * Date: 2017/5/8
 * Author: ShaFei
 * Version: V1.0
 */

public class LoginCallBack extends BaseCallBack {
    private static LoginCallBack instance = null;
    private Context mContext = null;
    private ILoginView iLoginView = null;
    private IMainView iMainView = null;
    private boolean isRunOnUI = false;
    private boolean isShowing = false;
    private String dialogMessageCache;
    private CommonPromptDialog.Builder builder;
    public String loginMessage = "";
    private Activity tempActivity;

    public static synchronized LoginCallBack getLoginCallBack() {
        if (instance == null) {
            instance = new LoginCallBack();
        }
        return instance;
    }

    @Override
    public void init(Context context) {
        mContext = context;
        PrintLog.i("注册LoginCallBack");
        // 登录回调
        UctClientApi.registerObserver(loginListener, IUCTLoginListener.IUCTLOGINLISTENER_INDEX);
        EventBus.getDefault().register(this);
        // 初始化ImageLoader
        initImageLoader(mContext);
    }

    @Override
    public void release() {
        PrintLog.i("反注册LoginCallBack");
        UctClientApi.unregisterObserver(loginListener, IUCTLoginListener.IUCTLOGINLISTENER_INDEX);
        EventBus.getDefault().unregister(this);
        iLoginView = null;
        iMainView = null;
    }

    /**
     * @description 登录回调结果显示
     * @param
     * @return
     */
    private IUCTLoginListener loginListener = new IUCTLoginListener() {

        @Override
        public int UCT_LoginCfm(int ret, String _telno, long svcTime,
                                int uiGpsIpAddr, int usGpsPort, int uiOdm, String versionId,
                                String nodeDn) throws UctLibException {
            PrintLog.i("UCT_LoginCfm [ret=" + ret + ", _telno=" + _telno + ", svcTime=" + svcTime
                    + ", uiGpsIpAddr=" + uiGpsIpAddr + ", usGpsPort=" + usGpsPort
                    + ", uiOdm=" + uiOdm + ", versionId=" + versionId + ", nodeDn="
                    + nodeDn + "]");
            String actClsName = AppManager.getAppManager().currentActivity().getComponentName().getClassName();
            isRunOnUI = false;
            if (!StrUtils.isEmpty(actClsName)) {
                if (actClsName.equals(LoginActivity.class.getName())) {
                    iLoginView = (ILoginView) AppManager.getAppManager().currentActivity();
                    isRunOnUI = true;
                } else if (actClsName.equals(MainActivity.class.getName())) {
                    iMainView = (IMainView) AppManager.getAppManager().currentActivity();
                    isRunOnUI = true;
                }
            }

            switch (ret) {
                //===============================start 开发者只考虑下面几种返回值=================================
                // 0表示登录成功
                case IUCTLoginListener.LOGIN_SUCCESS:
                    PrintLog.i("登录成功");
                    AppContext.getAppContext().setCurrentNodeDn(nodeDn);
                    if (isRunOnUI && (iLoginView != null)) {
                        iLoginView.skipToMain();
                    }
                    if (isRunOnUI && (iMainView != null)) {
                        iMainView.setPtytLoginStatus("");
                    }
                    loginMessage = "";
                    break;

                case IUCTLoginListener.USER_DO_NOT_EXIST:
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_login_error1), -1);
                    if (isRunOnUI && (iMainView != null)) {
                        iMainView.setPtytLoginStatus(mContext.getString(R.string.string_login_error1));
                    }
                    loginMessage = mContext.getString(R.string.string_login_error1);
                    break;

                case IUCTLoginListener.LOGIN_PASSWORD_ERROR:
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_login_error2), -1);
                    if (isRunOnUI && (iMainView != null)) {
                        iMainView.setPtytLoginStatus(mContext.getString(R.string.string_login_error2));
                    }
                    loginMessage = mContext.getString(R.string.string_login_error2);
                    break;

                case IUCTLoginListener.LOGIN_NETWORK_FAULT:
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_login_error3), -1);
                    if (isRunOnUI && (iMainView != null)) {
                        iMainView.setPtytLoginStatus(mContext.getString(R.string.string_login_error3));
                    }
                    loginMessage = mContext.getString(R.string.string_login_error3);
                    break;

                case IUCTLoginListener.LOGIN_TERMINAL_TYPE_ERROR:
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_login_error4), -1);
                    if (isRunOnUI && (iMainView != null)) {
                        iMainView.setPtytLoginStatus(mContext.getString(R.string.string_login_error4));
                    }
                    loginMessage = mContext.getString(R.string.string_login_error4);
                    break;
                //====================================end=========================================
                //以下返回值UCP暂时还未使用 不处理 开发过程中只处理以上返回值
                case IUCTLoginListener.LOGIN_NO_ROAMING_PRIVILEGES:
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_login_error5), -1);
                    if (isRunOnUI && (iMainView != null)) {
                        iMainView.setPtytLoginStatus(mContext.getString(R.string.string_login_error5));
                    }
                    loginMessage = mContext.getString(R.string.string_login_error5);
                    break;

                case IUCTLoginListener.LOGIN_AUTHENTICATION_FAILURE1:
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_login_error6), -1);
                    if (isRunOnUI && (iMainView != null)) {
                        iMainView.setPtytLoginStatus(mContext.getString(R.string.string_login_error6));
                    }
                    loginMessage = mContext.getString(R.string.string_login_error6);
                    break;

                case IUCTLoginListener.LOGIN_USER_THEFT:
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_login_error7), -1);
                    if (isRunOnUI && (iMainView != null)) {
                        iMainView.setPtytLoginStatus(mContext.getString(R.string.string_login_error7));
                    }
                    loginMessage = mContext.getString(R.string.string_login_error7);
                    break;
                // 用户异地登录(返回登录页面)
                case IUCTLoginListener.LOGIN_USER_COPIED:
                    //                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_login_error8), -1);
                    if (builder != null) {
                        if (tempActivity != null && !tempActivity.isFinishing()) {
                            builder.dismiss();
                        }
                        builder = null;
                    }
                    tempActivity = AppManager.getAppManager().currentActivity();
                    builder = new CommonPromptDialog.Builder(tempActivity);
                    //                    showReloginDialog(new CommonPromptDialog.Builder(AppManager.getAppManager().currentActivity()), AppManager.getAppManager().currentActivity(), mContext.getResources().getString(R.string.dialog_prompt_content1));
                    PrintLog.i("您的账号在另一台设备上登录");
                    showReloginDialog(builder, tempActivity, mContext.getResources().getString(R.string.dialog_prompt_content1));
                    break;

                case IUCTLoginListener.LOGIN_INSERT_FAIL:
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_login_error9), -1);
                    if (isRunOnUI && (iMainView != null)) {
                        iMainView.setPtytLoginStatus(mContext.getString(R.string.string_login_error9));
                    }
                    loginMessage = mContext.getString(R.string.string_login_error9);
                    break;

                case IUCTLoginListener.LOGIN_DEVICE_AUTHENTICATION_FAILED:
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_login_error10), -1);
                    if (isRunOnUI && (iMainView != null)) {
                        iMainView.setPtytLoginStatus(mContext.getString(R.string.string_login_error10));
                    }
                    loginMessage = mContext.getString(R.string.string_login_error10);
                    break;
                case IUCTLoginListener.LOGIN_IP_ADDRESS_MISMATCH:
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_login_error11), -1);
                    if (isRunOnUI && (iMainView != null)) {
                        iMainView.setPtytLoginStatus(mContext.getString(R.string.string_login_error11));
                    }
                    loginMessage = mContext.getString(R.string.string_login_error11);
                    break;
                case IUCTLoginListener.LOGIN_UNKNOWN_USER:
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_login_error12), -1);
                    if (isRunOnUI && (iMainView != null)) {
                        iMainView.setPtytLoginStatus(mContext.getString(R.string.string_login_error12));
                    }
                    loginMessage = mContext.getString(R.string.string_login_error12);
                    break;

                default:
                    AssertUtils.isSwitchDefault("UCT_LoginCfm Result= " + ret);
                    if (isRunOnUI && (iMainView != null)) {
                        iMainView.setPtytLoginStatus(mContext.getString(R.string.string_login_error13) + ret);
                    }
                    loginMessage = mContext.getString(R.string.string_login_error13);
                    break;
            }
            if (isRunOnUI && (iLoginView != null)) {
                iLoginView.hideProgress();
            }

            iLoginView = null;
            iMainView = null;
            return 0;
        }
    };

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEvent(EventBean eventBean) {
        if (eventBean.getAction().equals(ConstantUtils.ACTION_NETWORK_CHANGED)) {
            String actClsName = AppManager.getAppManager().currentActivity().getComponentName().getClassName();
            if (eventBean.isNetworkAvailable(UctApplication.getInstance())) {
                if (builder != null) {
                    if (tempActivity != null && !tempActivity.isFinishing()) {
                        builder.dismiss();
                    }
                    builder = null;
                }
                isShowing = false;
                PrintLog.i("网络好了，重新登录啦");
                boolean isUserOnline = UctClientApi.isUserOnline();
                if (isUserOnline) {// 防止多次广播登录
                    PrintLog.i("用户在线 return");
                    return;
                }
                if (!StrUtils.isEmpty(actClsName)) {
                    if (actClsName.equals(LoginActivity.class.getName()) || actClsName.equals(SplashActivity.class.getName())) {
                        PrintLog.i("在" + actClsName + "，不登陆 return");
                        return;
                    }
                }
                String strUsername = AppContext.getAppContext().getLoginNumber();
                String strPassword = AppContext.getAppContext().getLoginPassword();
                String strIp = AppContext.getAppContext().getLoginIp();
                int result = UctClientApi.uctLoginRequest(strUsername, strPassword, strIp, 0);
                if (result == -1) {
                    PrintLog.e("登录请求失败啦 result = " + result);
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_login_request_fail) + result, -1);
                    ActivitySkipUtils.toNextActivityAndFinish(AppManager.getAppManager().currentActivity(), LoginActivity.class);
                } else {
                    PrintLog.i("登录请求成功啦 result = " + result);
                }
            } else {
                boolean isUserOnline = UctClientApi.isUserOnline();
                if (!isUserOnline) {// 防止多次广播弹窗
                    PrintLog.i("用户不在线 return");
                    return;
                }
                if (!StrUtils.isEmpty(actClsName)) {
                    if (actClsName.equals(LoginActivity.class.getName()) || actClsName.equals(SplashActivity.class.getName())) {
                        PrintLog.i("在" + actClsName + "，不弹框 return");
                        return;
                    }
                }
                if (builder != null) {
                    if (tempActivity != null && !tempActivity.isFinishing()) {
                        builder.dismiss();
                    }
                    builder = null;
                }
                isShowing = false;
                tempActivity = AppManager.getAppManager().currentActivity();
                builder = new CommonPromptDialog.Builder(tempActivity);
                showReloginDialog(builder, tempActivity, mContext.getString(R.string.dialog_prompt_content2));
                PrintLog.i("网络不能用，弹框啦");
            }
        }
    }

    private void showReloginDialog(CommonPromptDialog.Builder builder, final Activity activity, final String message) {
        PrintLog.i("message = " + message);
        String actClsName = activity.getComponentName().getClassName();
        isRunOnUI = false;
        if (!StrUtils.isEmpty(actClsName)) {
            PrintLog.i("actClsName = " + actClsName);
            if (actClsName.equals(MainActivity.class.getName())) {
                iMainView = (IMainView) AppManager.getAppManager().currentActivity();
                isRunOnUI = true;
            }
        }
        PrintLog.i("isShowing = " + isShowing);
        if (isShowing) {// 防止多次弹出
            // 如果Dialog显示用户被占用，但是网络又断了，则状态显示网络异常，不显示被占用begin
            if (message.equals(dialogMessageCache)) {
                PrintLog.i("return1 网络异常和账户被占用同时显示时，只显示网络异常");
                return;
            }
            dialogMessageCache = message;

            if (isRunOnUI && (iMainView != null)) {
                iMainView.setPtytLoginStatus(message);
                iMainView = null;
            }
            loginMessage = message;
            // end
            PrintLog.i("return2 同样的原因多次发送，只弹一次框");
            return;
        }
        isShowing = true;
        dialogMessageCache = message;
        UctClientApi.uctLogOut(0);// 登出，停止发包
        if (isRunOnUI && (iMainView != null)) {
            iMainView.setPtytLoginStatus(message);
            iMainView = null;
        }
        loginMessage = message;
        if (activity.isFinishing()) {
            isShowing = false;
            PrintLog.i("return3 如果Dialog依附的页面finish，则Dialog也不必再弹");
            return;
        }
        builder.setTitle(activity.getResources().getString(R.string.dialog_prompt_title1));
        builder.setMessage(message);
        builder.setPositiveButton(activity.getResources().getString(R.string.dialog_prompt_confim1), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                PrintLog.i("弹框 点击了重新登录");
                isShowing = false;
                dialog.dismiss();
                String strUsername = AppContext.getAppContext().getLoginNumber();
                String strPassword = AppContext.getAppContext().getLoginPassword();
                String strIp = AppContext.getAppContext().getLoginIp();
                int result = UctClientApi.uctLoginRequest(strUsername, strPassword, strIp, 0);
                if (result == -1) {
                    ToastUtils.getToast().showMessageShort(activity, activity.getString(R.string.string_login_request_fail) + result, -1);
                    ActivitySkipUtils.toNextActivityAndFinish(activity, LoginActivity.class);
                    PrintLog.e("登录请求失败啦 result = " + result);
                } else {
                    PrintLog.i("登录请求成功啦 result = " + result);
                }
            }
        });

        builder.setNegativeButton(activity.getResources().getString(R.string.dialog_prompt_cancel1), new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                PrintLog.i("弹框 点击了知道了");
                isShowing = false;
                dialog.dismiss();
                AppManager.getAppManager().AppLogOut(AppManager.getAppManager().currentActivity());
            }
        });
        builder.create().show();
    }

    private void initImageLoader(Context context) {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(
                context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);// 线程池中线程的个数
        config.denyCacheImageMultipleSizesInMemory();// 拒绝缓存多个图片
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());// 将保存的时候的URI名称用MD5
        // 加密
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);// 设置图片下载和显示的工作队列排序
        // config.writeDebugLogs(); // 打开调试日志,删除不显示日志
        config.defaultDisplayImageOptions(BitmapUtil.defaultOptions);// 显示图片的参数
        // config.diskCache(new UnlimitedDiskCache(cacheDir));//自定义缓存路径
        ImageLoader.getInstance().init(config.build());
    }

}
