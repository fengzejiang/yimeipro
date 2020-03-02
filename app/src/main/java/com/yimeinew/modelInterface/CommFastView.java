package com.yimeinew.modelInterface;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;

public interface CommFastView extends BaseView{

    public void getQuickLotBack(Boolean bok, JSONObject info, String error, int key);
    String getCurrMO();
    /***
     * 保存生产记录成功返回
     * @param bok  是否成功
     * @param record 如果成功，返回生产记录，反之，返回null
     * @param error 保存失败，返回错误信息
     */
    public void saveRecordBack(boolean bok, Object record, String error);
    public void saveMoCallBack(Boolean bok, JSONObject batchInfo, String error);
    public void saveMarkingCallBack(Boolean bok, JSONObject batchInfo, String error);
    public void getPackInfoCallBack(Boolean bok, JSONObject headinfo, JSONArray info, String error, int key);
    public void  getAssistInfoBack(Boolean bok, JSONArray info, String error, int key);
    /***
     * 更改状态，返回处理
     * @param bok 是否成功
     * @param record 如果处理成功，返回当前记录
     * @param error 如果失败，返回当前错误信息
     */
    void changeRecordStateBack(boolean bok, Object record, String error);
    void updateCallBack(boolean bok, Object record, String error, int key);

}
