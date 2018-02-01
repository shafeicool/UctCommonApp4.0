package com.ptyt.uct.common;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.entity.MessagePhotoEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Title: com.ptyt.uct.common
 * Description:
 * Date: 2017/6/28
 * Author: ShaFei
 * Version: V1.0
 */

public class LocalAllPhotoLoader extends AsyncTaskLoader<List<MessagePhotoEntity>> {

    private List<MessagePhotoEntity> mDatas = null;
    // 相册类型
    private int albumType;
    // 图片数
    private int pictureCount = 0;
    // 视频数
    private int videoCount = 0;
    // 总数
    private int allCount = 0;

    // 第一张图片路径
    private String picturePath;
    // 第一张视频路径
    private String videoPath;
    // 第一张所有照片路径
    private String allPath;

    // 相册类型
    private static final int PHOTO_ALL = 0; // 所有图片和视频
    private static final int PHOTO_VIDEO = 1; // 所有视频
    private static final int PHOTO_PICTURE = 2; // 所有图片

    public LocalAllPhotoLoader(Context context, int albumType) {
        super(context);
        this.albumType = albumType;
    }

    public int getPictureCount() {
        return pictureCount;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public int getAllCount() {
        return allCount;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public String getAllPath() {
        return allPath;
    }

    @Override
    public List<MessagePhotoEntity> loadInBackground() {
        PrintLog.d("loadInBackground");
        List<MessagePhotoEntity> mAllData = new ArrayList<>();
        List<MessagePhotoEntity> mPictureData = new ArrayList<>();
        List<MessagePhotoEntity> mVideoData = new ArrayList<>();

        if (albumType == PHOTO_ALL || albumType == PHOTO_PICTURE) {
            Cursor imageCursor = getContext().getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.SIZE,
                            MediaStore.Images.Media.DATE_MODIFIED,
                            MediaStore.Images.Media.DATA,
                            MediaStore.Images.Media._ID}, null, null,
                    MediaStore.Images.Media._ID);

            if (imageCursor != null && imageCursor.getCount() > 0) {

                while (imageCursor.moveToNext()) {
                    Long size = imageCursor.getLong(imageCursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                    String imgPath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    Long modifiedDate = imageCursor.getLong(imageCursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
                    MessagePhotoEntity entity = new MessagePhotoEntity();
                    entity.setSize(size);
                    entity.setTime(modifiedDate);
                    entity.setType(MessageDBConstant.INFO_TYPE_IMAGE);
                    entity.setPath(imgPath);
                    entity.setChecked(false);
                    File file = new File(imgPath);
                    if (!file.exists()) {
                        continue;
                    }
                    mPictureData.add(entity);
                    pictureCount++;
                }

                if (mPictureData.size() > 0) {
                    sortByLastTime(mPictureData);
                    picturePath = mPictureData.get(0).getPath();
                    mAllData.addAll(mPictureData);
                }
            }

            if (imageCursor != null) {
                imageCursor.close();
            }
        }

        if (albumType == PHOTO_ALL || albumType == PHOTO_VIDEO) {
            Cursor videoCursor = getContext().getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.SIZE,
                            MediaStore.Video.Media.DATE_MODIFIED,
                            MediaStore.Video.Media.DURATION,
                            MediaStore.Video.Media.DATA,
                            MediaStore.Video.Media._ID}, null, null,
                    MediaStore.Video.Media._ID);

            if (videoCursor != null && videoCursor.getCount() > 0) {

                while (videoCursor.moveToNext()) {
                    Long size = videoCursor.getLong(videoCursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                    String videoPath = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.Media.DATA));
                    Long videoDuring = videoCursor.getLong(videoCursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                    Long modifiedDate = videoCursor.getLong(videoCursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED));
                    MessagePhotoEntity entity = new MessagePhotoEntity();
                    entity.setSize(size);
                    entity.setTime(modifiedDate);
                    entity.setType(MessageDBConstant.INFO_TYPE_VIDEO);
                    entity.setPath(videoPath);
                    entity.setDuring(videoDuring);
                    entity.setChecked(false);
                    File file = new File(videoPath);
                    if (!file.exists()) {
                        continue;
                    }
                    mVideoData.add(entity);
                    videoCount++;
                }

                if (mVideoData.size() > 0) {
                    sortByLastTime(mVideoData);
                    videoPath = mVideoData.get(0).getPath();
                    mAllData.addAll(mVideoData);
                }
            }

            if (videoCursor != null) {
                videoCursor.close();
            }
        }

        sortByLastTime(mAllData);
        allCount = mAllData.size();
        PrintLog.d("照片总共有" + allCount);
        if (allCount > 0) {
            allPath = mAllData.get(0).getPath();
        }


        return mAllData;
    }

    public void sortByLastTime(List<MessagePhotoEntity> list) {
        Collections.sort(list, new PhotoComparator());
        Collections.reverse(list);
    }

    public class PhotoComparator implements Comparator<MessagePhotoEntity> {

        @Override
        public int compare(MessagePhotoEntity lhs, MessagePhotoEntity rhs) {
            return Long.valueOf(lhs.getTime()).compareTo(Long.valueOf(rhs.getTime()));
        }
    }

    @Override
    public void deliverResult(List<MessagePhotoEntity> T) {
        PrintLog.d("deliverResult");
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (T != null) {
                T.clear();
                T = null;
            }
            return;
        }
        List<MessagePhotoEntity> oldDatas = mDatas;
        mDatas = T;

        if (isStarted()) {
            super.deliverResult(T);
        }

        if (oldDatas != null && oldDatas != mDatas) {
            oldDatas.clear();
            oldDatas = null;
        }
    }

    @Override
    protected void onStartLoading() {
        PrintLog.d("onStartLoading");
        if (mDatas != null && mDatas.size() > 0) {
            deliverResult(mDatas);
        }

        if (takeContentChanged() || mDatas == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        PrintLog.d("onStopLoading");
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(List<MessagePhotoEntity> T) {
        PrintLog.d("onCanceled");
        if (T != null) {
            T.clear();
            T = null;
        }
    }

    @Override
    protected void onReset() {
        PrintLog.d("onReset");
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mDatas != null) {
            mDatas.clear();
            mDatas = null;
        }
    }
}
