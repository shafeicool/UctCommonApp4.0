package com.ptyt.uct.widget.expandrecycleradapter;

import com.amap.api.maps.offlinemap.OfflineMapCity;

import java.util.List;

/**
 * @Description:
 * @Date: 2018/1/17
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class Province implements ExpandableListItem {
    public boolean mExpanded = false;
    public String name;
    public List<OfflineMapCity> cityList;

    @Override
    public List<?> getChildItemList() {
        return cityList;
    }

    @Override
    public boolean isExpanded() {
        return mExpanded;
    }

    @Override
    public void setExpanded(boolean isExpanded) {
        mExpanded = isExpanded;
    }
}
