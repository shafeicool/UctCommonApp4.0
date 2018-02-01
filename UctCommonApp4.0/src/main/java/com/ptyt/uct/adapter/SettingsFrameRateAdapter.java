package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.entity.SettingsFrameRateEntity;

/**
 * Title: com.ptyt.uct.adapter
 * Description:
 * Date: 2017/8/4
 * Author: ShaFei
 * Version: V1.0
 */

public class SettingsFrameRateAdapter extends BaseRecyAdapter<SettingsFrameRateEntity> {

    private Context mContext;

    public SettingsFrameRateAdapter(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SettingsFrameRateViewHolder(inflater.inflate(R.layout.item_settings_frame_rate, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SettingsFrameRateViewHolder viewHolder = (SettingsFrameRateViewHolder) holder;
        int frameRate = getItem(position).getFrameRate();
        viewHolder.tv_frame_rate.setText(frameRate + mContext.getResources().getString(R.string.string_settings_frame_rate_unit));
        if (getItem(position).getChecked()) {
            viewHolder.iv_checked.setBackgroundResource(R.mipmap.chenkbox_selected);
        } else {
            viewHolder.iv_checked.setBackgroundResource(R.mipmap.settings_chenkbox_normal);
        }
    }

    public class SettingsFrameRateViewHolder extends BaseViewHolder {

        private TextView tv_frame_rate;
        private ImageView iv_checked;

        public SettingsFrameRateViewHolder(View itemView) {
            super(itemView);
            tv_frame_rate = (TextView) itemView.findViewById(R.id.tv_frame_rate);
            iv_checked = (ImageView) itemView.findViewById(R.id.iv_checked);
        }
    }
}
