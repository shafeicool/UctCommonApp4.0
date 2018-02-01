package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.entity.MessageFileListEntity;
import com.ptyt.uct.utils.DateUtils;
import com.ptyt.uct.utils.FileUtils;

/**
 * Title: com.ptyt.uct.adapter
 * Description:
 * Date: 2017/5/26
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageFileListAdapter extends BaseRecyAdapter<MessageFileListEntity> {

    private Context mContext;

    public MessageFileListAdapter(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageFileListAdapter.MessageFileListHolder(inflater.inflate(R.layout.item_message_file_list, null));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MessageFileListHolder viewHolder = (MessageFileListHolder) holder;
        String fileType = getItem(position).getType();
        switch (fileType) {
            case FileUtils.WORD:
                viewHolder.fileTypeIv.setBackgroundResource(R.mipmap.icon_word);
                break;
            case FileUtils.EXCEL:
                viewHolder.fileTypeIv.setBackgroundResource(R.mipmap.icon_excel);
                break;
            case FileUtils.ZIP:
                viewHolder.fileTypeIv.setBackgroundResource(R.mipmap.icon_zip);
                break;
            default:
                viewHolder.fileTypeIv.setBackgroundResource(R.mipmap.icon_file_default);
                break;
            //            case FileUtils.OTHER:
            //                viewHolder.fileTypeIv.setBackgroundResource(0);
            //                break;
        }
        viewHolder.fileNameTv.setText(getItem(position).getSuffixName() + "");
        viewHolder.fileSizeTv.setText(FileUtils.FormatFileSize(getItem(position).getSize()) + "");
        viewHolder.fileTimeTv.setText(DateUtils.formatDatetime(getItem(position).getTime()) + "");
        if (getItem(position).getChecked()) {
            viewHolder.fileSelectIv.setBackgroundResource(R.mipmap.checkbox_selected);
        } else {
            viewHolder.fileSelectIv.setBackgroundResource(R.mipmap.checkbox_normal);
        }
    }

    public class MessageFileListHolder extends BaseViewHolder {
        private ImageView fileSelectIv, fileTypeIv;
        private TextView fileNameTv, fileSizeTv, fileTimeTv;

        public MessageFileListHolder(View itemView) {
            super(itemView);
            fileSelectIv = (ImageView) itemView.findViewById(R.id.file_select_iv);
            fileTypeIv = (ImageView) itemView.findViewById(R.id.file_type_iv);
            fileNameTv = (TextView) itemView.findViewById(R.id.file_name_tv);
            fileSizeTv = (TextView) itemView.findViewById(R.id.file_size_tv);
            fileTimeTv = (TextView) itemView.findViewById(R.id.file_time_tv);
        }
    }
}
