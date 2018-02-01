package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ptyt.uct.R;
import com.ptyt.uct.entity.MessagePhotoEntity;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.utils.DateUtils;

/**
 * Title: com.ptyt.uct.adapter
 * Description:
 * Date: 2017/6/27
 * Author: ShaFei
 * Version: V1.0
 */

public class MessagePhotoAdapter extends BaseRecyAdapter<MessagePhotoEntity> {

    private Context mContext;

    public MessagePhotoAdapter(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessagePhotoAdapter.MessagePhotoHolder(inflater.inflate(R.layout.item_message_photo_list, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MessagePhotoHolder viewHolder = (MessagePhotoHolder) holder;
        // 记得做发送文件大小限制
        int fileType = getItem(position).getType();
        String filePath = getItem(position).getPath();
        Glide.with(mContext).load(filePath).asBitmap().placeholder(R.drawable.icon_message_image_load_portrait).into(viewHolder.mImageIv);
        switch (fileType) {
            case MessageDBConstant.INFO_TYPE_IMAGE:
                viewHolder.mVideoIv.setBackgroundResource(0);
                viewHolder.mTimeTv.setText("");
                break;
            case MessageDBConstant.INFO_TYPE_VIDEO:
                viewHolder.mVideoIv.setBackgroundResource(R.mipmap.btn_play);
                viewHolder.mTimeTv.setText(DateUtils.getVideoDuring(getItem(position).getDuring()));
                break;
        }

        if (getItem(position).getChecked()) {
            viewHolder.mSelectIv.setImageResource(R.mipmap.checkbox_selected);
        } else {
            viewHolder.mSelectIv.setImageResource(R.mipmap.checkbox_normal);
        }

    }

    public class MessagePhotoHolder extends BaseViewHolder {

        private ImageView mImageIv, mSelectIv, mVideoIv;
        private TextView mTimeTv;

        public MessagePhotoHolder(View itemView) {
            super(itemView);
            mImageIv = (ImageView) itemView.findViewById(R.id.photo_image_iv);
            mSelectIv = (ImageView) itemView.findViewById(R.id.photo_select_iv);
            mVideoIv = (ImageView) itemView.findViewById(R.id.photo_video_iv);
            mTimeTv = (TextView) itemView.findViewById(R.id.photo_time_tv);

            mSelectIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkedChangeListener != null) {
                        checkedChangeListener.onCheckedChange(getLayoutPosition());
                    }
                }
            });
        }


    }

    private OnCheckedChangeListener checkedChangeListener;
    public interface OnCheckedChangeListener {
        /**
         * @param pos 点击位置
         */
        void onCheckedChange(int pos);
    }

    /**
     * 设置adapter的checkbox点击事件
     * @param checkedChangeListener
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener checkedChangeListener) {
        this.checkedChangeListener = checkedChangeListener;
    }

}
