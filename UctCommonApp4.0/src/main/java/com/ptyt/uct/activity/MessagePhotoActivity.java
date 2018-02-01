package com.ptyt.uct.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.adapter.BaseRecyAdapter;
import com.ptyt.uct.adapter.MessagePhotoAdapter;
import com.ptyt.uct.common.LocalAllPhotoLoader;
import com.ptyt.uct.entity.MessageAlbumEntity;
import com.ptyt.uct.entity.MessagePhotoEntity;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.widget.PhotoAlbumWindow;

import java.util.ArrayList;
import java.util.List;



/**
 * Title: com.ptyt.uct.activity
 * Description:
 * Date: 2017/6/27
 * Author: ShaFei
 * Version: V1.0
 */

public class MessagePhotoActivity extends BaseActionBarActivity implements
        MessagePhotoAdapter.OnCheckedChangeListener,
        View.OnClickListener,
        BaseRecyAdapter.OnItemClickListener,
        LoaderManager.LoaderCallbacks<List<MessagePhotoEntity>> {

    private Context mContext;
    private TextView mNoFileTv;
    private ProgressBar mProgressView;
    private RecyclerView mRecyclerView;
    private RelativeLayout mParent;
    private PhotoAlbumWindow mWindow;
    private TextView mThumbnailTv;

    private MessagePhotoAdapter mAdapter;
    private GridLayoutManager mGridLayoutManager;
    // 已选中的列表
    private List<MessagePhotoEntity> selectedList = new ArrayList<>();
    //    // 所有的列表
    //    public static List<MessagePhotoEntity> datas = new ArrayList<>();
    // 选中的数目
    private int counts = 0;
    // 限制发送图片或视频数目
    private static final int LIMIT_COUNTS = 10;
    // 是否选择缩略图
    private boolean isThumbnail = true;
    // 选择的相册类型，默认是所有图片和视频
    private int albumType = 0;
    // 是否第一次进入相册，用于初始化PopWindow数据
    private boolean isFirst = true;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_message_photo_list;
    }

    @Override
    protected void initWidget() {
        mContext = this;
        mParent = (RelativeLayout) findViewById(R.id.rl_parent);
        mProgressView = (ProgressBar) findViewById(R.id.progress_message_pbar);
        mNoFileTv = (TextView) findViewById(R.id.no_file_tv);
        mRecyclerView = (RecyclerView) findViewById(R.id.file_list_rv);
        mThumbnailTv = (TextView) findViewById(R.id.thumbnail_tv);

        mProgressView.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        mThumbnailTv.setVisibility(View.GONE);

        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.mipmap.btn_down, null);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
        tv_actionBarTitle.setCompoundDrawables(null, null, drawable, null);

        tv_actionBarRight.setVisibility(View.VISIBLE);
        tv_actionBarRight.setText(getResources().getString(R.string.string_photo_list_send));
        tv_actionBarRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.y28));
        tv_actionBarRight.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_13));

        tv_actionBarRight2.setBackgroundResource(R.drawable.shape_message_file_list_send);
        tv_actionBarRight2.setGravity(Gravity.CENTER);

        tv_actionBarTitle.setOnClickListener(this);
        rl_relative.setOnClickListener(this);
        mThumbnailTv.setOnClickListener(this);
    }

    protected void initData() {
        mGridLayoutManager = new GridLayoutManager(mContext, 4);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new MessagePhotoAdapter(mContext);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnCheckedChangeListener(this);
        mRecyclerView.setAdapter(mAdapter);

        mWindow = new PhotoAlbumWindow(MessagePhotoActivity.this);
        startLoading(mContext.getResources().getString(R.string.string_album_title1), albumType, true);
    }

    public void startLoading(String title, int pos, boolean isFirst) {
        if (!isFirst && albumType == pos) {
            return;
        }
        setActionBarTitle(title + "");
        albumType = pos;
        this.isFirst = isFirst;
        restoreDefault();
        updateRightButton();
        getSupportLoaderManager().initLoader(pos, null, this);
    }

    private void updateRightButton() {
        if (counts > 0) {
            tv_actionBarRight.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_12));
            tv_actionBarRight2.setVisibility(View.VISIBLE);
            tv_actionBarRight2.setText(counts + "");
            //            hasSelected = true;
        } else {
            tv_actionBarRight.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_13));
            tv_actionBarRight2.setVisibility(View.GONE);
            //            hasSelected = false;
        }
    }

    private void restoreDefault() {
        counts = 0;
        selectedList.clear();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_title:
                mWindow.showPopupWindow(mParent);
                break;
            case R.id.rl_relative:
                if (counts > 0) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isThumbnail", isThumbnail);
                    bundle.putParcelableArrayList("PhotoList", (ArrayList<? extends Parcelable>) selectedList);
                    intent.putExtras(bundle);
                    setResult(MessageActivity.PHOTO_RESULT_CODE, intent);
                    finish();
                }
                break;
            case R.id.thumbnail_tv:
                if (isThumbnail) {
                    isThumbnail = false;
                    Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.mipmap.radiobutton_pre, null);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                    mThumbnailTv.setCompoundDrawables(drawable, null, null, null);
                } else {
                    isThumbnail = true;
                    Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.mipmap.radiobutton_nor, null);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());//对图片进行压缩
                    mThumbnailTv.setCompoundDrawables(drawable, null, null, null);
                }
                break;
        }
    }

    @Override
    public void onItemClick(int pos, View itemView) {
        Intent intent = new Intent(this, MessageBrowsePhotoActivity.class);
        //        intent.putExtra("photo_list", (Serializable) datas);
        //        Bundle bundle = new Bundle();
        //        bundle.putParcelableArrayList("photo_list", (ArrayList<? extends Parcelable>) datas);
        //        intent.putExtras(bundle);
        intent.putExtra("photo_position", pos);
        //        intent.putExtra("photo_entity", mAdapter.getItem(pos));
        //        intent.putExtra("has_selected", hasSelected);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public Loader<List<MessagePhotoEntity>> onCreateLoader(int id, Bundle args) {
        PrintLog.i("Start to load photo");
        PrintLog.i("albumType = " + albumType);
        return new LocalAllPhotoLoader(this, albumType);
    }

    @Override
    public void onLoadFinished(Loader<List<MessagePhotoEntity>> loader, List<MessagePhotoEntity> data) {
        PrintLog.i("Load photo finished, data.size = " + data.size());
        mProgressView.setVisibility(View.GONE);
        mGridLayoutManager.scrollToPosition(0);

        if (isFirst && loader instanceof LocalAllPhotoLoader) {
            List<MessageAlbumEntity> list = new ArrayList<>();

            MessageAlbumEntity entity = new MessageAlbumEntity();
            // 取第一张作为相册封面
            entity.setPath(((LocalAllPhotoLoader) loader).getAllPath());
            entity.setName(getResources().getString(R.string.string_album_title1));
            entity.setCount(String.format(getResources().getString(R.string.string_album_count), ((LocalAllPhotoLoader) loader).getAllCount()));
            list.add(entity);

            entity = new MessageAlbumEntity();
            // 取第一张作为相册封面
            entity.setPath(((LocalAllPhotoLoader) loader).getVideoPath());
            entity.setName(getResources().getString(R.string.string_album_title2));
            entity.setCount(String.format(getResources().getString(R.string.string_album_count), ((LocalAllPhotoLoader) loader).getVideoCount()));
            list.add(entity);

            entity = new MessageAlbumEntity();
            // 取第一张作为相册封面
            entity.setPath(((LocalAllPhotoLoader) loader).getPicturePath());
            entity.setName(getResources().getString(R.string.string_album_title3));
            entity.setCount(String.format(getResources().getString(R.string.string_album_count), ((LocalAllPhotoLoader) loader).getPictureCount()));
            list.add(entity);
            mWindow.updateWindowAdapter(list);
        }

        if (data != null && data.size() > 0) {
            FileUtils.msgs.clear();
            FileUtils.datas.clear();
            FileUtils.datas = data;
            for (int i = 0; i < data.size(); i++) {// 当切换相册时，用来重置已经被已被选中的图片
                if (data.get(i).getChecked()) {
                    data.get(i).setChecked(false);
                }
            }
            mNoFileTv.setVisibility(View.GONE);
            mThumbnailTv.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mAdapter.addAll(data);
        } else {
            // 没有搜索到任何文件
            mRecyclerView.setVisibility(View.GONE);
            mThumbnailTv.setVisibility(View.GONE);
            mNoFileTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<MessagePhotoEntity>> loader) {
    }

    @Override
    public void onCheckedChange(int pos) {
        if (mAdapter.getItem(pos).getChecked()) {
            mAdapter.getItem(pos).setChecked(false);
            counts--;
            selectedList.remove(mAdapter.getItem(pos));
        } else {
            if ((mAdapter.getItem(pos).getType() == MessageDBConstant.INFO_TYPE_CAMERA_VIDEO || mAdapter.getItem(pos).getType() == MessageDBConstant.INFO_TYPE_VIDEO) && mAdapter.getItem(pos).getDuring() < 1000) {
                ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_photo_list_prompt1), -1);
                return;
            }
            if (counts >= LIMIT_COUNTS) {
                ToastUtils.getToast().showMessageShort(mContext, String.format(getResources().getString(R.string.string_photo_list_prompt2), LIMIT_COUNTS), -1);
                return;
            } else {
                mAdapter.getItem(pos).setChecked(true);
                counts++;
                selectedList.add(mAdapter.getItem(pos));
            }
        }
        updateRightButton();
        mAdapter.updateItem(pos, mAdapter.getItem(pos));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        PrintLog.i("onResume");
    }

    @Override
    protected void onStop() {
        PrintLog.i("onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        PrintLog.i("onDestroy");
        super.onDestroy();
    }

    //    @Override
    //    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    //        if (data != null) {
    //            switch (resultCode) {
    //                case SEND_PHOTO_RESULT_CODE:
    //                    Intent intent = new Intent();
    ////                    intent.putExtra("PhotoList", (Serializable) selectedList);
    //                    Bundle bundle = new Bundle();
    //                    bundle.putParcelableArrayList("PhotoList", (ArrayList<? extends Parcelable>) selectedList);
    //                    intent.putExtras(bundle);
    //                    setResult(MessageActivity.PHOTO_RESULT_CODE, intent);
    //                    finish();
    //                    break;
    //            }
    //        }
    //        super.onActivityResult(requestCode, resultCode, data);
    //    }

}
