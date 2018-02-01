package com.ptyt.uct.entity;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import uk.co.senab.photoview.PhotoView;

/**
 * Title: com.ptyt.uct.entity
 * Description:
 * Date: 2017/7/31
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageBrowsePhotoEntity {

    private VideoView videoView;
    private PhotoView photoView;
    private ImageView videoPlayIv;
    private ImageView animationView;
    private TextView progressView;
    private String path;
    private boolean isVideo;

    public VideoView getVideoView() {
        return videoView;
    }

    public void setVideoView(VideoView videoView) {
        this.videoView = videoView;
    }

    public PhotoView getPhotoView() {
        return photoView;
    }

    public void setPhotoView(PhotoView photoView) {
        this.photoView = photoView;
    }

    public ImageView getVideoPlayIv() {
        return videoPlayIv;
    }

    public void setVideoPlayIv(ImageView videoPlayIv) {
        this.videoPlayIv = videoPlayIv;
    }

    public ImageView getAnimationView() {
        return animationView;
    }

    public void setAnimationView(ImageView animationView) {
        this.animationView = animationView;
    }

    public TextView getProgressView() {
        return progressView;
    }

    public void setProgressView(TextView progressView) {
        this.progressView = progressView;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }
}
