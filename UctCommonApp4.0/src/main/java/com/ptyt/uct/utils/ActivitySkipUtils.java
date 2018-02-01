package com.ptyt.uct.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.android.uct.bean.ContactUser;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.VideoCallActivity;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.entity.Group;
import com.ptyt.uct.entity.GroupUser;
import com.ptyt.uct.model.ContactDBManager;

import java.util.List;

/**
 * @Description: Activity跳转工具类
 * @Date:        2017/4/26
 * @Author:      ShaFei
 * @Version:     V1.0
 */

public class ActivitySkipUtils {

    private static Context context;
    private static ActivitySkipUtils instance = null;

    public ActivitySkipUtils(Context context) {
        this.context = context;
    }

    /**
     * 初始化单例
     *
     * @param context
     */
    public static synchronized ActivitySkipUtils getInstance(Context context) {
        if (instance == null) {
            instance = new ActivitySkipUtils(context);
        }
        return instance;
    }

    /**
     *
     * @Description: 隐式启动,跳转
     * @param action
     *            含操作的Intent
     */
    public static void startActivityIntentSafe(Intent action) {
        // Verify it resolves
        PackageManager packageManager = context.getPackageManager();
        List activities = packageManager.queryIntentActivities(action,
                PackageManager.MATCH_DEFAULT_ONLY);
        boolean isIntentSafe = activities.size() > 0;

        // Start an activity if it's safe
        if (isIntentSafe) {
            context.startActivity(action);
        }

    }

    /**
     * @Description: 跳转,带参数的方法;需要其它的数据类型,再继续重载吧,暂时只写这么多吧,意义不大
     * @param cls
     * @param keys
     * @param values  手动改变int[] values类型,可以传递其它数据类型,就不重载了
     */
    public static void toNextActivity(Class<?> cls, String[] keys, int[] values) {
        Intent i = new Intent(context, cls);
        for (int j = 0; j < values.length; j++) {
            i.putExtra(keys[j], values[j]);
        }
        context.startActivity(i);

    }

    /**
     * @Description: 跳转
     * @param cls
     *            to,一般传XXXActivity.class
     */
    public static void toNextActivity(Class<?> cls) {
        Intent i = new Intent(context, cls);
        context.startActivity(i);
    }

    /**
     * @Description: 跳转,带参数的方法;需要其它的数据类型,再继续重载吧
     * @param cls
     * @param keyvalues
     *            需要传进去的String参数{{key1,values},{key2,value2}...}
     */
    public static void toNextActivity(Context context, Class<?> cls,
                                      String[][] keyvalues) {
        Intent i = new Intent(context, cls);
        for (String[] strings : keyvalues) {
            i.putExtra(strings[0], strings[1]);
        }
        context.startActivity(i);

    }

    /**
     * @Description: 跳转,带参数的方法;需要其它的数据类型,再继续重载吧
     * @param cls
     * @param bundle
     *            需要传进去的String参数Bundle
     */
    public static void toNextActivity(Class<?> cls, Bundle bundle) {
        Intent i = new Intent(context, cls);
        i.putExtras(bundle);
        context.startActivity(i);

    }

    public static void toNextActivityAndFinish(Class<?> cls) {
        Intent i = new Intent(context, cls);
        context.startActivity(i);

        ((Activity) context).finish();
    }

    public static void toNextActivityAndFinish(Context context, Class<?> cls) {
        PrintLog.i("从" + context.getClass().getSimpleName() + "跳转到" + cls.getSimpleName());
        Intent i = new Intent(context, cls);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

        ((Activity) context).finish();
    }

