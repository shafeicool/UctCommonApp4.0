package com.ptyt.uct.utils;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.entity.MessageFileListEntity;
import com.ptyt.uct.entity.MessagePhotoEntity;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 描述：文件通用类
 * <p>
 * 创建时间 2017/5/12.
 * <p>
 * 创建者 崔晓锋
 * <p>
 * 版本：V1.0
 * <p>
 * * Method:
 * getSDCardPath()  SDCard路径
 * getSDCardTotal() 总容量
 * getSDCardFree() 可用容量
 * createSDDirection() 创建目录
 * creatSDFile（）创建文件
 * deleteSDDirection  删除目录
 * isFileExist 文件是否存在
 * deleteSDFile  删除文件
 * renameSDFile 修改文件或目录名称
 * copySDFileTo 拷贝单个文件
 * copySDFilesTo 拷贝所有
 * moveSDFileTo 移动单个
 * moveSDFilesTo 移动多个
 * creatDataDirection 建立私有目录
 * deleteDataFile 删除私有文件
 * deleteDataDir 删除私有目录
 * renameDataFile 修改私有文件名
 * copyDataFileTo 私有目录下复制
 * moveDataFileTo 移动私有文件
 * moveDataFilesTo 移动私有目录下所有文件
 * deleteFile 删除文件
 * deleteDirection 删除目录
 * copyFileTo 拷贝
 * copyFilesTo
 * moveFileTo 移动
 * moveFilesTo
 */
public class FileUtils {
    private Context mContext;
    private String SDPATH;    //sdCard路径
    private String FILESPATH; //应用文件路径

    private static FileUtils instanse;

    /**
     * 时间命名
     */
    private static final String TIME_STRING = "yyyyMMddHHmmss";
    /**
     * 限制图片最大宽度进行压缩
     */
    private static final int MAX_WIDTH = 720;
    /**
     * 限制图片最大高度进行压缩
     */
    private static final int MAX_HEIGHT = 1280;
    /**
     * 上传最大图片限制
     */
    private static final int MAX_UPLOAD_PHOTO_SIZE = 300 * 1024;
    /**
     * 缓存文件根目录名
     */
    private static final String FILE_DIR = "goPlatform";
    /**
     * 上传的照片文件路径
     */
    private static final String UPLOAD_FILE = "download/media";
    // 冒号
    public static final String COLON = ":";
    // 在当前会话，所有图片和视频的对象集合
    public static List<ConversationMsg> msgs = new ArrayList<>();
    // 在当前会话，由msgs转换的对象集合
    public static List<MessagePhotoEntity> datas = new ArrayList<>();


    public FileUtils(Context context) {
        this.mContext = context;
        SDPATH = Environment.getExternalStorageDirectory().getPath() + "//";
        FILESPATH = this.mContext.getFilesDir().getPath() + "//";
    }

    public static FileUtils getInstance(Context context) {
        if (instanse == null) {
            instanse = new FileUtils(context);
        }
        return instanse;
    }

    public void release() {
        if (instanse != null) {
            instanse = null;
        }
    }

    /**
     * 表示SDCard存在并且可以读写
     */
    public static boolean isSDCardState() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取上传的路径
     *
     * @return
     */
    public static String getUploadCachePath(Context context) {
        if (isSDCardState()) {
            String path = Environment.getExternalStorageDirectory() + File.separator + FILE_DIR + File.separator + UPLOAD_FILE + File.separator;
            File directory = new File(path);
            if (!directory.exists())
                directory.mkdirs();
            return path;
        } else {
            File directory = new File(context.getCacheDir(), FileUtils.FILE_DIR + File.separator + UPLOAD_FILE);
            if (!directory.exists())
                directory.mkdirs();
            return directory.getAbsolutePath();
        }
    }

    /**
     * jpg文件名
     *
     * @param context
     * @return
     */
    public static String getUploadPhotoFile(Context context) {
        return getUploadCachePath(context) + getTimeString() + ".jpg";
    }

    /**
     * mp4文件名
     *
     * @param context
     * @return
     */
    public static String getUploadVideoFile(Context context) {
        return getUploadCachePath(context) + getTimeString() + ".mp4";
    }

    public static String getTimeString() {
        return new SimpleDateFormat(TIME_STRING).format(new Date());
    }

    /**
     * 保存拍摄图片
     *
     * @param photoPath
     * @param data
     * @param isFrontFacing 是否为前置拍摄
     * @return
     */
    public static boolean savePhoto(String photoPath, byte[] data, boolean isFrontFacing) {
        if (photoPath != null && data != null) {
            FileOutputStream fos = null;
            try {
                Bitmap preBitmap = compressBitmap(data, MAX_WIDTH, MAX_HEIGHT);
                Bitmap roBm = rotationBitmap(preBitmap);
                if (isFrontFacing) {
                    Matrix matrix = new Matrix();
                    matrix.postScale(1, -1);
                    Bitmap newBitmap = Bitmap.createBitmap(roBm, 0, 0, roBm.getWidth(), preBitmap.getHeight(), matrix, true);
                    roBm.recycle();
                    roBm = newBitmap;
                }
                byte[] newDatas = compressBitmapToBytes(roBm, MAX_UPLOAD_PHOTO_SIZE);
                fos = new FileOutputStream(photoPath);
                fos.write(newDatas);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeCloseable(fos);
            }
        }
        return false;
    }

