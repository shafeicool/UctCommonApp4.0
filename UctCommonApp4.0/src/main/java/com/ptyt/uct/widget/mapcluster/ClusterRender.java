package com.ptyt.uct.widget.mapcluster;

import com.amap.api.maps.model.BitmapDescriptor;

/**
 * Created by yiyi.qi on 16/10/10.
 */

public interface ClusterRender {
    /**
     * 根据聚合点的元素数目返回渲染背景样式
     *
     * @param cluster
     * @return
     */
    BitmapDescriptor getClusterDrawAble(Cluster cluster);
}
