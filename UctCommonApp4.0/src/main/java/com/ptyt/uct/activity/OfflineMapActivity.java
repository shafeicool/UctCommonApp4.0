package com.ptyt.uct.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.widget.DividerLine;
import com.ptyt.uct.widget.expandrecycleradapter.AbstractAdapterItem;
import com.ptyt.uct.widget.expandrecycleradapter.BaseExpandableAdapter;
import com.ptyt.uct.widget.expandrecycleradapter.CityItem;
import com.ptyt.uct.widget.expandrecycleradapter.Province;
import com.ptyt.uct.widget.expandrecycleradapter.ProvinceItem;
import com.ptyt.uct.widget.expandrecycleradapter.Title;
import com.ptyt.uct.widget.expandrecycleradapter.TitleItem;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

import static com.ptyt.uct.R.id.recyclerView;

/**
 * @Description:
 * @Date: 2018/1/16
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class OfflineMapActivity extends BaseActionBarActivity{

    private final int ITEM_TYPE_PROVINCE = 1;
    private final int ITEM_TYPE_CITY = 2;
    private final int ITEM_TYPE_TITLE = 3;
    private RecyclerView mRecyclerView;
    private BaseExpandableAdapter mBaseExpandableAdapter;
    private List mCompanyList;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_offline_map;
    }
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEvent(EventBean eventBean) {
        if(eventBean.getAction().equals(ConstantUtils.ACTION_OFFLINE_MAP_DOWNLOAD_FINISH)){
            OfflineMapCity city = (OfflineMapCity) eventBean.getObject();
            mBaseExpandableAdapter.addItem(1, city);
        }
    }
    @Override
    protected void initWidget() {
        mRecyclerView = ((RecyclerView) findViewById(recyclerView));
        //设置RecyclerView的分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(2);
        dividerLine.setColor(0x66DEE8F5);
        mRecyclerView.addItemDecoration(dividerLine);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        setActionBarTitle(getString(R.string.offline_map));
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrintLog.i("FAB onClick()");
                OfflineMapCity city = new OfflineMapCity();
                city.setCity("增加城市");
                mBaseExpandableAdapter.addItem(1, city);
            }
        });
    }


    @Override
    protected void initEvent() {
        super.initEvent();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initData() {
        super.initData();
        mCompanyList = new ArrayList<>();

        //构造OfflineMapManager对象
        final OfflineMapManager aMapManager = new OfflineMapManager(this, new OfflineMapManager.OfflineMapDownloadListener() {
            @Override
            public void onDownload(int status, int completeCode, String name) {}
            @Override
            public void onCheckUpdate(boolean b, String s) {
                PrintLog.e("b="+b + "  s="+s);
            }
            @Override
            public void onRemove(boolean b, String s, String s1) {}
        });
        //已下载地图管理
        Province p = new Province();
        p.name = "下载管理";
        ArrayList<OfflineMapCity> cityList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            OfflineMapCity city = new OfflineMapCity();
            city.setCity("城市"+i);
            cityList.add(city);
        }
        p.cityList = cityList;
        mCompanyList.add(p);

        //待下载列表
        mCompanyList.add(new Title("城市列表"));
        ArrayList<OfflineMapProvince> offlineMapProvinceList = aMapManager.getOfflineMapProvinceList();
        for (int i = 0; i < offlineMapProvinceList.size(); i++) {
            Province province = new Province();
            province.name = offlineMapProvinceList.get(i).getProvinceName();
            province.cityList = offlineMapProvinceList.get(i).getCityList();
            province.mExpanded = false;
            mCompanyList.add(province);
        }
        mBaseExpandableAdapter = new BaseExpandableAdapter(mCompanyList) {
            @NonNull
            @Override
            public AbstractAdapterItem<Object> getItemView(Object type) {
                int itemType = (int) type;
                switch (itemType) {
                    case ITEM_TYPE_PROVINCE:
                        return new ProvinceItem();
                    case ITEM_TYPE_CITY:
                        return new CityItem(OfflineMapActivity.this);
                    case ITEM_TYPE_TITLE://标题
                        return new TitleItem();
                }
                return null;
            }

            @Override
            public Object getItemViewType(Object t) {
                if (t instanceof Province)
                    return ITEM_TYPE_PROVINCE;
                else if (t instanceof OfflineMapCity)
                    return ITEM_TYPE_CITY;
                else if(t instanceof Title)
                    return ITEM_TYPE_TITLE;
                return -1;
            }
        };
        mRecyclerView.setAdapter(mBaseExpandableAdapter);
    }
}
