package com.yimeinew.modelInterface;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.data.EquipmentInfo;

public interface QuickBaseView extends BaseView{
    /***
     * 设备号远程访问回调处理
     * @param bok 是否存在
     * @param sbInfo 设备信息
     * @param error 如果不存在返回错误信息
     */
    void checkSbIdCallBack(boolean bok, EquipmentInfo sbInfo, final String error);
}
