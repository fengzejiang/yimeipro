package com.yimeinew.modelInterface;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;

/**
 * 包装通用设计思路
 */
public interface CommPackView extends BaseView{

    //预留两个通用
    public void commCallBack(Boolean bok, JSONObject info, String error, int key);
    public void commCallBack(Boolean bok, JSONObject info, String error, String key);
    //通过Tray查询相关信息，包括获取工单信息，这是第一次去获取
    public void getTrayAndMoCallBack(Boolean bok,  JSONObject info,String error, int key);
    //获取工单
    public String getCurrMO();
    //通过喷码信息去获取信息
    public void getMarkingCallBack(Boolean bok, JSONObject headinfo,JSONObject info, String error, int key);
    //保存表头,第一次保存后面不用。
    public void saveMoCallBack(Boolean bok, JSONObject headinfo,JSONObject info, String error, int key);
    //保存表身
    public void saveMarkingCallBack(Boolean bok,JSONObject headinfo, JSONObject info, String error, int key);
    //更新表头数据
    public void updateCallBack(Boolean bok,JSONObject info, String error, int key);
    //通过主键重新获取包装信息
    public void getPackInfoCallBack(Boolean bok,JSONObject headinfo, JSONArray info, String error, int key);
    //通过工单号去获取已包装信息
    public void getMoInfoCallBack(Boolean bok,  JSONObject info,String error, int key);
    //调用打印功能
    public void getPrintLableCallBack(Boolean bok,  JSONObject info,String error, int key);
    //通过主键重新获取包装信息
    public void getABPackInfoCallBack(Boolean bok,JSONObject aheadinfo, JSONArray ainfo,JSONObject bheadinfo, JSONArray binfo, String error, int key);
}
