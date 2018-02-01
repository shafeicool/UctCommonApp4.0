package com.ptyt.uct.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;

import com.android.uct.utils.PrintLog;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * SD卡相关的辅助类
 */
public class SDCardUtils {
    public static final String VIDEO_FILE_TYPE = ".mp4";
    public static final long AvailableMinInternalMemorySize = 524288000L;
    /**
     * 保存用户配置的文件
     */
    public static final String INFO_NAME = "info.properties";

    private SDCardUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 判断SDCard是否可用
     *
     * @return
     */
    public static boolean isSDCardEnable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);

    }

    /**
     * 获取系统存储路径
     *
     * @return
     */
    public static String getRootDirectoryPath() {
        return Environment.getRootDirectory().getAbsolutePath();
    }

    public static String getLogBasePath() {
        return getLogPath() + "UctCommonApp";
    }

    public static String getLogPath() {
        String path = getExternalSdCardPath() + File.separator + "PTYT" + File.separator + "UctCommonApp" + File.separator + "Log" + File.separator;
        mkdir2(path);
        return path;
    }

    public static String getUpgradePath() {
        String path = getExternalSdCardPath() + File.separator + "PTYT" + File.separator + "UctCommonApp" + File.separator + "UctCommonAppUpgrade";
        mkdir2(path);
        return path;
    }

    public static boolean deleteConversationFile(Context context, Long conversationId) {
        if (conversationId == null) {
            return false;
        }
        return FileUtils.deleteDirectory(context, getSDCardBasePath() + "Msg" + File.separator + conversationId);
    }

    /**
     * 获取SD卡路径
     *
     * @return
     */
    public static String getSDCardBasePath() {
        return getExternalSdCardPath()
                + File.separator + "PTYT" + File.separator + "UctCommonApp" + File.separator;
    }

    /**
     * 获取内置SD卡
     *
     * @return
     */
    public static String getBasePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * 内置sd卡的路径
     *
     * @return
     */
    public static String getUserInfoConfigPath() {
        String path = getBasePath() + File.separator + "uctapp" + File.separator;
        mkdir2(path);
        return path;
    }

    public static String getUserInfoConfigPath1() {
        String path = getExternalSdCardPath() + File.separator + "uctapp" + File.separator;
        mkdir2(path);
        return path;
    }

    /**
     * 获取扩展SD卡存储目录
     * 如果有外接的SD卡，并且已挂载，则返回这个外置SD卡目录
     * 否则：返回内置SD卡目录
     *
     * @return
     */
    public static String getExternalSdCardPath() {
        String sdPath = "/mnt/sdcard";
        if (!isSDCardEnable()) {
            PrintLog.e("【SD卡路径】" + sdPath);
            return sdPath;
        }
        sdPath = "/mnt/sdcard2";
        File sdCardFile = null;
        File file = new File(sdPath);
        if (file.isDirectory() && file.canWrite()) {
            PrintLog.d("【SD卡路径】" + sdPath);
            sdPath = file.getAbsolutePath();
            String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
            File testWritable = new File(sdPath, "test_" + timeStamp);
            if (testWritable.mkdirs()) {
                testWritable.delete();
            } else {
                sdPath = "/mnt/sdcard";
            }
        } else {
            sdCardFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            sdPath = sdCardFile.getAbsolutePath();
        }
        PrintLog.d("【SD卡路径】" + sdPath);
        return sdPath;
    }

    /**
     * 短信的图片 语音文件存储的跟路径跟路径
     *
     * @return
     */
    public static String getMsgBasePath(long conversationId) {
        String path = getSDCardBasePath() + "Msg" + File.separator + conversationId + File.separator;
        mkdir2(path);
        return path;
    }

    public static String getRemoteVideoBasePath() {
        String path = getSDCardBasePath() + "RemoteVideo" + File.separator;
        mkdir2(path);
        return path;
    }

    public static String getLocalVideoFileName() {
        return "VID_" + DateUtils.formatTimeVideoFileName(System.currentTimeMillis()) + VIDEO_FILE_TYPE;
    }

    public static final String DCIM =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();

    public static String getLocalDCIMPath() {
        String path = getExternalSdCardPath() + File.separator + Environment.DIRECTORY_DCIM + File.separator;
        mkdir2(path);
        return path;
    }

    public static String getLocalVideoBasePath() {
        String path = getSDCardBasePath() + "LocalVideo" + File.separator;
        mkdir2(path);
        return path;
    }

    /**
     * Description: 名称规则sdcard/UctCommonApp/PTYT/当前年月日/tel/
     *
     * @param tel
     * @return
     */
    public static String getChatRecordPath(long conversationId, String tel) {
        if (TextUtils.isEmpty(tel)) {
            tel = "01234567890";
        }
        String path = getMsgBasePath(conversationId) + getCurrTime() + File.separator + tel + File.separator;
        mkdir2(path);
        return path;
    }

    /**
     * Description:命名规则 年月日/目的号码/文件名称
     *
     * @param tel
     * @param msgId
     * @return
     */
    public static String getChatRemotePath(String tel, String msgId, String suffix) {
        //-------------------by yugc 录音的文件名称用msgid代替-------------------
        /*SimpleDateFormat sDataFromat = new SimpleDateFormat("yyyyMMddhhmmss",Locale.getDefault());
        Date date =new Date();
		String timeStr = sDataFromat.format(date);*/
        return getCurrTime() + File.separator + tel + File.separator + msgId + "." + suffix;
    }

    public static String getChatImageRemotePath(String tel) {
        return getCurrTime() + File.separator + tel + File.separator;
    }

    /**
     * 生成图片的文件名称
     *
     * @param tel    源号码
     * @param prefix 后缀名
     * @return
     */
    public static String getChatImageFileName(String tel, String prefix) {
        return tel + "_" + getMsgName() + "." + prefix;
    }

    /**
     * Description: 获取当前时间格式化成yyyymmdd
     *
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    private static String getCurrTime() {
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMdd");
        String currentTime = simpleDate.format(new Date());
        return currentTime;
    }

    /**
     * 获取当前时分秒的字符串+两位随机数字
     *
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    private static String getMsgName() {
        SimpleDateFormat simpleDate = new SimpleDateFormat("hhmmss");
        String currentTime = simpleDate.format(new Date());
        int i = 10 + (int) (Math.random() * 90);
        return currentTime + i;
    }

    public static boolean mkdir2(String path) {
        if (!isSDCardEnable()) {
            PrintLog.w("sd卡不可用");
            return false;
        }
        if (TextUtils.isEmpty(path)) {
            throw new NullPointerException("path==null");
        }
        String path1 = path.replace(File.separator, "#");
        String dir = "";
        String str[] = path1.split("#");
        for (int i = 0; i < str.length; i++) {
            dir += str[i] + File.separator;
            File file = new File(dir);
            if (!file.exists()) {
                boolean isSuccess = file.mkdir();
                if (!isSuccess) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public static boolean isFileInFolder(String path) {
        if (path.contains("PTYT/UctCommonApp/Msg") || path.contains("PTYT\\UctCommonApp\\Msg")) {
            return true;
        }
        return false;
    }

    public static String getChatImagePath(String tel) {
        return getSDCardBasePath() + tel + File.separator + "Image" + File.separator;
    }


    /**
     * 获取SD卡的剩余容量 单位byte
     *
     * @return
     */
    public static long getSDCardAllSize() {
        if (isSDCardEnable()) {
            StatFs stat = new StatFs(getSDCardBasePath());
            // 获取空闲的数据块的数量
            long availableBlocks = (long) stat.getAvailableBlocks() - 4;
            // 获取单个数据块的大小（byte）
            long freeBlocks = stat.getAvailableBlocks();
            return freeBlocks * availableBlocks;
        }
        return 0;
    }

    /**
     * 获取指定路径所在空间的剩余可用容量字节数，单位byte
     *
     * @param filePath
     * @return 容量字节 SDCard可用空间，内部存储可用空间
     */
    public static long getFreeBytes(String filePath) {
        // 如果是sd卡的下的路径，则获取sd卡可用容量
        if (filePath.startsWith(getSDCardBasePath())) {
            filePath = getSDCardBasePath();
        } else {// 如果是内部存储的路径，则获取内存存储的可用容量
            filePath = Environment.getDataDirectory().getAbsolutePath();
        }
        StatFs stat = new StatFs(filePath);
        long availableBlocks = (long) stat.getAvailableBlocks() - 4;
        return stat.getBlockSize() * availableBlocks;
    }

    /**
     * 获取SD卡的剩余容量 单位byte
     * (lc)
     *
     * @return
     */
    public static long getSDCardFreeSize() {
        if (isSDCardEnable()) {
            long blockSize;
            long totalBlocks;
            long availableBlocks;
            String path = getSDCardBasePath();
            mkdir2(path);
            StatFs stat = new StatFs(path);

            blockSize = stat.getBlockSize();
            totalBlocks = stat.getBlockCount();
            availableBlocks = stat.getAvailableBlocks();
            long availableSize = blockSize * availableBlocks;
            return availableSize;
        }
        return 0;
    }

    public static String getPrintSize(long size) {
        //如果字节数少于1024，则直接以B为单位，否则先除于1024，后3位因太少无意义
        if (size < 1024) {
            return String.valueOf(size) + "B";
        } else {
            size = size / 1024;
        }
        //如果原字节数除于1024之后，少于1024，则可以直接以KB作为单位
        //因为还没有到达要使用另一个单位的时候
        //接下去以此类推
        if (size < 1024) {
            return String.valueOf(size) + "KB";
        } else {
            size = size / 1024;
        }
        if (size < 1024) {
            //因为如果以MB为单位的话，要保留最后1位小数，
            //因此，把此数乘以100之后再取余
            size = size * 100;
            return String.valueOf((size / 100)) + "."
                    + String.valueOf((size % 100)) + "MB";
        } else {
            //否则如果要以GB为单位的，先除于1024再作同样的处理
            size = size * 100 / 1024;
            return String.valueOf((size / 100)) + "."
                    + String.valueOf((size % 100)) + "GB";
        }
    }

    /**
     * 获取手机内部空间总大小
     *
     * @return 大小，字节为单位
     */
    public static long getTotalInternalMemorySize() {
        // 获取内部存储根目录
        File path = Environment.getDataDirectory();
        // 系统的空间描述类
        StatFs stat = new StatFs(path.getPath());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return stat.getTotalBytes();
        } else {
            // 每个区块占字节数
            long blockSize = stat.getBlockSize();
            // 区块总数
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        }
    }

    /**
     * 获取手机内部可用空间大小
     *
     * @return 大小，字节为单位
     */
    public static long getAvailableInternalMemorySize() {
        String path = getSDCardBasePath();
        mkdir2(path);
        StatFs stat = new StatFs(path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return stat.getAvailableBytes();
        } else {
            // 每个区块占字节数
            long blockSize = stat.getBlockSize();
            // 获取可用区块数量
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        }
    }

    public static boolean isAvailableInternalMemory() {
        return (getAvailableInternalMemorySize() >= AvailableMinInternalMemorySize) ? true : false;
    }

    /**
     * 获取内置SDCard卡和外置SDCard卡路径
     *
     * @param mContext    这个参数大家都懂<br/>
     * @param is_removale 如果传入的是  true;表示该方法获取的是：外置sd卡路径<br/>
     *                    如果传入的是  false;表示该方法获取的是：内置sd卡路径<br/>
     * @return
     */

    public static String getStoragePath(Context mContext, boolean is_removale) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getLocalPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}