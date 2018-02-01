package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.entity.SettingsAccountEntity;

/**
 * Title: com.ptyt.uct.adapter
 * Description:
 * Date: 2017/8/4
 * Author: ShaFei
 * Version: V1.0
 */

public class SettingsAccountAdapter extends BaseRecyAdapter<SettingsAccountEntity> {

    private Context mContext;
    public boolean editorMode = false;

    public SettingsAccountAdapter(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SettingsAccountViewHolder(inflater.inflate(R.layout.item_settings_account, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SettingsAccountViewHolder viewHolder = (SettingsAccountViewHolder) holder;
        viewHolder.tv_name.setText(getItem(position).getName());
        viewHolder.tv_number.setText(getItem(position).getNumber());
//        if (editorMode) {
//            viewHolder.iv_delete.setVisibility(View.VISIBLE);
//            viewHolder.iv_checked.setVisibility(View.GONE);
//        } else {
//            viewHolder.iv_delete.setVisibility(View.GONE);
//            viewHolder.iv_checked.setVisibility(View.VISIBLE);
//            if (getItem(position).getChecked()) {
//                viewHolder.iv_checked.setVisibility(View.VISIBLE);
//            } else {
//                viewHolder.iv_checked.setVisibility(View.GONE);
//            }
//        }

    }

    public class SettingsAccountViewHolder extends BaseViewHolder {

        private ImageView iv_delete, iv_head, iv_checked;
        private TextView tv_name, tv_number;

        public SettingsAccountViewHolder(View itemView) {
            super(itemView);
            iv_delete = (ImageView) itemView.findViewById(R.id.iv_delete);
            iv_head = (ImageView) itemView.findViewById(R.id.iv_head);
            iv_checked = (ImageView) itemView.findViewById(R.id.iv_checked);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_number = (TextView) itemView.findViewById(R.id.tv_number);
        }
    }


}
