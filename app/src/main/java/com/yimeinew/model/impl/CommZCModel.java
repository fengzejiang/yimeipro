package com.yimeinew.model.impl;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.network.response.Response;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import io.reactivex.Observable;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/18 18:19
 */
public class CommZCModel extends BaseModel {

//    public Observable<Response<JSONObject>> BindBox(String boxId,String sid1,String zcno) {
//        return bindBox(boxId,sid1,zcno,true);
//    }

    public Observable<Response<JSONObject>> saveData(MESPRecord record,String status) {
        JSONObject jsonObject = CommonUtils.getJsonObjFromBean(record);
        jsonObject.put(CommCL.SAVE_DATA_STATE,status);
        return saveData(jsonObject,CommCL.CELL_ID_D0001WEB);
    }
    public Observable<Response<JSONObject>> saveQuickData(MESPRecord record,String status) {
        JSONObject jsonObject = CommonUtils.getJsonObjFromBean(record);
        jsonObject.put(CommCL.SAVE_DATA_STATE,status);
        return saveData(jsonObject,CommCL.CELL_ID_D0071);
    }
    public Observable<Response<JSONObject>> saveQuickData2(MESPRecord record,String status) {
        JSONObject jsonObject = CommonUtils.getJsonObjFromBean(record);
        jsonObject.put(CommCL.SAVE_DATA_STATE,status);
        return saveData(jsonObject,CommCL.CELL_ID_D0073W);
    }

    /**
     * 通用数据保存
     * @param record   记录
     * @param insObject   对象定义ID
     * @return
     */
    public Observable<Response<JSONObject>> comSaveData(Object record,String insObject) {
        JSONObject jsonObject = CommonUtils.getJsonObjFromBean(record);
        jsonObject.put(CommCL.SAVE_DATA_STATE,CommCL.API_SAVE_STATUS);
        return saveData(jsonObject,insObject);
    }
    /**
     * 通用数据更新-没有测试不要用
     * @param record   记录--记录里面必须含有ID
     * @param insObject   对象定义ID
     * @return
     */
    public Observable<Response<JSONObject>> comUpdateData(Object record,String insObject) {
        JSONObject jsonObject = CommonUtils.getJsonObjFromBean(record);
        jsonObject.put(CommCL.SAVE_DATA_STATE,CommCL.API_UPDATE_STATUS);
        return updateDataAID(jsonObject,insObject);
    }
}
