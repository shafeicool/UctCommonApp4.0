package com.ptyt.uct.entity;

import com.android.uct.service.UctClientApi;
import com.ptyt.uct.common.SettingsConstant;

/**
 * @Description: 登录页面用户信息存储实体
 * @Date: 2017/4/24
 * @Author: ShaFei
 * @Version: V1.0
 */

public class LoginUserEntity {

    private static LoginUserEntity instance = null;
//    private SharedPreferences preferences;
//    private SharedPreferences.Editor editor;



    public LoginUserEntity() {
//        preferences = UctApplication.getInstance().getSharedPreferences("user_login_data", Context.MODE_PRIVATE);
//        editor = preferences.edit();
    }

    public static synchronized LoginUserEntity getUserData() {
        if (instance == null) {
            instance = new LoginUserEntity();
        }
        return instance;
    }

    public String getUsername() {
        return (String) UctClientApi.getUserData(SettingsConstant.SETTINGS_USERNAME, "");
//        return preferences.getString(KEY_USERNAME, "");
    }

    public void setUsername(String username) {
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_USERNAME, username);
//        editor.putString(KEY_USERNAME, username);
//        editor.commit();
    }

    public String getPassword() {
        return (String) UctClientApi.getUserData(SettingsConstant.SETTINGS_PASSWORD, "");
//        return preferences.getString(KEY_PASSWORD, "");
    }

    public void setPassword(String password) {
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_PASSWORD, password);
//        editor.putString(KEY_PASSWORD, password);
//        editor.commit();
    }

    public String getIP() {
        return (String) UctClientApi.getUserData(SettingsConstant.SETTINGS_IP, "");
//        return preferences.getString(KEY_IP, "");
    }

    public void setIP(String ip) {
        UctClientApi.saveUserData(SettingsConstant.SETTINGS_IP, ip);
//        editor.putString(KEY_IP, ip);
//        editor.commit();
    }


}
