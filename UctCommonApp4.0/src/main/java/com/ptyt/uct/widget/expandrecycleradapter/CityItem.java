package com.ptyt.uct.widget.expandrecycleradapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.widget.LineProgressView;

import java.text.DecimalFormat;

import de.greenrobot.event.EventBus;

/**
 * @Description:
 * @Date: 2018/1/17
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class CityItem extends AbstractExpandableAdapterItem{

    private TextView mName;
    private ImageView iv_mapDownload;
    private TextView tv_size;
    private LineProgressView lineProgress;
    private Context mContext;
    private TextView tv_finished;

    @Override
    public int getLayoutResId() {
        return R.layout.item_department;
    }

    @Override
    public void onBindViews(View root) {
        mName = (TextView) root.findViewById(R.id.tv_name);
        iv_mapDownload = ((ImageView) root.findViewById(R.id.iv_mapDownload));
        tv_size = ((TextView) root.findViewById(R.id.tv_size));
        lineProgress = ((LineProgressView) root.findViewById(R.id.lineProgress));
        tv_finished = ((TextView) root.findViewById(R.id.tv_finished));
    }

    public CityItem(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(Object model, int position) {
        if (model instanceof OfflineMapCity) {
            final OfflineMapCity city = (OfflineMapCity) model;
            mName.setText(city.getCity());
            String size = new DecimalFormat("#.0").format(city.getSize() / (1024 * 1024.0));
            tv_size.setText("地图"+size+"MB");
            PrintLog.i("getcompleteCode="+city.getcompleteCode()+"  city.getCity()="+city.getCity() +"  city.getCode()="+city.getCode());
            iv_mapDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //按照citycode下载
                    PrintLog.i("city.getCity()="+city.getCity() +"  city.getCode()="+city.getCode());
                    lineProgress.setVisibility(View.VISIBLE);
                    OfflineMapDownloadUtils.downloadMap(mContext, lineProgress, city.getCode(), new OnLoadFinishedListener() {
                        @Override
                        public void onSucceed() {
                            PrintLog.e("onSucceed()");
                            tv_finished.setText("已下载");
                            lineProgress.setVisibility(View.GONE);
                            iv_mapDownload.setVisibility(View.GONE);
                            //通知下载管理增加已下载item
                            EventBus.getDefault().post(new EventBean(ConstantUtils.ACTION_OFFLINE_MAP_DOWNLOAD_FINISH,city));
                        }

                        @Override
                        public void unRAR() {
                            tv_finished.setText("解压中");
                        }

                        @Override
                        public void waiting() {
                            tv_finished.setText("等待连接");
                        }

                        @Override
                        public void pause() {
                            tv_finished.setText("已暂停");
                        }

                        @Override
                        public void loading() {
                            tv_finished.setText("下载中");
                            iv_mapDownload.setVisibility(View.GONE);
                        }
                    },true);
                }
            });

            if(city.getcompleteCode() == 100){
                tv_finished.setText("已下载");
                lineProgress.setVisibility(View.GONE);
                iv_mapDownload.setVisibility(View.GONE);
            }else {
                tv_finished.setText("");
                iv_mapDownload.setVisibility(View.VISIBLE);
                if(city.getcompleteCode() == 0){
                    lineProgress.setVisibility(View.GONE);
                }else {
                    lineProgress.onUpdate(city.getcompleteCode(),100);
                    lineProgress.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onExpansionToggled(boolean expanded) {

    }
}
