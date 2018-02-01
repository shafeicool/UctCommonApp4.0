package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.entity.CallRecord;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.DateUtils;
import com.ptyt.uct.utils.StrUtils;

/**
 * Title: com.ptyt.uct.adapter
 * Description:
 * Date: 2017/6/6
 * Author: ShaFei
 * Version: V1.0
 */

public class CallRecordAdapter extends BaseRecyAdapter<CallRecord> {

    private Context mContext;
    private boolean isEditor;

    public CallRecordAdapter(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CallRecordViewHolder(inflater.inflate(R.layout.item_call_record_list, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CallRecordViewHolder viewHolder = (CallRecordViewHolder) holder;
        String name = getItem(position).getName();
        String number = getItem(position).getNumber();
        if (StrUtils.isEmpty(name)) {
            viewHolder.name_tv.setText(number);
        } else {
            viewHolder.name_tv.setText(name);
        }
        Integer isRead = getItem(position).getIsRead();
        if (isRead == null || isRead == ConstantUtils.CALL_RECORD_ALREAD) {
            viewHolder.name_tv.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_01));
        } else {
            viewHolder.name_tv.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_18));
        }
        String callTime = getItem(position).getCallTime();
        if (callTime == null) {
            viewHolder.call_time_tv.setText(R.string.record_call_not_answer);
        } else {
            viewHolder.call_time_tv.setText(callTime);
        }
        Long recordTime = getItem(position).getRecordTime();
        if (recordTime == null || recordTime == 0) {
            viewHolder.record_time_tv.setText("");
        } else {
            viewHolder.record_time_tv.setText(DateUtils.getTimePoint(recordTime) + "");
        }
        Integer type = getItem(position).getType();
        if (type == null) {
            viewHolder.type_iv.setImageResource(0);
        } else {
            switch (type) {
                case ConstantUtils.CALL_RECORD_AUDIO_CALL_IN:
                    viewHolder.type_iv.setImageResource(R.mipmap.ic_audio_call_in);
                    break;
                case ConstantUtils.CALL_RECORD_AUDIO_CALL_OUT:
                    viewHolder.type_iv.setImageResource(R.mipmap.ic_audio_call_out);
                    break;
                case ConstantUtils.CALL_RECORD_VIDEO_CALL_IN:
                    viewHolder.type_iv.setImageResource(R.mipmap.icon_video_coming);
                    break;
                case ConstantUtils.CALL_RECORD_VIDEO_CALL_OUT:
                    viewHolder.type_iv.setImageResource(R.mipmap.icon_video_exhale);
                    break;
                case ConstantUtils.CALL_RECORD_VIDEO_UPLOAD_OUT:
                    viewHolder.type_iv.setImageResource(R.mipmap.ic_video_upload);
                    break;
                case ConstantUtils.CALL_RECORD_VIDEO_UPLOAD_IN:
                    viewHolder.type_iv.setImageResource(R.mipmap.ic_video_download);
                    break;
                case ConstantUtils.CALL_RECORD_VIDEO_DOWNLOAD_IN:
                    viewHolder.type_iv.setImageResource(R.mipmap.ic_video_download);
                    break;
                default:
                    viewHolder.type_iv.setImageResource(0);
                    break;
            }
        }
        if (isEditor) {
            viewHolder.checked_iv.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewHolder.record_time_tv.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            if (getItem(position).getChecked()) {
                viewHolder.checked_iv.setBackgroundResource(R.mipmap.ic_btn_check);
            } else {
                viewHolder.checked_iv.setBackgroundResource(R.mipmap.chenkbox_normal);
            }
        } else {
            viewHolder.checked_iv.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewHolder.record_time_tv.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
    }

    public void enterEditorMode(boolean isEditor) {
        this.isEditor = isEditor;
    }

    public boolean getEditorMode() {
        return isEditor;
    }

    public class CallRecordViewHolder extends BaseViewHolder {
        private TextView name_tv;
        private TextView call_time_tv;
        private ImageView type_iv;
        private ImageView checked_iv;
        private TextView record_time_tv;

        public CallRecordViewHolder(View itemView) {
            super(itemView);
            name_tv = (TextView) itemView.findViewById(R.id.name_tv);
            call_time_tv = (TextView) itemView.findViewById(R.id.call_time_tv);
            type_iv = (ImageView) itemView.findViewById(R.id.type_iv);
            checked_iv = (ImageView) itemView.findViewById(R.id.checked_iv);
            record_time_tv = (TextView) itemView.findViewById(R.id.record_time_tv);
        }
    }
}
