package com.ptyt.uct.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.ptyt.uct.R;

/**
 * Title: com.ptyt.uct.utils
 * Description: Bitmap工具类
 * Date: 2017/5/2
 * Author: ShaFei
 * Version: V1.0
 */

public class BitmapUtil {

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响
    private static Bitmap createScaleBitmap(Bitmap src, int dstWidth, int dstHeight) {
        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) {   // 如果没有缩放，那么不回收
            src.recycle();  // 释放Bitmap的native像素数组
        }
        return dst;
    }

    // 从Resources中加载图片
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options); // 读取图片长款
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight); // 计算inSampleSize
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeResource(res, resId, options); // 载入一个稍大的缩略图
        return createScaleBitmap(src, reqWidth, reqHeight); // 进一步得到目标大小的缩略图
    }

    // 从sd卡上加载图片
    public static Bitmap decodeSampledBitmapFromSd(String pathName, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, reqWidth, reqHeight);
    }

    /**
     * 根据指定的图像路径和大小来获取缩略图
     * 此方法有两点好处：
     * 1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
     * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
     * 2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使
     * 用这个工具生成的图像不会被拉伸。
     *
     * @param imagePath 图像的路径
     * @param width     指定输出图像的宽度
     * @param height    指定输出图像的高度
     * @return 生成的缩略图
     */
    public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be = 1;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    /**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height,
                                           int kind) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        System.out.println("w" + bitmap.getWidth());
        System.out.println("h" + bitmap.getHeight());
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    public static DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true).cacheOnDisk(false)
            .imageScaleType(ImageScaleType.EXACTLY).considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565).build();
    /**
     * 图片配置，竖直
     */
    public static DisplayImageOptions imageVerticalOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.icon_message_image_load_portrait)
            .showImageForEmptyUri(R.drawable.icon_message_image_load_portrait)
            .showImageOnFail(R.drawable.icon_message_image_load_portrait).cacheInMemory(true)
            .cacheOnDisk(false).imageScaleType(ImageScaleType.EXACTLY)
            .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    /**
     * 图片配置，横向
     */
    public static DisplayImageOptions imageHorizantalOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.icon_message_image_load_landscape)
            .showImageForEmptyUri(R.drawable.icon_message_image_load_landscape)
            .showImageOnFail(R.drawable.icon_message_image_load_landscape).cacheInMemory(true)
            .cacheOnDisk(false).imageScaleType(ImageScaleType.EXACTLY)
            .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    /**
     * 视频缩略图，竖直
     */
    public static DisplayImageOptions videoVerticalOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.icon_message_video_load_portrait)
            .showImageForEmptyUri(R.drawable.icon_message_video_load_portrait)
            .showImageOnFail(R.drawable.icon_message_video_load_portrait).cacheInMemory(true)
            .cacheOnDisk(false).imageScaleType(ImageScaleType.EXACTLY)
            .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    /**
     * 视频缩略图，横向
     */
    public static DisplayImageOptions videoHorizantalOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.icon_message_video_load_landscape)
            .showImageForEmptyUri(R.drawable.icon_message_video_load_landscape)
            .showImageOnFail(R.drawable.icon_message_video_load_landscape).cacheInMemory(true)
            .cacheOnDisk(false).imageScaleType(ImageScaleType.EXACTLY)
            .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)
            .build();
}