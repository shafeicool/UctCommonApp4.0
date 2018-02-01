package com.ptyt.uct.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.entity.LoginUserEntity;
import com.ptyt.uct.utils.ActivitySkipUtils;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.KeyBoardUtils;
import com.ptyt.uct.utils.NetUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.viewinterface.ILoginView;

/**
 * @Description: 依靠用户名密码登录界面，包含记住密码和自动登录功能
 * @Date: 2017/4/24
 * @Author: ShaFei
 * @Version: V1.0
 */
public class LoginActivity extends BaseActivity implements
        OnClickListener,
        ILoginView {

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mIpView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mSignInButton;
    private static final int LOGIN = 0;
    private static final int LOGOUT = 1;
    private static final int SAVE_CONFIG = 2;
    private static final int LOAD_CONFIG = 3;
    private String strUsername = "";
    private String strPassword = "";
    private String strIp = "";
    private LoginUserEntity loginUserBean;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_login);
        setNewMyLooper(true);

        mUsernameView = (EditText) findViewById(R.id.username_login_et);
        mPasswordView = (EditText) findViewById(R.id.password_login_et);
        mIpView = (EditText) findViewById(R.id.ip_login_et);
        mSignInButton = (Button) findViewById(R.id.signin_login_btn);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.progress_login_pbar);
        mSignInButton.setOnClickListener(this);
