package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.entity.EventBean;

/**
 * @Description:上传视频list
 * @Date: 2017/7/4 待删除
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class UpVideoAdapter extends BaseRecyAdapter<EventBean> {

    public UpVideoAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new UpVideoViewHolder(inflater.inflate(R.layout.item_upload_video,null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        EventBean eventBean = getItem(position);
        UpVideoViewHolder upVideoViewHolder = ((UpVideoViewHolder) holder);
        if(eventBean.getPcName() != null){
            upVideoViewHolder.tv_username.setText(eventBean.getPcName());
        }
        //上传时间
        
        //upVideoViewHolder.tv_speakTime.setText();
    }
    public class UpVideoViewHolder extends RecyclerView.ViewHolder{
        private final TextView tv_username;
        private final TextView tv_speakTime;

        public UpVideoViewHolder(View itemView) {
            super(itemView);
            tv_username = ((TextView) itemView.findViewById(R.id.tv_userName));
            tv_speakTime = ((TextView) itemView.findViewById(R.id.tv_speakTime));
        }
    }
}
