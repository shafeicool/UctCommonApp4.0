package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.uct.bean.ContactUser;
import com.ptyt.uct.R;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Date: 2017/8/29
 * @Author: ShaFei
 * @Version:V1.0
 */

public class CreateTempGroupAdapter extends BaseRecyAdapter<ContactUser> {


    private static final int COMMON_TYPE = 0, INDEX_TYPE = 1;
    private Context mContext;
    public Map<String, ContactUser> selectedUserMap;
    private int limitCounts;

    public CreateTempGroupAdapter(Context context) {
        super(context);
        this.mContext = context;
        selectedUserMap = new HashMap<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == COMMON_TYPE) {
            return new GMemberViewHolder(inflater.inflate(R.layout.item_create_temp_group, null));
        } else {
            return new IndexViewHolder(inflater.inflate(R.layout.member_index_item, null));
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final ContactUser contactUser = getItem(position);
        if (getItemViewType(position) == COMMON_TYPE) {
            final GMemberViewHolder viewHolder = (GMemberViewHolder) holder;
            /* ptyt begin, 解决当搜索号码时不能搜索出带有显示名的终端用户_4086_shafei_20170907 */
            String number = contactUser.getNumber();
            String desc = contactUser.getDesc();
            if (StrUtils.isEmpty(desc)) {
                viewHolder.tv_username.setText(number);
            } else {
                viewHolder.tv_username.setText(desc);
            }
            /* ptyt end */
            viewHolder.tv_userTel.setText(number);
            viewHolder.iv_avatar.setImageResource(R.mipmap.icon_message_person);
            //勾选图标
            if (checkableState) {
                viewHolder.iv_checkState.setVisibility(View.VISIBLE);
            }
            if (selectedUserMap.containsKey(contactUser.getNumber())) {
                viewHolder.iv_checkState.setImageResource(R.mipmap.ic_btn_check);
                viewHolder.relativeLayout.setTag(true);
            } else {
                viewHolder.iv_checkState.setImageResource(R.mipmap.chenkbox_normal);
                viewHolder.relativeLayout.setTag(false);
            }
            //            viewHolder.iv_checkState.setTag(R.id.iv_checkState, contactUser.getNumber());
            //点击时勾选图标的改变
            viewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isSelected = (boolean) v.getTag();
                    if (!isSelected) {
                        if (selectedUserMap.size() >= limitCounts) {
                            ToastUtils.getToast().showMessageLong(mContext, String.format(mContext.getResources().getString(R.string.string_create_group_prompt3), limitCounts), -1);
                            return;
                        }
                        v.setTag(true);
                        viewHolder.iv_checkState.setImageResource(R.mipmap.ic_btn_check);
                        selectedUserMap.put(contactUser.getNumber(), contactUser);
                        //通知fragment中选中的人员列表跟新
                        if (onSelectedMemberChangedListener != null) {
                            onSelectedMemberChangedListener.onSelectedMemberChanged(selectedUserMap, contactUser, true);
                        }
                    } else {
                        v.setTag(false);
                        viewHolder.iv_checkState.setImageResource(R.mipmap.chenkbox_normal);
                        selectedUserMap.remove(contactUser.getNumber());
                        //通知fragment中选中的人员列表跟新
                        if (onSelectedMemberChangedListener != null) {
                            onSelectedMemberChangedListener.onSelectedMemberChanged(selectedUserMap, contactUser, false);
                        }
                    }
                }
            });

        } else {
            final IndexViewHolder viewHolder = (IndexViewHolder) holder;
            viewHolder.tv_index.setText(contactUser.getNumber());
        }
    }

    private boolean checkableState;

    public void setCheckableState(boolean checkableState) {
        this.checkableState = checkableState;
    }

    public void setLimitCounts(int limitCounts) {
        this.limitCounts = limitCounts;
    }

    public class IndexViewHolder extends RecyclerView.ViewHolder {
        private final TextView tv_index;

        public IndexViewHolder(View itemView) {
            super(itemView);
            tv_index = ((TextView) itemView.findViewById(R.id.tv_index));

        }
    }

    public class GMemberViewHolder extends BaseViewHolder {
        private RelativeLayout relativeLayout;
        private final TextView tv_username, tv_userTel;
        private final ImageView iv_avatar;
        private final ImageView iv_checkState;

        public GMemberViewHolder(View itemView) {
            super(itemView);
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(layoutParams);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
            tv_username = ((TextView) itemView.findViewById(R.id.tv_username));
            tv_userTel = ((TextView) itemView.findViewById(R.id.tv_userTel));
            iv_avatar = ((ImageView) itemView.findViewById(R.id.iv_avatar));
            iv_checkState = ((ImageView) itemView.findViewById(R.id.iv_checkState));
        }
    }

    @Override
    public int getItemViewType(int position) {
        ContactUser item = getItem(position);
        String number = item.getNumber();
        if (number.length() > 1) {
            return COMMON_TYPE;
        }
        return INDEX_TYPE;
    }

    private OnSelectedMemberChangedListener onSelectedMemberChangedListener;

    public void setOnSelectedMemberChangedListener(OnSelectedMemberChangedListener onSelectedMemberChangedListener) {
        this.onSelectedMemberChangedListener = onSelectedMemberChangedListener;
    }

    /**
     * 人员选中时通知fragment,接口回调
     */
    public interface OnSelectedMemberChangedListener {
        void onSelectedMemberChanged(Map<String, ContactUser> groupUserMap, ContactUser contactUser, boolean isAdd);
    }


}