    /**
     * 三星手机适配
     *
     * @param bitmap
     * @return
     */
    private static Bitmap rotationBitmap(Bitmap bitmap) {
        String model = Build.MODEL;
        if (model.startsWith("SM-") || model.startsWith("GT-")) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return resizedBitmap;
        } else {
            return bitmap;
        }
    }

    /**
     * 把字节流按照图片方式大小进行压缩
     *
     * @param datas
     * @param w
     * @param h
     * @return
     */
    public static Bitmap compressBitmap(byte[] datas, int w, int h) {
        if (datas != null) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(datas, 0, datas.length, opts);
            if (opts.outWidth != 0 && opts.outHeight != 0) {
                PrintLog.d(opts.outWidth + " " + opts.outHeight);
                int scaleX = opts.outWidth / w;
                int scaleY = opts.outHeight / h;
                int scale = 1;
                if (scaleX >= scaleY && scaleX >= 1) {
                    scale = scaleX;
                }
                if (scaleX < scaleY && scaleY >= 1) {
                    scale = scaleY;
                }
                opts.inJustDecodeBounds = false;
                opts.inSampleSize = scale;
                PrintLog.d("compressBitmap inSampleSize " + datas.length + " " + scale);
                return BitmapFactory.decodeByteArray(datas, 0, datas.length, opts);
            }
        }
        return null;
    }

    /**
     * 质量压缩图片
     *
     * @param bitmap
     * @param maxSize
     * @return
     */
    public static byte[] compressBitmapToBytes(Bitmap bitmap, int maxSize) {
        if (bitmap == null)
            return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] datas = baos.toByteArray();
        int options = 80;
        int longs = datas.length;
        while (longs > maxSize && options > 0) {
            PrintLog.d("compressBitmapToBytes " + longs + "  " + options);
            baos.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            datas = baos.toByteArray();
            longs = datas.length;
            options -= 20;
        }
        return datas;
    }

    /**
     * 删除文件夹下所有文件,适当放到子线程中执行
     *
     * @param file
     */
    public static void delteFiles(File file) {
        if (file == null || !file.exists())
            return;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (!f.isDirectory()) {
                    f.delete();
                }
            }
        } else {
            file.delete();
        }
    }

    /**
     * 关闭资源
     *
     * @param close
     */
    public static void closeCloseable(Closeable close) {
        if (close != null) {
            try {
                close.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取SDCard文件路径
     */
    public String getSDCardPath() {
        if (isSDCardState()) {//如果SDCard存在并且可以读写
            SDPATH = Environment.getExternalStorageDirectory().getPath();
            return SDPATH;
        } else {
            return null;
        }
    }

    /**
     * 获取SDCard 总容量大小(MB)
     */
    public long getSDCardTotal() {
        if (null != getSDCardPath() && !getSDCardPath().equals("")) {
            StatFs statfs = new StatFs(getSDCardPath());
            //获取SDCard的Block总数
            long totalBlocks = statfs.getBlockCount();
            //获取每个block的大小
            long blockSize = statfs.getBlockSize();
            //计算SDCard 总容量大小MB
            long SDtotalSize = totalBlocks * blockSize / 1024 / 1024;
            return SDtotalSize;
        } else {
            return 0;
        }
    }

    /**
     * 获取SDCard 可用容量大小(MB)
     */
    public long getSDCardFree() {
        if (null != getSDCardPath() && !getSDCardPath().equals("")) {
            StatFs statfs = new StatFs(getSDCardPath());
            //获取SDCard的Block可用数
            long availaBlocks = statfs.getAvailableBlocks();
            //获取每个block的大小
            long blockSize = statfs.getBlockSize();
            //计算SDCard 可用容量大小MB
            long SDFreeSize = availaBlocks * blockSize / 1024 / 1024;
            return SDFreeSize;
        } else {
            return 0;
        }
    }

    /**
     * 在SD卡上创建目录
     *
     * @param dirName 要创建的目录名
     * @return 创建得到的目录
     */
    public File createSDDirection(String dirName) throws IOException {
        File dir = new File(SDPATH + "/" + dirName);
        dir.mkdir();
        return dir;
    }

    /**
     * 在SD卡上创建文件
     *
     * @param fileName 要创建的文件名
     * @return 创建得到的文件路径
     * @throws IOException
     */
    public File creatSDFile(String fileName) throws IOException {
        File file = new File(SDPATH + "/" + fileName);
        file.createNewFile();
        return file;
    }

    /**
     * 删除SD卡上的目录
     *
     * @param dirName
     */
    public boolean deleteSDDirection(String dirName) {
        File dir = new File(SDPATH + "/" + dirName);
        return deleteDirection(dir);
    }


    /**
     * 判断文件是否已经存在
     *
     * @param fileName 要检查的文件名
     * @return boolean, true表示存在，false表示不存在
     */
    public boolean isFileExist(String fileName) {
        File file = new File(SDPATH + "/" + fileName);
        return file.exists();
    }

    /**
     * 删除SD卡上的文件
     *
     * @param fileName 要删除文件路径
     */
    public boolean deleteSDFile(String fileName) {
        File file = new File(SDPATH + "/" + fileName);
        if (file == null || !file.exists() || file.isDirectory())
            return false;
        file.delete();
        return true;
    }


    /**
     * 修改SD卡上的文件或目录名
     *
     * @param oldfileName 旧文件名
     * @param newFileName 新文件名
     */
    public boolean renameSDFile(String oldfileName, String newFileName) {
        File oleFile = new File(SDPATH + "/" + oldfileName);
        File newFile = new File(SDPATH + "/" + newFileName);
        return oleFile.renameTo(newFile);
    }


    /**
     * 拷贝SD卡上的单个文件
     *
     * @param srcFileName  源文件
     * @param destFileName 目标目录
     * @throws IOException
     */

    public boolean copySDFileTo(String srcFileName, String destFileName) throws IOException {
        File srcFile = new File(SDPATH + "/" + srcFileName);
        File destFile = new File(SDPATH + "/" + destFileName);
        return copyFileTo(srcFile, destFile);
    }


    /**
     * 拷贝SD卡上指定目录的所有文件
     *
     * @param srcDirName
     * @param destDirName
     * @return
     * @throws IOException
     */
    public boolean copySDFilesTo(String srcDirName, String destDirName) throws IOException {
        File srcDir = new File(SDPATH + "/" + srcDirName);
        File destDir = new File(SDPATH + "/" + destDirName);
        return copyFilesTo(srcDir, destDir);
    }


    /**
     * 移动SD卡上的单个文件
     *
     * @param srcFileName
     * @param destFileName
     * @return
     * @throws IOException
     */

    public boolean moveSDFileTo(String srcFileName, String destFileName) throws IOException {
        File srcFile = new File(SDPATH + "/" + srcFileName);
        File destFile = new File(SDPATH + "/" + destFileName);
        return moveFileTo(srcFile, destFile);
    }


    /**
     * 移动SD卡上的指定目录的所有文件
     *
     * @param srcDirName
     * @param destDirName
     * @return
     * @throws IOException
     */

    public boolean moveSDFilesTo(String srcDirName, String destDirName) throws IOException {
        File srcDir = new File(SDPATH + "/" + srcDirName);
        File destDir = new File(SDPATH + "/" + destDirName);
        return moveFilesTo(srcDir, destDir);
    }


    /**
     * 建立私有目录
     *
     * @param dirName
     * @return
     */
    public File creatDataDirection(String dirName) {
        File dir = new File(FILESPATH + "/" + dirName);
        dir.mkdir();
        return dir;
    }


    /**
     * 删除私有文件
     *
     * @param fileName
     * @return
     */
    public boolean deleteDataFile(String fileName) {
        File file = new File(FILESPATH + "/" + fileName);
        return deleteFile(file);
    }


    /**
     * 删除私有目录
     *
     * @param dirName
     * @return
     */
    public boolean deleteDataDir(String dirName) {
        File file = new File(FILESPATH + "/" + dirName);
        return deleteDirection(file);
    }


    /**
     * 更改私有文件名
     *
     * @param oldName
     * @param newName
     * @return
     */

    public boolean renameDataFile(String oldName, String newName) {
        File oldFile = new File(FILESPATH + "/" + oldName);
        File newFile = new File(FILESPATH + "/" + newName);
        return oldFile.renameTo(newFile);
    }


    /**
     * 在私有目录下进行文件复制
     *
     * @param srcFileName  ： 包含路径及文件名
     * @param destFileName
     * @return
     * @throws IOException
     */

    public boolean copyDataFileTo(String srcFileName, String destFileName) throws IOException {
        File srcFile = new File(FILESPATH + "/" + srcFileName);
        File destFile = new File(FILESPATH + "/" + destFileName);
        return copyFileTo(srcFile, destFile);
    }


    /**
     * 复制私有目录里指定目录的所有文件
     *
     * @param srcDirName
     * @param destDirName
     * @return
     * @throws IOException
     */

    public boolean copyDataFilesTo(String srcDirName, String destDirName)

            throws IOException {
        File srcDir = new File(FILESPATH + "/" + srcDirName);
        File destDir = new File(FILESPATH + "/" + destDirName);
        return copyFilesTo(srcDir, destDir);
    }


    /**
     * 移动私有目录下的单个文件
     *
     * @param srcFileName
     * @param destFileName
     * @return
     * @throws IOException
     */

    public boolean moveDataFileTo(String srcFileName, String destFileName)

            throws IOException {
        File srcFile = new File(FILESPATH + "/" + srcFileName);
        File destFile = new File(FILESPATH + "/" + destFileName);
        return moveFileTo(srcFile, destFile);
    }


    /**
     * 移动私有目录下的指定目录下的所有文件
     *
     * @param srcDirName
     * @param destDirName
     * @return
     * @throws IOException
     */

    public boolean moveDataFilesTo(String srcDirName, String destDirName)

            throws IOException {
        File srcDir = new File(FILESPATH + "/" + srcDirName);
        File destDir = new File(FILESPATH + "/" + destDirName);
        return moveFilesTo(srcDir, destDir);
    }


    /**
     * 删除一个文件
     *
     * @param file
     * @return
     */

    public boolean deleteFile(File file) {
        if (file.isDirectory())
            return false;
        return file.delete();
    }

    /**
     * 删除一个目录（可以是非空目录）
     *
     * @param dir
     */

    public boolean deleteDirection(File dir) {
        if (dir == null || !dir.exists() || dir.isFile()) {
            return false;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                deleteDirection(file);// 递归
            }
        }
        dir.delete();
        return true;
    }

    /**
     * 拷贝一个文件,srcFile源文件，destFile目标文件
     *
     * @param srcFile 源文件
     *                destFile 目标文件
     * @throws IOException
     */

    public boolean copyFileTo(File srcFile, File destFile) throws IOException {

        if (srcFile.isDirectory() || destFile.isDirectory())
            return false;// 判断是否是文件
        FileInputStream fis = new FileInputStream(srcFile);
        FileOutputStream fos = new FileOutputStream(destFile);
        int readLen = 0;
        byte[] buf = new byte[1024];
        while ((readLen = fis.read(buf)) != -1) {
            fos.write(buf, 0, readLen);
        }
        fos.flush();
        fos.close();
        fis.close();
        return true;
    }
    /**
     * 保存raw中文件至sd卡中
     * @throws Throwable
     */
    public static void saveToSDCard(Context context,int rawFileRes,String path) throws Throwable {
        InputStream inStream = context.getResources().openRawResource(rawFileRes);
        File file = new File(path);
        FileOutputStream fileOutputStream = new FileOutputStream(file);//存入SDCard
        byte[] buffer = new byte[10];
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        int len = 0;
        while((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] bs = outStream.toByteArray();
        fileOutputStream.write(bs);
        outStream.close();
        inStream.close();
        fileOutputStream.flush();
        fileOutputStream.close();
    }
    //判断文件是否存在
    public static boolean fileIsExists(String strFile)
    {
        try
        {
            File f=new File(strFile);
            if(!f.exists())
            {
                return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }
    /**
     * 拷贝目录下的所有文件到指定目录
     *
     * @param srcDir
     * @param destDir
     * @return
     * @throws IOException
     */

    public boolean copyFilesTo(File srcDir, File destDir) throws IOException {

        if (!srcDir.isDirectory() || !destDir.isDirectory())
            return false;// 判断是否是目录
        if (!destDir.exists())
            return false;// 判断目标目录是否存在
        File[] srcFiles = srcDir.listFiles();
        for (int i = 0; i < srcFiles.length; i++) {
            if (srcFiles[i].isFile()) {
                // 获得目标文件
                File destFile = new File(destDir.getPath() + "//"
                        + srcFiles[i].getName());
                copyFileTo(srcFiles[i], destFile);
            } else if (srcFiles[i].isDirectory()) {
                File theDestDir = new File(destDir.getPath() + "//"
                        + srcFiles[i].getName());
                copyFilesTo(srcFiles[i], theDestDir);
            }
        }
        return true;
    }

    /**
     * 移动一个文件
     *
     * @param srcFile
     * @param destFile
     * @return
     * @throws IOException
     */

    public boolean moveFileTo(File srcFile, File destFile) throws IOException {

        boolean is_copy = copyFileTo(srcFile, destFile);

        if (!is_copy)
            return false;
        deleteFile(srcFile);
        return true;
    }

    /**
     * 移动目录下的所有文件到指定目录
     *
     * @param srcDir
     * @param destDir
     * @return
     * @throws IOException
     */

    public boolean moveFilesTo(File srcDir, File destDir) throws IOException {
        if (!srcDir.isDirectory() || !destDir.isDirectory()) {
            return false;
        }

        File[] srcDirFiles = srcDir.listFiles();
        for (int i = 0; i < srcDirFiles.length; i++) {
            if (srcDirFiles[i].isFile()) {
                File oneDestFile = new File(destDir.getPath() + "//"
                        + srcDirFiles[i].getName());
                moveFileTo(srcDirFiles[i], oneDestFile);
                deleteFile(srcDirFiles[i]);
            } else if (srcDirFiles[i].isDirectory()) {
                File oneDestFile = new File(destDir.getPath() + "//"
                        + srcDirFiles[i].getName());
                moveFilesTo(srcDirFiles[i], oneDestFile);
                deleteDirection(srcDirFiles[i]);
            }
        }
        return true;
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(Context context, String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            boolean b = file.delete();
            updateMediaDatabases(context, filePath);
            return b;
        }
        return false;
    }

    public static void updateMediaDatabases(Context context, String filePath)//filename是我们的文件全名，包括后缀哦
    {
        MediaScannerConnection.scanFile(context,
                new String[]{filePath}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
    }

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(Context context, String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(context, files[i].getAbsolutePath());
                if (!flag)
                    break;
            } else {
                //删除子目录
                flag = deleteDirectory(context, files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag)
            return false;
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     *
     * @param filePath 要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
     */
    public static boolean deleteFolder(Context context, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFile(context, filePath);
            } else {
                // 为目录时调用删除目录方法
                return deleteDirectory(context, filePath);
            }
        }
    }

    public static int getProgress(Long offsize, Long fileSize) {
        if (offsize != null && fileSize != null) {
            if (fileSize == 0) {
                return 0;
            }
            return (int) (((float) offsize / (float) fileSize) * 100);
        }
        return 0;
    }

    public static Intent openFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists())
            return null;
        /* 取得扩展名 */
        String end = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length()).toLowerCase(Locale.ENGLISH);
        /* 依扩展名的类型决定MimeType */
        if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") || end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
            return getAudioFileIntent(filePath);
        } else if (end.equals("3gp") || end.equals("mp4")) {
            return getAudioFileIntent(filePath);
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")) {
            return getImageFileIntent(filePath);
        } else if (end.equals("apk")) {
            return getApkFileIntent(filePath);
        } else if (end.equals("ppt")) {
            return getPptFileIntent(filePath);
        } else if (end.equals("xls")) {
            return getExcelFileIntent(filePath);
        } else if (end.equals("doc")) {
            return getDocFileIntent(filePath);
        } else if (end.equals("docx")) {
            return getDocxFileIntent(filePath);
        } else if (end.equals("pdf")) {
            return getPdfFileIntent(filePath);
        } else if (end.equals("chm")) {
            return getChmFileIntent(filePath);
        } else if (end.equals("txt")) {
            return getTextFileIntent(filePath, false);
        } else {
            return getAllIntent(filePath);
        }
    }

    //Android获取一个用于打开APK文件的intent
    public static Intent getAllIntent(String param) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "*/*");
        return intent;
    }

    //Android获取一个用于打开APK文件的intent
    public static Intent getApkFileIntent(String param) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        return intent;
    }

    //Android获取一个用于打开VIDEO文件的intent
    public static Intent getVideoFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "video/*");
        return intent;
    }

    //Android获取一个用于打开AUDIO文件的intent
    public static Intent getAudioFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "video/*");
        return intent;
    }

    //Android获取一个用于打开Html文件的intent
    public static Intent getHtmlFileIntent(String param) {

        Uri uri = Uri.parse(param).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content").encodedPath(param).build();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uri, "text/html");
        return intent;
    }

    //Android获取一个用于打开图片文件的intent
    public static Intent getImageFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "image/*");
        return intent;
    }

    //Android获取一个用于打开PPT文件的intent
    public static Intent getPptFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        return intent;
    }

    //Android获取一个用于打开Excel文件的intent
    public static Intent getExcelFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        return intent;
    }

    //Android获取一个用于打开Word文件的intent
    public static Intent getDocFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/msword");
        return intent;
    }

    //Android获取一个用于打开Word文件的intent
    public static Intent getDocxFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        return intent;
    }

    //Android获取一个用于打开CHM文件的intent
    public static Intent getChmFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/x-chm");
        return intent;
    }

    //Android获取一个用于打开文本文件的intent
    public static Intent getTextFileIntent(String param, boolean paramBoolean) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (paramBoolean) {
            Uri uri1 = Uri.parse(param);
            intent.setDataAndType(uri1, "text/plain");
        } else {
            Uri uri2 = Uri.fromFile(new File(param));
            intent.setDataAndType(uri2, "text/plain");
        }
        return intent;
    }

    //Android获取一个用于打开PDF文件的intent
    public static Intent getPdfFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/pdf");
        return intent;
    }

    public static void scanFile(Context mContext, String path) {
        if (StrUtils.isEmpty(path)) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// 如果是4.4及以上版本
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(new File(path)); //out is your output file
            mediaScanIntent.setData(contentUri);
            mContext.sendBroadcast(mediaScanIntent);
        } else {
            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    /**
     * 获取文件名不带后缀的，例如test.jpeg  返回结果为test
     *
     * @param fullFileName 带后缀的全文件名称
     * @return 返回去掉后缀的文件名称
     */
    public static String subFileName(String fullFileName) {
        if (TextUtils.isEmpty(fullFileName)) {
            return fullFileName;
        }
        int index = fullFileName.lastIndexOf(".");
        if (index != -1) {
            return fullFileName.substring(0, index);
        }
        return fullFileName;
    }

    public static final String OTHER = "other";
    public static final String WORD = "word";
    public static final String EXCEL = "excel";
    public static final String ZIP = "zip";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public List<MessageFileListEntity> getSpecificTypeOfFile(Context context, String[] extension) {

        List<MessageFileListEntity> list = new ArrayList<>();
        //从外存中获取
        Uri fileUri = MediaStore.Files.getContentUri("external");
        //筛选列，这里只筛选了：文件路径和不含后缀的文件名
        String[] projection = new String[]{
                MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.TITLE
        };
        //构造筛选语句
        String selection = "";
        for (int i = 0; i < extension.length; i++) {
            if (i != 0) {
                selection = selection + " OR ";
            }
            selection = selection + MediaStore.Files.FileColumns.DATA + " LIKE '%" + extension[i] + "'";
        }
        //按时间递增顺序对结果进行排序;待会从后往前移动游标就可实现时间递减
        String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED;
        //获取内容解析器对象
        ContentResolver resolver = context.getContentResolver();
        //获取游标
        Cursor cursor = resolver.query(fileUri, null, selection, null, sortOrder);

        if (cursor == null)
            return null;
        //游标从最后开始往前递减，以此实现时间递减顺序（最近访问的文件，优先显示）
        if (cursor.moveToLast()) {
            do {
                //输出文件的完整路径
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                if (!StrUtils.isEmpty(path) && (!(new File(path).isFile()))) {
                    continue;
                }
                String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE));
                String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
                String suffixName = path.substring(path.lastIndexOf(File.separator) + 1, path.length());
                Long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                Long time = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)); // 单位：秒
                PrintLog.i("FileUtils--path = " + path + ", type = " + mimeType + ", displayName = " + displayName + ", suffixName = " + suffixName + ", size = " + size);

                if (!(new File(path).canRead())) {
                    PrintLog.e("getSpecificTypeOfFile() path = " + path + " can not be read");
                    continue;
                }

                MessageFileListEntity entity = new MessageFileListEntity();
                entity.setPath(path);
                entity.setDisplayName(displayName);
                entity.setSuffixName(suffixName);
                entity.setSize(size);
                entity.setTime(time * 1000);

                for (int i = 0; i < MIME_MapTable.length; i++) {
                    if (!StrUtils.isEmpty(mimeType) && mimeType.equals(MIME_MapTable[i][1]) || !StrUtils.isEmpty(suffixName) && getSuffix(suffixName).equals(MIME_MapTable[i][0])) {
                        switch (MIME_MapTable[i][2]) {
                            case EXCEL:
                                entity.setType(EXCEL);
                                break;
                            case WORD:
                                entity.setType(WORD);
                                break;
                            case ZIP:
                                entity.setType(ZIP);
                                break;
                            default:
                                entity.setType(OTHER);
                                break;
                        }
                        break;
                    } else {
                        if (i == (MIME_MapTable.length - 1)) {
                            entity.setType(OTHER);
                        }
                    }
                }

                list.add(entity);
            } while (cursor.moveToPrevious());
        }
        cursor.close();
        return list;
    }

    private static String getSuffix(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return null;
        }
        String fileName = file.getName();
        if (fileName.equals("") || fileName.endsWith(".")) {
            return null;
        }
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(index + 1).toLowerCase(Locale.US);
        } else {
            return null;
        }
    }

    // 获取后缀名，不带"."
    public static String getSuffix(String path) {
        if (path.equals("") || path.endsWith(".")) {
            return null;
        }
        int index = path.lastIndexOf(".");
        if (index != -1) {
            return path.substring(index + 1).toLowerCase(Locale.US);
        } else {
            return null;
        }
    }

    public static String getFileNameFromPath(String path) {
        if (path.equals("") || path.endsWith(".")) {
            return null;
        }
        int index = path.lastIndexOf("/");
        if (index != -1) {
            return path.substring(index + 1);
        } else {
            return null;
        }
    }

    public static String FormatFileSize(Long fileS) { // 转换文件大小
        if (fileS == null) {
            return null;
        }
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS <= 0L) {
            fileSizeString = "0B";
        } else if (fileS < 1024L) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576L) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824L) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    public static Long getFileSize(String path) throws Exception {
        long size;
        File file = new File(path);
        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            size = fis.available();
        } else {
            return null;
        }
        return size;
    }

    /**
     * @param size
     * @return
     * @description 计算文件大小，用于发送短信的smsId，去掉最后面的B
     */
    public static String getFileSizeUnit(Long size) {
        if (size == null || size <= 0)
            return "0";
        final String[] units = new String[]{"", "K", "M", "G", "T"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("####.##").format(size
                / Math.pow(1024, digitGroups))
                + units[digitGroups];
    }

    /**
     * @param sourceSize
     * @return fileSize
     * @description 带有单位的size转化成字节size
     */
    public static long UnformatFileSize(String sourceSize) {
        long fileSize = 0L;
        if (isMatchs(sourceSize)) {
            fileSize = Long.parseLong(sourceSize);
        } else {
            String sizeData = sourceSize.substring(0, (sourceSize.length() - 1));
            String unit = sourceSize.substring((sourceSize.length() - 1));
            if (isMatchsDecimal(sizeData) &&
                    (unit.equalsIgnoreCase("K")
                            || unit.equalsIgnoreCase("M")
                            || unit.equalsIgnoreCase("G"))) {
                double length = Double.parseDouble(sizeData);
                if (unit.equalsIgnoreCase("K")) {
                    fileSize = (long) (length * 1024);
                } else if (unit.equalsIgnoreCase("M")) {
                    fileSize = (long) (length * 1024 * 1024);
                } else if (unit.equalsIgnoreCase("G")) {
                    fileSize = (long) (length * 1024 * 1024 * 1024);
                } else {
                    PrintLog.e("文件大小的单位不正确，unit = 【" + unit + "】");
                }
            }
        }
        return fileSize;
    }

    private static boolean isMatchsDecimal(String value) {
        return (TextUtils.isEmpty(value)) ? false : (value.matches("^[0-9]+(.[0-9]+)?$"));
    }

    private static boolean isMatchs(String value) {
        return value.matches("[0-9]+");
    }

    public static double FormatDouble(double number) {
        BigDecimal bigDecimal = new BigDecimal(number);
        return bigDecimal.setScale(4, BigDecimal.ROUND_DOWN).doubleValue();
    }

    public static String getMimeType(File file) {
        String suffix = getSuffix(file);
        if (suffix == null) {
            return "file/*";
        }
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        if (type != null || !type.isEmpty()) {
            return type;
        }
        return "file/*";
    }

    public static final String[][] MIME_MapTable = {
            //            {"apk", "application/vnd.android.package-archive", "apk"},
            {"xla", "application/vnd.ms-excel", "excel"},
            {"xlc", "application/vnd.ms-excel", "excel"},
            {"xll", "application/x-excel", "excel"},
            {"xlm", "application/vnd.ms-excel", "excel"},
            {"xls", "application/vnd.ms-excel", "excel"},
            {"xlt", "application/vnd.ms-excel", "excel"},
            {"xlw", "application/vnd.ms-excel", "excel"},
            {"xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "excel"},

            {"doc", "application/msword", "word"},
            {"docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "word"},
            {"dot", "application/x-dot", "word"},

            {"bz2", "application/x-bzip2", "zip"},
            {"gtar", "application/x-gtar", "zip"},
            {"gz", "application/x-gzip", "zip"},
            {"jar", "application/java-archive", "zip"},
            {"lzh", "application/x-lzh", "zip"},
            {"nar", "application/zip", "zip"},
            {"rar", "application/rar", "zip"},
            {"rar", "application/x-rar-compressed", "zip"},
            {"uu", "application/x-uuencode", "zip"},
            {"uue", "application/x-uuencode", "zip"},
            {"x-gzip", "application/x-gzip", "zip"},
            {"z", "application/x-compress", "zip"},
            {"zip", "application/zip", "zip"},

            {"txt", "text/plain", "other"},
            {"awb", "audio/amr-wb", "other"},
            {"m4a", "audio/mp4a-latm", "other"},
            {"mid", "audio/midi", "other"},
            {"midi", "audio/midi", "other"},
            {"mp2", "audio/x-mpeg", "other"},
            {"mp3", "audio/x-mpeg", "other"},
            {"mp4", "video/mp4", "other"},
            {"ogg", "audio/ogg", "other"},
            {"wav", "audio/x-wav", "other"}
    };

    private static final String[][] MIME_MapTable2 = {
            //{后缀名，MIME类型, 文件类型}
            {"3gp", "video/3gpp"},
            {"aab", "application/x-authoware-bin"},
            {"aam", "application/x-authoware-map"},
            {"aas", "application/x-authoware-seg"},
            {"ai", "application/postscript"},
            {"aif", "audio/x-aiff"},
            {"aifc", "audio/x-aiff"},
            {"aiff", "audio/x-aiff"},
            {"als", "audio/X-Alpha5"},
            {"amc", "application/x-mpeg"},
            {"ani", "application/octet-stream"},
            {"apk", "application/vnd.android.package-archive", "apk"},
            {"asc", "text/plain"},
            {"asd", "application/astound"},
            {"asf", "video/x-ms-asf"},
            {"asn", "application/astound"},
            {"asp", "application/x-asap"},
            {"asx", "video/x-ms-asf"},
            {"au", "audio/basic"},
            {"avb", "application/octet-stream"},
            {"avi", "video/x-msvideo"},
            {"awb", "audio/amr-wb"},
            {"bcpio", "application/x-bcpio"},
            {"bin", "application/octet-stream"},
            {"bld", "application/bld"},
            {"bld2", "application/bld2"},
            {"bmp", "image/bmp"},
            {"bpk", "application/octet-stream"},
            {"bz2", "application/x-bzip2", "zip"},
            {"c", "text/plain"},
            {"cal", "image/x-cals"},
            {"ccn", "application/x-cnc"},
            {"cco", "application/x-cocoa"},
            {"cdf", "application/x-netcdf"},
            {"cgi", "magnus-internal/cgi"},
            {"chat", "application/x-chat"},
            {"class", "application/octet-stream"},
            {"clp", "application/x-msclip"},
            {"cmx", "application/x-cmx"},
            {"co", "application/x-cult3d-object"},
            {"cod", "image/cis-cod"},
            {"conf", "text/plain"},
            {"cpio", "application/x-cpio"},
            {"cpp", "text/plain"},
            {"cpt", "application/mac-compactpro"},
            {"crd", "application/x-mscardfile"},
            {"csh", "application/x-csh"},
            {"csm", "chemical/x-csml"},
            {"csml", "chemical/x-csml"},
            {"css", "text/css"},
            {"cur", "application/octet-stream"},
            {"dcm", "x-lml/x-evm"},
            {"dcr", "application/x-director"},
            {"dcx", "image/x-dcx"},
            {"dhtml", "text/html"},
            {"dir", "application/x-director"},
            {"dll", "application/octet-stream"},
            {"dmg", "application/octet-stream"},
            {"dms", "application/octet-stream"},
            {"doc", "application/msword", "word"},
            {"docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "word"},
            {"dot", "application/x-dot", "word"},
            {"dvi", "application/x-dvi"},
            {"dwf", "drawing/x-dwf"},
            {"dwg", "application/x-autocad"},
            {"dxf", "application/x-autocad"},
            {"dxr", "application/x-director"},
            {"ebk", "application/x-expandedbook"},
            {"emb", "chemical/x-embl-dl-nucleotide"},
            {"embl", "chemical/x-embl-dl-nucleotide"},
            {"eps", "application/postscript"},
            {"eri", "image/x-eri"},
            {"es", "audio/echospeech"},
            {"esl", "audio/echospeech"},
            {"etc", "application/x-earthtime"},
            {"etx", "text/x-setext"},
            {"evm", "x-lml/x-evm"},
            {"evy", "application/x-envoy"},
            {"exe", "application/octet-stream"},
            {"fh4", "image/x-freehand"},
            {"fh5", "image/x-freehand"},
            {"fhc", "image/x-freehand"},
            {"fif", "image/fif"},
            {"fm", "application/x-maker"},
            {"fpx", "image/x-fpx"},
            {"fvi", "video/isivideo"},
            {"gau", "chemical/x-gaussian-input"},
            {"gca", "application/x-gca-compressed"},
            {"gdb", "x-lml/x-gdb"},
            {"gif", "image/gif"},
            {"gps", "application/x-gps"},
            {"gtar", "application/x-gtar", "zip"},
            {"gz", "application/x-gzip", "zip"},
            {"h", "text/plain"},
            {"hdf", "application/x-hdf"},
            {"hdm", "text/x-hdml"},
            {"hdml", "text/x-hdml"},
            {"hlp", "application/winhlp"},
            {"hqx", "application/mac-binhex40"},
            {"htm", "text/html"},
            {"html", "text/html"},
            {"hts", "text/html"},
            {"ice", "x-conference/x-cooltalk"},
            {"ico", "application/octet-stream"},
            {"ief", "image/ief"},
            {"ifm", "image/gif"},
            {"ifs", "image/ifs"},
            {"imy", "audio/melody"},
            {"ins", "application/x-NET-Install"},
            {"ips", "application/x-ipscript"},
            {"ipx", "application/x-ipix"},
            {"it", "audio/x-mod"},
            {"itz", "audio/x-mod"},
            {"ivr", "i-world/i-vrml"},
            {"j2k", "image/j2k"},
            {"jad", "text/vnd.sun.j2me.app-descriptor"},
            {"jam", "application/x-jam"},
            {"jar", "application/java-archive", "zip"},
            {"java", "text/plain"},
            {"jnlp", "application/x-java-jnlp-file"},
            {"jpe", "image/jpeg"},
            {"jpeg", "image/jpeg"},
            {"jpg", "image/jpeg"},
            {"jpz", "image/jpeg"},
            {"js", "application/x-javascript"},
            {"jwc", "application/jwc"},
            {"kjx", "application/x-kjx"},
            {"lak", "x-lml/x-lak"},
            {"latex", "application/x-latex"},
            {"lcc", "application/fastman"},
            {"lcl", "application/x-digitalloca"},
            {"lcr", "application/x-digitalloca"},
            {"lgh", "application/lgh"},
            {"lha", "application/octet-stream"},
            {"lml", "x-lml/x-lml"},
            {"lmlpack", "x-lml/x-lmlpack"},
            {"log", "text/plain"},
            {"lsf", "video/x-ms-asf"},
            {"lsx", "video/x-ms-asf"},
            {"lzh", "application/x-lzh", "zip"},
            {"m13", "application/x-msmediaview"},
            {"m14", "application/x-msmediaview"},
            {"m15", "audio/x-mod"},
            {"m3u", "audio/x-mpegurl"},
            {"m3url", "audio/x-mpegurl"},
            {"m4a", "audio/mp4a-latm"},
            {"m4b", "audio/mp4a-latm"},
            {"m4p", "audio/mp4a-latm"},
            {"m4u", "video/vnd.mpegurl"},
            {"m4v", "video/x-m4v"},
            {"ma1", "audio/ma1"},
            {"ma2", "audio/ma2"},
            {"ma3", "audio/ma3"},
            {"ma5", "audio/ma5"},
            {"man", "application/x-troff-man"},
            {"map", "magnus-internal/imagemap"},
            {"mbd", "application/mbedlet"},
            {"mct", "application/x-mascot"},
            {"mdb", "application/x-msaccess"},
            {"mdz", "audio/x-mod"},
            {"me", "application/x-troff-me"},
            {"mel", "text/x-vmel"},
            {"mi", "application/x-mif"},
            {"mid", "audio/midi"},
            {"midi", "audio/midi"},
            {"mif", "application/x-mif"},
            {"mil", "image/x-cals"},
            {"mio", "audio/x-mio"},
            {"mmf", "application/x-skt-lbs"},
            {"mng", "video/x-mng"},
            {"mny", "application/x-msmoney"},
            {"moc", "application/x-mocha"},
            {"mocha", "application/x-mocha"},
            {"mod", "audio/x-mod"},
            {"mof", "application/x-yumekara"},
            {"mol", "chemical/x-mdl-molfile"},
            {"mop", "chemical/x-mopac-input"},
            {"mov", "video/quicktime"},
            {"movie", "video/x-sgi-movie"},
            {"mp2", "audio/x-mpeg"},
            {"mp3", "audio/x-mpeg"},
            {"mp4", "video/mp4"},
            {"mpc", "application/vnd.mpohun.certificate"},
            {"mpe", "video/mpeg"},
            {"mpeg", "video/mpeg"},
            {"mpg video/mpeg"},
            {"mpg4", "video/mp4"},
            {"mpga", "audio/mpeg"},
            {"mpn", "application/vnd.mophun.application"},
            {"mpp", "application/vnd.ms-project"},
            {"mps", "application/x-mapserver"},
            {"mrl", "text/x-mrml"},
            {"mrm", "application/x-mrm"},
            {"ms", "application/x-troff-ms"},
            {"msg", "application/vnd.ms-outlook"},
            {"mts", "application/metastream"},
            {"mtx", "application/metastream"},
            {"mtz", "application/metastream"},
            {"mzv", "application/metastream"},
            {"nar", "application/zip"},
            {"nbmp", "image/nbmp"},
            {"nc", "application/x-netcdf"},
            {"ndb", "x-lml/x-ndb"},
            {"ndwn", "application/ndwn"},
            {"nif", "application/x-nif"},
            {"nmz", "application/x-scream"},
            {"nokia-op-logo", "image/vnd.nok-oplogo-color"},
            {"npx", "application/x-netfpx"},
            {"nsnd", "audio/nsnd"},
            {"nva", "application/x-neva1"},
            {"oda", "application/oda"},
            {"ogg", "audio/ogg"},
            {"oom", "application/x-AtlasMate-Plugin"},
            {"pac", "audio/x-pac"},
            {"pae", "audio/x-epac"},
            {"pan", "application/x-pan"},
            {"pbm", "image/x-portable-bitmap"},
            {"pcx", "image/x-pcx"},
            {"pda", "image/x-pda"},
            {"pdb", "chemical/x-pdb"},
            {"pdf", "application/pdf"},
            {"pfr", "application/font-tdpfr"},
            {"pgm", "image/x-portable-graymap"},
            {"pict", "image/x-pict"},
            {"pm", "application/x-perl"},
            {"pmd", "application/x-pmd"},
            {"png", "image/png"},
            {"pnm", "image/x-portable-anymap"},
            {"pnz", "image/png"},
            {"pot", "application/vnd.ms-powerpoint"},
            {"ppm", "image/x-portable-pixmap"},
            {"pps", "application/vnd.ms-powerpoint"},
            {"ppt", "application/vnd.ms-powerpoint"},
            {"pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {"pqf", "application/x-cprplayer"},
            {"pqi", "application/cprplayer"},
            {"prc", "application/x-prc"},
            {"prop", "text/plain"},
            {"proxy", "application/x-ns-proxy-autoconfig"},
            {"ps", "application/postscript"},
            {"ptlk", "application/listenup"},
            {"pub", "application/x-mspublisher"},
            {"pvx", "video/x-pv-pvx"},
            {"qcp", "audio/vnd.qcelp"},
            {"qt", "video/quicktime"},
            {"qti", "image/x-quicktime"},
            {"qtif", "image/x-quicktime"},
            {"r3t", "text/vnd.rn-realtext3d"},
            {"ra", "audio/x-pn-realaudio"},
            {"ram", "audio/x-pn-realaudio"},
            {"rar", "application/x-rar-compressed", "zip"},
            {"ras", "image/x-cmu-raster"},
            {"rc", "text/plain"},
            {"rdf", "application/rdf+xml"},
            {"rf", "image/vnd.rn-realflash"},
            {"rgb", "image/x-rgb"},
            {"rlf", "application/x-richlink"},
            {"rm", "audio/x-pn-realaudio"},
            {"rmf", "audio/x-rmf"},
            {"rmm", "audio/x-pn-realaudio"},
            {"rmvb", "audio/x-pn-realaudio"},
            {"rnx", "application/vnd.rn-realplayer"},
            {"roff", "application/x-troff"},
            {"rp", "image/vnd.rn-realpix"},
            {"rpm", "audio/x-pn-realaudio-plugin"},
            {"rt", "text/vnd.rn-realtext"},
            {"rte", "x-lml/x-gps"},
            {"rtf", "application/rtf"},
            {"rtg", "application/metastream"},
            {"rtx", "text/richtext"},
            {"rv", "video/vnd.rn-realvideo"},
            {"rwc", "application/x-rogerwilco"},
            {"s3m", "audio/x-mod"},
            {"s3z", "audio/x-mod"},
            {"sca", "application/x-supercard"},
            {"scd", "application/x-msschedule"},
            {"sdf", "application/e-score"},
            {"sea", "application/x-stuffit"},
            {"sgm", "text/x-sgml"},
            {"sgml", "text/x-sgml"},
            {"sh", "application/x-sh"},
            {"shar", "application/x-shar"},
            {"shtml", "magnus-internal/parsed-html"},
            {"shw", "application/presentations"},
            {"si6", "image/si6"},
            {"si7", "image/vnd.stiwap.sis"},
            {"si9", "image/vnd.lgtwap.sis"},
            {"sis", "application/vnd.symbian.install"},
            {"sit", "application/x-stuffit"},
            {"skd", "application/x-Koan"},
            {"skm", "application/x-Koan"},
            {"skp", "application/x-Koan"},
            {"skt", "application/x-Koan"},
            {"slc", "application/x-salsa"},
            {"smd", "audio/x-smd"},
            {"smi", "application/smil"},
            {"smil", "application/smil"},
            {"smp", "application/studiom"},
            {"smz", "audio/x-smd"},
            {"snd", "audio/basic"},
            {"spc", "text/x-speech"},
            {"spl", "application/futuresplash"},
            {"spr", "application/x-sprite"},
            {"sprite", "application/x-sprite"},
            {"spt", "application/x-spt"},
            {"src", "application/x-wais-source"},
            {"stk", "application/hyperstudio"},
            {"stm", "audio/x-mod"},
            {"sv4cpio", "application/x-sv4cpio"},
            {"sv4crc", "application/x-sv4crc"},
            {"svf", "image/vnd"},
            {"svg", "image/svg-xml"},
            {"svh", "image/svh"},
            {"svr", "x-world/x-svr"},
            {"swf", "application/x-shockwave-flash"},
            {"swfl", "application/x-shockwave-flash"},
            {"t", "application/x-troff"},
            {"tad", "application/octet-stream"},
            {"talk", "text/x-speech"},
            {"tar", "application/x-tar"},
            {"taz", "application/x-tar"},
            {"tbp", "application/x-timbuktu"},
            {"tbt", "application/x-timbuktu"},
            {"tcl", "application/x-tcl"},
            {"tex", "application/x-tex"},
            {"texi", "application/x-texinfo"},
            {"texinfo", "application/x-texinfo"},
            {"tgz", "application/x-tar"},
            {"thm", "application/vnd.eri.thm"},
            {"tif", "image/tiff"},
            {"tiff", "image/tiff"},
            {"tki", "application/x-tkined"},
            {"tkined", "application/x-tkined"},
            {"toc", "application/toc"},
            {"toy", "image/toy"},
            {"tr", "application/x-troff"},
            {"trk", "x-lml/x-gps"},
            {"trm", "application/x-msterminal"},
            {"tsi", "audio/tsplayer"},
            {"tsp", "application/dsptype"},
            {"tsv", "text/tab-separated-values"},
            {"tsv", "text/tab-separated-values"},
            {"ttf", "application/octet-stream"},
            {"ttz", "application/t-time"},
            {"txt", "text/plain"},
            {"ult", "audio/x-mod"},
            {"ustar", "application/x-ustar"},
            {"uu", "application/x-uuencode", "zip"},
            {"uue", "application/x-uuencode", "zip"},
            {"vcd", "application/x-cdlink"},
            {"vcf", "text/x-vcard"},
            {"vdo", "video/vdo"},
            {"vib", "audio/vib"},
            {"viv", "video/vivo"},
            {"vivo", "video/vivo"},
            {"vmd", "application/vocaltec-media-desc"},
            {"vmf", "application/vocaltec-media-file"},
            {"vmi", "application/x-dreamcast-vms-info"},
            {"vms", "application/x-dreamcast-vms"},
            {"vox", "audio/voxware"},
            {"vqe", "audio/x-twinvq-plugin"},
            {"vqf", "audio/x-twinvq"},
            {"vql", "audio/x-twinvq"},
            {"vre", "x-world/x-vream"},
            {"vrml", "x-world/x-vrml"},
            {"vrt", "x-world/x-vrt"},
            {"vrw", "x-world/x-vream"},
            {"vts", "workbook/formulaone"},
            {"wav", "audio/x-wav"},
            {"wax", "audio/x-ms-wax"},
            {"wbmp", "image/vnd.wap.wbmp"},
            {"web", "application/vnd.xara"},
            {"wi", "image/wavelet"},
            {"wis", "application/x-InstallShield"},
            {"wm", "video/x-ms-wm"},
            {"wma", "audio/x-ms-wma"},
            {"wmd", "application/x-ms-wmd"},
            {"wmf", "application/x-msmetafile"},
            {"wml", "text/vnd.wap.wml"},
            {"wmlc", "application/vnd.wap.wmlc"},
            {"wmls", "text/vnd.wap.wmlscript"},
            {"wmlsc", "application/vnd.wap.wmlscriptc"},
            {"wmlscript", "text/vnd.wap.wmlscript"},
            {"wmv", "audio/x-ms-wmv"},
            {"wmx", "video/x-ms-wmx"},
            {"wmz", "application/x-ms-wmz"},
            {"wpng", "image/x-up-wpng"},
            {"wps", "application/vnd.ms-works"},
            {"wpt", "x-lml/x-gps"},
            {"wri", "application/x-mswrite"},
            {"wrl", "x-world/x-vrml"},
            {"wrz", "x-world/x-vrml"},
            {"ws", "text/vnd.wap.wmlscript"},
            {"wsc", "application/vnd.wap.wmlscriptc"},
            {"wv", "video/wavelet"},
            {"wvx", "video/x-ms-wvx"},
            {"wxl", "application/x-wxl"},
            {"x-gzip", "application/x-gzip", "zip"},
            {"xar", "application/vnd.xara"},
            {"xbm", "image/x-xbitmap"},
            {"xdm", "application/x-xdma"},
            {"xdma", "application/x-xdma"},
            {"xdw", "application/vnd.fujixerox.docuworks"},
            {"xht", "application/xhtml+xml"},
            {"xhtm", "application/xhtml+xml"},
            {"xhtml", "application/xhtml+xml"},
            {"xla", "application/vnd.ms-excel", "excel"},
            {"xlc", "application/vnd.ms-excel", "excel"},
            {"xll", "application/x-excel", "excel"},
            {"xlm", "application/vnd.ms-excel", "excel"},
            {"xls", "application/vnd.ms-excel", "excel"},
            {"xlt", "application/vnd.ms-excel", "excel"},
            {"xlw", "application/vnd.ms-excel", "excel"},
            {"xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "excel"},
            {"xm", "audio/x-mod"},
            {"xml", "text/xml"},
            {"xmz", "audio/x-mod"},
            {"xpi", "application/x-xpinstall"},
            {"xpm", "image/x-xpixmap"},
            {"xsit", "text/xml"},
            {"xsl", "text/xml"},
            {"xul", "text/xul"},
            {"xwd", "image/x-xwindowdump"},
            {"xyz", "chemical/x-pdb"},
            {"yz1", "application/x-yz1"},
            {"z", "application/x-compress", "zip"},
            {"zac", "application/x-zaurus-zac"},
            {"zip", "application/zip", "zip"},
            {"", "*/*"}
    };
}
