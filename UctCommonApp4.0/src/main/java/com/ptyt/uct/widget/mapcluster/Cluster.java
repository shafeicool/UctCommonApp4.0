package com.ptyt.uct.widget.mapcluster;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yiyi.qi on 16/10/10.
 */

public class Cluster {


    private LatLng mLatLng;
    private List<RegionItem> mClusterItems;
    private Marker mMarker;


    Cluster( LatLng latLng) {

        mLatLng = latLng;
        mClusterItems = new ArrayList<RegionItem>();
    }

    void addClusterItem(RegionItem clusterItem) {
        mClusterItems.add(clusterItem);
    }

    public int getClusterCount() {
        return mClusterItems.size();
    }



    LatLng getCenterLatLng() {
        return mLatLng;
    }

    void setMarker(Marker marker) {
        mMarker = marker;
    }

    Marker getMarker() {
        return mMarker;
    }

    public List<RegionItem> getClusterItems() {
        return mClusterItems;
    }
}
