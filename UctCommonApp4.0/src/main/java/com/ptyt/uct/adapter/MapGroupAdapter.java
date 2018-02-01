package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.uct.bean.GroupData;
import com.ptyt.uct.R;

/**
 * @Description:
 * @Date: 2017/8/9
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class MapGroupAdapter extends BaseRecyAdapter<GroupData> {

    public void setSelectedPos(int selectedPos) {
        this.selectedPos = selectedPos;
    }

    private int selectedPos = 0;

    public MapGroupAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GroupViewHolder(inflater.inflate(R.layout.item_group_map,null));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        GroupViewHolder viewHolder = (GroupViewHolder) holder;
        GroupData item = getItem(position);
        viewHolder.tv_groupName.setText(item.groupName);
        if(selectedPos == position){
            viewHolder.tv_groupName.setTextColor(context.getResources().getColor(R.color.colorBlue));
        }else{
            viewHolder.tv_groupName.setTextColor(context.getResources().getColor(R.color.colorTextView_02));
        }
    }

    public class GroupViewHolder extends BaseViewHolder {

        private TextView tv_groupName;

        public GroupViewHolder(View itemView) {
            super(itemView);
            tv_groupName = (TextView) itemView.findViewById(R.id.tv_groupName);
        }
    }


}
