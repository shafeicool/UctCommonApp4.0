package com.ptyt.uct.widget.mapcluster;

import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Marker;

import java.util.List;

/**
 * Created by yiyi.qi on 16/10/10.
 */

public interface ClusterCallBack {
        /**
         * 点击聚合点的回调处理函数
         *
         * @param marker
         *            点击的聚合点
         * @param clusterItems
         *            聚合点所包含的元素
         */
        void onClusterClick(Marker marker, List<RegionItem> clusterItems);

        void onCameraChange(CameraPosition cameraPosition);

        void onCameraChangeFinish(CameraPosition cameraPos);
}
