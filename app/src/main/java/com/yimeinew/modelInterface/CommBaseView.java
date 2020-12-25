package com.yimeinew.modelInterface;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;

public interface CommBaseView  extends BaseView{
    public void  getAssistInfoBack(Boolean bok, JSONArray info, String error, int key);
    public void  saveDataBack(Boolean bok, JSONArray info,JSONObject record, String error, int key);
    public void  updateDataBack(Boolean bok, JSONArray info, String error, int key);
    public void  changeRecordStateBack(Boolean bok, JSONArray info, String error, int key);
    public void commCallBack(Boolean bok, JSONObject info, String error, int key);
    public void commCallBack(Boolean bok, JSONObject info, String error, String key);
}
