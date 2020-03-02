package com.yimeinew.utils;


import android.util.Log;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.network.response.ResponseTransformer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * PDA获取服务端时间线程，比较服务器时间和pda时间，获取相差差额
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/23 16:01
 */
public class ServicesTimeThread extends Thread {
    public static final int GET_TIME=1000*60*60*2;
    public static final int CHECK_TIME=1000*60*53;
    public static int TEMP_TIME=0;
    public static long my_time=0;
    public static final int wucha=1000*60*10;
    public static  int sycnum=0;
    public static final String TAG_NAME = ServicesTimeThread.class.getSimpleName();
    //    public static final int GET_TIME=1000*6;//测试用
    @Override
    public void run() {
        getServiceTime();//第一次获取时间
        sycnum++;
        while (true){
            TEMP_TIME+=CHECK_TIME;
            if(TEMP_TIME>=GET_TIME) {
                Log.i(TAG_NAME,"getTime");
                getServiceTime();
                TEMP_TIME=0;
                sycnum++;
            }
            checkClock();
            try {
                Thread.sleep(CHECK_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public  boolean checkClock(){
        long app_t =0;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            app_t=format.parse(DateUtil.getCurrDateTime(ICL.DF_YMDHM)).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(Math.abs(my_time-app_t)<=wucha){
            my_time=app_t;
            return true;
        }else{
            getServiceTime();
            //System.err.println("我去服务器求时间");
            return false;
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
                    my_time=curr_time;
                }
            }
        );
    }
}
