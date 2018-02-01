package com.ptyt.uct.widget.mapcluster;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.LruCache;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.AlphaAnimation;
import com.amap.api.maps.model.animation.Animation;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by yiyi.qi on 16/10/10.
 * 整体设计采用了两个线程,一个线程用于计算组织聚合数据,一个线程负责处理Marker相关操作
 */
public class ClusterOverlay implements AMap.OnCameraChangeListener,
        AMap.OnMarkerClickListener {
    private AMap mAMap;
    private Context mContext;
    private List<RegionItem> mClusterItems;
    private List<Cluster> mClusters;
    private int mClusterSize;
    private ClusterCallBack clusterCallBack;
    private ClusterRender mClusterRender;
    public List<Marker> mAddMarkers = new ArrayList<Marker>();
    private double mClusterDistance;
    private LruCache<String, BitmapDescriptor> mLruCache;
    private HandlerThread mMarkerHandlerThread = new HandlerThread("addMarker");
    private HandlerThread mSignClusterThread = new HandlerThread("calculateCluster");
    private Handler mMarkerhandler;
    private Handler mSignClusterHandler;
    private float mPXInMeters;
    private boolean mIsCanceled = false;
    private float zoom;

    /**
     * 构造函数
     *
     * @param amap
     * @param clusterSize 聚合范围的大小（指点像素单位距离内的点会聚合到一个点显示）
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
    public ClusterOverlay(AMap amap, int clusterSize, Context context) {
        this(amap, null, clusterSize, context);
    }

    /**
     * 构造函数,批量添加聚合元素时,调用此构造函数
     *
     * @param amap
     * @param clusterItems 聚合元素
     * @param clusterSize
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
    public ClusterOverlay(AMap amap, List<RegionItem> clusterItems, int clusterSize, Context context) {
//默认最多会缓存40张图片作为聚合显示元素图片,根据自己显示需求和app使用内存情况,可以修改数量
        PrintLog.i("ClusterOverlay()");
        mLruCache = new LruCache<String, BitmapDescriptor>(40) {
            protected void entryRemoved(boolean evicted, Integer key, BitmapDescriptor oldValue, BitmapDescriptor newValue) {
                oldValue.getBitmap().recycle();
            }
        };
        if (clusterItems != null) {
            mClusterItems = clusterItems;
        } else {
            mClusterItems = new ArrayList<RegionItem>();
        }
        mContext = context;
        mClusters = new ArrayList<Cluster>();
        this.mAMap = amap;
        mClusterSize = clusterSize;
        mPXInMeters = mAMap.getScalePerPixel();
        mClusterDistance = mPXInMeters * mClusterSize;
        amap.setOnCameraChangeListener(this);
        amap.setOnMarkerClickListener(this);
        initThreadHandler();
        assignClusters();
    }

    public void addAll(List<RegionItem> clusterItems){
        PrintLog.i("addAll() clusterItems.size()="+clusterItems.size());
        mClusterItems = clusterItems;
        assignClusters();
    }

    /**
     * 设置聚合点的点击事件
     *
     * @param clusterClickListener
     */
    public void setOnClusterClickListener(ClusterCallBack clusterClickListener) {
        PrintLog.e("setOnClusterClickListener()");
        clusterCallBack = clusterClickListener;
    }

    /**
     * 添加一个聚合点
     *
     * @param item
     */
    public void addClusterItem(ClusterItem item) {
        Message message = Message.obtain();
        message.what = SignClusterHandler.CALCULATE_SINGLE_CLUSTER;
        message.obj = item;
        mSignClusterHandler.sendMessage(message);
    }

    /**
     * 设置聚合元素的渲染样式，不设置则默认为气泡加数字形式进行渲染
     *
     * @param render
     */
    public void setClusterRenderer(ClusterRender render) {
        mClusterRender = render;
    }

    public void onDestroy() {
        mIsCanceled = true;
        mSignClusterHandler.removeCallbacksAndMessages(null);
        mMarkerhandler.removeCallbacksAndMessages(null);
        mSignClusterThread.quit();
        mMarkerHandlerThread.quit();
        for (Marker marker : mAddMarkers) {
            marker.remove();

        }
        mAddMarkers.clear();
        mLruCache.evictAll();
    }

    //初始化Handler
    private void initThreadHandler() {
        mMarkerHandlerThread.start();
        mSignClusterThread.start();
        mMarkerhandler = new MarkerHandler(mMarkerHandlerThread.getLooper());
        mSignClusterHandler = new SignClusterHandler(mSignClusterThread.getLooper());
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        clusterCallBack.onCameraChange(cameraPosition);

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPos) {
        PrintLog.i("onCameraChangeFinish");
//        if(AppUtils.isFastClick(300)){
//            return;
//        }
        clusterCallBack.onCameraChangeFinish(cameraPos);
        if(zoom == cameraPos.zoom){
            //return;
        }
        zoom = cameraPos.zoom;
        mPXInMeters = mAMap.getScalePerPixel();
        mClusterDistance = mPXInMeters * mClusterSize;
        assignClusters();
    }

    //点击事件
    @Override
    public boolean onMarkerClick(Marker arg0) {
        PrintLog.e("onMarkerClick()");
        if (clusterCallBack == null) {
            return true;
        }
       Cluster cluster= (Cluster) arg0.getObject();
        if(cluster!=null){
            List<RegionItem> clusterItems = cluster.getClusterItems();
            clusterCallBack.onClusterClick(arg0, clusterItems);
            return true;
        }
        return false;
    }


    /**
     * 将聚合元素添加至地图上
     */
    private void addClusterToMap(List<Cluster> clusters) {

        ArrayList<Marker> removeMarkers = new ArrayList<>();
        removeMarkers.addAll(mAddMarkers);
        AlphaAnimation alphaAnimation=new AlphaAnimation(1, 0);
        MyAnimationListener myAnimationListener=new MyAnimationListener(removeMarkers);
        //使用Iterator进行遍历时，出现过ConcurrentModificationException异常_20180118
        for (int i = 0; i < removeMarkers.size(); i++) {
            Marker marker = removeMarkers.get(i);
            marker.setAnimation(alphaAnimation);
            marker.setAnimationListener(myAnimationListener);
            marker.startAnimation();
        }
        mAddMarkers.clear();
        for (int i = 0; i < clusters.size(); i++) {
            Cluster cluster = clusters.get(i);
            addSingleClusterToMap(cluster);
        }
    }

    private AlphaAnimation mADDAnimation=new AlphaAnimation(0, 1);
    /**
     * 将单个聚合元素添加至地图显示
     *
     * @param cluster
     */
    private void addSingleClusterToMap(Cluster cluster) {
        PrintLog.e("addSingleClusterToMap()");
        RegionItem regionItem = cluster.getClusterItems().get(0);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.anchor(0.5f, 1.0f).icon(getBitmapDes(cluster)).position(regionItem.getPosition());
        markerOptions.title(regionItem.getUserTel());
        Marker marker = mAMap.addMarker(markerOptions);
        marker.setAnimation(mADDAnimation);
        marker.setObject(cluster);
        marker.startAnimation();
        cluster.setMarker(marker);
        mAddMarkers.add(marker);
        PrintLog.i("peopleAroundMap="+((MainActivity) mContext).getMapFragment().peopleAroundMap);
        ((MainActivity) mContext).getMapFragment().peopleAroundMap.put(regionItem.getUserTel(),regionItem);
        ((MainActivity) mContext).getMapFragment().markerList.add(marker);
    }



    private void calculateClusters() {
        mIsCanceled = false;
        mClusters.clear();
        LatLngBounds visibleBounds = mAMap.getProjection().getVisibleRegion().latLngBounds;
        for (RegionItem clusterItem : mClusterItems) {
            if (mIsCanceled) {
                return;
            }
            LatLng latlng = clusterItem.getPosition();
            if (visibleBounds.contains(latlng)) {
                Cluster cluster = getCluster(latlng,mClusters);
                if (cluster != null) {
                    cluster.addClusterItem(clusterItem);
                } else {
                    cluster = new Cluster(latlng);
                    mClusters.add(cluster);
                    cluster.addClusterItem(clusterItem);
                }

            }
        }

        //复制一份数据，规避同步
        List<Cluster> clusters = new ArrayList<Cluster>();
        clusters.addAll(mClusters);
        Message message = Message.obtain();
        message.what = MarkerHandler.ADD_CLUSTER_LIST;
        message.obj = clusters;
        if (mIsCanceled) {
            return;
        }
        mMarkerhandler.sendMessage(message);
    }

    /**
     * 对点进行聚合
     */
    private void assignClusters() {
        mIsCanceled = true;
        mSignClusterHandler.removeMessages(SignClusterHandler.CALCULATE_CLUSTER);
        mSignClusterHandler.sendEmptyMessage(SignClusterHandler.CALCULATE_CLUSTER);
    }

    /**
     * 在已有的聚合基础上，对添加的单个元素进行聚合
     *
     * @param clusterItem
     */
    private void calculateSingleCluster(RegionItem clusterItem) {
        PrintLog.e("calculateSingleCluster()");
        LatLngBounds visibleBounds = mAMap.getProjection().getVisibleRegion().latLngBounds;
        LatLng latlng = clusterItem.getPosition();
        if (!visibleBounds.contains(latlng)) {
            return;
        }
        Cluster cluster = getCluster(latlng,mClusters);
        if (cluster != null) {
            cluster.addClusterItem(clusterItem);
            Message message = Message.obtain();
            message.what = MarkerHandler.UPDATE_SINGLE_CLUSTER;

            message.obj = cluster;
            mMarkerhandler.removeMessages(MarkerHandler.UPDATE_SINGLE_CLUSTER);
            mMarkerhandler.sendMessageDelayed(message, 5);


        } else {

            cluster = new Cluster(latlng);
            mClusters.add(cluster);
            cluster.addClusterItem(clusterItem);
            Message message = Message.obtain();
            message.what = MarkerHandler.ADD_SINGLE_CLUSTER;
            message.obj = cluster;
            mMarkerhandler.sendMessage(message);

        }
    }

    /**
     * 根据一个点获取是否可以依附的聚合点，没有则返回null
     *
     * @param latLng
     * @return
     */
    private Cluster getCluster(LatLng latLng, List<Cluster> clusters) {
        for (Cluster cluster : clusters) {
            LatLng clusterCenterPoint = cluster.getCenterLatLng();
            double distance = AMapUtils.calculateLineDistance(latLng, clusterCenterPoint);
            if (distance < mClusterDistance / 3 && mAMap.getCameraPosition().zoom < 19) {
                return cluster;
            }
        }
        return null;
    }


    /**
     * 获取每个聚合点的绘制样式
     */
    private BitmapDescriptor getBitmapDes(Cluster cluster) {
        int num = cluster.getClusterCount();
        BitmapDescriptor bitmapDescriptor = null;
        PrintLog.e("BitmapDescriptor getBitmapDes---------------");
        if(num > 1){
            bitmapDescriptor = mLruCache.get(num+"?!");
            if (bitmapDescriptor == null) {
                PrintLog.e("View.inflate()");
                View view = View.inflate(mContext, R.layout.view_map_marker_cluster, null);
                TextView tv_peopleNum = ((TextView) view.findViewById(R.id.tv_peopleNum));
                tv_peopleNum.setText(num+"");
                bitmapDescriptor = BitmapDescriptorFactory.fromView(view);
                mLruCache.put(String.valueOf(num+"?!"), bitmapDescriptor);
            }
        }else if(num == 1){
            RegionItem regionItem = cluster.getClusterItems().get(0);
            String userTel = regionItem.getUserName();
            bitmapDescriptor = mLruCache.get(userTel);
            if(bitmapDescriptor == null){
                PrintLog.e("View.inflate 2()");
                View view = View.inflate(mContext, R.layout.map_marker, null);
                ((TextView) view.findViewById(R.id.tv_userName)).setText(regionItem.getUserName());
                bitmapDescriptor = BitmapDescriptorFactory.fromView(view);
                mLruCache.put(String.valueOf(num+"?!"), bitmapDescriptor);
            }
        }
        return bitmapDescriptor;
    }

    /**
     * 更新已加入地图聚合点的样式
     */
    private void updateCluster(Cluster cluster) {
        PrintLog.e("updateCluster()");
            Marker marker = cluster.getMarker();
            marker.setIcon(getBitmapDes(cluster));


    }


//-----------------------辅助内部类用---------------------------------------------

    /**
     * marker渐变动画，动画结束后将Marker删除
     */
    class MyAnimationListener implements Animation.AnimationListener {
        private List<Marker> mRemoveMarkers ;

        MyAnimationListener(List<Marker> removeMarkers) {
            mRemoveMarkers = removeMarkers;
        }

        @Override
        public void onAnimationStart() {

        }

        @Override
        public void onAnimationEnd() {
            for(Marker marker:mRemoveMarkers){
                marker.remove();
            }
            mRemoveMarkers.clear();
        }
    }

    /**
     * 处理market添加，更新等操作
     */
    class MarkerHandler extends Handler {

        static final int ADD_CLUSTER_LIST = 0;

        static final int ADD_SINGLE_CLUSTER = 1;

        static final int UPDATE_SINGLE_CLUSTER = 2;

        MarkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {

            switch (message.what) {
                case ADD_CLUSTER_LIST:
                    List<Cluster> clusters = (List<Cluster>) message.obj;
                    addClusterToMap(clusters);
                    break;
                case ADD_SINGLE_CLUSTER:
                    Cluster cluster = (Cluster) message.obj;
                    addSingleClusterToMap(cluster);
                    break;
                case UPDATE_SINGLE_CLUSTER:
                    Cluster updateCluster = (Cluster) message.obj;
                    updateCluster(updateCluster);
                    break;
            }
        }
    }

    /**
     * 处理聚合点算法线程
     */
    class SignClusterHandler extends Handler {
        static final int CALCULATE_CLUSTER = 0;
        static final int CALCULATE_SINGLE_CLUSTER = 1;

        SignClusterHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case CALCULATE_CLUSTER:
                    calculateClusters();
                    break;
                case CALCULATE_SINGLE_CLUSTER:
                    RegionItem item = (RegionItem) message.obj;
                    mClusterItems.add(item);
                    calculateSingleCluster(item);
                    break;
            }
        }
    }
}