package com.ptyt.uct.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtils {

    public static boolean isMatchs(String value) {
        return value.matches("[0-9]+");
    }

    public static ArrayList<String> getSplitString(String content) {
        ArrayList<String> retVal = new ArrayList<String>();
        int length = content.length();//字符长度
        if (length <= 300) {
            retVal.add(content);
            return retVal;
        }
        String temp = null;
        int start = 0;
        int end = 299;
        for (int i = 299; i < length; ++i) {
            temp = content.substring(start, end);
            long len = temp.getBytes().length;
            if (len > 299) {
                retVal.add(content.substring(start, end - 1));
                start = end - 1;
                end += 299;
            }
            ++end;
            if (end > length) {
                end = length;
                break;
            }
        }
        if (end == length) {
            retVal.add(content.substring(start, length));
        }
        return retVal;
    }

    /**
     * 正则替换所有特殊字符
     *
     * @param orgStr
     * @return
     */
    public static String replaceSpecStr(String orgStr, String replacement) {
        if (null != orgStr && !"".equals(orgStr.trim())) {
            String regEx = "[\\s~·`!！@#￥$%^……&*（()）\\-——\\-_=+【\\[\\]】｛{}｝\\|、\\\\；;：:‘'“”\"，,《<。.》>、/？?]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(orgStr);
            return m.replaceAll(replacement);
        }
        return null;
    }

    /**
     * YYYYMMDDHHMMSSNN_DSTDN_SRCDN，YYYYMMDDHHMMSS对应含义为年月日时分秒表示，其后的NN表示两位序号，可以直接从00开始，SRCDN表示源号码，DSTDN表示目的号码，TIMELEN表示对应秒数，无时长或未知时长可以不填
     *
     * @param dstdn 目的号码
     * @param srcdn 源号码
     * @return
     */
    private static int serialize;
    private static long oldTimes;
    private static long ranges1 = -1;

    public static synchronized String getSmsId(String dstdn, String srcdn) {
        SimpleDateFormat sDataFromat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        Date date = new Date();
        long currentTimes = System.currentTimeMillis();
        long range = Math.abs(currentTimes - oldTimes);
        String timeStr = sDataFromat.format(date);
        StringBuffer sBuffer = new StringBuffer(timeStr);
        //序号格式化 00 01 02格式
        DecimalFormat df2 = (DecimalFormat) DecimalFormat.getInstance();
        df2.applyPattern("00");
        //首先判断是否在同毫秒秒中发两条 如果同毫秒内发两条的添加序号
        if (range >= 0 && range <= 1000) {
            sBuffer.append(df2.format(serialize));
            try {
                Thread.sleep(11);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            //不在同一秒内的 序号清零
            serialize = 0;
            sBuffer.append("00");
        }
        long range2 = Math.abs(currentTimes - ranges1);
        if (range2 > 1000) {
            ranges1 = currentTimes;
            serialize = 0;
        }
        serialize++;
        sBuffer.append("_");
        sBuffer.append(dstdn);
        sBuffer.append("_");
        sBuffer.append(srcdn);
        oldTimes = currentTimes;
        return sBuffer.toString();
    }

    public static long getCurrentTimes() {
        //登录成功后保存本地时间 保存服务器时间
        //发短息的时候 服务器时间+本地时间差
        return System.currentTimeMillis();
    }

    // 提供“yyyy-mm-dd”形式的字符串到毫秒的转换
    public static long getMillis(String dateString) {
        String[] date = dateString.split("-");
        if (date.length != 3) {
            PrintLog.e("getMillis 错误." + dateString);
            return 0;
        }
        return getMillis(date[0], date[1], date[2]);

    }

    // 根据输入的年、月、日，转换成毫秒表示的时间
    public static long getMillis(int year, int month, int day) {
        GregorianCalendar calendar = new GregorianCalendar(year, month, day);
        return calendar.getTimeInMillis();

    }

    // 根据输入的年、月、日，转换成毫秒表示的时间
    public static long getMillis(String yearString, String monthString,
                                 String dayString) {
        int year = Integer.parseInt(yearString);
        int month = Integer.parseInt(monthString) - 1;
        int day = Integer.parseInt(dayString);
        return getMillis(year, month, day);

    }

    // 获得当前时间的毫秒表示
    public static long getNow() {
        return getCurrentTimes();

    }

    // 根据输入的毫秒数，获得日期字符串
    public static String getDate(long millis) {
        SimpleDateFormat simpleFromat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleFromat.format(millis);
        return date;

    }

    // 根据输入的毫秒数，获得年份
    public static int getYear(long millis) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.YEAR);

    }

    // 根据输入的毫秒数，获得月份
    public static int getMonth(long millis) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.MONTH);

    }

    // 根据输入的毫秒数，获得日期
    public static int getDay(long millis) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.DATE);

    }

    // 根据输入的毫秒数，获得小时
    public static int getHour(long millis) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.HOUR_OF_DAY);

    }

    // 根据输入的毫秒数，获得分钟
    public static int getMinute(long millis) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.MINUTE);

    }

    // 根据输入的毫秒数，获得秒
    public static int getSecond(long millis) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.SECOND);

    }

    //获取指定毫秒数的对应星期
    public static String getWeek(long millis) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(millis);
        String week = "";
        int cweek = calendar.get(Calendar.DAY_OF_WEEK);
        switch (cweek) {
            case 1:
                week = "日";
                break;
            case 2:
                week = "一";
                break;
            case 3:
                week = "二";
                break;
            case 4:
                week = "三";
                break;
            case 5:
                week = "四";
                break;
            case 6:
                week = "五";
                break;
            case 7:
                week = "六";
                break;
        }
        return week;

    }

    // 获得今天日期
    public static String getTodayData() {
        return getDate(getNow());

    }

    // 获得明天日期
    public static String getTomoData() {
        // 86400000为一天的毫秒数
        return getDate(getNow() + 86400000);

    }

    // 获得后天日期
    public static String getTheDayData() {
        return getDate(getNow() + 86400000 + 86400000);
    }

    // 获得昨天日期
    public static String getYesData() {
        return getDate(getNow() - 86400000L);
    }

    // 获得前天日期
    public static String getBeforeYesData() {
        return getDate(getNow() - 86400000L - 86400000L);
    }

    /**
     * 获取今天时间具体内容
     *
     * @return
     */
    public static String StringData() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String mYear = String.valueOf(c.get(Calendar.YEAR)); // 获取当前年份
        String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1); // 获取当前月份
        String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH)); // 获取当前月份的日期号码
        String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
        if ("1".equals(mWay)) {
            mWay = "天";
        } else if ("2".equals(mWay)) {
            mWay = "一";
        } else if ("3".equals(mWay)) {
            mWay = "er";
        } else if ("4".equals(mWay)) {
            mWay = "三";
        } else if ("5".equals(mWay)) {
            mWay = "四";
        } else if ("6".equals(mWay)) {
            mWay = "五";
        } else if ("7".equals(mWay)) {
            mWay = "六";
        }
        return mYear + "年" + mMonth + "月" + mDay + "日" + " 星期" + mWay;
    }

    /**
     * 时间格式化成今天、昨天 前天 几月几日的格式
     *
     * @return
     * @throws ParseException
     */
    @SuppressLint("SimpleDateFormat")
    public static String formatChatTime(Context mContext, Long formatTime) {
        if (formatTime == null) {
            return null;
        }
        SimpleDateFormat simpleFromat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleFromat.format(formatTime);
        if (getMillis(date) == getMillis(getBeforeYesData())) {
            return mContext.getString(R.string.string_the_day_before_yesterday) + formatTime(mContext, formatTime);
        } else if (getMillis(date) == getMillis(getYesData())) {
            return mContext.getString(R.string.string_yesterday) + formatTime(mContext, formatTime);
        } else if (getMillis(date) == getMillis(getTodayData())) {
            return formatTime(mContext, formatTime);
        } else {
            SimpleDateFormat sf = new SimpleDateFormat("yyyy" + mContext.getString(R.string.string_year_lib) + "MM" + mContext.getString(R.string.string_mouth) + "dd" + mContext.getString(R.string.string_day) + " ");
            String dateStr = sf.format(formatTime);
            return dateStr + formatTime(mContext, formatTime);
        }
    }

    @SuppressLint("SimpleDateFormat")
    private static String formatTime(Context mContext, long time) {
        //空格
        String blank = " ";
        final Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(time);
        int hour = mCalendar.get(Calendar.HOUR);
        int apm = mCalendar.get(Calendar.AM_PM);
        System.out.println("hour==" + hour + " apm==" + apm);
        String data = new SimpleDateFormat("HH:mm").format(time);
        if (apm == 0) {//上午
            return blank + mContext.getString(R.string.string_am) + data;
        } else {       //下午
            return blank + mContext.getString(R.string.string_pm) + data;
        }
    }

    /**
     * 判断IP地址是否合法
     *
     * @param ipaddr
     * @return
     */
    public static boolean isIPAddress(String ipaddr) {
        boolean flag = false;
        Pattern pattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
        Matcher m = pattern.matcher(ipaddr);
        flag = m.matches();
        return flag;
    }

    /**
     * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     *
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    /**
     * @param mContext
     * @param content
     * @return SpannableStringBuilder
     * @description 文字表达式转换表情
     */
    public static SpannableStringBuilder faceHandler(Context mContext, String content) {
        String tempContent = content;
        //        tempContent = tempContent.replace("\n", "");
        SpannableStringBuilder sb = new SpannableStringBuilder(tempContent);
        //表情的协议是\+表情的ID
        String regex = "(\\\\\\d{2})";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(tempContent);
        while (m.find()) {
            String imageName = m.group().replace("\\", "");
            PrintLog.i("imageName = " + imageName);
            try {
                int imageId = mContext.getResources().getIdentifier("icon_" + imageName, "mipmap", mContext.getPackageName());
                Drawable drawable = ContextCompat.getDrawable(mContext, imageId);
                drawable.setBounds(0, 0, DensityUtils.dp2px(mContext, 16), DensityUtils.dp2px(mContext, 16));//这里设置图片的大小
                ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
                sb.setSpan(imageSpan, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception e) {
                PrintLog.e("Exception" + e);
            }
        }
        return sb;
    }

    public static String getString(String s, String s1) {//s是需要删除某个子串的字符串s1是需要删除的子串
        int position = s.indexOf(s1);
        int length = s1.length();
        int Length = s.length();
        String newString = s.substring(0, position) + s.substring(position + length, Length);
        return newString;//返回已经删除好的字符串
    }

    public static String getString2(String s) {//s是需要删除某个子串的字符串s1是需要删除的子串
        java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00");
        df.format(s);
//        例：new java.text.DecimalFormat("#.00").format(3.1415926)
//#.00 表示两位小数 #.0000四位小数 以此类推...
        return null;
    }


}
