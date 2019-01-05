package com.yimeinew.model.impl;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.data.BindInfo;
import com.yimeinew.network.response.Response;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import io.reactivex.Observable;

import java.util.HashMap;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/9 17:28
 * 用于和服务段交互，获取数据
 */
public class UnbindingModel extends BaseModel{
    public UnbindingModel(){}


    public Observable<Response<JSONObject>> getUnBindBatchInfoBySid(String sid1){
        String cont = "~zcno='31' and isnull(mbox,'')<>'' and sid1='"+sid1+"'";
        HashMap<String,String> params = CommonUtils.getAsistsReqHashMap();
        params.put(CommCL.PARAM_ASSIST_FLD, CommCL.AID_QJ_BOX_QUERY);
        params.put(CommCL.PARAM_CONT_FLD, cont);
        return doServer(params);
    }

    public Observable<Response<JSONObject>> saveData(BindInfo unBindInfo) {
        JSONObject jsonObject = CommonUtils.getJsonObjFromBean(unBindInfo);
        jsonObject.put(CommCL.SAVE_DATA_STATE,"3");
        HashMap<String,String> params = CommonUtils.saveDataMap(jsonObject.toJSONString(),CommCL.CELL_ID_D0090WEB);
       return saveData(params);
    }

    public Observable<Response<JSONObject>> unBindBox(BindInfo unBindInfo) {
        return bindBox(unBindInfo.getMbox(),unBindInfo.getSid1(),unBindInfo.getZcno(),false);
    }
}
