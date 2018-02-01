package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.entity.SettingsBitRateEntity;

/**
 * Title: com.ptyt.uct.adapter
 * Description:
 * Date: 2017/8/4
 * Author: ShaFei
 * Version: V1.0
 */

public class SettingsBitRateAdapter extends BaseRecyAdapter<SettingsBitRateEntity> {

    private Context mContext;
    public SettingsBitRateAdapter(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SettingsBitRateViewHolder(inflater.inflate(R.layout.item_settings_bit_rate, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SettingsBitRateViewHolder viewHolder = (SettingsBitRateViewHolder) holder;
        viewHolder.tv_bit_rate.setText(getItem(position).getBitRate() + "");
        if (getItem(position).getChecked()) {
            viewHolder.tv_bit_rate.setBackgroundResource(R.drawable.shape_blue_background);
            viewHolder.tv_bit_rate.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_05));
        } else {
            viewHolder.tv_bit_rate.setBackgroundResource(R.drawable.shape_white_background);
            viewHolder.tv_bit_rate.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_19));
        }
    }

    public class SettingsBitRateViewHolder extends BaseViewHolder {

        private TextView tv_bit_rate;

        public SettingsBitRateViewHolder(View itemView) {
            super(itemView);
            tv_bit_rate = (TextView) itemView.findViewById(R.id.tv_bit_rate);
        }
    }
}
