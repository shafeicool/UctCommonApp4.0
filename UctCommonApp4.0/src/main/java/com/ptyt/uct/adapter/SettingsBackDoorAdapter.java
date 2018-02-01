package com.ptyt.uct.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.entity.SettingsBackDoorEntity;
import com.ptyt.uct.utils.StrUtils;

/**
 * Title: com.ptyt.uct.adapter
 * Description:
 * Date: 2017/8/4
 * Author: ShaFei
 * Version: V1.0
 */

public class SettingsBackDoorAdapter extends BaseRecyAdapter<SettingsBackDoorEntity> {

    private Context mContext;

    public SettingsBackDoorAdapter(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SettingsBackDoorViewHolder(inflater.inflate(R.layout.item_settings_back_door, null));
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        SettingsBackDoorViewHolder viewHolder = (SettingsBackDoorViewHolder) holder;
        String head = getItem(position).getHead();
        String subHead = getItem(position).getSubHead();
        Boolean isOpen = getItem(position).getOpen();
        if (StrUtils.isEmpty(head)) {
            viewHolder.tv_head.setText("");
        } else {
            viewHolder.tv_head.setText(head);
        }

        if (StrUtils.isEmpty(subHead)) {
            viewHolder.tv_subhead.setVisibility(View.GONE);
        } else {
            viewHolder.tv_subhead.setVisibility(View.VISIBLE);
            viewHolder.tv_subhead.setText(subHead);
        }

        if (isOpen == null) {
            viewHolder.iv_checked.setVisibility(View.GONE);
        } else {
            viewHolder.iv_checked.setVisibility(View.VISIBLE);
            viewHolder.iv_checked.setChecked(isOpen);
            viewHolder.iv_checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (checkedChangeListener != null) {
                        checkedChangeListener.onCheckedChange(position, buttonView, isChecked);
                    }
                }
            });
        }
    }

    public class SettingsBackDoorViewHolder extends BaseViewHolder {

        private TextView tv_head, tv_subhead;
        private Switch iv_checked;

        public SettingsBackDoorViewHolder(View itemView) {
            super(itemView);
            tv_head = (TextView) itemView.findViewById(R.id.tv_head);
            tv_subhead = (TextView) itemView.findViewById(R.id.tv_subhead);
            iv_checked = (Switch) itemView.findViewById(R.id.iv_checked);
        }
    }

    private OnCheckedChangeListener checkedChangeListener;

    public interface OnCheckedChangeListener {
        void onCheckedChange(int pos, CompoundButton buttonView, boolean isChecked);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener checkedChangeListener) {
        this.checkedChangeListener = checkedChangeListener;
    }
}
