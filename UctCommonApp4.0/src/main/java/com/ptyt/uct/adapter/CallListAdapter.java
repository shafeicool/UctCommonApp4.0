package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.uct.service.UctClientApi;
import com.ptyt.uct.R;
import com.ptyt.uct.entity.Group;
import com.ptyt.uct.widget.MarqueeTextView;

import static com.ptyt.uct.common.SettingsConstant.SETTINGS_LOCK_GROUP;

/**
 * @Description: 主界面"对讲"
 * @Date: 2017/5/9
 * @Author: KeChuanqi
 * @Version:V1.0
 */
public class CallListAdapter extends BaseRecyAdapter<Group> {

    public CallListAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.call_fragment_item, null);
        return new CallListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        CallListViewHolder viewHolder = (CallListViewHolder) holder;
        Group groupData = getItem(position);
        if(!TextUtils.isEmpty(groupData.getGroupName())){
            viewHolder.tv_name.setText(groupData.getGroupName());
        }else{
            viewHolder.tv_name.setText(groupData.getGrouTel());
        }
        String lockGroups = (String) UctClientApi.getUserData(SETTINGS_LOCK_GROUP, "");
        String[] lockGroup = lockGroups.split(",");
        boolean isFind = false;
        for (int i = 0; i < lockGroup.length; i++) {
            String s = lockGroup[i];
            if(groupData.getGrouTel().equals(s)){
                isFind = true;
                viewHolder.iv_lock.setVisibility(View.VISIBLE);
                break;
            }
        }
        if (!isFind){
            viewHolder.iv_lock.setVisibility(View.GONE);
        }
    }

    public class CallListViewHolder extends BaseViewHolder {
        private final MarqueeTextView tv_name;
        private final ImageView iv_unfold,iv_lock;
        private final TextView tv_recentTalkTime;

        public CallListViewHolder(final View itemView) {
            super(itemView);
            tv_name = ((MarqueeTextView) itemView.findViewById(R.id.tv_title_item));
            iv_lock = (ImageView) itemView.findViewById(R.id.iv_lockItem);
            iv_unfold = ((ImageView) itemView.findViewById(R.id.iv_unfold));
            tv_recentTalkTime = ((TextView) itemView.findViewById(R.id.tv_recentTalkTime));
            /**
             * 展开/回收
             */
            iv_unfold.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onUnfoldClickListener != null){
                        onUnfoldClickListener.onClick(v,itemView,getLayoutPosition());
                    }
                }
            });
        }
    }

    /**
     * 展开图标点击事件
     */
    private OnUnfoldClickListener onUnfoldClickListener;
    public void setOnUnfoldClickListener(OnUnfoldClickListener onUnfoldClickListener) {
        this.onUnfoldClickListener = onUnfoldClickListener;
    }
    public interface OnUnfoldClickListener{
        void onClick(View view,View itemView,int position);
    }
}
