package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;

/**
 * @Description:
 * @Date: 2017/8/7
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class MapSearchAdapter extends BasePagingAdapter<PoiItem> {

    public MapSearchAdapter(Context context) {
        super(context);
    }

    @Override
    protected CommonViewHolder setCommonViewHolder() {
        return new SearchViewHolder(View.inflate(context,R.layout.item_map_search,null));
    }

    @Override
    protected void onBindCommonViewHolder(RecyclerView.ViewHolder holder, PoiItem poiItem) {
        SearchViewHolder viewHolder = (SearchViewHolder) holder;
        PrintLog.i("map  poiItem.getTitle():"+poiItem.getTitle()+ "  getSnippet:"+poiItem.getSnippet());
        viewHolder.tv_itemName.setText(poiItem.getTitle());
        viewHolder.tv_itemAddress.setText(poiItem.getCityName()+"-"+poiItem.getAdName()+"-"+poiItem.getSnippet());
    }


    class SearchViewHolder extends CommonViewHolder{

        private final ImageView iv_itemType;
        private final TextView tv_itemName;
        private final TextView tv_itemAddress;

        public SearchViewHolder(View itemView) {
            super(itemView);
            iv_itemType = ((ImageView) itemView.findViewById(R.id.iv_itemType));
            tv_itemName = ((TextView) itemView.findViewById(R.id.tv_itemName));
            tv_itemAddress = ((TextView) itemView.findViewById(R.id.tv_itemAddress));
        }
    }
}
