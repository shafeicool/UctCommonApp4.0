package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.entity.MessageFunctionEntity;

/**
 * Title: com.ptyt.uct.adapter
 * Description:
 * Date: 2017/5/25
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageFuctionAdapter extends BaseRecyAdapter<MessageFunctionEntity> {

    private Context mContext;

    public MessageFuctionAdapter(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageFunctionViewHolder(inflater.inflate(R.layout.fragment_message_chat_bottom_select_file, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MessageFunctionViewHolder viewHolder = (MessageFunctionViewHolder) holder;
        viewHolder.mFuncBtn.setBackgroundResource(getItem(position).getIcon());
        viewHolder.mFuncTv.setText(getItem(position).getName());
    }

    public class MessageFunctionViewHolder extends BaseViewHolder {

        private ImageView mFuncBtn;
        private TextView mFuncTv;

        public MessageFunctionViewHolder(View itemView) {
            super(itemView);
            mFuncBtn = (ImageView) itemView.findViewById(R.id.func_iv);
            mFuncTv = (TextView) itemView.findViewById(R.id.func_tv);

        }
    }
}
