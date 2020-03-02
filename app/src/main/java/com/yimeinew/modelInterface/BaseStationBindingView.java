package com.yimeinew.modelInterface;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.data.EquipmentInfo;
import com.yimeinew.data.MESPRecord;

import java.util.List;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/10 9:34
 */
public interface BaseStationBindingView extends BaseView {
    /***
     * 料盒号输入框远程访问回调
     * @param bok 是否存在该料盒号
     * @param error 错误信息,或者是json字符串
     * @param key 界面上会有多个box输入框，key值代表第几个
     */
    void checkMboxCallBack(boolean bok, String error, int key);
    /***
     * 解绑批次输入框远程访问回调处理
     * @param bok 是否存在
     * @param batchInfo 如果存在，返回JSON格式的数据
     * @param error 如果不存在返回错误信息
     */
    void checkSidCallBack(boolean bok, JSONObject batchInfo, final String error);

    /***
     * 设备号远程访问回调处理
     * @param bok 是否存在
     * @param sbInfo 设备信息
     * @param error 如果不存在返回错误信息
     */
    void checkSbIdCallBack(boolean bok, EquipmentInfo sbInfo, final String error);

    void checkRecordCallBack(boolean bok, JSONObject sbInfo, final String error);

    /***
     * 获取当前工单号
     * @return
     */
    String getCurrMO();
    List<JSONObject> getDataList();
    /***
     * 保存生产记录成功返回
      * @param bok  是否成功
     * @param record 如果成功，返回生产记录，反之，返回null
     * @param error 保存失败，返回错误信息
     */
    void saveRecordBack(boolean bok, Object record, String error);

    /***
     * 更改状态，返回处理
     * @param bok 是否成功
     * @param record 如果处理成功，返回当前记录
     * @param error 如果失败，返回当前错误信息
     */
    void changeRecordStateBack(boolean bok, Object record, String error);

    /***
     * 批次更改状态，返回处理
     * @param bok 是否成功
     * @param recordList 如果处理成功，返回当前记录
     * @param error 如果失败，返回当前错误信息
     */
    void changeMultiRecordStateBack(boolean bok, List<MESPRecord> recordList, String error);

    /***
     * 获取批次记录返回
     * @param bok 是否成功
     * @param recordList 获取成功,返回JSONARRAY
     * @param error 错误信息
     * @param type 类型（是哪个返回的）
     */
    void getMultiRecordBack(boolean bok, JSONArray recordList, String error, int type);

    /***
     * 获取制成发起原因
     * @param bok 是否成功
     * @param recordList 成功返回JsonArray,失败返回null
     * @param error 错误信息
     */
    void loadReasonsBack(boolean bok, Object recordList, String error);

    /***
     * 获取制成检验项目
     * @param bok 是否成功
     * @param recordList 成功返回JsonArray,失败返回null
     * @param error 错误信息
     */
    void loadCheckProjectBack(boolean bok, Object recordList, String error);

    /***
     *
     * @param bok 是否成功
     * @param o 成功返回对象
     * @param error 失败返回错误信息
     */
    abstract void checkQCBatInfoBack(boolean bok, Object o, String error);

    void commonBack(boolean bok, Object recordList, String error, int key);

}
