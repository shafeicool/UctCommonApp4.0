package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.uct.bean.GroupData;
import com.ptyt.uct.R;
import com.ptyt.uct.utils.StrUtils;

/**
 * @Description:
 * @Date: 2017/7/20
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class SelectGroupMemberAdapter extends BaseRecyAdapter<GroupData> {

    public SelectGroupMemberAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MemberViewHolder(inflater.inflate(R.layout.item_selected_member, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final MemberViewHolder viewHolder = (MemberViewHolder) holder;
        /* ptyt begin, 解决当搜索号码时不能搜索出带有显示名的终端用户_4086_shafei_20170907 */
        final GroupData groupData = getItem(position);
        String number = groupData.groupId;
        String desc = groupData.groupName;
        if (StrUtils.isEmpty(desc)) {
            viewHolder.tv_selectedUserName.setText(number);
        } else {
            viewHolder.tv_selectedUserName.setText(desc);
        }
        /* ptyt end */
        viewHolder.tv_selectedUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通知fragment中选中的人员列表跟新
                if (onRemoveMemberChangedListener != null) {
                    onRemoveMemberChangedListener.onRemoveMemberChanged(groupData);
                }
            }
        });
    }

    class MemberViewHolder extends BaseViewHolder {

        private final TextView tv_selectedUserName;

        public MemberViewHolder(View itemView) {
            super(itemView);
            tv_selectedUserName = ((TextView) itemView.findViewById(R.id.tv_item_selectedUserName));
        }
    }

    private OnRemoveMemberChangedListener onRemoveMemberChangedListener;

    public void setOnRemoveMemberChangedListener(OnRemoveMemberChangedListener onRemoveMemberChangedListener) {
        this.onRemoveMemberChangedListener = onRemoveMemberChangedListener;
    }

    /**
     * 人员选中时通知fragment,接口回调
     */
    public interface OnRemoveMemberChangedListener {
        void onRemoveMemberChanged(GroupData groupData);
    }
}
