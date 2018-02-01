package com.ptyt.uct.utils;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.List;

/**
 * Title: KeyBoardUtils.java Description: Copyright: Copyright (c) 2011 Company:
 * ** Teachnology CreateTime: 2014-11-18
 *
 * @author yuguocheng
 * @version Ver1.0
 * @since Ver1.0
 */
public class KeyBoardUtils {
    //	/**
    //	 * 打卡软键盘
    //	 *
    //	 * @param mEditText
    //	 *            输入框
    //	 * @param mContext
    //	 *            上下文
    //	 */
    //	public static void openKeybord(EditText mEditText, Context mContext) {
    //		InputMethodManager imm = (InputMethodManager) mContext
    //				.getSystemService(Context.INPUT_METHOD_SERVICE);
    //		imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN);
    //		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
    //				InputMethodManager.HIDE_IMPLICIT_ONLY);
    //	}
    //
    //	/**
    //	 * 关闭软键盘
    //	 *
    //	 * @param mEditText
    //	 *            输入框
    //	 * @param mContext
    //	 *            上下文
    //	 */
    //	public static void closeKeybord(EditText mEditText, Context mContext) {
    //		InputMethodManager imm = (InputMethodManager) mContext
    //				.getSystemService(Context.INPUT_METHOD_SERVICE);
    //		imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    //	}


    // 显示输入法
    public static void openKeybord(Context context, View focusView) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(focusView, InputMethodManager.SHOW_FORCED);
    }

    // 隐藏输入法
    public static void closeKeybord(Context context) {
        View view = ((Activity) context).getWindow().peekDecorView();
        if (view != null && view.getWindowToken() != null) {
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // 隐藏输入法2
    public static void closeKeybord(Context context, EditText et) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

    //调用该方法；键盘若显示则隐藏; 隐藏则显示
    public static void toggleKeybord(Context context) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //判断InputMethod的当前状态
    public static boolean isShow(Context context, View focusView) {
        Object obj = context.getSystemService(Context.INPUT_METHOD_SERVICE);
        System.out.println(obj);
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean bool = imm.isActive(focusView);
        List<InputMethodInfo> mInputMethodProperties = imm.getEnabledInputMethodList();

        final int N = mInputMethodProperties.size();
        for (int i = 0; i < N; i++) {
            InputMethodInfo imi = mInputMethodProperties.get(i);
            if (imi.getId().equals(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD))) {
                //imi contains the information about the keyboard you are using
                break;
            }
        }
        return bool;
    }
}
