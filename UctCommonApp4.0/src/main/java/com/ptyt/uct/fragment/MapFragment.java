package com.ptyt.uct.fragment;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.android.uct.bean.GroupData;
import com.android.uct.bean.GroupOrganizationBean;
import com.android.uct.bean.User;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.google.gson.Gson;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.MainActivity;
import com.ptyt.uct.activity.MapSearchActivity;
import com.ptyt.uct.activity.MessageActivity;
import com.ptyt.uct.activity.OfflineMapActivity;
import com.ptyt.uct.activity.VideoCallActivity;
import com.ptyt.uct.adapter.BaseRecyAdapter;
import com.ptyt.uct.adapter.MapGroupAdapter;
import com.ptyt.uct.callback.GMemberListCallBack;
import com.ptyt.uct.callback.GroupInfoCallback;
import com.ptyt.uct.callback.MapCallBack;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.AppUrl;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.entity.Group;
import com.ptyt.uct.entity.GroupUser;
import com.ptyt.uct.entity.LoginUserEntity;
import com.ptyt.uct.entity.MapRequestUserInfo;
import com.ptyt.uct.entity.OperationResult;
import com.ptyt.uct.entity.UserGPSInfo;
import com.ptyt.uct.model.ContactDBManager;
import com.ptyt.uct.utils.ActivitySkipUtils;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.NetUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.viewinterface.IGMemberView;
import com.ptyt.uct.widget.DividerLine;
import com.ptyt.uct.widget.MarqueeTextView;
import com.ptyt.uct.widget.expandrecycleradapter.OfflineMapDownloadUtils;
import com.ptyt.uct.widget.mapcluster.ClusterCallBack;
import com.ptyt.uct.widget.mapcluster.ClusterOverlay;
import com.ptyt.uct.widget.mapcluster.RegionItem;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * @Description:
 * @Date: 2017/5/9
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class MapFragment extends BasePermissionFragment
        implements View.OnClickListener, BaseRecyAdapter.OnItemClickListener, IGMemberView, ClusterCallBack {

    //点击查看用
    public Map<String, RegionItem> peopleAroundMap;
    //marker清除用
    public List<Marker> markerList;
    private static final int AROUND_RADIUS = 0;//单位km
    private static final int DELAY_PAUSE_MAP = 111;
    private MapView mMapView;
    private AMap mMap;
    //当前自己的位置,选择的位置
    private LatLng currentLatLng,selectedLaLng;
    private View parentView;
    //左侧地图操作工具父view
    private View ll_mapControlTool;
    private TextView tv_userNameSelected;
    private TextView tv_addressSelected;
    //底部pupWindow的view
    private View view_popWindow;
    //当前地图选中的用户
    private RegionItem currentSelectedUser;
    //地图自定义工具图标随底部popupWindow的动画
    private ObjectAnimator anim_up, anim_down;
    private RecyclerView recyclerView_nearby;
    private View view_groupWindow;
    private ImageView iv_center_location;
    //中心marker的动画
    private AnimatorSet animator;

    public PopupWindow getPpw_mapUserInfo() {
        return ppw_mapUserInfo;
    }

    //化功能展开的popupWindow的view
    private PopupWindow ppw_mapUserInfo;
    //右下组选择
    private PopupWindow ppw_groupWindow;
    private MapGroupAdapter mapGroupAdapter;
    //当前选中的组id
    private GroupData selectedGroupData = null;
    private List<GroupData> groupDatas;
    private GeocodeSearch geocoderSearch;
    private MarqueeTextView tv_currentChooseGroup;
    private boolean isFirst;
    private BitmapDrawable popBitmapDrawable1;
    private BitmapDrawable popBitmapDrawable2;
    //地图是否在可见状态，用于判断当可见时定时更新周边人信息
    private boolean mMapViewIsOnResume = false;
    //定位次数
    private short locationNum;
    private TextView tv_centerPointLocation;
    private ImageView iv_compass;
    private ValueAnimator compassValueAnimator;
    //
    private int virtualBarHeigh;
    //周边人数据
    private String resultContent;
    private View view_center_nav;
    //中间点经纬度
    private LatLng centerLatLng;
    private TextView tv_peopleNum;
    private CameraPosition currentCameraPosition;

    public MapDialogFragment getMapDialogFragment() {
        return mapDialogFragment;
    }

    private MapDialogFragment mapDialogFragment;
    private Call<OperationResult> aroundUserListCall;
    //用户区分地图底部ppw.dismiss()时地图大marker的显示隐藏
    private int markerTag;

    public List<RegionItem> getClusterItems() {
        return clusterItems;
    }

    private List<RegionItem> clusterItems;

    @Override
    protected int setLayoutId() {
        return R.layout.fragment_map;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void initView(View view) {
        super.initView(view);
        PrintLog.i("initView");
        //离线地图保存的路径
        MapsInitializer.sdcardDir = OfflineMapDownloadUtils.OFFLINE_MAP_DIR;
        //获取地图控件引用
        isFirst = true;
        mMapView = (MapView) view.findViewById(R.id.mapView);
        parentView = view.findViewById(R.id.parentView);
        ll_mapControlTool = view.findViewById(R.id.ll_mapControlTool);
        tv_currentChooseGroup = ((MarqueeTextView) view.findViewById(R.id.tv_currentChooseGroup));
        tv_centerPointLocation = ((TextView) view.findViewById(R.id.tv_centerPointLocation));
        iv_compass = ((ImageView) view.findViewById(R.id.iv_compass));
        view_center_nav = view.findViewById(R.id.tv_center_nav);
        iv_center_location = ((ImageView) view.findViewById(R.id.iv_center_location));
        view_center_nav.setOnClickListener(this);
        iv_compass.setOnClickListener(this);
        view.findViewById(R.id.rl_nearby).setOnClickListener(this);
        view.findViewById(R.id.iv_search).setOnClickListener(this);
        view.findViewById(R.id.iv_location).setOnClickListener(this);
        view.findViewById(R.id.iv_zoomIn).setOnClickListener(this);
        view.findViewById(R.id.iv_zoomOut).setOnClickListener(this);
        view.findViewById(R.id.cv_downloadOfflineMap).setOnClickListener(this);
        //此方法必须重写,否则不显示
        mMapView.onCreate(savedInstanceState);
        mMap = mMapView.getMap();
        initMap();
        //组成员信息绑定回调
        GMemberListCallBack.getInstance().bindView(this);
        initWindowView();
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void initWindowView() {
        virtualBarHeigh = getVirtualBarHeigh();
        if(virtualBarHeigh > 0){//有虚拟按键
            view_popWindow = View.inflate(getActivity(), R.layout.ppw_map_detail_info_style2, null);
            tv_addressSelected = (TextView) view_popWindow.findViewById(R.id.tv_addressSelected);
        }else{
            view_popWindow = View.inflate(getActivity(), R.layout.ppw_map_detail_info, null);
            tv_addressSelected = (MarqueeTextView) view_popWindow.findViewById(R.id.tv_addressSelected);
        }
        tv_userNameSelected = (TextView) view_popWindow.findViewById(R.id.tv_userNameSelected);
        view_popWindow.findViewById(R.id.iv_audio).setOnClickListener(this);
        view_popWindow.findViewById(R.id.iv_message).setOnClickListener(this);
        view_popWindow.findViewById(R.id.iv_video).setOnClickListener(this);
        view_popWindow.findViewById(R.id.iv_upload).setOnClickListener(this);
        view_popWindow.findViewById(R.id.iv_navigation).setOnClickListener(this);
        int distanceY = getActivity().getResources().getDimensionPixelOffset(R.dimen.y238);
        anim_up = ObjectAnimator.ofFloat(ll_mapControlTool, "translationY", 0, -distanceY);
        anim_down = ObjectAnimator.ofFloat(ll_mapControlTool, "translationY", -distanceY, 0);
        anim_up.setDuration(400);
        //锸时器
        anim_up.setInterpolator(new OvershootInterpolator());
        anim_down.setDuration(200);
        //初始化右下侧的组呼选择框
        view_groupWindow = View.inflate(getActivity(), R.layout.ppw_map_group, null);
        recyclerView_nearby = ((RecyclerView) view_groupWindow.findViewById(R.id.recyclerView_nearby));
        recyclerView_nearby.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView_nearby.setHasFixedSize(true);
        //设置RecyclerView的分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0x99DEE8F5);
        recyclerView_nearby.addItemDecoration(dividerLine);
        mapGroupAdapter = new MapGroupAdapter(mContext);
        recyclerView_nearby.setAdapter(mapGroupAdapter);
        mapGroupAdapter.setOnItemClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEvent(EventBean eventBean) {
        String action = eventBean.getAction();
        if (action.equals(ConstantUtils.ACTION_LOCATION_CHANGE)) {
            AMapLocation aMapLocation = MapCallBack.getMapCallBack().aMapLocation;
            if(aMapLocation.getLatitude() == 0 || aMapLocation.getLongitude() == 0){
                PrintLog.i("未获取到经纬度信息");
                return;
            }
            currentLatLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
            // 设置当前地图显示为当前位置
            UctClientApi.uploadLocation(aMapLocation);
            if (isFirst) {//定位改变则刷新，此处可更改策略
                //下载当前城市离线地图
//                OfflineMapDownloadUtils.downloadMap(mContext,null,aMapLocation.getCity(),null,false);
                mMap.moveCamera(CameraUpdateFactory.changeTilt(20));//倾斜度20
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
                isFirst = false;
                //初始化组获取组
                groupDatas = GroupInfoCallback.getInstance().getmGroupList();
                if(groupDatas.size() > 0 && groupDatas.get(0)!= null){
                    if(selectedGroupData!=null){
                        tv_currentChooseGroup.setText(selectedGroupData.groupName+"");
                        UctClientApi.QueryAllGListFormIdx(selectedGroupData);
                    }
                }
            }
            locationNum++;
            if(selectedGroupData == null || TextUtils.isEmpty(selectedGroupData.groupId) || (ppw_mapUserInfo != null && ppw_mapUserInfo.isShowing()) ){
                return;
            }
            if(mMapViewIsOnResume && locationNum % 2 == 0){//更新周边人数据策略：地图显示且频率为定位频率的1/2
                tv_currentChooseGroup.setText(selectedGroupData.groupName);
                UctClientApi.QueryAllGListFormIdx(selectedGroupData);
            }
        }
        if (action.equals(ConstantUtils.ACTION_GROUP_INFO_REFRESH)) {//组信息更新
            PrintLog.i("onEvent() 组信息更新");
            //初始化组获取组
            groupDatas = GroupInfoCallback.getInstance().getmGroupList();
            if (groupDatas != null) {
                mapGroupAdapter.addAll(groupDatas);
            }
            //默认显示第一个组数据
            String groupId0 = groupDatas.get(0).groupId;
            if(TextUtils.isEmpty(groupId0)){return;}
            if (selectedGroupData == null) {
                selectedGroupData = groupDatas.get(0);
            }else if (!groupId0.equals(selectedGroupData.groupId)) {
                selectedGroupData = groupDatas.get(0);
                //订阅组信息数据
                tv_currentChooseGroup.setText(selectedGroupData.groupName);
                UctClientApi.QueryAllGListFormIdx(selectedGroupData);
            }
        } else if (action.equals(ConstantUtils.ACTION_FRAGMENT_CHANGED)) {
            if (mMapView == null) {
                return;
            }
            if (MainActivity.currentFragmentPosition != 2) {
                mMapView.onPause();
                mMapViewIsOnResume = false;
            } else {
                mMapView.onResume();
                mMapViewIsOnResume = true;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    protected void initData() {
        super.initData();
        PrintLog.i("initData()");
        //marker集合
        markerList = new ArrayList<>();
        //周围人数据map集合
        peopleAroundMap = new HashMap<>();
        //放在peopleAroundMap后面避免peopleAroundMap为空
        EventBus.getDefault().register(this);
    }

    /**
     * 组成员列表回调，不包括子组 UctClientApi.QueryGListFormIdx时回调
     */
    @Override
    public void onLoadSucceed(List<GroupData> groupList, List<User> userList) {}

    /**
     * 该组内所有成员回调,包括子组 UctClientApi.QueryAllGListFormIdx时回调
     * 订阅所有的组
     *
     * @param list
     */
    @Override
    public void onAllUserLoadSucceed(List<GroupOrganizationBean> list, ArrayList<String> userIds) {
        PrintLog.i("allUserList.size()=" + userIds.size());
        if (userIds == null || userIds.size() < 1) {
            ToastUtils.getToast().showMessageShort(getActivity(), mContext.getString(R.string.string_map_prompt1), -1);
            clearMapMarker();
            resultContent = null;
            return;
        }
        updatePeopleAround(userIds, AROUND_RADIUS);
    }

    /**
     * 获取周围在线成员位置信息
     *
     * @param userIds  组id
     * @param distance 距离（km）
     * @return
     */
    private void updatePeopleAround(List<String> userIds, int distance) {
        if(currentLatLng == null){
            ToastUtils.getToast().showMessageShort(mContext,getString(R.string.map_no_current_position),-1);
            return;
        }
        if(ppw_mapUserInfo != null && ppw_mapUserInfo.isShowing()){return;}//避免详情显示时更新图标
        // 1.获取在线用户
        if (userIds != null && userIds.size() > 0) {
            //获取在线用户
            LoginUserEntity loginUserBean = LoginUserEntity.getUserData();
            // ucp ip待修改
            String loginIp = AppContext.getAppContext().getLoginIp();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://" + loginIp + ":8098/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            AppUrl appUrl = retrofit.create(AppUrl.class);
            MapRequestUserInfo body = new MapRequestUserInfo(loginUserBean.getUsername(), currentLatLng.latitude, currentLatLng.longitude, distance, userIds);
            final String json = (new Gson()).toJson(body);
            aroundUserListCall = appUrl.getAroundUserList(json);
            aroundUserListCall.enqueue(new Callback<OperationResult>() {
                        @Override
                        public void onResponse(Call<OperationResult> call, Response<OperationResult> response) {
                            OperationResult result = response.body();
                            if (result != null && !TextUtils.isEmpty(result.resultContent)) {
                                if(result.resultContent.equals(resultContent)){
                                    return;
                                }else{
                                    resultContent = result.resultContent;
                                }
                                Gson gson = new Gson();
                                MapRequestUserInfo mapRequestUserInfo = gson.fromJson(resultContent, MapRequestUserInfo.class);
                                if (mapRequestUserInfo != null) {
                                    //1.清除之前图标数据
                                    clearMapMarker();
                                    addMarkerOnMap(mapRequestUserInfo.getInfos());
                                }
                            }
                            PrintLog.i("response.body()=" + result);
                        }

                        @Override
                        public void onFailure(Call<OperationResult> call, Throwable t) {
                            if(call.isCanceled()){
                                PrintLog.i("response is cancel");
                            }else{
                                PrintLog.i(t.getMessage());
                            }
                        }
                    });
        }
    }

    private Marker lastSelectedMarker, lastSelectedBigMarker;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void initEvent() {
        super.initEvent();
        mMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                if (mMapView == null) {
                    return;
                }
                if (MainActivity.currentFragmentPosition != 2) {
                    mMapView.onPause();
                    mMapViewIsOnResume = false;
                } else {
                    mMapView.onResume();
                    mMapViewIsOnResume = true;
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void doSingleMarkerClick(Marker marker){
        PrintLog.i("marker.getTitle = " + marker.getTitle());
        if (TextUtils.isEmpty(marker.getTitle())) {
            return;
        }
        if (lastSelectedMarker != null) {
            lastSelectedMarker.setVisible(true);
        }
        if (lastSelectedBigMarker != null) {
            lastSelectedBigMarker.remove();
        }
        marker.setVisible(false);
        lastSelectedMarker = marker;
        //取消之前的选中图标
        RegionItem regionItem = peopleAroundMap.get(marker.getTitle());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(regionItem.getPosition());
        markerOptions.title(regionItem.getUserName());
        markerOptions.visible(true);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_map_other_position_selected));
        markerOptions.icon(bitmapDescriptor);
        lastSelectedBigMarker = mMap.addMarker(markerOptions);
        showMapDetailInfo(0, parentView, regionItem);
    }
    /**
     * 反地理编码
     * @param latLng
     *
     */
    private String formatAddress = "";
    private void latLng2Address(final CameraPosition cameraPosition) {
        LatLng latLng = cameraPosition.target;
        if(latLng == null){return;}
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(latLng.latitude, latLng.longitude), 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
        geocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {

            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
                if(rCode == 1000){
                    RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
                    formatAddress = regeocodeAddress.getFormatAddress();
                    String cityName = regeocodeAddress.getCity();
                    if(cameraPosition.zoom > 11){
//                        OfflineMapDownloadUtils.downloadMap(mContext,null,cityName,null,false);
                    }
                    tv_centerPointLocation.setText(formatAddress);
                    view_center_nav.setVisibility(View.VISIBLE);
                }else{
                    tv_centerPointLocation.setText("");
                }
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private PopupWindow showMapDetailInfo(final int tag, View parent, final RegionItem user) {
        PrintLog.i("tag="+ tag + "  user.getUserTel()="+user.getUserTel());
        this.markerTag = tag;
        if(user == null){return null;}
        currentSelectedUser = user;
        selectedLaLng = user.getPosition();
        if (ppw_mapUserInfo == null) {
            ppw_mapUserInfo = new PopupWindow(view_popWindow, RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT, true);
            popBitmapDrawable1 = new BitmapDrawable();
            ppw_mapUserInfo.setBackgroundDrawable(popBitmapDrawable1);
            ppw_mapUserInfo.setOutsideTouchable(true);
            ppw_mapUserInfo.setAnimationStyle(R.style.my_popwindow_anim_style);
            ppw_mapUserInfo.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onDismiss() {
                    anim_down.start();
                    if(MapFragment.this.markerTag == 0){
                        lastSelectedBigMarker.setVisible(false);
                        lastSelectedMarker.setVisible(true);
                    }else if(lastSelectedMarker != null){
                        lastSelectedBigMarker.setVisible(false);
                        lastSelectedMarker.setVisible(true);
                    }
                }
            });
        }
        tv_userNameSelected.setText(user.getUserName() + " (" + user.getUserTel() + ")");
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(selectedLaLng.latitude, selectedLaLng.longitude), 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
        //反地理编码回调
        geocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
                if(rCode == 1000){
                    RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
                    formatAddress = regeocodeAddress.getFormatAddress();
                    tv_addressSelected.setText(formatAddress);
                }else{
                    tv_addressSelected.setText(R.string.map_search_no_result);
                }
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });
        ppw_mapUserInfo.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        view_popWindow.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!anim_up.isRunning()) {
                    anim_up.start();
                }
            }
        });

        if(tag == 1){
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(user.getPosition(), 19);
            mMap.moveCamera(cameraUpdate);
        }
        return ppw_mapUserInfo;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public PopupWindow showMapGroupWindow(Context context, View parent) {
        if (ppw_groupWindow == null) {
            ppw_groupWindow = new PopupWindow(view_groupWindow, RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT, true);
            popBitmapDrawable2 = new BitmapDrawable();
            ppw_groupWindow.setBackgroundDrawable(popBitmapDrawable2);
            ppw_groupWindow.setOutsideTouchable(true);
            ppw_groupWindow.setAnimationStyle(R.style.my_popwindow_anim_style);
        }
        groupDatas = GroupInfoCallback.getInstance().getmGroupList();
        mapGroupAdapter.addAll(groupDatas);
        /*ptyt begin 解决有虚拟按键的手机[HUAWEI]地图界面右下角[附近]的扩展菜单位置有偏差_4862_kechuanqi_20171211*/
        ppw_groupWindow.showAtLocation(parent, Gravity.BOTTOM | Gravity.RIGHT, getActivity().getResources().getDimensionPixelOffset(R.dimen.x50), getActivity().getResources().getDimensionPixelOffset(R.dimen.y104)+virtualBarHeigh);
        /*ptyt end*/
        return ppw_groupWindow;
    }

    /**获取虚拟功能键高度 */
    private int getVirtualBarHeigh() {
        int vh = 0;
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - windowManager.getDefaultDisplay().getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vh;
    }
    /**
     * 初始化地图
     */
    private MyLocationStyle myLocationStyle;
    private ClusterOverlay mClusterOverlay;
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
    private void initMap() {
        geocoderSearch = new GeocodeSearch(getActivity());
        //初始化定位蓝点样式类
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));//设置定位蓝点精度圆圈的边框颜色的方法。
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));//设置定位蓝点精度圆圈的填充颜色的方法。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER); //连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
        mMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        mMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        mMap.moveCamera(CameraUpdateFactory.changeTilt(20));//倾斜度20
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);//默认的缩放按钮
        uiSettings.setMyLocationButtonEnabled(false); //默认的定位按钮
        uiSettings.setZoomGesturesEnabled(true);//缩放手势
        //uiSettings.setCompassEnabled(true);
        uiSettings.setRotateGesturesEnabled(false);//旋转
        mClusterOverlay = new ClusterOverlay(mMap, null, ((int) mContext.getResources().getDimension(R.dimen.x160)), mContext);
        mClusterOverlay.setOnClusterClickListener(MapFragment.this);
        //下载全国离线大概图和当前城市图
        OfflineMapDownloadUtils.saveNationwideOfflineMap(mContext);
    }

    private void addMarkerOnMap(final List<UserGPSInfo> infos){
        if(infos==null || infos.size()==0){return;}
        //添加测试数据
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
            public void run() {
                List<RegionItem> items = new ArrayList();
                LatLng latLng;
                for (int i = 0; i < infos.size(); i++) {
                    UserGPSInfo userGPSInfo = infos.get(i);
                    String userTel = userGPSInfo.getDN();
                    String userNickname = ContactDBManager.getInstance(mContext).queryContactName(userTel);
                    latLng = GPS2GD(userGPSInfo.getLatitude(), userGPSInfo.getLongitude());
                    RegionItem regionItem = new RegionItem(latLng, userNickname,userTel,userGPSInfo.getEndTime());
                    items.add(regionItem);
                }
                mClusterOverlay.addAll(items);
            }
        }.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onDestroyView() {
        popBitmapDrawable1 = null;
        popBitmapDrawable2 = null;
        mapGroupAdapter = null;
        super.onDestroyView();
        PrintLog.i("onDestroyView");
        //销毁资源
        if(mClusterOverlay!=null){
            mClusterOverlay.onDestroy();
        }
        if(aroundUserListCall != null){
            aroundUserListCall.cancel();
        }
        EventBus.getDefault().unregister(this);
        //组成员信息解绑
        GMemberListCallBack.getInstance().unbindView(this);
        mMapView.onDestroy();
        mMap.clear();
        mMap = null;

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onResume() {
        super.onResume();
        PrintLog.i("mMapView.onResume()");
        if (mMapView == null) {
            return;
        }
        if (MainActivity.currentFragmentPosition == 2) {
            mMapView.onResume();
            mMapViewIsOnResume = true;
        } else {
            // 如果不在地图页面，不能立即暂停mapview，因为还有些东西没有恢复完，所以要等一会再去暂停
            mHandler.sendEmptyMessageDelayed(DELAY_PAUSE_MAP, 2000);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == DELAY_PAUSE_MAP) {
                if (MainActivity.currentFragmentPosition != 2 && mMapView != null) {
                    mMapView.onPause();
                    mMapViewIsOnResume = false;
                }
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        PrintLog.i("mMapView.onPause()");
        mMapView.onPause();
        mMapViewIsOnResume = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void doPermGrantedOfLocation() {
        super.doPermGrantedOfLocation();
        MapCallBack.getMapCallBack().mLocationClient.startLocation();
    }

    /**
     * @param v
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onClick(View v) {
        if(!NetUtils.isNetworkAvailable(mContext)){
            ToastUtils.getToast().showMessageShort(mContext,getString(R.string.net_error),-1);
            return;
        }
        switch (v.getId()) {
            case R.id.iv_search://地图搜索
                Intent searchIntent = new Intent(getActivity(), MapSearchActivity.class);
                Bundle bundle = new Bundle();
                if (currentLatLng != null) {
                    bundle.putDouble("latitude", currentLatLng.latitude);
                    bundle.putDouble("longitude", currentLatLng.longitude);
                    searchIntent.putExtras(bundle);
                    startActivityForResult(searchIntent, 100);
                }
                break;
            case R.id.iv_location://地图定位
                PrintLog.i("onClick() startLocation isFirst="+isFirst);
                //判断权限
                startReqPermOfLocation();
                isFirst = true;
                break;
            case R.id.iv_zoomIn://地图放大
                mMap.moveCamera(CameraUpdateFactory.zoomIn());
                break;
            case R.id.iv_zoomOut://地图缩小
                mMap.moveCamera(CameraUpdateFactory.zoomOut());
                break;
            case R.id.rl_nearby://附近 ,点击展开
                showMapGroupWindow(getActivity(), parentView);
                break;
            case R.id.iv_audio://语音呼叫
                startReqPermOfAudioCall();
                break;
            case R.id.iv_video://视频呼叫
                startReqPermOfVideoCall();
                break;
            case R.id.iv_upload://上传视频
                startReqPermOfVideoUpload();
                break;
            case R.id.iv_message://发送短信
                ActivitySkipUtils.intent2CallActivity(getActivity(), MessageActivity.class, -1, new GroupUser(currentSelectedUser.getUserTel(),currentSelectedUser.getUserName()));
                break;
            case R.id.iv_navigation://导航
                if(currentSelectedUser.getUserTel().equals(AppContext.getAppContext().getLoginNumber())){
                    ToastUtils.getToast().showMessageShort(mContext,mContext.getString(R.string.user_self),-1);
                    return;
                }
                setUpGaodeAppByLoca(selectedLaLng);
                break;
            case R.id.iv_compass://罗盘矫正
                //动画慢慢矫正
                startCameraAnimation();
                break;
            case R.id.tv_center_nav://中间点导航
                setUpGaodeAppByLoca(centerLatLng);
                break;
            case R.id.cv_downloadOfflineMap://离线地图下载
                //在Activity页面调用startActvity启动离线地图组件
//                startActivity(new Intent(mContext, com.amap.api.maps.offlinemap.OfflineMapActivity.class));
                startActivity(new Intent(mContext, OfflineMapActivity.class));
                break;
        }
    }

    /**
     * 权限授权完成时进行业务操作
     * @param businessTag
     */
    @Override
    public void doPermGrantedOfCallBusiness(int businessTag) {
        super.doPermGrantedOfCallBusiness(businessTag);
        ActivitySkipUtils.intent2CallActivity(getActivity(), VideoCallActivity.class, businessTag, new GroupUser(currentSelectedUser.getUserTel(),currentSelectedUser.getUserName()));
    }

    /**
     * 罗盘动画
     * @return
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startCameraAnimation() {//cameraPosition
        int tilt = (int) mMap.getCameraPosition().tilt;
        int rotation = (int) mMapView.getRotation();

        PrintLog.i("rotation="+rotation+"  tilt="+tilt + "  mMapView.getRotationX()="+mMapView.getRotationX()+"  mMapView.getRotationY()="+mMapView.getRotationY());
        if(compassValueAnimator == null){
            compassValueAnimator = ValueAnimator.ofInt(tilt, 0);
            compassValueAnimator.setDuration(300);
            compassValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator arg0) {
                    Integer animatedValue = (Integer) arg0.getAnimatedValue();
                    mMap.moveCamera(CameraUpdateFactory.changeTilt(animatedValue));
                }
            });
        }
        compassValueAnimator.start();


    }
    /**
     * 导航
     * @param destination 目的地
     */
    void setUpGaodeAppByLoca(LatLng destination){
        if(destination == null){return;}
        try {
            if(isInstallByread("com.autonavi.minimap")){//高德地图
                Intent intent = Intent.getIntent("androidamap://route?sourceApplication=softname&sname=我的位置&dlat="
                        +destination.latitude+"&dlon="+destination.longitude+"&dname="+""+"&dev=0&m=0&t=1");
                startActivity(intent);
            } else if(isInstallByread("com.baidu.BaiduMap")){//百度地图
                destination = gaode2baiduCoordinate(destination);
                Intent intent = Intent.getIntent("intent://map/direction?origin=我的位置&destination=latlng:"+destination.latitude+","+destination.longitude+"|name:东郡华城广场|A座" +
                        "&mode=driving&src=yourCompanyName|yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
                startActivity(intent);
            }else if(isInstallByread("com.tencent.map")){//腾讯地图
                Intent intent = Intent.getIntent("qqmap://map/routeplan?type=drive&from=&fromcoord=&to="+formatAddress+"&tocoord="+destination.latitude+","+destination.longitude+"&policy=1&referer=普天通用4.0");
                startActivity(intent);
            }
            else{
                ToastUtils.getToast().showMessageShort(mContext,mContext.getString(R.string.map_app_not_found),-1);
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    /**
     * 判断是否安装目标应用
     * @param packageName 目标应用安装后的包名
     * @return 是否已安装目标应用
     */
    private boolean isInstallByread(String packageName) {
        return new File("/data/data/" + packageName).exists();
    }

    /**
     * 右下侧组点击，过滤显示在线组成员位置
     *
     * @param pos      点击位置
     * @param itemView
     */
    @Override
    public void onItemClick(int pos, View itemView) {
        if (currentLatLng == null) {
            ToastUtils.getToast().showMessageShort(getActivity(), getString(R.string.string_map_prompt2), -1);
            return;
        }
        //订阅组信息数据
        if (mapGroupAdapter.getItem(pos) != null) {
            selectedGroupData = mapGroupAdapter.getItem(pos);
            mapGroupAdapter.setSelectedPos(pos);
            mapGroupAdapter.notifyDataSetChanged();
            tv_currentChooseGroup.setText(selectedGroupData.groupName);
            UctClientApi.QueryAllGListFormIdx(selectedGroupData);
        }
        ppw_groupWindow.dismiss();
    }

    /* ptyt begin, 区分组列表和组呼框中的功能菜单_kechuanqi_20171214 */
    public void showGroupData(Group group) {
        if(group.getGrouTel().equals(selectedGroupData.groupId)){return;}
        for (int i = 0; i < mapGroupAdapter.getDatas().size(); i++) {
            GroupData groupData = mapGroupAdapter.getDatas().get(i);
            if(groupData != null && groupData.groupId.equals(group.getGrouTel())){
                selectedGroupData = mapGroupAdapter.getItem(i);
                mapGroupAdapter.setSelectedPos(i);
                mapGroupAdapter.notifyDataSetChanged();
                UctClientApi.QueryAllGListFormIdx(selectedGroupData);
                tv_currentChooseGroup.setText(selectedGroupData.groupName);
                break;
            }
        }
    }
    /*ptyt end*/

    public void clearMapMarker() {
        //1.清除之前图标数据
        for (int j = 0; j < markerList.size(); j++) {
            markerList.get(j).remove();
        }
        if(mMap!=null){
            mMap.reloadMap();
            peopleAroundMap.clear();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == MapSearchFragment.MAP_SEARCH_RESULT_CODE) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            PrintLog.i("latitude=" + latitude + "   longitude=" + longitude);
            currentLatLng = new LatLng(latitude, longitude);
            // 设置当前地图显示为当前位置
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18), 1000, null);
        }
    }

    public LatLng GPS2GD(double la,double lon){
        LatLng latLng = new LatLng(la, lon);
        CoordinateConverter converter = new CoordinateConverter(mContext);
        // CoordType.GPS 待转换坐标类型
        converter.from(CoordinateConverter.CoordType.GPS);
        // 转换
        converter.coord(latLng);
        // 获取转换之后的高德坐标
        return converter.convert();
    }

    /**
     * @description  高德坐标转百度坐标（火系转百度系）
     * @param
     * @return
     */
    private LatLng gaode2baiduCoordinate(LatLng latLng) {
        double x = latLng.longitude, y = latLng.latitude;
        double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double tempLon = z * Math.cos(theta) + 0.0065;
        double tempLat = z * Math.sin(theta) + 0.006;
        return new LatLng(tempLat, tempLon);
    }

    /**
     * marker点击回调(聚合点/单点)
     * @param marker
     * @param clusterItems
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onClusterClick(Marker marker, List<RegionItem> clusterItems) {
         PrintLog.i("聚合点点击 currentCameraPosition.zoom="+currentCameraPosition.zoom);
        if(AppUtils.isFastClick()){return;}
        this.clusterItems = clusterItems;
         if(clusterItems.size() > 1){
             //当地图层级小于10时,地图进入下一层
             if(currentCameraPosition.zoom < 10){
                 mMap.animateCamera(CameraUpdateFactory.zoomIn(), null);
             }else{
                 if(mapDialogFragment == null){
                     mapDialogFragment = new MapDialogFragment();
                 }
                 mapDialogFragment.show(getFragmentManager(),"MapDialog");
             }
         }else if(clusterItems.size() == 1){
             doSingleMarkerClick(marker);
         }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if(AppUtils.isFastClick(200)){
            return;
        }
        if(view_center_nav.getVisibility() == View.VISIBLE){
            view_center_nav.setVisibility(View.GONE);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        PrintLog.i("onCameraChangeFinish() cameraPosition.zoom="+cameraPosition.zoom);
        centerLatLng = cameraPosition.target;
        this.currentCameraPosition = cameraPosition;
        latLng2Address(cameraPosition);
        if(cameraPosition.tilt == 0){
            iv_compass.setVisibility(View.GONE);
        }else{
            iv_compass.setVisibility(View.VISIBLE);
        }
        if(animator == null){
            animator = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.animator.anim_jump_show_map_center_icon);
            // 创建组合动画对象  &  加载XML动画
            animator.setTarget(iv_center_location);
        }
        if(!animator.isRunning()){
            animator.start();
        }
        //如果zoom > 11,且当前无此城市离线地图，则下载
//        OfflineMapDownloadUtils.downloadMap(mContext,null,cameraPosition,null,false);
    }

    /**
     *
     * @param item
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void onMapDialogItemClick(RegionItem item) {
        mapDialogFragment.dismiss();
        showMapDetailInfo(1, parentView, item);
    }
}


