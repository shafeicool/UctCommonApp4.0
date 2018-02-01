package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.widget.MarqueeTextView;
import com.ptyt.uct.widget.mapcluster.RegionItem;

/**
 * @Description:
 * @Date: 2017/12/20
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class MapDialogAdapter extends BaseRecyAdapter<RegionItem> {

    public MapDialogAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MapDialogViewHolder(inflater.inflate(R.layout.item_map_dialog,null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        RegionItem regionItem = getDatas().get(position);
        if(regionItem != null){
            MapDialogViewHolder viewHolder = (MapDialogViewHolder) holder;
            viewHolder.tv_userName.setText(regionItem.getUserName()+" ("+regionItem.getUserTel()+")");
            viewHolder.tv_uploadTime.setText(regionItem.getUploadTime());
        }
    }

    public class MapDialogViewHolder extends BaseViewHolder {
        private final MarqueeTextView tv_userName;
        private final TextView tv_uploadTime;
        public MapDialogViewHolder(View itemView) {
            super(itemView);
            PrintLog.i("GMemberViewHolder()");
            tv_userName = ((MarqueeTextView) itemView.findViewById(R.id.tv_userName));
            tv_uploadTime = ((TextView) itemView.findViewById(R.id.tv_uploadTime));
        }
    }
}
