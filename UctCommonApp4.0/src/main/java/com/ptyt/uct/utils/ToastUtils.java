package com.ptyt.uct.utils;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * @Description: Toast显示工具类
 * @Date:        2017/4/26
 * @Author:      ShaFei
 * @Version:     V1.0
 */

public class ToastUtils {
    private static ToastUtils toastUtil = null;
    private static Toast mToast;

    public static ToastUtils getToast() {
        if (null == toastUtil) {
            toastUtil = new ToastUtils();
        }
        return toastUtil;
    }

    public void release() {
        if (mToast != null) {
            mToast = null;
        }
        if (toastUtil != null) {
            toastUtil = null;
        }
    }

    public synchronized void showMessageLong(final Activity act, final String str,
                                         final int strId) {
        showMessage(act, str, strId, Toast.LENGTH_LONG);
    }

    public synchronized void showMessageShort(final Activity act, final String str,
                                             final int strId) {
        showMessage(act, str, strId, Toast.LENGTH_SHORT);
    }

    public synchronized void showMessageLong(final Context act, final String str,
                                             final int strId) {
        showMessage(act, str, strId, Toast.LENGTH_LONG);
    }

    public synchronized void showMessageShort(final Context act, final String str,
                                              final int strId) {
        showMessage(act, str, strId, Toast.LENGTH_SHORT);
    }

    private synchronized void showMessage(Context act, String str,
                                         int strId, int during) {
        if (mToast == null) {
            mToast = Toast.makeText(act, "", during);
        }
        if (strId == -1) {
            mToast.setText(str);
        } else {
            mToast.setText(strId);
        }
        mToast.show();
    }

    public synchronized void showMessage(final Activity act, final String str,
                                         final int strId, final int during) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(act, "", during);
                }
                if (strId == -1) {
                    mToast.setText(str);
                    //设置gravity属性，以改变默认位置
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                } else {
                    mToast.setText(strId);
                }
                mToast.show();
            }
        });
    }
}
