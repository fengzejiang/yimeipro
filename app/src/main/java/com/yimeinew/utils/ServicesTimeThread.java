package com.yimeinew.utils;

import android.util.Log;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.network.response.ResponseTransformer;

/**
 * PDA获取服务端时间线程，比较服务器时间和pda时间，获取相差差额
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/23 16:01
 */
public class ServicesTimeThread extends Thread {
    public static final int GET_TIME=1000*60*60*2;
    public static final String TAG_NAME = ServicesTimeThread.class.getSimpleName();
//    public static final int GET_TIME=1000*6;//测试用
    @Override
    public void run() {
        while (true){
            Log.i(TAG_NAME,"getTime");
            getServiceTime();
            try {
                Thread.sleep(GET_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void getServiceTime(){
        long app_t = System.currentTimeMillis();
        CommonUtils.getServerTime().compose(ResponseTransformer.handleResult()).subscribe(
            timeJson ->{
                if(timeJson.getInteger("id")==0){
                    JSONObject serTime = (JSONObject) ((JSONObject) timeJson.get("data")).get("data");
                    long curr_time = serTime.getLong("time");
                    long tim_ce = curr_time-app_t;
                    DateUtil.TIMEDIFF = tim_ce;
                }
            }
        );
    }
}
