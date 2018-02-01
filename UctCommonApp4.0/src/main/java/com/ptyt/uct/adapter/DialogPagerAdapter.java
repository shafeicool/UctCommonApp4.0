package com.ptyt.uct.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.VideoCallActivity;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.utils.ConstantUtils;
import static com.ptyt.uct.activity.VideoCallActivity.unAnsweredBusinessList;
import static com.ptyt.uct.utils.ConstantUtils.MEETING_CALL;

/**
 * @Description:
 * @Date: 2017/12/1
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class DialogPagerAdapter extends PagerAdapter{

    private Context mContext;
    ViewGroup.LayoutParams layoutParams2;

    public DialogPagerAdapter() {
    }

    public DialogPagerAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return unAnsweredBusinessList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View view = View.inflate(mContext, R.layout.view_upload_video_request, null);
        if(layoutParams2 == null){
            layoutParams2 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        view.setLayoutParams(layoutParams2);
        ImageView iv_uploadVideoReceive = (ImageView) view.findViewById(R.id.iv_uploadVideoReceive);
        ImageView iv_hangup = (ImageView) view.findViewById(R.id.iv_hangup);
        TextView tv_dialogStatus = (TextView) view.findViewById(R.id.tv_status_dialog);
        TextView tv_userName = (TextView) view.findViewById(R.id.tv_userName_dialog);
        TextView tv_hangup = (TextView) view.findViewById(R.id.tv_hangup);
        final EventBean eventBean = unAnsweredBusinessList.get(position);
        PrintLog.i("DialogPagerAdapter.instantiateItem() currentHUserCall PcName=" + eventBean.getPcName()+ "  unAnsweredBusinessList.size()"+ unAnsweredBusinessList.size());
        iv_uploadVideoReceive.setTag(position);
        iv_hangup.setTag(position);
        if(!TextUtils.isEmpty(eventBean.getPcName())){
            tv_userName.setText(eventBean.getPcName());
        }else {
            tv_userName.setText(eventBean.getPcCalling());
        }
        iv_hangup.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View v) {//拒绝接听，挂断
                ((VideoCallActivity) mContext).pagerDialogHangup(v);
            }
        });
        if(eventBean.getCallDirection() == 0){//如果是主叫
            iv_uploadVideoReceive.setAlpha(0.2f);
            tv_hangup.setText(R.string.string_cancel);
        }else{
            iv_uploadVideoReceive.setAlpha(1f);
            tv_hangup.setText(R.string.string_hang_up);
        }
        iv_uploadVideoReceive.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View v) {//接听
                if(eventBean.getCallDirection() == 1){
                    ((VideoCallActivity) mContext).pagerDialogAnswer(v);
                }
            }
        });

        switch (eventBean.getBusinessTag()){
            case ConstantUtils.UPLOAD_VIDEO:
                if(eventBean.getCallDirection() == 1){//被叫
                    tv_dialogStatus.setText(mContext.getString(R.string.string_other_request_your_video));
                }else{//主叫
                    tv_dialogStatus.setText(mContext.getString(R.string.string_audio_calling));
                }
                break;
            case ConstantUtils.VIDEO_SCALL:
                if(eventBean.getCallDirection() == 1){//被叫
                    tv_dialogStatus.setText(mContext.getString(R.string.string_video_call_other_requesting));
                }else{//主叫
                    tv_dialogStatus.setText(mContext.getString(R.string.string_video_calling));
                }
                break;
            case ConstantUtils.DOWNLOAD_VIDEO:
                if(eventBean.getCallDirection() == 1){//被叫
                    tv_dialogStatus.setText(mContext.getString(R.string.string_video_download_other_requesting));
                }else{//主叫
                    tv_dialogStatus.setText(mContext.getString(R.string.string_video_upload_to_other_requesting));
                }
                break;
            case MEETING_CALL:
            case ConstantUtils.AUDIO_SCALL:
                tv_dialogStatus.setText(mContext.getString(R.string.string_audio_call_other_requesting));
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View childAt = container.getChildAt(position);
        PrintLog.i("currentHUserCall destroyItem() childAt="+childAt);
        container.removeView(((View) object));
    }

    /**
     * 需要在adapter中重载getItemPosition()，返回不为-1即可，这样，再数据改变后，adapter.notifyDataSetChanged()才走instantiateItem()
     * @param object
     * @return
     */
    @Override
    public int getItemPosition(Object object) {
        PrintLog.i("DialogPagerAdapter.getItemPosition() currentHUserCall");
        return -2;

    }
}