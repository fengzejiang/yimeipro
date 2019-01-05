package com.yimeinew.network.request;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.network.response.Response;
import com.yimeinew.utils.CommCL;
import io.reactivex.Observable;


import retrofit2.http.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/7 16:07
 */
public interface Request {

    @POST(CommCL.API)
    Observable<Response<JSONObject>> login(@QueryMap Map<String,String> requestBody);

    @POST(CommCL.API)
    Observable<Response<JSONObject>> getAssistServer(@QueryMap Map<String,String> requestBody);

    @POST(CommCL.API)
    Observable<Response<JSONObject>> getServerTime(@QueryMap Map<String,String> requestBody);
    @POST(CommCL.API)
    Observable<Response<JSONObject>> saveData(@QueryMap HashMap<String, String> params);
    @POST(CommCL.API)
    Observable<Response<JSONObject>> commonService(@QueryMap HashMap<String, String> params);
}
