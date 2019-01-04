package com.yimei.model.impl;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimei.data.MESPRecord;
import com.yimei.network.response.Response;
import com.yimei.utils.CommCL;
import com.yimei.utils.CommonUtils;
import io.reactivex.Observable;

import java.util.HashMap;

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
}
