package com.ptyt.uct.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * @Description:
 * @Date: 2017/11/16
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public abstract class BasePermissionFragment extends BaseFragment implements EasyPermissions.PermissionCallbacks {

    //权限申请的标记
    private static final int EXTERNAL_STORAGE_PERM = 1;
    private static final int CAMERA_PERM = 2;
    private static final int CONTACTS_PERM = 3;
    private static final int RECORD_AUDIO_PERM = 4;
    private static final int CALL_PHONE_PERM = 5;
    private static final int LOCATION_PERM = 6;

    //具体业务
    private static final int AUDIO_CALL_PERM = 10;
    private static final int VIDEO_CALL_PERM = 11;
    private static final int VIDEO_UPLOAD_PERM = 12;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    /********************************   1.申请权限  *****************************/
    //1-1选照片
    @AfterPermissionGranted(EXTERNAL_STORAGE_PERM)
    protected void startReqPermOfStorage() {
        if (EasyPermissions.hasPermissions(mContext, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE})) {
            try {
                doPermGrantedOfStorage();
            } catch (Exception e) {
                ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.permissions_denied_prompt1), -1);
            }
        } else {
            EasyPermissions.requestPermissions(this, mContext.getString(R.string.permissions_denied_prompt1), EXTERNAL_STORAGE_PERM, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
        }
    }

    //1-2拍照
    @AfterPermissionGranted(CAMERA_PERM)
    protected void startReqPermOfCamera() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(mContext, perms)) {
            try {
                doPermGrantedOfCamera();
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.permissions_camera_error), -1);
            }
        } else {
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.str_request_camera_message), CAMERA_PERM, perms);
        }
    }

    //1-3 联系人
    @AfterPermissionGranted(CONTACTS_PERM)
    protected void startReqPermOfContact() {
        String[] perms = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_PHONE_STATE};
        if (EasyPermissions.hasPermissions(mContext, perms)) {
            try {
                doPermGrantedOfContact();
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.getToast().showMessageShort(mContext, getResources().getString(R.string.str_request_readcontacts_message), -1);
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
        boolean hasPermissions = EasyPermissions.hasPermissions(mContext, Manifest.permission.RECORD_AUDIO);
        PrintLog.e("startReqPermOfRecordAudio hasPermissions="+hasPermissions);
        if(hasPermissions){
             try {
                 doPermGrantedOfRecordAudio();
             } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_record_error), -1);
             }
         }else{
             EasyPermissions.requestPermissions(this, mContext.getString(R.string.permissions_record_prompt), RECORD_AUDIO_PERM, Manifest.permission.RECORD_AUDIO);
         }
    }

    /**
     * 获取请求权限中需要授权的权限
     */
    private static String[] getDeniedPermissions(@NonNull Context context, @NonNull String[] permissions) {
        List<String> deniedPermissions = new ArrayList();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions.toArray(new String[deniedPermissions.size()]);
    }

    public static boolean hasPermissions(Context context,String perms) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        PrintLog.e("ContextCompat.checkSelfPermission(context, perms)="+ContextCompat.checkSelfPermission(context, perms));
        if (ContextCompat.checkSelfPermission(context, perms) == PackageManager.PERMISSION_DENIED) {
           return false;
        }else if(ContextCompat.checkSelfPermission(context, perms) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }
    //1-5 打电话
    @AfterPermissionGranted(CALL_PHONE_PERM)
    protected void startReqPermOfCallPhone() {
        String[] perms = {Manifest.permission.CALL_PHONE};
        if (EasyPermissions.hasPermissions(mContext, perms)) {
            try {
                doPermGrantedOfCallPhone();
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.permissions_call_error), -1);
            }
        } else {
            EasyPermissions.requestPermissions(this, mContext.getString(R.string.permissions_call_prompt), CALL_PHONE_PERM, perms);
        }
    }

    //1-6 定位
    @AfterPermissionGranted(LOCATION_PERM)
    protected void startReqPermOfLocation() {
        String[] perms2 = {Manifest.permission.ACCESS_FINE_LOCATION};
        boolean hasPermissions = EasyPermissions.hasPermissions(mContext, perms2);
        PrintLog.e("ACCESS_FINE_LOCATION hasPermissions="+hasPermissions);
        if (hasPermissions) {
            try {
                doPermGrantedOfLocation();
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.permissions_location_error), -1);
            }
        } else {
            EasyPermissions.requestPermissions(this,
                    mContext.getString(R.string.permissions_location_prompt),
                    LOCATION_PERM, perms2);
        }
    }

    /**
     * 语音呼叫---> 获取权限
     */
    @AfterPermissionGranted(AUDIO_CALL_PERM)
    public void startReqPermOfAudioCall() {
        if(UctApplication.getInstance().isInGroupCall){//当前正在组呼时，不能发起语音呼叫
            ToastUtils.getToast().showMessageShort(mContext,getString(R.string.gcalling_cannot_audio_call),-1);
            return;
        }
        boolean hasPermissions = hasPermissions(getContext(), Manifest.permission.RECORD_AUDIO);
        PrintLog.e("startReqPermOfRecordAudio hasPermissions="+hasPermissions);
        if(hasPermissions){
            try {
                doPermGrantedOfCallBusiness(ConstantUtils.AUDIO_SCALL);
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_record_error), -1);
            }
        }else{
            EasyPermissions.requestPermissions(this, mContext.getString(R.string.permissions_record_prompt), RECORD_AUDIO_PERM, Manifest.permission.RECORD_AUDIO);
        }
    }

    /**
     * 视频呼叫---> 获取权限
     */
    @AfterPermissionGranted(VIDEO_CALL_PERM)
    public void startReqPermOfVideoCall() {
        if(UctApplication.getInstance().isInGroupCall){//当前正在组呼时，不能发起视频呼叫
            ToastUtils.getToast().showMessageShort(mContext,getString(R.string.gcalling_cannot_video_call),-1);
            return;
        }
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        boolean hasPermissions = EasyPermissions.hasPermissions(mContext, perms);
        PrintLog.e("startReqPermOfRecordAudio hasPermissions="+hasPermissions);
        if (hasPermissions) {
            try {
                doPermGrantedOfCallBusiness(ConstantUtils.VIDEO_SCALL);
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_video_call), -1);
            }
        }else{
            EasyPermissions.requestPermissions(this, mContext.getString(R.string.permissions_video_call), VIDEO_CALL_PERM, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO);
        }
    }

    /**
     * 上传视频---> 获取权限
     */
    @AfterPermissionGranted(VIDEO_UPLOAD_PERM)
    public void startReqPermOfVideoUpload() {
        boolean hasPermissions = hasPermissions(getContext(), Manifest.permission.CAMERA);
        PrintLog.e("startReqPermOfRecordAudio hasPermissions="+hasPermissions);
        if(hasPermissions){
            try {
                doPermGrantedOfCallBusiness(ConstantUtils.UPLOAD_VIDEO);
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_camera_error), -1);
            }
        }else{
            EasyPermissions.requestPermissions(this, mContext.getString(R.string.str_request_camera_message), VIDEO_UPLOAD_PERM, Manifest.permission.CAMERA);
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
                    if (EasyPermissions.hasPermissions(mContext, targetPerms)) {
                        try {
                            doPermGrantedOfStorage();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_denied_storage), -1);
                }
                break;
            case CAMERA_PERM:
                targetPerms = new String[]{Manifest.permission.CAMERA};
                try {
                    if (EasyPermissions.hasPermissions(mContext, targetPerms)) {
                        try {
                            doPermGrantedOfCamera();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_denied_camera), -1);
                }
                break;
            case CONTACTS_PERM:
                targetPerms = new String[]{Manifest.permission.READ_CONTACTS};
                try {
                    if (EasyPermissions.hasPermissions(mContext, targetPerms)) {
                        try {
                            doPermGrantedOfContact();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_denied_contact), -1);
                }
                break;
            case RECORD_AUDIO_PERM:
                targetPerms = new String[]{Manifest.permission.RECORD_AUDIO};
                try {
                    if (EasyPermissions.hasPermissions(mContext, targetPerms)) {
                        try {
                            doPermGrantedOfRecordAudio();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_record_error), -1);
                }
                break;
            case CALL_PHONE_PERM:
                targetPerms = new String[]{Manifest.permission.CALL_PHONE};
                try {
                    if (EasyPermissions.hasPermissions(mContext, targetPerms)) {
                        try {
                            doPermGrantedOfCallPhone();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_denied_call_phone), -1);
                }
                break;
            case LOCATION_PERM:
                targetPerms = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                try {
                    if (EasyPermissions.hasPermissions(mContext, targetPerms)) {
                        try {
                            doPermGrantedOfLocation();
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_denied_location), -1);
                }
                break;
            case VIDEO_CALL_PERM://视频呼叫
                targetPerms = new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
                try {
                    if (EasyPermissions.hasPermissions(mContext, targetPerms)) {
                        try {
                            doPermGrantedOfCallBusiness(ConstantUtils.VIDEO_SCALL);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_video_call_no_permission), -1);
                }
                break;
            case AUDIO_CALL_PERM://语音呼叫
                targetPerms = new String[]{Manifest.permission.RECORD_AUDIO};
                try {
                    if (EasyPermissions.hasPermissions(mContext, targetPerms)) {
                        try {
                            doPermGrantedOfCallBusiness(ConstantUtils.AUDIO_SCALL);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_audio_call_no_permission), -1);
                }
                break;
            case VIDEO_UPLOAD_PERM://视频上传
                targetPerms = new String[]{Manifest.permission.CAMERA};
                try {
                    if (EasyPermissions.hasPermissions(mContext, targetPerms)) {
                        try {
                            doPermGrantedOfCallBusiness(ConstantUtils.UPLOAD_VIDEO);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_video_upload_no_permission), -1);
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
                ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_denied_camera), -1);
                break;
            case CONTACTS_PERM:
                ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_denied_contact), -1);
                break;
            case RECORD_AUDIO_PERM:
                ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_denied_record_audio), -1);
                break;
            case CALL_PHONE_PERM:
                ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_denied_call_phone), -1);
                break;
            case LOCATION_PERM:
                ToastUtils.getToast().showMessageShort(mContext, getString(R.string.permissions_denied_location), -1);
                break;
        }
    }


    /******************************   3.权限成功，重写需要做的操作   ************************/

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
    public void doPermGrantedOfCallBusiness(int businessTag) {
        PrintLog.i("doPermGrantedOfCallBusiness()");
    }
}
