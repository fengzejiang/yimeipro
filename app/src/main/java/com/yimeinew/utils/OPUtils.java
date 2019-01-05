package com.yimeinew.utils;

import android.util.Log;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.model.impl.BaseModel;
import com.yimeinew.network.response.ResponseTransformer;

import java.util.HashMap;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/12 10:29
 */
public class OPUtils extends Thread{
    @Override
    public void run() {
        getOPs();
    }

    private void getOPs(){
        HashMap<String,String> params = CommonUtils.getAsistsReqHashMap();
        params.put(CommCL.PARAM_ASSIST_FLD, CommCL.AID_All_OP);
        params.put(CommCL.PARAM_SIZE_FLD, "9999");

        BaseModel.doServer(params).compose(ResponseTransformer.handleResult()).subscribe(rtnList->{
            Log.i("OPUtils",rtnList.toJSONString());
            if(rtnList.getInteger(CommCL.RTN_ID)==0){
                getOPtoCache((JSONObject) rtnList.get(CommCL.RTN_DATA));
            }
        });
    }

    private void getOPtoCache(JSONObject data) {
        JSONObject pageInfo = data.getJSONObject(CommCL.RTN_DATA);
        JSONArray oplist = pageInfo.getJSONArray(CommCL.RTN_VALUES);
        for(int i=0;i<oplist.size();i++){
            JSONObject op = oplist.getJSONObject(i);
            CommonUtils.cacheKeyValue(op.getString("sal_no"),op.getString("name"));
        }
    }

}
