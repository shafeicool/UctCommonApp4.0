package com.ptyt.uct.widget.expandrecycleradapter;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.widget.LineProgressView;

import java.io.File;
import java.util.HashMap;

import static com.ptyt.uct.R.raw.quanguo;

/**
 * @Description:
 * @Date: 2018/1/19
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class OfflineMapDownloadUtils {

    private static OfflineMapManager aMapManager;
    //离线地图保存路径
    public static String OFFLINE_MAP_DIR = Environment.getExternalStorageDirectory()+"/PTYT/OfflineMap";
    private static final String MAP_CONFIG_PATH="mobilemap"+File.separator+"offline_city.json";

    public static void downloadMap(Context mContext, final LineProgressView view, String cityCode, final OnLoadFinishedListener listener,boolean updateMap){
        if(TextUtils.isEmpty(cityCode)){return;}
        //如果已经下载或有旧版，且不是更新,则不用下载
//        if(!updateMap && FileUtils.fileIsExists(OFFLINE_MAP_DIR+"/data/map/"+cityName+".dat")){
        if(!updateMap && cityNameMap.get(cityCode)!=null){
            PrintLog.e("downloadMap() return cityName="+cityCode);
            return;
        }
        PrintLog.e("downloadMap() name="+cityCode);
        if (aMapManager != null) {
            aMapManager.destroy();
            aMapManager = null;
        }
        aMapManager = new OfflineMapManager(mContext, new OfflineMapManager.OfflineMapDownloadListener() {
            @Override
            public void onDownload(int status, int completeCode, String name) {
                PrintLog.i("status=" + status + "  completeCode=" + completeCode + "  name=" + name);
                if(view != null){
                    view.onUpdate(completeCode, 100);
                }
                if(listener != null){
                    switch (status){
                        case OfflineMapStatus.LOADING://下载中
                            listener.loading();
                            break;
                        case OfflineMapStatus.UNZIP://解压
                            listener.unRAR();
                            break;
                        case OfflineMapStatus.WAITING:
                            listener.waiting();
                            break;
                        case OfflineMapStatus.PAUSE:
                            listener.pause();
                            break;
                        case OfflineMapStatus.SUCCESS://成功
                            if(completeCode == 100){
                                //1.保存下载完成数据至本地
                                PrintLog.e("下载成功 name"+name);
                                cityNameMap.put(name,name);
                                listener.onSucceed();
                            }
                            break;
                    }
                }
            }

            @Override
            public void onCheckUpdate(boolean b, String s) {
                PrintLog.e("b=" + b + "  s=" + s);
            }

            @Override
            public void onRemove(boolean b, String s, String s1) {

            }
        });

        try {
            aMapManager.downloadByCityCode(cityCode);
        } catch (AMapException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存全国离线地图略图
     * @param mContext
     */
    public static void saveNationwideOfflineMap(final Context mContext) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                File f=new File(OFFLINE_MAP_DIR +"/data/map/quanguo.dat");
                if(FileUtils.fileIsExists(OFFLINE_MAP_DIR +"/data/map/quanguo.dat")){
                    return;
                }else {
                    if (!f.getParentFile().exists()) {
                        if(!f.getParentFile().mkdirs()) {
                            PrintLog.e("创建目标文件所在目录失败！");
                            return;
                        }
                    }
                    try {
                        FileUtils.saveToSDCard(mContext, quanguo, OFFLINE_MAP_DIR +"/data/map/quanguo.dat");
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static HashMap<String,String> cityNameMap;
    static {
        cityNameMap = new HashMap<>();
    }
}
