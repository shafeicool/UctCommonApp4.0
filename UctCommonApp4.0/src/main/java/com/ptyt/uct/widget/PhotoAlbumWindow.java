package com.ptyt.uct.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.ptyt.uct.R;
import com.ptyt.uct.activity.MessagePhotoActivity;
import com.ptyt.uct.adapter.BaseRecyAdapter;
import com.ptyt.uct.adapter.MessageAlbumAdapter;
import com.ptyt.uct.entity.MessageAlbumEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Title: com.ptyt.uct.widget
 * Description:
 * Date: 2017/7/11
 * Author: ShaFei
 * Version: V1.0
 */

public class PhotoAlbumWindow extends PopupWindow implements BaseRecyAdapter.OnItemClickListener, View.OnClickListener {

    private MessagePhotoActivity activity;
    private ImageView iv_close;
    private RecyclerView rl_list;

    private MessageAlbumAdapter mAdapter;
    private List<MessageAlbumEntity> list = new ArrayList<>();
    private MessageAlbumEntity entity;

    public PhotoAlbumWindow(MessagePhotoActivity activity) {
        this.activity = activity;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.dialog_message_photo_album, null);
        setContentView(contentView);
        initView(contentView);
        initData();
    }

    private void initView(View contentView) {
        iv_close = (ImageView) contentView.findViewById(R.id.iv_close);
        rl_list = (RecyclerView) contentView.findViewById(R.id.rl_list);
        rl_list.setHasFixedSize(true);
        rl_list.setLayoutManager(new LinearLayoutManager(activity));
        iv_close.setOnClickListener(this);

        this.setWidth(RecyclerView.LayoutParams.MATCH_PARENT);
        this.setHeight(RecyclerView.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(activity, R.color.colorBackground10)));
        this.setOutsideTouchable(true);
        this.setAnimationStyle(R.style.take_album_anim);
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1.0f);
            }
        });
    }

    private void initData() {
        mAdapter = new MessageAlbumAdapter(activity);
        mAdapter.setOnItemClickListener(this);
        rl_list.setAdapter(mAdapter);
    }

    private void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        activity.getWindow().setAttributes(lp);
    }

    public void updateWindowAdapter(List<MessageAlbumEntity> list) {
        mAdapter.addAll(list);
    }

    public void showPopupWindow(View view) {
        if (!isShowing()) {
            backgroundAlpha(0.2f);
            showAtLocation(view, Gravity.BOTTOM, 0, 0);
        } else {
            dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                if (isShowing()) {
                    dismiss();
                }
                break;
        }
    }

    @Override
    public void onItemClick(int pos, View itemView) {
        mAdapter.selectedPos = pos;
        mAdapter.notifyDataSetChanged();
        activity.startLoading(mAdapter.getItem(pos).getName() + "", pos, false);
        if (isShowing()) {
            dismiss();
        }
    }
}
