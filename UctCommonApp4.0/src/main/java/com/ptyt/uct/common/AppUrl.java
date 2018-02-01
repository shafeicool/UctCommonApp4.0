package com.ptyt.uct.common;

import com.ptyt.uct.entity.OperationResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @Description: 接口地址类
 * @Date: 2017/10/31
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public interface AppUrl {

    /**
     * 获取地图周边人信息
     * 192.168.4.40:8098/dispatch_v4/AroundUsersQuery?content=
     * {"user_id": "420","latitude": 22.52,"longitude": 113.92,"radius": 10,"numbers": [421,422]}
     * @param requestBody 提交的requestBody对象
     * @return
     */
//    @GET("dispatch_v4/AroundUsersQuery")
    @GET("AroundUsersQuery")
    Call<OperationResult>getAroundUserList(@Query("content") String requestBody);
}
