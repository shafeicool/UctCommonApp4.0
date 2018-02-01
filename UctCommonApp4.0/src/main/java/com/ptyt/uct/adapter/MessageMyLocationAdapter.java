package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.ptyt.uct.R;

/**
 * Title: com.ptyt.uct.adapter
 * Description:
 * Date: 2017/5/26
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageMyLocationAdapter extends BaseRecyAdapter<PoiItem> {

    private Context mContext;
    private int position = 0;
    // 地名
    private String placeName;
    // 具体地名
    private String specificPlaceName;

    public MessageMyLocationAdapter(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageMyLocationAdapter.MessageMyLocationHolder(inflater.inflate(R.layout.item_message_my_location, null));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MessageMyLocationHolder viewHolder = (MessageMyLocationHolder) holder;
        viewHolder.name_tv.setText(getItem(position).getTitle());
        viewHolder.location_tv.setText(getItem(position).getProvinceName() + getItem(position).getCityName() + getItem(position).getAdName() + getItem(position).getSnippet());
        if (this.position == position && position != -1) {
            viewHolder.location_select_iv.setImageResource(R.mipmap.icon_selected);
            setPlaceName(viewHolder.name_tv.getText().toString());
            setSpecificPlaceName(viewHolder.location_tv.getText().toString());
        } else {
            viewHolder.location_select_iv.setImageResource(0);
        }
    }

    public void setClickPosition(int position) {
        this.position = position;
        notifyDataSetChanged();
    }

    public int getClickPosition() {
        return position;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getSpecificPlaceName() {
        return specificPlaceName;
    }

    public void setSpecificPlaceName(String specificPlaceName) {
        this.specificPlaceName = specificPlaceName;
    }

    public class MessageMyLocationHolder extends BaseViewHolder {

        private ImageView location_select_iv;
        private TextView name_tv;
        private TextView location_tv;

        public MessageMyLocationHolder(View itemView) {
            super(itemView);
            location_select_iv = (ImageView) itemView.findViewById(R.id.location_select_iv);
            name_tv = (TextView) itemView.findViewById(R.id.name_tv);
            location_tv = (TextView) itemView.findViewById(R.id.location_tv);
        }
    }
}
