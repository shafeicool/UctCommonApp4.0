package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.uct.bean.ContactUser;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.utils.StrUtils;

/**
 * @Description:
 * @Date: 2017/8/29
 * @Author: ShaFei
 * @Version:V1.0
 */

public class ContactAdapter extends BaseRecyAdapter<ContactUser> {


    private static final int COMMON_TYPE = 0, INDEX_TYPE = 1;
    private Context mContext;

    public ContactAdapter(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == COMMON_TYPE) {
            return new GMemberViewHolder(inflater.inflate(R.layout.member_list_item, null));
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
        } else {
            final IndexViewHolder viewHolder = (IndexViewHolder) holder;
            viewHolder.tv_index.setText(contactUser.getNumber());
        }
    }

    public class IndexViewHolder extends RecyclerView.ViewHolder {
        private final TextView tv_index;

        public IndexViewHolder(View itemView) {
            super(itemView);
            tv_index = ((TextView) itemView.findViewById(R.id.tv_index));

        }
    }

    public class GMemberViewHolder extends BaseViewHolder {
        private final TextView tv_username, tv_userTel;
        private final ImageView iv_avatar;
        private final ImageView iv_checkState;

        public GMemberViewHolder(View itemView) {
            super(itemView);
            PrintLog.i("GMemberViewHolder()");
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(layoutParams);
            tv_username = ((TextView) itemView.findViewById(R.id.tv_username));
            tv_userTel = (TextView) itemView.findViewById(R.id.tv_userTel);
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

}
