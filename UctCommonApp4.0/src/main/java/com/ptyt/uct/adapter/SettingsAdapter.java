package com.ptyt.uct.adapter;

import android.content.Context;
import android.os.Build;
import android.preference.PreferenceActivity.Header;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.uct.service.UctClientApi;
import com.ptyt.uct.R;
import com.ptyt.uct.common.SettingsConstant;

import java.util.List;


/**
 * Title: com.ptyt.uct.adapter
 * Description:
 * Date: 2017/8/3
 * Author: ShaFei
 * Version: V1.0
 */
@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class SettingsAdapter extends BaseAdapter {

    private Context mContext;
    private List<Header> mHeaders;
    private LayoutInflater mInflater;

    public SettingsAdapter(Context mContext, List<Header> mHeaders) {
        this.mContext = mContext;
        this.mHeaders = mHeaders;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mHeaders.size();
    }

    @Override
    public Object getItem(int position) {
        return mHeaders != null ? mHeaders.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Header header = mHeaders.get(position);
        HeaderItemViewHolder itemHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_settings_root_option, parent, false);
            itemHolder = new HeaderItemViewHolder();
            itemHolder.mIcon = (ImageView) convertView.findViewById(R.id.ib_icon);
            itemHolder.mEnter = (ImageView) convertView.findViewById(R.id.ib_enter);
            itemHolder.mName = (TextView) convertView.findViewById(R.id.tv_name);
            itemHolder.mValue = (TextView) convertView.findViewById(R.id.tv_value);
            convertView.setTag(itemHolder);
        }
        itemHolder = (HeaderItemViewHolder) convertView.getTag();
        itemHolder.mIcon.setImageResource(header.iconRes);
        itemHolder.mName.setText(header.titleRes);
        if (header.id == R.id.bitrate) {
            String bitRate = UctClientApi.getUserData(SettingsConstant.SETTINGS_VIDEO_BITRATE, 512) + "";
            itemHolder.mValue.setText(bitRate);
        } else if (header.id == R.id.ringing) {
            int ringMode = (int) UctClientApi.getUserData(SettingsConstant.SETTINGS_AUDIO_RING, SettingsConstant.RING_MODE[0]);
            if (ringMode == SettingsConstant.RING_MODE[0]) {
                itemHolder.mValue.setText(mContext.getResources().getString(R.string.string_settings_ringing_ring));
            } else if (ringMode == SettingsConstant.RING_MODE[1]) {
                itemHolder.mValue.setText(mContext.getResources().getString(R.string.string_settings_ringing_shake));
            } else if (ringMode == SettingsConstant.RING_MODE[2]) {
                itemHolder.mValue.setText(mContext.getResources().getString(R.string.string_settings_ringing_ring_and_shake));
            }
        } else if (header.id == R.id.framerate) {
            String frameRate = UctClientApi.getUserData(SettingsConstant.SETTINGS_VIDEO_FRAMERATE, 15) + "";
            itemHolder.mValue.setText(frameRate + mContext.getResources().getString(R.string.string_settings_frame_rate_unit));
        } else if (header.id == R.id.resolution) {
            int width = ((Integer) UctClientApi.getUserData(SettingsConstant.SETTINGS_VIDEO_WIDTH, 640)).intValue();
            int height = ((Integer) UctClientApi.getUserData(SettingsConstant.SETTINGS_VIDEO_HEIGHT, 480)).intValue();
            if (width == SettingsConstant.RESOLUTION_WIDTH_MODE[0]) {
                itemHolder.mValue.setText(mContext.getResources().getString(R.string.string_settings_resolution_320) + "(" + width + "X" + height + ")");
            } else if (width == SettingsConstant.RESOLUTION_WIDTH_MODE[1]) {
                itemHolder.mValue.setText(mContext.getResources().getString(R.string.string_settings_resolution_640) + "(" + width + "X" + height + ")");
            } else if (width == SettingsConstant.RESOLUTION_WIDTH_MODE[2]) {
                itemHolder.mValue.setText(mContext.getResources().getString(R.string.string_settings_resolution_1280) + "(" + width + "X" + height + ")");
            } else if (width == SettingsConstant.RESOLUTION_WIDTH_MODE[3]) {
                itemHolder.mValue.setText(mContext.getResources().getString(R.string.string_settings_resolution_1920) + "(" + width + "X" + height + ")");
            } else {
                itemHolder.mValue.setText(mContext.getResources().getString(R.string.string_settings_resolution_640) + "(" + width + "X" + height + ")");
            }
        } else if (header.id == R.id.power_saving) {
            if (SettingsConstant.isPowerSavingMode()) {
                itemHolder.mValue.setText(mContext.getResources().getString(R.string.string_settings_power_saving_open));
            } else {
                itemHolder.mValue.setText(mContext.getResources().getString(R.string.string_settings_power_saving_close));
            }
        } else {
            itemHolder.mValue.setText("");
        }
        return convertView;
    }

    public class HeaderItemViewHolder {
        ImageView mIcon, mEnter;
        TextView mName, mValue;
    }


}
