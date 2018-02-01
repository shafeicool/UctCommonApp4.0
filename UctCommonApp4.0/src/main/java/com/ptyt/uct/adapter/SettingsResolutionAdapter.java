package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.entity.SettingsResolutionEntity;
import com.ptyt.uct.common.SettingsConstant;


/**
 * Title: com.ptyt.uct.adapter
 * Description:
 * Date: 2017/8/4
 * Author: ShaFei
 * Version: V1.0
 */

public class SettingsResolutionAdapter extends BaseRecyAdapter<SettingsResolutionEntity> {

    private Context mContext;

    public SettingsResolutionAdapter(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SettingsResolutionViewHolder(inflater.inflate(R.layout.item_settings_resolution, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SettingsResolutionViewHolder viewHolder = (SettingsResolutionViewHolder) holder;
        int width = getItem(position).getWidth();
        int height = getItem(position).getHeight();
        if (width == SettingsConstant.RESOLUTION_WIDTH_MODE[0]) {
            viewHolder.tv_resolution.setText(mContext.getResources().getString(R.string.string_settings_resolution_320) + "(" + width + "X" + height + ")");
        } else if (width == SettingsConstant.RESOLUTION_WIDTH_MODE[1]) {
            viewHolder.tv_resolution.setText(mContext.getResources().getString(R.string.string_settings_resolution_640) + "(" + width + "X" + height + ")");
        } else if (width == SettingsConstant.RESOLUTION_WIDTH_MODE[2]) {
            viewHolder.tv_resolution.setText(mContext.getResources().getString(R.string.string_settings_resolution_1280) + "(" + width + "X" + height + ")");
        } else if (width == SettingsConstant.RESOLUTION_WIDTH_MODE[3]) {
            viewHolder.tv_resolution.setText(mContext.getResources().getString(R.string.string_settings_resolution_1920) + "(" + width + "X" + height + ")");
        } else {
            viewHolder.tv_resolution.setText(mContext.getResources().getString(R.string.string_settings_resolution_640) + "(" + width + "X" + height + ")");
        }
        if (getItem(position).getChecked()) {
            viewHolder.iv_checked.setBackgroundResource(R.mipmap.chenkbox_selected);
        } else {
            viewHolder.iv_checked.setBackgroundResource(R.mipmap.settings_chenkbox_normal);
        }
    }

    public class SettingsResolutionViewHolder extends BaseViewHolder {

        private TextView tv_resolution;
        private ImageView iv_checked;

        public SettingsResolutionViewHolder(View itemView) {
            super(itemView);
            tv_resolution = (TextView) itemView.findViewById(R.id.tv_resolution);
            iv_checked = (ImageView) itemView.findViewById(R.id.iv_checked);
        }
    }
}
