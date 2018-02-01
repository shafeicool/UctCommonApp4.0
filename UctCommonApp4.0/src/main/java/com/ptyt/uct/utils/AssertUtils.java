package com.ptyt.uct.utils;

import com.android.uct.utils.PrintLog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 断言工具类
 * @Date:        2017/4/26
 * @Author:      ShaFei
 * @Version:     V1.0
 */

public class AssertUtils {
	/**
	 * 断言是否是有效的数字
	 * @param strValue 要判断的字符串
	 * @param msg 如果不是字符串 抛出断言的异常信息
	 */
	public static void isValidNumber(String strValue,String msg) {
		boolean isNumber = isNumeric(strValue);
		if (!isNumber) {
			PrintLog.e(msg);
			throw new AssertionError(msg);
		}
	}

	/**
	 * 判断字符串是否纯数字
	 *
	 * @param str
	 * @return
	 */
	private static boolean isNumeric(String str) {
		if (str == null || "".equals(str))
			return false;
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;

	}
	
	/**
	 * 断言是否是有效的数字
	 * @param msg 如果不是字符串 抛出断言的异常信息
	 */
	public static void isSwitchDefault(String msg) {
			PrintLog.e(msg);
			throw new AssertionError(msg);
	}
}