//        mIpView.addTextChangedListener(this);
        mIpView.setFilters(new InputFilter[]{new MyFilter()});
    }

    @Override
    protected void initData() {
        loginUserBean = LoginUserEntity.getUserData();
        sendMsg(true, LOGIN);
    }

    /**
     * @param
     * @return
     * @description 显示登录进度条
     */
    @Override
    public void showProgress() {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

                    mLoginFormView.setVisibility(View.GONE);
                    mLoginFormView.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(View.GONE);
                        }
                    });

                    if (!(mProgressView.getVisibility() == View.VISIBLE)) {
                        mProgressView.setVisibility(View.VISIBLE);
                        mProgressView.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mProgressView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } else {
                    // The ViewPropertyAnimator APIs are not available, so simply show
                    // and hide the relevant UI components.
                    mProgressView.setVisibility(View.VISIBLE);
                    mLoginFormView.setVisibility(View.GONE);
                }
            }
        });

    }

    /**
     * @param
     * @return
     * @description 隐藏登录进度条
     */
    @Override
    public void hideProgress() {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

                    mLoginFormView.setVisibility(View.VISIBLE);
                    mLoginFormView.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(View.VISIBLE);
                        }
                    });

                    mProgressView.setVisibility(View.GONE);
                    mProgressView.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mProgressView.setVisibility(View.GONE);
                        }
                    });
                } else {
                    // The ViewPropertyAnimator APIs are not available, so simply show
                    // and hide the relevant UI components.
                    mProgressView.setVisibility(View.GONE);
                    mLoginFormView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * @param
     * @return
     * @description 发起登录请求
     */
    private void attemptLogin() {
        // 防止快速点击
        if (AppUtils.isFastClick()) {
            return;
        }

        // 检查网络状态
        if (!NetUtils.isNetworkAvailable(UctApplication.getInstance())) {
            ToastUtils.getToast().showMessageShort(LoginActivity.this, getString(R.string.dialog_prompt_content2), -1);
            return;
        }

        // 重置错误
        mUsernameView.setError(null);
        mPasswordView.setError(null);
        mIpView.setError(null);

        if (StrUtils.isEmpty(strUsername)) {
            mUsernameView.setError(getString(R.string.error_null_username));
            mUsernameView.requestFocus();
            return;
        }

        if (StrUtils.isEmpty(strPassword)) {
            mPasswordView.setError(getString(R.string.error_null_password));
            mPasswordView.requestFocus();
            return;
        }

        if (StrUtils.isEmpty(strIp)) {
            mIpView.setError(getString(R.string.error_null_ip));
            mIpView.requestFocus();
            return;
        } else if (!StrUtils.isIPAddress(strIp)) {
            mIpView.setError(getString(R.string.error_invalid_ip));
            mIpView.requestFocus();
            return;
        }

        // 隐藏键盘
        KeyBoardUtils.closeKeybord(mContext);
        showProgress();
        AppContext.getAppContext().setLoginNumber(strUsername);
        AppContext.getAppContext().setLoginPassword(strPassword);
        AppContext.getAppContext().setLoginIp(strIp);
        int result = UctClientApi.uctLoginRequest(strUsername, strPassword, strIp, 0);
        if (result == -1) {
            ToastUtils.getToast().showMessageShort(LoginActivity.this, getString(R.string.string_login_request_fail) + result, -1);
            PrintLog.e("登录请求失败，result = " + result + ", strUsername = " + strUsername + ", strPassword = " + strPassword + ", strIp = " + strIp);
            hideProgress();
        }
    }

    private class MyFilter implements InputFilter {
        public MyFilter() {
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            mIpView.setError(null);
            //匹配IP
            if (TextUtils.isEmpty(source)) {
                return "";
            }
            StringBuffer stringBuffer = new StringBuffer(dest);
            String currentStr = stringBuffer.insert(dend, source).toString();
            if (!currentStr.contains(".")) {
                //如果不是纯数字不能输入
                if (!StrUtils.isMatchs(currentStr)) {
                    mIpView.setError(getString(R.string.error_invalid_ip));
                    return "";
                }
                int value = Integer.parseInt(currentStr);
                if (value > 255) {
                    mIpView.setError(getString(R.string.error_invalid_ip));
                    return "";
                }
            } else {
                if (!isMatch(currentStr, '.')) {
                    mIpView.setError(getString(R.string.error_invalid_ip));
                    return "";
                }
            }

            String newStr = currentStr.replace(".", "#");
            String sbStr[] = newStr.split("#");
            for (int i = 0; i < sbStr.length; i++) {
                String chatAt = sbStr[i];
                //如果不是纯数字不能输入
                if (!StrUtils.isMatchs(chatAt)) {
                    mIpView.setError(getString(R.string.error_invalid_ip));
                    return "";
                }
                int value = Integer.parseInt(chatAt);
                if (value > 255) {
                    mIpView.setError(getString(R.string.error_invalid_ip));
                    return "";
                }
                if ((value + "").indexOf("0") == 0) {
                    mIpView.setError(getString(R.string.error_invalid_ip));
                    return "";
                }
            }
            return source;
        }
    }

    private boolean isMatch(String totalString, char a) {
        int strLen = totalString.length();
        int count = 0;
        for (int i = 0; i < strLen; i++) {
            char c = totalString.charAt(i);
            if (c == a) {
                count++;
                if ((i + 1) < strLen) {
                    char c1 = totalString.charAt(i + 1);
                    if (c1 == a) {
                        return false;
                    }
                }
            }
        }
        if (count > 3) {
            return false;
        }
        return true;
    }

    @Override
    public boolean handleMessage(Message message) {
        super.handleMessage(message);
        switch (message.what) {
            case LOGIN:
                strUsername = loginUserBean.getUsername(); // 用户名
                strPassword = loginUserBean.getPassword(); // 密码
                strIp = loginUserBean.getIP(); // IP地址
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /* ptyt begin, 防止未登录就直接提示用户错误信息_4046_shafei_20170906 */
                        if (StrUtils.isEmpty(strUsername)) {
                            // 重置错误
                            mUsernameView.setError(null);
                            return;
                        } else {
                            mUsernameView.setText(strUsername);
                        }
                        if (StrUtils.isEmpty(strIp)) {
                            mIpView.setError(null);
                            return;
                        } else {
                            mIpView.setText(strIp);
                        }
                        if (StrUtils.isEmpty(strPassword)) {
                            mPasswordView.setError(null);
                            return;
                        } else {
                            mPasswordView.setText(strPassword);
                        }
                        /* ptyt end */
                        attemptLogin();
                    }
                });

                break;
            case LOGOUT:
                break;
            // 保存数据到xml
            case SAVE_CONFIG:
                loginUserBean.setUsername(strUsername);
                loginUserBean.setPassword(strPassword);
                loginUserBean.setIP(strIp);
                break;
            // 从xml中取出登录数据
            case LOAD_CONFIG:
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signin_login_btn:
                strUsername = mUsernameView.getText().toString();
                strPassword = mPasswordView.getText().toString();
                strIp = mIpView.getText().toString();
                attemptLogin();
                sendMsg(true, SAVE_CONFIG);
                break;
            default:
                break;
        }
    }

//    @Override
//    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//    }
//
//    @Override
//    public void onTextChanged(CharSequence s, int start, int before, int count) {
//        String ipText = mIpView.getText().toString();
//        if (!StrUtils.isEmpty(ipText)) {
//            String[] ipNumber = ipText.split("\\.");
//        }
//    }
//
//    @Override
//    public void afterTextChanged(Editable s) {
//
//    }

    @Override
    public void skipToMain() {
        ActivitySkipUtils.toNextActivityAndFinish(this, MainActivity.class);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mProgressView.getVisibility() == View.VISIBLE) {
                PrintLog.i("cancelLogin");
                hideProgress();
                UctClientApi.cancelLogin();
            } else {
                PrintLog.i("moveTaskToBack");
                moveTaskToBack(true);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        PrintLog.i("onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        PrintLog.i("onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        PrintLog.i("onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        PrintLog.i("onDestroy");
        super.onDestroy();
    }

}

