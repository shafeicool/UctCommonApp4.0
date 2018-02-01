package com.ptyt.uct.activity;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.utils.ToastUtils;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * @Description:
 * @Date: 2017/11/15
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class BasePermissionActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {


    //权限申请的标记
    private static final int EXTERNAL_STORAGE_PERM = 1;
    private static final int CAMERA_PERM = 2;
    private static final int CONTACTS_PERM = 3;
    private static final int RECORD_AUDIO_PERM = 4;
    private static final int CALL_PHONE_PERM = 5;
    private static final int LOCATION_PERM = 6;
    private static final int APP_LIB_PERM = 10;//app 必要的权限

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    /********************************   1.申请权限  *****************************/
    //1-1选照片
    @AfterPermissionGranted(EXTERNAL_STORAGE_PERM)
    protected void startReqPermOfStorage() {
        if (EasyPermissions.hasPermissions(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            try {
                doPermGrantedOfStorage();
            } catch (Exception e) {
                ToastUtils.getToast().showMessageShort(this, getResources().getString(R.string.permissions_storage_error), -1);
            }
        } else {
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.permissions_storage_prompt), EXTERNAL_STORAGE_PERM, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    //1-2拍照
    @AfterPermissionGranted(CAMERA_PERM)
    protected void startReqPermOfCamera() {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            try {
                doPermGrantedOfCamera();
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.getToast().showMessageShort(this, getResources().getString(R.string.permissions_camera_error), -1);
            }
        } else {
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.str_request_camera_message), CAMERA_PERM, perms);
        }
    }

    //1-3 联系人
    @AfterPermissionGranted(CONTACTS_PERM)
    protected void startReqPermOfContact() {
        String[] perms = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_PHONE_STATE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            try {
                doPermGrantedOfContact();
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.getToast().showMessageShort(this, getResources().getString(R.string.str_request_readcontacts_message), -1);
            }
        } else {
            EasyPermissions.requestPermissions(this,
                    getResources().getString(R.string.str_request_readcontacts_message),
                    CONTACTS_PERM, perms);
        }
    }

    //1-4 录音

    /**
     * 执行含有录音权限的操作---> 获取权限
     */
    @AfterPermissionGranted(RECORD_AUDIO_PERM)//录音权限
    public void startReqPermOfRecordAudio() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.RECORD_AUDIO)) {//如果已经有这个权限
            try {
                doPermGrantedOfRecordAudio();
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.getToast().showMessageShort(this, getString(R.string.permissions_record_error), -1);
            }
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.permissions_record_prompt), RECORD_AUDIO_PERM, Manifest.permission.RECORD_AUDIO);
        }
    }

    //1-5 打电话
    @AfterPermissionGranted(CALL_PHONE_PERM)
    protected void startReqPermOfCallPhone() {
        String[] perms = {Manifest.permission.CALL_PHONE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            try {
                doPermGrantedOfCallPhone();
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.getToast().showMessageShort(this, getResources().getString(R.string.permissions_call_error), -1);
            }
        } else {
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.permissions_call_prompt), CALL_PHONE_PERM, perms);
        }
    }

    //1-6 定位
    @AfterPermissionGranted(LOCATION_PERM)
    protected void startReqPermOfLocation() {
        String[] perms2 = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms2)) {
            try {
                doPermGrantedOfLocation();
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.getToast().showMessageShort(this, getResources().getString(R.string.permissions_location_error), -1);
            }
        } else {
            EasyPermissions.requestPermissions(this,
                    getResources().getString(R.string.permissions_location_prompt),
                    LOCATION_PERM, perms2);
        }
    }

    //1-7 app初始化库所必要的权限集合
    @AfterPermissionGranted(APP_LIB_PERM)
    protected void startReqPermOfInitApp() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION
                , Manifest.permission.READ_PHONE_STATE
                , Manifest.permission.RECORD_AUDIO
                , Manifest.permission.CAMERA
                , Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            try {
                appPermissionGranted();
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.getToast().showMessageShort(this, getResources().getString(R.string.permissions_uctlib_error), -1);
            }
        } else {
            EasyPermissions.requestPermissions(this,
                    getResources().getString(R.string.permissions_uctlib_prompt),
                    APP_LIB_PERM, perms);
        }
    }


    /******************************   2.申请权限的结果回调,同意/拒绝   ************************/

    /**
     * 2-1 同意
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

        String[] targetPerms;
        switch (requestCode) {
            case EXTERNAL_STORAGE_PERM:
                targetPerms = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                try {
                    if (EasyPermissions.hasPermissions(this, targetPerms)) {
                        try {
                            doPermGrantedOfStorage();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.getToast().showMessageShort(this, getString(R.string.permissions_denied_storage), -1);
                }
                break;
            case CAMERA_PERM:
                targetPerms = new String[]{Manifest.permission.CAMERA};
                try {
                    if (EasyPermissions.hasPermissions(this, targetPerms)) {
                        try {
                            doPermGrantedOfCamera();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.getToast().showMessageShort(this, getString(R.string.permissions_denied_camera), -1);
                }
                break;
            case CONTACTS_PERM:
                targetPerms = new String[]{Manifest.permission.READ_CONTACTS};
                try {
                    if (EasyPermissions.hasPermissions(this, targetPerms)) {
                        try {
                            doPermGrantedOfContact();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.getToast().showMessageShort(this, getString(R.string.permissions_denied_contact), -1);
                }
                break;
            case RECORD_AUDIO_PERM:
                targetPerms = new String[]{Manifest.permission.RECORD_AUDIO};
                try {
                    if (EasyPermissions.hasPermissions(this, targetPerms)) {
                        try {
                            doPermGrantedOfRecordAudio();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.getToast().showMessageShort(this, getString(R.string.permissions_record_error), -1);
                }
                break;
            case CALL_PHONE_PERM:
                targetPerms = new String[]{Manifest.permission.CALL_PHONE};
                try {
                    if (EasyPermissions.hasPermissions(this, targetPerms)) {
                        try {
                            doPermGrantedOfCallPhone();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.getToast().showMessageShort(this, getString(R.string.permissions_denied_call_phone), -1);
                }
                break;
            case LOCATION_PERM:
                targetPerms = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                try {
                    if (EasyPermissions.hasPermissions(this, targetPerms)) {
                        try {
                            doPermGrantedOfLocation();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.getToast().showMessageShort(this, getString(R.string.permissions_denied_location), -1);
                }
                break;
            case APP_LIB_PERM:
                targetPerms = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                try {
                    if (EasyPermissions.hasPermissions(this, targetPerms)) {
                        try {
                            appPermissionGranted();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.getToast().showMessageShort(this, getString(R.string.permissions_setting), -1);
                }
                break;
        }
    }

    /**
     * 2-2 拒绝
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_PERM:
                ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_denied_storage), -1);
                break;
            case CAMERA_PERM:
                ToastUtils.getToast().showMessageShort(this, getString(R.string.permissions_denied_camera), -1);
                break;
            case CONTACTS_PERM:
                ToastUtils.getToast().showMessageShort(this, getString(R.string.permissions_denied_contact), -1);
                break;
            case RECORD_AUDIO_PERM:
                ToastUtils.getToast().showMessageShort(this, getString(R.string.permissions_denied_record_audio), -1);
                break;
            case CALL_PHONE_PERM:
                ToastUtils.getToast().showMessageShort(this, getString(R.string.permissions_denied_call_phone), -1);
                break;
            case LOCATION_PERM:
                ToastUtils.getToast().showMessageShort(this, getString(R.string.permissions_denied_location), -1);
                break;
            case APP_LIB_PERM:
                // 检查用户拒绝权限的时候是否选择了“不再提醒”,将弹出对话框引导用户去系统设置  //String[] targetPerms = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CALL_PHONE,Manifest.permission.RECORD_AUDIO};
                if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                    PrintLog.i("EasyPermissions.somePermissionPermanentlyDenied()");
                    new AppSettingsDialog.Builder(this)
                            .setRationale(R.string.permissions_setting)
                            .build()
                            .show();
                } else {
                    appPermissionDeny();
                }
                break;
        }
    }


    /******************************   3.权限成功，重写需要做的操作   ************************/

    public void appPermissionDeny() {
    }

    public void appPermissionGranted() {
    }

    public void doPermGrantedOfStorage() {
        PrintLog.i("doPermGrantedOfStorage()");
    }

    public void doPermGrantedOfCamera() {
        PrintLog.i("doPermGrantedOfCamera()");
    }

    public void doPermGrantedOfContact() {
        PrintLog.i("doPermGrantedOfContact()");
    }

    public void doPermGrantedOfRecordAudio() {
        PrintLog.i("doPermGrantedOfRecordAudio()");
    }

    public void doPermGrantedOfCallPhone() {
        PrintLog.i("doPermGrantedOfCallPhone()");
    }

    public void doPermGrantedOfLocation() {
        PrintLog.i("doPermGrantedOfLocation()");
    }

}
