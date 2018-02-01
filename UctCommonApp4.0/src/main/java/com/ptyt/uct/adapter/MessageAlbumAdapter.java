package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ptyt.uct.R;
import com.ptyt.uct.entity.MessageAlbumEntity;

/**
 * Title: com.ptyt.uct.adapter
 * Description:
 * Date: 2017/7/11
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageAlbumAdapter extends BaseRecyAdapter<MessageAlbumEntity> {

    private Context mContext;
    //选中的位置
    public int selectedPos;

    public MessageAlbumAdapter(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageAlbumAdapter.MessageAlbumHolder(inflater.inflate(R.layout.item_message_photo_album, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MessageAlbumHolder viewHolder = (MessageAlbumHolder) holder;
        Glide.with(mContext).load(getItem(position).getPath() + "").placeholder(R.mipmap.icon_album).centerCrop().into(viewHolder.iv_show_first);
        viewHolder.tv_name.setText(getItem(position).getName() + "");
        viewHolder.tv_number.setText(getItem(position).getCount() + "");
        if (selectedPos == position) {
            viewHolder.iv_select.setVisibility(View.VISIBLE);
        } else {
            viewHolder.iv_select.setVisibility(View.INVISIBLE);
        }
        viewHolder.rl_item.setBackgroundResource(R.drawable.selector_press_background);
    }

    public class MessageAlbumHolder extends BaseViewHolder {

        private RelativeLayout rl_item;
        private ImageView iv_show_first, iv_select;
        private TextView tv_name, tv_number;

        public MessageAlbumHolder(View itemView) {
            super(itemView);
            rl_item = (RelativeLayout) itemView.findViewById(R.id.rl_item);
            iv_show_first = (ImageView) itemView.findViewById(R.id.iv_show_first);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_number = (TextView) itemView.findViewById(R.id.tv_number);
            iv_select = (ImageView) itemView.findViewById(R.id.iv_select);
        }


    }
}