    public static void toNextActivity(Context context, Intent i) {
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public static void finish(Activity activity) {
        activity.finish();
    }



    public static void intent2CallActivity(Context mContext, int callDirection,int iDirection, String pcCalling,
                                    String pcName, int hUserCall,int bussinessTag) {
        if(!TextUtils.isEmpty(pcCalling)){
            Intent intent = new Intent(mContext,VideoCallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("callDirection", callDirection);
            intent.putExtra("callNumber", pcCalling);
            intent.putExtra("hUserCall", hUserCall);
            if(!TextUtils.isEmpty(pcName)){
                intent.putExtra("userName", pcName);
            }else {
                String userName = ContactDBManager.getInstance(mContext).queryContactName(pcCalling);
                intent.putExtra("userName", userName);
            }
            intent.putExtra("businessTag",bussinessTag);
            mContext.startActivity(intent);
        }else{
            ToastUtils.getToast().showMessageShort(context,context.getString(R.string.string_audio_video_error1),-1);
        }
    }
    /**
     *
     * @param cls
     * @param tag 用于区分同一activity时处理不同业务 0-语音呼叫  1-视频呼叫 2-上传视频
     */
    public static void intent2CallActivity(Context context,Class cls, int tag, GroupUser user) {
        //如果有组呼，不能进行语音呼叫或视频呼叫
        if (UctApplication.getInstance().isInGroupCall) {
            if (tag == ConstantUtils.AUDIO_SCALL) {
                ToastUtils.getToast().showMessageShort(context, context.getString(R.string.gcalling_cannot_audio_call), -1);
                return;
            } else if (tag == ConstantUtils.VIDEO_SCALL) {
                ToastUtils.getToast().showMessageShort(context, context.getString(R.string.gcalling_cannot_video_call), -1);
                return;
            }
        }
        if(user != null && user.getUserTel() != null){
            if(user.getUserTel().equals(AppContext.getAppContext().getLoginNumber())){
                ToastUtils.getToast().showMessageShort(context,context.getString(R.string.string_audio_video_error2),-1);
            }else {
                Intent intent = new Intent(context, cls);
                intent.putExtra("callDirection", ConstantUtils.CALL_DIRECTION_ACTIVE);//0:主叫  1：被叫
                intent.putExtra("callNumber", user.getUserTel());
                if(!TextUtils.isEmpty(user.getUserName())){
                    intent.putExtra("userName",user.getUserName());
                }else {
                    String name = ContactDBManager.getInstance(context).queryContactName(user.getUserTel());
                    intent.putExtra("userName",name);
                }
                intent.putExtra("businessTag",tag);
                intent.putExtra("isGroupNo", false);
                context.startActivity(intent);
            }
        }else {
            ToastUtils.getToast().showMessageShort(context,context.getString(R.string.string_audio_video_error1),-1);
        }
    }

    /**
     *
     * @param cls
     * @param tag 用于区分同一activity时处理不同业务 0-语音呼叫  1-视频呼叫 2-上传视频
     */
    public static void intent2CallActivity(Context context,Class cls, int tag, ContactUser user) {
        //如果有组呼，不能进行语音呼叫或视频呼叫
        if (UctApplication.getInstance().isInGroupCall) {
            if (tag == ConstantUtils.AUDIO_SCALL) {
                ToastUtils.getToast().showMessageShort(context, context.getString(R.string.gcalling_cannot_audio_call), -1);
                return;
            } else if (tag == ConstantUtils.VIDEO_SCALL) {
                ToastUtils.getToast().showMessageShort(context, context.getString(R.string.gcalling_cannot_video_call), -1);
                return;
            }
        }
        if(user != null && user.getNumber() != null){
            if(user.getNumber().equals(AppContext.getAppContext().getLoginNumber())){
                ToastUtils.getToast().showMessageShort(context,context.getString(R.string.string_audio_video_error2),-1);
            }else {
                Intent intent = new Intent(context, cls);
                intent.putExtra("callDirection", ConstantUtils.CALL_DIRECTION_ACTIVE);//0:主叫  1：被叫
                intent.putExtra("callNumber", user.getNumber());
                if(!TextUtils.isEmpty(user.getDesc())){
                    intent.putExtra("userName",user.getDesc());
                }else {
                    String name = ContactDBManager.getInstance(context).queryContactName(user.getNumber());
                    intent.putExtra("userName",name);
                }
                intent.putExtra("businessTag",tag);
                intent.putExtra("isGroupNo", false);
                context.startActivity(intent);
            }
        }else {
            ToastUtils.getToast().showMessageShort(context,context.getString(R.string.string_audio_video_error1),-1);
        }
    }

    /**
     * CallFragment中跳转至成员列表界面
     */
    public static void intent2GMemberListActivity(Context context, Class cls, Group groupData) {
        if (groupData == null || TextUtils.isEmpty(groupData.getGrouTel())) {
            return;
        }
        Intent intent = new Intent(context, cls);
        intent.putExtra("groupId", groupData.getGrouTel());
        if (!TextUtils.isEmpty(groupData.getGroupName())) {
            intent.putExtra("groupName", groupData.getGroupName());
        } else {
            intent.putExtra("groupName", groupData.getGrouTel());
        }
        intent.putExtra("groupAdmin", groupData.getAdminTel());
        intent.putExtra("groupNewUser", groupData.getGroupCreateUser());
        intent.putExtra("isGroupNo", true);
        context.startActivity(intent);
    }
}
