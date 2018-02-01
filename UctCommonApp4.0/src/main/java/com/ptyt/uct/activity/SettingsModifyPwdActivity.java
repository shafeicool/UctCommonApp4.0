package com.ptyt.uct.activity;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.uct.ModifyPwdCfmListener;
import com.android.uct.exception.UctLibException;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.AppManager;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;


public class SettingsModifyPwdActivity extends BaseActionBarActivity implements View.OnClickListener {

    private EditText et_old;
    private EditText et_new;
    private TextView tv_confirm;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_settings_modify_pwd;
    }

    @Override
    protected void initWidget() {
        UctClientApi.registerObserver(modifyPwdCfmListener, ModifyPwdCfmListener.MODIFYPWDCFMLISTENER_INDEX);
        et_old = (EditText) findViewById(R.id.et_old);
        et_new = (EditText) findViewById(R.id.et_new);
        tv_confirm = (TextView) findViewById(R.id.tv_confirm);
        tv_actionBarTitle.setText(getResources().getString(R.string.string_settings_account_modify_password));
        tv_confirm.setOnClickListener(this);
    }

    private ModifyPwdCfmListener modifyPwdCfmListener = new ModifyPwdCfmListener() {

        @Override
        public int UCT_ModifyPwdCfm(int ret, String modifyUser) throws UctLibException {
            PrintLog.i("UCT_ModifyPwdCfm ret=" + ret + ", modifyUser=" + modifyUser);
            if (ret == 0) {
                AppManager.getAppManager().AppLogOut(mContext);
                ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_settings_account_password_success), -1);
            } else {
                ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_settings_account_password_fail), -1);
            }
            return 0;
        }
    };

    /**
     * 密码校验需求：
     * 1) 密码控制只能输入字母、数字、特殊符号(~!@#$%^&*()_+[]{}|\;:'",./<>?)
     * 2) 长度 6-16 位，必须包括字母、数字、特殊符号中的2种
     *
     * @param newPwd
     * @return
     */
    private String checkPwd(String newPwd) {
        //判断密码是否包含数字：包含返回1，不包含返回0
        int i = newPwd.matches(".*\\d+.*") ? 1 : 0;
        // 判断密码是否包含字母：包含返回1，不包含返回0
        int j = newPwd.matches(".*[a-zA-Z]+.*") ? 1 : 0;
        //判断密码是否包含特殊符号(~!@#$%^&*()_+|<>,.?/:;'[]{}\)：包含返回1，不包含返回0
        int k = newPwd.matches(".*[~!@#$%^&*()_+|<>,.?/:;'\\[\\]{}\"]+.*") ? 1 : 0;
        // 判断密码长度是否在6-16位
        int l = newPwd.length();
        if (i + j + k < 2 || l < 6 || l > 16) {
            return "密码规则:\n1.密码保证包括字母、数字、特殊符号中的至少两种 \n2.密码长度 6-16 位";
        }

        return "";
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_confirm:
                // 防止快速点击
                if (AppUtils.isFastClick()) {
                    break;
                }
                et_old.setError(null);
                et_new.setError(null);
                String oldPwd = et_old.getText().toString();
                String newPwd = et_new.getText().toString();
                if (StrUtils.isEmpty(oldPwd)) {
                    et_old.setError(getString(R.string.string_settings_account_password_null));
                    et_old.requestFocus();
                    break;
                }
                if (StrUtils.isEmpty(newPwd)) {
                    et_new.setError(getString(R.string.string_settings_account_password_null));
                    et_new.requestFocus();
                    break;
                }
                if (oldPwd.equals(newPwd)) {
                    et_new.setError(getString(R.string.string_settings_account_password_same));
                    et_new.requestFocus();
                    break;
                }
                if (!oldPwd.equals(AppContext.getAppContext().getLoginPassword())) {
                    et_old.setError(getString(R.string.string_settings_account_password_error));
                    et_old.requestFocus();
                    break;
                }
                String checkPwd = checkPwd(newPwd);
                if (!"".equals(checkPwd)) {
                    ToastUtils.getToast().showMessageShort(this, checkPwd, -1);
                    break;
                }
                int result = UctClientApi.UCTModifyPwdReq(AppContext.getAppContext().getLoginNumber(), oldPwd, newPwd);
                if (result != 0) {
                    ToastUtils.getToast().showMessageShort(this, getResources().getString(R.string.string_settings_account_password_fail), -1);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        UctClientApi.unregisterObserver(modifyPwdCfmListener, ModifyPwdCfmListener.MODIFYPWDCFMLISTENER_INDEX);
        super.onDestroy();
    }
}
