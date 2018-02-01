package com.ptyt.uct.widget.mapcluster;

import com.amap.api.maps.model.LatLng;

/**
 * @Description:
 * @Date: 2017/12/20
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public interface ClusterItem {
    /**
     * 返回聚合元素的地理位置
     *
     * @return
     */
     LatLng getPosition();
}
