package com.ptyt.uct.fragment;

import android.content.Intent;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.activity.MapSearchActivity;
import com.ptyt.uct.adapter.BasePagingAdapter;
import com.ptyt.uct.adapter.MapSearchAdapter;
import com.ptyt.uct.callback.MapCallBack;
import com.ptyt.uct.utils.ConstantUtils;

/**
 * @Description: 地图检索列表
 * @Date: 2017/8/7
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class MapSearchFragment extends BaseListFragment implements PoiSearch.OnPoiSearchListener{
    public static final int MAP_SEARCH_RESULT_CODE = 100;
    private MapSearchActivity mapSearchActivity;
    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private String currentKeyWord;
    private int currentPage = 1;//地图检索结果从第1页开始
    private MapSearchAdapter mapSearchAdapter;

    @Override
    protected BasePagingAdapter getAdapter() {
        mapSearchAdapter = new MapSearchAdapter(context);
        mapSearchAdapter.setOnItemClickListener(this);
        return mapSearchAdapter;
    }

    @Override
    protected void initData() {
        mapSearchActivity = ((MapSearchActivity) getActivity());
        //与activity的接口回调,获取搜索关键字
        mapSearchActivity.setKeyChangedListener(new MapSearchActivity.OnSearchKeyChangedListener() {
            @Override
            public void onChanged(String keyWord) {
                if(TextUtils.isEmpty(keyWord)){//为空时，清空列表
                    adapter.clear();
                    emptyLayout.setEmptyVisible(true);
                }else {
                    currentKeyWord = keyWord;
                    isRefreshMode = true;
                    currentPage = 1;
                    adapter.setState(ConstantUtils.STATE_LOAD_MORE);
                    swipeRefreshLayout.setRefreshing(true);
                    initData(currentKeyWord,currentPage);
                }
            }
        });
    }

    private void initData(String keyWord,int currentPage) {
        /**
         * 1.构造 PoiSearch.Query 对象，通过 PoiSearch.Query(String query, String ctgr, String city) 设置搜索条件
         * keyWord表示搜索字符串
         * 第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
         * /cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
         */
        /*ptyt start 解决地图搜索区域范围问题_kechuanqi_20171219*/
        AMapLocation aMapLocation = MapCallBack.getMapCallBack().aMapLocation;
        String adCode = aMapLocation.getAdCode();
        String cityAdCode = adCode;
        if(!TextUtils.isEmpty(adCode) && adCode.length() > 2){
            cityAdCode = adCode.substring(0, adCode.length() - 2)+"00";//区地区编码-> 城市地区编码
        }
        PrintLog.i("initData()   keyWord="+keyWord+"   currentPage="+currentPage + "  cityAdCode="+cityAdCode);
        //获得城市的地区编码
        query = new PoiSearch.Query(keyWord, "", cityAdCode);
        /*ptyt end*/
        query.setPageSize(10);// 设置每页最多返回多少条poiItem
        query.setPageNum(currentPage);//设置查询页码
        //2.构造 PoiSearch 对象，并设置监听
        if(poiSearch == null){
            poiSearch = new PoiSearch(context, query);
            poiSearch.setOnPoiSearchListener(this);
        }else {
            poiSearch.setQuery(query);
        }
        //3.调用 PoiSearch 的 searchPOIAsyn() 方法发送请求。
        poiSearch.searchPOIAsyn();
        //4、通过回调接口 onPoiSearched 解析返回的结果，将查询到的 POI 以绘制点的方式显示在地图上
    }


    @Override
    protected void loadMoreData() {
        super.loadMoreData();
        currentPage++;
        initData(currentKeyWord,currentPage);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        currentPage = 1;
        initData(currentKeyWord,currentPage);
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        PrintLog.i("onPoiSearched"+i+"  poiResult.getPageCount()="+poiResult.getPageCount()+"  Pois().size()="+poiResult.getPois().size()+"  isRefreshMode="+isRefreshMode+"  respondCode="+i);
        swipeRefreshLayout.setRefreshing(false);
        if(i == 1000){//相应吗1000为成功，其他为失败
            if(isRefreshMode){
                isRefreshMode = false;
                if(poiResult.getPois().size() == 0){
                    adapter.clear();
                    emptyLayout.setEmptyVisible(true);
                }else{
                    adapter.addAll(poiResult.getPois());
                    emptyLayout.setEmptyVisible(false);
                }
            }else {
                if(poiResult.getPois().size() < 10){
                    adapter.setState(ConstantUtils.STATE_NO_MORE);
                }
                adapter.addMoreData(poiResult.getPois());
            }
        }else{
            //
            emptyLayout.setEmptyVisible(true);
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void onItemClick(int pos) {
        PoiItem poiItem = mapSearchAdapter.getItem(pos);
        Intent mIntent = new Intent();
        mIntent.putExtra("latitude", poiItem.getLatLonPoint().getLatitude());
        mIntent.putExtra("longitude", poiItem.getLatLonPoint().getLongitude());
        // 设置结果，并进行传送
        getActivity().setResult(MAP_SEARCH_RESULT_CODE, mIntent);
        getActivity().finish();
    }
}
