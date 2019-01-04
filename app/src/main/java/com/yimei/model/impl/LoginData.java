package com.yimei.model.impl;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimei.data.User;
import com.yimei.model.LoginModelI;
import com.yimei.network.NetWorkManager;
import com.yimei.network.response.Response;
import com.yimei.utils.CommCL;
import com.yimei.utils.CommonUtils;
import io.reactivex.Observable;

import java.util.HashMap;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/7 13:39
 */
public class LoginData {
    private LoginModelI baseModel;
    public LoginData(LoginModelI baseModel){
        this.baseModel = baseModel;
    }

    /***
     * 执行登陆操作
     * @param user 登陆用户信息
     */

    public Observable<Response<JSONObject>> doLogin(User user){
        HashMap<String,String> root  = new HashMap<>();
        root.put(CommCL.PARAMS_USRCODE,user.getUserCode());
        root.put(CommCL.PARAMS_PASSWORD,CommonUtils.Str2Base64(user.getUserPassword()));
        root.put(CommCL.PARAMS_DBID,CommCL.DB_ID);
        root.put(CommCL.PARAMS_APIID,CommCL.PARAM_VALUE_API_ID_LOGIN);
        return NetWorkManager.getRequest().login(root);
    }


    public Observable<Response<JSONObject>> getZCInfo(){
        HashMap<String,String> params = CommonUtils.getAsistsReqHashMap();
        params.put(CommCL.PARAM_ASSIST_FLD, CommCL.AID_M_PROCESS_QUERY);
        params.put(CommCL.PARAM_CONT_FLD, "");
        return NetWorkManager.getRequest().getAssistServer(params);
    }

}
